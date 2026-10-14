package com.example.ui.viewmodel

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import java.util.UUID

enum class UnitPlanCategory {
    BACKLOG_YESTERDAY, // Kasus A: Kemarin belum diisi -> Wajib diisi dulu
    NO_PLAN_TODAY,     // Kasus B: Hari ini belum ada rencana
    HAS_PLAN_TODAY     // Kasus C: Hari ini sudah ada rencana -> Tambah rencana susulan
}

data class UnitPlanStatus(
    val unit: UnitEntity,
    val targetDate: String, // "09/10/2026" or "10/10/2026"
    val isBacklog: Boolean = false, // true if yesterday
    val isSusulan: Boolean = false, // true if today already has >= 1 plan (Kasus C)
    val existingPlanCount: Int = 0, // Jumlah rencana yang sudah ada hari ini
    val category: UnitPlanCategory = when {
        isBacklog -> UnitPlanCategory.BACKLOG_YESTERDAY
        isSusulan -> UnitPlanCategory.HAS_PLAN_TODAY
        else -> UnitPlanCategory.NO_PLAN_TODAY
    },
    val labelText: String
)

data class SubRencanaFormState(
    val id: String = UUID.randomUUID().toString(),
    val selectedOperator: OperatorEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val shiftKerja: String = "SIANG",
    val keterangan: String = ""
)

data class RencanaFormUiState(
    val statusUnit: String = "OPERASI", // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val selectedUnitStatus: UnitPlanStatus? = null,
    val selectedUnit: UnitEntity? = null,
    val targetDate: String = "",
    val isBacklog: Boolean = false,
    val isSusulan: Boolean = false, // True jika menginput rencana kerja susulan untuk unit yang sudah terisi
    val selectedOperator: OperatorEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val shiftKerja: String = "SIANG", // "SIANG" | "MALAM"
    val keterangan: String = "",
    val subPlans: List<SubRencanaFormState> = emptyList(),
    val isSubmitting: Boolean = false
)

data class KendalaItemFormState(
    val id: String = UUID.randomUUID().toString(),
    val selectedKendala: KendalaEntity? = null,
    val waktuMulai: String = "",
    val waktuSelesai: String = ""
) {
    val startMinutes: Int?
        get() = parseTimeToMinutes(waktuMulai)

    val endMinutes: Int?
        get() = parseTimeToMinutes(waktuSelesai)

    val isCrossMidnight: Boolean
        get() {
            val s = startMinutes ?: return false
            val e = endMinutes ?: return false
            return s > e
        }

    val isTimeOrderValid: Boolean
        get() {
            val startMin = startMinutes ?: return false
            val endMin = endMinutes ?: return false
            return endMin > startMin
        }

    val durasiMenit: Int?
        get() {
            val startMin = startMinutes ?: return null
            val endMin = endMinutes ?: return null
            if (endMin == startMin) return null
            return if (endMin > startMin) {
                endMin - startMin
            } else {
                // Shift malam melintasi tengah malam (misal: 23:00 - 02:00 = 3 jam / 180 menit)
                (endMin + 1440) - startMin
            }
        }

    val isTimeRangeValid: Boolean
        get() {
            if (waktuMulai.isBlank() || waktuSelesai.isBlank()) return false
            val startMin = startMinutes ?: return false
            val endMin = endMinutes ?: return false
            return startMin != endMin
        }

    val isValid: Boolean
        get() = selectedKendala != null && isTimeRangeValid

    val durasiLabel: String
        get() {
            val minutes = durasiMenit ?: return ""
            val hours = minutes / 60
            val remainingMins = minutes % 60
            val crossBadge = if (isCrossMidnight) " (Shift Malam/Lintas Hari)" else ""
            return when {
                hours > 0 && remainingMins > 0 -> "$hours Jam $remainingMins Menit$crossBadge"
                hours > 0 -> "$hours Jam$crossBadge"
                else -> "$remainingMins Menit$crossBadge"
            }
        }

    companion object {
        fun parseTimeToMinutes(timeStr: String): Int? {
            val trimmed = timeStr.trim()
            val parts = trimmed.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h * 60 + m
        }

        fun getIntervalPieces(item: KendalaItemFormState): List<Pair<Int, Int>> {
            val s = item.startMinutes ?: return emptyList()
            val e = item.endMinutes ?: return emptyList()
            if (s == e) return emptyList()
            return if (s < e) {
                listOf(Pair(s, e))
            } else {
                listOf(Pair(s, 1440), Pair(0, e))
            }
        }

        /**
         * Validasi Tumpang Tindih (Anti-Overlapping):
         * Waktu kejadian kendala baru TIDAK BOLEH berada di dalam rentang waktu kendala yang sudah diinput sebelumnya.
         * Mendukung rentang normal maupun lintas tengah malam.
         */
        fun hasOverlap(itemA: KendalaItemFormState, itemB: KendalaItemFormState): Boolean {
            val piecesA = getIntervalPieces(itemA)
            val piecesB = getIntervalPieces(itemB)
            if (piecesA.isEmpty() || piecesB.isEmpty()) return false
            for ((sA, eA) in piecesA) {
                for ((sB, eB) in piecesB) {
                    if (maxOf(sA, sB) < minOf(eA, eB)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}

data class RealisasiFormUiState(
    val selectedRencana: RencanaKerjaEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val nomorSpk: String = "",
    val statusSpk: String = "MENUNGGU",
    val durasiJamKerja: Double = 8.0,
    // Fleksibilitas Koreksi HM Awal & Logika Pergantian Shift
    val hmAwalOriginal: Double = 0.0,
    val hmAwal: Double = 0.0,
    val hmAwalInput: String = "",
    val isHmAwalCorrected: Boolean = false,
    val alasanKoreksiHm: String = "",
    val hmAkhirInput: String = "",
    val hmHarianBerjalan: Double = 0.0,
    val isHmValid: Boolean = true,
    val hmErrorMessage: String? = null,
    val hmMinStandar: Double = 6.0,
    val hmMaxStandar: Double = 10.0,
    val isUnderMinStandard: Boolean = false,
    val selectedKendala: KendalaEntity? = null,
    val catatanKendala: String = "",
    // Multi-Kendala Form State
    val daftarKendala: List<KendalaItemFormState> = listOf(KendalaItemFormState()),
    // Parameter Stik Solar BBM
    val stikSolarAwalInput: String = "",
    val stikSolarAkhirInput: String = "",
    val isIsiSolar: Boolean = false,
    val jumlahLiterSolarInput: String = "",
    val pemakaianSolarOtomatis: Double = 0.0,
    val hasilKerjaInput: String = "",
    val keterangan: String = "",
    val photoPath: String? = null,
    val photoBase64: String? = null,
    val photoSizeKb: Long? = null,
    val photoDimensions: String? = null,
    val isCompressingPhoto: Boolean = false,
    val isSubmitting: Boolean = false,
    // Kontrak Transaksi Unik & Anti-Duplikasi Offline (Idempotency)
    val clientTransactionId: String = "TX-" + java.util.UUID.randomUUID().toString(),
    // Kesinambungan HM Berantai (Sequential HM)
    val isSequentialHmFromPreviousPlan: Boolean = false,
    val sequentialSourcePlanText: String = ""
) {
    val isSpkTerbit: Boolean
        get() {
            val spk = nomorSpk.trim().ifBlank { selectedRencana?.nomorSpk?.trim() ?: "" }
            val isValidSpkString = spk.isNotBlank() &&
                    !spk.equals("null", ignoreCase = true) &&
                    !spk.equals("Belum Terbit (Menunggu Admin)", ignoreCase = true) &&
                    !spk.equals("SPK Menyusul", ignoreCase = true) &&
                    spk != "-" &&
                    !spk.equals("Menunggu SPK", ignoreCase = true) &&
                    !spk.equals("MENUNGGU", ignoreCase = true)
            val status = selectedRencana?.statusSpk ?: statusSpk
            val isStatusTerbit = status.equals("SPK_TERBIT", ignoreCase = true) ||
                    status.equals("TERBIT_SPK", ignoreCase = true) ||
                    status.equals("TERBIT", ignoreCase = true) ||
                    status.equals("APPROVED", ignoreCase = true) ||
                    status.equals("SELESAI", ignoreCase = true)
            return isValidSpkString && (isStatusTerbit || spk.startsWith("SPK", ignoreCase = true) || spk.length >= 1)
        }

    val parsedStikAwal: Double?
        get() = stikSolarAwalInput.trim().replace(',', '.').toDoubleOrNull()

    val parsedStikAkhir: Double?
        get() = stikSolarAkhirInput.trim().replace(',', '.').toDoubleOrNull()

    val parsedJumlahLiterSolar: Double?
        get() = if (isIsiSolar) jumlahLiterSolarInput.trim().replace(',', '.').toDoubleOrNull() else 0.0

    // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
    val computedKonsumsiSolar: Double
        get() {
            val awal = parsedStikAwal ?: 0.0
            val akhir = parsedStikAkhir ?: 0.0
            val refuel = if (isIsiSolar) (parsedJumlahLiterSolar ?: 0.0) else 0.0
            return (awal + refuel) - akhir
        }

    // 1. Jika Stik Solar Akhir > Stik Solar Awal dan toggle [Isi Solar Tambahan] MATI
    val isStikAkhirHigherWithoutRefuel: Boolean
        get() {
            val awal = parsedStikAwal
            val akhir = parsedStikAkhir
            return awal != null && akhir != null && akhir > awal && !isIsiSolar
        }

    // 2. Jika toggle [Isi Solar Tambahan] AKTIF: wajibkan input Jumlah Liter Pengisian BBM
    val isRefuelLitersMissing: Boolean
        get() = isIsiSolar && (jumlahLiterSolarInput.isBlank() || (parsedJumlahLiterSolar ?: 0.0) <= 0.0)

    // 3. Cegah hasil konsumsi bernilai negatif
    val isSolarConsumptionNegative: Boolean
        get() {
            val awal = parsedStikAwal
            val akhir = parsedStikAkhir
            if (awal != null && akhir != null) {
                val refuel = if (isIsiSolar) (parsedJumlahLiterSolar ?: 0.0) else 0.0
                return ((awal + refuel) - akhir) < 0.0
            }
            return false
        }

    val totalHambatanMenit: Int
        get() = daftarKendala.mapNotNull { it.durasiMenit }.sum()

    val totalHambatanLabel: String
        get() {
            val total = totalHambatanMenit
            val hours = total / 60
            val minutes = total % 60
            return when {
                total == 0 -> "0 Menit"
                hours > 0 && minutes > 0 -> "$hours Jam $minutes Menit"
                hours > 0 -> "$hours Jam"
                else -> "$minutes Menit"
            }
        }

    // Deteksi overlap untuk baris kendala tertentu terhadap baris lain
    fun isKendalaOverlapping(index: Int): Boolean {
        val current = daftarKendala.getOrNull(index) ?: return false
        if (!current.isTimeRangeValid) return false
        return daftarKendala.indices.any { j ->
            j != index && daftarKendala[j].isTimeRangeValid && KendalaItemFormState.hasOverlap(current, daftarKendala[j])
        }
    }

    // Deteksi khusus jika bertabrakan dengan kendala sebelumnya (index < current)
    fun isKendalaOverlappingWithPrevious(index: Int): Boolean {
        val current = daftarKendala.getOrNull(index) ?: return false
        if (!current.isTimeRangeValid) return false
        return (0 until index).any { j ->
            daftarKendala[j].isTimeRangeValid && KendalaItemFormState.hasOverlap(current, daftarKendala[j])
        }
    }

    // Cek apakah ada kendala yang bertabrakan di seluruh formulir
    val hasAnyKendalaOverlap: Boolean
        get() {
            for (i in 0 until daftarKendala.size) {
                if (!daftarKendala[i].isTimeRangeValid) continue
                for (j in i + 1 until daftarKendala.size) {
                    if (!daftarKendala[j].isTimeRangeValid) continue
                    if (KendalaItemFormState.hasOverlap(daftarKendala[i], daftarKendala[j])) {
                        return true
                    }
                }
            }
            return false
        }

    // Batas maksimal jam kerja shift (misal: 7.0 atau 8.0 jam)
    val maxShiftHours: Double
        get() = if (durasiJamKerja > 0.0) durasiJamKerja else 8.0

    // Akumulasi total jam kendala melebihi durasi kerja shift
    val isTotalKendalaExceedingShift: Boolean
        get() = totalHambatanMenit > (maxShiftHours * 60)

    // ======================================================================
    // 3. VALIDASI LONJAKAN HM EKSTREM (ANTI-TYPO FAT FINGER)
    // ======================================================================
    // Selisih HM berjalan: durasi_hm = hm_akhir - hm_awal
    val isHmJumpExtreme24h: Boolean
        get() {
            val akhir = hmAkhirInput.trim().replace(',', '.').toDoubleOrNull() ?: return false
            val durasi = akhir - hmAwal
            return durasi > 24.0
        }

    val isHmWarningExceed14h: Boolean
        get() {
            val akhir = hmAkhirInput.trim().replace(',', '.').toDoubleOrNull() ?: return false
            val durasi = akhir - hmAwal
            return durasi > 14.0 && durasi <= 24.0
        }
}

data class UnitShiftProgressItem(
    val unitCode: String,
    val isRealized: Boolean,
    val rencanaCount: Int,
    val latestActivity: String = "",
    val latestOperator: String = "",
    val spk: String = ""
)

data class FleetShiftProgressState(
    val realizedUnitsCount: Int = 0,
    val totalUnitsCount: Int = 0,
    val pendingUnitsCount: Int = 0,
    val progressPercentage: Float = 0f,
    val isAllCompleted: Boolean = false,
    val unitItems: List<UnitShiftProgressItem> = emptyList()
)

data class UnitAnalyticsRow(
    val unitCode: String,
    val model: String,
    val totalWorkHours: Double,
    val totalHmRunning: Double,
    val totalFuelLiters: Double,
    val efficiencyRatio: Double, // L/HM
    val standbyBreakdownCount: Int,
    val totalOutputVolume: Double,
    val unitOfMeasure: String
)

data class ActivityAnalyticsRow(
    val activityName: String,
    val sapCode: String,
    val unitOfMeasure: String,
    val totalOutputVolume: Double,
    val totalHm: Double,
    val unitInvolvedCount: Int,
    val kategoriStatus: String = "OPERASI"
)

data class FleetAnalyticsState(
    val selectedPeriod: String = "Bulan Ini", // "Bulan Ini", "30 Hari", "Semua"
    val selectedUnitFilter: String = "Semua Unit",
    val selectedWilayahFilter: String = "Semua Wilayah",
    val selectedJenisUnitFilter: String = "Semua Jenis",
    val totalSolarLiters: Double = 0.0,
    val totalHmRunning: Double = 0.0,
    val avgFuelConsumptionRatio: Double = 0.0, // Liter / HM
    val effectiveOperationHours: Double = 0.0,
    val standbyHours: Double = 0.0,
    val breakdownHours: Double = 0.0,
    val operationPercent: Float = 0f,
    val standbyPercent: Float = 0f,
    val breakdownPercent: Float = 0f,
    val unitSummaries: List<UnitAnalyticsRow> = emptyList(),
    val activitySummaries: List<ActivityAnalyticsRow> = emptyList()
)
