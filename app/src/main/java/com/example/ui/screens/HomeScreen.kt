package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.PropertyItem
import com.example.data.SamplePropertyData
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground
import com.example.ui.theme.WarmAmberAccent

@Composable
fun HomeScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onNavigateToFindHouses: (searchQuery: String?) -> Unit,
  onNavigateToPostHouse: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedArea by remember { mutableStateOf<String?>(null) }
  var showAreaPicker by remember { mutableStateOf(false) }
  var showHelpDialog by remember { mutableStateOf(false) }
  var selectedPropertyForDetails by remember { mutableStateOf<PropertyItem?>(null) }
  var enquiryProperty by remember { mutableStateOf<PropertyItem?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    // 1. Header with Sky Blue branding
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle,
      selectedArea = selectedArea,
      onLocationClick = { showAreaPicker = true }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("home_screen_scroll"),
      contentPadding = PaddingValues(bottom = 28.dp)
    ) {
      // 2. Welcome Banner & Subtitle in Deep Navy
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MainBackgroundWhite
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 18.dp)
          ) {
            Text(
              text = DwelloStrings.homeWelcome(currentLanguage),
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText,
                lineHeight = 32.sp
              ),
              modifier = Modifier.testTag("welcome_heading_text")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = DwelloStrings.homeSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodyLarge.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
              ),
              modifier = Modifier.testTag("welcome_subtitle_text")
            )
          }
        }
      }

      // 3. Search Box with placeholder "Search area or location"
      item {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("search_area_input"),
            placeholder = {
              Text(
                text = DwelloStrings.searchPlaceholder(currentLanguage),
                style = MaterialTheme.typography.bodyLarge.copy(color = NavyTextSecondary.copy(alpha = 0.7f))
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SkyBluePrimary,
                modifier = Modifier.size(24.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = NavyTextSecondary
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder,
              focusedTextColor = DeepNavyText,
              unfocusedTextColor = DeepNavyText
            )
          )

          // Area Chips (Popular in Benin City)
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = DwelloStrings.popularAreasTitle(currentLanguage),
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(SamplePropertyData.beninAreas) { area ->
              val isSelected = selectedArea == area.name
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable {
                    selectedArea = if (isSelected) null else area.name
                    if (selectedArea != null) {
                      onNavigateToFindHouses(selectedArea)
                    }
                  }
                  .testTag("area_chip_${area.name}"),
                color = if (isSelected) SkyBluePrimary else Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
                    text = area.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.White else DeepNavyText
                    )
                  )
                }
              }
            }
          }
        }
      }

      // 4. TWO LARGE MAIN BUTTONS: FIND A HOUSE (Sky Blue) and POST A HOUSE (White with Sky Blue border)
      item {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Large Button 1: FIND A HOUSE - Sky Blue with white text
          Button(
            onClick = { onNavigateToFindHouses(searchQuery.ifEmpty { selectedArea }) },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("find_a_house_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SkyBluePrimary,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Home,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(horizontalAlignment = Alignment.Start) {
                Text(
                  text = DwelloStrings.findHouseButton(currentLanguage),
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    fontSize = 17.sp,
                    color = Color.White
                  )
                )
                Text(
                  text = DwelloStrings.findHouseSubtext(currentLanguage),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.9f)
                  )
                )
              }
            }
          }

          // Large Button 2: POST A HOUSE - White with Sky Blue border and blue text
          OutlinedButton(
            onClick = { onNavigateToPostHouse() },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("post_a_house_button"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, SkyBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White,
              contentColor = SkyBluePrimary
            )
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(SoftBlueAccent),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AddHome,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(horizontalAlignment = Alignment.Start) {
                Text(
                  text = DwelloStrings.postHouseButton(currentLanguage),
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    fontSize = 17.sp,
                    color = SkyBluePrimary
                  )
                )
                Text(
                  text = DwelloStrings.postHouseSubtext(currentLanguage),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = NavyTextSecondary
                  )
                )
              }
            }
          }
        }
      }

      // 5. Featured Properties Section
      item {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = DwelloStrings.featuredTitle(currentLanguage),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          TextButton(
            onClick = { onNavigateToFindHouses(null) },
            modifier = Modifier.testTag("see_all_houses_button")
          ) {
            Text(
              text = DwelloStrings.viewAll(currentLanguage),
              color = SkyBluePrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }

      // Property Cards with Photographs and Intermediary "Send Enquiry" Action
      val displayedProperties = SamplePropertyData.dynamicProperties.take(3)
      items(displayedProperties) { property ->
        PropertyCard(
          property = property,
          currentLanguage = currentLanguage,
          onClick = { selectedPropertyForDetails = property },
          onEnquiryClick = { enquiryProperty = property },
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
      }

      // 6. Contact Section: "Need help? Contact Dwello Homes."
      item {
        Spacer(modifier = Modifier.height(20.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("help_contact_section_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(SkyBluePrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.HelpOutline,
                  contentDescription = "Help",
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = DwelloStrings.needHelpTitle(currentLanguage),
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Text(
                  text = DwelloStrings.needHelpSubtext(currentLanguage),
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = NavyTextSecondary
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Direct Call to Dwello Homes
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                  context.startActivity(intent)
                },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("call_help_desk_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SkyBluePrimary,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Call,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.callDirect(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
              }

              // WhatsApp Dwello Homes
              OutlinedButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("whatsapp_dwello_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color.White,
                  contentColor = SkyBluePrimary
                ),
                border = BorderStroke(1.5.dp, SkyBluePrimary),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = SkyBluePrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.chatWhatsapp(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                  )
                )
              }
            }
          }
        }
      }
    }
  }

  // Benin City Area Picker Dialog
  if (showAreaPicker) {
    AlertDialog(
      onDismissRequest = { showAreaPicker = false },
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Select Area in Benin City" else "Pick Area for Benin City",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(SamplePropertyData.beninAreas) { area ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedArea = area.name
                  showAreaPicker = false
                  onNavigateToFindHouses(area.name)
                }
                .padding(vertical = 10.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = area.name,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepNavyText
                  )
                )
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) area.descriptionEn else area.descriptionPidgin,
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
              }
            }
            HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAreaPicker = false }) {
          Text("Close", color = SkyBluePrimary, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Help & Contact Info Dialog
  if (showHelpDialog) {
    AlertDialog(
      onDismissRequest = { showHelpDialog = false },
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Contact Dwello Homes" else "Reach Dwello Homes Support",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Column {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Dwello Homes connects you with property owners and coordinates physical property inspections in Benin City. Contact our support team for enquiries."
            else
              "Dwello Homes dey connect you with house owner and arrange physical inspection for Benin City. Call our team anytime.",
            style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "📞 Phone: +234 800 393 556\n💬 WhatsApp: +234 803 123 4567\n📍 Benin Hub: Ring Road Axis, Edo State",
            style = MaterialTheme.typography.bodySmall.copy(
              color = SkyBlueDark,
              fontWeight = FontWeight.SemiBold,
              lineHeight = 20.sp
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showHelpDialog = false
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
            context.startActivity(intent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("Call Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showHelpDialog = false }) {
          Text("Cancel", color = NavyTextSecondary)
        }
      }
    )
  }

  // Send Enquiry Dialog
  enquiryProperty?.let { prop ->
    EnquirySubmissionDialog(
      property = prop,
      currentLanguage = currentLanguage,
      onDismiss = { enquiryProperty = null },
      onSubmit = {
        enquiryProperty = null
      }
    )
  }

  // Property Details Dialog
  selectedPropertyForDetails?.let { prop ->
    PropertyDetailDialog(
      property = prop,
      currentLanguage = currentLanguage,
      onDismiss = { selectedPropertyForDetails = null },
      onEnquire = {
        selectedPropertyForDetails = null
        enquiryProperty = prop
      }
    )
  }
}

@Composable
fun PropertyCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onClick: () -> Unit,
  onEnquiryClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("property_card_${property.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, CardBorderSubtle)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Property Photograph Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
      ) {
        Image(
          painter = painterResource(id = property.imageResId),
          contentDescription = property.titleEn,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Demonstration Data Pill
        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopStart),
          color = Color(0xCC263746),
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Demonstration Listing" else "Demo Listing",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // Category Tag
        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopEnd),
          color = SkyBluePrimary,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.categoryEn else property.categoryPidgin,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      // Card Body
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Location Badge in soft blue
          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = property.area,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
          }

          // Price in Sky Blue
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = property.priceFormatted,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SkyBluePrimary
              )
            )
            Text(
              text = DwelloStrings.perYear(currentLanguage),
              style = MaterialTheme.typography.labelSmall.copy(
                color = NavyTextSecondary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title in Deep Navy
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText,
            fontSize = 16.sp
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) property.descriptionEn else property.descriptionPidgin,
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            lineHeight = 18.sp
          ),
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Amenities row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (property.hasBorehole) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Running Water",
                tint = SkyBluePrimary,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Water", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
            }
          }
          if (property.hasPrepaidMeter) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = "Prepaid Meter",
                tint = WarmAmberAccent,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Prepaid Meter", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
            }
          }
          if (property.isGated) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Gated",
                tint = SkyBlueDark,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Gated", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        // Property owner notice & Intermediary Send Enquiry action
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = property.landlordName,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Text(
              text = "Via Dwello Homes",
              style = MaterialTheme.typography.labelSmall.copy(color = SkyBluePrimary, fontWeight = FontWeight.SemiBold)
            )
          }

          Button(
            onClick = onEnquiryClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SkyBluePrimary,
              contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.testTag("send_enquiry_btn_${property.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DwelloStrings.sendEnquiry(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@Composable
fun EnquirySubmissionDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onSubmit: () -> Unit
) {
  var tenantName by remember { mutableStateOf("") }
  var tenantPhone by remember { mutableStateOf("") }
  var inspectionDate by remember { mutableStateOf("This Weekend") }
  var submitted by remember { mutableStateOf(false) }

  if (!submitted) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Send Enquiry via Dwello Homes" else "Send Enquiry to Dwello Homes",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText,
          fontSize = 18.sp
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Property: ${property.titleEn} (${property.area})",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = SkyBluePrimary
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Dwello Homes will connect you with the landlord and coordinate your physical inspection."
            else
              "Dwello Homes go connect you with the landlord and arrange your inspection date.",
            style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
          )
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = tenantName,
            onValueChange = { tenantName = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = tenantPhone,
            onValueChange = { tenantPhone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = inspectionDate,
            onValueChange = { inspectionDate = it },
            label = { Text("Preferred Inspection Time") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { submitted = true },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("Submit Enquiry")
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel", color = NavyTextSecondary)
        }
      }
    )
  } else {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBluePrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Enquiry Submitted!",
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        }
      },
      text = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH)
            "Your enquiry for ${property.titleEn} in ${property.area} has been received by Dwello Homes. We will contact the property owner and update you shortly."
          else
            "Dwello Homes don receive your enquiry for ${property.titleEn}. We go contact the house owner and get back to you sharp sharp.",
          style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onSubmit()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("Done")
        }
      }
    )
  }
}

@Composable
fun PropertyDetailDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onEnquire: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
        fontWeight = FontWeight.Bold,
        color = DeepNavyText,
        fontSize = 18.sp
      )
    },
    text = {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
          // Property Hero Photograph
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp)
              .clip(RoundedCornerShape(10.dp))
          ) {
            Image(
              painter = painterResource(id = property.imageResId),
              contentDescription = property.titleEn,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Demonstration Data Notice
          Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = NavyTextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH)
                  "Sample demonstration listing - DWELLO HOMES Benin City"
                else
                  "Sample demo listing - DWELLO HOMES Benin City",
                style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary, fontSize = 11.sp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "${property.priceFormatted} ${DwelloStrings.perYear(currentLanguage)}",
            style = MaterialTheme.typography.titleMedium.copy(
              color = SkyBluePrimary,
              fontWeight = FontWeight.ExtraBold
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "📍 ${property.neighborhoodNote}, ${property.area}, Benin City",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = NavyTextSecondary,
              fontWeight = FontWeight.Medium
            )
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.descriptionEn else property.descriptionPidgin,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DeepNavyText,
              lineHeight = 20.sp
            )
          )
          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            color = VeryLightBlueBackground,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Property Owner: ${property.landlordName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Text(
                text = "Intermediary: Dwello Homes coordinates all physical inspections and communications directly with the landlord.",
                style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onEnquire,
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
      ) {
        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(DwelloStrings.sendEnquiry(currentLanguage))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = NavyTextSecondary)
      }
    }
  )
}
