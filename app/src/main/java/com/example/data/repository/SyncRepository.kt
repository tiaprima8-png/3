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
}
