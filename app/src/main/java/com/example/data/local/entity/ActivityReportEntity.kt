package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "activity_reports",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["unitCode"]),
        Index(value = ["supervisorName"]),
        Index(value = ["isSynced"]),
        Index(value = ["reportDate"]),
        Index(value = ["nomorSpk"], unique = true)
    ]
)
data class ActivityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientUuid: String = UUID.randomUUID().toString(),
    val reportDate: String,              // Format: "DD/MM/YYYY"
    val supervisorName: String,          // Active supervisor / Kasie
    val namaKasie: String = supervisorName,
    val createdBy: String = supervisorName,
    val unitCode: String,                // Selected Unit Code e.g. "HEH0004"
    val unitType: String,                // e.g. "EXCAVATOR"
    val activityName: String,            // e.g. "Galian Tanah Lunak"
    val sapCode: String,                 // e.g. "ACT-SAP-101"
    val unitOfMeasure: String,           // e.g. "m3"
    val operatorName: String,            // Selected Operator e.g. "Ahmad Fauzi"
    val workLocation: String,            // e.g. "001A / Mainroad"
    val wilayah: String = "PG1",         // 8 Wilayah Kerja baru
    val shift: String,                   // "SIANG" or "MALAM"
    val workDurationHours: Double = 7.0, // Default 7.00 hours
    val hmAwal: Double,                  // Read-only from last HM
    val hmAkhir: Double,                 // Input by supervisor
    val hmDailyRunning: Double,          // (hmAkhir - hmAwal)
    val workOutput: Double,              // Numeric result
    val notes: String = "",              // Field notes
    val unitStatus: String = "OPERASI",  // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val isRefueled: Boolean = false,     // Fuel refuel toggle
    val fuelLiters: Double = 0.0,        // Liter solar
    val stikSolarAwal: Double = 0.0,     // Stik solar awal (cm/L)
    val stikSolarAkhir: Double = 0.0,    // Stik solar akhir (cm/L)
    val pemakaianSolar: Double = 0.0,    // Pemakaian solar
    val kendalaId: String? = null,       // ID kendala dari master
    val kendalaNama: String? = null,     // Nama kendala dari master
    val catatanKendala: String? = null,  // Catatan detail kendala
    val daftarKendalaJson: String? = null, // JSON array of multi-kendala items
    val photoPath: String? = null,       // Local compressed JPEG path
    val photoBase64: String? = null,     // Base64 data string (for sync)
    val photoSizeKb: Long? = null,       // Size in KB (e.g. 145)
    val photoDimensions: String? = null, // e.g. "1280x960 px"
    val isSynced: Boolean = false,       // 0 = pending/offline, 1 = synced
    val isHmAwalCorrected: Boolean = false, // Audit: apakah HM Awal dikoreksi manual
    val alasanKoreksiHm: String? = null,  // Catatan alasan koreksi HM
    @com.google.gson.annotations.SerializedName("approval_status")
    val approvalStatus: String = "Menunggu Approval",
    @com.google.gson.annotations.SerializedName("approved_by")
    val approvedBy: String? = null,
    @com.google.gson.annotations.SerializedName("approved_at")
    val approvedAt: String? = null,
    @com.google.gson.annotations.SerializedName("is_locked")
    val isLocked: Boolean = false,
    @com.google.gson.annotations.SerializedName("operator_nik")
    val operatorNik: String? = null,
    @com.google.gson.annotations.SerializedName("nomor_spk")
    val nomorSpk: String = "SPK-AUTO-$clientUuid",
    @com.google.gson.annotations.SerializedName("id_rencana_kerja")
    val idRencanaKerja: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)

typealias RealisasiEntity = ActivityReportEntity


