package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel

@Composable
fun SubscriptionPaywallScreen(
  viewModel: FitnessViewModel
) {
  val userProfile by viewModel.userProfile.collectAsState()
  var selectedBilling by remember { mutableStateOf("ANNUAL") }

  val perksList = listOf(
    "Unlimited AI Gym Trainer Computer Vision Form Feedback",
    "Real-Time Biometric Adaptive Workout Engine (Heart Rate, Strain, Recovery)",
    "AI Food & Barcode Scanner with Strict Allergen Guards",
    "Kitchen Equipment-Tailored Meal Prep & Batch Recipes",
    "Express HIIT Intervals for Busy Schedules with Video Guides",
    "Wearable Health Sync (Garmin, Apple Watch, WHOOP, Oura, Pixel)",
    "Accountability Buddies & Shared Streak Challenges"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top VIP Emblem Card
    item {
      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.5.dp, VoltGreen),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pricing_hero_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(VoltGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Diamond,
              contentDescription = null,
              tint = VoltGreen,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "APEX ELITE VIP",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          Text(
            text = "Your 24/7 AI Trainer, Nutritionist & Gym Form Specialist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = VoltGreen,
            modifier = Modifier.padding(horizontal = 8.dp)
          ) {
            Text(
              text = "⭐ 7-DAY FREE TRIAL FOR ALL NEW ATHLETES • $0 TODAY ⭐",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
              color = DarkBackground,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Trial Status Banner
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (userProfile.isTrialActive) VoltGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (userProfile.isTrialActive) VoltGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = if (userProfile.isTrialActive) "Free Trial Active: ${userProfile.trialDaysLeft} Days Left" else "Trial Ended",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = if (userProfile.isTrialActive) VoltGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Full unlimited access to all AI features & wearable sync",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (userProfile.isTrialActive) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(24.dp))
          }
        }
      }
    }

    // Billing Options: Annual vs Monthly
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Annual Plan
        PlanOptionCard(
          title = "ANNUAL PASS",
          price = "$6.67 / mo",
          billingSub = "$79.99 billed annually",
          badgeText = "SAVE 48%",
          isSelected = selectedBilling == "ANNUAL",
          onClick = { selectedBilling = "ANNUAL" },
          modifier = Modifier.weight(1f)
        )

        // Monthly Plan
        PlanOptionCard(
          title = "MONTHLY PASS",
          price = "$12.99 / mo",
          billingSub = "Billed monthly",
          badgeText = "FLEXIBLE",
          isSelected = selectedBilling == "MONTHLY",
          onClick = { selectedBilling = "MONTHLY" },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Feature Checklist
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Everything Included in Apex Elite",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          perksList.forEach { perk ->
            Row(
              modifier = Modifier.padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VoltGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = perk,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // CTA Button
    item {
      Button(
        onClick = { viewModel.activateFreeTrial() },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("start_free_trial_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = DarkBackground),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Start 7-Day Free Trial (Pay $0.00 Today)",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleSmall
          )
        }
      }
    }

    // Disclaimer
    item {
      Text(
        text = "No commitment. Cancel easily at any time in Google Play Subscription settings before the 7-day trial ends to avoid recurring charges.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun PlanOptionCard(
  title: String,
  price: String,
  billingSub: String,
  badgeText: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) VoltGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    ),
    border = BorderStroke(
      2.dp,
      if (isSelected) VoltGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    ),
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) VoltGreen else MaterialTheme.colorScheme.background
      ) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) DarkBackground else CyberTeal,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = price,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
        color = if (isSelected) VoltGreen else MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = billingSub,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}
