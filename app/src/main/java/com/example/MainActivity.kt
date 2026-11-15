package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AndroidKeystoreSessionTokenStore
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.DwelloRepository
import com.example.data.DwelloStrings
import com.example.data.RoleNavigationRouter
import com.example.ui.DwelloViewModel
import com.example.ui.SessionStatus
import com.example.ui.screens.AccountTypeSelectionScreen
import com.example.ui.screens.FindHousesScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandlordDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.PostHouseScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent

enum class DwelloNavScreen {
  WELCOME,
  ACCOUNT_TYPE_SELECTION,
  REGISTRATION,
  LOGIN,
  FORGOT_PASSWORD,
  HOME,
  FIND_HOUSES,
  POST_HOUSE,
  MESSAGES,
  PROFILE,
  LANDLORD_DASHBOARD
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DwelloApp()
      }
    }
  }
}

@Composable
fun DwelloApp(repository: DwelloRepository? = null) {
  val context = LocalContext.current
  val resolvedRepository = remember(context, repository) {
    repository ?: DwelloRepository.createDefault(
      AndroidKeystoreSessionTokenStore(context.applicationContext)
    )
  }

  val dwelloViewModel: DwelloViewModel = if (repository != null) {
    remember(resolvedRepository) { DwelloViewModel(resolvedRepository) }
  } else {
    viewModel(factory = DwelloViewModel.Factory(resolvedRepository))
  }

  val uiState by dwelloViewModel.uiState.collectAsState()

  var currentScreen by remember { mutableStateOf(DwelloNavScreen.WELCOME) }
  var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
  var initialSearchQuery by remember { mutableStateOf<String?>(null) }
  var selectedRegistrationRole by remember { mutableStateOf(AuthAccountRole.TENANT) }
  var roleRestrictedTarget by remember { mutableStateOf<DwelloNavScreen?>(null) }

  val authenticatedUser = uiState.currentUser
  val authenticatedRole = if (uiState.sessionStatus == SessionStatus.AUTHENTICATED) {
    authenticatedUser?.role
  } else {
    null
  }

  // Synchronize auth state transitions automatically
  LaunchedEffect(uiState.sessionStatus, authenticatedRole) {
    when (uiState.sessionStatus) {
      SessionStatus.RESTORING -> Unit
      SessionStatus.AUTHENTICATED -> {
        val role = authenticatedRole
        if (role != null) {
          if (RoleNavigationRouter.isAuthScreen(currentScreen) ||
            !RoleNavigationRouter.canAccessScreen(role, currentScreen)
          ) {
            roleRestrictedTarget = null
            currentScreen = RoleNavigationRouter.postLoginDestination(role)
          }
        }
      }
      SessionStatus.UNAUTHENTICATED -> {
        roleRestrictedTarget = null
        if (!RoleNavigationRouter.isAuthScreen(currentScreen)) {
          currentScreen = DwelloNavScreen.WELCOME
        }
      }
    }
  }

  if (uiState.sessionStatus == SessionStatus.RESTORING) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MainBackgroundWhite)
        .testTag("session_restoring_view"),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = DwelloStrings.appName(),
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = SkyBluePrimary,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = SkyBluePrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) {
            "Restoring your Dwello session..."
          } else {
            "Dey check your Dwello session..."
          },
          style = MaterialTheme.typography.bodyMedium.copy(color = NavyTextSecondary)
        )
      }
    }
    return
  }

  val effectiveScreen = RoleNavigationRouter.redirectUnauthenticatedTarget(
    role = authenticatedRole,
    requestedScreen = currentScreen
  )
  val isAuthScreen = RoleNavigationRouter.isAuthScreen(effectiveScreen)
  val roleHomeScreen = authenticatedRole?.let { RoleNavigationRouter.postLoginDestination(it) }
    ?: DwelloNavScreen.WELCOME

  fun toggleLanguage() {
    currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) {
      AppLanguage.PIDGIN
    } else {
      AppLanguage.ENGLISH
    }
  }

  fun navigateWithRoleGuard(target: DwelloNavScreen) {
    val role = authenticatedRole
    if (role == null && !RoleNavigationRouter.isAuthScreen(target)) {
      roleRestrictedTarget = null
      currentScreen = DwelloNavScreen.WELCOME
      return
    }
    if (RoleNavigationRouter.canAccessScreen(role, target)) {
      roleRestrictedTarget = null
      currentScreen = target
    } else {
      roleRestrictedTarget = target
    }
  }

  // Handle system back navigation for secondary main app screens (auth screens manage their own backstack)
  if (!isAuthScreen && effectiveScreen != roleHomeScreen) {
    BackHandler {
      currentScreen = if (effectiveScreen == DwelloNavScreen.POST_HOUSE && authenticatedRole == AuthAccountRole.LANDLORD) {
        DwelloNavScreen.LANDLORD_DASHBOARD
      } else {
        roleHomeScreen
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (!isAuthScreen && authenticatedRole != null) {
        NavigationBar(
          containerColor = Color.White,
          contentColor = DeepNavyText,
          tonalElevation = 6.dp,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          // 1. Home (Tenant)
          val isHome = effectiveScreen == DwelloNavScreen.HOME
          NavigationBarItem(
            selected = isHome,
            onClick = { navigateWithRoleGuard(DwelloNavScreen.HOME) },
            icon = {
              Icon(
                imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = "Home",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = DwelloStrings.navHome(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SkyBluePrimary,
              selectedTextColor = SkyBluePrimary,
              unselectedIconColor = NavyTextSecondary,
              unselectedTextColor = NavyTextSecondary,
              indicatorColor = SoftBlueAccent
            ),
            modifier = Modifier.testTag("nav_item_home")
          )

          // 2. Find Houses (Tenant)
          val isFind = effectiveScreen == DwelloNavScreen.FIND_HOUSES
          NavigationBarItem(
            selected = isFind,
            onClick = {
              initialSearchQuery = null
              navigateWithRoleGuard(DwelloNavScreen.FIND_HOUSES)
            },
            icon = {
              Icon(
                imageVector = if (isFind) Icons.Filled.Search else Icons.Outlined.Search,
                contentDescription = "Find Houses",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = DwelloStrings.navFind(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isFind) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SkyBluePrimary,
              selectedTextColor = SkyBluePrimary,
              unselectedIconColor = NavyTextSecondary,
              unselectedTextColor = NavyTextSecondary,
              indicatorColor = SoftBlueAccent
            ),
            modifier = Modifier.testTag("nav_item_find_houses")
          )

          // 3. Post House (Landlord)
          val isPost = effectiveScreen == DwelloNavScreen.POST_HOUSE
          NavigationBarItem(
            selected = isPost,
            onClick = { navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE) },
            icon = {
              Icon(
                imageVector = if (isPost) Icons.Filled.AddHome else Icons.Outlined.AddHome,
                contentDescription = "Post House",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = DwelloStrings.navPost(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isPost) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SkyBluePrimary,
              selectedTextColor = SkyBluePrimary,
              unselectedIconColor = NavyTextSecondary,
              unselectedTextColor = NavyTextSecondary,
              indicatorColor = SoftBlueAccent
            ),
            modifier = Modifier.testTag("nav_item_post_house")
          )

          // 4. Enquiries (Shared)
          val isMessages = effectiveScreen == DwelloNavScreen.MESSAGES
          NavigationBarItem(
            selected = isMessages,
            onClick = { navigateWithRoleGuard(DwelloNavScreen.MESSAGES) },
            icon = {
              Icon(
                imageVector = if (isMessages) Icons.Filled.Chat else Icons.Outlined.Chat,
                contentDescription = "Enquiries",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = DwelloStrings.navMessages(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isMessages) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SkyBluePrimary,
              selectedTextColor = SkyBluePrimary,
              unselectedIconColor = NavyTextSecondary,
              unselectedTextColor = NavyTextSecondary,
              indicatorColor = SoftBlueAccent
            ),
            modifier = Modifier.testTag("nav_item_messages")
          )

          // 5. Profile
          val isProfile = effectiveScreen == DwelloNavScreen.PROFILE || effectiveScreen == DwelloNavScreen.LANDLORD_DASHBOARD
          NavigationBarItem(
            selected = isProfile,
            onClick = { navigateWithRoleGuard(DwelloNavScreen.PROFILE) },
            icon = {
              Icon(
                imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = DwelloStrings.navProfile(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isProfile) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SkyBluePrimary,
              selectedTextColor = SkyBluePrimary,
              unselectedIconColor = NavyTextSecondary,
              unselectedTextColor = NavyTextSecondary,
              indicatorColor = SoftBlueAccent
            ),
            modifier = Modifier.testTag("nav_item_profile")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MainBackgroundWhite)
    ) {
      when (effectiveScreen) {
        DwelloNavScreen.WELCOME -> {
          WelcomeScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = ::toggleLanguage,
            onGetStartedClick = {
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION
            },
            onLoginClick = {
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.LOGIN
            }
          )
        }

        DwelloNavScreen.ACCOUNT_TYPE_SELECTION -> {
          AccountTypeSelectionScreen(
            currentLanguage = currentLanguage,
            initialSelectedRole = selectedRegistrationRole,
            onLanguageToggle = ::toggleLanguage,
            onContinueToRegistration = { role ->
              selectedRegistrationRole = role
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.REGISTRATION
            },
            onBack = { currentScreen = DwelloNavScreen.WELCOME }
          )
        }

        DwelloNavScreen.REGISTRATION -> {
          RegistrationScreen(
            currentLanguage = currentLanguage,
            selectedRole = selectedRegistrationRole,
            isLoading = uiState.authLoading,
            serverError = uiState.authError,
            onLanguageToggle = ::toggleLanguage,
            onChangeRoleClick = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION },
            onNavigateToLogin = {
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.LOGIN
            },
            onRegisterSubmit = { fullName, normalizedPhone, email, passwordChars, role ->
              dwelloViewModel.registerAndLogin(
                fullName = fullName,
                normalizedPhone = normalizedPhone,
                email = email,
                passwordChars = passwordChars,
                role = role
              )
            },
            onClearError = { dwelloViewModel.clearAuthError() },
            onBack = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION }
          )
        }

        DwelloNavScreen.LOGIN -> {
          LoginScreen(
            currentLanguage = currentLanguage,
            isLoading = uiState.authLoading,
            serverError = uiState.authError,
            onLanguageToggle = ::toggleLanguage,
            onForgotPasswordClick = {
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.FORGOT_PASSWORD
            },
            onRegisterClick = {
              dwelloViewModel.clearAuthError()
              currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION
            },
            onLoginSubmit = { normalizedPhone, passwordChars ->
              dwelloViewModel.login(normalizedPhone, passwordChars)
            },
            onClearError = { dwelloViewModel.clearAuthError() },
            onBack = { currentScreen = DwelloNavScreen.WELCOME }
          )
        }

        DwelloNavScreen.FORGOT_PASSWORD -> {
          ForgotPasswordScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = ::toggleLanguage,
            onBackToLogin = { currentScreen = DwelloNavScreen.LOGIN }
          )
        }

        DwelloNavScreen.HOME -> {
          if (authenticatedRole == AuthAccountRole.TENANT) {
            HomeScreen(
              currentLanguage = currentLanguage,
              propertiesState = uiState.approvedPropertiesState,
              favouriteIds = uiState.favouriteIds,
              favouriteActionIds = uiState.favouriteActionIds,
              selectedPropertyDetailState = uiState.selectedPropertyDetailState,
              currentUser = authenticatedUser,
              enquirySubmitting = uiState.enquirySubmitting,
              enquirySubmitError = uiState.enquirySubmitError,
              enquirySubmittedSuccess = uiState.enquirySubmittedSuccess,
              onLanguageToggle = ::toggleLanguage,
              onNavigateToFindHouses = { query ->
                initialSearchQuery = query
                navigateWithRoleGuard(DwelloNavScreen.FIND_HOUSES)
              },
              onNavigateToPostHouse = {
                navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE)
              },
              onRetryLoadProperties = { dwelloViewModel.loadTenantData() },
              onToggleFavourite = { id -> dwelloViewModel.toggleFavourite(id) },
              onOpenPropertyDetails = { prop -> dwelloViewModel.openPropertyDetails(prop) },
              onClosePropertyDetails = { dwelloViewModel.closePropertyDetails() },
              onSubmitEnquiry = { propId, message, date ->
                dwelloViewModel.submitEnquiry(propId, message, date)
              },
              onResetEnquiryState = { dwelloViewModel.resetEnquirySubmissionState() }
            )
          }
        }

        DwelloNavScreen.FIND_HOUSES -> {
          if (authenticatedRole == AuthAccountRole.TENANT) {
            FindHousesScreen(
              currentLanguage = currentLanguage,
              initialSearchQuery = initialSearchQuery,
              propertiesState = uiState.approvedPropertiesState,
              favouriteIds = uiState.favouriteIds,
              favouriteActionIds = uiState.favouriteActionIds,
              selectedPropertyDetailState = uiState.selectedPropertyDetailState,
              currentUser = authenticatedUser,
              enquirySubmitting = uiState.enquirySubmitting,
              enquirySubmitError = uiState.enquirySubmitError,
              enquirySubmittedSuccess = uiState.enquirySubmittedSuccess,
              onLanguageToggle = ::toggleLanguage,
              onRetryLoadProperties = { dwelloViewModel.loadTenantData() },
              onToggleFavourite = { id -> dwelloViewModel.toggleFavourite(id) },
              onOpenPropertyDetails = { prop -> dwelloViewModel.openPropertyDetails(prop) },
              onClosePropertyDetails = { dwelloViewModel.closePropertyDetails() },
              onSubmitEnquiry = { propId, message, date ->
                dwelloViewModel.submitEnquiry(propId, message, date)
              },
              onResetEnquiryState = { dwelloViewModel.resetEnquirySubmissionState() },
              onOpenMessageWithLandlord = { _ ->
                navigateWithRoleGuard(DwelloNavScreen.MESSAGES)
              }
            )
          }
        }

        DwelloNavScreen.POST_HOUSE -> {
          if (authenticatedRole == AuthAccountRole.LANDLORD) {
            PostHouseScreen(
              currentLanguage = currentLanguage,
              currentUser = authenticatedUser,
              isSubmitting = uiState.postPropertyLoading,
              submitError = uiState.postPropertyError,
              submittedPropertyId = uiState.postPropertySuccessId,
              onLanguageToggle = ::toggleLanguage,
              onSubmitProperty = { title, typeDisplay, beds, baths, rent, period, desc, area, address, landmark ->
                dwelloViewModel.createProperty(
                  title = title,
                  propertyTypeDisplay = typeDisplay,
                  bedrooms = beds,
                  bathrooms = baths,
                  rentAmount = rent,
                  rentalPeriod = period,
                  description = desc,
                  neighbourhood = area,
                  fullAddress = address,
                  landmark = landmark
                )
              },
              onResetSubmitState = { dwelloViewModel.resetPostPropertyState() },
              onSuccessNavigateHome = {
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_DASHBOARD)
              },
              onBack = {
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_DASHBOARD)
              }
            )
          }
        }

        DwelloNavScreen.MESSAGES -> {
          val activeRole = authenticatedRole
          if (activeRole != null) {
            MessagesScreen(
              currentLanguage = currentLanguage,
              userRole = activeRole,
              enquiriesState = when (activeRole) {
                AuthAccountRole.TENANT -> uiState.tenantEnquiriesState
                AuthAccountRole.LANDLORD -> uiState.landlordEnquiriesState
              },
              isActionLoading = uiState.landlordActionLoading,
              actionMessage = uiState.landlordActionMessage,
              actionError = uiState.landlordActionError,
              onRefreshEnquiries = {
                when (activeRole) {
                  AuthAccountRole.TENANT -> dwelloViewModel.loadTenantEnquiries()
                  AuthAccountRole.LANDLORD -> dwelloViewModel.loadLandlordEnquiries()
                }
              },
              onUpdateEnquiryStatus = { enquiryId, status ->
                dwelloViewModel.updateEnquiryStatus(enquiryId, status)
              },
              onClearFeedback = { dwelloViewModel.clearLandlordFeedback() },
              onLanguageToggle = ::toggleLanguage
            )
          }
        }

        DwelloNavScreen.PROFILE -> {
          val activeUser = authenticatedUser
          if (authenticatedRole != null && activeUser != null) {
            ProfileScreen(
              currentLanguage = currentLanguage,
              currentUser = activeUser,
              landlordPropertiesState = uiState.landlordPropertiesState,
              landlordEnquiriesState = uiState.landlordEnquiriesState,
              isLoggingOut = uiState.authLoading,
              isActionLoading = uiState.landlordActionLoading,
              onLanguageSelected = { newLang -> currentLanguage = newLang },
              onLanguageToggle = ::toggleLanguage,
              onLogoutClick = { dwelloViewModel.logout() },
              onNavigateToLandlordDashboard = {
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_DASHBOARD)
              },
              onToggleAvailability = { property ->
                dwelloViewModel.togglePropertyAvailability(property)
              }
            )
          }
        }

        DwelloNavScreen.LANDLORD_DASHBOARD -> {
          if (authenticatedRole == AuthAccountRole.LANDLORD) {
            LandlordDashboardScreen(
              currentLanguage = currentLanguage,
              currentUser = authenticatedUser,
              propertiesState = uiState.landlordPropertiesState,
              enquiriesState = uiState.landlordEnquiriesState,
              isActionLoading = uiState.landlordActionLoading,
              actionMessage = uiState.landlordActionMessage,
              actionError = uiState.landlordActionError,
              onLanguageToggle = ::toggleLanguage,
              onRetryLoad = { dwelloViewModel.loadLandlordData() },
              onNavigateToPostHouse = {
                dwelloViewModel.resetPostPropertyState()
                navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE)
              },
              onUpdateListing = { propertyId, title, rent, area, desc, address, landmark ->
                dwelloViewModel.updatePropertyListing(
                  propertyId = propertyId,
                  title = title,
                  rentAmount = rent,
                  neighbourhood = area,
                  description = desc,
                  fullAddress = address,
                  landmark = landmark
                )
              },
              onToggleAvailability = { property ->
                dwelloViewModel.togglePropertyAvailability(property)
              },
              onUpdateEnquiryStatus = { enquiryId, status ->
                dwelloViewModel.updateEnquiryStatus(enquiryId, status)
              },
              onClearFeedback = { dwelloViewModel.clearLandlordFeedback() },
              onBackToHome = {
                currentScreen = DwelloNavScreen.PROFILE
              }
            )
          }
        }
      }
    }
  }

  // Friendly Role Access Notice dialog
  val currentRole = authenticatedRole
  if (currentRole != null && roleRestrictedTarget != null) {
    val isTenantBlocked = currentRole == AuthAccountRole.TENANT
    AlertDialog(
      onDismissRequest = { roleRestrictedTarget = null },
      title = {
        Text(
          text = DwelloStrings.roleRestrictedTitle(currentLanguage),
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(
          text = if (isTenantBlocked) {
            DwelloStrings.roleRestrictedTenantToLandlord(currentLanguage)
          } else {
            DwelloStrings.roleRestrictedLandlordToTenant(currentLanguage)
          },
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            roleRestrictedTarget = null
            currentScreen = DwelloNavScreen.PROFILE
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          modifier = Modifier.testTag("role_notice_go_to_profile_btn")
        ) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Go to Profile" else "Open Profile",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { roleRestrictedTarget = null },
          modifier = Modifier.testTag("role_notice_close_btn")
        ) {
          Text(
            text = "Close",
            color = NavyTextSecondary
          )
        }
      }
    )
  }
}
