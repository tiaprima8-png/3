package com.example.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RealisasiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RealisasiViewModel(
    private val realisasiRepository: RealisasiRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun approveLaporan(nomorSpk: String, kasieNama: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = realisasiRepository.approveLaporan(nomorSpk, kasieNama)
            _isLoading.value = false

            result.onSuccess {
                Toast.makeText(context, "Laporan resmi disetujui di Server & Lokal!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(context, "Gagal simpan ke Web Admin: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
