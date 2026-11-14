package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.ui.components.AuthLanguagePill
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun WelcomeScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onGetStartedClick: () -> Unit,
  onLoginClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Language Selector Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      AuthLanguagePill(
        currentLanguage = currentLanguage,
        onLanguageToggle = onLanguageToggle
      )
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Hero Branding Column
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        modifier = Modifier
          .size(116.dp)
          .testTag("welcome_logo"),
        shape = RoundedCornerShape(28.dp),
        color = VeryLightBlueBackground,
        border = BorderStroke(2.dp, SoftBlueBorder),
        shadowElevation = 4.dp
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_dwello_logo),
            contentDescription = "DWELLO HOMES Logo",
            modifier = Modifier
              .size(92.dp)
              .clip(RoundedCornerShape(20.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "DWELLO HOMES",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          color = SkyBluePrimary,
          letterSpacing = 1.2.sp
        ),
        modifier = Modifier.testTag("welcome_brand_name")
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = DwelloStrings.welcomeTagline(currentLanguage),
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.SemiBold,
          color = NavyTextSecondary
        ),
        modifier = Modifier.testTag("welcome_tagline")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Surface(
        color = VeryLightBlueBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftBlueBorder),
        modifier = Modifier.testTag("welcome_location_badge")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = DwelloStrings.welcomeLocationLabel(currentLanguage),
            style = MaterialTheme.typography.labelMedium.copy(
              color = DeepNavyText,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Heading & Supporting Description
      Text(
        text = DwelloStrings.welcomeHeading(currentLanguage),
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText,
          lineHeight = 38.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("welcome_heading")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = DwelloStrings.welcomeSupportingText(currentLanguage),
        style = MaterialTheme.typography.bodyLarge.copy(
          color = NavyTextSecondary,
          lineHeight = 24.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .testTag("welcome_supporting_text")
      )

      Spacer(modifier = Modifier.height(38.dp))

      // Get Started Button (Primary Sky Blue)
      Button(
        onClick = onGetStartedClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("welcome_get_started_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SkyBluePrimary,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        Text(
          text = DwelloStrings.getStartedBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
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

      Spacer(modifier = Modifier.height(14.dp))

      // Login Button (White with Sky Blue border)
      OutlinedButton(
        onClick = onLoginClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("welcome_login_button"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, SkyBluePrimary),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = Color.White,
          contentColor = SkyBluePrimary
        )
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Login,
          contentDescription = null,
          tint = SkyBluePrimary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = DwelloStrings.loginBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = SkyBluePrimary
          )
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
