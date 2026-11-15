package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
class RoleBasedNavigationRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private class RoleServerState {
    var nextRoleForMe: String = "tenant"
    var nextLoginRoleOverride: String? = null
    val calledPaths = CopyOnWriteArrayList<String>()
  }

  private fun createRepository(
    tokenStore: InMemorySessionTokenStore,
    state: RoleServerState
  ): DwelloRepository {
    val interceptor = Interceptor { chain ->
      val req = chain.request()
      val path = req.url.encodedPath
      val method = req.method
      val buf = Buffer()
      req.body?.writeTo(buf)
      val body = buf.readUtf8()
      state.calledPaths.add("$method $path")

      val (code, json) = when {
        method == "POST" && path == "/api/auth/login" -> {
          val resolvedRole = state.nextLoginRoleOverride ?: if (body.contains("8099988776")) "landlord" else "tenant"
          state.nextRoleForMe = resolvedRole
          val fullName = if (resolvedRole == "landlord") "Chief Eghosa" else "Osaro Ighodaro"
          val phone = if (resolvedRole == "landlord") "+2348099988776" else "+2348031234567"
          200 to """
            {
              "success": true,
              "token": "jwt-$resolvedRole",
              "expires_at": "2027-01-01T00:00:00Z",
              "user": {
                "id": "usr_$resolvedRole",
                "full_name": "$fullName",
                "phone": "$phone",
                "role": "$resolvedRole"
              }
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/auth/me" -> {
          val role = state.nextRoleForMe
          val fullName = if (role == "landlord") "Chief Eghosa" else "Osaro Ighodaro"
          val phone = if (role == "landlord") "+2348099988776" else "+2348031234567"
          200 to """
            {
              "success": true,
              "user": {
                "id": "usr_$role",
                "full_name": "$fullName",
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
          200 to """{"success": true, "properties": []}"""
        }

        method == "GET" && path == "/api/favourites" -> {
          200 to """{"success": true, "properties": []}"""
        }

        method == "GET" && path == "/api/tenant/enquiries" -> {
          200 to """{"success": true, "enquiries": []}"""
        }

        method == "GET" && path == "/api/landlord/properties" -> {
          200 to """
            {
              "success": true,
              "properties": [
                {
                  "id": "lp_pending_1",
                  "title": "Pending 2-Bedroom in GRA",
                  "property_type": "2_bedroom",
                  "bedrooms": 2,
                  "bathrooms": 2,
                  "rent_amount": 600000,
                  "rent_period": "yearly",
                  "description": "Waiting verification",
                  "neighbourhood": "GRA",
                  "moderation_status": "pending_approval",
                  "availability_status": "available"
                }
              ]
            }
          """.trimIndent()
        }

        method == "GET" && path == "/api/landlord/enquiries" -> {
          200 to """{"success": true, "enquiries": []}"""
        }

        else -> 404 to """{"error": "Unhandled route"}"""
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
  fun tenantShowsExactlyFourNavItemsAndHidesLandlordActions() {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("tenant-token", "2027-01-01T00:00:00Z")
    }
    val state = RoleServerState().apply { nextRoleForMe = "tenant" }
    val repo = createRepository(tokenStore, state)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }

    waitForTag("home_screen_scroll")

    // Tenant sees exactly: Home, Find Houses, Enquiries, Profile
    composeTestRule.onNodeWithTag("nav_item_home").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_messages").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_profile").assertIsDisplayed()

    // Tenants must NOT see Post House, My Properties, Landlord Dashboard, or Home Post a House CTA
    composeTestRule.onNodeWithTag("nav_item_post_house").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertDoesNotExist()
    composeTestRule.onNodeWithTag("post_a_house_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("landlord_home_scroll").assertDoesNotExist()
    composeTestRule.onNodeWithTag("landlord_dashboard_scroll").assertDoesNotExist()

    // Tenant never fetches landlord endpoints
    assertFalse(state.calledPaths.any { it.contains("/api/landlord/") })
  }

  @Test
  fun landlordShowsExactlyFiveNavItemsHidesFindHousesAndProtectsAdminApproval() {
    val tokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("landlord-token", "2027-01-01T00:00:00Z")
    }
    val state = RoleServerState().apply { nextRoleForMe = "landlord" }
    val repo = createRepository(tokenStore, state)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }

    waitForTag("landlord_home_scroll")

    // Landlord sees exactly: Home, Post House, My Properties, Enquiries, Profile
    composeTestRule.onNodeWithTag("nav_item_home").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_post_house").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_messages").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_profile").assertIsDisplayed()

    // Landlord must NOT see Tenant Find Houses search tab
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertDoesNotExist()
    composeTestRule.onNodeWithTag("home_screen_scroll").assertDoesNotExist()

    // Navigate across Landlord screens: Post House -> My Properties -> Enquiries -> Profile
    composeTestRule.onNodeWithTag("nav_item_post_house").performClick()
    waitForTag("post_house_step_1")

    composeTestRule.onNodeWithTag("nav_item_my_properties").performClick()
    waitForTag("landlord_dashboard_scroll")

    // Admin approval/rejection controls must never be exposed to Landlord
    assertTrue(composeTestRule.onAllNodesWithText("Approve Property", substring = true).fetchSemanticsNodes().isEmpty())
    assertTrue(composeTestRule.onAllNodesWithText("Reject Property", substring = true).fetchSemanticsNodes().isEmpty())
    assertFalse(state.calledPaths.any { it == "GET /api/properties" })
  }

  @Test
  fun logoutAndSwitchBetweenTenantAndLandlordUpdatesRoleNavigationCleanly() {
    val tokenStore = InMemorySessionTokenStore()
    val state = RoleServerState()
    val repo = createRepository(tokenStore, state)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    // 1. Login as Tenant
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    waitForTag("login_phone_input")
    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    waitForTag("home_screen_scroll")
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_post_house").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertDoesNotExist()

    // 2. Logout
    composeTestRule.onNodeWithTag("nav_item_profile").performClick()
    waitForTag("profile_open_auth_button")
    composeTestRule.onNodeWithTag("profile_open_auth_button").performClick()
    waitForTag("welcome_login_button")
    assertNull(tokenStore.getSessionToken())

    // 3. Re-login as Landlord
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    waitForTag("login_phone_input")
    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08099988776")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    // Tenant navigation is completely replaced by Landlord 5-item navigation
    waitForTag("landlord_home_scroll")
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_post_house").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertIsDisplayed()

    // 4. Logout again and login back as Tenant
    composeTestRule.onNodeWithTag("nav_item_profile").performClick()
    waitForTag("profile_open_auth_button")
    composeTestRule.onNodeWithTag("profile_open_auth_button").performClick()
    waitForTag("welcome_login_button")

    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    waitForTag("login_phone_input")
    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    waitForTag("home_screen_scroll")
    composeTestRule.onNodeWithTag("nav_item_find_houses").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_item_post_house").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_item_my_properties").assertDoesNotExist()
  }

  @Test
  fun missingOrInvalidRoleOnLoginRefusesAccessAndShowsError() {
    val tokenStore = InMemorySessionTokenStore()
    val state = RoleServerState().apply { nextLoginRoleOverride = "agent" }
    val repo = createRepository(tokenStore, state)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = repo)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    waitForTag("login_phone_input")
    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Account verification required", substring = true)
        .fetchSemanticsNodes().isNotEmpty()
    }
    assertNull(tokenStore.getSessionToken())
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertDoesNotExist()
  }

  @Test
  fun storedSessionWithEmptyRoleRefusesAccessAndShowsVerificationBanner() {
    val restoreTokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("stale-empty-role-token", "2027-01-01T00:00:00Z")
    }
    val restoreState = RoleServerState().apply { nextRoleForMe = "" }
    val restoreRepo = createRepository(restoreTokenStore, restoreState)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = restoreRepo)
      }
    }

    waitForTag("welcome_auth_error_banner")
    composeTestRule.onNodeWithTag("welcome_auth_error_banner").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertDoesNotExist()
    assertNull(restoreTokenStore.getSessionToken())
  }

  @Test
  fun storedSessionWithInvalidRoleRedirectsToWelcomeWithVerificationBanner() {
    val restoreTokenStore = InMemorySessionTokenStore().apply {
      saveSessionToken("stale-agent-token", "2027-01-01T00:00:00Z")
    }
    val restoreState = RoleServerState().apply { nextRoleForMe = "agent" }
    val restoreRepo = createRepository(restoreTokenStore, restoreState)

    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp(repository = restoreRepo)
      }
    }

    waitForTag("welcome_auth_error_banner")
    composeTestRule.onNodeWithTag("welcome_auth_error_banner").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertDoesNotExist()
    assertNull(restoreTokenStore.getSessionToken())
  }
}
