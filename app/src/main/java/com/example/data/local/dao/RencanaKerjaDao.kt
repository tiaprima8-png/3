package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.RencanaKerjaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RencanaKerjaDao {
    @Query("SELECT * FROM rencana_kerja ORDER BY created_at DESC")
    fun getAllRencana(): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE LOWER(TRIM(supervisor_name)) = LOWER(TRIM(:supervisor)) 
           OR LOWER(TRIM(pengawas)) = LOWER(TRIM(:supervisor))
           OR LOWER(TRIM(kasie)) = LOWER(TRIM(:supervisor))
           OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:supervisor, '@', '')))
        ORDER BY created_at DESC, id DESC
    """)
    fun getRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja rk
        WHERE (
            LOWER(TRIM(rk.operator)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(rk.nama_operator)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(rk.operator_nik)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(rk.created_by)) = LOWER(TRIM(REPLACE(:operatorIdentifier, '@', '')))
        )
        AND rk.is_realized = 0
        AND UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED')
        AND UPPER(TRIM(rk.status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED', 'TEREALISASI (MENUNGGU APPROVAL)')
        AND UPPER(TRIM(rk.status_tahap)) != 'REALISASI_SELESAI'
        AND NOT EXISTS (
            SELECT 1 FROM hasil_input_aktivitas h
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND h.nomor_spk = rk.nomor_spk
              AND h.hm_akhir > 0
              AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
        )
        AND NOT EXISTS (
            SELECT 1 FROM activity_reports r
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND r.nomorSpk = rk.nomor_spk
              AND r.hmAkhir > 0
              AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
        )
        ORDER BY 
            CASE 
                WHEN rk.tanggal_format LIKE '__/__/____' THEN
                    SUBSTR(rk.tanggal_format, 7, 4) || '-' || SUBSTR(rk.tanggal_format, 4, 2) || '-' || SUBSTR(rk.tanggal_format, 1, 2)
                WHEN rk.tanggal_format LIKE '____-__-__' THEN rk.tanggal_format
                ELSE CAST(rk.created_at AS TEXT)
            END ASC,
            CASE UPPER(rk.shift_kerja) WHEN 'SIANG' THEN 1 ELSE 2 END ASC,
            rk.sub_index ASC,
            rk.id ASC
    """)
    fun getRencanaForOperator(operatorIdentifier: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja rk
        WHERE (
            LOWER(TRIM(rk.operator)) = LOWER(TRIM(:namaOperator))
            OR LOWER(TRIM(rk.nama_operator)) = LOWER(TRIM(:namaOperator))
            OR LOWER(TRIM(rk.operator_nik)) = LOWER(TRIM(:nikOperator))
            OR LOWER(TRIM(rk.operator)) = LOWER(TRIM(:nikOperator))
            OR (TRIM(:namaOperator) != '' AND LOWER(TRIM(rk.operator)) LIKE '%' || LOWER(TRIM(:namaOperator)) || '%')
            OR LOWER(TRIM(rk.created_by)) = LOWER(TRIM(REPLACE(:nikOperator, '@', '')))
        )
        AND rk.is_realized = 0
        AND UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED')
        AND UPPER(TRIM(rk.status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED', 'TEREALISASI (MENUNGGU APPROVAL)')
        AND UPPER(TRIM(rk.status_tahap)) != 'REALISASI_SELESAI'
        AND NOT EXISTS (
            SELECT 1 FROM hasil_input_aktivitas h
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND h.nomor_spk = rk.nomor_spk
              AND h.hm_akhir > 0
              AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
        )
        AND NOT EXISTS (
            SELECT 1 FROM activity_reports r
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND r.nomorSpk = rk.nomor_spk
              AND r.hmAkhir > 0
              AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
        )
        ORDER BY 
            CASE 
                WHEN rk.tanggal_format LIKE '__/__/____' THEN
                    SUBSTR(rk.tanggal_format, 7, 4) || '-' || SUBSTR(rk.tanggal_format, 4, 2) || '-' || SUBSTR(rk.tanggal_format, 1, 2)
                WHEN rk.tanggal_format LIKE '____-__-__' THEN rk.tanggal_format
                ELSE CAST(rk.created_at AS TEXT)
            END ASC,
            CASE UPPER(rk.shift_kerja) WHEN 'SIANG' THEN 1 ELSE 2 END ASC,
            rk.sub_index ASC,
            rk.id ASC
    """)
    fun getPendingTugasForOperator(namaOperator: String, nikOperator: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (
            LOWER(TRIM(operator)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(nama_operator)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(operator_nik)) = LOWER(TRIM(:operatorIdentifier))
            OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:operatorIdentifier, '@', '')))
        )
        ORDER BY created_at DESC, id DESC
    """)
    fun getAllRencanaForOperator(operatorIdentifier: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (
            LOWER(TRIM(operator)) = LOWER(TRIM(:namaOperator))
            OR LOWER(TRIM(nama_operator)) = LOWER(TRIM(:namaOperator))
            OR LOWER(TRIM(operator_nik)) = LOWER(TRIM(:nikOperator))
            OR LOWER(TRIM(operator)) = LOWER(TRIM(:nikOperator))
            OR (TRIM(:namaOperator) != '' AND LOWER(TRIM(operator)) LIKE '%' || LOWER(TRIM(:namaOperator)) || '%')
            OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:nikOperator, '@', '')))
        )
        ORDER BY created_at DESC, id DESC
    """)
    fun getAllTugasForOperator(namaOperator: String, nikOperator: String): Flow<List<RencanaKerjaEntity>>


    @Query("""
        SELECT * FROM rencana_kerja rk
        WHERE (
            LOWER(TRIM(rk.kasie)) = LOWER(TRIM(:namaKasie)) 
            OR LOWER(TRIM(rk.supervisor_name)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(rk.pengawas)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(rk.created_by)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(rk.supervisor_name)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(rk.kasie)) = LOWER(TRIM(REPLACE(:username, '@', '')))
        )
        AND (
            NOT EXISTS (SELECT 1 FROM units)
            OR rk.kode_unit IN (
                SELECT unitCode FROM units 
                WHERE LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:namaKasie))
                   OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            )
        )
        AND rk.is_realized = 0 
        AND UPPER(TRIM(COALESCE(rk.status_realisasi, ''))) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED', 'TEREALISASI (MENUNGGU APPROVAL)', 'DISETUJUI KASIE', 'APPROVED')
        AND rk.status_tahap != 'REALISASI_SELESAI' 
        AND UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED')
        AND NOT EXISTS (
            SELECT 1 FROM hasil_input_aktivitas h
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND h.nomor_spk = rk.nomor_spk
              AND h.hm_akhir > 0
              AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
        )
        AND NOT EXISTS (
            SELECT 1 FROM activity_reports r
            WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
              AND r.nomorSpk = rk.nomor_spk
              AND r.hmAkhir > 0
              AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
        )
        ORDER BY 
            CASE 
                WHEN rk.tanggal_format LIKE '__/__/____' THEN
                    SUBSTR(rk.tanggal_format, 7, 4) || '-' || SUBSTR(rk.tanggal_format, 4, 2) || '-' || SUBSTR(rk.tanggal_format, 1, 2)
                WHEN rk.tanggal_format LIKE '____-__-__' THEN rk.tanggal_format
                ELSE CAST(rk.created_at AS TEXT)
            END ASC,
            CASE UPPER(rk.shift_kerja) WHEN 'SIANG' THEN 1 ELSE 2 END ASC,
            rk.sub_index ASC,
            rk.id ASC
    """)
    fun getRencanaKerjaForKasie(namaKasie: String, username: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (
            LOWER(TRIM(kasie)) = LOWER(TRIM(:namaKasie)) 
            OR LOWER(TRIM(supervisor_name)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(pengawas)) = LOWER(TRIM(:namaKasie))
            OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(supervisor_name)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            OR LOWER(TRIM(kasie)) = LOWER(TRIM(REPLACE(:username, '@', '')))
        )
        AND (
            NOT EXISTS (SELECT 1 FROM units)
            OR kode_unit IN (
                SELECT unitCode FROM units 
                WHERE LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:namaKasie))
                   OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:username, '@', '')))
            )
        )
        ORDER BY created_at DESC, id DESC
    """)
    fun getAllRencanaKerjaForKasie(namaKasie: String, username: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' 
               OR wilayah = :wilayah 
               OR REPLACE(wilayah, ' PG', ' ') = :wilayah 
               OR REPLACE(wilayah, ' ', ' PG') = :wilayah
               OR wilayah = REPLACE(:wilayah, ' ', ' PG')
               OR wilayah = REPLACE(:wilayah, ' PG', ' '))
          AND (:kodeUnit IS NULL OR :kodeUnit = 'Semua Unit' OR :kodeUnit = '' OR kode_unit = :kodeUnit)
        ORDER BY created_at DESC
    """)
    fun getRencanaFiltered(wilayah: String?, kodeUnit: String?): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja ORDER BY created_at DESC")
    suspend fun getAllRencanaList(): List<RencanaKerjaEntity>

    @Query("DELETE FROM rencana_kerja")
    suspend fun deleteAll(): Int

    @Query("""
        SELECT * FROM rencana_kerja rk
        WHERE (
            :supervisor = 'Semua' 
            OR LOWER(TRIM(rk.supervisor_name)) = LOWER(TRIM(:supervisor)) 
            OR LOWER(TRIM(rk.pengawas)) = LOWER(TRIM(:supervisor))
            OR LOWER(TRIM(rk.kasie)) = LOWER(TRIM(:supervisor))
            OR LOWER(TRIM(rk.created_by)) = LOWER(TRIM(REPLACE(:supervisor, '@', '')))
        )
          AND (
            :supervisor = 'Semua'
            OR NOT EXISTS (SELECT 1 FROM units)
            OR rk.kode_unit IN (
                SELECT unitCode FROM units 
                WHERE LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:supervisor))
                   OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:supervisor, '@', '')))
            )
          )
          AND rk.is_realized = 0 
          AND UPPER(TRIM(COALESCE(rk.status_realisasi, ''))) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED', 'TEREALISASI (MENUNGGU APPROVAL)', 'DISETUJUI KASIE', 'APPROVED')
          AND rk.status_tahap != 'REALISASI_SELESAI' 
          AND UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED')
          AND NOT EXISTS (
              SELECT 1 FROM hasil_input_aktivitas h
              WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
                AND h.nomor_spk = rk.nomor_spk
                AND h.hm_akhir > 0
                AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
          )
          AND NOT EXISTS (
              SELECT 1 FROM activity_reports r
              WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
                AND r.nomorSpk = rk.nomor_spk
                AND r.hmAkhir > 0
                AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
          )
        ORDER BY 
            CASE 
                WHEN rk.tanggal_format LIKE '__/__/____' THEN
                    SUBSTR(rk.tanggal_format, 7, 4) || '-' || SUBSTR(rk.tanggal_format, 4, 2) || '-' || SUBSTR(rk.tanggal_format, 1, 2)
                WHEN rk.tanggal_format LIKE '____-__-__' THEN rk.tanggal_format
                ELSE CAST(rk.created_at AS TEXT)
            END ASC,
            CASE UPPER(rk.shift_kerja) WHEN 'SIANG' THEN 1 ELSE 2 END ASC,
            rk.sub_index ASC,
            rk.id ASC
    """)
    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        SELECT * FROM rencana_kerja rk
        WHERE (
            :pengawas = 'Semua' 
            OR LOWER(TRIM(rk.supervisor_name)) = LOWER(TRIM(:pengawas)) 
            OR LOWER(TRIM(rk.pengawas)) = LOWER(TRIM(:pengawas))
            OR LOWER(TRIM(rk.kasie)) = LOWER(TRIM(:pengawas))
            OR LOWER(TRIM(rk.created_by)) = LOWER(TRIM(REPLACE(:pengawas, '@', '')))
        )
          AND (
            :pengawas = 'Semua'
            OR NOT EXISTS (SELECT 1 FROM units)
            OR rk.kode_unit IN (
                SELECT unitCode FROM units 
                WHERE LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(:pengawas))
                   OR LOWER(TRIM(supervisorAssigned)) = LOWER(TRIM(REPLACE(:pengawas, '@', '')))
            )
          )
          AND rk.is_realized = 0 
          AND UPPER(TRIM(COALESCE(rk.status_realisasi, ''))) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED', 'TEREALISASI (MENUNGGU APPROVAL)', 'DISETUJUI KASIE', 'APPROVED')
          AND rk.status_tahap != 'REALISASI_SELESAI' 
          AND UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED')
          AND NOT EXISTS (
              SELECT 1 FROM hasil_input_aktivitas h
              WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
                AND h.nomor_spk = rk.nomor_spk
                AND h.hm_akhir > 0
                AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
          )
          AND NOT EXISTS (
              SELECT 1 FROM activity_reports r
              WHERE rk.nomor_spk != '' AND rk.nomor_spk != 'SPK Menyusul' AND rk.nomor_spk != '-'
                AND r.nomorSpk = rk.nomor_spk
                AND r.hmAkhir > 0
                AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
          )
        ORDER BY 
            CASE 
                WHEN rk.tanggal_format LIKE '__/__/____' THEN
                    SUBSTR(rk.tanggal_format, 7, 4) || '-' || SUBSTR(rk.tanggal_format, 4, 2) || '-' || SUBSTR(rk.tanggal_format, 1, 2)
                WHEN rk.tanggal_format LIKE '____-__-__' THEN rk.tanggal_format
                ELSE CAST(rk.created_at AS TEXT)
            END ASC,
            CASE UPPER(rk.shift_kerja) WHEN 'SIANG' THEN 1 ELSE 2 END ASC,
            rk.sub_index ASC,
            rk.id ASC
    """)
    fun getRencanaKerjaBelumRealisasi(pengawas: String): Flow<List<RencanaKerjaEntity>>

    @Query("""
        UPDATE rencana_kerja
        SET is_realized = 0,
            status_realisasi = 'Menunggu Realisasi',
            status_tahap = 'RENCANA',
            status_spk = CASE WHEN nomor_spk != '' AND nomor_spk != 'SPK Menyusul' AND nomor_spk != '-' THEN 'TERBIT_SPK' ELSE 'MENUNGGU_SPK' END
        WHERE is_realized = 1
          AND NOT EXISTS (
              SELECT 1 FROM activity_reports r
              WHERE r.hmAkhir > 0
                AND (r.hmDailyRunning > 0 OR r.hmAkhir > r.hmAwal OR r.workOutput > 0)
                AND (
                    (rencana_kerja.nomor_spk != '' AND rencana_kerja.nomor_spk != 'SPK Menyusul' AND rencana_kerja.nomor_spk != '-' AND r.nomorSpk = rencana_kerja.nomor_spk)
                    OR (rencana_kerja.nomor_spk = '' AND r.idRencanaKerja = rencana_kerja.id)
                )
          )
          AND NOT EXISTS (
              SELECT 1 FROM hasil_input_aktivitas h
              WHERE h.hm_akhir > 0
                AND (h.hm_harian_berjalan > 0 OR h.hm_akhir > h.hm_awal OR h.hasil_kerja > 0)
                AND (
                    (rencana_kerja.nomor_spk != '' AND rencana_kerja.nomor_spk != 'SPK Menyusul' AND rencana_kerja.nomor_spk != '-' AND h.nomor_spk = rencana_kerja.nomor_spk)
                    OR (rencana_kerja.nomor_spk = '' AND h.rencana_id = rencana_kerja.id)
                )
          )
    """)
    suspend fun restoreUnrealizedPlansWithNoValidReport(): Int

    @Query("SELECT * FROM rencana_kerja WHERE id = :id LIMIT 1")
    suspend fun getRencanaById(id: Long): RencanaKerjaEntity?

    @Query("SELECT * FROM rencana_kerja WHERE (supervisor_name = :supervisor OR pengawas = :supervisor) AND kode_unit = :kodeUnit AND tanggal_format = :tanggal LIMIT 1")
    suspend fun findRencana(supervisor: String, kodeUnit: String, tanggal: String): RencanaKerjaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRencana(rencana: RencanaKerjaEntity): Long

    @Query("UPDATE rencana_kerja SET tanggal_format = '2026-10-28' WHERE tanggal_format = '28/10/2026'")
    suspend fun normalizeLegacyDate28(): Int

    @Query("UPDATE rencana_kerja SET tanggal_format = SUBSTR(tanggal_format, 7, 4) || '-' || SUBSTR(tanggal_format, 4, 2) || '-' || SUBSTR(tanggal_format, 1, 2) WHERE tanggal_format LIKE '__/__/____'")
    suspend fun normalizeDatesInDatabase(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRencanaList(list: List<RencanaKerjaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<RencanaKerjaEntity>): List<Long>

    @Transaction
    suspend fun replaceAll(listBaru: List<RencanaKerjaEntity>) {
        deleteAll()
        insertAll(listBaru)
    }

    @Transaction
    suspend fun replaceForSupervisor(namaPengawas: String, listBaru: List<RencanaKerjaEntity>) {
        clearActiveRencanaForSupervisor(namaPengawas)
        insertAll(listBaru)
    }

    @Update
    suspend fun updateRencana(rencana: RencanaKerjaEntity)

    @Query("UPDATE rencana_kerja SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE rencana_kerja SET is_realized = 1, status_tahap = 'REALISASI_SELESAI', status_spk = 'SELESAI', status_realisasi = 'SUDAH' WHERE id = :id")
    suspend fun markAsRealized(id: Long)

    // DILARANG KERAS update berdasarkan kodeUnit atau tanggal!
    // WAJIB update HANYA berdasarkan ID unik rencana yang dikerjakan:
    @Query("UPDATE rencana_kerja SET status_realisasi = 'Terealisasi', status_spk = 'Terealisasi', status_tahap = 'REALISASI_SELESAI', is_realized = 1 WHERE id = :rencanaId")
    suspend fun markTaskAsCompleted(rencanaId: Long)

    @Query("UPDATE rencana_kerja SET status_realisasi = :status, is_realized = :isRealized, status_tahap = :statusTahap WHERE id = :rencanaId")
    suspend fun updateStatusById(rencanaId: Long, status: String = "SUDAH", isRealized: Boolean = true, statusTahap: String = "REALISASI_SELESAI")

    @Query("""
        UPDATE rencana_kerja 
        SET status_spk = :status,
            status_realisasi = :status,
            is_realized = 1,
            status_tahap = 'REALISASI_SELESAI'
        WHERE (id = :id AND :id > 0)
           OR (:nomorSpk != '' AND :nomorSpk != 'SPK Menyusul' AND :nomorSpk != '-' AND nomor_spk = :nomorSpk AND :id <= 0)
    """)
    suspend fun updateStatus(nomorSpk: String, id: Long, status: String)

    suspend fun updateStatus(nomorSpk: String?, id: Long?, status: String = "Terealisasi & Approved") {
        updateStatus(nomorSpk = nomorSpk ?: "", id = id ?: 0L, status = status)
    }

    @Query("""
        UPDATE rencana_kerja
        SET status_spk = :status,
            status_realisasi = :status,
            is_realized = 1
        WHERE nomor_spk = :spkId AND :spkId != '' AND :spkId != '-' AND :spkId != 'SPK Menyusul'
    """)
    suspend fun updateStatusBySpkId(spkId: String, status: String = "Approved")

    suspend fun updateStatus(spkId: String, status: String) {
        updateStatusBySpkId(spkId, status)
    }


    @Query("UPDATE rencana_kerja SET wilayah = 'PG1' WHERE wilayah = 'Geodesi PG1' OR wilayah = 'Geodesi'")
    suspend fun normalizeGeodesiPg1(): Int

    @Query("""
        UPDATE rencana_kerja
        SET is_realized = 1,
            status_tahap = 'REALISASI_SELESAI',
            status_spk = 'Terealisasi & Approved',
            status_realisasi = 'Terealisasi & Approved'
        WHERE (id = :id AND :id > 0)
           OR (:nomorSpk != '' AND :nomorSpk != 'SPK Menyusul' AND :nomorSpk != '-' AND nomor_spk = :nomorSpk AND :id <= 0)
    """)
    suspend fun markAsRealizedAndApprovedInternal(
        id: Long,
        nomorSpk: String
    )

    suspend fun markAsRealizedAndApproved(
        id: Long,
        nomorSpk: String,
        kodeUnit: String = "",
        tanggalFormat: String = "",
        shiftKerja: String = ""
    ) {
        markAsRealizedAndApprovedInternal(id = id, nomorSpk = nomorSpk)
    }

    @Query("""
        UPDATE rencana_kerja
        SET is_realized = 1,
            status_tahap = 'REALISASI_SELESAI',
            status_spk = 'Terealisasi',
            status_realisasi = 'Terealisasi'
        WHERE (id = :id AND :id > 0)
           OR (:nomorSpk != '' AND :nomorSpk != 'SPK Menyusul' AND :nomorSpk != '-' AND nomor_spk = :nomorSpk AND :id <= 0)
    """)
    suspend fun markAsRealizedByDetailsInternal(
        id: Long,
        nomorSpk: String
    )

    suspend fun markAsRealizedByDetails(
        id: Long,
        nomorSpk: String,
        kodeUnit: String = "",
        tanggalFormat: String = "",
        shiftKerja: String = ""
    ) {
        markAsRealizedByDetailsInternal(id = id, nomorSpk = nomorSpk)
    }

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE id = :id")
    suspend fun updateSpk(id: Long, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit")
    suspend fun updateSpkByUnit(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit AND (status_realisasi = 'BELUM' OR is_realized = 0)")
    suspend fun updateSpkForActiveUnit(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE kode_unit = :kodeUnit")
    suspend fun updateSpkStatus(kodeUnit: String, spk: String, status: String)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE id = :id OR kode_unit = :kodeUnit")
    suspend fun updateSpkByIdOrUnit(id: Long, kodeUnit: String, spk: String, status: String)

    @Query("SELECT * FROM rencana_kerja WHERE kode_unit = :kodeUnit LIMIT 1")
    suspend fun getRencanaByUnit(kodeUnit: String): RencanaKerjaEntity?

    @Query("SELECT * FROM rencana_kerja WHERE is_synced = 0")
    suspend fun getUnsyncedRencanaList(): List<RencanaKerjaEntity>

    @Query("DELETE FROM rencana_kerja WHERE id = :id")
    suspend fun deleteRencana(id: Long): Int

    // 1. Sync Pruning Query
    @Query("DELETE FROM rencana_kerja WHERE (supervisor_name = :namaPengawas OR pengawas = :namaPengawas) AND (status_realisasi = 'BELUM' OR is_realized = 0) AND id NOT IN (:activeServerIds)")
    suspend fun deletePrunedRencana(namaPengawas: String, activeServerIds: List<Long>): Int

    @Query("DELETE FROM rencana_kerja WHERE (supervisor_name = :namaPengawas OR pengawas = :namaPengawas) AND (status_realisasi = 'BELUM' OR is_realized = 0)")
    suspend fun clearActiveRencanaForSupervisor(namaPengawas: String): Int

    // 2. Real-time Delete Query (Hanya hapus berdasarkan unitKode jika deletedId <= 0)
    @Query("DELETE FROM rencana_kerja WHERE (:deletedId > 0 AND id = :deletedId) OR (:deletedId <= 0 AND :unitKode != '' AND kode_unit = :unitKode)")
    suspend fun deleteByIdOrUnitInternal(deletedId: Long, unitKode: String): Int

    suspend fun deleteByIdOrUnit(deletedId: Long, unitKode: String?): Int {
        return deleteByIdOrUnitInternal(deletedId, unitKode?.trim() ?: "")
    }

    suspend fun deleteByIdOrUnit(deletedId: String?, unitKode: String?): Int {
        val idLong = deletedId?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val cleanUnit = unitKode?.trim() ?: ""
        return if (idLong > 0 || cleanUnit.isNotBlank()) {
            deleteByIdOrUnitInternal(idLong, cleanUnit)
        } else 0
    }

    @Query("DELETE FROM rencana_kerja WHERE kode_unit NOT IN (SELECT unitCode FROM units)")
    suspend fun deleteOrphanedRencanaKerja(): Int

    @Query("""
        DELETE FROM rencana_kerja 
        WHERE LOWER(TRIM(operator)) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(nama_operator)) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(operator_nik)) = LOWER(TRIM(:operatorIdentifier))
           OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:operatorIdentifier, '@', '')))
    """)
    suspend fun deletePlansForOperator(operatorIdentifier: String): Int

    @Query("""
        DELETE FROM rencana_kerja 
        WHERE LOWER(TRIM(operator)) = LOWER(TRIM(:namaOperator))
           OR LOWER(TRIM(nama_operator)) = LOWER(TRIM(:namaOperator))
           OR LOWER(TRIM(operator_nik)) = LOWER(TRIM(:nikOperator))
           OR LOWER(TRIM(operator)) = LOWER(TRIM(:nikOperator))
           OR (TRIM(:namaOperator) != '' AND LOWER(TRIM(operator)) LIKE '%' || LOWER(TRIM(:namaOperator)) || '%')
           OR LOWER(TRIM(created_by)) = LOWER(TRIM(REPLACE(:nikOperator, '@', '')))
    """)
    suspend fun deletePlansForOperator(namaOperator: String, nikOperator: String): Int
}
