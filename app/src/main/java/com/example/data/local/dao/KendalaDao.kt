package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.KendalaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KendalaDao {
    @Query("SELECT * FROM master_kendala ORDER BY id ASC")
    fun getAllKendala(): Flow<List<KendalaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKendalaList(list: List<KendalaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kendalaList: List<KendalaEntity>)

    @Query("DELETE FROM master_kendala")
    suspend fun clearAll()

    @Query("DELETE FROM master_kendala")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(listBaru: List<KendalaEntity>) {
        deleteAll()
        insertKendalaList(listBaru)
    }
}
