package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey val operatorId: String,  // e.g. "OPR-001"
    val name: String,                    // e.g. "Ahmad Fauzi"
    val badgeNumber: String,             // e.g. "NIK-94821"
    val supervisorAssigned: String,      // e.g. "Budi Santoso", "Agus Wijaya"
    val skillLevel: String = "Class A - Excavator & Dozer",
    val status: String = "AKTIF",
    val wilayah: String? = "Geodesi PG1",
    val updatedAt: Long = System.currentTimeMillis()
)

typealias MasterOperatorEntity = OperatorEntity
