package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.EnquiryRepository
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindHousesScreen(
  currentLanguage: AppLanguage,
  initialSearchQuery: String?,
  onLanguageToggle: () -> Unit,
  onOpenMessageWithLandlord: (PropertyItem) -> Unit,
  modifier: Modifier = Modifier
) {
  // Search & Filter State
  var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery ?: "") }
  var selectedAreaQuickFilter by remember { mutableStateOf<String?>(null) }
  var selectedPropertyType by remember { mutableStateOf("All") }
  var selectedRentalPeriod by remember { mutableStateOf<RentalPeriod?>(null) } // null = All
  var minRentInput by remember { mutableStateOf("") }
  var maxRentInput by remember { mutableStateOf("") }
  var showFilterSheet by remember { mutableStateOf(false) }

  // Details & Enquiry View State
  var viewingPropertyDetails by remember { mutableStateOf<PropertyItem?>(null) }
  var enquiryProperty by remember { mutableStateOf<PropertyItem?>(null) }

  val beninQuickAreas = listOf(
    "GRA",
    "Sapele Road",
    "Airport Road",
    "Ugbowo",
    "Ekosodin",
    "Aduwawa",
    "Ikpoba Hill",
    "New Benin",
    "Ekenwan",
    "Ogida"
  )

  // Critical Business Rule: PUBLIC SEARCH ONLY SHOWS APPROVED PROPERTIES!
  // Pending Approval, Rejected, and Unavailable properties are strictly filtered out.
  val approvedProperties = SamplePropertyData.getApprovedPublicProperties()

  val filteredProperties = approvedProperties.filter { item ->
    // 1. Text Search matching title, area, neighbourhood, or description
    val matchesSearch = searchQuery.isBlank() ||
      item.area.contains(searchQuery, ignoreCase = true) ||
      item.titleEn.contains(searchQuery, ignoreCase = true) ||
      item.titlePidgin.contains(searchQuery, ignoreCase = true) ||
      item.neighborhoodNote.contains(searchQuery, ignoreCase = true) ||
      item.categoryEn.contains(searchQuery, ignoreCase = true)

    // 2. Quick Area Chip Filter
    val matchesAreaChip = selectedAreaQuickFilter == null || item.area.equals(selectedAreaQuickFilter, ignoreCase = true)

    // 3. Property Type Filter
    val matchesType = selectedPropertyType == "All" || item.categoryEn.equals(selectedPropertyType, ignoreCase = true)

    // 4. Rental Period Filter
    val matchesPeriod = selectedRentalPeriod == null || item.rentalPeriod == selectedRentalPeriod

    // 5. Min / Max Rent Filter
    val minRent = minRentInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val maxRent = maxRentInput.filter { it.isDigit() }.toLongOrNull() ?: Long.MAX_VALUE
    val matchesBudget = item.priceNaira in minRent..maxRent

    matchesSearch && matchesAreaChip && matchesType && matchesPeriod && matchesBudget
  }

  val hasActiveFilters = selectedPropertyType != "All" ||
    selectedRentalPeriod != null ||
    minRentInput.isNotBlank() ||
    maxRentInput.isNotBlank() ||
    selectedAreaQuickFilter != null

  fun resetAllFilters() {
    selectedAreaQuickFilter = null
    selectedPropertyType = "All"
    selectedRentalPeriod = null
    minRentInput = ""
    maxRentInput = ""
    searchQuery = ""
  }

  // Handle back press when viewing details or enquiry
  if (viewingPropertyDetails != null) {
    BackHandler {
      viewingPropertyDetails = null
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Dwello Header
      DwelloHeader(
        currentLanguage = currentLanguage,
        onLanguageToggle = onLanguageToggle
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("find_houses_discovery_list"),
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        // 2. Screen Header & Subtitle
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(VeryLightBlueBackground)
              .padding(horizontal = 20.dp, vertical = 18.dp)
          ) {
            // Default location indicator badge
            Surface(
              color = Color.White,
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, SoftBlueBorder),
              modifier = Modifier.testTag("location_indicator_badge")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = DwelloStrings.defaultLocation(currentLanguage),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Title: "Find your next home"
            Text(
              text = DwelloStrings.findScreenTitle(currentLanguage),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText,
                lineHeight = 30.sp
              ),
              modifier = Modifier.testTag("find_screen_title")
            )

            // Subtitle: "Let's find a place you'll love."
            Text(
              text = DwelloStrings.findScreenSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodyMedium.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.Medium
              ),
              modifier = Modifier.testTag("find_screen_subtitle")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Search Box: "Search by area or neighbourhood"
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                  .weight(1f)
                  .testTag("search_area_input"),
                placeholder = {
                  Text(
                    text = DwelloStrings.searchAreaPlaceholder(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = NavyTextSecondary.copy(alpha = 0.7f)
                    )
                  )
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(22.dp)
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
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = SkyBluePrimary,
                  unfocusedBorderColor = SoftBlueBorder,
                  focusedTextColor = DeepNavyText,
                  unfocusedTextColor = DeepNavyText
                )
              )

              Spacer(modifier = Modifier.width(10.dp))

              // Filter Sheet Button with Badge if filters are active
              Surface(
                modifier = Modifier
                  .size(54.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { showFilterSheet = true }
                  .testTag("open_filter_button"),
                color = if (hasActiveFilters) SkyBluePrimary else Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, if (hasActiveFilters) SkyBluePrimary else SoftBlueBorder),
                shadowElevation = 2.dp
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filters",
                    tint = if (hasActiveFilters) Color.White else SkyBluePrimary,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }
        }

        // 4. Benin City Neighbourhood Quick-Filter Chips
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH) "Benin City Neighbourhoods" else "Benin City Areas",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              if (selectedAreaQuickFilter != null) {
                TextButton(
                  onClick = { selectedAreaQuickFilter = null },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Clear area",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = SkyBluePrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
              contentPadding = PaddingValues(horizontal = 20.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(beninQuickAreas) { area ->
                val isSelected = selectedAreaQuickFilter == area
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                      selectedAreaQuickFilter = if (isSelected) null else area
                    }
                    .testTag("quick_area_chip_$area"),
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
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = area,
                      style = MaterialTheme.typography.labelMedium.copy(
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

        // 5. Active Filters Indicator & Reset
        if (hasActiveFilters) {
          item {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
              color = SoftBlueAccent,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (currentLanguage == AppLanguage.ENGLISH) "Filters applied" else "Filter dey active",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                }

                TextButton(
                  onClick = { resetAllFilters() },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                  modifier = Modifier.testTag("reset_active_filters_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = SkyBlueDark,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = DwelloStrings.resetFilters(currentLanguage),
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

        // 6. Results Count Header
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${filteredProperties.size} ${if (currentLanguage == AppLanguage.ENGLISH) "verified properties" else "verified houses"}",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Surface(
              color = VeryLightBlueBackground,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "Benin City • Intermediary Service",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SkyBlueDark,
                  fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }

        // 7. Property Cards List
        if (filteredProperties.isEmpty()) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .testTag("no_results_card"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "No approved properties found matching your search."
                  else
                    "House never dey for this area or filter yet.",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "Try resetting your budget or neighbourhood filters to see more homes in Benin City."
                  else
                    "Change your budget or area filter make you fit see other houses for Benin City.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = NavyTextSecondary
                  )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { resetAllFilters() },
                  colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text(
                    text = DwelloStrings.resetFilters(currentLanguage),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
            }
          }
        } else {
          items(filteredProperties) { property ->
            DiscoveryPropertyCard(
              property = property,
              currentLanguage = currentLanguage,
              onViewDetails = { viewingPropertyDetails = property },
              onSendEnquiry = { enquiryProperty = property },
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    // Modal Filter BottomSheet
    if (showFilterSheet) {
      ModalBottomSheet(
        onDismissRequest = { showFilterSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
      ) {
        FilterSheetContent(
          currentLanguage = currentLanguage,
          selectedPropertyType = selectedPropertyType,
          onPropertyTypeSelected = { selectedPropertyType = it },
          selectedRentalPeriod = selectedRentalPeriod,
          onRentalPeriodSelected = { selectedRentalPeriod = it },
          minRentInput = minRentInput,
          onMinRentChanged = { minRentInput = it },
          maxRentInput = maxRentInput,
          onMaxRentChanged = { maxRentInput = it },
          onApplyFilters = { showFilterSheet = false },
          onResetFilters = {
            resetAllFilters()
            showFilterSheet = false
          }
        )
      }
    }

    // Property Details Full-Screen Overlay
    viewingPropertyDetails?.let { property ->
      PropertyDetailsView(
        property = property,
        currentLanguage = currentLanguage,
        onBack = { viewingPropertyDetails = null },
        onSendEnquiry = {
          enquiryProperty = property
        }
      )
    }

    // Enquiry Submission Dialog
    enquiryProperty?.let { property ->
      EnquiryFormDialog(
        property = property,
        currentLanguage = currentLanguage,
        onDismiss = { enquiryProperty = null }
      )
    }
  }
}

/**
 * Property Discovery Card complying with all required specifications:
 * - Property photograph
 * - Property title
 * - Neighbourhood
 * - Rental price in Nigerian Naira (₦)
 * - Rental period (Yearly / Monthly)
 * - Number of bedrooms & bathrooms
 * - Property type chip
 * - VIEW DETAILS button
 * - Clearly marked as demonstration listing
 */
@Composable
fun DiscoveryPropertyCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onViewDetails: () -> Unit,
  onSendEnquiry: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onViewDetails)
      .testTag("discovery_card_${property.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, CardBorderSubtle)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 1. Property Photo Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
      ) {
        Image(
          painter = painterResource(id = property.imageResId),
          contentDescription = property.titleEn,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Demonstration Data Pill (Clearly identifies demonstration listing)
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
              text = if (currentLanguage == AppLanguage.ENGLISH) "Demo Listing" else "Demo House",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // Property Type Tag
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

      // 2. Card Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Location & Price Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Neighbourhood badge
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

          // Price in Naira with period
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = property.priceFormatted,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SkyBluePrimary,
                fontSize = 18.sp
              )
            )
            Text(
              text = if (property.rentalPeriod == RentalPeriod.MONTHLY)
                DwelloStrings.perMonth(currentLanguage)
              else
                DwelloStrings.perYear(currentLanguage),
              style = MaterialTheme.typography.labelSmall.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Property Title
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText,
            fontSize = 16.sp
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Neighbourhood note
        Text(
          text = "📍 ${property.neighborhoodNote}, Benin City",
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            fontWeight = FontWeight.Medium
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Specs: Bedrooms, Bathrooms, Water, Light
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Bedrooms
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bed,
              contentDescription = null,
              tint = DeepNavyText,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${property.bedrooms} Bed${if (property.bedrooms > 1) "s" else ""}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = DeepNavyText,
                fontWeight = FontWeight.SemiBold
              )
            )
          }

          // Bathrooms
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bathtub,
              contentDescription = null,
              tint = DeepNavyText,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${property.bathrooms} Bath",
              style = MaterialTheme.typography.labelSmall.copy(
                color = DeepNavyText,
                fontWeight = FontWeight.SemiBold
              )
            )
          }

          if (property.hasBorehole) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Water",
                style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
              )
            }
          }

          if (property.hasPrepaidMeter) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = WarmAmberAccent,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Prepaid",
                style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row: VIEW DETAILS and SEND ENQUIRY
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // VIEW DETAILS button
          OutlinedButton(
            onClick = onViewDetails,
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("view_details_btn_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, SkyBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White,
              contentColor = SkyBluePrimary
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
              text = DwelloStrings.viewDetails(currentLanguage),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
              )
            )
          }

          // SEND ENQUIRY button
          Button(
            onClick = onSendEnquiry,
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("enquire_card_btn_${property.id}"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SkyBluePrimary,
              contentColor = Color.White
            )
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
              tint = Color.White
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

/**
 * Filter Sheet Content allowing tenants to filter by:
 * - Property Type
 * - Rent Budget (Min & Max in ₦)
 * - Rental Period (Yearly / Monthly)
 * - Apply & Reset buttons
 */
@Composable
fun FilterSheetContent(
  currentLanguage: AppLanguage,
  selectedPropertyType: String,
  onPropertyTypeSelected: (String) -> Unit,
  selectedRentalPeriod: RentalPeriod?,
  onRentalPeriodSelected: (RentalPeriod?) -> Unit,
  minRentInput: String,
  onMinRentChanged: (String) -> Unit,
  maxRentInput: String,
  onMaxRentChanged: (String) -> Unit,
  onApplyFilters: () -> Unit,
  onResetFilters: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    // Sheet Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = DwelloStrings.filterDialogTitle(currentLanguage),
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = DeepNavyText
        )
      )

      TextButton(onClick = onResetFilters) {
        Text(
          text = DwelloStrings.resetFilters(currentLanguage),
          color = SkyBluePrimary,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 1. PROPERTY TYPE
    Text(
      text = DwelloStrings.propertyTypeLabel(currentLanguage),
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Bold,
        color = DeepNavyText
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    val allTypes = listOf("All") + SamplePropertyData.propertyTypesList
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(vertical = 4.dp)
    ) {
      items(allTypes) { type ->
        val isSelected = selectedPropertyType == type
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPropertyTypeSelected(type) }
            .testTag("filter_type_$type"),
          color = if (isSelected) SkyBluePrimary else VeryLightBlueBackground,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
        ) {
          Text(
            text = type,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else DeepNavyText
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 2. RENT BUDGET (₦)
    Text(
      text = DwelloStrings.rentBudgetLabel(currentLanguage),
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Bold,
        color = DeepNavyText
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedTextField(
        value = minRentInput,
        onValueChange = onMinRentChanged,
        label = { Text(DwelloStrings.minRentLabel(currentLanguage)) },
        placeholder = { Text("e.g. 100000") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .weight(1f)
          .testTag("filter_min_rent_input"),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = VeryLightBlueBackground,
          unfocusedContainerColor = VeryLightBlueBackground,
          focusedBorderColor = SkyBluePrimary,
          unfocusedBorderColor = SoftBlueBorder
        )
      )

      OutlinedTextField(
        value = maxRentInput,
        onValueChange = onMaxRentChanged,
        label = { Text(DwelloStrings.maxRentLabel(currentLanguage)) },
        placeholder = { Text("e.g. 800000") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .weight(1f)
          .testTag("filter_max_rent_input"),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = VeryLightBlueBackground,
          unfocusedContainerColor = VeryLightBlueBackground,
          focusedBorderColor = SkyBluePrimary,
          unfocusedBorderColor = SoftBlueBorder
        )
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 3. RENTAL PERIOD
    Text(
      text = DwelloStrings.rentalPeriodLabel(currentLanguage),
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Bold,
        color = DeepNavyText
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Option: All Periods
      val isAllPeriod = selectedRentalPeriod == null
      Surface(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onRentalPeriodSelected(null) }
          .testTag("filter_period_all"),
        color = if (isAllPeriod) SkyBluePrimary else VeryLightBlueBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isAllPeriod) SkyBluePrimary else SoftBlueBorder)
      ) {
        Box(
          modifier = Modifier.padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = DwelloStrings.periodAll(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (isAllPeriod) FontWeight.Bold else FontWeight.Medium,
              color = if (isAllPeriod) Color.White else DeepNavyText
            )
          )
        }
      }

      // Option: Yearly
      val isYearly = selectedRentalPeriod == RentalPeriod.YEARLY
      Surface(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onRentalPeriodSelected(RentalPeriod.YEARLY) }
          .testTag("filter_period_yearly"),
        color = if (isYearly) SkyBluePrimary else VeryLightBlueBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isYearly) SkyBluePrimary else SoftBlueBorder)
      ) {
        Box(
          modifier = Modifier.padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = DwelloStrings.periodYearly(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (isYearly) FontWeight.Bold else FontWeight.Medium,
              color = if (isYearly) Color.White else DeepNavyText
            )
          )
        }
      }

      // Option: Monthly
      val isMonthly = selectedRentalPeriod == RentalPeriod.MONTHLY
      Surface(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onRentalPeriodSelected(RentalPeriod.MONTHLY) }
          .testTag("filter_period_monthly"),
        color = if (isMonthly) SkyBluePrimary else VeryLightBlueBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isMonthly) SkyBluePrimary else SoftBlueBorder)
      ) {
        Box(
          modifier = Modifier.padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = DwelloStrings.periodMonthly(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Medium,
              color = if (isMonthly) Color.White else DeepNavyText
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(26.dp))

    // APPLY FILTER BUTTON
    Button(
      onClick = onApplyFilters,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .testTag("apply_filters_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = SkyBluePrimary,
        contentColor = Color.White
      )
    ) {
      Text(
        text = DwelloStrings.applyFilters(currentLanguage),
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.5.sp
        )
      )
    }

    Spacer(modifier = Modifier.height(18.dp))
  }
}

/**
 * Dedicated Property Details View meeting all specifications:
 * - Multiple property photographs with interactive thumbnail selector
 * - Property title, Location, Rental price & period
 * - Bedrooms & Bathrooms
 * - Property type
 * - Comprehensive description
 * - Available facilities
 * - Rental terms
 * - Availability status
 * - Prominent "SEND ENQUIRY TO DWELLO HOMES" button
 * - Zero landlord private phone exposure
 */
@Composable
fun PropertyDetailsView(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onBack: () -> Unit,
  onSendEnquiry: () -> Unit
) {
  var selectedPhotoIndex by remember { mutableIntStateOf(0) }
  val photos = property.imageResList.ifEmpty { listOf(property.imageResId) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("property_details_scroll"),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // 1. Top Bar with Back Button & Title
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.White,
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("details_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DeepNavyText
              )
            }

            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Property Details" else "House Details",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
              color = SoftBlueAccent,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "Dwello Verified",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = SkyBluePrimary
                )
              )
            }
          }
        }
      }

      // 2. Multiple Property Photographs Hero & Gallery Thumbnails
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Large Main Photo
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
          ) {
            Image(
              painter = painterResource(id = photos[selectedPhotoIndex.coerceIn(photos.indices)]),
              contentDescription = property.titleEn,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )

            // Demonstration Pill
            Surface(
              modifier = Modifier
                .padding(14.dp)
                .align(Alignment.TopStart),
              color = Color(0xCC263746),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = DwelloStrings.demoNotice(currentLanguage),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              )
            }

            // Photo Counter Pill
            Surface(
              modifier = Modifier
                .padding(14.dp)
                .align(Alignment.BottomEnd),
              color = Color(0xCC000000),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "${selectedPhotoIndex + 1} / ${photos.size} Photos",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          // Thumbnail Row
          if (photos.size > 1) {
            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .background(VeryLightBlueBackground)
                .padding(vertical = 10.dp, horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(photos.indices.toList()) { index ->
                val isSelected = selectedPhotoIndex == index
                Card(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedPhotoIndex = index }
                    .border(
                      BorderStroke(2.dp, if (isSelected) SkyBluePrimary else Color.Transparent),
                      RoundedCornerShape(8.dp)
                    ),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Image(
                    painter = painterResource(id = photos[index]),
                    contentDescription = "Thumbnail $index",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
              }
            }
          }
        }
      }

      // 3. Property Information Overview
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          // Property Type & Availability Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = SkyBluePrimary,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH) property.categoryEn else property.categoryPidgin,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }

            Surface(
              color = VeryLightBlueBackground,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = property.availabilityStatus,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = DeepNavyText,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Title
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DeepNavyText,
              lineHeight = 28.sp
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Location
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = SkyBluePrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${property.neighborhoodNote}, ${property.area}, Benin City, Edo State",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.SemiBold
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Price Tag Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Rental Price" else "House Rent",
                  style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                )
                Text(
                  text = property.priceFormatted,
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SkyBluePrimary
                  )
                )
              }

              Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SoftBlueBorder)
              ) {
                Text(
                  text = if (property.rentalPeriod == RentalPeriod.MONTHLY)
                    DwelloStrings.perMonth(currentLanguage)
                  else
                    DwelloStrings.perYear(currentLanguage),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Specifications Box
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Bedrooms
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, CardBorderSubtle)
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.Bed, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${property.bedrooms} Bed${if (property.bedrooms > 1) "s" else ""}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
              }
            }

            // Bathrooms
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, CardBorderSubtle)
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.Bathtub, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${property.bathrooms} Bath${if (property.bathrooms > 1) "s" else ""}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
              }
            }

            // Compound Type
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, CardBorderSubtle)
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = if (property.isGated) "Gated" else "Open",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Description Section
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Description" else "Description of House",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.descriptionEn else property.descriptionPidgin,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = DeepNavyText,
              lineHeight = 22.sp
            )
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Available Facilities
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Available Facilities" else "Things Wey Dey Inside House",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            property.facilities.forEach { facility ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = facility,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepNavyText,
                    fontWeight = FontWeight.Medium
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Rental Terms
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Rental Terms" else "Rental Conditions",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = VeryLightBlueBackground,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Text(
              text = property.rentalTerms,
              modifier = Modifier.padding(14.dp),
              style = MaterialTheme.typography.bodySmall.copy(
                color = DeepNavyText,
                lineHeight = 18.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Intermediary Notice (Protects landlord privacy)
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftBlueAccent),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Dwello Homes Intermediary Protection",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "Dwello Homes connects you directly with the property owner. To ensure your security, an accredited Dwello Homes coordinator will accompany you on your physical inspection."
                  else
                    "Dwello Homes dey connect you with the house owner. Our coordinator go follow you go check the house in person make everything safe.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = NavyTextSecondary,
                    lineHeight = 18.sp
                  )
                )
              }
            }
          }
        }
      }
    }

    // Sticky Bottom Bar with Prominent "SEND ENQUIRY TO DWELLO HOMES" Button
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth(),
      color = Color.White,
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, CardBorderSubtle)
    ) {
      Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Button(
          onClick = onSendEnquiry,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("send_enquiry_to_dwello_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SkyBluePrimary,
            contentColor = Color.White
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = DwelloStrings.sendEnquiryToDwello(currentLanguage),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            )
          )
        }
      }
    }
  }
}

/**
 * Enquiry Form Dialog meeting all specifications:
 * - Tenant Name (validated)
 * - Phone Number (validated)
 * - Property selected (pre-filled)
 * - Preferred inspection date
 * - Additional message
 * - SUBMIT ENQUIRY button
 * - Confirmation screen: "Thank you! Your enquiry has been prepared for Dwello Homes."
 */
@Composable
fun EnquiryFormDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit
) {
  var tenantName by remember { mutableStateOf("") }
  var tenantPhone by remember { mutableStateOf("") }
  var preferredDate by remember { mutableStateOf("This Saturday 11:00 AM") }
  var additionalMessage by remember { mutableStateOf("") }

  var nameError by remember { mutableStateOf(false) }
  var phoneError by remember { mutableStateOf(false) }
  var isSubmitted by remember { mutableStateOf(false) }

  val datePresets = listOf(
    "This Saturday 11:00 AM",
    "This Sunday 2:00 PM",
    "Weekday Morning (10:00 AM)",
    "Weekday Evening (4:30 PM)"
  )

  if (!isSubmitted) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SkyBluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Book Inspection Enquiry" else "Send House Enquiry",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
        }
      },
      text = {
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            // Selected Property Summary Card
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "Selected Property:",
                  style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                )
                Text(
                  text = property.titleEn,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Text(
                  text = "📍 ${property.area}, Benin City • ${property.priceFormatted}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          // 1. Tenant Name (Required)
          item {
            OutlinedTextField(
              value = tenantName,
              onValueChange = {
                tenantName = it
                if (it.isNotBlank()) nameError = false
              },
              label = { Text("Your Full Name *") },
              placeholder = { Text("e.g. Osaro Omoregie") },
              isError = nameError,
              supportingText = {
                if (nameError) {
                  Text("Please enter your name", color = MaterialTheme.colorScheme.error)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("enquiry_name_input"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SkyBluePrimary,
                unfocusedBorderColor = SoftBlueBorder
              )
            )
          }

          // 2. Phone Number (Required)
          item {
            OutlinedTextField(
              value = tenantPhone,
              onValueChange = {
                tenantPhone = it
                if (it.length >= 8) phoneError = false
              },
              label = { Text("Phone Number *") },
              placeholder = { Text("e.g. 0803 123 4567") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              isError = phoneError,
              supportingText = {
                if (phoneError) {
                  Text("Please enter a valid phone number", color = MaterialTheme.colorScheme.error)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("enquiry_phone_input"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SkyBluePrimary,
                unfocusedBorderColor = SoftBlueBorder
              )
            )
          }

          // 3. Preferred Inspection Date
          item {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Preferred Inspection Date / Time" else "When you wan check the house?",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              datePresets.forEach { preset ->
                val isSelected = preferredDate == preset
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { preferredDate = preset }
                    .testTag("preset_date_$preset"),
                  color = if (isSelected) SoftBlueAccent else VeryLightBlueBackground,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                      text = preset,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) DeepNavyText else NavyTextSecondary
                      )
                    )
                  }
                }
              }
            }
          }

          // 4. Additional Message (Optional)
          item {
            OutlinedTextField(
              value = additionalMessage,
              onValueChange = { additionalMessage = it },
              label = { Text("Additional Message (Optional)") },
              placeholder = { Text("e.g. Any special request or move-in timeline") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("enquiry_message_input"),
              maxLines = 3,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
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
            var valid = true
            if (tenantName.isBlank()) {
              nameError = true
              valid = false
            }
            if (tenantPhone.isBlank() || tenantPhone.length < 7) {
              phoneError = true
              valid = false
            }

            if (valid) {
              // Save to EnquiryRepository for local demonstration and Messages/Enquiries tab integration
              EnquiryRepository.addEnquiry(
                EnquiryItem(
                  id = "enq_${System.currentTimeMillis()}",
                  propertyId = property.id,
                  propertyTitle = property.titleEn,
                  propertyArea = property.area,
                  propertyPrice = property.priceFormatted,
                  tenantName = tenantName.trim(),
                  tenantPhone = tenantPhone.trim(),
                  preferredInspectionDate = preferredDate,
                  message = additionalMessage.trim()
                )
              )
              isSubmitted = true
            }
          },
          modifier = Modifier.testTag("submit_enquiry_form_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "SUBMIT ENQUIRY",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel", color = NavyTextSecondary)
        }
      }
    )
  } else {
    // Confirmation Screen (Exact wording specified in prompt)
    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Enquiry Prepared",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Thank you! Your enquiry has been prepared for Dwello Homes.",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Dwello Homes will review your requested inspection for ${property.titleEn} in ${property.area}. Our coordinator will contact you at $tenantPhone to confirm the landlord's availability."
            else
              "Dwello Homes go check your inspection request for ${property.titleEn} for ${property.area}. We go call you on $tenantPhone to confirm the time with the house owner.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = NavyTextSecondary,
              lineHeight = 20.sp
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            color = SoftBlueAccent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "📌 Preferred Inspection: $preferredDate",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Text(
                text = "Your enquiry is saved in the Enquiries tab.",
                style = MaterialTheme.typography.labelSmall.copy(color = SkyBlueDark)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
