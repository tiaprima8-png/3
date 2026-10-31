package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.MasterRealisasiEntity
import com.example.ui.components.isNeedApproval
import com.example.ui.viewmodel.HeavyTrackViewModel

/**
 * RiwayatScreen - Layar Riwayat Operasional Realisasi Alat Berat Lapangan.
 * Menyediakan alias dan pembungkus HistoryScreen dengan dukungan penuh helper isNeedApproval.
 */
@Composable
fun RiwayatScreen(
    viewModel: HeavyTrackViewModel,
    modifier: Modifier = Modifier,
    onNavigateToForm: () -> Unit = {}
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        HistoryScreen(
            viewModel = viewModel,
            onNavigateToForm = onNavigateToForm
        )
    }
}

/**
 * Re-export helper isNeedApproval untuk penggunaan langsung di konteks RiwayatScreen
 */
fun isLaporanNeedApproval(item: MasterRealisasiEntity): Boolean = isNeedApproval(item)
fun isLaporanNeedApproval(item: ActivityReportEntity): Boolean = isNeedApproval(item)
