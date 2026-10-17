const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write anything", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("community_posts").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      goal: "Fat Loss",
      fitnessLevel: "Intermediate",
      allergies: [],
      cookingEquipment: [],
      isVip: false,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Smith",
      goal: "Lean Muscle",
      fitnessLevel: "Advanced",
      allergies: ["Peanuts"],
      cookingEquipment: ["Air Fryer"],
      isVip: true,
      createdAt: new Date(),
    })
  );

  const doc = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  if (!doc.exists) throw new Error("Document should exist");
});

test("Authenticated user: cannot read or write another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      goal: "Endurance",
      fitnessLevel: "Beginner",
      allergies: [],
      cookingEquipment: [],
      isVip: false,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot inject unknown ghost keys", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      goal: "Lean Muscle",
      fitnessLevel: "Intermediate",
      allergies: [],
      cookingEquipment: [],
      isVip: false,
      isAdmin: true, // Ghost key
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can log biometrics and workouts under their path", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("biometrics").doc("bio_1").set({
      userId: ALICE_UID,
      heartRateBpm: 142,
      recoveryScore: 88,
      strainScore: 14.5,
      hrvMs: 75,
      activeCaloriesBurned: 650,
      steps: 8500,
      sleepScore: 92,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("work_1").set({
      userId: ALICE_UID,
      title: "HIIT Tabata Burn",
      durationMinutes: 15,
      caloriesBurned: 220,
      rpeScore: 8,
      strainGenerated: 6.2,
      createdAt: new Date(),
    })
  );
});

test("Cross-user isolation: Bob cannot read Alice's biometrics or food logs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("food_logs").doc("food_1").set({
      userId: ALICE_UID,
      foodName: "Grilled Chicken Salad",
      calories: 450,
      proteinGrams: 42,
      carbsGrams: 15,
      fatGrams: 12,
      confidencePercent: 95,
      detectedAllergens: [],
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("food_logs").doc("food_1").get()
  );
});

test("Community posts: authenticated users can read and create, but cannot modify others' posts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("community_posts").doc("post_1").set({
      postId: "post_1",
      userId: ALICE_UID,
      userName: "Alice",
      content: "Crushed a 15 min HIIT session!",
      workoutType: "HIIT",
      metric: "220 kcal",
      likesCount: 0,
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const post = await assertSucceeds(bobDb.collection("community_posts").doc("post_1").get());
  if (!post.exists) throw new Error("Bob should read post");

  // Bob cannot edit Alice's post content
  await assertFails(
    bobDb.collection("community_posts").doc("post_1").update({
      content: "Hacked by Bob",
    })
  );

  // Bob can like the post
  await assertSucceeds(
    bobDb.collection("community_posts").doc("post_1").collection("likes").doc(BOB_UID).set({
      userId: BOB_UID,
      postId: "post_1",
      createdAt: new Date(),
    })
  );
});
