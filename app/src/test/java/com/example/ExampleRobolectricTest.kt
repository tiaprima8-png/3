package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RKCE", appName)
  }

  @Test
  fun `verify SPK_PUBLISHED json payload parsing and status`() {
    val jsonString = """{"unit_kode": "DT-102", "nomor_spk": "SPK/CE/2026/10/375", "id_rencana_kerja": 102}"""
    val json = org.json.JSONObject(jsonString)
    val unitKode = json.getString("unit_kode")
    val nomorSpk = json.getString("nomor_spk")
    val rencanaId = json.optInt("id_rencana_kerja")

    assertEquals("DT-102", unitKode)
    assertEquals("SPK/CE/2026/10/375", nomorSpk)
    assertEquals(102, rencanaId)

    val waitingPlan = com.example.data.local.entity.RencanaKerjaEntity(
        id = rencanaId.toLong(),
        supervisorName = "Budi Santoso",
        kodeUnit = unitKode,
        operator = "Ahmad Fauzi",
        kodeLokasi = "001A",
        nomorSpk = "",
        statusSpk = "MENUNGGU"
    )
    org.junit.Assert.assertFalse(waitingPlan.isSpkTerbit)

    val publishedPlan = waitingPlan.copy(
        nomorSpk = nomorSpk,
        statusSpk = "SPK_TERBIT"
    )
    org.junit.Assert.assertTrue(publishedPlan.isSpkTerbit)
    assertEquals("SPK/CE/2026/10/375", publishedPlan.nomorSpk)
  }

  @Test
  fun `verify multi kendala time order and anti overlapping validation`() {
    // 1. Validasi Urutan Waktu: Waktu Selesai harus > Waktu Mulai
    val kInvalidOrder = com.example.ui.viewmodel.KendalaItemFormState(
        waktuMulai = "10:00",
        waktuSelesai = "08:00"
    )
    org.junit.Assert.assertFalse(kInvalidOrder.isTimeOrderValid)
    org.junit.Assert.assertTrue(kInvalidOrder.isCrossMidnight)

    val kSameTime = com.example.ui.viewmodel.KendalaItemFormState(
        waktuMulai = "08:00",
        waktuSelesai = "08:00"
    )
    org.junit.Assert.assertFalse(kSameTime.isTimeOrderValid)
    org.junit.Assert.assertFalse(kSameTime.isTimeRangeValid)

    val kValid1 = com.example.ui.viewmodel.KendalaItemFormState(
        waktuMulai = "08:00",
        waktuSelesai = "10:00"
    )
    org.junit.Assert.assertTrue(kValid1.isTimeOrderValid)
    org.junit.Assert.assertTrue(kValid1.isTimeRangeValid)
    assertEquals(120, kValid1.durasiMenit)

    // 2. Validasi Tumpang Tindih (Anti-Overlapping):
    // Kendala 1: (08:00 - 10:00), Kendala 2: (09:30 - 11:00) -> Overlap!
    val kOverlap = com.example.ui.viewmodel.KendalaItemFormState(
        waktuMulai = "09:30",
        waktuSelesai = "11:00"
    )
    org.junit.Assert.assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kValid1, kOverlap))

    // Kendala 3: (10:00 - 12:00) -> Menyambung tanpa overlap
    val kContiguous = com.example.ui.viewmodel.KendalaItemFormState(
        waktuMulai = "10:00",
        waktuSelesai = "12:00"
    )
    org.junit.Assert.assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kValid1, kContiguous))

    // 3. Form state overlap detection
    val stateOverlap = com.example.ui.viewmodel.RealisasiFormUiState(
        daftarKendala = listOf(kValid1, kOverlap)
    )
    org.junit.Assert.assertTrue(stateOverlap.hasAnyKendalaOverlap)
    org.junit.Assert.assertTrue(stateOverlap.isKendalaOverlapping(0))
    org.junit.Assert.assertTrue(stateOverlap.isKendalaOverlapping(1))
    org.junit.Assert.assertTrue(stateOverlap.isKendalaOverlappingWithPrevious(1))
    org.junit.Assert.assertFalse(stateOverlap.isKendalaOverlappingWithPrevious(0))

    // 4. Batas Maksimal Shift: Akumulasi kendala tidak boleh > jam shift (8.0 jam = 480 menit)
    val kLong1 = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "07:00", waktuSelesai = "12:00") // 5 jam = 300 menit
    val kLong2 = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "12:00", waktuSelesai = "16:00") // 4 jam = 240 menit (Total 9 jam = 540 menit)
    val stateExceedShift = com.example.ui.viewmodel.RealisasiFormUiState(
        durasiJamKerja = 8.0,
        daftarKendala = listOf(kLong1, kLong2)
    )
    org.junit.Assert.assertEquals(540, stateExceedShift.totalHambatanMenit)
    org.junit.Assert.assertTrue(stateExceedShift.isTotalKendalaExceedingShift)
  }

  @Test
  fun `verify HM Awal manual correction logic and payload flags`() {
    val originalHm = 4120.0
    val correctedHm = 4125.0
    val hmAkhir = 4132.5

    // State saat dikoreksi manual
    val correctedState = com.example.ui.viewmodel.RealisasiFormUiState(
        hmAwalOriginal = originalHm,
        hmAwal = correctedHm,
        hmAwalInput = "4125.0",
        isHmAwalCorrected = true,
        alasanKoreksiHm = "Salah input shift sebelumnya oleh operator kemarin",
        hmAkhirInput = "4132.5"
    )

    org.junit.Assert.assertTrue(correctedState.isHmAwalCorrected)
    assertEquals(originalHm, correctedState.hmAwalOriginal, 0.01)
    assertEquals(correctedHm, correctedState.hmAwal, 0.01)
    org.junit.Assert.assertTrue(correctedState.alasanKoreksiHm.isNotBlank())

    // Validasi HM Akhir wajib lebih besar dari HM Awal (baik default maupun koreksi)
    val runningHm = hmAkhir - correctedHm
    assertEquals(7.5, runningHm, 0.01)
    org.junit.Assert.assertTrue(runningHm > 0.0)

    // Validasi Payload JSON API ke server
    val req = com.example.data.remote.PostActivityReportRequest.create(
        clientUuid = "TX-UNIT-TEST-001",
        reportDate = "10/10/2026",
        supervisorName = "Budi Santoso",
        unitCode = "EX-301",
        unitType = "EXCAVATOR",
        activityName = "Galian Tanah",
        sapCode = "SAP-01",
        unitOfMeasure = "m3",
        operatorName = "Ahmad Fauzi",
        workLocation = "001A",
        shift = "SIANG",
        workDurationHours = 8.0,
        hmAwal = correctedHm,
        hmAkhir = hmAkhir,
        hmDailyRunning = runningHm,
        workOutput = 400.0,
        notes = "Koreksi HM berjalan lancar",
        isHmAwalCorrected = true,
        alasanKoreksiHm = "Salah input shift sebelumnya oleh operator kemarin"
    )

    assertEquals(true, req.is_hm_awal_corrected)
    assertEquals("Salah input shift sebelumnya oleh operator kemarin", req.alasan_koreksi_hm)
    assertEquals(correctedHm, req.hm_awal ?: 0.0, 0.01)
    assertEquals(hmAkhir, req.hm_akhir ?: 0.0, 0.01)
  }
}
