package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ActivityMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityMasterDao {
    @Query("SELECT * FROM master_activities ORDER BY activityName ASC")
    fun getAllActivities(): Flow<List<ActivityMasterEntity>>

    @Query("""
        SELECT * FROM master_activities 
        WHERE (
              LOWER(TRIM(jenis_unit)) = LOWER(TRIM(:modelTipe))
              OR LOWER(TRIM(jenis_unit)) = LOWER(TRIM(:jenisUnit))
              OR LOWER(TRIM(jenis_unit)) = 'semua'
              OR LOWER(TRIM(jenis_unit)) = 'all'
              OR :jenisUnit IS NULL OR :jenisUnit = '' OR :jenisUnit = 'SEMUA' OR :jenisUnit = 'ALL'
          )
          AND (
              :kategoriFilter IS NULL 
              OR :kategoriFilter = '' 
              OR UPPER(TRIM(:kategoriFilter)) = 'SEMUA' 
              OR UPPER(TRIM(:kategoriFilter)) = 'ALL'
              OR UPPER(TRIM(kategori_status)) = UPPER(TRIM(:kategoriFilter))
              OR UPPER(TRIM(category)) = UPPER(TRIM(:kategoriFilter))
          )
          AND (
              :searchQuery IS NULL 
              OR :searchQuery = '' 
              OR LOWER(activityName) LIKE '%' || LOWER(:searchQuery) || '%'
              OR LOWER(sapCode) LIKE '%' || LOWER(:searchQuery) || '%'
          )
        ORDER BY 
          CASE UPPER(TRIM(kategori_status))
            WHEN 'OPERASI' THEN 1
            WHEN 'STANDBY' THEN 2
            WHEN 'BREAKDOWN' THEN 3
            ELSE 4
          END,
          activityName ASC
    """)
    fun getAktivitasForUnit(
        jenisUnit: String,
        modelTipe: String = jenisUnit,
        kategoriFilter: String?,
        searchQuery: String?
    ): Flow<List<ActivityMasterEntity>>

    @Query("""
        SELECT * FROM master_activities 
        WHERE (
              LOWER(TRIM(jenis_unit)) = LOWER(TRIM(:modelTipe))
              OR LOWER(TRIM(jenis_unit)) = 'semua'
              OR LOWER(TRIM(jenis_unit)) = 'all'
              OR :modelTipe IS NULL OR :modelTipe = ''
          )
          AND (
              :kategoriFilter IS NULL 
              OR :kategoriFilter = '' 
              OR UPPER(TRIM(:kategoriFilter)) = 'SEMUA' 
              OR UPPER(TRIM(:kategoriFilter)) = 'ALL'
              OR UPPER(TRIM(kategori_status)) = UPPER(TRIM(:kategoriFilter))
              OR UPPER(TRIM(category)) = UPPER(TRIM(:kategoriFilter))
          )
          AND (
              :searchQuery IS NULL 
              OR :searchQuery = '' 
              OR LOWER(activityName) LIKE '%' || LOWER(:searchQuery) || '%'
              OR LOWER(sapCode) LIKE '%' || LOWER(:searchQuery) || '%'
          )
        ORDER BY 
          CASE UPPER(TRIM(kategori_status))
            WHEN 'OPERASI' THEN 1
            WHEN 'STANDBY' THEN 2
            WHEN 'BREAKDOWN' THEN 3
            ELSE 4
          END,
          activityName ASC
    """)
    fun getAktivitasStrict(
        modelTipe: String,
        kategoriFilter: String?,
        searchQuery: String?
    ): Flow<List<ActivityMasterEntity>>

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
    suspend fun insertAll(activities: List<ActivityMasterEntity>)

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

    @Transaction
    suspend fun replaceAll(listBaru: List<ActivityMasterEntity>) {
        deleteAll()
        insertActivities(listBaru)
    }
}

typealias AktivitasDao = ActivityMasterDao
