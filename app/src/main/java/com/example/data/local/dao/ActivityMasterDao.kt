package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityMasterDao {
    @Query("SELECT * FROM master_activities ORDER BY activityName ASC")
    fun getAllActivities(): Flow<List<ActivityMasterEntity>>

    @Query("""
        SELECT * FROM master_activities 
        WHERE :jenisUnit IS NULL OR :jenisUnit = '' OR :jenisUnit = 'ALL' OR jenis_unit = 'ALL' 
           OR UPPER(jenis_unit) = UPPER(:jenisUnit) 
           OR UPPER(jenis_unit) LIKE '%' || UPPER(:jenisUnit) || '%'
        ORDER BY activityName ASC
    """)
    fun getActivitiesByJenisUnit(jenisUnit: String?): Flow<List<ActivityMasterEntity>>

    @Query("SELECT * FROM master_activities WHERE sapCode = :sapCode LIMIT 1")
    suspend fun getActivityBySapCode(sapCode: String): ActivityMasterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityMasterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityMasterEntity)

    @Update
    suspend fun updateActivity(activity: ActivityMasterEntity)

    @Query("DELETE FROM master_activities WHERE sapCode = :sapCode")
    suspend fun deleteActivity(sapCode: String)

    @Query("DELETE FROM master_activities")
    suspend fun clearAllActivities()

    @Query("DELETE FROM master_activities")
    suspend fun deleteAll()
}
