package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.AuthFormValidator
import com.example.data.DwelloStrings
import com.example.data.LoginValidationResult
import com.example.ui.components.AuthTopBar
import com.example.ui.components.NigerianPhoneInputField
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun LoginScreen(
  currentLanguage: AppLanguage,
  isLoading: Boolean,
  serverError: String?,
  onLanguageToggle: () -> Unit,
  onForgotPasswordClick: () -> Unit,
  onRegisterClick: () -> Unit,
  onLoginSubmit: (normalizedPhone: String, passwordChars: CharArray) -> Unit,
  onClearError: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  var phoneInput by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var validationResult by remember { mutableStateOf<LoginValidationResult?>(null) }

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
      backTestTag = "login_back_button"
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Compact Dwello Logo
      Surface(
        modifier = Modifier
          .size(74.dp)
          .testTag("login_logo"),
        shape = RoundedCornerShape(18.dp),
        color = VeryLightBlueBackground,
        border = BorderStroke(1.5.dp, SoftBlueBorder)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Image(
            painter = painterResource(id = R.drawable.ic_dwello_logo),
            contentDescription = "DWELLO HOMES Logo",
            modifier = Modifier
              .size(58.dp)
              .clip(RoundedCornerShape(14.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = DwelloStrings.loginTitle(currentLanguage),
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText
        ),
        modifier = Modifier.testTag("login_title")
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = DwelloStrings.loginSubtitle(currentLanguage),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = NavyTextSecondary
        )
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Nigerian Phone Number (no email required)
      NigerianPhoneInputField(
        phoneInput = phoneInput,
        onPhoneChange = {
          phoneInput = it
          if (serverError != null) onClearError()
        },
        currentLanguage = currentLanguage,
        errorText = validationResult?.phoneError,
        fieldTestTag = "login_phone_input",
        errorTestTag = "login_phone_error"
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Password Input
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = DwelloStrings.passwordLabel(currentLanguage),
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            if (serverError != null) onClearError()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input"),
          placeholder = {
            Text(
              text = DwelloStrings.passwordHelper(currentLanguage),
              color = NavyTextSecondary.copy(alpha = 0.65f)
            )
          },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SkyBluePrimary)
          },
          trailingIcon = {
            IconButton(
              onClick = { passwordVisible = !passwordVisible },
              modifier = Modifier
                .size(48.dp)
                .testTag("login_password_visibility_toggle")
            ) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = NavyTextSecondary
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          isError = validationResult?.passwordError != null,
          singleLine = true,
          enabled = !isLoading,
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
        validationResult?.passwordError?.let { err ->
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = err,
            style = MaterialTheme.typography.bodySmall.copy(
              color = ErrorRed,
              fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.testTag("login_password_error")
          )
        }
      }

      // Forgot Password Link
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(
          onClick = onForgotPasswordClick,
          enabled = !isLoading,
          modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("login_forgot_password_link")
        ) {
          Text(
            text = DwelloStrings.forgotPasswordLink(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = SkyBluePrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      if (!serverError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_server_error"),
          color = Color(0xFFFEF2F2),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Color(0xFFFECACA))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = ErrorRed,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = serverError,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.Medium
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Login Button
      Button(
        onClick = {
          val result = AuthFormValidator.validateLogin(
            phoneNumber = phoneInput,
            password = password,
            language = currentLanguage
          )
          validationResult = result
          if (result.isValid && result.normalizedPhone != null) {
            val passwordChars = password.toCharArray()
            onLoginSubmit(result.normalizedPhone, passwordChars)
          }
        },
        enabled = !isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("login_submit_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SkyBluePrimary,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.5.dp,
            modifier = Modifier
              .size(24.dp)
              .testTag("login_loading_indicator")
          )
        } else {
          Text(
            text = DwelloStrings.loginBtn(currentLanguage),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 17.sp,
              color = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Don't have an account? Register
      TextButton(
        onClick = onRegisterClick,
        enabled = !isLoading,
        modifier = Modifier
          .heightIn(min = 48.dp)
          .testTag("login_register_link")
      ) {
        Text(
          text = DwelloStrings.dontHaveAccountPrompt(currentLanguage),
          style = MaterialTheme.typography.bodyMedium.copy(
            color = SkyBluePrimary,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }
  }
}
