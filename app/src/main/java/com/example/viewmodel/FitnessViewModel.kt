package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FitnessRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen(val label: String) {
  AI_TRAINER("AI Trainer"),
  GYM_FORM("Form Feedback"),
  FOOD_SCAN("Food Scanner"),
  MEAL_PREP("Meal Prep"),
  HIIT_SESSIONS("Quick HIIT"),
  WEARABLE_SYNC("Wearables"),
  SOCIAL("Community"),
  PRICING("Apex Elite")
}

class FitnessViewModel(
  private val repository: FitnessRepository = FitnessRepository()
) : ViewModel() {

  val biometrics: StateFlow<BiometricData> = repository.biometrics
  val userProfile: StateFlow<UserProfile> = repository.userProfile
  val currentWorkout: StateFlow<WorkoutPlan> = repository.currentWorkout
  val foodLogs: StateFlow<List<FoodScanItem>> = repository.foodLogs
  val recipes: StateFlow<List<MealPrepRecipe>> = repository.recipes
  val hiitSessions: StateFlow<List<HiitSession>> = repository.hiitSessions
  val socialFriends: StateFlow<List<FriendAccount>> = repository.socialFriends
  val formChecks: StateFlow<Map<String, MovementFormCheck>> = repository.formChecks
  val selectedFormMovement: StateFlow<String> = repository.selectedFormMovement

  // Navigation
  private val _currentScreen = MutableStateFlow(AppScreen.AI_TRAINER)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  // App Theme: null = follow system, true = dark, false = light
  private val _isDarkThemeOverride = MutableStateFlow<Boolean?>(false) // default to clean white theme
  val isDarkThemeOverride: StateFlow<Boolean?> = _isDarkThemeOverride.asStateFlow()

  // HIIT Interval Timer State
  private val _selectedHiitSession = MutableStateFlow<HiitSession?>(null)
  val selectedHiitSession: StateFlow<HiitSession?> = _selectedHiitSession.asStateFlow()

  private val _isTimerRunning = MutableStateFlow(false)
  val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

  private val _timerSecondsRemaining = MutableStateFlow(30)
  val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

  private val _isWorkInterval = MutableStateFlow(true)
  val isWorkInterval: StateFlow<Boolean> = _isWorkInterval.asStateFlow()

  private val _currentRound = MutableStateFlow(1)
  val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

  private var timerJob: Job? = null

  // Food Scanner UI state
  private val _scannedFoodResult = MutableStateFlow<FoodScanItem?>(null)
  val scannedFoodResult: StateFlow<FoodScanItem?> = _scannedFoodResult.asStateFlow()

  private val _isScanningCamera = MutableStateFlow(false)
  val isScanningCamera: StateFlow<Boolean> = _isScanningCamera.asStateFlow()

  // Feedback Notification banner (Snackbars / Toast messages)
  private val _userFeedbackMessage = MutableStateFlow<String?>(null)
  val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

  init {
    _selectedHiitSession.value = repository.hiitSessions.value.firstOrNull()
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun setDarkTheme(isDark: Boolean?) {
    _isDarkThemeOverride.value = isDark
  }

  fun showFeedback(msg: String) {
    _userFeedbackMessage.value = msg
  }

  fun clearFeedback() {
    _userFeedbackMessage.value = null
  }

  // AI Trainer
  fun generatePersonalizedWorkout(rpe: Int = 8) {
    repository.generatePersonalizedWorkout(rpe)
    showFeedback("AI Trainer adapted workout to ${biometrics.value.recoveryScore}% recovery score!")
  }

  fun completeExerciseSet(exerciseId: String) {
    repository.completeExerciseSet(exerciseId)
    showFeedback("Set marked complete! Biometrics & strain updated.")
  }

  fun submitRpeExertion(rpe: Int) {
    repository.recordWorkoutRpe(rpe)
    showFeedback("RPE $rpe recorded! Future sets calibrated for maximum hypertrophy.")
  }

  // Form Analysis
  fun selectFormMovement(movement: String) {
    repository.selectFormMovement(movement)
  }

  fun updateFormCheckAngle(movement: String, angle: Int, depth: Int, spine: String, audioCue: String) {
    repository.updateFormCheckAngle(movement, angle, depth, spine, audioCue)
  }

  // Food Scanner
  fun triggerCameraFoodScan(foodNamePreset: String? = null) {
    viewModelScope.launch {
      _isScanningCamera.value = true
      delay(1200) // realistic AI vision processing simulation
      _isScanningCamera.value = false

      val userAllergens = userProfile.value.activeAllergens
      val scannedItem = if (foodNamePreset != null) {
        when (foodNamePreset) {
          "Grilled Chicken & Quinoa Bowl" -> FoodScanItem(
            id = "scan_${System.currentTimeMillis()}",
            name = "Grilled Lemon Chicken with Quinoa & Steamed Greens",
            mealType = "Lunch",
            calories = 520,
            proteinGrams = 48.0,
            carbsGrams = 44.0,
            fatGrams = 12.0,
            fiberGrams = 6.0,
            servingGrams = 340,
            healthGrade = "A+",
            allergenWarnings = listOfNotNull(
              if (userAllergens.contains("Gluten-Free")) "Gluten-Free Safe" else null,
              if (userAllergens.contains("Nut-Free")) "Nut-Free Safe" else null
            ),
            aiNutrientAnalysis = "High biological value complete protein. Micronutrient-rich with 45% daily magnesium and zinc."
          )
          "Grass-Fed Steak & Sweet Potato" -> FoodScanItem(
            id = "scan_${System.currentTimeMillis()}",
            name = "Grass-Fed Sirloin Steak with Roasted Sweet Potato",
            mealType = "Dinner",
            calories = 610,
            proteinGrams = 54.0,
            carbsGrams = 38.0,
            fatGrams = 24.0,
            fiberGrams = 5.0,
            servingGrams = 320,
            healthGrade = "A",
            allergenWarnings = listOf("Nut-Free Safe", "Dairy-Free Safe", "Gluten-Free Safe"),
            aiNutrientAnalysis = "Rich in heme iron, bioavailable creatine, and vitamin B12 for recovery."
          )
          else -> createGenericFoodScan()
        }
      } else {
        createGenericFoodScan()
      }

      _scannedFoodResult.value = scannedItem
      showFeedback("AI Vision identified: ${scannedItem.name}")
    }
  }

  private fun createGenericFoodScan(): FoodScanItem {
    return FoodScanItem(
      id = "scan_${System.currentTimeMillis()}",
      name = "Avocado Toast on Sourdough with Poached Egg",
      mealType = "Breakfast",
      calories = 410,
      proteinGrams = 18.0,
      carbsGrams = 36.0,
      fatGrams = 22.0,
      fiberGrams = 7.0,
      servingGrams = 210,
      healthGrade = "A",
      allergenWarnings = listOf("Contains Egg", "Contains Gluten (Wheat)"),
      aiNutrientAnalysis = "Monounsaturated fatty acids from avocado support hormonal health; egg lutein and choline aid neuromuscular signaling."
    )
  }

  fun dismissScannedResult() {
    _scannedFoodResult.value = null
  }

  fun confirmLogScannedFood() {
    val item = _scannedFoodResult.value ?: return
    repository.logFoodItem(item)
    _scannedFoodResult.value = null
    showFeedback("Logged ${item.calories} kcal to your daily nutrition!")
  }

  fun deleteFoodItem(id: String) {
    repository.removeFoodItem(id)
    showFeedback("Food entry removed.")
  }

  // Meal Prep Equipment & Allergies
  fun toggleEquipment(equipment: String) {
    repository.toggleEquipment(equipment)
  }

  fun toggleAllergy(allergy: String) {
    repository.toggleAllergy(allergy)
  }

  // HIIT Timer controls
  fun selectHiitSession(session: HiitSession) {
    _selectedHiitSession.value = session
    resetTimer()
  }

  fun startTimer() {
    if (_isTimerRunning.value) return
    _isTimerRunning.value = true
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_isTimerRunning.value) {
        delay(1000)
        if (_timerSecondsRemaining.value > 1) {
          _timerSecondsRemaining.value -= 1
        } else {
          // Switch interval
          val session = _selectedHiitSession.value ?: break
          if (_isWorkInterval.value) {
            _isWorkInterval.value = false
            _timerSecondsRemaining.value = session.restSeconds
            showFeedback("REST PHASE! Shake out arms and legs.")
          } else {
            val nextRound = _currentRound.value + 1
            if (nextRound > session.rounds) {
              _isTimerRunning.value = false
              showFeedback("HIIT SESSION COMPLETE! Burned ~${session.estimatedBurnKcal} kcal! 🔥")
              break
            } else {
              _currentRound.value = nextRound
              _isWorkInterval.value = true
              _timerSecondsRemaining.value = session.workSeconds
              showFeedback("ROUND $nextRound: WORK! Give 100% effort!")
            }
          }
        }
      }
    }
  }

  fun pauseTimer() {
    _isTimerRunning.value = false
    timerJob?.cancel()
  }

  fun resetTimer() {
    _isTimerRunning.value = false
    timerJob?.cancel()
    val session = _selectedHiitSession.value
    _timerSecondsRemaining.value = session?.workSeconds ?: 30
    _isWorkInterval.value = true
    _currentRound.value = 1
  }

  fun skipInterval() {
    val session = _selectedHiitSession.value ?: return
    if (_isWorkInterval.value) {
      _isWorkInterval.value = false
      _timerSecondsRemaining.value = session.restSeconds
    } else {
      _currentRound.value = minOf(session.rounds, _currentRound.value + 1)
      _isWorkInterval.value = true
      _timerSecondsRemaining.value = session.workSeconds
    }
  }

  // Wearable Health Sync
  fun syncWearableDevice(deviceName: String? = null) {
    viewModelScope.launch {
      showFeedback("Syncing biometric telemetry with wearable...")
      delay(800)
      repository.triggerWearableSync(deviceName)
      showFeedback("Wearable synced! Live biometrics & recovery refreshed.")
    }
  }

  // Social
  fun sendFistBump(friendId: String, friendName: String) {
    repository.sendFistBump(friendId)
    showFeedback("👊 Fist Bump sent to $friendName! Streak boosted!")
  }

  fun toggleAccountabilityBuddy(friendId: String) {
    repository.toggleAccountabilityBuddy(friendId)
  }

  // Subscription / Pricing
  fun activateFreeTrial() {
    repository.activateTrialOrSubscription(isSubscribed = true)
    showFeedback("🎉 7-Day Apex Elite VIP Trial Activated! All AI features unlocked.")
  }

  fun updateFitnessGoal(goal: String) {
    repository.updateFitnessGoal(goal)
    showFeedback("Fitness goal updated to $goal")
  }

  val waterIntakeMl: StateFlow<Int> = repository.waterIntakeMl

  fun logWater(amount: Int) {
    repository.logWater(amount)
    showFeedback("+$amount ml hydration logged! 💧")
  }

  fun resetWater() {
    repository.resetWater()
    showFeedback("Hydration reset for today.")
  }

  fun addCustomExercise(name: String, targetMuscle: String, sets: Int, reps: String, weightKg: Double, coachCue: String) {
    val newEx = Exercise(
      id = "custom_${System.currentTimeMillis()}",
      name = name,
      targetMuscle = targetMuscle,
      sets = sets,
      reps = reps,
      weightKg = weightKg,
      coachCue = coachCue
    )
    repository.addCustomExercise(newEx)
    showFeedback("Added '$name' to your routine!")
  }

  // Form Tempo Metronome
  private val _isMetronomeRunning = MutableStateFlow(false)
  val isMetronomeRunning: StateFlow<Boolean> = _isMetronomeRunning.asStateFlow()

  private val _metronomeCount = MutableStateFlow(1)
  val metronomeCount: StateFlow<Int> = _metronomeCount.asStateFlow()

  private val _metronomePhase = MutableStateFlow("LOWER (3s)")
  val metronomePhase: StateFlow<String> = _metronomePhase.asStateFlow()

  private var metronomeJob: Job? = null

  fun toggleMetronome() {
    if (_isMetronomeRunning.value) {
      _isMetronomeRunning.value = false
      metronomeJob?.cancel()
    } else {
      _isMetronomeRunning.value = true
      metronomeJob?.cancel()
      metronomeJob = viewModelScope.launch {
        while (_isMetronomeRunning.value) {
          _metronomePhase.value = "LOWER (ECCENTRIC)"
          for (i in 3 downTo 1) {
            _metronomeCount.value = i
            delay(1000)
          }
          _metronomePhase.value = "PAUSE (HOLD BOTTOM)"
          _metronomeCount.value = 1
          delay(1000)
          _metronomePhase.value = "DRIVE (EXPLODE UP)"
          _metronomeCount.value = 1
          delay(1000)
          _metronomePhase.value = "RESET TOP"
          _metronomeCount.value = 1
          delay(1000)
        }
      }
    }
  }

  fun cancelTrialOrRestore() {
    repository.activateTrialOrSubscription(isSubscribed = false)
    showFeedback("Subscription preference updated.")
  }
}
