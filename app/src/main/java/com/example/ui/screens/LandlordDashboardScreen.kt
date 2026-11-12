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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.derivedStateOf
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

enum class LandlordPropertyFilter {
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
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf(LandlordPropertyFilter.ALL) }
  var propertyToEdit by remember { mutableStateOf<PropertyItem?>(null) }
  var showEnquiriesDialog by remember { mutableStateOf(false) }
  var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

  // Derive stats dynamically from repository
  val landlordStats by remember {
    derivedStateOf {
      SamplePropertyData.getLandlordStats(SamplePropertyData.CURRENT_LANDLORD_NAME)
    }
  }

  // Derive properties for current landlord
  val landlordProperties by remember {
    derivedStateOf {
      SamplePropertyData.getLandlordProperties(SamplePropertyData.CURRENT_LANDLORD_NAME)
    }
  }

  // Filter properties
  val filteredProperties = remember(landlordProperties, selectedFilter) {
    when (selectedFilter) {
      LandlordPropertyFilter.ALL -> landlordProperties
      LandlordPropertyFilter.APPROVED -> landlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.APPROVED }
      LandlordPropertyFilter.PENDING -> landlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL }
      LandlordPropertyFilter.REJECTED -> landlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.REJECTED }
      LandlordPropertyFilter.UNAVAILABLE -> landlordProperties.filter { it.approvalStatus == PropertyApprovalStatus.UNAVAILABLE }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    // 1. App Header with Language Switch
    DwelloHeader(
      currentLanguage = currentLanguage,
      onLanguageToggle = onLanguageToggle
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("landlord_dashboard_scroll"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // 2. Top Title Bar with Back Button & Landlord Info
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onBackToHome)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = SkyBluePrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = DwelloStrings.landlordDashboardTitle(currentLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = DeepNavyText
                )
              )
              Text(
                text = "Welcome, ${SamplePropertyData.CURRENT_LANDLORD_NAME} • Benin City",
                style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
              )
            }
          }

          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Text(
              text = "LANDLORD",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary,
                fontSize = 11.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 3. Privacy & Phone Protection Notice
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
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

        Spacer(modifier = Modifier.height(16.dp))
      }

      // 4. Dashboard Statistics Grid / Cards
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Portfolio Statistics" else "House Summary Stats",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Total, Approved, Pending
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Total Properties
          DashboardStatCard(
            title = DwelloStrings.statTotalProperties(currentLanguage),
            count = landlordStats.totalProperties.toString(),
            colorBg = SoftBlueAccent,
            colorContent = SkyBluePrimary,
            modifier = Modifier.weight(1f),
            testTag = "stat_total_properties"
          )

          // Approved & Live
          DashboardStatCard(
            title = DwelloStrings.statApprovedProperties(currentLanguage),
            count = landlordStats.approvedCount.toString(),
            colorBg = Color(0xFFE8F5E9),
            colorContent = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f),
            testTag = "stat_approved_properties"
          )

          // Pending Approval
          DashboardStatCard(
            title = DwelloStrings.statPendingApproval(currentLanguage),
            count = landlordStats.pendingCount.toString(),
            colorBg = Color(0xFFFEF3C7),
            colorContent = Color(0xFFB45309),
            modifier = Modifier.weight(1f),
            testTag = "stat_pending_properties"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Rejected, New Enquiries
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Rejected
          DashboardStatCard(
            title = DwelloStrings.statRejectedProperties(currentLanguage),
            count = landlordStats.rejectedCount.toString(),
            colorBg = Color(0xFFFEE2E2),
            colorContent = Color(0xFFDC2626),
            modifier = Modifier.weight(1f),
            testTag = "stat_rejected_properties"
          )

          // New Enquiries (Clickable to view details)
          DashboardStatCard(
            title = DwelloStrings.statNewEnquiries(currentLanguage),
            count = landlordStats.newEnquiriesCount.toString(),
            colorBg = SoftBlueAccent,
            colorContent = SkyBlueDark,
            isClickable = true,
            onClick = { showEnquiriesDialog = true },
            modifier = Modifier.weight(1f),
            testTag = "stat_new_enquiries"
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 5. PROMINENT "ADD NEW PROPERTY" BUTTON (Sky Blue #168BE8)
      item {
        Button(
          onClick = onNavigateToPostHouse,
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag("add_new_property_button"),
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
                .background(Color.White.copy(alpha = 0.25f)),
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
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(22.dp))
      }

      // 6. Section Heading: My Properties & Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = DwelloStrings.myPropertiesHeading(currentLanguage),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Text(
            text = "${filteredProperties.size} listed",
            style = MaterialTheme.typography.labelMedium.copy(
              color = NavyTextSecondary,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(LandlordPropertyFilter.values()) { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
              LandlordPropertyFilter.ALL -> "${DwelloStrings.filterAll(currentLanguage)} (${landlordProperties.size})"
              LandlordPropertyFilter.APPROVED -> "Approved (${landlordStats.approvedCount})"
              LandlordPropertyFilter.PENDING -> "Pending (${landlordStats.pendingCount})"
              LandlordPropertyFilter.REJECTED -> "Rejected (${landlordStats.rejectedCount})"
              LandlordPropertyFilter.UNAVAILABLE -> "Unavailable (${landlordStats.unavailableCount})"
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
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else DeepNavyText
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }

      // 7. My Properties List
      if (filteredProperties.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp),
            shape = RoundedCornerShape(14.dp),
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
                tint = NavyTextSecondary,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH)
                  "No properties found in this category"
                else
                  "No house found for this category",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = DeepNavyText
                )
              )
              Spacer(modifier = Modifier.height(12.dp))
              OutlinedButton(
                onClick = { selectedFilter = LandlordPropertyFilter.ALL },
                border = BorderStroke(1.dp, SkyBluePrimary)
              ) {
                Text("Show All Properties", color = SkyBluePrimary)
              }
            }
          }
        }
      } else {
        items(filteredProperties) { property ->
          LandlordPropertyCard(
            property = property,
            currentLanguage = currentLanguage,
            onEditClick = { propertyToEdit = property },
            onToggleAvailability = {
              val newStatus = SamplePropertyData.toggleListingAvailability(property.id)
              actionFeedbackMessage = if (newStatus == PropertyApprovalStatus.UNAVAILABLE) {
                "Property marked as Unavailable and hidden from tenant searches."
              } else {
                "Property marked as Available."
              }
            },
            onResubmitClick = {
              val success = SamplePropertyData.resubmitRejectedProperty(property.id)
              if (success) {
                actionFeedbackMessage = "Property resubmitted for Dwello Homes verification."
              }
            },
            modifier = Modifier.padding(bottom = 16.dp)
          )
        }
      }

      // 8. Business Rules & Compliance Notice at bottom
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH) "Dwello Homes Quality Standards" else "Dwello Homes Rule",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = DwelloStrings.noSelfApprovalNotice(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(
                color = NavyTextSecondary,
                lineHeight = 18.sp
              )
            )
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
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
        SamplePropertyData.updatePropertyListing(
          propertyId = prop.id,
          newTitle = updatedTitle,
          newPriceNaira = updatedPrice,
          newArea = updatedArea,
          newDescription = updatedDesc,
          newRentalTerms = updatedTerms
        )
        actionFeedbackMessage = "Listing updated! Status updated to Under Review."
        propertyToEdit = null
      }
    )
  }

  // Tenant Enquiries View Dialog
  if (showEnquiriesDialog) {
    LandlordEnquiriesDialog(
      currentLanguage = currentLanguage,
      onDismiss = { showEnquiriesDialog = false }
    )
  }

  // Action Feedback Toast / Alert Dialog
  actionFeedbackMessage?.let { msg ->
    AlertDialog(
      onDismissRequest = { actionFeedbackMessage = null },
      icon = {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBluePrimary)
      },
      title = {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Status Updated" else "Update Done",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(text = msg, style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText))
      },
      confirmButton = {
        Button(
          onClick = { actionFeedbackMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
        ) {
          Text("OK")
        }
      }
    )
  }
}

@Composable
fun DashboardStatCard(
  title: String,
  count: String,
  colorBg: Color,
  colorContent: Color,
  modifier: Modifier = Modifier,
  isClickable: Boolean = false,
  onClick: () -> Unit = {},
  testTag: String = ""
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier)
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = colorBg),
    border = BorderStroke(1.dp, colorContent.copy(alpha = 0.25f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = count,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = colorContent,
          fontSize = 24.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.SemiBold,
          color = DeepNavyText,
          fontSize = 11.sp,
          lineHeight = 14.sp
        ),
        maxLines = 2
      )
    }
  }
}

@Composable
fun LandlordPropertyCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onEditClick: () -> Unit,
  onToggleAvailability: () -> Unit,
  onResubmitClick: () -> Unit,
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
      // 1. Property Image & Approval Status Badge
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

        // Status Badge at top right
        val (badgeBg, badgeText, badgeIcon, statusLabel) = when (property.approvalStatus) {
          PropertyApprovalStatus.APPROVED -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            Icons.Default.CheckCircle,
            "APPROVED (LIVE)"
          )
          PropertyApprovalStatus.PENDING_APPROVAL -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            Icons.Default.HourglassTop,
            "PENDING APPROVAL"
          )
          PropertyApprovalStatus.REJECTED -> Quadruple(
            Color(0xFFFEE2E2),
            Color(0xFF991B1B),
            Icons.Default.Close,
            "REJECTED"
          )
          PropertyApprovalStatus.UNAVAILABLE -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            Icons.Default.VisibilityOff,
            "UNAVAILABLE"
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
              text = statusLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = badgeText,
                fontSize = 11.sp
              )
            )
          }
        }

        // Location Chip on top left
        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopStart),
          color = Color(0xCC263746),
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = property.area,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      // 2. Card Content (Title, Price, Location details)
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
          Text(
            text = property.titleEn,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            ),
            modifier = Modifier.weight(1f)
          )

          Text(
            text = "${property.priceFormatted} / yr",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SkyBluePrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "📍 ${property.neighborhoodNote}, ${property.area}, Benin City",
          style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
        )

        // 3. REJECTION REASON DISPLAY (Required for Stage 3A)
        if (property.approvalStatus == PropertyApprovalStatus.REJECTED && !property.rejectionReason.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = Color(0xFFDC2626),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.rejectionReasonTitle(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                  )
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = property.rejectionReason,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF7F1D1D),
                  lineHeight = 18.sp,
                  fontWeight = FontWeight.Medium
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = DwelloStrings.rejectionFixHelp(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SkyBluePrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
        }

        // 4. Pending Approval Status Message
        if (property.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = Color(0xFFFFFBEB),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Under review by Dwello Homes moderation team in Benin City. Landlords cannot self-approve.",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFF92400E),
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        // 5. Unavailable notice
        if (property.approvalStatus == PropertyApprovalStatus.UNAVAILABLE) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = NavyTextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "This property is marked as unavailable/occupied and is hidden from public tenant searches.",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NavyTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        // 6. Action Buttons: EDIT LISTING & MARK UNAVAILABLE
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Edit Listing Option
          OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("edit_listing_button_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, SkyBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White,
              contentColor = SkyBluePrimary
            )
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = SkyBluePrimary
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

          // Mark as Unavailable / Mark as Available Option
          val isCurrentlyUnavailable = property.approvalStatus == PropertyApprovalStatus.UNAVAILABLE
          Button(
            onClick = onToggleAvailability,
            modifier = Modifier
              .weight(1.2f)
              .height(44.dp)
              .testTag("toggle_availability_button_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCurrentlyUnavailable) SkyBluePrimary else Color(0xFFF1F5F9),
              contentColor = if (isCurrentlyUnavailable) Color.White else DeepNavyText
            )
          ) {
            Icon(
              imageVector = if (isCurrentlyUnavailable) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = if (isCurrentlyUnavailable) Color.White else DeepNavyText
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isCurrentlyUnavailable)
                DwelloStrings.markAvailableButton(currentLanguage)
              else
                DwelloStrings.markUnavailableButton(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
          }

          // Resubmit Button (If REJECTED)
          if (property.approvalStatus == PropertyApprovalStatus.REJECTED) {
            OutlinedButton(
              onClick = onResubmitClick,
              modifier = Modifier
                .height(44.dp)
                .testTag("resubmit_button_${property.id}"),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.5.dp, Color(0xFFD97706)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
              )
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFB45309)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun EditPropertyDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit,
  onSave: (title: String, price: Long, area: String, description: String, rentalTerms: String) -> Unit
) {
  var title by remember { mutableStateOf(property.titleEn) }
  var priceString by remember { mutableStateOf(property.priceNaira.toString()) }
  var area by remember { mutableStateOf(property.area) }
  var description by remember { mutableStateOf(property.descriptionEn) }
  var rentalTerms by remember { mutableStateOf(property.rentalTerms) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = SkyBluePrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Edit Property Listing" else "Edit House Details",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText,
          fontSize = 18.sp
        )
      }
    },
    text = {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
          Text(
            text = "Editing this property will submit changes to Dwello Homes for review.",
            style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
          )
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Property Title") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_title_input"),
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
            value = priceString,
            onValueChange = { priceString = it },
            label = { Text("Yearly Rent (₦)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = SkyBluePrimary) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_price_input"),
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
            value = area,
            onValueChange = { area = it },
            label = { Text("Area in Benin City") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_area_input"),
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
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Amenities") },
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp)
              .testTag("edit_description_input"),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = rentalTerms,
            onValueChange = { rentalTerms = it },
            label = { Text("Rental Terms") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_terms_input"),
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
          val cleanPrice = priceString.filter { it.isDigit() }.toLongOrNull() ?: property.priceNaira
          onSave(title, cleanPrice, area, description, rentalTerms)
        },
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
        modifier = Modifier.testTag("save_listing_changes_button")
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
fun LandlordEnquiriesDialog(
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
          text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant Inspection Enquiries" else "Tenant Enquiries",
          fontWeight = FontWeight.Bold,
          color = DeepNavyText,
          fontSize = 18.sp
        )
      }
    },
    text = {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
          Surface(
            color = VeryLightBlueBackground,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "Dwello Homes coordinates tenant inspections directly. You do not need to share your phone number publicly."
              else
                "Dwello Homes dey coordinate all inspection direct. You no need give tenant your phone number.",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        if (enquiries.isEmpty()) {
          item {
            Text(
              text = "No pending enquiries at this time.",
              style = MaterialTheme.typography.bodyMedium.copy(color = NavyTextSecondary)
            )
          }
        } else {
          items(enquiries) { enq ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = enq.propertyTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Text(
                    text = enq.propertyPrice,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = SkyBluePrimary
                    )
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Tenant: ${enq.tenantName} (${enq.tenantPhone})",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepNavyText
                  )
                )
                Text(
                  text = "Preferred Date: ${enq.preferredInspectionDate}",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  color = SoftBlueAccent,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "Status: ${enq.statusEn}",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = SkyBlueDark,
                      fontWeight = FontWeight.Bold
                    )
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

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
