package com.example

import android.app.Application
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthenticatedUser
import com.example.data.DwelloSupportConfig
import com.example.ui.AsyncResourceState
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandlordHomeScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SupportContactRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val tenantUser = AuthenticatedUser(
    id = "tenant_1",
    fullName = "Osaro Tenant",
    phone = "+2348031234567",
    role = AuthAccountRole.TENANT,
    backendRoleRaw = "tenant"
  )

  private val landlordUser = AuthenticatedUser(
    id = "landlord_1",
    fullName = "Chief Eghosa",
    phone = "+2348099988776",
    role = AuthAccountRole.LANDLORD,
    backendRoleRaw = "landlord"
  )

  private fun clearStartedActivities() {
    val shadowApp = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
    while (shadowApp.nextStartedActivity != null) {
      // Drain previous intents
    }
  }

  private fun nextStartedActivityIntent(): Intent? {
    return Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
  }

  @Test
  fun tenantHomeSupportButtonsDisplayAndDialNewSupportNumber() {
    clearStartedActivities()
    composeTestRule.setContent {
      MyApplicationTheme {
        HomeScreen(
          currentLanguage = AppLanguage.ENGLISH,
          propertiesState = AsyncResourceState.Success(emptyList()),
          favouriteIds = emptySet(),
          favouriteActionIds = emptySet(),
          selectedPropertyDetailState = null,
          currentUser = tenantUser,
          enquirySubmitting = false,
          enquirySubmitError = null,
          enquirySubmittedSuccess = false,
          onLanguageToggle = {},
          onNavigateToFindHouses = {},
          onRetryLoadProperties = {},
          onToggleFavourite = {},
          onOpenPropertyDetails = {},
          onClosePropertyDetails = {},
          onSubmitEnquiry = { _, _, _ -> },
          onResetEnquiryState = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("home_screen_scroll")
      .performScrollToNode(hasTestTag("help_contact_section_card"))
    composeTestRule.onNodeWithTag("call_help_desk_button").performClick()

    val dialIntent = nextStartedActivityIntent()
    assertNotNull(dialIntent)
    assertEquals(Intent.ACTION_DIAL, dialIntent?.action)
    assertEquals(DwelloSupportConfig.SUPPORT_TEL_URI, dialIntent?.data?.toString())

    composeTestRule.onNodeWithTag("whatsapp_dwello_button").performClick()
    composeTestRule.onNodeWithText("Phone: 09155259017", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Call Now").performClick()

    val dialogDialIntent = nextStartedActivityIntent()
    assertNotNull(dialogDialIntent)
    assertEquals(Intent.ACTION_DIAL, dialogDialIntent?.action)
    assertEquals("tel:09155259017", dialogDialIntent?.data?.toString())
  }

  @Test
  fun profileSupportButtonDisplaysAndDialsNewSupportNumber() {
    clearStartedActivities()
    composeTestRule.setContent {
      MyApplicationTheme {
        ProfileScreen(
          currentLanguage = AppLanguage.ENGLISH,
          currentUser = tenantUser,
          landlordPropertiesState = AsyncResourceState.Success(emptyList()),
          landlordEnquiriesState = AsyncResourceState.Success(emptyList()),
          isLoggingOut = false,
          isActionLoading = false,
          onLanguageSelected = {},
          onLanguageToggle = {},
          onLogoutClick = {},
          onNavigateToLandlordDashboard = {},
          onToggleAvailability = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("profile_scroll_view")
      .performScrollToNode(hasTestTag("profile_call_support_button"))
    composeTestRule.onNodeWithText("09155259017", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_call_support_button").performClick()

    val dialIntent = nextStartedActivityIntent()
    assertNotNull(dialIntent)
    assertEquals(Intent.ACTION_DIAL, dialIntent?.action)
    assertEquals("tel:09155259017", dialIntent?.data?.toString())
  }

  @Test
  fun messagesSupportButtonDisplaysAndDialsNewSupportNumber() {
    clearStartedActivities()
    composeTestRule.setContent {
      MyApplicationTheme {
        MessagesScreen(
          currentLanguage = AppLanguage.ENGLISH,
          userRole = AuthAccountRole.TENANT,
          enquiriesState = AsyncResourceState.Success(emptyList()),
          isActionLoading = false,
          actionMessage = null,
          actionError = null,
          onRefreshEnquiries = {},
          onUpdateEnquiryStatus = { _, _ -> },
          onClearFeedback = {},
          onLanguageToggle = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("enquiries_call_support_button").performScrollTo()
    composeTestRule.onNodeWithText("Call Dwello Support (09155259017)").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enquiries_call_support_button").performClick()

    val dialIntent = nextStartedActivityIntent()
    assertNotNull(dialIntent)
    assertEquals(Intent.ACTION_DIAL, dialIntent?.action)
    assertEquals("tel:09155259017", dialIntent?.data?.toString())
  }

  @Test
  fun landlordHomeSupportActionsDialAndOpenWhatsappWithNewSupportNumber() {
    clearStartedActivities()
    composeTestRule.setContent {
      MyApplicationTheme {
        LandlordHomeScreen(
          currentLanguage = AppLanguage.ENGLISH,
          currentUser = landlordUser,
          propertiesState = AsyncResourceState.Success(emptyList()),
          enquiriesState = AsyncResourceState.Success(emptyList()),
          onLanguageToggle = {},
          onRefreshData = {},
          onNavigateToPostHouse = {},
          onNavigateToMyProperties = {},
          onNavigateToEnquiries = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("landlord_home_scroll")
      .performScrollToNode(hasTestTag("landlord_call_support_button"))
    composeTestRule.onNodeWithTag("landlord_call_support_button").performClick()
    val dialIntent = nextStartedActivityIntent()
    assertNotNull(dialIntent)
    assertEquals(Intent.ACTION_DIAL, dialIntent?.action)
    assertEquals("tel:09155259017", dialIntent?.data?.toString())

    composeTestRule.onNodeWithTag("landlord_whatsapp_support_button").performClick()
    val whatsappIntent = nextStartedActivityIntent()
    assertNotNull(whatsappIntent)
    assertEquals(Intent.ACTION_VIEW, whatsappIntent?.action)
    assertEquals("https://wa.me/2349155259017", whatsappIntent?.data?.toString())
  }

  @Test
  fun forgotPasswordScreenDisplaysNewSupportNumberInRecoveryNotice() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ForgotPasswordScreen(
          currentLanguage = AppLanguage.ENGLISH,
          onLanguageToggle = {},
          onBackToLogin = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("forgot_password_phone_input").performTextInput("08031234567")
    composeTestRule.onNodeWithTag("forgot_password_continue_button").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("forgot_password_backend_notice").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("09155259017", substring = true).assertIsDisplayed()
  }
}
