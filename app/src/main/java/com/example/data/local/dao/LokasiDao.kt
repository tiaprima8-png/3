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
