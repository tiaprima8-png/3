package com.example.data.local.entity

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasterRealisasiEntity(
    @SerializedName("id") @Json(name = "id") val id: Long = 0,

    @SerializedName("nomor_spk") @Json(name = "nomor_spk") val nomorSpk: String = "",
    @SerializedName("tanggal") @Json(name = "tanggal") val tanggal: String = "",
    @SerializedName("shift") @Json(name = "shift") val shift: String = "SIANG",
    @SerializedName("kode_unit") @Json(name = "kode_unit") val kodeUnit: String = "",
    @SerializedName("jenis_unit") @Json(name = "jenis_unit") val jenisUnit: String? = null,
    @SerializedName("operator") @Json(name = "operator") val operator: String = "",
    @SerializedName("kasie") @Json(name = "kasie") val kasie: String = "",
    @SerializedName("wilayah") @Json(name = "wilayah") val wilayah: String? = null,
    @SerializedName("nama_aktivitas") @Json(name = "nama_aktivitas") val namaAktivitas: String = "",
    @SerializedName("lokasi") @Json(name = "lokasi") val lokasi: String = "",
    @SerializedName("hm_awal") @Json(name = "hm_awal") val hmAwal: Double = 0.0,
    @SerializedName("hm_akhir") @Json(name = "hm_akhir") val hmAkhir: Double = 0.0,
    @SerializedName("total_hm") @Json(name = "total_hm") val totalHm: Double = 0.0,
    @SerializedName("konsumsi_solar") @Json(name = "konsumsi_solar") val konsumsiSolar: Double = 0.0,
    @SerializedName("stik_solar_awal") @Json(name = "stik_solar_awal") val stikSolarAwal: Double = 0.0,
    @SerializedName("stik_solar_akhir") @Json(name = "stik_solar_akhir") val stikSolarAkhir: Double = 0.0,
    @SerializedName("hasil_kerja") @Json(name = "hasil_kerja") val hasilKerja: Double = 0.0,
    @SerializedName("satuan") @Json(name = "satuan") val satuan: String? = "ha",
    @SerializedName("keterangan") @Json(name = "keterangan") val keterangan: String? = null,
    @SerializedName("kendala") @Json(name = "kendala") val kendala: String? = null,

    // SERIALISASI STATUS WAJIB LENGKAP:
    @SerializedName("approval_status") @Json(name = "approval_status") val approvalStatus: String? = "Menunggu",
    @SerializedName("is_locked") @Json(name = "is_locked") val isLocked: Boolean = false
) {
    fun toActivityReportEntity(): ActivityReportEntity {
        return ActivityReportEntity(
            id = id,
            reportDate = tanggal,
            supervisorName = kasie,
            namaKasie = kasie,
            createdBy = kasie,
            unitCode = kodeUnit,
            unitType = jenisUnit ?: "HE",
            activityName = namaAktivitas,
            sapCode = "ACT-SAP-101",
            unitOfMeasure = satuan ?: "m3",
            operatorName = operator,
            workLocation = lokasi,
            wilayah = wilayah ?: "PG1",
            shift = shift,
            hmAwal = hmAwal,
            hmAkhir = hmAkhir,
            hmDailyRunning = if (totalHm > 0.0) totalHm else (if (hmAkhir > hmAwal) hmAkhir - hmAwal else 0.0),
            pemakaianSolar = konsumsiSolar,
            stikSolarAwal = stikSolarAwal,
            stikSolarAkhir = stikSolarAkhir,
            workOutput = hasilKerja,
            notes = keterangan ?: "",
            kendalaNama = kendala,
            approvalStatus = approvalStatus ?: "Menunggu",
            isLocked = isLocked,
            nomorSpk = nomorSpk.ifBlank { "SPK-AUTO-$id" },
            isSynced = true
        )
    }

    companion object {
        fun fromActivityReport(rep: ActivityReportEntity): MasterRealisasiEntity {
            return MasterRealisasiEntity(
                id = rep.id,
                nomorSpk = rep.nomorSpk,
                tanggal = rep.reportDate,
                shift = rep.shift,
                kodeUnit = rep.unitCode,
                jenisUnit = rep.unitType,
                operator = rep.operatorName,
                kasie = rep.supervisorName,
                wilayah = rep.wilayah,
                namaAktivitas = rep.activityName,
                lokasi = rep.workLocation,
                hmAwal = rep.hmAwal,
                hmAkhir = rep.hmAkhir,
                totalHm = rep.hmDailyRunning,
                konsumsiSolar = rep.pemakaianSolar,
                stikSolarAwal = rep.stikSolarAwal,
                stikSolarAkhir = rep.stikSolarAkhir,
                hasilKerja = rep.workOutput,
                satuan = rep.unitOfMeasure,
                keterangan = rep.notes,
                kendala = rep.kendalaNama,
                approvalStatus = rep.approvalStatus,
                isLocked = rep.isLocked
            )
        }
    }
}

typealias RealisasiDto = MasterRealisasiEntity
