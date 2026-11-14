package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.DwelloStrings
import com.example.data.RoleNavigationRouter
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
fun DwelloApp() {
  var currentScreen by remember { mutableStateOf(DwelloNavScreen.WELCOME) }
  var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
  var initialSearchQuery by remember { mutableStateOf<String?>(null) }
  var authenticatedRole by remember { mutableStateOf<AuthAccountRole?>(null) }
  var selectedRegistrationRole by remember { mutableStateOf(AuthAccountRole.TENANT) }
  var roleRestrictedTarget by remember { mutableStateOf<DwelloNavScreen?>(null) }

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

          // 4. Messages (Shared)
          val isMessages = effectiveScreen == DwelloNavScreen.MESSAGES
          NavigationBarItem(
            selected = isMessages,
            onClick = { navigateWithRoleGuard(DwelloNavScreen.MESSAGES) },
            icon = {
              Icon(
                imageVector = if (isMessages) Icons.Filled.Chat else Icons.Outlined.Chat,
                contentDescription = "Messages",
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

          // 5. Profile / Landlord
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
            onGetStartedClick = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION },
            onLoginClick = { currentScreen = DwelloNavScreen.LOGIN }
          )
        }

        DwelloNavScreen.ACCOUNT_TYPE_SELECTION -> {
          AccountTypeSelectionScreen(
            currentLanguage = currentLanguage,
            initialSelectedRole = selectedRegistrationRole,
            onLanguageToggle = ::toggleLanguage,
            onContinueToRegistration = { role ->
              selectedRegistrationRole = role
              currentScreen = DwelloNavScreen.REGISTRATION
            },
            onBack = { currentScreen = DwelloNavScreen.WELCOME }
          )
        }

        DwelloNavScreen.REGISTRATION -> {
          RegistrationScreen(
            currentLanguage = currentLanguage,
            selectedRole = selectedRegistrationRole,
            onLanguageToggle = ::toggleLanguage,
            onChangeRoleClick = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION },
            onNavigateToLogin = { currentScreen = DwelloNavScreen.LOGIN },
            onPreviewAuthenticatedRole = { role ->
              authenticatedRole = role
              roleRestrictedTarget = null
              currentScreen = RoleNavigationRouter.postLoginDestination(role)
            },
            onBack = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION }
          )
        }

        DwelloNavScreen.LOGIN -> {
          LoginScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = ::toggleLanguage,
            onForgotPasswordClick = { currentScreen = DwelloNavScreen.FORGOT_PASSWORD },
            onRegisterClick = { currentScreen = DwelloNavScreen.ACCOUNT_TYPE_SELECTION },
            onPreviewAuthenticatedRole = { role ->
              authenticatedRole = role
              roleRestrictedTarget = null
              currentScreen = RoleNavigationRouter.postLoginDestination(role)
            },
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
              onLanguageToggle = ::toggleLanguage,
              onNavigateToFindHouses = { query ->
                initialSearchQuery = query
                navigateWithRoleGuard(DwelloNavScreen.FIND_HOUSES)
              },
              onNavigateToPostHouse = {
                navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE)
              }
            )
          }
        }

        DwelloNavScreen.FIND_HOUSES -> {
          if (authenticatedRole == AuthAccountRole.TENANT) {
            FindHousesScreen(
              currentLanguage = currentLanguage,
              initialSearchQuery = initialSearchQuery,
              onLanguageToggle = ::toggleLanguage,
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
              onLanguageToggle = ::toggleLanguage,
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
          if (authenticatedRole != null) {
            MessagesScreen(
              currentLanguage = currentLanguage,
              onLanguageToggle = ::toggleLanguage
            )
          }
        }

        DwelloNavScreen.PROFILE -> {
          val activeRole = authenticatedRole
          if (activeRole != null) {
            ProfileScreen(
              currentLanguage = currentLanguage,
              onLanguageSelected = { newLang -> currentLanguage = newLang },
              onLanguageToggle = ::toggleLanguage,
              activeRole = activeRole,
              onRoleChange = { newRole ->
                authenticatedRole = newRole
                roleRestrictedTarget = null
              },
              onOpenWelcomeAuth = {
                authenticatedRole = null
                roleRestrictedTarget = null
                currentScreen = DwelloNavScreen.WELCOME
              },
              onNavigateToLandlordDashboard = {
                navigateWithRoleGuard(DwelloNavScreen.LANDLORD_DASHBOARD)
              }
            )
          }
        }

        DwelloNavScreen.LANDLORD_DASHBOARD -> {
          if (authenticatedRole == AuthAccountRole.LANDLORD) {
            LandlordDashboardScreen(
              currentLanguage = currentLanguage,
              onLanguageToggle = ::toggleLanguage,
              onNavigateToPostHouse = {
                navigateWithRoleGuard(DwelloNavScreen.POST_HOUSE)
              },
              onBackToHome = {
                currentScreen = DwelloNavScreen.PROFILE
              }
            )
          }
        }
      }
    }
  }

  // Friendly Role Access Notice dialog (only shown when authenticated user taps a screen disallowed for their role)
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
            text = if (currentLanguage == AppLanguage.ENGLISH) "Close" else "Close",
            color = NavyTextSecondary
          )
        }
      }
    )
  }
}
