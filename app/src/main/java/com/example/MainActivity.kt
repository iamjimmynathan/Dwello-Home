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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.Apartment
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.screens.LandlordHomeScreen
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
  LANDLORD_HOME,
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

  // Synchronize auth state transitions automatically and clear navigation state on logout/role switch
  LaunchedEffect(uiState.sessionStatus, authenticatedRole) {
    when (uiState.sessionStatus) {
      SessionStatus.RESTORING -> Unit
      SessionStatus.AUTHENTICATED -> {
        val role = authenticatedRole
        if (role != null) {
          initialSearchQuery = null
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
        initialSearchQuery = null
        selectedRegistrationRole = AuthAccountRole.TENANT
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

  val unauthCheckedScreen = RoleNavigationRouter.redirectUnauthenticatedTarget(
    role = authenticatedRole,
    requestedScreen = currentScreen
  )
  val roleHomeScreen = authenticatedRole?.let { RoleNavigationRouter.postLoginDestination(it) }
    ?: DwelloNavScreen.WELCOME
  val effectiveScreen = if (
    authenticatedRole != null &&
    !RoleNavigationRouter.isAuthScreen(unauthCheckedScreen) &&
    !RoleNavigationRouter.canAccessScreen(authenticatedRole, unauthCheckedScreen)
  ) {
    roleHomeScreen
  } else {
    unauthCheckedScreen
  }
  val isAuthScreen = RoleNavigationRouter.isAuthScreen(effectiveScreen)

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
      currentScreen = roleHomeScreen
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      val navItems = RoleNavigationRouter.bottomNavScreensForRole(authenticatedRole)
      if (!isAuthScreen && authenticatedRole != null && navItems.isNotEmpty()) {
        val labelFontSize = if (navItems.size >= 5) 10.sp else 11.sp
        NavigationBar(
          containerColor = Color.White,
          contentColor = DeepNavyText,
          tonalElevation = 6.dp,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          navItems.forEach { navScreen ->
            val isSelected = effectiveScreen == navScreen
            val (labelText, selectedIcon, unselectedIcon, tag) = when (navScreen) {
              DwelloNavScreen.HOME,
              DwelloNavScreen.LANDLORD_HOME -> QuadIconItem(
                label = DwelloStrings.navHome(currentLanguage),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_item_home"
              )
              DwelloNavScreen.FIND_HOUSES -> QuadIconItem(
                label = DwelloStrings.navFind(currentLanguage),
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                testTag = "nav_item_find_houses"
              )
              DwelloNavScreen.POST_HOUSE -> QuadIconItem(
                label = DwelloStrings.navPost(currentLanguage),
                selectedIcon = Icons.Filled.AddHome,
                unselectedIcon = Icons.Outlined.AddHome,
                testTag = "nav_item_post_house"
              )
              DwelloNavScreen.LANDLORD_DASHBOARD -> QuadIconItem(
                label = DwelloStrings.navMyProperties(currentLanguage),
                selectedIcon = Icons.Filled.Apartment,
                unselectedIcon = Icons.Outlined.Apartment,
                testTag = "nav_item_my_properties"
              )
              DwelloNavScreen.MESSAGES -> QuadIconItem(
                label = DwelloStrings.navMessages(currentLanguage),
                selectedIcon = Icons.Filled.Chat,
                unselectedIcon = Icons.Outlined.Chat,
                testTag = "nav_item_messages"
              )
              DwelloNavScreen.PROFILE -> QuadIconItem(
                label = DwelloStrings.navProfile(currentLanguage),
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                testTag = "nav_item_profile"
              )
              else -> QuadIconItem(
                label = DwelloStrings.navHome(currentLanguage),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_item_home"
              )
            }

            NavigationBarItem(
              selected = isSelected,
              onClick = {
                if (navScreen == DwelloNavScreen.FIND_HOUSES) {
                  initialSearchQuery = null
                }
                navigateWithRoleGuard(navScreen)
              },
              icon = {
                Icon(
                  imageVector = if (isSelected) selectedIcon else unselectedIcon,
                  contentDescription = labelText,
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  text = labelText,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = labelFontSize
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
              modifier = Modifier.testTag(tag)
            )
          }
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
            authError = uiState.authError,
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

        DwelloNavScreen.LANDLORD_HOME -> {
          if (authenticatedRole == AuthAccountRole.LANDLORD) {
            LandlordHomeScreen(
              currentLanguage = currentLanguage,
              currentUser = authenticatedUser,
              propertiesState = uiState.landlordPropertiesState,
              enquiriesState = uiState.landlordEnquiriesState,
              onLanguageToggle = ::toggleLanguage,
              onRefreshData = { dwelloViewModel.loadLandlordData() },
              onNavigateToPostHouse = {
                dwelloViewModel.resetPostPropertyState()
                navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE)
              },
              onNavigateToMyProperties = {
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_DASHBOARD)
              },
              onNavigateToEnquiries = {
                navigateWithRoleGuard(DwelloNavScreen.MESSAGES)
              }
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
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_HOME)
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
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_HOME)
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

private data class QuadIconItem(
  val label: String,
  val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
  val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
  val testTag: String
)
