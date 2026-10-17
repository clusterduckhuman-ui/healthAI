package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MovementFormCheck
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun GymTrainerFormScreen(
  viewModel: FitnessViewModel
) {
  val selectedMovement by viewModel.selectedFormMovement.collectAsState()
  val formChecks by viewModel.formChecks.collectAsState()
  val currentCheck = formChecks[selectedMovement] ?: formChecks.values.first()

  var simulatedDepth by remember { mutableFloatStateOf(currentCheck.depthPercent.toFloat()) }
  var simulatedAngle by remember { mutableIntStateOf(currentCheck.currentAngleDegrees) }
  val isMetronomeRunning by viewModel.isMetronomeRunning.collectAsState()
  val metronomeCount by viewModel.metronomeCount.collectAsState()
  val metronomePhase by viewModel.metronomePhase.collectAsState()

  val movementsList = listOf("Barbell Back Squat", "Barbell Deadlift", "Push-Up", "Walking Barbell Lunge")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Movement Selector Chips
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AI Gym Trainer",
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Computer Vision Form Feedback & Joint Angles",
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
                text = "CV ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VoltGreen
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Movement Horizontal Selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(movementsList) { movement ->
            val isSelected = movement == selectedMovement
            FilterChip(
              selected = isSelected,
              onClick = {
                viewModel.selectFormMovement(movement)
                val newCheck = formChecks[movement]
                if (newCheck != null) {
                  simulatedDepth = newCheck.depthPercent.toFloat()
                  simulatedAngle = newCheck.currentAngleDegrees
                }
              },
              label = {
                Text(
                  text = movement,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = VoltGreen,
                selectedLabelColor = DarkBackground,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.testTag("form_movement_chip_${movement.replace(" ", "_")}")
            )
          }
        }
      }
    }

    // Interactive Viewfinder & Skeleton Overlay
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
          .testTag("camera_viewfinder_card")
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Canvas rendering pose skeleton and angle tracker
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Viewfinder Grid lines
            val gridColor = LightGreen.copy(alpha = 0.2f)
            drawLine(gridColor, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 1f)
            drawLine(gridColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 1f)
            drawLine(gridColor, Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
            drawLine(gridColor, Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)

            // Dynamic Skeleton Coordinates based on simulated angle
            val depthRatio = simulatedDepth / 100f
            val hipY = h * 0.48f + (depthRatio * 35f)
            val kneeY = h * 0.68f
            val ankleY = h * 0.88f

            val head = Offset(w * 0.5f, h * 0.20f)
            val shoulder = Offset(w * 0.5f, h * 0.30f)
            val spineMid = Offset(w * 0.5f, h * 0.40f)
            val hip = Offset(w * 0.5f, hipY)
            val kneeLeft = Offset(w * 0.40f, kneeY)
            val kneeRight = Offset(w * 0.60f, kneeY)
            val ankleLeft = Offset(w * 0.38f, ankleY)
            val ankleRight = Offset(w * 0.62f, ankleY)

            val skeletonColor = if (simulatedDepth >= 85) VoltGreen else if (simulatedDepth >= 70) FlameOrange else PulseRed
            val stroke = Stroke(width = 6f, cap = StrokeCap.Round)

            // Torso & Spine
            drawLine(skeletonColor, shoulder, hip, strokeWidth = 7f, cap = StrokeCap.Round)
            // Left Leg
            drawLine(skeletonColor, hip, kneeLeft, strokeWidth = 7f, cap = StrokeCap.Round)
            drawLine(skeletonColor, kneeLeft, ankleLeft, strokeWidth = 7f, cap = StrokeCap.Round)
            // Right Leg
            drawLine(skeletonColor, hip, kneeRight, strokeWidth = 7f, cap = StrokeCap.Round)
            drawLine(skeletonColor, kneeRight, ankleRight, strokeWidth = 7f, cap = StrokeCap.Round)

            // Joint Nodes
            val jointRadius = 9f
            listOf(head, shoulder, spineMid, hip, kneeLeft, kneeRight, ankleLeft, ankleRight).forEach { pt ->
              drawCircle(Color.White, radius = jointRadius + 2f, center = pt)
              drawCircle(skeletonColor, radius = jointRadius, center = pt)
            }

            // Depth Arc at Knee Joint
            drawCircle(
              color = CyberTeal.copy(alpha = 0.3f),
              radius = 28f,
              center = kneeLeft,
              style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
          }

          // Real-time telemetry HUD overlay
          Column(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(16.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xAA000000)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = PulseRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "FEED: 60 FPS • SKELETON 24 KPT",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
            }
          }

          // Angle & Depth Badge
          Column(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(16.dp),
            horizontalAlignment = Alignment.End
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (simulatedDepth >= 85) VoltGreen.copy(alpha = 0.9f) else FlameOrange.copy(alpha = 0.9f)
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "$simulatedAngle°",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                  color = DarkBackground
                )
                Text(
                  text = "KNEE ANGLE",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = DarkBackground
                )
              }
            }
          }

          // Bottom Viewfinder Bar
          Surface(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth(),
            color = Color(0xCC0B0F14)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Target: ${currentCheck.targetAngleRange}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CyberTeal
              )
              Text(
                text = "Depth: ${simulatedDepth.toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (simulatedDepth >= 85) VoltGreen else FlameOrange
              )
              Text(
                text = "Score: ${currentCheck.scoreOutOf100}/100",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = VoltGreen
              )
            }
          }
        }
      }
    }

    // Depth Angle Simulator Controls
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Live Depth Simulation Control",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${simulatedDepth.toInt()}% Depth",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = VoltGreen
            )
          }

          Slider(
            value = simulatedDepth,
            onValueChange = { depth ->
              simulatedDepth = depth
              simulatedAngle = (60 + (depth * 0.45)).toInt()
              val cue = if (depth >= 85) {
                "Great depth! Hips cleared knees cleanly. Drive feet through the floor."
              } else if (depth >= 70) {
                "Almost there! Sink 2 inches deeper to hit parallel hip crease."
              } else {
                "Shallow rep! Incomplete range of motion. Initiate with hip hinge."
              }
              val spine = if (depth >= 75) "Optimal Neutral (Chest Elevated)" else "Slight Lumbar Flexion Alert"
              viewModel.updateFormCheckAngle(selectedMovement, simulatedAngle, depth.toInt(), spine, cue)
            },
            valueRange = 50f..100f,
            colors = SliderDefaults.colors(
              thumbColor = VoltGreen,
              activeTrackColor = VoltGreen,
              inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag("depth_simulation_slider")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                simulatedDepth = 94f
                simulatedAngle = 92
                viewModel.updateFormCheckAngle(
                  selectedMovement, 92, 94, "Optimal Neutral",
                  "Flawless rep! Clean parallel crease, spine locked."
                )
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Optimal (94%)", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
              onClick = {
                simulatedDepth = 65f
                simulatedAngle = 72
                viewModel.updateFormCheckAngle(
                  selectedMovement, 72, 65, "Chest Collapsing",
                  "Depth too high! Keep chest open and sit between hips."
                )
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Shallow (65%)", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // Real-Time Audio Cue / Trainer Correction Speech Box
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (simulatedDepth >= 85) VoltGreen.copy(alpha = 0.12f) else FlameOrange.copy(alpha = 0.12f)
        ),
        border = BorderStroke(
          1.5.dp,
          if (simulatedDepth >= 85) VoltGreen else FlameOrange
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              tint = if (simulatedDepth >= 85) VoltGreen else FlameOrange,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "TRAINER VOICE CUE",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              ),
              color = if (simulatedDepth >= 85) VoltGreen else FlameOrange
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "\"${currentCheck.audioCueText}\"",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Correction: ${currentCheck.correctiveAction}",
              style = MaterialTheme.typography.bodySmall,
              color = CyberTeal
            )
          }
        }
      }
    }

    // Form Tempo Cadence Metronome (NEW FEATURE)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Timer, contentDescription = null, tint = LightGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "LIFTING TEMPO METRONOME",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                  color = LightGreen
                )
                Text(
                  text = "Cadence: 3-1-1-0 (Hypertrophy Standard)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Button(
              onClick = { viewModel.toggleMetronome() },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isMetronomeRunning) PureWhite else LightGreen,
                contentColor = PureBlack
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = if (isMetronomeRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isMetronomeRunning) "Stop Beat" else "Start Metronome",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
              )
            }
          }

          if (isMetronomeRunning) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PureBlack,
              border = BorderStroke(1.dp, LightGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("CURRENT PHASE", style = MaterialTheme.typography.labelSmall, color = LightGreen)
                  Text(
                    text = metronomePhase,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = PureWhite
                  )
                }

                Surface(
                  shape = CircleShape,
                  color = LightGreen,
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = "$metronomeCount",
                      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                      color = PureBlack
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Biomechanical Checkpoints Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Biomechanical Checkpoints",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          CheckpointRow(
            label = "Spine Neutrality",
            status = currentCheck.spineNeutrality,
            isPassed = currentCheck.spineNeutrality.contains("Optimal", ignoreCase = true)
          )

          Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

          CheckpointRow(
            label = "Knee Tracking",
            status = currentCheck.kneeTracking,
            isPassed = true
          )

          Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

          CheckpointRow(
            label = "Tempo & Cadence",
            status = currentCheck.tempoCadence,
            isPassed = true
          )
        }
      }
    }

    // Common Mistakes to Avoid
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Expert Tips: Common Mistakes To Avoid",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          currentCheck.commonMistakes.forEach { mistake ->
            Row(
              modifier = Modifier.padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .padding(top = 6.dp)
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(FlameOrange)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = mistake,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CheckpointRow(label: String, status: String, isPassed: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
        contentDescription = null,
        tint = if (isPassed) VoltGreen else FlameOrange,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = status,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        color = if (isPassed) VoltGreen else FlameOrange
      )
    }
  }
}
