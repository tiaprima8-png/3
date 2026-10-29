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
    version = 25,
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

        // 1. Kosongkan seluruh tabel master lokal (Room SQLite only)
        userDao().deleteAll()
        unitDao().deleteAll()
        operatorDao().deleteAll()
        aktivitasDao().deleteAll()
        kendalaDao().deleteAll()
        lokasiDao().deleteAll()
        rencanaKerjaDao().deleteAll()

        // 2. Isi ulang dengan data segar dari server
        val mappedUsers = if (activeUsername != null) {
            bootstrap.users.map { u ->
                if (u.username.equals(activeUsername, ignoreCase = true)) {
                    u.copy(isActiveSession = true, lastLoginAt = System.currentTimeMillis())
                } else {
                    u.copy(isActiveSession = false)
                }
            }
        } else {
            bootstrap.users
        }

        val unitOwnerMap = bootstrap.units.associate { it.unitCode.uppercase() to it.supervisorAssigned }
        val sanitizedRencana = bootstrap.rencanaKerja.map { ren ->
            val unitOwner = unitOwnerMap[ren.kodeUnit.uppercase()]
            val trueOwner = if (!unitOwner.isNullOrBlank()) {
                unitOwner
            } else {
                ren.kasie.ifBlank { ren.supervisorName }
            }
            ren.copy(
                supervisorName = trueOwner,
                pengawas = trueOwner,
                kasie = trueOwner,
                createdBy = ren.createdBy.ifBlank { trueOwner }
            )
        }

        userDao().insertAll(mappedUsers)
        unitDao().insertAll(bootstrap.units)
        operatorDao().insertAll(bootstrap.operators)
        bootstrap.aktivitas.chunked(250).forEach { chunk ->
            aktivitasDao().insertAll(chunk)
        }
        kendalaDao().insertAll(bootstrap.kendala)
        bootstrap.lokasi.chunked(250).forEach { chunk ->
            lokasiDao().insertAll(chunk)
        }
        rencanaKerjaDao().insertAll(sanitizedRencana)
    }

    @androidx.room.Transaction
    open suspend fun syncAllMasterData(bootstrap: com.example.data.remote.BootstrapResponseData) {
        refreshLocalCacheOnly(bootstrap)
    }

    fun cleanDumpTruckAndBulldozerDirect() {
        try {
            val db = openHelper.writableDatabase
            db.execSQL("CREATE VIEW IF NOT EXISTS master_unit AS SELECT unitCode AS kode_unit, unitType AS jenis_unit, brandModel AS merk_model, lastHm AS last_hm, supervisorAssigned AS supervisor, status, hmMinStandar, hmMaxStandar, wilayah, updatedAt FROM units")
            db.execSQL("CREATE TRIGGER IF NOT EXISTS trigger_delete_master_unit INSTEAD OF DELETE ON master_unit BEGIN DELETE FROM units WHERE unitCode = OLD.kode_unit; END")
            db.execSQL("DELETE FROM master_unit WHERE LOWER(jenis_unit) IN ('dump truck', 'bulldozer')")
            db.execSQL("DELETE FROM units WHERE LOWER(unitType) IN ('dump truck', 'bulldozer') OR LOWER(unitType) LIKE '%dump%' OR LOWER(unitType) LIKE '%bulldozer%'")
            db.execSQL("DELETE FROM rencana_kerja WHERE kode_unit NOT IN (SELECT kode_unit FROM master_unit)")
            db.execSQL("DELETE FROM activity_reports WHERE unitCode NOT IN (SELECT unitCode FROM units) OR LOWER(unitType) IN ('dump truck', 'bulldozer')")
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "heavytrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            try {
                                db.execSQL("CREATE VIEW IF NOT EXISTS master_unit AS SELECT unitCode AS kode_unit, unitType AS jenis_unit, brandModel AS merk_model, lastHm AS last_hm, supervisorAssigned AS supervisor, status, hmMinStandar, hmMaxStandar, wilayah, updatedAt FROM units")
                                db.execSQL("CREATE TRIGGER IF NOT EXISTS trigger_delete_master_unit INSTEAD OF DELETE ON master_unit BEGIN DELETE FROM units WHERE unitCode = OLD.kode_unit; END")
                                db.execSQL("CREATE VIEW IF NOT EXISTS master_operator AS SELECT operatorId, name AS namaOperator, badgeNumber, supervisorAssigned AS kasiePenanggungJawab, skillLevel, status, wilayah, updatedAt FROM operators")
                                db.execSQL("DELETE FROM master_unit WHERE LOWER(jenis_unit) IN ('dump truck', 'bulldozer')")
                                db.execSQL("DELETE FROM units WHERE LOWER(unitType) IN ('dump truck', 'bulldozer') OR LOWER(unitType) LIKE '%dump%' OR LOWER(unitType) LIKE '%bulldozer%'")
                                db.execSQL("DELETE FROM rencana_kerja WHERE kode_unit NOT IN (SELECT kode_unit FROM master_unit)")
                                db.execSQL("DELETE FROM activity_reports WHERE unitCode NOT IN (SELECT unitCode FROM units) OR LOWER(unitType) IN ('dump truck', 'bulldozer')")
                            } catch (_: Exception) {}
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
