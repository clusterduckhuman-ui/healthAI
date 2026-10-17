package com.example.data

import android.content.Context
import com.example.R
import com.example.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFitnessRepository(private val db: FirebaseFirestore) {

  // Primary constructor with injected db; secondary constructor resolves named database id
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // --- USER PROFILE ---
  suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      val snapshot = docRef.get().await()
      val exists = snapshot.exists()
      val payload = mutableMapOf<String, Any>(
        "userId" to uid,
        "displayName" to profile.name,
        "goal" to profile.fitnessGoal,
        "fitnessLevel" to profile.fitnessLevel,
        "allergies" to profile.activeAllergens.toList(),
        "cookingEquipment" to profile.selectedEquipment.toList(),
        "isVip" to profile.isSubscribed
      )
      auth.currentUser?.email?.let { payload["email"] = it }

      if (!exists) {
        payload["createdAt"] = FieldValue.serverTimestamp()
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.set(payload).await()
      } else {
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.update(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val path = "users/$userId"
    db.collection("users").document(userId)
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          val data = snapshot.data ?: return@map null
          UserProfile(
            name = data["displayName"] as? String ?: "Athlete",
            fitnessGoal = data["goal"] as? String ?: "Lean Muscle & Power",
            fitnessLevel = data["fitnessLevel"] as? String ?: "Intermediate",
            activeAllergens = ((data["allergies"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()).toSet(),
            selectedEquipment = ((data["cookingEquipment"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()).toSet(),
            isSubscribed = data["isVip"] as? Boolean ?: false,
            isTrialActive = true,
            trialDaysLeft = 7
          )
        } else {
          null
        }
      }
      .catch { e ->
        if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
        throw e
      }
      .collect { emit(it) }
  }

  // --- BIOMETRICS ---
  suspend fun logBiometrics(data: BiometricData): Result<String> {
    val uid = requireUserId()
    val bioId = "bio_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val docRef = db.collection("users").document(uid).collection("biometrics").document(bioId)
    val payload = mapOf(
      "userId" to uid,
      "heartRateBpm" to data.heartRateBpm,
      "recoveryScore" to data.recoveryScore,
      "strainScore" to data.strainScore.toDouble(),
      "hrvMs" to data.hrvMs,
      "activeCaloriesBurned" to data.activeCaloriesBurned,
      "steps" to data.stepsCount,
      "sleepScore" to data.sleepQualityScore,
      "createdAt" to FieldValue.serverTimestamp()
    )
    return try {
      docRef.set(payload).await()
      Result.success(bioId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeBiometrics(userId: String): Flow<List<BiometricData>> = flow {
    val path = "users/$userId/biometrics"
    db.collection("users").document(userId).collection("biometrics")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(20)
      .snapshots()
      .map { querySnapshot ->
        querySnapshot.documents.map { doc ->
          BiometricData(
            heartRateBpm = (doc.getLong("heartRateBpm") ?: 138L).toInt(),
            recoveryScore = (doc.getLong("recoveryScore") ?: 85L).toInt(),
            strainScore = (doc.getDouble("strainScore") ?: 14.2).toFloat(),
            hrvMs = (doc.getLong("hrvMs") ?: 72L).toInt(),
            activeCaloriesBurned = (doc.getLong("activeCaloriesBurned") ?: 620L).toInt(),
            stepsCount = (doc.getLong("steps") ?: 8740L).toInt(),
            sleepQualityScore = (doc.getLong("sleepScore") ?: 89L).toInt(),
            connectedDeviceName = "Garmin Forerunner 965",
            isLiveSyncing = true,
            batteryPercentage = 94,
            lastSyncTime = "Synced"
          )
        }
      }
      .catch { e ->
        if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
        throw e
      }
      .collect { emit(it) }
  }

  // --- FOOD LOGS ---
  suspend fun logFoodItem(item: FoodScanItem): Result<String> {
    val uid = requireUserId()
    val logId = if (item.id.isNotBlank() && item.id.matches(Regex("^[a-zA-Z0-9_-]+$"))) item.id
                else "food_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val docRef = db.collection("users").document(uid).collection("food_logs").document(logId)
    val payload = mapOf(
      "userId" to uid,
      "foodName" to item.name,
      "calories" to item.calories,
      "proteinGrams" to item.proteinGrams.toInt(),
      "carbsGrams" to item.carbsGrams.toInt(),
      "fatGrams" to item.fatGrams.toInt(),
      "confidencePercent" to 95,
      "detectedAllergens" to item.allergenWarnings,
      "createdAt" to FieldValue.serverTimestamp()
    )
    return try {
      docRef.set(payload).await()
      Result.success(logId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeFoodLogs(userId: String): Flow<List<FoodScanItem>> = flow {
    val path = "users/$userId/food_logs"
    db.collection("users").document(userId).collection("food_logs")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(30)
      .snapshots()
      .map { querySnapshot ->
        querySnapshot.documents.map { doc ->
          val calories = (doc.getLong("calories") ?: 0L).toInt()
          val protein = (doc.getLong("proteinGrams") ?: 0L).toDouble()
          val carbs = (doc.getLong("carbsGrams") ?: 0L).toDouble()
          val fat = (doc.getLong("fatGrams") ?: 0L).toDouble()
          val allergens = (doc.get("detectedAllergens") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
          FoodScanItem(
            id = doc.id,
            name = doc.getString("foodName") ?: "Scanned Meal",
            mealType = "Logged Meal",
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            fiberGrams = 4.0,
            servingGrams = 250,
            healthGrade = "A",
            allergenWarnings = allergens,
            aiNutrientAnalysis = "High quality macronutrients logged to cloud.",
            timeLogged = "Cloud Sync"
          )
        }
      }
      .catch { e ->
        if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
        throw e
      }
      .collect { emit(it) }
  }

  // --- WORKOUT LOGS ---
  suspend fun logCompletedWorkout(workout: WorkoutPlan): Result<String> {
    val uid = requireUserId()
    val workoutId = if (workout.id.isNotBlank() && workout.id.matches(Regex("^[a-zA-Z0-9_-]+$"))) workout.id
                    else "work_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val docRef = db.collection("users").document(uid).collection("workouts").document(workoutId)
    val payload = mapOf(
      "userId" to uid,
      "title" to workout.title,
      "durationMinutes" to workout.durationMinutes,
      "caloriesBurned" to (workout.durationMinutes * 9),
      "rpeScore" to (workout.userRpeExertion ?: 7),
      "strainGenerated" to workout.targetStrain.toDouble(),
      "createdAt" to FieldValue.serverTimestamp()
    )
    return try {
      docRef.set(payload).await()
      Result.success(workoutId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  // --- COMMUNITY POSTS ---
  suspend fun createCommunityPost(content: String, workoutType: String, metric: String): Result<String> {
    val uid = requireUserId()
    val postId = "post_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val docRef = db.collection("community_posts").document(postId)
    val userName = auth.currentUser?.displayName ?: "Apex Athlete"
    val payload = mapOf(
      "postId" to postId,
      "userId" to uid,
      "userName" to userName,
      "content" to content,
      "workoutType" to workoutType,
      "metric" to metric,
      "likesCount" to 0,
      "createdAt" to FieldValue.serverTimestamp()
    )
    return try {
      docRef.set(payload).await()
      Result.success(postId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun likeCommunityPost(postId: String): Result<Unit> {
    val uid = requireUserId()
    val postRef = db.collection("community_posts").document(postId)
    val likeRef = postRef.collection("likes").document(uid)
    return try {
      likeRef.set(
        mapOf(
          "userId" to uid,
          "postId" to postId,
          "createdAt" to FieldValue.serverTimestamp()
        )
      ).await()
      postRef.update("likesCount", FieldValue.increment(1)).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, likeRef.path)
      Result.failure(e)
    }
  }

  fun observeCommunityPosts(): Flow<List<CommunityPostData>> = flow {
    val path = "community_posts"
    db.collection(path)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(20)
      .snapshots()
      .map { querySnapshot ->
        querySnapshot.documents.map { doc ->
          CommunityPostData(
            postId = doc.id,
            userId = doc.getString("userId") ?: "",
            userName = doc.getString("userName") ?: "Athlete",
            content = doc.getString("content") ?: "",
            workoutType = doc.getString("workoutType") ?: "Workout",
            metric = doc.getString("metric") ?: "",
            likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
            createdAt = doc.getTimestamp("createdAt")
          )
        }
      }
      .catch { e ->
        if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
        throw e
      }
      .collect { emit(it) }
  }
}

data class CommunityPostData(
  val postId: String = "",
  val userId: String = "",
  val userName: String = "",
  val content: String = "",
  val workoutType: String = "",
  val metric: String = "",
  val likesCount: Int = 0,
  val createdAt: Timestamp? = null
)
