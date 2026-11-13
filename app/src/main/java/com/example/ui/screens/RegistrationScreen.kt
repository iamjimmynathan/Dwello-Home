package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthBackendResult
import com.example.data.AuthFormValidator
import com.example.data.DemoAuthGateway
import com.example.data.DwelloStrings
import com.example.data.RegistrationValidationResult
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
fun RegistrationScreen(
  currentLanguage: AppLanguage,
  selectedRole: AuthAccountRole,
  onLanguageToggle: () -> Unit,
  onChangeRoleClick: () -> Unit,
  onNavigateToLogin: () -> Unit,
  onPreviewAuthenticatedRole: (AuthAccountRole) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  val scope = rememberCoroutineScope()

  var fullName by remember { mutableStateOf("") }
  var phoneInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var acceptedTerms by remember { mutableStateOf(false) }

  var validationResult by remember { mutableStateOf<RegistrationValidationResult?>(null) }
  var demoDialogNormalizedPhone by remember { mutableStateOf<String?>(null) }
  var showTermsDialog by remember { mutableStateOf(false) }

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
      backTestTag = "registration_back_button"
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = DwelloStrings.registerTitle(currentLanguage),
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText
        ),
        modifier = Modifier.testTag("registration_title")
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = DwelloStrings.registerSubtitle(currentLanguage),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = NavyTextSecondary,
          lineHeight = 20.sp
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Selected Account Type Display Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("registration_selected_role_banner"),
        color = VeryLightBlueBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, SkyBluePrimary)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = if (selectedRole == AuthAccountRole.TENANT) Icons.Default.Home else Icons.Default.AddHome,
              contentDescription = null,
              tint = SkyBluePrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = DwelloStrings.selectedRoleBannerLabel(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
              )
              Text(
                text = if (selectedRole == AuthAccountRole.TENANT)
                  DwelloStrings.tenantAccountLabel(currentLanguage)
                else
                  DwelloStrings.landlordAccountLabel(currentLanguage),
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DeepNavyText
                )
              )
            }
          }

          TextButton(
            onClick = onChangeRoleClick,
            modifier = Modifier
              .heightIn(min = 48.dp)
              .testTag("registration_change_role_button")
          ) {
            Text(
              text = DwelloStrings.changeRoleText(currentLanguage),
              color = SkyBluePrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Full Name (Required)
      Text(
        text = DwelloStrings.fullNameLabel(currentLanguage),
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("register_fullname_input"),
        placeholder = {
          Text(
            text = DwelloStrings.fullNamePlaceholder(currentLanguage),
            color = NavyTextSecondary.copy(alpha = 0.65f)
          )
        },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SkyBluePrimary)
        },
        isError = validationResult?.fullNameError != null,
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
      validationResult?.fullNameError?.let { err ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = err,
          style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed, fontWeight = FontWeight.Medium),
          modifier = Modifier.testTag("register_fullname_error")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Nigerian Phone Number (Required)
      NigerianPhoneInputField(
        phoneInput = phoneInput,
        onPhoneChange = { phoneInput = it },
        currentLanguage = currentLanguage,
        errorText = validationResult?.phoneError,
        fieldTestTag = "register_phone_input",
        errorTestTag = "register_phone_error"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Email Address (Optional)
      Text(
        text = DwelloStrings.emailOptionalLabel(currentLanguage),
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = emailInput,
        onValueChange = { emailInput = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("register_email_input"),
        placeholder = {
          Text(
            text = DwelloStrings.emailPlaceholder(currentLanguage),
            color = NavyTextSecondary.copy(alpha = 0.65f)
          )
        },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SkyBluePrimary)
        },
        isError = validationResult?.emailError != null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
      validationResult?.emailError?.let { err ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = err,
          style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed, fontWeight = FontWeight.Medium),
          modifier = Modifier.testTag("register_email_error")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Password (Required, min 8 characters)
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
          .testTag("register_password_input"),
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
              .testTag("register_password_visibility_toggle")
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
          style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed, fontWeight = FontWeight.Medium),
          modifier = Modifier.testTag("register_password_error")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Confirm Password (Required)
      Text(
        text = DwelloStrings.confirmPasswordLabel(currentLanguage),
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("register_confirm_password_input"),
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
            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
            modifier = Modifier
              .size(48.dp)
              .testTag("register_confirm_password_visibility_toggle")
          ) {
            Icon(
              imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
              tint = NavyTextSecondary
            )
          }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = validationResult?.confirmPasswordError != null,
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
      validationResult?.confirmPasswordError?.let { err ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = err,
          style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed, fontWeight = FontWeight.Medium),
          modifier = Modifier.testTag("register_confirm_password_error")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Terms & Conditions + Privacy Policy Checkbox
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
          .clip(RoundedCornerShape(10.dp))
          .clickable { acceptedTerms = !acceptedTerms }
          .padding(vertical = 4.dp)
          .testTag("register_terms_row"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = acceptedTerms,
          onCheckedChange = { acceptedTerms = it },
          colors = CheckboxDefaults.colors(
            checkedColor = SkyBluePrimary,
            uncheckedColor = NavyTextSecondary
          ),
          modifier = Modifier.testTag("register_terms_checkbox")
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = DwelloStrings.acceptTermsLabel(currentLanguage),
          style = MaterialTheme.typography.bodySmall.copy(
            color = DeepNavyText,
            lineHeight = 18.sp
          ),
          modifier = Modifier.weight(1f)
        )
      }

      TextButton(
        onClick = { showTermsDialog = true },
        modifier = Modifier
          .heightIn(min = 48.dp)
          .testTag("register_view_terms_button")
      ) {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Read Terms & Privacy Summary" else "Read Terms & Privacy Summary",
          color = SkyBluePrimary,
          fontWeight = FontWeight.SemiBold
        )
      }

      validationResult?.termsError?.let { err ->
        Text(
          text = err,
          style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed, fontWeight = FontWeight.Medium),
          modifier = Modifier.testTag("register_terms_error")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Honest Demo Notice
      DemoAuthNoticeBanner(currentLanguage = currentLanguage)

      Spacer(modifier = Modifier.height(18.dp))

      // Create Account Button
      Button(
        onClick = {
          val result = AuthFormValidator.validateRegistration(
            fullName = fullName,
            phoneNumber = phoneInput,
            email = emailInput,
            password = password,
            confirmPassword = confirmPassword,
            acceptedTerms = acceptedTerms,
            selectedRole = selectedRole,
            language = currentLanguage
          )
          validationResult = result
          if (result.isValid && result.normalizedPhone != null) {
            scope.launch {
              val passwordChars = password.toCharArray()
              val gatewayResult = DemoAuthGateway.registerWithPhone(
                fullName = fullName.trim(),
                normalizedPhone = result.normalizedPhone,
                email = emailInput.trim().ifEmpty { null },
                rawPasswordChars = passwordChars,
                role = selectedRole
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
          .testTag("create_account_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SkyBluePrimary,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        Text(
          text = DwelloStrings.createAccountBtn(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Already have an account? Login
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(
          onClick = onNavigateToLogin,
          modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("register_already_have_account_link")
        ) {
          Text(
            text = DwelloStrings.alreadyHaveAccountPrompt(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = SkyBluePrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Honest Frontend Demonstration Dialog (Never fakes real account creation)
  demoDialogNormalizedPhone?.let { normalizedPhone ->
    AlertDialog(
      onDismissRequest = { demoDialogNormalizedPhone = null },
      title = {
        Text(
          text = DwelloStrings.registrationDemoDialogTitle(currentLanguage),
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(
          text = DwelloStrings.registrationDemoDialogBody(currentLanguage, normalizedPhone),
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            demoDialogNormalizedPhone = null
            onPreviewAuthenticatedRole(selectedRole)
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text(
            text = if (selectedRole == AuthAccountRole.TENANT)
              (if (currentLanguage == AppLanguage.ENGLISH) "Preview Tenant Home" else "Open Tenant Home")
            else
              (if (currentLanguage == AppLanguage.ENGLISH) "Preview Landlord Dashboard" else "Open Landlord Office"),
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { demoDialogNormalizedPhone = null }) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Stay Here" else "Stay Here",
            color = NavyTextSecondary
          )
        }
      }
    )
  }

  // Terms & Privacy Summary Dialog
  if (showTermsDialog) {
    AlertDialog(
      onDismissRequest = { showTermsDialog = false },
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Terms & Privacy Summary" else "Terms & Privacy Summary",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH)
            "• Dwello Homes connects Tenants and Landlords in Benin City without public middlemen or agents.\n• Private landlord phone numbers are never publicly exposed to other users.\n• All submitted properties start as Pending Approval and are moderated before going live.\n• Your phone number will be used for account verification once backend SMS/OTP integration is connected."
          else
            "• Dwello Homes dey connect Tenants and Landlords for Benin City without agent.\n• Private landlord phone number no dey show outside for public.\n• Any new house go start as Pending Approval before e go live.\n• Your phone number go use for OTP verification once backend connect.",
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            acceptedTerms = true
            showTermsDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Accept & Close" else "Accept & Close",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showTermsDialog = false }) {
          Text("Close", color = NavyTextSecondary)
        }
      }
    )
  }
}
