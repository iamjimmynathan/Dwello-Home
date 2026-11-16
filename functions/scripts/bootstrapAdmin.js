"use strict";

/**
 * One-time CLI script to provision the initial private Dwello Homes Admin account.
 * Run securely from an authorized operator workstation using Application Default Credentials:
 *
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/admin-key.json
 *   export DWELLO_BOOTSTRAP_ADMIN_PHONE=+2348000000001
 *   export DWELLO_BOOTSTRAP_ADMIN_PASSWORD='StrongAdminPassword!'
 *   export DWELLO_BOOTSTRAP_ADMIN_NAME='Dwello Platform Admin'
 *   node scripts/bootstrapAdmin.js
 *
 * Never paste service-account JSON keys into repository source files or the Android APK.
 */

const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");
const { normalizeNigerianPhoneToE164 } = require("../src/phone");
const { hashPassword } = require("../src/password");

async function main() {
  const rawPhone = process.env.DWELLO_BOOTSTRAP_ADMIN_PHONE || "";
  const password = process.env.DWELLO_BOOTSTRAP_ADMIN_PASSWORD || "";
  const fullName = (process.env.DWELLO_BOOTSTRAP_ADMIN_NAME || "Dwello Platform Admin").trim();
  const pepper = process.env.DWELLO_AUTH_PEPPER || "";
  const databaseId =
    process.env.DWELLO_FIRESTORE_DATABASE_ID ||
    "ai-studio-android-dwelloho-b885f2f3-86cf-4fd4-b080-a65e5fa25ef9";

  const phoneE164 = normalizeNigerianPhoneToE164(rawPhone);
  if (!phoneE164) {
    throw new Error("DWELLO_BOOTSTRAP_ADMIN_PHONE must be a valid Nigerian phone number.");
  }
  if (password.length < 12) {
    throw new Error("DWELLO_BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters.");
  }

  initializeApp();
  const auth = getAuth();
  const db = getFirestore(databaseId);

  let userRecord;
  try {
    userRecord = await auth.getUserByPhoneNumber(phoneE164);
  } catch (err) {
    userRecord = await auth.createUser({
      phoneNumber: phoneE164,
      displayName: fullName,
    });
  }

  await auth.setCustomUserClaims(userRecord.uid, { role: "admin" });
  const passwordHash = await hashPassword(password, pepper);
  const now = new Date();

  const batch = db.batch();
  batch.set(
    db.collection("users").doc(userRecord.uid),
    {
      uid: userRecord.uid,
      fullName,
      phoneE164,
      role: "admin",
      isActive: true,
      phoneVerified: true,
      createdAt: now,
      updatedAt: now,
    },
    { merge: true }
  );
  batch.set(
    db.collection("auth_credentials").doc(userRecord.uid),
    {
      uid: userRecord.uid,
      phoneE164,
      passwordHash,
      role: "admin",
      failedAttempts: 0,
      createdAt: now,
      updatedAt: now,
    },
    { merge: true }
  );
  await batch.commit();

  console.log(`Admin account ready for UID=${userRecord.uid} (${phoneE164}).`);
}

main().catch((err) => {
  console.error("Admin bootstrap failed:", err.message);
  process.exit(1);
});
