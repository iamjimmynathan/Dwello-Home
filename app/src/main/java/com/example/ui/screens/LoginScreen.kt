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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.AuthAccountRole
import com.example.data.AuthBackendResult
import com.example.data.AuthFormValidator
import com.example.data.DemoAuthGateway
import com.example.data.DwelloStrings
import com.example.data.LoginValidationResult
import com.example.ui.components.AuthTopBar
import com.example.ui.components.DemoAuthNoticeBanner
import com.example.ui.components.NigerianPhoneInputField
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onForgotPasswordClick: () -> Unit,
  onRegisterClick: () -> Unit,
  onPreviewAuthenticatedRole: (AuthAccountRole) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  val scope = rememberCoroutineScope()

  var phoneInput by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var validationResult by remember { mutableStateOf<LoginValidationResult?>(null) }
  var demoDialogNormalizedPhone by remember { mutableStateOf<String?>(null) }

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
        onPhoneChange = { phoneInput = it },
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
          onValueChange = { password = it },
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

      Spacer(modifier = Modifier.height(8.dp))

      // Honest Demo Notice
      DemoAuthNoticeBanner(currentLanguage = currentLanguage)

      Spacer(modifier = Modifier.height(20.dp))

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
            scope.launch {
              val passwordChars = password.toCharArray()
              val gatewayResult = DemoAuthGateway.loginWithPhone(
                normalizedPhone = result.normalizedPhone,
                rawPasswordChars = passwordChars
              )
              if (gatewayResult is AuthBackendResult.DemoValidationPassed) {
                demoDialogNormalizedPhone = gatewayResult.normalizedPhone
              }
            }
          }
        },
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
        Text(
          text = DwelloStrings.loginBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Don't have an account? Register
      TextButton(
        onClick = onRegisterClick,
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

  // Honest Frontend Login Preview Dialog (never fakes real authentication)
  demoDialogNormalizedPhone?.let { normalizedPhone ->
    AlertDialog(
      onDismissRequest = { demoDialogNormalizedPhone = null },
      title = {
        Text(
          text = DwelloStrings.loginDemoDialogTitle(currentLanguage),
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = DwelloStrings.loginDemoDialogBody(currentLanguage, normalizedPhone),
            style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = {
              demoDialogNormalizedPhone = null
              onPreviewAuthenticatedRole(AuthAccountRole.TENANT)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("login_preview_tenant_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
          ) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Preview as Tenant (Home)" else "Test as Tenant (Home)",
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = {
              demoDialogNormalizedPhone = null
              onPreviewAuthenticatedRole(AuthAccountRole.LANDLORD)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("login_preview_landlord_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, SkyBluePrimary)
          ) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Preview as Landlord (Dashboard)" else "Test as Landlord (Dashboard)",
              color = SkyBluePrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { demoDialogNormalizedPhone = null }) {
          Text("Close", color = NavyTextSecondary)
        }
      }
    )
  }
}
