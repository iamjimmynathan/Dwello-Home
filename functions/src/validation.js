"use strict";

const PUBLIC_ROLES = new Set(["tenant", "landlord"]);
const ALL_ROLES = new Set(["tenant", "landlord", "admin"]);

const PROPERTY_TYPES = new Set([
  "self_contained",
  "mini_flat",
  "1_bedroom",
  "2_bedroom",
  "3_bedroom",
  "duplex",
  "detached_house",
  "shared_room",
]);

const RENT_PERIODS = new Set(["monthly", "yearly"]);
const ENQUIRY_STATUSES = new Set(["new", "contacted", "closed"]);
const ALLOWED_IMAGE_CONTENT_TYPES = new Set([
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/webp",
  "image/heic",
  "image/heif",
]);
const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

function cleanString(value, maxLen = 4000) {
  if (typeof value !== "string") return "";
  return value.trim().slice(0, maxLen);
}

function sanitizeUserDto(userDoc) {
  return {
    id: userDoc.uid,
    full_name: userDoc.fullName,
    phone: userDoc.phoneE164,
    email: userDoc.email || null,
    role: userDoc.role,
    is_active: userDoc.isActive !== false,
    phone_verified: userDoc.phoneVerified ? 1 : 0,
  };
}

/**
 * Maps internal Firestore property doc to the REST DTO.
 * Strictly NEVER includes private landlord phone numbers.
 */
function sanitizePropertyDto(id, doc) {
  return {
    id,
    landlord_id: doc.landlordId,
    landlord_name: doc.landlordName || "Verified Property Owner",
    title: doc.title,
    property_type: doc.propertyType,
    bedrooms: Number(doc.bedrooms || 1),
    bathrooms: Number(doc.bathrooms || 1),
    rent_amount: Number(doc.rentAmount || 0),
    rent_period: doc.rentPeriod || "yearly",
    description: doc.description || "",
    city: doc.city || "Benin City",
    neighbourhood: doc.neighbourhood || "",
    full_address: doc.fullAddress || "",
    landmark: doc.landmark || null,
    facilities: Array.isArray(doc.facilities) ? doc.facilities : [],
    moderation_status: doc.moderationStatus || "pending_approval",
    availability_status: doc.availabilityStatus || "available",
    rejection_reason: doc.rejectionReason || null,
    created_at: toIsoString(doc.createdAt),
    updated_at: toIsoString(doc.updatedAt),
  };
}

function sanitizePropertyImageDto(id, doc) {
  return {
    id,
    property_id: doc.propertyId,
    image_url: doc.imageUrl,
    url: doc.imageUrl,
    storage_path: doc.storagePath,
    content_type: doc.contentType || null,
    sort_order: Number(doc.sortOrder || 0),
  };
}

function sanitizeEnquiryDto(id, doc) {
  return {
    id,
    property_id: doc.propertyId,
    tenant_id: doc.tenantId,
    landlord_id: doc.landlordId,
    property_title: doc.propertyTitle,
    neighbourhood: doc.neighbourhood,
    rent_amount: Number(doc.rentAmount || 0),
    tenant_name: doc.tenantName,
    preferred_inspection_date: doc.preferredInspectionDate || null,
    message: doc.message || null,
    status: doc.status || "new",
    created_at: toIsoString(doc.createdAt),
    updated_at: toIsoString(doc.updatedAt),
  };
}

function toIsoString(value) {
  if (!value) return null;
  if (typeof value === "string") return value;
  if (typeof value.toDate === "function") return value.toDate().toISOString();
  if (value instanceof Date) return value.toISOString();
  return null;
}

module.exports = {
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
  toIsoString,
};
