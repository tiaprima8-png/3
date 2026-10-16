package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draft_realisasi",
    indices = [
        Index(value = ["kode_unit"]),
        Index(value = ["updated_at"])
    ]
)
data class DraftRealisasiEntity(
    @PrimaryKey
    @ColumnInfo(name = "rencana_id")
    val rencanaId: Long,

    @ColumnInfo(name = "kode_unit")
    val kodeUnit: String = "",

    @ColumnInfo(name = "hm_awal_input")
    val hmAwalInput: String = "",

    @ColumnInfo(name = "is_hm_awal_corrected")
    val isHmAwalCorrected: Boolean = false,

    @ColumnInfo(name = "alasan_koreksi_hm")
    val alasanKoreksiHm: String = "",

    @ColumnInfo(name = "hm_akhir_input")
    val hmAkhirInput: String = "",

    @ColumnInfo(name = "stik_solar_awal_input")
    val stikSolarAwalInput: String = "",

    @ColumnInfo(name = "stik_solar_akhir_input")
    val stikSolarAkhirInput: String = "",

    @ColumnInfo(name = "is_isi_solar")
    val isIsiSolar: Boolean = false,

    @ColumnInfo(name = "jumlah_liter_solar_input")
    val jumlahLiterSolarInput: String = "",

    @ColumnInfo(name = "daftar_kendala_json")
    val daftarKendalaJson: String = "",

    @ColumnInfo(name = "hasil_kerja_input")
    val hasilKerjaInput: String = "",

    @ColumnInfo(name = "keterangan")
    val keterangan: String = "",

    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,

    @ColumnInfo(name = "photo_base64")
    val photoBase64: String? = null,

    @ColumnInfo(name = "photo_size_kb")
    val photoSizeKb: Long? = null,

    @ColumnInfo(name = "photo_dimensions")
    val photoDimensions: String? = null,

    @ColumnInfo(name = "client_transaction_id")
    val clientTransactionId: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
