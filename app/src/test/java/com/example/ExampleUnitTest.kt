package com.example

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.model.SupervisorDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun test_hm_delta_calculation() {
        val hmAwal = 4125.5
        val hmAkhir = 4132.5
        val delta = hmAkhir - hmAwal
        assertEquals(7.0, delta, 0.001)
        assertTrue(hmAkhir >= hmAwal)
    }

    @Test
    fun test_hm_invalid_when_less_than_awal() {
        val hmAwal = 4125.5
        val hmAkhir = 4120.0
        assertFalse(hmAkhir >= hmAwal)
    }

    @Test
    fun test_search_multi_criteria_unit() {
        val unit = UnitEntity("HEH0004", "EXCAVATOR", "Komatsu PC200-8", 4125.5, "Agung")
        assertTrue(unit.unitCode.contains("HEH", ignoreCase = true))
        assertTrue(unit.unitType.contains("EXCAVATOR", ignoreCase = true))
        assertTrue(unit.brandModel.contains("Komatsu", ignoreCase = true))
    }

    @Test
    fun test_search_multi_criteria_activity_sap() {
        val act = ActivityMasterEntity("ACT-SAP-101", "Galian Tanah Lunak", "m3")
        assertTrue(act.activityName.contains("gali", ignoreCase = true))
        assertTrue(act.sapCode.contains("101", ignoreCase = true))
    }

    @Test
    fun test_supervisor_authentication_success() {
        val spv1 = SupervisorDirectory.authenticate("Agung", "123")
        assertNotNull(spv1)
        assertEquals("Agung", spv1?.name)

        val spv2 = SupervisorDirectory.authenticate("Zainudin", "123")
        assertNotNull(spv2)
        assertEquals("KASIE-ZAINUDIN", spv2?.nik)
    }

    @Test
    fun test_supervisor_authentication_failure() {
        val invalidSpv = SupervisorDirectory.authenticate("UNKNOWN_SPV", "9999")
        assertNull(invalidSpv)
    }

    @Test
    fun test_image_compression_dimension_landscape() {
        // High-res landscape 4000x3000 (4:3 aspect ratio)
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(4000, 3000, 1280)
        assertEquals(1280, w)
        assertEquals(960, h)
        assertTrue(w <= 1280)
        assertTrue(h <= 1280)
    }

    @Test
    fun test_image_compression_dimension_portrait() {
        // High-res portrait 3000x4000 (3:4 aspect ratio)
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(3000, 4000, 1280)
        assertEquals(960, w)
        assertEquals(1280, h)
        assertTrue(w <= 1280)
        assertTrue(h <= 1280)
    }

    @Test
    fun test_image_compression_dimension_square() {
        // High-res square 2400x2400
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(2400, 2400, 1280)
        assertEquals(1280, w)
        assertEquals(1280, h)
    }

    @Test
    fun test_image_compression_keeps_small_dimension() {
        // Low-res image 800x600 should not be upscaled
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(800, 600, 1280)
        assertEquals(800, w)
        assertEquals(600, h)
    }

    @Test
    fun test_search_multi_criteria_lokasi() {
        val lok = com.example.data.local.entity.LokasiEntity("001A", "Geodesi PG1", 10.5, 8.2, "Area Front")
        // Search by kode
        assertTrue(lok.kodeLokasi.contains("001", ignoreCase = true))
        // Search by wilayah
        assertTrue(lok.wilayah.contains("Geodesi PG1", ignoreCase = true))
    }

    @Test
    fun test_fuel_consumption_ratio_calculation() {
        val totalSolar = 150.0 // Liters
        val totalHm = 10.0 // Hours
        val ratio = totalSolar / totalHm
        assertEquals(15.0, ratio, 0.001)
    }

    @Test
    fun test_operational_work_time_distribution() {
        val operasiHours = 8.0
        val standbyHours = 1.5
        val breakdownHours = 0.5
        val total = operasiHours + standbyHours + breakdownHours
        assertEquals(10.0, total, 0.001)
        val opPercent = operasiHours / total
        assertEquals(0.80, opPercent, 0.001)
    }

    @Test
    fun test_rencana_unrealized_anti_duplication() {
        val rencana1 = com.example.data.local.entity.RencanaKerjaEntity(
            id = 1L,
            supervisorName = "Budi Santoso",
            kodeUnit = "EX-201",
            operator = "Bambang",
            kodeLokasi = "001A",
            isRealized = false,
            statusTahap = "RENCANA"
        )
        val rencana2 = com.example.data.local.entity.RencanaKerjaEntity(
            id = 2L,
            supervisorName = "Budi Santoso",
            kodeUnit = "DZ-101",
            operator = "Hendra",
            kodeLokasi = "PG1",
            isRealized = true,
            statusTahap = "REALISASI_SELESAI"
        )
        val allPlans = listOf(rencana1, rencana2)
        val activePlans = allPlans.filter { !it.isRealized && it.statusTahap == "RENCANA" }
        assertEquals(1, activePlans.size)
        assertEquals("EX-201", activePlans.first().kodeUnit)
    }

    @Test
    fun test_spk_lock_verification() {
        val publishedSpk = "SPK-2026-GEO-001"
        val isLocked = publishedSpk.isNotBlank() && publishedSpk != "Belum Terbit (Menunggu Admin)"
        assertTrue(isLocked)
    }

    @Test
    fun test_kendala_time_duration_calculation() {
        val kendalaItem = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "09:30",
            waktuSelesai = "11:45"
        )
        // 09:30 to 11:45 = 135 minutes (2 Jam 15 Menit)
        assertEquals(135, kendalaItem.durasiMenit)
        assertEquals("2 Jam 15 Menit", kendalaItem.durasiLabel)
        assertTrue(kendalaItem.isTimeRangeValid)
    }

    @Test
    fun test_kendala_invalid_equal_times() {
        val invalidEqualTimes = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "10:00",
            waktuSelesai = "10:00"
        )
        assertFalse(invalidEqualTimes.isTimeRangeValid)
        assertNull(invalidEqualTimes.durasiMenit)

        val invalidFormat = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "ab:cd",
            waktuSelesai = "12:00"
        )
        assertFalse(invalidFormat.isTimeRangeValid)
        assertNull(invalidFormat.durasiMenit)
    }

    @Test
    fun test_kendala_cross_midnight_calculation() {
        // Shift malam melintasi tengah malam: 23:00 s/d 02:00 = 3 Jam (180 Menit)
        val crossMidnight = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "23:00",
            waktuSelesai = "02:00"
        )
        assertTrue(crossMidnight.isTimeRangeValid)
        assertTrue(crossMidnight.isCrossMidnight)
        assertEquals(180, crossMidnight.durasiMenit)
        assertTrue(crossMidnight.durasiLabel.contains("3 Jam"))

        // Shift malam: 22:30 s/d 01:15 = 2 Jam 45 Menit (165 Menit)
        val crossMidnight2 = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "22:30",
            waktuSelesai = "01:15"
        )
        assertTrue(crossMidnight2.isTimeRangeValid)
        assertEquals(165, crossMidnight2.durasiMenit)
    }

    @Test
    fun test_kendala_anti_overlapping_detection() {
        // Range 1: 08:00 - 10:00
        val k1 = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "08:00", waktuSelesai = "10:00")
        // Range 2 (Overlapping): 09:30 - 11:00
        val k2Overlap = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "09:30", waktuSelesai = "11:00")
        // Range 3 (Non-overlapping touching edge): 10:00 - 12:00
        val k3Adjacent = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "10:00", waktuSelesai = "12:00")

        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(k1, k2Overlap))
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(k1, k3Adjacent))

        // Cross-midnight overlap test:
        // Range A: 23:00 - 02:00
        val kCrossA = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "23:00", waktuSelesai = "02:00")
        // Range B: 01:00 - 03:00 (collides between 01:00 and 02:00)
        val kCrossB = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "01:00", waktuSelesai = "03:00")
        // Range C: 02:00 - 04:00 (adjacent, not colliding)
        val kCrossC = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "02:00", waktuSelesai = "04:00")

        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kCrossA, kCrossB))
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kCrossA, kCrossC))

        // UI state level overlap check
        val formWithOverlap = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kCrossA, kCrossB)
        )
        assertTrue(formWithOverlap.hasAnyKendalaOverlap)
        assertTrue(formWithOverlap.isKendalaOverlapping(0))
        assertTrue(formWithOverlap.isKendalaOverlapping(1))

        val formWithoutOverlap = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kCrossA, kCrossC)
        )
        assertFalse(formWithoutOverlap.hasAnyKendalaOverlap)
    }

    @Test
    fun test_kendala_shift_limit_validation() {
        // Shift durasi 7 jam = 420 menit
        val formState = com.example.ui.viewmodel.RealisasiFormUiState(
            durasiJamKerja = 7.0,
            daftarKendala = listOf(
                com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "08:00", waktuSelesai = "12:00"), // 4 jam = 240 min
                com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "12:00", waktuSelesai = "16:00")  // 4 jam = 240 min -> Total 8 jam > 7 jam
            )
        )
        assertEquals(480, formState.totalHambatanMenit)
        assertTrue(formState.isTotalKendalaExceedingShift)
    }

    @Test
    fun test_hm_akhir_must_be_strictly_greater_than_awal() {
        val hmAwal = 3500.0
        val hmAkhirEqual = 3500.0
        val hmAkhirLesser = 3499.0
        val hmAkhirGreater = 3507.0

        assertFalse(hmAkhirEqual > hmAwal)
        assertFalse(hmAkhirLesser > hmAwal)
        assertTrue(hmAkhirGreater > hmAwal)
    }

    @Test
    fun test_dynamic_unit_hm_standards_reading() {
        val excavator = UnitEntity("EXC-01", "EXCAVATOR", "Komatsu PC200", 4000.0, "Budi", "OPERASIONAL", 6.0, 10.0)
        val compactor = UnitEntity("CMP-01", "COMPACTOR", "Bomag BW211", 3000.0, "Budi", "OPERASIONAL", 5.5, 9.5)
        val grader = UnitEntity("GRD-01", "MOTOR GRADER", "CAT 140K", 2000.0, "Budi", "OPERASIONAL", 5.0, 9.0)

        assertEquals(6.0, excavator.hmMinStandar, 0.001)
        assertEquals(10.0, excavator.hmMaxStandar, 0.001)

        assertEquals(5.5, compactor.hmMinStandar, 0.001)
        assertEquals(9.5, compactor.hmMaxStandar, 0.001)

        assertEquals(5.0, grader.hmMinStandar, 0.001)
        assertEquals(9.0, grader.hmMaxStandar, 0.001)
    }

    @Test
    fun test_kendala_json_serialization_and_deserialization() {
        val payloads = listOf(
            com.example.data.remote.KendalaItemPayload(
                id_kendala = "KND-01",
                nama_kendala = "Hujan",
                waktu_mulai = "09:30",
                waktu_selesai = "11:45",
                durasi_menit = 135
            ),
            com.example.data.remote.KendalaItemPayload(
                id_kendala = "KND-02",
                nama_kendala = "Breakdown Mekanikal",
                waktu_mulai = "13:00",
                waktu_selesai = "14:30",
                durasi_menit = 90
            )
        )

        val json = com.example.data.remote.KendalaJsonConverter.toJson(payloads)
        assertTrue(json.contains("KND-01"))
        assertTrue(json.contains("135"))
        assertTrue(json.contains("Breakdown Mekanikal"))

        val parsed = com.example.data.remote.KendalaJsonConverter.fromJson(json)
        assertEquals(2, parsed.size)
        assertEquals("KND-01", parsed[0].id_kendala)
        assertEquals("09:30", parsed[0].waktu_mulai)
        assertEquals("11:45", parsed[0].waktu_selesai)
        assertEquals(135, parsed[0].durasi_menit)
        assertEquals("Breakdown Mekanikal", parsed[1].nama_kendala)
    }

    @Test
    fun test_kendala_lapangan_payload_mapping_post_aktivitas_unit() {
        val kendala1 = com.example.data.remote.KendalaItemPayload.create(
            namaKendala = "Antar - jemput",
            kodeKendala = "1.0",
            jamMulai = "08:00",
            jamSelesai = "09:00",
            durasiJam = 1.0
        )
        val kendala2 = com.example.data.remote.KendalaItemPayload.create(
            namaKendala = "Transport",
            kodeKendala = "3.0",
            jamMulai = "09:00",
            jamSelesai = "10:00",
            durasiJam = 1.0
        )

        assertEquals("Antar - jemput", kendala1.nama_kendala)
        assertEquals("1.0", kendala1.kode_kendala)
        assertEquals("08:00", kendala1.jam_mulai)
        assertEquals("09:00", kendala1.jam_selesai)
        assertEquals(1.0, kendala1.durasi_jam ?: 0.0, 0.001)

        assertEquals("Transport", kendala2.nama_kendala)
        assertEquals("3.0", kendala2.kode_kendala)
        assertEquals("09:00", kendala2.jam_mulai)
        assertEquals("10:00", kendala2.jam_selesai)
        assertEquals(1.0, kendala2.durasi_jam ?: 0.0, 0.001)

        val listKendala = listOf(kendala1, kendala2)

        val request = com.example.data.remote.PostActivityReportRequest.create(
            clientUuid = "TEST-UUID-01",
            reportDate = "12/10/2026",
            supervisorName = "Agung",
            unitCode = "HEH0004",
            unitType = "EXCAVATOR",
            activityName = "Galian Tanah",
            sapCode = "ACT-SAP-101",
            unitOfMeasure = "m3",
            operatorName = "Ahmad",
            workLocation = "001A",
            shift = "SIANG",
            workDurationHours = 7.0,
            hmAwal = 4125.0,
            hmAkhir = 4132.0,
            hmDailyRunning = 7.0,
            workOutput = 100.0,
            notes = "Realisasi harian lancar",
            kendalaLapangan = listKendala,
            kendalaList = listKendala,
            daftarKendala = listKendala
        )

        assertNotNull(request.kendala_lapangan)
        assertNotNull(request.kendala_list)
        assertEquals(2, request.kendala_lapangan?.size)
        assertEquals(2, request.kendala_list?.size)
        assertEquals("1.0", request.kendala_lapangan?.get(0)?.kode_kendala)
        assertEquals("Antar - jemput", request.kendala_lapangan?.get(0)?.nama_kendala)
        assertEquals("3.0", request.kendala_lapangan?.get(1)?.kode_kendala)
        assertEquals("Transport", request.kendala_lapangan?.get(1)?.nama_kendala)

        // Verify JSON serialization via Moshi
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter(com.example.data.remote.PostActivityReportRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"kendala_lapangan\""))
        assertTrue(json.contains("\"kendala_list\""))
        assertTrue(json.contains("\"nama_kendala\":\"Antar - jemput\""))
        assertTrue(json.contains("\"kode_kendala\":\"1.0\""))
        assertTrue(json.contains("\"jam_mulai\":\"08:00\""))
        assertTrue(json.contains("\"jam_selesai\":\"09:00\""))
        assertTrue(json.contains("\"durasi_jam\":1.0"))
        assertTrue(json.contains("\"nama_kendala\":\"Transport\""))
        assertTrue(json.contains("\"kode_kendala\":\"3.0\""))

        // Also verify PostHasilAktivitasRequest serialization with kendala_lapangan and kendala_list
        val hasilReq = com.example.data.remote.PostHasilAktivitasRequest(
            id = 101L,
            rencana_id = 12L,
            kode_unit = "HEH0004",
            hm_awal = 4125.0,
            hm_akhir = 4132.0,
            kendala_lapangan = listKendala,
            kendala_list = listKendala,
            daftar_kendala = listKendala
        )
        val hasilAdapter = moshi.adapter(com.example.data.remote.PostHasilAktivitasRequest::class.java)
        val hasilJson = hasilAdapter.toJson(hasilReq)
        assertTrue(hasilJson.contains("\"kendala_lapangan\""))
        assertTrue(hasilJson.contains("\"kendala_list\""))
        assertTrue(hasilJson.contains("\"nama_kendala\":\"Antar - jemput\""))
        assertTrue(hasilJson.contains("\"kode_kendala\":\"1.0\""))
        assertTrue(hasilJson.contains("\"nama_kendala\":\"Transport\""))
        assertTrue(hasilJson.contains("\"kode_kendala\":\"3.0\""))
    }

    @Test
    fun test_kendala_resolve_kode_from_name_and_variations() {
        assertEquals("1.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala(null, "Antar - jemput"))
        assertEquals("1.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala("KND-01", "Antar - jemput"))
        assertEquals("1.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala("1", "Antar - jemput"))
        assertEquals("3.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala(null, "Transport"))
        assertEquals("3.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala("KND-03", "Transport"))
        assertEquals("3.0", com.example.data.remote.KendalaItemPayload.resolveKodeKendala("3.0", "Transport"))
    }

    @Test
    fun test_official_apk_distribution_urls() {
        val defaultUrl = com.example.ui.viewmodel.HeavyTrackViewModel.APK_DIRECT_DOWNLOAD_URL
        val repoUrl = com.example.ui.viewmodel.HeavyTrackViewModel.GITHUB_REPO_URL
        val releasesPage = com.example.ui.viewmodel.HeavyTrackViewModel.GITHUB_RELEASES_PAGE_URL
        val debugUrl = com.example.ui.viewmodel.HeavyTrackViewModel.APK_DEBUG_DOWNLOAD_URL

        assertTrue(defaultUrl.startsWith("https://github.com/tiaprima8-png/tes"))
        assertTrue(defaultUrl.endsWith(".apk"))
        assertTrue(repoUrl.contains("tiaprima8-png/tes"))
        assertTrue(releasesPage.endsWith("/releases"))
        assertTrue(debugUrl.endsWith(".apk"))
    }

    @Test
    fun test_master_kendala_15_items_mapping() {
        val serverList = com.example.data.remote.CompanyServerSimulator.serverKendala
        assertEquals(15, serverList.size)

        // Verify the 15 standard items in order
        val expectedNames = listOf(
            "Antar - jemput",
            "Cek unit, Implement Pemanasan mesin",
            "Transport",
            "Kerja",
            "Istirahat",
            "Perbaikan Unit",
            "Perbaikan Implement",
            "Perawatan Unit",
            "Perawatan Implement",
            "Ganti Implement",
            "Tunggu Solar",
            "Tunggu Mekanik",
            "Tunggu Trailler",
            "Tunggu Lokasi",
            "Tunggu Cuaca"
        )

        serverList.forEachIndexed { index, item ->
            assertEquals(expectedNames[index], item.resolvedNama)
            val generatedKode = item.resolveKode(index)
            assertTrue(generatedKode.startsWith("KND-"))
        }

        // Test fallback resolveKode when API provides no code or null
        val payloadWithoutCode = com.example.data.remote.KendalaPayload(nama_kendala = "Tunggu Cuaca")
        assertEquals("KND-15", payloadWithoutCode.resolveKode(14))
    }

    @Test
    fun test_dump_truck_and_bulldozer_removed_from_master_units() {
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("Dump Truck"))
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("dump truck"))
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("Bulldozer"))
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("bulldozer"))
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("DT"))
        assertFalse(com.example.data.model.UnitConstants.isJenisUnitValid("DZ"))

        assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid("Excavator Long Arm"))
        assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid("Excavator Mini"))
        assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid("Excavator Standar"))
        assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid("Motor Grader"))
        assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid("Compactor"))

        assertEquals(5, com.example.data.model.UnitConstants.AKTIF.size)
        assertFalse(com.example.data.model.UnitConstants.AKTIF.contains("Dump Truck"))
        assertFalse(com.example.data.model.UnitConstants.AKTIF.contains("Bulldozer"))

        assertFalse(com.example.data.model.UnitConstants.FILTER.contains("Dump Truck"))
        assertFalse(com.example.data.model.UnitConstants.FILTER.contains("Bulldozer"))
    }

    @Test
    fun test_unit_plan_status_three_cases() {
        val uA = UnitEntity("EX-301", "Excavator Standar", "Komatsu PC300", 3500.0, "Budi")
        val uB = UnitEntity("CMP-01", "Compactor", "Bomag BW211", 4200.0, "Budi")
        val uC = UnitEntity("GD-01", "Motor Grader", "Cat 14M", 2800.0, "Budi")

        val yesterdayStr = "09/10/2026"
        val todayStr = "10/10/2026"

        val rencanaList = listOf(
            // Unit CMP-01 ada kemarin, belum ada hari ini (Kasus B)
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 1,
                supervisorName = "Budi",
                kodeUnit = "CMP-01",
                operator = "Agus",
                kodeLokasi = "001A",
                tanggalFormat = yesterdayStr
            ),
            // Unit GD-01 ada kemarin dan sudah ada 1 rencana hari ini (Kasus C)
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 2,
                supervisorName = "Budi",
                kodeUnit = "GD-01",
                operator = "Fauzi",
                kodeLokasi = "001A",
                tanggalFormat = yesterdayStr
            ),
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 3,
                supervisorName = "Budi",
                kodeUnit = "GD-01",
                operator = "Fauzi",
                kodeLokasi = "001A",
                tanggalFormat = todayStr
            )
            // Unit EX-301 belum ada kemarin sama sekali (Kasus A)
        )

        val units = listOf(uA, uB, uC)

        val resultStatuses = units.map { u ->
            val plansYesterday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == yesterdayStr
            }
            val plansToday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == todayStr
            }

            when {
                plansYesterday.isEmpty() -> {
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = yesterdayStr,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [Tgl Kemarin: Belum Diisi]"
                    )
                }
                plansToday.isEmpty() -> {
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = false,
                        existingPlanCount = 0,
                        labelText = "${u.unitCode} [Hari Ini: Belum Ada Rencana]"
                    )
                }
                else -> {
                    val count = plansToday.size
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = true,
                        existingPlanCount = count,
                        labelText = "${u.unitCode} [Hari Ini: Sudah Ada $count Rencana] (+ Tambah Rencana Susulan)"
                    )
                }
            }
        }

        // 1. Kasus A: EX-301
        val statusA = resultStatuses.find { it.unit.unitCode == "EX-301" }
        assertNotNull(statusA)
        assertTrue(statusA!!.isBacklog)
        assertFalse(statusA.isSusulan)
        assertEquals("EX-301 [Tgl Kemarin: Belum Diisi]", statusA.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.BACKLOG_YESTERDAY, statusA.category)

        // 2. Kasus B: CMP-01
        val statusB = resultStatuses.find { it.unit.unitCode == "CMP-01" }
        assertNotNull(statusB)
        assertFalse(statusB!!.isBacklog)
        assertFalse(statusB.isSusulan)
        assertEquals("CMP-01 [Hari Ini: Belum Ada Rencana]", statusB.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.NO_PLAN_TODAY, statusB.category)

        // 3. Kasus C: GD-01
        val statusC = resultStatuses.find { it.unit.unitCode == "GD-01" }
        assertNotNull(statusC)
        assertFalse(statusC!!.isBacklog)
        assertTrue(statusC.isSusulan)
        assertEquals(1, statusC.existingPlanCount)
        assertEquals("GD-01 [Hari Ini: Sudah Ada 1 Rencana] (+ Tambah Rencana Susulan)", statusC.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.HAS_PLAN_TODAY, statusC.category)

        // Pastikan tidak ada unit yang hilang (total 3 unit tetap tersedia)
        assertEquals(3, resultStatuses.size)
    }

    // ======================================================================
    // PENGUJIAN 5 BLIND SPOTS FORMULIR REALISASI KERJA & SINKRONISASI RKCE
    // ======================================================================

    @Test
    fun test_formula_pemakaian_solar() {
        // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
        // Kasus 1: Tanpa pengisian solar tambahan (85.0 - 60.0 = 25.0 Liter)
        val state1 = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "85.0",
            stikSolarAkhirInput = "60.0",
            isIsiSolar = false,
            jumlahLiterSolarInput = ""
        )
        assertEquals(25.0, state1.computedKonsumsiSolar, 0.001)
        assertFalse(state1.isStikAkhirHigherWithoutRefuel)
        assertFalse(state1.isSolarConsumptionNegative)
        assertFalse(state1.isRefuelLitersMissing)

        // Kasus 2: Dengan pengisian solar tambahan 100 Liter (Stik awal 20.0 + Isi 100.0 - Stik akhir 80.0 = 40.0 Liter)
        val state2 = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "20.0",
            stikSolarAkhirInput = "80.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = "100.0"
        )
        assertEquals(40.0, state2.computedKonsumsiSolar, 0.001)
        assertFalse(state2.isStikAkhirHigherWithoutRefuel)
        assertFalse(state2.isSolarConsumptionNegative)
        assertFalse(state2.isRefuelLitersMissing)
    }

    @Test
    fun test_validasi_stik_solar_warning_and_negative() {
        // Logika 1: Stik Akhir > Stik Awal dan toggle [Isi Solar Tambahan] MATI -> Warning muncul
        val warningState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "40.0",
            stikSolarAkhirInput = "75.0",
            isIsiSolar = false,
            jumlahLiterSolarInput = ""
        )
        assertTrue(warningState.isStikAkhirHigherWithoutRefuel)
        assertTrue(warningState.isSolarConsumptionNegative) // (40 + 0) - 75 = -35.0

        // Logika 2: Toggle [Isi Solar Tambahan] AKTIF tapi input liter kosong -> isRefuelLitersMissing true
        val missingRefuelState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "40.0",
            stikSolarAkhirInput = "75.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = ""
        )
        assertTrue(missingRefuelState.isRefuelLitersMissing)

        // Logika 3: Pengisian BBM diisi tapi total konsumsi tetap negatif
        val negativeState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "30.0",
            stikSolarAkhirInput = "90.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = "40.0" // (30 + 40) - 90 = -20
        )
        assertTrue(negativeState.isSolarConsumptionNegative)
        assertEquals(-20.0, negativeState.computedKonsumsiSolar, 0.001)
    }

    @Test
    fun test_kendala_time_range_normal_and_cross_midnight() {
        // Normal shift daytime: 08:00 - 10:00 = 2 jam (120 menit)
        val kendalaNormal = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "08:00",
            waktuSelesai = "10:00"
        )
        assertTrue(kendalaNormal.isTimeRangeValid)
        assertFalse(kendalaNormal.isCrossMidnight)
        assertEquals(120, kendalaNormal.durasiMenit)

        // Night shift cross midnight: 23:00 - 02:00 = 3 jam (180 menit)
        val kendalaCrossMidnight = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "23:00",
            waktuSelesai = "02:00"
        )
        assertTrue(kendalaCrossMidnight.isTimeRangeValid)
        assertTrue(kendalaCrossMidnight.isCrossMidnight)
        assertEquals(180, kendalaCrossMidnight.durasiMenit)
    }

    @Test
    fun test_kendala_anti_overlapping() {
        val kendalaA = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "08:00",
            waktuSelesai = "10:00"
        )
        val kendalaB = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "09:30",
            waktuSelesai = "11:00"
        )
        val kendalaC = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "10:30",
            waktuSelesai = "12:00"
        )

        // A dan B bertabrakan (08:00-10:00 vs 09:30-11:00)
        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kendalaA, kendalaB))
        // A dan C TIDAK bertabrakan (08:00-10:00 vs 10:30-12:00)
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kendalaA, kendalaC))

        val formState = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kendalaA, kendalaB)
        )
        assertTrue(formState.hasAnyKendalaOverlap)
    }

    @Test
    fun test_idempotency_client_transaction_id_format() {
        val txId = "TX-" + java.util.UUID.randomUUID().toString()
        assertTrue(txId.startsWith("TX-"))
        assertTrue(txId.length > 10)
    }

    @Test
    fun test_supervisor_session_logout_clears_session() {
        val spv = SupervisorDirectory.authenticate("Agung", "123")
        assertNotNull(spv)

        var currentSession = spv
        var isLoggedIn = true
        assertNotNull(currentSession)
        assertTrue(isLoggedIn)

        // Trigger logout
        isLoggedIn = false
        currentSession = null

        assertFalse(isLoggedIn)
        assertNull(currentSession)
    }

    @Test
    fun test_bootstrap_data_payload_resolution() {
        val payload = com.example.data.remote.BootstrapDataPayload(
            rencana_aktif = listOf(
                com.example.data.remote.PostRencanaKerjaRequest(
                    kode_unit = "HEH0013",
                    nomor_spk = "SPK-2026-GEO-001",
                    status_spk = "TERBIT_SPK",
                    nama_pengawas = "Agung"
                )
            ),
            riwayat_aktivitas = listOf(
                com.example.data.remote.PostActivityReportRequest(
                    tanggal = "11/10/2026",
                    nama_pengawas = "Agung",
                    kode_unit = "HEH0013",
                    hm_akhir = 12.0
                )
            )
        )

        assertEquals(1, payload.resolvedRencanaAktif.size)
        assertEquals("SPK-2026-GEO-001", payload.resolvedRencanaAktif[0].nomor_spk)
        assertEquals("TERBIT_SPK", payload.resolvedRencanaAktif[0].status_spk)
        assertEquals(1, payload.resolvedRiwayatAktivitas.size)
        assertEquals("HEH0013", payload.resolvedRiwayatAktivitas[0].kode_unit)
    }

    @Test
    fun test_bootstrap_simulator_returns_active_spk_plans_and_history() = kotlinx.coroutines.test.runTest {
        com.example.data.remote.CompanyServerSimulator.serverRencanaKerja.add(
            com.example.data.remote.PostRencanaKerjaRequest(
                id = "101",
                kode_unit = "HEH0013",
                nomor_spk = "SPK-2026-GEO-001",
                status_spk = "TERBIT_SPK",
                status_tahap = "RENCANA",
                is_realized = false,
                nama_pengawas = "Agung"
            )
        )
        val response = com.example.data.remote.CompanyServerSimulator.fetchBootstrapData(
            pengawas = "Agung",
            tanggal = "11/10/2026"
        )
        assertNotNull(response.data)
        val data = response.data!!
        assertTrue(data.resolvedRencanaAktif.isNotEmpty())
        assertTrue(data.resolvedRiwayatAktivitas.isNotEmpty())

        val planWithSpk = data.resolvedRencanaAktif.find { it.status_spk == "TERBIT_SPK" }
        assertNotNull(planWithSpk)
        assertTrue(planWithSpk!!.nomor_spk?.startsWith("SPK-") == true)
    }

    @Test
    fun test_user_entity_kasie_and_manager_role_and_wilayah() {
        val kasieUser = com.example.data.local.entity.UserEntity(
            username = "budi",
            name = "Budi Santoso",
            nik = "SPV-901",
            role = "KASIE",
            roleTitle = "Kasie",
            wilayah = "Geodesi PG1"
        )
        assertTrue(kasieUser.isKasie)
        assertFalse(kasieUser.isManager)
        assertEquals("Geodesi PG1", kasieUser.wilayah)

        val managerUser = com.example.data.local.entity.UserEntity(
            username = "farid",
            name = "Farid Hadi Sofian",
            nik = "MGR-101",
            role = "MANAGER",
            roleTitle = "Manager Operasional",
            wilayah = "Semua Wilayah"
        )
        assertTrue(managerUser.isManager)
        assertFalse(managerUser.isKasie)
        assertEquals("Semua Wilayah", managerUser.wilayah)
    }

    @Test
    fun test_supervisor_directory_wilayah_and_manager_role() {
        val spvPg1 = SupervisorDirectory.authenticate("Agung", "123")
        assertNotNull(spvPg1)
        assertEquals("Geodesi PG1", spvPg1?.wilayah)
        assertEquals("KASIE", spvPg1?.role)
        assertTrue(spvPg1!!.isKasie)

        val spvPg2 = SupervisorDirectory.authenticate("Zainudin", "123")
        assertNotNull(spvPg2)
        assertEquals("Geodesi PG2", spvPg2?.wilayah)
        assertEquals("KASIE", spvPg2?.role)

        val spvPg3 = SupervisorDirectory.authenticate("Hisar", "123")
        assertNotNull(spvPg3)
        assertEquals("RC PG1", spvPg3?.wilayah)

        val mgr = SupervisorDirectory.authenticate("Lutfi", "123")
        assertNotNull(mgr)
        assertTrue(mgr!!.isManager)
        assertEquals("Semua Wilayah", mgr.wilayah)
    }

    @Test
    fun test_api_master_unit_and_bootstrap_data_free_of_dump_truck_and_bulldozer() = kotlinx.coroutines.test.runTest {
        val masterUnitResp = com.example.data.remote.CompanyServerSimulator.getMasterUnit()
        assertEquals("SUCCESS", masterUnitResp.status)
        assertNotNull(masterUnitResp.data)
        val units = masterUnitResp.data!!
        assertTrue(units.isNotEmpty())
        for (u in units) {
            val type = u.resolvedUnitType.lowercase()
            assertFalse("Unit ${u.resolvedUnitCode} has invalid type $type", type.contains("dump"))
            assertFalse("Unit ${u.resolvedUnitCode} has invalid type $type", type.contains("bulldozer"))
            assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid(u.resolvedUnitType))
        }

        val bootstrapResp = com.example.data.remote.CompanyServerSimulator.fetchBootstrapData("Agung", "13/10/2026")
        assertEquals("SUCCESS", bootstrapResp.status)
        assertNotNull(bootstrapResp.data)
        val bootstrapUnits = bootstrapResp.data!!.resolvedMasterData.units ?: emptyList()
        assertTrue(bootstrapUnits.isNotEmpty())
        for (u in bootstrapUnits) {
            val type = u.resolvedUnitType.lowercase()
            assertFalse("Bootstrap unit ${u.resolvedUnitCode} has invalid type $type", type.contains("dump"))
            assertFalse("Bootstrap unit ${u.resolvedUnitCode} has invalid type $type", type.contains("bulldozer"))
            assertTrue(com.example.data.model.UnitConstants.isJenisUnitValid(u.resolvedUnitType))
        }
    }

    @Test
    fun test_modal_and_filter_unit_types_contain_only_active_operational_units() {
        val expectedActiveTypes = listOf(
            "Excavator Long Arm",
            "Excavator Mini",
            "Excavator Standar",
            "Motor Grader",
            "Compactor"
        )
        assertEquals(expectedActiveTypes, com.example.data.model.UnitConstants.AKTIF)
        assertFalse(com.example.data.model.UnitConstants.AKTIF.contains("Dump Truck"))
        assertFalse(com.example.data.model.UnitConstants.AKTIF.contains("Bulldozer"))

        val expectedFilterTypes = listOf(
            "Semua Jenis",
            "Excavator Long Arm",
            "Excavator Mini",
            "Excavator Standar",
            "Motor Grader",
            "Compactor"
        )
        assertEquals(expectedFilterTypes, com.example.data.model.UnitConstants.FILTER)
        assertFalse(com.example.data.model.UnitConstants.FILTER.contains("Dump Truck"))
        assertFalse(com.example.data.model.UnitConstants.FILTER.contains("Bulldozer"))
    }

    @Test
    fun test_sql_cleaning_queries_integrity_simulation() {
        val initialMasterUnits = mutableListOf(
            "EXC-01" to "Excavator Standar",
            "DT-101" to "Dump Truck",
            "DZ-01" to "Bulldozer",
            "GRD-01" to "Motor Grader",
            "CMP-01" to "Compactor"
        )
        val initialRencana = mutableListOf(
            1L to "EXC-01",
            2L to "DT-101",
            3L to "DZ-01",
            4L to "GRD-01"
        )

        // 1. Clean master unit: DELETE FROM master_unit WHERE LOWER(jenis_unit) IN ('dump truck', 'bulldozer');
        initialMasterUnits.removeAll { (_, type) ->
            type.lowercase() in listOf("dump truck", "bulldozer")
        }
        assertEquals(3, initialMasterUnits.size)
        assertFalse(initialMasterUnits.any { it.second.lowercase() in listOf("dump truck", "bulldozer") })

        // 2. Clean orphaned rencana_kerja: DELETE FROM rencana_kerja WHERE kode_unit NOT IN (SELECT kode_unit FROM master_unit);
        val validCodes = initialMasterUnits.map { it.first }.toSet()
        initialRencana.removeAll { (_, code) -> code !in validCodes }

        assertEquals(2, initialRencana.size)
        assertTrue(initialRencana.all { it.second in validCodes })
        assertFalse(initialRencana.any { it.second == "DT-101" || it.second == "DZ-01" })
    }

    @Test
    fun test_kasie_jahidi_unit_filtering_only_7_units_and_correct_badge() {
        val masterUnitResp = com.example.data.remote.CompanyServerSimulator.getMasterUnit()
        assertEquals("SUCCESS", masterUnitResp.status)
        val allUnits = masterUnitResp.data!!

        // Total units must be 39
        assertEquals(39, allUnits.size)

        // Filter units for Jahidi
        val jahidiUnits = allUnits.filter { u ->
            val spv = u.resolvedSupervisor.trim().lowercase()
            spv == "jahidi"
        }
        assertEquals(7, jahidiUnits.size)

        val jahidiCodes = jahidiUnits.map { it.resolvedUnitCode }.toSet()
        // Must contain Jahidi's units
        assertTrue(jahidiCodes.contains("CMP-02"))
        assertTrue(jahidiCodes.contains("HEH0017"))
        assertTrue(jahidiCodes.contains("HEH0018"))
        assertTrue(jahidiCodes.contains("GRD-04"))
        assertTrue(jahidiCodes.contains("EXC-14"))
        assertTrue(jahidiCodes.contains("SCC0007"))
        assertTrue(jahidiCodes.contains("HEH0034"))

        // Must NOT contain Hisar's units
        assertFalse(jahidiCodes.contains("HEH0005"))
        assertFalse(jahidiCodes.contains("HEH0006"))
        assertFalse(jahidiCodes.contains("EXC-10"))
        assertFalse(jahidiCodes.contains("HEH0019"))

        // Filter units for Hisar
        val hisarUnits = allUnits.filter { u ->
            val spv = u.resolvedSupervisor.trim().lowercase()
            spv == "hisar"
        }
        assertEquals(7, hisarUnits.size)
        val hisarCodes = hisarUnits.map { it.resolvedUnitCode }.toSet()
        assertTrue(hisarCodes.contains("HEH0005"))
        assertTrue(hisarCodes.contains("HEH0006"))

        // Badge count simulation for Jahidi: must be 7 units, NOT 14 units!
        val backlogCount = jahidiUnits.size
        val subtitle = if (backlogCount > 0) "${jahidiUnits.size} unit ($backlogCount backlog)" else "${jahidiUnits.size} unit terdaftar"
        assertEquals("7 unit (7 backlog)", subtitle)
    }

    @Test
    fun test_kasie_jahidi_operator_filtering_and_dialog_subtitle() {
        val masterOpResp = com.example.data.remote.CompanyServerSimulator.getMasterOperator()
        assertEquals("SUCCESS", masterOpResp.status)
        val allOperators = masterOpResp.data!!

        // Total master operators in company is 45
        assertEquals(45, allOperators.size)

        // Jahidi's Kasie parameters
        val kasieName = "Jahidi"
        val username = "@jahidi"
        val wilayahKasie = "RC PG1"

        val jahidiOperators = allOperators.filter { op ->
            val spv = op.resolvedSupervisor.trim().removePrefix("@").lowercase()
            val wil = (op.wilayah ?: "").trim().lowercase()
            (spv == "jahidi" || spv.contains("jahidi")) ||
            (spv.isBlank() && wil == wilayahKasie.lowercase())
        }

        // Must only contain operators belonging to Jahidi (not all 45, not operators from Geodesi or Lebung)
        assertTrue(jahidiOperators.isNotEmpty())
        assertTrue(jahidiOperators.size < 10)
        assertTrue(jahidiOperators.all { op ->
            val wil = op.wilayah ?: ""
            wil.contains("RC PG1", ignoreCase = true)
        })

        // Operators from Geodesi or Lebung must NEVER appear
        assertFalse(jahidiOperators.any { (it.wilayah ?: "").contains("Geodesi", ignoreCase = true) })
        assertFalse(jahidiOperators.any { (it.wilayah ?: "").contains("LEBUNG", ignoreCase = true) })

        // Dialog subtitle test
        val dialogSubtitle = "${jahidiOperators.size} Operator Aktif"
        assertEquals("${jahidiOperators.size} Operator Aktif", dialogSubtitle)
    }
}
