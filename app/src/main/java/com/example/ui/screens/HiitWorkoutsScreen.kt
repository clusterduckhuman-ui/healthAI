package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HiitExerciseDemo
import com.example.model.HiitSession
import com.example.ui.components.CircularProgressBadge
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun HiitWorkoutsScreen(
  viewModel: FitnessViewModel
) {
  val sessions by viewModel.hiitSessions.collectAsState()
  val selectedSession by viewModel.selectedHiitSession.collectAsState()
  val isRunning by viewModel.isTimerRunning.collectAsState()
  val secondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
  val isWorkInterval by viewModel.isWorkInterval.collectAsState()
  val currentRound by viewModel.currentRound.collectAsState()

  val activeSession = selectedSession ?: sessions.first()
  val totalIntervalSec = if (isWorkInterval) activeSession.workSeconds else activeSession.restSeconds
  val progress = (secondsRemaining.toFloat() / totalIntervalSec).coerceIn(0f, 1f)

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
            text = "Express HIIT Workouts",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "High Output Sessions For Packed Schedules",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = FlameOrange.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, FlameOrange)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "~${activeSession.estimatedBurnKcal} kcal",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = FlameOrange
            )
          }
        }
      }
    }

    // Session Switcher Tabs (5-Min, 10-Min, 15-Min)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        sessions.forEach { session ->
          val isSelected = session.id == activeSession.id
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) VoltGreen else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, if (isSelected) VoltGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { viewModel.selectHiitSession(session) }
              .testTag("hiit_session_tab_${session.durationMinutes}m")
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "${session.durationMinutes} MIN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = if (isSelected) DarkBackground else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = session.difficulty,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) DarkBackground else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Interactive Interval Timer HUD Card
    item {
      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isWorkInterval) PureBlack else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
          1.5.dp,
          if (isWorkInterval) LightGreen else PureWhite
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hiit_timer_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Phase Badge
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isWorkInterval) VoltGreen.copy(alpha = 0.2f) else CyberTeal.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, if (isWorkInterval) VoltGreen else CyberTeal)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isWorkInterval) Icons.Default.FlashOn else Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = if (isWorkInterval) VoltGreen else CyberTeal,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isWorkInterval) "WORK INTERVAL - MAX INTENSITY" else "RECOVERY REST INTERVAL",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                ),
                color = if (isWorkInterval) VoltGreen else CyberTeal
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Circular Ring with Countdown
          Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressBadge(
              progressPercent = progress,
              fillColor = if (isWorkInterval) VoltGreen else CyberTeal,
              trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
              strokeWidth = 14f,
              modifier = Modifier.fillMaxSize()
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "$secondsRemaining",
                  style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                  ),
                  color = if (isWorkInterval) VoltGreen else CyberTeal
                )
                Text(
                  text = "SECONDS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  ),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Round Counter & Cadence
          Text(
            text = "Round $currentRound of ${activeSession.rounds} • Ratio ${activeSession.workSeconds}s / ${activeSession.restSeconds}s",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Play, Pause, Skip, Reset Control Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Reset
            IconButton(
              onClick = { viewModel.resetTimer() },
              modifier = Modifier.size(44.dp)
            ) {
              Icon(Icons.Default.Replay, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Play / Pause Main Button
            FilledIconButton(
              onClick = {
                if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
              },
              modifier = Modifier
                .size(64.dp)
                .testTag("hiit_play_pause_btn"),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isWorkInterval) VoltGreen else CyberTeal,
                contentColor = DarkBackground
              )
            ) {
              Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Start",
                modifier = Modifier.size(32.dp)
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Skip Interval
            IconButton(
              onClick = { viewModel.skipInterval() },
              modifier = Modifier.size(44.dp)
            ) {
              Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    // Video & Movement Demonstration Masterclass List
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Exercise Movement Demonstrations",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Form checkpoints & tempo for max metabolic burn",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Demo Exercise Cards
    items(activeSession.exercises) { demo ->
      HiitExerciseDemoCard(demo = demo)
    }
  }
}

@Composable
private fun HiitExerciseDemoCard(demo: HiitExerciseDemo) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = demo.name,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = FlameOrange.copy(alpha = 0.2f)
        ) {
          Text(
            text = demo.repsOrTime,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = FlameOrange,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Video Demo Preview Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(PureBlack),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(VoltGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "VIDEO DEMONSTRATION & CADENCE",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
              color = VoltGreen
            )
            Text(
              text = demo.videoPlaceholderText,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = demo.keyFormTip,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Targets: ${demo.muscleGroup}",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = CyberTeal
      )
    }
  }
}
