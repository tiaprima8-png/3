package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.MasterLokasiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LokasiDao {
    @Query("""
        SELECT * FROM lokasi 
        WHERE status = 'Aktif'
          AND (
              :wilayah IS NULL 
              OR :wilayah = '' 
              OR :wilayah = 'Semua' 
              OR UPPER(TRIM(wilayah)) = UPPER(TRIM(:wilayah))
              OR (:wilayah = 'PG1' AND (UPPER(wilayah) LIKE '%PG1' OR UPPER(wilayah) LIKE '%PG1 %' OR UPPER(wilayah) = 'PG1') AND UPPER(wilayah) NOT LIKE '%PG12%')
              OR (:wilayah = 'PG2' AND (UPPER(wilayah) LIKE '%PG2' OR UPPER(wilayah) LIKE '%PG2 %' OR UPPER(wilayah) = 'PG2') AND UPPER(wilayah) NOT LIKE '%PG12%')
              OR (:wilayah = 'OTHER' AND ((UPPER(wilayah) NOT LIKE '%PG1' AND UPPER(wilayah) NOT LIKE '%PG1 %' AND UPPER(wilayah) NOT LIKE '%PG2' AND UPPER(wilayah) NOT LIKE '%PG2 %') OR UPPER(wilayah) LIKE '%OTHER%' OR UPPER(wilayah) LIKE '%PG12%' OR UPPER(wilayah) LIKE '%PG3%'))
              OR UPPER(TRIM(wilayah)) LIKE '%' || UPPER(TRIM(:wilayah)) || '%'
          )
          AND (
              :searchQuery IS NULL 
              OR :searchQuery = '' 
              OR LOWER(kode_lokasi) LIKE '%' || LOWER(:searchQuery) || '%'
          )
        ORDER BY kode_lokasi ASC
    """)
    fun getAllActiveLokasi(searchQuery: String?, wilayah: String?): Flow<List<MasterLokasiEntity>>

    @Query("SELECT COUNT(*) FROM lokasi WHERE status = 'Aktif'")
    suspend fun getActiveLokasiCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lokasiList: List<MasterLokasiEntity>)

    @Query("DELETE FROM lokasi")
    suspend fun deleteAll()

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
              OR LOWER(TRIM(:wilayahFilter)) = 'semua' 
              OR LOWER(TRIM(:wilayahFilter)) = 'semua wilayah'
              OR UPPER(TRIM(wilayah)) = UPPER(TRIM(:wilayahFilter))
              OR (:wilayahFilter = 'PG1' AND (UPPER(wilayah) LIKE '%PG1' OR UPPER(wilayah) LIKE '%PG1 %') AND UPPER(wilayah) NOT LIKE '%PG12%')
              OR (:wilayahFilter = 'PG2' AND (UPPER(wilayah) LIKE '%PG2' OR UPPER(wilayah) LIKE '%PG2 %') AND UPPER(wilayah) NOT LIKE '%PG12%')
              OR (:wilayahFilter = 'OTHER' AND (UPPER(wilayah) NOT LIKE '%PG1' AND UPPER(wilayah) NOT LIKE '%PG2%'))
              OR UPPER(TRIM(wilayah)) LIKE '%' || UPPER(TRIM(:wilayahFilter)) || '%'
              OR UPPER(TRIM(:wilayahFilter)) LIKE '%' || UPPER(TRIM(wilayah)) || '%'
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

    @Query("DELETE FROM lokasi")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(listBaru: List<LokasiEntity>) {
        deleteAll()
        insertLokasiList(listBaru)
    }
}
