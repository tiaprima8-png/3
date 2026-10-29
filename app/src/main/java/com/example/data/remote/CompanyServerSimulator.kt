package com.example.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class SpkPublishedEvent(
    val planId: Long,
    val kodeUnit: String,
    val nomorSpk: String,
    val supervisorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class RencanaDeletedEvent(
    val planId: Long,
    val unitKode: String,
    val supervisorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Simulates company central backend and Web Admin database.
 * Supports:
 * - GET /api/master-data (filtered by supervisor)
 * - POST /api/aktivitas-unit
 * - Admin web updates (tambah, edit, hapus master data)
 * - Offline simulation toggle
 * - WebSocket / Polling simulation for SPK_PUBLISHED
 */
object CompanyServerSimulator {
    var isSimulatedOffline: Boolean = false

    private val _spkEventFlow = MutableSharedFlow<SpkPublishedEvent>(extraBufferCapacity = 64)
    val spkEventFlow = _spkEventFlow.asSharedFlow()

    private val _rencanaDeletedEventFlow = MutableSharedFlow<RencanaDeletedEvent>(extraBufferCapacity = 64)
    val rencanaDeletedEventFlow = _rencanaDeletedEventFlow.asSharedFlow()

    // Map of planId -> published SPK
    val publishedSpkMap = ConcurrentHashMap<Long, String>()

    // Central master users in company web admin (12 akun user aktif)
    val serverUsers = CopyOnWriteArrayList<UserApiPayload>().apply {
        addAll(createDefaultServerUsers())
    }

    // Central master units in company web admin (39 unit alat berat aktif)
    val serverUnits = CopyOnWriteArrayList<UnitPayload>().apply {
        addAll(createDefaultServerUnits())
    }

    // Central master kendala operasional in company web admin (15 master kendala aktif)
    val serverKendala = CopyOnWriteArrayList<KendalaPayload>().apply {
        addAll(createDefaultServerKendala())
    }

    // Central master activities in company web admin (107 master aktivitas aktif)
    val serverActivities = CopyOnWriteArrayList<ActivityPayload>().apply {
        addAll(createDefaultServerActivities())
    }

    // Central master operators in company web admin (45 master operator aktif)
    val serverOperators = CopyOnWriteArrayList<OperatorPayload>().apply {
        addAll(createDefaultServerOperators())
    }

    // Central master lokasi in company web admin (7 master lokasi aktif)
    val serverLokasi = CopyOnWriteArrayList<LokasiPayload>().apply {
        addAll(createDefaultServerLokasi())
    }

    // Central received reports in Web Admin
    val receivedReportsOnServer = CopyOnWriteArrayList<PostActivityReportRequest>()
    val serverRencanaKerja = CopyOnWriteArrayList<PostRencanaKerjaRequest>().apply {
        add(
            PostRencanaKerjaRequest(
                id = "REN-HEH0013",
                id_rencana_kerja = "REN-HEH0013",
                rencana_id = "REN-HEH0013",
                status_unit = "OPERASI",
                kode_unit = "HEH0013",
                unit_kode = "HEH0013",
                unit = "HEH0013",
                nama_unit = "Komatsu PC200-8",
                operator = "Ahmad Fauzi",
                nama_operator = "Ahmad Fauzi",
                sap_code = "ACT-SAP-101",
                kode_sap = "ACT-SAP-101",
                nama_aktivitas = "Galian Tanah Lunak",
                aktivitas = "Galian Tanah Lunak",
                satuan_aktivitas = "m3",
                satuan = "m3",
                kode_lokasi = "001A",
                lokasi = "001A",
                wilayah = "Geodesi PG1",
                shift_kerja = "SIANG",
                shift = "SIANG",
                keterangan = "Penggalian front cut site A",
                keterangan_rencana = "Penggalian front cut site A",
                nomor_spk = null,
                status_spk = "MENUNGGU_SPK",
                status_tahap = "RENCANA",
                is_realized = false,
                nama_pengawas = "Agung",
                pengawas = "Agung",
                tanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                tanggal_kerja = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )
        )
    }
    val serverHasilAktivitas = CopyOnWriteArrayList<PostHasilAktivitasRequest>()

    suspend fun fetchMasterData(supervisorName: String): ApiResponse<MasterDataPayload> {
        delay(600) // network latency
        if (isSimulatedOffline) {
            throw java.io.IOException("Koneksi gagal: Perangkat berada di luar jangkauan jaringan (Offline Lapangan)")
        }

        cleanDumpTruckAndBulldozer()

        // Supervisor isolation
        val filteredUnits = serverUnits.filter { 
            com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) &&
            (it.resolvedSupervisor.equals(supervisorName, ignoreCase = true) || supervisorName.equals("Semua", ignoreCase = true))
        }
        val filteredOperators = serverOperators.filter { it.resolvedSupervisor.equals(supervisorName, ignoreCase = true) }
        val activities = serverActivities.toList()
        val lokasi = serverLokasi.toList()
        val kendala = serverKendala.toList()

        return ApiResponse(
            status = "SUCCESS",
            message = "Data master berhasil disinkronkan dari Web Admin untuk pengawas: $supervisorName",
            data = MasterDataPayload(
                units = filteredUnits,
                activities = activities,
                aktivitas = activities,
                operators = filteredOperators,
                lokasi = lokasi,
                locations = lokasi,
                kendala = kendala
            )
        )
    }

    suspend fun fetchBootstrapData(pengawas: String, tanggal: String): ApiResponse<BootstrapDataPayload> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Koneksi gagal: Perangkat berada di luar jangkauan jaringan (Offline Lapangan)")
        }

        cleanDumpTruckAndBulldozer()

        // 1. Filter Master Data for supervisor
        val filteredUnits = serverUnits.filter { 
            com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) &&
            it.resolvedSupervisor.equals(pengawas, ignoreCase = true) 
        }
        val filteredOperators = serverOperators.filter { it.resolvedSupervisor.equals(pengawas, ignoreCase = true) }
        val activities = serverActivities.toList()
        val lokasi = serverLokasi.toList()
        val kendala = serverKendala.toList()
        val masterData = MasterDataPayload(
            units = filteredUnits,
            activities = activities,
            aktivitas = activities,
            operators = filteredOperators,
            lokasi = lokasi,
            locations = lokasi,
            kendala = kendala
        )

        // 2. Active plans for supervisor (Synchronized with live Web Admin database)
        val cleanPengawas = pengawas.trim().removePrefix("@")
        val ownedUnitCodes = filteredUnits.map { it.resolvedUnitCode.uppercase() }.toSet()
        val activePlans = serverRencanaKerja.filter {
            (it.resolvedPengawas.equals(cleanPengawas, ignoreCase = true) ||
             it.nama_pengawas.equals(cleanPengawas, ignoreCase = true)) &&
            (ownedUnitCodes.isEmpty() || it.resolvedUnitKode.uppercase() in ownedUnitCodes) &&
            it.is_realized != true &&
            it.status_tahap == "RENCANA"
        }

        // 3. Initial historical activity reports on server ONLY for Agung (owner of HEH0013, CMP-01, GRD-01)
        if (cleanPengawas.equals("Agung", ignoreCase = true) &&
            receivedReportsOnServer.none { it.nama_pengawas.equals("Agung", ignoreCase = true) || it.supervisor_name.equals("Agung", ignoreCase = true) }
        ) {
            receivedReportsOnServer.addAll(
                listOf(
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = "Agung",
                        kode_unit = "HEH0013",
                        jenis_unit = "Excavator Standar",
                        nama_aktivitas = "Galian Tanah Lunak",
                        kode_sap = "ACT-SAP-101",
                        satuan = "m3",
                        nama_operator = "Ahmad Fauzi",
                        lokasi_kerja = "001A",
                        shift = "SIANG",
                        durasi_jam_kerja = 7.0,
                        hm_awal = 11.0,
                        hm_akhir = 12.0,
                        hm_harian_berjalan = 1.0,
                        hasil_kerja = 450.0,
                        keterangan = "Penggalian front cut berjalan lancar dan aman.",
                        status_unit = "OPERASI",
                        is_isi_solar = true,
                        jumlah_liter_solar = 120.0,
                        stik_solar_awal = 80.0,
                        stik_solar_akhir = 48.0,
                        pemakaian_solar = 152.0,
                        client_uuid = "SRV-REP-Agung-HEH0013-12.0",
                        client_transaction_id = "TX-SRV-001"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = "Agung",
                        kode_unit = "CMP-01",
                        jenis_unit = "Compactor",
                        nama_aktivitas = "Pemadatan Subgrade",
                        kode_sap = "ACT-SAP-201",
                        satuan = "m2",
                        nama_operator = "Eko Prasetyo",
                        lokasi_kerja = "002B",
                        shift = "SIANG",
                        durasi_jam_kerja = 6.0,
                        hm_awal = 2094.0,
                        hm_akhir = 2100.0,
                        hm_harian_berjalan = 6.0,
                        hasil_kerja = 850.0,
                        keterangan = "Pemadatan subgrade area disposal selesai padat.",
                        status_unit = "OPERASI",
                        is_isi_solar = true,
                        jumlah_liter_solar = 75.0,
                        stik_solar_awal = 60.0,
                        stik_solar_akhir = 35.0,
                        pemakaian_solar = 100.0,
                        client_uuid = "SRV-REP-Agung-CMP01-2100.0",
                        client_transaction_id = "TX-SRV-002"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = "Agung",
                        kode_unit = "GRD-01",
                        jenis_unit = "Motor Grader",
                        nama_aktivitas = "Grading Mainroad Tambang",
                        kode_sap = "ACT-SAP-202",
                        satuan = "m",
                        nama_operator = "Hendra Gunawan",
                        lokasi_kerja = "MAINROAD-01",
                        shift = "SIANG",
                        durasi_jam_kerja = 6.5,
                        hm_awal = 1863.5,
                        hm_akhir = 1870.0,
                        hm_harian_berjalan = 6.5,
                        hasil_kerja = 1250.0,
                        keterangan = "Perawatan dan grading mainroad hauling jalan lancar.",
                        status_unit = "OPERASI",
                        is_isi_solar = false,
                        jumlah_liter_solar = 0.0,
                        client_uuid = "SRV-REP-Agung-GRD01-1870.0",
                        client_transaction_id = "TX-SRV-003"
                    )
                )
            )
        }

        val supervisorReports = receivedReportsOnServer.filter {
            (it.nama_pengawas.equals(cleanPengawas, ignoreCase = true) ||
             it.supervisor_name.equals(cleanPengawas, ignoreCase = true)) &&
            (ownedUnitCodes.isEmpty() || (it.kode_unit ?: it.unit_code ?: "").uppercase() in ownedUnitCodes)
        }

        return ApiResponse(
            status = "SUCCESS",
            message = "Data bootstrap operasional pengawas $pengawas berhasil diambil (${activePlans.size} rencana, ${supervisorReports.size} riwayat)",
            data = BootstrapDataPayload(
                units = masterData.units,
                operators = masterData.operators,
                activities = masterData.activities,
                master_data = masterData,
                masterData = masterData,
                rencana_aktif = activePlans,
                rencanaAktif = activePlans,
                riwayat_aktivitas = supervisorReports,
                riwayatAktivitas = supervisorReports
            )
        )
    }

    suspend fun getActiveRencanaKerja(pengawas: String): ApiResponse<List<PostRencanaKerjaRequest>> {
        delay(200)
        val cleanPengawas = pengawas.trim().removePrefix("@")
        val isAll = cleanPengawas.equals("Semua", ignoreCase = true) || cleanPengawas.isBlank()
        val ownedUnitCodes = if (isAll) emptySet() else serverUnits
            .filter { it.resolvedSupervisor.equals(cleanPengawas, ignoreCase = true) }
            .map { it.resolvedUnitCode.uppercase() }
            .toSet()

        val activePlans = serverRencanaKerja.filter { plan ->
            plan.is_realized != true &&
            if (isAll) {
                true
            } else {
                val matchesKasie = plan.resolvedPengawas.equals(cleanPengawas, ignoreCase = true) ||
                        plan.nama_pengawas.equals(cleanPengawas, ignoreCase = true)
                val matchesUnit = ownedUnitCodes.isEmpty() || plan.resolvedUnitKode.uppercase() in ownedUnitCodes
                matchesKasie && matchesUnit
            }
        }
        return ApiResponse(
            status = "SUCCESS",
            message = "Rencana kerja aktif pengawas $pengawas berhasil diambil (${activePlans.size} rencana)",
            data = activePlans
        )
    }

    suspend fun postRencanaKerja(request: PostRencanaKerjaRequest): ApiResponse<String> {
        delay(500)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Rencana kerja tersimpan di lokal (MENUNGGU_SPK)")
        }
        serverRencanaKerja.add(request)

        return ApiResponse(
            status = "SUCCESS",
            message = "Rencana kerja Unit ${request.kode_unit ?: ""} berhasil dicatat di Web Admin. Status: MENUNGGU_SPK",
            data = "MENUNGGU_SPK"
        )
    }

    /**
     * Manually or immediately triggers admin SPK publishing for a plan
     */
    suspend fun adminPublishSpk(planId: Long, kodeUnit: String, supervisorName: String): String {
        val spkNum = "SPK-2026-X" + (100 + (planId % 900))
        publishedSpkMap[planId] = spkNum
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId.toString() || it.numericId == planId || it.kode_unit == kodeUnit }
        if (idx >= 0) {
            val old = serverRencanaKerja[idx]
            serverRencanaKerja[idx] = old.copy(nomor_spk = spkNum, status_spk = "TERBIT_SPK")
        }
        _spkEventFlow.emit(
            SpkPublishedEvent(
                planId = planId,
                kodeUnit = kodeUnit,
                nomorSpk = spkNum,
                supervisorName = supervisorName
            )
        )
        return spkNum
    }

    /**
     * Manually triggers admin deleting a work plan (simulates Web Admin deletion)
     */
    suspend fun adminDeleteRencana(planId: Long, unitKode: String, supervisorName: String = "") {
        serverRencanaKerja.removeIf {
            (planId > 0 && (it.numericId == planId || it.id == planId.toString())) ||
            (unitKode.isNotBlank() && it.resolvedUnitKode.equals(unitKode, ignoreCase = true))
        }
        publishedSpkMap.remove(planId)
        _rencanaDeletedEventFlow.emit(
            RencanaDeletedEvent(
                planId = planId,
                unitKode = unitKode,
                supervisorName = supervisorName
            )
        )
    }

    /**
     * Manually triggers admin deleting an activity report (simulates Web Admin deletion)
     */
    suspend fun adminDeleteReport(reportId: Long, clientUuid: String = "") {
        receivedReportsOnServer.removeIf {
            (clientUuid.isNotBlank() && (it.client_uuid == clientUuid || it.client_transaction_id == clientUuid)) ||
            (reportId > 0 && it.id_rencana_kerja == reportId)
        }
    }

    fun markRencanaRealized(planId: Long) {
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId.toString() || it.numericId == planId }
        if (idx >= 0) {
            val old = serverRencanaKerja[idx]
            serverRencanaKerja[idx] = old.copy(
                is_realized = true,
                status_tahap = "REALISASI_SELESAI",
                status_spk = "SELESAI"
            )
        }
    }

    fun getPublishedSpk(planId: Long): String? = publishedSpkMap[planId]

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): ApiResponse<String> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil kerja disimpan ke penyimpanan lokal")
        }

        // Anti-duplikasi di server simulator
        val txId = request.client_transaction_id
        if (!txId.isNullOrBlank() && serverHasilAktivitas.any { it.client_transaction_id == txId }) {
            return ApiResponse(
                status = "SUCCESS",
                message = "Realisasi Unit ${request.kode_unit ?: ""} sudah tercatat di Web Admin (Anti-Duplikat Terverifikasi).",
                data = "ACK-IDEMPOTENT"
            )
        }

        serverHasilAktivitas.add(request)

        val targetUnitCode = request.kode_unit ?: ""
        val lastHmVal = request.hm_akhir ?: 0.0

        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val kendalaItems = request.kendala_lapangan ?: request.kendala_list ?: request.daftar_kendala
        val kendalaInfo = if (!kendalaItems.isNullOrEmpty()) {
            " [Kendala: ${kendalaItems.joinToString { "${it.nama_kendala} [${it.kode_kendala}]" }}]"
        } else ""
        val fuelInfo = if (request.is_isi_solar == true) " [Solar: ${request.jumlah_liter_solar} L]" else ""
        val auditInfo = if (request.is_hm_awal_corrected == true) " [AUDIT ADMIN: Koreksi HM Awal Manual (Alasan: ${request.alasan_koreksi_hm ?: "-"})]" else ""
        return ApiResponse(
            status = "SUCCESS",
            message = "Realisasi Unit $targetUnitCode berhasil dikirim.$kendalaInfo$fuelInfo$auditInfo HM Unit diperbarui ke $lastHmVal.",
            data = "ACK-${System.currentTimeMillis()}"
        )
    }

    suspend fun postActivityReport(report: PostActivityReportRequest): ApiResponse<String> {
        delay(700)
        if (isSimulatedOffline) {
            throw java.io.IOException("Jaringan tidak tersedia: Laporan disimpan ke penyimpanan lokal perangkat")
        }

        // Anti-duplikasi di server simulator
        val clientUuidVal = report.client_uuid ?: report.client_transaction_id
        if (!clientUuidVal.isNullOrBlank() && receivedReportsOnServer.any { (it.client_uuid ?: it.client_transaction_id) == clientUuidVal }) {
            return ApiResponse(
                status = "SUCCESS",
                message = "Laporan sudah tercatat sebelumnya di server Web Admin (Anti-Duplikat Terverifikasi).",
                data = "REPORT-ACK-IDEMPOTENT"
            )
        }

        // Add to server log
        receivedReportsOnServer.add(report)

        val targetUnitCode = report.kode_unit ?: report.unit_code ?: ""
        val lastHmVal = report.hm_akhir ?: 0.0

        // Update server unit last HM
        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val hasPhoto = !report.foto_bukti.isNullOrBlank() || !report.photo_base64.isNullOrBlank()
        val photoInfo = if (hasPhoto) " [Foto Bukti Kegiatan Terverifikasi (${report.photo_size_kb ?: 145} KB, ${report.photo_dimensions ?: "1280x960 px"})]" else ""
        val rptKendalaItems = report.kendala_lapangan ?: report.kendala_list ?: report.daftar_kendala
        val rptKendalaInfo = if (!rptKendalaItems.isNullOrEmpty()) {
            " [Kendala: ${rptKendalaItems.joinToString { "${it.nama_kendala} [${it.kode_kendala}]" }}]"
        } else ""
        val statusInfo = " [Status: ${report.status_unit ?: "OPERASI"}]"
        val fuelInfo = if (report.is_isi_solar == true) " [Solar: ${report.jumlah_liter_solar} L]" else ""
        val auditInfo = if (report.is_hm_awal_corrected == true) " [AUDIT: Koreksi HM Awal (${report.hm_awal}) Alasan: ${report.alasan_koreksi_hm ?: "-"}]" else ""

        return ApiResponse(
            status = "SUCCESS",
            message = "Laporan berhasil dikirim dan diverifikasi oleh Server Web Admin.$rptKendalaInfo$statusInfo$fuelInfo$auditInfo. HM Unit diperbarui ke $lastHmVal.$photoInfo",
            data = "REPORT-ACK-${System.currentTimeMillis()}"
        )
    }

    // Methods for Web Admin simulator (Tambah / Edit / Hapus Master Data)
    fun addOrUpdateUnit(unit: UnitPayload) {
        val idx = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(unit.resolvedUnitCode, ignoreCase = true) }
        if (idx >= 0) {
            serverUnits[idx] = unit
        } else {
            serverUnits.add(unit)
        }
    }

    fun deleteUnit(unitCode: String) {
        serverUnits.removeAll { it.resolvedUnitCode.equals(unitCode, ignoreCase = true) }
        val remainingCodes = serverUnits.map { it.resolvedUnitCode.lowercase() }.toSet()
        serverRencanaKerja.removeAll { ren -> ren.resolvedUnitKode.lowercase() !in remainingCodes }
    }

    fun cleanDumpTruckAndBulldozer() {
        serverUnits.removeAll { 
            val type = it.resolvedUnitType.lowercase()
            type.contains("dump") || type.contains("dozer") || !com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType)
        }
        val remainingCodes = serverUnits.map { it.resolvedUnitCode.lowercase() }.toSet()
        serverRencanaKerja.removeAll { ren ->
            ren.resolvedUnitKode.lowercase() !in remainingCodes
        }
        receivedReportsOnServer.removeAll { rep ->
            (rep.kode_unit?.lowercase() ?: "") !in remainingCodes
        }
    }

    fun getMasterUnit(supervisor: String? = null): ApiResponse<List<UnitPayload>> {
        cleanDumpTruckAndBulldozer()
        val list = serverUnits.filter {
            com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) &&
            (supervisor.isNullOrBlank() || it.resolvedSupervisor.equals(supervisor, ignoreCase = true) || supervisor.equals("Semua", ignoreCase = true))
        }
        return ApiResponse(
            status = "SUCCESS",
            message = "Master Unit berhasil diambil (${list.size} unit)",
            data = list
        )
    }

    fun getMasterOperator(supervisor: String? = null): ApiResponse<List<OperatorPayload>> {
        val list = serverOperators.filter {
            it.resolvedStatus.equals("AKTIF", ignoreCase = true) &&
            (supervisor.isNullOrBlank() || it.resolvedSupervisor.equals(supervisor, ignoreCase = true) || supervisor.equals("Semua", ignoreCase = true))
        }
        return ApiResponse(
            status = "SUCCESS",
            message = "Master Operator berhasil diambil (${list.size} operator)",
            data = list
        )
    }

    fun addOrUpdateActivity(activity: ActivityPayload) {
        val idx = serverActivities.indexOfFirst { it.resolvedSapCode.equals(activity.resolvedSapCode, ignoreCase = true) }
        if (idx >= 0) {
            serverActivities[idx] = activity
        } else {
            serverActivities.add(activity)
        }
    }

    fun deleteActivity(sapCode: String) {
        serverActivities.removeAll { it.resolvedSapCode.equals(sapCode, ignoreCase = true) }
    }

    fun addOrUpdateOperator(operator: OperatorPayload) {
        val idx = serverOperators.indexOfFirst { it.resolvedOperatorId.equals(operator.resolvedOperatorId, ignoreCase = true) }
        if (idx >= 0) {
            serverOperators[idx] = operator
        } else {
            serverOperators.add(operator)
        }
    }

    fun deleteOperator(operatorId: String) {
        serverOperators.removeAll { it.resolvedOperatorId.equals(operatorId, ignoreCase = true) }
    }

    suspend fun authenticate(username: String, pin: String): LoginResult {
        delay(300) // latency simulation
        val trimmedUser = username.trim()
        val trimmedPin = pin.trim()

        if (trimmedUser.isBlank()) {
            return LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin.")
        }

        if (trimmedUser.contains("nonaktif", ignoreCase = true) || trimmedUser.contains("inactive", ignoreCase = true)) {
            return LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin.")
        }

        val (matched, errorMsg) = com.example.data.model.SupervisorDirectory.authenticateKasie(trimmedUser, trimmedPin)
        if (matched == null) {
            return LoginResult.Error(errorMsg ?: "Gagal Masuk: Akun tidak terdaftar atau kata sandi salah di Website Admin.")
        }

        return LoginResult.Success(
            token = "SIM-JWT-${System.currentTimeMillis()}",
            user = UserProfileDto(
                id = matched.userId,
                nama_lengkap = matched.namaLengkap,
                username = matched.username,
                role = matched.role,
                wilayah = matched.wilayah,
                status = matched.status
            ),
            message = "Login berhasil sebagai Kasie Lapangan"
        )
    }

    fun getMobileBootstrapResponse(supervisorName: String = "", tanggal: String = ""): CentralizedBootstrapResponse {
        cleanDumpTruckAndBulldozer()
        val cleanPengawas = supervisorName.trim().removePrefix("@")
        val isAll = cleanPengawas.isBlank() || cleanPengawas.equals("Semua", ignoreCase = true)
        val users = serverUsers.filter { it.isAktif }
        val units = serverUnits.filter { 
            com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) &&
            (it.resolvedStatus.equals("OPERASIONAL", ignoreCase = true) || it.resolvedStatus.equals("Aktif", ignoreCase = true))
        }
        val ownedUnitCodes = if (isAll) emptySet() else units
            .filter { it.resolvedSupervisor.equals(cleanPengawas, ignoreCase = true) }
            .map { it.resolvedUnitCode.uppercase() }
            .toSet()
        val operators = serverOperators.filter { it.resolvedStatus.equals("AKTIF", ignoreCase = true) }
        val aktivitas = serverActivities.filter { !it.resolvedKategoriStatus.equals("Nonaktif", ignoreCase = true) }
        val kendala = serverKendala.toList()
        val lokasi = serverLokasi.toList()
        val rencana = serverRencanaKerja.filter { plan ->
            plan.is_realized != true &&
            if (isAll) {
                true
            } else {
                val matchesKasie = plan.resolvedPengawas.equals(cleanPengawas, ignoreCase = true) ||
                    plan.nama_pengawas.equals(cleanPengawas, ignoreCase = true)
                val matchesUnit = ownedUnitCodes.isEmpty() || plan.resolvedUnitKode.uppercase() in ownedUnitCodes
                matchesKasie && matchesUnit
            }
        }

        val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(Date())

        return CentralizedBootstrapResponse(
            status = "success",
            timestamp = isoTime,
            data = CentralizedBootstrapDataPayload(
                users = users,
                units = units,
                operators = operators,
                aktivitas = aktivitas,
                kendala = kendala,
                lokasi = lokasi,
                rencanaKerja = rencana
            )
        )
    }

    private fun createDefaultServerUsers(): List<UserApiPayload> {
        return listOf(
            UserApiPayload(id = "33", nama_lengkap = "Farid", username = "farid", hak_akses = "Super Admin", role = "SUPER ADMIN", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "34", nama_lengkap = "Deni", username = "deni", hak_akses = "Super Admin", role = "SUPER ADMIN", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "35", nama_lengkap = "Agung", username = "agung", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "Geodesi PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "36", nama_lengkap = "Zainudin", username = "zainudin", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "Geodesi PG2", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "37", nama_lengkap = "Jahidi", username = "jahidi", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "RC PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "38", nama_lengkap = "Hisar", username = "hisar", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "RC PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "39", nama_lengkap = "Yono", username = "yono1", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "RC PG2", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "40", nama_lengkap = "Nyoman", username = "nyoman", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "RC PG3", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "41", nama_lengkap = "Saipul", username = "saipul", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "LEBUNG PG12", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "42", nama_lengkap = "Galung", username = "galung", hak_akses = "Kasie Lapangan", role = "KASIE", wilayah = "LEBUNG PG3", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "43", nama_lengkap = "Lutfi", username = "lutfi", hak_akses = "Manager Operasional", role = "MANAGER", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "44", nama_lengkap = "Putut", username = "putut", hak_akses = "Manager Operasional", role = "MANAGER", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif")
        )
    }

    private fun createDefaultServerLokasi(): List<LokasiPayload> {
        return listOf(
            LokasiPayload("001A", "Geodesi PG1", 10.5, 8.2, "Area Front Utama"),
            LokasiPayload("002B", "Geodesi PG2", 14.0, 11.5, "Pit Cut Site B"),
            LokasiPayload("101C", "RC PG1", 25.0, 21.4, "Hauling Route South"),
            LokasiPayload("102D", "RC PG2", 18.5, 15.0, "Pit West Front"),
            LokasiPayload("103E", "RC PG3", 20.0, 16.8, "Loading Pit C"),
            LokasiPayload("201F", "LEBUNG PG12", 30.0, 26.0, "Lebung Wetland Area"),
            LokasiPayload("202G", "LEBUNG PG3", 28.0, 24.0, "Lebung South Area")
        )
    }

    private fun createDefaultServerKendala(): List<KendalaPayload> {
        return listOf(
            KendalaPayload(id = 1, kode_kendala = "KND-01", nama_kendala = "Antar - jemput", kategori = "OPERASIONAL", deskripsi = "Mobilisasi personel antar - jemput shift"),
            KendalaPayload(id = 2, kode_kendala = "KND-02", nama_kendala = "Cek unit, Implement Pemanasan mesin", kategori = "OPERASIONAL", deskripsi = "Pemeriksaan P2H harian dan pemanasan unit"),
            KendalaPayload(id = 3, kode_kendala = "KND-03", nama_kendala = "Transport", kategori = "OPERASIONAL", deskripsi = "Perjalanan/transportasi unit ke front kerja"),
            KendalaPayload(id = 4, kode_kendala = "KND-04", nama_kendala = "Kerja", kategori = "OPERASIONAL", deskripsi = "Aktivitas kerja operasional"),
            KendalaPayload(id = 5, kode_kendala = "KND-05", nama_kendala = "Istirahat", kategori = "OPERASIONAL", deskripsi = "Waktu istirahat resmi operator"),
            KendalaPayload(id = 6, kode_kendala = "KND-06", nama_kendala = "Perbaikan Unit", kategori = "MEKANIKAL", deskripsi = "Perbaikan kerusakan pada unit alat berat"),
            KendalaPayload(id = 7, kode_kendala = "KND-07", nama_kendala = "Perbaikan Implement", kategori = "MEKANIKAL", deskripsi = "Perbaikan attachment/implement unit"),
            KendalaPayload(id = 8, kode_kendala = "KND-08", nama_kendala = "Perawatan Unit", kategori = "MEKANIKAL", deskripsi = "Servis rutin dan perawatan berkala unit"),
            KendalaPayload(id = 9, kode_kendala = "KND-09", nama_kendala = "Perawatan Implement", kategori = "MEKANIKAL", deskripsi = "Servis dan greasing implement"),
            KendalaPayload(id = 10, kode_kendala = "KND-10", nama_kendala = "Ganti Implement", kategori = "OPERASIONAL", deskripsi = "Penggantian bucket/blade/ripper attachment"),
            KendalaPayload(id = 11, kode_kendala = "KND-11", nama_kendala = "Tunggu Solar", kategori = "LOGISTIK", deskripsi = "Menunggu pengisian bahan bakar solar / fuel truck"),
            KendalaPayload(id = 12, kode_kendala = "KND-12", nama_kendala = "Tunggu Mekanik", kategori = "MEKANIKAL", deskripsi = "Menunggu kedatangan mekanik servis"),
            KendalaPayload(id = 13, kode_kendala = "KND-13", nama_kendala = "Tunggu Trailler", kategori = "LOGISTIK", deskripsi = "Menunggu lowbed / trailler mobilisasi unit"),
            KendalaPayload(id = 14, kode_kendala = "KND-14", nama_kendala = "Tunggu Lokasi", kategori = "OPERASIONAL", deskripsi = "Menunggu kesiapan front kerja / lokasi kerja"),
            KendalaPayload(id = 15, kode_kendala = "KND-15", nama_kendala = "Tunggu Cuaca", kategori = "CUACA", deskripsi = "Hambatan cuaca buruk / hujan lebat / slippery")
        )
    }

    private fun createDefaultServerUnits(): List<UnitPayload> {
        val list = mutableListOf<UnitPayload>()
        // 39 unit operasional aktif dialokasikan per Kasie penanggung jawab
        val kasieDistribution = listOf(
            Triple("Agung", "Geodesi PG1", listOf(
                Pair("HEH0004", Triple("Excavator Mini", "Kubota U50-5", 6.0)),
                Pair("HEH0013", Triple("Excavator Standar", "Komatsu PC200-8", 12.0)),
                Pair("HEH0015", Triple("Excavator Standar", "Kobelco SK200", 25.0)),
                Pair("HEH0029", Triple("Excavator Long Arm", "CAT 320D Long Arm", 18.0)),
                Pair("SCC0001", Triple("Compactor", "Bomag BW211 D-40", 15.0))
            )),
            Triple("Zainudin", "Geodesi PG2", listOf(
                Pair("EXC-09", Triple("Excavator Standar", "Hitachi ZX210-5G", 3990.2)),
                Pair("GRD-02", Triple("Motor Grader", "CAT 140M", 2300.0)),
                Pair("HEH0016", Triple("Excavator Standar", "Komatsu PC210", 3120.0)),
                Pair("SCC0002", Triple("Compactor", "Sakai SV520", 1820.0)),
                Pair("HEH0030", Triple("Excavator Long Arm", "Komatsu PC210LC-10", 4100.0))
            )),
            Triple("Jahidi", "RC PG1", listOf(
                Pair("CMP-02", Triple("Compactor", "Sakai SV520", 1750.0)),
                Pair("HEH0017", Triple("Excavator Standar", "Hitachi ZX200-5G", 3210.0)),
                Pair("HEH0018", Triple("Excavator Standar", "Kobelco SK210", 2890.0)),
                Pair("GRD-04", Triple("Motor Grader", "Komatsu GD511A", 1950.0)),
                Pair("EXC-14", Triple("Excavator Standar", "CAT 320D", 2450.0)),
                Pair("SCC0007", Triple("Compactor", "Bomag BW211", 1600.0)),
                Pair("HEH0034", Triple("Excavator Long Arm", "Komatsu PC210LC", 3800.0))
            )),
            Triple("Hisar", "RC PG1", listOf(
                Pair("HEH0005", Triple("Excavator Mini", "Yanmar VIO50", 1120.0)),
                Pair("HEH0006", Triple("Excavator Mini", "Kubota U50", 940.0)),
                Pair("EXC-10", Triple("Excavator Standar", "Komatsu PC210-10", 3540.0)),
                Pair("HEH0019", Triple("Excavator Standar", "CAT 320 GC", 2650.0)),
                Pair("SCC0003", Triple("Compactor", "Bomag BW211", 1640.0)),
                Pair("GRD-05", Triple("Motor Grader", "CAT 120K", 2410.0)),
                Pair("HEH0031", Triple("Excavator Long Arm", "Kobelco SK210LC", 3980.0))
            )),
            Triple("Yono", "RC PG2", listOf(
                Pair("GRD-03", Triple("Motor Grader", "Komatsu GD511A", 2150.0)),
                Pair("HEH0020", Triple("Excavator Standar", "Hitachi ZX210", 3780.0)),
                Pair("HEH0021", Triple("Excavator Standar", "Komatsu PC200", 3410.0)),
                Pair("SCC0004", Triple("Compactor", "Dynapac CA250", 1530.0))
            )),
            Triple("Nyoman", "RC PG3", listOf(
                Pair("EXC-11", Triple("Excavator Long Arm", "CAT 320D2", 5120.0)),
                Pair("HEH0022", Triple("Excavator Standar", "Komatsu PC200-8", 2980.0)),
                Pair("GRD-06", Triple("Motor Grader", "CAT 140K", 2870.0)),
                Pair("SCC0005", Triple("Compactor", "Bomag BW216", 1920.0))
            )),
            Triple("Saipul", "LEBUNG PG12", listOf(
                Pair("CMP-03", Triple("Compactor", "Bomag BW216", 1890.0)),
                Pair("HEH0024", Triple("Excavator Standar", "Komatsu PC210-10", 3100.0)),
                Pair("HEH0025", Triple("Excavator Standar", "Hitachi ZX200", 4200.0)),
                Pair("GRD-07", Triple("Motor Grader", "Komatsu GD535", 2290.0))
            )),
            Triple("Galung", "LEBUNG PG3", listOf(
                Pair("EXC-12", Triple("Excavator Standar", "Hitachi ZX200-5A", 4200.0)),
                Pair("HEH0026", Triple("Excavator Standar", "Kobelco SK200", 3350.0)),
                Pair("GRD-08", Triple("Motor Grader", "CAT 140M", 2540.0))
            ))
        )

        for ((kasie, wil, units) in kasieDistribution) {
            for ((code, specs) in units) {
                val (uType, brand, lastHm) = specs
                val isCompactor = uType.contains("Compactor", ignoreCase = true)
                val isGrader = uType.contains("Grader", ignoreCase = true)
                list.add(
                    UnitPayload(
                        kode_unit = code,
                        unit_code = code,
                        unitCode = code,
                        jenis_unit = uType,
                        unit_type = uType,
                        unitType = uType,
                        model_unit = brand,
                        brand_model = brand,
                        brandModel = brand,
                        hm_unit_terakhir_diinputkan = lastHm,
                        last_hm = lastHm,
                        lastHm = lastHm,
                        kasie_penanggung_jawab = kasie,
                        nama_pengawas = kasie,
                        supervisor_assigned = kasie,
                        supervisorAssigned = kasie,
                        status = "OPERASIONAL",
                        hm_min_standar = if (isCompactor || isGrader) 5.0 else 6.0,
                        hm_max_standar = if (isCompactor || isGrader) 8.5 else 10.0,
                        wilayah = wil
                    )
                )
            }
        }
        return list // Total 39 units
    }

    private fun createDefaultServerOperators(): List<OperatorPayload> {
        val list = mutableListOf<OperatorPayload>()
        val kasieList = listOf(
            Pair("Agung", "Geodesi PG1"),
            Pair("Zainudin", "Geodesi PG2"),
            Pair("Jahidi", "RC PG1"),
            Pair("Hisar", "RC PG1"),
            Pair("Yono", "RC PG2"),
            Pair("Nyoman", "RC PG3"),
            Pair("Saipul", "LEBUNG PG12"),
            Pair("Galung", "LEBUNG PG3")
        )
        val names = listOf(
            "Ahmad Fauzi", "Bambang Sudrajat", "Dedi Supriyadi", "Eko Prasetyo", "Hendra Gunawan",
            "Indra Kusuma", "Joko Susilo", "Kurniawan Putra", "Asep Hidayat", "Wayan Sukarta",
            "Rian Pratama", "Doni Saputra", "Teguh Santoso", "Budi Utomo", "Sigit Purnomo",
            "Faisal Akbar", "Rahmat Hidayat", "Agus Setiawan", "Ilham Maulana", "Rizki Ramadhan",
            "Wahyu Hidayat", "Bayu Saputra", "Guruh Prasetyo", "Hari Wibowo", "Irfan Hakim",
            "Julianto", "Kusnadi", "Lukman Hakim", "Mansur", "Nanang Kosim",
            "Oki Setiawan", "Panji Tri", "Rudi Hartono", "Slamet Riyadi", "Tri Wahyudi",
            "Untung Slamet", "Viko Pratama", "Wawan Kurnia", "Yadi Mulyadi", "Zaenal Abidin",
            "Ari Wibowo", "Candra Wijaya", "Dani Saputra", "Endang Sutrisno", "Fajar Sidiq"
        )

        for (i in 0 until 45) {
            val (kasie, wil) = kasieList[i % kasieList.size]
            val opName = names.getOrElse(i) { "Operator ${i + 1}" }
            val opId = String.format(Locale.US, "OPR-%03d", i + 1)
            val badge = String.format(Locale.US, "NIK-%05d", 80000 + i + 1)
            val level = when (i % 3) {
                0 -> "Class A - Excavator"
                1 -> "Class A - Grader"
                else -> "Class B - Compactor"
            }
            list.add(
                OperatorPayload(
                    operatorId = opId,
                    name = opName,
                    badgeNumber = badge,
                    supervisorAssigned = kasie,
                    kasie_penanggung_jawab = kasie,
                    kasiePenanggungJawab = kasie,
                    kasie = kasie,
                    skillLevel = level,
                    status = "AKTIF",
                    wilayah = wil
                )
            )
        }
        return list // Total 45 operators
    }

    private fun createDefaultServerActivities(): List<ActivityPayload> {
        val list = mutableListOf<ActivityPayload>()
        val categories = listOf("PRODUKSI", "SUPPORT", "INFRASTRUKTUR", "STANDBY", "BREAKDOWN")
        val baseActivities = listOf(
            Triple("Galian Tanah Lunak", "m3", "Excavator Standar"),
            Triple("Galian Tanah Berbatu", "m3", "Excavator Standar"),
            Triple("Loading Overburden", "m3", "Excavator Standar"),
            Triple("Pekerjaan Parit & Saluran", "m3", "Excavator Mini"),
            Triple("Pekerjaan Slope Long Arm", "m3", "Excavator Long Arm"),
            Triple("Pemadatan Subgrade", "m2", "Compactor"),
            Triple("Grading Mainroad", "m", "Motor Grader"),
            Triple("Perawatan Jalan Hauling", "m", "Motor Grader"),
            Triple("Clearing & Grubbing", "m2", "Excavator Standar"),
            Triple("Penataan Disposal", "m3", "Excavator Standar"),
            Triple("Pembersihan Sump Tambang", "m3", "Excavator Long Arm"),
            Triple("Timbunan Tanah Pilihan", "m3", "Compactor")
        )

        for (i in 1..107) {
            val sap = String.format(Locale.US, "ACT-SAP-%03d", i)
            val base = baseActivities[(i - 1) % baseActivities.size]
            val cat = when {
                i > 100 -> "BREAKDOWN"
                i > 95 -> "STANDBY"
                i % 4 == 0 -> "INFRASTRUKTUR"
                i % 3 == 0 -> "SUPPORT"
                else -> "PRODUKSI"
            }
            val katStatus = when (cat) {
                "STANDBY" -> "STANDBY"
                "BREAKDOWN" -> "BREAKDOWN"
                else -> "OPERASI"
            }
            val name = if (cat == "STANDBY") {
                "Standby Operasional ${i - 95}"
            } else if (cat == "BREAKDOWN") {
                "Breakdown Mekanikal ${i - 100}"
            } else {
                "${base.first} Sektor $i"
            }
            val unit = if (katStatus != "OPERASI") "ALL" else base.third
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = if (katStatus != "OPERASI") "jam" else base.second,
                    description = "Aktivitas standar operasional $name",
                    category = cat,
                    kategoriStatus = katStatus,
                    jenisUnit = unit
                )
            )
        }
        return list // Total 107 activities
    }
}
