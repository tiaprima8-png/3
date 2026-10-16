package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    indices = [
        Index(value = ["unitType"]),
        Index(value = ["supervisorAssigned"]),
        Index(value = ["wilayah"])
    ]
)
data class UnitEntity(
    @PrimaryKey val unitCode: String, // e.g. "HEH0004", "HEH0013", "MGC0001"
    val unitType: String,            // e.g. "EXCAVATOR", "COMPACTOR", "MOTOR GRADER"
    val brandModel: String,          // e.g. "Komatsu PC200-8", "CAT 320D"
    val lastHm: Double,              // e.g. 3420.5 (used as auto-fill HM Awal)
    val supervisorAssigned: String,  // e.g. "Agung", "Zainudin"
    val status: String = "OPERASIONAL", // "OPERASIONAL", "STANDBY", "MAINTENANCE"
    val hmMinStandar: Double = 6.0,  // Standar minimal HM (misal 6.0 HM)
    val hmMaxStandar: Double = 10.0, // Standar maksimal HM (misal 10.0 HM)
    val wilayah: String = "Geodesi PG1",     // 8 Wilayah Kerja baru
    val updatedAt: Long = System.currentTimeMillis()
)

typealias MasterUnitEntity = UnitEntity
