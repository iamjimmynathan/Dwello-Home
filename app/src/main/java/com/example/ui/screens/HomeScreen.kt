package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.PropertyItem
import com.example.data.SamplePropertyData
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.ForestAccent
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightSagePill
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.SoftCreamBorder
import com.example.ui.theme.SoftCreamCard
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmGoldAccent

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

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SoftCreamBackground)
  ) {
    // 1. Header with DWELLO HOMES name, house logo, language switch, Benin City location
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
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      // 2. Welcome Banner & Subtitle
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = SoftCreamBackground
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
                color = ForestGreenDark,
                lineHeight = 32.sp
              ),
              modifier = Modifier.testTag("welcome_heading_text")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = DwelloStrings.homeSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondaryDark,
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
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondaryDark.copy(alpha = 0.7f))
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ForestGreenPrimary,
                modifier = Modifier.size(24.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = TextSecondaryDark
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = ForestGreenPrimary,
              unfocusedBorderColor = SoftCreamBorder,
              focusedTextColor = TextPrimaryDark,
              unfocusedTextColor = TextPrimaryDark
            )
          )

          // Area Chips (Popular in Benin City)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = DwelloStrings.popularAreasTitle(currentLanguage),
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = ForestGreenDark
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
                color = if (isSelected) ForestGreenPrimary else Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else SoftCreamBorder)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isSelected) WarmGoldAccent else ForestAccent,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = area.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.White else TextPrimaryDark
                    )
                  )
                }
              }
            }
          }
        }
      }

      // 4. TWO LARGE MAIN BUTTONS: FIND A HOUSE and POST A HOUSE
      item {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Large Button 1: FIND A HOUSE
          Button(
            onClick = { onNavigateToFindHouses(searchQuery.ifEmpty { selectedArea }) },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("find_a_house_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ForestGreenPrimary,
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
                  .background(Color.White.copy(alpha = 0.2f)),
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
                    fontSize = 17.sp
                  )
                )
                Text(
                  text = DwelloStrings.findHouseSubtext(currentLanguage),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.85f)
                  )
                )
              }
            }
          }

          // Large Button 2: POST A HOUSE
          OutlinedButton(
            onClick = { onNavigateToPostHouse() },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("post_a_house_button"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, ForestGreenPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White,
              contentColor = ForestGreenPrimary
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
                  .background(ForestGreenContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AddHome,
                  contentDescription = null,
                  tint = ForestGreenPrimary,
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
                    color = ForestGreenPrimary
                  )
                )
                Text(
                  text = DwelloStrings.postHouseSubtext(currentLanguage),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryDark
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
              color = ForestGreenDark
            )
          )
          TextButton(
            onClick = { onNavigateToFindHouses(null) },
            modifier = Modifier.testTag("see_all_houses_button")
          ) {
            Text(
              text = DwelloStrings.viewAll(currentLanguage),
              color = ForestGreenPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }

      // Property Cards
      val displayedProperties = SamplePropertyData.sampleProperties.take(3)
      items(displayedProperties) { property ->
        PropertyCard(
          property = property,
          currentLanguage = currentLanguage,
          onClick = { selectedPropertyForDetails = property },
          onCallClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phoneContact.replace(" ", "")}"))
            context.startActivity(intent)
          },
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
      }

      // 6. Contact Section: "Need help? Contact a landlord or agent."
      item {
        Spacer(modifier = Modifier.height(20.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("help_contact_section_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ForestGreenDark),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(WarmGoldAccent),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.HelpOutline,
                  contentDescription = "Help",
                  tint = ForestGreenDark,
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = DwelloStrings.needHelpTitle(currentLanguage),
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
                Text(
                  text = DwelloStrings.needHelpSubtext(currentLanguage),
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.8f)
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Direct Call Button
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
                  containerColor = Color.White,
                  contentColor = ForestGreenDark
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Call,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = ForestGreenDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.callDirect(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
              }

              // WhatsApp / Agent Desk
              OutlinedButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("whatsapp_agent_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color.Transparent,
                  contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = WarmGoldAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.chatWhatsapp(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
          color = ForestGreenDark
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
                tint = ForestGreenPrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = area.name,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                  )
                )
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) area.descriptionEn else area.descriptionPidgin,
                  style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
              }
            }
            HorizontalDivider(color = SoftCreamBorder.copy(alpha = 0.5f))
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAreaPicker = false }) {
          Text("Close", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
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
          text = if (currentLanguage == AppLanguage.ENGLISH) "Benin City Property Support" else "Benin City Help Desk",
          fontWeight = FontWeight.Bold,
          color = ForestGreenDark
        )
      },
      text = {
        Column {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "We help tenants and landlords in Benin City connect safely. You can reach our local assistance team on WhatsApp or direct call."
            else
              "We dey help tenants and landlords for Benin City connect without wahala. You fit call our team direct or chat WhatsApp anytime.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "📞 Phone: +234 800 393 556\n💬 WhatsApp: +234 803 123 4567\n📍 Benin Office: Ring Road Axis, Edo State",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ForestGreenDark,
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
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
        ) {
          Text("Call Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showHelpDialog = false }) {
          Text("Cancel", color = TextSecondaryDark)
        }
      }
    )
  }

  // Property Details Dialog
  selectedPropertyForDetails?.let { prop ->
    PropertyDetailDialog(
      property = prop,
      currentLanguage = currentLanguage,
      onDismiss = { selectedPropertyForDetails = null },
      onCall = {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prop.phoneContact.replace(" ", "")}"))
        context.startActivity(intent)
      }
    )
  }
}

@Composable
fun PropertyCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onClick: () -> Unit,
  onCallClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("property_card_${property.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SoftCreamCard),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, SoftCreamBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Location Badge
        Surface(
          color = LightSagePill,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = ForestGreenPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = property.area,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreenDark
              )
            )
          }
        }

        // Price
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = property.priceFormatted,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = ForestGreenPrimary
            )
          )
          Text(
            text = DwelloStrings.perYear(currentLanguage),
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondaryDark
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title
      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimaryDark,
          fontSize = 16.sp
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH) property.descriptionEn else property.descriptionPidgin,
        style = MaterialTheme.typography.bodySmall.copy(
          color = TextSecondaryDark,
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
              tint = ForestAccent,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Water", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
          }
        }
        if (property.hasPrepaidMeter) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = "Prepaid Meter",
              tint = WarmGoldAccent,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Prepaid Meter", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
          }
        }
        if (property.isGated) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = "Gated",
              tint = ForestGreenPrimary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Gated", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = SoftCreamBorder.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(10.dp))

      // Landlord name & Call action
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
              color = TextPrimaryDark
            )
          )
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.landlordRoleEn else property.landlordRolePidgin,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
          )
        }

        Button(
          onClick = onCallClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreenPrimary,
            contentColor = Color.White
          ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("call_landlord_btn_${property.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = DwelloStrings.tapToCall(currentLanguage),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}

@Composable
fun PropertyDetailDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onCall: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
        fontWeight = FontWeight.Bold,
        color = ForestGreenDark,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "${property.priceFormatted} ${DwelloStrings.perYear(currentLanguage)}",
          style = MaterialTheme.typography.titleMedium.copy(
            color = ForestGreenPrimary,
            fontWeight = FontWeight.ExtraBold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "📍 ${property.neighborhoodNote}, ${property.area}, Benin City",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextSecondaryDark,
            fontWeight = FontWeight.Medium
          )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) property.descriptionEn else property.descriptionPidgin,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimaryDark,
            lineHeight = 20.sp
          )
        )
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          color = LightSagePill,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Contact: ${property.landlordName} (${if (currentLanguage == AppLanguage.ENGLISH) property.landlordRoleEn else property.landlordRolePidgin})",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreenDark
              )
            )
            Text(
              text = "Phone: ${property.phoneContact}",
              style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onCall,
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
      ) {
        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Call Now")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = TextSecondaryDark)
      }
    }
  )
}
