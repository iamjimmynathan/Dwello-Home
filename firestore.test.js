const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const TENANT_UID = "tenant_uid_01";
const OTHER_TENANT_UID = "tenant_uid_02";
const LANDLORD_UID = "landlord_uid_01";
const OTHER_LANDLORD_UID = "landlord_uid_02";
const ADMIN_UID = "admin_uid_01";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function tsNow() {
  return new Date(Date.now() - 1000);
}

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
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      const db = ctx.firestore();
      const now = tsNow();
      await db.collection("users").doc(TENANT_UID).set({
        uid: TENANT_UID,
        fullName: "Osas Tenant",
        phoneE164: "+2348031112222",
        role: "tenant",
        isActive: true,
        phoneVerified: false,
        createdAt: now,
        updatedAt: now,
      });
      await db.collection("users").doc(OTHER_TENANT_UID).set({
        uid: OTHER_TENANT_UID,
        fullName: "Efe Tenant",
        phoneE164: "+2348039998888",
        role: "tenant",
        isActive: true,
        phoneVerified: false,
        createdAt: now,
        updatedAt: now,
      });
      await db.collection("users").doc(LANDLORD_UID).set({
        uid: LANDLORD_UID,
        fullName: "Igho Landlord",
        phoneE164: "+2348053334444",
        role: "landlord",
        isActive: true,
        phoneVerified: false,
        createdAt: now,
        updatedAt: now,
      });
      await db.collection("users").doc(OTHER_LANDLORD_UID).set({
        uid: OTHER_LANDLORD_UID,
        fullName: "Nosakhare Landlord",
        phoneE164: "+2348057776666",
        role: "landlord",
        isActive: true,
        phoneVerified: false,
        createdAt: now,
        updatedAt: now,
      });
      await db.collection("users").doc(ADMIN_UID).set({
        uid: ADMIN_UID,
        fullName: "Dwello Admin",
        phoneE164: "+2348000000001",
        role: "admin",
        isActive: true,
        phoneVerified: true,
        createdAt: now,
        updatedAt: now,
      });
      await db.collection("auth_credentials").doc(LANDLORD_UID).set({
        uid: LANDLORD_UID,
        phoneE164: "+2348053334444",
        passwordHash: "scrypt$abc$def",
        role: "landlord",
        failedAttempts: 0,
        createdAt: now,
        updatedAt: now,
      });
    });
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("properties").get());
  await assertFails(unauth.collection("users").doc(TENANT_UID).get());
  await assertFails(unauth.collection("auth_credentials").doc(LANDLORD_UID).get());
  await assertFails(unauth.collection("admin_logs").doc("log_1").get());
});

test("PII & Credentials: users cannot read other users or access auth_credentials", async () => {
  const tenantDb = testEnv.authenticatedContext(TENANT_UID).firestore();
  await assertSucceeds(tenantDb.collection("users").doc(TENANT_UID).get());
  await assertFails(tenantDb.collection("users").doc(LANDLORD_UID).get());
  await assertFails(tenantDb.collection("users").get());
  await assertFails(tenantDb.collection("auth_credentials").doc(LANDLORD_UID).get());
});

test("Privilege escalation blocked: user cannot elevate own role or self-create user doc", async () => {
  const tenantDb = testEnv.authenticatedContext(TENANT_UID).firestore();
  await assertFails(
    tenantDb.collection("users").doc(TENANT_UID).update({
      role: "admin",
      updatedAt: tsNow(),
    })
  );
  await assertFails(
    tenantDb.collection("users").doc("spoof_uid").set({
      uid: "spoof_uid",
      fullName: "Hacker",
      phoneE164: "+2348012345678",
      role: "admin",
      isActive: true,
      phoneVerified: false,
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );
});

test("Landlord property creation: must default to pending_approval and forbid landlordPhone", async () => {
  const landlordDb = testEnv.authenticatedContext(LANDLORD_UID).firestore();
  const baseProp = {
    landlordId: LANDLORD_UID,
    landlordName: "Igho Landlord",
    title: "Modern 2 Bedroom Flat in GRA",
    propertyType: "2_bedroom",
    bedrooms: 2,
    bathrooms: 2,
    rentAmount: 1800000,
    rentPeriod: "yearly",
    description: "Clean tiled apartment with borehole and prepaid meter in GRA Benin City.",
    city: "Benin City",
    neighbourhood: "GRA",
    fullAddress: "14 Boundary Road, GRA, Benin City",
    facilities: ["Water", "Electricity"],
    availabilityStatus: "available",
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };

  // Self-approving on create must fail
  await assertFails(
    landlordDb.collection("properties").doc("prop_self_approve").set({
      ...baseProp,
      moderationStatus: "approved",
    })
  );

  // Including private phone field must fail
  await assertFails(
    landlordDb.collection("properties").doc("prop_with_phone").set({
      ...baseProp,
      moderationStatus: "pending_approval",
      landlordPhone: "+2348053334444",
    })
  );

  // Valid pending_approval submission by landlord succeeds
  await assertSucceeds(
    landlordDb.collection("properties").doc("prop_valid_1").set({
      ...baseProp,
      moderationStatus: "pending_approval",
    })
  );

  // Tenant cannot create a property
  const tenantDb = testEnv.authenticatedContext(TENANT_UID).firestore();
  await assertFails(
    tenantDb.collection("properties").doc("prop_tenant_attempt").set({
      ...baseProp,
      landlordId: TENANT_UID,
      moderationStatus: "pending_approval",
    })
  );
});

test("Moderation visibility: tenants only see approved & available properties; only admin can approve", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const now = tsNow();
    await db.collection("properties").doc("prop_pending").set({
      landlordId: LANDLORD_UID,
      landlordName: "Igho Landlord",
      title: "Pending 1 Bedroom",
      propertyType: "1_bedroom",
      bedrooms: 1,
      bathrooms: 1,
      rentAmount: 650000,
      rentPeriod: "yearly",
      description: "Spacious 1 bedroom flat near Ugbowo Campus gate.",
      city: "Benin City",
      neighbourhood: "Ugbowo",
      fullAddress: "22 Campus Road, Ugbowo",
      moderationStatus: "pending_approval",
      availabilityStatus: "available",
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("properties").doc("prop_approved").set({
      landlordId: LANDLORD_UID,
      landlordName: "Igho Landlord",
      title: "Approved 2 Bedroom",
      propertyType: "2_bedroom",
      bedrooms: 2,
      bathrooms: 2,
      rentAmount: 1500000,
      rentPeriod: "yearly",
      description: "Newly renovated flat with security and parking in Sapele Road.",
      city: "Benin City",
      neighbourhood: "Sapele Road",
      fullAddress: "8 Sapele Road, Benin City",
      moderationStatus: "approved",
      availabilityStatus: "available",
      createdAt: now,
      updatedAt: now,
    });
  });

  const tenantDb = testEnv.authenticatedContext(TENANT_UID).firestore();
  const landlordDb = testEnv.authenticatedContext(LANDLORD_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();

  // Tenant cannot read pending property or unfiltered list
  await assertFails(tenantDb.collection("properties").doc("prop_pending").get());
  await assertFails(tenantDb.collection("properties").get());

  // Tenant CAN read approved + available property and filtered query
  await assertSucceeds(tenantDb.collection("properties").doc("prop_approved").get());
  const approvedSnap = await assertSucceeds(
    tenantDb
      .collection("properties")
      .where("moderationStatus", "==", "approved")
      .where("availabilityStatus", "==", "available")
      .get()
  );
  assert.equal(approvedSnap.docs.length, 1);

  // Landlord cannot approve their own listing
  await assertFails(
    landlordDb.collection("properties").doc("prop_pending").update({
      moderationStatus: "approved",
      updatedAt: tsNow(),
    })
  );

  // Admin CAN approve
  await assertSucceeds(
    adminDb.collection("properties").doc("prop_pending").update({
      moderationStatus: "approved",
      rejectionReason: null,
      moderatedBy: ADMIN_UID,
      moderatedAt: tsNow(),
      updatedAt: tsNow(),
    })
  );
});

test("Enquiries & Favourites: isolated by tenant/landlord and restricted to approved+available listings", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const now = tsNow();
    await db.collection("properties").doc("prop_live").set({
      landlordId: LANDLORD_UID,
      landlordName: "Igho Landlord",
      title: "Live Duplex GRA",
      propertyType: "duplex",
      bedrooms: 3,
      bathrooms: 3,
      rentAmount: 3500000,
      rentPeriod: "yearly",
      description: "Executive duplex with security and borehole water.",
      city: "Benin City",
      neighbourhood: "GRA",
      fullAddress: "5 Ihama Road, GRA",
      moderationStatus: "approved",
      availabilityStatus: "available",
      createdAt: now,
      updatedAt: now,
    });
  });

  const tenantDb = testEnv.authenticatedContext(TENANT_UID).firestore();
  const otherTenantDb = testEnv.authenticatedContext(OTHER_TENANT_UID).firestore();
  const landlordDb = testEnv.authenticatedContext(LANDLORD_UID).firestore();
  const otherLandlordDb = testEnv.authenticatedContext(OTHER_LANDLORD_UID).firestore();

  // Tenant submits enquiry
  await assertSucceeds(
    tenantDb.collection("enquiries").doc("enq_1").set({
      propertyId: "prop_live",
      tenantId: TENANT_UID,
      landlordId: LANDLORD_UID,
      tenantName: "Osas Tenant",
      propertyTitle: "Live Duplex GRA",
      neighbourhood: "GRA",
      rentAmount: 3500000,
      message: "Can I inspect on Saturday?",
      status: "new",
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  // Other tenant & other landlord cannot read enq_1
  await assertFails(otherTenantDb.collection("enquiries").doc("enq_1").get());
  await assertFails(otherLandlordDb.collection("enquiries").doc("enq_1").get());

  // Owning landlord can read and mark contacted
  await assertSucceeds(landlordDb.collection("enquiries").doc("enq_1").get());
  await assertSucceeds(
    landlordDb.collection("enquiries").doc("enq_1").update({
      status: "contacted",
      updatedAt: tsNow(),
    })
  );

  // Tenant can create & read own favourite; other tenant cannot delete it
  await assertSucceeds(
    tenantDb.collection("favourites").doc(`${TENANT_UID}_prop_live`).set({
      userId: TENANT_UID,
      propertyId: "prop_live",
      createdAt: tsNow(),
    })
  );
  await assertFails(otherTenantDb.collection("favourites").doc(`${TENANT_UID}_prop_live`).delete());
  await assertSucceeds(tenantDb.collection("favourites").doc(`${TENANT_UID}_prop_live`).delete());
});

test("Admin logs: cannot be written by any client SDK", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertFails(
    adminDb.collection("admin_logs").doc("log_attempt").set({
      adminUid: ADMIN_UID,
      action: "approve_property",
      targetType: "property",
      targetId: "prop_1",
      createdAt: tsNow(),
    })
  );
});
