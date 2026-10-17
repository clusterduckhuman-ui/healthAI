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
import com.example.model.FoodScanItem
import com.example.ui.components.CircularProgressBadge
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun FoodScannerScreen(
  viewModel: FitnessViewModel
) {
  val foodLogs by viewModel.foodLogs.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val scannedResult by viewModel.scannedFoodResult.collectAsState()
  val isScanning by viewModel.isScanningCamera.collectAsState()

  // Calculated Daily Totals
  val totalCalories = foodLogs.sumOf { it.calories }
  val totalProtein = foodLogs.sumOf { it.proteinGrams.toInt() }
  val totalCarbs = foodLogs.sumOf { it.carbsGrams.toInt() }
  val totalFat = foodLogs.sumOf { it.fatGrams.toInt() }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Screen Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "AI Food Scanner",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Instant Calorie & Macro Analysis with Allergen Check",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CyberTeal.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, CyberTeal)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${userProfile.activeAllergens.size} Filter Guards",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = CyberTeal
            )
          }
        }
      }
    }

    // Daily Macro Dashboard Ring Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Calorie Ring
            Box(
              modifier = Modifier.size(100.dp),
              contentAlignment = Alignment.Center
            ) {
              val calorieRatio = (totalCalories.toFloat() / userProfile.dailyCalorieTarget).coerceIn(0f, 1f)
              CircularProgressBadge(
                progressPercent = calorieRatio,
                fillColor = VoltGreen,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                strokeWidth = 10f,
                modifier = Modifier.fillMaxSize()
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "$totalCalories",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "of ${userProfile.dailyCalorieTarget} kcal",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Macro Bars (Protein, Carbs, Fat)
            Column(modifier = Modifier.weight(1f)) {
              MacroLinearIndicator(
                label = "Protein",
                current = totalProtein,
                target = userProfile.dailyProteinTargetGrams,
                color = CyberTeal
              )
              Spacer(modifier = Modifier.height(10.dp))
              MacroLinearIndicator(
                label = "Carbohydrates",
                current = totalCarbs,
                target = 250,
                color = FlameOrange
              )
              Spacer(modifier = Modifier.height(10.dp))
              MacroLinearIndicator(
                label = "Healthy Fats",
                current = totalFat,
                target = 70,
                color = RecoveryGold
              )
            }
          }
        }
      }
    }

    // Scanner Camera Viewfinder Simulation Card
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("food_scanner_viewfinder")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Camera Simulation Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(PureBlack),
            contentAlignment = Alignment.Center
          ) {
            // Viewfinder Reticle
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val reticleColor = if (isScanning) VoltGreen else CyberTeal.copy(alpha = 0.6f)
              val cornerLen = 30f

              // 4 Corner Brackets
              val pad = 36f
              // Top-Left
              drawLine(reticleColor, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeWidth = 5f)
              drawLine(reticleColor, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeWidth = 5f)
              // Top-Right
              drawLine(reticleColor, Offset(w - pad, pad), Offset(w - pad - cornerLen, pad), strokeWidth = 5f)
              drawLine(reticleColor, Offset(w - pad, pad), Offset(w - pad, pad + cornerLen), strokeWidth = 5f)
              // Bottom-Left
              drawLine(reticleColor, Offset(pad, h - pad), Offset(pad + cornerLen, h - pad), strokeWidth = 5f)
              drawLine(reticleColor, Offset(pad, h - pad), Offset(pad, h - pad - cornerLen), strokeWidth = 5f)
              // Bottom-Right
              drawLine(reticleColor, Offset(w - pad, h - pad), Offset(w - pad - cornerLen, h - pad), strokeWidth = 5f)
              drawLine(reticleColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - cornerLen), strokeWidth = 5f)

              if (isScanning) {
                // Laser scan line across middle
                drawLine(
                  color = VoltGreen,
                  start = Offset(pad, h / 2f),
                  end = Offset(w - pad, h / 2f),
                  strokeWidth = 4f
                )
              }
            }

            if (isScanning) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                  color = VoltGreen,
                  modifier = Modifier.size(36.dp),
                  strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Analyzing Food Nutrients & Allergens...",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = VoltGreen
                )
              }
            } else {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = CyberTeal,
                  modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Point camera at meal, plate, or barcode",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Scan Trigger Presets
          Text(
            text = "Quick Camera Scan Simulation",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.triggerCameraFoodScan("Grilled Chicken & Quinoa Bowl") },
              modifier = Modifier
                .weight(1f)
                .testTag("scan_chicken_bowl_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Chicken Bowl", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }

            Button(
              onClick = { viewModel.triggerCameraFoodScan("Grass-Fed Steak & Sweet Potato") },
              modifier = Modifier
                .weight(1f)
                .testTag("scan_steak_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = DarkBackground),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.LunchDining, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Steak & Potato", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }

    // AI Nutrient Analysis Popup Card (When Item Scanned)
    scannedResult?.let { scanned ->
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.5.dp, VoltGreen),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("scanned_food_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = VoltGreen,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = scanned.healthGrade,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                      color = DarkBackground
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = scanned.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${scanned.servingGrams}g serving • ${scanned.calories} Calories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(onClick = { viewModel.dismissScannedResult() }) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(20.dp))
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Allergen Guard Badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              scanned.allergenWarnings.forEach { tag ->
                val isWarning = tag.contains("Contains", ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isWarning) PulseRed.copy(alpha = 0.2f) else VoltGreen.copy(alpha = 0.2f),
                  border = BorderStroke(1.dp, if (isWarning) PulseRed else VoltGreen)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Check,
                      contentDescription = null,
                      tint = if (isWarning) PulseRed else VoltGreen,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = tag,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isWarning) PulseRed else VoltGreen
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Macros Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              MacroBox("Protein", "${scanned.proteinGrams.toInt()}g", CyberTeal)
              MacroBox("Carbs", "${scanned.carbsGrams.toInt()}g", FlameOrange)
              MacroBox("Fats", "${scanned.fatGrams.toInt()}g", RecoveryGold)
              MacroBox("Fiber", "${scanned.fiberGrams.toInt()}g", VoltGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = scanned.aiNutrientAnalysis,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = { viewModel.confirmLogScannedFood() },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("confirm_log_food_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Log To Today's Nutrition (+${scanned.calories} kcal)", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Today's Food Diary List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Today's Logged Meals",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${foodLogs.size} meals logged",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Food Items
    items(foodLogs, key = { it.id }) { item ->
      FoodLogListItem(
        item = item,
        onDelete = { viewModel.deleteFoodItem(item.id) }
      )
    }
  }
}

@Composable
private fun MacroBox(label: String, value: String, color: Color) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.background,
    border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = Modifier.width(76.dp)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = color)
    }
  }
}

@Composable
private fun MacroLinearIndicator(label: String, current: Int, target: Int, color: Color) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(
        text = "$current / $target g",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = (current.toFloat() / target).coerceIn(0f, 1f),
      color = color,
      trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
      strokeCap = StrokeCap.Round,
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
    )
  }
}

@Composable
private fun FoodLogListItem(
  item: FoodScanItem,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = CyberTeal.copy(alpha = 0.2f)
          ) {
            Text(
              text = item.mealType.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
              color = CyberTeal,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = item.timeLogged,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${item.calories} kcal • P: ${item.proteinGrams.toInt()}g • C: ${item.carbsGrams.toInt()}g • F: ${item.fatGrams.toInt()}g",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
      }
    }
  }
}
