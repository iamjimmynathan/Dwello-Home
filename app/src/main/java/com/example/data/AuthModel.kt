package com.example.data

import com.example.DwelloNavScreen

/**
 * Public account roles in Dwello Homes.
 * Strictly limited to TENANT and LANDLORD.
 * No Agent, Broker, or public Administrator roles are permitted.
 */
enum class AuthAccountRole {
  TENANT,
  LANDLORD
}

/**
 * Clean representation of a user profile preview (never holds raw passwords).
 */
data class AuthUserPreview(
  val fullName: String,
  val normalizedPhone: String,
  val email: String? = null,
  val role: AuthAccountRole
)

/**
 * Normalized Nigerian phone number helper.
 * Consistently normalizes valid Nigerian mobile numbers to +234XXXXXXXXXX format
 * for future backend authentication, duplicate phone checking, and OTP delivery.
 */
object NigerianPhoneNormalizer {
  // Valid Nigerian mobile subscriber part: 10 digits starting with 7, 8, or 9
  private val SUBSCRIBER_REGEX = Regex("^[789]\\d{9}$")

  /**
   * Normalizes a Nigerian phone input into E.164 (+234XXXXXXXXXX) or returns null if invalid.
   * Accepts:
   * - Local format with leading 0: e.g. "08031234567", "0803 123 4567", "081-234-56789"
   * - 10-digit subscriber format (when +234 prefix is used): e.g. "8031234567"
   * - International format: e.g. "+2348031234567", "2348031234567", "+23408031234567"
   */
  fun normalizeToE164(rawInput: String): String? {
    val cleaned = rawInput.trim()
      .replace(" ", "")
      .replace("-", "")
      .replace("(", "")
      .replace(")", "")

    if (cleaned.isEmpty()) return null

    val subscriberDigits = when {
      cleaned.startsWith("+234") -> {
        val rest = cleaned.removePrefix("+234")
        if (rest.startsWith("0")) rest.removePrefix("0") else rest
      }
      cleaned.startsWith("234") -> {
        val rest = cleaned.removePrefix("234")
        if (rest.startsWith("0")) rest.removePrefix("0") else rest
      }
      cleaned.startsWith("0") -> cleaned.removePrefix("0")
      else -> cleaned
    }

    return if (SUBSCRIBER_REGEX.matches(subscriberDigits)) {
      "+234$subscriberDigits"
    } else {
      null
    }
  }

  fun isValidNigerianPhone(rawInput: String): Boolean {
    return normalizeToE164(rawInput) != null
  }
}

data class RegistrationValidationResult(
  val isValid: Boolean,
  val fullNameError: String? = null,
  val phoneError: String? = null,
  val emailError: String? = null,
  val passwordError: String? = null,
  val confirmPasswordError: String? = null,
  val termsError: String? = null,
  val roleError: String? = null,
  val normalizedPhone: String? = null
)

data class LoginValidationResult(
  val isValid: Boolean,
  val phoneError: String? = null,
  val passwordError: String? = null,
  val normalizedPhone: String? = null
)

data class ForgotPasswordValidationResult(
  val isValid: Boolean,
  val phoneError: String? = null,
  val normalizedPhone: String? = null
)

/**
 * Stateless validation logic for Stage 4 Registration, Login, and Forgot Password.
 * Never persists passwords or creates fake authenticated sessions.
 */
object AuthFormValidator {
  const val MIN_PASSWORD_LENGTH = 8
  private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

  fun validateRegistration(
    fullName: String,
    phoneNumber: String,
    email: String,
    password: String,
    confirmPassword: String,
    acceptedTerms: Boolean,
    selectedRole: AuthAccountRole?,
    language: AppLanguage = AppLanguage.ENGLISH
  ): RegistrationValidationResult {
    val trimmedName = fullName.trim()
    val fullNameErr = when {
      trimmedName.isEmpty() -> DwelloStrings.authErrorNameRequired(language)
      trimmedName.length < 2 -> DwelloStrings.authErrorNameTooShort(language)
      else -> null
    }

    val normalized = NigerianPhoneNormalizer.normalizeToE164(phoneNumber)
    val phoneErr = when {
      phoneNumber.trim().isEmpty() -> DwelloStrings.authErrorPhoneRequired(language)
      normalized == null -> DwelloStrings.authErrorPhoneInvalid(language)
      else -> null
    }

    val trimmedEmail = email.trim()
    val emailErr = if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matches(trimmedEmail)) {
      DwelloStrings.authErrorEmailInvalid(language)
    } else {
      null
    }

    val passwordErr = when {
      password.isEmpty() -> DwelloStrings.authErrorPasswordRequired(language)
      password.length < MIN_PASSWORD_LENGTH -> DwelloStrings.authErrorPasswordLength(language)
      else -> null
    }

    val confirmErr = when {
      confirmPassword.isEmpty() -> DwelloStrings.authErrorConfirmPasswordRequired(language)
      confirmPassword != password -> DwelloStrings.authErrorPasswordMismatch(language)
      else -> null
    }

    val termsErr = if (!acceptedTerms) {
      DwelloStrings.authErrorTermsRequired(language)
    } else {
      null
    }

    val roleErr = if (selectedRole == null) {
      DwelloStrings.authErrorRoleRequired(language)
    } else {
      null
    }

    val valid = fullNameErr == null &&
      phoneErr == null &&
      emailErr == null &&
      passwordErr == null &&
      confirmErr == null &&
      termsErr == null &&
      roleErr == null

    return RegistrationValidationResult(
      isValid = valid,
      fullNameError = fullNameErr,
      phoneError = phoneErr,
      emailError = emailErr,
      passwordError = passwordErr,
      confirmPasswordError = confirmErr,
      termsError = termsErr,
      roleError = roleErr,
      normalizedPhone = normalized
    )
  }

  fun validateLogin(
    phoneNumber: String,
    password: String,
    language: AppLanguage = AppLanguage.ENGLISH
  ): LoginValidationResult {
    val normalized = NigerianPhoneNormalizer.normalizeToE164(phoneNumber)
    val phoneErr = when {
      phoneNumber.trim().isEmpty() -> DwelloStrings.authErrorPhoneRequired(language)
      normalized == null -> DwelloStrings.authErrorPhoneInvalid(language)
      else -> null
    }

    val passwordErr = when {
      password.isEmpty() -> DwelloStrings.authErrorPasswordRequired(language)
      password.length < MIN_PASSWORD_LENGTH -> DwelloStrings.authErrorPasswordLength(language)
      else -> null
    }

    return LoginValidationResult(
      isValid = phoneErr == null && passwordErr == null,
      phoneError = phoneErr,
      passwordError = passwordErr,
      normalizedPhone = normalized
    )
  }

  fun validateForgotPassword(
    phoneNumber: String,
    language: AppLanguage = AppLanguage.ENGLISH
  ): ForgotPasswordValidationResult {
    val normalized = NigerianPhoneNormalizer.normalizeToE164(phoneNumber)
    val phoneErr = when {
      phoneNumber.trim().isEmpty() -> DwelloStrings.authErrorPhoneRequired(language)
      normalized == null -> DwelloStrings.authErrorPhoneInvalid(language)
      else -> null
    }

    return ForgotPasswordValidationResult(
      isValid = phoneErr == null,
      phoneError = phoneErr,
      normalizedPhone = normalized
    )
  }
}

/**
 * Role-based navigation guard and login-first access router.
 * - Unauthenticated users (role == null) may ONLY access Auth screens (WELCOME, ACCOUNT_TYPE_SELECTION, REGISTRATION, LOGIN, FORGOT_PASSWORD).
 * - Unauthenticated users cannot access HOME, LANDLORD_HOME, FIND_HOUSES, POST_HOUSE, MESSAGES, PROFILE, or LANDLORD_DASHBOARD.
 * - TENANT bottom nav (4 items): HOME, FIND_HOUSES, MESSAGES, PROFILE.
 *   Tenants cannot access LANDLORD_HOME, POST_HOUSE, or LANDLORD_DASHBOARD.
 * - LANDLORD bottom nav (5 items): LANDLORD_HOME, POST_HOUSE, LANDLORD_DASHBOARD (My Properties), MESSAGES, PROFILE.
 *   Landlords cannot access tenant HOME or FIND_HOUSES.
 * - Admin approval/rejection actions are never exposed to Tenant or Landlord accounts.
 */
object RoleNavigationRouter {
  fun isAuthScreen(screen: DwelloNavScreen): Boolean {
    return when (screen) {
      DwelloNavScreen.WELCOME,
      DwelloNavScreen.ACCOUNT_TYPE_SELECTION,
      DwelloNavScreen.REGISTRATION,
      DwelloNavScreen.LOGIN,
      DwelloNavScreen.FORGOT_PASSWORD -> true

      DwelloNavScreen.HOME,
      DwelloNavScreen.LANDLORD_HOME,
      DwelloNavScreen.FIND_HOUSES,
      DwelloNavScreen.POST_HOUSE,
      DwelloNavScreen.MESSAGES,
      DwelloNavScreen.PROFILE,
      DwelloNavScreen.LANDLORD_DASHBOARD -> false
    }
  }

  fun postLoginDestination(role: AuthAccountRole): DwelloNavScreen {
    return when (role) {
      AuthAccountRole.TENANT -> DwelloNavScreen.HOME
      AuthAccountRole.LANDLORD -> DwelloNavScreen.LANDLORD_HOME
    }
  }

  fun bottomNavScreensForRole(role: AuthAccountRole?): List<DwelloNavScreen> {
    return when (role) {
      AuthAccountRole.TENANT -> listOf(
        DwelloNavScreen.HOME,
        DwelloNavScreen.FIND_HOUSES,
        DwelloNavScreen.MESSAGES,
        DwelloNavScreen.PROFILE
      )
      AuthAccountRole.LANDLORD -> listOf(
        DwelloNavScreen.LANDLORD_HOME,
        DwelloNavScreen.POST_HOUSE,
        DwelloNavScreen.LANDLORD_DASHBOARD,
        DwelloNavScreen.MESSAGES,
        DwelloNavScreen.PROFILE
      )
      null -> emptyList()
    }
  }

  fun canAccessScreen(role: AuthAccountRole?, screen: DwelloNavScreen): Boolean {
    if (isAuthScreen(screen)) return true
    if (role == null) return false

    return when (screen) {
      DwelloNavScreen.MESSAGES,
      DwelloNavScreen.PROFILE -> true

      DwelloNavScreen.HOME,
      DwelloNavScreen.FIND_HOUSES -> role == AuthAccountRole.TENANT

      DwelloNavScreen.LANDLORD_HOME,
      DwelloNavScreen.LANDLORD_DASHBOARD,
      DwelloNavScreen.POST_HOUSE -> role == AuthAccountRole.LANDLORD

      DwelloNavScreen.WELCOME,
      DwelloNavScreen.ACCOUNT_TYPE_SELECTION,
      DwelloNavScreen.REGISTRATION,
      DwelloNavScreen.LOGIN,
      DwelloNavScreen.FORGOT_PASSWORD -> true
    }
  }

  fun canBrowseApprovedProperties(role: AuthAccountRole?): Boolean {
    return role == AuthAccountRole.TENANT
  }

  fun canAccessPropertyDetails(role: AuthAccountRole?): Boolean {
    return role == AuthAccountRole.TENANT
  }

  fun canSubmitPropertyEnquiry(role: AuthAccountRole?): Boolean {
    return role == AuthAccountRole.TENANT
  }

  fun canAccessLandlordManagement(role: AuthAccountRole?): Boolean {
    return role == AuthAccountRole.LANDLORD
  }

  fun canApproveOrRejectListings(role: AuthAccountRole?): Boolean {
    // Approval/rejection is exclusively reserved for Dwello Homes administrators outside public mobile roles.
    return false
  }

  fun redirectUnauthenticatedTarget(
    role: AuthAccountRole?,
    requestedScreen: DwelloNavScreen
  ): DwelloNavScreen {
    if (role == null && !isAuthScreen(requestedScreen)) {
      return DwelloNavScreen.WELCOME
    }
    return requestedScreen
  }
}
