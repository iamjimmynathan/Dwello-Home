package com.example

import android.content.Context
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
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AndroidKeystoreSessionTokenStore
import com.example.data.DwelloRepository
import com.example.data.InMemorySessionTokenStore
import com.example.network.DwelloNetworkClient
import com.example.ui.theme.MyApplicationTheme
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginFirstAccessRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private class FakeLiveApiState(
    var activeRole: String = "tenant",
    var prop1Available: Boolean = true
  ) {
    val calledPaths = CopyOnWriteArrayList<String>()
    val favourites = CopyOnWriteArrayList<String>()
  }

  private fun createTestRepository(
    tokenStore: InMemorySessionTokenStore = InMemorySessionTokenStore(),
    state: FakeLiveApiState = FakeLiveApiState()
  ): DwelloRepository {
    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val path = req.url.encodedPath
      val method = req.method
      val buffer = Buffer()
      req.body?.writeTo(buffer)
      val bodyText = buffer.readUtf8()
      state.calledPaths.add("$method $path")

      val (code, json) = when {
        method == "POST" && path == "/api/auth/login" -> {
          val isLandlord = bodyText.contains("8099988776") || state.activeRole == "landlord"
          val role = if (isLandlord) "landlord" else "tenant"
          state.activeRole = role
          val name = if (isLandlord) "Chief Eghosa" else "Osaro Ighodaro"
          val phone = if (isLandlord) "+2348099988776" else "+2348031234567"
          200 to """
            {
              "success": true,
              "token": "live-jwt-$role",
              "expires_at": "2027-01-01T00:00:00Z",
              "user": {
                "id": "user_$role",
                "full_name": "$name",
                "phone": "$phone",
                "role": "$role"
              }
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/auth/me" -> {
          val role = state.activeRole
          val name = if (role == "landlord") "Chief Eghosa" else "Osaro Ighodaro"
          val phone = if (role == "landlord") "+2348099988776" else "+2348031234567"
          200 to """
            {
              "success": true,
              "user": {
                "id": "user_$role",
                "full_name": "$name",
                "phone": "$phone",
                "role": "$role"
              }
            }
          """.trimIndent()
        }

        method == "POST" && path == "/api/auth/logout" -> {
          200 to """{"success": true, "message": "Logged out"}"""
        }

        method == "GET" && path == "/api/properties" -> {
          200 to """
            {
              "success": true,
              "properties": [
                {
                  "id": "prop_1",
                  "title": "Modern 2-Bedroom Flat in GRA",
                  "property_type": "2_bedroom",
                  "bedrooms": 2,
                  "bathrooms": 2,
                  "rent_amount": 750000,
                  "rent_period": "yearly",
                  "description": "Borehole water, prepaid meter and gated compound.",
                  "neighbourhood": "GRA",
                  "landmark": "Near Golf Course Road",
                  "moderation_status": "approved",
                  "availability_status": "available"
                }
              ]
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/properties/prop_1" -> {
          200 to """
            {
              "success": true,
              "property": {
                "id": "prop_1",
                "title": "Modern 2-Bedroom Flat in GRA",
                "property_type": "2_bedroom",
                "bedrooms": 2,
                "bathrooms": 2,
                "rent_amount": 750000,
                "rent_period": "yearly",
                "description": "Borehole water, prepaid meter and gated compound.",
                "neighbourhood": "GRA",
                "landmark": "Near Golf Course Road",
                "moderation_status": "approved",
                "availability_status": "available"
              },
              "images": []
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/favourites" -> {
          val itemsJson = state.favourites.joinToString(",") { id ->
            """
              {
                "id": "$id",
                "title": "Modern 2-Bedroom Flat in GRA",
                "property_type": "2_bedroom",
                "bedrooms": 2,
                "bathrooms": 2,
                "rent_amount": 750000,
                "rent_period": "yearly",
                "description": "Gated compound",
                "neighbourhood": "GRA",
                "moderation_status": "approved",
                "availability_status": "available"
              }
            """.trimIndent()
          }
          200 to """{"success": true, "properties": [$itemsJson]}"""
        }

        method == "POST" && path.startsWith("/api/favourites/") -> {
          val id = path.removePrefix("/api/favourites/")
          if (!state.favourites.contains(id)) state.favourites.add(id)
          200 to """{"success": true, "message": "Property added to favourites"}"""
        }

        method == "DELETE" && path.startsWith("/api/favourites/") -> {
          val id = path.removePrefix("/api/favourites/")
          state.favourites.remove(id)
          200 to """{"success": true, "message": "Removed"}"""
        }

        method == "POST" && path == "/api/enquiries" -> {
          201 to """{"success": true, "message": "Enquiry created", "enquiry_id": "enq_1"}"""
        }

        method == "GET" && path == "/api/tenant/enquiries" -> {
          200 to """
            {
              "success": true,
              "enquiries": [
                {
                  "id": "enq_1",
                  "property_id": "prop_1",
                  "message": "Interested in inspection",
                  "preferred_inspection_date": "This Saturday 11:00 AM",
                  "status": "new",
                  "property_title": "Modern 2-Bedroom Flat in GRA",
                  "neighbourhood": "GRA",
                  "rent_amount": 750000
                }
              ]
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/landlord/properties" -> {
          val avail = if (state.prop1Available) "available" else "unavailable"
          200 to """
            {
              "success": true,
              "properties": [
                {
                  "id": "lp_1",
                  "title": "Self Con at Ugbowo",
                  "property_type": "self_contained",
                  "bedrooms": 1,
                  "bathrooms": 1,
                  "rent_amount": 250000,
                  "rent_period": "yearly",
                  "description": "Quiet compound",
                  "neighbourhood": "Ugbowo",
                  "moderation_status": "approved",
                  "availability_status": "$avail"
                }
              ]
            }
          """.trimIndent()
        }

        method == "PATCH" && path == "/api/properties/lp_1/availability" -> {
          state.prop1Available = !bodyText.contains("\"unavailable\"")
          val avail = if (state.prop1Available) "available" else "unavailable"
          200 to """{"success": true, "message": "Updated", "availability_status": "$avail"}"""
        }

        method == "GET" && path == "/api/landlord/enquiries" -> {
          200 to """
            {
              "success": true,
              "enquiries": [
                {
                  "id": "lenq_1",
                  "property_id": "lp_1",
                  "message": "Can I view tomorrow?",
                  "preferred_inspection_date": "Sunday 2:00 PM",
                  "status": "new",
                  "property_title": "Self Con at Ugbowo",
                  "neighbourhood": "Ugbowo",
                  "tenant_name": "Osaro",
                  "rent_amount": 250000
                }
              ]
            }
          """.trimIndent()
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
    return DwelloRepository(apiService, tokenStore)
  }

  private fun waitForTag(tag: String) {
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun welcomeScreenShowsRequiredBrandingWithoutGuestPreviewOrAgentNotice() {
    val repo = createTestRepository()
    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("auth_language_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("DWELLO HOMES").assertIsDisplayed()
    composeTestRule.onNodeWithText("Benin City Property Rental").assertIsDisplayed()
    composeTestRule.onNodeWithText("Benin City, Edo State").assertIsDisplayed()
    composeTestRule.onNodeWithText("Find a place to call home.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_get_started_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("welcome_demo_preview_button").assertDoesNotExist()
    assertTrue(
      composeTestRule.onAllNodesWithText("Browse Houses in Demo Preview", substring = true)
        .fetchSemanticsNodes().isEmpty()
    )
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertDoesNotExist()
  }

  @Test
  fun tenantLiveLoginFavouritesEnquiryRoleNoticeAndLogoutWorkEndToEnd() {
    val tokenStore = InMemorySessionTokenStore()
    val apiState = FakeLiveApiState(activeRole = "tenant")
    val repo = createTestRepository(tokenStore, apiState)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    // Navigate to Login & submit real credentials
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    // Lands on Tenant Home after live login
    waitForTag("home_screen_scroll")
    composeTestRule.onNodeWithTag("home_screen_scroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
    assertEquals("live-jwt-tenant", tokenStore.getSessionToken())

    // Open Find Houses -> wait for live property load -> scroll to card -> toggle favourite -> view details
    composeTestRule.onNodeWithTag("nav_item_find_houses").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      apiState.calledPaths.contains("GET /api/properties") &&
        composeTestRule.onAllNodesWithTag("find_houses_loading").fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.onNodeWithTag("find_houses_discovery_list")
      .performScrollToNode(hasTestTag("discovery_card_prop_1"))
    composeTestRule.onNodeWithTag("favourite_toggle_btn_prop_1").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      apiState.calledPaths.contains("POST /api/favourites/prop_1")
    }

    composeTestRule.onNodeWithTag("view_details_btn_prop_1").performScrollTo().performClick()
    waitForTag("send_enquiry_to_dwello_button")
    assertTrue(apiState.calledPaths.contains("GET /api/properties/prop_1"))

    // Submit an enquiry
    composeTestRule.onNodeWithTag("send_enquiry_to_dwello_button").performClick()
    waitForTag("submit_enquiry_form_btn")
    composeTestRule.onNodeWithTag("submit_enquiry_form_btn").performClick()
    waitForTag("enquiry_done_view_tab_btn")
    assertTrue(apiState.calledPaths.contains("POST /api/enquiries"))

    // Navigate to Enquiries tab
    composeTestRule.onNodeWithTag("enquiry_done_view_tab_btn").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      apiState.calledPaths.contains("GET /api/tenant/enquiries") &&
        composeTestRule.onAllNodesWithTag("enquiries_loading").fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.onNodeWithTag("messages_list")
      .performScrollToNode(hasTestTag("enquiry_card_enq_1"))

    // Tenant must NOT see Post House tab, My Properties tab, or Home Post a House CTA
    composeTestRule.onNodeWithTag("nav_item_post_house").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertDoesNotExist()
    composeTestRule.onNodeWithTag("post_a_house_button").assertDoesNotExist()

    // Navigate to Profile via Tenant bottom nav and verify no client-side role switcher exists
    composeTestRule.onNodeWithTag("nav_item_profile").performClick()
    waitForTag("profile_scroll_view")
    composeTestRule.onNodeWithTag("profile_user_name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_user_role_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("role_tenant_btn").assertDoesNotExist()
    composeTestRule.onNodeWithTag("role_landlord_btn").assertDoesNotExist()

    // Logout calls /api/auth/logout, clears token, and returns to Welcome screen
    composeTestRule.onNodeWithTag("profile_open_auth_button").performClick()
    waitForTag("welcome_login_button")
    assertTrue(apiState.calledPaths.contains("POST /api/auth/logout"))
    assertNull(tokenStore.getSessionToken())
  }

  @Test
  fun landlordLiveLoginDashboardAvailabilityAndSearchRestrictionWorkEndToEnd() {
    val tokenStore = InMemorySessionTokenStore()
    val apiState = FakeLiveApiState(activeRole = "landlord")
    val repo = createTestRepository(tokenStore, apiState)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08099988776")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    // Lands on Landlord Home first, with Find Houses hidden from Landlord nav
    waitForTag("landlord_home_scroll")
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertIsDisplayed()
    assertEquals("live-jwt-landlord", tokenStore.getSessionToken())

    // Navigate to My Properties tab (Landlord Dashboard)
    composeTestRule.onNodeWithTag("nav_item_my_properties").performClick()
    waitForTag("landlord_dashboard_scroll")
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      apiState.calledPaths.contains("GET /api/landlord/properties") &&
        composeTestRule.onAllNodesWithTag("landlord_properties_loading").fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.onNodeWithTag("landlord_dashboard_scroll")
      .performScrollToNode(hasTestTag("landlord_property_card_lp_1"))

    // Toggle availability calls PATCH /api/properties/lp_1/availability
    composeTestRule.onNodeWithTag("toggle_availability_button_lp_1").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      apiState.calledPaths.contains("PATCH /api/properties/lp_1/availability")
    }
    assertFalse(apiState.prop1Available)

    // Dismiss feedback dialog
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()

    // Navigate to Profile cleanly
    composeTestRule.onNodeWithTag("nav_item_profile").performClick()
    waitForTag("profile_scroll_view")
  }

  @Test
  fun storedSessionRestoresAutomaticallyViaAuthMeOnStartup() {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("persisted-token-abc", "2027-01-01T00:00:00Z")
    }
    val apiState = FakeLiveApiState(activeRole = "tenant")
    val repo = createTestRepository(tokenStore, apiState)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }

    // Should restore via GET /api/auth/me directly to Tenant Home without showing Welcome/Login
    waitForTag("home_screen_scroll")
    assertTrue(apiState.calledPaths.contains("GET /api/auth/me"))
    composeTestRule.onNodeWithTag("home_screen_scroll").assertIsDisplayed()
  }

  @Test
  fun androidKeystoreSessionTokenStoreNeverPersistsPlaintextToken() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = AndroidKeystoreSessionTokenStore(context)
    val rawSecret = "dwello_super_secret_token_987654"

    store.saveSessionToken(rawSecret, "2027-01-01T00:00:00Z")
    assertEquals(rawSecret, store.getSessionToken())

    val prefs = context.getSharedPreferences("dwello_secure_session_prefs", Context.MODE_PRIVATE)
    val storedValues = prefs.all.values.map { it?.toString().orEmpty() }
    assertFalse(
      "Plaintext session token must NEVER appear in SharedPreferences",
      storedValues.any { it.contains(rawSecret) }
    )

    store.clearSession()
    assertNull(store.getSessionToken())
  }
}
