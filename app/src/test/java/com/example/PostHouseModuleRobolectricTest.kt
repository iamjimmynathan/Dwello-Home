package com.example

import android.content.Context
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import com.example.data.DwelloRepository
import com.example.data.InMemoryPropertyImageDataSource
import com.example.data.InMemorySessionTokenStore
import com.example.network.DwelloNetworkClient
import com.example.ui.theme.MyApplicationTheme
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PostHouseModuleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Before
  fun configureOfflineCoilLoader() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tinyPng = byteArrayOf(
      0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
      0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
      0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
      0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4.toByte(),
      0x89.toByte(), 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
      0x54, 0x78, 0x9C.toByte(), 0x63, 0x00, 0x01, 0x00, 0x00,
      0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4.toByte(), 0x00,
      0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(),
      0x42, 0x60, 0x82.toByte()
    )
    val offlineClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(200)
          .message("OK")
          .body(tinyPng.toResponseBody("image/png".toMediaType()))
          .build()
      }
      .build()
    Coil.setImageLoader(
      ImageLoader.Builder(context)
        .okHttpClient(offlineClient)
        .build()
    )
  }

  private class FakeRegistryOwner : ActivityResultRegistryOwner {
    var nextMultiPickUris: List<Uri> = emptyList()
    var nextSinglePickUri: Uri? = null

    override val activityResultRegistry: ActivityResultRegistry = object : ActivityResultRegistry() {
      @Suppress("UNCHECKED_CAST")
      override fun <I, O> onLaunch(
        requestCode: Int,
        contract: ActivityResultContract<I, O>,
        input: I,
        options: ActivityOptionsCompat?
      ) {
        if (contract is androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia) {
          dispatchResult(requestCode, nextMultiPickUris as O)
        } else {
          dispatchResult(requestCode, nextSinglePickUri as O)
        }
      }
    }
  }

  private data class StoredListing(
    val id: String,
    val title: String,
    val propertyType: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val rentAmount: Long,
    val rentPeriod: String,
    val description: String,
    val neighbourhood: String,
    val fullAddress: String,
    val landmark: String,
    val moderationStatus: String,
    val availabilityStatus: String,
    val rejectionReason: String? = null,
    val createdAt: String = "2026-11-15T10:00:00Z"
  ) {
    fun toJson(images: List<StoredImage> = emptyList()): String {
      val obj = JSONObject()
      obj.put("id", id)
      obj.put("title", title)
      obj.put("property_type", propertyType)
      obj.put("bedrooms", bedrooms)
      obj.put("bathrooms", bathrooms)
      obj.put("rent_amount", rentAmount)
      obj.put("rent_period", rentPeriod)
      obj.put("description", description)
      obj.put("neighbourhood", neighbourhood)
      obj.put("full_address", fullAddress)
      obj.put("landmark", landmark)
      obj.put("moderation_status", moderationStatus)
      obj.put("availability_status", availabilityStatus)
      obj.put("created_at", createdAt)
      if (rejectionReason != null) {
        obj.put("rejection_reason", rejectionReason)
      }
      val imgArr = JSONArray()
      images.sortedBy { it.displayOrder }.forEach { imgArr.put(it.toJson()) }
      obj.put("images", imgArr)
      return obj.toString()
    }
  }

  private data class StoredImage(
    val id: String,
    val propertyId: String,
    val imageKey: String,
    val imageUrl: String,
    val displayOrder: Int,
    val createdAt: String = "2026-11-15T11:31:00Z"
  ) {
    fun toJson(): JSONObject {
      return JSONObject().apply {
        put("id", id)
        put("property_id", propertyId)
        put("image_key", imageKey)
        put("image_url", imageUrl)
        put("display_order", displayOrder)
        put("created_at", createdAt)
      }
    }
  }

  private class StatefulServer(
    var activeRole: String = "landlord"
  ) {
    val calledPaths = CopyOnWriteArrayList<String>()
    val propertyImages = ConcurrentHashMap<String, CopyOnWriteArrayList<StoredImage>>()
    val listings = CopyOnWriteArrayList<StoredListing>(
      listOf(
        StoredListing(
          id = "prop_approved_1",
          title = "Luxury 2 Bedroom in GRA",
          propertyType = "2_bedroom",
          bedrooms = 2,
          bathrooms = 2,
          rentAmount = 750000L,
          rentPeriod = "yearly",
          description = "Gated compound with Borehole water and Electricity",
          neighbourhood = "GRA",
          fullAddress = "12 Golf Course Road, GRA, Benin City",
          landmark = "Near Golf Course Road",
          moderationStatus = "approved",
          availabilityStatus = "available",
          createdAt = "2026-11-10T09:00:00Z"
        ),
        StoredListing(
          id = "prop_unavail_1",
          title = "Occupied 3 Bedroom in Sapele Road",
          propertyType = "3_bedroom",
          bedrooms = 3,
          bathrooms = 3,
          rentAmount = 900000L,
          rentPeriod = "yearly",
          description = "Already rented out",
          neighbourhood = "Sapele Road",
          fullAddress = "8 Country Home Road, Sapele Road, Benin City",
          landmark = "Country Home",
          moderationStatus = "approved",
          availabilityStatus = "unavailable",
          createdAt = "2026-11-08T12:00:00Z"
        ),
        StoredListing(
          id = "prop_rejected_1",
          title = "Rejected Room in Ekosodin",
          propertyType = "self_contained",
          bedrooms = 1,
          bathrooms = 1,
          rentAmount = 180000L,
          rentPeriod = "yearly",
          description = "Close to UNIBEN back gate",
          neighbourhood = "Ekosodin",
          fullAddress = "5 Ekosodin Main Street, Ekosodin, Benin City",
          landmark = "UNIBEN Back Gate",
          moderationStatus = "rejected",
          availabilityStatus = "available",
          rejectionReason = "Please provide clearer street landmark and interior room photos.",
          createdAt = "2026-11-12T14:30:00Z"
        )
      )
    )
    var createdCounter = 1
    var imageCounter = 1
  }

  private fun createRepository(
    tokenStore: InMemorySessionTokenStore,
    server: StatefulServer
  ): DwelloRepository {
    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val path = req.url.encodedPath
      val method = req.method
      val buf = Buffer()
      req.body?.writeTo(buf)
      val bodyText = buf.readUtf8()
      server.calledPaths.add("$method $path")

      val uploadImageMatch = Regex("^/api/properties/([^/]+)/images$").matchEntire(path)
      val deleteImageMatch = Regex("^/api/properties/([^/]+)/images/([^/]+)$").matchEntire(path)
      val propertyByIdMatch = Regex("^/api/properties/([^/]+)$").matchEntire(path)

      val (code, json) = when {
        method == "GET" && path == "/api/auth/me" -> {
          val role = server.activeRole
          200 to """
            {
              "success": true,
              "user": {
                "id": "usr_$role",
                "full_name": "${if (role == "landlord") "Chief Eghosa" else "Osaro Ighodaro"}",
                "phone": "${if (role == "landlord") "+2348099988776" else "+2348031234567"}",
                "role": "$role"
              }
            }
          """.trimIndent()
        }

        method == "POST" && path == "/api/properties" -> {
          val reqJson = JSONObject(bodyText)
          val newId = "prop_new_${server.createdCounter++}"
          val newListing = StoredListing(
            id = newId,
            title = reqJson.getString("title"),
            propertyType = reqJson.getString("property_type"),
            bedrooms = reqJson.getInt("bedrooms"),
            bathrooms = reqJson.getInt("bathrooms"),
            rentAmount = reqJson.getLong("rent_amount"),
            rentPeriod = reqJson.getString("rent_period"),
            description = reqJson.getString("description"),
            neighbourhood = reqJson.getString("neighbourhood"),
            fullAddress = reqJson.optString("full_address"),
            landmark = reqJson.optString("landmark"),
            moderationStatus = "pending_approval",
            availabilityStatus = "available",
            createdAt = "2026-11-15T11:30:00Z"
          )
          server.listings.add(0, newListing)
          201 to """
            {
              "success": true,
              "message": "Property submitted for Dwello Homes approval",
              "property_id": "$newId",
              "moderation_status": "pending_approval"
            }
          """.trimIndent()
        }

        method == "POST" && uploadImageMatch != null -> {
          val propId = uploadImageMatch.groupValues[1]
          val imgNum = server.imageCounter++
          val list = server.propertyImages.getOrPut(propId) { CopyOnWriteArrayList() }
          val orderMatch = Regex("name=\"display_order\"\\s*\\r?\\n\\r?\\n(\\d+)").find(bodyText)
          val displayOrder = orderMatch?.groupValues?.get(1)?.toIntOrNull() ?: list.size
          val key = "properties/$propId/photo_$imgNum.jpg"
          val url = "/api/images/properties%2F$propId%2Fphoto_$imgNum.jpg"
          val storedImg = StoredImage(
            id = "img_$imgNum",
            propertyId = propId,
            imageKey = key,
            imageUrl = url,
            displayOrder = displayOrder
          )
          list.add(storedImg)
          201 to JSONObject().apply {
            put("success", true)
            put("message", "Property image uploaded")
            put("image", storedImg.toJson())
          }.toString()
        }

        method == "DELETE" && deleteImageMatch != null -> {
          val propId = deleteImageMatch.groupValues[1]
          val imgId = deleteImageMatch.groupValues[2]
          server.propertyImages[propId]?.removeIf { it.id == imgId }
          200 to """{"success": true, "message": "Image removed"}"""
        }

        method == "PATCH" && propertyByIdMatch != null -> {
          val propId = propertyByIdMatch.groupValues[1]
          val reqJson = JSONObject(bodyText)
          val idx = server.listings.indexOfFirst { it.id == propId }
          if (idx >= 0) {
            val cur = server.listings[idx]
            server.listings[idx] = cur.copy(
              title = reqJson.optString("title", cur.title),
              rentAmount = if (reqJson.has("rent_amount")) reqJson.getLong("rent_amount") else cur.rentAmount,
              neighbourhood = reqJson.optString("neighbourhood", cur.neighbourhood),
              description = reqJson.optString("description", cur.description),
              fullAddress = reqJson.optString("full_address", cur.fullAddress),
              landmark = reqJson.optString("landmark", cur.landmark),
              moderationStatus = "pending_approval"
            )
            200 to """{"success": true, "message": "Listing updated", "moderation_status": "pending_approval"}"""
          } else {
            404 to """{"error": "Property not found"}"""
          }
        }

        method == "GET" && path == "/api/landlord/properties" -> {
          val arrayJson = server.listings.joinToString(",") { prop ->
            prop.toJson(server.propertyImages[prop.id].orEmpty())
          }
          200 to """{"success": true, "properties": [$arrayJson]}"""
        }

        method == "GET" && path == "/api/landlord/enquiries" -> {
          200 to """{"success": true, "enquiries": []}"""
        }

        method == "GET" && path == "/api/properties" -> {
          val approvedOnly = server.listings.filter {
            it.moderationStatus == "approved" && it.availabilityStatus == "available"
          }
          val arrayJson = approvedOnly.joinToString(",") { prop ->
            prop.toJson(server.propertyImages[prop.id].orEmpty())
          }
          200 to """{"success": true, "properties": [$arrayJson]}"""
        }

        method == "GET" && propertyByIdMatch != null -> {
          val propId = propertyByIdMatch.groupValues[1]
          val prop = server.listings.firstOrNull { it.id == propId }
          if (prop == null) {
            404 to """{"error": "Property not found"}"""
          } else {
            val imgs = server.propertyImages[propId].orEmpty().sortedBy { it.displayOrder }
            val imgArr = JSONArray().apply { imgs.forEach { put(it.toJson()) } }
            200 to """{"success": true, "property": ${prop.toJson(imgs)}, "images": $imgArr}"""
          }
        }

        method == "GET" && path == "/api/favourites" -> {
          200 to """{"success": true, "properties": []}"""
        }

        method == "GET" && path == "/api/tenant/enquiries" -> {
          200 to """{"success": true, "enquiries": []}"""
        }

        else -> 404 to """{"error": "Unhandled route $method $path"}"""
      }

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
    return DwelloRepository(
      apiService = apiService,
      tokenStore = tokenStore,
      imageDataSource = InMemoryPropertyImageDataSource(
        fallbackSampleBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
      )
    )
  }

  private fun waitForTag(tag: String) {
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun testFiveStepSubmissionValidationPhotoPreviewReplaceRemoveDashboardAndVisibilityRules() {
    val registryOwner = FakeRegistryOwner()
    val server = StatefulServer(activeRole = "landlord")
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("landlord-jwt", "2027-01-01T00:00:00Z")
    }
    val repo = createRepository(tokenStore, server)

    composeTestRule.setContent {
      CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
        MyApplicationTheme {
          DwelloApp(repository = repo)
        }
      }
    }

    // Wait for Landlord Home, then open My Properties tab (Landlord Dashboard)
    waitForTag("landlord_home_scroll")
    composeTestRule.onNodeWithTag("nav_item_my_properties").performClick()
    waitForTag("landlord_dashboard_scroll")
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("landlord_properties_loading").fetchSemanticsNodes().isEmpty()
    }

    // Verify Scenario 7: Rejected listing displays rejection reason, View Details, and NO Mark Unavailable button
    composeTestRule.onNodeWithTag("landlord_dashboard_scroll")
      .performScrollToNode(hasTestTag("landlord_property_card_prop_rejected_1"))
    composeTestRule.onNodeWithTag("landlord_property_submitted_date_prop_rejected_1").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("rejection_reason_box_prop_rejected_1").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_availability_button_prop_rejected_1").assertDoesNotExist()
    composeTestRule.onNodeWithTag("see_rejection_reason_button_prop_rejected_1").performScrollTo().performClick()
    waitForTag("landlord_property_details_dialog")
    assertTrue(
      composeTestRule.onAllNodesWithText("Please provide clearer street landmark and interior room photos.")
        .fetchSemanticsNodes().isNotEmpty()
    )
    composeTestRule.onNodeWithText("Close").performClick()

    // Navigate to Post a House wizard
    composeTestRule.onNodeWithTag("landlord_dashboard_scroll")
      .performScrollToNode(hasTestTag("add_new_property_button"))
    composeTestRule.onNodeWithTag("add_new_property_button").performClick()
    waitForTag("post_house_step_1")
    composeTestRule.onNodeWithText("Step 1 of 5").assertIsDisplayed()

    // Scenario 3: Empty required fields in Step 1 are rejected
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()
    waitForTag("title_error_text")
    composeTestRule.onNodeWithTag("description_error_text").performScrollTo().assertIsDisplayed()

    // Scenario 1: Complete Step 1 (Basic Information)
    composeTestRule.onNodeWithTag("property_title_input").performScrollTo()
      .performTextInput("Spacious 2 Bedroom Flat in Ugbowo")
    composeTestRule.onNodeWithTag("property_type_chip_2 Bedroom").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("property_description_input").performScrollTo()
      .performTextInput("Clean apartment with tiled floors and gated compound.")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()

    // Step 2: Location (prefilled Benin City) + empty validation
    waitForTag("post_house_step_2")
    composeTestRule.onNodeWithText("Step 2 of 5").assertIsDisplayed()
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()
    waitForTag("neighbourhood_error_text")
    composeTestRule.onNodeWithTag("street_error_text").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("neighbourhood_chip_Ugbowo").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("address_or_landmark_input").performScrollTo()
      .performTextInput("18 Technical College Road, near UNIBEN Gate")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()

    // Step 3: Rent and Facilities + empty rent validation
    waitForTag("post_house_step_3")
    composeTestRule.onNodeWithText("Step 3 of 5").assertIsDisplayed()
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()
    waitForTag("rent_error_text")

    composeTestRule.onNodeWithTag("rent_amount_input").performScrollTo().performTextInput("550000")
    composeTestRule.onNodeWithTag("period_yearly_btn").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("bedrooms_increase_btn").performScrollTo().performClick() // 2 beds
    composeTestRule.onNodeWithTag("bathrooms_increase_btn").performScrollTo().performClick() // 2 baths
    composeTestRule.onNodeWithTag("facility_checkbox_Borehole").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("facility_checkbox_Tiled Floor").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()

    // Step 4: Property Photos — reject empty, reject unsupported file type, add multiple, replace, remove
    waitForTag("post_house_step_4")
    composeTestRule.onNodeWithText("Step 4 of 5").assertIsDisplayed()
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()
    waitForTag("photos_error_text")

    // Reject unsupported file (.pdf)
    registryOwner.nextMultiPickUris = listOf(Uri.parse("file:///storage/emulated/0/Download/lease.pdf"))
    composeTestRule.onNodeWithTag("select_photos_button").performScrollTo().performClick()
    waitForTag("photos_error_text")

    // Scenario 2: Select multiple valid photos, preview, replace one, remove one
    registryOwner.nextMultiPickUris = listOf(
      Uri.parse("file:///tmp/front_view.jpg"),
      Uri.parse("file:///tmp/parlor.png"),
      Uri.parse("file:///tmp/kitchen.webp")
    )
    composeTestRule.onNodeWithTag("select_photos_button").performScrollTo().performClick()
    waitForTag("selected_photos_list")
    composeTestRule.onNodeWithText("Selected Photos (3)").performScrollTo().assertIsDisplayed()

    // Replace photo 1 with another image
    registryOwner.nextSinglePickUri = Uri.parse("file:///tmp/updated_parlor.jpg")
    composeTestRule.onNodeWithTag("replace_photo_btn_1").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Selected Photos (3)").performScrollTo().assertIsDisplayed()

    // Remove photo 2 -> count becomes 2
    composeTestRule.onNodeWithTag("remove_photo_btn_2").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Selected Photos (2)").performScrollTo().assertIsDisplayed()

    // Advance to Step 5: Review and Submit
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick()
    waitForTag("post_house_step_5")
    composeTestRule.onNodeWithText("Step 5 of 5").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Spacious 2 Bedroom Flat in Ugbowo").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Property Photos (2)").performScrollTo().assertIsDisplayed()

    // Test editing a section from Step 5 (Edit Step 1 -> modify title -> step forward back to Step 5)
    composeTestRule.onNodeWithTag("review_edit_step_1").performScrollTo().performClick()
    waitForTag("post_house_step_1")
    composeTestRule.onNodeWithTag("property_title_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("property_title_input").performTextInput("Verified 2 Bedroom Flat in Ugbowo")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick() // -> 2
    waitForTag("post_house_step_2")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick() // -> 3
    waitForTag("post_house_step_3")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick() // -> 4
    waitForTag("post_house_step_4")
    composeTestRule.onNodeWithTag("step_next_button").performScrollTo().performClick() // -> 5
    waitForTag("post_house_step_5")
    composeTestRule.onNodeWithText("Verified 2 Bedroom Flat in Ugbowo").performScrollTo().assertIsDisplayed()

    // Scenario 4: Submit for Approval -> creates PENDING_APPROVAL listing via POST /api/properties AND uploads 2 images
    composeTestRule.onNodeWithTag("submit_property_button").performScrollTo().performClick()
    waitForTag("view_in_dashboard_button")
    assertTrue(server.calledPaths.contains("POST /api/properties"))

    val created = server.listings.first { it.title == "Verified 2 Bedroom Flat in Ugbowo" }
    assertEquals("pending_approval", created.moderationStatus)
    assertEquals("2_bedroom", created.propertyType)
    assertEquals(2, server.calledPaths.count { it == "POST /api/properties/${created.id}/images" })
    assertEquals(2, server.propertyImages[created.id]?.size ?: 0)
    assertEquals(listOf(0, 1), server.propertyImages[created.id]?.map { it.displayOrder })

    composeTestRule.onNodeWithTag("view_in_dashboard_button").performClick()
    waitForTag("landlord_dashboard_scroll")
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("landlord_properties_loading").fetchSemanticsNodes().isEmpty()
    }

    // Verify new Pending Approval listing shows in My Properties with uploaded multi-photo badge & details gallery
    composeTestRule.onNodeWithTag("landlord_dashboard_scroll")
      .performScrollToNode(hasTestTag("landlord_property_card_${created.id}"))
    composeTestRule.onNodeWithText("Verified 2 Bedroom Flat in Ugbowo").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("landlord_card_photo_count_${created.id}").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("landlord_property_submitted_date_${created.id}").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_availability_button_${created.id}").assertDoesNotExist()

    // Open Property Details -> verify multi-image gallery (1 / 2 Photos -> click thumbnail 1 -> 2 / 2 Photos)
    composeTestRule.onNodeWithTag("view_landlord_details_button_${created.id}").performScrollTo().performClick()
    waitForTag("landlord_details_photo_counter")
    composeTestRule.onNodeWithText("1 / 2 Photos").assertIsDisplayed()
    composeTestRule.onNodeWithTag("landlord_details_thumbnail_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("2 / 2 Photos").assertIsDisplayed()
    composeTestRule.onNodeWithText("Close").performClick()

    // Open Edit Property -> add 1 new photo -> Save & Submit -> verify all 3 photos persist on server & UI
    composeTestRule.onNodeWithTag("edit_listing_button_${created.id}").performScrollTo().performClick()
    waitForTag("edit_property_dialog")
    waitForTag("edit_existing_photos_row")
    registryOwner.nextMultiPickUris = listOf(Uri.parse("file:///tmp/extra_bedroom.jpg"))
    composeTestRule.onNodeWithTag("edit_add_photos_button").performScrollTo().performClick()
    waitForTag("edit_new_photos_row")
    composeTestRule.onNodeWithTag("save_listing_changes_button").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule.onAllNodesWithText("Status Updated").fetchSemanticsNodes().isNotEmpty() &&
        server.propertyImages[created.id]?.size == 3
    }
    assertEquals(3, server.propertyImages[created.id]?.size ?: 0)
    assertEquals(listOf(0, 1, 2), server.propertyImages[created.id]?.map { it.displayOrder })
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("3 photos").fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun testPendingAndUnavailableHiddenFromFindHousesWhileApprovedAppearsAndSearchWorks() {
    val server = StatefulServer(activeRole = "tenant")
    // Add a pending listing to verify tenant Find a House excludes it
    server.listings.add(
      StoredListing(
        id = "prop_pending_ugbowo",
        title = "Hidden Pending Ugbowo House",
        propertyType = "2_bedroom",
        bedrooms = 2,
        bathrooms = 2,
        rentAmount = 500000L,
        rentPeriod = "yearly",
        description = "Waiting admin approval",
        neighbourhood = "Ugbowo",
        fullAddress = "18 Technical College Road, Ugbowo, Benin City",
        landmark = "UNIBEN Gate",
        moderationStatus = "pending_approval",
        availabilityStatus = "available"
      )
    )

    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("tenant-jwt", "2027-01-01T00:00:00Z")
    }
    val repo = createRepository(tokenStore, server)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }

    waitForTag("home_screen_scroll")
    composeTestRule.onNodeWithTag("nav_item_find_houses").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      server.calledPaths.contains("GET /api/properties") &&
        composeTestRule.onAllNodesWithTag("find_houses_loading").fetchSemanticsNodes().isEmpty()
    }

    // Scenario 6: Approved & available listing appears
    composeTestRule.onNodeWithTag("find_houses_discovery_list")
      .performScrollToNode(hasTestTag("discovery_card_prop_approved_1"))
    composeTestRule.onNodeWithText("Luxury 2 Bedroom in GRA").assertIsDisplayed()

    // Scenario 5: Pending, Rejected, and Unavailable listings do NOT appear in Find a House
    composeTestRule.onNodeWithTag("discovery_card_prop_pending_ugbowo").assertDoesNotExist()
    composeTestRule.onNodeWithTag("discovery_card_prop_rejected_1").assertDoesNotExist()
    composeTestRule.onNodeWithTag("discovery_card_prop_unavail_1").assertDoesNotExist()
    assertTrue(
      composeTestRule.onAllNodesWithText("Hidden Pending Ugbowo House", substring = true)
        .fetchSemanticsNodes().isEmpty()
    )

    // Scenario 8: Existing Find a House search & Property Details still work
    composeTestRule.onNodeWithTag("view_details_btn_prop_approved_1").performScrollTo().performClick()
    waitForTag("send_enquiry_to_dwello_button")
  }
}
