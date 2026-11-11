package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.SamplePropertyData
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightSagePill
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.SoftCreamBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmGoldAccent

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
  var rentPrice by remember { mutableStateOf("") }
  var landlordName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var hasWater by remember { mutableStateOf(true) }
  var hasPrepaidMeter by remember { mutableStateOf(true) }
  var isGated by remember { mutableStateOf(true) }

  var showSuccessDialog by remember { mutableStateOf(false) }

  val houseTypes = listOf("Self-Con", "2-Bedroom Flat", "3-Bedroom Flat", "Single Room", "Commercial Shop")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SoftCreamBackground)
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
          colors = CardDefaults.cardColors(containerColor = ForestGreenDark)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarmGoldAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AddHome,
                contentDescription = null,
                tint = ForestGreenDark,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = DwelloStrings.postHouseHeader(currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = DwelloStrings.postHouseInstruction(currentLanguage),
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color.White.copy(alpha = 0.85f)
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
            color = ForestGreenDark
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
                  color = if (isSelected) ForestGreenPrimary else Color.White,
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else SoftCreamBorder)
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
                        color = if (isSelected) Color.White else TextPrimaryDark
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

      // Step 2: Location in Benin City
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "2. Area in Benin City" else "2. Area wey the house dey for Benin",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
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
                  color = if (isSelected) LightSagePill else Color.White,
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else SoftCreamBorder)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = null,
                      tint = if (isSelected) ForestGreenPrimary else TextSecondaryDark,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = area.name,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ForestGreenDark else TextPrimaryDark
                      )
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = ForestGreenPrimary,
            unfocusedBorderColor = SoftCreamBorder
          )
        )

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 3: Rent Price
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "3. Yearly Rent (₦)" else "3. House Rent per year (₦)",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = rentPrice,
          onValueChange = { rentPrice = it },
          label = { Text(if (currentLanguage == AppLanguage.ENGLISH) "e.g. 250000" else "e.g. 250000") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rent_price_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = ForestGreenPrimary) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = ForestGreenPrimary,
            unfocusedBorderColor = SoftCreamBorder
          )
        )

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 4: Amenities
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "4. Amenities in Compound" else "4. Things wey dey the compound",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
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
              colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Running Water (Borehole)" else "Borehole Water / Running Tap",
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimaryDark
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
              colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Prepaid Meter (Light)" else "Prepaid Meter (Nepa Light)",
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimaryDark
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
              colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
            )
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Fenced & Gated Compound" else "Fenced Compound with Gate",
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimaryDark
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Step 5: Contact
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "5. Landlord / Caretaker Phone" else "5. Phone number to receive calls",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = landlordName,
          onValueChange = { landlordName = it },
          label = { Text(if (currentLanguage == AppLanguage.ENGLISH) "Your Name" else "Your Name") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("landlord_name_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = ForestGreenPrimary,
            unfocusedBorderColor = SoftCreamBorder
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { phoneNumber = it },
          label = { Text(if (currentLanguage == AppLanguage.ENGLISH) "Phone Number (e.g. 080...)" else "Phone Number (e.g. 080...)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("phone_number_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          leadingIcon = {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = ForestGreenPrimary)
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = ForestGreenPrimary,
            unfocusedBorderColor = SoftCreamBorder
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Submit Button
        Button(
          onClick = { showSuccessDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("submit_post_house_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreenPrimary,
            contentColor = Color.White
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "POST HOUSE NOW (FREE)" else "PUT HOUSE NOW (FREE)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            )
          )
        }
      }
    }
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
            tint = ForestGreenPrimary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "House Posted Successfully!" else "House Don Enter!",
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark,
            fontSize = 18.sp
          )
        }
      },
      text = {
        Column {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Your $houseType at $selectedArea, Benin City has been submitted. Tenants looking for houses in this area will contact you directly."
            else
              "Your $houseType for $selectedArea, Benin City don enter. Tenants wey dey find house for this area go call you direct.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark, lineHeight = 20.sp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = LightSagePill,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Listing Ref: DWL-BEN-${(1000..9999).random()}",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
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
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
        ) {
          Text("Done / Go Home")
        }
      }
    )
  }
}
