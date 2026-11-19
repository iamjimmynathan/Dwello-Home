package com.example

import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthFormValidator
import com.example.data.DwelloApiResult
import com.example.data.DwelloMappers
import com.example.data.DwelloRepository
import com.example.data.DwelloSupportConfig
import com.example.data.EnquiryItem
import com.example.data.EnquiryStatus
import com.example.data.InMemoryPropertyImageDataSource
import com.example.data.InMemorySessionTokenStore
import com.example.data.NigerianPhoneNormalizer
import com.example.data.PreparedUploadImage
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyFormValidator
import com.example.data.PropertyImageDataSource
import com.example.data.PropertyItem
import com.example.data.PropertyPhotoValidator
import com.example.data.PropertySubmissionResult
import com.example.data.PropertyTypeSlugMapper
import com.example.data.RentalPeriod
import com.example.data.RoleNavigationRouter
import com.example.data.SamplePropertyData
import com.example.data.SelectedPhotoItem
import com.example.data.calculateLandlordStats
import com.example.network.DwelloNetworkClient
import com.example.network.EnquiryDto
import com.example.network.PropertyDto
import com.example.network.PropertyImageDto
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  private data class RecordedRequest(
    val method: String,
    val path: String,
    val authHeader: String?,
    val body: String
  )

  private fun createMockedRepo(
    tokenStore: InMemorySessionTokenStore = InMemorySessionTokenStore(),
    imageDataSource: PropertyImageDataSource = InMemoryPropertyImageDataSource(
      fallbackSampleBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
    ),
    handler: (RecordedRequest) -> Pair<Int, String>
  ): Pair<DwelloRepository, MutableList<RecordedRequest>> {
    val recorded = mutableListOf<RecordedRequest>()
    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val buffer = Buffer()
      req.body?.writeTo(buffer)
      val rec = RecordedRequest(
        method = req.method,
        path = req.url.encodedPath,
        authHeader = req.header("Authorization"),
        body = buffer.readUtf8()
      )
      recorded.add(rec)
      val (code, json) = handler(rec)
      Response.Builder()
        .request(req)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code in 200..299) "OK" else "Error")
        .body(json.toResponseBody("application/json".toMediaType()))
        .build()
    }
    val apiService = DwelloNetworkClient.createApiService(
      tokenStore = tokenStore,
      baseUrl = "https://dwellohomes-api.jimmynathan11.workers.dev/",
      extraInterceptors = listOf(interceptor)
    )
    return DwelloRepository(apiService, tokenStore, imageDataSource) to recorded
  }

  @Test
  fun testPropertyFormValidatorValidationRules() {
    val valid = PropertyFormValidator.validate(
      title = "Executive 3-Bedroom Flat in GRA",
      propertyType = "3-bedroom apartment",
      rentAmount = 850000L,
      bedrooms = 3,
      neighbourhood = "GRA",
      description = "Spacious all en-suite bedrooms with borehole water and prepaid meter.",
      photoCount = 2
    )
    assertTrue(valid.isValid)

    val missingTitle = PropertyFormValidator.validate(
      title = "",
      propertyType = "Mini flat",
      rentAmount = 300000L,
      bedrooms = 1,
      neighbourhood = "Ugbowo",
      description = "Nice mini flat",
      photoCount = 1
    )
    assertFalse(missingTitle.isValid)
    assertNotNull(missingTitle.titleError)

    val zeroRent = PropertyFormValidator.validate(
      title = "Self Con in Ekosodin",
      propertyType = "Self-contained",
      rentAmount = 0L,
      bedrooms = 1,
      neighbourhood = "Ekosodin",
      description = "Student self con",
      photoCount = 1
    )
    assertFalse(zeroRent.isValid)
    assertNotNull(zeroRent.rentError)
  }

  @Test
  fun testLandlordStatsCalculationFromLiveLists() {
    val props = listOf(
      PropertyItem(
        id = "1",
        titleEn = "A",
        titlePidgin = "A",
        categoryEn = "Mini flat",
        categoryPidgin = "Mini flat",
        area = "GRA",
        priceNaira = 400000L,
        priceFormatted = "₦400,000",
        bedrooms = 1,
        bathrooms = 1,
        landlordName = "Owner",
        descriptionEn = "Desc",
        descriptionPidgin = "Desc",
        neighborhoodNote = "GRA",
        approvalStatus = PropertyApprovalStatus.APPROVED
      ),
      PropertyItem(
        id = "2",
        titleEn = "B",
        titlePidgin = "B",
        categoryEn = "Self-contained",
        categoryPidgin = "Self-contained",
        area = "Ugbowo",
        priceNaira = 200000L,
        priceFormatted = "₦200,000",
        bedrooms = 1,
        bathrooms = 1,
        landlordName = "Owner",
        descriptionEn = "Desc",
        descriptionPidgin = "Desc",
        neighborhoodNote = "Ugbowo",
        approvalStatus = PropertyApprovalStatus.PENDING_APPROVAL
      ),
      PropertyItem(
        id = "3",
        titleEn = "C",
        titlePidgin = "C",
        categoryEn = "2-bedroom apartment",
        categoryPidgin = "2-bedroom apartment",
        area = "Sapele Road",
        priceNaira = 550000L,
        priceFormatted = "₦550,000",
        bedrooms = 2,
        bathrooms = 2,
        landlordName = "Owner",
        descriptionEn = "Desc",
        descriptionPidgin = "Desc",
        neighborhoodNote = "Sapele Road",
        approvalStatus = PropertyApprovalStatus.REJECTED,
        rejectionReason = "Unclear photos"
      ),
      PropertyItem(
        id = "4",
        titleEn = "D",
        titlePidgin = "D",
        categoryEn = "Duplex",
        categoryPidgin = "Duplex",
        area = "Airport Road",
        priceNaira = 1200000L,
        priceFormatted = "₦1,200,000",
        bedrooms = 4,
        bathrooms = 4,
        landlordName = "Owner",
        descriptionEn = "Desc",
        descriptionPidgin = "Desc",
        neighborhoodNote = "Airport Road",
        approvalStatus = PropertyApprovalStatus.UNAVAILABLE
      )
    )
    val enquiries = listOf(
      EnquiryItem(
        id = "e1",
        propertyId = "1",
        propertyTitle = "A",
        propertyArea = "GRA",
        propertyPrice = "₦400,000",
        tenantName = "Osas",
        preferredInspectionDate = "Saturday",
        message = "Interested",
        status = EnquiryStatus.NEW
      ),
      EnquiryItem(
        id = "e2",
        propertyId = "1",
        propertyTitle = "A",
        propertyArea = "GRA",
        propertyPrice = "₦400,000",
        tenantName = "Efe",
        preferredInspectionDate = "Sunday",
        message = "Inspection",
        status = EnquiryStatus.CONTACTED
      )
    )

    val stats = calculateLandlordStats(props, enquiries)
    assertEquals(4, stats.totalProperties)
    assertEquals(1, stats.approvedCount)
    assertEquals(1, stats.pendingCount)
    assertEquals(1, stats.rejectedCount)
    assertEquals(1, stats.unavailableCount)
    assertEquals(1, stats.newEnquiriesCount)
  }

  @Test
  fun testPropertyTypeSlugMapperAndMappersNeverExposePrivatePhone() {
    assertEquals("self_contained", PropertyTypeSlugMapper.toApiSlug("Self-contained"))
    assertEquals("mini_flat", PropertyTypeSlugMapper.toApiSlug("Mini Flat"))
    assertEquals("2_bedroom", PropertyTypeSlugMapper.toApiSlug("2 Bedroom"))
    assertEquals("2 Bedroom", PropertyTypeSlugMapper.toDisplayLabel("2_bedroom"))
    assertEquals("shared_room", PropertyTypeSlugMapper.toApiSlug("Shared Accommodation"))
    assertEquals("Shared Accommodation", PropertyTypeSlugMapper.toDisplayLabel("shared_room"))
    SamplePropertyData.propertyTypesList.forEach { label ->
      assertEquals(label, PropertyTypeSlugMapper.toDisplayLabel(PropertyTypeSlugMapper.toApiSlug(label)))
    }

    val dto = PropertyDto(
      id = "prop_100",
      landlord_id = "land_1",
      title = "Clean 2-Bedroom Flat",
      property_type = "2_bedroom",
      bedrooms = 2,
      bathrooms = 2,
      rent_amount = 650000L,
      rent_period = "yearly",
      description = "Borehole water, prepaid meter, fenced & gated compound",
      neighbourhood = "Ugbowo",
      full_address = "12 BDPA Estate, Ugbowo",
      landmark = "Near UNIBEN Main Gate",
      moderation_status = "approved",
      availability_status = "available",
      landlord_name = "Chief Eghosa"
    )
    val item = with(DwelloMappers) {
      dto.toPropertyItem(listOf(PropertyImageDto(id = "img1", image_url = "https://cdn.example.com/1.jpg")))
    }

    assertEquals(PropertyApprovalStatus.APPROVED, item.approvalStatus)
    assertEquals("₦650,000", item.priceFormatted)
    assertEquals("09155259017", item.phoneContact) // Intermediary Dwello line only
    assertEquals(listOf("https://cdn.example.com/1.jpg"), item.photoUris)
    assertTrue(item.hasBorehole)
    assertTrue(item.hasPrepaidMeter)

    val unavailableDto = dto.copy(availability_status = "unavailable")
    val unavailableItem = with(DwelloMappers) { unavailableDto.toPropertyItem() }
    assertEquals(PropertyApprovalStatus.UNAVAILABLE, unavailableItem.approvalStatus)

    val enquiryItem = with(DwelloMappers) {
      EnquiryDto(
        id = "enq_1",
        property_id = "prop_100",
        message = "Want to inspect on Saturday",
        preferred_inspection_date = "2026-12-05",
        status = "contacted",
        property_title = "Clean 2-Bedroom Flat",
        neighbourhood = "Ugbowo",
        tenant_name = "Osaro",
        rent_amount = 650000L
      ).toEnquiryItem()
    }
    assertEquals(EnquiryStatus.CONTACTED, enquiryItem.status)
    assertEquals("", enquiryItem.tenantPhone) // Never invent/expose private phone
  }

  @Test
  fun testOnlyTenantAndLandlordPublicRolesExistAndAccessGuardsHold() {
    val roles = AuthAccountRole.entries.map { it.name }
    assertEquals(2, roles.size)
    assertTrue(roles.contains("TENANT"))
    assertTrue(roles.contains("LANDLORD"))
    assertFalse(roles.contains("AGENT"))
    assertFalse(roles.contains("ADMIN"))

    assertTrue(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.WELCOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.HOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.LANDLORD_HOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.FIND_HOUSES))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.LANDLORD_DASHBOARD))
    assertTrue(RoleNavigationRouter.bottomNavScreensForRole(null).isEmpty())

    assertEquals(DwelloNavScreen.HOME, RoleNavigationRouter.postLoginDestination(AuthAccountRole.TENANT))
    assertEquals(DwelloNavScreen.LANDLORD_HOME, RoleNavigationRouter.postLoginDestination(AuthAccountRole.LANDLORD))

    assertEquals(
      listOf(
        DwelloNavScreen.HOME,
        DwelloNavScreen.FIND_HOUSES,
        DwelloNavScreen.MESSAGES,
        DwelloNavScreen.PROFILE
      ),
      RoleNavigationRouter.bottomNavScreensForRole(AuthAccountRole.TENANT)
    )
    assertEquals(
      listOf(
        DwelloNavScreen.LANDLORD_HOME,
        DwelloNavScreen.POST_HOUSE,
        DwelloNavScreen.LANDLORD_DASHBOARD,
        DwelloNavScreen.MESSAGES,
        DwelloNavScreen.PROFILE
      ),
      RoleNavigationRouter.bottomNavScreensForRole(AuthAccountRole.LANDLORD)
    )

    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.LANDLORD_HOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.LANDLORD_DASHBOARD))
    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.POST_HOUSE))

    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.HOME))
    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.FIND_HOUSES))

    assertFalse(RoleNavigationRouter.canApproveOrRejectListings(null))
    assertFalse(RoleNavigationRouter.canApproveOrRejectListings(AuthAccountRole.TENANT))
    assertFalse(RoleNavigationRouter.canApproveOrRejectListings(AuthAccountRole.LANDLORD))
  }

  @Test
  fun testNigerianPhoneNumberNormalizationAndAuthValidation() {
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("08031234567"))
    assertEquals("+2348031234567", NigerianPhoneNormalizer.normalizeToE164("+234 803 123 4567"))
    assertNull(NigerianPhoneNormalizer.normalizeToE164("01234567890"))

    val regValid = AuthFormValidator.validateRegistration(
      fullName = "Osaro Ighodaro",
      phoneNumber = "0803 123 4567",
      email = "",
      password = "Password123",
      confirmPassword = "Password123",
      acceptedTerms = true,
      selectedRole = AuthAccountRole.TENANT,
      language = AppLanguage.ENGLISH
    )
    assertTrue(regValid.isValid)
    assertEquals("+2348031234567", regValid.normalizedPhone)
  }

  @Test
  fun testLiveAuthRegisterAndLoginChainsEndpointsStoresTokenAndClearsPasswordBuffer() = runBlocking {
    val tokenStore = InMemorySessionTokenStore()
    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when (req.path) {
        "/api/auth/register" -> 201 to """
          {
            "success": true,
            "message": "Registration successful",
            "user": {
              "id": "usr_1",
              "full_name": "Osaro Ighodaro",
              "phone": "+2348031234567",
              "role": "tenant"
            }
          }
        """.trimIndent()
        "/api/auth/login" -> 200 to """
          {
            "success": true,
            "token": "live-bearer-token-abc",
            "expires_at": "2027-01-01T00:00:00Z",
            "user": {
              "id": "usr_1",
              "full_name": "Osaro Ighodaro",
              "phone": "+2348031234567",
              "role": "tenant"
            }
          }
        """.trimIndent()
        else -> 404 to """{"error":"Not found"}"""
      }
    }

    val passwordChars = "Password123".toCharArray()
    val result = repo.registerAndLogin(
      fullName = "Osaro Ighodaro",
      normalizedPhone = "+2348031234567",
      email = null,
      passwordChars = passwordChars,
      role = AuthAccountRole.TENANT
    )

    assertTrue("Password buffer must be wiped", passwordChars.all { it == '\u0000' })
    assertTrue(result is DwelloApiResult.Success)
    val user = (result as DwelloApiResult.Success).data
    assertEquals("usr_1", user.id)
    assertEquals(AuthAccountRole.TENANT, user.role)
    assertEquals("live-bearer-token-abc", tokenStore.getSessionToken())

    assertEquals(2, requests.size)
    assertEquals("/api/auth/register", requests[0].path)
    assertNull("Register must not attach Bearer token", requests[0].authHeader)
    assertEquals("/api/auth/login", requests[1].path)
    assertNull("Login must not attach Bearer token", requests[1].authHeader)
  }

  @Test
  fun testRestoreSessionLogoutAnd401ClearsToken() = runBlocking {
    val tokenStore = InMemorySessionTokenStore()
    tokenStore.saveSessionToken("valid-token-999", null)

    var nextMeStatus = 200
    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when (req.path) {
        "/api/auth/me" -> {
          if (nextMeStatus == 200) {
            200 to """
              {
                "success": true,
                "user": {
                  "id": "land_1",
                  "full_name": "Mr. Eghosa",
                  "phone": "+2348099988776",
                  "role": "landlord"
                }
              }
            """.trimIndent()
          } else {
            401 to """{"message": "Token expired"}"""
          }
        }
        "/api/auth/logout" -> 200 to """{"success": true, "message": "Logged out"}"""
        else -> 404 to "{}"
      }
    }

    val restored = repo.restoreSession()
    assertTrue(restored is DwelloApiResult.Success)
    assertEquals(AuthAccountRole.LANDLORD, (restored as DwelloApiResult.Success).data.role)
    assertEquals("Bearer valid-token-999", requests[0].authHeader)

    // Logout calls endpoint with Bearer token and clears tokenStore
    val logoutRes = repo.logout()
    assertTrue(logoutRes is DwelloApiResult.Success)
    assertEquals("/api/auth/logout", requests[1].path)
    assertEquals("Bearer valid-token-999", requests[1].authHeader)
    assertNull(tokenStore.getSessionToken())

    // 401 on restore clears stale token and flags isUnauthorized
    tokenStore.saveSessionToken("expired-token", null)
    nextMeStatus = 401
    val expiredRes = repo.restoreSession()
    assertTrue(expiredRes is DwelloApiResult.Error)
    assertTrue((expiredRes as DwelloApiResult.Error).isUnauthorized)
    assertNull(tokenStore.getSessionToken())
  }

  @Test
  fun testTenantPropertiesFavouritesAndEnquiriesIntegration() = runBlocking {
    val tokenStore = InMemorySessionTokenStore()
    tokenStore.saveSessionToken("tenant-token", null)

    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when {
        req.method == "GET" && req.path == "/api/properties" -> 200 to """
          {
            "success": true,
            "properties": [
              {
                "id": "p1",
                "title": "Modern 2-Bedroom in GRA",
                "property_type": "2_bedroom",
                "bedrooms": 2,
                "bathrooms": 2,
                "rent_amount": 750000,
                "rent_period": "yearly",
                "description": "Gated compound with borehole water",
                "neighbourhood": "GRA",
                "moderation_status": "approved",
                "availability_status": "available"
              },
              {
                "id": "p2",
                "title": "Hidden Pending Flat",
                "property_type": "mini_flat",
                "bedrooms": 1,
                "bathrooms": 1,
                "rent_amount": 250000,
                "rent_period": "yearly",
                "description": "Pending",
                "neighbourhood": "Ugbowo",
                "moderation_status": "pending_approval",
                "availability_status": "available"
              }
            ]
          }
        """.trimIndent()
        req.method == "GET" && req.path == "/api/properties/p1" -> 200 to """
          {
            "success": true,
            "property": {
              "id": "p1",
              "title": "Modern 2-Bedroom in GRA",
              "property_type": "2_bedroom",
              "bedrooms": 2,
              "bathrooms": 2,
              "rent_amount": 750000,
              "rent_period": "yearly",
              "description": "Gated compound with borehole water",
              "neighbourhood": "GRA",
              "moderation_status": "approved",
              "availability_status": "available"
            },
            "images": [{"id": "img1", "image_url": "https://cdn.example.com/p1.jpg"}]
          }
        """.trimIndent()
        req.method == "GET" && req.path == "/api/favourites" -> 200 to """
          {"success": true, "properties": []}
        """.trimIndent()
        req.method == "POST" && req.path == "/api/favourites/p1" -> 200 to """
          {"success": true, "message": "Added to favourites"}
        """.trimIndent()
        req.method == "DELETE" && req.path == "/api/favourites/p1" -> 200 to """
          {"success": true, "message": "Removed"}
        """.trimIndent()
        req.method == "POST" && req.path == "/api/enquiries" -> 201 to """
          {"success": true, "message": "Enquiry created", "enquiry_id": "enq_99"}
        """.trimIndent()
        req.method == "GET" && req.path == "/api/tenant/enquiries" -> 200 to """
          {
            "success": true,
            "enquiries": [
              {
                "id": "enq_99",
                "property_id": "p1",
                "message": "Saturday viewing",
                "preferred_inspection_date": "Saturday 11 AM",
                "status": "new",
                "property_title": "Modern 2-Bedroom in GRA",
                "neighbourhood": "GRA",
                "rent_amount": 750000
              }
            ]
          }
        """.trimIndent()
        else -> 404 to "{}"
      }
    }

    val props = (repo.getApprovedProperties() as DwelloApiResult.Success).data
    assertEquals(1, props.size)
    assertEquals("p1", props[0].id)

    val detail = (repo.getPropertyDetails("p1") as DwelloApiResult.Success).data
    assertEquals(listOf("https://cdn.example.com/p1.jpg"), detail.photoUris)

    assertTrue(repo.addFavourite("p1") is DwelloApiResult.Success)
    assertTrue(repo.removeFavourite("p1") is DwelloApiResult.Success)

    val enqSubmit = repo.submitEnquiry("p1", "Saturday viewing", "Saturday 11 AM") as DwelloApiResult.Success
    assertEquals("enq_99", enqSubmit.data)

    val tenantEnquiries = (repo.getTenantEnquiries() as DwelloApiResult.Success).data
    assertEquals(1, tenantEnquiries.size)
    assertEquals(EnquiryStatus.NEW, tenantEnquiries[0].status)
    assertTrue(requests.all { it.authHeader == "Bearer tenant-token" })
  }

  @Test
  fun testLandlordPropertyAndEnquiryEndpointsIntegration() = runBlocking {
    val tokenStore = InMemorySessionTokenStore()
    tokenStore.saveSessionToken("landlord-token", null)

    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when {
        req.method == "POST" && req.path == "/api/properties" -> 201 to """
          {
            "success": true,
            "message": "Submitted for approval",
            "property_id": "new_prop_1",
            "moderation_status": "pending_approval"
          }
        """.trimIndent()
        req.method == "GET" && req.path == "/api/landlord/properties" -> 200 to """
          {
            "success": true,
            "properties": [
              {
                "id": "new_prop_1",
                "title": "New Benin Flat",
                "property_type": "2_bedroom",
                "bedrooms": 2,
                "bathrooms": 2,
                "rent_amount": 500000,
                "rent_period": "yearly",
                "description": "Clean apartment",
                "neighbourhood": "Ugbowo",
                "moderation_status": "pending_approval",
                "availability_status": "available"
              }
            ]
          }
        """.trimIndent()
        req.method == "PATCH" && req.path == "/api/properties/new_prop_1" -> 200 to """
          {"success": true, "message": "Updated", "moderation_status": "pending_approval"}
        """.trimIndent()
        req.method == "PATCH" && req.path == "/api/properties/new_prop_1/availability" -> 200 to """
          {"success": true, "message": "Marked unavailable", "availability_status": "unavailable"}
        """.trimIndent()
        req.method == "GET" && req.path == "/api/landlord/enquiries" -> 200 to """
          {
            "success": true,
            "enquiries": [
              {
                "id": "enq_5",
                "property_id": "new_prop_1",
                "message": "Available this week?",
                "preferred_inspection_date": "Sunday 2 PM",
                "status": "new",
                "property_title": "New Benin Flat",
                "neighbourhood": "Ugbowo",
                "tenant_name": "Efe Omoregie",
                "rent_amount": 500000
              }
            ]
          }
        """.trimIndent()
        req.method == "PATCH" && req.path == "/api/landlord/enquiries/enq_5" -> 200 to """
          {"success": true, "message": "Status updated", "status": "contacted"}
        """.trimIndent()
        else -> 404 to "{}"
      }
    }

    val created = repo.createProperty(
      title = "New Benin Flat",
      propertyTypeDisplay = "2-bedroom apartment",
      bedrooms = 2,
      bathrooms = 2,
      rentAmount = 500000L,
      rentalPeriod = RentalPeriod.YEARLY,
      description = "Clean apartment",
      neighbourhood = "Ugbowo",
      fullAddress = "10 Technical College Road",
      landmark = "UNIBEN Gate"
    ) as DwelloApiResult.Success
    assertEquals("new_prop_1", created.data)

    val createReq = requests.first { it.path == "/api/properties" && it.method == "POST" }
    assertTrue(createReq.body.contains("\"property_type\":\"2_bedroom\""))
    assertTrue(createReq.body.contains("\"rent_period\":\"yearly\""))

    val listings = (repo.getLandlordProperties() as DwelloApiResult.Success).data
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, listings[0].approvalStatus)

    val updated = repo.updateProperty("new_prop_1", "Updated Flat", 520000L, "Ugbowo", "Updated desc", null, null)
    assertTrue(updated is DwelloApiResult.Success)

    val avail = repo.updatePropertyAvailability("new_prop_1", makeAvailable = false) as DwelloApiResult.Success
    assertEquals("unavailable", avail.data)

    val enqList = (repo.getLandlordEnquiries() as DwelloApiResult.Success).data
    assertEquals("Efe Omoregie", enqList[0].tenantName)

    val enqUpdate = repo.updateEnquiryStatus("enq_5", EnquiryStatus.CONTACTED) as DwelloApiResult.Success
    assertEquals(EnquiryStatus.CONTACTED, enqUpdate.data)
  }

  @Test
  fun testStepByStepFormValidationAndPhotoFileTypeValidation() {
    // Step 1
    val s1Invalid = PropertyFormValidator.validateStep1("", "", "   ")
    assertFalse(s1Invalid.isValid)
    assertNotNull(s1Invalid.titleError)
    assertNotNull(s1Invalid.typeError)
    assertNotNull(s1Invalid.descriptionError)
    assertTrue(PropertyFormValidator.validateStep1("Clean 2 Bedroom in GRA", "2 Bedroom", "Spacious compound").isValid)

    // Step 2
    val s2Invalid = PropertyFormValidator.validateStep2("", "", "")
    assertFalse(s2Invalid.isValid)
    assertNotNull(s2Invalid.cityError)
    assertNotNull(s2Invalid.neighbourhoodError)
    assertNotNull(s2Invalid.streetError)
    assertTrue(PropertyFormValidator.validateStep2("Benin City", "Ugbowo", "15 Technical College Road").isValid)

    // Step 3
    val s3Invalid = PropertyFormValidator.validateStep3(0L, 0, 0)
    assertFalse(s3Invalid.isValid)
    assertNotNull(s3Invalid.rentError)
    assertNotNull(s3Invalid.bedroomsError)
    assertNotNull(s3Invalid.bathroomsError)
    assertTrue(PropertyFormValidator.validateStep3(450000L, 2, 2).isValid)

    // Step 4 & Photo File Type Validation
    assertFalse(PropertyFormValidator.validateStep4(emptyList()).isValid)
    assertFalse(PropertyPhotoValidator.isSupportedImage("content://docs/lease.pdf", "application/pdf"))
    assertFalse(PropertyPhotoValidator.isSupportedImage("content://videos/tour.mp4", "video/mp4"))
    assertFalse(PropertyPhotoValidator.isSupportedImage("file:///storage/emulated/0/Download/terms.pdf", null))
    assertTrue(PropertyPhotoValidator.isSupportedImage("content://media/external/images/media/101.jpg", "image/jpeg"))
    assertTrue(PropertyPhotoValidator.isSupportedImage("file:///tmp/living_room.png", null))
    assertTrue(PropertyPhotoValidator.isSupportedImage("content://media/picker/0/com.android.providers.media.photopicker/media/55", null))

    val step4WithPdf = PropertyFormValidator.validateStep4(
      listOf(SelectedPhotoItem("content://docs/lease.pdf", "application/pdf"))
    )
    assertFalse(step4WithPdf.isValid)
    assertNotNull(step4WithPdf.photosError)

    val step4Valid = PropertyFormValidator.validateStep4(
      listOf(
        SelectedPhotoItem("content://media/1.jpg", "image/jpeg"),
        SelectedPhotoItem("content://media/2.png", "image/png")
      )
    )
    assertTrue(step4Valid.isValid)
  }

  @Test
  fun testPendingRejectedAndUnavailableListingsExcludedFromTenantApprovedSearch() = runBlocking {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("tenant-token", null)
    }
    val (repo, _) = createMockedRepo(tokenStore) { _ ->
      200 to """
        {
          "success": true,
          "properties": [
            {
              "id": "p_approved_avail",
              "title": "Approved Live 2 Bedroom",
              "property_type": "2_bedroom",
              "bedrooms": 2,
              "bathrooms": 2,
              "rent_amount": 600000,
              "rent_period": "yearly",
              "description": "Ready",
              "neighbourhood": "GRA",
              "moderation_status": "approved",
              "availability_status": "available"
            },
            {
              "id": "p_pending",
              "title": "Pending Submission",
              "property_type": "mini_flat",
              "bedrooms": 1,
              "bathrooms": 1,
              "rent_amount": 300000,
              "rent_period": "yearly",
              "description": "Waiting",
              "neighbourhood": "Ugbowo",
              "moderation_status": "pending_approval",
              "availability_status": "available"
            },
            {
              "id": "p_rejected",
              "title": "Rejected House",
              "property_type": "self_contained",
              "bedrooms": 1,
              "bathrooms": 1,
              "rent_amount": 200000,
              "rent_period": "yearly",
              "description": "Rejected",
              "neighbourhood": "Ekosodin",
              "moderation_status": "rejected",
              "availability_status": "available",
              "rejection_reason": "Blurry exterior photo"
            },
            {
              "id": "p_approved_unavail",
              "title": "Occupied Approved Flat",
              "property_type": "3_bedroom",
              "bedrooms": 3,
              "bathrooms": 3,
              "rent_amount": 900000,
              "rent_period": "yearly",
              "description": "Rented out",
              "neighbourhood": "Sapele Road",
              "moderation_status": "approved",
              "availability_status": "unavailable"
            }
          ]
        }
      """.trimIndent()
    }

    val result = repo.getApprovedProperties() as DwelloApiResult.Success
    assertEquals(listOf("p_approved_avail"), result.data.map { it.id })
  }

  @Test
  fun testPropertyImageUrlResolutionAndDisplayOrderSorting() {
    val base = "https://dwellohomes-api.jimmynathan11.workers.dev/"
    assertEquals(
      "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fp1%2Ffront.jpg",
      DwelloMappers.resolvePropertyImageUrl("/api/images/properties%2Fp1%2Ffront.jpg", null, base)
    )
    assertEquals(
      "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fp1%2Ffront.jpg",
      DwelloMappers.resolvePropertyImageUrl(null, "properties/p1/front.jpg", base)
    )
    assertEquals(
      "https://cdn.example.com/custom.webp",
      DwelloMappers.resolvePropertyImageUrl("https://cdn.example.com/custom.webp", "properties/p1/ignored.webp", base)
    )

    val dto = PropertyDto(
      id = "prop_sort",
      title = "Sorted House",
      property_type = "2_bedroom",
      bedrooms = 2,
      bathrooms = 2,
      rent_amount = 500000L,
      rent_period = "yearly",
      description = "Nice house",
      neighbourhood = "GRA",
      moderation_status = "approved",
      availability_status = "available",
      images = listOf(
        PropertyImageDto(id = "img_2", image_key = "properties/prop_sort/2.jpg", display_order = 2),
        PropertyImageDto(id = "img_0", image_url = "/api/images/properties%2Fprop_sort%2F0.jpg", display_order = 0),
        PropertyImageDto(id = "img_1", image_url = "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fprop_sort%2F1.jpg", display_order = 1)
      )
    )

    val mapped = with(DwelloMappers) { dto.toPropertyItem(baseUrl = base) }
    assertEquals(
      listOf(
        "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fprop_sort%2F0.jpg",
        "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fprop_sort%2F1.jpg",
        "https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fprop_sort%2F2.jpg"
      ),
      mapped.photoUris
    )
    assertEquals(listOf("img_0", "img_1", "img_2"), mapped.serverImages.map { it.id })

    val coverFallbackDto = dto.copy(
      images = emptyList(),
      cover_image_url = "/api/images/properties%2Fprop_sort%2Fcover.jpg"
    )
    val coverMapped = with(DwelloMappers) { coverFallbackDto.toPropertyItem(baseUrl = base) }
    assertEquals(
      listOf("https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fprop_sort%2Fcover.jpg"),
      coverMapped.photoUris
    )
  }

  @Test
  fun testCreatePropertyWithImagesUploadsMultipartSequentiallyAndSupportsRetryWithoutDuplicateProperty() = runBlocking {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("landlord-jwt", null)
    }
    var uploadAttempt = 0
    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when {
        req.method == "POST" && req.path == "/api/properties" -> 201 to """
          {"success": true, "property_id": "prop_img_10", "moderation_status": "pending_approval"}
        """.trimIndent()
        req.method == "POST" && req.path == "/api/properties/prop_img_10/images" -> {
          uploadAttempt++
          if (uploadAttempt == 2) {
            500 to """{"error": "Temporary R2 write failure"}"""
          } else {
            val order = if (uploadAttempt == 1) 0 else 1
            201 to """
              {
                "success": true,
                "image": {
                  "id": "img_$order",
                  "property_id": "prop_img_10",
                  "image_key": "properties/prop_img_10/photo_$order.jpg",
                  "image_url": "/api/images/properties%2Fprop_img_10%2Fphoto_$order.jpg",
                  "display_order": $order
                }
              }
            """.trimIndent()
          }
        }
        else -> 404 to "{}"
      }
    }

    val photos = listOf(
      SelectedPhotoItem("content://media/1", "image/jpeg", "front.jpg"),
      SelectedPhotoItem("content://media/2", "image/jpeg", "parlor.jpg")
    )

    val firstTry = repo.createPropertyWithImages(
      title = "2 Bedroom in GRA",
      propertyTypeDisplay = "2 Bedroom",
      bedrooms = 2,
      bathrooms = 2,
      rentAmount = 700000L,
      rentalPeriod = RentalPeriod.YEARLY,
      description = "Fenced and gated",
      neighbourhood = "GRA",
      fullAddress = "1 Golf Road",
      landmark = "Golf Course",
      photos = photos
    )

    assertTrue(firstTry is PropertySubmissionResult.MetadataSavedUploadFailed)
    val partial = firstTry as PropertySubmissionResult.MetadataSavedUploadFailed
    assertEquals("prop_img_10", partial.propertyId)
    assertEquals(1, partial.uploadedImages.size)
    assertEquals(1, partial.remainingPhotos.size)
    assertEquals(1, partial.nextDisplayOrder)

    val retry = repo.uploadRemainingPropertyImages(
      propertyId = partial.propertyId,
      photos = partial.remainingPhotos,
      startDisplayOrder = partial.nextDisplayOrder,
      alreadyUploaded = partial.uploadedImages
    )

    assertTrue(retry is PropertySubmissionResult.Success)
    val done = retry as PropertySubmissionResult.Success
    assertEquals(2, done.uploadedImages.size)
    assertEquals(1, requests.count { it.method == "POST" && it.path == "/api/properties" })
    assertEquals(3, requests.count { it.method == "POST" && it.path == "/api/properties/prop_img_10/images" })
    assertTrue(requests.filter { it.path == "/api/properties/prop_img_10/images" }.all {
      it.body.contains("name=\"image\"")
    })
  }

  @Test
  fun testListHydrationAndDeletePropertyImage() = runBlocking {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("landlord-jwt", null)
    }
    val (repo, requests) = createMockedRepo(tokenStore) { req ->
      when {
        req.method == "GET" && req.path == "/api/landlord/properties" -> 200 to """
          {
            "success": true,
            "properties": [
              {
                "id": "p_hydrate",
                "title": "Hydrated Flat",
                "property_type": "2_bedroom",
                "bedrooms": 2,
                "bathrooms": 2,
                "rent_amount": 500000,
                "rent_period": "yearly",
                "description": "Nice",
                "neighbourhood": "Ugbowo",
                "moderation_status": "pending_approval",
                "availability_status": "available"
              }
            ]
          }
        """.trimIndent()
        req.method == "GET" && req.path == "/api/properties/p_hydrate" -> 200 to """
          {
            "success": true,
            "property": {
              "id": "p_hydrate",
              "title": "Hydrated Flat",
              "property_type": "2_bedroom",
              "bedrooms": 2,
              "bathrooms": 2,
              "rent_amount": 500000,
              "rent_period": "yearly",
              "description": "Nice",
              "neighbourhood": "Ugbowo",
              "moderation_status": "pending_approval",
              "availability_status": "available"
            },
            "images": [
              {"id": "img_10", "image_key": "properties/p_hydrate/ext.jpg", "display_order": 0}
            ]
          }
        """.trimIndent()
        req.method == "DELETE" && req.path == "/api/properties/p_hydrate/images/img_10" -> 200 to """
          {"success": true, "message": "Image deleted"}
        """.trimIndent()
        else -> 404 to "{}"
      }
    }

    val listed = (repo.getLandlordProperties() as DwelloApiResult.Success).data
    assertEquals(
      listOf("https://dwellohomes-api.jimmynathan11.workers.dev/api/images/properties%2Fp_hydrate%2Fext.jpg"),
      listed[0].photoUris
    )
    assertEquals("img_10", listed[0].serverImages.first().id)

    val delRes = repo.deletePropertyImage("p_hydrate", "img_10")
    assertTrue(delRes is DwelloApiResult.Success)
    assertTrue(requests.any { it.method == "DELETE" && it.path == "/api/properties/p_hydrate/images/img_10" })
  }

  @Test
  fun testCustomerSupportContactUsesConfiguredPhoneNumber() {
    assertEquals("09155259017", DwelloSupportConfig.SUPPORT_PHONE)
    assertEquals("tel:09155259017", DwelloSupportConfig.SUPPORT_TEL_URI)
    assertEquals("https://wa.me/2349155259017", DwelloSupportConfig.SUPPORT_WHATSAPP_URI)
    val defaultProperty = PropertyItem(
      id = "prop_default",
      titleEn = "Test Flat",
      titlePidgin = "Test Flat",
      categoryEn = "2 Bedroom",
      categoryPidgin = "2 Bedroom",
      area = "GRA",
      priceNaira = 500000L,
      priceFormatted = "₦500,000",
      bedrooms = 2,
      bathrooms = 2,
      landlordName = "Owner",
      descriptionEn = "Clean",
      descriptionPidgin = "Clean",
      neighborhoodNote = "Quiet"
    )
    assertEquals("09155259017", defaultProperty.phoneContact)
  }
}
