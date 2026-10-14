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

    // Central master units in company web admin (Hanya Unit Operasional Aktif: Excavator Long Arm, Excavator Mini, Excavator Standar, Motor Grader, Compactor)
    val serverUnits = CopyOnWriteArrayList<UnitPayload>().apply {
        addAll(
            listOf(
                UnitPayload("HEH0004", "Excavator Mini", "Kubota U50-5", 6.0, "Agung", "OPERASIONAL", 6.0, 10.5, "Geodesi PG1"),
                UnitPayload("HEH0013", "Excavator Standar", "Komatsu PC200-8", 12.0, "Agung", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("HEH0015", "Excavator Standar", "Kobelco SK200", 25.0, "Agung", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("HEH0029", "Excavator Long Arm", "Caterpillar 320D Long Arm", 18.0, "Agung", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("SCC0001", "Compactor", "Bomag BW211 D-40", 15.0, "Agung", "OPERASIONAL", 5.0, 8.5, "Geodesi PG1"),
                // Units assigned to other official supervisors
                UnitPayload("EXC-09", "Excavator Standar", "Hitachi ZX210-5G", 3990.2, "Zainudin", "OPERASIONAL", 6.0, 10.0, "Geodesi PG2"),
                UnitPayload("GRD-02", "Motor Grader", "CAT 140M", 2300.0, "Zainudin", "OPERASIONAL", 5.0, 9.0, "Geodesi PG2"),
                UnitPayload("CMP-02", "Compactor", "Sakai SV520", 1750.0, "Jaidi", "OPERASIONAL", 5.0, 8.5, "Geodesi PG3"),
                UnitPayload("EXC-10", "Excavator Standar", "Komatsu PC210-10", 3540.0, "Hisar", "OPERASIONAL", 6.0, 10.0, "RC PG1"),
                UnitPayload("GRD-03", "Motor Grader", "Komatsu GD511A", 2150.0, "Yono", "OPERASIONAL", 5.0, 9.0, "RC PG2"),
                UnitPayload("EXC-11", "Excavator Long Arm", "CAT 320D2", 5120.0, "Nyoman", "OPERASIONAL", 6.0, 10.0, "RC PG3"),
                UnitPayload("CMP-03", "Compactor", "Bomag BW216", 1890.0, "Saipul", "OPERASIONAL", 5.0, 8.5, "LEBUNG PG12"),
                UnitPayload("EXC-12", "Excavator Standar", "Hitachi ZX200-5A", 4200.0, "Galung", "OPERASIONAL", 6.0, 10.0, "LEBUNG PG3")
            )
        )
    }

    // Central master kendala operasional in company web admin (15 Item Standar Lapangan)
    val serverKendala = CopyOnWriteArrayList<KendalaPayload>().apply {
        addAll(
            listOf(
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
        )
    }

    // Central master activities in company web admin
    val serverActivities = CopyOnWriteArrayList<ActivityPayload>().apply {
        addAll(
            listOf(
                ActivityPayload("ACT-SAP-101", "Galian Tanah Lunak", "m3", "Penggalian area cut site A", "PRODUKSI", "OPERASI", "Excavator Standar"),
                ActivityPayload("ACT-SAP-102", "Pekerjaan Parit & Saluran", "m3", "Penggalian saluran drainase tambang", "PRODUKSI", "OPERASI", "Excavator Mini"),
                ActivityPayload("ACT-SAP-103", "Pekerjaan Slope & Cut Long Arm", "m3", "Pekerjaan lereng cuting jarak jauh", "PRODUKSI", "OPERASI", "Excavator Long Arm"),
                ActivityPayload("ACT-SAP-201", "Pemadatan Subgrade", "m2", "Pemadatan lapisan tanah dasar disposal & jalan", "SUPPORT", "OPERASI", "Compactor"),
                ActivityPayload("ACT-SAP-202", "Grading Mainroad Tambang", "m", "Perawatan kerataan dan grading jalan hauling", "INFRASTRUKTUR", "OPERASI", "Motor Grader"),
                ActivityPayload("ACT-SAP-STB-01", "Standby Hujan / Cuaca Buruk", "jam", "Kondisi hujan lebat atau jalur licin", "STANDBY", "STANDBY", "ALL"),
                ActivityPayload("ACT-SAP-STB-02", "Standby Tunggu Trailler / Front", "jam", "Menunggu lowbed mobilisasi atau kesiapan front", "STANDBY", "STANDBY", "ALL"),
                ActivityPayload("ACT-SAP-BDN-01", "Breakdown Kerusakan Mesin / Engine", "jam", "Trouble mesin / transmisi unit", "BREAKDOWN", "BREAKDOWN", "ALL"),
                ActivityPayload("ACT-SAP-BDN-02", "Breakdown Perbaikan Undercarriage", "jam", "Perbaikan track / undercarriage unit", "BREAKDOWN", "BREAKDOWN", "ALL")
            )
        )
    }

    // Central master operators in company web admin
    val serverOperators = CopyOnWriteArrayList<OperatorPayload>().apply {
        addAll(
            listOf(
                OperatorPayload("OPR-001", "Ahmad Fauzi", "NIK-94821", "Agung", "Senior Operator - Class A", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-002", "Bambang Sudrajat", "NIK-88234", "Agung", "Senior Operator - Class A", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-003", "Dedi Supriyadi", "NIK-77412", "Agung", "Operator Alat Berat - Class B", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-004", "Eko Prasetyo", "NIK-65129", "Agung", "Operator Compactor - Class B", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-005", "Hendra Gunawan", "NIK-91203", "Agung", "Master Grader Operator", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-006", "Indra Kusuma", "NIK-83190", "Zainudin", "Senior Excavator Operator", "AKTIF", "Geodesi PG2"),
                OperatorPayload("OPR-007", "Joko Susilo", "NIK-74102", "Jaidi", "Grader Operator", "AKTIF", "Geodesi PG3"),
                OperatorPayload("OPR-008", "Kurniawan Putra", "NIK-69214", "Hisar", "Compactor Operator", "AKTIF", "RC PG1"),
                OperatorPayload("OPR-009", "Asep Hidayat", "NIK-61234", "Yono", "Excavator Operator", "AKTIF", "RC PG2"),
                OperatorPayload("OPR-010", "Wayan Sukarta", "NIK-54321", "Nyoman", "Long Arm Operator", "AKTIF", "RC PG3"),
                OperatorPayload("OPR-011", "Rian Pratama", "NIK-43210", "Saipul", "Grader Operator", "AKTIF", "LEBUNG PG12"),
                OperatorPayload("OPR-012", "Doni Saputra", "NIK-32109", "Galung", "Excavator Operator", "AKTIF", "LEBUNG PG3")
            )
        )
    }

    // Central master lokasi in company web admin
    val serverLokasi = CopyOnWriteArrayList<LokasiPayload>().apply {
        addAll(
            listOf(
                LokasiPayload("001A", "Geodesi PG1", 10.5, 8.2, "Area Front Utama"),
                LokasiPayload("002B", "Geodesi PG1", 14.0, 11.5, "Disposal North"),
                LokasiPayload("100A", "Geodesi PG2", 20.0, 16.8, "Pit Cut Site B"),
                LokasiPayload("101B", "Geodesi PG2", 18.5, 15.0, "Loading Area 2"),
                LokasiPayload("201C", "RC PG1", 25.0, 21.4, "Hauling Route South"),
                LokasiPayload("MAINROAD-01", "INFRA", 30.0, 28.0, "Jalan Tambang Utama"),
                LokasiPayload("DISPOSAL-W", "DISP", 15.0, 12.0, "Penimbunan Barat"),
                LokasiPayload("PIT-NORTH", "PIT", 35.0, 30.5, "Pit Penambangan Utara")
            )
        )
    }

    // Central received reports in Web Admin
    val receivedReportsOnServer = CopyOnWriteArrayList<PostActivityReportRequest>()
    val serverRencanaKerja = CopyOnWriteArrayList<PostRencanaKerjaRequest>()
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
        val activePlans = serverRencanaKerja.filter {
            it.nama_pengawas.equals(pengawas, ignoreCase = true) &&
            it.is_realized != true &&
            it.status_tahap == "RENCANA"
        }

        // 3. Ensure initial historical activity reports on server for this supervisor if none yet
        if (receivedReportsOnServer.none { it.nama_pengawas.equals(pengawas, ignoreCase = true) || it.supervisor_name.equals(pengawas, ignoreCase = true) }) {
            receivedReportsOnServer.addAll(
                listOf(
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
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
                        client_uuid = "SRV-REP-$pengawas-HEH0013-12.0",
                        client_transaction_id = "TX-SRV-001"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
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
                        client_uuid = "SRV-REP-$pengawas-CMP01-2100.0",
                        client_transaction_id = "TX-SRV-002"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
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
                        client_uuid = "SRV-REP-$pengawas-GRD01-1870.0",
                        client_transaction_id = "TX-SRV-003"
                    )
                )
            )
        }

        val supervisorReports = receivedReportsOnServer.filter {
            it.nama_pengawas.equals(pengawas, ignoreCase = true) ||
            it.supervisor_name.equals(pengawas, ignoreCase = true)
        }

        return ApiResponse(
            status = "SUCCESS",
            message = "Data bootstrap operasional pengawas $pengawas berhasil diambil (${activePlans.size} rencana, ${supervisorReports.size} riwayat)",
            data = BootstrapDataPayload(
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
        if (serverRencanaKerja.none { it.nama_pengawas.equals(pengawas, ignoreCase = true) }) {
            fetchBootstrapData(pengawas, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
        }
        val activePlans = serverRencanaKerja.filter {
            (it.nama_pengawas.isNullOrBlank() || it.nama_pengawas.equals(pengawas, ignoreCase = true)) &&
            it.is_realized != true
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

        val matched = com.example.data.model.SupervisorDirectory.authenticate(trimmedUser, trimmedPin)
        if (matched == null) {
            return LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin.")
        }

        if (!matched.isAktif) {
            return LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin.")
        }

        // Validasi PIN/Password (wajib default '123', '1234', atau matched.passwordPin)
        if (trimmedPin.isNotEmpty() && trimmedPin != matched.passwordPin && trimmedPin != "123" && trimmedPin != "1234") {
            return LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin.")
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
            message = "Login berhasil sebagai ${matched.role}"
        )
    }
}
