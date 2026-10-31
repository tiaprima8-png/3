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
        AND hmAkhir > 0
        AND (hmDailyRunning > 0 OR hmAkhir > hmAwal OR workOutput > 0)
        ORDER BY createdAt DESC, id DESC
    """)
    fun getRealisasiForKasie(namaKasie: String, username: String): Flow<List<ActivityReportEntity>>

    @Query("""
        SELECT * FROM activity_reports 
        WHERE (
            LOWER(TRIM(namaKasie)) = LOWER(TRIM(:kasieNama))
            OR LOWER(TRIM(supervisorName)) = LOWER(TRIM(:kasieNama))
            OR LOWER(TRIM(supervisorName)) = LOWER(TRIM(REPLACE(:kasieNama, '@', '')))
            OR LOWER(TRIM(namaKasie)) = LOWER(TRIM(REPLACE(:kasieNama, '@', '')))
        )
        AND (
            UPPER(TRIM(approvalStatus)) = 'MENUNGGU APPROVAL'
            OR UPPER(TRIM(approvalStatus)) = 'MENUNGGU VERIFIKASI'
            OR UPPER(TRIM(approvalStatus)) = 'PENDING'
        )
        AND isLocked = 0
        AND hmAkhir > 0
        AND (hmDailyRunning > 0 OR hmAkhir > hmAwal OR workOutput > 0)
        ORDER BY createdAt DESC, id DESC
    """)
    fun getPendingVerificationForKasie(kasieNama: String): Flow<List<ActivityReportEntity>>

    @Query("DELETE FROM activity_reports WHERE hmAkhir <= 0 OR (hmDailyRunning <= 0 AND hmAkhir <= hmAwal AND workOutput <= 0)")
    suspend fun deleteInvalidEmptyReports(): Int

    @Query("""
        SELECT * FROM activity_reports 
        WHERE LOWER(TRIM(COALESCE(operatorNik, ''))) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(operatorName)) = LOWER(TRIM(:operatorIdentifier))
           OR (TRIM(:operatorIdentifier) != '' AND LOWER(TRIM(operatorName)) LIKE '%' || LOWER(TRIM(:operatorIdentifier)) || '%')
           OR LOWER(TRIM(createdBy)) = LOWER(TRIM(REPLACE(:operatorIdentifier, '@', '')))
        ORDER BY createdAt DESC, id DESC
    """)
    fun getRealisasiForOperator(operatorIdentifier: String): Flow<List<ActivityReportEntity>>

    @Query("""
        SELECT * FROM activity_reports 
        WHERE LOWER(TRIM(operatorName)) = LOWER(TRIM(:namaOperator))
           OR LOWER(TRIM(COALESCE(operatorNik, ''))) = LOWER(TRIM(:nikOperator))
           OR LOWER(TRIM(operatorName)) = LOWER(TRIM(:nikOperator))
           OR (TRIM(:namaOperator) != '' AND LOWER(TRIM(operatorName)) LIKE '%' || LOWER(TRIM(:namaOperator)) || '%')
           OR LOWER(TRIM(createdBy)) = LOWER(TRIM(REPLACE(:nikOperator, '@', '')))
        ORDER BY createdAt DESC, id DESC
    """)
    fun getRiwayatForOperator(namaOperator: String, nikOperator: String): Flow<List<ActivityReportEntity>>

    @Query("""
        UPDATE activity_reports 
        SET approvalStatus = 'Disetujui Kasie',
            approvedBy = :approvedBy,
            approvedAt = :approvedAt,
            isLocked = 1
        WHERE id = :id
    """)
    suspend fun approveReport(id: Long, approvedBy: String, approvedAt: String = System.currentTimeMillis().toString())

    @Query("""
        UPDATE activity_reports 
        SET approvalStatus = :status,
            isLocked = :isLocked,
            approvedBy = COALESCE(:approvedBy, approvedBy),
            approvedAt = COALESCE(:approvedAt, approvedAt)
        WHERE id = :id
    """)
    suspend fun updateApprovalStatus(
        id: Long,
        status: String = "Disetujui Kasie",
        isLocked: Boolean = true,
        approvedBy: String? = null,
        approvedAt: String? = null
    )




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

    @Query("SELECT * FROM activity_reports ORDER BY id DESC")
    fun getAllRealisasiFlow(): Flow<List<ActivityReportEntity>>

    @Query("SELECT COALESCE(SUM(hmDailyRunning), 0.0) FROM activity_reports")
    fun getTotalHmBerjalanFlow(): Flow<Double>

    @Query("SELECT COUNT(DISTINCT nomorSpk) FROM activity_reports")
    fun getTotalLaporanCountFlow(): Flow<Int>

    @Query("SELECT * FROM activity_reports WHERE clientUuid = :uuid LIMIT 1")
    suspend fun getReportByUuid(uuid: String): ActivityReportEntity?

    @Query("SELECT * FROM activity_reports WHERE nomorSpk = :nomorSpk AND nomorSpk != '' AND nomorSpk != '-' AND nomorSpk != 'SPK Menyusul' LIMIT 1")
    suspend fun getReportByNomorSpk(nomorSpk: String): ActivityReportEntity?

    @Query("SELECT * FROM activity_reports WHERE idRencanaKerja = :rencanaId AND idRencanaKerja > 0 LIMIT 1")
    suspend fun getReportByRencanaId(rencanaId: Long): ActivityReportEntity?

    @Query("SELECT * FROM activity_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): ActivityReportEntity?

    @Query("SELECT * FROM activity_reports WHERE supervisorName = :supervisor AND unitCode = :unitCode AND hmAkhir = :hmAkhir LIMIT 1")
    suspend fun findExistingReport(supervisor: String, unitCode: String, hmAkhir: Double): ActivityReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ActivityReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ActivityReportEntity>)

    @Update
    suspend fun updateReport(report: ActivityReportEntity)

    @Query("UPDATE activity_reports SET reportDate = SUBSTR(reportDate, 7, 4) || '-' || SUBSTR(reportDate, 4, 2) || '-' || SUBSTR(reportDate, 1, 2) WHERE reportDate LIKE '__/__/____'")
    suspend fun normalizeDatesInDatabase(): Int

    @Query("UPDATE activity_reports SET isSynced = 1, syncedAt = :timestamp WHERE id = :reportId")
    suspend fun markReportAsSynced(reportId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE activity_reports SET isSynced = 0 WHERE id = :reportId")
    suspend fun markReportAsUnsynced(reportId: Long)

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

