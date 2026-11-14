package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginFirstAccessRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun welcomeScreenShowsRequiredBrandingWithoutGuestPreviewOrAgentNotice() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp()
      }
    }
    composeTestRule.waitForIdle()

    // Kept branding & actions
    composeTestRule.onNodeWithTag("auth_language_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("DWELLO HOMES").assertIsDisplayed()
    composeTestRule.onNodeWithText("Benin City Property Rental").assertIsDisplayed()
    composeTestRule.onNodeWithText("Benin City, Edo State").assertIsDisplayed()
    composeTestRule.onNodeWithText("Find a place to call home.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_get_started_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().assertIsDisplayed()

    // Removed guest browsing shortcut
    composeTestRule.onNodeWithTag("welcome_demo_preview_button").assertDoesNotExist()
    assertTrue(
      composeTestRule.onAllNodesWithText("Browse Houses in Demo Preview", substring = true)
        .fetchSemanticsNodes().isEmpty()
    )

    // Removed unnecessary agent notice
    assertTrue(
      composeTestRule.onAllNodesWithText("There is no Agent role", substring = true)
        .fetchSemanticsNodes().isEmpty()
    )

    // Unauthenticated start must NOT show bottom navigation or role notice dialog
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertDoesNotExist()
    assertTrue(
      composeTestRule.onAllNodesWithText("Role Access Notice", substring = true)
        .fetchSemanticsNodes().isEmpty()
    )
  }

  @Test
  fun tenantLoginAndRoleAccessNoticeBehaviorWorksCleanly() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp()
      }
    }
    composeTestRule.waitForIdle()

    // Navigate to Login
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Enter valid Nigerian phone + password
    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Enter as Tenant
    composeTestRule.onNodeWithTag("login_preview_tenant_button").performClick()
    composeTestRule.waitForIdle()

    // Tenant Home loads cleanly with bottom bar and NO role notice on normal entry
    composeTestRule.onNodeWithTag("home_screen_scroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
    assertTrue(
      composeTestRule.onAllNodesWithText("Role Access Notice")
        .fetchSemanticsNodes().isEmpty()
    )

    // Tenant can open Find Houses
    composeTestRule.onNodeWithTag("nav_item_find_houses").performClick()
    composeTestRule.waitForIdle()
    assertTrue(
      composeTestRule.onAllNodesWithText("Role Access Notice")
        .fetchSemanticsNodes().isEmpty()
    )

    // Tenant attempting to open Post House gets friendly Role Access Notice
    composeTestRule.onNodeWithTag("nav_item_post_house").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Role Access Notice").assertIsDisplayed()

    // Close dismisses dialog
    composeTestRule.onNodeWithTag("role_notice_close_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue(
      composeTestRule.onAllNodesWithText("Role Access Notice")
        .fetchSemanticsNodes().isEmpty()
    )

    // Trigger again and test Go to Profile navigation
    composeTestRule.onNodeWithTag("nav_item_post_house").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("role_notice_go_to_profile_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_scroll_view").assertIsDisplayed()
  }

  @Test
  fun landlordLoginAndSearchRestrictionBehaviorWorksCleanly() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DwelloApp()
      }
    }
    composeTestRule.waitForIdle()

    // Navigate to Login
    composeTestRule.onNodeWithTag("welcome_login_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("login_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("login_password_input").performTextInput("Password123")
    composeTestRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Enter as Landlord
    composeTestRule.onNodeWithTag("login_preview_landlord_button").performClick()
    composeTestRule.waitForIdle()

    // Landlord Dashboard loads with NO role notice on normal entry
    assertTrue(
      composeTestRule.onAllNodesWithText("Role Access Notice")
        .fetchSemanticsNodes().isEmpty()
    )

    // Landlord attempting to open tenant-only search gets friendly Role Access Notice
    composeTestRule.onNodeWithTag("nav_item_find_houses").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Role Access Notice").assertIsDisplayed()

    // Go to Profile navigates cleanly
    composeTestRule.onNodeWithTag("role_notice_go_to_profile_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_scroll_view").assertIsDisplayed()
  }
}
