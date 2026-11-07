package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.dao.ActivityReportDao
import com.example.data.local.dao.HasilInputAktivitasDao
import com.example.data.local.dao.RencanaKerjaDao
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.remote.ApiClientManager
import com.example.data.remote.PostActivityReportRequest
import com.example.data.remote.RealisasiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RealisasiRepository(
    private val apiClientManager: ApiClientManager,
    private val realisasiDao: HasilInputAktivitasDao,
    private val rencanaKerjaDao: RencanaKerjaDao,
    private val reportDao: ActivityReportDao? = null
) {
    private val apiService get() = apiClientManager.getRawApiService()

    /**
     * Eksekusi approval langsung ke server Web Admin (POST api/realisasi/approve),
     * lalu kunci di database Room lokal (realisasiDao & rencanaKerjaDao).
     */
    suspend fun approveLaporan(nomorSpk: String, kasieNama: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Panggil server Web Admin terlebih dahulu
            val payload = mapOf(
                "nomor_spk" to nomorSpk,
                "kasie_nama" to kasieNama
            )
            val response = try {
                apiService.approveRealisasi(payload)
            } catch (netErr: Exception) {
                if (apiClientManager.useSimulationFallback || com.example.data.remote.CompanyServerSimulator.isSimulatedOffline) {
                    com.example.data.remote.CompanyServerSimulator.approveRealisasi(nomorSpk, kasieNama)
                    realisasiDao.updateApproval(
                        nomorSpk = nomorSpk,
                        status = "Disetujui oleh $kasieNama",
                        isLocked = true
                    )
                    reportDao?.updateApproval(
                        nomorSpk = nomorSpk,
                        status = "Disetujui oleh $kasieNama",
                        isLocked = true
                    )
                    rencanaKerjaDao.updateStatus(nomorSpk, "Approved")
                    return@withContext Result.success(Unit)
                }
                throw netErr
            }

            if (response.isSuccessful && response.body()?.success == true) {
                // 2. Jika server sukses, kunci di database Room lokal
                realisasiDao.updateApproval(
                    nomorSpk = nomorSpk, 
                    status = "Disetujui oleh $kasieNama", 
                    isLocked = true
                )
                reportDao?.updateApproval(
                    nomorSpk = nomorSpk,
                    status = "Disetujui oleh $kasieNama",
                    isLocked = true
                )
                rencanaKerjaDao.updateStatus(nomorSpk, "Approved")
                com.example.data.remote.CompanyServerSimulator.approveRealisasi(nomorSpk, kasieNama)
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e("APPROVE", "Server menolak approval: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("APPROVE", "Gagal koneksi ke server: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Eksekusi pengiriman data realisasi langsung ke server Web Admin (POST HTTP),
     * menyimpan ke Room lokal, serta menandai rencana kerja selesai.
     */
    suspend fun submitRealisasiLangsung(
        request: RealisasiRequest,
        context: Context
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val spk = request.nomor_spk?.takeIf { it.isNotBlank() } ?: "SPK-AUTO-${request.rencanaId}"
        val txId = request.client_transaction_id?.takeIf { it.isNotBlank() } ?: "TX-$spk"
        val dateVal = request.tanggal ?: request.tanggal_kerja ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val supervisor = request.nama_pengawas ?: request.kasie ?: ""
        val opName = request.nama_operator ?: request.operator ?: ""

        val reportReq = PostActivityReportRequest.create(
            clientUuid = txId,
            reportDate = dateVal,
            supervisorName = supervisor,
            unitCode = request.kode_unit ?: "",
            unitType = request.jenis_unit ?: "",
            activityName = request.nama_aktivitas ?: "",
            sapCode = request.kode_sap ?: "ACT-SAP-101",
            unitOfMeasure = request.satuan ?: "m3",
            operatorName = opName,
            workLocation = request.lokasi_kerja ?: request.kode_lokasi ?: "",
            shift = request.shift ?: "SIANG",
            workDurationHours = request.durasi_jam_kerja ?: request.jam_kerja ?: 7.0,
            hmAwal = request.hm_awal ?: 0.0,
            hmAkhir = request.hm_akhir ?: 0.0,
            hmDailyRunning = request.hm_harian_berjalan ?: 0.0,
            workOutput = request.hasil_kerja ?: 0.0,
            notes = request.keterangan ?: "",
            unitStatus = request.status_unit ?: "OPERASI",
            isRefueled = request.is_isi_solar ?: false,
            fuelLiters = request.jumlah_liter_solar ?: 0.0,
            stikSolarAwal = request.stik_solar_awal,
            stikSolarAkhir = request.stik_solar_akhir,
            pemakaianSolar = request.pemakaian_solar,
            kendalaId = request.kendala_id,
            kendalaNama = request.kendala_nama,
            catatanKendala = request.catatan_kendala,
            kendalaLapangan = request.kendala_lapangan,
            kendalaList = request.kendala_list,
            daftarKendala = request.daftar_kendala,
            photoBase64 = request.photoBase64 ?: request.photo_base64 ?: request.foto_bukti ?: request.foto,
            photoPath = request.photoPath ?: request.photo_path,
            photoSizeKb = request.photoSizeKb ?: request.photo_size_kb,
            photoDimensions = request.photoDimensions ?: request.photo_dimensions,
            isHmAwalCorrected = request.is_hm_awal_corrected ?: false,
            alasanKoreksiHm = request.alasan_koreksi_hm,
            clientTransactionId = txId,
            idRencanaKerja = request.rencanaId,
            nomorSpk = spk,
            wilayah = request.wilayah ?: "PG1",
            approvalStatus = request.approval_status ?: "Menunggu Approval",
            approvedBy = request.approved_by,
            approvedAt = request.approved_at,
            isLocked = request.is_locked ?: false,
            operatorNik = request.operator_nik,
            rolePenginput = request.role_penginput ?: "operator"
        )

        return@withContext try {
            // 1. Kirim HTTP POST ke backend
            val response = apiService.submitRealisasi(request)

            if (response.isSuccessful && response.body()?.success != false) {
                // Pastikan endpoint aktivitas-unit dan hasil-aktivitas juga tersinkron
                try {
                    apiService.postActivityReport(reportReq)
                } catch (_: Exception) {}
                try {
                    apiService.postHasilAktivitas(request)
                } catch (_: Exception) {}

                // 2. Simpan ke Room lokal sebagai sudah tersinkron (is_synced = true)
                val localEntity = mapToLocalEntity(request, isSynced = true)
                realisasiDao.insertHasil(localEntity)

                // 3. Tandai tugas di RencanaKerjaDao sebagai selesai
                if (request.rencanaId > 0) {
                    rencanaKerjaDao.markTaskAsCompleted(request.rencanaId)
                }

                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "HTTP Error ${response.code()}"
                Log.e("HTTP_REALISASI", "Gagal kirim: ${response.code()} - $errorMsg")

                // Simpan sebagai antrean lokal (is_synced = false)
                val localDraft = mapToLocalEntity(request, isSynced = false)
                realisasiDao.insertHasil(localDraft)
                if (request.rencanaId > 0) {
                    rencanaKerjaDao.markTaskAsCompleted(request.rencanaId)
                }

                Result.failure(Exception("Error Server (${response.code()}): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("HTTP_REALISASI", "Exception saat kirim realisasi: ${e.message}", e)

            // Jika internet mati, simpan sebagai draft offline (is_synced = false)
            val localDraft = mapToLocalEntity(request, isSynced = false)
            realisasiDao.insertHasil(localDraft)
            if (request.rencanaId > 0) {
                rencanaKerjaDao.markTaskAsCompleted(request.rencanaId)
            }

            Result.failure(e)
        }
    }

    /**
     * Sinkronisasi ulang item tertunda (is_synced == false) ke server Web Admin.
     */
    suspend fun syncUnsyncedRealisasi(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsyncedList = realisasiDao.getUnsyncedHasilList().filter { !it.isSynced }
            var syncedCount = 0
            for (hasil in unsyncedList) {
                try {
                    val req = RealisasiRequest(
                        id = hasil.id,
                        rencana_id = hasil.rencanaId,
                        id_rencana_kerja = hasil.rencanaId,
                        nomor_spk = hasil.nomorSpk,
                        jam_kerja = hasil.jamKerja,
                        durasi_jam_kerja = hasil.jamKerja,
                        hm_awal = hasil.hmAwal,
                        hm_akhir = hasil.hmAkhir,
                        hm_harian_berjalan = hasil.hmHarianBerjalan,
                        is_isi_solar = hasil.isIsiSolar,
                        jumlah_liter_solar = hasil.jumlahLiterSolar,
                        stik_solar_awal = hasil.stikSolarAwal,
                        stik_solar_akhir = hasil.stikSolarAkhir,
                        pemakaian_solar = hasil.pemakaianSolar,
                        kendala_id = hasil.kendalaId,
                        kendala_nama = hasil.kendalaNama,
                        catatan_kendala = hasil.catatanKendala,
                        hasil_kerja = hasil.hasilKerja,
                        keterangan = hasil.keterangan,
                        nama_pengawas = hasil.supervisorName,
                        kasie = hasil.namaKasie.ifBlank { hasil.supervisorName },
                        kode_unit = hasil.kodeUnit,
                        nama_operator = hasil.operator,
                        operator = hasil.operator,
                        lokasi_kerja = hasil.kodeLokasi,
                        kode_lokasi = hasil.kodeLokasi,
                        nama_aktivitas = hasil.activityName,
                        kode_sap = hasil.sapCode,
                        satuan = hasil.satuan,
                        status_unit = hasil.unitStatus,
                        foto_bukti = hasil.photoBase64,
                        fotoBukti = hasil.photoBase64,
                        bukti_foto = hasil.photoBase64,
                        foto = hasil.photoBase64,
                        photo_base64 = hasil.photoBase64,
                        photoBase64 = hasil.photoBase64,
                        photo_path = hasil.photoPath,
                        photoPath = hasil.photoPath,
                        photo_size_kb = hasil.photoSizeKb,
                        photoSizeKb = hasil.photoSizeKb,
                        photo_dimensions = hasil.photoDimensions,
                        photoDimensions = hasil.photoDimensions,
                        is_hm_awal_corrected = hasil.isHmAwalCorrected,
                        alasan_koreksi_hm = hasil.alasanKoreksiHm,
                        client_transaction_id = hasil.clientTransactionId,
                        wilayah = hasil.wilayah,
                        approval_status = hasil.approvalStatus,
                        approved_by = hasil.approvedBy,
                        approved_at = hasil.approvedAt,
                        is_locked = hasil.isLocked,
                        operator_nik = hasil.operatorNik,
                        tanggal = hasil.tanggalFormat,
                        tanggal_kerja = hasil.tanggalFormat,
                        shift = hasil.shift
                    )
                    val reportReq = PostActivityReportRequest.create(
                        clientUuid = hasil.clientTransactionId.ifBlank { UUID.randomUUID().toString() },
                        reportDate = hasil.tanggalFormat,
                        supervisorName = hasil.supervisorName,
                        unitCode = hasil.kodeUnit,
                        unitType = "",
                        activityName = hasil.activityName,
                        sapCode = hasil.sapCode,
                        unitOfMeasure = hasil.satuan,
                        operatorName = hasil.operator,
                        workLocation = hasil.kodeLokasi,
                        shift = hasil.shift,
                        workDurationHours = hasil.jamKerja,
                        hmAwal = hasil.hmAwal,
                        hmAkhir = hasil.hmAkhir,
                        hmDailyRunning = hasil.hmHarianBerjalan,
                        workOutput = hasil.hasilKerja,
                        notes = hasil.keterangan,
                        unitStatus = hasil.unitStatus,
                        isRefueled = hasil.isIsiSolar,
                        fuelLiters = hasil.jumlahLiterSolar,
                        stikSolarAwal = hasil.stikSolarAwal,
                        stikSolarAkhir = hasil.stikSolarAkhir,
                        pemakaianSolar = hasil.pemakaianSolar,
                        kendalaId = hasil.kendalaId,
                        kendalaNama = hasil.kendalaNama,
                        catatanKendala = hasil.catatanKendala,
                        photoBase64 = hasil.photoBase64,
                        photoPath = hasil.photoPath,
                        photoSizeKb = hasil.photoSizeKb,
                        photoDimensions = hasil.photoDimensions,
                        isHmAwalCorrected = hasil.isHmAwalCorrected,
                        alasanKoreksiHm = hasil.alasanKoreksiHm,
                        clientTransactionId = hasil.clientTransactionId,
                        idRencanaKerja = hasil.rencanaId,
                        nomorSpk = hasil.nomorSpk,
                        wilayah = hasil.wilayah,
                        approvalStatus = hasil.approvalStatus,
                        approvedBy = hasil.approvedBy,
                        approvedAt = hasil.approvedAt,
                        isLocked = hasil.isLocked,
                        operatorNik = hasil.operatorNik
                    )
                    apiClientManager.submitRealisasiToServer(reportReq, req)
                    val now = System.currentTimeMillis()
                    realisasiDao.markAsSynced(hasil.id, now)
                    reportDao?.markReportAsSyncedByUnitAndHm(hasil.kodeUnit, hasil.hmAkhir, hasil.supervisorName, now)
                    syncedCount++
                } catch (e: Exception) {
                    Log.e("HTTP_REALISASI", "Gagal sinkron ulang item ${hasil.id}: ${e.message}", e)
                }
            }
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToLocalEntity(
        request: RealisasiRequest,
        isSynced: Boolean
    ): HasilInputAktivitasEntity {
        val spk = request.nomor_spk?.takeIf { it.isNotBlank() } ?: "SPK-AUTO-${request.rencanaId}"
        val txId = request.client_transaction_id?.takeIf { it.isNotBlank() } ?: "TX-$spk"
        val supervisor = request.nama_pengawas ?: request.kasie ?: ""
        val opName = request.nama_operator ?: request.operator ?: ""
        val stikAwal = request.stik_solar_awal ?: 0.0
        val stikAkhir = request.stik_solar_akhir ?: 0.0
        val isiLiters = request.jumlah_liter_solar ?: 0.0
        val isIsi = (request.is_isi_solar == true) || isiLiters > 0.0
        val calculatedSolar = if (stikAwal > 0.0 || stikAkhir > 0.0) {
            ((stikAwal + isiLiters) - stikAkhir).coerceAtLeast(0.0)
        } else {
            request.pemakaian_solar ?: (if (isIsi) isiLiters else 0.0)
        }
        val kendalaItems = request.daftar_kendala ?: request.kendala_list ?: request.kendala_lapangan
        val kendalaJson = kendalaItems?.takeIf { it.isNotEmpty() }?.let { com.example.data.remote.KendalaJsonConverter.toJson(it) }

        return HasilInputAktivitasEntity(
            id = request.id ?: 0L,
            rencanaId = request.rencanaId,
            nomorSpk = spk,
            supervisorName = supervisor,
            namaKasie = request.kasie ?: supervisor,
            createdBy = opName.ifBlank { supervisor },
            kodeUnit = request.kode_unit ?: "",
            operator = opName,
            operatorNik = request.operator_nik ?: "",
            kodeLokasi = request.lokasi_kerja ?: request.kode_lokasi ?: "",
            wilayah = request.wilayah ?: "PG1",
            activityName = request.nama_aktivitas ?: "",
            sapCode = request.kode_sap ?: "ACT-SAP-101",
            satuan = request.satuan ?: "m3",
            unitStatus = request.status_unit ?: "OPERASI",
            shift = request.shift ?: "SIANG",
            jamKerja = request.durasi_jam_kerja ?: request.jam_kerja ?: 7.0,
            hmAwal = request.hm_awal ?: 0.0,
            hmAkhir = request.hm_akhir ?: 0.0,
            hmHarianBerjalan = request.hm_harian_berjalan ?: 0.0,
            isIsiSolar = isIsi,
            jumlahLiterSolar = isiLiters,
            stikSolarAwal = stikAwal,
            stikSolarAkhir = stikAkhir,
            pemakaianSolar = calculatedSolar,
            kendalaId = request.kendala_id ?: kendalaItems?.firstOrNull()?.resolvedKode,
            kendalaNama = request.kendala_nama ?: kendalaItems?.firstOrNull()?.resolvedNama,
            catatanKendala = request.catatan_kendala,
            daftarKendalaJson = kendalaJson,
            hasilKerja = request.hasil_kerja ?: 0.0,
            keterangan = request.keterangan ?: "",
            photoBase64 = request.photoBase64 ?: request.photo_base64 ?: request.foto_bukti ?: request.fotoBukti ?: request.bukti_foto ?: request.foto,
            photoPath = request.photoPath ?: request.photo_path,
            photoSizeKb = request.photoSizeKb ?: request.photo_size_kb,
            photoDimensions = request.photoDimensions ?: request.photo_dimensions,
            isHmAwalCorrected = request.is_hm_awal_corrected ?: false,
            alasanKoreksiHm = request.alasan_koreksi_hm,
            statusKirim = if (isSynced) "SYNCED" else "PENDING",
            isSynced = isSynced,
            syncedAt = if (isSynced) System.currentTimeMillis() else null,
            approvalStatus = request.approval_status ?: "Menunggu Approval",
            approvedBy = request.approved_by,
            approvedAt = request.approved_at,
            isLocked = request.is_locked ?: false,
            clientTransactionId = txId,
            tanggalFormat = request.tanggal ?: request.tanggal_kerja ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            createdAt = System.currentTimeMillis()
        )
    }
}
