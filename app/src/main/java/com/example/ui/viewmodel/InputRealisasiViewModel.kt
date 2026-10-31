package com.example.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.RealisasiRequest
import com.example.data.repository.RealisasiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InputRealisasiViewModel(
    private val realisasiRepository: RealisasiRepository,
    private val onNavigateBack: () -> Unit = {}
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var isLoadingValue: Boolean
        get() = _isLoading.value
        set(value) {
            _isLoading.value = value
        }

    fun submitRealisasi(request: RealisasiRequest, context: Context) {
        val sanitized = request.copy(
            approval_status = "Menunggu Approval",
            approved_by = null,
            approved_at = null,
            is_locked = false
        )
        onSubmitButtonClicked(sanitized, context)
    }

    fun onSubmitButtonClicked(request: RealisasiRequest, context: Context) {
        val sanitized = request.copy(
            approval_status = "Menunggu Approval",
            approved_by = null,
            approved_at = null,
            is_locked = false
        )
        viewModelScope.launch {
            isLoadingValue = true
            val result = realisasiRepository.submitRealisasiLangsung(sanitized, context)
            isLoadingValue = false

            result.onSuccess {
                Toast.makeText(
                    context,
                    "Realisasi berhasil terkirim ke Web Admin!",
                    Toast.LENGTH_SHORT
                ).show()
                onNavigateBack()
            }.onFailure { err ->
                Toast.makeText(
                    context,
                    "Gagal sinkron ke server: ${err.message}. Data tersimpan lokal.",
                    Toast.LENGTH_LONG
                ).show()
                onNavigateBack()
            }
        }
    }

    fun approveLaporan(nomorSpk: String, kasieNama: String, context: Context) {
        viewModelScope.launch {
            isLoadingValue = true
            val result = realisasiRepository.approveLaporan(nomorSpk, kasieNama)
            isLoadingValue = false

            result.onSuccess {
                Toast.makeText(context, "Laporan resmi disetujui di Server & Lokal!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(context, "Gagal simpan ke Web Admin: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

