package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AktivitasUnitEntity
import kotlinx.coroutines.flow.Flow

data class AnalyticsAggregationSummary(
    val totalJamOperasi: Double = 0.0,
    val totalJamStandby: Double = 0.0,
    val totalJamBreakdown: Double = 0.0,
    val totalHm: Double = 0.0,
    val totalSolar: Double = 0.0
)

@Dao
interface AktivitasUnitDao {
    @Query("""
        SELECT 
          SUM(CASE WHEN kategori_status = 'OPERASI' THEN durasi_jam_kerja ELSE 0 END) AS totalJamOperasi,
          SUM(CASE WHEN kategori_status = 'STANDBY' THEN durasi_jam_kerja ELSE 0 END) AS totalJamStandby,
          SUM(CASE WHEN kategori_status = 'BREAKDOWN' THEN durasi_jam_kerja ELSE 0 END) AS totalJamBreakdown,
          SUM(durasi_hm) AS totalHm,
          SUM(pengisian_solar) AS totalSolar
        FROM aktivitas_unit
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' OR wilayah = :wilayah)
          AND tanggal BETWEEN :startDate AND :endDate
    """)
    fun getSummaryBetweenDates(startDate: String, endDate: String, wilayah: String? = null): Flow<AnalyticsAggregationSummary?>

    @Query("""
        SELECT 
          SUM(CASE WHEN kategori_status = 'OPERASI' THEN durasi_jam_kerja ELSE 0 END) AS totalJamOperasi,
          SUM(CASE WHEN kategori_status = 'STANDBY' THEN durasi_jam_kerja ELSE 0 END) AS totalJamStandby,
          SUM(CASE WHEN kategori_status = 'BREAKDOWN' THEN durasi_jam_kerja ELSE 0 END) AS totalJamBreakdown,
          SUM(durasi_hm) AS totalHm,
          SUM(pengisian_solar) AS totalSolar
        FROM aktivitas_unit
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' OR wilayah = :wilayah)
    """)
    fun getAllSummary(wilayah: String? = null): Flow<AnalyticsAggregationSummary?>

    @Query("""
        SELECT * FROM aktivitas_unit 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' OR wilayah = :wilayah)
        ORDER BY created_at DESC
    """)
    fun getAllAktivitas(wilayah: String? = null): Flow<List<AktivitasUnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AktivitasUnitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AktivitasUnitEntity>)

    @Query("DELETE FROM aktivitas_unit")
    suspend fun clearAll()
}
