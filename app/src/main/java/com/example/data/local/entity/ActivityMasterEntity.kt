package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "master_activities")
data class ActivityMasterEntity(
    @PrimaryKey
    @ColumnInfo(name = "sapCode")
    @SerializedName("kode_sap")
    val sapCode: String,                  // e.g. "ACT-SAP-101", "ACT-SAP-102"

    @ColumnInfo(name = "activityName")
    @SerializedName("nama_aktivitas")
    val activityName: String,             // e.g. "Galian Tanah Lunak", "Loading Overburden"

    @ColumnInfo(name = "unitOfMeasure")
    @SerializedName("satuan")
    val unitOfMeasure: String,            // e.g. "m3", "m2", "m", "ton", "rit"

    @ColumnInfo(name = "description")
    @SerializedName("description")
    val description: String = "",

    @ColumnInfo(name = "category")
    @SerializedName("kategori")
    val category: String = "PRODUKSI",    // "PRODUKSI", "SUPPORT", "HAULING", "INFRASTRUKTUR"

    @ColumnInfo(name = "kategori_status", defaultValue = "OPERASI")
    @SerializedName("kategori_status")
    val kategoriStatus: String = "OPERASI", // "OPERASI", "STANDBY", "BREAKDOWN"

    @ColumnInfo(name = "jenis_unit", defaultValue = "ALL")
    @SerializedName("jenis_unit")
    val jenisUnit: String = "ALL", // "EXCAVATOR", "DUMP TRUCK", "BULLDOZER", "WATER TRUCK", "MOTOR GRADER", "ALL"

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)

typealias MasterAktivitasEntity = ActivityMasterEntity
