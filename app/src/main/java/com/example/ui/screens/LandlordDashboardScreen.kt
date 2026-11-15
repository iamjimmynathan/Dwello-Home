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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
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
import androidx.compose.material3.CircularProgressIndicator
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
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.AuthenticatedUser
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.EnquiryStatus
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
import com.example.data.calculateLandlordStats
import com.example.ui.AsyncResourceState
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
  currentUser: AuthenticatedUser?,
  propertiesState: AsyncResourceState<List<PropertyItem>>,
  enquiriesState: AsyncResourceState<List<EnquiryItem>>,
  isActionLoading: Boolean,
  actionMessage: String?,
  actionError: String?,
  onLanguageToggle: () -> Unit,
  onRetryLoad: () -> Unit,
  onNavigateToPostHouse: () -> Unit,
  onUpdateListing: (
    propertyId: String,
    title: String,
    rentAmount: Long,
    neighbourhood: String,
    description: String,
    fullAddress: String?,
    landmark: String?
  ) -> Unit,
  onToggleAvailability: (PropertyItem) -> Unit,
  onUpdateEnquiryStatus: (enquiryId: String, status: EnquiryStatus) -> Unit,
  onClearFeedback: () -> Unit,
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf(LandlordPropertyFilter.ALL) }
  var propertyToEdit by remember { mutableStateOf<PropertyItem?>(null) }
  var propertyToViewDetails by remember { mutableStateOf<PropertyItem?>(null) }
  var showEnquiriesDialog by remember { mutableStateOf(false) }

  val landlordProperties = when (propertiesState) {
    is AsyncResourceState.Success -> propertiesState.data
    else -> emptyList()
  }
  val landlordEnquiries = when (enquiriesState) {
    is AsyncResourceState.Success -> enquiriesState.data
    else -> emptyList()
  }

  val landlordStats = remember(landlordProperties, landlordEnquiries) {
    calculateLandlordStats(landlordProperties, landlordEnquiries)
  }

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
      // Top Title Bar with Landlord Info & Refresh button
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .weight(1f)
              .clickable(onClick = onBackToHome)
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
                text = "Welcome, ${currentUser?.fullName ?: "Landlord"} • Benin City",
                style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onRetryLoad,
              modifier = Modifier.testTag("landlord_refresh_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh dashboard",
                tint = SkyBluePrimary
              )
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
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Privacy & Phone Protection Notice
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

      // Dashboard Statistics Grid / Cards
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Portfolio Statistics" else "House Summary Stats",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          DashboardStatCard(
            title = DwelloStrings.statTotalProperties(currentLanguage),
            count = landlordStats.totalProperties.toString(),
            colorBg = SoftBlueAccent,
            colorContent = SkyBluePrimary,
            modifier = Modifier.weight(1f),
            testTag = "stat_total_properties"
          )

          DashboardStatCard(
            title = DwelloStrings.statApprovedProperties(currentLanguage),
            count = landlordStats.approvedCount.toString(),
            colorBg = Color(0xFFE8F5E9),
            colorContent = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f),
            testTag = "stat_approved_properties"
          )

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

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          DashboardStatCard(
            title = DwelloStrings.statRejectedProperties(currentLanguage),
            count = landlordStats.rejectedCount.toString(),
            colorBg = Color(0xFFFEE2E2),
            colorContent = Color(0xFFDC2626),
            modifier = Modifier.weight(1f),
            testTag = "stat_rejected_properties"
          )

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

      // PROMINENT "ADD NEW PROPERTY" BUTTON
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

      // Section Heading: My Properties & Filter Chips
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

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(LandlordPropertyFilter.entries.toTypedArray()) { filter ->
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

      // My Properties List / Loading / Error / Empty
      when (propertiesState) {
        is AsyncResourceState.Idle,
        is AsyncResourceState.Loading -> {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("landlord_properties_loading"),
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
                CircularProgressIndicator(color = SkyBluePrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Loading your properties...",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepNavyText,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
        }

        is AsyncResourceState.Error -> {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("landlord_properties_error"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
              border = BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = propertiesState.message,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Medium
                  )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = onRetryLoad,
                  colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                  modifier = Modifier.testTag("landlord_retry_button")
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(DwelloStrings.retryLabel(currentLanguage))
                }
              }
            }
          }
        }

        is AsyncResourceState.Success -> {
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
                    text = if (landlordProperties.isEmpty()) {
                      if (currentLanguage == AppLanguage.ENGLISH)
                        "You haven't posted any properties yet."
                      else
                        "You never post any house yet."
                    } else {
                      if (currentLanguage == AppLanguage.ENGLISH)
                        "No properties found in this category"
                      else
                        "No house found for this category"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = DeepNavyText
                    )
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  if (landlordProperties.isEmpty()) {
                    Button(
                      onClick = onNavigateToPostHouse,
                      colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                    ) {
                      Text(DwelloStrings.addNewPropertyButton(currentLanguage), fontWeight = FontWeight.Bold)
                    }
                  } else {
                    OutlinedButton(
                      onClick = { selectedFilter = LandlordPropertyFilter.ALL },
                      border = BorderStroke(1.dp, SkyBluePrimary)
                    ) {
                      Text("Show All Properties", color = SkyBluePrimary)
                    }
                  }
                }
              }
            }
          } else {
            items(filteredProperties, key = { it.id }) { property ->
              LandlordPropertyCard(
                property = property,
                currentLanguage = currentLanguage,
                isActionLoading = isActionLoading,
                onViewDetailsClick = { propertyToViewDetails = property },
                onEditClick = { propertyToEdit = property },
                onToggleAvailability = { onToggleAvailability(property) },
                modifier = Modifier.padding(bottom = 16.dp)
              )
            }
          }
        }
      }

      // Business Rules & Compliance Notice at bottom
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

  // Property Details Dialog (shows full info, photos, submission date, and rejection reason)
  propertyToViewDetails?.let { prop ->
    LandlordPropertyDetailsDialog(
      property = prop,
      currentLanguage = currentLanguage,
      onEditClick = {
        propertyToViewDetails = null
        propertyToEdit = prop
      },
      onDismiss = { propertyToViewDetails = null }
    )
  }

  // Edit Listing Dialog (PATCH /api/properties/{id})
  propertyToEdit?.let { prop ->
    EditPropertyDialog(
      property = prop,
      currentLanguage = currentLanguage,
      isSaving = isActionLoading,
      onDismiss = { propertyToEdit = null },
      onSave = { updatedTitle, updatedPrice, updatedArea, updatedDesc, updatedAddress, updatedLandmark ->
        onUpdateListing(
          prop.id,
          updatedTitle,
          updatedPrice,
          updatedArea,
          updatedDesc,
          updatedAddress,
          updatedLandmark
        )
        propertyToEdit = null
      }
    )
  }

  // Tenant Enquiries View Dialog (GET /api/landlord/enquiries + PATCH /api/landlord/enquiries/{id})
  if (showEnquiriesDialog) {
    LandlordEnquiriesDialog(
      currentLanguage = currentLanguage,
      enquiriesState = enquiriesState,
      isActionLoading = isActionLoading,
      onUpdateStatus = onUpdateEnquiryStatus,
      onDismiss = { showEnquiriesDialog = false }
    )
  }

  // Action Feedback Dialog (success or error)
  val feedbackMsg = actionError ?: actionMessage
  if (!feedbackMsg.isNullOrBlank()) {
    val isErr = actionError != null
    AlertDialog(
      onDismissRequest = onClearFeedback,
      icon = {
        Icon(
          imageVector = if (isErr) Icons.Default.Warning else Icons.Default.CheckCircle,
          contentDescription = null,
          tint = if (isErr) Color(0xFFDC2626) else SkyBluePrimary
        )
      },
      title = {
        Text(
          text = if (isErr) {
            if (currentLanguage == AppLanguage.ENGLISH) "Action Failed" else "Something Wrong"
          } else {
            if (currentLanguage == AppLanguage.ENGLISH) "Status Updated" else "Update Done"
          },
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      },
      text = {
        Text(text = feedbackMsg, style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavyText))
      },
      confirmButton = {
        Button(
          onClick = onClearFeedback,
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
  isActionLoading: Boolean,
  onViewDetailsClick: () -> Unit,
  onEditClick: () -> Unit,
  onToggleAvailability: () -> Unit,
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
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
      ) {
        if (property.photoUris.isNotEmpty()) {
          AsyncImage(
            model = property.photoUris.first(),
            contentDescription = property.titleEn,
            placeholder = painterResource(id = property.imageResId),
            error = painterResource(id = property.imageResId),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Image(
            painter = painterResource(id = property.imageResId),
            contentDescription = property.titleEn,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

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

          val periodShort = if (property.rentalPeriod == RentalPeriod.MONTHLY) "/ mo" else "/ yr"
          Text(
            text = "${property.priceFormatted} $periodShort",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SkyBluePrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${property.neighborhoodNote}, ${property.area}, Benin City",
          style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${DwelloStrings.submittedDatePrefix(currentLanguage)}: ${property.createdAtDisplay.ifBlank { "Submitted" }}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = NavyTextSecondary,
            fontWeight = FontWeight.SemiBold
          ),
          modifier = Modifier.testTag("landlord_property_submitted_date_${property.id}")
        )

        if (property.approvalStatus == PropertyApprovalStatus.REJECTED) {
          val reasonText = property.rejectionReason?.takeIf { it.isNotBlank() }
            ?: "This listing did not meet Dwello Homes verification requirements. Please edit and resubmit."
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("rejection_reason_box_${property.id}")
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
                text = reasonText,
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

        // Row 1: View Details + Edit Listing / Edit Pending Listing
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onViewDetailsClick,
            enabled = !isActionLoading,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("view_landlord_details_button_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, SoftBlueBorder),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = VeryLightBlueBackground,
              contentColor = DeepNavyText
            )
          ) {
            Icon(
              imageVector = Icons.Default.Visibility,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = SkyBluePrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DwelloStrings.viewPropertyDetailsBtn(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText,
                fontSize = 12.sp
              )
            )
          }

          val editLabel = when (property.approvalStatus) {
            PropertyApprovalStatus.PENDING_APPROVAL -> DwelloStrings.editPendingListingBtn(currentLanguage)
            PropertyApprovalStatus.REJECTED -> DwelloStrings.editAndResubmitBtn(currentLanguage)
            else -> DwelloStrings.editListingButton(currentLanguage)
          }

          OutlinedButton(
            onClick = onEditClick,
            enabled = !isActionLoading,
            modifier = Modifier
              .weight(1.2f)
              .height(48.dp)
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
              text = editLabel,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary,
                fontSize = 12.sp
              )
            )
          }
        }

        // Row 2: Mark approved property as unavailable (only on APPROVED listings) or See Rejection Reason (on REJECTED)
        if (property.approvalStatus == PropertyApprovalStatus.APPROVED) {
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = onToggleAvailability,
            enabled = !isActionLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("toggle_availability_button_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFF1F5F9),
              contentColor = DeepNavyText
            )
          ) {
            Icon(
              imageVector = Icons.Default.VisibilityOff,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = DeepNavyText
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DwelloStrings.markUnavailableButton(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
          }
        } else if (property.approvalStatus == PropertyApprovalStatus.REJECTED) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = onViewDetailsClick,
            enabled = !isActionLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("see_rejection_reason_button_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFFFEF2F2),
              contentColor = Color(0xFF991B1B)
            )
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = Color(0xFFDC2626)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DwelloStrings.seeRejectionReasonBtn(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF991B1B)
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun LandlordPropertyDetailsDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onEditClick: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("landlord_property_details_dialog"),
    title = {
      Text(
        text = property.titleEn,
        fontWeight = FontWeight.ExtraBold,
        color = DeepNavyText,
        fontSize = 18.sp
      )
    },
    text = {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
              .clip(RoundedCornerShape(12.dp))
          ) {
            if (property.photoUris.isNotEmpty()) {
              AsyncImage(
                model = property.photoUris.first(),
                contentDescription = property.titleEn,
                placeholder = painterResource(id = property.imageResId),
                error = painterResource(id = property.imageResId),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Image(
                painter = painterResource(id = property.imageResId),
                contentDescription = property.titleEn,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))

          val periodText = if (property.rentalPeriod == RentalPeriod.MONTHLY) "/ month" else "/ year"
          Text(
            text = "${property.priceFormatted} $periodText",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SkyBluePrimary
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${property.categoryEn} • ${property.bedrooms} Bed • ${property.bathrooms} Bath",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${property.area}, Benin City • ${property.fullAddress.ifBlank { property.neighborhoodNote }}",
            style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${DwelloStrings.submittedDatePrefix(currentLanguage)}: ${property.createdAtDisplay.ifBlank { "Submitted" }}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NavyTextSecondary,
              fontWeight = FontWeight.Bold
            )
          )

          if (property.approvalStatus == PropertyApprovalStatus.REJECTED) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              color = Color(0xFFFEF2F2),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, Color(0xFFFECACA)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = DwelloStrings.rejectionReasonTitle(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = property.rejectionReason?.takeIf { it.isNotBlank() }
                    ?: "Listing rejected by Dwello Homes moderation team.",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = property.descriptionEn,
            style = MaterialTheme.typography.bodySmall.copy(
              color = DeepNavyText,
              lineHeight = 18.sp
            )
          )
          if (property.facilities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Facilities: ${property.facilities.joinToString(", ")}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onEditClick,
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
      ) {
        Text(DwelloStrings.editListingButton(currentLanguage))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = NavyTextSecondary)
      }
    }
  )
}

@Composable
fun EditPropertyDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (
    title: String,
    price: Long,
    area: String,
    description: String,
    fullAddress: String?,
    landmark: String?
  ) -> Unit
) {
  var title by remember { mutableStateOf(property.titleEn) }
  var priceString by remember { mutableStateOf(property.priceNaira.toString()) }
  var area by remember { mutableStateOf(property.area) }
  var description by remember { mutableStateOf(property.descriptionEn) }
  var fullAddress by remember { mutableStateOf(property.fullAddress) }
  var landmark by remember { mutableStateOf(property.landmark) }

  AlertDialog(
    onDismissRequest = { if (!isSaving) onDismiss() },
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
            text = "Saving edits updates the server listing and submits changes for Dwello Homes review.",
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
            enabled = !isSaving,
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
            onValueChange = { priceString = it.filter { ch -> ch.isDigit() } },
            label = { Text("Rent Amount (₦)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = SkyBluePrimary) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_price_input"),
            singleLine = true,
            enabled = !isSaving,
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
            label = { Text("Neighbourhood in Benin City") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_area_input"),
            singleLine = true,
            enabled = !isSaving,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = fullAddress,
            onValueChange = { fullAddress = it },
            label = { Text("Full Address (Optional)") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_address_input"),
            singleLine = true,
            enabled = !isSaving,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = VeryLightBlueBackground,
              unfocusedContainerColor = VeryLightBlueBackground,
              focusedBorderColor = SkyBluePrimary,
              unfocusedBorderColor = SoftBlueBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = landmark,
            onValueChange = { landmark = it },
            label = { Text("Nearby Landmark (Optional)") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_landmark_input"),
            singleLine = true,
            enabled = !isSaving,
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
            enabled = !isSaving,
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
          onSave(
            title.trim().ifBlank { property.titleEn },
            cleanPrice,
            area.trim().ifBlank { property.area },
            description.trim().ifBlank { property.descriptionEn },
            fullAddress.trim().takeIf { it.isNotEmpty() },
            landmark.trim().takeIf { it.isNotEmpty() }
          )
        },
        enabled = !isSaving,
        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
        modifier = Modifier.testTag("save_listing_changes_button")
      ) {
        Text("Save & Submit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !isSaving) {
        Text("Cancel", color = NavyTextSecondary)
      }
    }
  )
}

@Composable
fun LandlordEnquiriesDialog(
  currentLanguage: AppLanguage,
  enquiriesState: AsyncResourceState<List<EnquiryItem>>,
  isActionLoading: Boolean,
  onUpdateStatus: (enquiryId: String, status: EnquiryStatus) -> Unit,
  onDismiss: () -> Unit
) {
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
                "Dwello Homes coordinates tenant inspections directly. Update enquiry statuses below as inspections progress."
              else
                "Dwello Homes dey coordinate all inspection direct. You fit update enquiry status below.",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        when (enquiriesState) {
          is AsyncResourceState.Idle,
          is AsyncResourceState.Loading -> {
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(color = SkyBluePrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading enquiries...", color = NavyTextSecondary)
              }
            }
          }

          is AsyncResourceState.Error -> {
            item {
              Text(
                text = enquiriesState.message,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B))
              )
            }
          }

          is AsyncResourceState.Success -> {
            val enquiries = enquiriesState.data
            if (enquiries.isEmpty()) {
              item {
                Text(
                  text = "No enquiries received yet.",
                  style = MaterialTheme.typography.bodyMedium.copy(color = NavyTextSecondary)
                )
              }
            } else {
              items(enquiries, key = { it.id }) { enq ->
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
                        ),
                        modifier = Modifier.weight(1f)
                      )
                      if (enq.propertyPrice.isNotBlank()) {
                        Text(
                          text = enq.propertyPrice,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                          )
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Tenant: ${enq.tenantName}",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepNavyText
                      )
                    )
                    Text(
                      text = "Preferred Date: ${enq.preferredInspectionDate}",
                      style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                    )
                    if (enq.message.isNotBlank()) {
                      Text(
                        text = "\"${enq.message}\"",
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText)
                      )
                    }
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      if (enq.status != EnquiryStatus.CONTACTED) {
                        OutlinedButton(
                          onClick = { onUpdateStatus(enq.id, EnquiryStatus.CONTACTED) },
                          enabled = !isActionLoading,
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          border = BorderStroke(1.dp, SkyBluePrimary)
                        ) {
                          Text("Mark Contacted", fontSize = 11.sp, color = SkyBluePrimary)
                        }
                      }
                      if (enq.status != EnquiryStatus.CLOSED) {
                        OutlinedButton(
                          onClick = { onUpdateStatus(enq.id, EnquiryStatus.CLOSED) },
                          enabled = !isActionLoading,
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          border = BorderStroke(1.dp, NavyTextSecondary)
                        ) {
                          Text("Close", fontSize = 11.sp, color = NavyTextSecondary)
                        }
                      }
                    }
                  }
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
