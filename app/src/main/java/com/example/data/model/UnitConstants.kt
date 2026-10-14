package com.example.data.model

/**
 * Daftar Jenis Unit Operasional Aktif di RKCE (Web Admin & Mobile Android).
 * Jenis Dump Truck dan Bulldozer telah dihapus secara permanen.
 */
val LIST_JENIS_UNIT_AKTIF = listOf(
    "Excavator Long Arm",
    "Excavator Mini",
    "Excavator Standar",
    "Motor Grader",
    "Compactor"
)

val LIST_JENIS_UNIT_FILTER = listOf(
    "Semua Jenis",
    "Excavator Long Arm",
    "Excavator Mini",
    "Excavator Standar",
    "Motor Grader",
    "Compactor"
)

object UnitConstants {
    val AKTIF = LIST_JENIS_UNIT_AKTIF
    val FILTER = LIST_JENIS_UNIT_FILTER

    fun isJenisUnitValid(jenis: String?): Boolean {
        if (jenis.isNullOrBlank()) return false
        val lower = jenis.lowercase().trim()
        if (lower.contains("dump") || lower.contains("bulldozer") || lower.contains("dozer") || lower == "dt" || lower == "dz") {
            return false
        }
        return true
    }
}
