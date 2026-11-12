package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.EnquiryRepository
import com.example.data.PropertyApprovalStatus
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

enum class PropertyStatusFilter {
  ALL,
  APPROVED,
  PENDING,
  REJECTED,
  UNAVAILABLE
}

@Composable
fun LandlordDashboardScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  onNavigateToPostHouse: () -> Unit,
  onSwitchToTenantView: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf(PropertyStatusFilter.ALL) }
  var propertyToEdit by remember { mutableStateOf<PropertyItem?>(null) }
  var showEnquiriesDialog by remember { mutableStateOf(false) }
  var statusChangeNotification by remember { mutableStateOf<String?>(null) }

  // Re-read stats and properties from reactive repository
  val stats = remember(SamplePropertyData.dynamicProperties.size, SamplePropertyData.dynamicProperties.map { it.approvalStatus }) {
    SamplePropertyData.getLandlordStats()
  }

  val allLandlordProperties = remember(SamplePropertyData.dynamicProperties.size, SamplePropertyData.dynamicProperties.map { it.approvalStatus to it.titleEn }) {
    SamplePropertyData.getLandlordProperties()
  }

  val filteredProperties = remember(allLandlordProperties, selectedFilter) {
    when (selectedFilter) {
      PropertyStatusFilter.ALL -> allLandlordProperties
      PropertyStatusFilter.APPROVED -> allLandlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.APPROVED }
      PropertyStatusFilter.PENDING -> allLandlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL }
      PropertyStatusFilter.REJECTED -> allLandlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.REJECTED }
      PropertyStatusFilter.UNAVAILABLE -> allLandlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.UNAVAILABLE }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    // 1. Dwello Header
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("landlord_dashboard_scroll"),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      // 2. Landlord Profile & Welcome Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = VeryLightBlueBackground,
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SkyBluePrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = SamplePropertyData.CURRENT_LANDLORD_NAME,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.ExtraBold,
                      color = DeepNavyText
                    )
                  )
                  Text(
                    text = if (currentLanguage == AppLanguage.ENGLISH) "Verified Landlord • Benin City" else "Verified Landlord • Benin City",
                    style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                  )
                }
              }

              // Switch to Tenant View pill
              OutlinedButton(
                onClick = onSwitchToTenantView,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SkyBluePrimary),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("switch_to_tenant_view_btn")
              ) {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant View" else "Tenant View",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
              text = DwelloStrings.landlordDashboardSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(
                color = NavyTextSecondary,
                lineHeight = 18.sp
              )
            )
          }
        }
      }

      // 3. PROMINENT ADD NEW PROPERTY BUTTON
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
          Button(
            onClick = onNavigateToPostHouse,
            modifier = Modifier
              .fillMaxWidth()
              .height(58.dp)
              .testTag("prominent_add_property_btn"),
            shape = RoundedCornerShape(16.dp),
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
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Text(
                text = DwelloStrings.addNewPropertyButton(currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp,
                  color = Color.White
                )
              )
            }
          }
        }
      }

      // 4. DASHBOARD STATISTICS GRID (5 Key Metrics)
      item {
        Spacer(modifier = Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Listing Performance & Status" else "House Statistics & Status",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Row 1: Total Properties & Approved (Live)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Total Properties Stat
            StatCard(
              title = DwelloStrings.statTotalProperties(currentLanguage),
              count = stats.totalProperties.toString(),
              color = DeepNavyText,
              backgroundColor = Color(0xFFF1F5F9),
              borderColor = CardBorderSubtle,
              icon = Icons.Default.AddHome,
              modifier = Modifier.weight(1f).testTag("stat_card_total")
            )

            // Approved (Live) Stat
            StatCard(
              title = DwelloStrings.statApprovedProperties(currentLanguage),
              count = stats.approvedCount.toString(),
              color = Color(0xFF0369A1),
              backgroundColor = SoftBlueAccent,
              borderColor = SoftBlueBorder,
              icon = Icons.Default.CheckCircle,
              modifier = Modifier.weight(1f).testTag("stat_card_approved")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Row 2: Pending Approval & Rejected
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Pending Approval Stat
            StatCard(
              title = DwelloStrings.statPendingApproval(currentLanguage),
              count = stats.pendingCount.toString(),
              color = Color(0xFFB45309),
              backgroundColor = Color(0xFFFEF3C7),
              borderColor = Color(0xFFFDE68A),
              icon = Icons.Default.HourglassTop,
              modifier = Modifier.weight(1f).testTag("stat_card_pending")
            )

            // Rejected Stat
            StatCard(
              title = DwelloStrings.statRejectedProperties(currentLanguage),
              count = stats.rejectedCount.toString(),
              color = Color(0xFFB91C1C),
              backgroundColor = Color(0xFFFEE2E2),
              borderColor = Color(0xFFFECACA),
              icon = Icons.Default.ErrorOutline,
              modifier = Modifier.weight(1f).testTag("stat_card_rejected")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Row 3: New Enquiries Card (Full Width with interactive button)
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showEnquiriesDialog = true }
              .testTag("stat_card_enquiries"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
            border = BorderStroke(1.5.dp, SkyBluePrimary)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SkyBluePrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Mail,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = DwelloStrings.statNewEnquiries(currentLanguage),
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Text(
                    text = if (currentLanguage == AppLanguage.ENGLISH)
                      "Tenant inspection bookings via Dwello Homes"
                    else
                      "Inspection booking wey tenants request",
                    style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                  )
                }
              }

              Surface(
                color = SkyBluePrimary,
                shape = CircleShape
              ) {
                Text(
                  text = stats.newEnquiriesCount.toString(),
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                )
              }
            }
          }
        }
      }

      // 5. BUSINESS RULES NOTICE BANNER
      item {
        Spacer(modifier = Modifier.height(18.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("business_rules_notice_card"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          border = BorderStroke(1.dp, CardBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Dwello Homes Quality & Privacy Guarantee",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = DwelloStrings.noSelfApprovalNotice(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = DwelloStrings.privatePhoneProtectedNotice(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(color = SkyBlueDark, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            )
          }
        }
      }

      // 6. MY PROPERTIES SECTION & FILTER CHIPS
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
            text = "${DwelloStrings.myPropertiesHeading(currentLanguage)} (${filteredProperties.size})",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips row
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
        ) {
          items(PropertyStatusFilter.values()) { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
              PropertyStatusFilter.ALL -> "${DwelloStrings.filterAll(currentLanguage)} (${allLandlordProperties.size})"
              PropertyStatusFilter.APPROVED -> "Approved (${stats.approvedCount})"
              PropertyStatusFilter.PENDING -> "Pending (${stats.pendingCount})"
              PropertyStatusFilter.REJECTED -> "Rejected (${stats.rejectedCount})"
              PropertyStatusFilter.UNAVAILABLE -> "Unavailable (${stats.unavailableCount})"
            }

            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { selectedFilter = filter }
                .testTag("filter_chip_${filter.name}"),
              color = if (isSelected) SkyBluePrimary else Color.White,
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
            ) {
              Text(
                text = label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else DeepNavyText
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }

      // 7. MY PROPERTIES LIST ITEMS
      if (filteredProperties.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No listings found in this category",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
          }
        }
      } else {
        items(filteredProperties) { property ->
          LandlordPropertyItemCard(
            property = property,
            currentLanguage = currentLanguage,
            onEditClick = { propertyToEdit = property },
            onToggleAvailability = {
              val newStatus = SamplePropertyData.toggleListingAvailability(property.id)
              statusChangeNotification = if (newStatus == PropertyApprovalStatus.UNAVAILABLE) {
                "Listing marked as Unavailable. It is now hidden from public searches."
              } else {
                "Listing restored as Available."
              }
            },
            onResubmit = {
              SamplePropertyData.resubmitRejectedProperty(property.id)
              statusChangeNotification = "Property resubmitted for Dwello Homes verification."
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
          )
        }
      }
    }
  }

  // Edit Listing Dialog
  propertyToEdit?.let { prop ->
    EditPropertyDialog(
      property = prop,
      currentLanguage = currentLanguage,
      onDismiss = { propertyToEdit = null },
      onSave = { updatedTitle, updatedPrice, updatedArea, updatedDesc, updatedTerms ->
        val success = SamplePropertyData.updatePropertyListing(
          propertyId = prop.id,
          newTitle = updatedTitle,
          newPriceNaira = updatedPrice,
          newArea = updatedArea,
          newDescription = updatedDesc,
          newRentalTerms = updatedTerms
        )
        if (success) {
          statusChangeNotification = if (prop.approvalStatus == PropertyApprovalStatus.REJECTED) {
            "Listing updated and resubmitted for Dwello Homes review (Status: Pending Approval)."
          } else {
            "Listing updated successfully."
          }
        }
        propertyToEdit = null
      }
    )
  }

  // Tenant Enquiries Dialog
  if (showEnquiriesDialog) {
    TenantEnquiriesDialog(
      currentLanguage = currentLanguage,
      onDismiss = { showEnquiriesDialog = false }
    )
  }

  // Status Change Toast / Alert
  statusChangeNotification?.let { msg ->
    AlertDialog(
      onDismissRequest = { statusChangeNotification = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBluePrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Status Updated", fontWeight = FontWeight.Bold, color = DeepNavyText)
        }
      },
      text = {
        Text(text = msg, style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText))
      },
      confirmButton = {
        Button(
          onClick = { statusChangeNotification = null },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("OK")
        }
      }
    )
  }
}

@Composable
fun StatCard(
  title: String,
  count: String,
  color: Color,
  backgroundColor: Color,
  borderColor: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
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
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = count,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = color,
            fontSize = 24.sp
          )
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.SemiBold,
          color = DeepNavyText,
          fontSize = 12.sp
        ),
        maxLines = 1
      )
    }
  }
}

@Composable
fun LandlordPropertyItemCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onEditClick: () -> Unit,
  onToggleAvailability: () -> Unit,
  onResubmit: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("landlord_property_card_${property.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, CardBorderSubtle)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 1. Property Image Header with Approval Status Badge
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(145.dp)
      ) {
        Image(
          painter = painterResource(id = property.imageResId),
          contentDescription = property.titleEn,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Approval Status Badge
        val (badgeBg, badgeText, badgeLabel, badgeIcon) = when (property.approvalStatus) {
          PropertyApprovalStatus.APPROVED -> Quadruple(
            Color(0xFFE0F2FE),
            Color(0xFF0369A1),
            if (currentLanguage == AppLanguage.ENGLISH) "Approved & Live" else "Approved (Live)",
            Icons.Default.CheckCircle
          )
          PropertyApprovalStatus.PENDING_APPROVAL -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            if (currentLanguage == AppLanguage.ENGLISH) "Pending Approval" else "Under Review",
            Icons.Default.HourglassTop
          )
          PropertyApprovalStatus.REJECTED -> Quadruple(
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            if (currentLanguage == AppLanguage.ENGLISH) "Rejected" else "Rejected",
            Icons.Default.ErrorOutline
          )
          PropertyApprovalStatus.UNAVAILABLE -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            if (currentLanguage == AppLanguage.ENGLISH) "Unavailable" else "Not Available",
            Icons.Default.VisibilityOff
          )
        }

        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopEnd),
          color = badgeBg,
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, badgeText.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = badgeIcon,
              contentDescription = null,
              tint = badgeText,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = badgeLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = badgeText,
                fontSize = 11.sp
              )
            )
          }
        }

        // Category Tag at bottom left of image
        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.BottomStart),
          color = Color(0xCC263746),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = property.categoryEn,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp
            )
          )
        }
      }

      // 2. Card Details
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
          // Location Badge in soft blue
          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(6.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${property.area}, Benin City",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
          }

          // Price Formatted in Sky Blue
          Text(
            text = "${property.priceFormatted} / yr",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SkyBluePrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Property Title
        Text(
          text = property.titleEn,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText,
            fontSize = 17.sp
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Description snippet
        Text(
          text = property.descriptionEn,
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            lineHeight = 18.sp
          ),
          maxLines = 2
        )

        // 3. REJECTION REASON DISPLAY (Required for REJECTED properties)
        if (property.approvalStatus == PropertyApprovalStatus.REJECTED && !property.rejectionReason.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("rejection_reason_card_${property.id}"),
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFECACA))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = Color(0xFFB91C1C),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.rejectionReasonTitle(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB91C1C)
                  )
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = property.rejectionReason,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF7F1D1D),
                  lineHeight = 18.sp
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = DwelloStrings.rejectionFixHelp(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFF991B1B),
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        // PENDING NOTICE
        if (property.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFFBEB),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFFDE68A))
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Under verification by Dwello Homes. Hidden from public searches until approved.",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF92400E), fontSize = 11.sp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // 4. ACTION BUTTONS: EDIT LISTING & MARK UNAVAILABLE/AVAILABLE
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Edit Listing Button
          OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("edit_listing_btn_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, SkyBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = SkyBluePrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DwelloStrings.editListingButton(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
              )
            )
          }

          // Mark as Unavailable / Mark as Available Button
          if (property.approvalStatus == PropertyApprovalStatus.UNAVAILABLE) {
            // Restore Availability Button
            Button(
              onClick = onToggleAvailability,
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("mark_available_btn_${property.id}"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SkyBluePrimary,
                contentColor = Color.White
              )
            ) {
              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = DwelloStrings.markAvailableButton(currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold
                )
              )
            }
          } else {
            // Mark as Unavailable Button
            OutlinedButton(
              onClick = onToggleAvailability,
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("mark_unavailable_btn_${property.id}"),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, NavyTextSecondary.copy(alpha = 0.5f)),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
              Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = NavyTextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = DwelloStrings.markUnavailableButton(currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = DeepNavyText
                )
              )
            }
          }
        }
      }
    }
  }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun EditPropertyDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (title: String, priceNaira: Long, area: String, desc: String, terms: String) -> Unit
) {
  var editTitle by remember { mutableStateOf(property.titleEn) }
  var editPrice by remember { mutableStateOf(property.priceNaira.toString()) }
  var editArea by remember { mutableStateOf(property.area) }
  var editDesc by remember { mutableStateOf(property.descriptionEn) }
  var editTerms by remember { mutableStateOf(property.rentalTerms) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = SkyBluePrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Edit Property Listing" else "Change House Details",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText,
          fontSize = 18.sp
        )
      }
    },
    text = {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
          if (property.approvalStatus == PropertyApprovalStatus.REJECTED) {
            Surface(
              color = Color(0xFFFEF3C7),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Note: Saving edits to a rejected property will automatically resubmit it as 'Pending Approval' for Dwello Homes verification.",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E), fontSize = 11.sp)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
          }

          OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            label = { Text("Property Title") },
            modifier = Modifier.fillMaxWidth().testTag("edit_title_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editPrice,
            onValueChange = { editPrice = it },
            label = { Text("Yearly Rent (₦)") },
            modifier = Modifier.fillMaxWidth().testTag("edit_price_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = SkyBluePrimary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editArea,
            onValueChange = { editArea = it },
            label = { Text("Area / Location in Benin City") },
            modifier = Modifier.fillMaxWidth().testTag("edit_area_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editDesc,
            onValueChange = { editDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().testTag("edit_desc_input"),
            minLines = 3,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editTerms,
            onValueChange = { editTerms = it },
            label = { Text("Rental Terms") },
            modifier = Modifier.fillMaxWidth().testTag("edit_terms_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cleanPrice = editPrice.filter { it.isDigit() }.toLongOrNull() ?: property.priceNaira
          onSave(editTitle, cleanPrice, editArea, editDesc, editTerms)
        },
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
        modifier = Modifier.testTag("save_listing_changes_btn")
      ) {
        Text("Save & Submit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = NavyTextSecondary)
      }
    }
  )
}

@Composable
fun TenantEnquiriesDialog(
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit
) {
  val enquiries = EnquiryRepository.submittedEnquiries

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Mail, contentDescription = null, tint = SkyBluePrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant Inspection Bookings" else "Tenant Enquiries & Bookings",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText,
          fontSize = 18.sp
        )
      }
    },
    text = {
      if (enquiries.isEmpty()) {
        Text("No new enquiries currently received.")
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(enquiries) { enq ->
            Surface(
              color = VeryLightBlueBackground,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, SoftBlueBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = enq.tenantName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                  )
                  Text(
                    text = enq.propertyPrice,
                    style = MaterialTheme.typography.labelSmall.copy(color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Property: ${enq.propertyTitle} (${enq.propertyArea})",
                  style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Preferred Time: ${enq.preferredInspectionDate}",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "\"${enq.message}\"",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  color = Color.White,
                  shape = RoundedCornerShape(6.dp),
                  border = BorderStroke(1.dp, SoftBlueBorder)
                ) {
                  Text(
                    text = "Status: ${enq.statusEn} (Coordination via Dwello Homes)",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = SkyBlueDark)
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
      ) {
        Text("Close")
      }
    }
  )
}
