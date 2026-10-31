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

    private val _realisasiCreatedEventFlow = MutableSharedFlow<PostActivityReportRequest>(extraBufferCapacity = 64)
    val realisasiCreatedEventFlow = _realisasiCreatedEventFlow.asSharedFlow()

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
    val serverRencanaKerja = CopyOnWriteArrayList<PostRencanaKerjaRequest>()
    val serverHasilAktivitas = CopyOnWriteArrayList<PostHasilAktivitasRequest>()

    fun clearAllTransactions() {
        receivedReportsOnServer.clear()
        serverRencanaKerja.clear()
        serverHasilAktivitas.clear()
        publishedSpkMap.clear()
    }

    suspend fun fetchMasterData(supervisorName: String): ApiResponse<MasterDataPayload> {
        delay(600) // network latency
        if (isSimulatedOffline) {
            throw java.io.IOException("Koneksi gagal: Perangkat berada di luar jangkauan jaringan (Offline Lapangan)")
        }

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
                val matchesOperator = plan.resolvedOperator.equals(cleanPengawas, ignoreCase = true)
                val matchesUnit = ownedUnitCodes.isEmpty() || plan.resolvedUnitKode.uppercase() in ownedUnitCodes
                (matchesKasie && matchesUnit) || matchesOperator
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
        val reqSpk = request.nomor_spk?.trim() ?: ""
        val reqId = request.id?.trim() ?: ""
        val existingIdx = serverRencanaKerja.indexOfFirst {
            (reqSpk.isNotBlank() && reqSpk != "SPK Menyusul" && reqSpk != "-" && it.nomor_spk.equals(reqSpk, ignoreCase = true)) ||
            (reqId.isNotBlank() && reqId != "0" && it.id == reqId && reqSpk.isBlank())
        }
        if (existingIdx >= 0) {
            serverRencanaKerja[existingIdx] = request
        } else {
            serverRencanaKerja.add(request)
        }

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
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId.toString() || it.numericId == planId }
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
            (planId <= 0 && unitKode.isNotBlank() && it.resolvedUnitKode.equals(unitKode, ignoreCase = true))
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

    fun markRencanaRealized(planId: Long, nomorSpk: String = "") {
        val cleanSpk = nomorSpk.trim()
        val idx = serverRencanaKerja.indexOfFirst {
            (planId > 0 && (it.id == planId.toString() || it.numericId == planId)) ||
            (cleanSpk.isNotBlank() && cleanSpk != "SPK Menyusul" && cleanSpk != "-" && it.nomor_spk.equals(cleanSpk, ignoreCase = true))
        }
        if (idx >= 0) {
            val old = serverRencanaKerja[idx]
            serverRencanaKerja[idx] = old.copy(
                is_realized = true,
                status_tahap = "REALISASI_SELESAI",
                status_spk = "Terealisasi"
            )
        }
    }

    fun getPublishedSpk(planId: Long): String? = publishedSpkMap[planId]

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): ApiResponse<String> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil kerja disimpan ke penyimpanan lokal")
        }

        // Anti-duplikasi berdasarkan client_transaction_id atau nomor_spk
        val txId = request.client_transaction_id
        val spkVal = request.nomor_spk?.trim() ?: ""
        val renIdVal = request.rencana_id ?: request.id_rencana_kerja ?: 0L
        if ((!txId.isNullOrBlank() && serverHasilAktivitas.any { it.client_transaction_id == txId }) ||
            (spkVal.isNotBlank() && spkVal != "SPK Menyusul" && spkVal != "-" && serverHasilAktivitas.any { it.nomor_spk.equals(spkVal, ignoreCase = true) })
        ) {
            markRencanaRealized(renIdVal, spkVal)
            return ApiResponse(
                status = "SUCCESS",
                message = "Realisasi Unit ${request.kode_unit ?: ""} sudah tercatat di Web Admin (Anti-Duplikat Terverifikasi).",
                data = "ACK-IDEMPOTENT"
            )
        }

        serverHasilAktivitas.add(request)
        markRencanaRealized(renIdVal, spkVal)

        val targetUnitCode = request.kode_unit ?: ""
        val lastHmVal = request.hm_akhir ?: 0.0

        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val convertedReport = PostActivityReportRequest(
            id_rencana_kerja = renIdVal,
            kode_unit = request.kode_unit,
            nama_operator = request.operator_nik ?: "",
            operator_nik = request.operator_nik,
            nama_pengawas = request.nama_pengawas,
            supervisor_name = request.nama_pengawas,
            tanggal = request.tanggal,
            shift = request.shift,
            wilayah = request.wilayah,
            durasi_jam_kerja = request.jam_kerja,
            hm_awal = request.hm_awal,
            hm_akhir = request.hm_akhir,
            hm_harian_berjalan = request.hm_harian_berjalan,
            hasil_kerja = request.hasil_kerja,
            keterangan = request.keterangan,
            is_isi_solar = request.is_isi_solar,
            jumlah_liter_solar = request.jumlah_liter_solar,
            pemakaian_solar = request.pemakaian_solar,
            nomor_spk = request.nomor_spk,
            approval_status = request.approval_status ?: "Menunggu Approval",
            is_locked = request.is_locked ?: false,
            client_uuid = request.client_transaction_id,
            client_transaction_id = request.client_transaction_id
        )
        val isDuplicateReport = receivedReportsOnServer.any { existing ->
            (!txId.isNullOrBlank() && (existing.client_transaction_id == txId || existing.client_uuid == txId)) ||
            (spkVal.isNotBlank() && spkVal != "SPK Menyusul" && spkVal != "-" && existing.nomor_spk.equals(spkVal, ignoreCase = true)) ||
            (renIdVal > 0 && existing.id_rencana_kerja == renIdVal)
        }
        if (!isDuplicateReport) {
            receivedReportsOnServer.add(convertedReport)
            _realisasiCreatedEventFlow.tryEmit(convertedReport)
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

        val clientUuidVal = report.client_uuid ?: report.client_transaction_id
        val spkVal = report.nomor_spk?.trim() ?: ""
        val renIdVal = report.id_rencana_kerja ?: 0L
        markRencanaRealized(renIdVal, spkVal)

        // Anti-duplikasi berdasarkan UUID, nomor_spk, atau id_rencana_kerja
        val existingIdx = receivedReportsOnServer.indexOfFirst { existing ->
            (!clientUuidVal.isNullOrBlank() && ((existing.client_uuid ?: existing.client_transaction_id) == clientUuidVal || existing.client_transaction_id == clientUuidVal)) ||
            (spkVal.isNotBlank() && spkVal != "SPK Menyusul" && spkVal != "-" && existing.nomor_spk.equals(spkVal, ignoreCase = true)) ||
            (renIdVal > 0 && existing.id_rencana_kerja == renIdVal)
        }
        if (existingIdx >= 0) {
            receivedReportsOnServer[existingIdx] = report
            return ApiResponse(
                status = "SUCCESS",
                message = "Laporan sudah tercatat sebelumnya di server Web Admin (Anti-Duplikat Terverifikasi).",
                data = "REPORT-ACK-IDEMPOTENT"
            )
        }

        // Add to server log
        receivedReportsOnServer.add(report)
        _realisasiCreatedEventFlow.tryEmit(report)

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

    fun getRealisasiForKasie(kasieName: String): ApiResponse<List<PostActivityReportRequest>> {
        val cleanTarget = kasieName.trim().removePrefix("@").lowercase()
        val unapproved = receivedReportsOnServer.filter { rpt ->
            val rptKasie = (rpt.nama_pengawas ?: rpt.supervisor_name ?: "").trim().removePrefix("@").lowercase()
            val matchesKasie = cleanTarget.isBlank() || cleanTarget == "semua" || rptKasie == cleanTarget || rptKasie.contains(cleanTarget) || cleanTarget.contains(rptKasie)
            val notApproved = rpt.is_locked != true && !(rpt.approval_status ?: "").startsWith("Disetujui", ignoreCase = true)
            matchesKasie && notApproved
        }
        return ApiResponse(
            status = "SUCCESS",
            message = "Ditemukan ${unapproved.size} realisasi operator menunggu verifikasi",
            data = unapproved
        )
    }

    fun approveRealisasi(nomorSpk: String, kasieNama: String): Boolean {
        var updated = false
        val cleanSpk = nomorSpk.trim()
        val approvalStatus = "Disetujui oleh $kasieNama"
        val timestamp = System.currentTimeMillis().toString()

        for (i in receivedReportsOnServer.indices) {
            val rpt = receivedReportsOnServer[i]
            val rptSpk = (rpt.nomor_spk ?: "").trim()
            val txId = (rpt.client_transaction_id ?: "").trim()
            if (cleanSpk.isNotBlank() && (rptSpk.equals(cleanSpk, ignoreCase = true) || txId.equals(cleanSpk, ignoreCase = true))) {
                receivedReportsOnServer[i] = rpt.copy(
                    is_locked = true,
                    approval_status = approvalStatus,
                    approved_by = kasieNama,
                    approved_at = timestamp
                )
                updated = true
            }
        }

        for (i in serverHasilAktivitas.indices) {
            val h = serverHasilAktivitas[i]
            val hSpk = (h.nomor_spk ?: "").trim()
            if (cleanSpk.isNotBlank() && hSpk.equals(cleanSpk, ignoreCase = true)) {
                serverHasilAktivitas[i] = h.copy(
                    is_locked = true,
                    approval_status = approvalStatus,
                    approved_by = kasieNama,
                    approved_at = timestamp
                )
                updated = true
            }
        }

        for (i in serverRencanaKerja.indices) {
            val rk = serverRencanaKerja[i]
            val rkSpk = (rk.nomor_spk ?: "").trim()
            if (cleanSpk.isNotBlank() && rkSpk.equals(cleanSpk, ignoreCase = true)) {
                serverRencanaKerja[i] = rk.copy(
                    status_spk = "Approved",
                    status_tahap = "REALISASI_SELESAI",
                    is_realized = true
                )
                updated = true
            }
        }

        return updated
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
        // No-op: Dump Truck (e.g. TTM010) is an active operational fleet unit
    }

    fun getMasterUnit(supervisor: String? = null): ApiResponse<List<UnitPayload>> {
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
        delay(150) // latency simulation
        val trimmedUser = username.toString().trim().removePrefix("@").trim()
        val trimmedPin = pin.toString().trim()

        if (trimmedUser.isBlank()) {
            return LoginResult.Error("Akun tidak terdaftar atau telah dinonaktifkan oleh Admin.")
        }

        if (trimmedUser.contains("nonaktif", ignoreCase = true) || trimmedUser.contains("inactive", ignoreCase = true)) {
            return LoginResult.Error("Akun tidak terdaftar atau telah dinonaktifkan oleh Admin.")
        }

        val (matched, errorMsg) = com.example.data.model.SupervisorDirectory.authenticateKasie(trimmedUser, trimmedPin)
        if (matched == null) {
            return LoginResult.Error(errorMsg ?: "Akun tidak terdaftar atau telah dinonaktifkan oleh Admin.")
        }

        return LoginResult.Success(
            token = "token-auth-session",
            user = UserProfileDto(
                id = matched.userId,
                nama = matched.namaLengkap,
                nama_lengkap = matched.namaLengkap,
                username = matched.username,
                role = if (matched.isOperator) "operator" else "kasie",
                kasie_atasan = matched.kasieAtasan,
                wilayah = matched.wilayah,
                status = matched.status
            ),
            message = if (matched.isOperator) "Login berhasil sebagai Operator" else "Login berhasil sebagai Kasie Lapangan"
        )
    }

    fun getMobileBootstrapResponse(supervisorName: String = "", tanggal: String = ""): CentralizedBootstrapResponse {
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
        val baseUsers = listOf(
            UserApiPayload(id = "33", nama = "Farid", nama_lengkap = "Farid", username = "farid", password = "123", hak_akses = "Super Admin", role = "SUPER ADMIN", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "34", nama = "Deni", nama_lengkap = "Deni", username = "deni", password = "123", hak_akses = "Super Admin", role = "SUPER ADMIN", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "35", nama = "Agung", nama_lengkap = "Agung", username = "agung", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "Geodesi PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "36", nama = "Zainudin", nama_lengkap = "Zainudin", username = "zainudin", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "Geodesi PG2", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "37", nama = "Jahidi", nama_lengkap = "Jahidi", username = "jahidi", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "RC PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "38", nama = "Hisar", nama_lengkap = "Hisar", username = "hisar", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "RC PG1", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "39", nama = "Yono", nama_lengkap = "Yono", username = "yono1", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "RC PG2", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "40", nama = "Nyoman", nama_lengkap = "Nyoman", username = "nyoman", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "RC PG3", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "41", nama = "Saipul", nama_lengkap = "Saipul", username = "saipul", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "LEBUNG PG12", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "42", nama = "Galung", nama_lengkap = "Galung", username = "galung", password = "123", hak_akses = "Kasie Lapangan", role = "kasie", wilayah = "LEBUNG PG3", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "43", nama = "Lutfi", nama_lengkap = "Lutfi", username = "lutfi", password = "123", hak_akses = "Manager Operasional", role = "MANAGER", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif"),
            UserApiPayload(id = "44", nama = "Putut", nama_lengkap = "Putut", username = "putut", password = "123", hak_akses = "Manager Operasional", role = "MANAGER", wilayah = "Semua Wilayah", status_aktif = 1, status = "Aktif")
        )
        // Auto-seed 35 akun operator dari master_operator (status = 'Aktif') ke tabel users
        val operatorUsers = createDefaultServerOperators()
            .filter { it.resolvedStatus.equals("Aktif", ignoreCase = true) }
            .mapIndexed { idx, op ->
                val nik = op.resolvedBadgeNumber
                UserApiPayload(
                    id = "${101 + idx}",
                    nama = op.resolvedName,
                    nama_lengkap = op.resolvedName,
                    username = nik,
                    password = nik,
                    hak_akses = "Operator Alat Berat",
                    role = "operator",
                    kasie_atasan = op.resolvedSupervisor,
                    wilayah = op.resolvedWilayah,
                    status_aktif = 1,
                    status = "Aktif"
                )
            }
        return baseUsers + operatorUsers
    }

    private fun createDefaultServerLokasi(): List<LokasiPayload> {
        val list = ArrayList<LokasiPayload>(3614)
        for (i in 0 until 3614) {
            val num = (i / 26) + 1
            val letter = ('A'.code + (i % 26)).toChar()
            val kode = String.format(Locale.US, "%03d%c", num, letter)
            val wilayah = when {
                i < 1500 -> if (i % 2 == 0) "Geodesi PG1" else "RC PG1"
                i < 3000 -> if (i % 2 == 0) "Geodesi PG2" else "RC PG2"
                else -> if (i % 2 == 0) "LEBUNG PG3" else "OTHER"
            }
            val bruto = 10.0 + ((i % 40) * 0.5)
            val netto = Math.round(bruto * 0.85 * 10.0) / 10.0
            list.add(
                LokasiPayload(
                    kodeLokasi = kode,
                    wilayah = wilayah,
                    luasBruto = bruto,
                    luasNetto = netto,
                    keterangan = "Sektor Kerja Lapangan $kode",
                    status = "Aktif"
                )
            )
        }
        return list
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
                Pair("HEH0034", Triple("Excavator Long Arm", "Komatsu PC210LC", 3800.0)),
                Pair("MGC0008", Triple("Motor Grader", "CAT 120K", 1980.0)),
                Pair("TTM010", Triple("DUMP TRUCK", "Hino Dutro 130 HD", 2540.0))
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
            "Oki Setiawan", "Panji Tri", "Rudi Hartono", "Slamet Riyadi", "Tri Wahyudi"
        )

        // 35 Operator Aktif dengan NIK 8-digit murni (mulai dari '85044295')
        for (i in 0 until 35) {
            val (kasie, wil) = kasieList[i % kasieList.size]
            val opName = names.getOrElse(i) { "Operator ${i + 1}" }
            val pureNik = (85044295 + i).toString()
            val opId = String.format(Locale.US, "OPR-%03d", i + 1)
            val level = when (i % 3) {
                0 -> "Class A - Excavator"
                1 -> "Class A - Grader"
                else -> "Class B - Compactor"
            }
            list.add(
                OperatorPayload(
                    operatorId = opId,
                    name = opName,
                    badgeNumber = pureNik,
                    supervisorAssigned = kasie,
                    kasie_penanggung_jawab = kasie,
                    kasiePenanggungJawab = kasie,
                    kasie = kasie,
                    skillLevel = level,
                    status = "Aktif",
                    wilayah = wil
                )
            )
        }
        return list // Total 35 operators aktif
    }

    private fun createDefaultServerActivities(): List<ActivityPayload> {
        val list = mutableListOf<ActivityPayload>()
        val unitTypes = listOf("Excavator Standar", "Excavator Long Arm", "Excavator Mini", "Motor Grader", "Compactor", "Dump Truck")

        // 1. Seed official Standby (8) & Breakdown (2) for EACH unit type
        val standbyList = listOf(
            "Tunggu Operator" to "6111199001",
            "Off Time" to "6111199002",
            "Tunggu Mekanik" to "6111199003",
            "Tunggu Sparepart" to "6111199004",
            "Tunggu Solar" to "6111199005",
            "Tunggu Cuaca" to "6111199006",
            "Tunggu Trailer" to "6111199007",
            "Tunggu Lokasi" to "6111199008"
        )
        val breakdownList = listOf(
            "Perbaikan Unit" to "6111199010",
            "Perawatan Unit" to "6111199011"
        )

        for (type in unitTypes) {
            val typePrefix = type.replace(" ", "").uppercase().take(3)
            standbyList.forEachIndexed { idx, (actName, sapCode) ->
                list.add(
                    ActivityPayload(
                        sapCode = "$sapCode-$typePrefix",
                        activityName = actName,
                        unitOfMeasure = "H",
                        description = "Aktivitas Standby $actName ($type)",
                        category = "STANDBY",
                        kategoriStatus = "STANDBY",
                        jenisUnit = type
                    )
                )
            }
            breakdownList.forEachIndexed { idx, (actName, sapCode) ->
                list.add(
                    ActivityPayload(
                        sapCode = "$sapCode-$typePrefix",
                        activityName = actName,
                        unitOfMeasure = "H",
                        description = "Aktivitas Breakdown $actName ($type)",
                        category = "BREAKDOWN",
                        kategoriStatus = "BREAKDOWN",
                        jenisUnit = type
                    )
                )
            }
        }

        // Universal Standby & Breakdown for "ALL"
        standbyList.forEach { (actName, sapCode) ->
            list.add(
                ActivityPayload(
                    sapCode = sapCode,
                    activityName = actName,
                    unitOfMeasure = "H",
                    description = "Aktivitas Standby $actName",
                    category = "STANDBY",
                    kategoriStatus = "STANDBY",
                    jenisUnit = "ALL"
                )
            )
        }
        breakdownList.forEach { (actName, sapCode) ->
            list.add(
                ActivityPayload(
                    sapCode = sapCode,
                    activityName = actName,
                    unitOfMeasure = "H",
                    description = "Aktivitas Breakdown $actName",
                    category = "BREAKDOWN",
                    kategoriStatus = "BREAKDOWN",
                    jenisUnit = "ALL"
                )
            )
        }

        // 2. Seed OPERASI activities per unit type
        // Excavator Standar: 75 OPERASI activities
        for (i in 1..75) {
            val sap = String.format(Locale.US, "ACT-EXS-%03d", i)
            val name = when (i % 5) {
                1 -> "Galian Tanah Lunak Sektor $i"
                2 -> "Galian Tanah Berbatu Sektor $i"
                3 -> "Loading Overburden Sektor $i"
                4 -> "Clearing & Grubbing Sektor $i"
                else -> "Penataan Disposal Sektor $i"
            }
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "m3",
                    description = "Aktivitas operasional Excavator Standar $name",
                    category = "PRODUKSI",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Excavator Standar"
                )
            )
        }

        // Excavator Long Arm: 20 OPERASI activities
        for (i in 1..20) {
            val sap = String.format(Locale.US, "ACT-EXL-%03d", i)
            val name = if (i % 2 == 1) "Pekerjaan Slope Long Arm Sektor $i" else "Pembersihan Sump Tambang Sektor $i"
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "m3",
                    description = "Aktivitas operasional Excavator Long Arm $name",
                    category = "PRODUKSI",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Excavator Long Arm"
                )
            )
        }

        // Excavator Mini: 20 OPERASI activities
        for (i in 1..20) {
            val sap = String.format(Locale.US, "ACT-EXM-%03d", i)
            val name = "Pekerjaan Parit & Saluran Sektor $i"
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "m3",
                    description = "Aktivitas operasional Excavator Mini $name",
                    category = "INFRASTRUKTUR",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Excavator Mini"
                )
            )
        }

        // Motor Grader: 39 OPERASI activities
        for (i in 1..39) {
            val sap = String.format(Locale.US, "ACT-GRD-%03d", i)
            val name = if (i % 2 == 1) "Grading Mainroad Sektor $i" else "Perawatan Jalan Hauling Sektor $i"
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "m",
                    description = "Aktivitas operasional Motor Grader $name",
                    category = "INFRASTRUKTUR",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Motor Grader"
                )
            )
        }

        // Compactor: 30 OPERASI activities
        for (i in 1..30) {
            val sap = String.format(Locale.US, "ACT-CMP-%03d", i)
            val name = if (i % 2 == 1) "Pemadatan Subgrade Sektor $i" else "Timbunan Tanah Pilihan Sektor $i"
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "m2",
                    description = "Aktivitas operasional Compactor $name",
                    category = "INFRASTRUKTUR",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Compactor"
                )
            )
        }

        // Dump Truck: 30 OPERASI activities
        for (i in 1..30) {
            val sap = String.format(Locale.US, "ACT-DMP-%03d", i)
            val name = if (i % 2 == 1) "Hauling Material Subgrade Sektor $i" else "Hauling Pasir & Batu Sektor $i"
            list.add(
                ActivityPayload(
                    sapCode = sap,
                    activityName = name,
                    unitOfMeasure = "rit",
                    description = "Aktivitas operasional Dump Truck $name",
                    category = "HAULING",
                    kategoriStatus = "OPERASI",
                    jenisUnit = "Dump Truck"
                )
            )
        }

        return list
    }
}
