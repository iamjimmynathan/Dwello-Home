package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthBackendResult
import com.example.data.AuthFormValidator
import com.example.data.DemoAuthGateway
import com.example.data.DwelloStrings
import com.example.data.ForgotPasswordValidationResult
import com.example.ui.components.AuthTopBar
import com.example.ui.components.NigerianPhoneInputField
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onBackToLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBackToLogin)

  val scope = rememberCoroutineScope()

  var phoneInput by remember { mutableStateOf("") }
  var validationResult by remember { mutableStateOf<ForgotPasswordValidationResult?>(null) }
  var submittedNormalizedPhone by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AuthTopBar(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle,
      onBackClick = onBackToLogin,
      backTestTag = "forgot_password_back_button"
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        modifier = Modifier.size(68.dp),
        shape = RoundedCornerShape(18.dp),
        color = VeryLightBlueBackground,
        border = BorderStroke(1.5.dp, SoftBlueBorder)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.LockReset,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(34.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = DwelloStrings.forgotPasswordTitle(currentLanguage),
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText
        ),
        modifier = Modifier.testTag("forgot_password_title")
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = DwelloStrings.forgotPasswordSubtitle(currentLanguage),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = NavyTextSecondary,
          lineHeight = 20.sp
        )
      )

      Spacer(modifier = Modifier.height(24.dp))

      NigerianPhoneInputField(
        phoneInput = phoneInput,
        onPhoneChange = {
          phoneInput = it
          submittedNormalizedPhone = null
        },
        currentLanguage = currentLanguage,
        errorText = validationResult?.phoneError,
        fieldTestTag = "forgot_password_phone_input",
        errorTestTag = "forgot_password_phone_error"
      )

      Spacer(modifier = Modifier.height(22.dp))

      Button(
        onClick = {
          val result = AuthFormValidator.validateForgotPassword(
            phoneNumber = phoneInput,
            language = currentLanguage
          )
          validationResult = result
          if (result.isValid && result.normalizedPhone != null) {
            scope.launch {
              val response = DemoAuthGateway.requestPasswordResetOtp(result.normalizedPhone)
              if (response is AuthBackendResult.BackendNotConnected) {
                submittedNormalizedPhone = result.normalizedPhone
              }
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("forgot_password_continue_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SkyBluePrimary,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        Text(
          text = DwelloStrings.continueBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color.White
          )
        )
      }

      // Honest Backend/OTP Pending Explanation (No fake OTP code or simulated SMS)
      submittedNormalizedPhone?.let { normalizedPhone ->
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("forgot_password_backend_notice"),
          color = VeryLightBlueBackground,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.5.dp, SkyBluePrimary)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
          ) {
            Surface(
              color = SoftBlueAccent,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.size(32.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = SkyBlueDark,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH)
                  "Phone Number Ready ($normalizedPhone)"
                else
                  "Phone Number Ready ($normalizedPhone)",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = DwelloStrings.otpBackendPendingNotice(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DeepNavyText,
                  lineHeight = 19.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      TextButton(
        onClick = onBackToLogin,
        modifier = Modifier
          .heightIn(min = 48.dp)
          .testTag("forgot_password_back_to_login_link")
      ) {
        Text(
          text = DwelloStrings.backToLoginLink(currentLanguage),
          style = MaterialTheme.typography.bodyMedium.copy(
            color = SkyBluePrimary,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }
  }
}
