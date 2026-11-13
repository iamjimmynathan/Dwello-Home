package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.NigerianPhoneNormalizer
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun AuthTopBar(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onBackClick: () -> Unit,
  backTestTag: String = "auth_back_button",
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Color.White,
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(48.dp)
          .testTag(backTestTag)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = if (currentLanguage == AppLanguage.ENGLISH) "Back" else "Go Back",
          tint = DeepNavyText
        )
      }

      AuthLanguagePill(
        currentLanguage = currentLanguage,
        onLanguageToggle = onLanguageToggle
      )
    }
  }
}

@Composable
fun AuthLanguagePill(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .heightIn(min = 48.dp)
      .clip(RoundedCornerShape(24.dp))
      .clickable(onClick = onLanguageToggle)
      .testTag("auth_language_toggle"),
    color = VeryLightBlueBackground,
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.dp, SoftBlueBorder)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Translate,
        contentDescription = if (currentLanguage == AppLanguage.ENGLISH) "Switch Language" else "Change Language",
        tint = SkyBluePrimary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH) "English | Pidgin" else "Pidgin | English",
        style = MaterialTheme.typography.labelLarge.copy(
          color = DeepNavyText,
          fontWeight = FontWeight.Bold
        )
      )
    }
  }
}

@Composable
fun NigerianPhoneInputField(
  phoneInput: String,
  onPhoneChange: (String) -> Unit,
  currentLanguage: AppLanguage,
  errorText: String?,
  fieldTestTag: String,
  errorTestTag: String,
  modifier: Modifier = Modifier
) {
  var showCountryInfoDialog by remember { mutableStateOf(false) }
  val normalizedPreview = remember(phoneInput) {
    NigerianPhoneNormalizer.normalizeToE164(phoneInput)
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = DwelloStrings.phoneNumberLabel(currentLanguage),
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        color = DeepNavyText
      )
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Nigeria +234 Country Code Selector/Prefix
      Surface(
        modifier = Modifier
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .clickable { showCountryInfoDialog = true }
          .testTag("${fieldTestTag}_country_code"),
        color = VeryLightBlueBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SoftBlueBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "🇳🇬", fontSize = 18.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "+234",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DeepNavyText
            )
          )
        }
      }

      OutlinedTextField(
        value = phoneInput,
        onValueChange = onPhoneChange,
        modifier = Modifier
          .weight(1f)
          .testTag(fieldTestTag),
        placeholder = {
          Text(
            text = DwelloStrings.phoneNumberPlaceholder(currentLanguage),
            color = NavyTextSecondary.copy(alpha = 0.65f)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = SkyBluePrimary
          )
        },
        isError = errorText != null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = SkyBluePrimary,
          unfocusedBorderColor = SoftBlueBorder,
          errorBorderColor = ErrorRed,
          focusedTextColor = DeepNavyText,
          unfocusedTextColor = DeepNavyText
        )
      )
    }

    if (errorText != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = errorText,
        style = MaterialTheme.typography.bodySmall.copy(
          color = ErrorRed,
          fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.testTag(errorTestTag)
      )
    } else if (normalizedPreview != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH)
          "Normalized format: $normalizedPreview"
        else
          "Format ready: $normalizedPreview",
        style = MaterialTheme.typography.labelSmall.copy(
          color = SuccessGreen,
          fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.testTag("${fieldTestTag}_normalized")
      )
    }
  }

  if (showCountryInfoDialog) {
    AlertDialog(
      onDismissRequest = { showCountryInfoDialog = false },
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Nigeria (+234) Phone Numbers" else "Nigeria (+234) Phone Numbers",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH)
            "Dwello Homes serves Benin City, Nigeria (+234). You can enter your number starting with 0 (e.g. 0803 123 4567), 10 digits (8031234567), or +234 (e.g. +2348031234567). All valid numbers are normalized consistently to +234 format."
          else
            "Dwello Homes dey work for Benin City, Nigeria (+234). You fit type your number with 0 (like 0803 123 4567), 10 digits, or +234. All numbers go save cleanly as +234 format.",
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp)
        )
      },
      confirmButton = {
        Button(
          onClick = { showCountryInfoDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}

@Composable
fun DemoAuthNoticeBanner(
  currentLanguage: AppLanguage,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = VeryLightBlueBackground,
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, SoftBlueBorder)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(SoftBlueAccent),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = SkyBlueDark,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = DwelloStrings.demoAuthBannerText(currentLanguage),
        style = MaterialTheme.typography.bodySmall.copy(
          color = DeepNavyText,
          lineHeight = 18.sp
        )
      )
    }
  }
}
