"use strict";

const SUBSCRIBER_REGEX = /^[789]\d{9}$/;

/**
 * Normalizes Nigerian mobile numbers to E.164 (+234XXXXXXXXXX).
 * Matches Android NigerianPhoneNormalizer behavior.
 */
function normalizeNigerianPhoneToE164(rawInput) {
  if (typeof rawInput !== "string") return null;
  const cleaned = rawInput
    .trim()
    .replace(/[\s\-()]/g, "");

  if (!cleaned) return null;

  let subscriberDigits;
  if (cleaned.startsWith("+234")) {
    const rest = cleaned.slice(4);
    subscriberDigits = rest.startsWith("0") ? rest.slice(1) : rest;
  } else if (cleaned.startsWith("234")) {
    const rest = cleaned.slice(3);
    subscriberDigits = rest.startsWith("0") ? rest.slice(1) : rest;
  } else if (cleaned.startsWith("0")) {
    subscriberDigits = cleaned.slice(1);
  } else {
    subscriberDigits = cleaned;
  }

  return SUBSCRIBER_REGEX.test(subscriberDigits) ? `+234${subscriberDigits}` : null;
}

module.exports = {
  normalizeNigerianPhoneToE164,
};
