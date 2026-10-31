package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.MasterUnitEntity
import com.example.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

typealias UnitAlatBeratDao = UnitDao
typealias MasterUnitDao = UnitDao

@Dao
interface UnitDao {
    @Query("""
        SELECT * FROM units 
        WHERE (status = 'Aktif' OR status = 'OPERASIONAL' OR status = 'aktif' OR status = 'operasional' OR status != 'Nonaktif')
          AND (
              LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:namaKasie))
              OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:username))
              OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:username, '@', '')))
              OR LOWER(TRIM(REPLACE(supervisorAssigned, '@', ''))) = LOWER(TRIM(REPLACE(:username, '@', '')))
              OR LOWER(TRIM(REPLACE(supervisorAssigned, '@', ''))) = LOWER(TRIM(REPLACE(:namaKasie, '@', '')))
              OR (:namaKasie = 'Yono' AND LOWER(TRIM(supervisorAssigned)) = 'suyono')
          )
        ORDER BY unitCode ASC
    """)
    fun getUnitsForKasie(namaKasie: String, username: String): Flow<List<MasterUnitEntity>>

    @Query("""
        SELECT * FROM units 
        WHERE (
            LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:kasieName)) 
            OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:username))
            OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(REPLACE(supervisorAssigned, '@', ''))) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(REPLACE(supervisorAssigned, '@', ''))) = LOWER(TRIM(REPLACE(:kasieName, '@', '')))
            OR (:kasieName = 'Yono' AND LOWER(TRIM(supervisorAssigned)) = 'suyono')
        )
        AND (status = 'Aktif' OR status = 'OPERASIONAL' OR status = 'aktif' OR status = 'operasional' OR status != 'Nonaktif')
        ORDER BY unitCode ASC
    """)
    fun getUnitsForCurrentKasie(kasieName: String, username: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units ORDER BY unitCode ASC")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE supervisorAssigned = :supervisor ORDER BY unitCode ASC")
    fun getUnitsForSupervisor(supervisor: String): Flow<List<UnitEntity>>

    @Query("""
        SELECT * FROM units 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' 
               OR wilayah = :wilayah 
               OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
               OR REPLACE(wilayah, ' ', ' PG') = :wilayah
               OR wilayah = REPLACE(:wilayah, ' ', ' PG')
               OR wilayah = REPLACE(:wilayah, ' PG', ' '))
        ORDER BY unitCode ASC
    """)
    fun getUnitsByWilayah(wilayah: String?): Flow<List<UnitEntity>>

    @Query("""
        SELECT * FROM units 
        WHERE (:wilayah IS NOT NULL AND :wilayah != '' AND :wilayah != 'SEMUA' AND :wilayah != 'Semua Wilayah' 
               AND (wilayah = :wilayah 
                    OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
                    OR REPLACE(wilayah, ' ', ' PG') = :wilayah
                    OR wilayah = REPLACE(:wilayah, ' ', ' PG')
                    OR wilayah = REPLACE(:wilayah, ' PG', ' ')))
           OR ((:wilayah IS NULL OR :wilayah = '' OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah') AND (supervisorAssigned = :supervisor OR :supervisor = '' OR :supervisor = 'ALL'))
        ORDER BY unitCode ASC
    """)
    fun getUnitsForSupervisorOrWilayah(supervisor: String, wilayah: String?): Flow<List<UnitEntity>>

    @Query("""
        SELECT * FROM units 
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
            (:unitType LIKE '%DUMP%' AND unitType LIKE '%TRUCK%') OR
            unitType = :unitType OR
            unitType LIKE '%' || :unitType || '%'
          )
          AND (:unitCode IS NULL OR :unitCode = 'Semua Unit' OR :unitCode = '' OR unitCode = :unitCode)
        ORDER BY unitCode ASC
    """)
    fun getUnitsByFilter(wilayah: String?, unitType: String?, unitCode: String?): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE unitCode = :unitCode LIMIT 1")
    suspend fun getUnitByCode(unitCode: String): UnitEntity?

    @Query("SELECT COUNT(*) FROM units")
    fun getUnitCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity)

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Query("UPDATE units SET lastHm = :newLastHm, updatedAt = :timestamp WHERE unitCode = :unitCode")
    suspend fun updateUnitHm(unitCode: String, newLastHm: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM units WHERE unitCode = :unitCode")
    suspend fun deleteUnit(unitCode: String)

    @Query("DELETE FROM units")
    suspend fun clearAllUnits()

    @Query("DELETE FROM units")
    suspend fun deleteAll()

    @Query("UPDATE units SET wilayah = 'PG1' WHERE wilayah = 'Geodesi PG1' OR wilayah = 'Geodesi'")
    suspend fun normalizeGeodesiPg1(): Int

    @Transaction
    suspend fun replaceAll(listBaru: List<UnitEntity>) {
        deleteAll()
        insertUnits(listBaru)
    }

    @Query("SELECT COUNT(*) FROM units")
    suspend fun deleteDumpTruckAndBulldozer(): Int
}
