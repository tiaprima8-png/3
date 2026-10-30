package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ActivityReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityReportDao {
    @Query("SELECT * FROM activity_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ActivityReportEntity>>

    @Query("""
        SELECT * FROM activity_reports 
        WHERE LOWER(TRIM(supervisorName)) = LOWER(TRIM(:supervisor)) 
           OR LOWER(TRIM(namaKasie)) = LOWER(TRIM(:supervisor))
           OR LOWER(TRIM(createdBy)) = LOWER(TRIM(REPLACE(:supervisor, '@', '')))
           OR :supervisor = 'ALL' 
           OR :supervisor = 'Semua'
        ORDER BY createdAt DESC, id DESC
    """)
    fun getReportsForSupervisor(supervisor: String): Flow<List<ActivityReportEntity>>

    @Query("""
        SELECT * FROM activity_reports 
        WHERE (
            LOWER(TRIM(namaKasie)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(supervisorName)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(createdBy)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(supervisorName)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(namaKasie)) = LOWER(TRIM(REPLACE(:username, '@', '')))
        )
        AND (
            NOT EXISTS (SELECT 1 FROM units)
            OR unitCode IN (
                SELECT unitCode FROM units 
                WHERE LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:namaKasie))
                   OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            )
        )
        ORDER BY createdAt DESC, id DESC
    """)
    fun getRealisasiForKasie(namaKasie: String, username: String): Flow<List<ActivityReportEntity>>

    @Query("""
        SELECT * FROM activity_reports 
        WHERE LOWER(TRIM(operatorNik)) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(operatorName)) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(createdBy)) = LOWER(TRIM(REPLACE(:operatorIdentifier, '@', '')))
        ORDER BY createdAt DESC, id DESC
    """)
    fun getRealisasiForOperator(operatorIdentifier: String): Flow<List<ActivityReportEntity>>

    @Query("""
        UPDATE activity_reports 
        SET approval_status = 'Disetujui Kasie',
            approved_by = :approvedBy,
            approved_at = :approvedAt,
            is_locked = 1
        WHERE id = :id
    """)
    suspend fun approveReport(id: Long, approvedBy: String, approvedAt: String = System.currentTimeMillis().toString())


    @Query("""
        SELECT * FROM activity_reports 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' 
               OR wilayah = :wilayah 
               OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
               OR REPLACE(wilayah, ' ', ' PG') = :wilayah
               OR wilayah = REPLACE(:wilayah, ' ', ' PG')
               OR wilayah = REPLACE(:wilayah, ' PG', ' '))
          AND (
            :unitType IS NULL OR :unitType = 'Semua Jenis' OR :unitType = '' OR 
            (:unitType LIKE '%EXCAVATOR%' AND (unitType LIKE '%EXCAVATOR%' OR unitType LIKE '%HE%')) OR
            (:unitType LIKE '%GRADER%' AND unitType LIKE '%GRADER%') OR
            (:unitType LIKE '%COMPACTOR%' AND unitType LIKE '%COMPACTOR%') OR
            unitType = :unitType OR
            unitType LIKE '%' || :unitType || '%'
          )
          AND (:unitCode IS NULL OR :unitCode = 'Semua Unit' OR :unitCode = '' OR unitCode = :unitCode)
        ORDER BY createdAt DESC
    """)
    fun getReportsFiltered(wilayah: String?, unitType: String?, unitCode: String?): Flow<List<ActivityReportEntity>>

    @Query("SELECT * FROM activity_reports WHERE isSynced = 0 ORDER BY createdAt ASC")
    fun getUnsyncedReports(): Flow<List<ActivityReportEntity>>

    @Query("SELECT * FROM activity_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReportsList(): List<ActivityReportEntity>

    @Query("SELECT COUNT(*) FROM activity_reports WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM activity_reports WHERE clientUuid = :uuid LIMIT 1")
    suspend fun getReportByUuid(uuid: String): ActivityReportEntity?

    @Query("SELECT * FROM activity_reports WHERE supervisorName = :supervisor AND unitCode = :unitCode AND hmAkhir = :hmAkhir LIMIT 1")
    suspend fun findExistingReport(supervisor: String, unitCode: String, hmAkhir: Double): ActivityReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ActivityReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ActivityReportEntity>)

    @Update
    suspend fun updateReport(report: ActivityReportEntity)

    @Query("UPDATE activity_reports SET isSynced = 1, syncedAt = :timestamp WHERE id = :reportId")
    suspend fun markReportAsSynced(reportId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE activity_reports SET isSynced = 1, syncedAt = :timestamp WHERE unitCode = :unitCode AND hmAkhir = :hmAkhir AND supervisorName = :supervisor")
    suspend fun markReportAsSyncedByUnitAndHm(unitCode: String, hmAkhir: Double, supervisor: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE activity_reports SET isSynced = 1, syncedAt = :timestamp WHERE clientUuid = :clientUuid")
    suspend fun markReportAsSyncedByClientUuid(clientUuid: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM activity_reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ActivityReportEntity>)

    @Query("DELETE FROM activity_reports")
    suspend fun clearAllReports()

    @Query("DELETE FROM activity_reports")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM activity_reports WHERE isSynced = 1")
    suspend fun deleteAllSynced(): Int

    @Transaction
    suspend fun replaceAll(listBaru: List<ActivityReportEntity>) {
        deleteAll()
        insertReports(listBaru)
    }

    @Transaction
    suspend fun replaceAllSynced(listBaru: List<ActivityReportEntity>) {
        deleteAllSynced()
        insertReports(listBaru)
    }

    @Query("DELETE FROM activity_reports WHERE unitCode NOT IN (SELECT unitCode FROM units) OR LOWER(unitType) IN ('dump truck', 'bulldozer')")
    suspend fun deleteOrphanedReports(): Int
}

typealias RealisasiDao = ActivityReportDao

