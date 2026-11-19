package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.AuthenticatedUser
import com.example.data.DwelloMappers
import com.example.data.DwelloStrings
import com.example.data.PropertyFormValidator
import com.example.data.PropertyPhotoValidator
import com.example.data.RentalPeriod
import com.example.data.SamplePropertyData
import com.example.data.SelectedPhotoItem
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostHouseScreen(
  currentLanguage: AppLanguage,
  currentUser: AuthenticatedUser?,
  isSubmitting: Boolean,
  submitError: String?,
  submittedPropertyId: String?,
  uploadProgressMessage: String? = null,
  pendingUploadPropertyId: String? = null,
  onLanguageToggle: () -> Unit,
  onSubmitProperty: (
    title: String,
    propertyTypeDisplay: String,
    bedrooms: Int,
    bathrooms: Int,
    rentAmount: Long,
    rentalPeriod: RentalPeriod,
    description: String,
    neighbourhood: String,
    fullAddress: String,
    landmark: String?,
    selectedPhotos: List<SelectedPhotoItem>
  ) -> Unit,
  onResetSubmitState: () -> Unit,
  onSuccessNavigateHome: () -> Unit,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val handleBack = onBack ?: onSuccessNavigateHome

  // 5-Step Wizard Navigation State (1..5)
  var currentStep by remember { mutableIntStateOf(1) }

  // Step 1: Basic Information
  var propertyTitle by remember { mutableStateOf("") }
  var selectedPropertyType by remember { mutableStateOf(SamplePropertyData.propertyTypesList.first()) }
  var propertyDescription by remember { mutableStateOf("") }

  // Step 2: Location
  var city by remember { mutableStateOf("Benin City") }
  var selectedNeighbourhood by remember { mutableStateOf("") }
  var streetDescription by remember { mutableStateOf("") }

  // Step 3: Rent & Facilities
  var rentAmountString by remember { mutableStateOf("") }
  var rentalPeriod by remember { mutableStateOf(RentalPeriod.YEARLY) }
  var bedroomsCount by remember { mutableIntStateOf(1) }
  var bathroomsCount by remember { mutableIntStateOf(1) }
  val selectedFacilities = remember {
    mutableStateListOf(
      "Water",
      "Electricity",
      "Security"
    )
  }
  var additionalFacilitiesText by remember { mutableStateOf("") }

  // Step 4: Property Photos
  val selectedPhotos = remember { mutableStateListOf<SelectedPhotoItem>() }
  var replacingPhotoIndex by remember { mutableIntStateOf(-1) }

  // Step Validation Error States
  var titleError by remember { mutableStateOf<String?>(null) }
  var typeError by remember { mutableStateOf<String?>(null) }
  var descriptionError by remember { mutableStateOf<String?>(null) }
  var cityError by remember { mutableStateOf<String?>(null) }
  var neighbourhoodError by remember { mutableStateOf<String?>(null) }
  var streetError by remember { mutableStateOf<String?>(null) }
  var rentError by remember { mutableStateOf<String?>(null) }
  var photosError by remember { mutableStateOf<String?>(null) }

  // Discard Draft Dialog State
  var showDiscardDialog by remember { mutableStateOf(false) }

  val isFormDirty = remember(
    propertyTitle,
    propertyDescription,
    selectedNeighbourhood,
    streetDescription,
    rentAmountString,
    selectedPhotos.size,
    currentStep
  ) {
    currentStep > 1 ||
      propertyTitle.isNotBlank() ||
      propertyDescription.isNotBlank() ||
      selectedNeighbourhood.isNotBlank() ||
      streetDescription.isNotBlank() ||
      rentAmountString.isNotBlank() ||
      selectedPhotos.isNotEmpty()
  }

  fun handleTopOrSystemBack() {
    if (isSubmitting) return
    if (currentStep > 1) {
      currentStep -= 1
    } else if (isFormDirty) {
      showDiscardDialog = true
    } else {
      onResetSubmitState()
      handleBack()
    }
  }

  BackHandler(enabled = !isSubmitting && submittedPropertyId == null) {
    handleTopOrSystemBack()
  }

  // Zero-permission Photo Picker for multiple photos
  val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      var rejectedUnsupported = false
      uris.forEach { uri ->
        val uriStr = uri.toString()
        val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        if (PropertyPhotoValidator.isSupportedImage(uriStr, mimeType)) {
          if (selectedPhotos.none { it.uriString == uriStr }) {
            val label = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
              ?: "Photo ${selectedPhotos.size + 1}"
            selectedPhotos.add(SelectedPhotoItem(uriString = uriStr, mimeType = mimeType, fileName = label))
          }
        } else {
          rejectedUnsupported = true
        }
      }
      photosError = if (rejectedUnsupported) {
        if (currentLanguage == AppLanguage.ENGLISH) {
          "Some selected files are not supported images. Please select JPG, PNG, WEBP, or HEIC images only."
        } else {
          "Some file wey you pick no be correct image. Pick only JPG, PNG, WEBP, or HEIC picture."
        }
      } else {
        null
      }
    }
  }

  // Zero-permission Photo Picker to replace a single selected photo
  val replacePhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    val idx = replacingPhotoIndex
    replacingPhotoIndex = -1
    if (uri != null && idx in selectedPhotos.indices) {
      val uriStr = uri.toString()
      val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
      if (PropertyPhotoValidator.isSupportedImage(uriStr, mimeType)) {
        val label = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
          ?: "Photo ${idx + 1}"
        selectedPhotos[idx] = SelectedPhotoItem(uriString = uriStr, mimeType = mimeType, fileName = label)
        photosError = null
      } else {
        photosError = if (currentLanguage == AppLanguage.ENGLISH) {
          "Unsupported file type. Please choose a JPG, PNG, WEBP, or HEIC image."
        } else {
          "This file type no dey supported. Pick JPG, PNG, WEBP, or HEIC image."
        }
      }
    }
  }

  fun validateCurrentStepAndAdvance() {
    when (currentStep) {
      1 -> {
        val result = PropertyFormValidator.validateStep1(
          title = propertyTitle,
          propertyType = selectedPropertyType,
          description = propertyDescription
        )
        titleError = if (result.titleError != null) DwelloStrings.errorTitleRequired(currentLanguage) else null
        typeError = if (result.typeError != null) DwelloStrings.errorTypeRequired(currentLanguage) else null
        descriptionError = if (result.descriptionError != null) DwelloStrings.errorDescriptionRequired(currentLanguage) else null
        if (result.isValid) currentStep = 2
      }

      2 -> {
        val result = PropertyFormValidator.validateStep2(
          city = city,
          neighbourhood = selectedNeighbourhood,
          streetDescription = streetDescription
        )
        cityError = if (result.cityError != null) DwelloStrings.errorCityRequired(currentLanguage) else null
        neighbourhoodError = if (result.neighbourhoodError != null) DwelloStrings.errorNeighbourhoodRequired(currentLanguage) else null
        streetError = if (result.streetError != null) DwelloStrings.errorStreetRequired(currentLanguage) else null
        if (result.isValid) currentStep = 3
      }

      3 -> {
        val rentVal = rentAmountString.toLongOrNull() ?: 0L
        val result = PropertyFormValidator.validateStep3(
          rentAmount = rentVal,
          bedrooms = bedroomsCount,
          bathrooms = bathroomsCount
        )
        rentError = if (result.rentError != null) DwelloStrings.errorRentRequired(currentLanguage) else null
        if (result.isValid) currentStep = 4
      }

      4 -> {
        val result = PropertyFormValidator.validateStep4(selectedPhotos.toList())
        photosError = if (result.photosError != null) {
          if (selectedPhotos.isEmpty()) {
            DwelloStrings.noPhotosSelected(currentLanguage)
          } else {
            result.photosError
          }
        } else {
          null
        }
        if (result.isValid) currentStep = 5
      }
    }
  }

  fun submitPropertyListing() {
    val s1 = PropertyFormValidator.validateStep1(propertyTitle, selectedPropertyType, propertyDescription)
    val s2 = PropertyFormValidator.validateStep2(city, selectedNeighbourhood, streetDescription)
    val rentVal = rentAmountString.toLongOrNull() ?: 0L
    val s3 = PropertyFormValidator.validateStep3(rentVal, bedroomsCount, bathroomsCount)
    val s4 = PropertyFormValidator.validateStep4(selectedPhotos.toList())

    if (!s1.isValid) {
      titleError = if (s1.titleError != null) DwelloStrings.errorTitleRequired(currentLanguage) else null
      typeError = if (s1.typeError != null) DwelloStrings.errorTypeRequired(currentLanguage) else null
      descriptionError = if (s1.descriptionError != null) DwelloStrings.errorDescriptionRequired(currentLanguage) else null
      currentStep = 1
      return
    }
    if (!s2.isValid) {
      cityError = if (s2.cityError != null) DwelloStrings.errorCityRequired(currentLanguage) else null
      neighbourhoodError = if (s2.neighbourhoodError != null) DwelloStrings.errorNeighbourhoodRequired(currentLanguage) else null
      streetError = if (s2.streetError != null) DwelloStrings.errorStreetRequired(currentLanguage) else null
      currentStep = 2
      return
    }
    if (!s3.isValid) {
      rentError = if (s3.rentError != null) DwelloStrings.errorRentRequired(currentLanguage) else null
      currentStep = 3
      return
    }
    if (!s4.isValid) {
      photosError = s4.photosError ?: DwelloStrings.noPhotosSelected(currentLanguage)
      currentStep = 4
      return
    }

    val facilitiesList = selectedFacilities.joinToString(", ")
    val extraNotes = additionalFacilitiesText.trim()
    val fullDescription = buildString {
      append(propertyDescription.trim())
      if (facilitiesList.isNotEmpty()) {
        append("\n\nFacilities: $facilitiesList")
      }
      if (extraNotes.isNotEmpty()) {
        append("\nAdditional facilities: $extraNotes")
      }
    }

    val cleanCity = city.trim().ifBlank { "Benin City" }
    val cleanStreet = streetDescription.trim()
    val composedFullAddress = "$cleanStreet, $selectedNeighbourhood, $cleanCity"

    onSubmitProperty(
      propertyTitle.trim(),
      selectedPropertyType,
      bedroomsCount,
      bathroomsCount,
      rentVal,
      rentalPeriod,
      fullDescription,
      selectedNeighbourhood,
      composedFullAddress,
      cleanStreet,
      selectedPhotos.toList()
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle
    )

    val formScrollState = rememberScrollState()
    LaunchedEffect(currentStep) {
      formScrollState.scrollTo(0)
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(formScrollState)
        .padding(horizontal = 20.dp, vertical = 14.dp)
        .testTag("post_house_form_scroll")
    ) {
      // Top Title & Step Indicator Header
      run {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { handleTopOrSystemBack() },
            modifier = Modifier
              .size(48.dp)
              .testTag("post_house_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = SkyBluePrimary
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = DwelloStrings.postHouseFormTitle(currentLanguage),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText
              )
            )
            Text(
              text = "${currentUser?.fullName ?: "Landlord"} • ${city.ifBlank { "Benin City" }}, Edo State",
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )
          }

          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, SoftBlueBorder),
            modifier = Modifier.testTag("post_house_step_indicator")
          ) {
            Text(
              text = DwelloStrings.stepProgressLabel(currentLanguage, currentStep),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SkyBluePrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5-Segment Visual Stepper Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          for (stepIndex in 1..5) {
            val isCompleted = stepIndex < currentStep
            val isActive = stepIndex == currentStep
            Box(
              modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                  when {
                    isActive -> SkyBluePrimary
                    isCompleted -> Color(0xFF0EA5E9).copy(alpha = 0.65f)
                    else -> SoftBlueAccent
                  }
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = DwelloStrings.stepTitle(currentLanguage, currentStep),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = DeepNavyText,
            fontSize = 20.sp
          ),
          modifier = Modifier.testTag("post_house_step_title")
        )

        Spacer(modifier = Modifier.height(14.dp))
      }

      // STEP CONTENT
      when (currentStep) {
        // STEP 1: BASIC INFORMATION
        1 -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_house_step_1")
          ) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
                border = BorderStroke(1.dp, SoftBlueBorder)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = DwelloStrings.postHouseFormSubtitle(currentLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = DeepNavyText,
                      lineHeight = 18.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.titleLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = propertyTitle,
                onValueChange = {
                  propertyTitle = it
                  if (it.isNotBlank()) titleError = null
                },
                placeholder = { Text(DwelloStrings.titlePlaceholder(currentLanguage)) },
                isError = titleError != null,
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("property_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )
              titleError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("title_error_text")
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.selectTypeLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(8.dp))

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                SamplePropertyData.propertyTypesList.forEach { pType ->
                  val isSelected = selectedPropertyType == pType
                  FilterChip(
                    selected = isSelected,
                    enabled = !isSubmitting,
                    onClick = {
                      selectedPropertyType = pType
                      typeError = null
                    },
                    label = {
                      Text(
                        text = pType,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = SkyBluePrimary,
                      selectedLabelColor = Color.White,
                      containerColor = Color.White,
                      labelColor = DeepNavyText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      enabled = true,
                      selected = isSelected,
                      borderColor = SoftBlueBorder,
                      selectedBorderColor = SkyBluePrimary,
                      borderWidth = 1.dp
                    ),
                    modifier = Modifier.testTag("property_type_chip_$pType")
                  )
                }
              }
              typeError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("type_error_text")
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.descriptionLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = propertyDescription,
                onValueChange = {
                  propertyDescription = it
                  if (it.isNotBlank()) descriptionError = null
                },
                placeholder = { Text(DwelloStrings.descriptionPlaceholder(currentLanguage)) },
                isError = descriptionError != null,
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .testTag("property_description_input"),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )
              descriptionError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("description_error_text")
                )
              }

              Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // STEP 2: LOCATION
        2 -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_house_step_2")
          ) {
              Text(
                text = DwelloStrings.cityLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = city,
                onValueChange = {
                  city = it
                  if (it.isNotBlank()) cityError = null
                },
                isError = cityError != null,
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("city_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )
              cityError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("city_error_text")
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.neighbourhoodLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(8.dp))

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                SamplePropertyData.beninAreas.forEach { areaItem ->
                  val isSelected = selectedNeighbourhood == areaItem.name
                  Surface(
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .clickable(enabled = !isSubmitting) {
                        selectedNeighbourhood = areaItem.name
                        neighbourhoodError = null
                      }
                      .testTag("neighbourhood_chip_${areaItem.name}"),
                    color = if (isSelected) SkyBluePrimary else Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else SkyBluePrimary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = areaItem.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          color = if (isSelected) Color.White else DeepNavyText
                        )
                      )
                    }
                  }
                }
              }
              neighbourhoodError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("neighbourhood_error_text")
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.streetAreaDescriptionLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = streetDescription,
                onValueChange = {
                  streetDescription = it
                  if (it.isNotBlank()) streetError = null
                },
                placeholder = { Text(DwelloStrings.streetAreaPlaceholder(currentLanguage)) },
                isError = streetError != null,
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("address_or_landmark_input"),
                singleLine = false,
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )
              streetError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("street_error_text")
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = DwelloStrings.locationNotice(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NavyTextSecondary,
                  fontSize = 11.sp
                )
              )

              Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // STEP 3: RENT & FACILITIES
        3 -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_house_step_3")
          ) {
              Text(
                text = DwelloStrings.rentAmountLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = rentAmountString,
                onValueChange = {
                  rentAmountString = it.filter { ch -> ch.isDigit() }
                  val amount = rentAmountString.toLongOrNull() ?: 0L
                  if (amount > 0L) rentError = null
                },
                placeholder = { Text(DwelloStrings.rentAmountPlaceholder(currentLanguage)) },
                isError = rentError != null,
                enabled = !isSubmitting,
                prefix = {
                  Text("₦ ", fontWeight = FontWeight.ExtraBold, color = SkyBluePrimary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("rent_amount_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )
              rentError?.let { err ->
                Text(
                  text = err,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("rent_error_text")
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = DwelloStrings.paymentPeriodLabel(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                val isMonthly = rentalPeriod == RentalPeriod.MONTHLY
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isSubmitting) { rentalPeriod = RentalPeriod.MONTHLY }
                    .testTag("period_monthly_btn"),
                  color = if (isMonthly) SkyBluePrimary else Color.White,
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, if (isMonthly) SkyBluePrimary else SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isMonthly) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                      text = DwelloStrings.periodMonthly(currentLanguage),
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isMonthly) Color.White else DeepNavyText
                      )
                    )
                  }
                }

                val isYearly = rentalPeriod == RentalPeriod.YEARLY
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isSubmitting) { rentalPeriod = RentalPeriod.YEARLY }
                    .testTag("period_yearly_btn"),
                  color = if (isYearly) SkyBluePrimary else Color.White,
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, if (isYearly) SkyBluePrimary else SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isYearly) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                      text = DwelloStrings.periodYearly(currentLanguage),
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isYearly) Color.White else DeepNavyText
                      )
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = DwelloStrings.bedroomsCountLabel(currentLanguage),
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(VeryLightBlueBackground, RoundedCornerShape(12.dp))
                      .border(1.dp, SoftBlueBorder, RoundedCornerShape(12.dp))
                      .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    IconButton(
                      onClick = { if (bedroomsCount > 1) bedroomsCount-- },
                      enabled = !isSubmitting,
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("bedrooms_decrease_btn")
                    ) {
                      Icon(Icons.Default.Remove, contentDescription = "Decrease Bedrooms", tint = SkyBluePrimary)
                    }
                    Text(
                      text = "$bedroomsCount",
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavyText
                      ),
                      modifier = Modifier.testTag("bedrooms_count_text")
                    )
                    IconButton(
                      onClick = { bedroomsCount++ },
                      enabled = !isSubmitting,
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("bedrooms_increase_btn")
                    ) {
                      Icon(Icons.Default.Add, contentDescription = "Increase Bedrooms", tint = SkyBluePrimary)
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = DwelloStrings.bathroomsCountLabel(currentLanguage),
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(VeryLightBlueBackground, RoundedCornerShape(12.dp))
                      .border(1.dp, SoftBlueBorder, RoundedCornerShape(12.dp))
                      .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    IconButton(
                      onClick = { if (bathroomsCount > 1) bathroomsCount-- },
                      enabled = !isSubmitting,
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("bathrooms_decrease_btn")
                    ) {
                      Icon(Icons.Default.Remove, contentDescription = "Decrease Bathrooms", tint = SkyBluePrimary)
                    }
                    Text(
                      text = "$bathroomsCount",
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavyText
                      ),
                      modifier = Modifier.testTag("bathrooms_count_text")
                    )
                    IconButton(
                      onClick = { bathroomsCount++ },
                      enabled = !isSubmitting,
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("bathrooms_increase_btn")
                    ) {
                      Icon(Icons.Default.Add, contentDescription = "Increase Bathrooms", tint = SkyBluePrimary)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = DwelloStrings.facilitiesInstruction(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(8.dp))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(VeryLightBlueBackground, RoundedCornerShape(14.dp))
                  .border(1.dp, SoftBlueBorder, RoundedCornerShape(14.dp))
                  .padding(10.dp)
              ) {
                SamplePropertyData.standardFacilitiesList.chunked(2).forEach { pair ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    pair.forEach { facility ->
                      val isChecked = selectedFacilities.contains(facility)
                      Row(
                        modifier = Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(8.dp))
                          .clickable(enabled = !isSubmitting) {
                            if (isChecked) {
                              selectedFacilities.remove(facility)
                            } else {
                              selectedFacilities.add(facility)
                            }
                          }
                          .padding(vertical = 4.dp)
                          .testTag("facility_checkbox_$facility"),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Checkbox(
                          checked = isChecked,
                          enabled = !isSubmitting,
                          onCheckedChange = { checked ->
                            if (checked && !selectedFacilities.contains(facility)) {
                              selectedFacilities.add(facility)
                            } else if (!checked) {
                              selectedFacilities.remove(facility)
                            }
                          },
                          colors = CheckboxDefaults.colors(checkedColor = SkyBluePrimary),
                          modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = facility,
                          style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                            color = DeepNavyText
                          )
                        )
                      }
                    }
                    if (pair.size == 1) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = DwelloStrings.additionalFacilitiesLabel(currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = additionalFacilitiesText,
                onValueChange = { additionalFacilitiesText = it },
                placeholder = { Text(DwelloStrings.additionalFacilitiesPlaceholder(currentLanguage)) },
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(88.dp)
                  .testTag("additional_facilities_input"),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = VeryLightBlueBackground,
                  unfocusedContainerColor = VeryLightBlueBackground,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )

              Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // STEP 4: PROPERTY PHOTOS
        4 -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_house_step_4")
          ) {
              Text(
                text = DwelloStrings.photosInstruction(currentLanguage),
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = NavyTextSecondary,
                  lineHeight = 20.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = {
                  multiPhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                enabled = !isSubmitting,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp)
                  .testTag("select_photos_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SoftBlueAccent,
                  contentColor = SkyBluePrimary
                ),
                border = BorderStroke(1.5.dp, SkyBluePrimary)
              ) {
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = DwelloStrings.addMorePhotosBtn(currentLanguage),
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SkyBluePrimary
                  )
                )
              }

              photosError?.let { err ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  color = Color(0xFFFEF2F2),
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, Color(0xFFFECACA)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("photos_error_text")
                ) {
                  Text(
                    text = err,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF991B1B),
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              if (selectedPhotos.isNotEmpty()) {
                Text(
                  text = DwelloStrings.selectedPhotosCount(currentLanguage, selectedPhotos.size),
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_photos_list")
                ) {
                  selectedPhotos.forEachIndexed { index, photo ->
                    Card(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_photo_card_$index"),
                      shape = RoundedCornerShape(14.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.White),
                      border = BorderStroke(1.dp, SoftBlueBorder)
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Box(
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VeryLightBlueBackground)
                        ) {
                          AsyncImage(
                            model = Uri.parse(photo.uriString),
                            contentDescription = "Selected Property Photo ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                          )

                          Surface(
                            modifier = Modifier
                              .padding(8.dp)
                              .align(Alignment.TopStart),
                            color = DeepNavyText.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(6.dp)
                          ) {
                            Text(
                              text = "Photo ${index + 1}",
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                              style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                              )
                            )
                          }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          OutlinedButton(
                            onClick = {
                              replacingPhotoIndex = index
                              replacePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                              )
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                              .weight(1f)
                              .height(48.dp)
                              .testTag("replace_photo_btn_$index"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SkyBluePrimary)
                          ) {
                            Icon(
                              imageVector = Icons.Default.SwapHoriz,
                              contentDescription = null,
                              tint = SkyBluePrimary,
                              modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                              text = DwelloStrings.replacePhotoBtn(currentLanguage),
                              color = SkyBluePrimary,
                              fontWeight = FontWeight.Bold
                            )
                          }

                          OutlinedButton(
                            onClick = {
                              selectedPhotos.removeAt(index)
                              photosError = null
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                              .weight(1f)
                              .height(48.dp)
                              .testTag("remove_photo_btn_$index"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626))
                          ) {
                            Icon(
                              imageVector = Icons.Default.Delete,
                              contentDescription = null,
                              tint = Color(0xFFDC2626),
                              modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                              text = DwelloStrings.removePhotoBtn(currentLanguage),
                              color = Color(0xFFDC2626),
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }
                      }
                    }
                  }
                }
              } else {
                Surface(
                  color = VeryLightBlueBackground,
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, SoftBlueBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Icon(
                      imageVector = Icons.Default.PhotoLibrary,
                      contentDescription = null,
                      tint = SkyBluePrimary,
                      modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = if (currentLanguage == AppLanguage.ENGLISH) {
                        "Select at least 1 property photo (JPG, PNG, WEBP, or HEIC) to continue."
                      } else {
                        "Pick at least 1 house picture (JPG, PNG, WEBP, or HEIC) before you continue."
                      },
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepNavyText,
                        fontWeight = FontWeight.Medium
                      )
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = DwelloStrings.localPhotoNotice(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NavyTextSecondary,
                  fontSize = 11.sp
                )
              )

              Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // STEP 5: REVIEW AND SUBMIT
        else -> {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_house_step_5")
          ) {
              Text(
                text = DwelloStrings.reviewHeaderTitle(currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = DwelloStrings.reviewHeaderSubtitle(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = NavyTextSecondary,
                  lineHeight = 18.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Section 1 Review Card
              ReviewSectionCard(
                title = DwelloStrings.stepTitle(currentLanguage, 1),
                editLabel = DwelloStrings.editSectionBtn(currentLanguage),
                onEditClick = { currentStep = 1 },
                editTestTag = "review_edit_step_1",
                isEnabled = !isSubmitting
              ) {
                ReviewKeyValueRow("Title", propertyTitle)
                ReviewKeyValueRow("Property Type", selectedPropertyType)
                ReviewKeyValueRow("Description", propertyDescription)
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Section 2 Review Card
              ReviewSectionCard(
                title = DwelloStrings.stepTitle(currentLanguage, 2),
                editLabel = DwelloStrings.editSectionBtn(currentLanguage),
                onEditClick = { currentStep = 2 },
                editTestTag = "review_edit_step_2",
                isEnabled = !isSubmitting
              ) {
                ReviewKeyValueRow("City", city.ifBlank { "Benin City" })
                ReviewKeyValueRow("Neighbourhood", selectedNeighbourhood)
                ReviewKeyValueRow("Street / Area", streetDescription)
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Section 3 Review Card
              val rentLong = rentAmountString.toLongOrNull() ?: 0L
              val periodText = if (rentalPeriod == RentalPeriod.MONTHLY) {
                DwelloStrings.periodMonthly(currentLanguage)
              } else {
                DwelloStrings.periodYearly(currentLanguage)
              }
              ReviewSectionCard(
                title = DwelloStrings.stepTitle(currentLanguage, 3),
                editLabel = DwelloStrings.editSectionBtn(currentLanguage),
                onEditClick = { currentStep = 3 },
                editTestTag = "review_edit_step_3",
                isEnabled = !isSubmitting
              ) {
                ReviewKeyValueRow("Rent", "${DwelloMappers.formatNaira(rentLong)} ($periodText)")
                ReviewKeyValueRow("Rooms", "$bedroomsCount Bedrooms • $bathroomsCount Bathrooms")
                ReviewKeyValueRow(
                  "Facilities",
                  selectedFacilities.joinToString(", ").ifBlank { "None selected" }
                )
                if (additionalFacilitiesText.isNotBlank()) {
                  ReviewKeyValueRow("Extra Notes", additionalFacilitiesText.trim())
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Section 4 Review Card
              ReviewSectionCard(
                title = "${DwelloStrings.stepTitle(currentLanguage, 4)} (${selectedPhotos.size})",
                editLabel = DwelloStrings.editSectionBtn(currentLanguage),
                onEditClick = { currentStep = 4 },
                editTestTag = "review_edit_step_4",
                isEnabled = !isSubmitting
              ) {
                if (selectedPhotos.isNotEmpty()) {
                  LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp)
                  ) {
                    itemsIndexed(selectedPhotos) { idx, photo ->
                      AsyncImage(
                        model = Uri.parse(photo.uriString),
                        contentDescription = "Review photo ${idx + 1}",
                        modifier = Modifier
                          .size(84.dp)
                          .clip(RoundedCornerShape(10.dp))
                          .border(1.dp, SoftBlueBorder, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = DwelloStrings.localPhotoNotice(currentLanguage),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = NavyTextSecondary,
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Admin Approval & Privacy Protection Banner
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.HourglassTop,
                      contentDescription = null,
                      tint = Color(0xFFB45309),
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (currentLanguage == AppLanguage.ENGLISH) {
                        "Initial Status: Pending Approval"
                      } else {
                        "First Status: Pending Approval"
                      },
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                      )
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = DwelloStrings.noSelfApprovalNotice(currentLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF92400E),
                      lineHeight = 18.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SoftBlueAccent),
                border = BorderStroke(1.dp, SoftBlueBorder)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = DwelloStrings.privatePhoneProtectedNotice(currentLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = DeepNavyText,
                      lineHeight = 18.sp,
                      fontSize = 12.sp
                    )
                  )
                }
              }

              if (!submitError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                  color = Color(0xFFFEF2F2),
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, Color(0xFFFECACA)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_house_server_error")
                ) {
                  Text(
                    text = submitError,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF991B1B),
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))
            }
        }
      }

      // WIZARD FOOTER NAVIGATION BUTTONS (Back / Next or Submit for Approval)
      run {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (currentStep > 1) {
            OutlinedButton(
              onClick = { currentStep -= 1 },
              enabled = !isSubmitting,
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .testTag("step_back_button"),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.5.dp, SkyBluePrimary)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = DwelloStrings.previousStepBtn(currentLanguage),
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
              )
            }
          }

          if (currentStep < 5) {
            Button(
              onClick = { validateCurrentStepAndAdvance() },
              enabled = !isSubmitting,
              modifier = Modifier
                .weight(if (currentStep > 1) 1.4f else 1f)
                .height(54.dp)
                .testTag("step_next_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SkyBluePrimary,
                contentColor = Color.White
              )
            ) {
              Text(
                text = DwelloStrings.nextStepBtn(currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 16.sp
                )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            Button(
              onClick = { submitPropertyListing() },
              enabled = !isSubmitting,
              modifier = Modifier
                .weight(1.6f)
                .height(54.dp)
                .testTag("submit_property_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SkyBluePrimary,
                contentColor = Color.White
              )
            ) {
              if (isSubmitting) {
                CircularProgressIndicator(
                  color = Color.White,
                  strokeWidth = 2.5.dp,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = uploadProgressMessage ?: if (currentLanguage == AppLanguage.ENGLISH) {
                    "Submitting..."
                  } else {
                    "Dey Submit..."
                  },
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
              } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (!pendingUploadPropertyId.isNullOrBlank()) {
                    DwelloStrings.retryPhotoUploadBtn(currentLanguage)
                  } else {
                    DwelloStrings.submitForApprovalBtn(currentLanguage)
                  },
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }

  // DISCARD DRAFT CONFIRMATION DIALOG
  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      title = {
        Text(
          text = DwelloStrings.discardDialogTitle(currentLanguage),
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(
          text = DwelloStrings.discardDialogMessage(currentLanguage),
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDiscardDialog = false
            onResetSubmitState()
            handleBack()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.testTag("confirm_discard_button")
        ) {
          Text(DwelloStrings.discardConfirmBtn(currentLanguage))
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showDiscardDialog = false },
          border = BorderStroke(1.dp, SkyBluePrimary),
          modifier = Modifier.testTag("cancel_discard_button")
        ) {
          Text(DwelloStrings.discardCancelBtn(currentLanguage), color = SkyBluePrimary)
        }
      }
    )
  }

  // SUBMISSION SUCCESS CONFIRMATION DIALOG (POST /api/properties)
  if (!submittedPropertyId.isNullOrBlank()) {
    AlertDialog(
      onDismissRequest = {
        onResetSubmitState()
        onSuccessNavigateHome()
      },
      icon = {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = SkyBluePrimary,
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(
          text = DwelloStrings.successDialogTitle(currentLanguage),
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText,
          fontSize = 20.sp
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = DwelloStrings.successDialogMessage(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DeepNavyText,
              lineHeight = 22.sp,
              fontWeight = FontWeight.Medium
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Status: PENDING APPROVAL",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                  )
                )
                Text(
                  text = DwelloStrings.successDialogPendingNote(currentLanguage),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF92400E),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Listing ID: $submittedPropertyId",
              modifier = Modifier.padding(8.dp),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onResetSubmitState()
            onSuccessNavigateHome()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("view_in_dashboard_button")
        ) {
          Text(
            text = DwelloStrings.viewInDashboardBtn(currentLanguage),
            fontWeight = FontWeight.Bold
          )
        }
      }
    )
  }
}

@Composable
private fun ReviewSectionCard(
  title: String,
  editLabel: String,
  onEditClick: () -> Unit,
  editTestTag: String,
  isEnabled: Boolean,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, CardBorderSubtle)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = DeepNavyText
          )
        )
        TextButton(
          onClick = onEditClick,
          enabled = isEnabled,
          modifier = Modifier.testTag(editTestTag)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = editLabel,
            color = SkyBluePrimary,
            fontWeight = FontWeight.Bold
          )
        }
      }
      HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.45f))
      Spacer(modifier = Modifier.height(8.dp))
      content()
    }
  }
}

@Composable
private fun ReviewKeyValueRow(label: String, value: String) {
  Column(modifier = Modifier.padding(vertical = 3.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = NavyTextSecondary,
        fontWeight = FontWeight.SemiBold
      )
    )
    Text(
      text = value.ifBlank { "-" },
      style = MaterialTheme.typography.bodyMedium.copy(
        color = DeepNavyText,
        fontWeight = FontWeight.Medium
      )
    )
  }
}
