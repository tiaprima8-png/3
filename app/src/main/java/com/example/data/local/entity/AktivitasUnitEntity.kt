package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "aktivitas_unit")
data class AktivitasUnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tanggal")
    val tanggal: String = "",

    @ColumnInfo(name = "kategori_status")
    @SerializedName("kategori_status")
    val kategoriStatus: String = "OPERASI",

    @ColumnInfo(name = "durasi_jam_kerja")
    val durasiJamKerja: Double = 7.0,

    @ColumnInfo(name = "durasi_hm")
    val durasiHm: Double = 0.0,

    @ColumnInfo(name = "pengisian_solar")
    val pengisianSolar: Double = 0.0,

    @ColumnInfo(name = "nama_pengawas")
    val namaPengawas: String = "",

    @ColumnInfo(name = "kode_unit")
    val kodeUnit: String = "",

    @ColumnInfo(name = "nama_aktivitas")
    val namaAktivitas: String = "",

    @ColumnInfo(name = "kode_sap")
    val kodeSap: String = "",

    @ColumnInfo(name = "satuan")
    val satuan: String = "m3",

    @ColumnInfo(name = "nama_operator")
    val namaOperator: String = "",

    @ColumnInfo(name = "lokasi_kerja")
    val lokasiKerja: String = "",

    @ColumnInfo(name = "wilayah")
    val wilayah: String? = null,

    @ColumnInfo(name = "shift")
    val shift: String = "SIANG",

    @ColumnInfo(name = "hasil_kerja")
    val hasilKerja: Double = 0.0,

    @ColumnInfo(name = "keterangan")
    val keterangan: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
