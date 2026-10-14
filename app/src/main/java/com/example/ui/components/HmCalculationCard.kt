package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun HmCalculationCard(
    hmAwal: Double,
    hmAkhirInput: String,
    onHmAkhirChange: (String) -> Unit,
    onResetHm: () -> Unit,
    onQuickAddHours: (Double) -> Unit,
    isValid: Boolean,
    errorMessage: String?,
    hmDailyRunning: Double,
    unitSelected: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hm_calculation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Emerald50, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kalkulasi Hour Meter (HM)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Validasi otomatis HM Awal vs HM Akhir",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                // Reset HM Button
                OutlinedButton(
                    onClick = onResetHm,
                    enabled = unitSelected,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Slate700
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("reset_hm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset HM",
                        modifier = Modifier.size(16.dp),
                        tint = Slate500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset HM",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Fields Row: HM Awal (locked) & HM Akhir (input)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HM Awal Field (Terkunci / Read-Only)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "HM Awal (Terkunci)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (unitSelected) String.format("%.1f", hmAwal) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                            )
                            Text(
                                text = "Jam",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // HM Akhir Field (Input)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HM Akhir (Input)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = hmAkhirInput,
                        onValueChange = onHmAkhirChange,
                        enabled = unitSelected,
                        placeholder = {
                            Text(
                                text = if (unitSelected) "0.0" else "Pilih unit",
                                color = Slate400,
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "Jam",
                                color = Slate500,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        isError = !isValid && hmAkhirInput.isNotBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            errorBorderColor = ErrorRose,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("hm_akhir_input")
                    )
                }
            }

            // Quick Add hours chips when unit is selected
            if (unitSelected && hmAwal > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cepat:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                    listOf(4.0, 7.0, 8.0, 10.0).forEach { addHours ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAddHours(addHours) }
                        ) {
                            Text(
                                text = "+${addHours.toInt()} Jam",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Instant Error Message if hm_akhir < hm_awal
            AnimatedVisibility(
                visible = !isValid && hmAkhirInput.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = ErrorRose50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Peringatan Error",
                            tint = ErrorRose,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "HM Akhir tidak boleh lebih kecil dari HM Awal!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ErrorRose,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // HM Harian Berjalan (Mint highlight container #ECFDF5)
            AnimatedVisibility(
                visible = isValid && hmAkhirInput.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("hm_daily_running_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HM Harian Berjalan (Total Operasi):",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Emerald900,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Formula: ${String.format("%.1f", (hmAkhirInput.toDoubleOrNull() ?: hmAwal))} - ${String.format("%.1f", hmAwal)} Jam",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Emerald700,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald600
                        ) {
                            Text(
                                text = "+${String.format("%.2f", hmDailyRunning)} JAM",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
