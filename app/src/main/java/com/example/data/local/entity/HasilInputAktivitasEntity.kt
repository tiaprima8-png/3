package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hasil_input_aktivitas",
    indices = [
        Index(value = ["rencana_id"]),
        Index(value = ["kode_unit"]),
        Index(value = ["supervisor_name"]),
        Index(value = ["created_at"]),
        Index(value = ["nomor_spk"], unique = true)
    ]
)
data class HasilInputAktivitasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "rencana_id")
    val rencanaId: Long = 0,

    @ColumnInfo(name = "nomor_spk")
    val nomorSpk: String = "SPK-AUTO-${java.util.UUID.randomUUID()}",

    @ColumnInfo(name = "supervisor_name")
    val supervisorName: String = "",

    @ColumnInfo(name = "nama_kasie", defaultValue = "")
    val namaKasie: String = supervisorName,

    @ColumnInfo(name = "created_by", defaultValue = "")
    val createdBy: String = supervisorName,

    @ColumnInfo(name = "kode_unit")
    val kodeUnit: String = "",

    @ColumnInfo(name = "operator")
    val operator: String = "",

    @ColumnInfo(name = "kode_lokasi")
    val kodeLokasi: String = "",

    @ColumnInfo(name = "activity_name")
    val activityName: String = "",

    @ColumnInfo(name = "sap_code")
    val sapCode: String = "",

    @ColumnInfo(name = "satuan")
    val satuan: String = "m3",

    @ColumnInfo(name = "unit_status")
    val unitStatus: String = "OPERASI",

    @ColumnInfo(name = "shift")
    val shift: String = "SIANG",

    @ColumnInfo(name = "jam_kerja")
    val jamKerja: Double = 7.0,

    @ColumnInfo(name = "hm_awal")
    val hmAwal: Double = 0.0,

    @ColumnInfo(name = "hm_akhir")
    val hmAkhir: Double = 0.0,

    @ColumnInfo(name = "hm_harian_berjalan")
    val hmHarianBerjalan: Double = 0.0,

    @ColumnInfo(name = "is_isi_solar")
    val isIsiSolar: Boolean = false,

    @ColumnInfo(name = "jumlah_liter_solar")
    val jumlahLiterSolar: Double = 0.0,

    @ColumnInfo(name = "stik_solar_awal", defaultValue = "0.0")
    val stikSolarAwal: Double = 0.0,

    @ColumnInfo(name = "stik_solar_akhir", defaultValue = "0.0")
    val stikSolarAkhir: Double = 0.0,

    @ColumnInfo(name = "pemakaian_solar", defaultValue = "0.0")
    val pemakaianSolar: Double = 0.0,

    @ColumnInfo(name = "kendala_id")
    val kendalaId: String? = null,

    @ColumnInfo(name = "kendala_nama")
    val kendalaNama: String? = null,

    @ColumnInfo(name = "catatan_kendala")
    val catatanKendala: String? = null,

    @ColumnInfo(name = "daftar_kendala_json")
    val daftarKendalaJson: String? = null,

    @ColumnInfo(name = "hasil_kerja")
    val hasilKerja: Double = 0.0,

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

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "tanggal_format")
    val tanggalFormat: String = "",

    @ColumnInfo(name = "is_hm_awal_corrected", defaultValue = "0")
    val isHmAwalCorrected: Boolean = false,

    @ColumnInfo(name = "alasan_koreksi_hm")
    val alasanKoreksiHm: String? = null,

    @ColumnInfo(name = "client_transaction_id", defaultValue = "''")
    val clientTransactionId: String = "",

    @ColumnInfo(name = "status_kirim", defaultValue = "'PENDING'")
    val statusKirim: String = "PENDING",

    @ColumnInfo(name = "wilayah", defaultValue = "PG1")
    val wilayah: String? = "PG1",

    @ColumnInfo(name = "approval_status", defaultValue = "'Menunggu Approval'")
    val approvalStatus: String = "Menunggu Approval",

    @ColumnInfo(name = "approved_by")
    val approvedBy: String? = null,

    @ColumnInfo(name = "approved_at")
    val approvedAt: String? = null,

    @ColumnInfo(name = "is_locked", defaultValue = "0")
    val isLocked: Boolean = false,

    @ColumnInfo(name = "operator_nik")
    val operatorNik: String? = null
)
