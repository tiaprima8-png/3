package com.example.data.remote

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.UserEntity
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class PostActivityReportRequest(
    // Indonesian keys expected by https://1-portalce.ai.studio/api/aktivitas-unit
    @Json(name = "tanggal") val tanggal: String? = null,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "jenis_unit") val jenis_unit: String? = null,
    @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @Json(name = "kode_sap") val kode_sap: String? = null,
    @Json(name = "satuan") val satuan: String? = null,
    @Json(name = "nama_operator") val nama_operator: String? = null,
    @Json(name = "lokasi_kerja") val lokasi_kerja: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "shift") val shift: String? = null,
    @Json(name = "durasi_jam_kerja") val durasi_jam_kerja: Double? = null,
    @Json(name = "hm_awal") val hm_awal: Double? = null,
    @Json(name = "hm_akhir") val hm_akhir: Double? = null,
    @Json(name = "hm_harian_berjalan") val hm_harian_berjalan: Double? = null,
    @Json(name = "hasil_kerja") val hasil_kerja: Double? = null,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "status_unit") val status_unit: String? = "OPERASI", // "OPERASI" | "STANDBY" | "BREAKDOWN"
    @Json(name = "is_isi_solar") val is_isi_solar: Boolean? = false,
    @Json(name = "jumlah_liter_solar") val jumlah_liter_solar: Double? = 0.0,
    @Json(name = "stik_solar_awal") val stik_solar_awal: Double? = null,
    @Json(name = "stik_solar_akhir") val stik_solar_akhir: Double? = null,
    @Json(name = "pemakaian_solar") val pemakaian_solar: Double? = null,
    @Json(name = "kendala_id") val kendala_id: String? = null,
    @Json(name = "kendala_nama") val kendala_nama: String? = null,
    @Json(name = "catatan_kendala") val catatan_kendala: String? = null,
    @Json(name = "kendala_lapangan") val kendala_lapangan: List<KendalaItemPayload>? = null,
    @Json(name = "kendala_list") val kendala_list: List<KendalaItemPayload>? = null,
    @Json(name = "daftar_kendala") val daftar_kendala: List<KendalaItemPayload>? = null,
    @Json(name = "foto_bukti") val foto_bukti: String? = null,
    @Json(name = "foto") val foto: String? = null,
    @Json(name = "photo_base64") val photo_base64: String? = null,
    @Json(name = "photo_path") val photo_path: String? = null,
    @Json(name = "photo_size_kb") val photo_size_kb: Long? = null,
    @Json(name = "photo_dimensions") val photo_dimensions: String? = null,
    // Audit log koreksi HM Awal manual
    @Json(name = "is_hm_awal_corrected") val is_hm_awal_corrected: Boolean? = false,
    @Json(name = "alasan_koreksi_hm") val alasan_koreksi_hm: String? = null,
    @Json(name = "client_transaction_id") val client_transaction_id: String? = null,
    @Json(name = "id_rencana_kerja") val id_rencana_kerja: Long? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,

    // Dual compatibility with standard camelCase/English keys
    @Json(name = "client_uuid") val client_uuid: String? = null,
    @Json(name = "report_date") val report_date: String? = null,
    @Json(name = "supervisor_name") val supervisor_name: String? = null,
    @Json(name = "unit_code") val unit_code: String? = null,
    @Json(name = "unit_type") val unit_type: String? = null,
    @Json(name = "unit_status") val unit_status: String? = null,
    @Json(name = "is_refueled") val is_refueled: Boolean? = null,
    @Json(name = "fuel_liters") val fuel_liters: Double? = null,
    @Json(name = "activity_name") val activity_name: String? = null,
    @Json(name = "sap_code") val sap_code: String? = null,
    @Json(name = "unit_of_measure") val unit_of_measure: String? = null,
    @Json(name = "operator_name") val operator_name: String? = null,
    @Json(name = "work_location") val work_location: String? = null,
    @Json(name = "work_duration_hours") val work_duration_hours: Double? = null,
    @Json(name = "work_output") val work_output: Double? = null,
    @Json(name = "notes") val notes: String? = null
) {
    companion object {
        fun create(
            clientUuid: String,
            reportDate: String,
            supervisorName: String,
            unitCode: String,
            unitType: String,
            activityName: String,
            sapCode: String,
            unitOfMeasure: String,
            operatorName: String,
            workLocation: String,
            shift: String,
            workDurationHours: Double,
            hmAwal: Double,
            hmAkhir: Double,
            hmDailyRunning: Double,
            workOutput: Double,
            notes: String,
            unitStatus: String = "OPERASI",
            isRefueled: Boolean = false,
            fuelLiters: Double = 0.0,
            stikSolarAwal: Double? = null,
            stikSolarAkhir: Double? = null,
            pemakaianSolar: Double? = null,
            kendalaId: String? = null,
            kendalaNama: String? = null,
            catatanKendala: String? = null,
            daftarKendala: List<KendalaItemPayload>? = null,
            kendalaLapangan: List<KendalaItemPayload>? = null,
            kendalaList: List<KendalaItemPayload>? = null,
            photoBase64: String? = null,
            photoSizeKb: Long? = null,
            photoDimensions: String? = null,
            photoPath: String? = null,
            isHmAwalCorrected: Boolean = false,
            alasanKoreksiHm: String? = null,
            clientTransactionId: String? = null,
            idRencanaKerja: Long? = null,
            nomorSpk: String? = null,
            wilayah: String? = null
        ): PostActivityReportRequest {
            val resolvedKendalaItems = (kendalaLapangan ?: kendalaList ?: daftarKendala)?.map { it.toCanonical() }
            return PostActivityReportRequest(
                // Indonesian exact fields for 1-portalce.ai.studio
                tanggal = reportDate,
                nama_pengawas = supervisorName,
                kode_unit = unitCode,
                jenis_unit = unitType,
                nama_aktivitas = activityName,
                kode_sap = sapCode,
                satuan = unitOfMeasure,
                nama_operator = operatorName,
                lokasi_kerja = workLocation,
                wilayah = wilayah,
                shift = shift,
                durasi_jam_kerja = workDurationHours,
                hm_awal = hmAwal,
                hm_akhir = hmAkhir,
                hm_harian_berjalan = hmDailyRunning,
                hasil_kerja = workOutput,
                keterangan = notes,
                status_unit = unitStatus,
                is_isi_solar = isRefueled,
                jumlah_liter_solar = fuelLiters,
                stik_solar_awal = stikSolarAwal,
                stik_solar_akhir = stikSolarAkhir,
                pemakaian_solar = pemakaianSolar,
                kendala_id = kendalaId,
                kendala_nama = kendalaNama,
                catatan_kendala = catatanKendala,
                kendala_lapangan = resolvedKendalaItems,
                kendala_list = resolvedKendalaItems,
                daftar_kendala = resolvedKendalaItems,
                foto_bukti = photoBase64,
                foto = photoBase64,
                photo_base64 = photoBase64,
                photo_path = photoPath,
                photo_size_kb = photoSizeKb,
                photo_dimensions = photoDimensions,
                is_hm_awal_corrected = isHmAwalCorrected,
                alasan_koreksi_hm = alasanKoreksiHm,
                client_transaction_id = clientTransactionId,
                id_rencana_kerja = idRencanaKerja,
                nomor_spk = nomorSpk,

                // Fallbacks
                client_uuid = clientUuid,
                report_date = reportDate,
                supervisor_name = supervisorName,
                unit_code = unitCode,
                unit_type = unitType,
                unit_status = unitStatus,
                is_refueled = isRefueled,
                fuel_liters = fuelLiters,
                activity_name = activityName,
                sap_code = sapCode,
                unit_of_measure = unitOfMeasure,
                operator_name = operatorName,
                work_location = workLocation,
                work_duration_hours = workDurationHours,
                work_output = workOutput,
                notes = notes
            )
        }
    }
}

typealias AktivitasUnitRequest = PostActivityReportRequest

@JsonClass(generateAdapter = true)
data class KendalaItemPayload(
    @Json(name = "nama_kendala") val nama_kendala: String? = null,
    @Json(name = "kode_kendala") val kode_kendala: String? = null,
    @Json(name = "jam_mulai") val jam_mulai: String? = null,
    @Json(name = "jam_selesai") val jam_selesai: String? = null,
    @Json(name = "durasi_jam") val durasi_jam: Double? = null,

    // Backward-compatible keys
    @Json(name = "id_kendala") val id_kendala: String? = null,
    @Json(name = "waktu_mulai") val waktu_mulai: String? = null,
    @Json(name = "waktu_selesai") val waktu_selesai: String? = null,
    @Json(name = "durasi_menit") val durasi_menit: Int? = null,
    @Json(name = "durasi") val durasi: Double? = null
) {
    val resolvedNama: String get() = (nama_kendala ?: "").trim()
    val resolvedKode: String get() = (kode_kendala ?: id_kendala ?: "").trim()
    val resolvedJamMulai: String get() = (jam_mulai ?: waktu_mulai ?: "").trim()
    val resolvedJamSelesai: String get() = (jam_selesai ?: waktu_selesai ?: "").trim()
    val resolvedDurasiMenit: Int get() = durasi_menit ?: ((durasi_jam ?: durasi ?: 0.0) * 60).toInt()
    val resolvedDurasiJam: Double get() = durasi_jam ?: durasi ?: ((durasi_menit ?: 0) / 60.0)

    fun toCanonical(): KendalaItemPayload {
        val kode = Companion.resolveKodeKendala(resolvedKode, resolvedNama)
        val nama = resolvedNama
        val start = resolvedJamMulai
        val end = resolvedJamSelesai
        val durJam = Math.round(resolvedDurasiJam * 10.0) / 10.0
        val durMin = resolvedDurasiMenit
        val legacyId = id_kendala ?: kode
        return KendalaItemPayload(
            nama_kendala = nama,
            kode_kendala = kode,
            jam_mulai = start,
            jam_selesai = end,
            durasi_jam = durJam,
            id_kendala = legacyId,
            waktu_mulai = start,
            waktu_selesai = end,
            durasi_menit = durMin,
            durasi = durJam
        )
    }

    companion object {
        fun resolveKodeKendala(rawKode: String?, rawNama: String? = null): String {
            if (!rawNama.isNullOrBlank()) {
                val namaTrimmed = rawNama.trim().lowercase()
                when {
                    namaTrimmed.contains("antar") && namaTrimmed.contains("jemput") -> return "1.0"
                    namaTrimmed.contains("pemanasan") || (namaTrimmed.contains("cek unit") && namaTrimmed.contains("implement")) -> return "2.0"
                    namaTrimmed.contains("transport") -> return "3.0"
                    namaTrimmed == "kerja" -> return "4.0"
                    namaTrimmed.contains("istirahat") -> return "5.0"
                    namaTrimmed.contains("perbaikan unit") -> return "6.0"
                    namaTrimmed.contains("perbaikan implement") -> return "7.0"
                    namaTrimmed.contains("perawatan unit") -> return "8.0"
                    namaTrimmed.contains("perawatan implement") -> return "9.0"
                    namaTrimmed.contains("ganti implement") -> return "10.0"
                    namaTrimmed.contains("tunggu solar") -> return "11.0"
                    namaTrimmed.contains("tunggu mekanik") -> return "12.0"
                    namaTrimmed.contains("tunggu trailler") || namaTrimmed.contains("trailer") -> return "13.0"
                    namaTrimmed.contains("tunggu lokasi") -> return "14.0"
                    namaTrimmed.contains("tunggu cuaca") || namaTrimmed.contains("hujan") || namaTrimmed.contains("slippery") -> return "15.0"
                }
            }
            if (rawKode.isNullOrBlank()) return ""
            val trimmed = rawKode.trim()
            // Format angka desimal seperti "1.0", "3.0"
            if (trimmed.matches(Regex("""^\d+(\.\d+)?$"""))) {
                return if (!trimmed.contains(".")) "$trimmed.0" else trimmed
            }
            // Format delay standar "KND-01", "KND-1", "KND-03", dsb.
            val kndMatch = Regex("""(?i)KND-(\d+)""").find(trimmed)
            if (kndMatch != null) {
                val num = kndMatch.groupValues[1].toIntOrNull()
                if (num != null) return "$num.0"
            }
            // Jika ada digit angka
            val digits = trimmed.filter { it.isDigit() }
            val num = digits.toIntOrNull()
            if (num != null && num > 0) {
                return "$num.0"
            }
            return trimmed
        }

        fun create(
            namaKendala: String?,
            kodeKendala: String?,
            jamMulai: String?,
            jamSelesai: String?,
            durasiJam: Double? = null,
            durasiMenit: Int? = null
        ): KendalaItemPayload {
            val nama = (namaKendala ?: "").trim()
            val kode = resolveKodeKendala(kodeKendala, nama)
            val start = (jamMulai ?: "").trim()
            val end = (jamSelesai ?: "").trim()

            val calcHours: Double = when {
                durasiJam != null -> Math.round(durasiJam * 10.0) / 10.0
                durasiMenit != null && durasiMenit > 0 -> {
                    Math.round((durasiMenit / 60.0) * 10.0) / 10.0
                }
                start.isNotBlank() && end.isNotBlank() -> {
                    val sMin = parseTimeToMinutes(start)
                    val eMin = parseTimeToMinutes(end)
                    if (sMin != null && eMin != null) {
                        val m = if (eMin >= sMin) eMin - sMin else (eMin + 1440) - sMin
                        Math.round((m / 60.0) * 10.0) / 10.0
                    } else 0.0
                }
                else -> 0.0
            }

            val calcMinutes: Int = durasiMenit ?: (calcHours * 60.0).toInt()

            return KendalaItemPayload(
                nama_kendala = nama,
                kode_kendala = kode,
                jam_mulai = start,
                jam_selesai = end,
                durasi_jam = calcHours,
                id_kendala = kode,
                waktu_mulai = start,
                waktu_selesai = end,
                durasi_menit = calcMinutes,
                durasi = calcHours
            )
        }

        private fun parseTimeToMinutes(timeStr: String): Int? {
            val parts = timeStr.trim().split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h * 60 + m
        }
    }
}

@JsonClass(generateAdapter = true)
data class KendalaPayload(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "id_kendala") val id_kendala: String? = null,
    @Json(name = "kode_kendala") val kode_kendala: String? = null,
    @Json(name = "kode") val kode: String? = null,
    @Json(name = "nama_kendala") val nama_kendala: String? = null,
    @Json(name = "nama") val nama: String? = null,
    @Json(name = "kategori") val kategori: String? = null,
    @Json(name = "deskripsi") val deskripsi: String? = null,
    @Json(name = "keterangan") val keterangan: String? = null
) {
    fun resolveKode(index: Int): String {
        val candidate = kode_kendala ?: kode ?: id_kendala ?: (id?.toString())
        return if (!candidate.isNullOrBlank()) {
            candidate
        } else {
            String.format(java.util.Locale.US, "KND-%02d", index + 1)
        }
    }

    val resolvedNama: String
        get() = (nama_kendala ?: nama ?: deskripsi ?: keterangan)?.takeIf { it.isNotBlank() } ?: "Kendala Lapangan"

    val resolvedKategori: String
        get() = kategori?.takeIf { it.isNotBlank() } ?: "OPERASIONAL"

    val resolvedDeskripsi: String
        get() = (deskripsi ?: keterangan) ?: ""
}

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val status: String? = "success",
    val message: String? = "",
    val data: T? = null,
    val timestamp: Long? = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class MasterDataPayload(
    val units: List<UnitPayload>? = emptyList(),
    // Matches 1-portalce.ai.studio "aktivitas"
    val aktivitas: List<ActivityPayload>? = emptyList(),
    val activities: List<ActivityPayload>? = emptyList(),
    val operators: List<OperatorPayload>? = emptyList(),
    val lokasi: List<LokasiPayload>? = emptyList(),
    val locations: List<LokasiPayload>? = emptyList(),
    val kendala: List<KendalaPayload>? = emptyList(),
    val users: List<UserApiPayload>? = emptyList(),
    val kasie: List<UserApiPayload>? = emptyList()
) {
    val resolvedActivities: List<ActivityPayload>
        get() {
            if (!aktivitas.isNullOrEmpty()) return aktivitas
            if (!activities.isNullOrEmpty()) return activities
            return emptyList()
        }

    val resolvedLokasi: List<LokasiPayload>
        get() {
            if (!lokasi.isNullOrEmpty()) return lokasi
            if (!locations.isNullOrEmpty()) return locations
            return emptyList()
        }
}

@JsonClass(generateAdapter = true)
data class BootstrapDataPayload(
    @Json(name = "master_data") val master_data: MasterDataPayload? = null,
    @Json(name = "masterData") val masterData: MasterDataPayload? = null,
    @Json(name = "rencana_aktif") val rencana_aktif: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "rencanaAktif") val rencanaAktif: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "riwayat_aktivitas") val riwayat_aktivitas: List<PostActivityReportRequest>? = emptyList(),
    @Json(name = "riwayatAktivitas") val riwayatAktivitas: List<PostActivityReportRequest>? = emptyList(),

    // Flat fallbacks if server returns master fields directly:
    val units: List<UnitPayload>? = emptyList(),
    val aktivitas: List<ActivityPayload>? = emptyList(),
    val activities: List<ActivityPayload>? = emptyList(),
    val operators: List<OperatorPayload>? = emptyList(),
    val lokasi: List<LokasiPayload>? = emptyList(),
    val kendala: List<KendalaPayload>? = emptyList()
) {
    val resolvedRencanaAktif: List<PostRencanaKerjaRequest>
        get() = if (!rencana_aktif.isNullOrEmpty()) rencana_aktif else (rencanaAktif ?: emptyList())

    val resolvedRiwayatAktivitas: List<PostActivityReportRequest>
        get() = if (!riwayat_aktivitas.isNullOrEmpty()) riwayat_aktivitas else (riwayatAktivitas ?: emptyList())

    val resolvedMasterData: MasterDataPayload
        get() = master_data ?: masterData ?: MasterDataPayload(
            units = units,
            aktivitas = aktivitas,
            activities = activities,
            operators = operators,
            lokasi = lokasi,
            kendala = kendala
        )
}

@JsonClass(generateAdapter = true)
data class BootstrapResponseData(
    @Json(name = "users") val users: List<UserEntity> = emptyList(),
    @Json(name = "units") val units: List<UnitEntity> = emptyList(),
    @Json(name = "operators") val operators: List<OperatorEntity> = emptyList(),
    @Json(name = "aktivitas") val aktivitas: List<ActivityMasterEntity> = emptyList(),
    @Json(name = "kendala") val kendala: List<KendalaEntity> = emptyList(),
    @Json(name = "lokasi") val lokasi: List<LokasiEntity> = emptyList(),
    @Json(name = "rencanaKerja") val rencanaKerja: List<RencanaKerjaEntity> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CentralizedBootstrapResponse(
    @Json(name = "status") val status: String? = "success",
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "data") val data: CentralizedBootstrapDataPayload? = null
)

@JsonClass(generateAdapter = true)
data class CentralizedBootstrapDataPayload(
    @Json(name = "users") val users: List<UserApiPayload>? = emptyList(),
    @Json(name = "units") val units: List<UnitPayload>? = emptyList(),
    @Json(name = "operators") val operators: List<OperatorPayload>? = emptyList(),
    @Json(name = "aktivitas") val aktivitas: List<ActivityPayload>? = emptyList(),
    @Json(name = "activities") val activities: List<ActivityPayload>? = emptyList(),
    @Json(name = "kendala") val kendala: List<KendalaPayload>? = emptyList(),
    @Json(name = "lokasi") val lokasi: List<LokasiPayload>? = emptyList(),
    @Json(name = "locations") val locations: List<LokasiPayload>? = emptyList(),
    @Json(name = "rencanaKerja") val rencanaKerja: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "rencana_kerja") val rencana_kerja: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "rencana_aktif") val rencana_aktif: List<PostRencanaKerjaRequest>? = emptyList()
) {
    val resolvedAktivitas: List<ActivityPayload>
        get() = if (!aktivitas.isNullOrEmpty()) aktivitas else (activities ?: emptyList())

    val resolvedLokasi: List<LokasiPayload>
        get() = if (!lokasi.isNullOrEmpty()) lokasi else (locations ?: emptyList())

    val resolvedRencanaKerja: List<PostRencanaKerjaRequest>
        get() = if (!rencanaKerja.isNullOrEmpty()) rencanaKerja else if (!rencana_kerja.isNullOrEmpty()) rencana_kerja else (rencana_aktif ?: emptyList())
}

@JsonClass(generateAdapter = true)
data class UnitPayload(
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "unit_code") val unit_code: String? = null,
    @Json(name = "unitCode") val unitCode: String? = null,

    @Json(name = "jenis_unit") val jenis_unit: String? = null,
    @Json(name = "unit_type") val unit_type: String? = null,
    @Json(name = "unitType") val unitType: String? = null,

    @Json(name = "model_unit") val model_unit: String? = null,
    @Json(name = "brand_model") val brand_model: String? = null,
    @Json(name = "brandModel") val brandModel: String? = null,

    @Json(name = "hm_unit_terakhir_diinputkan") val hm_unit_terakhir_diinputkan: Double? = null,
    @Json(name = "last_hm") val last_hm: Double? = null,
    @Json(name = "lastHm") val lastHm: Double? = null,

    @Json(name = "kasie_penanggung_jawab") val kasie_penanggung_jawab: String? = null,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "supervisor_assigned") val supervisor_assigned: String? = null,
    @Json(name = "supervisorAssigned") val supervisorAssigned: String? = null,

    @Json(name = "status") val status: String? = "OPERASIONAL",
    @Json(name = "hm_min_standar") val hm_min_standar: Double? = null,
    @Json(name = "hm_max_standar") val hm_max_standar: Double? = null,
    @Json(name = "hm_min") val hm_min: Double? = null,
    @Json(name = "hm_max") val hm_max: Double? = null,
    @Json(name = "min_standar") val min_standar: Double? = null,
    @Json(name = "max_standar") val max_standar: Double? = null,
    @Json(name = "hmMinStandar") val hmMinStandar: Double? = null,
    @Json(name = "hmMaxStandar") val hmMaxStandar: Double? = null,
    @Json(name = "wilayah") val wilayah: String? = null
) {
    constructor(
        unitCode: String,
        unitType: String,
        brandModel: String,
        lastHm: Double,
        supervisorAssigned: String,
        status: String = "OPERASIONAL",
        hmMinStandar: Double = 6.0,
        hmMaxStandar: Double = 10.0,
        wilayah: String = "Geodesi PG1"
    ) : this(
        kode_unit = unitCode,
        unit_code = unitCode,
        unitCode = unitCode,
        jenis_unit = unitType,
        unit_type = unitType,
        unitType = unitType,
        model_unit = brandModel,
        brand_model = brandModel,
        brandModel = brandModel,
        hm_unit_terakhir_diinputkan = lastHm,
        last_hm = lastHm,
        lastHm = lastHm,
        nama_pengawas = supervisorAssigned,
        supervisor_assigned = supervisorAssigned,
        supervisorAssigned = supervisorAssigned,
        status = status,
        hm_min_standar = hmMinStandar,
        hm_max_standar = hmMaxStandar,
        wilayah = wilayah
    )

    val resolvedUnitCode: String get() = kode_unit ?: unit_code ?: unitCode ?: "UNKNOWN"
    val resolvedUnitType: String get() = jenis_unit ?: unit_type ?: unitType ?: "EXCAVATOR"
    val resolvedBrandModel: String get() = model_unit ?: brand_model ?: brandModel ?: ""
    val resolvedLastHm: Double get() = hm_unit_terakhir_diinputkan ?: last_hm ?: lastHm ?: 0.0
    val resolvedSupervisor: String get() = kasie_penanggung_jawab ?: nama_pengawas ?: supervisor_assigned ?: supervisorAssigned ?: ""
    val resolvedStatus: String get() = status ?: "OPERASIONAL"
    val resolvedHmMinStandar: Double get() = hm_min_standar ?: hm_min ?: min_standar ?: hmMinStandar ?: 6.0
    val resolvedHmMaxStandar: Double get() = hm_max_standar ?: hm_max ?: max_standar ?: hmMaxStandar ?: 10.0
    val resolvedWilayah: String get() = wilayah?.takeIf { it.isNotBlank() } ?: when {
        resolvedSupervisor.contains("Agus", ignoreCase = true) -> "Geodesi PG2"
        resolvedSupervisor.contains("Rudi", ignoreCase = true) -> "RC PG1"
        else -> "Geodesi PG1"
    }
}

@JsonClass(generateAdapter = true)
data class ActivityPayload(
    @SerializedName("kode_sap") @Json(name = "kode_sap") val kode_sap: String? = null,
    @SerializedName("sap_code") @Json(name = "sap_code") val sap_code: String? = null,
    @SerializedName("sapCode") @Json(name = "sapCode") val sapCode: String? = null,

    @SerializedName("nama_aktivitas") @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @SerializedName("activity_name") @Json(name = "activity_name") val activity_name: String? = null,
    @SerializedName("activityName") @Json(name = "activityName") val activityName: String? = null,

    @SerializedName("satuan") @Json(name = "satuan") val satuan: String? = null,
    @SerializedName("unit_of_measure") @Json(name = "unit_of_measure") val unit_of_measure: String? = null,
    @SerializedName("unitOfMeasure") @Json(name = "unitOfMeasure") val unitOfMeasure: String? = null,

    @SerializedName("description") @Json(name = "description") val description: String? = null,
    @SerializedName("keterangan") @Json(name = "keterangan") val keterangan: String? = null,

    @SerializedName("kategori") @Json(name = "kategori") val kategori: String? = null,
    @SerializedName("category") @Json(name = "category") val category: String? = "PRODUKSI",

    @SerializedName("kategori_status") @Json(name = "kategori_status") val kategori_status: String? = null,
    @SerializedName("kategoriStatus") @Json(name = "kategoriStatus") val kategoriStatus: String? = null,

    @SerializedName("jenis_unit") @Json(name = "jenis_unit") val jenis_unit: String? = null
) {
    constructor(
        sapCode: String,
        activityName: String,
        unitOfMeasure: String,
        description: String = "",
        category: String = "PRODUKSI",
        kategoriStatus: String = "OPERASI",
        jenisUnit: String = "ALL"
    ) : this(
        kode_sap = sapCode,
        sap_code = sapCode,
        sapCode = sapCode,
        nama_aktivitas = activityName,
        activity_name = activityName,
        activityName = activityName,
        satuan = unitOfMeasure,
        unit_of_measure = unitOfMeasure,
        unitOfMeasure = unitOfMeasure,
        description = description,
        keterangan = description,
        kategori = category,
        category = category,
        kategori_status = kategoriStatus,
        kategoriStatus = kategoriStatus,
        jenis_unit = jenisUnit
    )

    val resolvedSapCode: String get() = kode_sap ?: sap_code ?: sapCode ?: "ACT-SAP-999"
    val resolvedActivityName: String get() = nama_aktivitas ?: activity_name ?: activityName ?: "Aktivitas Lapangan"
    val resolvedUnitOfMeasure: String get() = satuan ?: unit_of_measure ?: unitOfMeasure ?: "m3"
    val resolvedDescription: String get() = description ?: keterangan ?: ""
    val resolvedCategory: String get() = kategori ?: category ?: "PRODUKSI"
    val resolvedJenisUnit: String get() {
        val raw = jenis_unit?.trim()
        if (!raw.isNullOrBlank() && !raw.equals("ALL", ignoreCase = true)) {
            return raw
        }
        val name = resolvedActivityName.uppercase()
        return when {
            name.contains("GALIAN") || name.contains("LOADING") || name.contains("EXCAVATOR") -> "Excavator Standar"
            name.contains("LONG ARM") -> "Excavator Long Arm"
            name.contains("MINI") -> "Excavator Mini"
            name.contains("GRADING") || name.contains("MOTOR GRADER") || name.contains("GRADER") -> "Motor Grader"
            name.contains("PEMADATAN") || name.contains("COMPACTOR") -> "Compactor"
            else -> raw?.ifBlank { "ALL" } ?: "ALL"
        }
    }
    val resolvedKategoriStatus: String get() = (kategori_status ?: kategoriStatus)?.trim()?.uppercase()?.takeIf { it.isNotBlank() } ?: when {
        resolvedActivityName.contains("Standby", ignoreCase = true) || resolvedSapCode.contains("STB", ignoreCase = true) -> "STANDBY"
        resolvedActivityName.contains("Breakdown", ignoreCase = true) || resolvedActivityName.contains("Perbaikan", ignoreCase = true) || resolvedSapCode.contains("BDN", ignoreCase = true) -> "BREAKDOWN"
        else -> "OPERASI"
    }
}

@JsonClass(generateAdapter = true)
data class MasterAktivitasDto(
    @SerializedName("kode_sap") @Json(name = "kode_sap") val kodeSap: String = "",
    @SerializedName("nama_aktivitas") @Json(name = "nama_aktivitas") val namaAktivitas: String = "",
    @SerializedName("satuan") @Json(name = "satuan") val satuan: String = "m3",
    @SerializedName("kategori") @Json(name = "kategori") val kategori: String = "PRODUKSI",
    @SerializedName("kategori_status") @Json(name = "kategori_status") val kategoriStatus: String = "OPERASI",
    @SerializedName("jenis_unit") @Json(name = "jenis_unit") val jenisUnit: String = "ALL",
    @SerializedName("description") @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class OperatorPayload(
    @Json(name = "nama_operator") val nama_operator: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "nama") val nama: String? = null,

    @Json(name = "nik") val nik: String? = null,
    @Json(name = "badge_number") val badge_number: String? = null,
    @Json(name = "badgeNumber") val badgeNumber: String? = null,

    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "supervisor_assigned") val supervisor_assigned: String? = null,
    @Json(name = "supervisorAssigned") val supervisorAssigned: String? = null,
    @Json(name = "kasie_penanggung_jawab") val kasie_penanggung_jawab: String? = null,
    @Json(name = "kasiePenanggungJawab") val kasiePenanggungJawab: String? = null,
    @Json(name = "kasie") val kasie: String? = null,

    @Json(name = "jabatan") val jabatan: String? = null,
    @Json(name = "skill_level") val skill_level: String? = "Class A",
    @Json(name = "skillLevel") val skillLevel: String? = null,

    @Json(name = "operator_id") val operator_id: String? = null,
    @Json(name = "operatorId") val operatorId: String? = null,

    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "status") val status: String? = "AKTIF"
) {
    constructor(
        operatorId: String,
        name: String,
        badgeNumber: String,
        supervisorAssigned: String,
        skillLevel: String = "Class A",
        status: String = "AKTIF",
        wilayah: String = "Geodesi PG1"
    ) : this(
        nama_operator = name,
        name = name,
        nama = name,
        nik = badgeNumber,
        badge_number = badgeNumber,
        badgeNumber = badgeNumber,
        nama_pengawas = supervisorAssigned,
        supervisor_assigned = supervisorAssigned,
        supervisorAssigned = supervisorAssigned,
        jabatan = skillLevel,
        skill_level = skillLevel,
        skillLevel = skillLevel,
        operator_id = operatorId,
        operatorId = operatorId,
        wilayah = wilayah,
        status = status
    )

    val resolvedOperatorId: String get() = operator_id ?: operatorId ?: nik ?: "OPR-000"
    val resolvedName: String get() = nama_operator ?: name ?: nama ?: "Operator"
    val resolvedBadgeNumber: String get() = nik ?: badge_number ?: badgeNumber ?: "NIK-000"
    val resolvedSupervisor: String get() = kasie_penanggung_jawab ?: kasiePenanggungJawab ?: kasie ?: nama_pengawas ?: supervisor_assigned ?: supervisorAssigned ?: ""
    val resolvedSkillLevel: String get() = jabatan ?: skill_level ?: skillLevel ?: "Class A"
    val resolvedStatus: String get() = status ?: "AKTIF"
    val resolvedWilayah: String get() = wilayah?.takeIf { it.isNotBlank() } ?: when {
        resolvedSupervisor.contains("Agus", ignoreCase = true) -> "Geodesi PG2"
        resolvedSupervisor.contains("Rudi", ignoreCase = true) -> "RC PG1"
        else -> "Geodesi PG1"
    }
}

@JsonClass(generateAdapter = true)
data class LokasiPayload(
    @Json(name = "kode_lokasi") val kode_lokasi: String? = null,
    @Json(name = "kodeLokasi") val kodeLokasi: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "luas_bruto") val luas_bruto: Double? = null,
    @Json(name = "luas_netto") val luas_netto: Double? = null,
    @Json(name = "keterangan") val keterangan: String? = null
) {
    constructor(
        kodeLokasi: String,
        wilayah: String,
        luasBruto: Double,
        luasNetto: Double,
        keterangan: String = ""
    ) : this(
        kode_lokasi = kodeLokasi,
        kodeLokasi = kodeLokasi,
        wilayah = wilayah,
        luas_bruto = luasBruto,
        luas_netto = luasNetto,
        keterangan = keterangan
    )

    val resolvedKode: String get() = kode_lokasi ?: kodeLokasi ?: "LOK-001"
    val resolvedWilayah: String get() = wilayah ?: "Geodesi PG1"
    val resolvedLuasBruto: Double get() = luas_bruto ?: 0.0
    val resolvedLuasNetto: Double get() = luas_netto ?: 0.0
}

@JsonClass(generateAdapter = true)
data class PostRencanaKerjaRequest(
    @Json(name = "id") val id: String? = null,
    @Json(name = "id_rencana_kerja") val id_rencana_kerja: String? = null,
    @Json(name = "rencana_id") val rencana_id: String? = null,
    @Json(name = "status_unit") val status_unit: String? = "OPERASI",
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "unit_kode") val unit_kode: String? = null,
    @Json(name = "unit") val unit: String? = null,
    @Json(name = "nama_unit") val nama_unit: String? = null,
    @Json(name = "operator") val operator: String? = null,
    @Json(name = "nama_operator") val nama_operator: String? = null,
    @Json(name = "sap_code") val sap_code: String? = null,
    @Json(name = "kode_sap") val kode_sap: String? = null,
    @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @Json(name = "aktivitas") val aktivitas: String? = null,
    @Json(name = "satuan_aktivitas") val satuan_aktivitas: String? = null,
    @Json(name = "satuan") val satuan: String? = null,
    @Json(name = "kode_lokasi") val kode_lokasi: String? = null,
    @Json(name = "lokasi") val lokasi: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "shift_kerja") val shift_kerja: String? = "SIANG",
    @Json(name = "shift") val shift: String? = null,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "keterangan_rencana") val keterangan_rencana: String? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,
    @Json(name = "status_spk") val status_spk: String? = "MENUNGGU_SPK",
    @Json(name = "status_tahap") val status_tahap: String? = "RENCANA",
    @Json(name = "is_realized") val is_realized: Boolean? = false,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "pengawas") val pengawas: String? = null,
    @Json(name = "tanggal") val tanggal: String? = null,
    @Json(name = "tanggal_kerja") val tanggal_kerja: String? = null
) {
    val numericId: Long
        get() {
            val raw = id ?: id_rencana_kerja ?: rencana_id ?: ""
            return raw.filter { it.isDigit() }.toLongOrNull() ?: 0L
        }

    val resolvedUnitKode: String
        get() = (kode_unit ?: unit_kode ?: unit ?: "UNIT").trim()

    val resolvedAktivitas: String
        get() = nama_aktivitas ?: aktivitas ?: keterangan_rencana ?: keterangan ?: ""

    val resolvedSapCode: String
        get() = sap_code ?: kode_sap ?: "ACT-SAP-101"

    val resolvedSatuan: String
        get() = satuan_aktivitas ?: satuan ?: "m3"

    val resolvedOperator: String
        get() = operator ?: nama_operator ?: ""

    val resolvedLokasi: String
        get() = kode_lokasi ?: lokasi ?: "001A"

    val resolvedShift: String
        get() = shift_kerja ?: shift ?: "SIANG"

    val resolvedPengawas: String
        get() = (nama_pengawas ?: pengawas ?: "").trim()

    val resolvedSupervisor: String
        get() = resolvedPengawas
}

@JsonClass(generateAdapter = true)
data class PostHasilAktivitasRequest(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "rencana_id") val rencana_id: Long? = null,
    @Json(name = "id_rencana_kerja") val id_rencana_kerja: Long? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,
    @Json(name = "jam_kerja") val jam_kerja: Double? = 7.0,
    @Json(name = "hm_awal") val hm_awal: Double? = null,
    @Json(name = "hm_akhir") val hm_akhir: Double? = null,
    @Json(name = "hm_harian_berjalan") val hm_harian_berjalan: Double? = null,
    @Json(name = "is_isi_solar") val is_isi_solar: Boolean? = false,
    @Json(name = "jumlah_liter_solar") val jumlah_liter_solar: Double? = 0.0,
    @Json(name = "stik_solar_awal") val stik_solar_awal: Double? = null,
    @Json(name = "stik_solar_akhir") val stik_solar_akhir: Double? = null,
    @Json(name = "pemakaian_solar") val pemakaian_solar: Double? = null,
    @Json(name = "kendala_id") val kendala_id: String? = null,
    @Json(name = "kendala_nama") val kendala_nama: String? = null,
    @Json(name = "catatan_kendala") val catatan_kendala: String? = null,
    @Json(name = "kendala_lapangan") val kendala_lapangan: List<KendalaItemPayload>? = null,
    @Json(name = "kendala_list") val kendala_list: List<KendalaItemPayload>? = null,
    @Json(name = "daftar_kendala") val daftar_kendala: List<KendalaItemPayload>? = null,
    @Json(name = "hasil_kerja") val hasil_kerja: Double? = 0.0,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "foto_bukti") val foto_bukti: String? = null,
    // Audit log koreksi HM Awal manual
    @Json(name = "is_hm_awal_corrected") val is_hm_awal_corrected: Boolean? = false,
    @Json(name = "alasan_koreksi_hm") val alasan_koreksi_hm: String? = null,
    @Json(name = "client_transaction_id") val client_transaction_id: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserApiPayload(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "nama_lengkap") val nama_lengkap: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "hak_akses") val hak_akses: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "status_aktif") val status_aktif: Any? = 1,
    @Json(name = "status") val status: String? = null,
    @Json(name = "email") val email: String? = null
) {
    val numericOrStringId: String
        get() = id?.toString() ?: "1"

    val resolvedNamaLengkap: String
        get() = nama_lengkap?.ifBlank { null } ?: username ?: "Pengguna"

    val resolvedUsername: String
        get() = (username ?: "").trim().removePrefix("@")

    val isAktif: Boolean
        get() {
            if (!status.isNullOrBlank()) {
                val s = status.trim()
                if (s.equals("Nonaktif", ignoreCase = true) ||
                    s.equals("Inactive", ignoreCase = true) ||
                    s.equals("Blocked", ignoreCase = true) ||
                    s.equals("Tidak Aktif", ignoreCase = true) ||
                    s == "0"
                ) {
                    return false
                }
                if (s.equals("Aktif", ignoreCase = true) ||
                    s.equals("Active", ignoreCase = true) ||
                    s == "1"
                ) {
                    return true
                }
            }
            if (status_aktif != null) {
                when (status_aktif) {
                    is Boolean -> return status_aktif
                    is Number -> return status_aktif.toInt() == 1
                    is String -> {
                        val trimmed = status_aktif.trim()
                        if (trimmed == "0" || trimmed.equals("false", ignoreCase = true) || trimmed.equals("nonaktif", ignoreCase = true)) {
                            return false
                        }
                        if (trimmed == "1" || trimmed == "1.0" || trimmed.equals("true", ignoreCase = true) || trimmed.equals("aktif", ignoreCase = true)) {
                            return true
                        }
                        val d = trimmed.toDoubleOrNull()
                        if (d != null) {
                            return d.toInt() == 1
                        }
                    }
                }
                val str = status_aktif.toString().trim()
                val num = str.toDoubleOrNull()
                if (num != null) {
                    return num.toInt() == 1
                }
                return !str.equals("0", ignoreCase = true) && !str.equals("false", ignoreCase = true) && !str.equals("nonaktif", ignoreCase = true)
            }
            return true
        }

    val resolvedRole: String
        get() {
            val rRole = (role ?: "").trim().uppercase()
            val rHak = (hak_akses ?: "").trim().uppercase()
            return when {
                rRole.contains("SUPER_ADMIN") || rRole.contains("SUPERADMIN") || rRole == "SUPER ADMIN" || rHak.contains("SUPER ADMIN") || rHak.contains("SUPERADMIN") -> "SUPER ADMIN"
                rRole.contains("MANAGER") || rHak.contains("MANAGER") -> "MANAGER"
                rRole.contains("SUPERVISOR") || rRole.contains("KASIE") || rHak.contains("KASIE") || rHak.contains("PENGAWAS") -> "KASIE"
                else -> "KASIE"
            }
        }

    val isKasie: Boolean
        get() = resolvedRole == "KASIE"

    val isExecutive: Boolean
        get() = resolvedRole == "SUPER ADMIN" || resolvedRole == "MANAGER"

    fun toSupervisor(): com.example.data.model.Supervisor {
        val cleanUser = resolvedUsername
        val cleanName = resolvedNamaLengkap
        val roleStr = resolvedRole
        val isExec = roleStr == "SUPER ADMIN" || roleStr == "MANAGER"
        return com.example.data.model.Supervisor(
            id = numericOrStringId,
            name = cleanName,
            namaLengkap = cleanName,
            username = cleanUser,
            nik = cleanUser,
            passwordPin = "123",
            defaultShift = if (isExec) "ALL" else "SIANG",
            assignedSector = if (isExec) "Seluruh Proyek Lapangan (Akses Web)" else "Sektor Operasional Lapangan (${wilayah ?: "Geodesi PG1"})",
            phone = "+62 812-0000-0000",
            roleTitle = when (roleStr) {
                "SUPER ADMIN" -> "Super Admin"
                "MANAGER" -> "Manager Operasional"
                else -> "Kasie Lapangan"
            },
            role = roleStr,
            wilayah = wilayah ?: if (isExec) "Semua Wilayah" else "Geodesi PG1",
            token = "SYNC-$cleanUser",
            status = if (isAktif) "Aktif" else "Nonaktif"
        )
    }
}

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "nama_lengkap") val nama_lengkap: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "role") val role: String? = "KASIE", // KASIE | MANAGER | SUPER ADMIN
    @Json(name = "hak_akses") val hak_akses: String? = null,
    @Json(name = "wilayah") val wilayah: String? = "Geodesi 1",
    @Json(name = "status") val status: String? = "Aktif",
    @Json(name = "status_aktif") val status_aktif: Any? = 1
) {
    val numericOrStringId: String
        get() = id?.toString() ?: "1"
    val resolvedNamaLengkap: String
        get() = nama_lengkap?.ifBlank { null } ?: username ?: "Pengguna"
    val isAktif: Boolean
        get() {
            if (!status.isNullOrBlank()) {
                val s = status.trim()
                if (s.equals("Nonaktif", ignoreCase = true) || s.equals("Inactive", ignoreCase = true) || s.equals("Tidak Aktif", ignoreCase = true) || s == "0") {
                    return false
                }
                if (s.equals("Aktif", ignoreCase = true) || s.equals("Active", ignoreCase = true) || s == "1") {
                    return true
                }
            }
            if (status_aktif != null) {
                when (status_aktif) {
                    is Boolean -> return status_aktif
                    is Number -> return status_aktif.toInt() == 1
                    is String -> {
                        val s = status_aktif.trim()
                        if (s == "0" || s.equals("false", ignoreCase = true) || s.equals("nonaktif", ignoreCase = true)) return false
                        if (s == "1" || s == "1.0" || s.equals("true", ignoreCase = true) || s.equals("aktif", ignoreCase = true)) return true
                    }
                }
                val num = status_aktif.toString().toDoubleOrNull()
                if (num != null) return num.toInt() == 1
            }
            return true
        }
    val resolvedRole: String
        get() {
            val rRole = (role ?: "").trim().uppercase()
            val rHak = (hak_akses ?: "").trim().uppercase()
            return when {
                rRole.contains("SUPER_ADMIN") || rRole.contains("SUPERADMIN") || rRole == "SUPER ADMIN" || rHak.contains("SUPER ADMIN") || rHak.contains("SUPERADMIN") -> "SUPER ADMIN"
                rRole.contains("MANAGER") || rHak.contains("MANAGER") -> "MANAGER"
                rRole.contains("SUPERVISOR") || rRole.contains("KASIE") || rHak.contains("KASIE") || rHak.contains("PENGAWAS") -> "KASIE"
                else -> "KASIE"
            }
        }
    val isKasie: Boolean
        get() = resolvedRole == "KASIE"
    val isExecutive: Boolean
        get() = resolvedRole == "SUPER ADMIN" || resolvedRole == "MANAGER"
}

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "token") val token: String? = null,
    @Json(name = "user") val user: UserProfileDto? = null,
    @Json(name = "message") val message: String? = null
)

sealed class LoginResult {
    data class Success(
        val token: String,
        val user: UserProfileDto,
        val message: String = "Login berhasil"
    ) : LoginResult()

    data class Error(val message: String) : LoginResult()
}

object KendalaJsonConverter {
    private val moshi by lazy {
        Moshi.Builder().build()
    }
    private val listType by lazy {
        Types.newParameterizedType(List::class.java, KendalaItemPayload::class.java)
    }
    private val adapter by lazy {
        moshi.adapter<List<KendalaItemPayload>>(listType)
    }

    fun toJson(list: List<KendalaItemPayload>): String {
        return try {
            adapter.toJson(list)
        } catch (_: Exception) {
            "[]"
        }
    }

    fun fromJson(json: String?): List<KendalaItemPayload> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

