package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

@Entity(
    tableName = "units",
    indices = [
        Index(value = ["unitType"]),
        Index(value = ["supervisorAssigned"]),
        Index(value = ["wilayah"])
    ]
)
data class UnitEntity(
    @PrimaryKey 
    @SerializedName("kode_unit")
    @Json(name = "kode_unit")
    val unitCode: String, // e.g. "HEH0004", "HEH0013", "MGC0001", "TTM010"

    @SerializedName("jenis_unit")
    @Json(name = "jenis_unit")
    val unitType: String, // String murni: e.g. "EXCAVATOR", "COMPACTOR", "MOTOR GRADER", "DUMP TRUCK"

    @SerializedName("model_unit")
    @Json(name = "model_unit")
    val brandModel: String, // e.g. "Komatsu PC200-8", "CAT 320D", "Hino Dutro 130 HD"

    @SerializedName("last_hm")
    @Json(name = "last_hm")
    val lastHm: Double, // e.g. 3420.5

    @SerializedName("kasie_penanggung_jawab")
    @Json(name = "kasie_penanggung_jawab")
    val supervisorAssigned: String, // e.g. "Jahidi", "Agung", "Yono"

    val status: String = "OPERASIONAL", // "OPERASIONAL", "STANDBY", "MAINTENANCE", "Aktif"
    val hmMinStandar: Double = 6.0,
    val hmMaxStandar: Double = 10.0,
    val wilayah: String = "Geodesi PG1",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val kodeUnit: String get() = unitCode
    val jenisUnit: String get() = unitType
    val modelTipe: String get() = brandModel
    val kasiePenanggungJawab: String get() = supervisorAssigned
}

typealias MasterUnitEntity = UnitEntity
