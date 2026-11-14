package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.DwelloStrings
import com.example.ui.components.AuthTopBar
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun AccountTypeSelectionScreen(
  currentLanguage: AppLanguage,
  initialSelectedRole: AuthAccountRole,
  onLanguageToggle: () -> Unit,
  onContinueToRegistration: (AuthAccountRole) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  var selectedRole by remember(initialSelectedRole) { mutableStateOf(initialSelectedRole) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AuthTopBar(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle,
      onBackClick = onBack,
      backTestTag = "account_type_back_button"
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
      Text(
        text = DwelloStrings.accountTypeTitle(currentLanguage),
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText
        ),
        modifier = Modifier.testTag("account_type_title")
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = DwelloStrings.accountTypeSubtitle(currentLanguage),
        style = MaterialTheme.typography.bodyLarge.copy(
          color = NavyTextSecondary,
          lineHeight = 22.sp
        )
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Strictly TWO public account types: TENANT and LANDLORD
      AccountTypeOptionCard(
        roleBadge = DwelloStrings.tenantAccountLabel(currentLanguage),
        heading = DwelloStrings.tenantAccountHeading(currentLanguage),
        description = DwelloStrings.tenantAccountDescription(currentLanguage),
        icon = Icons.Default.Home,
        isSelected = selectedRole == AuthAccountRole.TENANT,
        onClick = { selectedRole = AuthAccountRole.TENANT },
        testTag = "account_type_tenant_card"
      )

      Spacer(modifier = Modifier.height(16.dp))

      AccountTypeOptionCard(
        roleBadge = DwelloStrings.landlordAccountLabel(currentLanguage),
        heading = DwelloStrings.landlordAccountHeading(currentLanguage),
        description = DwelloStrings.landlordAccountDescription(currentLanguage),
        icon = Icons.Default.AddHome,
        isSelected = selectedRole == AuthAccountRole.LANDLORD,
        onClick = { selectedRole = AuthAccountRole.LANDLORD },
        testTag = "account_type_landlord_card"
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Continue to Registration
      Button(
        onClick = { onContinueToRegistration(selectedRole) },
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("continue_to_registration_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SkyBluePrimary,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        Text(
          text = DwelloStrings.continueToRegistrationBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White
          )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = Color.White
        )
      }
    }
  }
}

@Composable
private fun AccountTypeOptionCard(
  roleBadge: String,
  heading: String,
  description: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) VeryLightBlueBackground else Color.White
    ),
    border = BorderStroke(
      width = if (isSelected) 2.5.dp else 1.dp,
      color = if (isSelected) SkyBluePrimary else CardBorderSubtle
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(if (isSelected) SkyBluePrimary else SoftBlueAccent),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = roleBadge,
          tint = if (isSelected) Color.White else SkyBluePrimary,
          modifier = Modifier.size(30.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = if (isSelected) SkyBluePrimary else SoftBlueAccent,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = roleBadge,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else SkyBluePrimary,
                letterSpacing = 0.8.sp
              )
            )
          }

          if (isSelected) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Selected",
              tint = SkyBluePrimary,
              modifier = Modifier.size(24.dp)
            )
          } else {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .padding(1.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = heading,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = DeepNavyText,
            fontSize = 18.sp
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = NavyTextSecondary,
            lineHeight = 20.sp
          )
        )
      }
    }
  }
}
