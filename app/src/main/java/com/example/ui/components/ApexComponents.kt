package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BiometricData
import com.example.ui.theme.*

@Composable
fun ApexTopBar(
  biometrics: BiometricData,
  isTrialActive: Boolean,
  isDarkTheme: Boolean,
  onSyncClicked: () -> Unit,
  onPricingClicked: () -> Unit,
  onThemeToggleClicked: () -> Unit,
  onSettingsClicked: () -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 3.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // App Branding & Recovery Pill
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (biometrics.recoveryScore >= 70) VoltGreen else FlameOrange)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "APEX",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "PULSE",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            ),
            color = VoltGreen
          )
        }
        Text(
          text = "Recovery: ${biometrics.recoveryScore}% • ${biometrics.connectedDeviceName}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Quick Actions
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Sync Wearables
        IconButton(
          onClick = onSyncClicked,
          modifier = Modifier
            .size(40.dp)
            .testTag("topbar_sync_wearable_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Sync Wearable",
            tint = CyberTeal,
            modifier = Modifier.size(20.dp)
          )
        }

        // VIP / Trial Badge
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isTrialActive) VoltGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
          border = BorderStroke(1.dp, if (isTrialActive) VoltGreen else DarkBorder),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onPricingClicked() }
            .testTag("topbar_vip_badge")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Stars,
              contentDescription = "VIP Trial",
              tint = VoltGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isTrialActive) "7D TRIAL" else "PRO",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isTrialActive) VoltGreen else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Theme Toggle (Dark / Light)
        IconButton(
          onClick = onThemeToggleClicked,
          modifier = Modifier
            .size(40.dp)
            .testTag("topbar_theme_toggle_btn")
        ) {
          Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle Theme",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }

        // Settings / Profile
        IconButton(
          onClick = onSettingsClicked,
          modifier = Modifier
            .size(40.dp)
            .testTag("topbar_profile_settings_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Preferences",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun BiometricSummaryCard(
  biometrics: BiometricData,
  modifier: Modifier = Modifier,
  onSyncClicked: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart rate",
            tint = PulseRed,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE BIOMETRIC TELEMETRY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        TextButton(
          onClick = onSyncClicked,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = biometrics.lastSyncTime,
            style = MaterialTheme.typography.labelSmall
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4 Metrics Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Recovery
        BiometricMetricItem(
          label = "Recovery",
          value = "${biometrics.recoveryScore}%",
          accentColor = if (biometrics.recoveryScore >= 75) VoltGreen else FlameOrange,
          subLabel = if (biometrics.recoveryScore >= 75) "Optimal State" else "Steady State"
        )

        // Strain
        BiometricMetricItem(
          label = "Day Strain",
          value = "${biometrics.strainScore}",
          accentColor = FlameOrange,
          subLabel = "Target: 16.0"
        )

        // Heart Rate
        BiometricMetricItem(
          label = "Heart Rate",
          value = "${biometrics.heartRateBpm}",
          accentColor = PulseRed,
          subLabel = "Zone 3 Aerobic"
        )

        // Calories
        BiometricMetricItem(
          label = "Active Burn",
          value = "${biometrics.activeCaloriesBurned}",
          accentColor = CyberTeal,
          subLabel = "kcal"
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Heart Rate Visual Sparkline / Bar Indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = null,
            tint = CyberTeal,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${biometrics.connectedDeviceName} • ${biometrics.batteryPercentage}% Batt",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = "${biometrics.stepsCount} steps • HRV ${biometrics.hrvMs}ms",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun BiometricMetricItem(
  label: String,
  value: String,
  accentColor: Color,
  subLabel: String
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp
      ),
      color = accentColor
    )
    Text(
      text = subLabel,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun CircularProgressBadge(
  progressPercent: Float,
  modifier: Modifier = Modifier,
  trackColor: Color = Color.Gray.copy(alpha = 0.2f),
  fillColor: Color = VoltGreen,
  strokeWidth: Float = 14f,
  centerContent: @Composable () -> Unit
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val diameter = size.minDimension
      val radius = diameter / 2f - strokeWidth
      val centerOffset = Offset(size.width / 2f, size.height / 2f)

      // Background Track
      drawCircle(
        color = trackColor,
        radius = radius,
        center = centerOffset,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )

      // Animated Filled Sweep
      val sweep = (progressPercent.coerceIn(0f, 1f)) * 360f
      drawArc(
        color = fillColor,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )
    }
    centerContent()
  }
}
