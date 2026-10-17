package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FitnessRepository {

  private val _biometrics = MutableStateFlow(
    BiometricData(
      heartRateBpm = 136,
      recoveryScore = 84,
      strainScore = 14.6f,
      hrvMs = 74,
      activeCaloriesBurned = 640,
      stepsCount = 8920,
      sleepQualityScore = 91,
      vo2Max = 53.2,
      connectedDeviceName = "Garmin Forerunner 965",
      isLiveSyncing = true,
      batteryPercentage = 92,
      lastSyncTime = "Sync: 2m ago"
    )
  )
  val biometrics: StateFlow<BiometricData> = _biometrics.asStateFlow()

  private val _userProfile = MutableStateFlow(
    UserProfile(
      name = "Jordan Vance",
      age = 29,
      weightKg = 77.2,
      heightCm = 181,
      fitnessGoal = "Lean Muscle & Power",
      fitnessLevel = "Intermediate",
      dailyCalorieTarget = 2500,
      dailyProteinTargetGrams = 180,
      selectedEquipment = setOf("Air Fryer", "Blender", "Oven", "Stovetop", "Instant Pot"),
      activeAllergens = setOf("Nut-Free", "Shellfish-Free"),
      isSubscribed = false,
      isTrialActive = true,
      trialDaysLeft = 7
    )
  )
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _currentWorkout = MutableStateFlow(createDefaultWorkout(84))
  val currentWorkout: StateFlow<WorkoutPlan> = _currentWorkout.asStateFlow()

  private val _foodLogs = MutableStateFlow(createInitialFoodLogs())
  val foodLogs: StateFlow<List<FoodScanItem>> = _foodLogs.asStateFlow()

  private val _recipes = MutableStateFlow(createPresetRecipes())
  val recipes: StateFlow<List<MealPrepRecipe>> = _recipes.asStateFlow()

  private val _hiitSessions = MutableStateFlow(createPresetHiitSessions())
  val hiitSessions: StateFlow<List<HiitSession>> = _hiitSessions.asStateFlow()

  private val _socialFriends = MutableStateFlow(createInitialFriends())
  val socialFriends: StateFlow<List<FriendAccount>> = _socialFriends.asStateFlow()

  private val _formChecks = MutableStateFlow(createPresetFormChecks())
  val formChecks: StateFlow<Map<String, MovementFormCheck>> = _formChecks.asStateFlow()

  private val _selectedFormMovement = MutableStateFlow("Barbell Back Squat")
  val selectedFormMovement: StateFlow<String> = _selectedFormMovement.asStateFlow()

  private val _waterIntakeMl = MutableStateFlow(2250)
  val waterIntakeMl: StateFlow<Int> = _waterIntakeMl.asStateFlow()

  fun logWater(amountMl: Int) {
    _waterIntakeMl.update { minOf(6000, it + amountMl) }
  }

  fun resetWater() {
    _waterIntakeMl.value = 0
  }

  fun addCustomExercise(exercise: Exercise) {
    _currentWorkout.update { plan ->
      plan.copy(exercises = plan.exercises + exercise)
    }
  }

  fun triggerWearableSync(deviceName: String? = null) {
    _biometrics.update { current ->
      val newHr = (130..155).random()
      val newSteps = current.stepsCount + (150..450).random()
      val newCals = current.activeCaloriesBurned + (15..45).random()
      val newStrain = minOf(21.0f, current.strainScore + 0.3f)
      current.copy(
        heartRateBpm = newHr,
        stepsCount = newSteps,
        activeCaloriesBurned = newCals,
        strainScore = (Math.round(newStrain * 10) / 10.0).toFloat(),
        connectedDeviceName = deviceName ?: current.connectedDeviceName,
        isLiveSyncing = true,
        lastSyncTime = "Sync: Just now"
      )
    }
  }

  fun generatePersonalizedWorkout(rpePerformance: Int = 8) {
    val currentRecovery = _biometrics.value.recoveryScore
    val newPlan = createDefaultWorkout(currentRecovery, rpePerformance)
    _currentWorkout.value = newPlan
  }

  fun completeExerciseSet(exerciseId: String) {
    _currentWorkout.update { plan ->
      val updatedExercises = plan.exercises.map { ex ->
        if (ex.id == exerciseId) {
          val nextSets = minOf(ex.sets, ex.completedSets + 1)
          ex.copy(completedSets = nextSets)
        } else ex
      }
      val allDone = updatedExercises.all { it.completedSets >= it.sets }
      plan.copy(exercises = updatedExercises, isCompleted = allDone)
    }
    // Also bump strain and burned calories
    _biometrics.update {
      it.copy(
        activeCaloriesBurned = it.activeCaloriesBurned + 35,
        strainScore = minOf(21.0f, it.strainScore + 0.4f),
        heartRateBpm = (145..168).random()
      )
    }
  }

  fun recordWorkoutRpe(rpe: Int) {
    _currentWorkout.update { it.copy(userRpeExertion = rpe) }
    // Adaptive feedback: if RPE was too high (>9) with lower recovery, next generation adjusts
  }

  fun selectFormMovement(movementName: String) {
    _selectedFormMovement.value = movementName
  }

  fun updateFormCheckAngle(movement: String, angle: Int, depth: Int, spine: String, audioCue: String) {
    _formChecks.update { current ->
      val existing = current[movement] ?: return@update current
      val score = when {
        depth >= 90 && spine.contains("Optimal", ignoreCase = true) -> 96
        depth >= 75 -> 84
        else -> 68
      }
      val updated = existing.copy(
        currentAngleDegrees = angle,
        depthPercent = depth,
        spineNeutrality = spine,
        audioCueText = audioCue,
        scoreOutOf100 = score
      )
      current + (movement to updated)
    }
  }

  fun logFoodItem(food: FoodScanItem) {
    _foodLogs.update { listOf(food) + it }
  }

  fun removeFoodItem(foodId: String) {
    _foodLogs.update { list -> list.filterNot { it.id == foodId } }
  }

  fun toggleEquipment(equipment: String) {
    _userProfile.update { profile ->
      val current = profile.selectedEquipment.toMutableSet()
      if (current.contains(equipment)) {
        current.remove(equipment)
      } else {
        current.add(equipment)
      }
      profile.copy(selectedEquipment = current)
    }
  }

  fun toggleAllergy(allergy: String) {
    _userProfile.update { profile ->
      val current = profile.activeAllergens.toMutableSet()
      if (current.contains(allergy)) {
        current.remove(allergy)
      } else {
        current.add(allergy)
      }
      profile.copy(activeAllergens = current)
    }
  }

  fun sendFistBump(friendId: String) {
    _socialFriends.update { list ->
      list.map { friend ->
        if (friend.id == friendId) {
          friend.copy(fistBumpsCount = friend.fistBumpsCount + 1)
        } else friend
      }
    }
  }

  fun toggleAccountabilityBuddy(friendId: String) {
    _socialFriends.update { list ->
      list.map { friend ->
        if (friend.id == friendId) {
          friend.copy(isAccountabilityBuddy = !friend.isAccountabilityBuddy)
        } else friend
      }
    }
  }

  fun activateTrialOrSubscription(isSubscribed: Boolean) {
    _userProfile.update {
      it.copy(
        isSubscribed = isSubscribed,
        isTrialActive = true,
        trialDaysLeft = 7
      )
    }
  }

  fun updateFitnessGoal(newGoal: String) {
    _userProfile.update { it.copy(fitnessGoal = newGoal) }
  }

  companion object {
    private fun createDefaultWorkout(recovery: Int, previousRpe: Int = 8): WorkoutPlan {
      val isHighRecovery = recovery >= 75
      val isLowRecovery = recovery < 45

      return if (isHighRecovery) {
        WorkoutPlan(
          id = "wp_adaptive_01",
          title = "High-Output Hypertrophy & Power",
          focusArea = "Chest, Shoulders & Triceps",
          durationMinutes = 48,
          intensityLevel = "High Strain",
          targetStrain = 16.5f,
          aiBiometricRationale = "Biometrics show $recovery% optimal recovery (HRV 74ms). Your central nervous system is primed for peak volume and heavy compound loads today.",
          exercises = listOf(
            Exercise("ex_1", "Incline Dumbbell Press", "Upper Chest", 4, "8-10 reps", 32.0, "Maintain 30-degree incline. Drive through palms without flaring elbows.", 1),
            Exercise("ex_2", "Barbell Overhead Press", "Anterior Deltoids", 4, "6-8 reps", 55.0, "Squeeze glutes and brace core. Lock out directly over mid-foot.", 0),
            Exercise("ex_3", "Weighted Dips", "Chest & Triceps", 3, "10-12 reps", 15.0, "Control descent for 3 seconds tempo to maximize mechanical tension.", 0),
            Exercise("ex_4", "Cable Lateral Raises", "Lateral Delts", 4, "12-15 reps", 12.5, "Lead with elbows. Keep scapulae depressed throughout range.", 0),
            Exercise("ex_5", "Overhead Rope Triceps Extension", "Triceps Long Head", 3, "12 reps", 22.5, "Keep elbows pinned close to temples for full stretch.", 0)
          )
        )
      } else if (isLowRecovery) {
        WorkoutPlan(
          id = "wp_adaptive_recovery",
          title = "Active Recovery & Fascial Mobility",
          focusArea = "Full Body Mobility & Core",
          durationMinutes = 30,
          intensityLevel = "Active Recovery",
          targetStrain = 8.0f,
          aiBiometricRationale = "Recovery is lower today at $recovery%. Recommending an active recovery session to flush metabolic waste and preserve joint longevity.",
          exercises = listOf(
            Exercise("ex_rec_1", "Cat-Cow & Thoracic Rotations", "Spine & Core", 3, "12 reps", 0.0, "Slow breathing cadence matched to spinal flexion."),
            Exercise("ex_rec_2", "Kettlebell Goblet Squat Holds", "Hips & Ankles", 3, "45 sec hold", 16.0, "Sink deep into hip crease, pry knees open gently."),
            Exercise("ex_rec_3", "Banded Face Pulls", "Rotator Cuff & Posture", 3, "15 reps", 10.0, "Retract shoulder blades and hold peak contraction for 2 sec."),
            Exercise("ex_rec_4", "Dead Bug Core Bracing", "Deep Transverse Abdominis", 3, "10 reps/side", 0.0, "Flatten lower back firmly into the floor.")
          )
        )
      } else {
        WorkoutPlan(
          id = "wp_adaptive_moderate",
          title = "Progressive Functional Conditioning",
          focusArea = "Posterior Chain & Back",
          durationMinutes = 42,
          intensityLevel = "Optimal Hypertrophy",
          targetStrain = 13.0f,
          aiBiometricRationale = "Recovery at $recovery% represents balanced readiness. Targeting moderate volume with progressive overload and strict form cadence.",
          exercises = listOf(
            Exercise("ex_mod_1", "Neutral Grip Pull-ups", "Lats & Biceps", 4, "8 reps", 0.0, "Full dead hang to chin clearly over the bar."),
            Exercise("ex_mod_2", "Barbell Romanian Deadlift", "Hamstrings & Glutes", 4, "10 reps", 80.0, "Hinge deeply at hips. Keep bar glued to shins."),
            Exercise("ex_mod_3", "Chest-Supported Dumbbell Rows", "Mid Back & Rhomboids", 3, "12 reps", 26.0, "Drive elbows back towards hip pockets."),
            Exercise("ex_mod_4", "Rear Delt Flyes", "Posterior Delts", 3, "15 reps", 10.0, "Control negative tempo for 2 seconds.")
          )
        )
      }
    }

    private fun createInitialFoodLogs(): List<FoodScanItem> = listOf(
      FoodScanItem(
        id = "food_1",
        name = "Wild Atlantic Salmon & Brown Rice Bowl",
        mealType = "Lunch",
        calories = 620,
        proteinGrams = 46.0,
        carbsGrams = 58.0,
        fatGrams = 22.0,
        fiberGrams = 6.5,
        servingGrams = 380,
        healthGrade = "A+",
        allergenWarnings = listOf("Nut-Free Safe", "Gluten-Free Safe"),
        aiNutrientAnalysis = "Optimal post-workout profile: High in EPA/DHA Omega-3s, bioavailable leucine for muscle protein synthesis, and clean low-GI complex carbohydrates.",
        timeLogged = "12:45 PM"
      ),
      FoodScanItem(
        id = "food_2",
        name = "Greek Yogurt Power Parfait with Berries & Chia",
        mealType = "Breakfast",
        calories = 380,
        proteinGrams = 32.0,
        carbsGrams = 42.0,
        fatGrams = 8.0,
        fiberGrams = 7.0,
        servingGrams = 260,
        healthGrade = "A+",
        allergenWarnings = listOf("Contains Dairy", "Nut-Free Safe"),
        aiNutrientAnalysis = "Excellent slow-digesting micellar casein and rich prebiotic polyphenols from blackberries and blueberries.",
        timeLogged = "08:15 AM"
      )
    )

    private fun createPresetRecipes(): List<MealPrepRecipe> = listOf(
      MealPrepRecipe(
        id = "recipe_1",
        title = "Crispy Lemon-Herb Air Fryer Chicken & Roasted Sweet Potatoes",
        targetFitnessGoal = "Lean Muscle Bulk",
        prepMinutes = 25,
        batchPortions = 4,
        caloriesPerPortion = 540,
        proteinGrams = 48.0,
        carbsGrams = 52.0,
        fatGrams = 14.0,
        requiredEquipment = listOf("Air Fryer"),
        freeFromAllergens = listOf("Gluten-Free", "Dairy-Free", "Nut-Free", "Shellfish-Free"),
        ingredients = listOf(
          "1.8 lbs Chicken Breast, cubed",
          "3 Large Sweet Potatoes, diced into 1/2 inch cubes",
          "2 cups Broccoli florets",
          "1.5 tbsp Extra Virgin Olive Oil",
          "1 tbsp Smoked Paprika, Garlic Powder & Sea Salt",
          "Fresh rosemary and lemon wedges"
        ),
        instructions = listOf(
          "Toss diced sweet potatoes with 1/2 tbsp olive oil, paprika, and salt. Air fry at 400°F (200°C) for 15 minutes shaking basket halfway.",
          "Season chicken cubes with herbs and remaining olive oil. Air fry at 380°F (190°C) for 10-12 minutes until internal temp reaches 165°F.",
          "Add broccoli florets for the final 4 minutes to crisp up.",
          "Divide evenly across 4 glass meal prep containers."
        ),
        storageAdvice = "Keeps fresh in airtight glass containers refrigerated for up to 5 days. Reheat at 350°F in air fryer for 3 mins."
      ),
      MealPrepRecipe(
        id = "recipe_2",
        title = "Instant Pot Shredded Barbacoa Beef & Cilantro-Lime Rice",
        targetFitnessGoal = "Lean Muscle Bulk",
        prepMinutes = 35,
        batchPortions = 5,
        caloriesPerPortion = 580,
        proteinGrams = 52.0,
        carbsGrams = 48.0,
        fatGrams = 18.0,
        requiredEquipment = listOf("Instant Pot", "Stovetop"),
        freeFromAllergens = listOf("Gluten-Free", "Dairy-Free", "Nut-Free", "Shellfish-Free"),
        ingredients = listOf(
          "2.5 lbs Lean Beef Chuck Roast / Brisket trimmed",
          "4 cloves Garlic, minced",
          "2 Chipotle peppers in adobo with 2 tbsp adobo sauce",
          "1 cup Beef bone broth",
          "2 cups Basmati rice",
          "Fresh cilantro & lime juice"
        ),
        instructions = listOf(
          "Sear seasoned beef in Instant Pot on 'Sauté' mode for 3 minutes per side.",
          "Blend chipotles, adobo, garlic, lime juice, and bone broth. Pour over beef.",
          "Seal valve and pressure cook on High for 55 minutes. Natural pressure release for 15 minutes.",
          "Shred beef with two forks in the juices. Cook rice on stovetop with lime and cilantro.",
          "Distribute into 5 containers with black beans and avocado."
        ),
        storageAdvice = "Freezer friendly for up to 2 months. Thaw overnight in fridge before reheating."
      ),
      MealPrepRecipe(
        id = "recipe_3",
        title = "Sheet-Pan Tuscan Salmon, Asparagus & Quinoa",
        targetFitnessGoal = "Fat Loss / Cutting",
        prepMinutes = 20,
        batchPortions = 4,
        caloriesPerPortion = 440,
        proteinGrams = 42.0,
        carbsGrams = 32.0,
        fatGrams = 16.0,
        requiredEquipment = listOf("Oven", "Stovetop"),
        freeFromAllergens = listOf("Gluten-Free", "Dairy-Free", "Nut-Free"),
        ingredients = listOf(
          "4 Salmon fillets (6 oz each)",
          "1 bunch fresh Asparagus, trimmed",
          "1.5 cups dry Quinoa (cooked in vegetable broth)",
          "1 pint Cherry tomatoes",
          "2 tbsp Balsamic glaze & Italian seasoning"
        ),
        instructions = listOf(
          "Preheat oven to 400°F (200°C) and line a large baking sheet with parchment.",
          "Arrange salmon fillets, asparagus, and cherry tomatoes in single layer.",
          "Drizzle with light olive oil, oregano, sea salt, and cracked black pepper.",
          "Roast for 14-16 minutes until salmon flakes tenderly with a fork.",
          "Pair with fluffy cooked quinoa in meal prep containers."
        ),
        storageAdvice = "Consume within 3-4 days. Enjoy chilled or gently warmed."
      ),
      MealPrepRecipe(
        id = "recipe_4",
        title = "High-Protein Supergreen Blender Smoothie Packs",
        targetFitnessGoal = "Clean Maintenance",
        prepMinutes = 10,
        batchPortions = 6,
        caloriesPerPortion = 340,
        proteinGrams = 36.0,
        carbsGrams = 38.0,
        fatGrams = 6.0,
        requiredEquipment = listOf("Blender"),
        freeFromAllergens = listOf("Gluten-Free", "Dairy-Free", "Nut-Free", "Shellfish-Free"),
        ingredients = listOf(
          "6 scoops Plant / Whey Isolate Protein powder",
          "3 cups Baby spinach & kale",
          "3 cups Frozen wild blueberries & bananas",
          "3 tbsp Ground flaxseed & chia seeds",
          "Oat milk or almond milk base"
        ),
        instructions = listOf(
          "Prep 6 freezer-safe reusable silicone bags.",
          "In each bag add 1/2 cup greens, 1/2 cup frozen fruit, and 1/2 tbsp seed mix.",
          "Store in freezer. In the morning, dump pack into blender with 1 scoop protein and 300ml cold liquid.",
          "Blend for 45 seconds on high speed for ultra-creamy nutrient shake."
        ),
        storageAdvice = "Packs keep frozen for up to 3 months. Zero morning prep time!"
      )
    )

    private fun createPresetHiitSessions(): List<HiitSession> = listOf(
      HiitSession(
        id = "hiit_5",
        title = "5-Min Micro-Dose Engine Ignition",
        durationMinutes = 5,
        workSeconds = 30,
        restSeconds = 15,
        rounds = 6,
        estimatedBurnKcal = 85,
        difficulty = "Express Burn",
        exercises = listOf(
          HiitExerciseDemo("Explosive High Knees", "30s Max Cadence", "Drive knees to waist level, pump arms rhythmically.", "Cardio & Hip Flexors", "High Knee Cadence 170+ spm"),
          HiitExerciseDemo("Push-Up to Mountain Climber", "30s Dynamic", "1 push-up followed by 4 rapid mountain climbers.", "Chest, Core & Shoulders", "Neutral spine, strict elbow tuck"),
          HiitExerciseDemo("Jump Squats with Soft Landing", "30s Explosive", "Triple extension at ankle, knee, hip. Absorb shock softly.", "Quads, Glutes & Calves", "Knees track over toes"),
          HiitExerciseDemo("Shadow Boxing with Sprawls", "30s High Speed", "Jab-Cross-Hook combos with sprawl drop every 5 punches.", "Full Body Conditioning", "Keep hands by cheekbones")
        )
      ),
      HiitSession(
        id = "hiit_10",
        title = "10-Min Desk Crunch MetCon",
        durationMinutes = 10,
        workSeconds = 40,
        restSeconds = 20,
        rounds = 10,
        estimatedBurnKcal = 160,
        difficulty = "Moderate Fast",
        exercises = listOf(
          HiitExerciseDemo("Burpee Over Nothing", "40s Work", "Drop chest to deck, jump up with vertical clap.", "Total Body & Anaerobic", "Pacing: 12-15 reps/round"),
          HiitExerciseDemo("Bicycle Crunches to Hollow Body", "40s Work", "Slow eccentric rotation, touch opposite elbow to knee.", "Anterior Core & Obliques", "Keep shoulder blades off ground"),
          HiitExerciseDemo("Reverse Lunge to Plyo Knee Drive", "20s per leg", "Step deep into lunge, explode upwards onto toes.", "Hamstrings, Glutes & Balance", "90-degree bend at front knee"),
          HiitExerciseDemo("Bear Crawl Hold & Shoulder Taps", "40s Work", "Knees hovering 2 inches off floor. Minimize hip sway.", "Core, Shoulders & Serratus", "Anti-rotational stability")
        )
      ),
      HiitSession(
        id = "hiit_15",
        title = "15-Min Tabata Inferno Protocol",
        durationMinutes = 15,
        workSeconds = 45,
        restSeconds = 15,
        rounds = 15,
        estimatedBurnKcal = 240,
        difficulty = "Intense / Advanced",
        exercises = listOf(
          HiitExerciseDemo("Tuck Jumps & Lateral Skater Bounds", "45s Max Effort", "Lateral bound side to side followed by explosive tuck.", "Cardiovascular & Fast Twitch Fibers", "Absorb landing into deep squat"),
          HiitExerciseDemo("Spiderman Push-Ups", "45s Controlled", "Bring knee toward same-side elbow as chest lowers.", "Upper Body & Oblique Core", "Keep hips square to floor"),
          HiitExerciseDemo("Speed Squats to Jump Lunges", "45s Work", "Alternate 2 bodyweight squats into split switch jumps.", "Leg Power & Lactic Clearance", "Maintain upright athletic posture"),
          HiitExerciseDemo("Plank Jacks with Plank Walkouts", "45s Work", "Jack feet wide while stepping hands 6 inches in front.", "Core & Shoulder Endurance", "Brace glutes and abs tightly")
        )
      )
    )

    private fun createInitialFriends(): List<FriendAccount> = listOf(
      FriendAccount(
        id = "f_1",
        name = "Marcus Brody",
        avatarInitials = "MB",
        statusUpdate = "Crushed 5x5 Deadlifts at 365 lbs! 🔥",
        currentStreakDays = 14,
        weeklyScorePoints = 2480,
        lastWorkoutCompleted = "Heavy Pull Day (55 min)",
        fistBumpsCount = 28,
        isAccountabilityBuddy = true
      ),
      FriendAccount(
        id = "f_2",
        name = "Elena Rostova",
        avatarInitials = "ER",
        statusUpdate = "10km morning run + 15m Tabata done ⚡",
        currentStreakDays = 21,
        weeklyScorePoints = 3150,
        lastWorkoutCompleted = "15-Min Tabata Inferno",
        fistBumpsCount = 42,
        isAccountabilityBuddy = true
      ),
      FriendAccount(
        id = "f_3",
        name = "David Chen",
        avatarInitials = "DC",
        statusUpdate = "Meal prepped 6 Air Fryer chicken bowls 🥑",
        currentStreakDays = 9,
        weeklyScorePoints = 1890,
        lastWorkoutCompleted = "HIIT Engine (10 min)",
        fistBumpsCount = 15,
        isAccountabilityBuddy = false
      ),
      FriendAccount(
        id = "f_4",
        name = "Sarah Miller",
        avatarInitials = "SM",
        statusUpdate = "Recovery day: 94% Sleep score and mobility yoga",
        currentStreakDays = 12,
        weeklyScorePoints = 1750,
        lastWorkoutCompleted = "Mobility Flow (30 min)",
        fistBumpsCount = 19,
        isAccountabilityBuddy = false
      )
    )

    private fun createPresetFormChecks(): Map<String, MovementFormCheck> = mapOf(
      "Barbell Back Squat" to MovementFormCheck(
        movementName = "Barbell Back Squat",
        currentAngleDegrees = 92,
        targetAngleRange = "85° - 95° (Parallel or Below)",
        depthPercent = 94,
        spineNeutrality = "Optimal Neutral (Chest Elevated)",
        kneeTracking = "Properly tracking 2nd toe, zero valgus",
        tempoCadence = "3-1-X-1 (Control on negative)",
        scoreOutOf100 = 95,
        audioCueText = "Great depth! Hips cleared knees cleanly. Push floor away through mid-foot.",
        correctiveAction = "Keep your gaze 10 feet forward on the floor to prevent cervical hyperextension.",
        commonMistakes = listOf(
          "Knees caving inward (Knee Valgus) on ascent",
          "Heels lifting off the ground (Ankle mobility restriction)",
          "Chest collapsing forward (Weak thoracic extensors)",
          "Butt wink at deepest bottom range"
        )
      ),
      "Barbell Deadlift" to MovementFormCheck(
        movementName = "Barbell Deadlift",
        currentAngleDegrees = 110,
        targetAngleRange = "105° - 115° at Knee Crease",
        depthPercent = 88,
        spineNeutrality = "Neutral Lumbar & Packed Lats",
        kneeTracking = "Shins vertical at initiation",
        tempoCadence = "Explosive pull, controlled descent",
        scoreOutOf100 = 92,
        audioCueText = "Strong bar path! Lock out by squeezing glutes, avoid backward hyperextension.",
        correctiveAction = "Pull the slack out of the barbell before initiating floor drive.",
        commonMistakes = listOf(
          "Rounding lower back (Spinal flexion risk)",
          "Hips shooting up before chest rises (Stripper deadlift)",
          "Bar drifting away from shins during pull"
        )
      ),
      "Push-Up" to MovementFormCheck(
        movementName = "Push-Up",
        currentAngleDegrees = 88,
        targetAngleRange = "80° - 90° at Elbow Joint",
        depthPercent = 96,
        spineNeutrality = "Rigid Plank Alignment",
        kneeTracking = "Feet together, locked knees",
        tempoCadence = "2-0-1-0",
        scoreOutOf100 = 98,
        audioCueText = "Pristine alignment. Elbows at 45-degree arrow shape. Full chest contact.",
        correctiveAction = "Maintain active glute and core contraction to avoid lumbar sag.",
        commonMistakes = listOf(
          "Sagging lower back and forward head posture",
          "Flaring elbows out to 90 degrees (Shoulder impingement)"
        )
      ),
      "Walking Barbell Lunge" to MovementFormCheck(
        movementName = "Walking Barbell Lunge",
        currentAngleDegrees = 90,
        targetAngleRange = "90° Front & Back Knees",
        depthPercent = 91,
        spineNeutrality = "Torso Upright with Slight Forward Lean",
        kneeTracking = "Front knee centered over shoelaces",
        tempoCadence = "Cadence 2s step",
        scoreOutOf100 = 93,
        audioCueText = "Smooth stride length. Back knee hovered 1 inch above turf. Perfect deceleration.",
        correctiveAction = "Drive through front heel to activate glute medius on transition.",
        commonMistakes = listOf(
          "Overstriding causing excessive hip extension strain",
          "Front knee collapsing medial to big toe"
        )
      )
    )
  }
}
