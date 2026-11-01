package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rencana_kerja",
    indices = [
        Index(value = ["kode_unit"]),
        Index(value = ["supervisor_name"]),
        Index(value = ["is_realized"]),
        Index(value = ["created_at"])
    ]
)
data class RencanaKerjaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "supervisor_name")
    val supervisorName: String,

    @ColumnInfo(name = "pengawas", defaultValue = "")
    val pengawas: String = supervisorName,

    @ColumnInfo(name = "kasie", defaultValue = "")
    val kasie: String = supervisorName,

    @ColumnInfo(name = "created_by", defaultValue = "")
    val createdBy: String = supervisorName,

    @ColumnInfo(name = "status_unit")
    val statusUnit: String = "OPERASI", // "OPERASI", "STANDBY", "BREAKDOWN"

    @ColumnInfo(name = "kode_unit")
    val kodeUnit: String,

    @ColumnInfo(name = "operator")
    val operator: String,

    @ColumnInfo(name = "nama_operator", defaultValue = "")
    val namaOperator: String = operator,

    @ColumnInfo(name = "operator_nik", defaultValue = "")
    val operatorNik: String = "",

    @ColumnInfo(name = "sap_code", defaultValue = "")
    val sapCode: String = "",

    @ColumnInfo(name = "nama_aktivitas", defaultValue = "")
    val namaAktivitas: String = "",

    @ColumnInfo(name = "satuan_aktivitas", defaultValue = "m3")
    val satuanAktivitas: String = "m3",

    @ColumnInfo(name = "kode_lokasi")
    val kodeLokasi: String,

    @ColumnInfo(name = "wilayah", defaultValue = "PG1")
    val wilayah: String? = "PG1",

    @ColumnInfo(name = "shift_kerja")
    val shiftKerja: String = "SIANG", // "SIANG", "MALAM"

    @ColumnInfo(name = "keterangan")
    val keterangan: String = "",

    @ColumnInfo(name = "nomor_spk")
    val nomorSpk: String = "",

    @ColumnInfo(name = "status_spk")
    val statusSpk: String = "MENUNGGU_SPK", // "MENUNGGU_SPK", "TERBIT_SPK", "SELESAI"

    @ColumnInfo(name = "status_tahap", defaultValue = "RENCANA")
    val statusTahap: String = "RENCANA", // "RENCANA", "REALISASI_SELESAI"

    @ColumnInfo(name = "is_realized", defaultValue = "0")
    val isRealized: Boolean = false,

    @ColumnInfo(name = "status_realisasi", defaultValue = "BELUM")
    val statusRealisasi: String = if (isRealized || statusTahap == "REALISASI_SELESAI") "SUDAH" else "BELUM", // "BELUM", "SUDAH"

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "tanggal_format")
    val tanggalFormat: String = "",

    @ColumnInfo(name = "sub_index", defaultValue = "1")
    val subIndex: Int = 1,

    @ColumnInfo(name = "parent_spk", defaultValue = "")
    val parentSpk: String = ""
) {
    val isSpkTerbit: Boolean
        get() {
            val cleanSpk = nomorSpk.trim()
            val isValidSpkString = cleanSpk.isNotBlank() &&
                    !cleanSpk.equals("null", ignoreCase = true) &&
                    !cleanSpk.equals("Belum Terbit (Menunggu Admin)", ignoreCase = true) &&
                    !cleanSpk.equals("SPK Menyusul", ignoreCase = true) &&
                    cleanSpk != "-" &&
                    !cleanSpk.equals("Menunggu SPK", ignoreCase = true) &&
                    !cleanSpk.equals("MENUNGGU", ignoreCase = true)
            val isStatusTerbit = statusSpk.equals("SPK_TERBIT", ignoreCase = true) ||
                    statusSpk.equals("TERBIT_SPK", ignoreCase = true) ||
                    statusSpk.equals("TERBIT", ignoreCase = true) ||
                    statusSpk.equals("APPROVED", ignoreCase = true) ||
                    statusSpk.equals("SELESAI", ignoreCase = true)
            return isValidSpkString && (isStatusTerbit || cleanSpk.startsWith("SPK", ignoreCase = true) || cleanSpk.length >= 1)
        }

    val kategori: String
        get() = statusUnit

    val satuan: String
        get() = satuanAktivitas
}
