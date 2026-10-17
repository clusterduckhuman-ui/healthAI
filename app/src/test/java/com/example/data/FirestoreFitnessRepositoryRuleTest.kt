package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.BiometricData
import com.example.model.FoodScanItem
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreFitnessRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveUserProfile_authenticatedUser_success(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = FirestoreFitnessRepository(firestore)

    val profile = UserProfile(
      name = "Alice Pro",
      fitnessGoal = "Lean Muscle",
      fitnessLevel = "Advanced",
      selectedEquipment = setOf("Air Fryer", "Oven"),
      activeAllergens = setOf("Nut-Free"),
      isSubscribed = true
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveUserProfile(profile)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun logBiometrics_authenticatedUser_writesAndReturnsId(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = FirestoreFitnessRepository(firestore)

    val biometrics = BiometricData(
      heartRateBpm = 145,
      recoveryScore = 88,
      strainScore = 15.2f,
      hrvMs = 78,
      activeCaloriesBurned = 710,
      stepsCount = 9400,
      sleepQualityScore = 90
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.logBiometrics(biometrics)
    }
    assertTrue(result.isSuccess)
    val bioId = result.getOrThrow()
    assertTrue(bioId.isNotEmpty())
  }

  @Test
  fun logFoodItem_authenticatedUser_savesCorrectly(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = FirestoreFitnessRepository(firestore)

    val food = FoodScanItem(
      id = "food_test123",
      name = "Grilled Salmon Bowl",
      mealType = "Dinner",
      calories = 520,
      proteinGrams = 42.0,
      carbsGrams = 35.0,
      fatGrams = 18.0,
      fiberGrams = 6.0,
      servingGrams = 300,
      healthGrade = "A+",
      allergenWarnings = listOf("Fish"),
      aiNutrientAnalysis = "Optimal post-workout profile"
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.logFoodItem(food)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun communityPostAndLike_authenticatedUser_success(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val aliceRepo = FirestoreFitnessRepository(firestore)

    val postResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createCommunityPost(
        content = "Completed 20 min HIIT circuit!",
        workoutType = "HIIT",
        metric = "310 kcal"
      )
    }
    assertTrue(postResult.isSuccess)
    val postId = postResult.getOrThrow()
    assertTrue(postId.isNotEmpty())

    // Bob likes Alice's post
    signInTestUser(BOB_EMAIL)
    val bobRepo = FirestoreFitnessRepository(firestore)
    val likeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.likeCommunityPost(postId)
    }
    assertTrue(likeResult.isSuccess)
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceDirectSubcollection_failsWithPermissionDenied(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = FirestoreFitnessRepository(firestore)
    val bioId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.logBiometrics(BiometricData(heartRateBpm = 130)).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid).collection("biometrics").document(bioId).get().await()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun unauthenticatedAccess_fails(): Unit = runBlocking {
    auth.signOut()
    val repository = FirestoreFitnessRepository(firestore)

    try {
      repository.requireUserId()
      fail("Expected IllegalStateException")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("signed in") == true)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_rule@test.com"
    const val BOB_EMAIL = "bob_rule@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
  }
}
