package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.AktivitasUnitEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.remote.ApiClientManager
import com.example.data.remote.BootstrapResponseData
import com.example.data.remote.CompanyServerSimulator
import com.example.data.remote.KendalaJsonConverter
import com.example.data.remote.PostActivityReportRequest
import com.example.data.remote.PostHasilAktivitasRequest
import com.example.data.remote.PostRencanaKerjaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SyncLogItem(
    val timestamp: String,
    val endpoint: String,
    val method: String,
    val status: String,
    val detail: String,
    val isSuccess: Boolean
)

class HeavyTrackRepository(
    private val database: AppDatabase,
    val apiClientManager: ApiClientManager,
    val userSessionManager: com.example.data.local.UserSessionManager = com.example.data.local.UserSessionManager(apiClientManager.context)
) {
    private val unitDao = database.unitDao()
    val masterUnitDao = unitDao
    private val activityDao = database.activityMasterDao()
    private val operatorDao = database.operatorDao()
    private val reportDao = database.activityReportDao()
    private val lokasiDao = database.lokasiDao()
    private val rencanaDao = database.rencanaKerjaDao()
    private val hasilDao = database.hasilInputAktivitasDao()
    private val kendalaDao = database.kendalaDao()
    private val draftDao = database.draftRealisasiDao()
    private val aktivitasUnitDao = database.aktivitasUnitDao()
    private val userDao = database.userDao()

    // ==========================================
    // USER SESSION & ROLE MANAGEMENT (ROOM SQLITE & DATASTORE PREFERENCES)
    // ==========================================
    fun getActiveUserSession(): Flow<UserEntity?> = userDao.getActiveUserSession()

    suspend fun getActiveUser(): UserEntity? = withContext(Dispatchers.IO) {
        val roomUser = userDao.getActiveUser()
        if (roomUser != null && roomUser.isActiveSession) {
            return@withContext roomUser
        }

        // Restore from DataStore Preferences jika ada
        val dsSession = userSessionManager.getSession()
        if (dsSession != null && dsSession.isLoggedIn && dsSession.isAktif) {
            val restoredUser = UserEntity(
                username = dsSession.username,
                id = dsSession.userId,
                token = dsSession.token,
                name = dsSession.namaLengkap,
                namaLengkap = dsSession.namaLengkap,
                nik = dsSession.username,
                role = dsSession.role,
                roleTitle = if (dsSession.isManager) "Manager Operasional" else if (dsSession.isSuperAdmin) "Super Admin" else "Kasie Lapangan",
                wilayah = dsSession.wilayah,
                status = dsSession.status,
                defaultShift = if (dsSession.isExecutive) "ALL" else "SIANG",
                assignedSector = if (dsSession.isExecutive) "Seluruh Proyek Lapangan" else "Sektor Operasional Lapangan",
                phone = "+62 812-0000-0000",
                isActiveSession = true,
                lastLoginAt = System.currentTimeMillis()
            )
            userDao.clearActiveSessions()
            userDao.insertOrUpdate(restoredUser)
            return@withContext restoredUser
        }

        null
    }

    suspend fun saveUserSession(user: UserEntity) = withContext(Dispatchers.IO) {
        // Simpan ke Room SQLite (users_session)
        userDao.clearActiveSessions()
        userDao.insertOrUpdate(user.copy(isActiveSession = true, lastLoginAt = System.currentTimeMillis()))

        // Simpan ke DataStore Preferences
        userSessionManager.saveSession(
            token = user.token,
            userId = user.id,
            namaLengkap = user.resolvedNamaLengkap,
            username = user.username,
            role = user.role,
            wilayah = user.wilayah ?: "Geodesi 1",
            status = user.status
        )
    }

    suspend fun clearUserSession() = withContext(Dispatchers.IO) {
        userDao.clearActiveSessions()
        userSessionManager.clearSession()
    }

    private val _syncLogs = MutableStateFlow<List<SyncLogItem>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogItem>> = _syncLogs.asStateFlow()

    private val _lastSyncTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimeMillis: StateFlow<Long> = _lastSyncTimeMillis.asStateFlow()

    private fun addLog(endpoint: String, method: String, status: String, detail: String, isSuccess: Boolean) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val item = SyncLogItem(time, endpoint, method, status, detail, isSuccess)
        _syncLogs.value = listOf(item) + _syncLogs.value.take(20)
    }

    fun getUnitsForKasie(namaKasie: String, username: String): Flow<List<UnitEntity>> =
        unitDao.getUnitsForKasie(namaKasie, username)

    fun getUnitsForCurrentKasie(kasieName: String, username: String): Flow<List<UnitEntity>> =
        unitDao.getUnitsForKasie(kasieName, username)

    fun getUnitsForSupervisor(supervisor: String): Flow<List<UnitEntity>> =
        unitDao.getUnitsForSupervisor(supervisor)

    fun getUnitsForSupervisorOrWilayah(supervisor: String, wilayah: String?): Flow<List<UnitEntity>> =
        unitDao.getUnitsForSupervisorOrWilayah(supervisor, wilayah)

    fun getUnitsByWilayah(wilayah: String?): Flow<List<UnitEntity>> =
        unitDao.getUnitsByWilayah(wilayah)

    fun getOperatorsForKasie(kasieName: String, username: String, wilayahKasie: String): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsForKasie(kasieName, username, wilayahKasie)

    fun getAllOperators(): Flow<List<OperatorEntity>> =
        operatorDao.getAllOperators()

    fun getOperatorsByWilayah(wilayah: String?): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsFiltered(wilayah)

    fun getAllUnits(): Flow<List<UnitEntity>> =
        unitDao.getAllUnits()

    fun getUnitsByFilter(wilayah: String?, unitType: String?, unitCode: String?): Flow<List<UnitEntity>> =
        unitDao.getUnitsByFilter(wilayah, unitType, unitCode)

    fun getReportsFiltered(wilayah: String?, unitType: String?, unitCode: String?): Flow<List<ActivityReportEntity>> =
        reportDao.getReportsFiltered(wilayah, unitType, unitCode)

    fun getRencanaFiltered(wilayah: String?, kodeUnit: String?): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getRencanaFiltered(wilayah, kodeUnit)

    fun getAllActivities(): Flow<List<ActivityMasterEntity>> =
        activityDao.getAllActivities()

    fun getActivitiesByJenisUnit(jenisUnit: String?): Flow<List<ActivityMasterEntity>> =
        activityDao.getActivitiesByJenisUnit(jenisUnit)

    fun getAktivitasForUnit(
        jenisUnit: String,
        modelTipe: String = jenisUnit,
        kategoriFilter: String?,
        searchQuery: String?
    ): Flow<List<ActivityMasterEntity>> =
        activityDao.getAktivitasForUnit(jenisUnit, modelTipe, kategoriFilter, searchQuery)

    fun getAktivitasStrict(
        modelTipe: String,
        kategoriFilter: String?,
        searchQuery: String?
    ): Flow<List<ActivityMasterEntity>> =
        activityDao.getAktivitasStrict(modelTipe, kategoriFilter, searchQuery)

    fun getOperatorsForSupervisor(supervisor: String): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsForSupervisor(supervisor)

    fun getOperatorsForSupervisorOrWilayah(supervisor: String, wilayah: String?): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsForSupervisorOrWilayah(supervisor, wilayah)

    fun getOperatorsFiltered(wilayah: String?): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsFiltered(wilayah)

    fun getAllLokasi(): Flow<List<LokasiEntity>> =
        lokasiDao.getAllLokasi()

    fun getAllActiveLokasi(searchQuery: String?, wilayah: String?): Flow<List<LokasiEntity>> =
        lokasiDao.getAllActiveLokasi(searchQuery, wilayah)

    suspend fun getActiveLokasiCount(): Int =
        lokasiDao.getActiveLokasiCount()

    @androidx.room.Transaction
    suspend fun updateMasterLokasi(newList: List<com.example.data.local.entity.MasterLokasiEntity>) {
        lokasiDao.deleteAll()
        lokasiDao.insertAll(newList)
    }

    fun getAllLokasiFiltered(wilayahFilter: String?, query: String?): Flow<List<LokasiEntity>> =
        lokasiDao.getAllLokasiFiltered(wilayahFilter, query)

    fun searchLokasiFast(wilayahFilter: String?, query: String?): Flow<List<LokasiEntity>> =
        lokasiDao.searchLokasiFast(wilayahFilter, query)

    fun searchLokasi(query: String): Flow<List<LokasiEntity>> =
        lokasiDao.searchLokasi(query)

    fun getRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getRencanaForSupervisor(supervisor)

    fun getRencanaKerjaForKasie(namaKasie: String, username: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getRencanaKerjaForKasie(namaKasie, username)

    fun getAllRencanaKerjaForKasie(namaKasie: String, username: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getAllRencanaKerjaForKasie(namaKasie, username)

    fun getAllRencana(): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getAllRencana()

    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getActiveRencanaForSupervisor(supervisor)

    fun getHasilForSupervisor(supervisor: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForSupervisor(supervisor)

    fun getHasilForKasie(namaKasie: String, username: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForKasie(namaKasie, username)

    fun getAllHasil(): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getAllHasil()

    fun getAllKendala(): Flow<List<KendalaEntity>> =
        kendalaDao.getAllKendala()

    fun getAllReports(): Flow<List<ActivityReportEntity>> =
        reportDao.getAllReports()

    fun getReportsForSupervisor(supervisor: String): Flow<List<ActivityReportEntity>> =
        reportDao.getReportsForSupervisor(supervisor)

    fun getRealisasiForKasie(namaKasie: String, username: String): Flow<List<ActivityReportEntity>> =
        reportDao.getRealisasiForKasie(namaKasie, username)

    fun getUnsyncedReports(): Flow<List<ActivityReportEntity>> =
        reportDao.getUnsyncedReports()

    fun getUnsyncedCount(): Flow<Int> =
        combine(reportDao.getUnsyncedCount(), hasilDao.getUnsyncedCount()) { repCount, hasCount ->
            maxOf(repCount, hasCount)
        }

    suspend fun getUnsyncedCountValue(): Int = withContext(Dispatchers.IO) {
        val repCount = reportDao.getUnsyncedReportsList().count { !it.isSynced }
        val hasCount = hasilDao.getUnsyncedHasilList().count { !it.isSynced || it.statusKirim == "PENDING" }
        maxOf(repCount, hasCount)
    }

    suspend fun reconcileSyncStatus() = withContext(Dispatchers.IO) {
        try {
            val allHasil = hasilDao.getAllHasil().firstOrNull() ?: emptyList()
            val unsyncedReports = reportDao.getUnsyncedReportsList()
            for (report in unsyncedReports) {
                val matchingSyncedHasil = allHasil.find {
                    it.isSynced && it.kodeUnit == report.unitCode && it.hmAkhir == report.hmAkhir
                }
                if (matchingSyncedHasil != null) {
                    val syncTime = matchingSyncedHasil.syncedAt ?: System.currentTimeMillis()
                    reportDao.markReportAsSynced(report.id, syncTime)
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun getUnitByCode(unitCode: String): UnitEntity? =
        unitDao.getUnitByCode(unitCode)

    suspend fun getLatestRealizedHmForUnit(unitCode: String): Double? = withContext(Dispatchers.IO) {
        val latestHasil = hasilDao.getLatestHasilForUnit(unitCode)
        if (latestHasil != null && latestHasil.hmAkhir > 0.0) {
            return@withContext latestHasil.hmAkhir
        }
        val unit = unitDao.getUnitByCode(unitCode)
        return@withContext unit?.lastHm
    }

    suspend fun seedInitialDataIfEmpty(activeSupervisor: String) = withContext(Dispatchers.IO) {
        try {
            fetchMasterData(activeSupervisor)
        } catch (_: Exception) {
            // JANGAN pernah menimpa Room SQLite dengan 12 user/39 unit/107 aktivitas tiruan.
            // Biarkan data lokal SQLite yang ada tetap tampil apa adanya.
        }
    }

    suspend fun fetchMasterData(supervisorName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (payload, message) = apiClientManager.fetchMasterData(supervisorName)

            // 1. Process Units Aktif
            val incomingUnits = (payload.units ?: emptyList())
                .filter { com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) }
                .filter { !it.resolvedStatus.equals("Nonaktif", ignoreCase = true) && !it.resolvedStatus.equals("Inactive", ignoreCase = true) }
            val unitEntities = incomingUnits.map {
                UnitEntity(
                    unitCode = it.resolvedUnitCode,
                    unitType = it.resolvedUnitType,
                    brandModel = it.resolvedBrandModel,
                    lastHm = it.resolvedLastHm,
                    supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                    status = it.resolvedStatus,
                    hmMinStandar = it.resolvedHmMinStandar,
                    hmMaxStandar = it.resolvedHmMaxStandar,
                    wilayah = it.resolvedWilayah,
                    updatedAt = System.currentTimeMillis()
                )
            }
            unitDao.replaceAll(unitEntities)
            reportDao.deleteOrphanedReports()

            // 2. Process Activities Aktif
            val incomingActivities = payload.resolvedActivities
                .filter { !it.resolvedKategoriStatus.equals("Nonaktif", ignoreCase = true) }
            val actEntities = incomingActivities.map {
                ActivityMasterEntity(
                    sapCode = it.resolvedSapCode,
                    activityName = it.resolvedActivityName,
                    unitOfMeasure = it.resolvedUnitOfMeasure,
                    description = it.resolvedDescription,
                    category = it.resolvedCategory,
                    kategoriStatus = it.resolvedKategoriStatus,
                    jenisUnit = it.resolvedJenisUnit,
                    updatedAt = System.currentTimeMillis()
                )
            }
            activityDao.replaceAll(actEntities)

            // 3. Process Operators Aktif
            val incomingOperators = (payload.operators ?: emptyList())
                .filter { it.resolvedStatus.equals("Aktif", ignoreCase = true) || it.resolvedStatus.equals("Active", ignoreCase = true) || it.resolvedStatus.isBlank() }
            val opEntities = incomingOperators.map {
                OperatorEntity(
                    operatorId = it.resolvedOperatorId,
                    name = it.resolvedName,
                    badgeNumber = it.resolvedBadgeNumber,
                    supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                    skillLevel = it.resolvedSkillLevel,
                    status = it.resolvedStatus,
                    wilayah = it.resolvedWilayah,
                    updatedAt = System.currentTimeMillis()
                )
            }
            operatorDao.replaceAll(opEntities)

            // 4. Process Lokasi
            val incomingLokasi = payload.resolvedLokasi
            val lokasiEntities = incomingLokasi.map {
                LokasiEntity(
                    kodeLokasi = it.resolvedKode,
                    wilayah = it.resolvedWilayah,
                    luasBruto = it.resolvedLuasBruto,
                    luasNetto = it.resolvedLuasNetto,
                    keterangan = it.keterangan ?: "",
                    status = it.resolvedStatus,
                    updatedAt = System.currentTimeMillis()
                )
            }
            lokasiDao.replaceAll(lokasiEntities)

            // 5. Process Kendala
            val incomingKendala = payload.kendala ?: emptyList()
            if (incomingKendala.isNotEmpty()) {
                val kendalaEntities = incomingKendala.mapIndexed { index, it ->
                    KendalaEntity(
                        kodeKendala = it.resolveKode(index),
                        namaKendala = it.resolvedNama,
                        kategori = it.resolvedKategori,
                        deskripsi = it.resolvedDeskripsi,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                kendalaDao.replaceAll(kendalaEntities)
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            val summary = "${unitEntities.size} Unit, ${actEntities.size} Aktivitas, ${opEntities.size} Operator, ${lokasiEntities.size} Lokasi"
            addLog("GET /api/master-data", "GET", "200 OK", summary, true)

            Result.success("$message ($summary)")
        } catch (e: Exception) {
            addLog("GET /api/master-data", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(Exception("Gagal terhubung ke Web Admin. Periksa koneksi internet."))
        }
    }

    @androidx.room.Transaction
    suspend fun refreshLocalCacheOnly(bootstrap: BootstrapResponseData) = withContext(Dispatchers.IO) {
        // Eksekusi transaksi Clear and Replace Room HANYA di SQLite internal perangkat Android
        database.refreshLocalCacheOnly(bootstrap)
        reportDao.deleteOrphanedReports()

        _lastSyncTimeMillis.value = System.currentTimeMillis()
        val summary = "${bootstrap.users.size} Pengguna, ${bootstrap.units.size} Unit, ${bootstrap.operators.size} Operator, ${bootstrap.aktivitas.size} Aktivitas, ${bootstrap.kendala.size} Kendala, ${bootstrap.lokasi.size} Lokasi, ${bootstrap.rencanaKerja.size} Rencana"
        addLog("GET /api/mobile/bootstrap-data", "GET", "200 OK", "Clear & Replace: $summary", true)
    }

    @androidx.room.Transaction
    suspend fun syncAllMasterData(bootstrap: BootstrapResponseData) = withContext(Dispatchers.IO) {
        refreshLocalCacheOnly(bootstrap)
    }

    suspend fun syncBootstrapData(supervisorName: String, tanggal: String): Result<String> = withContext(Dispatchers.IO) {
        fetchBootstrapData(supervisorName, tanggal)
    }

    suspend fun fetchBootstrapData(supervisorName: String, tanggal: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (bootstrapData, message) = apiClientManager.fetchCentralizedBootstrap(supervisorName, tanggal)
            refreshLocalCacheOnly(bootstrapData)
            Result.success("Data aplikasi berhasil disinkronkan.")
        } catch (e: Exception) {
            addLog("GET /api/mobile/bootstrap-data", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(Exception("Gagal terhubung ke Web Admin. Periksa koneksi internet."))
        }
    }

    suspend fun submitRencanaKerja(rencana: RencanaKerjaEntity): Result<RencanaKerjaEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = rencanaDao.insertRencana(rencana.copy(isSynced = false))
            val savedRencana = rencana.copy(id = insertedId)

            val apiReq = PostRencanaKerjaRequest(
                id = insertedId.toString(),
                status_unit = savedRencana.statusUnit,
                kode_unit = savedRencana.kodeUnit,
                operator = savedRencana.operator,
                sap_code = savedRencana.sapCode,
                nama_aktivitas = savedRencana.namaAktivitas,
                satuan_aktivitas = savedRencana.satuanAktivitas,
                kode_lokasi = savedRencana.kodeLokasi,
                wilayah = savedRencana.wilayah,
                shift_kerja = savedRencana.shiftKerja,
                keterangan = savedRencana.keterangan,
                nomor_spk = savedRencana.nomorSpk,
                status_spk = savedRencana.statusSpk,
                status_tahap = savedRencana.statusTahap,
                is_realized = savedRencana.isRealized,
                nama_pengawas = savedRencana.supervisorName
            )

            try {
                val respMsg = apiClientManager.postRencanaKerja(apiReq)
                rencanaDao.markAsSynced(insertedId)
                addLog("POST /api/rencana-kerja", "POST", "200 OK", "Rencana Unit ${savedRencana.kodeUnit} [${savedRencana.statusUnit}]", true)
                _lastSyncTimeMillis.value = System.currentTimeMillis()
                Result.success(savedRencana.copy(isSynced = true))
            } catch (netEx: Exception) {
                addLog("POST /api/rencana-kerja", "POST", "OFFLINE/PENDING", "Unit ${savedRencana.kodeUnit} disimpan lokal", false)
                Result.success(savedRencana.copy(isSynced = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRencanaSpk(planId: Long, spk: String, status: String = "SPK_TERBIT") = withContext(Dispatchers.IO) {
        rencanaDao.updateSpk(planId, spk, status)
    }

    suspend fun getRencanaById(planId: Long): RencanaKerjaEntity? = withContext(Dispatchers.IO) {
        rencanaDao.getRencanaById(planId)
    }

    suspend fun updateSpkStatus(kodeUnit: String, nomorSpk: String, statusSpk: String = "SPK_TERBIT") = withContext(Dispatchers.IO) {
        rencanaDao.updateSpkStatus(kodeUnit, nomorSpk, statusSpk)
    }

    suspend fun updateSpkForActiveUnit(kodeUnit: String, nomorSpk: String, statusSpk: String = "SPK_TERBIT") = withContext(Dispatchers.IO) {
        rencanaDao.updateSpkForActiveUnit(kodeUnit, nomorSpk, statusSpk)
    }

    suspend fun updateRencanaSpkByUnitOrId(planId: Long, unitKode: String, spk: String, status: String = "SPK_TERBIT"): RencanaKerjaEntity? = withContext(Dispatchers.IO) {
        if (planId > 0) {
            rencanaDao.updateSpk(planId, spk, status)
            val byId = rencanaDao.getRencanaById(planId)
            if (byId != null) return@withContext byId
        }
        if (unitKode.isNotBlank()) {
            rencanaDao.updateSpkByUnit(unitKode, spk, status)
            return@withContext rencanaDao.getRencanaByUnit(unitKode)
        }
        null
    }

    suspend fun deleteRencanaByIdOrUnit(planId: Long, unitKode: String): Int = withContext(Dispatchers.IO) {
        val count = rencanaDao.deleteByIdOrUnit(planId, unitKode)
        if (planId > 0) {
            draftDao.deleteDraftByRencanaId(planId)
        }
        count
    }

    suspend fun deleteRencanaByIdOrUnit(planIdStr: String?, unitKode: String?): Int = withContext(Dispatchers.IO) {
        val numId = planIdStr?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val cleanUnit = unitKode?.trim() ?: ""
        val count = rencanaDao.deleteByIdOrUnit(planIdStr, unitKode)
        if (numId > 0) {
            draftDao.deleteDraftByRencanaId(numId)
        }
        count
    }

    /**
     * Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * SINKRONISASI REKONSILIASI PENUH:
     * 1. Ambil list ID rencana kerja aktif dari respons server (activeServerIds).
     * 2. Lakukan pembersihan (Purge / Sync Pruning) di Room SQLite lokal:
     *    Hapus semua rencana kerja milik pengawas yang belum terealisasi TAPI ID-nya tidak ada di activeServerIds.
     * 3. Lakukan Upsert untuk sisa data yang valid dari server.
     * 4. State Flow Room SQLite akan otomatis terbarui secara reaktif ke UI.
     */
    suspend fun fetchAndReplaceActiveRencana(supervisorName: String): Result<Pair<List<RencanaKerjaEntity>, String>> = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil data rencana kerja resmi terbaru dari server Web Admin (REST API /api/rencana-kerja)
            val (incomingRequests, msg) = apiClientManager.fetchActiveRencanaKerja(supervisorName)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            // 2. Petakan data rencana kerja terbaru dari server dengan verifikasi kepemilikan Kasie & Unit
            val cleanRequestedKasie = supervisorName.trim().removePrefix("@")
            val isRequestingAll = cleanRequestedKasie.isBlank() || cleanRequestedKasie.equals("Semua", ignoreCase = true)
            val serverEntities = mutableListOf<RencanaKerjaEntity>()
            for ((index, renReq) in incomingRequests.withIndex()) {
                val kodeUnitVal = renReq.resolvedUnitKode.ifBlank { "UNIT-${index + 1}" }
                val unitEntity = unitDao.getUnitByCode(kodeUnitVal)
                val unitOwner = unitEntity?.supervisorAssigned?.trim()?.removePrefix("@").orEmpty()
                val reqKasie = renReq.resolvedPengawas.trim().removePrefix("@")
                val trueOwner = when {
                    unitOwner.isNotBlank() -> unitOwner
                    reqKasie.isNotBlank() && !reqKasie.equals("Semua", ignoreCase = true) -> reqKasie
                    else -> cleanRequestedKasie
                }

                // Isolasi Data Penuh: Jika request berasal dari Kasie tertentu, tolak rencana kerja milik Kasie lain
                if (!isRequestingAll) {
                    val isOwnedByKasie = trueOwner.equals(cleanRequestedKasie, ignoreCase = true) ||
                        trueOwner.lowercase().contains(cleanRequestedKasie.lowercase()) ||
                        cleanRequestedKasie.lowercase().contains(trueOwner.lowercase().takeIf { it.isNotBlank() } ?: "___")
                    if (!isOwnedByKasie) continue
                }

                val spkNum = renReq.nomor_spk?.trim() ?: ""
                val spkStatus = when {
                    !renReq.status_spk.isNullOrBlank() -> renReq.status_spk
                    spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" -> "SPK_TERBIT"
                    else -> "MENUNGGU_SPK"
                }

                val serverId = renReq.numericId
                val targetId = if (serverId > 0) serverId else (index + 1).toLong()
                val resolvedCreatedBy = renReq.created_by?.takeIf { it.isNotBlank() }
                    ?: renReq.createdBy?.takeIf { it.isNotBlank() }
                    ?: trueOwner

                val entity = RencanaKerjaEntity(
                    id = targetId,
                    supervisorName = trueOwner,
                    pengawas = trueOwner,
                    kasie = trueOwner,
                    createdBy = resolvedCreatedBy,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = kodeUnitVal,
                    operator = renReq.resolvedOperator,
                    sapCode = renReq.resolvedSapCode,
                    namaAktivitas = renReq.resolvedAktivitas,
                    satuanAktivitas = renReq.resolvedSatuan,
                    kodeLokasi = renReq.resolvedLokasi,
                    wilayah = renReq.wilayah?.takeIf { it.isNotBlank() } ?: unitEntity?.wilayah ?: "Geodesi PG1",
                    shiftKerja = renReq.resolvedShift,
                    keterangan = renReq.keterangan ?: renReq.keterangan_rencana ?: "",
                    nomorSpk = spkNum,
                    statusSpk = spkStatus,
                    statusTahap = renReq.status_tahap ?: "RENCANA",
                    statusRealisasi = if (renReq.is_realized == true) "SUDAH" else "BELUM",
                    isRealized = renReq.is_realized ?: false,
                    isSynced = true,
                    createdAt = System.currentTimeMillis() - (index * 1000L),
                    tanggalFormat = renReq.tanggal?.takeIf { it.isNotBlank() } ?: renReq.tanggal_kerja?.takeIf { it.isNotBlank() } ?: today
                )
                serverEntities.add(entity)
            }

            // 3. ATOMIC CLEAR AND REPLACE via Room DAO:
            if (isRequestingAll) {
                rencanaDao.replaceAll(serverEntities)
            } else {
                rencanaDao.replaceForSupervisor(cleanRequestedKasie, serverEntities)
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            addLog("GET /api/rencana-kerja", "GET", "200 OK", "${serverEntities.size} Rencana Kerja Aktif (Clear & Replace)", true)
            Result.success(Pair(serverEntities, msg))
        } catch (e: Exception) {
            addLog("GET /api/rencana-kerja", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun clearAllRencanaKerja(): Int = withContext(Dispatchers.IO) {
        rencanaDao.deleteAll()
    }

    suspend fun markRencanaRealized(planId: Long) = withContext(Dispatchers.IO) {
        rencanaDao.markAsRealized(planId)
        CompanyServerSimulator.markRencanaRealized(planId)
    }

    suspend fun submitHasilAktivitas(hasil: HasilInputAktivitasEntity): Result<HasilInputAktivitasEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = hasilDao.insertHasil(hasil.copy(isSynced = false))
            val savedHasil = hasil.copy(id = insertedId)

            // Update unit HM in local DB
            unitDao.updateUnitHm(savedHasil.kodeUnit, savedHasil.hmAkhir)

            // Mark Rencana Kerja as realized so it disappears from active dropdown immediately
            if (savedHasil.rencanaId > 0) {
                rencanaDao.markAsRealized(savedHasil.rencanaId)
                CompanyServerSimulator.markRencanaRealized(savedHasil.rencanaId)
            }

            val kendalaPayloads = KendalaJsonConverter.fromJson(savedHasil.daftarKendalaJson).map { it.toCanonical() }

            // Also mirror into ActivityReportEntity for historical compatibility and analytics
            val reportMirror = ActivityReportEntity(
                reportDate = savedHasil.tanggalFormat.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                supervisorName = savedHasil.supervisorName,
                namaKasie = savedHasil.namaKasie.ifBlank { savedHasil.supervisorName },
                createdBy = savedHasil.createdBy.ifBlank { savedHasil.supervisorName },
                unitCode = savedHasil.kodeUnit,
                unitType = "",
                activityName = savedHasil.activityName,
                sapCode = savedHasil.sapCode,
                unitOfMeasure = savedHasil.satuan,
                operatorName = savedHasil.operator,
                workLocation = savedHasil.kodeLokasi,
                shift = savedHasil.shift,
                workDurationHours = savedHasil.jamKerja,
                hmAwal = savedHasil.hmAwal,
                hmAkhir = savedHasil.hmAkhir,
                hmDailyRunning = savedHasil.hmHarianBerjalan,
                workOutput = savedHasil.hasilKerja,
                notes = savedHasil.keterangan,
                unitStatus = savedHasil.unitStatus,
                isRefueled = savedHasil.isIsiSolar,
                fuelLiters = savedHasil.jumlahLiterSolar,
                stikSolarAwal = savedHasil.stikSolarAwal,
                stikSolarAkhir = savedHasil.stikSolarAkhir,
                pemakaianSolar = savedHasil.pemakaianSolar,
                kendalaId = savedHasil.kendalaId,
                kendalaNama = savedHasil.kendalaNama,
                catatanKendala = savedHasil.catatanKendala,
                daftarKendalaJson = savedHasil.daftarKendalaJson,
                photoPath = savedHasil.photoPath,
                photoBase64 = savedHasil.photoBase64,
                photoSizeKb = savedHasil.photoSizeKb,
                photoDimensions = savedHasil.photoDimensions,
                isHmAwalCorrected = savedHasil.isHmAwalCorrected,
                alasanKoreksiHm = savedHasil.alasanKoreksiHm,
                isSynced = false
            )
            val mirrorReportId = reportDao.insertReport(reportMirror)
            val activeUser = userDao.getActiveUser()
            val targetWilayah = reportMirror.wilayah.takeIf { it.isNotBlank() }
                ?: activeUser?.wilayah
                ?: "Geodesi PG1"

            aktivitasUnitDao.insert(
                AktivitasUnitEntity(
                    tanggal = reportMirror.reportDate,
                    kategoriStatus = savedHasil.unitStatus.ifBlank { "OPERASI" },
                    durasiJamKerja = savedHasil.jamKerja,
                    durasiHm = savedHasil.hmHarianBerjalan,
                    pengisianSolar = if (savedHasil.isIsiSolar) savedHasil.jumlahLiterSolar else 0.0,
                    namaPengawas = savedHasil.supervisorName,
                    kodeUnit = savedHasil.kodeUnit,
                    namaAktivitas = savedHasil.activityName,
                    kodeSap = savedHasil.sapCode,
                    satuan = savedHasil.satuan,
                    namaOperator = savedHasil.operator,
                    lokasiKerja = savedHasil.kodeLokasi,
                    wilayah = targetWilayah,
                    shift = savedHasil.shift,
                    hasilKerja = savedHasil.hasilKerja,
                    keterangan = savedHasil.keterangan,
                    createdAt = savedHasil.createdAt
                )
            )
            savedHasil.hmAkhir?.let { hmAkhir ->
                unitDao.updateUnitHm(savedHasil.kodeUnit, hmAkhir)
            }

            val spkClean = if (savedHasil.nomorSpk.isNotBlank() &&
                savedHasil.nomorSpk != "SPK Menyusul" &&
                savedHasil.nomorSpk != "Belum Terbit (Menunggu Admin)" &&
                savedHasil.nomorSpk != "-") savedHasil.nomorSpk else null

            val apiReq = PostHasilAktivitasRequest(
                id = insertedId,
                rencana_id = savedHasil.rencanaId,
                id_rencana_kerja = savedHasil.rencanaId,
                nomor_spk = spkClean,
                jam_kerja = savedHasil.jamKerja,
                hm_awal = savedHasil.hmAwal,
                hm_akhir = savedHasil.hmAkhir,
                hm_harian_berjalan = savedHasil.hmHarianBerjalan,
                is_isi_solar = savedHasil.isIsiSolar,
                jumlah_liter_solar = savedHasil.jumlahLiterSolar,
                stik_solar_awal = savedHasil.stikSolarAwal,
                stik_solar_akhir = savedHasil.stikSolarAkhir,
                pemakaian_solar = savedHasil.pemakaianSolar,
                kendala_id = savedHasil.kendalaId,
                kendala_nama = savedHasil.kendalaNama,
                catatan_kendala = savedHasil.catatanKendala,
                kendala_lapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                kendala_list = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                daftar_kendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                hasil_kerja = savedHasil.hasilKerja,
                keterangan = savedHasil.keterangan,
                nama_pengawas = savedHasil.supervisorName,
                kode_unit = savedHasil.kodeUnit,
                foto_bukti = savedHasil.photoBase64,
                is_hm_awal_corrected = savedHasil.isHmAwalCorrected,
                alasan_koreksi_hm = savedHasil.alasanKoreksiHm,
                client_transaction_id = savedHasil.clientTransactionId,
                wilayah = targetWilayah
            )

            val apiActivityReq = PostActivityReportRequest.create(
                clientUuid = reportMirror.clientUuid,
                reportDate = reportMirror.reportDate,
                supervisorName = reportMirror.supervisorName,
                unitCode = reportMirror.unitCode,
                unitType = reportMirror.unitType,
                activityName = reportMirror.activityName,
                sapCode = reportMirror.sapCode,
                unitOfMeasure = reportMirror.unitOfMeasure,
                operatorName = reportMirror.operatorName,
                workLocation = reportMirror.workLocation,
                shift = reportMirror.shift,
                workDurationHours = reportMirror.workDurationHours,
                hmAwal = reportMirror.hmAwal,
                hmAkhir = reportMirror.hmAkhir,
                hmDailyRunning = reportMirror.hmDailyRunning,
                workOutput = reportMirror.workOutput,
                notes = reportMirror.notes,
                unitStatus = reportMirror.unitStatus,
                isRefueled = reportMirror.isRefueled,
                fuelLiters = reportMirror.fuelLiters,
                stikSolarAwal = reportMirror.stikSolarAwal,
                stikSolarAkhir = reportMirror.stikSolarAkhir,
                pemakaianSolar = reportMirror.pemakaianSolar,
                kendalaId = reportMirror.kendalaId,
                kendalaNama = reportMirror.kendalaNama,
                catatanKendala = reportMirror.catatanKendala,
                kendalaLapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                kendalaList = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                photoBase64 = reportMirror.photoBase64,
                photoSizeKb = reportMirror.photoSizeKb,
                photoDimensions = reportMirror.photoDimensions,
                photoPath = reportMirror.photoPath,
                isHmAwalCorrected = savedHasil.isHmAwalCorrected,
                alasanKoreksiHm = savedHasil.alasanKoreksiHm,
                clientTransactionId = savedHasil.clientTransactionId,
                idRencanaKerja = savedHasil.rencanaId,
                nomorSpk = spkClean,
                wilayah = targetWilayah
            )

            try {
                try { apiClientManager.postActivityReport(apiActivityReq) } catch (_: Exception) {}
                val respMsg = apiClientManager.postHasilAktivitas(apiReq)
                val syncTimestamp = System.currentTimeMillis()
                // Update langsung SQLite/Room lokal: is_synced = true dan status_sync = 'SYNCED'
                hasilDao.markAsSynced(insertedId, syncTimestamp)
                reportDao.markReportAsSynced(mirrorReportId, syncTimestamp)
                reportDao.markReportAsSyncedByUnitAndHm(savedHasil.kodeUnit, savedHasil.hmAkhir, savedHasil.supervisorName, syncTimestamp)
                draftDao.deleteDraftByRencanaId(savedHasil.rencanaId)
                _lastSyncTimeMillis.value = syncTimestamp
                val kendalaTag = if (kendalaPayloads.isNotEmpty()) " [Kendala: ${kendalaPayloads.size} item]" else ""
                addLog("POST /api/aktivitas-unit", "POST", "200 OK", "Realisasi Unit ${savedHasil.kodeUnit} (HM ${savedHasil.hmAkhir})$kendalaTag", true)
                Result.success(savedHasil.copy(isSynced = true, statusKirim = "SYNCED", syncedAt = syncTimestamp))
            } catch (netEx: Exception) {
                addLog("POST /api/aktivitas-unit", "POST", "OFFLINE/PENDING", "Realisasi Unit ${savedHasil.kodeUnit} disimpan lokal", false)
                Result.success(savedHasil.copy(isSynced = false, statusKirim = "PENDING"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitReport(report: ActivityReportEntity): Result<ActivityReportEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = reportDao.insertReport(report.copy(isSynced = false))
            val savedReport = report.copy(id = insertedId)
            val activeUser = userDao.getActiveUser()
            val targetWilayah = savedReport.wilayah.takeIf { it.isNotBlank() }
                ?: activeUser?.wilayah
                ?: "Geodesi PG1"

            aktivitasUnitDao.insert(
                AktivitasUnitEntity(
                    tanggal = savedReport.reportDate,
                    kategoriStatus = savedReport.unitStatus.ifBlank { "OPERASI" },
                    durasiJamKerja = savedReport.workDurationHours,
                    durasiHm = savedReport.hmDailyRunning,
                    pengisianSolar = if (savedReport.isRefueled) savedReport.fuelLiters else 0.0,
                    namaPengawas = savedReport.supervisorName,
                    kodeUnit = savedReport.unitCode,
                    namaAktivitas = savedReport.activityName,
                    kodeSap = savedReport.sapCode,
                    satuan = savedReport.unitOfMeasure,
                    namaOperator = savedReport.operatorName,
                    lokasiKerja = savedReport.workLocation,
                    wilayah = targetWilayah,
                    shift = savedReport.shift,
                    hasilKerja = savedReport.workOutput,
                    keterangan = savedReport.notes,
                    createdAt = savedReport.createdAt
                )
            )
            val kendalaPayloads = KendalaJsonConverter.fromJson(savedReport.daftarKendalaJson).map { it.toCanonical() }

            val apiReq = PostActivityReportRequest.create(
                clientUuid = savedReport.clientUuid,
                reportDate = savedReport.reportDate,
                supervisorName = savedReport.supervisorName,
                unitCode = savedReport.unitCode,
                unitType = savedReport.unitType,
                activityName = savedReport.activityName,
                sapCode = savedReport.sapCode,
                unitOfMeasure = savedReport.unitOfMeasure,
                operatorName = savedReport.operatorName,
                workLocation = savedReport.workLocation,
                shift = savedReport.shift,
                workDurationHours = savedReport.workDurationHours,
                hmAwal = savedReport.hmAwal,
                hmAkhir = savedReport.hmAkhir,
                hmDailyRunning = savedReport.hmDailyRunning,
                workOutput = savedReport.workOutput,
                notes = savedReport.notes,
                unitStatus = savedReport.unitStatus,
                isRefueled = savedReport.isRefueled,
                fuelLiters = savedReport.fuelLiters,
                kendalaId = savedReport.kendalaId,
                kendalaNama = savedReport.kendalaNama,
                catatanKendala = savedReport.catatanKendala,
                kendalaLapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                kendalaList = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                photoBase64 = savedReport.photoBase64,
                photoSizeKb = savedReport.photoSizeKb,
                photoDimensions = savedReport.photoDimensions,
                photoPath = savedReport.photoPath,
                isHmAwalCorrected = savedReport.isHmAwalCorrected,
                alasanKoreksiHm = savedReport.alasanKoreksiHm,
                wilayah = targetWilayah
            )

            try {
                val responseMsg = apiClientManager.postActivityReport(apiReq)
                reportDao.markReportAsSynced(insertedId)
                unitDao.updateUnitHm(savedReport.unitCode, savedReport.hmAkhir)

                val fuelTag = if (savedReport.isRefueled) " [Solar: ${savedReport.fuelLiters}L]" else ""
                addLog("POST /api/aktivitas-unit", "POST", "200 OK", "Unit ${savedReport.unitCode} [${savedReport.unitStatus}]$fuelTag (HM ${savedReport.hmAkhir})", true)
                Result.success(savedReport.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            } catch (netEx: Exception) {
                unitDao.updateUnitHm(savedReport.unitCode, savedReport.hmAkhir)
                addLog("POST /api/aktivitas-unit", "POST", "OFFLINE/PENDING", "Unit ${savedReport.unitCode} [${savedReport.unitStatus}] disimpan di SQLite lokal", false)
                Result.success(savedReport.copy(isSynced = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingReports(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Anti-duplikat: Filter hanya record yang benar-benar memiliki isSynced == false
            val pending = reportDao.getUnsyncedReportsList().filter { !it.isSynced }
            if (pending.isEmpty()) {
                return@withContext Result.success(0)
            }

            var successCount = 0
            for (report in pending) {
                try {
                    val kendalaPayloads = KendalaJsonConverter.fromJson(report.daftarKendalaJson).map { it.toCanonical() }
                    val apiReq = PostActivityReportRequest.create(
                        clientUuid = report.clientUuid,
                        reportDate = report.reportDate,
                        supervisorName = report.supervisorName,
                        unitCode = report.unitCode,
                        unitType = report.unitType,
                        activityName = report.activityName,
                        sapCode = report.sapCode,
                        unitOfMeasure = report.unitOfMeasure,
                        operatorName = report.operatorName,
                        workLocation = report.workLocation,
                        shift = report.shift,
                        workDurationHours = report.workDurationHours,
                        hmAwal = report.hmAwal,
                        hmAkhir = report.hmAkhir,
                        hmDailyRunning = report.hmDailyRunning,
                        workOutput = report.workOutput,
                        notes = report.notes,
                        unitStatus = report.unitStatus,
                        isRefueled = report.isRefueled,
                        fuelLiters = report.fuelLiters,
                        kendalaId = report.kendalaId,
                        kendalaNama = report.kendalaNama,
                        catatanKendala = report.catatanKendala,
                        kendalaLapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        kendalaList = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        photoBase64 = report.photoBase64,
                        photoSizeKb = report.photoSizeKb,
                        photoDimensions = report.photoDimensions,
                        photoPath = report.photoPath,
                        isHmAwalCorrected = report.isHmAwalCorrected,
                        alasanKoreksiHm = report.alasanKoreksiHm,
                        wilayah = report.wilayah
                    )
                    apiClientManager.postActivityReport(apiReq)
                    val now = System.currentTimeMillis()
                    reportDao.markReportAsSynced(report.id, now)
                    hasilDao.markHasilAsSyncedByUnitAndHm(report.unitCode, report.hmAkhir, report.supervisorName, now)
                    unitDao.updateUnitHm(report.unitCode, report.hmAkhir)
                    successCount++
                } catch (_: Exception) {}
            }

            if (successCount > 0) {
                addLog("POST /api/aktivitas-unit (BATCH)", "POST", "SYNC COMPLETED", "Sinkron $successCount dari ${pending.size} laporan", true)
                _lastSyncTimeMillis.value = System.currentTimeMillis()
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingOperationalData(): Result<Int> = withContext(Dispatchers.IO) {
        var totalSynced = 0
        try {
            // 1. Sync pending rencana: Filter hanya record yang benar-benar is_synced == false
            val pendingRencana = rencanaDao.getUnsyncedRencanaList().filter { !it.isSynced }
            for (rencana in pendingRencana) {
                try {
                    val req = PostRencanaKerjaRequest(
                        id = rencana.id.toString(),
                        status_unit = rencana.statusUnit,
                        kode_unit = rencana.kodeUnit,
                        operator = rencana.operator,
                        kode_lokasi = rencana.kodeLokasi,
                        shift_kerja = rencana.shiftKerja,
                        keterangan = rencana.keterangan,
                        nomor_spk = rencana.nomorSpk,
                        status_spk = rencana.statusSpk,
                        nama_pengawas = rencana.supervisorName
                    )
                    apiClientManager.postRencanaKerja(req)
                    rencanaDao.markAsSynced(rencana.id)
                    totalSynced++
                } catch (_: Exception) {}
            }

            // 2. Sync pending hasil aktivitas: Filter hanya record yang benar-benar is_synced == false atau status_kirim == 'PENDING'
            val pendingHasil = hasilDao.getUnsyncedHasilList().filter { !it.isSynced || it.statusKirim == "PENDING" }
            for (hasil in pendingHasil) {
                try {
                    val kendalaPayloads = KendalaJsonConverter.fromJson(hasil.daftarKendalaJson).map { it.toCanonical() }
                    val req = PostHasilAktivitasRequest(
                        id = hasil.id,
                        rencana_id = hasil.rencanaId,
                        nomor_spk = hasil.nomorSpk,
                        jam_kerja = hasil.jamKerja,
                        hm_awal = hasil.hmAwal,
                        hm_akhir = hasil.hmAkhir,
                        hm_harian_berjalan = hasil.hmHarianBerjalan,
                        is_isi_solar = hasil.isIsiSolar,
                        jumlah_liter_solar = hasil.jumlahLiterSolar,
                        kendala_id = hasil.kendalaId,
                        kendala_nama = hasil.kendalaNama,
                        catatan_kendala = hasil.catatanKendala,
                        kendala_lapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        kendala_list = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        daftar_kendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        hasil_kerja = hasil.hasilKerja,
                        keterangan = hasil.keterangan,
                        nama_pengawas = hasil.supervisorName,
                        kode_unit = hasil.kodeUnit,
                        foto_bukti = hasil.photoBase64,
                        is_hm_awal_corrected = hasil.isHmAwalCorrected,
                        alasan_koreksi_hm = hasil.alasanKoreksiHm,
                        wilayah = hasil.wilayah
                    )
                    apiClientManager.postHasilAktivitas(req)
                    val now = System.currentTimeMillis()
                    hasilDao.markAsSynced(hasil.id, now)
                    reportDao.markReportAsSyncedByUnitAndHm(hasil.kodeUnit, hasil.hmAkhir, hasil.supervisorName, now)
                    draftDao.deleteDraftByRencanaId(hasil.rencanaId)
                    totalSynced++
                } catch (_: Exception) {}
            }

            // 3. Sync pending reports (ActivityReportEntity) yang belum tersinkron
            val reportsResult = syncAllPendingReports()
            totalSynced += reportsResult.getOrDefault(0)

            if (totalSynced > 0) {
                _lastSyncTimeMillis.value = System.currentTimeMillis()
                addLog("BATCH QUEUE SYNC", "SYNC", "SUCCESS", "Tersinkron total $totalSynced entri operasional", true)
            }
            Result.success(totalSynced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(url: String): Pair<Boolean, String> {
        val result = apiClientManager.testConnection(url)
        addLog("TEST $url", "PING", if (result.first) "CONNECTED" else "FAILED", result.second, result.first)
        return result
    }

    suspend fun deleteReport(reportId: Long) = withContext(Dispatchers.IO) {
        reportDao.deleteReport(reportId)
    }

    suspend fun deleteRencana(rencanaId: Long) = withContext(Dispatchers.IO) {
        rencanaDao.deleteRencana(rencanaId)
    }

    suspend fun deleteHasil(hasilId: Long) = withContext(Dispatchers.IO) {
        hasilDao.deleteHasil(hasilId)
    }

    suspend fun getDraftForRencana(rencanaId: Long): com.example.data.local.entity.DraftRealisasiEntity? = withContext(Dispatchers.IO) {
        draftDao.getDraftByRencanaId(rencanaId)
    }

    suspend fun saveDraftForRencana(draft: com.example.data.local.entity.DraftRealisasiEntity) = withContext(Dispatchers.IO) {
        draftDao.insertOrUpdateDraft(draft)
    }

    suspend fun deleteDraftForRencana(rencanaId: Long) = withContext(Dispatchers.IO) {
        draftDao.deleteDraftByRencanaId(rencanaId)
    }

    fun getAllDrafts(): Flow<List<com.example.data.local.entity.DraftRealisasiEntity>> = draftDao.getAllDrafts()

    suspend fun addOrUpdateUnit(unit: UnitEntity) = withContext(Dispatchers.IO) {
        if (!com.example.data.model.UnitConstants.isJenisUnitValid(unit.unitType)) {
            throw IllegalArgumentException("Jenis unit tidak valid atau telah dihapus")
        }
        unitDao.insertUnit(unit)
        CompanyServerSimulator.addOrUpdateUnit(
            com.example.data.remote.UnitPayload(
                unitCode = unit.unitCode,
                unitType = unit.unitType,
                brandModel = unit.brandModel,
                lastHm = unit.lastHm,
                supervisorAssigned = unit.supervisorAssigned,
                status = unit.status,
                hmMinStandar = unit.hmMinStandar,
                hmMaxStandar = unit.hmMaxStandar,
                wilayah = unit.wilayah
            )
        )
    }

    suspend fun deleteUnit(unitCode: String) = withContext(Dispatchers.IO) {
        unitDao.deleteUnit(unitCode)
        rencanaDao.deleteOrphanedRencanaKerja()
        reportDao.deleteOrphanedReports()
        CompanyServerSimulator.deleteUnit(unitCode)
    }

    suspend fun cleanDumpTruckAndBulldozer() = withContext(Dispatchers.IO) {
        // No-op: Dump Truck and other active fleet units are preserved
    }
}
