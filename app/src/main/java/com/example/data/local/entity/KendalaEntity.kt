package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_kendala")
data class KendalaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val kodeKendala: String,              // e.g. "KND-01" s/d "KND-15"
    val namaKendala: String,              // e.g. "Antar - jemput", "Cek unit, Implement Pemanasan mesin", dll.
    val kategori: String = "OPERASIONAL", // "CUACA", "MEKANIKAL", "OPERASIONAL", "SUPPORT", "LOGISTIK"
    val deskripsi: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

