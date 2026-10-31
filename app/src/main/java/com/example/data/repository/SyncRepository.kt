package com.example.data.repository

import androidx.room.Transaction
import com.example.data.local.dao.LokasiDao
import com.example.data.local.entity.MasterLokasiEntity
import kotlinx.coroutines.flow.Flow

class SyncRepository(
    private val lokasiDao: LokasiDao
) {
    @Transaction
    suspend fun updateMasterLokasi(newList: List<MasterLokasiEntity>) {
        lokasiDao.deleteAll()
        lokasiDao.insertAll(newList)
    }

    suspend fun getActiveLokasiCount(): Int = lokasiDao.getActiveLokasiCount()

    fun getAllActiveLokasi(searchQuery: String?, wilayah: String?): Flow<List<MasterLokasiEntity>> =
        lokasiDao.getAllActiveLokasi(searchQuery, wilayah)

    suspend fun syncRealisasiWithServer(
        kasieName: String = "",
        apiClientManager: com.example.data.remote.ApiClientManager,
        realisasiDao: com.example.data.local.dao.ActivityReportDao,
        hasilDao: com.example.data.local.dao.HasilInputAktivitasDao,
        rencanaKerjaDao: com.example.data.local.dao.RencanaKerjaDao
    ): Result<List<com.example.data.local.entity.ActivityReportEntity>> {
        return try {
            val (incoming, _) = apiClientManager.fetchPendingRealisasiForKasie(kasieName)
            val remoteList = incoming.filter { item ->
                val hmAwal = item.hm_awal ?: 0.0
                val hmAkhir = item.hm_akhir ?: 0.0
                val totalHm = item.hm_harian_berjalan ?: (if (hmAkhir > hmAwal) hmAkhir - hmAwal else 0.0)
                val hasilKerja = item.hasil_kerja ?: 0.0
                hmAkhir > 0.0 && (totalHm > 0.0 || hmAkhir > hmAwal || hasilKerja > 0.0)
            }

            // FULL SYNC: Hapus data lokal lama, lalu isi dengan data remote terkini
            realisasiDao.deleteAll()
            hasilDao.deleteAll()

            val list = mutableListOf<com.example.data.local.entity.ActivityReportEntity>()
            val hasilEntities = mutableListOf<com.example.data.local.entity.HasilInputAktivitasEntity>()
            for ((idx, req) in remoteList.withIndex()) {
                val uuid = req.client_uuid ?: req.client_transaction_id ?: java.util.UUID.randomUUID().toString()
                val spk = (req.nomor_spk ?: "").trim()
                val renId = req.id_rencana_kerja ?: 0L
                val resolvedSpk = spk.ifBlank { "SPK-AUTO-${if (renId > 0) renId else uuid}" }
                val statusStr = req.approval_status?.lowercase()?.trim() ?: ""
                val isLocked = req.is_locked == true ||
                    statusStr.startsWith("disetujui") ||
                    statusStr.contains("disetujui") ||
                    statusStr == "approved"
                val hmAwal = req.hm_awal ?: 0.0
                val hmAkhir = req.hm_akhir ?: 0.0
                val hmDaily = if ((req.hm_harian_berjalan ?: 0.0) > 0.0) req.hm_harian_berjalan!! else if (hmAkhir > hmAwal) hmAkhir - hmAwal else 0.0
                val spv = req.nama_pengawas ?: req.supervisor_name ?: kasieName
                val resolvedApproval = if (isLocked) {
                    req.approval_status?.takeIf { it.isNotBlank() } ?: "Disetujui Kasie"
                } else {
                    req.approval_status?.takeIf { it.isNotBlank() } ?: "Menunggu"
                }

                val entity = com.example.data.local.entity.ActivityReportEntity(
                    id = (idx + 1).toLong(),
                    clientUuid = uuid,
                    reportDate = req.tanggal ?: "",
                    supervisorName = spv,
                    namaKasie = spv,
                    unitCode = req.kode_unit ?: "",
                    unitType = req.jenis_unit ?: "",
                    activityName = req.nama_aktivitas ?: "",
                    sapCode = req.kode_sap ?: "ACT-SAP-101",
                    unitOfMeasure = req.satuan ?: "m3",
                    operatorName = req.nama_operator ?: "",
                    operatorNik = req.operator_nik,
                    workLocation = req.lokasi_kerja ?: "",
                    shift = req.shift ?: "SIANG",
                    hmAwal = hmAwal,
                    hmAkhir = hmAkhir,
                    hmDailyRunning = hmDaily,
                    workOutput = req.hasil_kerja ?: 0.0,
                    isRefueled = req.is_isi_solar == true,
                    fuelLiters = req.jumlah_liter_solar ?: 0.0,
                    pemakaianSolar = req.pemakaian_solar ?: req.jumlah_liter_solar ?: 0.0,
                    approvalStatus = resolvedApproval,
                    isLocked = isLocked,
                    nomorSpk = resolvedSpk,
                    idRencanaKerja = renId,
                    isSynced = true
                )
                list.add(entity)
                hasilEntities.add(
                    com.example.data.local.entity.HasilInputAktivitasEntity(
                        id = (idx + 1).toLong(),
                        rencanaId = renId,
                        nomorSpk = resolvedSpk,
                        supervisorName = spv,
                        namaKasie = spv,
                        createdBy = entity.operatorName,
                        kodeUnit = entity.unitCode,
                        operator = entity.operatorName,
                        operatorNik = entity.operatorNik ?: "",
                        kodeLokasi = entity.workLocation,
                        activityName = entity.activityName,
                        sapCode = entity.sapCode,
                        satuan = entity.unitOfMeasure,
                        unitStatus = entity.unitStatus,
                        shift = entity.shift,
                        jamKerja = entity.workDurationHours,
                        hmAwal = hmAwal,
                        hmAkhir = hmAkhir,
                        hmHarianBerjalan = hmDaily,
                        hasilKerja = entity.workOutput,
                        isIsiSolar = entity.isRefueled,
                        jumlahLiterSolar = entity.fuelLiters,
                        pemakaianSolar = entity.pemakaianSolar,
                        statusKirim = "SYNCED",
                        isSynced = true,
                        approvalStatus = entity.approvalStatus,
                        isLocked = isLocked,
                        tanggalFormat = entity.reportDate,
                        clientTransactionId = uuid
                    )
                )
            }
            if (list.isNotEmpty()) {
                realisasiDao.insertAll(list)
                hasilDao.insertAll(hasilEntities)
            }
            rencanaKerjaDao.restoreUnrealizedPlansWithNoValidReport()
            Result.success(list)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Gagal sync realisasi: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncRencanaWithServer(
        supervisorName: String = "",
        apiClientManager: com.example.data.remote.ApiClientManager,
        rencanaKerjaDao: com.example.data.local.dao.RencanaKerjaDao
    ): Result<List<com.example.data.local.entity.RencanaKerjaEntity>> {
        return try {
            val (incomingRequests, _) = apiClientManager.fetchActiveRencanaKerja(supervisorName)
            val remoteList = incomingRequests.mapIndexed { index, renReq ->
                val spkNum = renReq.nomor_spk?.trim() ?: ""
                val spkStatus = when {
                    !renReq.status_spk.isNullOrBlank() -> renReq.status_spk
                    spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" -> "SPK_TERBIT"
                    else -> "MENUNGGU_SPK"
                }
                val spv = renReq.resolvedPengawas.ifBlank { supervisorName }
                com.example.data.local.entity.RencanaKerjaEntity(
                    id = if (renReq.numericId > 0) renReq.numericId else (index + 1).toLong(),
                    supervisorName = spv,
                    pengawas = spv,
                    kasie = spv,
                    createdBy = renReq.created_by ?: spv,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = renReq.resolvedUnitKode,
                    operator = renReq.resolvedOperator,
                    namaOperator = renReq.resolvedOperator,
                    sapCode = renReq.resolvedSapCode,
                    namaAktivitas = renReq.resolvedAktivitas,
                    satuanAktivitas = renReq.resolvedSatuan,
                    kodeLokasi = renReq.resolvedLokasi,
                    wilayah = renReq.wilayah ?: "PG1",
                    shiftKerja = renReq.resolvedShift,
                    keterangan = renReq.keterangan ?: "",
                    nomorSpk = spkNum,
                    statusSpk = spkStatus,
                    statusTahap = renReq.status_tahap ?: "RENCANA",
                    statusRealisasi = if (renReq.is_realized == true) "Terealisasi" else "Menunggu Realisasi",
                    isRealized = renReq.is_realized == true,
                    isSynced = true,
                    tanggalFormat = renReq.tanggal ?: ""
                )
            }

            // FULL SYNC: Hapus rencana lokal lama, lalu isi data remote
            rencanaKerjaDao.deleteAll()
            if (remoteList.isNotEmpty()) {
                rencanaKerjaDao.insertAll(remoteList)
            }
            Result.success(remoteList)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Gagal sync rencana: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncAllData(
        supervisorName: String,
        apiClientManager: com.example.data.remote.ApiClientManager,
        realisasiDao: com.example.data.local.dao.ActivityReportDao,
        hasilDao: com.example.data.local.dao.HasilInputAktivitasDao,
        rencanaKerjaDao: com.example.data.local.dao.RencanaKerjaDao
    ) {
        syncRencanaWithServer(supervisorName, apiClientManager, rencanaKerjaDao)
        syncRealisasiWithServer(supervisorName, apiClientManager, realisasiDao, hasilDao, rencanaKerjaDao)
    }

    suspend fun resetLocalTransactionCache(
        rencanaKerjaDao: com.example.data.local.dao.RencanaKerjaDao,
        realisasiDao: com.example.data.local.dao.ActivityReportDao,
        hasilDao: com.example.data.local.dao.HasilInputAktivitasDao
    ) {
        rencanaKerjaDao.deleteAll()
        realisasiDao.deleteAll()
        hasilDao.deleteAll()
    }

    suspend fun syncPendingRealisasiForKasie(
        kasieName: String,
        apiClientManager: com.example.data.remote.ApiClientManager,
        reportDao: com.example.data.local.dao.ActivityReportDao,
        hasilDao: com.example.data.local.dao.HasilInputAktivitasDao,
        rencanaDao: com.example.data.local.dao.RencanaKerjaDao
    ): Result<List<com.example.data.local.entity.ActivityReportEntity>> {
        return syncRealisasiWithServer(kasieName, apiClientManager, reportDao, hasilDao, rencanaDao)
    }
}

