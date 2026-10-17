package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsDialog(
  viewModel: FitnessViewModel,
  onDismiss: () -> Unit,
  onOpenPricing: () -> Unit
) {
  val userProfile by viewModel.userProfile.collectAsState()
  val isDarkTheme by viewModel.isDarkThemeOverride.collectAsState()

  val appliances = listOf("Air Fryer", "Blender", "Instant Pot", "Oven", "Stovetop", "Slow Cooker", "Microwave", "Food Processor")
  val allergens = listOf("Nut-Free", "Shellfish-Free", "Gluten-Free", "Dairy-Free", "Egg-Free", "Soy-Free", "Vegan", "Keto")
  val goals = listOf("Lean Muscle & Power", "Fat Loss / Cutting", "Endurance & Speed", "Clean Maintenance")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Tune, contentDescription = null, tint = VoltGreen)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Athlete Profile & Kitchen", fontWeight = FontWeight.Black)
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Dark Mode Preference
        item {
          Text("App Theme & Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = isDarkTheme == true,
              onClick = { viewModel.setDarkTheme(true) },
              label = { Text("Dark Mode", style = MaterialTheme.typography.labelSmall) },
              leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
              modifier = Modifier.testTag("theme_dark_chip")
            )
            FilterChip(
              selected = isDarkTheme == false,
              onClick = { viewModel.setDarkTheme(false) },
              label = { Text("Light Mode", style = MaterialTheme.typography.labelSmall) },
              leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
              modifier = Modifier.testTag("theme_light_chip")
            )
          }
        }

        // Fitness Goal
        item {
          Text("Primary Fitness Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            goals.forEach { g ->
              val isSel = userProfile.fitnessGoal == g
              FilterChip(
                selected = isSel,
                onClick = { viewModel.updateFitnessGoal(g) },
                label = { Text(g, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = VoltGreen,
                  selectedLabelColor = DarkBackground
                )
              )
            }
          }
        }

        // Available Kitchen Equipment
        item {
          Text("Kitchen Equipment Owned (${userProfile.selectedEquipment.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Text("Meal preps will adapt directly to these tools.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            appliances.forEach { appliance ->
              val isSel = userProfile.selectedEquipment.contains(appliance)
              FilterChip(
                selected = isSel,
                onClick = { viewModel.toggleEquipment(appliance) },
                label = { Text(appliance, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                  if (isSel) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                }
              )
            }
          }
        }

        // Food Allergies & Restrictions
        item {
          Text("Food Allergies & Restrictions (${userProfile.activeAllergens.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Text("Strictly guarded in food scans and recipe recommendations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            allergens.forEach { al ->
              val isSel = userProfile.activeAllergens.contains(al)
              FilterChip(
                selected = isSel,
                onClick = { viewModel.toggleAllergy(al) },
                label = { Text(al, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                  if (isSel) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CyberTeal,
                  selectedLabelColor = DarkBackground
                )
              )
            }
          }
        }

        // Subscription Tier link
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = VoltGreen.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, VoltGreen),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onDismiss()
                onOpenPricing()
              }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Apex Elite VIP Tier", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = VoltGreen)
                Text("7-Day Free Trial Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VoltGreen)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground)
      ) {
        Text("Save & Close", fontWeight = FontWeight.Bold)
      }
    }
  )
}
