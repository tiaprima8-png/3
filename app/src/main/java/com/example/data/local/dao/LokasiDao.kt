package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.LokasiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LokasiDao {
    @Query("SELECT * FROM lokasi ORDER BY kode_lokasi ASC")
    fun getAllLokasi(): Flow<List<LokasiEntity>>

    @Query("SELECT * FROM lokasi WHERE kode_lokasi = :kode LIMIT 1")
    suspend fun getLokasiByKode(kode: String): LokasiEntity?

    @Query("""
        SELECT * FROM lokasi 
        WHERE LOWER(TRIM(status)) = 'aktif'
          AND (
              :wilayahFilter IS NULL 
              OR TRIM(:wilayahFilter) = '' 
              OR LOWER(TRIM(:wilayahFilter)) = 'semua wilayah'
              OR UPPER(TRIM(wilayah)) = UPPER(TRIM(:wilayahFilter))
              OR UPPER(TRIM(wilayah)) = UPPER(TRIM(REPLACE(REPLACE(REPLACE(:wilayahFilter, 'Geodesi ', ''), 'GEODESI ', ''), 'RC ', '')))
              OR UPPER(TRIM(wilayah)) = UPPER(TRIM(REPLACE(REPLACE(:wilayahFilter, 'Lebung ', ''), 'LEBUNG ', '')))
              OR UPPER(TRIM(:wilayahFilter)) LIKE '%' || UPPER(TRIM(wilayah)) || '%'
              OR UPPER(TRIM(wilayah)) LIKE '%' || UPPER(TRIM(:wilayahFilter)) || '%'
              OR (UPPER(:wilayahFilter) LIKE '%PG12%' AND UPPER(TRIM(wilayah)) IN ('PG1', 'PG2', 'PG12', 'LEBUNG PG12', 'RC PG1', 'RC PG2', 'GEODESI PG1', 'GEODESI PG2'))
          )
          AND (
              :query IS NULL 
              OR TRIM(:query) = '' 
              OR LOWER(kode_lokasi) LIKE '%' || LOWER(TRIM(:query)) || '%'
          )
        ORDER BY kode_lokasi ASC
    """)
    fun getAllLokasiFiltered(wilayahFilter: String?, query: String?): Flow<List<LokasiEntity>>

    fun searchLokasiFast(wilayahFilter: String?, query: String?): Flow<List<LokasiEntity>> =
        getAllLokasiFiltered(wilayahFilter, query)

    @Query("""
        SELECT * FROM lokasi 
        WHERE kode_lokasi LIKE '%' || :query || '%' 
           OR wilayah LIKE '%' || :query || '%'
        ORDER BY kode_lokasi ASC
    """)
    fun searchLokasi(query: String): Flow<List<LokasiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLokasi(lokasi: LokasiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLokasiList(lokasiList: List<LokasiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lokasiList: List<LokasiEntity>)

    @Query("DELETE FROM lokasi")
    suspend fun clearAll()

    @Query("DELETE FROM lokasi")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(listBaru: List<LokasiEntity>) {
        deleteAll()
        insertLokasiList(listBaru)
    }
}
