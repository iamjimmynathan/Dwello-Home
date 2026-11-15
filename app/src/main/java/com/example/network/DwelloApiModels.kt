package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiMessageResponse(
  val success: Boolean = false,
  val message: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
  val id: String,
  val full_name: String,
  val phone: String,
  val email: String? = null,
  val role: String,
  val phone_verified: Int? = null
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
  val full_name: String,
  val phone: String,
  val email: String? = null,
  val password: String,
  val role: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
  val success: Boolean = false,
  val message: String? = null,
  val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
  val phone: String,
  val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
  val success: Boolean = false,
  val message: String? = null,
  val token: String? = null,
  val expires_at: String? = null,
  val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class MeResponse(
  val success: Boolean = false,
  val message: String? = null,
  val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class PropertyDto(
  val id: String,
  val landlord_id: String? = null,
  val title: String,
  val property_type: String = "self_contained",
  val bedrooms: Int = 1,
  val bathrooms: Int = 1,
  val rent_amount: Long = 0L,
  val rent_period: String = "yearly",
  val description: String = "",
  val neighbourhood: String = "",
  val full_address: String? = null,
  val landmark: String? = null,
  val moderation_status: String? = null,
  val availability_status: String? = null,
  val rejection_reason: String? = null,
  val created_at: String? = null,
  val updated_at: String? = null,
  val landlord_name: String? = null
)

@JsonClass(generateAdapter = true)
data class PropertyImageDto(
  val id: String? = null,
  val image_url: String? = null,
  val url: String? = null
)

@JsonClass(generateAdapter = true)
data class PropertiesListResponse(
  val success: Boolean = false,
  val message: String? = null,
  val properties: List<PropertyDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PropertyDetailResponse(
  val success: Boolean = false,
  val message: String? = null,
  val property: PropertyDto? = null,
  val images: List<PropertyImageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CreatePropertyRequest(
  val title: String,
  val property_type: String,
  val bedrooms: Int,
  val bathrooms: Int,
  val rent_amount: Long,
  val rent_period: String,
  val description: String,
  val neighbourhood: String,
  val full_address: String,
  val landmark: String? = null
)

@JsonClass(generateAdapter = true)
data class CreatePropertyResponse(
  val success: Boolean = false,
  val message: String? = null,
  val property_id: String? = null,
  val moderation_status: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePropertyRequest(
  val title: String,
  val rent_amount: Long,
  val neighbourhood: String,
  val description: String,
  val full_address: String? = null,
  val landmark: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePropertyResponse(
  val success: Boolean = false,
  val message: String? = null,
  val moderation_status: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateAvailabilityRequest(
  val availability_status: String
)

@JsonClass(generateAdapter = true)
data class UpdateAvailabilityResponse(
  val success: Boolean = false,
  val message: String? = null,
  val availability_status: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateEnquiryRequest(
  val property_id: String,
  val message: String? = null,
  val preferred_inspection_date: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateEnquiryResponse(
  val success: Boolean = false,
  val message: String? = null,
  val enquiry_id: String? = null
)

@JsonClass(generateAdapter = true)
data class EnquiryDto(
  val id: String,
  val property_id: String,
  val message: String? = null,
  val preferred_inspection_date: String? = null,
  val status: String = "new",
  val created_at: String? = null,
  val property_title: String? = null,
  val neighbourhood: String? = null,
  val tenant_name: String? = null,
  val rent_amount: Long? = null
)

@JsonClass(generateAdapter = true)
data class EnquiriesListResponse(
  val success: Boolean = false,
  val message: String? = null,
  val enquiries: List<EnquiryDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UpdateEnquiryStatusRequest(
  val status: String
)

@JsonClass(generateAdapter = true)
data class UpdateEnquiryStatusResponse(
  val success: Boolean = false,
  val message: String? = null,
  val status: String? = null
)
