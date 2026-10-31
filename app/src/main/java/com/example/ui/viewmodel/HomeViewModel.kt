package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MasterRealisasiEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.repository.HeavyTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val isGlobalSyncing: Boolean = false,
    val currentUserNama: String = "Kasie Lapangan",
    val currentWilayah: String = "PG1",
    val currentUserRole: String = "kasie",
    val isKasie: Boolean = true,
    val totalUnitHariIni: Int = 0,
    val unitSelesaiCount: Int = 0,
    val listRencana: List<RencanaKerjaEntity> = emptyList(),
    val mapRealisasi: Map<String, MasterRealisasiEntity> = emptyMap()
)

class HomeViewModel(
    private val repository: HeavyTrackRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isGlobalSyncing = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllRencana(),
        repository.getAllReports(),
        repository.getActiveUserSession(),
        _isGlobalSyncing
    ) { renList, reportList, activeUser, syncing ->
        val activeSpv = activeUser?.resolvedNamaLengkap ?: "Kasie Lapangan"
        val mapReal = reportList.associate { rep ->
            rep.nomorSpk to MasterRealisasiEntity.fromActivityReport(rep)
        }
        val realizedCount = renList.count { it.isRealized || mapReal[it.nomorSpk]?.isLocked == true }
        val isOperator = activeUser?.isOperator == true || activeSpv.contains("operator", ignoreCase = true) || activeSpv.contains("Taufik", ignoreCase = true)

        HomeUiState(
            isGlobalSyncing = syncing,
            currentUserNama = activeSpv,
            currentWilayah = activeUser?.wilayah ?: "PG1",
            currentUserRole = if (isOperator) "operator" else "kasie",
            isKasie = !isOperator,
            totalUnitHariIni = renList.map { it.kodeUnit }.distinct().size,
            unitSelesaiCount = realizedCount,
            listRencana = renList,
            mapRealisasi = mapReal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun fetchRencanaKerjaTerbaru() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val spv = repository.getActiveUser()?.resolvedNamaLengkap ?: ""
                repository.syncRencanaWithServer(spv)
            } catch (_: Exception) {}
            _isRefreshing.value = false
        }
    }

    fun performFullTwoWaySync(context: Context) {
        viewModelScope.launch {
            _isGlobalSyncing.value = true
            try {
                repository.syncAllPendingReports()
                val spv = repository.getActiveUser()?.resolvedNamaLengkap ?: ""
                repository.syncRencanaWithServer(spv)
            } catch (_: Exception) {}
            _isGlobalSyncing.value = false
        }
    }

    fun downloadLatestApk(context: Context) {
        // Triggers APK download
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearUserSession()
        }
    }

    fun approveLaporan(nomorSpk: String, context: Context) {
        viewModelScope.launch {
            repository.realisasiRepository.approveLaporan(nomorSpk, "Kasie Lapangan")
        }
    }
}
