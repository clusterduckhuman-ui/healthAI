package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ApexTopBar
import com.example.ui.components.ProfileSettingsDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FitnessViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val fitnessViewModel: FitnessViewModel = viewModel()
      val isDarkOverride by fitnessViewModel.isDarkThemeOverride.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val isDarkTheme = isDarkOverride ?: systemInDark

      MyApplicationTheme(darkTheme = isDarkTheme) {
        ApexPulseApp(viewModel = fitnessViewModel, isDarkTheme = isDarkTheme)
      }
    }
  }
}

data class NavItem(
  val screen: AppScreen,
  val icon: ImageVector,
  val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexPulseApp(
  viewModel: FitnessViewModel,
  isDarkTheme: Boolean
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val biometrics by viewModel.biometrics.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  var showProfileDialog by remember { mutableStateOf(false) }

  // BackHandler: Navigate back to AI Trainer if on another screen
  BackHandler(enabled = currentScreen != AppScreen.AI_TRAINER) {
    viewModel.navigateTo(AppScreen.AI_TRAINER)
  }

  // Handle Feedback snackbars
  LaunchedEffect(feedbackMessage) {
    feedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearFeedback()
    }
  }

  val primaryNavItems = listOf(
    NavItem(AppScreen.AI_TRAINER, Icons.Default.FitnessCenter, "AI Trainer"),
    NavItem(AppScreen.GYM_FORM, Icons.Default.Videocam, "Form Check"),
    NavItem(AppScreen.FOOD_SCAN, Icons.Default.QrCodeScanner, "Food Scan"),
    NavItem(AppScreen.MEAL_PREP, Icons.Default.Restaurant, "Meal Prep"),
    NavItem(AppScreen.HIIT_SESSIONS, Icons.Default.Bolt, "Quick HIIT")
  )

  val secondaryNavItems = listOf(
    NavItem(AppScreen.WEARABLE_SYNC, Icons.Default.Watch, "Wearables"),
    NavItem(AppScreen.SOCIAL, Icons.Default.Group, "Community"),
    NavItem(AppScreen.PRICING, Icons.Default.Stars, "Apex Elite")
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      ApexTopBar(
        biometrics = biometrics,
        isTrialActive = userProfile.isTrialActive,
        isDarkTheme = isDarkTheme,
        onSyncClicked = { viewModel.syncWearableDevice() },
        onPricingClicked = { viewModel.navigateTo(AppScreen.PRICING) },
        onThemeToggleClicked = { viewModel.setDarkTheme(!isDarkTheme) },
        onSettingsClicked = { showProfileDialog = true }
      )
    },
    bottomBar = {
      Column {
        // Quick Secondary Navigation Row (Wearables, Social, VIP)
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            secondaryNavItems.forEach { item ->
              val isSelected = currentScreen == item.screen
              TextButton(
                onClick = { viewModel.navigateTo(item.screen) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("nav_secondary_${item.screen.name}")
              ) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = item.label,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) VoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = item.label,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) VoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Primary Navigation Bar
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp,
          windowInsets = WindowInsets.navigationBars
        ) {
          primaryNavItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
              selected = isSelected,
              onClick = { viewModel.navigateTo(item.screen) },
              icon = {
                Icon(
                  imageVector = item.icon,
                  contentDescription = item.label
                )
              },
              label = {
                Text(
                  text = item.label,
                  style = MaterialTheme.typography.labelSmall
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PureBlack,
                indicatorColor = LightGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = LightGreen
              ),
              modifier = Modifier.testTag("nav_primary_${item.screen.name}")
            )
          }
        }
      }
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
      ) { screen ->
        when (screen) {
          AppScreen.AI_TRAINER -> AiTrainerScreen(
            viewModel = viewModel,
            onNavigateToScreen = { target -> viewModel.navigateTo(target) }
          )
          AppScreen.GYM_FORM -> GymTrainerFormScreen(viewModel = viewModel)
          AppScreen.FOOD_SCAN -> FoodScannerScreen(viewModel = viewModel)
          AppScreen.MEAL_PREP -> MealPrepScreen(viewModel = viewModel)
          AppScreen.HIIT_SESSIONS -> HiitWorkoutsScreen(viewModel = viewModel)
          AppScreen.WEARABLE_SYNC -> WearableSyncScreen(viewModel = viewModel)
          AppScreen.SOCIAL -> SocialCommunityScreen(viewModel = viewModel)
          AppScreen.PRICING -> SubscriptionPaywallScreen(viewModel = viewModel)
        }
      }
    }
  }

  // Profile Settings Dialog
  if (showProfileDialog) {
    ProfileSettingsDialog(
      viewModel = viewModel,
      onDismiss = { showProfileDialog = false },
      onOpenPricing = { viewModel.navigateTo(AppScreen.PRICING) }
    )
  }
}
