package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActivityMasterDao
import com.example.data.local.dao.ActivityReportDao
import com.example.data.local.dao.DraftRealisasiDao
import com.example.data.local.dao.HasilInputAktivitasDao
import com.example.data.local.dao.KendalaDao
import com.example.data.local.dao.LokasiDao
import com.example.data.local.dao.OperatorDao
import com.example.data.local.dao.RencanaKerjaDao
import com.example.data.local.dao.AktivitasUnitDao
import com.example.data.local.dao.UnitDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.AktivitasUnitEntity
import com.example.data.local.entity.DraftRealisasiEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UnitEntity::class,
        ActivityMasterEntity::class,
        OperatorEntity::class,
        ActivityReportEntity::class,
        LokasiEntity::class,
        RencanaKerjaEntity::class,
        HasilInputAktivitasEntity::class,
        KendalaEntity::class,
        DraftRealisasiEntity::class,
        AktivitasUnitEntity::class,
        UserEntity::class
    ],
    version = 32,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    fun masterUnitDao(): UnitDao = unitDao()
    fun unitAlatBeratDao(): UnitDao = unitDao()
    abstract fun activityMasterDao(): ActivityMasterDao
    abstract fun operatorDao(): OperatorDao
    fun masterOperatorDao(): OperatorDao = operatorDao()
    abstract fun activityReportDao(): ActivityReportDao
    fun realisasiDao(): ActivityReportDao = activityReportDao()
    abstract fun lokasiDao(): LokasiDao
    abstract fun rencanaKerjaDao(): RencanaKerjaDao
    abstract fun hasilInputAktivitasDao(): HasilInputAktivitasDao
    abstract fun kendalaDao(): KendalaDao
    abstract fun draftRealisasiDao(): DraftRealisasiDao
    abstract fun aktivitasUnitDao(): AktivitasUnitDao
    abstract fun userDao(): UserDao
    fun aktivitasDao(): ActivityMasterDao = activityMasterDao()

    @androidx.room.Transaction
    open suspend fun refreshLocalCacheOnly(bootstrap: com.example.data.remote.BootstrapResponseData) {
        val currentUser = userDao().getActiveUser()
        val activeUsername = currentUser?.username
        val existingPlans = rencanaKerjaDao().getAllRencanaList()

        // 1. Bersihkan data realisasi kosong/palsu dan kosongkan tabel master lokal (Room SQLite only)
        activityReportDao().deleteInvalidEmptyReports()
        hasilInputAktivitasDao().deleteInvalidEmptyHasil()
        rencanaKerjaDao().restoreUnrealizedPlansWithNoValidReport()
        val existingPlansAfterRestore = rencanaKerjaDao().getAllRencanaList()

        userDao().deleteAll()
        unitDao().deleteAll()
        operatorDao().deleteAll()
        aktivitasDao().deleteAll()
        kendalaDao().deleteAll()
        lokasiDao().deleteAll()
        rencanaKerjaDao().deleteAll()

        // 2. Pastikan seluruh 35 operator dari master_operator (status = 'Aktif') terdaftar di tabel users
        val combinedUsersMap = LinkedHashMap<String, UserEntity>()
        for (u in bootstrap.users) {
            combinedUsersMap[u.username.lowercase()] = u
        }

        // Auto-seed dari bootstrap.operators (master_operator status = 'Aktif')
        for ((idx, op) in bootstrap.operators.withIndex()) {
            val isAktifOp = op.status.equals("Aktif", ignoreCase = true) ||
                op.status.equals("Active", ignoreCase = true) ||
                op.status == "1"
            if (!isAktifOp) continue
            val cleanNik = op.badgeNumber.removePrefix("NIK-").trim().ifBlank { op.operatorId.trim() }
            if (cleanNik.isBlank()) continue
            val kasieAtasan = (op.kasiePenanggungJawab ?: op.supervisorAssigned).trim().ifBlank { "Agung" }
            val wil = (op.wilayah ?: "Geodesi PG1").trim().ifBlank { "Geodesi PG1" }
            if (!combinedUsersMap.containsKey(cleanNik.lowercase())) {
                combinedUsersMap[cleanNik.lowercase()] = UserEntity(
                    username = cleanNik,
                    id = op.operatorId.ifBlank { "${101 + idx}" },
                    password = cleanNik,
                    token = "token-auth-session",
                    name = op.name.trim(),
                    namaLengkap = op.name.trim(),
                    nik = cleanNik,
                    role = "operator",
                    roleTitle = "Operator Alat Berat",
                    wilayah = wil,
                    kasieAtasan = kasieAtasan,
                    status = "Aktif",
                    defaultShift = "SIANG",
                    assignedSector = "Operator Alat Berat ($wil)",
                    phone = "+62 812-3456-7901",
                    isActiveSession = false,
                    lastLoginAt = System.currentTimeMillis()
                )
            }
        }

        // Pastikan 35 operator resmi di SupervisorDirectory juga selalu terdaftar di tabel users
        for (opSpv in com.example.data.model.SupervisorDirectory.officialOperatorList) {
            val key = opSpv.username.lowercase()
            if (!combinedUsersMap.containsKey(key)) {
                combinedUsersMap[key] = UserEntity(
                    username = opSpv.username,
                    id = opSpv.id,
                    password = opSpv.passwordPin.ifBlank { opSpv.username },
                    token = "token-auth-session",
                    name = opSpv.name,
                    namaLengkap = opSpv.namaLengkap,
                    nik = opSpv.nik,
                    role = "operator",
                    roleTitle = "Operator Alat Berat",
                    wilayah = opSpv.wilayah,
                    kasieAtasan = opSpv.kasieAtasan,
                    status = "Aktif",
                    defaultShift = "SIANG",
                    assignedSector = opSpv.assignedSector,
                    phone = opSpv.phone,
                    isActiveSession = false,
                    lastLoginAt = System.currentTimeMillis()
                )
            }
        }

        // Pertahankan sesi user yang sedang aktif login
        if (currentUser != null && !activeUsername.isNullOrBlank()) {
            val activeKey = activeUsername.lowercase()
            val existing = combinedUsersMap[activeKey]
            if (existing != null) {
                combinedUsersMap[activeKey] = existing.copy(
                    isActiveSession = true,
                    lastLoginAt = System.currentTimeMillis()
                )
            } else {
                combinedUsersMap[activeKey] = currentUser.copy(
                    isActiveSession = true,
                    lastLoginAt = System.currentTimeMillis()
                )
            }
        }

        val mappedUsers = combinedUsersMap.values.toList()

        val unitOwnerMap = bootstrap.units.associate { it.unitCode.uppercase() to it.supervisorAssigned }
        val sanitizedRencana = bootstrap.rencanaKerja.map { ren ->
            val unitOwner = unitOwnerMap[ren.kodeUnit.uppercase()]
            val trueOwner = if (!unitOwner.isNullOrBlank()) {
                unitOwner
            } else {
                ren.kasie.ifBlank { ren.supervisorName }
            }
            val renSpk = ren.nomorSpk.trim()
            val hasRenSpk = renSpk.isNotBlank() && renSpk != "SPK Menyusul" && renSpk != "-"
            val matchedLocal = existingPlansAfterRestore.find { loc ->
                val locSpk = loc.nomorSpk.trim()
                val hasLocSpk = locSpk.isNotBlank() && locSpk != "SPK Menyusul" && locSpk != "-"
                if (hasRenSpk && hasLocSpk) {
                    locSpk.equals(renSpk, ignoreCase = true)
                } else {
                    loc.id == ren.id && ren.id > 0 && loc.kodeUnit.equals(ren.kodeUnit, ignoreCase = true)
                }
            }
            val resolvedOpNik = ren.operatorNik.ifBlank {
                matchedLocal?.operatorNik?.takeIf { it.isNotBlank() }
                    ?: com.example.data.model.SupervisorDirectory.findOperatorByNameOrNik(ren.operator)?.nik
                    ?: ""
            }
            val resolvedSubIndex = if (ren.subIndex > 1) ren.subIndex else (matchedLocal?.subIndex ?: parseSubIndexFromSpk(ren.nomorSpk))
            val resolvedParentSpk = ren.parentSpk.ifBlank { matchedLocal?.parentSpk ?: parseParentSpk(ren.nomorSpk) }
            val reportForSpk = if (hasRenSpk) activityReportDao().getReportByNomorSpk(renSpk) else null
            val hasValidReport = reportForSpk != null && reportForSpk.hmAkhir > 0.0 &&
                (reportForSpk.hmDailyRunning > 0.0 || reportForSpk.hmAkhir > reportForSpk.hmAwal || reportForSpk.workOutput > 0.0)
            if (matchedLocal != null && matchedLocal.isRealized && hasValidReport) {
                ren.copy(
                    id = matchedLocal.id,
                    supervisorName = trueOwner,
                    pengawas = trueOwner,
                    kasie = trueOwner,
                    createdBy = ren.createdBy.ifBlank { trueOwner },
                    namaOperator = ren.namaOperator.ifBlank { ren.operator },
                    operatorNik = resolvedOpNik,
                    isRealized = true,
                    statusTahap = "REALISASI_SELESAI",
                    statusSpk = matchedLocal.statusSpk.ifBlank { "Terealisasi & Approved" },
                    statusRealisasi = matchedLocal.statusRealisasi.ifBlank { "Terealisasi & Approved" },
                    subIndex = resolvedSubIndex,
                    parentSpk = resolvedParentSpk
                )
            } else {
                val safeId = if (matchedLocal != null && matchedLocal.id > 0) {
                    matchedLocal.id
                } else if (ren.id > 0 && existingPlansAfterRestore.none { it.id == ren.id }) {
                    ren.id
                } else {
                    0L
                }
                ren.copy(
                    id = safeId,
                    supervisorName = trueOwner,
                    pengawas = trueOwner,
                    kasie = trueOwner,
                    createdBy = ren.createdBy.ifBlank { trueOwner },
                    namaOperator = ren.namaOperator.ifBlank { ren.operator },
                    operatorNik = resolvedOpNik,
                    isRealized = false,
                    statusTahap = "RENCANA",
                    statusRealisasi = "Menunggu Realisasi",
                    subIndex = resolvedSubIndex,
                    parentSpk = resolvedParentSpk
                )
            }
        }.toMutableList()

        userDao().insertAll(mappedUsers)
        unitDao().insertAll(bootstrap.units)
        operatorDao().insertAll(bootstrap.operators)
        com.example.data.model.SupervisorDirectory.updateFromOperatorEntities(bootstrap.operators)
        bootstrap.aktivitas.chunked(250).forEach { chunk ->
            aktivitasDao().insertAll(chunk)
        }
        kendalaDao().insertAll(bootstrap.kendala)
        bootstrap.lokasi.chunked(250).forEach { chunk ->
            lokasiDao().insertAll(chunk)
        }
        if (sanitizedRencana.isNotEmpty()) {
            rencanaKerjaDao().insertAll(sanitizedRencana)
        }
    }

    @androidx.room.Transaction
    open suspend fun syncAllMasterData(bootstrap: com.example.data.remote.BootstrapResponseData) {
        refreshLocalCacheOnly(bootstrap)
    }

    fun cleanDumpTruckAndBulldozerDirect() {
        // No-op: Dump Truck and other active fleet units are preserved
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_31_32 = object : androidx.room.migration.Migration(31, 32) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Pastikan nomorSpk yang kosong / placeholder diberi nilai unik sebelum membuat unique index
                db.execSQL("UPDATE activity_reports SET nomorSpk = 'SPK-LEGACY-' || id WHERE TRIM(COALESCE(nomorSpk, '')) IN ('', '-', 'SPK Menyusul', 'Belum Terbit (Menunggu Admin)')")
                // Hapus duplikat berdasarkan nomorSpk, pertahankan record dengan id terbesar
                db.execSQL("DELETE FROM activity_reports WHERE id NOT IN (SELECT MAX(id) FROM activity_reports GROUP BY nomorSpk)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_activity_reports_nomorSpk` ON `activity_reports` (`nomorSpk`)")

                db.execSQL("UPDATE hasil_input_aktivitas SET nomor_spk = 'SPK-LEGACY-' || id WHERE TRIM(COALESCE(nomor_spk, '')) IN ('', '-', 'SPK Menyusul', 'Belum Terbit (Menunggu Admin)')")
                db.execSQL("DELETE FROM hasil_input_aktivitas WHERE id NOT IN (SELECT MAX(id) FROM hasil_input_aktivitas GROUP BY nomor_spk)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_hasil_input_aktivitas_nomor_spk` ON `hasil_input_aktivitas` (`nomor_spk`)")
            }
        }

        private fun parseSubIndexFromSpk(spk: String?): Int {
            if (spk.isNullOrBlank()) return 1
            val parts = spk.trim().split("-")
            if (parts.size >= 4) {
                val last = parts.last().toIntOrNull()
                if (last != null && last in 1..99) return last
            }
            return 1
        }

        private fun parseParentSpk(spk: String?): String {
            if (spk.isNullOrBlank()) return ""
            val parts = spk.trim().split("-")
            return if (parts.size >= 4 && parts.last().toIntOrNull() in 1..99) {
                parts.dropLast(1).joinToString("-")
            } else spk.trim()
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "heavytrack_database"
                )
                    .addMigrations(MIGRATION_31_32)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            try {
                                db.execSQL("CREATE VIEW IF NOT EXISTS master_unit AS SELECT unitCode AS kode_unit, unitType AS jenis_unit, brandModel AS merk_model, lastHm AS last_hm, supervisorAssigned AS supervisor, status, hmMinStandar, hmMaxStandar, wilayah, updatedAt FROM units")
                                db.execSQL("CREATE TRIGGER IF NOT EXISTS trigger_delete_master_unit INSTEAD OF DELETE ON master_unit BEGIN DELETE FROM units WHERE unitCode = OLD.kode_unit; END")
                                db.execSQL("CREATE VIEW IF NOT EXISTS master_operator AS SELECT operatorId, name AS namaOperator, badgeNumber AS nik, badgeNumber, supervisorAssigned AS kasiePenanggungJawab, supervisorAssigned AS kasie_penanggung_jawab, skillLevel, status, wilayah, updatedAt FROM operators")
                                db.execSQL("CREATE VIEW IF NOT EXISTS master_aktivitas AS SELECT sapCode AS kode_sap, activityName AS nama_aktivitas, unitOfMeasure AS satuan, description, category AS kategori, kategori_status, jenis_unit, status, updatedAt FROM master_activities")
                                db.execSQL("CREATE VIEW IF NOT EXISTS users AS SELECT username, user_id AS id, CASE WHEN password != '' THEN password ELSE username END AS password, nama_lengkap AS nama, nama_lengkap, role, kasie_atasan, wilayah, status FROM users_session")
                                db.execSQL("UPDATE lokasi SET wilayah = 'PG1' WHERE wilayah = 'Geodesi PG1' OR wilayah = 'Geodesi'")
                                db.execSQL("UPDATE rencana_kerja SET wilayah = 'PG1' WHERE wilayah = 'Geodesi PG1' OR wilayah = 'Geodesi'")
                                db.execSQL("UPDATE units SET wilayah = 'PG1' WHERE wilayah = 'Geodesi PG1' OR wilayah = 'Geodesi'")
                                db.execSQL("UPDATE operators SET badgeNumber = 'achmad' WHERE LOWER(TRIM(name)) = 'achmad taufik' OR LOWER(TRIM(name)) LIKE '%achmad taufik%'")
                                db.execSQL("UPDATE users_session SET username = 'achmad' WHERE LOWER(TRIM(name)) = 'achmad taufik' OR LOWER(TRIM(nama_lengkap)) = 'achmad taufik' OR LOWER(TRIM(nama_lengkap)) LIKE '%achmad taufik%'")
                                if (!hasColumn(db, "rencana_kerja", "sub_index")) {
                                    db.execSQL("ALTER TABLE rencana_kerja ADD COLUMN sub_index INTEGER NOT NULL DEFAULT 1")
                                }
                                if (!hasColumn(db, "rencana_kerja", "parent_spk")) {
                                    db.execSQL("ALTER TABLE rencana_kerja ADD COLUMN parent_spk TEXT NOT NULL DEFAULT ''")
                                }
                            } catch (_: Exception) {}
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun hasColumn(
            db: androidx.sqlite.db.SupportSQLiteDatabase,
            tableName: String,
            columnName: String
        ): Boolean {
            var cursor: android.database.Cursor? = null
            return try {
                cursor = db.query("PRAGMA table_info($tableName)")
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex == -1) return false
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex).equals(columnName, ignoreCase = true)) {
                        return true
                    }
                }
                false
            } catch (_: Exception) {
                false
            } finally {
                cursor?.close()
            }
        }
    }
}
