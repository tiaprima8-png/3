package com.example.data.model

/**
 * Daftar Jenis Unit Operasional Aktif di RKCE (Web Admin & Mobile Android).
 * Mendukung Excavator, Motor Grader, Compactor, Dump Truck (termasuk TTM010 milik Kasie Jahidi), dll.
 */
val LIST_JENIS_UNIT_AKTIF = listOf(
    "Excavator Long Arm",
    "Excavator Mini",
    "Excavator Standar",
    "Motor Grader",
    "Compactor",
    "Dump Truck",
    "DUMP TRUCK"
)

val LIST_JENIS_UNIT_FILTER = listOf(
    "Semua Jenis",
    "Excavator Long Arm",
    "Excavator Mini",
    "Excavator Standar",
    "Motor Grader",
    "Compactor",
    "Dump Truck",
    "DUMP TRUCK"
)

object UnitConstants {
    val AKTIF = LIST_JENIS_UNIT_AKTIF
    val FILTER = LIST_JENIS_UNIT_FILTER

    fun isJenisUnitValid(jenis: String?): Boolean {
        if (jenis.isNullOrBlank()) return false
        return true
    }
}
