package com.example.data.model

/**
 * Daftar Konstanta Wilayah Kerja Operasional di RKCE
 */
val LIST_WILAYAH = listOf(
    "Geodesi 1",
    "Geodesi 2",
    "Geodesi 3",
    "RC 1",
    "RC 2",
    "RC 3",
    "LEBUNG PG12",
    "LEBUNG PG3"
)

/**
 * Daftar opsi filter wilayah khusus akun Manager (dengan 'Semua Wilayah' di urutan pertama)
 * Sesuai format panel filter eksekutif:
 * [Semua Wilayah, Geodesi 1, Geodesi 2, Geodesi 3, RC 1, RC 2, RC 3, LEBUNG PG12, LEBUNG PG3]
 */
val LIST_WILAYAH_MANAGER = listOf(
    "Semua Wilayah",
    "Geodesi 1",
    "Geodesi 2",
    "Geodesi 3",
    "RC 1",
    "RC 2",
    "RC 3",
    "LEBUNG PG12",
    "LEBUNG PG3"
)

object WilayahConstants {
    val LIST_WILAYAH = com.example.data.model.LIST_WILAYAH
    val LIST_WILAYAH_MANAGER = com.example.data.model.LIST_WILAYAH_MANAGER
    val LIST_WILAYAH_MANAGER_FILTER = LIST_WILAYAH_MANAGER

    fun matches(itemWilayah: String?, selectedFilter: String?): Boolean {
        if (selectedFilter.isNullOrBlank() || selectedFilter.equals("Semua Wilayah", ignoreCase = true)) {
            return true
        }
        if (itemWilayah.isNullOrBlank()) {
            return false
        }
        val cleanItem = itemWilayah.trim()
        val cleanFilter = selectedFilter.trim()
        if (cleanItem.equals(cleanFilter, ignoreCase = true)) return true

        // Bidirectional alias matching untuk varian nama wilayah
        if (cleanFilter.equals("Geodesi 1", ignoreCase = true) || cleanFilter.equals("Geodesi PG1", ignoreCase = true)) {
            if (cleanItem.contains("Geodesi 1", ignoreCase = true) || cleanItem.contains("PG1", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("Geodesi 2", ignoreCase = true) || cleanFilter.equals("Geodesi PG2", ignoreCase = true)) {
            if (cleanItem.contains("Geodesi 2", ignoreCase = true) || cleanItem.contains("PG2", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("Geodesi 3", ignoreCase = true)) {
            if (cleanItem.contains("Geodesi 3", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("RC 1", ignoreCase = true) || cleanFilter.equals("RC PG1", ignoreCase = true)) {
            if (cleanItem.contains("RC 1", ignoreCase = true) || cleanItem.contains("RC PG1", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("RC 2", ignoreCase = true) || cleanFilter.equals("RC PG2", ignoreCase = true)) {
            if (cleanItem.contains("RC 2", ignoreCase = true) || cleanItem.contains("RC PG2", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("RC 3", ignoreCase = true) || cleanFilter.equals("RC PG3", ignoreCase = true)) {
            if (cleanItem.contains("RC 3", ignoreCase = true) || cleanItem.contains("RC PG3", ignoreCase = true) || cleanItem.equals("PG3", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("LEBUNG PG12", ignoreCase = true) || cleanFilter.equals("LEBUNG 12", ignoreCase = true)) {
            if (cleanItem.contains("LEBUNG PG12", ignoreCase = true) || cleanItem.contains("LEBUNG 12", ignoreCase = true)) return true
        }
        if (cleanFilter.equals("LEBUNG PG3", ignoreCase = true) || cleanFilter.equals("LEBUNG 3", ignoreCase = true)) {
            if (cleanItem.contains("LEBUNG PG3", ignoreCase = true) || cleanItem.contains("LEBUNG 3", ignoreCase = true)) return true
        }

        return false
    }

    fun normalizeWilayah(raw: String?): String {
        if (raw.isNullOrBlank()) return "Geodesi 1"
        val trimmed = raw.trim()
        val found = LIST_WILAYAH.find { it.equals(trimmed, ignoreCase = true) }
        if (found != null) return found

        return when {
            trimmed.contains("RC", ignoreCase = true) && trimmed.contains("3") -> "RC 3"
            trimmed.contains("RC", ignoreCase = true) && trimmed.contains("2") -> "RC 2"
            trimmed.contains("RC", ignoreCase = true) -> "RC 1"
            trimmed.contains("LEBUNG", ignoreCase = true) && (trimmed.contains("12") || trimmed.contains("1") || trimmed.contains("2")) -> "LEBUNG PG12"
            trimmed.contains("LEBUNG", ignoreCase = true) -> "LEBUNG PG3"
            trimmed.contains("PG3", ignoreCase = true) -> "RC 3"
            trimmed.contains("PG2", ignoreCase = true) || trimmed.contains("2") -> "Geodesi 2"
            trimmed.contains("3") -> "Geodesi 3"
            else -> "Geodesi 1"
        }
    }
}
