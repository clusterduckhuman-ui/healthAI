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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MealPrepRecipe
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealPrepScreen(
  viewModel: FitnessViewModel
) {
  val recipes by viewModel.recipes.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()

  val allKitchenAppliances = listOf(
    "Air Fryer", "Blender", "Instant Pot", "Oven", "Stovetop", "Slow Cooker", "Microwave", "Food Processor"
  )

  val allAllergenOptions = listOf(
    "Nut-Free", "Shellfish-Free", "Gluten-Free", "Dairy-Free", "Egg-Free", "Soy-Free", "Vegan", "Keto"
  )

  var selectedRecipeDetail by remember { mutableStateOf<MealPrepRecipe?>(null) }
  var goalFilter by remember { mutableStateOf("All Goals") }
  var showGroceryList by remember { mutableStateOf(false) }
  val checkedGroceryItems = remember { mutableStateMapOf<String, Boolean>() }

  // Filter recipes based on:
  // 1. Kitchen Equipment (must be capable with user's selected equipment)
  // 2. Allergies / Restrictions (must be safe for user's selected allergens)
  val filteredRecipes = recipes.filter { recipe ->
    val hasRequiredEquipment = recipe.requiredEquipment.all { equip ->
      userProfile.selectedEquipment.contains(equip)
    }
    // Check allergy safety
    val safeForUser = userProfile.activeAllergens.all { userAllergen ->
      recipe.freeFromAllergens.any { freeAllergen ->
        freeAllergen.contains(userAllergen.replace("-Free", ""), ignoreCase = true) ||
            userAllergen.contains(freeAllergen.replace("-Free", ""), ignoreCase = true)
      }
    }
    val matchesGoal = goalFilter == "All Goals" || recipe.targetFitnessGoal.contains(goalFilter, ignoreCase = true)
    hasRequiredEquipment && safeForUser && matchesGoal
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Hero Banner Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp)
          .testTag("meal_prep_hero_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, LightGreen)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle culinary grid lines
            for (x in 0..8) {
              val xPos = w * (x / 8f)
              drawLine(gridColor, Offset(xPos, 0f), Offset(xPos, h), strokeWidth = 1f)
            }
            // Accent geometric circles
            drawCircle(
              color = LightGreen.copy(alpha = 0.15f),
              radius = h * 0.7f,
              center = Offset(w * 0.88f, h * 0.45f)
            )
          }

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
                text = "BATCH COOKING TAILORED TO YOUR KITCHEN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Equipment-Optimized Recipes",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Filtered by your appliances, allergies & macros",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Equipment Selector Box (Pick what cooking equipment you have)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Kitchen, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Available Kitchen Equipment",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = VoltGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = "${userProfile.selectedEquipment.size} Active",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VoltGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Select what you have in your kitchen. We only suggest recipes you can actually cook!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          // FlowRow of Equipment Chips
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            allKitchenAppliances.forEach { appliance ->
              val isSelected = userProfile.selectedEquipment.contains(appliance)
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.toggleEquipment(appliance) },
                label = { Text(appliance, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  }
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = VoltGreen,
                  selectedLabelColor = DarkBackground,
                  containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("equipment_chip_${appliance.replace(" ", "_")}")
              )
            }
          }
        }
      }
    }

    // Food Allergies & Dietary Restrictions Guard Box
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, CyberTeal.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Food Allergies & Restrictions Guard",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CyberTeal.copy(alpha = 0.2f)
            ) {
              Text(
                text = "${userProfile.activeAllergens.size} Protected",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CyberTeal,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Active allergies strictly remove non-compliant ingredients from your meal plans.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            allAllergenOptions.forEach { allergen ->
              val isSelected = userProfile.activeAllergens.contains(allergen)
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.toggleAllergy(allergy = allergen) },
                label = { Text(allergen, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  }
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CyberTeal,
                  selectedLabelColor = DarkBackground,
                  containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("allergy_chip_${allergen.replace("-", "_")}")
              )
            }
          }
        }
      }
    }

    // Goal Filter Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Curated Meal Prep Menus (${filteredRecipes.size})",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(listOf("All Goals", "Lean Muscle", "Fat Loss", "Maintenance")) { goal ->
            val isSelected = goalFilter == goal
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) VoltGreen else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { goalFilter = goal }
            ) {
              Text(
                text = goal,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) DarkBackground else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Smart Batch Grocery Shopping List Banner (NEW FEATURE)
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, LightGreen.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showGroceryList = true }
          .testTag("open_grocery_list_card")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(LightGreen.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = LightGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Batch Prep Grocery Checklist",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Aggregated shopping list for this week's recipes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGreen)
        }
      }
    }

    // Recipe Cards
    if (filteredRecipes.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.SoupKitchen, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No Recipes Match Current Filter",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Try toggling on more kitchen equipment (e.g. Air Fryer, Instant Pot, Oven) or relax allergy restrictions.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredRecipes, key = { it.id }) { recipe ->
        RecipeCard(
          recipe = recipe,
          onViewDetails = { selectedRecipeDetail = recipe }
        )
      }
    }
  }

  // Recipe Step-by-Step Modal Dialog
  selectedRecipeDetail?.let { detail ->
    AlertDialog(
      onDismissRequest = { selectedRecipeDetail = null },
      title = {
        Text(detail.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      },
      text = {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          item {
            Text(
              text = "Batch Yield: ${detail.batchPortions} Meal Containers • ${detail.prepMinutes} min Cook",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = VoltGreen
            )
            Text(
              text = "${detail.caloriesPerPortion} kcal/portion • P: ${detail.proteinGrams.toInt()}g • C: ${detail.carbsGrams.toInt()}g • F: ${detail.fatGrams.toInt()}g",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Required Equipment:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            Text(detail.requiredEquipment.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = CyberTeal)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Ingredients:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            detail.ingredients.forEach { ing ->
              Text("• $ing", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text("Cooking Instructions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            detail.instructions.forEachIndexed { idx, step ->
              Text("${idx + 1}. $step", style = MaterialTheme.typography.bodySmall)
              Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Storage & Reheating Tip:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            Text(detail.storageAdvice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { selectedRecipeDetail = null },
          colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground)
        ) {
          Text("Got It!")
        }
      }
    )
  }

  // Grocery Checklist Dialog (NEW FEATURE)
  if (showGroceryList) {
    val allIngredients = filteredRecipes.flatMap { it.ingredients }.distinct()
    AlertDialog(
      onDismissRequest = { showGroceryList = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = LightGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Pantry & Grocery Checklist", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item {
            Text(
              "Check off items as you buy them for your meal preps:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
          }
          items(allIngredients) { ingredient ->
            val isChecked = checkedGroceryItems[ingredient] == true
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { checkedGroceryItems[ingredient] = !isChecked }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = isChecked,
                onCheckedChange = { checkedGroceryItems[ingredient] = it },
                colors = CheckboxDefaults.colors(
                  checkedColor = LightGreen,
                  checkmarkColor = PureBlack
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = ingredient,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                ),
                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showGroceryList = false },
          colors = ButtonDefaults.buttonColors(containerColor = LightGreen, contentColor = PureBlack)
        ) {
          Text("Done Shopping", fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
private fun RecipeCard(
  recipe: MealPrepRecipe,
  onViewDetails: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
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
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = VoltGreen.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, VoltGreen)
        ) {
          Text(
            text = recipe.targetFitnessGoal.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = VoltGreen,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${recipe.prepMinutes} mins",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = recipe.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Equipment & Allergen Safety Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        recipe.requiredEquipment.forEach { eq ->
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = CyberTeal.copy(alpha = 0.15f)
          ) {
            Text(
              text = eq,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
              color = CyberTeal,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        recipe.freeFromAllergens.take(2).forEach { al ->
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = VoltGreen.copy(alpha = 0.15f)
          ) {
            Text(
              text = al,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
              color = VoltGreen,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Macros & CTA Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "${recipe.caloriesPerPortion} kcal • ${recipe.batchPortions} containers",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "P: ${recipe.proteinGrams.toInt()}g • C: ${recipe.carbsGrams.toInt()}g • F: ${recipe.fatGrams.toInt()}g per meal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onViewDetails,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = VoltGreen),
          border = BorderStroke(1.dp, VoltGreen),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text("Recipe Steps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
      }
    }
  }
}
