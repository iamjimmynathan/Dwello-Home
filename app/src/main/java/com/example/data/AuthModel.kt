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
 * Role-based navigation guard and post-login routing helper.
 * - TENANT lands on Tenant Home screen (HOME).
 * - LANDLORD lands on Landlord Dashboard (LANDLORD_DASHBOARD).
 * - Tenants cannot access landlord-only screens (LANDLORD_DASHBOARD, POST_HOUSE).
 * - Landlords cannot access tenant-only screens (HOME, FIND_HOUSES) without switching role.
 * - MESSAGES (Enquiries) and PROFILE are shared screens.
 */
object RoleNavigationRouter {
  fun postLoginDestination(role: AuthAccountRole): DwelloNavScreen {
    return when (role) {
      AuthAccountRole.TENANT -> DwelloNavScreen.HOME
      AuthAccountRole.LANDLORD -> DwelloNavScreen.LANDLORD_DASHBOARD
    }
  }

  fun canAccessScreen(role: AuthAccountRole, screen: DwelloNavScreen): Boolean {
    return when (screen) {
      DwelloNavScreen.WELCOME,
      DwelloNavScreen.ACCOUNT_TYPE_SELECTION,
      DwelloNavScreen.REGISTRATION,
      DwelloNavScreen.LOGIN,
      DwelloNavScreen.FORGOT_PASSWORD,
      DwelloNavScreen.MESSAGES,
      DwelloNavScreen.PROFILE -> true

      DwelloNavScreen.HOME,
      DwelloNavScreen.FIND_HOUSES -> role == AuthAccountRole.TENANT

      DwelloNavScreen.LANDLORD_DASHBOARD,
      DwelloNavScreen.POST_HOUSE -> role == AuthAccountRole.LANDLORD
    }
  }
}

/**
 * Clean backend authentication boundary for future integration.
 * Supports phone-based auth, duplicate phone enforcement, secure password hashing,
 * optional email storage, OTP verification, and session token management.
 */
sealed class AuthBackendResult {
  data class DemoValidationPassed(
    val normalizedPhone: String,
    val message: String
  ) : AuthBackendResult()

  data class BackendNotConnected(
    val reason: String
  ) : AuthBackendResult()
}

interface AuthBackendGateway {
  suspend fun registerWithPhone(
    fullName: String,
    normalizedPhone: String,
    email: String?,
    rawPasswordChars: CharArray,
    role: AuthAccountRole
  ): AuthBackendResult

  suspend fun loginWithPhone(
    normalizedPhone: String,
    rawPasswordChars: CharArray
  ): AuthBackendResult

  suspend fun requestPasswordResetOtp(
    normalizedPhone: String
  ): AuthBackendResult
}

/**
 * Frontend-only demonstration gateway.
 * Explicitly clears password char arrays and never stores passwords or fakes real auth.
 */
object DemoAuthGateway : AuthBackendGateway {
  override suspend fun registerWithPhone(
    fullName: String,
    normalizedPhone: String,
    email: String?,
    rawPasswordChars: CharArray,
    role: AuthAccountRole
  ): AuthBackendResult {
    rawPasswordChars.fill('\u0000')
    return AuthBackendResult.DemoValidationPassed(
      normalizedPhone = normalizedPhone,
      message = "Demonstration UI only: backend registration, duplicate phone checks, and OTP verification are not connected yet."
    )
  }

  override suspend fun loginWithPhone(
    normalizedPhone: String,
    rawPasswordChars: CharArray
  ): AuthBackendResult {
    rawPasswordChars.fill('\u0000')
    return AuthBackendResult.DemoValidationPassed(
      normalizedPhone = normalizedPhone,
      message = "Demonstration UI only: real password verification and session management require backend integration."
    )
  }

  override suspend fun requestPasswordResetOtp(
    normalizedPhone: String
  ): AuthBackendResult {
    return AuthBackendResult.BackendNotConnected(
      reason = "Password recovery and SMS/OTP delivery will become available after connecting the backend and SMS provider."
    )
  }
}
