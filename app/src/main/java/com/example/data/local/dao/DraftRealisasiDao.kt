package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DraftRealisasiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftRealisasiDao {
    @Query("SELECT * FROM draft_realisasi WHERE rencana_id = :rencanaId LIMIT 1")
    suspend fun getDraftByRencanaId(rencanaId: Long): DraftRealisasiEntity?

    @Query("SELECT * FROM draft_realisasi WHERE rencana_id = :rencanaId LIMIT 1")
    fun observeDraftByRencanaId(rencanaId: Long): Flow<DraftRealisasiEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDraft(draft: DraftRealisasiEntity)

    @Query("DELETE FROM draft_realisasi WHERE rencana_id = :rencanaId")
    suspend fun deleteDraftByRencanaId(rencanaId: Long)

    @Query("SELECT * FROM draft_realisasi")
    fun getAllDrafts(): Flow<List<DraftRealisasiEntity>>

    @Query("DELETE FROM draft_realisasi")
    suspend fun clearAllDrafts()
}
