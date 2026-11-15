package com.example

import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthFormValidator
import com.example.data.DwelloApiResult
import com.example.data.DwelloMappers
import com.example.data.DwelloRepository
import com.example.data.EnquiryItem
import com.example.data.EnquiryStatus
import com.example.data.InMemorySessionTokenStore
import com.example.data.NigerianPhoneNormalizer
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyFormValidator
import com.example.data.PropertyItem
import com.example.data.PropertyTypeSlugMapper
import com.example.data.RentalPeriod
import com.example.data.RoleNavigationRouter
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
    return DwelloRepository(apiService, tokenStore) to recorded
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
    assertEquals("mini_flat", PropertyTypeSlugMapper.toApiSlug("Mini flat"))
    assertEquals("2_bedroom", PropertyTypeSlugMapper.toApiSlug("2-bedroom apartment"))
    assertEquals("2-bedroom apartment", PropertyTypeSlugMapper.toDisplayLabel("2_bedroom"))

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
    assertEquals("0800 393 556", item.phoneContact) // Intermediary Dwello line only
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
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.FIND_HOUSES))
    assertFalse(RoleNavigationRouter.canAccessScreen(null, DwelloNavScreen.LANDLORD_DASHBOARD))

    assertEquals(DwelloNavScreen.HOME, RoleNavigationRouter.postLoginDestination(AuthAccountRole.TENANT))
    assertEquals(DwelloNavScreen.LANDLORD_DASHBOARD, RoleNavigationRouter.postLoginDestination(AuthAccountRole.LANDLORD))
    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.TENANT, DwelloNavScreen.LANDLORD_DASHBOARD))
    assertFalse(RoleNavigationRouter.canAccessScreen(AuthAccountRole.LANDLORD, DwelloNavScreen.FIND_HOUSES))
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
}
