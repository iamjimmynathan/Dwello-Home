"use strict";

const crypto = require("node:crypto");
const { promisify } = require("node:util");

const scryptAsync = promisify(crypto.scrypt);
const KEY_LENGTH = 64;
const SCRYPT_PARAMS = { N: 16384, r: 8, p: 1 };

/**
 * Hashes a user password with a unique 16-byte random salt and optional server pepper.
 * Format: scrypt$<saltHex>$<derivedHex>
 */
async function hashPassword(password, pepper = "") {
  if (typeof password !== "string" || password.length < 8) {
    throw new Error("Password must be at least 8 characters.");
  }
  const salt = crypto.randomBytes(16).toString("hex");
  const material = `${password}${pepper}`;
  const derived = await scryptAsync(material, salt, KEY_LENGTH, SCRYPT_PARAMS);
  return `scrypt$${salt}$${Buffer.from(derived).toString("hex")}`;
}

/**
 * Constant-time verification of a password against a stored scrypt hash.
 */
async function verifyPassword(password, storedHash, pepper = "") {
  if (typeof password !== "string" || typeof storedHash !== "string") {
    return false;
  }
  const parts = storedHash.split("$");
  if (parts.length !== 3 || parts[0] !== "scrypt") {
    return false;
  }
  const [, salt, expectedHex] = parts;
  if (!salt || !expectedHex) return false;

  const material = `${password}${pepper}`;
  const derived = await scryptAsync(material, salt, KEY_LENGTH, SCRYPT_PARAMS);
  const actualBuf = Buffer.from(derived);
  const expectedBuf = Buffer.from(expectedHex, "hex");
  if (actualBuf.length !== expectedBuf.length) {
    return false;
  }
  return crypto.timingSafeEqual(actualBuf, expectedBuf);
}

module.exports = {
  hashPassword,
  verifyPassword,
};
