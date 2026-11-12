package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
import com.example.data.SamplePropertyData
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostHouseScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onSuccessNavigateHome: () -> Unit,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val handleBack = onBack ?: onSuccessNavigateHome

  // Form State
  var propertyTitle by remember { mutableStateOf("") }
  var selectedPropertyType by remember { mutableStateOf(SamplePropertyData.propertyTypesList.first()) }
  var bedroomsCount by remember { mutableIntStateOf(1) }
  var bathroomsCount by remember { mutableIntStateOf(1) }
  var rentAmountString by remember { mutableStateOf("") }
  var rentalPeriod by remember { mutableStateOf(RentalPeriod.YEARLY) }
  var propertyDescription by remember { mutableStateOf("") }

  // Location State
  var selectedNeighbourhood by remember { mutableStateOf("") }
  var addressOrLandmark by remember { mutableStateOf("") }

  // Facilities State
  val selectedFacilities = remember {
    mutableStateListOf(
      "Water supply",
      "Electricity",
      "Security"
    )
  }
  var additionalFacilitiesText by remember { mutableStateOf("") }

  // Photos State (Multiple photo URIs from Android Photo Picker)
  val selectedPhotoUris = remember { mutableStateListOf<String>() }

  // Validation Error States
  var titleError by remember { mutableStateOf<String?>(null) }
  var typeError by remember { mutableStateOf<String?>(null) }
  var rentError by remember { mutableStateOf<String?>(null) }
  var bedroomsError by remember { mutableStateOf<String?>(null) }
  var neighbourhoodError by remember { mutableStateOf<String?>(null) }
  var descriptionError by remember { mutableStateOf<String?>(null) }
  var photosError by remember { mutableStateOf<String?>(null) }

  // Dialog States
  var showDiscardDialog by remember { mutableStateOf(false) }
  var showSuccessDialog by remember { mutableStateOf(false) }
  var generatedRefNumber by remember { mutableStateOf("") }

  // Determine if form has dirty uncommitted data
  val isFormDirty = remember(
    propertyTitle,
    rentAmountString,
    propertyDescription,
    selectedNeighbourhood,
    addressOrLandmark,
    selectedPhotoUris.size
  ) {
    propertyTitle.isNotBlank() ||
      rentAmountString.isNotBlank() ||
      propertyDescription.isNotBlank() ||
      selectedNeighbourhood.isNotBlank() ||
      addressOrLandmark.isNotBlank() ||
      selectedPhotoUris.isNotEmpty()
  }

  // Intercept back navigation
  BackHandler {
    if (isFormDirty) {
      showDiscardDialog = true
    } else {
      handleBack()
    }
  }

  // Android Photo Picker Launcher (Zero-permission Android 13+ standard Photo Picker)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      val newUriStrings = uris.map { it.toString() }
      newUriStrings.forEach { uriStr ->
        if (!selectedPhotoUris.contains(uriStr)) {
          selectedPhotoUris.add(uriStr)
        }
      }
      photosError = null
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    // 1. Header with Language Switch
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("post_house_form_scroll"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // 2. Navigation Top Bar with Back Button
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              if (isFormDirty) {
                showDiscardDialog = true
              } else {
                handleBack()
              }
            },
            modifier = Modifier.testTag("post_house_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = SkyBluePrimary
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column {
            Text(
              text = DwelloStrings.postHouseFormTitle(currentLanguage),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText
              )
            )
            Text(
              text = "Dwello Homes • Benin City, Edo State",
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Instructional Banner for High Accessibility
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
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SkyBluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = DwelloStrings.postHouseInstruction(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = DeepNavyText,
                  fontWeight = FontWeight.Medium,
                  lineHeight = 18.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // ==========================================
      // SECTION 1: PROPERTY PHOTOS
      // ==========================================
      item {
        Text(
          text = DwelloStrings.sectionPhotosTitle(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Text(
          text = DwelloStrings.photosInstruction(currentLanguage),
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            lineHeight = 18.sp
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Big Select Photos Button
        Button(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("select_photos_button"),
          shape = RoundedCornerShape(12.dp),
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
            text = DwelloStrings.selectPhotosBtn(currentLanguage),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = SkyBluePrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Photos Horizontal Carousel Preview
        if (selectedPhotoUris.isNotEmpty()) {
          Text(
            text = DwelloStrings.selectedPhotosCount(currentLanguage, selectedPhotoUris.size),
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(selectedPhotoUris) { uriString ->
              Box(
                modifier = Modifier
                  .size(110.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, SoftBlueBorder, RoundedCornerShape(12.dp))
              ) {
                AsyncImage(
                  model = Uri.parse(uriString),
                  contentDescription = "Selected Property Photo",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )

                // Remove Photo Button
                Surface(
                  modifier = Modifier
                    .padding(6.dp)
                    .size(26.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .clickable { selectedPhotoUris.remove(uriString) }
                    .testTag("remove_photo_btn"),
                  color = Color.Black.copy(alpha = 0.75f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier
                      .padding(4.dp)
                      .size(16.dp)
                  )
                }
              }
            }
          }
        } else {
          // No photos message
          Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = NavyTextSecondary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = DwelloStrings.noPhotosSelected(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
              )
            }
          }
        }

        // Inline Photo Error
        photosError?.let { err ->
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = err,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.testTag("photos_error_text")
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = DwelloStrings.localPhotoNotice(currentLanguage),
          style = MaterialTheme.typography.labelSmall.copy(
            color = NavyTextSecondary,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(24.dp))
      }

      // ==========================================
      // SECTION 2: PROPERTY DETAILS
      // ==========================================
      item {
        Text(
          text = DwelloStrings.sectionDetailsTitle(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Property Title
        Text(
          text = DwelloStrings.titleLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
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

        Spacer(modifier = Modifier.height(16.dp))

        // Property Type Chips
        Text(
          text = DwelloStrings.selectTypeLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
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
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Steppers for Bedrooms and Bathrooms
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Bedrooms
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
                .padding(horizontal = 6.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = {
                  if (bedroomsCount > 1) {
                    bedroomsCount--
                    bedroomsError = null
                  }
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Bedrooms", tint = SkyBluePrimary)
              }
              Text(
                text = "$bedroomsCount",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              IconButton(
                onClick = {
                  bedroomsCount++
                  bedroomsError = null
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Bedrooms", tint = SkyBluePrimary)
              }
            }
          }

          // Bathrooms
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
                .padding(horizontal = 6.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = { if (bathroomsCount > 1) bathroomsCount-- },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Bathrooms", tint = SkyBluePrimary)
              }
              Text(
                text = "$bathroomsCount",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              IconButton(
                onClick = { bathroomsCount++ },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Bathrooms", tint = SkyBluePrimary)
              }
            }
          }
        }

        bedroomsError?.let { err ->
          Text(
            text = err,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rent Amount and Payment Period
        Text(
          text = DwelloStrings.rentAmountLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
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

        Spacer(modifier = Modifier.height(14.dp))

        // Preferred Payment Period Segmented Options
        Text(
          text = DwelloStrings.paymentPeriodLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Yearly
          val isYearly = rentalPeriod == RentalPeriod.YEARLY
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { rentalPeriod = RentalPeriod.YEARLY }
              .testTag("period_yearly_btn"),
            color = if (isYearly) SkyBluePrimary else Color.White,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isYearly) SkyBluePrimary else SoftBlueBorder)
          ) {
            Row(
              modifier = Modifier.padding(vertical = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isYearly) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = DwelloStrings.periodYearlyOption(currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isYearly) Color.White else DeepNavyText
                )
              )
            }
          }

          // Monthly
          val isMonthly = rentalPeriod == RentalPeriod.MONTHLY
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { rentalPeriod = RentalPeriod.MONTHLY }
              .testTag("period_monthly_btn"),
            color = if (isMonthly) SkyBluePrimary else Color.White,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isMonthly) SkyBluePrimary else SoftBlueBorder)
          ) {
            Row(
              modifier = Modifier.padding(vertical = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isMonthly) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = DwelloStrings.periodMonthlyOption(currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isMonthly) Color.White else DeepNavyText
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Property Description
        Text(
          text = DwelloStrings.descriptionLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
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
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
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

      // ==========================================
      // SECTION 3: PROPERTY LOCATION (BENIN CITY)
      // ==========================================
      item {
        Text(
          text = DwelloStrings.sectionLocationTitle(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = DwelloStrings.neighbourhoodLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Benin City Neighbourhood Chips
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
                .clickable {
                  selectedNeighbourhood = areaItem.name
                  neighbourhoodError = null
                }
                .testTag("neighbourhood_chip_${areaItem.name}"),
              color = if (isSelected) SkyBluePrimary else Color.White,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Full Address or Landmark
        Text(
          text = DwelloStrings.addressLabel(currentLanguage),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = addressOrLandmark,
          onValueChange = { addressOrLandmark = it },
          placeholder = { Text(DwelloStrings.addressPlaceholder(currentLanguage)) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("address_or_landmark_input"),
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

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = DwelloStrings.locationNotice(currentLanguage),
          style = MaterialTheme.typography.labelSmall.copy(
            color = NavyTextSecondary,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(24.dp))
      }

      // ==========================================
      // SECTION 4: PROPERTY FACILITIES
      // ==========================================
      item {
        Text(
          text = DwelloStrings.sectionFacilitiesTitle(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Text(
          text = DwelloStrings.facilitiesInstruction(currentLanguage),
          style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Facilities Checkbox List in very light blue background
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
                    .clickable {
                      if (isChecked) {
                        selectedFacilities.remove(facility)
                      } else {
                        selectedFacilities.add(facility)
                      }
                    }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                      if (checked) selectedFacilities.add(facility) else selectedFacilities.remove(facility)
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

        // Additional Facilities Description
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
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
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

        Spacer(modifier = Modifier.height(20.dp))
      }

      // ==========================================
      // SECTION 5: PRIVACY & SUBMIT BUTTON
      // ==========================================
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SoftBlueAccent),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = SkyBluePrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = DwelloStrings.privatePhoneProtectedNotice(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(
                color = DeepNavyText,
                lineHeight = 18.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Primary Submit Button
        Button(
          onClick = {
            // Validate all fields
            var hasErrors = false

            if (propertyTitle.trim().isEmpty()) {
              titleError = DwelloStrings.errorTitleRequired(currentLanguage)
              hasErrors = true
            } else {
              titleError = null
            }

            if (selectedPropertyType.isEmpty()) {
              typeError = DwelloStrings.errorTypeRequired(currentLanguage)
              hasErrors = true
            } else {
              typeError = null
            }

            val rentVal = rentAmountString.toLongOrNull() ?: 0L
            if (rentVal <= 0L) {
              rentError = DwelloStrings.errorRentRequired(currentLanguage)
              hasErrors = true
            } else {
              rentError = null
            }

            if (selectedNeighbourhood.isEmpty()) {
              neighbourhoodError = DwelloStrings.errorNeighbourhoodRequired(currentLanguage)
              hasErrors = true
            } else {
              neighbourhoodError = null
            }

            if (propertyDescription.trim().isEmpty()) {
              descriptionError = DwelloStrings.errorDescriptionRequired(currentLanguage)
              hasErrors = true
            } else {
              descriptionError = null
            }

            if (selectedPhotoUris.isEmpty()) {
              photosError = DwelloStrings.noPhotosSelected(currentLanguage)
              hasErrors = true
            } else {
              photosError = null
            }

            if (!hasErrors) {
              // Create property and set status automatically to PENDING_APPROVAL
              val newPropId = "prop_landlord_${System.currentTimeMillis()}"
              val formattedPrice = "₦${String.format("%,d", rentVal)}"
              val paymentPeriodSuffix = if (rentalPeriod == RentalPeriod.YEARLY) " / year" else " / month"

              val newProperty = PropertyItem(
                id = newPropId,
                titleEn = propertyTitle.trim(),
                titlePidgin = propertyTitle.trim(),
                categoryEn = selectedPropertyType,
                categoryPidgin = selectedPropertyType,
                area = selectedNeighbourhood,
                priceNaira = rentVal,
                priceFormatted = formattedPrice,
                rentalPeriod = rentalPeriod,
                bedrooms = bedroomsCount,
                bathrooms = bathroomsCount,
                isGated = selectedFacilities.contains("Fenced compound") || selectedFacilities.contains("Security"),
                hasBorehole = selectedFacilities.contains("Water supply") || selectedFacilities.contains("Running water"),
                hasPrepaidMeter = selectedFacilities.contains("Electricity"),
                landlordName = SamplePropertyData.CURRENT_LANDLORD_NAME,
                landlordRoleEn = "Landlord (Property Owner)",
                landlordRolePidgin = "Direct House Owner",
                phoneContact = "0800 393 556", // Never expose private landlord phone number to tenants
                descriptionEn = propertyDescription.trim(),
                descriptionPidgin = propertyDescription.trim(),
                neighborhoodNote = addressOrLandmark.ifBlank { "Benin City Residential Axis" },
                photoUris = selectedPhotoUris.toList(),
                imageResId = R.drawable.img_benin_house_exterior,
                facilities = selectedFacilities.toList(),
                additionalFacilitiesNote = additionalFacilitiesText.trim(),
                rentalTerms = "${if (rentalPeriod == RentalPeriod.YEARLY) "1 Year" else "Monthly"} rent advance. Inspection strictly coordinated via Dwello Homes.",
                availabilityStatus = "Under Review by Dwello Homes",
                approvalStatus = PropertyApprovalStatus.PENDING_APPROVAL,
                rejectionReason = null,
                submissionTimestamp = System.currentTimeMillis(),
                isDemonstrationData = false
              )

              // Add to repository
              SamplePropertyData.addProperty(newProperty)

              generatedRefNumber = "DWL-BEN-${(1000..9999).random()}"
              showSuccessDialog = true
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("submit_property_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SkyBluePrimary,
            contentColor = Color.White
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = DwelloStrings.submitButtonText(currentLanguage),
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }

  // ==========================================
  // DISCARD CONFIRMATION DIALOG
  // ==========================================
  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
      },
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

  // ==========================================
  // SUBMISSION SUCCESS CONFIRMATION DIALOG
  // ==========================================
  if (showSuccessDialog) {
    AlertDialog(
      onDismissRequest = {
        showSuccessDialog = false
        handleBack()
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

          // Pending Approval Badge
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
              text = "Listing Ref: $generatedRefNumber",
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
            showSuccessDialog = false
            handleBack()
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
