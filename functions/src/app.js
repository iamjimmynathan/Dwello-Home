"use strict";

const crypto = require("node:crypto");
const express = require("express");
const cors = require("cors");
const { normalizeNigerianPhoneToE164 } = require("./phone");
const { hashPassword, verifyPassword } = require("./password");
const { createAuthMiddleware } = require("./authMiddleware");
const {
  PUBLIC_ROLES,
  ALL_ROLES,
  PROPERTY_TYPES,
  RENT_PERIODS,
  ENQUIRY_STATUSES,
  ALLOWED_IMAGE_CONTENT_TYPES,
  EMAIL_REGEX,
  cleanString,
  sanitizeUserDto,
  sanitizePropertyDto,
  sanitizePropertyImageDto,
  sanitizeEnquiryDto,
} = require("./validation");

function defaultNowFn() {
  return new Date();
}

function createDwelloApp({
  db,
  auth,
  storage = null,
  storageBucketName = process.env.DWELLO_STORAGE_BUCKET || "",
  defaultCity = process.env.DWELLO_DEFAULT_CITY || "Benin City",
  pepper = process.env.DWELLO_AUTH_PEPPER || "",
  allowedOrigins = process.env.DWELLO_ALLOWED_ORIGINS || "",
  nowFn = defaultNowFn,
} = {}) {
  if (!db || !auth) {
    throw new Error("createDwelloApp requires both db and auth dependencies.");
  }

  const app = express();
  app.disable("x-powered-by");

  const originAllowlist = new Set(
    String(allowedOrigins)
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean)
  );

  app.use(
    cors({
      origin(origin, callback) {
        // Mobile clients and curl have no Origin header; allowlisted Admin Web origins are permitted.
        if (!origin || originAllowlist.size === 0 || originAllowlist.has(origin)) {
          return callback(null, true);
        }
        return callback(new Error("Origin not allowed by CORS"));
      },
    })
  );
  app.use(express.json({ limit: "1mb" }));

  const { requireAuth, requireRoles } = createAuthMiddleware({ auth, db });
  const router = express.Router();

  async function writeAdminLog({ adminUid, action, targetType, targetId, reason = null, metadata = null }) {
    const logRef = db.collection("admin_logs").doc();
    await logRef.set({
      adminUid,
      action,
      targetType,
      targetId,
      ...(reason ? { reason } : {}),
      ...(metadata ? { metadata } : {}),
      createdAt: nowFn(),
    });
  }

  async function attachImagesToProperties(propertiesList) {
    if (!Array.isArray(propertiesList) || propertiesList.length === 0) return [];
    const enriched = [];
    for (const item of propertiesList) {
      const imgSnap = await db.collection("property_images").where("propertyId", "==", item.id).get();
      const images = imgSnap.docs
        .map((doc) => sanitizePropertyImageDto(doc.id, doc.data()))
        .sort((a, b) => a.sort_order - b.sort_order);
      enriched.push({ ...item, images });
    }
    return enriched;
  }

  // --- HEALTH ---
  router.get("/health", (req, res) => {
    res.json({
      success: true,
      service: "dwello-homes-shared-api",
      project: "dwello-home",
      timestamp: nowFn().toISOString(),
    });
  });

  // --- AUTHENTICATION (Nigerian Phone + Password -> Firebase Custom Token) ---
  router.post("/auth/register", async (req, res) => {
    try {
      const fullName = cleanString(req.body?.full_name ?? req.body?.fullName, 120);
      const rawPhone = cleanString(req.body?.phone, 40);
      const rawEmail = req.body?.email ? cleanString(req.body.email, 160) : null;
      const password = typeof req.body?.password === "string" ? req.body.password : "";
      const requestedRole = cleanString(req.body?.role, 32).toLowerCase();

      if (fullName.length < 2) {
        return res.status(400).json({ success: false, message: "Full name must be at least 2 characters." });
      }

      const phoneE164 = normalizeNigerianPhoneToE164(rawPhone);
      if (!phoneE164) {
        return res.status(400).json({
          success: false,
          message: "Please enter a valid Nigerian mobile number (e.g. 0803 123 4567).",
        });
      }

      if (rawEmail && !EMAIL_REGEX.test(rawEmail)) {
        return res.status(400).json({ success: false, message: "Please enter a valid email address." });
      }

      if (password.length < 8) {
        return res.status(400).json({
          success: false,
          message: "Password must be at least 8 characters long.",
        });
      }

      if (!PUBLIC_ROLES.has(requestedRole)) {
        return res.status(403).json({
          success: false,
          message: "Public registration is restricted strictly to Tenant or Landlord accounts.",
        });
      }

      const existingCreds = await db
        .collection("auth_credentials")
        .where("phoneE164", "==", phoneE164)
        .limit(1)
        .get();

      if (!existingCreds.empty) {
        return res.status(409).json({
          success: false,
          message: "An account with this phone number already exists. Please log in.",
        });
      }

      const uid = `dw_${crypto.randomUUID().replace(/-/g, "").slice(0, 24)}`;
      const authRecord = await auth.createUser({
        uid,
        phoneNumber: phoneE164,
        displayName: fullName,
        ...(rawEmail ? { email: rawEmail } : {}),
      });

      const createdUid = authRecord.uid;
      try {
        await auth.setCustomUserClaims(createdUid, { role: requestedRole });
        const passwordHash = await hashPassword(password, pepper);
        const now = nowFn();

        const userDoc = {
          uid: createdUid,
          fullName,
          phoneE164,
          ...(rawEmail ? { email: rawEmail } : {}),
          role: requestedRole,
          isActive: true,
          phoneVerified: false,
          createdAt: now,
          updatedAt: now,
        };

        const credDoc = {
          uid: createdUid,
          phoneE164,
          passwordHash,
          role: requestedRole,
          failedAttempts: 0,
          createdAt: now,
          updatedAt: now,
        };

        const batch = db.batch();
        batch.set(db.collection("users").doc(createdUid), userDoc);
        batch.set(db.collection("auth_credentials").doc(createdUid), credDoc);
        await batch.commit();

        const customToken = await auth.createCustomToken(createdUid, { role: requestedRole });
        return res.status(201).json({
          success: true,
          message: "Account created successfully.",
          custom_token: customToken,
          user: sanitizeUserDto(userDoc),
        });
      } catch (innerErr) {
        await auth.deleteUser(createdUid).catch(() => {});
        throw innerErr;
      }
    } catch (err) {
      if (err && String(err.code || "").includes("already-exists")) {
        return res.status(409).json({
          success: false,
          message: "An account with this phone number or email already exists.",
        });
      }
      return res.status(500).json({ success: false, message: "Unable to complete registration." });
    }
  });

  router.post("/auth/login", async (req, res) => {
    try {
      const rawPhone = cleanString(req.body?.phone, 40);
      const password = typeof req.body?.password === "string" ? req.body.password : "";

      const phoneE164 = normalizeNigerianPhoneToE164(rawPhone);
      if (!phoneE164 || password.length < 8) {
        return res.status(400).json({
          success: false,
          message: "Valid Nigerian phone number and password are required.",
        });
      }

      const credSnap = await db
        .collection("auth_credentials")
        .where("phoneE164", "==", phoneE164)
        .limit(1)
        .get();

      if (credSnap.empty) {
        return res.status(401).json({
          success: false,
          message: "Invalid phone number or password.",
        });
      }

      const credDoc = credSnap.docs[0];
      const credData = credDoc.data();
      const valid = await verifyPassword(password, credData.passwordHash, pepper);

      if (!valid) {
        await credDoc.ref.update({
          failedAttempts: Number(credData.failedAttempts || 0) + 1,
          updatedAt: nowFn(),
        });
        return res.status(401).json({
          success: false,
          message: "Invalid phone number or password.",
        });
      }

      const userSnap = await db.collection("users").doc(credData.uid).get();
      if (!userSnap.exists) {
        return res.status(403).json({
          success: false,
          message: "Account profile is unavailable.",
        });
      }

      const userData = userSnap.data();
      if (userData.isActive === false) {
        return res.status(403).json({
          success: false,
          message: "This account has been suspended. Contact Dwello Homes support.",
        });
      }

      if (Number(credData.failedAttempts || 0) > 0) {
        await credDoc.ref.update({ failedAttempts: 0, updatedAt: nowFn() });
      }

      const customToken = await auth.createCustomToken(userData.uid, { role: userData.role });
      return res.json({
        success: true,
        message: "Login successful.",
        custom_token: customToken,
        user: sanitizeUserDto(userData),
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Unable to sign in right now." });
    }
  });

  router.get("/auth/me", requireAuth, (req, res) => {
    return res.json({
      success: true,
      user: sanitizeUserDto(req.user.raw),
    });
  });

  router.post("/auth/logout", requireAuth, async (req, res) => {
    try {
      await auth.revokeRefreshTokens(req.user.uid);
      return res.json({
        success: true,
        message: "Logged out and refresh tokens revoked.",
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to log out cleanly." });
    }
  });

  // --- USER PROFILES ---
  router.get("/users/me", requireAuth, (req, res) => {
    return res.json({
      success: true,
      user: sanitizeUserDto(req.user.raw),
    });
  });

  router.patch("/users/me", requireAuth, async (req, res) => {
    try {
      const updates = {};
      if (req.body?.full_name !== undefined || req.body?.fullName !== undefined) {
        const nextName = cleanString(req.body.full_name ?? req.body.fullName, 120);
        if (nextName.length < 2) {
          return res.status(400).json({ success: false, message: "Full name must be at least 2 characters." });
        }
        updates.fullName = nextName;
      }
      if (req.body?.email !== undefined) {
        const nextEmail = req.body.email === null ? null : cleanString(req.body.email, 160);
        if (nextEmail && !EMAIL_REGEX.test(nextEmail)) {
          return res.status(400).json({ success: false, message: "Please provide a valid email address." });
        }
        updates.email = nextEmail || null;
      }

      if (Object.keys(updates).length === 0) {
        return res.status(400).json({ success: false, message: "No valid profile fields provided." });
      }

      updates.updatedAt = nowFn();
      const userRef = db.collection("users").doc(req.user.uid);
      await userRef.update(updates);
      if (updates.fullName) {
        await auth.updateUser(req.user.uid, { displayName: updates.fullName }).catch(() => {});
      }
      const freshSnap = await userRef.get();
      return res.json({
        success: true,
        message: "Profile updated.",
        user: sanitizeUserDto(freshSnap.data()),
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update profile." });
    }
  });

  // --- TENANT & PUBLIC PROPERTY BROWSING (Approved + Available ONLY) ---
  router.get("/properties", requireAuth, async (req, res) => {
    try {
      const snapshot = await db
        .collection("properties")
        .where("moderationStatus", "==", "approved")
        .where("availabilityStatus", "==", "available")
        .get();

      const neighbourhoodFilter = cleanString(req.query.neighbourhood || "", 100).toLowerCase();
      const typeFilter = cleanString(req.query.property_type || req.query.propertyType || "", 60).toLowerCase();
      const queryFilter = cleanString(req.query.q || "", 120).toLowerCase();
      const minRent = req.query.min_rent ? Number(req.query.min_rent) : null;
      const maxRent = req.query.max_rent ? Number(req.query.max_rent) : null;

      const properties = await attachImagesToProperties(
        snapshot.docs
          .map((doc) => sanitizePropertyDto(doc.id, doc.data()))
          .filter((item) => {
            if (neighbourhoodFilter && item.neighbourhood.toLowerCase() !== neighbourhoodFilter) return false;
            if (typeFilter && item.property_type.toLowerCase() !== typeFilter) return false;
            if (minRent !== null && Number.isFinite(minRent) && item.rent_amount < minRent) return false;
            if (maxRent !== null && Number.isFinite(maxRent) && item.rent_amount > maxRent) return false;
            if (queryFilter) {
              const haystack = `${item.title} ${item.neighbourhood} ${item.description}`.toLowerCase();
              if (!haystack.includes(queryFilter)) return false;
            }
            return true;
          })
      );

      return res.json({ success: true, properties });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Unable to load properties." });
    }
  });

  router.get("/properties/:id", requireAuth, async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const snap = await db.collection("properties").doc(propertyId).get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      const data = snap.data();
      const isPubliclyVisible = data.moderationStatus === "approved" && data.availabilityStatus === "available";
      const isOwner = data.landlordId === req.user.uid;
      const isAdmin = req.user.role === "admin";

      if (!isPubliclyVisible && !isOwner && !isAdmin) {
        return res.status(403).json({
          success: false,
          message: "This property is not publicly available.",
        });
      }

      const imgSnap = await db.collection("property_images").where("propertyId", "==", propertyId).get();
      const images = imgSnap.docs
        .map((doc) => sanitizePropertyImageDto(doc.id, doc.data()))
        .sort((a, b) => a.sort_order - b.sort_order);

      return res.json({
        success: true,
        property: sanitizePropertyDto(snap.id, data),
        images,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Unable to load property details." });
    }
  });

  // --- LANDLORD PROPERTY CREATION & MANAGEMENT ---
  router.post("/properties", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const title = cleanString(req.body?.title, 140);
      const propertyType = cleanString(req.body?.property_type ?? req.body?.propertyType, 60).toLowerCase();
      const bedrooms = Number(req.body?.bedrooms || 0);
      const bathrooms = Number(req.body?.bathrooms || 0);
      const rentAmount = Number(req.body?.rent_amount ?? req.body?.rentAmount ?? 0);
      const rentPeriod = cleanString(req.body?.rent_period ?? req.body?.rentPeriod ?? "yearly", 20).toLowerCase();
      const description = cleanString(req.body?.description, 4000);
      const neighbourhood = cleanString(req.body?.neighbourhood, 100);
      const fullAddress = cleanString(req.body?.full_address ?? req.body?.fullAddress, 300);
      const landmark = req.body?.landmark ? cleanString(req.body.landmark, 200) : null;
      const facilities = Array.isArray(req.body?.facilities)
        ? req.body.facilities.map((f) => cleanString(String(f), 60)).filter(Boolean).slice(0, 12)
        : [];

      if (title.length < 3) {
        return res.status(400).json({ success: false, message: "Property title must be at least 3 characters." });
      }
      if (!PROPERTY_TYPES.has(propertyType)) {
        return res.status(400).json({ success: false, message: "Invalid property type." });
      }
      if (!Number.isInteger(bedrooms) || bedrooms < 1 || bedrooms > 20) {
        return res.status(400).json({ success: false, message: "Bedrooms must be between 1 and 20." });
      }
      if (!Number.isInteger(bathrooms) || bathrooms < 1 || bathrooms > 20) {
        return res.status(400).json({ success: false, message: "Bathrooms must be between 1 and 20." });
      }
      if (!Number.isFinite(rentAmount) || rentAmount <= 0) {
        return res.status(400).json({ success: false, message: "Rent amount in Naira must be greater than zero." });
      }
      if (!RENT_PERIODS.has(rentPeriod)) {
        return res.status(400).json({ success: false, message: "Rent period must be monthly or yearly." });
      }
      if (description.length < 10) {
        return res.status(400).json({ success: false, message: "Description must be at least 10 characters." });
      }
      if (neighbourhood.length < 2 || fullAddress.length < 3) {
        return res.status(400).json({ success: false, message: "Neighbourhood and address are required." });
      }

      const now = nowFn();
      const docRef = db.collection("properties").doc();
      const payload = {
        landlordId: req.user.uid,
        landlordName: req.user.fullName,
        title,
        propertyType,
        bedrooms,
        bathrooms,
        rentAmount: Math.round(rentAmount),
        rentPeriod,
        description,
        city: defaultCity,
        neighbourhood,
        fullAddress,
        ...(landmark ? { landmark } : {}),
        facilities,
        // Always enforce Pending Approval on creation
        moderationStatus: "pending_approval",
        availabilityStatus: "available",
        createdAt: now,
        updatedAt: now,
      };

      await docRef.set(payload);
      return res.status(201).json({
        success: true,
        message: "Property submitted and awaiting admin approval.",
        property_id: docRef.id,
        moderation_status: "pending_approval",
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to submit property." });
    }
  });

  router.get("/landlord/properties", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const snap = await db
        .collection("properties")
        .where("landlordId", "==", req.user.uid)
        .get();
      const properties = await attachImagesToProperties(
        snap.docs.map((doc) => sanitizePropertyDto(doc.id, doc.data()))
      );
      return res.json({ success: true, properties });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Unable to load your properties." });
    }
  });

  router.patch("/properties/:id", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const propRef = db.collection("properties").doc(propertyId);
      const snap = await propRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      const existing = snap.data();
      if (existing.landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "You can only edit your own listings." });
      }

      const updates = {};
      if (req.body?.title !== undefined) {
        const title = cleanString(req.body.title, 140);
        if (title.length < 3) return res.status(400).json({ success: false, message: "Title too short." });
        updates.title = title;
      }
      if (req.body?.rent_amount !== undefined || req.body?.rentAmount !== undefined) {
        const rentAmount = Number(req.body.rent_amount ?? req.body.rentAmount);
        if (!Number.isFinite(rentAmount) || rentAmount <= 0) {
          return res.status(400).json({ success: false, message: "Invalid rent amount." });
        }
        updates.rentAmount = Math.round(rentAmount);
      }
      if (req.body?.neighbourhood !== undefined) {
        const n = cleanString(req.body.neighbourhood, 100);
        if (n.length < 2) return res.status(400).json({ success: false, message: "Invalid neighbourhood." });
        updates.neighbourhood = n;
      }
      if (req.body?.description !== undefined) {
        const d = cleanString(req.body.description, 4000);
        if (d.length < 10) return res.status(400).json({ success: false, message: "Description too short." });
        updates.description = d;
      }
      if (req.body?.full_address !== undefined || req.body?.fullAddress !== undefined) {
        const a = cleanString(req.body.full_address ?? req.body.fullAddress, 300);
        if (a.length < 3) return res.status(400).json({ success: false, message: "Address too short." });
        updates.fullAddress = a;
      }
      if (req.body?.landmark !== undefined) {
        updates.landmark = req.body.landmark ? cleanString(req.body.landmark, 200) : null;
      }

      if (Object.keys(updates).length === 0) {
        return res.status(400).json({ success: false, message: "No valid update fields supplied." });
      }

      // Edits return listing to Pending Approval
      updates.moderationStatus = "pending_approval";
      updates.rejectionReason = null;
      updates.updatedAt = nowFn();

      await propRef.update(updates);

      const imgSnap = await db.collection("property_images").where("propertyId", "==", propertyId).get();
      if (!imgSnap.empty) {
        const batch = db.batch();
        imgSnap.docs.forEach((doc) => batch.update(doc.ref, { moderationStatus: "pending_approval" }));
        await batch.commit();
      }

      return res.json({
        success: true,
        message: "Property updated and returned for admin approval.",
        moderation_status: "pending_approval",
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update property." });
    }
  });

  router.patch("/properties/:id/availability", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const nextStatus = cleanString(req.body?.availability_status ?? req.body?.availabilityStatus, 24).toLowerCase();
      if (nextStatus !== "available" && nextStatus !== "unavailable") {
        return res.status(400).json({ success: false, message: "Availability status must be available or unavailable." });
      }

      const propRef = db.collection("properties").doc(propertyId);
      const snap = await propRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      const existing = snap.data();
      if (existing.landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "You can only update your own property." });
      }
      if (existing.moderationStatus !== "approved") {
        return res.status(409).json({
          success: false,
          message: "Only approved properties can toggle availability.",
        });
      }

      await propRef.update({ availabilityStatus: nextStatus, updatedAt: nowFn() });
      return res.json({
        success: true,
        message: `Property marked as ${nextStatus}.`,
        availability_status: nextStatus,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update availability." });
    }
  });

  // --- PROPERTY IMAGES (Storage upload URL + Metadata) ---
  router.post("/properties/:id/images/upload-url", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const contentType = cleanString(req.body?.content_type ?? req.body?.contentType, 80).toLowerCase();
      if (!ALLOWED_IMAGE_CONTENT_TYPES.has(contentType)) {
        return res.status(400).json({
          success: false,
          message: "Unsupported image type. Allowed: image/jpeg, image/png, image/webp.",
        });
      }

      const propSnap = await db.collection("properties").doc(propertyId).get();
      if (!propSnap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      if (propSnap.data().landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "You can only upload photos for your own property." });
      }
      if (!storage || !storageBucketName) {
        return res.status(503).json({
          success: false,
          message: "Firebase Storage bucket is not configured yet.",
        });
      }

      const ext = contentType === "image/png" ? "png" : contentType === "image/webp" ? "webp" : "jpg";
      const imageId = `img_${crypto.randomUUID().replace(/-/g, "").slice(0, 16)}`;
      const storagePath = `property-images/${req.user.uid}/${propertyId}/${imageId}.${ext}`;
      const bucket = storage.bucket(storageBucketName);
      const file = bucket.file(storagePath);
      const [uploadUrl] = await file.getSignedUrl({
        version: "v4",
        action: "write",
        expires: Date.now() + 15 * 60 * 1000,
        contentType,
      });

      return res.json({
        success: true,
        image_id: imageId,
        storage_path: storagePath,
        upload_url: uploadUrl,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to generate photo upload URL." });
    }
  });

  router.post("/properties/:id/images", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const storagePath = cleanString(req.body?.storage_path ?? req.body?.storagePath, 500);
      const imageUrl = cleanString(req.body?.image_url ?? req.body?.imageUrl, 1000);
      const contentType = req.body?.content_type ? cleanString(req.body.content_type, 80) : null;
      const sortOrder = Number.isInteger(req.body?.sort_order) ? req.body.sort_order : 0;

      if (!storagePath || !imageUrl) {
        return res.status(400).json({ success: false, message: "storage_path and image_url are required." });
      }
      const propSnap = await db.collection("properties").doc(propertyId).get();
      if (!propSnap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      const prop = propSnap.data();
      if (prop.landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "You can only attach photos to your own property." });
      }

      const imgRef = db.collection("property_images").doc();
      const payload = {
        propertyId,
        landlordId: req.user.uid,
        moderationStatus: prop.moderationStatus,
        storagePath,
        imageUrl,
        ...(contentType ? { contentType } : {}),
        sortOrder,
        createdAt: nowFn(),
      };
      await imgRef.set(payload);

      return res.status(201).json({
        success: true,
        message: "Property image registered.",
        image: sanitizePropertyImageDto(imgRef.id, payload),
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to register property image." });
    }
  });

  router.delete("/properties/:id/images/:imageId", requireAuth, requireRoles("landlord", "admin"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const imageId = cleanString(req.params.imageId, 128);
      const imgRef = db.collection("property_images").doc(imageId);
      const imgSnap = await imgRef.get();
      if (!imgSnap.exists || imgSnap.data().propertyId !== propertyId) {
        return res.status(404).json({ success: false, message: "Property image not found." });
      }
      const imgData = imgSnap.data();
      if (req.user.role !== "admin" && imgData.landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "Permission denied." });
      }

      await imgRef.delete();
      if (storage && storageBucketName && imgData.storagePath) {
        await storage.bucket(storageBucketName).file(imgData.storagePath).delete({ ignoreNotFound: true }).catch(() => {});
      }
      return res.json({ success: true, message: "Property image deleted." });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to delete property image." });
    }
  });

  // --- ADMIN MODERATION (Separate Admin Web App) ---
  router.get("/admin/properties", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const statusFilter = cleanString(req.query.status || "", 40).toLowerCase();
      let query = db.collection("properties");
      if (statusFilter) {
        query = query.where("moderationStatus", "==", statusFilter);
      }
      const snap = await query.get();
      const properties = snap.docs.map((doc) => sanitizePropertyDto(doc.id, doc.data()));
      return res.json({ success: true, properties });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to fetch moderation properties." });
    }
  });

  router.patch("/admin/properties/:id/approve", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const propRef = db.collection("properties").doc(propertyId);
      const snap = await propRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }

      const now = nowFn();
      await propRef.update({
        moderationStatus: "approved",
        rejectionReason: null,
        moderatedBy: req.user.uid,
        moderatedAt: now,
        updatedAt: now,
      });

      const imgSnap = await db.collection("property_images").where("propertyId", "==", propertyId).get();
      if (!imgSnap.empty) {
        const batch = db.batch();
        imgSnap.docs.forEach((doc) => batch.update(doc.ref, { moderationStatus: "approved" }));
        await batch.commit();
      }

      await writeAdminLog({
        adminUid: req.user.uid,
        action: "approve_property",
        targetType: "property",
        targetId: propertyId,
      });

      return res.json({
        success: true,
        message: "Property approved and now visible to tenants.",
        moderation_status: "approved",
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to approve property." });
    }
  });

  router.patch("/admin/properties/:id/reject", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const reason = cleanString(req.body?.reason ?? req.body?.rejection_reason, 500);
      if (reason.length < 3) {
        return res.status(400).json({ success: false, message: "A rejection reason is required." });
      }

      const propRef = db.collection("properties").doc(propertyId);
      const snap = await propRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }

      const now = nowFn();
      await propRef.update({
        moderationStatus: "rejected",
        rejectionReason: reason,
        moderatedBy: req.user.uid,
        moderatedAt: now,
        updatedAt: now,
      });

      const imgSnap = await db.collection("property_images").where("propertyId", "==", propertyId).get();
      if (!imgSnap.empty) {
        const batch = db.batch();
        imgSnap.docs.forEach((doc) => batch.update(doc.ref, { moderationStatus: "rejected" }));
        await batch.commit();
      }

      await writeAdminLog({
        adminUid: req.user.uid,
        action: "reject_property",
        targetType: "property",
        targetId: propertyId,
        reason,
      });

      return res.json({
        success: true,
        message: "Property rejected.",
        moderation_status: "rejected",
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to reject property." });
    }
  });

  // --- ENQUIRIES ---
  router.post("/enquiries", requireAuth, requireRoles("tenant"), async (req, res) => {
    try {
      const propertyId = cleanString(req.body?.property_id ?? req.body?.propertyId, 128);
      const message = req.body?.message ? cleanString(req.body.message, 1500) : null;
      const preferredInspectionDate = req.body?.preferred_inspection_date
        ? cleanString(req.body.preferred_inspection_date, 80)
        : null;

      if (!propertyId) {
        return res.status(400).json({ success: false, message: "property_id is required." });
      }

      const propSnap = await db.collection("properties").doc(propertyId).get();
      if (!propSnap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }

      const prop = propSnap.data();
      if (prop.moderationStatus !== "approved" || prop.availabilityStatus !== "available") {
        return res.status(409).json({
          success: false,
          message: "Enquiries can only be submitted for approved, available properties.",
        });
      }

      const now = nowFn();
      const enqRef = db.collection("enquiries").doc();
      const payload = {
        propertyId,
        tenantId: req.user.uid,
        landlordId: prop.landlordId,
        tenantName: req.user.fullName,
        propertyTitle: prop.title,
        neighbourhood: prop.neighbourhood,
        rentAmount: prop.rentAmount,
        ...(message ? { message } : {}),
        ...(preferredInspectionDate ? { preferredInspectionDate } : {}),
        status: "new",
        createdAt: now,
        updatedAt: now,
      };
      await enqRef.set(payload);

      return res.status(201).json({
        success: true,
        message: "Enquiry submitted successfully.",
        enquiry_id: enqRef.id,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to submit enquiry." });
    }
  });

  router.get("/tenant/enquiries", requireAuth, requireRoles("tenant"), async (req, res) => {
    try {
      const snap = await db.collection("enquiries").where("tenantId", "==", req.user.uid).get();
      const enquiries = snap.docs.map((doc) => sanitizeEnquiryDto(doc.id, doc.data()));
      return res.json({ success: true, enquiries });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to load enquiries." });
    }
  });

  router.get("/landlord/enquiries", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const snap = await db.collection("enquiries").where("landlordId", "==", req.user.uid).get();
      const enquiries = snap.docs.map((doc) => sanitizeEnquiryDto(doc.id, doc.data()));
      return res.json({ success: true, enquiries });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to load landlord enquiries." });
    }
  });

  router.patch("/landlord/enquiries/:id", requireAuth, requireRoles("landlord"), async (req, res) => {
    try {
      const enquiryId = cleanString(req.params.id, 128);
      const nextStatus = cleanString(req.body?.status, 24).toLowerCase();
      if (!ENQUIRY_STATUSES.has(nextStatus)) {
        return res.status(400).json({ success: false, message: "Status must be new, contacted, or closed." });
      }

      const enqRef = db.collection("enquiries").doc(enquiryId);
      const snap = await enqRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "Enquiry not found." });
      }
      if (snap.data().landlordId !== req.user.uid) {
        return res.status(403).json({ success: false, message: "Permission denied." });
      }

      await enqRef.update({ status: nextStatus, updatedAt: nowFn() });
      return res.json({
        success: true,
        message: `Enquiry marked as ${nextStatus}.`,
        status: nextStatus,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update enquiry status." });
    }
  });

  // --- FAVOURITES ---
  router.get("/favourites", requireAuth, requireRoles("tenant"), async (req, res) => {
    try {
      const favSnap = await db.collection("favourites").where("userId", "==", req.user.uid).get();
      const propertyIds = favSnap.docs.map((d) => d.data().propertyId).filter(Boolean);

      const properties = [];
      for (const propId of propertyIds) {
        const pSnap = await db.collection("properties").doc(propId).get();
        if (pSnap.exists) {
          const pData = pSnap.data();
          if (pData.moderationStatus === "approved" && pData.availabilityStatus === "available") {
            properties.push(sanitizePropertyDto(pSnap.id, pData));
          }
        }
      }
      return res.json({
        success: true,
        properties: await attachImagesToProperties(properties),
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to load favourites." });
    }
  });

  router.post("/favourites/:id", requireAuth, requireRoles("tenant"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const propSnap = await db.collection("properties").doc(propertyId).get();
      if (!propSnap.exists) {
        return res.status(404).json({ success: false, message: "Property not found." });
      }
      const prop = propSnap.data();
      if (prop.moderationStatus !== "approved" || prop.availabilityStatus !== "available") {
        return res.status(409).json({
          success: false,
          message: "Only approved, available properties can be favourited.",
        });
      }

      const favId = `${req.user.uid}_${propertyId}`;
      await db.collection("favourites").doc(favId).set({
        userId: req.user.uid,
        propertyId,
        createdAt: nowFn(),
      });
      return res.json({ success: true, message: "Property added to favourites." });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to add favourite." });
    }
  });

  router.delete("/favourites/:id", requireAuth, requireRoles("tenant"), async (req, res) => {
    try {
      const propertyId = cleanString(req.params.id, 128);
      const favId = `${req.user.uid}_${propertyId}`;
      await db.collection("favourites").doc(favId).delete();
      return res.json({ success: true, message: "Favourite removed." });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to remove favourite." });
    }
  });

  // --- ADMINISTRATOR USER MANAGEMENT & LOGS (Admin Web App) ---
  router.get("/admin/users", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const roleFilter = cleanString(req.query.role || "", 32).toLowerCase();
      let query = db.collection("users");
      if (roleFilter && ALL_ROLES.has(roleFilter)) {
        query = query.where("role", "==", roleFilter);
      }
      const snap = await query.get();
      const users = snap.docs.map((doc) => sanitizeUserDto(doc.data()));
      return res.json({ success: true, users });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to load users." });
    }
  });

  router.patch("/admin/users/:id/status", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const targetUid = cleanString(req.params.id, 128);
      const isActive = req.body?.is_active ?? req.body?.isActive;
      if (typeof isActive !== "boolean") {
        return res.status(400).json({ success: false, message: "Boolean is_active field is required." });
      }
      if (targetUid === req.user.uid && !isActive) {
        return res.status(409).json({ success: false, message: "Administrators cannot suspend their own account." });
      }

      const userRef = db.collection("users").doc(targetUid);
      const snap = await userRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "User not found." });
      }

      await userRef.update({ isActive, updatedAt: nowFn() });
      await auth.updateUser(targetUid, { disabled: !isActive }).catch(() => {});
      if (!isActive) {
        await auth.revokeRefreshTokens(targetUid).catch(() => {});
      }

      await writeAdminLog({
        adminUid: req.user.uid,
        action: isActive ? "activate_user" : "suspend_user",
        targetType: "user",
        targetId: targetUid,
      });

      return res.json({
        success: true,
        message: isActive ? "User account activated." : "User account suspended.",
        is_active: isActive,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update user status." });
    }
  });

  router.patch("/admin/users/:id/role", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const targetUid = cleanString(req.params.id, 128);
      const nextRole = cleanString(req.body?.role, 32).toLowerCase();
      if (!ALL_ROLES.has(nextRole)) {
        return res.status(400).json({ success: false, message: "Role must be tenant, landlord, or admin." });
      }
      if (targetUid === req.user.uid && nextRole !== "admin") {
        return res.status(409).json({ success: false, message: "Administrators cannot demote their own account." });
      }

      const userRef = db.collection("users").doc(targetUid);
      const snap = await userRef.get();
      if (!snap.exists) {
        return res.status(404).json({ success: false, message: "User not found." });
      }

      const batch = db.batch();
      batch.update(userRef, { role: nextRole, updatedAt: nowFn() });
      const credRef = db.collection("auth_credentials").doc(targetUid);
      batch.update(credRef, { role: nextRole, updatedAt: nowFn() });
      await batch.commit();

      await auth.setCustomUserClaims(targetUid, { role: nextRole }).catch(() => {});
      await writeAdminLog({
        adminUid: req.user.uid,
        action: "set_user_role",
        targetType: "user",
        targetId: targetUid,
        metadata: { role: nextRole },
      });

      return res.json({
        success: true,
        message: `User role updated to ${nextRole}.`,
        role: nextRole,
      });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to update user role." });
    }
  });

  router.get("/admin/logs", requireAuth, requireRoles("admin"), async (req, res) => {
    try {
      const snap = await db.collection("admin_logs").orderBy("createdAt", "desc").limit(100).get();
      const logs = snap.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
        createdAt: doc.data().createdAt?.toDate ? doc.data().createdAt.toDate().toISOString() : doc.data().createdAt,
      }));
      return res.json({ success: true, logs });
    } catch (err) {
      return res.status(500).json({ success: false, message: "Failed to load admin logs." });
    }
  });

  // Mount under both /api and / so both Hosting/Cloud Run (/api/...) and cloudfunctions.net/api (/...) resolve identically.
  app.use("/api", router);
  app.use("/", router);

  app.use((req, res) => {
    res.status(404).json({ success: false, message: "Endpoint not found." });
  });

  return app;
}

module.exports = {
  createDwelloApp,
};
