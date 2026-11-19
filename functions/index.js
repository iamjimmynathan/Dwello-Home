"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp, getApps } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");
const { getStorage } = require("firebase-admin/storage");
const { createDwelloApp } = require("./src/app");

if (getApps().length === 0) {
  initializeApp();
}

const DATABASE_ID =
  process.env.DWELLO_FIRESTORE_DATABASE_ID ||
  "ai-studio-android-dwelloho-b885f2f3-86cf-4fd4-b080-a65e5fa25ef9";

const db = getFirestore(DATABASE_ID);
const auth = getAuth();
const storage = getStorage();

const app = createDwelloApp({
  db,
  auth,
  storage,
  storageBucketName: process.env.DWELLO_STORAGE_BUCKET || "dwello-home.firebasestorage.app",
  defaultCity: process.env.DWELLO_DEFAULT_CITY || "Benin City",
  pepper: process.env.DWELLO_AUTH_PEPPER || "",
  allowedOrigins: process.env.DWELLO_ALLOWED_ORIGINS || "",
});

exports.api = onRequest(
  {
    region: "europe-west3",
    memory: "256MiB",
    maxInstances: 10,
  },
  app
);
