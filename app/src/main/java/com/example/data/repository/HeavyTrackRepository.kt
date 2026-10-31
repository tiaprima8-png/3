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
import com.example.data.model.SupervisorDirectory
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
import com.example.data.remote.RealtimeRealisasiData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
                roleTitle = when {
                    dsSession.isManager -> "Manager Operasional"
                    dsSession.isSuperAdmin -> "Super Admin"
                    dsSession.isOperator -> "Operator Alat Berat"
                    else -> "Kasie Lapangan"
                },
                wilayah = dsSession.wilayah,
                kasieAtasan = dsSession.kasieAtasan,
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
            kasieAtasan = user.kasieAtasan ?: "",
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

    fun getRencanaForOperator(operatorIdentifier: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getRencanaForOperator(operatorIdentifier)

    fun getPendingTugasForOperator(namaOperator: String, nikOperator: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getPendingTugasForOperator(namaOperator, nikOperator)

    fun getAllRencanaForOperator(operatorIdentifier: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getAllRencanaForOperator(operatorIdentifier)

    fun getAllTugasForOperator(namaOperator: String, nikOperator: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getAllTugasForOperator(namaOperator, nikOperator)

    fun getAllRencana(): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getAllRencana()

    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getActiveRencanaForSupervisor(supervisor)

    fun getHasilForSupervisor(supervisor: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForSupervisor(supervisor)

    fun getHasilForKasie(namaKasie: String, username: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForKasie(namaKasie, username)

    fun getHasilForOperator(operatorIdentifier: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForOperator(operatorIdentifier)

    fun getRiwayatHasilForOperator(namaOperator: String, nikOperator: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getRiwayatHasilForOperator(namaOperator, nikOperator)

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

    fun getRealisasiForOperator(operatorIdentifier: String): Flow<List<ActivityReportEntity>> =
        reportDao.getRealisasiForOperator(operatorIdentifier)

    fun getRiwayatForOperator(namaOperator: String, nikOperator: String): Flow<List<ActivityReportEntity>> =
        reportDao.getRiwayatForOperator(namaOperator, nikOperator)


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
            seedOperatorsIntoUsersTable()
            fetchMasterData(activeSupervisor)
        } catch (_: Exception) {
            try {
                seedOperatorsIntoUsersTable()
            } catch (_: Exception) {}
        }
    }

    /**
     * AUTO-SEED 35 AKUN OPERATOR KE DATABASE TABEL USERS:
     * Mengambil semua data dari master_operator (status = 'Aktif') serta daftar resmi 35 operator,
     * dan memasukkannya ke tabel users (users_session) dengan:
     * - username: NIK Operator (string murni, contoh: '85044295')
     * - password: NIK Operator
     * - nama: Nama Lengkap Operator
     * - role: 'operator'
     * - kasie_atasan: kasie_penanggung_jawab dari operator tersebut
     * - status: 'Aktif'
     */
    suspend fun seedOperatorsIntoUsersTable() = withContext(Dispatchers.IO) {
        val dbOperators = operatorDao.getAllActiveOperatorsList()
        if (dbOperators.isNotEmpty()) {
            com.example.data.model.SupervisorDirectory.updateFromOperatorEntities(dbOperators)
        }
        val activeSession = userDao.getActiveUser()
        val seededOperatorUsers = mutableMapOf<String, com.example.data.local.entity.UserEntity>()

        com.example.data.model.SupervisorDirectory.officialOperatorList.forEach { op ->
            val cleanNik = op.nik.ifBlank { op.username }.trim()
            if (cleanNik.isNotBlank()) {
                val isCurrentActive = activeSession?.username?.equals(cleanNik, ignoreCase = true) == true
                seededOperatorUsers[cleanNik.lowercase()] = com.example.data.local.entity.UserEntity(
                    username = cleanNik,
                    password = cleanNik,
                    id = op.id,
                    token = if (isCurrentActive) activeSession.token else "SEED-OP-$cleanNik",
                    name = op.resolvedNamaLengkap,
                    namaLengkap = op.resolvedNamaLengkap,
                    nik = cleanNik,
                    role = "operator",
                    roleTitle = "Operator Alat Berat",
                    kasieAtasan = op.kasieAtasan,
                    wilayah = op.wilayah,
                    status = "Aktif",
                    isActiveSession = isCurrentActive,
                    lastLoginAt = if (isCurrentActive) activeSession.lastLoginAt else System.currentTimeMillis()
                )
            }
        }

        dbOperators.forEachIndexed { idx, op ->
            val cleanNik = op.badgeNumber.ifBlank { op.operatorId }.trim()
            if (cleanNik.isNotBlank()) {
                val isCurrentActive = activeSession?.username?.equals(cleanNik, ignoreCase = true) == true
                val resolvedKasie = (op.kasiePenanggungJawab ?: "").ifBlank { op.supervisorAssigned }.ifBlank { "Agung" }
                seededOperatorUsers[cleanNik.lowercase()] = com.example.data.local.entity.UserEntity(
                    username = cleanNik,
                    password = cleanNik,
                    id = (101 + idx).toString(),
                    token = if (isCurrentActive) activeSession.token else "SEED-OP-$cleanNik",
                    name = op.name,
                    namaLengkap = op.name,
                    nik = cleanNik,
                    role = "operator",
                    roleTitle = "Operator Alat Berat",
                    kasieAtasan = resolvedKasie,
                    wilayah = (op.wilayah ?: "").ifBlank { "RC PG1" },
                    status = "Aktif",
                    isActiveSession = isCurrentActive,
                    lastLoginAt = if (isCurrentActive) activeSession.lastLoginAt else System.currentTimeMillis()
                )
            }
        }

        if (seededOperatorUsers.isNotEmpty()) {
            userDao.insertAll(seededOperatorUsers.values.toList())
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
            val incomingOperators = (payload.resolvedOperators)
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
                    kasiePenanggungJawab = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                    updatedAt = System.currentTimeMillis()
                )
            }
            operatorDao.replaceAll(opEntities)
            seedOperatorsIntoUsersTable()

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

    suspend fun submitBatchRencanaKerja(rencanaList: List<RencanaKerjaEntity>): Result<List<RencanaKerjaEntity>> = withContext(Dispatchers.IO) {
        try {
            reportDao.deleteInvalidEmptyReports()
            hasilDao.deleteInvalidEmptyHasil()
            val unsyncedList = rencanaList.map {
                it.copy(
                    id = 0L,
                    isSynced = false,
                    isRealized = false,
                    statusTahap = "RENCANA",
                    statusRealisasi = "Menunggu Realisasi"
                )
            }
            val insertedIds = rencanaDao.insertAll(unsyncedList)
            val savedList = unsyncedList.mapIndexed { idx, ren ->
                ren.copy(id = insertedIds.getOrNull(idx) ?: 0L)
            }

            val finalResultList = mutableListOf<RencanaKerjaEntity>()
            for (savedRencana in savedList) {
                val apiReq = PostRencanaKerjaRequest(
                    id = savedRencana.id.toString(),
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
                    nama_pengawas = savedRencana.supervisorName,
                    tanggal = savedRencana.tanggalFormat,
                    tanggal_kerja = savedRencana.tanggalFormat
                )
                try {
                    apiClientManager.postRencanaKerja(apiReq)
                    rencanaDao.markAsSynced(savedRencana.id)
                    finalResultList.add(savedRencana.copy(isSynced = true))
                } catch (_: Exception) {
                    finalResultList.add(savedRencana.copy(isSynced = false))
                }
            }
            _lastSyncTimeMillis.value = System.currentTimeMillis()
            addLog("POST /api/rencana-kerja/batch", "POST", "200 OK", "Simpan ${finalResultList.size} Rencana Kerja Unit ${finalResultList.firstOrNull()?.kodeUnit ?: ""}", true)
            Result.success(finalResultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRencanaKerja(rencana: RencanaKerjaEntity): Result<RencanaKerjaEntity> = withContext(Dispatchers.IO) {
        submitBatchRencanaKerja(listOf(rencana)).map { it.first() }
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
            reportDao.deleteInvalidEmptyReports()
            hasilDao.deleteInvalidEmptyHasil()
            rencanaDao.restoreUnrealizedPlansWithNoValidReport()

            // 1. Ambil data rencana kerja resmi terbaru dari server Web Admin (REST API /api/rencana-kerja)
            val (incomingRequests, msg) = apiClientManager.fetchActiveRencanaKerja(supervisorName)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val existingLocalPlans = rencanaDao.getAllRencanaList()

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
                val resolvedCreatedBy = renReq.created_by?.takeIf { it.isNotBlank() }
                    ?: renReq.createdBy?.takeIf { it.isNotBlank() }
                    ?: trueOwner
                val resolvedDate = renReq.tanggal?.takeIf { it.isNotBlank() } ?: renReq.tanggal_kerja?.takeIf { it.isNotBlank() } ?: today
                val resolvedOpName = renReq.resolvedOperator
                // Cocokkan rencana lokal HANYA berdasarkan ID spesifik atau Nomor SPK unik (JANGAN berdasarkan kodeUnit + tanggal + shift agar multi-rencana tidak tertimpa!)
                val matchedLocal = existingLocalPlans.find { loc ->
                    (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" && loc.nomorSpk.equals(spkNum, ignoreCase = true)) ||
                    (serverId > 0 && loc.id == serverId && (spkNum.isBlank() || loc.nomorSpk.isBlank() || loc.nomorSpk.equals(spkNum, ignoreCase = true)))
                }
                val maxLocalId = (existingLocalPlans.maxOfOrNull { it.id } ?: 0L) + serverEntities.size + 100L
                val targetId = when {
                    matchedLocal != null && matchedLocal.id > 0 -> matchedLocal.id
                    serverId > 0 && existingLocalPlans.none { it.id == serverId } && serverEntities.none { it.id == serverId } -> serverId
                    else -> maxLocalId + index + 1L
                }
                val resolvedOpNik = matchedLocal?.operatorNik?.takeIf { it.isNotBlank() }
                    ?: operatorDao.findActiveOperatorByNikOrName(resolvedOpName)?.badgeNumber?.removePrefix("NIK-")?.trim()
                    ?: SupervisorDirectory.findOperatorByNameOrNik(resolvedOpName)?.nik
                    ?: ""
                val reportBySpk = if (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-") reportDao.getReportByNomorSpk(spkNum) else null
                val hasilBySpk = if (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-") hasilDao.getHasilByNomorSpk(spkNum) else null
                val hasValidReportInDb = reportBySpk != null && reportBySpk.hmAkhir > 0.0 &&
                    (reportBySpk.hmDailyRunning > 0.0 || reportBySpk.hmAkhir > reportBySpk.hmAwal || reportBySpk.workOutput > 0.0)
                val hasValidHasilInDb = hasilBySpk != null && hasilBySpk.hmAkhir > 0.0 &&
                    (hasilBySpk.hmHarianBerjalan > 0.0 || hasilBySpk.hmAkhir > hasilBySpk.hmAwal || hasilBySpk.hasilKerja > 0.0)
                val hasRealisasiInDb = hasValidReportInDb || hasValidHasilInDb
                val isAlreadyRealized = hasRealisasiInDb || (matchedLocal?.isRealized == true && hasRealisasiInDb)
                val parsedSubIdx = spkNum.split("-").takeIf { it.size >= 4 }?.lastOrNull()?.toIntOrNull()?.takeIf { it in 1..99 } ?: 1
                val resolvedSubIndex = matchedLocal?.subIndex?.takeIf { it > 1 } ?: parsedSubIdx
                val resolvedParentSpk = matchedLocal?.parentSpk?.takeIf { it.isNotBlank() }
                    ?: (if (spkNum.split("-").size >= 4) spkNum.split("-").dropLast(1).joinToString("-") else spkNum)

                val entity = RencanaKerjaEntity(
                    id = targetId,
                    supervisorName = trueOwner,
                    pengawas = trueOwner,
                    kasie = trueOwner,
                    createdBy = resolvedCreatedBy,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = kodeUnitVal,
                    operator = resolvedOpName,
                    namaOperator = resolvedOpName,
                    operatorNik = resolvedOpNik,
                    sapCode = renReq.resolvedSapCode,
                    namaAktivitas = renReq.resolvedAktivitas,
                    satuanAktivitas = renReq.resolvedSatuan,
                    kodeLokasi = renReq.resolvedLokasi,
                    wilayah = renReq.wilayah?.takeIf { it.isNotBlank() } ?: unitEntity?.wilayah ?: "Geodesi PG1",
                    shiftKerja = renReq.resolvedShift,
                    keterangan = renReq.keterangan ?: renReq.keterangan_rencana ?: "",
                    nomorSpk = spkNum,
                    statusSpk = if (isAlreadyRealized) (matchedLocal?.statusSpk?.ifBlank { "Terealisasi" } ?: "Terealisasi") else spkStatus,
                    statusTahap = if (isAlreadyRealized) "REALISASI_SELESAI" else (renReq.status_tahap ?: "RENCANA"),
                    statusRealisasi = if (isAlreadyRealized) (matchedLocal?.statusRealisasi?.ifBlank { "Terealisasi" } ?: "Terealisasi") else "Menunggu Realisasi",
                    isRealized = isAlreadyRealized,
                    isSynced = true,
                    createdAt = matchedLocal?.createdAt ?: (System.currentTimeMillis() - (index * 1000L)),
                    tanggalFormat = resolvedDate,
                    subIndex = resolvedSubIndex,
                    parentSpk = resolvedParentSpk
                )
                serverEntities.add(entity)
            }

            // FULL MIRROR SYNC: Hapus rencana lokal lama, lalu isi data remote terkini
            rencanaDao.deleteAll()
            if (serverEntities.isNotEmpty()) {
                rencanaDao.insertAll(serverEntities)
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            addLog("GET /api/rencana-kerja", "GET", "200 OK", "${serverEntities.size} Rencana Kerja Aktif (Full Mirror Sync)", true)
            Result.success(Pair(serverEntities, msg))
        } catch (e: Exception) {
            addLog("GET /api/rencana-kerja", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun clearAllRencanaKerja(): Int = withContext(Dispatchers.IO) {
        rencanaDao.deleteAll()
    }

    suspend fun resetLocalTransactionCache() = withContext(Dispatchers.IO) {
        rencanaDao.deleteAll()
        reportDao.deleteAll()
        hasilDao.deleteAll()
        aktivitasUnitDao.clearAll()
        draftDao.clearAllDrafts()
        CompanyServerSimulator.clearAllTransactions()
        _lastSyncTimeMillis.value = System.currentTimeMillis()
        addLog("LOCAL RESET", "DELETE", "0 DATA", "Database transaksi lokal berhasil di-reset ke 0.", true)
    }

    suspend fun syncRealisasiWithServer(kasieName: String = "") = withContext(Dispatchers.IO) {
        fetchPendingRealisasiForKasie(kasieName)
    }

    suspend fun syncRencanaWithServer(supervisorName: String = "") = withContext(Dispatchers.IO) {
        fetchAndReplaceActiveRencana(supervisorName)
    }

    suspend fun syncAllData(supervisorName: String = "") = withContext(Dispatchers.IO) {
        syncRencanaWithServer(supervisorName)
        syncRealisasiWithServer(supervisorName)
    }

    suspend fun markRencanaRealized(planId: Long) = withContext(Dispatchers.IO) {
        rencanaDao.markAsRealized(planId)
        CompanyServerSimulator.markRencanaRealized(planId)
    }

    suspend fun fetchPendingRealisasiForKasie(kasieName: String): Result<List<ActivityReportEntity>> = withContext(Dispatchers.IO) {
        try {
            // 1. Dorong terlebih dahulu antrean lokal yang belum terkirim (is_synced == false) ke server
            try {
                syncAllPendingOperationalData()
            } catch (_: Exception) {}

            // Simpan cadangan item lokal yang masih gagal terkirim (isSynced == false) agar tidak hilang
            val unsyncedReportsBackup = reportDao.getUnsyncedReportsList().filter { !it.isSynced }
            val unsyncedHasilBackup = hasilDao.getUnsyncedHasilList().filter { !it.isSynced }

            val (incomingReports, _) = apiClientManager.fetchPendingRealisasiForKasie(kasieName)
            // Filter ketat: Hanya terima data yang benar-benar sudah diisi angka realisasi oleh operator
            val validReports = incomingReports.filter { req ->
                val hmAwal = req.hm_awal ?: 0.0
                val hmAkhir = req.hm_akhir ?: 0.0
                val totalHm = req.hm_harian_berjalan ?: (if (hmAkhir > hmAwal) hmAkhir - hmAwal else 0.0)
                val hasilKerja = req.hasil_kerja ?: 0.0
                hmAkhir > 0.0 && (totalHm > 0.0 || hmAkhir > hmAwal || hasilKerja > 0.0)
            }

            // FULL MIRROR SYNC: Hapus data transaksi lokal lama, lalu isi dengan data remote terkini
            // (Jika server dikosongkan / 0 laporan, lokal juga ikut bersih menjadi 0 laporan)
            reportDao.deleteAll()
            hasilDao.deleteAll()
            aktivitasUnitDao.clearAll()

            val savedEntities = mutableListOf<ActivityReportEntity>()
            if (validReports.isNotEmpty()) {
                for (req in validReports) {
                    val entity = saveIncomingRealisasiInternal(req)
                    if (entity != null) {
                        savedEntities.add(entity)
                    }
                }
            }

            // Pulihkan antrean lokal yang belum terkirim ke server (jika masih offline)
            for (unsyncedRep in unsyncedReportsBackup) {
                val alreadyInServer = savedEntities.any {
                    (unsyncedRep.nomorSpk.isNotBlank() && it.nomorSpk.equals(unsyncedRep.nomorSpk, ignoreCase = true)) ||
                        (unsyncedRep.clientUuid.isNotBlank() && it.clientUuid.equals(unsyncedRep.clientUuid, ignoreCase = true))
                }
                if (!alreadyInServer) {
                    reportDao.insertReport(unsyncedRep)
                    savedEntities.add(unsyncedRep)
                }
            }
            for (unsyncedHas in unsyncedHasilBackup) {
                val alreadyInServer = savedEntities.any {
                    (unsyncedHas.nomorSpk.isNotBlank() && it.nomorSpk.equals(unsyncedHas.nomorSpk, ignoreCase = true)) ||
                        (unsyncedHas.clientTransactionId.isNotBlank() && it.clientUuid.equals(unsyncedHas.clientTransactionId, ignoreCase = true))
                }
                if (!alreadyInServer) {
                    hasilDao.insertHasil(unsyncedHas)
                }
            }

            rencanaDao.restoreUnrealizedPlansWithNoValidReport()
            addLog("GET /api/realisasi?kasie=$kasieName", "GET", "200 OK", "Full Mirror Sync: ${savedEntities.size} realisasi", true)
            Result.success(savedEntities)
        } catch (e: Exception) {
            addLog("GET /api/realisasi?kasie=$kasieName", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun saveIncomingRealisasi(req: PostActivityReportRequest): ActivityReportEntity? = withContext(Dispatchers.IO) {
        saveIncomingRealisasiInternal(req)
    }

    suspend fun saveIncomingRealisasi(relData: RealtimeRealisasiData): ActivityReportEntity? = withContext(Dispatchers.IO) {
        val txId = if (relData.id.isNotBlank()) relData.id else UUID.randomUUID().toString()
        val req = PostActivityReportRequest(
            id_rencana_kerja = relData.rencanaId,
            kode_unit = relData.unitKode,
            nama_operator = relData.operator,
            operator_nik = relData.operatorNik,
            nama_pengawas = relData.kasie,
            supervisor_name = relData.kasie,
            tanggal = relData.tanggal,
            shift = relData.shift,
            hm_awal = relData.hmAwal,
            hm_akhir = relData.hmAkhir,
            hm_harian_berjalan = relData.totalHm,
            jumlah_liter_solar = relData.solar,
            pemakaian_solar = relData.solar,
            nomor_spk = relData.nomorSpk,
            approval_status = relData.approvalStatus,
            is_locked = relData.isLocked,
            client_transaction_id = txId
        )
        saveIncomingRealisasiInternal(req)
    }

    private suspend fun saveIncomingRealisasiInternal(req: PostActivityReportRequest): ActivityReportEntity? {
        val reqHmAwal = req.hm_awal ?: 0.0
        val reqHmAkhir = req.hm_akhir ?: 0.0
        val reqHmDaily = if ((req.hm_harian_berjalan ?: 0.0) > 0.0) req.hm_harian_berjalan!! else if (reqHmAkhir > reqHmAwal) reqHmAkhir - reqHmAwal else 0.0
        val reqWorkOutput = req.hasil_kerja ?: 0.0

        // DILARANG menyimpan data realisasi palsu/kosong (misal HM 0.0, Solar 25.0 L) atau menandai rencana sebagai selesai!
        if (reqHmAkhir <= 0.0 || (reqHmDaily <= 0.0 && reqHmAkhir <= reqHmAwal && reqWorkOutput <= 0.0)) {
            return null
        }

        val renId = req.id_rencana_kerja ?: 0L
        val rawSpk = (req.nomor_spk ?: "").trim()
        val hasValidSpk = rawSpk.isNotBlank() && rawSpk != "SPK Menyusul" && rawSpk != "-"
        val clientUuid = req.client_uuid?.takeIf { it.isNotBlank() }
            ?: req.client_transaction_id?.takeIf { it.isNotBlank() }
            ?: if (hasValidSpk) "UUID-$rawSpk" else UUID.randomUUID().toString()

        val matchedPlanById = if (renId > 0) rencanaDao.getRencanaById(renId) else null
        val isPlanMatchValid = matchedPlanById != null &&
            matchedPlanById.kodeUnit.equals(req.kode_unit ?: req.unit_code ?: "", ignoreCase = true) &&
            (!hasValidSpk || matchedPlanById.nomorSpk.isBlank() || matchedPlanById.nomorSpk.equals(rawSpk, ignoreCase = true))

        val spk = if (hasValidSpk) {
            rawSpk
        } else if (isPlanMatchValid && matchedPlanById != null) {
            matchedPlanById.nomorSpk.takeIf { it.isNotBlank() && it != "SPK Menyusul" && it != "-" }
                ?: "SPK-AUTO-$renId"
        } else {
            "SPK-AUTO-$clientUuid"
        }

        // Cari record yang sudah ada berdasarkan nomorSpk unik atau clientUuid (JANGAN mencocokkan renId buta jika SPK berbeda!)
        val existingReport = (if (hasValidSpk) reportDao.getReportByNomorSpk(spk) else null)
            ?: reportDao.getReportByUuid(clientUuid)
        val targetReportId = existingReport?.id ?: 0L
        val effectiveClientUuid = existingReport?.clientUuid ?: clientUuid
        val effectiveRenId = if (isPlanMatchValid) renId else (existingReport?.idRencanaKerja ?: 0L)

        val dateVal = req.tanggal ?: req.report_date ?: existingReport?.reportDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val supervisor = req.nama_pengawas ?: req.supervisor_name ?: existingReport?.supervisorName ?: ""
        val unit = req.kode_unit ?: req.unit_code ?: existingReport?.unitCode ?: ""
        val opName = req.nama_operator?.takeIf { it.isNotBlank() } ?: existingReport?.operatorName ?: ""
        val opNik = req.operator_nik?.takeIf { it.isNotBlank() } ?: existingReport?.operatorNik ?: ""
        val hmAwal = reqHmAwal
        val hmAkhir = reqHmAkhir
        val hmDaily = reqHmDaily
        val fuel = req.pemakaian_solar ?: req.jumlah_liter_solar ?: existingReport?.pemakaianSolar ?: 0.0
        val isLocked = existingReport?.isLocked == true || req.is_locked == true || req.approval_status.equals("Disetujui Kasie", ignoreCase = true)
        val approvalStatus = if (isLocked) "Disetujui Kasie" else (req.approval_status ?: existingReport?.approvalStatus ?: "Menunggu Approval")

        val reportEntity = ActivityReportEntity(
            id = targetReportId,
            clientUuid = effectiveClientUuid,
            reportDate = dateVal,
            supervisorName = supervisor,
            namaKasie = supervisor,
            createdBy = req.role_penginput ?: existingReport?.createdBy ?: supervisor,
            unitCode = unit,
            unitType = req.jenis_unit?.takeIf { it.isNotBlank() } ?: existingReport?.unitType ?: "",
            activityName = req.nama_aktivitas?.takeIf { it.isNotBlank() } ?: existingReport?.activityName ?: "",
            sapCode = req.kode_sap?.takeIf { it.isNotBlank() } ?: existingReport?.sapCode ?: "ACT-SAP-101",
            unitOfMeasure = req.satuan?.takeIf { it.isNotBlank() } ?: existingReport?.unitOfMeasure ?: "m3",
            operatorName = opName,
            operatorNik = opNik,
            workLocation = req.lokasi_kerja?.takeIf { it.isNotBlank() } ?: existingReport?.workLocation ?: "",
            wilayah = req.wilayah?.takeIf { it.isNotBlank() } ?: existingReport?.wilayah ?: "PG1",
            shift = req.shift?.takeIf { it.isNotBlank() } ?: existingReport?.shift ?: "SIANG",
            workDurationHours = req.durasi_jam_kerja ?: existingReport?.workDurationHours ?: 7.0,
            hmAwal = hmAwal,
            hmAkhir = hmAkhir,
            hmDailyRunning = hmDaily,
            workOutput = reqWorkOutput,
            notes = req.keterangan?.takeIf { it.isNotBlank() } ?: existingReport?.notes ?: "",
            unitStatus = req.status_unit?.takeIf { it.isNotBlank() } ?: existingReport?.unitStatus ?: "OPERASI",
            isRefueled = req.is_isi_solar == true || fuel > 0.0,
            fuelLiters = fuel,
            pemakaianSolar = fuel,
            approvalStatus = approvalStatus,
            approvedBy = req.approved_by ?: existingReport?.approvedBy,
            approvedAt = req.approved_at ?: existingReport?.approvedAt,
            isLocked = isLocked,
            nomorSpk = spk,
            idRencanaKerja = effectiveRenId,
            createdAt = existingReport?.createdAt ?: System.currentTimeMillis(),
            isSynced = true
        )

        val insertedReportId = reportDao.insertReport(reportEntity)
        val savedReport = reportEntity.copy(id = insertedReportId)

        // Cari atau simpan juga ke hasil_input_aktivitas tanpa duplikasi
        val existingHasil = (if (hasValidSpk) hasilDao.getHasilByNomorSpk(spk) else null)
            ?: hasilDao.getHasilByTxId(effectiveClientUuid)
        val targetHasilId = existingHasil?.id ?: 0L

        val hasilEntity = HasilInputAktivitasEntity(
            id = targetHasilId,
            rencanaId = effectiveRenId,
            nomorSpk = spk,
            supervisorName = supervisor,
            namaKasie = supervisor,
            createdBy = opName,
            kodeUnit = unit,
            operator = opName,
            operatorNik = opNik,
            kodeLokasi = reportEntity.workLocation,
            activityName = reportEntity.activityName,
            sapCode = reportEntity.sapCode,
            satuan = reportEntity.unitOfMeasure,
            unitStatus = reportEntity.unitStatus,
            shift = reportEntity.shift,
            jamKerja = reportEntity.workDurationHours,
            hmAwal = hmAwal,
            hmAkhir = hmAkhir,
            hmHarianBerjalan = hmDaily,
            hasilKerja = reportEntity.workOutput,
            keterangan = reportEntity.notes,
            isIsiSolar = reportEntity.isRefueled,
            jumlahLiterSolar = fuel,
            pemakaianSolar = fuel,
            statusKirim = "SYNCED",
            isSynced = true,
            approvalStatus = approvalStatus,
            approvedBy = reportEntity.approvedBy,
            approvedAt = reportEntity.approvedAt,
            isLocked = isLocked,
            tanggalFormat = dateVal,
            createdAt = existingHasil?.createdAt ?: reportEntity.createdAt,
            clientTransactionId = effectiveClientUuid
        )
        hasilDao.insertHasil(hasilEntity)

        // WAJIB update HANYA berdasarkan Nomor SPK unik atau ID rencana yang terverifikasi cocok
        if (hasValidSpk) {
            rencanaDao.updateStatus(
                nomorSpk = rawSpk,
                id = 0L,
                status = if (isLocked) "Terealisasi & Approved" else "Terealisasi"
            )
        } else if (isPlanMatchValid && effectiveRenId > 0) {
            rencanaDao.markTaskAsCompleted(effectiveRenId)
            if (isLocked) {
                rencanaDao.updateStatusById(
                    rencanaId = effectiveRenId,
                    status = "Terealisasi & Approved",
                    isRealized = true,
                    statusTahap = "REALISASI_SELESAI"
                )
            }
        }
        return savedReport
    }

    suspend fun submitHasilAktivitas(hasil: HasilInputAktivitasEntity): Result<HasilInputAktivitasEntity> = withContext(Dispatchers.IO) {
        try {
            val currentOpNik = hasil.operatorNik ?: ""
            val resolvedOpNik = if (currentOpNik.isNotBlank() && !currentOpNik.equals(hasil.operator, ignoreCase = true)) {
                currentOpNik
            } else {
                operatorDao.findActiveOperatorByNikOrName(hasil.operator)?.badgeNumber?.removePrefix("NIK-")?.trim()
                    ?: SupervisorDirectory.findOperatorByNameOrNik(hasil.operator)?.nik
                    ?: currentOpNik.ifBlank { hasil.operator }
            }
            val effectiveSpk = hasil.nomorSpk.trim().takeIf {
                it.isNotBlank() && it != "SPK Menyusul" && it != "-" && it != "Belum Terbit (Menunggu Admin)"
            } ?: (if (hasil.rencanaId > 0) {
                rencanaDao.getRencanaById(hasil.rencanaId)?.nomorSpk?.trim()?.takeIf {
                    it.isNotBlank() && it != "SPK Menyusul" && it != "-"
                } ?: "SPK-AUTO-${hasil.rencanaId}"
            } else {
                "SPK-AUTO-${hasil.clientTransactionId.ifBlank { UUID.randomUUID().toString() }}"
            })
            val effectiveTxId = hasil.clientTransactionId.ifBlank { "TX-$effectiveSpk" }

            // Cek apakah sudah ada record dengan nomor_spk atau rencana_id yang sama agar tidak duplikat
            val existingHasil = hasilDao.getHasilByNomorSpk(effectiveSpk)
                ?: (if (hasil.rencanaId > 0) hasilDao.getHasilByRencanaId(hasil.rencanaId) else null)
                ?: hasilDao.getHasilByTxId(effectiveTxId)

            val hasilWithNik = hasil.copy(
                id = existingHasil?.id ?: hasil.id,
                nomorSpk = effectiveSpk,
                clientTransactionId = effectiveTxId,
                operatorNik = resolvedOpNik,
                isSynced = false
            )
            val insertedId = hasilDao.insertHasil(hasilWithNik)
            val savedHasil = hasilWithNik.copy(id = insertedId)

            // Update unit HM in local DB
            unitDao.updateUnitHm(savedHasil.kodeUnit, savedHasil.hmAkhir)

            // WAJIB update HANYA berdasarkan ID unik rencana yang dikerjakan (DILARANG KERAS update berdasarkan kodeUnit atau tanggal!)
            if (savedHasil.rencanaId > 0) {
                rencanaDao.markTaskAsCompleted(savedHasil.rencanaId)
                if (savedHasil.isLocked) {
                    rencanaDao.updateStatusById(
                        rencanaId = savedHasil.rencanaId,
                        status = "Terealisasi & Approved",
                        isRealized = true,
                        statusTahap = "REALISASI_SELESAI"
                    )
                } else {
                    rencanaDao.updateStatusById(
                        rencanaId = savedHasil.rencanaId,
                        status = "Terealisasi (Menunggu Approval)",
                        isRealized = true,
                        statusTahap = "REALISASI_SELESAI"
                    )
                }
                CompanyServerSimulator.markRencanaRealized(savedHasil.rencanaId, effectiveSpk)
            } else if (effectiveSpk.isNotBlank() && !effectiveSpk.startsWith("SPK-AUTO-")) {
                rencanaDao.updateStatus(
                    nomorSpk = effectiveSpk,
                    id = 0L,
                    status = if (savedHasil.isLocked) "Terealisasi & Approved" else "Terealisasi"
                )
                CompanyServerSimulator.markRencanaRealized(0L, effectiveSpk)
            }

            val kendalaPayloads = KendalaJsonConverter.fromJson(savedHasil.daftarKendalaJson).map { it.toCanonical() }

            val existingReport = reportDao.getReportByNomorSpk(effectiveSpk)
                ?: (if (savedHasil.rencanaId > 0) reportDao.getReportByRencanaId(savedHasil.rencanaId) else null)
                ?: reportDao.getReportByUuid(effectiveTxId)

            // Mirror ke ActivityReportEntity (1 record unik per nomorSpk)
            val reportMirror = ActivityReportEntity(
                id = existingReport?.id ?: 0L,
                clientUuid = effectiveTxId,
                reportDate = savedHasil.tanggalFormat.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
                supervisorName = savedHasil.supervisorName,
                namaKasie = savedHasil.namaKasie.ifBlank { savedHasil.supervisorName },
                createdBy = savedHasil.createdBy.ifBlank { savedHasil.supervisorName },
                unitCode = savedHasil.kodeUnit,
                unitType = existingReport?.unitType ?: "",
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
                approvalStatus = savedHasil.approvalStatus,
                approvedBy = savedHasil.approvedBy,
                approvedAt = savedHasil.approvedAt,
                isLocked = savedHasil.isLocked,
                operatorNik = savedHasil.operatorNik,
                nomorSpk = effectiveSpk,
                idRencanaKerja = savedHasil.rencanaId,
                isSynced = false
            )
            val mirrorReportId = reportDao.insertReport(reportMirror)
            val activeUser = userDao.getActiveUser()
            val rawWilayah = reportMirror.wilayah.takeIf { it.isNotBlank() } ?: activeUser?.wilayah ?: "PG1"
            val targetWilayah = if (rawWilayah.equals("Geodesi PG1", ignoreCase = true) || rawWilayah.equals("Geodesi", ignoreCase = true)) "PG1" else rawWilayah

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

            val rolePenginputVal = if (savedHasil.isLocked) "kasie" else "operator"

            val apiReq = PostHasilAktivitasRequest(
                id = insertedId,
                rencana_id = savedHasil.rencanaId,
                id_rencana_kerja = savedHasil.rencanaId,
                nomor_spk = spkClean,
                jam_kerja = savedHasil.jamKerja,
                durasi_jam_kerja = savedHasil.jamKerja,
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
                kasie = savedHasil.namaKasie.ifBlank { savedHasil.supervisorName },
                kode_unit = savedHasil.kodeUnit,
                jenis_unit = reportMirror.unitType,
                nama_operator = savedHasil.operator,
                operator = savedHasil.operator,
                lokasi_kerja = savedHasil.kodeLokasi,
                kode_lokasi = savedHasil.kodeLokasi,
                nama_aktivitas = savedHasil.activityName,
                kode_sap = savedHasil.sapCode,
                satuan = savedHasil.satuan,
                status_unit = savedHasil.unitStatus,
                foto_bukti = savedHasil.photoBase64,
                is_hm_awal_corrected = savedHasil.isHmAwalCorrected,
                alasan_koreksi_hm = savedHasil.alasanKoreksiHm,
                client_transaction_id = savedHasil.clientTransactionId,
                wilayah = targetWilayah,
                approval_status = savedHasil.approvalStatus,
                approved_by = savedHasil.approvedBy,
                approved_at = savedHasil.approvedAt,
                is_locked = savedHasil.isLocked,
                operator_nik = savedHasil.operatorNik,
                role_penginput = rolePenginputVal,
                tanggal = savedHasil.tanggalFormat,
                tanggal_kerja = savedHasil.tanggalFormat,
                shift = savedHasil.shift
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
                wilayah = targetWilayah,
                approvalStatus = savedHasil.approvalStatus,
                approvedBy = savedHasil.approvedBy,
                approvedAt = savedHasil.approvedAt,
                isLocked = savedHasil.isLocked,
                operatorNik = savedHasil.operatorNik,
                rolePenginput = rolePenginputVal
            )

            try {
                // 1. Kirim HTTP POST langsung ke backend Web Admin (/api/realisasi, /api/aktivitas-unit, /api/hasil-aktivitas)
                apiClientManager.submitRealisasiToServer(apiActivityReq, apiReq)
                val syncTimestamp = System.currentTimeMillis()
                // 2. Update langsung SQLite/Room lokal: is_synced = true dan status_sync = 'SYNCED'
                hasilDao.markAsSynced(insertedId, syncTimestamp)
                reportDao.markReportAsSynced(mirrorReportId, syncTimestamp)
                draftDao.deleteDraftByRencanaId(savedHasil.rencanaId)
                _lastSyncTimeMillis.value = syncTimestamp
                val kendalaTag = if (kendalaPayloads.isNotEmpty()) " [Kendala: ${kendalaPayloads.size} item]" else ""
                addLog("POST /api/realisasi", "POST", "200 OK", "Realisasi Unit ${savedHasil.kodeUnit} (HM ${savedHasil.hmAkhir})$kendalaTag", true)
                Result.success(savedHasil.copy(isSynced = true, statusKirim = "SYNCED", syncedAt = syncTimestamp))
            } catch (netEx: Exception) {
                android.util.Log.e("HTTP_REALISASI", "Exception saat kirim realisasi: ${netEx.message}", netEx)
                addLog("POST /api/realisasi", "POST", "OFFLINE/PENDING", "Gagal kirim ke server (${netEx.message}). Realisasi Unit ${savedHasil.kodeUnit} disimpan lokal", false)
                // Kembalikan Result.failure agar ViewModel menampilkan Toast gagal sinkron, sementara data tetap tersimpan lokal (is_synced = false)
                Result.failure(netEx)
            }
        } catch (e: Exception) {
            android.util.Log.e("HTTP_REALISASI", "Exception saat submitHasilAktivitas: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun submitRealisasiLangsung(
        request: com.example.data.remote.RealisasiRequest,
        context: android.content.Context
    ): Result<Unit> {
        val realisasiRepo = RealisasiRepository(
            apiClientManager = apiClientManager,
            realisasiDao = hasilDao,
            rencanaKerjaDao = rencanaDao,
            reportDao = reportDao
        )
        return realisasiRepo.submitRealisasiLangsung(request, context)
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
                    if (report.isLocked || report.approvalStatus.contains("Disetujui", ignoreCase = true)) {
                        try {
                            apiClientManager.approveRealisasi(report.id, report.approvedBy ?: report.supervisorName)
                        } catch (_: Exception) {}
                    }
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

    suspend fun refreshTugasOperatorFromServer(operatorIdentifier: String): Result<Pair<List<RencanaKerjaEntity>, String>> = withContext(Dispatchers.IO) {
        try {
            val (incomingRequests, msg) = apiClientManager.fetchActiveRencanaKerja(operatorIdentifier)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val existingLocalPlans = rencanaDao.getAllRencanaList()
            val serverEntities = mutableListOf<RencanaKerjaEntity>()

            for ((index, renReq) in incomingRequests.withIndex()) {
                val kodeUnitVal = renReq.resolvedUnitKode.ifBlank { "UNIT-${index + 1}" }
                val unitEntity = unitDao.getUnitByCode(kodeUnitVal)
                val spkNum = renReq.nomor_spk?.trim() ?: ""
                val spkStatus = when {
                    !renReq.status_spk.isNullOrBlank() -> renReq.status_spk
                    spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" -> "SPK_TERBIT"
                    else -> "MENUNGGU_SPK"
                }
                val serverId = renReq.numericId
                val matchedLocal = existingLocalPlans.find { loc ->
                    (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" && loc.nomorSpk.equals(spkNum, ignoreCase = true)) ||
                    (serverId > 0 && loc.id == serverId && (spkNum.isBlank() || loc.nomorSpk.isBlank() || loc.nomorSpk.equals(spkNum, ignoreCase = true)))
                }
                val maxLocalId = (existingLocalPlans.maxOfOrNull { it.id } ?: 0L) + serverEntities.size + 100L
                val targetId = when {
                    matchedLocal != null && matchedLocal.id > 0 -> matchedLocal.id
                    serverId > 0 && existingLocalPlans.none { it.id == serverId } && serverEntities.none { it.id == serverId } -> serverId
                    else -> maxLocalId + index + 1L
                }
                val resolvedOpName = renReq.resolvedOperator
                val resolvedOpNik = matchedLocal?.operatorNik?.takeIf { it.isNotBlank() }
                    ?: operatorDao.findActiveOperatorByNikOrName(resolvedOpName)?.badgeNumber?.removePrefix("NIK-")?.trim()
                    ?: SupervisorDirectory.findOperatorByNameOrNik(resolvedOpName)?.nik
                    ?: operatorIdentifier
                val reportBySpk = if (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-") reportDao.getReportByNomorSpk(spkNum) else null
                val hasilBySpk = if (spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-") hasilDao.getHasilByNomorSpk(spkNum) else null
                val hasValidReportInDb = reportBySpk != null && reportBySpk.hmAkhir > 0.0 &&
                    (reportBySpk.hmDailyRunning > 0.0 || reportBySpk.hmAkhir > reportBySpk.hmAwal || reportBySpk.workOutput > 0.0)
                val hasValidHasilInDb = hasilBySpk != null && hasilBySpk.hmAkhir > 0.0 &&
                    (hasilBySpk.hmHarianBerjalan > 0.0 || hasilBySpk.hmAkhir > hasilBySpk.hmAwal || hasilBySpk.hasilKerja > 0.0)
                val hasRealisasiInDb = hasValidReportInDb || hasValidHasilInDb
                val isAlreadyRealized = hasRealisasiInDb || (matchedLocal?.isRealized == true && hasRealisasiInDb)
                val parsedSubIdx = spkNum.split("-").takeIf { it.size >= 4 }?.lastOrNull()?.toIntOrNull()?.takeIf { it in 1..99 } ?: 1
                val resolvedSubIndex = matchedLocal?.subIndex?.takeIf { it > 1 } ?: parsedSubIdx
                val resolvedParentSpk = matchedLocal?.parentSpk?.takeIf { it.isNotBlank() }
                    ?: (if (spkNum.split("-").size >= 4) spkNum.split("-").dropLast(1).joinToString("-") else spkNum)

                val entity = RencanaKerjaEntity(
                    id = targetId,
                    supervisorName = renReq.resolvedPengawas.ifBlank { matchedLocal?.supervisorName ?: "Agung" },
                    pengawas = renReq.resolvedPengawas.ifBlank { matchedLocal?.pengawas ?: "Agung" },
                    kasie = renReq.resolvedPengawas.ifBlank { matchedLocal?.kasie ?: "Agung" },
                    createdBy = renReq.created_by ?: renReq.resolvedPengawas,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = kodeUnitVal,
                    operator = resolvedOpName,
                    namaOperator = resolvedOpName,
                    operatorNik = resolvedOpNik,
                    sapCode = renReq.resolvedSapCode,
                    namaAktivitas = renReq.resolvedAktivitas,
                    satuanAktivitas = renReq.resolvedSatuan,
                    kodeLokasi = renReq.resolvedLokasi,
                    wilayah = renReq.wilayah?.takeIf { it.isNotBlank() } ?: unitEntity?.wilayah ?: "Geodesi PG1",
                    shiftKerja = renReq.resolvedShift,
                    keterangan = renReq.keterangan ?: renReq.keterangan_rencana ?: "",
                    nomorSpk = spkNum,
                    statusSpk = if (isAlreadyRealized) "Terealisasi" else spkStatus,
                    statusTahap = if (isAlreadyRealized) "REALISASI_SELESAI" else (renReq.status_tahap ?: "RENCANA"),
                    statusRealisasi = if (isAlreadyRealized) "Terealisasi" else "Menunggu Realisasi",
                    isRealized = isAlreadyRealized,
                    isSynced = true,
                    createdAt = matchedLocal?.createdAt ?: (System.currentTimeMillis() - (index * 1000L)),
                    tanggalFormat = renReq.tanggal?.takeIf { it.isNotBlank() } ?: today,
                    subIndex = resolvedSubIndex,
                    parentSpk = resolvedParentSpk
                )
                serverEntities.add(entity)
            }

            // FULL MIRROR SYNC untuk tugas operator
            rencanaDao.deletePlansForOperator(operatorIdentifier)
            if (serverEntities.isNotEmpty()) {
                rencanaDao.insertAll(serverEntities)
            }
            _lastSyncTimeMillis.value = System.currentTimeMillis()
            addLog("GET /api/rencana-kerja", "GET", "200 OK", "${serverEntities.size} Tugas Operator Tersinkron (Full Mirror)", true)
            Result.success(Pair(serverEntities, msg))
        } catch (e: Exception) {
            addLog("GET /api/rencana-kerja", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingOperationalData(): Result<Int> = withContext(Dispatchers.IO) {
        var totalSynced = 0
        try {
            // HANYA mengunggah data realisasi / hasil aktivitas dari HP ke Web Admin (SATU ARAH: Rencana Kerja hanya dari Server -> HP)
            // 1. Sync pending hasil aktivitas: Filter hanya record yang benar-benar is_synced == false atau status_kirim == 'PENDING'
            val pendingHasil = hasilDao.getUnsyncedHasilList().filter { !it.isSynced || it.statusKirim == "PENDING" }
            for (hasil in pendingHasil) {
                try {
                    val kendalaPayloads = KendalaJsonConverter.fromJson(hasil.daftarKendalaJson).map { it.toCanonical() }
                    val spkClean = hasil.nomorSpk.takeIf {
                        it.isNotBlank() && it != "SPK Menyusul" && it != "-" && it != "Belum Terbit (Menunggu Admin)"
                    }
                    val rolePenginputVal = if (hasil.isLocked) "kasie" else "operator"
                    val req = PostHasilAktivitasRequest(
                        id = hasil.id,
                        rencana_id = hasil.rencanaId,
                        id_rencana_kerja = hasil.rencanaId,
                        nomor_spk = spkClean,
                        jam_kerja = hasil.jamKerja,
                        durasi_jam_kerja = hasil.jamKerja,
                        hm_awal = hasil.hmAwal,
                        hm_akhir = hasil.hmAkhir,
                        hm_harian_berjalan = hasil.hmHarianBerjalan,
                        is_isi_solar = hasil.isIsiSolar,
                        jumlah_liter_solar = hasil.jumlahLiterSolar,
                        stik_solar_awal = hasil.stikSolarAwal,
                        stik_solar_akhir = hasil.stikSolarAkhir,
                        pemakaian_solar = hasil.pemakaianSolar,
                        kendala_id = hasil.kendalaId,
                        kendala_nama = hasil.kendalaNama,
                        catatan_kendala = hasil.catatanKendala,
                        kendala_lapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        kendala_list = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        daftar_kendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        hasil_kerja = hasil.hasilKerja,
                        keterangan = hasil.keterangan,
                        nama_pengawas = hasil.supervisorName,
                        kasie = hasil.namaKasie.ifBlank { hasil.supervisorName },
                        kode_unit = hasil.kodeUnit,
                        nama_operator = hasil.operator,
                        operator = hasil.operator,
                        lokasi_kerja = hasil.kodeLokasi,
                        kode_lokasi = hasil.kodeLokasi,
                        nama_aktivitas = hasil.activityName,
                        kode_sap = hasil.sapCode,
                        satuan = hasil.satuan,
                        status_unit = hasil.unitStatus,
                        foto_bukti = hasil.photoBase64,
                        is_hm_awal_corrected = hasil.isHmAwalCorrected,
                        alasan_koreksi_hm = hasil.alasanKoreksiHm,
                        client_transaction_id = hasil.clientTransactionId,
                        wilayah = hasil.wilayah,
                        approval_status = hasil.approvalStatus,
                        approved_by = hasil.approvedBy,
                        approved_at = hasil.approvedAt,
                        is_locked = hasil.isLocked,
                        operator_nik = hasil.operatorNik,
                        role_penginput = rolePenginputVal,
                        tanggal = hasil.tanggalFormat,
                        tanggal_kerja = hasil.tanggalFormat,
                        shift = hasil.shift
                    )
                    val reportReq = PostActivityReportRequest.create(
                        clientUuid = hasil.clientTransactionId.ifBlank { "TX-${hasil.id}" },
                        reportDate = hasil.tanggalFormat,
                        supervisorName = hasil.supervisorName,
                        unitCode = hasil.kodeUnit,
                        unitType = "",
                        activityName = hasil.activityName,
                        sapCode = hasil.sapCode,
                        unitOfMeasure = hasil.satuan,
                        operatorName = hasil.operator,
                        workLocation = hasil.kodeLokasi,
                        shift = hasil.shift,
                        workDurationHours = hasil.jamKerja,
                        hmAwal = hasil.hmAwal,
                        hmAkhir = hasil.hmAkhir,
                        hmDailyRunning = hasil.hmHarianBerjalan,
                        workOutput = hasil.hasilKerja,
                        notes = hasil.keterangan,
                        unitStatus = hasil.unitStatus,
                        isRefueled = hasil.isIsiSolar,
                        fuelLiters = hasil.jumlahLiterSolar,
                        stikSolarAwal = hasil.stikSolarAwal,
                        stikSolarAkhir = hasil.stikSolarAkhir,
                        pemakaianSolar = hasil.pemakaianSolar,
                        kendalaId = hasil.kendalaId,
                        kendalaNama = hasil.kendalaNama,
                        catatanKendala = hasil.catatanKendala,
                        kendalaLapangan = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        kendalaList = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        photoBase64 = hasil.photoBase64,
                        photoSizeKb = hasil.photoSizeKb,
                        photoDimensions = hasil.photoDimensions,
                        photoPath = hasil.photoPath,
                        isHmAwalCorrected = hasil.isHmAwalCorrected,
                        alasanKoreksiHm = hasil.alasanKoreksiHm,
                        clientTransactionId = hasil.clientTransactionId,
                        idRencanaKerja = hasil.rencanaId,
                        nomorSpk = spkClean,
                        wilayah = hasil.wilayah,
                        approvalStatus = hasil.approvalStatus,
                        approvedBy = hasil.approvedBy,
                        approvedAt = hasil.approvedAt,
                        isLocked = hasil.isLocked,
                        operatorNik = hasil.operatorNik,
                        rolePenginput = rolePenginputVal
                    )
                    apiClientManager.submitRealisasiToServer(reportReq, req)
                    val now = System.currentTimeMillis()
                    hasilDao.markAsSynced(hasil.id, now)
                    reportDao.markReportAsSyncedByUnitAndHm(hasil.kodeUnit, hasil.hmAkhir, hasil.supervisorName, now)
                    draftDao.deleteDraftByRencanaId(hasil.rencanaId)
                    totalSynced++
                } catch (e: Exception) {
                    android.util.Log.e("HTTP_REALISASI", "Gagal sinkron ulang hasil ${hasil.id}: ${e.message}", e)
                }
            }

            // 2. Sync pending reports (ActivityReportEntity) yang belum tersinkron
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

    suspend fun approveRealisasi(reportId: Long, approvedBy: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestampStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            reportDao.approveReport(reportId, approvedBy, timestampStr)
            try {
                reportDao.updateApprovalStatus(reportId, "Disetujui Kasie", true, approvedBy, timestampStr)
            } catch (_: Exception) {}
            try {
                hasilDao.approveRealization(reportId, approvedBy, timestampStr)
                hasilDao.updateApprovalStatus(reportId, "Disetujui Kasie", true, approvedBy, timestampStr)
            } catch (_: Exception) {}
            val targetReport = reportDao.getReportById(reportId)
            if (targetReport != null) {
                rencanaDao.updateStatus(
                    nomorSpk = targetReport.nomorSpk ?: "",
                    id = targetReport.idRencanaKerja,
                    status = "Terealisasi & Approved"
                )
                if (!targetReport.nomorSpk.isNullOrBlank()) {
                    try {
                        rencanaDao.updateStatus(targetReport.nomorSpk, "Approved")
                    } catch (_: Exception) {}
                }
            }
            try {
                val msg = apiClientManager.approveRealisasi(reportId, approvedBy)
                val now = System.currentTimeMillis()
                reportDao.markReportAsSynced(reportId, now)
                addLog("PATCH /api/realisasi/$reportId/approve", "PATCH", "200 OK", "Disetujui oleh $approvedBy (Terkunci & Synced)", true)
                _lastSyncTimeMillis.value = now
                Result.success(msg)
            } catch (netEx: Exception) {
                reportDao.markReportAsUnsynced(reportId)
                com.example.worker.OfflineSyncWorker.scheduleAutoSync(apiClientManager.context)
                addLog("PATCH /api/realisasi/$reportId/approve", "PATCH", "OFFLINE/PENDING", "Disetujui oleh $approvedBy (Terkunci lokal, masuk antrean sync)", false)
                Result.success("Disetujui Kasie ($approvedBy) & dikunci lokal. Masuk antrean sync offline.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun correctRealisasi(updatedReport: ActivityReportEntity): Result<ActivityReportEntity> = withContext(Dispatchers.IO) {
        try {
            reportDao.updateReport(updatedReport)
            unitDao.updateUnitHm(updatedReport.unitCode, updatedReport.hmAkhir)
            addLog("UPDATE /api/realisasi/${updatedReport.id}", "PUT", "200 OK", "Koreksi Kasie: Unit ${updatedReport.unitCode} (HM ${updatedReport.hmAkhir})", true)
            Result.success(updatedReport)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
