package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.OperatorEntity
import kotlinx.coroutines.flow.Flow

typealias MasterOperatorDao = OperatorDao

@Dao
interface OperatorDao {
    @Query("""
        SELECT * FROM operators 
        WHERE (status = 'Aktif' OR status = 'AKTIF' OR status = 'operasional' OR status != 'Nonaktif')
          AND (
              LOWER(TRIM(COALESCE(kasiePenanggungJawab, supervisorAssigned))) = LOWER(TRIM(:kasieName))
              OR LOWER(TRIM(COALESCE(kasiePenanggungJawab, supervisorAssigned))) = LOWER(TRIM(:username))
              OR LOWER(TRIM(COALESCE(kasiePenanggungJawab, supervisorAssigned))) = LOWER(TRIM(REPLACE(:username, '@', '')))
              OR LOWER(TRIM(REPLACE(COALESCE(kasiePenanggungJawab, supervisorAssigned), '@', ''))) = LOWER(TRIM(REPLACE(:username, '@', '')))
              OR LOWER(TRIM(REPLACE(COALESCE(kasiePenanggungJawab, supervisorAssigned), '@', ''))) = LOWER(TRIM(REPLACE(:kasieName, '@', '')))
              OR (
                  (kasiePenanggungJawab IS NULL OR kasiePenanggungJawab = '' OR supervisorAssigned = '' OR supervisorAssigned = 'ALL')
                  AND :wilayahKasie != '' 
                  AND (
                      LOWER(TRIM(wilayah)) = LOWER(TRIM(:wilayahKasie))
                      OR LOWER(TRIM(REPLACE(wilayah, ' PG', ' '))) = LOWER(TRIM(REPLACE(:wilayahKasie, ' PG', ' ')))
                      OR LOWER(TRIM(REPLACE(wilayah, ' ', ' PG'))) = LOWER(TRIM(REPLACE(:wilayahKasie, ' ', ' PG')))
                  )
              )
          )
        ORDER BY name ASC
    """)
    fun getOperatorsForKasie(
        kasieName: String, 
        username: String, 
        wilayahKasie: String
    ): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators ORDER BY name ASC")
    fun getAllOperators(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators WHERE LOWER(TRIM(status)) = 'aktif' OR LOWER(TRIM(status)) = 'active' ORDER BY name ASC")
    suspend fun getAllActiveOperatorsList(): List<OperatorEntity>

    @Query("""
        SELECT * FROM operators
        WHERE (LOWER(TRIM(status)) = 'aktif' OR LOWER(TRIM(status)) = 'active' OR status != 'Nonaktif')
          AND (
              LOWER(TRIM(badgeNumber)) = LOWER(TRIM(:nikOrName))
              OR LOWER(TRIM(operatorId)) = LOWER(TRIM(:nikOrName))
              OR LOWER(TRIM(REPLACE(badgeNumber, 'NIK-', ''))) = LOWER(TRIM(:nikOrName))
              OR LOWER(TRIM(REPLACE(operatorId, 'OPR-', ''))) = LOWER(TRIM(:nikOrName))
              OR LOWER(TRIM(name)) = LOWER(TRIM(:nikOrName))
          )
        LIMIT 1
    """)
    suspend fun findActiveOperatorByNikOrName(nikOrName: String): OperatorEntity?

    @Query("SELECT * FROM operators WHERE supervisorAssigned = :supervisor ORDER BY name ASC")
    fun getOperatorsForSupervisor(supervisor: String): Flow<List<OperatorEntity>>

    @Query("""
        SELECT * FROM operators 
        WHERE (:wilayah IS NOT NULL AND :wilayah != '' AND :wilayah != 'SEMUA' AND :wilayah != 'Semua Wilayah' 
               AND (wilayah = :wilayah 
                    OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
                    OR REPLACE(wilayah, ' ', ' PG') = :wilayah
                    OR wilayah = REPLACE(:wilayah, ' ', ' PG')
                    OR wilayah = REPLACE(:wilayah, ' PG', ' ')))
           OR ((:wilayah IS NULL OR :wilayah = '' OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah') AND (supervisorAssigned = :supervisor OR :supervisor = '' OR :supervisor = 'ALL'))
        ORDER BY name ASC
    """)
    fun getOperatorsForSupervisorOrWilayah(supervisor: String, wilayah: String?): Flow<List<OperatorEntity>>

    @Query("""
        SELECT * FROM operators 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' 
               OR wilayah = :wilayah 
               OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
               OR REPLACE(wilayah, ' ', ' PG') = :wilayah
               OR wilayah = REPLACE(:wilayah, ' ', ' PG')
               OR wilayah = REPLACE(:wilayah, ' PG', ' '))
        ORDER BY name ASC
    """)
    fun getOperatorsFiltered(wilayah: String?): Flow<List<OperatorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperators(operators: List<OperatorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(operators: List<OperatorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: OperatorEntity)

    @Update
    suspend fun updateOperator(operator: OperatorEntity)

    @Query("DELETE FROM operators WHERE operatorId = :operatorId")
    suspend fun deleteOperator(operatorId: String)

    @Query("DELETE FROM operators")
    suspend fun clearAllOperators()

    @Query("DELETE FROM operators")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(listBaru: List<OperatorEntity>) {
        deleteAll()
        insertOperators(listBaru)
    }
}
