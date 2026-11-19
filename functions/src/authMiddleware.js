"use strict";

/**
 * Verifies Firebase ID tokens from Authorization: Bearer <idToken>
 * and resolves the authoritative user role + active status from Firestore.
 */
function createAuthMiddleware({ auth, db }) {
  async function requireAuth(req, res, next) {
    const authHeader = req.headers.authorization || req.headers.Authorization || "";
    if (typeof authHeader !== "string" || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({
        success: false,
        message: "Authentication required. Provide a valid Firebase ID token.",
      });
    }

    const idToken = authHeader.slice("Bearer ".length).trim();
    if (!idToken) {
      return res.status(401).json({
        success: false,
        message: "Missing Bearer token.",
      });
    }

    try {
      const decoded = await auth.verifyIdToken(idToken, true);
      const userSnap = await db.collection("users").doc(decoded.uid).get();
      if (!userSnap.exists) {
        return res.status(403).json({
          success: false,
          message: "User profile not found for authenticated account.",
        });
      }

      const userData = userSnap.data();
      if (userData.isActive === false) {
        return res.status(403).json({
          success: false,
          message: "This account has been suspended. Contact Dwello Homes support.",
        });
      }

      req.user = {
        uid: decoded.uid,
        role: userData.role,
        fullName: userData.fullName,
        phoneE164: userData.phoneE164,
        email: userData.email || null,
        isActive: userData.isActive !== false,
        raw: userData,
      };
      return next();
    } catch (err) {
      const code = err && err.code ? String(err.code) : "";
      const isRevokedOrExpired =
        code.includes("id-token-revoked") ||
        code.includes("id-token-expired") ||
        code.includes("argument-error") ||
        code.includes("invalid-id-token");
      return res.status(401).json({
        success: false,
        message: isRevokedOrExpired
          ? "Session expired or revoked. Please log in again."
          : "Invalid authentication token.",
      });
    }
  }

  function requireRoles(...allowedRoles) {
    const roleSet = new Set(allowedRoles);
    return (req, res, next) => {
      if (!req.user || !roleSet.has(req.user.role)) {
        return res.status(403).json({
          success: false,
          message: "You do not have permission to perform this action.",
        });
      }
      return next();
    };
  }

  return {
    requireAuth,
    requireRoles,
  };
}

module.exports = {
  createAuthMiddleware,
};
