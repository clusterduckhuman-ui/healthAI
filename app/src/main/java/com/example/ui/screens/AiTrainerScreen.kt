package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exercise
import com.example.model.WorkoutPlan
import com.example.ui.components.BiometricSummaryCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FitnessViewModel

@Composable
fun AiTrainerScreen(
  viewModel: FitnessViewModel,
  onNavigateToScreen: (AppScreen) -> Unit
) {
  val workout by viewModel.currentWorkout.collectAsState()
  val biometrics by viewModel.biometrics.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val waterIntake by viewModel.waterIntakeMl.collectAsState()

  var showRpeDialog by remember { mutableStateOf(false) }
  var show1RmCalculator by remember { mutableStateOf(false) }
  var showAddExerciseDialog by remember { mutableStateOf(false) }

  // 1RM Calculation States
  var calcWeightInput by remember { mutableStateOf("100") }
  var calcRepsInput by remember { mutableStateOf("5") }

  // Custom Exercise States
  var customName by remember { mutableStateOf("") }
  var customMuscle by remember { mutableStateOf("Chest") }
  var customSets by remember { mutableStateOf("3") }
  var customReps by remember { mutableStateOf("10 reps") }
  var customWeight by remember { mutableStateOf("50.0") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 100% Native Vector & Canvas Athletic HUD Hero (Zero AI Photos)
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .testTag("ai_trainer_hero_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, LightGreen)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Canvas rendering sleek athletic radar grid and pulse line
          val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // High-tech subtle grid
            for (x in 0..10) {
              val xPos = w * (x / 10f)
              drawLine(gridColor, Offset(xPos, 0f), Offset(xPos, h), strokeWidth = 1f)
            }
            for (y in 0..6) {
              val yPos = h * (y / 6f)
              drawLine(gridColor, Offset(0f, yPos), Offset(w, yPos), strokeWidth = 1f)
            }

            // Concentric fitness radar arcs
            drawCircle(
              color = LightGreen.copy(alpha = 0.15f),
              radius = h * 0.75f,
              center = Offset(w * 0.85f, h * 0.45f),
              style = Stroke(width = 2f)
            )
            drawCircle(
              color = LightGreen.copy(alpha = 0.09f),
              radius = h * 1.1f,
              center = Offset(w * 0.85f, h * 0.45f),
              style = Stroke(width = 2f)
            )

            // Neon Light Green pulse wave across background
            val waveY = h * 0.65f
            val wavePts = listOf(
              Offset(0f, waveY),
              Offset(w * 0.35f, waveY),
              Offset(w * 0.42f, waveY - 24f),
              Offset(w * 0.48f, waveY + 28f),
              Offset(w * 0.54f, waveY - 45f), // QRS spike
              Offset(w * 0.58f, waveY + 16f),
              Offset(w * 0.65f, waveY),
              Offset(w, waveY)
            )
            for (i in 0 until wavePts.size - 1) {
              drawLine(
                color = LightGreen,
                start = wavePts[i],
                end = wavePts[i + 1],
                strokeWidth = 3f,
                cap = StrokeCap.Round
              )
            }
          }

          // Content overlay
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(18.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = LightGreen.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, LightGreen)
            ) {
              Text(
                text = "AI NEURAL TRAINER • BIOMETRICS ADAPTIVE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = workout.title,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${workout.focusArea} • ${workout.durationMinutes} min • Target Strain ${workout.targetStrain}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Biometric Telemetry HUD
    item {
      BiometricSummaryCard(
        biometrics = biometrics,
        onSyncClicked = { viewModel.syncWearableDevice() }
      )
    }

    // Hydration & Electrolyte Tracker (NEW FEATURE)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.WaterDrop, contentDescription = null, tint = LightGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "HYDRATION & RECOVERY FLUIDS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Text(
              text = "$waterIntake / 3500 ml",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
              color = LightGreen
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = (waterIntake.toFloat() / 3500f).coerceIn(0f, 1f),
            color = LightGreen,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.logWater(250) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.4f))
            ) {
              Text("+250ml", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }

            OutlinedButton(
              onClick = { viewModel.logWater(500) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.4f))
            ) {
              Text("+500ml", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }

            OutlinedButton(
              onClick = { viewModel.logWater(750) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.4f))
            ) {
              Text("+750ml", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }

    // AI Bio-Feedback & Adaptive Engine Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = LightGreen,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "BIOMETRIC RATIONALE",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = LightGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = workout.intensityLevel.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = LightGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = workout.aiBiometricRationale,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { viewModel.generatePersonalizedWorkout() },
              modifier = Modifier
                .weight(1f)
                .testTag("ai_regenerate_workout_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = LightGreen,
                contentColor = PureBlack
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Adapt To Bio-Data", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { onNavigateToScreen(AppScreen.GYM_FORM) },
              modifier = Modifier
                .weight(1f)
                .testTag("open_form_checker_btn"),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, LightGreen)
            ) {
              Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = LightGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Form Check", color = LightGreen, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Tools Row: 1RM Calculator & Add Custom Movement (NEW FEATURES)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = { show1RmCalculator = true },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, LightGreen)
        ) {
          Icon(Icons.Default.Calculate, contentDescription = null, tint = LightGreen, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("1RM Calculator", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = LightGreen)
        }

        Button(
          onClick = { showAddExerciseDialog = true },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Movement", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
      }
    }

    // Workout Progress & Exercise List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Today's Target Routine",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          val completedCount = workout.exercises.count { it.completedSets >= it.sets }
          Text(
            text = "$completedCount of ${workout.exercises.size} movements completed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (workout.isCompleted) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = LightGreen.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, LightGreen)
          ) {
            Text(
              text = "WORKOUT FINISHED! 🔥",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = LightGreen,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        } else {
          TextButton(onClick = { showRpeDialog = true }) {
            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Log RPE Exertion")
          }
        }
      }
    }

    // Exercise Items
    items(workout.exercises, key = { it.id }) { exercise ->
      ExerciseListItem(
        exercise = exercise,
        onCompleteSet = { viewModel.completeExerciseSet(exercise.id) },
        onCheckForm = {
          viewModel.selectFormMovement(
            if (exercise.name.contains("Squat", ignoreCase = true)) "Barbell Back Squat"
            else if (exercise.name.contains("Deadlift", ignoreCase = true)) "Barbell Deadlift"
            else if (exercise.name.contains("Push", ignoreCase = true) || exercise.name.contains("Dip", ignoreCase = true)) "Push-Up"
            else "Barbell Back Squat"
          )
          onNavigateToScreen(AppScreen.GYM_FORM)
        }
      )
    }
  }

  // 1RM Calculator Modal Dialog (NEW FEATURE)
  if (show1RmCalculator) {
    val weightVal = calcWeightInput.toDoubleOrNull() ?: 100.0
    val repsVal = calcRepsInput.toIntOrNull() ?: 5
    // Brzycki formula: 1RM = Weight / (1.0278 - (0.0278 * reps))
    val oneRm = if (repsVal in 1..20) (weightVal / (1.0278 - 0.0278 * repsVal)).toInt() else weightVal.toInt()

    // Plate math on 20kg bar for this 1RM
    val weightToLoadPerSide = maxOf(0.0, (oneRm - 20.0) / 2.0)

    AlertDialog(
      onDismissRequest = { show1RmCalculator = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LightGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("1RM & Plate Calculator", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text("Calculate your theoretical One Rep Max and training percentages based on your completed sets.", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = calcWeightInput,
              onValueChange = { calcWeightInput = it },
              label = { Text("Weight (kg)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = calcRepsInput,
              onValueChange = { calcRepsInput = it },
              label = { Text("Reps") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, LightGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("ESTIMATED 1-REP MAX", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = LightGreen)
              Text("$oneRm KG", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
              Text("Olympic Bar Loaded: ${weightToLoadPerSide.toInt()} kg on each side", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("90% (Power): ${(oneRm * 0.9).toInt()} kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            Text("80% (Hypertrophy): ${(oneRm * 0.8).toInt()} kg", style = MaterialTheme.typography.labelSmall, color = LightGreen)
            Text("70% (Speed): ${(oneRm * 0.7).toInt()} kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { show1RmCalculator = false },
          colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PureBlack)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Add Custom Exercise Dialog (NEW FEATURE)
  if (showAddExerciseDialog) {
    AlertDialog(
      onDismissRequest = { showAddExerciseDialog = false },
      title = { Text("Add Custom Exercise", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = customName,
            onValueChange = { customName = it },
            label = { Text("Exercise Name") },
            placeholder = { Text("e.g. Barbell Hip Thrust") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = customMuscle,
            onValueChange = { customMuscle = it },
            label = { Text("Target Muscle Group") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = customSets,
              onValueChange = { customSets = it },
              label = { Text("Sets") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = customReps,
              onValueChange = { customReps = it },
              label = { Text("Reps") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = customWeight,
              onValueChange = { customWeight = it },
              label = { Text("Kg") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (customName.isNotBlank()) {
              viewModel.addCustomExercise(
                name = customName,
                targetMuscle = customMuscle,
                sets = customSets.toIntOrNull() ?: 3,
                reps = customReps,
                weightKg = customWeight.toDoubleOrNull() ?: 0.0,
                coachCue = "Maintain strict tempo and form throughout every repetition."
              )
              showAddExerciseDialog = false
              customName = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PureBlack)
        ) {
          Text("Add Movement", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddExerciseDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // RPE Rating Dialog
  if (showRpeDialog) {
    AlertDialog(
      onDismissRequest = { showRpeDialog = false },
      title = { Text("Rate Workout Exertion (RPE)", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            "How difficult did today's training feel on the Borg RPE scale? Your AI Trainer uses this to calibrate recovery demands.",
            style = MaterialTheme.typography.bodyMedium
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            listOf(6, 7, 8, 9, 10).forEach { score ->
              Surface(
                shape = CircleShape,
                color = if (workout.userRpeExertion == score) LightGreen else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (workout.userRpeExertion == score) LightGreen else DarkBorder),
                modifier = Modifier
                  .size(46.dp)
                  .clickable {
                    viewModel.submitRpeExertion(score)
                    showRpeDialog = false
                  },
                shadowElevation = 2.dp
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "$score",
                    fontWeight = FontWeight.Black,
                    color = if (workout.userRpeExertion == score) PureBlack else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "6: Moderate • 8: 2 Reps In Tank • 10: Absolute Max Failure",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showRpeDialog = false }) {
          Text("Done")
        }
      }
    )
  }
}

@Composable
private fun ExerciseListItem(
  exercise: Exercise,
  onCompleteSet: () -> Unit,
  onCheckForm: () -> Unit
) {
  val isCompleted = exercise.completedSets >= exercise.sets

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
    ),
    border = BorderStroke(
      1.dp,
      if (isCompleted) LightGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${exercise.targetMuscle} • ${exercise.sets} sets × ${exercise.reps} @ ${exercise.weightKg} kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isCompleted) LightGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
          border = BorderStroke(1.dp, if (isCompleted) LightGreen else DarkBorder)
        ) {
          Text(
            text = "${exercise.completedSets}/${exercise.sets} SETS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isCompleted) LightGreen else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.Top) {
        Icon(
          imageVector = Icons.Default.TipsAndUpdates,
          contentDescription = null,
          tint = LightGreen,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = exercise.coachCue,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onCheckForm,
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
        ) {
          Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = LightGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Form Analyzer", color = LightGreen, style = MaterialTheme.typography.labelMedium)
        }

        Button(
          onClick = onCompleteSet,
          enabled = !isCompleted,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompleted) Color.Gray else LightGreen,
            contentColor = PureBlack
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("complete_set_${exercise.id}")
        ) {
          Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isCompleted) "Completed" else "Log Set ${exercise.completedSets + 1}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
