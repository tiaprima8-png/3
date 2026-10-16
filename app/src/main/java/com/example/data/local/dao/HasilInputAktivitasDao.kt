package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.HasilInputAktivitasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HasilInputAktivitasDao {
    @Query("SELECT * FROM hasil_input_aktivitas ORDER BY created_at DESC")
    fun getAllHasil(): Flow<List<HasilInputAktivitasEntity>>

    @Query("SELECT * FROM hasil_input_aktivitas WHERE supervisor_name = :supervisor ORDER BY created_at DESC")
    fun getHasilForSupervisor(supervisor: String): Flow<List<HasilInputAktivitasEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHasil(hasil: HasilInputAktivitasEntity): Long

    @Query("UPDATE hasil_input_aktivitas SET is_synced = 1, status_kirim = 'SYNCED', synced_at = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE hasil_input_aktivitas SET is_synced = 1, status_kirim = 'SYNCED', synced_at = :timestamp WHERE kode_unit = :unitCode AND hm_akhir = :hmAkhir AND supervisor_name = :supervisor")
    suspend fun markHasilAsSyncedByUnitAndHm(unitCode: String, hmAkhir: Double, supervisor: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE hasil_input_aktivitas SET is_synced = 1, status_kirim = 'SYNCED', synced_at = :timestamp WHERE client_transaction_id = :txId")
    suspend fun markHasilAsSyncedByTxId(txId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM hasil_input_aktivitas WHERE kode_unit = :kodeUnit ORDER BY created_at DESC, id DESC LIMIT 1")
    suspend fun getLatestHasilForUnit(kodeUnit: String): HasilInputAktivitasEntity?

    @Query("SELECT * FROM hasil_input_aktivitas WHERE is_synced = 0")
    suspend fun getUnsyncedHasilList(): List<HasilInputAktivitasEntity>

    @Query("SELECT COUNT(*) FROM hasil_input_aktivitas WHERE is_synced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hasilList: List<HasilInputAktivitasEntity>)

    @Query("DELETE FROM hasil_input_aktivitas WHERE id = :id")
    suspend fun deleteHasil(id: Long)

    @Query("DELETE FROM hasil_input_aktivitas")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM hasil_input_aktivitas WHERE is_synced = 1")
    suspend fun deleteAllSynced(): Int

    @Transaction
    suspend fun replaceAll(listBaru: List<HasilInputAktivitasEntity>) {
        deleteAll()
        insertAll(listBaru)
    }

    @Transaction
    suspend fun replaceAllSynced(listBaru: List<HasilInputAktivitasEntity>) {
        deleteAllSynced()
        insertAll(listBaru)
    }
}
