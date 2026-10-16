package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmber50

@Composable
fun SyncStatusBadge(isSynced: Boolean, modifier: Modifier = Modifier) {
    if (isSynced) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Emerald100,
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Tersinkron",
                    tint = Emerald900,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "✓",
                    color = Emerald900,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = WarningAmber50,
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Menunggu Sinkron",
                    tint = WarningAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Menunggu Sinkron",
                    color = Color(0xFFB45309),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun ShiftBadge(shift: String, modifier: Modifier = Modifier) {
    val isDay = shift.equals("SIANG", ignoreCase = true)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDay) Color(0xFFFEF3C7) else Color(0xFFE0E7FF),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                contentDescription = null,
                tint = if (isDay) Color(0xFFD97706) else Color(0xFF4338CA),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shift.uppercase(),
                color = if (isDay) Color(0xFF92400E) else Color(0xFF3730A3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun UnitStatusBadge(status: String, modifier: Modifier = Modifier) {
    val cleanStatus = status.uppercase()
    val (bgColor, textColor, borderColor, icon) = when (cleanStatus) {
        "STANDBY" -> Quad(Color(0xFFFEF3C7), Color(0xFFB45309), WarningAmber.copy(alpha = 0.5f), Icons.Default.HourglassTop)
        "BREAKDOWN" -> Quad(Color(0xFFFFE4E6), Color(0xFFBE123C), ErrorRose.copy(alpha = 0.5f), Icons.Default.Build)
        else -> Quad(Emerald100, Emerald900, Emerald700.copy(alpha = 0.3f), Icons.Default.PlayArrow) // OPERASI
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cleanStatus,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun FuelBadge(liters: Double, modifier: Modifier = Modifier) {
    if (liters <= 0.0) return
    val formattedLiters = if (liters % 1.0 == 0.0) liters.toInt().toString() else String.format("%.1f", liters)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFEF3C7),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Solar: $formattedLiters L",
                color = Color(0xFF92400E),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
