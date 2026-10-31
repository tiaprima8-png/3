package com.example.ui.viewmodel

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.Supervisor
import com.example.data.model.SupervisorDirectory
import com.example.data.model.WilayahConstants
import com.example.data.remote.ActivityPayload
import com.example.data.remote.CompanyServerSimulator
import com.example.data.remote.KendalaItemPayload
import com.example.data.remote.KendalaJsonConverter
import com.example.data.remote.OperatorPayload
import com.example.data.remote.UnitPayload
import com.example.data.repository.HeavyTrackRepository
import com.example.data.repository.SyncLogItem
import com.example.data.remote.RealtimeSocketManager
import com.example.data.remote.RealtimeRealisasiData
import com.example.data.remote.PostActivityReportRequest
import com.example.util.ImageCompressor
import com.example.util.NotificationHelper
import com.example.ui.screens.formatTanggalIndonesia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FormUiState(
    val reportDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val selectedUnit: UnitEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedOperator: OperatorEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val workLocation: String = "001A",
    val shift: String = "SIANG",
    val workDurationHours: Double = 7.0,
    val hmAwal: Double = 0.0,
    val hmAkhirInput: String = "",
    val hmDailyRunning: Double = 0.0,
    val isHmValid: Boolean = true,
    val hmErrorMessage: String? = null,
    val workOutputInput: String = "",
    val notes: String = "",
    val unitStatus: String = "OPERASI",  // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val isRefueled: Boolean = false,
    val fuelLitersInput: String = "",
    val standbyReason: String = "Hujan / Slippery",
    val breakdownTrouble: String = "Trouble Engine / Mesin",
    val photoPath: String? = null,
    val photoBase64: String? = null,
    val photoSizeKb: Long? = null,
    val photoDimensions: String? = null,
    val isCompressingPhoto: Boolean = false,
    val isSubmitting: Boolean = false,
    val formError: String? = null
)

fun normalizeDateToIso(dateStr: String): String {
    val clean = dateStr.trim()
    if (clean.isBlank()) return "0000-00-00"
    if (clean.contains("/")) {
        val parts = clean.split("/")
        if (parts.size == 3) {
            val d = parts[0].padStart(2, '0')
            val m = parts[1].padStart(2, '0')
            val y = parts[2]
            return "$y-$m-$d"
        }
    }
    return clean
}

// Bobot Shift: SIANG = 1, MALAM = 2
fun getShiftWeight(shift: String?): Int {
    return if (shift?.equals("MALAM", ignoreCase = true) == true) 2 else 1
}

/**
 * Cek apakah tugas lain (otherTask) mendahului tugas saat ini (currentTask) untuk unit yang sama.
 * Aturan Strict FIFO:
 * 1. Antar Tanggal: Tanggal lebih lampau wajib selesai terlebih dahulu.
 * 2. Antar Shift pada hari yang sama: Shift SIANG wajib selesai sebelum Shift MALAM bisa dibuka.
 * 3. Dalam Shift yang sama: Sub-rencana (#1, #2, #3) TIDAK BOLEH saling mengunci (bebas diinput mana saja).
 */
fun isPriorTask(currentTask: RencanaKerjaEntity, otherTask: RencanaKerjaEntity): Boolean {
    if (currentTask.id == otherTask.id && currentTask.id > 0) return false
    val currentDate = normalizeDateToIso(currentTask.tanggalFormat)
    val otherDate = normalizeDateToIso(otherTask.tanggalFormat)

    // 1. Antar Tanggal: jika tanggal tugas lain lebih lampau, maka tugas lain mendahului
    if (otherDate < currentDate) return true

    // 2. Pada Tanggal yang Sama:
    if (otherDate == currentDate) {
        val currentShiftWeight = getShiftWeight(currentTask.shiftKerja)
        val otherShiftWeight = getShiftWeight(otherTask.shiftKerja)

        // Shift SIANG mendahului Shift MALAM
        if (otherShiftWeight < currentShiftWeight) {
            return true
        }

        // 3. Dalam shift yang sama: TIDAK ADA yang mendahului (bebas dipilih / tidak saling mengunci)
        return false
    }

    return false
}

fun isTaskOlder(task1: RencanaKerjaEntity, task2: RencanaKerjaEntity): Boolean {
    // task1 mendahului task2
    return isPriorTask(currentTask = task2, otherTask = task1)
}

fun sortRencanaChrono(list: List<RencanaKerjaEntity>): List<RencanaKerjaEntity> {
    return list.sortedWith(
        compareBy<RencanaKerjaEntity> { normalizeDateToIso(it.tanggalFormat) }
            .thenBy { getShiftWeight(it.shiftKerja) }
            .thenBy { it.subIndex }
            .thenBy { it.id }
    )
}

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
    data class Info(val message: String) : UiMessage
}

class HeavyTrackViewModel(
    private val repository: HeavyTrackRepository
) : ViewModel() {

    companion object {
        const val INTERNAL_WEB_ADMIN_URL = "https://1-portalce.ai.studio"
        const val WEB_ADMIN_APK_DOWNLOAD_PAGE = "https://1-portalce.ai.studio/download-apk"
        const val WEB_ADMIN_APK_DIRECT_URL = "https://1-portalce.ai.studio/download-apk?download=true"
        const val GITHUB_REPO_URL = "https://github.com/tiaprima8-png/3"
        const val GITHUB_RELEASES_PAGE_URL = "https://github.com/tiaprima8-png/3/releases"
        const val APK_DIRECT_DOWNLOAD_URL = "https://1-portalce.ai.studio/download-apk?download=true"
        const val APK_DEBUG_DOWNLOAD_URL = "https://1-portalce.ai.studio/download-apk?download=true"
    }

    // Configurable APK Download URL (default ke Portal Web Admin resmi)
    private val _customApkUrl = MutableStateFlow(WEB_ADMIN_APK_DOWNLOAD_PAGE)
    val customApkUrl: StateFlow<String> = _customApkUrl.asStateFlow()

    fun updateCustomApkUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        if (trimmed.isNotEmpty()) {
            _customApkUrl.value = trimmed
        }
    }

    fun openApkDownloadInBrowser(context: Context, urlToOpen: String? = null) {
        val targetUrl = (urlToOpen ?: _customApkUrl.value).ifEmpty { APK_DIRECT_DOWNLOAD_URL }
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Gagal membuka browser: ${e.localizedMessage ?: "Tidak ada aplikasi peramban"}"))
            }
        }
    }

    // Active Supervisor
    private val _activeSupervisor = MutableStateFlow("")
    val activeSupervisor: StateFlow<String> = _activeSupervisor.asStateFlow()

    // Supervisor Login Session
    private val _currentSupervisorSession = MutableStateFlow<Supervisor?>(null)
    val currentSupervisorSession: StateFlow<Supervisor?> = _currentSupervisorSession.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Master Users state (Disinkronkan secara langsung dari Web Admin)
    private val _masterUsers = MutableStateFlow<List<Supervisor>>(SupervisorDirectory.allSupervisors)
    val masterUsers: StateFlow<List<Supervisor>> = _masterUsers.asStateFlow()

    private val _isSyncingUsers = MutableStateFlow(false)
    val isSyncingUsers: StateFlow<Boolean> = _isSyncingUsers.asStateFlow()

    // Mode Monitoring Eksekutif vs Supervisor Lapangan (Manager & Super Admin)
    val isManagerSession: StateFlow<Boolean> = _currentSupervisorSession
        .map { it?.isExecutive == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSuperAdminSession: StateFlow<Boolean> = _currentSupervisorSession
        .map { it?.isSuperAdmin == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isOperatorSession: StateFlow<Boolean> = _currentSupervisorSession
        .map { it?.isOperator == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isKasieSession: StateFlow<Boolean> = _currentSupervisorSession
        .map { it != null && it.isKasie && !it.isOperator }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userRole: StateFlow<String> = _currentSupervisorSession
        .map { it?.role ?: "KASIE" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "KASIE")


    // Base URL configuration for Web Admin (Internalized)
    val apiBaseUrl: String = INTERNAL_WEB_ADMIN_URL

    // Real-time WebSocket Client via Socket.IO
    val realtimeSocketManager = RealtimeSocketManager(repository.apiClientManager.context)
    val isSocketConnected: StateFlow<Boolean> = realtimeSocketManager.isConnected

    // Offline mode simulation toggle
    private val _isOfflineSimulated = MutableStateFlow(CompanyServerSimulator.isSimulatedOffline)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    // Sync logs
    val syncLogs: StateFlow<List<SyncLogItem>> = repository.syncLogs

    // UI events (snackbars)
    private val _uiEvents = MutableSharedFlow<UiMessage>()
    val uiEvents: SharedFlow<UiMessage> = _uiEvents.asSharedFlow()

    // Syncing state
    private val _isSyncingMaster = MutableStateFlow(false)
    val isSyncingMaster: StateFlow<Boolean> = _isSyncingMaster.asStateFlow()

    private val _isSyncingPending = MutableStateFlow(false)
    val isSyncingPending: StateFlow<Boolean> = _isSyncingPending.asStateFlow()

    val isSyncing: StateFlow<Boolean> = combine(_isSyncingMaster, _isSyncingPending) { master, pending ->
        master || pending
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isBootstrapping = MutableStateFlow(false)
    val isBootstrapping: StateFlow<Boolean> = _isBootstrapping.asStateFlow()

    val lastSyncTimeMillis: StateFlow<Long> = repository.lastSyncTimeMillis

    // Master Units (filtered strictly by current Kasie, or all units for Manager/Super Admin)
    val units: StateFlow<List<UnitEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor
    ) { session, supervisor ->
        session to supervisor
    }.flatMapLatest { (session, supervisor) ->
        val kasieName = session?.namaLengkap?.ifBlank { session.name }?.ifBlank { supervisor } ?: supervisor
        val username = session?.username ?: ""
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getAllUnits()
        } else if (kasieName.isNotBlank() || username.isNotBlank()) {
            repository.getUnitsForKasie(kasieName, username)
        } else {
            repository.getAllUnits()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Activities
    val activities: StateFlow<List<ActivityMasterEntity>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Operators (filtered strictly by current Kasie & Wilayah)
    val operators: StateFlow<List<OperatorEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor
    ) { session, supervisor ->
        session to supervisor
    }.flatMapLatest { (session, supervisor) ->
        val kasieName = session?.namaLengkap?.ifBlank { session.name }?.ifBlank { supervisor } ?: supervisor
        val username = session?.username ?: ""
        val wilayah = session?.wilayah ?: ""
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getOperatorsFiltered(null)
        } else if (kasieName.isNotBlank() || username.isNotBlank() || wilayah.isNotBlank()) {
            repository.getOperatorsForKasie(kasieName, username, wilayah)
        } else {
            repository.getAllOperators()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Lokasi Kerja (Debounced 200ms, Flexible Wilayah Filter Chip: [Semua], [PG1], [PG2], [OTHER])
    private val _lokasiSearchQuery = MutableStateFlow("")
    val lokasiSearchQuery: StateFlow<String> = _lokasiSearchQuery.asStateFlow()

    private val _lokasiWilayahFilter = MutableStateFlow("Semua")
    val lokasiWilayahFilter: StateFlow<String> = _lokasiWilayahFilter.asStateFlow()

    fun setLokasiWilayahFilter(wilayah: String) {
        _lokasiWilayahFilter.value = wilayah
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lokasiPilihan: StateFlow<List<LokasiEntity>> = combine(
        _lokasiSearchQuery.debounce(200).distinctUntilChanged(),
        _lokasiWilayahFilter
    ) { query, wilayahFilter ->
        query to wilayahFilter
    }.flatMapLatest { (query, filter) ->
        val effectiveWilayah = if (filter.isBlank() || filter.equals("Semua", ignoreCase = true)) {
            null
        } else {
            filter
        }
        repository.getAllActiveLokasi(searchQuery = query, wilayah = effectiveWilayah)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lokasiList: StateFlow<List<LokasiEntity>> = lokasiPilihan

    fun onLokasiSearchQueryChanged(newQuery: String) {
        _lokasiSearchQuery.value = newQuery
    }

    // Master Kendala Lapangan
    val kendalaList: StateFlow<List<KendalaEntity>> = repository.getAllKendala()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports list (historical) - strictly isolated per Kasie or per Operator
    val reports: StateFlow<List<ActivityReportEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor,
        units
    ) { session, supervisor, kasieUnits ->
        Triple(session, supervisor, kasieUnits)
    }.flatMapLatest { (session, supervisor, kasieUnits) ->
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getAllReports()
        } else if (session?.isOperator == true) {
            val opName = session.resolvedNamaLengkap
            val opUser = session.username
            val opNik = session.nik
            combine(
                repository.getRealisasiForOperator(opName),
                repository.getRealisasiForOperator(opUser),
                repository.getRealisasiForOperator(opNik)
            ) { byName, byUser, byNik ->
                (byName + byUser + byNik).distinctBy { it.id }.sortedByDescending { it.createdAt }
            }
        } else {
            val kasieName = session?.namaLengkap?.ifBlank { session.name } ?: supervisor
            val username = session?.username?.ifBlank { kasieName } ?: kasieName
            val ownedUnitCodes = kasieUnits.map { it.unitCode.uppercase() }.toSet()
            repository.getRealisasiForKasie(kasieName, username).map { list ->
                if (ownedUnitCodes.isEmpty()) list
                else list.filter { 
                    it.namaKasie.equals(kasieName, ignoreCase = true) ||
                    it.supervisorName.equals(kasieName, ignoreCase = true) ||
                    ownedUnitCodes.contains(it.unitCode.uppercase())
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All reports across all inputs / supervisors
    val allReports: StateFlow<List<ActivityReportEntity>> = repository.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaGlobal: StateFlow<List<RencanaKerjaEntity>> = repository.getAllRencana()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hasil Realisasi list - strictly isolated per Kasie or per Operator
    val hasilAktivitasList: StateFlow<List<HasilInputAktivitasEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor,
        units
    ) { session, supervisor, kasieUnits ->
        Triple(session, supervisor, kasieUnits)
    }.flatMapLatest { (session, supervisor, kasieUnits) ->
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getAllHasil()
        } else if (session?.isOperator == true) {
            val opName = session.resolvedNamaLengkap
            val opUser = session.username
            val opNik = session.nik
            combine(
                repository.getHasilForOperator(opName),
                repository.getHasilForOperator(opUser),
                repository.getHasilForOperator(opNik)
            ) { byName, byUser, byNik ->
                (byName + byUser + byNik).distinctBy { it.id }.sortedByDescending { it.createdAt }
            }
        } else {
            val kasieName = session?.namaLengkap?.ifBlank { session.name } ?: supervisor
            val username = session?.username?.ifBlank { kasieName } ?: kasieName
            val ownedUnitCodes = kasieUnits.map { it.unitCode.uppercase() }.toSet()
            repository.getHasilForKasie(kasieName, username).map { list ->
                if (ownedUnitCodes.isEmpty()) list
                else list.filter { 
                    it.namaKasie.equals(kasieName, ignoreCase = true) ||
                    it.supervisorName.equals(kasieName, ignoreCase = true) ||
                    ownedUnitCodes.contains(it.kodeUnit.uppercase())
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Rencana Kerja (shift today / active and NOT yet realized) - strictly isolated per Kasie or Operator
    val activeRencanaList: StateFlow<List<RencanaKerjaEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor,
        units
    ) { session, supervisor, kasieUnits ->
        Triple(session, supervisor, kasieUnits)
    }.flatMapLatest { (session, supervisor, kasieUnits) ->
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getActiveRencanaForSupervisor("Semua").map { list ->
                sortRencanaChrono(list)
            }
        } else if (session?.isOperator == true) {
            val opName = session.resolvedNamaLengkap
            val opUser = session.username
            val opNik = session.nik
            combine(
                repository.getRencanaForOperator(opName),
                repository.getRencanaForOperator(opUser),
                repository.getRencanaForOperator(opNik)
            ) { byName, byUser, byNik ->
                sortRencanaChrono((byName + byUser + byNik).distinctBy { it.id })
            }
        } else {
            val kasieName = session?.namaLengkap?.ifBlank { session.name } ?: supervisor
            val username = session?.username?.ifBlank { kasieName } ?: kasieName
            val ownedUnitCodes = kasieUnits.map { it.unitCode.uppercase() }.toSet()
            repository.getRencanaKerjaForKasie(kasieName, username).map { list ->
                val filtered = if (ownedUnitCodes.isEmpty()) list
                else list.filter { 
                    it.kasie.equals(kasieName, ignoreCase = true) ||
                    it.supervisorName.equals(kasieName, ignoreCase = true) ||
                    ownedUnitCodes.contains(it.kodeUnit.uppercase())
                }
                sortRencanaChrono(filtered)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaList: StateFlow<List<RencanaKerjaEntity>> = combine(
        _currentSupervisorSession,
        _activeSupervisor,
        units
    ) { session, supervisor, kasieUnits ->
        Triple(session, supervisor, kasieUnits)
    }.flatMapLatest { (session, supervisor, kasieUnits) ->
        if (session?.isManager == true || session?.isSuperAdmin == true) {
            repository.getAllRencana()
        } else if (session?.isOperator == true) {
            val opName = session.resolvedNamaLengkap
            val opUser = session.username
            combine(
                repository.getAllRencanaForOperator(opName),
                repository.getAllRencanaForOperator(opUser)
            ) { byName, byUser ->
                (byName + byUser).distinctBy { it.id }.sortedByDescending { it.createdAt }
            }
        } else {
            val kasieName = session?.namaLengkap?.ifBlank { session.name } ?: supervisor
            val username = session?.username?.ifBlank { kasieName } ?: kasieName
            val ownedUnitCodes = kasieUnits.map { it.unitCode.uppercase() }.toSet()
            repository.getAllRencanaKerjaForKasie(kasieName, username).map { list ->
                if (ownedUnitCodes.isEmpty()) list
                else list.filter { ownedUnitCodes.contains(it.kodeUnit.uppercase()) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allDrafts: StateFlow<List<com.example.data.local.entity.DraftRealisasiEntity>> = repository.getAllDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selector Unit: Status informatif 3 Kasus (Kasus A: Backlog Kemarin, Kasus B: Belum Ada Rencana Hari Ini, Kasus C: Sudah Ada Rencana Hari Ini / Tambah Susulan)
    val availableUnitsForRencana: StateFlow<List<UnitPlanStatus>> = combine(
        units,
        allRencanaList,
        hasilAktivitasList,
        reports
    ) { unitList, rencanaList, hasilList, reportList ->
        val nowMs = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayIso = sdfIso.format(Date(nowMs))
        val yesterdayIso = sdfIso.format(Date(nowMs - oneDayMs))
        val twoDaysAgoIso = sdfIso.format(Date(nowMs - 2 * oneDayMs))

        val todayDisplay = formatTanggalIndonesia(todayIso)
        val yesterdayDisplay = formatTanggalIndonesia(yesterdayIso)
        val twoDaysAgoDisplay = formatTanggalIndonesia(twoDaysAgoIso)

        unitList.map { u ->
            val code = u.unitCode.trim()

            val completedDatesFromHasil = hasilList
                .filter { it.kodeUnit.equals(code, ignoreCase = true) }
                .map { normalizeDateToIso(it.tanggalFormat) }
            val completedDatesFromReports = reportList
                .filter { it.unitCode.equals(code, ignoreCase = true) }
                .map { normalizeDateToIso(it.reportDate) }
            val completedDatesFromRencana = rencanaList
                .filter { r ->
                    r.kodeUnit.equals(code, ignoreCase = true) &&
                    (r.isRealized ||
                     r.statusRealisasi.equals("SUDAH", ignoreCase = true) ||
                     r.statusRealisasi.equals("TEREALISASI", ignoreCase = true) ||
                     r.statusRealisasi.contains("APPROVED", ignoreCase = true) ||
                     r.statusTahap.equals("REALISASI_SELESAI", ignoreCase = true))
                }
                .map { normalizeDateToIso(it.tanggalFormat) }

            val completedDates = (completedDatesFromHasil + completedDatesFromReports + completedDatesFromRencana)
                .filter { it.isNotBlank() }
                .toSet()

            val plansToday = rencanaList.filter {
                it.kodeUnit.equals(code, ignoreCase = true) &&
                normalizeDateToIso(it.tanggalFormat) == todayIso
            }

            val isTwoDaysAgoDone = completedDates.contains(twoDaysAgoIso)
            val isYesterdayDone = completedDates.contains(yesterdayIso)

            when {
                !isTwoDaysAgoDone -> {
                    // Kasus A1 (Backlog H-2): Wajib diisi terlebih dahulu
                    UnitPlanStatus(
                        unit = u,
                        targetDate = twoDaysAgoIso,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [H-2 ($twoDaysAgoDisplay): Belum Diisi]"
                    )
                }
                !isYesterdayDone -> {
                    // Kasus A2 (Backlog H-1 Kemarin): Wajib diisi
                    UnitPlanStatus(
                        unit = u,
                        targetDate = yesterdayIso,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [H-1 ($yesterdayDisplay): Belum Diisi]"
                    )
                }
                plansToday.isEmpty() -> {
                    // Kasus B (Belum Ada Rencana Hari Ini - Tertib & Siap Hari Ini)
                    UnitPlanStatus(
                        unit = u,
                        targetDate = todayIso,
                        isBacklog = false,
                        isSusulan = false,
                        existingPlanCount = 0,
                        labelText = "${u.unitCode} [Tertib • Siap Hari Ini]"
                    )
                }
                else -> {
                    // Kasus C (Sudah Ada Rencana Hari Ini): Pengawas tetap dapat menambah rencana susulan
                    val count = plansToday.size
                    UnitPlanStatus(
                        unit = u,
                        targetDate = todayIso,
                        isBacklog = false,
                        isSusulan = true,
                        existingPlanCount = count,
                        labelText = "${u.unitCode} [Hari Ini: Sudah Ada $count Rencana] (+ Tambah Rencana Susulan)"
                    )
                }
            }
        }.sortedWith(
            compareBy<UnitPlanStatus> {
                when {
                    it.isBacklog && it.targetDate == twoDaysAgoIso -> 0 // Prioritas 1: Backlog H-2
                    it.isBacklog -> 1 // Prioritas 2: Backlog H-1
                    !it.isSusulan -> 2 // Prioritas 3: Hari ini baru
                    else -> 3 // Prioritas 4: Hari ini susulan
                }
            }.thenBy { it.unit.unitCode }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hasil Input Realisasi - strictly isolated per Kasie
    val hasilList: StateFlow<List<HasilInputAktivitasEntity>> = hasilAktivitasList

    // Unsynced count
    val unsyncedCount: StateFlow<Int> = repository.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ==========================================
    // 4. KARTU RINGKASAN PROGRES HARIAN (SHIFT PROGRESS TRACKER)
    // ==========================================
    val fleetShiftProgress: StateFlow<FleetShiftProgressState> = combine(
        units,
        allRencanaList,
        hasilList
    ) { unitList, rencanaList, realisasiList ->
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val ownedCodes = unitList.map { it.unitCode.uppercase() }.toSet()
        val plansToday = rencanaList.filter {
            it.tanggalFormat == todayStr && (ownedCodes.isEmpty() || ownedCodes.contains(it.kodeUnit.uppercase()))
        }
        val resultsToday = realisasiList.filter {
            it.tanggalFormat == todayStr && (ownedCodes.isEmpty() || ownedCodes.contains(it.kodeUnit.uppercase()))
        }

        // Hanya hitung unit yang menjadi tanggung jawab Kasie yang sedang login
        val baseUnits = unitList

        val items = baseUnits.map { u ->
            val uPlans = plansToday.filter { it.kodeUnit.equals(u.unitCode, ignoreCase = true) }
            val uResults = resultsToday.filter { it.kodeUnit.equals(u.unitCode, ignoreCase = true) }
            // Unit terealisasi jika memiliki hasil realisasi hari ini atau rencana telah ditandai isRealized
            val isRealized = uResults.isNotEmpty() || uPlans.any { it.isRealized }
            val latestPlan = uPlans.maxByOrNull { it.createdAt }
            UnitShiftProgressItem(
                unitCode = u.unitCode,
                isRealized = isRealized,
                rencanaCount = uPlans.size,
                latestActivity = latestPlan?.namaAktivitas?.ifBlank { latestPlan.sapCode } ?: u.unitType,
                latestOperator = latestPlan?.operator ?: "",
                spk = latestPlan?.nomorSpk ?: ""
            )
        }

        val total = items.size
        val realized = items.count { it.isRealized }
        val pending = total - realized
        val pct = if (total > 0) (realized.toFloat() / total.toFloat()) else 0f

        FleetShiftProgressState(
            realizedUnitsCount = realized,
            totalUnitsCount = total,
            pendingUnitsCount = pending,
            progressPercentage = pct,
            isAllCompleted = total > 0 && realized == total,
            unitItems = items
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FleetShiftProgressState())

    // ==========================================
    // TAHAP FORMULIR (SUB-TAB: 0 RENCANA, 1 REALISASI)
    // ==========================================
    private val _formSubTab = MutableStateFlow(0)
    val formSubTab: StateFlow<Int> = _formSubTab.asStateFlow()

    private val _rencanaFormState = MutableStateFlow(RencanaFormUiState())
    val rencanaFormState: StateFlow<RencanaFormUiState> = _rencanaFormState.asStateFlow()

    private val _realisasiFormState = MutableStateFlow(RealisasiFormUiState())
    val realisasiFormState: StateFlow<RealisasiFormUiState> = _realisasiFormState.asStateFlow()

    // Backward-compatible single form state
    private val _formState = MutableStateFlow(FormUiState())
    val formState: StateFlow<FormUiState> = _formState.asStateFlow()

    // ==========================================
    // ==========================================
    // FLEET ANALYTICS & EXECUTIVE MANAGER FILTERS
    // ==========================================
    private val _analyticsPeriod = MutableStateFlow("Bulan Ini")
    val analyticsPeriod: StateFlow<String> = _analyticsPeriod.asStateFlow()

    private val _analyticsUnitFilter = MutableStateFlow("Semua Unit")
    val analyticsUnitFilter: StateFlow<String> = _analyticsUnitFilter.asStateFlow()

    // Filter Eksekutif Manager (Wilayah, Jenis Unit, Kode Unit)
    private val _filterWilayah = MutableStateFlow("Semua Wilayah")
    val filterWilayah: StateFlow<String> = _filterWilayah.asStateFlow()

    private val _filterJenisUnit = MutableStateFlow("Semua Jenis")
    val filterJenisUnit: StateFlow<String> = _filterJenisUnit.asStateFlow()

    private val _filterKodeUnit = MutableStateFlow("Semua Unit")
    val filterKodeUnit: StateFlow<String> = _filterKodeUnit.asStateFlow()

    data class ExecutiveFilter(
        val period: String,
        val wilayah: String,
        val jenisUnit: String,
        val kodeUnit: String
    )

    val executiveFilterState: StateFlow<ExecutiveFilter> = combine(
        _analyticsPeriod,
        _filterWilayah,
        _filterJenisUnit,
        _filterKodeUnit
    ) { period, wilayah, jenis, kode ->
        ExecutiveFilter(period, wilayah, jenis, kode)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ExecutiveFilter("Bulan Ini", "Semua Wilayah", "Semua Jenis", "Semua Unit")
    )

    val allUnitsGlobal: StateFlow<List<UnitEntity>> = repository.getAllUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredUnits: StateFlow<List<UnitEntity>> = combine(
        _filterWilayah,
        _filterJenisUnit,
        _filterKodeUnit
    ) { wilayah, jenis, kode ->
        Triple(wilayah, jenis, kode)
    }.flatMapLatest { (wilayah, jenis, kode) ->
        repository.getUnitsByFilter(wilayah, jenis, kode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredReports: StateFlow<List<ActivityReportEntity>> = combine(
        _filterWilayah,
        _filterJenisUnit,
        _filterKodeUnit
    ) { wilayah, jenis, kode ->
        Triple(wilayah, jenis, kode)
    }.flatMapLatest { (wilayah, jenis, kode) ->
        repository.getReportsFiltered(wilayah, jenis, kode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRencanaList: StateFlow<List<RencanaKerjaEntity>> = combine(
        _filterWilayah,
        _filterKodeUnit
    ) { wilayah, kode ->
        Pair(wilayah, kode)
    }.flatMapLatest { (wilayah, kode) ->
        repository.getRencanaFiltered(wilayah, kode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analyticsState: StateFlow<FleetAnalyticsState> = combine(
        _currentSupervisorSession,
        reports,
        filteredReports,
        executiveFilterState
    ) { session, supervisorReports, filteredRepList, filters ->
        val targetReports = if (session?.isManager == true || session?.isSuperAdmin == true) filteredRepList else supervisorReports
        calculateAnalytics(targetReports, filters.period, filters.kodeUnit, filters.wilayah, filters.jenisUnit)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FleetAnalyticsState()
    )

    fun setFilterWilayah(wilayah: String) {
        _filterWilayah.value = wilayah
    }

    fun setFilterJenisUnit(jenis: String) {
        _filterJenisUnit.value = jenis
    }

    fun setFilterKodeUnit(kode: String) {
        _filterKodeUnit.value = kode
        _analyticsUnitFilter.value = kode
    }

    fun resetExecutiveFilters() {
        _filterWilayah.value = "Semua Wilayah"
        _filterJenisUnit.value = "Semua Jenis"
        _filterKodeUnit.value = "Semua Unit"
        _analyticsUnitFilter.value = "Semua Unit"
    }

    init {
        viewModelScope.launch {
            // Restore active session dari Room SQLite & DataStore Preferences jika ada
            try {
                val savedSession = repository.getActiveUser()
                if (savedSession != null && savedSession.isActiveSession && savedSession.isAktif) {
                    val spv = Supervisor(
                        id = savedSession.id,
                        name = savedSession.resolvedNamaLengkap,
                        namaLengkap = savedSession.resolvedNamaLengkap,
                        username = savedSession.username,
                        nik = savedSession.nik.ifBlank { savedSession.username },
                        passwordPin = "",
                        defaultShift = savedSession.defaultShift,
                        assignedSector = savedSession.assignedSector,
                        phone = savedSession.phone,
                        roleTitle = savedSession.roleTitle,
                        role = savedSession.role,
                        wilayah = savedSession.wilayah ?: if (savedSession.isExecutive) "Semua Wilayah" else "Geodesi PG1",
                        kasieAtasan = savedSession.kasieAtasan ?: "",
                        token = savedSession.token,
                        status = savedSession.status
                    )
                    _currentSupervisorSession.value = spv
                    _activeSupervisor.value = if (spv.isOperator && spv.kasieAtasan.isNotBlank()) spv.kasieAtasan else spv.name
                    _isLoggedIn.value = true

                } else {
                    _currentSupervisorSession.value = null
                    _activeSupervisor.value = ""
                    _isLoggedIn.value = false
                }
            } catch (_: Exception) {
                _currentSupervisorSession.value = null
                _activeSupervisor.value = ""
                _isLoggedIn.value = false
            }

            // Selalu sinkronkan data master user terbaru dari Web Admin saat inisialisasi
            syncMasterUsers(silent = true)
            repository.seedInitialDataIfEmpty(_activeSupervisor.value)
            repository.reconcileSyncStatus()
            fetchBootstrapData(showOverlay = false)
            // Tarik rencana kerja aktif terbaru dari Web Admin saat inisialisasi agar SPK yang sudah terbit langsung terisi
            syncActivePlansFromServer()
        }

        // 1. Hubungkan client WebSocket ke backend Web Admin
        val targetServerUrl = repository.apiClientManager.baseUrl
        realtimeSocketManager.onConnectedCallback = {
            viewModelScope.launch {
                syncActivePlansFromServer()
            }
        }
        realtimeSocketManager.connect(targetServerUrl)

        // 2. WEBSOCKET & SOCKET.IO LISTENER REAL-TIME (SPK_PUBLISHED)
        viewModelScope.launch {
            realtimeSocketManager.spkPublishedEvents.collect { event ->
                handleSpkPublished(event.rencanaId, event.unitKode, event.nomorSpk, event.statusSpk)
            }
        }

        // 2b. REAL-TIME LISTENER PENGHAPUSAN (RENCANA_KERJA_DELETED)
        viewModelScope.launch {
            realtimeSocketManager.rencanaDeletedEvents.collect { event ->
                handleRencanaDeleted(event.deletedId, event.unitKode)
            }
        }

        // 3. Listener event SPK dari backend simulator (bridge)
        viewModelScope.launch {
            CompanyServerSimulator.spkEventFlow.collect { event ->
                handleSpkPublished(event.planId, event.kodeUnit, event.nomorSpk, "SPK_TERBIT")
            }
        }

        // 3b. Listener event penghapusan dari backend simulator (bridge)
        viewModelScope.launch {
            CompanyServerSimulator.rencanaDeletedEventFlow.collect { event ->
                handleRencanaDeleted(event.planId.toString(), event.unitKode)
            }
        }

        // 3c. Listener event realisasi operator baru (Socket.IO & Native WS)
        viewModelScope.launch {
            realtimeSocketManager.realisasiCreatedEvents.collect { event ->
                handleIncomingRealisasi(event)
            }
        }

        // 3d. Listener event realisasi operator dari backend simulator
        viewModelScope.launch {
            CompanyServerSimulator.realisasiCreatedEventFlow.collect { event ->
                handleIncomingRealisasi(event)
            }
        }

        // 4. Polling background berkala untuk memastikan data SPK tetap mutakhir jika WebSocket reconnecting
        viewModelScope.launch {
            while (true) {
                delay(15000)
                if (_formSubTab.value == 1) {
                    try {
                        repository.fetchAndReplaceActiveRencana(_activeSupervisor.value).onSuccess { (newPlans, _) ->
                            val currentSelected = _realisasiFormState.value.selectedRencana
                            if (currentSelected != null && _realisasiFormState.value.nomorSpk.isBlank()) {
                                val match = newPlans.find { it.id == currentSelected.id || it.kodeUnit.equals(currentSelected.kodeUnit, ignoreCase = true) }
                                if (match != null && match.isSpkTerbit) {
                                    _realisasiFormState.value = _realisasiFormState.value.copy(
                                        nomorSpk = match.nomorSpk,
                                        statusSpk = "SPK_TERBIT",
                                        selectedRencana = match.copy(nomorSpk = match.nomorSpk, statusSpk = "SPK_TERBIT")
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Listener & Handler Real-Time Penerbitan SPK (SPK_PUBLISHED):
     * 1. Update database SQLite lokal / Room:
     *    rencanaKerjaDao.updateSpkForActiveUnit(cleanUnit) & updateRencanaSpk(rencanaId)
     * 2. Perbarui State UI secara instan (Reaktif) pada form input nomor SPK:
     *    Ganti nomor SPK di form input dan badge status dari "Menunggu SPK" menjadi "SPK: $nomorSpk".
     * 3. Tampilkan Push Notification / In-App Toast.
     */
    fun handleSpkPublished(rencanaId: Long, unitKode: String, nomorSpk: String, statusSpk: String = "SPK_TERBIT") {
        viewModelScope.launch(Dispatchers.Main) {
            val cleanSpk = nomorSpk.trim()
            val cleanUnit = unitKode.trim()
            if (cleanSpk.isBlank() || cleanSpk.equals("null", ignoreCase = true) || cleanSpk == "-" || cleanSpk == "SPK Menyusul") {
                return@launch
            }

            val effectiveStatus = if (statusSpk.isBlank() || statusSpk.equals("MENUNGGU_SPK", ignoreCase = true)) "SPK_TERBIT" else statusSpk

            // 1. Update ke SQLite lokal / Room untuk ID (dan fallback unit hanya jika rencanaId <= 0)
            withContext(Dispatchers.IO) {
                if (rencanaId > 0) {
                    repository.updateRencanaSpk(rencanaId, cleanSpk, effectiveStatus)
                } else if (cleanUnit.isNotBlank()) {
                    repository.updateSpkForActiveUnit(cleanUnit, cleanSpk, effectiveStatus)
                    repository.updateSpkStatus(cleanUnit, cleanSpk, effectiveStatus)
                }
            }

            // 2. Perbarui state form input nomor SPK jika rencana yang sedang dipilih cocok
            val cur = _realisasiFormState.value.selectedRencana
            val isCurrentMatch = cur != null && (
                (rencanaId > 0 && cur.id == rencanaId) ||
                (rencanaId <= 0 && cleanUnit.isNotBlank() && cur.kodeUnit.equals(cleanUnit, ignoreCase = true))
            )

            if (isCurrentMatch && cur != null) {
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    nomorSpk = cleanSpk,
                    statusSpk = effectiveStatus,
                    selectedRencana = cur.copy(
                        nomorSpk = cleanSpk,
                        statusSpk = effectiveStatus
                    )
                )
            }

            // 3. Sinkronkan seluruh rencana kerja di background agar konsisten dengan server
            withContext(Dispatchers.IO) {
                try {
                    repository.fetchAndReplaceActiveRencana(_activeSupervisor.value)
                } catch (_: Exception) {}
            }

            // 4. Tampilkan Push Notification / In-App Toast
            NotificationHelper.showSpkPublishedNotification(
                repository.apiClientManager.context,
                unitKode = cleanUnit.ifBlank { cur?.kodeUnit ?: "Unit" },
                nomorSpk = cleanSpk
            )
            _uiEvents.emit(
                UiMessage.Success("SPK Resmi Diterima dari Web Admin: $cleanSpk untuk ${cleanUnit.ifBlank { cur?.kodeUnit ?: "Rencana Kerja" }}")
            )
        }
    }

    /**
     * Listener & Handler Real-Time Penghapusan Rencana Kerja (RENCANA_KERJA_DELETED):
     * Sesuai instruksi:
     * 1. Hapus dari database Room SQLite lokal:
     *    rencanaKerjaDao.deleteByIdOrUnit(deletedId, unitKode)
     * 2. Hapus dari daftar rencana aktif di UI state secara reaktif:
     *    Reset form selection jika rencana yang dihapus sedang dipilih pengawas.
     *    Room Flow otomatis memperbarui daftar rencana di UI secara instan.
     */
    fun handleRencanaDeleted(deletedIdStr: String, unitKode: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val numId = deletedIdStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val cleanUnit = unitKode.trim()

            // 1. Hapus dari database Room SQLite lokal
            withContext(Dispatchers.IO) {
                repository.deleteRencanaByIdOrUnit(deletedIdStr, cleanUnit)
            }

            // 2. Perbarui State form input realisasi jika unit yang sedang dipilih dihapus di server
            val cur = _realisasiFormState.value.selectedRencana
            val isCurrentDeleted = cur != null && (
                (numId > 0 && cur.id == numId) ||
                (cleanUnit.isNotBlank() && cur.kodeUnit.equals(cleanUnit, ignoreCase = true))
            )

            if (isCurrentDeleted) {
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    selectedRencana = null,
                    nomorSpk = "",
                    statusSpk = "MENUNGGU_SPK"
                )
            }

            // 3. Tampilkan pesan informasi ke pengawas
            val unitLabel = if (cleanUnit.isNotBlank()) "Unit $cleanUnit" else "ID $deletedIdStr"
            _uiEvents.emit(
                UiMessage.Info("Rencana kerja $unitLabel telah dihapus oleh Admin Web.")
            )
        }
    }

    fun handleIncomingRealisasi(relData: RealtimeRealisasiData) {
        viewModelScope.launch(Dispatchers.IO) {
            if (relData.hmAkhir <= 0.0 || (relData.totalHm <= 0.0 && relData.hmAkhir <= relData.hmAwal)) {
                return@launch
            }
            val session = _currentSupervisorSession.value
            val kasieName = session?.resolvedNamaLengkap?.ifBlank { _activeSupervisor.value } ?: _activeSupervisor.value
            val matchesKasie = kasieName.isBlank() ||
                    relData.kasie.isBlank() ||
                    relData.kasie.equals(kasieName, ignoreCase = true) ||
                    relData.kasie.contains(kasieName, ignoreCase = true) ||
                    kasieName.contains(relData.kasie, ignoreCase = true)

            if (matchesKasie) {
                val saved = repository.saveIncomingRealisasi(relData)
                if (saved != null) {
                    withContext(Dispatchers.Main) {
                        _uiEvents.emit(UiMessage.Info("Realisasi baru diterima dari Operator ${relData.operator} (${relData.unitKode})"))
                    }
                }
            }
        }
    }

    fun handleIncomingRealisasi(req: PostActivityReportRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val hmAwal = req.hm_awal ?: 0.0
            val hmAkhir = req.hm_akhir ?: 0.0
            val totalHm = req.hm_harian_berjalan ?: (if (hmAkhir > hmAwal) hmAkhir - hmAwal else 0.0)
            val hasilKerja = req.hasil_kerja ?: 0.0
            if (hmAkhir <= 0.0 || (totalHm <= 0.0 && hmAkhir <= hmAwal && hasilKerja <= 0.0)) {
                return@launch
            }
            val session = _currentSupervisorSession.value
            val kasieName = session?.resolvedNamaLengkap?.ifBlank { _activeSupervisor.value } ?: _activeSupervisor.value
            val reqKasie = req.nama_pengawas ?: req.supervisor_name ?: ""
            val matchesKasie = kasieName.isBlank() ||
                    reqKasie.isBlank() ||
                    reqKasie.equals(kasieName, ignoreCase = true) ||
                    reqKasie.contains(kasieName, ignoreCase = true) ||
                    kasieName.contains(reqKasie, ignoreCase = true)

            if (matchesKasie) {
                val saved = repository.saveIncomingRealisasi(req)
                if (saved != null) {
                    withContext(Dispatchers.Main) {
                        _uiEvents.emit(UiMessage.Info("Realisasi baru diterima dari Operator ${req.nama_operator ?: ""} (${req.kode_unit ?: ""})"))
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeSocketManager.disconnect()
    }

    fun syncActivePlansFromServer(onDone: ((List<RencanaKerjaEntity>) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val kasieName = _currentSupervisorSession.value?.resolvedNamaLengkap?.ifBlank { _activeSupervisor.value } ?: _activeSupervisor.value
            if (isKasieSession.value && kasieName.isNotBlank()) {
                repository.fetchPendingRealisasiForKasie(kasieName)
            }
            val pullResult = repository.fetchAndReplaceActiveRencana(_activeSupervisor.value)
            pullResult.onSuccess { (newPlans, _) ->
                withContext(Dispatchers.Main) {
                    val cur = _realisasiFormState.value.selectedRencana
                    if (cur != null) {
                        val matching = newPlans.find { it.id == cur.id || it.kodeUnit.equals(cur.kodeUnit, ignoreCase = true) }
                        if (matching == null) {
                            // Rencana yang sedang dipilih sudah dihapus di server, reset form selection
                            _realisasiFormState.value = _realisasiFormState.value.copy(
                                selectedRencana = null,
                                nomorSpk = "",
                                statusSpk = "MENUNGGU_SPK"
                            )
                        } else if (matching.isSpkTerbit && _realisasiFormState.value.nomorSpk.isBlank()) {
                            _realisasiFormState.value = _realisasiFormState.value.copy(
                                nomorSpk = matching.nomorSpk,
                                statusSpk = "SPK_TERBIT",
                                selectedRencana = cur.copy(nomorSpk = matching.nomorSpk, statusSpk = "SPK_TERBIT")
                            )
                        }
                    }
                    onDone?.invoke(newPlans)
                }
            }
        }
    }

    fun setFormSubTab(tab: Int) {
        _formSubTab.value = tab
        if (tab == 1) {
            // Saat membuka tab Realisasi Kerja, otomatis sinkronkan rencana kerja aktif agar nomor SPK terbaru langsung muncul
            syncActivePlansFromServer()
        }
    }

    // ==========================================
    // LOGIN & SESI (DENGAN PERSISTENSI ROOM SQLITE & DATASTORE PREFERENCES)
    // ==========================================
    fun login(supervisor: Supervisor) {
        _currentSupervisorSession.value = supervisor
        _activeSupervisor.value = if (supervisor.isOperator && supervisor.kasieAtasan.isNotBlank()) {
            supervisor.kasieAtasan
        } else {
            supervisor.name
        }
        _isLoggedIn.value = true
        _rencanaFormState.value = _rencanaFormState.value.copy(
            shiftKerja = supervisor.defaultShift
        )
        // Saat user awal login, jangan langsung masuk form realisasi
        clearSelectedRencanaForRealisasi()

        // Simpan sesi dan role pengguna ke Room SQLite & DataStore Preferences
        val cleanUsername = supervisor.username.ifBlank { supervisor.nik.ifBlank { supervisor.name } }
        val userEntity = UserEntity(
            username = cleanUsername,
            password = supervisor.passwordPin.ifBlank { cleanUsername },
            id = supervisor.userId,
            token = supervisor.token,
            name = supervisor.name,
            namaLengkap = supervisor.resolvedNamaLengkap,
            nik = supervisor.nik.ifBlank { cleanUsername },
            role = if (supervisor.isOperator) "operator" else if (supervisor.isKasie) "kasie" else supervisor.role,
            roleTitle = supervisor.roleTitle,
            wilayah = supervisor.wilayah,
            kasieAtasan = supervisor.kasieAtasan,
            status = supervisor.status,
            defaultShift = supervisor.defaultShift,
            assignedSector = supervisor.assignedSector,
            phone = supervisor.phone,
            isActiveSession = true,
            lastLoginAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                repository.saveUserSession(userEntity)
            } catch (_: Exception) {}

            when {
                supervisor.isSuperAdmin -> {
                    _uiEvents.emit(UiMessage.Success("Selamat datang, Super Admin ${supervisor.resolvedNamaLengkap}"))
                }
                supervisor.isManager -> {
                    _uiEvents.emit(UiMessage.Success("Selamat datang, Manager ${supervisor.resolvedNamaLengkap}"))
                }
                supervisor.isOperator -> {
                    val kasieLabel = supervisor.kasieAtasan.ifBlank { "Kasie Lapangan" }
                    _uiEvents.emit(UiMessage.Success("Selamat bertugas, Operator ${supervisor.resolvedNamaLengkap} • Kasie: $kasieLabel"))
                }
                else -> {
                    _uiEvents.emit(UiMessage.Success("Selamat bertugas, Kasie ${supervisor.resolvedNamaLengkap} • ${supervisor.wilayah}"))
                }
            }
            // Mekanisme Sinkronisasi Bersih: Kosongkan tabel rencana kerja lama saat login
            repository.clearAllRencanaKerja()
            fetchBootstrapData(showOverlay = true)
            if (supervisor.isOperator) {
                val opIdentifier = supervisor.nik.ifBlank { supervisor.username.ifBlank { supervisor.resolvedNamaLengkap } }
                repository.refreshTugasOperatorFromServer(opIdentifier)
            }
            syncPendingReports()
        }
    }

    suspend fun loginWithApi(username: String, pin: String): com.example.data.remote.LoginResult {
        val result = repository.apiClientManager.executeLogin(username, pin)
        when (result) {
            is com.example.data.remote.LoginResult.Success -> {
                val userDto = result.user
                if (userDto.isExecutive) {
                    return com.example.data.remote.LoginResult.Error(
                        "Akses Ditolak: Akun Super Admin & Manager hanya dapat digunakan pada Website Portal Admin (https://1-portalce.ai.studio/). Aplikasi mobile RKCE ini khusus untuk Kasie & Operator Lapangan."
                    )
                }
                if (!userDto.isAktif) {
                    return com.example.data.remote.LoginResult.Error(
                        "Akun '${userDto.resolvedNamaLengkap}' berstatus Nonaktif di Website Admin. Silakan hubungi Super Admin untuk mengaktifkan kembali."
                    )
                }

                val isOp = userDto.isOperator
                val supervisor = Supervisor(
                    id = userDto.numericOrStringId,
                    name = userDto.resolvedNamaLengkap,
                    namaLengkap = userDto.resolvedNamaLengkap,
                    username = userDto.username ?: username.trim(),
                    nik = userDto.username ?: username.trim(),
                    passwordPin = pin.trim(),
                    defaultShift = "SIANG",
                    assignedSector = if (isOp) "Operator Alat Berat (${userDto.wilayah ?: "Geodesi PG1"})" else "Sektor Operasional Lapangan (${userDto.wilayah ?: "Geodesi PG1"})",
                    phone = "+62 812-0000-0000",
                    roleTitle = if (isOp) "Operator Alat Berat" else "Kasie Lapangan",
                    role = if (isOp) "operator" else "kasie",
                    wilayah = userDto.wilayah ?: "Geodesi PG1",
                    kasieAtasan = userDto.resolvedKasieAtasan,
                    token = result.token,
                    status = userDto.status ?: "Aktif"
                )

                login(supervisor)
                return result
            }
            is com.example.data.remote.LoginResult.Error -> {
                if (result.message.contains("tidak terdaftar", ignoreCase = true) ||
                    result.message.contains("dinonaktifkan", ignoreCase = true)
                ) {
                    repository.clearUserSession()
                }
                return result
            }
        }
    }

    fun loginManual(username: String, pin: String): Boolean {
        val trimmedUser = username.trim().removePrefix("@").trim()
        val trimmedPin = pin.trim()
        val (matched, _) = SupervisorDirectory.authenticateKasie(trimmedUser, trimmedPin)
        if (matched != null && (matched.isKasie || matched.isOperator) && matched.isAktif) {
            login(matched)
            return true
        }
        return false
    }


    fun syncMasterUsers(silent: Boolean = false) {
        viewModelScope.launch {
            _isSyncingUsers.value = true
            try {
                val (apiUsers, _) = repository.apiClientManager.fetchMasterUsers()
                if (apiUsers.isNotEmpty()) {
                    SupervisorDirectory.updateFromApiUsers(apiUsers)
                    _masterUsers.value = SupervisorDirectory.allSupervisors
                    if (!silent) {
                        _uiEvents.emit(UiMessage.Success("Data master akun pengguna berhasil diperbarui (${apiUsers.size} akun)"))
                    }
                    // Jika user yang sedang login statusnya diubah atau dinonaktifkan di web admin
                    val currentSession = _currentSupervisorSession.value
                    if (currentSession != null) {
                        val updatedUser = SupervisorDirectory.findUserByUsername(currentSession.username)
                        if (updatedUser != null) {
                            if (!updatedUser.isAktif) {
                                logout()
                                _uiEvents.emit(UiMessage.Error("Akun Anda telah dinonaktifkan oleh administrator website."))
                            } else {
                                _currentSupervisorSession.value = updatedUser
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (!silent) {
                    _uiEvents.emit(UiMessage.Info("Gagal memperbarui data user: ${e.message}"))
                }
            } finally {
                _isSyncingUsers.value = false
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentSupervisorSession.value = null
        _activeSupervisor.value = ""
        _rencanaFormState.value = RencanaFormUiState()
        clearSelectedRencanaForRealisasi()
        resetExecutiveFilters()
        viewModelScope.launch {
            try {
                repository.clearUserSession()
                repository.clearAllRencanaKerja()
            } catch (_: Exception) {}
            _uiEvents.emit(UiMessage.Info("Anda telah keluar dari sesi aplikasi"))
            // Tarik ulang user terbaru saat kembali ke halaman login
            syncMasterUsers(silent = true)
        }
    }

    fun switchSupervisor(newSupervisor: String) {
        _activeSupervisor.value = newSupervisor
        fetchBootstrapData(showOverlay = true)
    }

    fun toggleOfflineSimulation() {
        val next = !_isOfflineSimulated.value
        CompanyServerSimulator.isSimulatedOffline = next
        _isOfflineSimulated.value = next
        viewModelScope.launch {
            if (next) {
                _uiEvents.emit(UiMessage.Info("Mode Offline Lapangan Aktif (Data tersimpan di SQLite lokal)"))
            } else {
                _uiEvents.emit(UiMessage.Success("Koneksi Pulih! Mengirim antrean offline ke Web Admin..."))
                syncPendingReports()
            }
        }
    }

    // ==========================================
    // AUTO-SYNC & MASTER DATA / BOOTSTRAP HYDRATION
    // ==========================================
    fun fetchBootstrapData(showOverlay: Boolean = true) {
        viewModelScope.launch {
            if (showOverlay) {
                _isBootstrapping.value = true
            }
            _isSyncingMaster.value = true
            val nowMs = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nowMs))
            val currentSupervisor = _activeSupervisor.value

            val result = repository.fetchBootstrapData(currentSupervisor, todayStr)
            val session = _currentSupervisorSession.value
            val kasieName = session?.resolvedNamaLengkap?.ifBlank { currentSupervisor } ?: currentSupervisor
            repository.fetchAndReplaceActiveRencana(currentSupervisor)
            repository.fetchPendingRealisasiForKasie(kasieName)
            _isSyncingMaster.value = false
            if (showOverlay) {
                _isBootstrapping.value = false
            }

            result.onSuccess { summary ->
                _uiEvents.emit(UiMessage.Success("Data aplikasi berhasil disinkronkan."))
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal terhubung ke Web Admin. Periksa koneksi internet."))
            }
        }
    }

    fun forceSyncWithServer() {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            val nowMs = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentSupervisor = _activeSupervisor.value

            val pendingCount = repository.getUnsyncedCountValue()
            if (pendingCount > 0) {
                _isSyncingPending.value = true
                repository.syncAllPendingOperationalData()
                _isSyncingPending.value = false
            }

            val result = repository.fetchBootstrapData(currentSupervisor, nowMs)
            val session = _currentSupervisorSession.value
            val kasieName = session?.resolvedNamaLengkap?.ifBlank { currentSupervisor } ?: currentSupervisor
            repository.fetchAndReplaceActiveRencana(currentSupervisor)
            repository.fetchPendingRealisasiForKasie(kasieName)
            _isSyncingMaster.value = false

            result.onSuccess { summary ->
                _uiEvents.emit(UiMessage.Success("Data aplikasi berhasil disinkronkan."))
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal terhubung ke Web Admin. Periksa koneksi internet."))
            }
        }
    }

    /**
     * RESET PAKSA CACHE TRANSAKSI LOKAL KE 0
     * Membersihkan seluruh tabel rencana_kerja dan realisasi lokal agar cerminannya bersih 0.
     */
    fun resetLocalTransactionData(context: Context? = null) {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            try {
                repository.resetLocalTransactionCache()
                clearSelectedRencanaForRealisasi()
                _rencanaFormState.value = RencanaFormUiState()
                _isSyncingMaster.value = false

                context?.let { ctx ->
                    Toast.makeText(
                        ctx,
                        "Database transaksi lokal berhasil di-reset ke 0.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                _uiEvents.emit(UiMessage.Success("Database transaksi lokal berhasil di-reset ke 0."))
            } catch (err: Exception) {
                _isSyncingMaster.value = false
                _uiEvents.emit(UiMessage.Error("Gagal mereset data lokal: ${err.localizedMessage ?: err.message}"))
            }
        }
    }

    /**
     * AKSI TOMBOL "SINKRON" (MANUAL PULL DARI SERVER)
     * Saat user menekan tombol "Sinkron" di pojok kanan atas:
     * 1. Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * 2. Ganti seluruh daftar rencana kerja di layar dengan data terbaru dari server.
     * 3. Jika unit terpilih memiliki status_spk == 'SPK_TERBIT', langsung tampilkan nomor SPK
     *    di Card "1. Nomor SPK" dan buka kunci form realisasi.
     */
    fun triggerHeaderSync() {
        viewModelScope.launch {
            _isSyncingMaster.value = true

            // Sinkronkan pending laporan terlebih dahulu jika ada
            val pendingCount = repository.getUnsyncedCountValue()
            if (pendingCount > 0) {
                _isSyncingPending.value = true
                repository.syncAllPendingOperationalData()
                _isSyncingPending.value = false
            }

            // 1. Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
            // 2. Ganti seluruh daftar rencana kerja di layar dengan data terbaru dari server
            val pullResult = repository.fetchAndReplaceActiveRencana(_activeSupervisor.value)

            pullResult.onSuccess { (newPlans, msg) ->
                // Refresh master data pendukung
                fetchBootstrapData(showOverlay = false)

                // 3. Periksa status unit yang sedang dipilih di form
                val currentSelected = _realisasiFormState.value.selectedRencana
                val matchingPlan = if (currentSelected != null) {
                    newPlans.find { it.id == currentSelected.id }
                        ?: newPlans.find { it.kodeUnit.equals(currentSelected.kodeUnit, ignoreCase = true) }
                } else {
                    null
                }

                if (currentSelected != null && matchingPlan == null) {
                    // Rencana yang sedang dipilih sudah dihapus oleh Admin di Web
                    _realisasiFormState.value = _realisasiFormState.value.copy(
                        selectedRencana = null,
                        nomorSpk = "",
                        statusSpk = "MENUNGGU_SPK"
                    )
                } else if (matchingPlan != null) {
                    selectRencanaForRealisasi(matchingPlan)
                    val isSpkPublished = matchingPlan.statusSpk.equals("SPK_TERBIT", ignoreCase = true) ||
                            matchingPlan.statusSpk.equals("TERBIT_SPK", ignoreCase = true) ||
                            (matchingPlan.nomorSpk.isNotBlank() && matchingPlan.nomorSpk != "SPK Menyusul" && matchingPlan.nomorSpk != "-")

                    if (isSpkPublished) {
                        _realisasiFormState.value = _realisasiFormState.value.copy(
                            nomorSpk = matchingPlan.nomorSpk,
                            statusSpk = "SPK_TERBIT",
                            selectedRencana = matchingPlan.copy(
                                nomorSpk = matchingPlan.nomorSpk,
                                statusSpk = "SPK_TERBIT"
                            )
                        )
                    }
                }

                val spkNotice = matchingPlan?.let {
                    if (it.isSpkTerbit) " [SPK ${it.kodeUnit}: ${it.nomorSpk}]" else ""
                } ?: ""
                _uiEvents.emit(UiMessage.Success("Sinkron Berhasil: $msg$spkNotice"))
            }.onFailure { err ->
                fetchBootstrapData(showOverlay = false)
                _uiEvents.emit(UiMessage.Info("Sinkron Server: ${err.localizedMessage ?: err.message}"))
            }

            _isSyncingMaster.value = false
        }
    }

    fun fetchMasterData() {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            val result = repository.fetchMasterData(_activeSupervisor.value)
            _isSyncingMaster.value = false
            result.onSuccess { msg ->
                _uiEvents.emit(UiMessage.Success("Auto-Sync: $msg"))
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Info("Offline: ${err.message ?: "Data dimuat dari SQLite lokal"}"))
            }
        }
    }

    // 5. AUTO-FETCH DATA MASTER SAAT HALAMAN DIBUKA (ONRESUME / ONENTER)
    fun autoFetchMasterDataOnResume() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Non-blocking background sync: updates master kendala & unit standards (hm_min_standar / hm_max_standar)
                repository.fetchMasterData(_activeSupervisor.value)
                syncActivePlansFromServer()

                // If a unit is currently selected in form, refresh dynamic standards from the latest DB
                val currentSelectedUnitCode = _realisasiFormState.value.selectedRencana?.kodeUnit
                if (!currentSelectedUnitCode.isNullOrBlank()) {
                    val updatedDbUnit = repository.getUnitByCode(currentSelectedUnitCode)
                    if (updatedDbUnit != null) {
                        val state = _realisasiFormState.value
                        val cleanAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                        val (isValid, running, errorMsg) = validateHm(state.hmAwal, cleanAkhir)
                        _realisasiFormState.value = state.copy(
                            hmMinStandar = updatedDbUnit.hmMinStandar,
                            hmMaxStandar = updatedDbUnit.hmMaxStandar,
                            isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < updatedDbUnit.hmMinStandar
                        )
                    }
                }
            } catch (_: Exception) {
                // Fallback non-blocking: data lokal SQLite tetap aktif
            }
        }
    }

    fun syncPendingReports(context: Context? = null) {
        viewModelScope.launch {
            val pendingCount = repository.getUnsyncedCountValue()
            if (pendingCount <= 0) {
                context?.let {
                    android.widget.Toast.makeText(it, "Semua laporan sudah tersinkron ke Web Admin.", android.widget.Toast.LENGTH_SHORT).show()
                }
                _uiEvents.emit(UiMessage.Info("Semua laporan sudah tersinkron ke Web Admin."))
                return@launch
            }
            _isSyncingPending.value = true
            val result = repository.syncAllPendingOperationalData()
            _isSyncingPending.value = false
            result.onSuccess { count ->
                if (count > 0) {
                    context?.let {
                        android.widget.Toast.makeText(it, "Berhasil menyinkronkan $count laporan tertunda ke Web Admin!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    _uiEvents.emit(UiMessage.Success("Berhasil menyinkronkan $count entri operasional ke Web Admin!"))
                } else {
                    context?.let {
                        android.widget.Toast.makeText(it, "Gagal menyinkronkan laporan tertunda ke server. Periksa koneksi.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    _uiEvents.emit(UiMessage.Error("Gagal menyinkronkan laporan tertunda ke server. Periksa koneksi internet."))
                }
            }.onFailure { err ->
                context?.let {
                    android.widget.Toast.makeText(it, "Gagal sinkron ke server: ${err.message}. Data tersimpan lokal.", android.widget.Toast.LENGTH_LONG).show()
                }
                _uiEvents.emit(UiMessage.Error("Gagal sinkron antrean: ${err.message}"))
            }
        }
    }

    fun getLastSyncDescription(): String {
        val lastTime = lastSyncTimeMillis.value
        val now = System.currentTimeMillis()
        val diffMinutes = (now - lastTime) / (60 * 1000)
        return when {
            diffMinutes <= 1 -> "Baru saja"
            diffMinutes < 60 -> "$diffMinutes menit yang lalu"
            else -> {
                val hours = diffMinutes / 60
                "$hours jam yang lalu"
            }
        }
    }

    // ==========================================
    // TAHAP 1: FORMULIR RENCANA KERJA
    // ==========================================
    fun setRencanaStatus(status: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(statusUnit = status)
    }

    fun getCompletedDatesForUnit(kodeUnit: String): Set<String> {
        val cleanCode = kodeUnit.trim()
        val fromHasil = hasilAktivitasList.value
            .filter { it.kodeUnit.equals(cleanCode, ignoreCase = true) }
            .map { normalizeDateToIso(it.tanggalFormat) }
        val fromReports = reports.value
            .filter { it.unitCode.equals(cleanCode, ignoreCase = true) }
            .map { normalizeDateToIso(it.reportDate) }
        val fromRencana = allRencanaList.value
            .filter { r ->
                r.kodeUnit.equals(cleanCode, ignoreCase = true) &&
                (r.isRealized ||
                 r.statusRealisasi.equals("SUDAH", ignoreCase = true) ||
                 r.statusRealisasi.equals("TEREALISASI", ignoreCase = true) ||
                 r.statusRealisasi.contains("APPROVED", ignoreCase = true) ||
                 r.statusTahap.equals("REALISASI_SELESAI", ignoreCase = true))
            }
            .map { normalizeDateToIso(it.tanggalFormat) }

        return (fromHasil + fromReports + fromRencana)
            .filter { it.isNotBlank() }
            .toSet()
    }

    fun checkUnitBacklogDate(kodeUnit: String): String? {
        val nowMs = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val yesterdayIso = sdfIso.format(Date(nowMs - oneDayMs)) // e.g. "2026-10-29"
        val twoDaysAgoIso = sdfIso.format(Date(nowMs - 2 * oneDayMs)) // e.g. "2026-10-28"

        val completedDates = getCompletedDatesForUnit(kodeUnit)

        val isTwoDaysAgoDone = completedDates.contains(twoDaysAgoIso)
        if (!isTwoDaysAgoDone) {
            return twoDaysAgoIso
        }

        val isYesterdayDone = completedDates.contains(yesterdayIso)
        if (!isYesterdayDone) {
            return yesterdayIso
        }

        return null
    }

    fun selectRencanaUnitStatus(status: UnitPlanStatus) {
        val current = _rencanaFormState.value
        val isDifferentUnit = current.selectedUnit?.unitCode != status.unit.unitCode
        val backlogDate = checkUnitBacklogDate(status.unit.unitCode) ?: status.targetDate.takeIf { status.isBacklog }
        val isBacklogActive = backlogDate != null || status.isBacklog
        val effectiveDate = backlogDate ?: status.targetDate

        _rencanaFormState.value = current.copy(
            selectedUnitStatus = status,
            selectedUnit = status.unit,
            targetDate = effectiveDate,
            isBacklog = isBacklogActive,
            isSusulan = if (isBacklogActive) false else status.isSusulan,
            // Jika memilih unit baru atau unit Kasus C (susulan), persiapkan form input bersih
            selectedOperator = if (isDifferentUnit) null else current.selectedOperator,
            selectedActivity = if (isDifferentUnit) null else current.selectedActivity,
            selectedLokasi = if (isDifferentUnit) null else current.selectedLokasi,
            keterangan = if (isDifferentUnit) "" else current.keterangan,
            subPlans = if (isDifferentUnit) emptyList() else current.subPlans
        )
    }

    fun selectRencanaUnit(unit: UnitEntity) {
        val current = _rencanaFormState.value
        val isDifferentUnit = current.selectedUnit?.unitCode != unit.unitCode
        val matchedStatus = availableUnitsForRencana.value.find { it.unit.unitCode.equals(unit.unitCode, ignoreCase = true) }
        val backlogDate = checkUnitBacklogDate(unit.unitCode) ?: matchedStatus?.targetDate?.takeIf { matchedStatus.isBacklog }

        if (matchedStatus != null) {
            selectRencanaUnitStatus(matchedStatus)
        } else {
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val effectiveDate = backlogDate ?: todayStr
            val isBacklogActive = backlogDate != null
            _rencanaFormState.value = current.copy(
                selectedUnit = unit,
                targetDate = effectiveDate,
                isBacklog = isBacklogActive,
                isSusulan = false,
                selectedActivity = if (isDifferentUnit) null else current.selectedActivity,
                selectedOperator = if (isDifferentUnit) null else current.selectedOperator,
                selectedLokasi = if (isDifferentUnit) null else current.selectedLokasi,
                subPlans = if (isDifferentUnit) emptyList() else current.subPlans
            )
        }
    }

    fun getUnitTypeForUnitCode(unitCode: String?): String {
        if (unitCode.isNullOrBlank()) return ""
        val matchedUnit = units.value.find { it.unitCode.equals(unitCode, ignoreCase = true) }
        if (matchedUnit != null && matchedUnit.unitType.isNotBlank()) return matchedUnit.unitType
        val clean = unitCode.trim().uppercase()
        return when {
            clean.startsWith("EX") || clean.startsWith("HE") -> "Excavator Standar"
            clean.startsWith("MG") || clean.startsWith("GR") -> "Motor Grader"
            clean.startsWith("CMP") -> "Compactor"
            else -> ""
        }
    }

    fun filterActivitiesByUnitType(
        allActivities: List<ActivityMasterEntity>,
        unitType: String?,
        kategoriFilter: String? = "SEMUA",
        searchQuery: String? = ""
    ): List<ActivityMasterEntity> {
        val cleanType = unitType?.trim()?.uppercase() ?: ""
        val cleanCategory = kategoriFilter?.trim()?.uppercase() ?: "SEMUA"
        val cleanQuery = searchQuery?.trim()?.lowercase() ?: ""

        return allActivities.filter { act ->
            val actType = act.jenisUnit.trim().uppercase()

            val matchesType = when {
                cleanType.isBlank() -> true
                actType == "ALL" || actType == "SEMUA" -> true
                actType == cleanType -> true
                // Strict unit model separation (Excavator Standar, Long Arm, Mini strictly isolated)
                cleanType.contains("STANDAR") && actType.contains("STANDAR") -> true
                cleanType.contains("LONG ARM") && actType.contains("LONG ARM") -> true
                cleanType.contains("MINI") && actType.contains("MINI") -> true
                cleanType.contains("GRADER") && actType.contains("GRADER") -> true
                cleanType.contains("COMPACTOR") && actType.contains("COMPACTOR") -> true
                (cleanType.contains("DUMP") || cleanType.contains("TRUCK")) && (actType.contains("DUMP") || actType.contains("TRUCK")) -> true
                else -> false
            }

            val actStatus = act.kategoriStatus.trim().uppercase()
            val actCategory = act.category.trim().uppercase()
            val matchesCategory = cleanCategory == "SEMUA" || cleanCategory == "ALL" || cleanCategory.isBlank() ||
                actStatus == cleanCategory || actCategory == cleanCategory

            val matchesQuery = cleanQuery.isBlank() ||
                act.activityName.lowercase().contains(cleanQuery) ||
                act.sapCode.lowercase().contains(cleanQuery)

            matchesType && matchesCategory && matchesQuery
        }.sortedWith(
            compareBy(
                { act ->
                    when (act.kategoriStatus.trim().uppercase()) {
                        "OPERASI" -> 1
                        "STANDBY" -> 2
                        "BREAKDOWN" -> 3
                        else -> 4
                    }
                },
                { it.activityName }
            )
        )
    }

    fun selectRencanaOperator(operator: OperatorEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedOperator = operator)
    }

    fun selectRencanaActivity(activity: ActivityMasterEntity) {
        val autoStatus = activity.kategoriStatus.ifBlank { "OPERASI" }
        _rencanaFormState.value = _rencanaFormState.value.copy(
            selectedActivity = activity,
            statusUnit = autoStatus
        )
    }

    fun selectRencanaLokasi(lokasi: LokasiEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedLokasi = lokasi)
    }

    fun setRencanaShift(shift: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(shiftKerja = shift)
    }

    fun setRencanaKeterangan(ket: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(keterangan = ket)
    }

    // Sub-Form Multi-Rencana (1 Unit & 1 Tanggal bisa > 1 Rencana Kerja)
    fun addSubRencana() {
        val current = _rencanaFormState.value
        val newSub = SubRencanaFormState(
            shiftKerja = current.shiftKerja
        )
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans + newSub
        )
    }

    fun removeSubRencana(id: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.filter { it.id != id }
        )
    }

    fun updateSubRencanaOperator(id: String, op: OperatorEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedOperator = op) else it }
        )
    }

    fun updateSubRencanaActivity(id: String, act: ActivityMasterEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedActivity = act) else it }
        )
    }

    fun updateSubRencanaLokasi(id: String, lok: LokasiEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedLokasi = lok) else it }
        )
    }

    fun updateSubRencanaShift(id: String, shift: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(shiftKerja = shift) else it }
        )
    }

    fun updateSubRencanaKeterangan(id: String, ket: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(keterangan = ket) else it }
        )
    }

    fun kirimRencanaKerjaSekaligus() {
        submitRencanaKerja()
    }

    fun submitRencanaKerja() {
        val state = _rencanaFormState.value
        val unit = state.selectedUnit
        val op = state.selectedOperator
        val act = state.selectedActivity
        val lok = state.selectedLokasi

        if (unit == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Unit Alat Berat terlebih dahulu")) }
            return
        }
        if (op == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Operator Pelaksana")) }
            return
        }
        if (act == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Aktivitas / Jenis Pekerjaan")) }
            return
        }
        if (lok == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Lokasi Kerja Lapangan")) }
            return
        }

        // Validasi kelengkapan seluruh sub-form jika ditambahkan
        state.subPlans.forEachIndexed { idx, sub ->
            if (sub.selectedOperator == null || sub.selectedActivity == null || sub.selectedLokasi == null) {
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage.Error("Rencana Tambahan #${idx + 1} belum lengkap. Mohon pilih Operator, Aktivitas, dan Lokasi."))
                }
                return
            }
        }

        val rawTargetDate = if (state.targetDate.isNotBlank()) {
            state.targetDate
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        val normalizedTargetDate = normalizeDateToIso(rawTargetDate)

        viewModelScope.launch {
            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = true)

            val isoClean = normalizedTargetDate.replace("-", "")
            val randCode = ((System.currentTimeMillis() % 9000) + 1000).toInt()
            val baseSpk = "SPK-$isoClean-$randCode"

            data class SubPlanInput(
                val shift: String,
                val opName: String,
                val opNik: String,
                val sapCode: String,
                val actName: String,
                val satuan: String,
                val kodeLokasi: String,
                val keterangan: String
            )

            val primaryInput = SubPlanInput(
                shift = state.shiftKerja.ifBlank { "SIANG" }.trim().uppercase(),
                opName = op.name,
                opNik = op.badgeNumber.ifBlank { op.operatorId },
                sapCode = act.sapCode,
                actName = act.activityName,
                satuan = act.unitOfMeasure.ifBlank { "m3" },
                kodeLokasi = lok.kodeLokasi,
                keterangan = state.keterangan
            )

            val subInputs = state.subPlans.map { sub ->
                val subOpName = sub.selectedOperator?.name ?: op.name
                val subOpNik = sub.selectedOperator?.badgeNumber?.ifBlank { sub.selectedOperator.operatorId } ?: op.badgeNumber
                SubPlanInput(
                    shift = sub.shiftKerja.ifBlank { "SIANG" }.trim().uppercase(),
                    opName = subOpName,
                    opNik = subOpNik,
                    sapCode = sub.selectedActivity?.sapCode ?: "",
                    actName = sub.selectedActivity?.activityName ?: "",
                    satuan = sub.selectedActivity?.unitOfMeasure ?: "m3",
                    kodeLokasi = sub.selectedLokasi?.kodeLokasi ?: lok.kodeLokasi,
                    keterangan = sub.keterangan
                )
            }

            val allRencanaInputList = listOf(primaryInput) + subInputs
            val nowMs = System.currentTimeMillis()

            val rencanaEntities = allRencanaInputList.mapIndexed { index, input ->
                RencanaKerjaEntity(
                    // Jangan set ID agar autoGenerate Room membuat ID unik (1, 2, 3, ...)
                    nomorSpk = "$baseSpk-${index + 1}",
                    tanggalFormat = normalizedTargetDate,
                    shiftKerja = input.shift.uppercase(), // Mengambil shift spesifik dari kartu tersebut (SIANG atau MALAM)
                    kodeUnit = unit.unitCode,
                    wilayah = unit.wilayah ?: "PG1",
                    kasie = _activeSupervisor.value,
                    supervisorName = _activeSupervisor.value,
                    statusUnit = state.statusUnit,
                    operator = input.opName,
                    namaOperator = input.opName,
                    operatorNik = input.opNik,
                    sapCode = input.sapCode,
                    namaAktivitas = input.actName,
                    satuanAktivitas = input.satuan,
                    kodeLokasi = input.kodeLokasi,
                    keterangan = input.keterangan,
                    statusSpk = "TERBIT_SPK",
                    statusTahap = "RENCANA",
                    statusRealisasi = "Menunggu Realisasi",
                    isRealized = false,
                    createdAt = nowMs + index,
                    subIndex = index + 1,
                    parentSpk = baseSpk
                )
            }

            val batchResult = repository.submitBatchRencanaKerja(rencanaEntities)

            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = false)

            batchResult.onSuccess { savedList ->
                val totalPlans = savedList.size
                val planNotice = when {
                    state.isBacklog -> " [Backlog Tertinggal Terselesaikan]"
                    state.isSusulan -> " [Rencana Kerja Susulan Ditambahkan]"
                    else -> ""
                }
                val planType = if (state.isSusulan) "Rencana Kerja Susulan" else "Rencana Kerja"
                _uiEvents.emit(
                    UiMessage.Success("Berhasil membuat $totalPlans $planType untuk Unit ${unit.unitCode} ($normalizedTargetDate)$planNotice")
                )
                // Reset form rencana
                _rencanaFormState.value = RencanaFormUiState(
                    shiftKerja = state.shiftKerja
                )
                // Kembalikan ke daftar rencana kerja di layar agar pengawas melihat daftar rencana yang sudah dibuat
                clearSelectedRencanaForRealisasi()
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan rencana: ${err.message}"))
            }
        }
    }

    // ==========================================
    // TAHAP 2: FORMULIR REALISASI KERJA
    // ==========================================
    fun selectRencanaForRealisasi(rencana: RencanaKerjaEntity) {
        val unitEntity = units.value.find { it.unitCode.equals(rencana.kodeUnit, ignoreCase = true) }

        // 2. KESINAMBUNGAN HM PADA MULTI-RENCANA DALAM 1 UNIT (SEQUENTIAL HM / ESTAFET)
        // Cek riwayat realisasi in-memory pada unit yang sama di shift/sesi ini
        val memoryLatestHasil = hasilAktivitasList.value
            .filter { it.kodeUnit.equals(rencana.kodeUnit, ignoreCase = true) }
            .maxByOrNull { it.createdAt }
        val latestReport = reports.value
            .filter { it.unitCode.equals(rencana.kodeUnit, ignoreCase = true) }
            .maxByOrNull { it.createdAt }
        val latestReportHm = latestReport?.hmAkhir?.takeIf { it > 0.0 }
        val isSeqMem = (memoryLatestHasil != null && memoryLatestHasil.hmAkhir > 0.0) || latestReportHm != null
        val sequentialAwalMem = when {
            memoryLatestHasil != null && memoryLatestHasil.hmAkhir > 0.0 -> memoryLatestHasil.hmAkhir
            latestReportHm != null -> latestReportHm
            else -> null
        }
        val hmAwalVal = sequentialAwalMem ?: unitEntity?.lastHm ?: 0.0
        val sequentialTextMem = if (isSeqMem) "HM Akhir Rencana Sebelumnya: ${String.format(Locale.US, "%.1f", hmAwalVal)}" else ""

        val minStd = unitEntity?.hmMinStandar ?: 6.0
        val maxStd = unitEntity?.hmMaxStandar ?: 10.0

        val matchedActivity = activities.value.find {
            it.sapCode.equals(rencana.sapCode, ignoreCase = true) ||
            it.activityName.equals(rencana.namaAktivitas, ignoreCase = true)
        } ?: if (rencana.sapCode.isNotBlank()) {
            ActivityMasterEntity(
                sapCode = rencana.sapCode,
                activityName = rencana.namaAktivitas.ifBlank { "Aktivitas Lapangan" },
                unitOfMeasure = rencana.satuanAktivitas.ifBlank { "m3" }
            )
        } else null

        val matchedLokasi = lokasiList.value.find {
            it.kodeLokasi.equals(rencana.kodeLokasi, ignoreCase = true)
        } ?: if (rencana.kodeLokasi.isNotBlank()) {
            LokasiEntity(
                kodeLokasi = rencana.kodeLokasi,
                wilayah = "Aktual",
                luasBruto = 0.0,
                luasNetto = 0.0
            )
        } else null

        val rawSpk = rencana.nomorSpk.trim()
        val isPlaceholderSpk = rawSpk.isBlank() ||
                rawSpk.equals("Belum Terbit (Menunggu Admin)", ignoreCase = true) ||
                rawSpk.equals("SPK Menyusul", ignoreCase = true) ||
                rawSpk == "-" ||
                rawSpk.equals("Menunggu SPK", ignoreCase = true) ||
                rawSpk.equals("MENUNGGU", ignoreCase = true)
        val resolvedSpk = if (!isPlaceholderSpk) rawSpk else (CompanyServerSimulator.getPublishedSpk(rencana.id) ?: "")

        _realisasiFormState.value = _realisasiFormState.value.copy(
            selectedRencana = rencana,
            selectedActivity = matchedActivity,
            selectedLokasi = matchedLokasi,
            nomorSpk = resolvedSpk,
            statusSpk = if (resolvedSpk.isNotBlank()) "SPK_TERBIT" else rencana.statusSpk,
            hmAwalOriginal = hmAwalVal,
            hmAwal = hmAwalVal,
            hmAwalInput = String.format(Locale.US, "%.1f", hmAwalVal),
            isHmAwalCorrected = false,
            alasanKoreksiHm = "",
            isSequentialHmFromPreviousPlan = isSeqMem,
            sequentialSourcePlanText = sequentialTextMem,
            clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString(),
            hmAkhirInput = "",
            hmHarianBerjalan = 0.0,
            isHmValid = true,
            hmErrorMessage = null,
            hmMinStandar = minStd,
            hmMaxStandar = maxStd,
            isUnderMinStandard = false,
            selectedKendala = null,
            catatanKendala = "",
            stikSolarAwalInput = "",
            stikSolarAkhirInput = "",
            isIsiSolar = false,
            jumlahLiterSolarInput = "",
            pemakaianSolarOtomatis = 0.0
        )

        // Query database lokal secara asinkron untuk memastikan kesinambungan HM berantai dan auto-restore draft
        viewModelScope.launch {
            val dbUnit = repository.getUnitByCode(rencana.kodeUnit)
            val latestRealizedHm = repository.getLatestRealizedHmForUnit(rencana.kodeUnit)
            val isSeqDb = latestRealizedHm != null && latestRealizedHm > 0.0 && (dbUnit == null || latestRealizedHm >= dbUnit.lastHm)
            val resolvedAwal = latestRealizedHm ?: dbUnit?.lastHm ?: hmAwalVal
            val seqTextDb = if (isSeqDb) "HM Akhir Rencana Sebelumnya: ${String.format(Locale.US, "%.1f", resolvedAwal)}" else ""

            val curState = _realisasiFormState.value
            val effectiveMinStd = dbUnit?.hmMinStandar ?: curState.hmMinStandar
            val effectiveMaxStd = dbUnit?.hmMaxStandar ?: curState.hmMaxStandar

            // Cek apakah terdapat Auto-Save Draft di database lokal untuk rencana/unit ini
            val savedDraft = repository.getDraftForRencana(rencana.id)
            if (savedDraft != null) {
                val restoredAwal = if (savedDraft.isHmAwalCorrected) {
                    savedDraft.hmAwalInput.replace(',', '.').toDoubleOrNull() ?: resolvedAwal
                } else resolvedAwal

                val cleanAkhir = savedDraft.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                val (isValid, running, errorMsg) = validateHm(restoredAwal, cleanAkhir)

                val restoredKendala = if (savedDraft.daftarKendalaJson.isNotBlank()) {
                    val payloads = KendalaJsonConverter.fromJson(savedDraft.daftarKendalaJson)
                    if (payloads.isNotEmpty()) {
                        payloads.map { p ->
                            val kndCode = p.resolvedKode
                            val kndName = p.resolvedNama
                            val matched = kendalaList.value.find { 
                                it.kodeKendala.equals(kndCode, ignoreCase = true) ||
                                KendalaItemPayload.resolveKodeKendala(it.kodeKendala) == kndCode ||
                                it.namaKendala.equals(kndName, ignoreCase = true)
                            }
                            KendalaItemFormState(
                                selectedKendala = matched ?: KendalaEntity(
                                    kodeKendala = kndCode,
                                    namaKendala = kndName,
                                    kategori = "OPERASIONAL"
                                ),
                                waktuMulai = p.resolvedJamMulai,
                                waktuSelesai = p.resolvedJamSelesai
                            )
                        }
                    } else listOf(KendalaItemFormState())
                } else listOf(KendalaItemFormState())

                _realisasiFormState.value = curState.copy(
                    hmMinStandar = effectiveMinStd,
                    hmMaxStandar = effectiveMaxStd,
                    hmAwalOriginal = resolvedAwal,
                    hmAwal = restoredAwal,
                    hmAwalInput = savedDraft.hmAwalInput.ifBlank { String.format(Locale.US, "%.1f", restoredAwal) },
                    isHmAwalCorrected = savedDraft.isHmAwalCorrected,
                    alasanKoreksiHm = savedDraft.alasanKoreksiHm,
                    hmAkhirInput = savedDraft.hmAkhirInput,
                    stikSolarAwalInput = savedDraft.stikSolarAwalInput,
                    stikSolarAkhirInput = savedDraft.stikSolarAkhirInput,
                    isIsiSolar = savedDraft.isIsiSolar,
                    jumlahLiterSolarInput = savedDraft.jumlahLiterSolarInput,
                    daftarKendala = restoredKendala,
                    selectedKendala = restoredKendala.firstOrNull()?.selectedKendala,
                    hasilKerjaInput = savedDraft.hasilKerjaInput,
                    keterangan = savedDraft.keterangan,
                    photoPath = savedDraft.photoPath,
                    photoBase64 = savedDraft.photoBase64,
                    photoSizeKb = savedDraft.photoSizeKb,
                    photoDimensions = savedDraft.photoDimensions,
                    clientTransactionId = savedDraft.clientTransactionId.ifBlank { curState.clientTransactionId },
                    isSequentialHmFromPreviousPlan = isSeqDb || curState.isSequentialHmFromPreviousPlan,
                    sequentialSourcePlanText = if (seqTextDb.isNotBlank()) seqTextDb else curState.sequentialSourcePlanText,
                    isHmValid = isValid,
                    hmHarianBerjalan = running,
                    hmErrorMessage = errorMsg,
                    isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < effectiveMinStd
                )
                recalculateSolarConsumption()
                _uiEvents.emit(UiMessage.Info("Draft inputan formulir ${rencana.kodeUnit} berhasil dipulihkan otomatis."))
            } else {
                val updatedAwal = if (!curState.isHmAwalCorrected) resolvedAwal else curState.hmAwal
                val cleanAkhir = curState.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                val (isValid, running, errorMsg) = validateHm(updatedAwal, cleanAkhir)

                _realisasiFormState.value = curState.copy(
                    hmMinStandar = effectiveMinStd,
                    hmMaxStandar = effectiveMaxStd,
                    hmAwalOriginal = resolvedAwal,
                    hmAwal = updatedAwal,
                    hmAwalInput = if (!curState.isHmAwalCorrected) String.format(Locale.US, "%.1f", resolvedAwal) else curState.hmAwalInput,
                    isSequentialHmFromPreviousPlan = isSeqDb || curState.isSequentialHmFromPreviousPlan,
                    sequentialSourcePlanText = if (seqTextDb.isNotBlank()) seqTextDb else curState.sequentialSourcePlanText,
                    isHmValid = isValid,
                    hmHarianBerjalan = running,
                    hmErrorMessage = errorMsg,
                    isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < effectiveMinStd
                )
            }

            // Jika nomor SPK saat ini masih kosong / placeholder, cek server secara asinkron
            if (_realisasiFormState.value.nomorSpk.isBlank() || _realisasiFormState.value.nomorSpk.equals("SPK Menyusul", ignoreCase = true)) {
                try {
                    val (serverPlans, _) = repository.apiClientManager.fetchActiveRencanaKerja(_activeSupervisor.value)
                    val matchedServer = serverPlans.find {
                        (it.numericId > 0 && it.numericId == rencana.id) ||
                        (rencana.nomorSpk.isNotBlank() && it.nomor_spk?.equals(rencana.nomorSpk, ignoreCase = true) == true)
                    }
                    val serverSpk = matchedServer?.nomor_spk?.trim() ?: ""
                    if (serverSpk.isNotBlank() && !serverSpk.equals("null", ignoreCase = true) && serverSpk != "-" && serverSpk != "SPK Menyusul") {
                        if (rencana.id > 0) repository.updateRencanaSpk(rencana.id, serverSpk, "SPK_TERBIT")
                        withContext(Dispatchers.Main) {
                            val cur = _realisasiFormState.value.selectedRencana
                            if (cur?.id == rencana.id) {
                                _realisasiFormState.value = _realisasiFormState.value.copy(
                                    nomorSpk = serverSpk,
                                    statusSpk = "SPK_TERBIT",
                                    selectedRencana = cur.copy(nomorSpk = serverSpk, statusSpk = "SPK_TERBIT")
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Fitur Koreksi Manual HM Awal & Logika Pergantian Shift
    fun toggleKoreksiHmAwal() {
        val current = _realisasiFormState.value
        val newCorrected = !current.isHmAwalCorrected
        if (newCorrected) {
            _realisasiFormState.value = current.copy(
                isHmAwalCorrected = true,
                hmAwalInput = if (current.hmAwal > 0.0) String.format(Locale.US, "%.1f", current.hmAwal) else ""
            )
        } else {
            // Kembalikan ke HM Awal riwayat sistem default
            val original = current.hmAwalOriginal
            val cleanHmAkhir = current.hmAkhirInput.replace(',', '.').toDoubleOrNull()
            val (isValid, running, errorMsg) = validateHm(original, cleanHmAkhir)
            val isUnderMin = cleanHmAkhir != null && isValid && running > 0.0 && running < current.hmMinStandar
            val kendalaList = if (isUnderMin && current.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else current.daftarKendala

            _realisasiFormState.value = current.copy(
                isHmAwalCorrected = false,
                hmAwal = original,
                hmAwalInput = String.format(Locale.US, "%.1f", original),
                alasanKoreksiHm = "",
                isHmValid = isValid,
                hmHarianBerjalan = running,
                hmErrorMessage = errorMsg,
                isUnderMinStandard = isUnderMin,
                daftarKendala = kendalaList
            )
        }
    }

    fun setRealisasiHmAwalManual(input: String) {
        val state = _realisasiFormState.value
        val cleanInput = input.replace(',', '.')
        val parsedAwal = cleanInput.toDoubleOrNull() ?: 0.0
        val cleanHmAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
        val (isValid, running, errorMsg) = validateHm(parsedAwal, cleanHmAkhir)
        val isUnderMin = cleanHmAkhir != null && isValid && running > 0.0 && running < state.hmMinStandar
        val kendalaList = if (isUnderMin && state.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else state.daftarKendala

        _realisasiFormState.value = state.copy(
            hmAwalInput = cleanInput,
            hmAwal = parsedAwal,
            isHmValid = isValid,
            hmHarianBerjalan = running,
            hmErrorMessage = errorMsg,
            isUnderMinStandard = isUnderMin,
            daftarKendala = kendalaList
        )
        recalculateSolarConsumption()
    }

    fun setRealisasiAlasanKoreksiHm(reason: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(alasanKoreksiHm = reason)
    }

    fun adminSimulatePublishSpk(planId: Long, unitCode: String) {
        viewModelScope.launch {
            val spk = CompanyServerSimulator.adminPublishSpk(planId, unitCode, _activeSupervisor.value)
            repository.updateRencanaSpk(planId, spk, "SPK_TERBIT")
            val cur = _realisasiFormState.value.selectedRencana
            if (cur != null && (cur.id == planId || cur.kodeUnit.equals(unitCode, ignoreCase = true))) {
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    nomorSpk = spk,
                    statusSpk = "SPK_TERBIT",
                    selectedRencana = cur.copy(nomorSpk = spk, statusSpk = "SPK_TERBIT")
                )
            }
            _uiEvents.emit(UiMessage.Success("Web Admin telah menerbitkan SPK: $spk untuk unit $unitCode"))
        }
    }

    fun adminSimulateDeleteRencana(planId: Long, unitCode: String) {
        viewModelScope.launch {
            CompanyServerSimulator.adminDeleteRencana(planId, unitCode, _activeSupervisor.value)
            handleRencanaDeleted(planId.toString(), unitCode)
        }
    }

    /**
     * Memeriksa dan menarik status SPK terbaru secara langsung dari Web Admin API / Simulator
     * untuk rencana kerja yang sedang aktif dipilih pada form realisasi.
     */
    fun fetchLatestSpkForCurrentRencana() {
        val cur = _realisasiFormState.value.selectedRencana
        viewModelScope.launch {
            val targetUnit = cur?.kodeUnit ?: "Semua Unit"
            _uiEvents.emit(UiMessage.Info("Memeriksa penerbitan SPK dari Web Admin ($targetUnit)..."))
            try {
                val pullResult = repository.fetchAndReplaceActiveRencana(_activeSupervisor.value)
                pullResult.onSuccess { (newPlans, _) ->
                    val matching = if (cur != null) {
                        newPlans.find {
                            it.id == cur.id ||
                            (cur.nomorSpk.isNotBlank() && it.nomorSpk.equals(cur.nomorSpk, ignoreCase = true))
                        }
                    } else {
                        newPlans.find { it.isSpkTerbit } ?: newPlans.firstOrNull()
                    }

                    if (matching != null && (matching.isSpkTerbit || (matching.nomorSpk.isNotBlank() && matching.nomorSpk != "SPK Menyusul" && matching.nomorSpk != "-"))) {
                        _realisasiFormState.value = _realisasiFormState.value.copy(
                            nomorSpk = matching.nomorSpk,
                            statusSpk = "SPK_TERBIT",
                            selectedRencana = matching.copy(nomorSpk = matching.nomorSpk, statusSpk = "SPK_TERBIT")
                        )
                        withContext(Dispatchers.IO) {
                            if (matching.id > 0) repository.updateRencanaSpk(matching.id, matching.nomorSpk, "SPK_TERBIT")
                        }
                        _uiEvents.emit(UiMessage.Success("SPK Resmi Diterima dari Web Admin: ${matching.nomorSpk} untuk unit ${matching.kodeUnit}"))
                    } else if (matching != null) {
                        // Periksa simulator jika dalam mode fallback
                        val simSpk = CompanyServerSimulator.getPublishedSpk(matching.id)
                        if (!simSpk.isNullOrBlank()) {
                            setRealisasiNomorSpk(simSpk)
                            _uiEvents.emit(UiMessage.Success("SPK Resmi Diterima dari Web Admin: $simSpk"))
                        } else {
                            _uiEvents.emit(UiMessage.Info("SPK untuk unit ${matching.kodeUnit} belum diterbitkan oleh Web Admin."))
                        }
                    } else {
                        _uiEvents.emit(UiMessage.Info("Belum ada rencana kerja aktif yang ditemukan di server."))
                    }
                }.onFailure {
                    val simSpk = cur?.let { CompanyServerSimulator.getPublishedSpk(it.id) }
                    if (!simSpk.isNullOrBlank()) {
                        setRealisasiNomorSpk(simSpk)
                        _uiEvents.emit(UiMessage.Success("SPK Resmi Diterima dari Web Admin: $simSpk"))
                    } else {
                        _uiEvents.emit(UiMessage.Info("SPK untuk unit $targetUnit belum diterbitkan oleh Web Admin."))
                    }
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal memeriksa SPK: ${e.localizedMessage ?: e.message}"))
            }
        }
    }

    fun setRealisasiActivity(activity: ActivityMasterEntity) {
        val autoStatus = activity.kategoriStatus.ifBlank { "OPERASI" }
        val curRencana = _realisasiFormState.value.selectedRencana
        _realisasiFormState.value = _realisasiFormState.value.copy(
            selectedActivity = activity,
            selectedRencana = curRencana?.copy(statusUnit = autoStatus)
        )
    }

    fun setRealisasiLokasi(lokasi: LokasiEntity) {
        _realisasiFormState.value = _realisasiFormState.value.copy(selectedLokasi = lokasi)
    }

    fun setRealisasiNomorSpk(spk: String) {
        val trimmed = spk.trim()
        val cur = _realisasiFormState.value.selectedRencana
        val newStatus = if (trimmed.isNotBlank() && trimmed != "SPK Menyusul" && trimmed != "-") "SPK_TERBIT" else "MENUNGGU_SPK"
        _realisasiFormState.value = _realisasiFormState.value.copy(
            nomorSpk = spk,
            statusSpk = newStatus,
            selectedRencana = cur?.copy(nomorSpk = spk, statusSpk = newStatus)
        )
        if (cur != null && cur.id > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateRencanaSpk(cur.id, spk, newStatus)
            }
        }
    }

    fun setRealisasiDurasiJam(durasi: Double) {
        _realisasiFormState.value = _realisasiFormState.value.copy(durasiJamKerja = durasi)
    }

    fun setRealisasiHmAkhir(input: String) {
        val state = _realisasiFormState.value
        val hmAwal = state.hmAwal
        val cleanInput = input.replace(',', '.')
        val inputDouble = cleanInput.toDoubleOrNull()

        val (isValid, running, errorMsg) = validateHm(hmAwal, inputDouble)
        val isUnderMin = inputDouble != null && isValid && running > 0.0 && running < state.hmMinStandar
        val kendalaList = if (isUnderMin && state.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else state.daftarKendala

        _realisasiFormState.value = state.copy(
            hmAkhirInput = cleanInput,
            isHmValid = isValid,
            hmHarianBerjalan = running,
            hmErrorMessage = errorMsg,
            isUnderMinStandard = isUnderMin,
            daftarKendala = kendalaList
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun quickAddRealisasiHours(hours: Double) {
        val hmAwal = _realisasiFormState.value.hmAwal
        val newHmAkhir = hmAwal + hours
        setRealisasiHmAkhir(String.format(Locale.US, "%.1f", newHmAkhir))
    }

    // Parameter Stik Solar BBM
    fun setRealisasiStikAwal(input: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            stikSolarAwalInput = input.replace(',', '.')
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiStikAkhir(input: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            stikSolarAkhirInput = input.replace(',', '.')
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiIsiSolar(isIsi: Boolean) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            isIsiSolar = isIsi,
            jumlahLiterSolarInput = if (!isIsi) "" else _realisasiFormState.value.jumlahLiterSolarInput
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiJumlahSolar(liters: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            jumlahLiterSolarInput = liters.replace(',', '.')
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    private fun recalculateSolarConsumption() {
        val state = _realisasiFormState.value
        val awal = state.parsedStikAwal ?: 0.0
        val akhir = state.parsedStikAkhir ?: 0.0
        val isi = if (state.isIsiSolar) (state.parsedJumlahLiterSolar ?: 0.0) else 0.0
        // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
        val total = (awal + isi) - akhir
        _realisasiFormState.value = _realisasiFormState.value.copy(
            pemakaianSolarOtomatis = total
        )
    }

    // Master Kendala Dropdown Selection (Multi-Kendala Support)
    fun selectRealisasiKendala(kendala: KendalaEntity) {
        updateKendalaSelection(0, kendala)
    }

    fun addKendalaRow() {
        val current = _realisasiFormState.value.daftarKendala
        _realisasiFormState.value = _realisasiFormState.value.copy(
            daftarKendala = current + KendalaItemFormState()
        )
        autoSaveDraftCurrentRealisasi()
    }

    fun removeKendalaRow(index: Int) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            if (current.isEmpty()) {
                current.add(KendalaItemFormState())
            }
            _realisasiFormState.value = _realisasiFormState.value.copy(
                daftarKendala = current,
                selectedKendala = current.firstOrNull()?.selectedKendala
            )
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun updateKendalaSelection(index: Int, kendala: KendalaEntity) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(selectedKendala = kendala)
            _realisasiFormState.value = _realisasiFormState.value.copy(
                daftarKendala = current,
                selectedKendala = current.firstOrNull()?.selectedKendala
            )
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun updateKendalaWaktuMulai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuMulai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun updateKendalaWaktuSelesai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuSelesai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun setRealisasiCatatanKendala(notes: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(catatanKendala = notes)
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiHasilKerja(hasil: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            hasilKerjaInput = hasil,
            isHasilKerjaError = false
        )
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiHasilKerjaError(isError: Boolean) {
        _realisasiFormState.value = _realisasiFormState.value.copy(isHasilKerjaError = isError)
    }

    fun setRealisasiKeterangan(ket: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(keterangan = ket)
        autoSaveDraftCurrentRealisasi()
    }

    fun processAndCompressRealisasiPhoto(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _realisasiFormState.value = _realisasiFormState.value.copy(isCompressingPhoto = true)
            val result = ImageCompressor.compressImage(context, imageUri)
            _realisasiFormState.value = _realisasiFormState.value.copy(isCompressingPhoto = false)
            result.onSuccess { compressed ->
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    photoPath = compressed.filePath,
                    photoBase64 = compressed.base64,
                    photoSizeKb = compressed.sizeKb,
                    photoDimensions = compressed.dimensionsText
                )
                autoSaveDraftCurrentRealisasi()
                _uiEvents.emit(
                    UiMessage.Success("Foto bukti terkompresi: ${compressed.sizeKb} KB (${compressed.dimensionsText})")
                )
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal kompresi foto: ${err.message}"))
            }
        }
    }

    fun removeRealisasiPhoto() {
        val old = _realisasiFormState.value.photoPath
        if (!old.isNullOrBlank()) {
            try { File(old).delete() } catch (_: Exception) {}
        }
        _realisasiFormState.value = _realisasiFormState.value.copy(
            photoPath = null,
            photoBase64 = null,
            photoSizeKb = null,
            photoDimensions = null
        )
        autoSaveDraftCurrentRealisasi()
    }

    // ======================================================================
    // 1. AUTO-SAVE DRAFT FORMULIR REALISASI (ANTI-HILANG DATA)
    // ======================================================================
    fun autoSaveDraftCurrentRealisasi() {
        val state = _realisasiFormState.value
        val ren = state.selectedRencana ?: return
        viewModelScope.launch {
            val kendalaPayloads = state.daftarKendala
                .filter { it.selectedKendala != null || it.waktuMulai.isNotBlank() || it.waktuSelesai.isNotBlank() }
                .map { item ->
                    val knd = item.selectedKendala
                    val minutes = item.durasiMenit ?: 0
                    val hours = Math.round((minutes / 60.0) * 10.0) / 10.0
                    KendalaItemPayload.create(
                        namaKendala = knd?.namaKendala ?: "",
                        kodeKendala = knd?.kodeKendala ?: "",
                        jamMulai = item.waktuMulai,
                        jamSelesai = item.waktuSelesai,
                        durasiJam = hours,
                        durasiMenit = minutes
                    )
                }
            val jsonKendala = if (kendalaPayloads.isNotEmpty()) KendalaJsonConverter.toJson(kendalaPayloads) else ""

            val draft = com.example.data.local.entity.DraftRealisasiEntity(
                rencanaId = ren.id,
                kodeUnit = ren.kodeUnit,
                hmAwalInput = state.hmAwalInput,
                isHmAwalCorrected = state.isHmAwalCorrected,
                alasanKoreksiHm = state.alasanKoreksiHm,
                hmAkhirInput = state.hmAkhirInput,
                stikSolarAwalInput = state.stikSolarAwalInput,
                stikSolarAkhirInput = state.stikSolarAkhirInput,
                isIsiSolar = state.isIsiSolar,
                jumlahLiterSolarInput = state.jumlahLiterSolarInput,
                daftarKendalaJson = jsonKendala,
                hasilKerjaInput = state.hasilKerjaInput,
                keterangan = state.keterangan,
                photoPath = state.photoPath,
                photoBase64 = state.photoBase64,
                photoSizeKb = state.photoSizeKb,
                photoDimensions = state.photoDimensions,
                clientTransactionId = state.clientTransactionId,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDraftForRencana(draft)
        }
    }

    fun clearSelectedRencanaForRealisasi() {
        _realisasiFormState.value = RealisasiFormUiState(
            clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
        )
    }

    fun resetRealisasiFormManual() {
        val currentRencana = _realisasiFormState.value.selectedRencana
        if (currentRencana != null) {
            viewModelScope.launch {
                repository.deleteDraftForRencana(currentRencana.id)
                // Inisialisasi ulang form untuk rencana terpilih
                selectRencanaForRealisasi(currentRencana)
                _uiEvents.emit(UiMessage.Info("Formulir unit ${currentRencana.kodeUnit} berhasil direset."))
            }
        } else {
            _realisasiFormState.value = RealisasiFormUiState(
                clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
            )
        }
    }

    fun onSubmitButtonClicked(request: com.example.data.remote.RealisasiRequest, context: Context) {
        viewModelScope.launch {
            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = true)
            val result = repository.submitRealisasiLangsung(request, context)
            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = false)

            result.onSuccess {
                android.widget.Toast.makeText(context, "Realisasi berhasil terkirim ke Web Admin!", android.widget.Toast.LENGTH_SHORT).show()
                _uiEvents.emit(UiMessage.Success("Realisasi berhasil terkirim ke Web Admin!"))
                _realisasiFormState.value = RealisasiFormUiState(
                    clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
                )
            }.onFailure { err ->
                android.widget.Toast.makeText(
                    context,
                    "Gagal sinkron ke server: ${err.message}. Data tersimpan lokal.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                _uiEvents.emit(UiMessage.Error("Gagal sinkron ke server: ${err.message}. Data tersimpan lokal."))
                _realisasiFormState.value = RealisasiFormUiState(
                    clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
                )
            }
        }
    }

    fun submitRealisasiKerja(context: Context? = null) {
        val state = _realisasiFormState.value
        // Kunci tombol kirim setelah 1x klik (loading state & anti-duplikasi)
        if (state.isSubmitting) return

        val rencana = state.selectedRencana
        if (rencana == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Rencana Kerja aktif terlebih dahulu")) }
            return
        }

        val hmAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
        if (hmAkhir == null || hmAkhir <= state.hmAwal) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("HM Akhir wajib diisi dan lebih besar dari HM Awal (${String.format(Locale.US, "%.1f", state.hmAwal)})"))
            }
            return
        }

        // Validasi Alasan Koreksi HM Awal jika diedit manual
        if (state.isHmAwalCorrected && state.alasanKoreksiHm.isBlank()) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Alasan koreksi HM Awal wajib diisi jika koreksi manual HM Awal diaktifkan."))
            }
            return
        }

        // Validasi Tumpang Tindih (Anti-Overlapping) Jam Kendala
        if (state.hasAnyKendalaOverlap) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Jam kendala bertabrakan dengan kendala sebelumnya! Perbaiki jam input kendala."))
            }
            return
        }

        // Validasi Batas Maksimal Shift untuk Kendala
        if (state.isTotalKendalaExceedingShift) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Akumulasi total durasi kendala (${state.totalHambatanLabel}) melebihi durasi jam kerja shift (${String.format(Locale.US, "%.1f", state.maxShiftHours)} Jam)!"))
            }
            return
        }

        // 1. Validasi Pemakaian Solar BBM
        if (state.isRefuelLitersMissing) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Pengisian BBM tambahan aktif: Wajib mengisi Jumlah Liter Pengisian BBM (> 0)"))
            }
            return
        }

        if (state.isSolarConsumptionNegative) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Konsumsi solar tidak boleh bernilai negatif (${String.format(Locale.US, "%.1f", state.computedKonsumsiSolar)} L). Periksa kembali stik solar dan pengisian BBM."))
            }
            return
        }

        val runningHm = hmAkhir - state.hmAwal
        val isUnderMin = runningHm > 0.0 && runningHm < state.hmMinStandar

        // Validasi HM Kurang dari Standar Minimal:
        // Wajib minimal 1 kendala lengkap dengan waktu mulai & selesai
        if (isUnderMin) {
            if (state.daftarKendala.isEmpty() || !state.daftarKendala.all { it.isValid }) {
                viewModelScope.launch {
                    val invalidReason = when {
                        state.daftarKendala.isEmpty() -> "Wajib mencatat minimal 1 kendala lapangan."
                        state.daftarKendala.any { it.selectedKendala == null } -> "Penyebab kendala wajib dipilih dari Master Kendala."
                        state.daftarKendala.any { it.waktuMulai.isBlank() || it.waktuSelesai.isBlank() } -> "Waktu mulai dan selesai kendala wajib diisi (JJ:MM)."
                        state.daftarKendala.any { !it.isTimeRangeValid } -> "Rentang jam kendala tidak valid (JJ:MM)."
                        else -> "Lengkapi data kendala dan rentang waktu kejadian."
                    }
                    _uiEvents.emit(
                        UiMessage.Error("HM Berjalan (${String.format(Locale.US, "%.1f", runningHm)} HM) di bawah standar (${state.hmMinStandar} HM). $invalidReason")
                    )
                }
                return
            }
        }

        val fuelLiters = if (state.isIsiSolar) {
            val parsed = state.parsedJumlahLiterSolar
            if (parsed == null || parsed <= 0.0) {
                viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pengisian solar aktif: Isi Jumlah Liter Solar (> 0)")) }
                return
            }
            parsed
        } else {
            0.0
        }

        val resolvedStatusUnit = state.selectedActivity?.kategoriStatus?.ifBlank { "OPERASI" }
            ?: rencana.statusUnit.ifBlank { "OPERASI" }

        // Fleksibilitas Kondisi Lapangan: Gunakan Aktivitas & Lokasi aktual jika diedit oleh pengawas
        val actName = state.selectedActivity?.activityName
            ?: rencana.namaAktivitas.ifBlank { "Aktivitas Operasi Lapangan" }
        val sapCode = state.selectedActivity?.sapCode
            ?: rencana.sapCode.ifBlank { "ACT-OPR-01" }
        val satuan = state.selectedActivity?.unitOfMeasure?.ifBlank { rencana.satuanAktivitas.ifBlank { "m3" } }
            ?: rencana.satuanAktivitas.ifBlank { "m3" }
        val lokasiAktual = state.selectedLokasi?.kodeLokasi
            ?: rencana.kodeLokasi

        val isOperasi = resolvedStatusUnit.equals("OPERASI", ignoreCase = true)
        val workOutput = safeParseDouble(state.hasilKerjaInput)

        // VALIDASI ATURAN OPERASI: Jika kategori = "OPERASI" dan Total HM > 0, Hasil Kerja WAJIB DIISI (> 0)
        if (isOperasi && runningHm > 0.0 && workOutput <= 0.0) {
            _realisasiFormState.value = _realisasiFormState.value.copy(isHasilKerjaError = true)
            viewModelScope.launch {
                _uiEvents.emit(
                    UiMessage.Error("Hasil kerja ($satuan) wajib diisi untuk aktivitas Operasi!")
                )
            }
            return
        }

        val stikAwal = state.parsedStikAwal ?: 0.0
        val stikAkhir = state.parsedStikAkhir ?: 0.0

        val validKendalaList: List<KendalaItemFormState> = state.daftarKendala.filter { it.isValid }

        val kendalaPayloads = validKendalaList.map { item ->
            val knd = item.selectedKendala
            val minutes = item.durasiMenit ?: 0
            val hours = Math.round((minutes / 60.0) * 10.0) / 10.0
            KendalaItemPayload.create(
                namaKendala = knd?.namaKendala ?: "",
                kodeKendala = knd?.kodeKendala ?: "",
                jamMulai = item.waktuMulai,
                jamSelesai = item.waktuSelesai,
                durasiJam = hours,
                durasiMenit = minutes
            )
        }

        val daftarKendalaJson = if (kendalaPayloads.isNotEmpty()) {
            KendalaJsonConverter.toJson(kendalaPayloads)
        } else {
            null
        }

        val primaryKendala = validKendalaList.firstOrNull()?.selectedKendala
        val kendalaSummaryName = if (validKendalaList.isNotEmpty()) {
            validKendalaList.joinToString(", ") { "${it.selectedKendala?.namaKendala} (${it.waktuMulai}-${it.waktuSelesai})" }
        } else {
            null
        }
        val kendalaSummaryCatatan = if (validKendalaList.isNotEmpty()) {
            "Total ${validKendalaList.size} Kendala [Durasi: ${state.totalHambatanLabel}]: " +
                    validKendalaList.joinToString("; ") {
                        "${it.selectedKendala?.namaKendala} (${it.waktuMulai} - ${it.waktuSelesai}, ${it.durasiLabel})"
                    }
        } else {
            null
        }

        // Kunci tombol kirim secara langsung
        _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = true)

        viewModelScope.launch {
            val txId = state.clientTransactionId.ifBlank { "TX-" + java.util.UUID.randomUUID().toString() }
            val session = _currentSupervisorSession.value
            val isOp = session?.isOperator == true
            val resolvedKasie = if (isOp) {
                session?.kasieAtasan?.ifBlank { rencana.kasie.ifBlank { rencana.supervisorName } } ?: rencana.supervisorName
            } else {
                _activeSupervisor.value.ifBlank { rencana.supervisorName }
            }
            val resolvedCreatedBy = session?.username?.ifBlank { session.resolvedNamaLengkap } ?: resolvedKasie
            val resolvedOperatorNik = if (isOp) {
                session?.nik?.ifBlank { session.username } ?: rencana.operatorNik
            } else {
                rencana.operatorNik.ifBlank { rencana.operator }
            }

            val effectiveSpk = state.nomorSpk.ifBlank { rencana.nomorSpk }.ifBlank { "SPK-AUTO-${rencana.id}" }
            val isDirectKasieSubmit = !isOp
            val hasilEntity = HasilInputAktivitasEntity(
                rencanaId = rencana.id,
                nomorSpk = effectiveSpk,
                supervisorName = resolvedKasie,
                namaKasie = resolvedKasie,
                createdBy = resolvedCreatedBy,
                kodeUnit = rencana.kodeUnit,
                operator = rencana.operator,
                kodeLokasi = lokasiAktual,
                activityName = actName,
                sapCode = sapCode,
                satuan = satuan,
                unitStatus = resolvedStatusUnit,
                shift = rencana.shiftKerja,
                jamKerja = state.durasiJamKerja,
                hmAwal = state.hmAwal,
                hmAkhir = hmAkhir,
                hmHarianBerjalan = runningHm,
                isIsiSolar = state.isIsiSolar,
                jumlahLiterSolar = fuelLiters,
                stikSolarAwal = stikAwal,
                stikSolarAkhir = stikAkhir,
                pemakaianSolar = state.computedKonsumsiSolar,
                kendalaId = primaryKendala?.kodeKendala,
                kendalaNama = kendalaSummaryName,
                catatanKendala = kendalaSummaryCatatan,
                daftarKendalaJson = daftarKendalaJson,
                hasilKerja = workOutput,
                keterangan = state.keterangan,
                photoPath = state.photoPath,
                photoBase64 = state.photoBase64,
                photoSizeKb = state.photoSizeKb,
                photoDimensions = state.photoDimensions,
                createdAt = System.currentTimeMillis(),
                tanggalFormat = normalizeDateToIso(rencana.tanggalFormat.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }),
                isHmAwalCorrected = state.isHmAwalCorrected,
                alasanKoreksiHm = if (state.isHmAwalCorrected) state.alasanKoreksiHm.trim().ifBlank { "Koreksi manual HM Awal" } else null,
                approvalStatus = "Menunggu Approval",
                approvedBy = null,
                approvedAt = null,
                isLocked = false,
                operatorNik = resolvedOperatorNik,
                clientTransactionId = txId,
                statusKirim = "PENDING"
            )

            val result = repository.submitHasilAktivitas(hasilEntity)

            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = false)

            result.onSuccess { saved ->
                context?.let {
                    android.widget.Toast.makeText(it, "Realisasi berhasil terkirim ke Web Admin!", android.widget.Toast.LENGTH_SHORT).show()
                }
                val fuelTag = if (saved.isIsiSolar) " [Solar: ${saved.jumlahLiterSolar}L]" else ""
                val spkTag = if (saved.nomorSpk.isNotBlank()) " (SPK: ${saved.nomorSpk})" else ""
                val kendalaTag = if (!saved.kendalaNama.isNullOrBlank()) " [Kendala: ${saved.kendalaNama}]" else ""
                _uiEvents.emit(
                    UiMessage.Success("Realisasi Unit ${saved.kodeUnit}$spkTag$fuelTag$kendalaTag berhasil terkirim ke Web Admin!")
                )

                // Bersihkan draft lokal jika berhasil dikirim ke server (status SYNCED)
                if (saved.statusKirim == "SYNCED") {
                    repository.deleteDraftForRencana(saved.rencanaId)
                }

                // Reset realisasi form dengan Client Transaction ID baru
                _realisasiFormState.value = RealisasiFormUiState(
                    clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
                )
            }.onFailure { err ->
                context?.let {
                    android.widget.Toast.makeText(
                        it,
                        "Gagal sinkron ke server: ${err.message}. Data tersimpan lokal.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                _uiEvents.emit(UiMessage.Error("Gagal sinkron ke server: ${err.message}. Data tersimpan lokal."))

                // Reset realisasi form agar kembali ke daftar tugas dan menampilkan item di Riwayat sebagai Tertunda Sync
                _realisasiFormState.value = RealisasiFormUiState(
                    clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
                )
            }
        }
    }

    private fun validateHm(hmAwal: Double, hmAkhir: Double?): Triple<Boolean, Double, String?> {
        if (hmAkhir == null) {
            return Triple(true, 0.0, null)
        }
        return if (hmAkhir <= hmAwal) {
            val diff = hmAwal - hmAkhir
            val msg = if (hmAkhir == hmAwal) {
                "HM Akhir (${String.format(Locale.US, "%.1f", hmAkhir)}) harus lebih besar dari HM Awal (${String.format(Locale.US, "%.1f", hmAwal)})"
            } else {
                "HM Akhir (${String.format(Locale.US, "%.1f", hmAkhir)}) lebih kecil dari HM Awal (${String.format(Locale.US, "%.1f", hmAwal)}) selisih -${String.format(Locale.US, "%.1f", diff)}"
            }
            Triple(
                false,
                0.0,
                msg
            )
        } else {
            val running = hmAkhir - hmAwal
            if (running > 24.0) {
                Triple(
                    false,
                    running,
                    "Lonjakan HM melebihi 24 jam dalam 1 shift! Periksa kembali angka meteran HM Akhir."
                )
            } else {
                Triple(true, running, null)
            }
        }
    }

    // ==========================================
    // MODUL DISTRIBUSI & PEMASANGAN INSTALLER APK
    // ==========================================
    fun getAppVersionName(context: Context): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.6"
        } catch (_: Exception) {
            "1.6"
        }
    }

    fun getApkFileSizeFormatted(context: Context): String {
        return try {
            val sourcePath = context.applicationInfo.sourceDir
            if (!sourcePath.isNullOrEmpty()) {
                val f = File(sourcePath)
                if (f.exists() && f.length() > 0) {
                    val mb = f.length().toDouble() / (1024 * 1024)
                    return String.format(Locale.getDefault(), "%.1f MB", mb)
                }
            }
            "18.4 MB"
        } catch (_: Exception) {
            "18.4 MB"
        }
    }

    fun copyApkUrlToClipboard(context: Context, url: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("URL Installer RKCE", url)
            clipboard.setPrimaryClip(clip)
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Success("Tautan unduh APK pembaruan berhasil disalin ke papan klip!"))
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Gagal menyalin tautan: ${e.message}"))
            }
        }
    }

    fun getOrExtractApkFile(context: Context): File? {
        return try {
            val sourcePath = context.applicationInfo.sourceDir
            if (sourcePath.isNullOrEmpty()) return null
            val sourceApk = File(sourcePath)
            if (!sourceApk.exists() || sourceApk.length() == 0L) return null

            val vName = getAppVersionName(context)
            val targetFileName = "RKCE_Installer_v${vName}.apk"

            val cacheShareDir = File(context.cacheDir, "apk_share").apply { mkdirs() }
            val targetApk = File(cacheShareDir, targetFileName)

            // Bersihkan berkas usang dan salin ulang dari APK terbaru
            cacheShareDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk") && file.name != targetFileName) {
                    file.delete()
                }
            }
            if (!targetApk.exists() || targetApk.length() != sourceApk.length() || targetApk.lastModified() < sourceApk.lastModified()) {
                sourceApk.copyTo(targetApk, overwrite = true)
            }
            targetApk
        } catch (_: Exception) {
            null
        }
    }

    fun downloadApk(context: Context) {
        viewModelScope.launch {
            try {
                val vName = getAppVersionName(context)
                val fileName = "RKCE_Installer_v${vName}.apk"
                var savedLocally = false
                val sourcePath = context.applicationInfo.sourceDir
                val sourceApk = if (!sourcePath.isNullOrEmpty()) File(sourcePath) else null

                if (sourceApk != null && sourceApk.exists() && sourceApk.length() > 0L) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { output ->
                                sourceApk.inputStream().use { input -> input.copyTo(output) }
                            }
                            savedLocally = true
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        downloadsDir.mkdirs()
                        val destFile = File(downloadsDir, fileName)
                        sourceApk.copyTo(destFile, overwrite = true)
                        savedLocally = true
                    }
                }

                if (savedLocally) {
                    _uiEvents.emit(
                        UiMessage.Success(
                            "Installer pembaruan '$fileName' berhasil disimpan di folder Download! Berkas versi terbaru utuh dan siap dipasang atau dikirim ke HP lain."
                        )
                    )
                } else {
                    // Fallback unduh via DownloadManager dari URL resmi
                    val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                    val request = DownloadManager.Request(Uri.parse(downloadUrl))
                        .setTitle(fileName)
                        .setDescription("Mengunduh installer resmi RKCE pembaruan aplikasi...")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    dm?.enqueue(request)
                    _uiEvents.emit(UiMessage.Success("Memulai unduhan berkas APK pembaruan resmi ke folder Download perangkat..."))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan installer: ${e.localizedMessage ?: "Periksa izin penyimpanan"}"))
            }
        }
    }

    fun shareApkViaWhatsApp(context: Context) {
        shareApk(context, targetWhatsAppOnly = true)
    }

    fun shareApkGeneral(context: Context) {
        shareApk(context, targetWhatsAppOnly = false)
    }

    private fun shareApk(context: Context, targetWhatsAppOnly: Boolean) {
        viewModelScope.launch {
            try {
                val vName = getAppVersionName(context)
                val fileName = "RKCE_Installer_v${vName}.apk"
                val apkFile = getOrExtractApkFile(context)
                val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                val shareCaption = """
*Aplikasi RKCE (Rencana Kerja Civil Engineering) - Pembaruan v$vName*

Halo Rekan Pengawas & Operator, berkas installer APK pembaruan aplikasi resmi terlampir ($fileName). Anda dapat langsung memasang atau memperbarui aplikasi ini di HP Android Anda.

*FITUR PEMBARUAN TERBARU (v$vName):*
- Validasi Antrean Strict FIFO (Tombol realisasi terkunci jika ada tugas backlog)
- Penguncian & Pewarisan Otomatis Tanggal Kerja & Shift Rencana ke Realisasi
- Dukungan penuh Login Kasie & 35 Akun NIK Operator (contoh: NIK 85044295)
- Fitur Approval & Penguncian Permanen Laporan Realisasi oleh Kasie
- Sinkronisasi instan Top Bar (🔄) & akses penuh 3.614 data lokasi kerja

*PANDUAN PEMBARUAN & AGAR BISA DIINSTAL DI HP LAIN:*
1. Buka atau klik berkas *$fileName* yang terlampir.
2. Jika muncul peringatan keamanan sistem Android, pilih *'Setelan' / 'Pengaturan'* lalu aktifkan *'Izinkan dari sumber ini'*.
3. Jika muncul notifikasi Google Play Protect ('Aplikasi tidak dikenal' / 'Blocked by Play Protect'):
   👉 Klik *'Detail Lainnya'* (More details)
   👉 Pilih *'Tetap Instal'* (Install anyway).
4. *PENTING SAAT PEMBARUAN APLIKASI:*
   Jika sebelumnya sudah ada versi RKCE yang terpasang di HP dan muncul pesan 'Aplikasi tidak terpasang' (App not installed / bentrok versi lama), silakan copot (uninstall) dulu versi lama tersebut, lalu pasang berkas pembaruan ini.

*Tautan Unduh Resmi Pembaruan:*
- Portal Unduh Web Admin: $WEB_ADMIN_APK_DOWNLOAD_PAGE
- Unduh Langsung APK: $downloadUrl
- Halaman Rilis GitHub: $GITHUB_RELEASES_PAGE_URL
                """.trimIndent()

                if (apkFile != null && apkFile.exists()) {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.android.package-archive"
                        putExtra(Intent.EXTRA_STREAM, apkUri)
                        putExtra(Intent.EXTRA_TEXT, shareCaption)
                        putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v$vName")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (targetWhatsAppOnly) {
                            setPackage("com.whatsapp")
                        }
                    }

                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val chooser = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.android.package-archive"
                                putExtra(Intent.EXTRA_STREAM, apkUri)
                                putExtra(Intent.EXTRA_TEXT, shareCaption)
                                putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v$vName")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                            "Bagikan Berkas Installer RKCE"
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }
                } else {
                    // Fallback kirim link teks jika berkas tidak dapat diekstrak
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareCaption)
                        if (targetWhatsAppOnly) {
                            setPackage("com.whatsapp")
                        }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val chooser = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareCaption)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                            "Bagikan Tautan Unduh RKCE"
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Tidak dapat membagikan installer: ${e.message}"))
            }
        }
    }

    fun launchApkInstaller(context: Context) {
        viewModelScope.launch {
            try {
                val apkFile = getOrExtractApkFile(context)
                if (apkFile != null && apkFile.exists()) {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(installIntent)
                } else {
                    _uiEvents.emit(UiMessage.Error("Berkas installer belum siap diekstrak."))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal membuka penginstal: ${e.message}"))
            }
        }
    }

    fun refreshAnalyticsData(context: Context? = null) {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            val currentSupervisor = _activeSupervisor.value
            val session = _currentSupervisorSession.value
            val kasieName = session?.resolvedNamaLengkap?.ifBlank { currentSupervisor } ?: currentSupervisor
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            // 1. Kirim ulang item lokal yang is_synced == false ke server Web Admin terlebih dahulu
            val syncedPendingCount = repository.syncAllPendingOperationalData().getOrDefault(0)
            // 2. Tarik data terbaru dari server
            repository.fetchBootstrapData(currentSupervisor, todayStr)
            repository.fetchAndReplaceActiveRencana(currentSupervisor)
            repository.fetchPendingRealisasiForKasie(kasieName)
            _isSyncingMaster.value = false
            val syncMsg = if (syncedPendingCount > 0) {
                "Berhasil mengirim $syncedPendingCount laporan tertunda & menyinkronkan Riwayat dari Web Admin!"
            } else {
                "Data Analitik & Riwayat berhasil disinkronkan secara penuh dari server Web Admin"
            }
            context?.let {
                android.widget.Toast.makeText(it, syncMsg, android.widget.Toast.LENGTH_SHORT).show()
            }
            _uiEvents.emit(UiMessage.Success(syncMsg))
        }
    }

    // ==========================================
    // FLEET ANALYTICS CALCULATION (ISOLASI DATA)
    // ==========================================
    fun setAnalyticsPeriod(period: String) {
        _analyticsPeriod.value = period
    }

    fun setAnalyticsUnitFilter(unit: String) {
        _analyticsUnitFilter.value = unit
    }

    fun matchesJenisUnit(unitType: String, filter: String): Boolean {
        if (!com.example.data.model.UnitConstants.isJenisUnitValid(unitType)) return false
        if (filter == "Semua Jenis" || filter.isBlank()) return true
        val u = unitType.uppercase()
        val f = filter.uppercase()
        return when {
            f.contains("LONG ARM") -> u.contains("LONG ARM")
            f.contains("MINI") -> u.contains("MINI")
            f.contains("STANDAR") -> u.contains("STANDAR") || (u.contains("EXCAVATOR") && !u.contains("LONG") && !u.contains("MINI"))
            f.contains("EXCAVATOR") || f.contains("HE") -> u.contains("EXCAVATOR") || u.contains("HE")
            f.contains("GRADER") -> u.contains("GRADER")
            f.contains("COMPACTOR") -> u.contains("COMPACTOR")
            else -> u.contains(f) || f.contains(u)
        }
    }

    private fun calculateAnalytics(
        allReports: List<ActivityReportEntity>,
        period: String,
        unitFilter: String,
        wilayahFilter: String = "Semua Wilayah",
        jenisFilter: String = "Semua Jenis"
    ): FleetAnalyticsState {
        // Filter by Period
        val now = System.currentTimeMillis()
        val periodFiltered = allReports.filter { report ->
            when (period) {
                "30 Hari" -> (now - report.createdAt) <= (30L * 24 * 60 * 60 * 1000)
                "Bulan Ini" -> {
                    val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
                    report.reportDate.contains(currentMonth) || (now - report.createdAt) <= (31L * 24 * 60 * 60 * 1000)
                }
                "Custom Range" -> (now - report.createdAt) <= (14L * 24 * 60 * 60 * 1000) // 14 hari terakhir
                else -> true
            }
        }

        // Filter by Wilayah
        val wilayahFiltered = if (wilayahFilter == "Semua Wilayah" || wilayahFilter.isBlank()) {
            periodFiltered
        } else {
            periodFiltered.filter { WilayahConstants.matches(it.wilayah, wilayahFilter) }
        }

        // Filter by Jenis Unit
        val jenisFiltered = if (jenisFilter == "Semua Jenis" || jenisFilter.isBlank()) {
            wilayahFiltered
        } else {
            wilayahFiltered.filter { matchesJenisUnit(it.unitType, jenisFilter) }
        }

        // Filter by Unit
        val filtered = if (unitFilter == "Semua Unit" || unitFilter.isBlank()) {
            jenisFiltered
        } else {
            jenisFiltered.filter { it.unitCode.equals(unitFilter, ignoreCase = true) }
        }

        val totalSolar = filtered.sumOf {
            when {
                it.pemakaianSolar > 0.0 -> it.pemakaianSolar
                it.isRefueled -> it.fuelLiters
                else -> 0.0
            }
        }
        val totalHm = filtered.sumOf { it.hmDailyRunning }
        val fuelRatio = if (totalHm > 0.0) totalSolar / totalHm else 0.0

        val operasiHours = filtered.filter { it.unitStatus == "OPERASI" }.sumOf { it.workDurationHours }
        val standbyHours = filtered.filter { it.unitStatus == "STANDBY" }.sumOf { it.workDurationHours }
        val breakdownHours = filtered.filter { it.unitStatus == "BREAKDOWN" }.sumOf { it.workDurationHours }
        val totalHours = operasiHours + standbyHours + breakdownHours

        val opPercent = if (totalHours > 0) (operasiHours / totalHours).toFloat() else 0.8f
        val stbPercent = if (totalHours > 0) (standbyHours / totalHours).toFloat() else 0.15f
        val bdnPercent = if (totalHours > 0) (breakdownHours / totalHours).toFloat() else 0.05f

        // Unit Summaries
        val unitsMap = filtered.groupBy { it.unitCode }
        val unitRows = unitsMap.map { (code, reps) ->
            val uTotalHours = reps.sumOf { it.workDurationHours }
            val uTotalHm = reps.sumOf { it.hmDailyRunning }
            val uSolar = reps.sumOf {
                when {
                    it.pemakaianSolar > 0.0 -> it.pemakaianSolar
                    it.isRefueled -> it.fuelLiters
                    else -> 0.0
                }
            }
            val uRatio = if (uTotalHm > 0.0) uSolar / uTotalHm else 0.0
            val uTroubleCount = reps.count { it.unitStatus != "OPERASI" }
            val uOutput = reps.sumOf { it.workOutput }
            val uModel = reps.firstOrNull()?.unitType ?: "HEAVY UNIT"
            val uSatuan = reps.firstOrNull()?.unitOfMeasure ?: "m3"

            UnitAnalyticsRow(
                unitCode = code,
                model = uModel,
                totalWorkHours = uTotalHours,
                totalHmRunning = uTotalHm,
                totalFuelLiters = uSolar,
                efficiencyRatio = uRatio,
                standbyBreakdownCount = uTroubleCount,
                totalOutputVolume = uOutput,
                unitOfMeasure = uSatuan
            )
        }.sortedByDescending { it.totalHmRunning }

        // Activity Summaries
        val actMap = filtered.groupBy { it.sapCode.ifBlank { it.activityName } }
        val actRows = actMap.map { (code, reps) ->
            val first = reps.first()
            val totalOutput = reps.sumOf { it.workOutput }
            val totalHmRunning = reps.sumOf { it.hmDailyRunning }
            val uniqueUnits = reps.map { it.unitCode }.distinct().size
            val matchedMaster = activities.value.find { it.sapCode.equals(first.sapCode, ignoreCase = true) || it.activityName.equals(first.activityName, ignoreCase = true) }
            val resolvedStatus = matchedMaster?.kategoriStatus ?: first.unitStatus.ifBlank { "OPERASI" }

            ActivityAnalyticsRow(
                activityName = first.activityName,
                sapCode = first.sapCode.ifBlank { "ACT" },
                unitOfMeasure = first.unitOfMeasure,
                totalOutputVolume = totalOutput,
                totalHm = totalHmRunning,
                unitInvolvedCount = uniqueUnits,
                kategoriStatus = resolvedStatus
            )
        }.sortedByDescending { it.totalOutputVolume }

        return FleetAnalyticsState(
            selectedPeriod = period,
            selectedUnitFilter = unitFilter,
            selectedWilayahFilter = wilayahFilter,
            selectedJenisUnitFilter = jenisFilter,
            totalSolarLiters = totalSolar,
            totalHmRunning = totalHm,
            avgFuelConsumptionRatio = fuelRatio,
            effectiveOperationHours = operasiHours,
            standbyHours = standbyHours,
            breakdownHours = breakdownHours,
            operationPercent = opPercent,
            standbyPercent = stbPercent,
            breakdownPercent = bdnPercent,
            unitSummaries = unitRows,
            activitySummaries = actRows
        )
    }

    fun approveRealisasi(report: ActivityReportEntity, context: Context? = null) {
        if (report.isLocked) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Info("Realisasi Unit ${report.unitCode} sudah disetujui dan terkunci permanen."))
            }
            return
        }
        val approver = _currentSupervisorSession.value?.resolvedNamaLengkap?.ifBlank { _activeSupervisor.value }
            ?: _activeSupervisor.value.ifBlank { "Kasie Lapangan" }
        viewModelScope.launch {
            val res = repository.approveRealisasi(report.id, approver)
            res.onSuccess {
                context?.let { ctx ->
                    android.widget.Toast.makeText(ctx, "Laporan berhasil disetujui dan dikunci resmi.", android.widget.Toast.LENGTH_SHORT).show()
                }
                _uiEvents.emit(
                    UiMessage.Success("✔ Realisasi Unit ${report.unitCode} berhasil disetujui oleh $approver & dikunci permanen!")
                )
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyetujui realisasi: ${err.message}"))
            }
        }
    }

    fun correctRealisasi(
        report: ActivityReportEntity,
        hmAwalBaru: Double,
        hmAkhirBaru: Double,
        solarBaru: Double,
        hasilKerjaBaru: Double,
        keteranganBaru: String
    ) {
        if (report.isLocked) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Data sudah disetujui & terkunci permanen, tidak dapat dikoreksi."))
            }
            return
        }
        if (hmAkhirBaru <= hmAwalBaru) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("HM Akhir harus lebih besar dari HM Awal."))
            }
            return
        }
        val running = hmAkhirBaru - hmAwalBaru
        val updated = report.copy(
            hmAwal = hmAwalBaru,
            hmAkhir = hmAkhirBaru,
            hmDailyRunning = running,
            fuelLiters = solarBaru,
            pemakaianSolar = solarBaru,
            isRefueled = solarBaru > 0.0,
            workOutput = hasilKerjaBaru,
            notes = keteranganBaru
        )
        viewModelScope.launch {
            repository.correctRealisasi(updated).onSuccess {
                _uiEvents.emit(UiMessage.Success("Koreksi data realisasi Unit ${report.unitCode} berhasil disimpan."))
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan koreksi: ${err.message}"))
            }
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
            _uiEvents.emit(UiMessage.Info("Laporan dihapus dari riwayat"))
        }
    }


    fun cleanDumpTruckAndBulldozer() {
        // No-op: Dump Truck and other active units are preserved
    }

    fun addOrUpdateUnit(unit: com.example.data.local.entity.UnitEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addOrUpdateUnit(unit)
                _uiEvents.emit(UiMessage.Success("Unit ${unit.unitCode} berhasil disimpan."))
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan unit: ${e.localizedMessage ?: e.message}"))
            }
        }
    }

    fun deleteUnit(unitCode: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteUnit(unitCode)
                _uiEvents.emit(UiMessage.Info("Unit $unitCode berhasil dihapus dari Master Data."))
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal menghapus unit: ${e.localizedMessage ?: e.message}"))
            }
        }
    }
}

class HeavyTrackViewModelFactory(
    private val repository: HeavyTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeavyTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeavyTrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

typealias RencanaKerjaViewModel = HeavyTrackViewModel
typealias RealisasiViewModel = HeavyTrackViewModel
typealias RiwayatViewModel = HeavyTrackViewModel

