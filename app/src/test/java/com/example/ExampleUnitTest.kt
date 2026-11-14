package com.example

import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthBackendResult
import com.example.data.AuthFormValidator
import com.example.data.DemoAuthGateway
import com.example.data.NigerianPhoneNormalizer
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyFormValidator
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
import com.example.data.RoleNavigationRouter
import com.example.data.SamplePropertyData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  // ==========================================
  // STAGE 3A TESTS: APPROVAL STATUS & STATS
  // ==========================================

  @Test
  fun testNewPropertyDefaultsToPendingApproval() {
    val initialPendingCount = SamplePropertyData.dynamicProperties.count {
      it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL
    }

    val newListing = PropertyItem(
      id = "test_prop_${System.currentTimeMillis()}",
      titleEn = "Test Flat in Uselu",
      titlePidgin = "Test Flat for Uselu",
      categoryEn = "2-bedroom apartment",
      categoryPidgin = "2-bedroom apartment",
      area = "Ugbowo",
      priceNaira = 350000L,
      priceFormatted = "₦350,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 2,
      bathrooms = 1,
      landlordName = "Mr. Osasere",
      descriptionEn = "Test description",
      descriptionPidgin = "Test description",
      neighborhoodNote = "Uselu market axis",
      approvalStatus = PropertyApprovalStatus.APPROVED // Submitting with APPROVED flag
    )

    // Act
    SamplePropertyData.addProperty(newListing)

    // Assert: Business rule strictly enforces PENDING_APPROVAL regardless of input
    val addedItem = SamplePropertyData.dynamicProperties.first { it.id == newListing.id }
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, addedItem.approvalStatus)
    assertNull(addedItem.rejectionReason)

    val updatedPendingCount = SamplePropertyData.dynamicProperties.count {
      it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL
    }
    assertEquals(initialPendingCount + 1, updatedPendingCount)
  }

  @Test
  fun testOnlyApprovedPropertiesAppearInPublicSearch() {
    val publicListings = SamplePropertyData.getApprovedPublicProperties()

    // Assert: None of the public listings can be PENDING, REJECTED, or UNAVAILABLE
    assertTrue(publicListings.isNotEmpty())
    publicListings.forEach { property ->
      assertEquals(
        "Public search must only contain APPROVED properties: ${property.titleEn}",
        PropertyApprovalStatus.APPROVED,
        property.approvalStatus
      )
    }

    // Verify rejected and pending items are not in public search
    val pendingIds = SamplePropertyData.dynamicProperties
      .filter { it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL }
      .map { it.id }
    val rejectedIds = SamplePropertyData.dynamicProperties
      .filter { it.approvalStatus == PropertyApprovalStatus.REJECTED }
      .map { it.id }

    val publicIds = publicListings.map { it.id }.toSet()
    pendingIds.forEach { assertFalse("Pending property $it must not be in public search", publicIds.contains(it)) }
    rejectedIds.forEach { assertFalse("Rejected property $it must not be in public search", publicIds.contains(it)) }
  }

  @Test
  fun testLandlordDashboardStatsCalculation() {
    val stats = SamplePropertyData.getLandlordStats(SamplePropertyData.CURRENT_LANDLORD_NAME)

    // Assert: Stats accurately tally all states
    assertTrue("Total properties should be >= 3", stats.totalProperties >= 3)
    assertTrue("Approved count should be >= 1", stats.approvedCount >= 1)
    assertTrue("Pending count should be >= 1", stats.pendingCount >= 1)
    assertTrue("Rejected count should be >= 1", stats.rejectedCount >= 1)
    assertTrue("Unavailable count should be >= 1", stats.unavailableCount >= 1)
    assertTrue("New enquiries count should be >= 1", stats.newEnquiriesCount >= 1)

    // Total must equal the sum of categories
    assertEquals(
      stats.totalProperties,
      stats.approvedCount + stats.pendingCount + stats.rejectedCount + stats.unavailableCount
    )
  }

  @Test
  fun testEditingRejectedPropertyResetsToPendingApproval() {
    val rejectedProperty = SamplePropertyData.dynamicProperties.firstOrNull {
      it.approvalStatus == PropertyApprovalStatus.REJECTED
    }
    assertNotNull("There should be a sample rejected property for testing", rejectedProperty)
    val propId = rejectedProperty!!.id
    assertNotNull(rejectedProperty.rejectionReason)

    // Act: Landlord edits the rejected listing
    val updateResult = SamplePropertyData.updatePropertyListing(
      propertyId = propId,
      newTitle = "Fixed Studio Self-Con Apartment",
      newPriceNaira = 230000L,
      newArea = "Sapele Road",
      newDescription = "Updated with clear compound exterior pictures",
      newRentalTerms = "1 Year rent in advance"
    )

    // Assert: Edit succeeded and status reset to PENDING_APPROVAL with cleared rejection reason
    assertTrue(updateResult)
    val updatedProp = SamplePropertyData.dynamicProperties.first { it.id == propId }
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, updatedProp.approvalStatus)
    assertNull(updatedProp.rejectionReason)
    assertEquals("Fixed Studio Self-Con Apartment", updatedProp.titleEn)
  }

  @Test
  fun testToggleListingAvailability() {
    val approvedProp = SamplePropertyData.dynamicProperties.first {
      it.approvalStatus == PropertyApprovalStatus.APPROVED && it.landlordName == SamplePropertyData.CURRENT_LANDLORD_NAME
    }
    val propId = approvedProp.id

    // Act 1: Landlord marks it as unavailable
    val newStatus = SamplePropertyData.toggleListingAvailability(propId)
    assertEquals(PropertyApprovalStatus.UNAVAILABLE, newStatus)

    // Verify it is immediately excluded from public search
    val publicAfterMark = SamplePropertyData.getApprovedPublicProperties()
    assertFalse("Unavailable property must not appear in public search", publicAfterMark.any { it.id == propId })

    // Act 2: Landlord marks it as available again
    val restoredStatus = SamplePropertyData.toggleListingAvailability(propId)
    assertEquals(PropertyApprovalStatus.APPROVED, restoredStatus)

    val publicAfterRestore = SamplePropertyData.getApprovedPublicProperties()
    assertTrue("Restored property must appear in public search", publicAfterRestore.any { it.id == propId })
  }

  // ==========================================
  // STAGE 3B TESTS: POST A HOUSE WORKFLOW
  // ==========================================

  @Test
  fun testValidPropertySubmissionWorkflow() {
    // 1. Validate form fields using PropertyFormValidator
    val validation = PropertyFormValidator.validate(
      title = "Executive 3-Bedroom Flat in GRA",
      propertyType = "3-bedroom apartment",
      rentAmount = 850000L,
      bedrooms = 3,
      neighbourhood = "GRA",
      description = "Spacious all en-suite bedrooms with borehole water and prepaid meter.",
      photoCount = 3
    )
    assertTrue("Form validation must pass for complete and valid fields", validation.isValid)
    assertNull(validation.titleError)
    assertNull(validation.typeError)
    assertNull(validation.rentError)
    assertNull(validation.neighbourhoodError)
    assertNull(validation.descriptionError)
    assertNull(validation.photosError)

    // 2. Submit property to repository
    val propId = "test_stage3b_${System.currentTimeMillis()}"
    val newProp = PropertyItem(
      id = propId,
      titleEn = "Executive 3-Bedroom Flat in GRA",
      titlePidgin = "Executive 3-Bedroom Flat in GRA",
      categoryEn = "3-bedroom apartment",
      categoryPidgin = "3-bedroom apartment",
      area = "GRA",
      priceNaira = 850000L,
      priceFormatted = "₦850,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 3,
      bathrooms = 3,
      landlordName = SamplePropertyData.CURRENT_LANDLORD_NAME,
      descriptionEn = "Spacious all en-suite bedrooms with borehole water and prepaid meter.",
      descriptionPidgin = "Spacious all en-suite bedrooms with borehole water and prepaid meter.",
      neighborhoodNote = "Boundary Road, GRA",
      photoUris = listOf("content://media/external/images/media/101"),
      facilities = listOf("Water supply", "Electricity", "Security"),
      approvalStatus = PropertyApprovalStatus.APPROVED, // Must be forced to PENDING_APPROVAL
      submissionTimestamp = System.currentTimeMillis()
    )

    SamplePropertyData.addProperty(newProp)

    // 3. Verify newly submitted property defaulted to PENDING_APPROVAL
    val submittedProp = SamplePropertyData.dynamicProperties.first { it.id == propId }
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, submittedProp.approvalStatus)
    assertEquals(SamplePropertyData.CURRENT_LANDLORD_NAME, submittedProp.landlordName)
    assertTrue(submittedProp.photoUris.isNotEmpty())

    // 4. Verify newly submitted property appears in Landlord Dashboard
    val landlordProps = SamplePropertyData.getLandlordProperties()
    assertTrue("Newly submitted property must appear in Landlord's list", landlordProps.any { it.id == propId })

    // 5. Verify pending property remains invisible in public tenant search
    val publicProps = SamplePropertyData.getApprovedPublicProperties()
    assertFalse("Pending property must not appear in public tenant search", publicProps.any { it.id == propId })
  }

  @Test
  fun testMissingRequiredFieldsValidation() {
    // Missing title
    val missingTitle = PropertyFormValidator.validate(
      title = "",
      propertyType = "Mini flat",
      rentAmount = 300000L,
      bedrooms = 1,
      neighbourhood = "Ugbowo",
      description = "Nice mini flat",
      photoCount = 1
    )
    assertFalse("Validation should fail when title is blank", missingTitle.isValid)
    assertNotNull("Title error must be present", missingTitle.titleError)

    // Missing neighbourhood
    val missingNeighbourhood = PropertyFormValidator.validate(
      title = "Decent Mini Flat",
      propertyType = "Mini flat",
      rentAmount = 300000L,
      bedrooms = 1,
      neighbourhood = "",
      description = "Nice mini flat",
      photoCount = 1
    )
    assertFalse("Validation should fail when neighbourhood is empty", missingNeighbourhood.isValid)
    assertNotNull("Neighbourhood error must be present", missingNeighbourhood.neighbourhoodError)

    // Missing description
    val missingDesc = PropertyFormValidator.validate(
      title = "Decent Mini Flat",
      propertyType = "Mini flat",
      rentAmount = 300000L,
      bedrooms = 1,
      neighbourhood = "Airport Road",
      description = "   ",
      photoCount = 1
    )
    assertFalse("Validation should fail when description is blank", missingDesc.isValid)
    assertNotNull("Description error must be present", missingDesc.descriptionError)

    // Missing photo (photoCount = 0)
    val missingPhotos = PropertyFormValidator.validate(
      title = "Decent Mini Flat",
      propertyType = "Mini flat",
      rentAmount = 300000L,
      bedrooms = 1,
      neighbourhood = "Airport Road",
      description = "Nice mini flat",
      photoCount = 0
    )
    assertFalse("Validation should fail when no photo is selected", missingPhotos.isValid)
    assertNotNull("Photos error must be present", missingPhotos.photosError)
  }

  @Test
  fun testInvalidRentAmountValidation() {
    // Rent = 0
    val zeroRent = PropertyFormValidator.validate(
      title = "Self Con in Ekosodin",
      propertyType = "Self-contained",
      rentAmount = 0L,
      bedrooms = 1,
      neighbourhood = "Ekosodin",
      description = "Student self con",
      photoCount = 1
    )
    assertFalse("Validation must fail when rent amount is 0", zeroRent.isValid)
    assertNotNull("Rent error must be present", zeroRent.rentError)

    // Rent negative
    val negativeRent = PropertyFormValidator.validate(
      title = "Self Con in Ekosodin",
      propertyType = "Self-contained",
      rentAmount = -50000L,
      bedrooms = 1,
      neighbourhood = "Ekosodin",
      description = "Student self con",
      photoCount = 1
    )
    assertFalse("Validation must fail when rent amount is negative", negativeRent.isValid)
    assertNotNull("Rent error must be present", negativeRent.rentError)
  }

  @Test
  fun testExistingApprovedPropertiesRemainVisible() {
    val initialApproved = SamplePropertyData.getApprovedPublicProperties()
    val approvedCount = initialApproved.size
    assertTrue("Initial approved public properties must not be empty", approvedCount > 0)

    // Submit a pending property
    val pendingProp = PropertyItem(
      id = "temp_pending_${System.currentTimeMillis()}",
      titleEn = "New Pending Flat",
      titlePidgin = "New Pending Flat",
      categoryEn = "2-bedroom apartment",
      categoryPidgin = "2-bedroom apartment",
      area = "Ogida",
      priceNaira = 400000L,
      priceFormatted = "₦400,000",
      bedrooms = 2,
      bathrooms = 1,
      landlordName = SamplePropertyData.CURRENT_LANDLORD_NAME,
      descriptionEn = "Pending verification flat",
      descriptionPidgin = "Pending verification flat",
      neighborhoodNote = "Near Siluko",
      approvalStatus = PropertyApprovalStatus.PENDING_APPROVAL
    )
    SamplePropertyData.addProperty(pendingProp)

    // Verify existing approved properties are still visible and unchanged
    val approvedAfter = SamplePropertyData.getApprovedPublicProperties()
    assertEquals("Approved public properties count must remain unchanged after adding pending listing", approvedCount, approvedAfter.size)
    assertFalse("Approved list must not contain the new pending property", approvedAfter.any { it.id == pendingProp.id })
  }

  // ==========================================
  // STAGE 4 TESTS: AUTH, ROLES & PHONE NORMALIZATION
  // ==========================================

  @Test
  fun testOnlyTenantAndLandlordPublicRolesExist() {
    val roles = AuthAccountRole.entries.map { it.name }
    assertEquals(2, roles.size)
    assertTrue(roles.contains("TENANT"))
    assertTrue(roles.contains("LANDLORD"))
    assertFalse(roles.contains("AGENT"))
    assertFalse(roles.contains("ADMIN"))
  }

  @Test
  fun testUnauthenticatedUsersAreBlockedFromPropertiesDetailsEnquiriesAndDashboards() {
    // Unauthenticated users (role == null) may ONLY access Auth screens
    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.WELCOME))
    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.ACCOUNT_TYPE_SELECTION))
    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.REGISTRATION))
    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.LOGIN))
    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.FORGOT_PASSWORD))

    // Unauthenticated users cannot access Home, Find Houses, Messages, Profile, Post House, or Landlord Dashboard
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.HOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.FIND_HOUSES))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.MESSAGES))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.PROFILE))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.POST_HOUSE))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.LANDLORD_DASHBOARD))

    // Unauthenticated users cannot browse houses, view property details, submit enquiries, or manage properties
    assertFalse(RoleNavigationRouter.canBrowseApprovedProperties(null))
    assertFalse(RoleNavigationRouter.canAccessPropertyDetails(null))
    assertFalse(RoleNavigationRouter.canSubmitPropertyEnquiry(null))
    assertFalse(RoleNavigationRouter.canAccessLandlordManagement(null))

    // Any unauthenticated navigation attempt to a protected route redirects to WELCOME
    assertEquals(
      DwelloNavScreen.WELCOME,
      RoleNavigationRouter.redirectUnauthenticatedTarget(null, DwelloNavScreen.FIND_HOUSES)
    )
    assertEquals(
      DwelloNavScreen.WELCOME,
      RoleNavigationRouter.redirectUnauthenticatedTarget(null, DwelloNavScreen.HOME)
    )
    assertEquals(
      DwelloNavScreen.WELCOME,
      RoleNavigationRouter.redirectUnauthenticatedTarget(null, DwelloNavScreen.LANDLORD_DASHBOARD)
    )
  }

  @Test
  fun testRoleBasedPostLoginRoutingAndAccessGuards() {
    // Post-login destinations
    assertEquals(DwelloNavScreen.HOME, RoleNavigationRouter.postLoginDestination(AuthAccountRole.TENANT))
    assertEquals(DwelloNavScreen.LANDLORD_DASHBOARD, RoleNavigationRouter.postLoginDestination(AuthAccountRole.LANDLORD))

    // Tenant access & capabilities
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.HOME))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.FIND_HOUSES))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.MESSAGES))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.PROFILE))
    assertTrue(RoleNavigationRouter.canBrowseApprovedProperties(AuthAccountRole.TENANT))
    assertTrue(RoleNavigationRouter.canAccessPropertyDetails(AuthAccountRole.TENANT))
    assertTrue(RoleNavigationRouter.canSubmitPropertyEnquiry(AuthAccountRole.TENANT))
    assertFalse(RoleNavigationRouter.canAccessLandlordManagement(AuthAccountRole.TENANT))
    assertFalse(
      "Tenants must NOT access Landlord Dashboard",
      RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.LANDLORD_DASHBOARD)
    )
    assertFalse(
      "Tenants must NOT access Post House",
      RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.POST_HOUSE)
    )

    // Tenant approved & available properties check
    val tenantSearchList = SamplePropertyData.getApprovedPublicProperties()
    assertTrue(tenantSearchList.isNotEmpty())
    assertTrue(tenantSearchList.all { it.approvalStatus == PropertyApprovalStatus.APPROVED })
    assertFalse(tenantSearchList.any { it.approvalStatus == PropertyApprovalStatus.UNAVAILABLE })

    // Landlord access & capabilities
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.LANDLORD_DASHBOARD))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.POST_HOUSE))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.MESSAGES))
    assertTrue(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.PROFILE))
    assertTrue(RoleNavigationRouter.canAccessLandlordManagement(AuthAccountRole.LANDLORD))
    assertFalse(RoleNavigationRouter.canBrowseApprovedProperties(AuthAccountRole.LANDLORD))
    assertFalse(
      "Landlords must NOT access Tenant Home without switching role",
      RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.HOME)
    )
    assertFalse(
      "Landlords must NOT access Find Houses without switching role",
      RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.FIND_HOUSES)
    )
  }

  @Test
  fun testNigerianPhoneNumberNormalization() {
    // Valid Nigerian mobile formats all normalize to +234XXXXXXXXXX
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("08031234567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("0803 123 4567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("8031234567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("+2348031234567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("2348031234567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("+23408031234567"))
    assertEquals("+2349012345678", NigerianPhoneNormalizer.normalizeToE164("0901-234-5678"))
    assertEquals("+2347065551234", NigerianPhoneNormalizer.normalizeToE164("0706 555 1234"))

    // Invalid phone numbers
    assertNull(NigerianPhoneNormalizer.normalizeToE164(""))
    assertNull(NigerianPhoneNormalizer.normalizeToE164("0803123"))
    assertNull(NigerianPhoneNormalizer.normalizeToE164("01234567890"))
    assertNull(NigerianPhoneNormalizer.normalizeToE164("+447911123456"))
    assertFalse(NigerianPhoneNormalizer.isValidNigerianPhone("12345"))
  }

  @Test
  fun testRegistrationValidationWithOptionalEmailAndPasswordRules() {
    // 1. Valid registration with blank optional email
    val validWithoutEmail = AuthFormValidator.validateRegistration(
      fullName = "Osaro Ighodaro",
      phoneNumber = "0803 123 4567",
      email = "",
      password = "Password123",
      confirmPassword = "Password123",
      acceptedTerms = true,
      selectedRole = AuthAccountRole.TENANT,
      language = AppLanguage.ENGLISH
    )
    assertTrue("Registration should succeed with optional email left blank", validWithoutEmail.isValid)
    assertEquals("+2348031234567", validWithoutEmail.normalizedPhone)
    assertNull(validWithoutEmail.emailError)

    // 2. Invalid non-empty email fails
    val invalidEmail = AuthFormValidator.validateRegistration(
      fullName = "Osaro Ighodaro",
      phoneNumber = "08031234567",
      email = "not-an-email",
      password = "Password123",
      confirmPassword = "Password123",
      acceptedTerms = true,
      selectedRole = AuthAccountRole.LANDLORD,
      language = AppLanguage.ENGLISH
    )
    assertFalse(invalidEmail.isValid)
    assertNotNull(invalidEmail.emailError)

    // 3. Password shorter than 8 characters & mismatch fail
    val shortAndMismatch = AuthFormValidator.validateRegistration(
      fullName = "Efe Omoregie",
      phoneNumber = "08123456789",
      email = "efe@example.com",
      password = "pass",
      confirmPassword = "different",
      acceptedTerms = false,
      selectedRole = null,
      language = AppLanguage.PIDGIN
    )
    assertFalse(shortAndMismatch.isValid)
    assertNotNull(shortAndMismatch.passwordError)
    assertNotNull(shortAndMismatch.confirmPasswordError)
    assertNotNull(shortAndMismatch.termsError)
    assertNotNull(shortAndMismatch.roleError)
  }

  @Test
  fun testLoginAndForgotPasswordValidationAndDemoGatewayHonesty() = runBlocking {
    // Login uses Nigerian phone + password (no email required)
    val validLogin = AuthFormValidator.validateLogin(
      phoneNumber = "+234 803 123 4567",
      password = "MySecretPassword",
      language = AppLanguage.ENGLISH
    )
    assertTrue(validLogin.isValid)
    assertEquals("+2348031234567", validLogin.normalizedPhone)

    val invalidLogin = AuthFormValidator.validateLogin(
      phoneNumber = "123",
      password = "123",
      language = AppLanguage.ENGLISH
    )
    assertFalse(invalidLogin.isValid)
    assertNotNull(invalidLogin.phoneError)
    assertNotNull(invalidLogin.passwordError)

    // Forgot password validation
    val forgotValid = AuthFormValidator.validateForgotPassword("08091234567")
    assertTrue(forgotValid.isValid)
    assertEquals("+2348091234567", forgotValid.normalizedPhone)

    val forgotInvalid = AuthFormValidator.validateForgotPassword("000")
    assertFalse(forgotInvalid.isValid)
    assertNotNull(forgotInvalid.phoneError)

    // DemoAuthGateway clears sensitive password CharArray and does not fake OTP delivery
    val regChars = "SecretPass1".toCharArray()
    val regResult = DemoAuthGateway.registerWithPhone(
      fullName = "Test User",
      normalizedPhone = "+2348031234567",
      email = null,
      rawPasswordChars = regChars,
      role = AuthAccountRole.TENANT
    )
    assertTrue(regResult is AuthBackendResult.DemoValidationPassed)
    assertTrue("Password buffer must be cleared after demo validation", regChars.all { it == '\u0000' })

    val otpResult = DemoAuthGateway.requestPasswordResetOtp("+2348031234567")
    assertTrue("OTP recovery must honestly report backend not connected", otpResult is AuthBackendResult.BackendNotConnected)
  }
}
