package com.example.model

data class BiometricData(
  val heartRateBpm: Int = 138,
  val recoveryScore: Int = 85, // 0 - 100%
  val strainScore: Float = 14.2f, // 0.0 - 21.0
  val hrvMs: Int = 72,
  val activeCaloriesBurned: Int = 620,
  val stepsCount: Int = 8740,
  val sleepQualityScore: Int = 89,
  val vo2Max: Double = 52.4,
  val connectedDeviceName: String = "Garmin Forerunner 965",
  val isLiveSyncing: Boolean = true,
  val batteryPercentage: Int = 94,
  val lastSyncTime: String = "Just now"
)

data class Exercise(
  val id: String,
  val name: String,
  val targetMuscle: String,
  val sets: Int,
  val reps: String,
  val weightKg: Double,
  val coachCue: String,
  val completedSets: Int = 0,
  val isFormGuided: Boolean = true
)

data class WorkoutPlan(
  val id: String,
  val title: String,
  val focusArea: String,
  val durationMinutes: Int,
  val intensityLevel: String, // "High Strain", "Optimal Hypertrophy", "Active Recovery"
  val targetStrain: Float,
  val aiBiometricRationale: String,
  val exercises: List<Exercise>,
  val isCompleted: Boolean = false,
  val userRpeExertion: Int? = null // 1 - 10
)

data class MovementFormCheck(
  val movementName: String,
  val currentAngleDegrees: Int,
  val targetAngleRange: String,
  val depthPercent: Int,
  val spineNeutrality: String,
  val kneeTracking: String,
  val tempoCadence: String,
  val scoreOutOf100: Int,
  val audioCueText: String,
  val correctiveAction: String,
  val commonMistakes: List<String>
)

data class FoodScanItem(
  val id: String,
  val name: String,
  val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
  val calories: Int,
  val proteinGrams: Double,
  val carbsGrams: Double,
  val fatGrams: Double,
  val fiberGrams: Double,
  val servingGrams: Int,
  val healthGrade: String, // "A+", "A", "B"
  val allergenWarnings: List<String>,
  val aiNutrientAnalysis: String,
  val timeLogged: String = "Today"
)

data class MealPrepRecipe(
  val id: String,
  val title: String,
  val targetFitnessGoal: String, // "Lean Muscle Bulk", "Fat Loss / Cutting", "Keto Athletic", "Clean Maintenance"
  val prepMinutes: Int,
  val batchPortions: Int,
  val caloriesPerPortion: Int,
  val proteinGrams: Double,
  val carbsGrams: Double,
  val fatGrams: Double,
  val requiredEquipment: List<String>, // "Air Fryer", "Blender", "Instant Pot", "Oven", "Stovetop"
  val freeFromAllergens: List<String>, // "Gluten-Free", "Dairy-Free", "Nut-Free", "Shellfish-Free"
  val ingredients: List<String>,
  val instructions: List<String>,
  val storageAdvice: String
)

data class HiitExerciseDemo(
  val name: String,
  val repsOrTime: String,
  val keyFormTip: String,
  val muscleGroup: String,
  val videoPlaceholderText: String
)

data class HiitSession(
  val id: String,
  val title: String,
  val durationMinutes: Int,
  val workSeconds: Int,
  val restSeconds: Int,
  val rounds: Int,
  val estimatedBurnKcal: Int,
  val difficulty: String,
  val exercises: List<HiitExerciseDemo>
)

data class FriendAccount(
  val id: String,
  val name: String,
  val avatarInitials: String,
  val statusUpdate: String,
  val currentStreakDays: Int,
  val weeklyScorePoints: Int,
  val lastWorkoutCompleted: String,
  val fistBumpsCount: Int,
  val isAccountabilityBuddy: Boolean
)

data class SubscriptionPlan(
  val planId: String,
  val name: String,
  val priceDisplay: String,
  val billedPeriod: String,
  val trialDays: Int = 7,
  val isBestValue: Boolean = false,
  val perks: List<String>
)

data class UserProfile(
  val name: String = "Alex Stone",
  val age: Int = 28,
  val weightKg: Double = 76.5,
  val heightCm: Int = 180,
  val fitnessGoal: String = "Lean Muscle & Conditioning",
  val fitnessLevel: String = "Intermediate",
  val dailyCalorieTarget: Int = 2450,
  val dailyProteinTargetGrams: Int = 175,
  val selectedEquipment: Set<String> = setOf("Air Fryer", "Blender", "Oven", "Stovetop", "Instant Pot"),
  val activeAllergens: Set<String> = setOf("Nut-Free", "Shellfish-Free"),
  val isSubscribed: Boolean = false,
  val isTrialActive: Boolean = true,
  val trialDaysLeft: Int = 7
)
