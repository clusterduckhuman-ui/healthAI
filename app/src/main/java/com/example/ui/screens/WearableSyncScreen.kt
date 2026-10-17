package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BiometricData
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun WearableSyncScreen(
  viewModel: FitnessViewModel
) {
  val biometrics by viewModel.biometrics.collectAsState()

  val supportedWearables = listOf(
    "Garmin Forerunner 965" to "Connected • Live BLE Telemetry",
    "Apple Watch Ultra 2" to "HealthKit Sync Ready",
    "WHOOP 4.0 Strap" to "Continuous Strain & Recovery Feed",
    "Oura Ring Gen 3" to "Sleep & Readiness Score Ready",
    "Google Pixel Watch 3" to "Health Connect Synced"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Wearable Health Hub",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Continuous Biometric & Heart Rate Telemetry",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = VoltGreen.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, VoltGreen)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(VoltGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LIVE SYNC",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = VoltGreen
            )
          }
        }
      }
    }

    // Live Heart Rate ECG Pulse Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightGreen),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Favorite, contentDescription = null, tint = LightGreen, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "REAL-TIME CARDIAC MONITOR",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                  color = LightGreen
                )
                Text(
                  text = "${biometrics.heartRateBpm} BPM",
                  style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = LightGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = "ZONE 3 (CARDIO)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = LightGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // ECG Waveform Canvas
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            val w = size.width
            val h = size.height
            val midY = h / 2f

            // Baseline points with heartbeat spikes
            val points = listOf(
              Offset(0f, midY),
              Offset(w * 0.15f, midY),
              Offset(w * 0.25f, midY - 6f),
              Offset(w * 0.30f, midY + 4f),
              Offset(w * 0.35f, midY - 32f), // R spike
              Offset(w * 0.40f, midY + 16f), // S spike
              Offset(w * 0.45f, midY - 8f),  // T wave
              Offset(w * 0.55f, midY),
              Offset(w * 0.70f, midY - 32f), // second R spike
              Offset(w * 0.75f, midY + 16f),
              Offset(w * 0.80f, midY - 8f),
              Offset(w, midY)
            )

            for (i in 0 until points.size - 1) {
              drawLine(
                color = LightGreen,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3f,
                cap = StrokeCap.Round
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Resting HR: 52 bpm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("HRV: ${biometrics.hrvMs} ms (High)", style = MaterialTheme.typography.labelSmall, color = LightGreen)
            Text("VO2 Max: ${biometrics.vo2Max}", style = MaterialTheme.typography.labelSmall, color = LightGreen)
          }
        }
      }
    }

    // Body Recovery Battery & Sleep Score Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Physiological Readiness Battery",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${biometrics.recoveryScore}%",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = VoltGreen
              )
              Text("Prime Adaptation Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.background,
              modifier = Modifier.padding(8.dp)
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Sleep Score",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${biometrics.sleepQualityScore}%",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                  color = CyberTeal
                )
                Text(
                  text = "7h 48m (1h 52m REM)",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = biometrics.recoveryScore / 100f,
            color = VoltGreen,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )
        }
      }
    }

    // Force Sync CTA Button
    item {
      Button(
        onClick = { viewModel.syncWearableDevice() },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("force_sync_wearables_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground),
        shape = RoundedCornerShape(14.dp)
      ) {
        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Force Sync Health Devices Now", fontWeight = FontWeight.Bold)
      }
    }

    // Connected & Supported Health Devices
    item {
      Text(
        text = "Supported Wearables & Sensor Profiles",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    items(supportedWearables.size) { index ->
      val (device, status) = supportedWearables[index]
      val isConnected = device == biometrics.connectedDeviceName

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
          1.dp,
          if (isConnected) VoltGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.syncWearableDevice(device) }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isConnected) VoltGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Watch,
                contentDescription = null,
                tint = if (isConnected) VoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = device,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isConnected) "Active Connection • 92% Battery" else status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) VoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (isConnected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Connected", tint = VoltGreen)
          } else {
            OutlinedButton(
              onClick = { viewModel.syncWearableDevice(device) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Text("Connect", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }
  }
}
