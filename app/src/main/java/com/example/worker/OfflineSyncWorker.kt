package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClientManager
import com.example.data.repository.HeavyTrackRepository
import java.util.concurrent.TimeUnit

class OfflineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val apiClientManager = ApiClientManager(applicationContext)
        val repository = HeavyTrackRepository(database, apiClientManager)

        val pendingHasil = database.hasilInputAktivitasDao().getUnsyncedHasilList()
        val pendingRencana = database.rencanaKerjaDao().getUnsyncedRencanaList()

        if (pendingHasil.isEmpty() && pendingRencana.isEmpty()) {
            return Result.success()
        }

        val syncResult = repository.syncAllPendingOperationalData()

        return if (syncResult.isSuccess) {
            val syncedCount = syncResult.getOrDefault(0)
            if (syncedCount > 0) {
                // Tampilkan notifikasi push ringkas: "Laporan unit [Kode Unit] berhasil disinkronkan ke server"
                val unitCodes = pendingHasil.map { it.kodeUnit }.distinct()
                val unitText = if (unitCodes.isNotEmpty()) unitCodes.joinToString(", ") else "armada"
                showSyncNotification(
                    title = "Sinkronisasi Berhasil",
                    message = "Laporan unit $unitText berhasil disinkronkan ke server"
                )
            }
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun showSyncNotification(title: String, message: String) {
        val channelId = "offline_sync_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sinkronisasi Data Otomatis",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi status sinkronisasi laporan lapangan otomatis di latar belakang"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // Android 13+ user may not have granted POST_NOTIFICATIONS
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "heavytrack_offline_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "heavytrack_periodic_sync_work"

        fun scheduleAutoSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )

            // Periodic fallback every 15 minutes with connected constraint
            val periodicRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME + "_immediate",
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }
}
