package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.RencanaKerjaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RencanaKerjaDao {
    @Query("SELECT * FROM rencana_kerja ORDER BY created_at DESC")
    fun getAllRencana(): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja WHERE supervisor_name = :supervisor OR pengawas = :supervisor ORDER BY created_at DESC")
    fun getRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' 
               OR wilayah = :wilayah 
               OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
               OR REPLACE(wilayah, ' ', ' PG') = :wilayah
               OR wilayah = REPLACE(:wilayah, ' ', ' PG')
               OR wilayah = REPLACE(:wilayah, ' PG', ' '))
          AND (:kodeUnit IS NULL OR :kodeUnit = 'Semua Unit' OR :kodeUnit = '' OR kode_unit = :kodeUnit)
        ORDER BY created_at DESC
    """)
    fun getRencanaFiltered(wilayah: String?, kodeUnit: String?): Flow<List<RencanaKerjaEntity>>

    @Query("DELETE FROM rencana_kerja")
    suspend fun deleteAll(): Int

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (:supervisor = 'Semua' OR :supervisor = '' OR supervisor_name = :supervisor OR pengawas = :supervisor) 
          AND is_realized = 0 
          AND (status_realisasi = 'BELUM' OR status_realisasi = '' OR status_realisasi IS NULL)
          AND status_tahap != 'REALISASI_SELESAI' 
          AND status_spk != 'SELESAI'
        ORDER BY created_at DESC
    """)
    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (:pengawas = 'Semua' OR :pengawas = '' OR supervisor_name = :pengawas OR pengawas = :pengawas) 
          AND is_realized = 0 
          AND (status_realisasi = 'BELUM' OR status_realisasi = '' OR status_realisasi IS NULL)
          AND status_tahap != 'REALISASI_SELESAI' 
          AND status_spk != 'SELESAI'
        ORDER BY created_at DESC
    """)
    fun getRencanaKerjaBelumRealisasi(pengawas: String): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja WHERE id = :id LIMIT 1")
    suspend fun getRencanaById(id: Long): RencanaKerjaEntity?

    @Query("SELECT * FROM rencana_kerja WHERE (supervisor_name = :supervisor OR pengawas = :supervisor) AND kode_unit = :kodeUnit AND tanggal_format = :tanggal LIMIT 1")
    suspend fun findRencana(supervisor: String, kodeUnit: String, tanggal: String): RencanaKerjaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRencana(rencana: RencanaKerjaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRencanaList(list: List<RencanaKerjaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<RencanaKerjaEntity>)

    @Transaction
    suspend fun replaceAll(listBaru: List<RencanaKerjaEntity>) {
        deleteAll()
        insertAll(listBaru)
    }

    @Transaction
    suspend fun replaceForSupervisor(namaPengawas: String, listBaru: List<RencanaKerjaEntity>) {
        clearActiveRencanaForSupervisor(namaPengawas)
        insertAll(listBaru)
    }

    @Update
    suspend fun updateRencana(rencana: RencanaKerjaEntity)

    @Query("UPDATE rencana_kerja SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE rencana_kerja SET is_realized = 1, status_tahap = 'REALISASI_SELESAI', status_spk = 'SELESAI', status_realisasi = 'SUDAH' WHERE id = :id")
    suspend fun markAsRealized(id: Long)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE id = :id")
    suspend fun updateSpk(id: Long, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit")
    suspend fun updateSpkByUnit(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit AND (status_realisasi = 'BELUM' OR is_realized = 0)")
    suspend fun updateSpkForActiveUnit(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit")
    suspend fun updateSpkStatus(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE id = :id OR kode_unit = :kodeUnit")
    suspend fun updateSpkByIdOrUnit(id: Long, kodeUnit: String, spk: String, status: String)

    @Query("SELECT * FROM rencana_kerja WHERE kode_unit = :kodeUnit LIMIT 1")
    suspend fun getRencanaByUnit(kodeUnit: String): RencanaKerjaEntity?

    @Query("SELECT * FROM rencana_kerja WHERE is_synced = 0")
    suspend fun getUnsyncedRencanaList(): List<RencanaKerjaEntity>

    @Query("DELETE FROM rencana_kerja WHERE id = :id")
    suspend fun deleteRencana(id: Long): Int

    // 1. Sync Pruning Query
    @Query("DELETE FROM rencana_kerja WHERE (supervisor_name = :namaPengawas OR pengawas = :namaPengawas) AND (status_realisasi = 'BELUM' OR is_realized = 0) AND id NOT IN (:activeServerIds)")
    suspend fun deletePrunedRencana(namaPengawas: String, activeServerIds: List<Long>): Int

    @Query("DELETE FROM rencana_kerja WHERE (supervisor_name = :namaPengawas OR pengawas = :namaPengawas) AND (status_realisasi = 'BELUM' OR is_realized = 0)")
    suspend fun clearActiveRencanaForSupervisor(namaPengawas: String): Int

    // 2. Real-time Delete Query
    @Query("DELETE FROM rencana_kerja WHERE (:deletedId > 0 AND id = :deletedId) OR (:unitKode != '' AND kode_unit = :unitKode)")
    suspend fun deleteByIdOrUnitInternal(deletedId: Long, unitKode: String): Int

    suspend fun deleteByIdOrUnit(deletedId: Long, unitKode: String?): Int {
        return deleteByIdOrUnitInternal(deletedId, unitKode?.trim() ?: "")
    }

    suspend fun deleteByIdOrUnit(deletedId: String?, unitKode: String?): Int {
        val idLong = deletedId?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val cleanUnit = unitKode?.trim() ?: ""
        return if (idLong > 0 || cleanUnit.isNotBlank()) {
            deleteByIdOrUnitInternal(idLong, cleanUnit)
        } else 0
    }

    @Query("DELETE FROM rencana_kerja WHERE kode_unit NOT IN (SELECT unitCode FROM units)")
    suspend fun deleteOrphanedRencanaKerja(): Int
}
