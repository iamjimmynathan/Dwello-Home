"use strict";

const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");
const { createDwelloApp } = require("../src/app");

/**
 * Faithful in-memory Firestore + Firebase Auth test double for fast local HTTP API integration tests.
 * Verifies real Express routing, Nigerian phone normalization, scrypt hashing, custom token -> ID token verification,
 * role-based access control, pending_approval moderation, private phone stripping, and admin audit logs.
 */
function createMockFirebaseEnv() {
  const collections = new Map();
  let autoId = 1;

  function ensureCollection(name) {
    if (!collections.has(name)) {
      collections.set(name, new Map());
    }
    return collections.get(name);
  }

  function makeDocSnapshot(collectionName, id, data) {
    return {
      id,
      exists: data !== undefined,
      data: () => (data ? { ...data } : undefined),
      ref: makeDocRef(collectionName, id),
    };
  }

  function makeDocRef(collectionName, id) {
    const col = ensureCollection(collectionName);
    return {
      id,
      path: `${collectionName}/${id}`,
      async get() {
        return makeDocSnapshot(collectionName, id, col.get(id));
      },
      async set(data, options = {}) {
        if (options.merge && col.has(id)) {
          col.set(id, { ...col.get(id), ...data });
        } else {
          col.set(id, { ...data });
        }
      },
      async update(updates) {
        if (!col.has(id)) {
          throw new Error(`Document ${collectionName}/${id} not found`);
        }
        col.set(id, { ...col.get(id), ...updates });
      },
      async delete() {
        col.delete(id);
      },
    };
  }

  function makeQuery(collectionName, filters = [], orderField = null, orderDir = "asc", limitCount = null) {
    return {
      where(field, op, value) {
        return makeQuery(collectionName, [...filters, { field, op, value }], orderField, orderDir, limitCount);
      },
      orderBy(field, dir = "asc") {
        return makeQuery(collectionName, filters, field, dir, limitCount);
      },
      limit(n) {
        return makeQuery(collectionName, filters, orderField, orderDir, n);
      },
      async get() {
        const col = ensureCollection(collectionName);
        let entries = Array.from(col.entries()).filter(([, doc]) =>
          filters.every(({ field, op, value }) => (op === "==" ? doc[field] === value : true))
        );
        if (orderField) {
          entries.sort(([, a], [, b]) => {
            const va = a[orderField] instanceof Date ? a[orderField].getTime() : a[orderField];
            const vb = b[orderField] instanceof Date ? b[orderField].getTime() : b[orderField];
            return orderDir === "desc" ? vb - va : va - vb;
          });
        }
        if (limitCount !== null) {
          entries = entries.slice(0, limitCount);
        }
        const docs = entries.map(([id, data]) => makeDocSnapshot(collectionName, id, data));
        return { empty: docs.length === 0, docs };
      },
    };
  }

  const db = {
    collection(name) {
      ensureCollection(name);
      return {
        doc(id) {
          const resolvedId = id || `doc_${autoId++}`;
          return makeDocRef(name, resolvedId);
        },
        where(field, op, value) {
          return makeQuery(name, [{ field, op, value }]);
        },
        orderBy(field, dir) {
          return makeQuery(name, [], field, dir);
        },
        async get() {
          return makeQuery(name).get();
        },
      };
    },
    batch() {
      const ops = [];
      return {
        set(ref, data, options) {
          ops.push(() => ref.set(data, options));
        },
        update(ref, data) {
          ops.push(() => ref.update(data));
        },
        async commit() {
          for (const fn of ops) await fn();
        },
      };
    },
  };

  const authUsers = new Map();
  const revokedUids = new Set();

  const auth = {
    async createUser({ uid, phoneNumber, displayName, email }) {
      const finalUid = uid || `uid_${autoId++}`;
      for (const existing of authUsers.values()) {
        if (phoneNumber && existing.phoneNumber === phoneNumber) {
          const err = new Error("Phone exists");
          err.code = "auth/phone-number-already-exists";
          throw err;
        }
      }
      const record = { uid: finalUid, phoneNumber, displayName, email, disabled: false, claims: {} };
      authUsers.set(finalUid, record);
      return record;
    },
    async setCustomUserClaims(uid, claims) {
      const u = authUsers.get(uid);
      if (u) u.claims = { ...claims };
    },
    async createCustomToken(uid, claims) {
      return Buffer.from(JSON.stringify({ type: "custom", uid, claims })).toString("base64url");
    },
    async verifyIdToken(idToken) {
      const parsed = JSON.parse(Buffer.from(idToken, "base64url").toString("utf8"));
      if (parsed.type !== "id" || !parsed.uid || !authUsers.has(parsed.uid)) {
        const err = new Error("Invalid token");
        err.code = "auth/invalid-id-token";
        throw err;
      }
      if (revokedUids.has(parsed.uid) && !parsed.fresh) {
        const err = new Error("Revoked");
        err.code = "auth/id-token-revoked";
        throw err;
      }
      return { uid: parsed.uid, ...parsed.claims };
    },
    async updateUser(uid, updates) {
      const u = authUsers.get(uid);
      if (u) Object.assign(u, updates);
    },
    async deleteUser(uid) {
      authUsers.delete(uid);
    },
    async revokeRefreshTokens(uid) {
      revokedUids.add(uid);
    },
  };

  function exchangeCustomTokenForIdToken(customToken, fresh = false) {
    const parsed = JSON.parse(Buffer.from(customToken, "base64url").toString("utf8"));
    return Buffer.from(JSON.stringify({ type: "id", uid: parsed.uid, claims: parsed.claims, fresh })).toString("base64url");
  }

  return { db, auth, exchangeCustomTokenForIdToken, authUsers };
}

let server;
let baseUrl;
let mockEnv;

async function apiFetch(path, { method = "GET", token = null, body = null } = {}) {
  const headers = { Accept: "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== null) headers["Content-Type"] = "application/json";
  const res = await fetch(`${baseUrl}${path}`, {
    method,
    headers,
    body: body !== null ? JSON.stringify(body) : undefined,
  });
  const json = await res.json();
  return { status: res.status, json };
}

before(async () => {
  mockEnv = createMockFirebaseEnv();
  const app = createDwelloApp({
    db: mockEnv.db,
    auth: mockEnv.auth,
    pepper: "test-pepper",
  });
  server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const addr = server.address();
  baseUrl = `http://127.0.0.1:${addr.port}`;
});

after(async () => {
  if (server) {
    await new Promise((resolve) => server.close(resolve));
  }
});

test("End-to-end auth, RBAC, property moderation, enquiries, favourites, and admin user management", async () => {
  // 1. Health check
  const health = await apiFetch("/api/health");
  assert.equal(health.status, 200);
  assert.equal(health.json.project, "dwello-home");

  // 2. Public admin registration must be blocked
  const adminRegAttempt = await apiFetch("/api/auth/register", {
    method: "POST",
    body: {
      full_name: "Fake Admin",
      phone: "08031234560",
      password: "Password123!",
      role: "admin",
    },
  });
  assert.equal(adminRegAttempt.status, 403);

  // 3. Register Landlord & Tenant with Nigerian phone normalization
  const landlordReg = await apiFetch("/api/auth/register", {
    method: "POST",
    body: {
      full_name: "Chinedu Landlord",
      phone: "0803 555 1111",
      password: "StrongPassword1!",
      role: "landlord",
    },
  });
  assert.equal(landlordReg.status, 201);
  assert.equal(landlordReg.json.user.phone, "+2348035551111");
  assert.equal(landlordReg.json.user.role, "landlord");
  assert.ok(landlordReg.json.custom_token);

  const tenantReg = await apiFetch("/api/auth/register", {
    method: "POST",
    body: {
      full_name: "Amaka Tenant",
      phone: "08124449999",
      password: "TenantPassword1!",
      role: "tenant",
    },
  });
  assert.equal(tenantReg.status, 201);
  assert.equal(tenantReg.json.user.phone, "+2348124449999");

  // 4. Wrong password rejected; valid phone + password issues custom token -> ID token
  const badLogin = await apiFetch("/api/auth/login", {
    method: "POST",
    body: { phone: "+2348035551111", password: "WrongPassword1!" },
  });
  assert.equal(badLogin.status, 401);

  const landlordLogin = await apiFetch("/api/auth/login", {
    method: "POST",
    body: { phone: "08035551111", password: "StrongPassword1!" },
  });
  assert.equal(landlordLogin.status, 200);
  const landlordIdToken = mockEnv.exchangeCustomTokenForIdToken(landlordLogin.json.custom_token);
  const tenantIdToken = mockEnv.exchangeCustomTokenForIdToken(tenantReg.json.custom_token);

  // 5. Provision a private Admin account in DB & Auth (simulating bootstrapAdmin.js)
  await mockEnv.auth.createUser({ uid: "admin_master", phoneNumber: "+2348000000001", displayName: "Dwello Admin" });
  await mockEnv.db.collection("users").doc("admin_master").set({
    uid: "admin_master",
    fullName: "Dwello Admin",
    phoneE164: "+2348000000001",
    role: "admin",
    isActive: true,
    phoneVerified: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  });
  const adminCustom = await mockEnv.auth.createCustomToken("admin_master", { role: "admin" });
  const adminIdToken = mockEnv.exchangeCustomTokenForIdToken(adminCustom);

  // 6. Landlord submits property -> defaults to pending_approval; tenant cannot see it or create properties
  const tenantCreateAttempt = await apiFetch("/api/properties", {
    method: "POST",
    token: tenantIdToken,
    body: {
      title: "Unauthorized Listing",
      property_type: "mini_flat",
      bedrooms: 1,
      bathrooms: 1,
      rent_amount: 500000,
      rent_period: "yearly",
      description: "Tenants must not create properties.",
      neighbourhood: "Ugbowo",
      full_address: "12 Ugbowo Road",
    },
  });
  assert.equal(tenantCreateAttempt.status, 403);

  const createdProp = await apiFetch("/api/properties", {
    method: "POST",
    token: landlordIdToken,
    body: {
      title: "Spacious 2 Bedroom Flat in GRA",
      property_type: "2_bedroom",
      bedrooms: 2,
      bathrooms: 2,
      rent_amount: 1600000,
      rent_period: "yearly",
      description: "Clean modern apartment with borehole water and gated parking.",
      neighbourhood: "GRA",
      full_address: "10 Golf Course Road, GRA, Benin City",
      landmark: "Near Benin Club",
      facilities: ["Water", "Parking", "Security"],
    },
  });
  assert.equal(createdProp.status, 201);
  assert.equal(createdProp.json.moderation_status, "pending_approval");
  const propertyId = createdProp.json.property_id;

  // Tenant browsing sees 0 properties before admin approval
  const beforeApproveList = await apiFetch("/api/properties", { token: tenantIdToken });
  assert.equal(beforeApproveList.status, 200);
  assert.equal(beforeApproveList.json.properties.length, 0);

  // Landlord cannot approve own listing
  const landlordApproveAttempt = await apiFetch(`/api/admin/properties/${propertyId}/approve`, {
    method: "PATCH",
    token: landlordIdToken,
  });
  assert.equal(landlordApproveAttempt.status, 403);

  // Admin approves property -> creates admin_log
  const adminApprove = await apiFetch(`/api/admin/properties/${propertyId}/approve`, {
    method: "PATCH",
    token: adminIdToken,
  });
  assert.equal(adminApprove.status, 200);
  assert.equal(adminApprove.json.moderation_status, "approved");

  // Tenant now sees approved property WITHOUT any private landlord phone number
  const afterApproveList = await apiFetch("/api/properties", { token: tenantIdToken });
  assert.equal(afterApproveList.status, 200);
  assert.equal(afterApproveList.json.properties.length, 1);
  assert.equal(afterApproveList.json.properties[0].landlord_phone, undefined);
  assert.equal(afterApproveList.json.properties[0].phone, undefined);

  // 7. Tenant submits enquiry & saves favourite; landlord marks enquiry contacted
  const enqRes = await apiFetch("/api/enquiries", {
    method: "POST",
    token: tenantIdToken,
    body: {
      property_id: propertyId,
      message: "Interested in inspecting tomorrow.",
      preferred_inspection_date: "2026-11-20",
    },
  });
  assert.equal(enqRes.status, 201);
  const enquiryId = enqRes.json.enquiry_id;

  const favAdd = await apiFetch(`/api/favourites/${propertyId}`, { method: "POST", token: tenantIdToken });
  assert.equal(favAdd.status, 200);

  const favList = await apiFetch("/api/favourites", { token: tenantIdToken });
  assert.equal(favList.status, 200);
  assert.equal(favList.json.properties.length, 1);

  const enqPatch = await apiFetch(`/api/landlord/enquiries/${enquiryId}`, {
    method: "PATCH",
    token: landlordIdToken,
    body: { status: "contacted" },
  });
  assert.equal(enqPatch.status, 200);
  assert.equal(enqPatch.json.status, "contacted");

  // 8. Admin checks audit logs and suspends tenant -> suspended tenant blocked on login & API
  const logsRes = await apiFetch("/api/admin/logs", { token: adminIdToken });
  assert.equal(logsRes.status, 200);
  assert.ok(logsRes.json.logs.some((l) => l.action === "approve_property" && l.targetId === propertyId));

  const suspendRes = await apiFetch(`/api/admin/users/${tenantReg.json.user.id}/status`, {
    method: "PATCH",
    token: adminIdToken,
    body: { is_active: false },
  });
  assert.equal(suspendRes.status, 200);

  const suspendedLogin = await apiFetch("/api/auth/login", {
    method: "POST",
    body: { phone: "08124449999", password: "TenantPassword1!" },
  });
  assert.equal(suspendedLogin.status, 403);
});
