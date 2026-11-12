package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import com.example.R
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.PropertyItem
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

@Composable
fun PostHouseScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onSuccessNavigateHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  var houseType by remember { mutableStateOf("Self-Con") }
  var selectedArea by remember { mutableStateOf("Uselu / Ugbowo") }
  var streetAddress by remember { mutableStateOf("") }
  var rentPrice by remember { mutableStateOf("250000") }
  var landlordName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var hasWater by remember { mutableStateOf(true) }
  var hasPrepaidMeter by remember { mutableStateOf(true) }
  var isGated by remember { mutableStateOf(true) }
  var selectedPhotoRes by remember { mutableIntStateOf(R.drawable.img_benin_house_exterior) }

  var showPreviewDialog by remember { mutableStateOf(false) }
  var showSuccessDialog by remember { mutableStateOf(false) }
  var postedRefNumber by remember { mutableStateOf("") }

  val houseTypes = listOf("Self-Con", "2-Bedroom Flat", "3-Bedroom Flat", "Single Room", "Commercial Shop")

  val previewProperty = remember(houseType, selectedArea, streetAddress, rentPrice, landlordName, phoneNumber, hasWater, hasPrepaidMeter, isGated, selectedPhotoRes) {
    val cleanPrice = rentPrice.filter { it.isDigit() }.toLongOrNull() ?: 250000L
    PropertyItem(
      id = "user_prop_${System.currentTimeMillis()}",
      titleEn = "$houseType Apartment",
      titlePidgin = "$houseType House",
      categoryEn = houseType,
      categoryPidgin = houseType,
      area = selectedArea,
      priceNaira = cleanPrice,
      priceFormatted = "₦${String.format("%,d", cleanPrice)}",
      bedrooms = if (houseType.contains("2")) 2 else if (houseType.contains("3")) 3 else 1,
      bathrooms = 1,
      isGated = isGated,
      hasBorehole = hasWater,
      hasPrepaidMeter = hasPrepaidMeter,
      landlordName = landlordName.ifBlank { "Property Owner" },
      landlordRoleEn = "Landlord (Property Owner)",
      landlordRolePidgin = "House Owner",
      phoneContact = phoneNumber.ifBlank { "0803 000 0000" },
      descriptionEn = "Quality $houseType in $selectedArea, Benin City. ${if (streetAddress.isNotBlank()) "Location: $streetAddress. " else ""}Features borehole running water, prepaid meter, and secure compound.",
      descriptionPidgin = "Clean $houseType for $selectedArea, Benin City. ${if (streetAddress.isNotBlank()) "Address: $streetAddress. " else ""}Water and prepaid light dey.",
      neighborhoodNote = streetAddress.ifBlank { "Benin City Residential Axis" },
      imageResId = selectedPhotoRes,
      imageResList = listOf(selectedPhotoRes, R.drawable.img_benin_house_interior),
      facilities = listOf(
        if (hasWater) "Borehole Water" else "Water Container",
        if (hasPrepaidMeter) "Prepaid Meter" else "Shared Meter",
        if (isGated) "Gated Compound" else "Open Compound",
        "Tiled Floor"
      ),
      rentalTerms = "1 Year advance rent. Inspection and tenant coordination managed via Dwello Homes.",
      approvalStatus = com.example.data.PropertyApprovalStatus.PENDING_APPROVAL,
      isDemonstrationData = false
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    // Header
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("post_house_form_scroll"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
      // Step Heading
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
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
                imageVector = Icons.Default.AddHome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = DwelloStrings.postHouseHeader(currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Text(
                text = DwelloStrings.postHouseInstruction(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = NavyTextSecondary
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 1: House Type
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "1. Select House Type" else "1. Choose Type of House",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          houseTypes.chunked(2).forEach { rowItems ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowItems.forEach { type ->
                val isSelected = houseType == type
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { houseType = type }
                    .testTag("house_type_chip_$type"),
                  color = if (isSelected) SkyBluePrimary else Color.White,
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                  ) {
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = type,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else DeepNavyText
                      )
                    )
                  }
                }
              }
              if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 2: Property Photographs
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "2. Property Photograph" else "2. House Picture",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Select property photo representation:" else "Choose picture for the house:",
          style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Photo Option 1: Exterior
          val isExt = selectedPhotoRes == R.drawable.img_benin_house_exterior
          Card(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedPhotoRes = R.drawable.img_benin_house_exterior },
            border = BorderStroke(2.dp, if (isExt) SkyBluePrimary else SoftBlueBorder)
          ) {
            Column {
              Image(
                painter = painterResource(id = R.drawable.img_benin_house_exterior),
                contentDescription = "Compound Exterior",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(85.dp),
                contentScale = ContentScale.Crop
              )
              Text(
                text = "Compound Exterior",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isExt) FontWeight.Bold else FontWeight.Normal,
                  color = if (isExt) SkyBluePrimary else DeepNavyText
                )
              )
            }
          }

          // Photo Option 2: Interior
          val isInt = selectedPhotoRes == R.drawable.img_benin_house_interior
          Card(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedPhotoRes = R.drawable.img_benin_house_interior },
            border = BorderStroke(2.dp, if (isInt) SkyBluePrimary else SoftBlueBorder)
          ) {
            Column {
              Image(
                painter = painterResource(id = R.drawable.img_benin_house_interior),
                contentDescription = "Apartment Interior",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(85.dp),
                contentScale = ContentScale.Crop
              )
              Text(
                text = "Living Room Interior",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isInt) FontWeight.Bold else FontWeight.Normal,
                  color = if (isInt) SkyBluePrimary else DeepNavyText
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 3: Location in Benin City
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "3. Location in Benin City" else "3. Area wey the house dey for Benin",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Area selection buttons
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          SamplePropertyData.beninAreas.take(6).chunked(2).forEach { rowAreas ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowAreas.forEach { area ->
                val isSelected = selectedArea == area.name
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedArea = area.name },
                  color = if (isSelected) SoftBlueAccent else Color.White,
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = null,
                      tint = if (isSelected) SkyBluePrimary else NavyTextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = area.name,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = DeepNavyText
                      )
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = streetAddress,
          onValueChange = { streetAddress = it },
          label = { Text(if (currentLanguage == AppLanguage.ENGLISH) "Street name or landmark" else "Street name or nearest bus stop") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("street_address_input"),
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

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 4: Rent Price
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "4. Yearly Rent (₦)" else "4. House Rent per year (₦)",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = rentPrice,
          onValueChange = { rentPrice = it },
          label = { Text("e.g. 250000") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rent_price_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = SkyBluePrimary) },
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

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 5: Amenities
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "5. Amenities in Compound" else "5. Things wey dey the compound",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(VeryLightBlueBackground, RoundedCornerShape(12.dp))
            .border(1.dp, SoftBlueBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { hasWater = !hasWater }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = hasWater,
              onCheckedChange = { hasWater = it },
              colors = CheckboxDefaults.colors(checkedColor = SkyBluePrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Running Water (Borehole)" else "Borehole Water / Running Tap",
              style = MaterialTheme.typography.bodyMedium,
              color = DeepNavyText
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { hasPrepaidMeter = !hasPrepaidMeter }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = hasPrepaidMeter,
              onCheckedChange = { hasPrepaidMeter = it },
              colors = CheckboxDefaults.colors(checkedColor = SkyBluePrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Prepaid Meter (Light)" else "Prepaid Meter (Nepa Light)",
              style = MaterialTheme.typography.bodyMedium,
              color = DeepNavyText
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isGated = !isGated }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isGated,
              onCheckedChange = { isGated = it },
              colors = CheckboxDefaults.colors(checkedColor = SkyBluePrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Fenced & Gated Compound" else "Fenced Compound with Gate",
              style = MaterialTheme.typography.bodyMedium,
              color = DeepNavyText
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 6: Contact Information
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "6. Landlord Phone Number" else "6. Phone number to receive calls",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = landlordName,
          onValueChange = { landlordName = it },
          label = { Text("Your Name") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("landlord_name_input"),
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { phoneNumber = it },
          label = { Text("Phone Number (e.g. 080...)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("phone_number_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          leadingIcon = {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SkyBluePrimary)
          },
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

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: PREVIEW PROPERTY BEFORE POSTING & SUBMIT DIRECT
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // PREVIEW BUTTON
          OutlinedButton(
            onClick = { showPreviewDialog = true },
            modifier = Modifier
              .weight(1f)
              .height(56.dp)
              .testTag("preview_property_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, SkyBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White,
              contentColor = SkyBluePrimary
            )
          ) {
            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "PREVIEW" else "PREVIEW",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }

          // SUBMIT NOW BUTTON
          Button(
            onClick = {
              SamplePropertyData.addProperty(previewProperty)
              postedRefNumber = "DWL-BEN-${(1000..9999).random()}"
              showSuccessDialog = true
            },
            modifier = Modifier
              .weight(1.5f)
              .height(56.dp)
              .testTag("submit_post_house_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SkyBluePrimary,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "POST NOW (FREE)" else "PUT NOW (FREE)",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold
              )
            )
          }
        }
      }
    }
  }

  // PREVIEW DIALOG
  if (showPreviewDialog) {
    AlertDialog(
      onDismissRequest = { showPreviewDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Visibility, contentDescription = null, tint = SkyBluePrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Preview Your Listing" else "Check Wetin Tenants Go See",
            fontWeight = FontWeight.Bold,
            color = DeepNavyText,
            fontSize = 18.sp
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Surface(
            color = VeryLightBlueBackground,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "This is how tenants in Benin City will see your listing card:"
              else
                "Na so tenants go take see your house card:",
              modifier = Modifier.padding(8.dp),
              style = MaterialTheme.typography.labelSmall.copy(color = DeepNavyText)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          PropertyCard(
            property = previewProperty,
            currentLanguage = currentLanguage,
            onClick = {},
            onEnquiryClick = {}
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showPreviewDialog = false
            SamplePropertyData.addProperty(previewProperty)
            postedRefNumber = "DWL-BEN-${(1000..9999).random()}"
            showSuccessDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text(if (currentLanguage == AppLanguage.ENGLISH) "Looks Good, Post Now" else "Correct, Post Am")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showPreviewDialog = false },
          border = BorderStroke(1.dp, SkyBluePrimary)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = SkyBluePrimary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit Details", color = SkyBluePrimary)
        }
      }
    )
  }

  // Success Dialog
  if (showSuccessDialog) {
    AlertDialog(
      onDismissRequest = { showSuccessDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Property Submitted for Review" else "House Don Enter for Review",
            fontWeight = FontWeight.Bold,
            color = DeepNavyText,
            fontSize = 18.sp
          )
        }
      },
      text = {
        Column {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Thank you! Your $houseType at $selectedArea, Benin City has been submitted to Dwello Homes."
            else
              "Thank you! Your $houseType for $selectedArea, Benin City don enter Dwello Homes.",
            style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Status: Pending Approval",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                )
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "Per Dwello Homes policy, an administrator will verify property details before approving it for public tenant listings."
                  else
                    "Dwello Homes admin go check the house details well before e go appear for public search.",
                  style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF92400E), fontSize = 11.sp)
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
              text = "Listing Ref: $postedRefNumber",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showSuccessDialog = false
            onSuccessNavigateHome()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
