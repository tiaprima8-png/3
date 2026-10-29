package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lokasi",
    indices = [
        Index(value = ["kode_lokasi"]),
        Index(value = ["wilayah"]),
        Index(value = ["status"])
    ]
)
data class LokasiEntity(
    @PrimaryKey
    @ColumnInfo(name = "kode_lokasi")
    val kodeLokasi: String,

    @ColumnInfo(name = "wilayah")
    val wilayah: String,

    @ColumnInfo(name = "luas_bruto")
    val luasBruto: Double = 0.0,

    @ColumnInfo(name = "luas_netto")
    val luasNetto: Double = 0.0,

    @ColumnInfo(name = "keterangan")
    val keterangan: String = "",

    @ColumnInfo(name = "status")
    val status: String = "Aktif",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

typealias MasterLokasiEntity = LokasiEntity

