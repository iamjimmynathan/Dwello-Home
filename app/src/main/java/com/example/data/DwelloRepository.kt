package com.example.data

import com.example.R
import com.example.network.CreateEnquiryRequest
import com.example.network.CreatePropertyRequest
import com.example.firebase.AndroidFirebaseSessionBridge
import com.example.firebase.DwelloBackendConfig
import com.example.firebase.FirebaseCustomTokenSessionBridge
import com.example.network.DwelloApiService
import com.example.network.DwelloNetworkClient
import com.example.network.EnquiryDto
import com.example.network.LoginRequest
import com.example.network.PropertyDto
import com.example.network.PropertyImageDto
import com.example.network.RegisterRequest
import com.example.network.UpdateAvailabilityRequest
import com.example.network.UpdateEnquiryStatusRequest
import com.example.network.UpdatePropertyRequest
import com.example.network.UserDto
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

data class AuthenticatedUser(
  val id: String,
  val fullName: String,
  val phone: String,
  val email: String? = null,
  val role: AuthAccountRole,
  val backendRoleRaw: String
)

sealed class DwelloApiResult<out T> {
  data class Success<T>(val data: T, val message: String? = null) : DwelloApiResult<T>()
  data class Error(
    val message: String,
    val httpCode: Int? = null,
    val isUnauthorized: Boolean = false
  ) : DwelloApiResult<Nothing>()
}

object PropertyTypeSlugMapper {
  private val displayToSlug = linkedMapOf(
    "Self-contained" to "self_contained",
    "Mini Flat" to "mini_flat",
    "1 Bedroom" to "1_bedroom",
    "2 Bedroom" to "2_bedroom",
    "3 Bedroom" to "3_bedroom",
    "Duplex" to "duplex",
    "Detached House" to "detached_house",
    "Shared Accommodation" to "shared_room"
  )

  private val aliasToSlug = mapOf(
    "mini flat" to "mini_flat",
    "1-bedroom apartment" to "1_bedroom",
    "2-bedroom apartment" to "2_bedroom",
    "3-bedroom apartment" to "3_bedroom",
    "detached house" to "detached_house",
    "shared accommodation" to "shared_room"
  )

  private val slugToDisplay = displayToSlug.entries.associate { (display, slug) -> slug to display }

  fun toApiSlug(displayLabel: String): String {
    val clean = displayLabel.trim()
    displayToSlug.entries.firstOrNull { it.key.equals(clean, ignoreCase = true) }?.let {
      return it.value
    }
    aliasToSlug[clean.lowercase(Locale.ROOT)]?.let { return it }
    return clean.lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
  }

  fun toDisplayLabel(slug: String): String {
    val clean = slug.trim().lowercase(Locale.ROOT)
    slugToDisplay[clean]?.let { return it }
    return clean.replace("_", " ").replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }
  }
}

object DwelloMappers {
  fun parsePublicRole(rawRole: String): AuthAccountRole? {
    return when (rawRole.trim().lowercase(Locale.ROOT)) {
      "tenant" -> AuthAccountRole.TENANT
      "landlord" -> AuthAccountRole.LANDLORD
      else -> null
    }
  }

  fun UserDto.toAuthenticatedUserOrNull(): AuthenticatedUser? {
    val parsedRole = parsePublicRole(role) ?: return null
    return AuthenticatedUser(
      id = id,
      fullName = full_name,
      phone = phone,
      email = email,
      role = parsedRole,
      backendRoleRaw = role
    )
  }

  fun formatNaira(amount: Long): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return "₦$formatted"
  }

  fun PropertyDto.toPropertyItem(images: List<PropertyImageDto> = emptyList()): PropertyItem {
    val displayType = PropertyTypeSlugMapper.toDisplayLabel(property_type)
    val period = if (rent_period.equals("monthly", ignoreCase = true)) {
      RentalPeriod.MONTHLY
    } else {
      RentalPeriod.YEARLY
    }

    val isAvailableFlag = availability_status.isNullOrBlank() || availability_status.equals("available", ignoreCase = true)
    val status = when {
      !isAvailableFlag -> PropertyApprovalStatus.UNAVAILABLE
      moderation_status.equals("approved", ignoreCase = true) -> PropertyApprovalStatus.APPROVED
      moderation_status.equals("rejected", ignoreCase = true) -> PropertyApprovalStatus.REJECTED
      else -> PropertyApprovalStatus.PENDING_APPROVAL
    }

    val availabilityLabel = when (status) {
      PropertyApprovalStatus.UNAVAILABLE -> "Unavailable / Occupied"
      PropertyApprovalStatus.APPROVED -> "Available for Inspection"
      PropertyApprovalStatus.REJECTED -> "Listing Not Approved"
      PropertyApprovalStatus.PENDING_APPROVAL -> "Under Review by Dwello Homes"
    }

    val formattedDate = created_at?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
      raw.substringBefore("T").ifBlank { raw }
    } ?: "Submitted"

    val note = listOfNotNull(
      landmark?.trim()?.takeIf { it.isNotEmpty() },
      full_address?.trim()?.takeIf { it.isNotEmpty() }
    ).firstOrNull() ?: if (neighbourhood.isNotBlank()) "$neighbourhood, Benin City" else "Benin City"

    val imageUrls = images.mapNotNull { it.image_url ?: it.url }
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val fallbackRes = when (property_type.lowercase(Locale.ROOT)) {
      "duplex", "detached_house", "3_bedroom" -> R.drawable.img_benin_house_modern_1794407034302
      "2_bedroom" -> R.drawable.img_benin_house_interior
      "mini_flat" -> R.drawable.img_benin_flat_interior_1794407048683
      else -> R.drawable.img_benin_house_exterior
    }

    val periodText = if (period == RentalPeriod.MONTHLY) "Monthly" else "1 Year"

    return PropertyItem(
      id = id,
      titleEn = title,
      titlePidgin = title,
      categoryEn = displayType,
      categoryPidgin = displayType,
      area = neighbourhood.ifBlank { "Benin City" },
      priceNaira = rent_amount,
      priceFormatted = formatNaira(rent_amount),
      rentalPeriod = period,
      bedrooms = bedrooms.coerceAtLeast(1),
      bathrooms = bathrooms.coerceAtLeast(1),
      isGated = description.contains("gate", ignoreCase = true) || description.contains("fenc", ignoreCase = true),
      hasBorehole = description.contains("water", ignoreCase = true) || description.contains("borehole", ignoreCase = true),
      hasPrepaidMeter = description.contains("prepaid", ignoreCase = true) || description.contains("meter", ignoreCase = true) || description.contains("electricity", ignoreCase = true),
      landlordName = landlord_name?.trim()?.takeIf { it.isNotEmpty() } ?: "Verified Property Owner",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556", // Private landlord phone is never exposed to tenants
      descriptionEn = description,
      descriptionPidgin = description,
      neighborhoodNote = note,
      fullAddress = full_address?.trim().orEmpty(),
      landmark = landmark?.trim().orEmpty(),
      imageResId = fallbackRes,
      imageResList = listOf(fallbackRes),
      photoUris = imageUrls,
      facilities = buildList {
        if (description.contains("water", ignoreCase = true)) add("Water")
        if (description.contains("borehole", ignoreCase = true)) add("Borehole")
        if (description.contains("electricity", ignoreCase = true) || description.contains("prepaid", ignoreCase = true)) add("Electricity")
        if (description.contains("parking", ignoreCase = true)) add("Parking")
        if (description.contains("security", ignoreCase = true) || description.contains("gate", ignoreCase = true)) add("Security")
        if (description.contains("kitchen", ignoreCase = true)) add("Kitchen")
        if (description.contains("tiled", ignoreCase = true)) add("Tiled Floor")
        if (description.contains("wardrobe", ignoreCase = true)) add("Wardrobe")
        if (isEmpty()) add("Verified Benin City Listing")
      },
      rentalTerms = "$periodText rent term. Physical inspection coordinated through Dwello Homes.",
      availabilityStatus = availabilityLabel,
      isAvailable = isAvailableFlag,
      approvalStatus = status,
      rejectionReason = rejection_reason?.takeIf { it.isNotBlank() },
      createdAtDisplay = formattedDate,
      isDemonstrationData = false
    )
  }

  fun EnquiryDto.toEnquiryItem(): EnquiryItem {
    val normalizedStatus = EnquiryStatus.fromApiValue(status)
    val (labelEn, labelPidgin) = when (normalizedStatus) {
      EnquiryStatus.NEW -> "New Enquiry" to "New Enquiry"
      EnquiryStatus.CONTACTED -> "Contacted" to "Dwello Don Contact"
      EnquiryStatus.CLOSED -> "Closed" to "Don Close"
    }

    return EnquiryItem(
      id = id,
      propertyId = property_id,
      propertyTitle = property_title?.takeIf { it.isNotBlank() } ?: "Dwello Property Listing",
      propertyArea = neighbourhood?.takeIf { it.isNotBlank() } ?: "Benin City",
      propertyPrice = rent_amount?.let { formatNaira(it) } ?: "",
      tenantName = tenant_name?.takeIf { it.isNotBlank() } ?: "Dwello Tenant",
      tenantPhone = "", // Never invent or expose private phone numbers
      preferredInspectionDate = preferred_inspection_date?.takeIf { it.isNotBlank() } ?: "Flexible",
      message = message?.trim().orEmpty(),
      createdAt = created_at?.trim().orEmpty(),
      status = normalizedStatus,
      statusEn = labelEn,
      statusPidgin = labelPidgin
    )
  }
}

open class DwelloRepository(
  private val apiService: DwelloApiService,
  private val tokenStore: SessionTokenStore,
  private val firebaseSessionBridge: FirebaseCustomTokenSessionBridge? = null
) {

  fun hasStoredSession(): Boolean {
    return !tokenStore.getSessionToken().isNullOrBlank()
  }

  fun clearLocalSession() {
    tokenStore.clearSession()
  }

  suspend fun restoreSession(): DwelloApiResult<AuthenticatedUser> {
    if (firebaseSessionBridge != null) {
      val refreshedIdToken = firebaseSessionBridge.getCurrentFirebaseIdToken(forceRefresh = false)
      if (!refreshedIdToken.isNullOrBlank()) {
        tokenStore.saveSessionToken(refreshedIdToken, tokenStore.getSessionExpiresAt())
      }
    }
    if (!hasStoredSession()) {
      return DwelloApiResult.Error("No active session.", httpCode = 401, isUnauthorized = true)
    }
    return safeApiCall(
      call = { apiService.getCurrentUser() },
      onSuccess = { body ->
        val dto = body.user
        val mapped = dto?.let { with(DwelloMappers) { it.toAuthenticatedUserOrNull() } }
        if (mapped == null) {
          tokenStore.clearSession()
          val roleDesc = dto?.role?.trim()?.takeIf { it.isNotEmpty() } ?: "missing"
          DwelloApiResult.Error(
            message = if (dto != null) {
              "Account verification required: account role '$roleDesc' is not valid. Please sign in with a verified Tenant or Landlord account."
            } else {
              "Unable to verify your Dwello account details. Please log in again."
            },
            httpCode = 403,
            isUnauthorized = false
          )
        } else {
          DwelloApiResult.Success(mapped, body.message)
        }
      }
    ).also { result ->
      if (result is DwelloApiResult.Error && result.isUnauthorized) {
        tokenStore.clearSession()
      }
    }
  }

  suspend fun registerAndLogin(
    fullName: String,
    normalizedPhone: String,
    email: String?,
    passwordChars: CharArray,
    role: AuthAccountRole
  ): DwelloApiResult<AuthenticatedUser> {
    val passwordStr = String(passwordChars)
    return try {
      val roleStr = when (role) {
        AuthAccountRole.TENANT -> "tenant"
        AuthAccountRole.LANDLORD -> "landlord"
      }
      val regResult = safeApiCall(
        call = {
          apiService.register(
            RegisterRequest(
              full_name = fullName.trim(),
              phone = normalizedPhone,
              email = email?.trim()?.takeIf { it.isNotEmpty() },
              password = passwordStr,
              role = roleStr
            )
          )
        },
        onSuccess = { body ->
          DwelloApiResult.Success(body, body.message)
        }
      )

      when (regResult) {
        is DwelloApiResult.Error -> regResult
        is DwelloApiResult.Success -> {
          val regBody = regResult.data
          val customToken = regBody.custom_token
          val userDto = regBody.user
          if (!customToken.isNullOrBlank() && firebaseSessionBridge != null) {
            val mappedUser = userDto?.let { with(DwelloMappers) { it.toAuthenticatedUserOrNull() } }
            if (mappedUser == null) {
              tokenStore.clearSession()
              val roleDesc = userDto?.role?.trim()?.ifEmpty { "missing" } ?: "missing"
              DwelloApiResult.Error(
                "Account verification required: account role '$roleDesc' is not valid. Please use a verified Tenant or Landlord account."
              )
            } else {
              val idToken = firebaseSessionBridge.exchangeCustomTokenForIdToken(customToken)
              tokenStore.saveSessionToken(idToken, null)
              DwelloApiResult.Success(mappedUser, regBody.message)
            }
          } else {
            // Cloudflare /api/auth/register creates the user without returning a token;
            // immediately log in to obtain and encrypt the Bearer session token.
            loginInternal(normalizedPhone, passwordStr)
          }
        }
      }
    } finally {
      passwordChars.fill('\u0000')
    }
  }

  suspend fun login(
    normalizedPhone: String,
    passwordChars: CharArray
  ): DwelloApiResult<AuthenticatedUser> {
    val passwordStr = String(passwordChars)
    return try {
      loginInternal(normalizedPhone, passwordStr)
    } finally {
      passwordChars.fill('\u0000')
    }
  }

  private suspend fun loginInternal(
    normalizedPhone: String,
    password: String
  ): DwelloApiResult<AuthenticatedUser> {
    return safeApiCall(
      call = {
        apiService.login(
          LoginRequest(
            phone = normalizedPhone,
            password = password
          )
        )
      },
      onSuccess = { body ->
        val customToken = body.custom_token
        val bearerToken = body.token
        val userDto = body.user
        val mappedUser = userDto?.let { with(DwelloMappers) { it.toAuthenticatedUserOrNull() } }
        val resolvedToken = when {
          !customToken.isNullOrBlank() && firebaseSessionBridge != null ->
            firebaseSessionBridge.exchangeCustomTokenForIdToken(customToken)
          !bearerToken.isNullOrBlank() -> bearerToken
          else -> null
        }

        if (resolvedToken.isNullOrBlank() || mappedUser == null) {
          tokenStore.clearSession()
          val msg = if (userDto != null && mappedUser == null) {
            val roleDesc = userDto.role.trim().ifEmpty { "missing" }
            "Account verification required: account role '$roleDesc' is not valid. Please use a verified Tenant or Landlord account."
          } else {
            body.message ?: "Login succeeded without valid session credentials."
          }
          DwelloApiResult.Error(msg)
        } else {
          tokenStore.saveSessionToken(resolvedToken, body.expires_at)
          DwelloApiResult.Success(mappedUser, body.message)
        }
      }
    )
  }

  suspend fun logout(): DwelloApiResult<Unit> {
    val hasToken = hasStoredSession()
    val remoteResult = if (hasToken) {
      safeApiCall(
        call = { apiService.logout() },
        onSuccess = { body -> DwelloApiResult.Success(Unit, body.message) }
      )
    } else {
      DwelloApiResult.Success(Unit)
    }
    firebaseSessionBridge?.signOutFirebase()
    tokenStore.clearSession()
    return remoteResult
  }

  suspend fun getApprovedProperties(): DwelloApiResult<List<PropertyItem>> {
    return safeApiCall(
      call = { apiService.getApprovedProperties() },
      onSuccess = { body ->
        val mapped = body.properties
          .map { with(DwelloMappers) { it.toPropertyItem() } }
          .filter { it.approvalStatus == PropertyApprovalStatus.APPROVED && it.isAvailable }
        DwelloApiResult.Success(mapped, body.message)
      }
    )
  }

  suspend fun getPropertyDetails(propertyId: String): DwelloApiResult<PropertyItem> {
    return safeApiCall(
      call = { apiService.getPropertyById(propertyId) },
      onSuccess = { body ->
        val propDto = body.property
        if (propDto == null) {
          DwelloApiResult.Error(body.message ?: "Property not found.")
        } else {
          val item = with(DwelloMappers) { propDto.toPropertyItem(body.images) }
          DwelloApiResult.Success(item, body.message)
        }
      }
    )
  }

  suspend fun getFavourites(): DwelloApiResult<List<PropertyItem>> {
    return safeApiCall(
      call = { apiService.getFavourites() },
      onSuccess = { body ->
        val mapped = body.properties.map { with(DwelloMappers) { it.toPropertyItem() } }
        DwelloApiResult.Success(mapped, body.message)
      }
    )
  }

  suspend fun addFavourite(propertyId: String): DwelloApiResult<String> {
    return safeApiCall(
      call = { apiService.addFavourite(propertyId) },
      onSuccess = { body ->
        DwelloApiResult.Success(body.message ?: "Property added to favourites", body.message)
      }
    )
  }

  suspend fun removeFavourite(propertyId: String): DwelloApiResult<String> {
    return safeApiCall(
      call = { apiService.removeFavourite(propertyId) },
      onSuccess = { body ->
        DwelloApiResult.Success(body.message ?: "Favourite removed", body.message)
      }
    )
  }

  suspend fun submitEnquiry(
    propertyId: String,
    message: String?,
    preferredInspectionDate: String?
  ): DwelloApiResult<String> {
    return safeApiCall(
      call = {
        apiService.submitEnquiry(
          CreateEnquiryRequest(
            property_id = propertyId,
            message = message?.trim()?.takeIf { it.isNotEmpty() },
            preferred_inspection_date = preferredInspectionDate?.trim()?.takeIf { it.isNotEmpty() }
          )
        )
      },
      onSuccess = { body ->
        DwelloApiResult.Success(
          data = body.enquiry_id ?: "",
          message = body.message ?: "Enquiry submitted successfully."
        )
      }
    )
  }

  suspend fun getTenantEnquiries(): DwelloApiResult<List<EnquiryItem>> {
    return safeApiCall(
      call = { apiService.getTenantEnquiries() },
      onSuccess = { body ->
        val mapped = body.enquiries.map { with(DwelloMappers) { it.toEnquiryItem() } }
        DwelloApiResult.Success(mapped, body.message)
      }
    )
  }

  suspend fun createProperty(
    title: String,
    propertyTypeDisplay: String,
    bedrooms: Int,
    bathrooms: Int,
    rentAmount: Long,
    rentalPeriod: RentalPeriod,
    description: String,
    neighbourhood: String,
    fullAddress: String,
    landmark: String?
  ): DwelloApiResult<String> {
    return safeApiCall(
      call = {
        apiService.createProperty(
          CreatePropertyRequest(
            title = title.trim(),
            property_type = PropertyTypeSlugMapper.toApiSlug(propertyTypeDisplay),
            bedrooms = bedrooms.coerceAtLeast(1),
            bathrooms = bathrooms.coerceAtLeast(1),
            rent_amount = rentAmount,
            rent_period = if (rentalPeriod == RentalPeriod.MONTHLY) "monthly" else "yearly",
            description = description.trim(),
            neighbourhood = neighbourhood.trim(),
            full_address = fullAddress.trim(),
            landmark = landmark?.trim()?.takeIf { it.isNotEmpty() }
          )
        )
      },
      onSuccess = { body ->
        DwelloApiResult.Success(
          data = body.property_id ?: "",
          message = body.message ?: "Property submitted for Dwello Homes review."
        )
      }
    )
  }

  suspend fun getLandlordProperties(): DwelloApiResult<List<PropertyItem>> {
    return safeApiCall(
      call = { apiService.getLandlordProperties() },
      onSuccess = { body ->
        val mapped = body.properties.map { with(DwelloMappers) { it.toPropertyItem() } }
        DwelloApiResult.Success(mapped, body.message)
      }
    )
  }

  suspend fun updateProperty(
    propertyId: String,
    title: String,
    rentAmount: Long,
    neighbourhood: String,
    description: String,
    fullAddress: String?,
    landmark: String?
  ): DwelloApiResult<String> {
    return safeApiCall(
      call = {
        apiService.updateProperty(
          propertyId = propertyId,
          request = UpdatePropertyRequest(
            title = title.trim(),
            rent_amount = rentAmount,
            neighbourhood = neighbourhood.trim(),
            description = description.trim(),
            full_address = fullAddress?.trim()?.takeIf { it.isNotEmpty() },
            landmark = landmark?.trim()?.takeIf { it.isNotEmpty() }
          )
        )
      },
      onSuccess = { body ->
        DwelloApiResult.Success(
          data = body.moderation_status ?: "pending_approval",
          message = body.message ?: "Property updated and returned for admin approval."
        )
      }
    )
  }

  suspend fun updatePropertyAvailability(
    propertyId: String,
    makeAvailable: Boolean
  ): DwelloApiResult<String> {
    val targetStatus = if (makeAvailable) "available" else "unavailable"
    return safeApiCall(
      call = {
        apiService.updatePropertyAvailability(
          propertyId = propertyId,
          request = UpdateAvailabilityRequest(availability_status = targetStatus)
        )
      },
      onSuccess = { body ->
        DwelloApiResult.Success(
          data = body.availability_status ?: targetStatus,
          message = body.message ?: "Property availability updated."
        )
      }
    )
  }

  suspend fun getLandlordEnquiries(): DwelloApiResult<List<EnquiryItem>> {
    return safeApiCall(
      call = { apiService.getLandlordEnquiries() },
      onSuccess = { body ->
        val mapped = body.enquiries.map { with(DwelloMappers) { it.toEnquiryItem() } }
        DwelloApiResult.Success(mapped, body.message)
      }
    )
  }

  suspend fun updateEnquiryStatus(
    enquiryId: String,
    status: EnquiryStatus
  ): DwelloApiResult<EnquiryStatus> {
    return safeApiCall(
      call = {
        apiService.updateEnquiryStatus(
          enquiryId = enquiryId,
          request = UpdateEnquiryStatusRequest(status = status.apiValue)
        )
      },
      onSuccess = { body ->
        val returned = body.status?.let { EnquiryStatus.fromApiValue(it) } ?: status
        DwelloApiResult.Success(returned, body.message)
      }
    )
  }

  private suspend fun <Dto, Out> safeApiCall(
    call: suspend () -> Response<Dto>,
    onSuccess: suspend (Dto) -> DwelloApiResult<Out>
  ): DwelloApiResult<Out> {
    return try {
      val response = call()
      if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
          onSuccess(body)
        } else {
          DwelloApiResult.Error("Empty response from server.", httpCode = response.code())
        }
      } else {
        val errMsg = parseErrorMessage(response)
        DwelloApiResult.Error(
          message = errMsg,
          httpCode = response.code(),
          isUnauthorized = response.code() == 401
        )
      }
    } catch (e: IOException) {
      DwelloApiResult.Error("Network error: please check your internet connection and try again.")
    } catch (e: Exception) {
      DwelloApiResult.Error(e.message ?: "Unexpected error communicating with Dwello Homes.")
    }
  }

  private fun parseErrorMessage(response: Response<*>): String {
    val rawError = runCatching { response.errorBody()?.string() }.getOrNull()
    if (!rawError.isNullOrBlank()) {
      val parsed = runCatching {
        val json = JSONObject(rawError)
        json.optString("message").takeIf { it.isNotBlank() }
          ?: json.optString("error").takeIf { it.isNotBlank() }
      }.getOrNull()
      if (!parsed.isNullOrBlank()) return parsed
    }
    return when (response.code()) {
      400 -> "Please check your details and try again."
      401 -> "Your session has expired. Please log in again."
      403 -> "You do not have permission to perform this action."
      404 -> "The requested item was not found."
      409 -> "This record already exists."
      in 500..599 -> "Dwello Homes server is temporarily unavailable. Please try again."
      else -> "Request failed (${response.code()})."
    }
  }

  companion object {
    fun createDefault(tokenStore: SessionTokenStore): DwelloRepository {
      val useFirebase = DwelloBackendConfig.isFirebaseApiActive()
      val apiService = DwelloNetworkClient.createApiService(
        tokenStore = tokenStore,
        baseUrl = DwelloBackendConfig.activeApiBaseUrl()
      )
      return DwelloRepository(
        apiService = apiService,
        tokenStore = tokenStore,
        firebaseSessionBridge = if (useFirebase) AndroidFirebaseSessionBridge() else null
      )
    }
  }
}
