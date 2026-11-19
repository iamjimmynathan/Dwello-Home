# Dwello Homes — Shared Firebase Backend & REST API Specification

This backend serves **both** the **Dwello Homes Android APK** (`Tenants` and `Landlords`) and the separately developed **Dwello Homes Admin Web App** (`Admins`).

---

## 1. Firebase Project & Resources

- **Firebase Project:** `Dwello Home` (`dwello-home`)
- **Primary Region:** `europe-west3`
- **Cloud Firestore Named Database ID:** `ai-studio-android-dwelloho-b885f2f3-86cf-4fd4-b080-a65e5fa25ef9`
- **Firebase Storage Bucket:** `dwello-home.firebasestorage.app` (requires Blaze plan enablement)
- **Cloud Functions Entry Point:** `api` (`onRequest` HTTP v2 in `europe-west3`)

---

## 2. Firestore Collections

| Collection | Purpose | Access Policy |
|---|---|---|
| `users/{uid}` | User profiles (`uid`, `fullName`, `phoneE164`, `email`, `role`, `isActive`, `phoneVerified`, timestamps). | Owner `get` / limited self-update (`fullName`, `email` only). `admin` read via REST API. Public `list` blocked to prevent PII leaks. |
| `auth_credentials/{uid}` | Server-only `scrypt` password hashes + `phoneE164` lookup. | `allow read, write: if false` for all clients. Accessed only by Cloud Functions via Admin SDK. |
| `properties/{propertyId}` | Benin City property listings. Defaults to `pending_approval`. Never stores private landlord phone numbers. | Tenants read only `approved` + `available`. Landlords read/create/edit own listings (edits reset to `pending_approval`). Only `admin` approves/rejects. |
| `property_images/{imageId}` | Photo metadata linked to Firebase Storage paths. | Synced with parent `moderationStatus`. Owned by `landlordId`. |
| `enquiries/{enquiryId}` | Tenant inspection enquiries on `approved` + `available` listings. | Readable only by `tenantId`, `landlordId`, or `admin`. |
| `favourites/{uid}_{propertyId}` | Tenant saved property bookmarks. | Strictly scoped to `userId == request.auth.uid`. |
| `admin_logs/{logId}` | Immutable audit trail of moderation & user actions. | Server-written only (`allow write: if false`). Readable by `admin` only. |

---

## 3. Authentication Flow (Nigerian Phone + Password -> Firebase ID Token)

1. Client collects Nigerian phone number + password (and role `tenant` or `landlord` on registration).
2. Client calls `POST /api/auth/register` or `POST /api/auth/login`.
3. Cloud Function normalizes phone to `+234XXXXXXXXXX`, verifies password hash via `crypto.scrypt` + `timingSafeEqual`, enforces `isActive`, and mints a **Firebase Custom Token** (`custom_token`).
4. Client calls Firebase Auth SDK: `signInWithCustomToken(custom_token)`.
5. Firebase Auth returns a **Firebase ID Token** (JWT) + refresh token.
6. Client sends `Authorization: Bearer <firebaseIdToken>` on all subsequent REST API calls and Firestore/Storage SDK calls.
7. Cloud Functions verify every request via `admin.auth().verifyIdToken(idToken, true)` and enforce `users/{uid}.role` and `isActive` on the server.

---

## 4. REST API Endpoints

Base URL after deploying Cloud Functions:
`https://europe-west3-dwello-home.cloudfunctions.net/api` (or the Cloud Run service URL output by `firebase deploy --only functions`).
All routes are mounted under both `/api/...` and `/...`.

### Authentication
- `GET /api/health` — Service health status.
- `POST /api/auth/register` — Public registration (`role`: `"tenant"` or `"landlord"` only; `"admin"` returns `403`). Returns `{ custom_token, user }`.
- `POST /api/auth/login` — Verifies Nigerian phone + password. Returns `{ custom_token, user }`.
- `GET /api/auth/me` — *(Auth)* Returns authenticated user profile.
- `POST /api/auth/logout` — *(Auth)* Revokes Firebase refresh tokens server-side.

### User Profiles
- `GET /api/users/me` — *(Auth)* Returns current user profile.
- `PATCH /api/users/me` — *(Auth)* Updates `full_name` and optional `email` only.

### Property Browsing & Landlord Management
- `GET /api/properties` — *(Auth)* Returns `approved` + `available` properties (supports `neighbourhood`, `property_type`, `min_rent`, `max_rent`, `q`). Never returns landlord phone numbers.
- `GET /api/properties/:id` — *(Auth)* Returns property + `images` (if `approved && available`, owner, or admin).
- `POST /api/properties` — *(Landlord)* Creates listing with forced `moderation_status: "pending_approval"`.
- `GET /api/landlord/properties` — *(Landlord)* Lists landlord's own properties across all statuses.
- `PATCH /api/properties/:id` — *(Landlord owner)* Edits property details and resets `moderation_status` to `"pending_approval"`.
- `PATCH /api/properties/:id/availability` — *(Landlord owner)* Toggles `available` / `unavailable` on `approved` properties.
- `POST /api/properties/:id/images/upload-url` — *(Landlord owner)* Issues signed Firebase Storage upload URL (`image/jpeg`, `image/png`, `image/webp`).
- `POST /api/properties/:id/images` — *(Landlord owner)* Registers uploaded photo in `property_images`.
- `DELETE /api/properties/:id/images/:imageId` — *(Landlord owner or Admin)* Deletes photo record and Storage object.

### Enquiries & Favourites
- `POST /api/enquiries` — *(Tenant)* Submits enquiry for an `approved` + `available` property.
- `GET /api/tenant/enquiries` — *(Tenant)* Lists tenant's own enquiries.
- `GET /api/landlord/enquiries` — *(Landlord)* Lists enquiries on landlord's properties.
- `PATCH /api/landlord/enquiries/:id` — *(Landlord owner)* Updates status (`new`, `contacted`, `closed`).
- `GET /api/favourites` — *(Tenant)* Lists favourited `approved` + `available` properties.
- `POST /api/favourites/:id` — *(Tenant)* Saves property to favourites.
- `DELETE /api/favourites/:id` — *(Tenant)* Removes property from favourites.

### Admin Moderation & User Management (Separate Admin Web App Only)
- `GET /api/admin/properties?status=pending_approval` — *(Admin)* Lists all or status-filtered properties.
- `PATCH /api/admin/properties/:id/approve` — *(Admin)* Approves property + photos and logs to `admin_logs`.
- `PATCH /api/admin/properties/:id/reject` — *(Admin)* Rejects property with required `reason` and logs to `admin_logs`.
- `GET /api/admin/users?role=` — *(Admin)* Lists user profiles (never password hashes).
- `PATCH /api/admin/users/:id/status` — *(Admin)* Activates or suspends user account (`is_active: boolean`) and revokes tokens when suspended.
- `PATCH /api/admin/users/:id/role` — *(Admin)* Updates user role (`tenant`, `landlord`, `admin`) and Firebase custom claims.
- `GET /api/admin/logs` — *(Admin)* Lists latest 100 entries from `admin_logs`.

---

## 5. Required Environment Variables

### Cloud Functions (`functions/.env` / Cloud Secret Manager)
- `DWELLO_FIRESTORE_DATABASE_ID` — Named Firestore database (`ai-studio-android-dwelloho-b885f2f3-86cf-4fd4-b080-a65e5fa25ef9`).
- `DWELLO_STORAGE_BUCKET` — Firebase Storage bucket (`dwello-home.firebasestorage.app`).
- `DWELLO_ALLOWED_ORIGINS` — Comma-separated CORS origins for the separate Admin Web App.
- `DWELLO_DEFAULT_CITY` — Default property market city (`Benin City`).
- `DWELLO_AUTH_PEPPER` — *(Secret)* Optional server-side pepper mixed into `scrypt` password hashing.
- `DWELLO_BOOTSTRAP_ADMIN_PHONE` / `DWELLO_BOOTSTRAP_ADMIN_PASSWORD` / `DWELLO_BOOTSTRAP_ADMIN_NAME` — Used **only once locally** via `functions/scripts/bootstrapAdmin.js` with `GOOGLE_APPLICATION_CREDENTIALS` to create the private Admin account; never stored in app code.

### Android App (`.env` / AI Studio Secrets Panel)
- `FIREBASE_API_BASE_URL` — Deployed Cloud Functions base URL (`https://europe-west3-dwello-home.cloudfunctions.net/api/` or your deployed Cloud Run URL). Firebase is the sole active backend; Cloudflare toggles and fallbacks are removed.
