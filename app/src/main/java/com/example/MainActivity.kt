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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.data.DwelloStrings
import com.example.ui.screens.FindHousesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.PostHouseScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent

enum class DwelloNavScreen {
  HOME,
  FIND_HOUSES,
  POST_HOUSE,
  MESSAGES,
  PROFILE
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
  var currentScreen by remember { mutableStateOf(DwelloNavScreen.HOME) }
  var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
  var initialSearchQuery by remember { mutableStateOf<String?>(null) }

  // Handle system back navigation to return to Home screen
  if (currentScreen != DwelloNavScreen.HOME) {
    BackHandler {
      currentScreen = DwelloNavScreen.HOME
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(
        containerColor = Color.White,
        contentColor = DeepNavyText,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
      ) {
        // 1. Home
        val isHome = currentScreen == DwelloNavScreen.HOME
        NavigationBarItem(
          selected = isHome,
          onClick = { currentScreen = DwelloNavScreen.HOME },
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

        // 2. Find Houses
        val isFind = currentScreen == DwelloNavScreen.FIND_HOUSES
        NavigationBarItem(
          selected = isFind,
          onClick = {
            initialSearchQuery = null
            currentScreen = DwelloNavScreen.FIND_HOUSES
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

        // 3. Post House
        val isPost = currentScreen == DwelloNavScreen.POST_HOUSE
        NavigationBarItem(
          selected = isPost,
          onClick = { currentScreen = DwelloNavScreen.POST_HOUSE },
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

        // 4. Messages
        val isMessages = currentScreen == DwelloNavScreen.MESSAGES
        NavigationBarItem(
          selected = isMessages,
          onClick = { currentScreen = DwelloNavScreen.MESSAGES },
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

        // 5. Profile
        val isProfile = currentScreen == DwelloNavScreen.PROFILE
        NavigationBarItem(
          selected = isProfile,
          onClick = { currentScreen = DwelloNavScreen.PROFILE },
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
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MainBackgroundWhite)
    ) {
      when (currentScreen) {
        DwelloNavScreen.HOME -> {
          HomeScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = {
              currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.PIDGIN else AppLanguage.ENGLISH
            },
            onNavigateToFindHouses = { query ->
              initialSearchQuery = query
              currentScreen = DwelloNavScreen.FIND_HOUSES
            },
            onNavigateToPostHouse = {
              currentScreen = DwelloNavScreen.POST_HOUSE
            }
          )
        }

        DwelloNavScreen.FIND_HOUSES -> {
          FindHousesScreen(
            currentLanguage = currentLanguage,
            initialSearchQuery = initialSearchQuery,
            onLanguageToggle = {
              currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.PIDGIN else AppLanguage.ENGLISH
            },
            onOpenMessageWithLandlord = { _ ->
              currentScreen = DwelloNavScreen.MESSAGES
            }
          )
        }

        DwelloNavScreen.POST_HOUSE -> {
          PostHouseScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = {
              currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.PIDGIN else AppLanguage.ENGLISH
            },
            onSuccessNavigateHome = {
              currentScreen = DwelloNavScreen.HOME
            }
          )
        }

        DwelloNavScreen.MESSAGES -> {
          MessagesScreen(
            currentLanguage = currentLanguage,
            onLanguageToggle = {
              currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.PIDGIN else AppLanguage.ENGLISH
            }
          )
        }

        DwelloNavScreen.PROFILE -> {
          ProfileScreen(
            currentLanguage = currentLanguage,
            onLanguageSelected = { newLang -> currentLanguage = newLang },
            onLanguageToggle = {
              currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.PIDGIN else AppLanguage.ENGLISH
            }
          )
        }
      }
    }
  }
}
