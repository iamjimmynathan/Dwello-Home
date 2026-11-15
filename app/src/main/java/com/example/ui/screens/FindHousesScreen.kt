package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.AuthenticatedUser
import com.example.data.DwelloStrings
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
import com.example.data.SamplePropertyData
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
import com.example.ui.theme.WarmAmberAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindHousesScreen(
  currentLanguage: AppLanguage,
  initialSearchQuery: String?,
  propertiesState: AsyncResourceState<List<PropertyItem>>,
  favouriteIds: Set<String>,
  favouriteActionIds: Set<String>,
  selectedPropertyDetailState: AsyncResourceState<PropertyItem>?,
  currentUser: AuthenticatedUser?,
  enquirySubmitting: Boolean,
  enquirySubmitError: String?,
  enquirySubmittedSuccess: Boolean,
  onLanguageToggle: () -> Unit,
  onRetryLoadProperties: () -> Unit,
  onToggleFavourite: (String) -> Unit,
  onOpenPropertyDetails: (PropertyItem) -> Unit,
  onClosePropertyDetails: () -> Unit,
  onSubmitEnquiry: (propertyId: String, message: String?, preferredInspectionDate: String?) -> Unit,
  onResetEnquiryState: () -> Unit,
  onOpenMessageWithLandlord: (PropertyItem) -> Unit,
  modifier: Modifier = Modifier
) {
  // Search & Filter State
  var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery ?: "") }
  var selectedAreaQuickFilter by remember { mutableStateOf<String?>(null) }
  var showFavouritesOnly by remember { mutableStateOf(false) }
  var selectedPropertyType by remember { mutableStateOf("All") }
  var selectedRentalPeriod by remember { mutableStateOf<RentalPeriod?>(null) }
  var minRentInput by remember { mutableStateOf("") }
  var maxRentInput by remember { mutableStateOf("") }
  var showFilterSheet by remember { mutableStateOf(false) }

  // Details & Enquiry View State
  var lastRequestedDetailSummary by remember { mutableStateOf<PropertyItem?>(null) }
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

  // Public search ONLY shows APPROVED properties returned from GET /api/properties
  val approvedProperties = when (propertiesState) {
    is AsyncResourceState.Success -> propertiesState.data.filter {
      it.approvalStatus == PropertyApprovalStatus.APPROVED
    }
    else -> emptyList()
  }

  val filteredProperties = approvedProperties.filter { item ->
    val matchesSearch = searchQuery.isBlank() ||
      item.area.contains(searchQuery, ignoreCase = true) ||
      item.titleEn.contains(searchQuery, ignoreCase = true) ||
      item.titlePidgin.contains(searchQuery, ignoreCase = true) ||
      item.neighborhoodNote.contains(searchQuery, ignoreCase = true) ||
      item.categoryEn.contains(searchQuery, ignoreCase = true)

    val matchesAreaChip = selectedAreaQuickFilter == null || item.area.equals(selectedAreaQuickFilter, ignoreCase = true)
    val matchesFav = !showFavouritesOnly || favouriteIds.contains(item.id)
    val matchesType = selectedPropertyType == "All" || item.categoryEn.equals(selectedPropertyType, ignoreCase = true)
    val matchesPeriod = selectedRentalPeriod == null || item.rentalPeriod == selectedRentalPeriod
    val minRent = minRentInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val maxRent = maxRentInput.filter { it.isDigit() }.toLongOrNull() ?: Long.MAX_VALUE
    val matchesBudget = item.priceNaira in minRent..maxRent

    matchesSearch && matchesAreaChip && matchesFav && matchesType && matchesPeriod && matchesBudget
  }

  val hasActiveFilters = selectedPropertyType != "All" ||
    selectedRentalPeriod != null ||
    minRentInput.isNotBlank() ||
    maxRentInput.isNotBlank() ||
    selectedAreaQuickFilter != null ||
    showFavouritesOnly

  fun resetAllFilters() {
    selectedAreaQuickFilter = null
    showFavouritesOnly = false
    selectedPropertyType = "All"
    selectedRentalPeriod = null
    minRentInput = ""
    maxRentInput = ""
    searchQuery = ""
  }

  if (selectedPropertyDetailState != null) {
    BackHandler {
      onClosePropertyDetails()
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

            Text(
              text = DwelloStrings.findScreenTitle(currentLanguage),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText,
                lineHeight = 30.sp
              ),
              modifier = Modifier.testTag("find_screen_title")
            )

            Text(
              text = DwelloStrings.findScreenSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodyMedium.copy(
                color = NavyTextSecondary,
                fontWeight = FontWeight.Medium
              ),
              modifier = Modifier.testTag("find_screen_subtitle")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Search Box + Filter Sheet Button
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

        // 4. Saved Favourites + Benin City Neighbourhood Quick-Filter Chips
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
              if (selectedAreaQuickFilter != null || showFavouritesOnly) {
                TextButton(
                  onClick = {
                    selectedAreaQuickFilter = null
                    showFavouritesOnly = false
                  },
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
              item {
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showFavouritesOnly = !showFavouritesOnly }
                    .testTag("quick_favourites_chip"),
                  color = if (showFavouritesOnly) Color(0xFFE11D48) else Color.White,
                  shape = RoundedCornerShape(20.dp),
                  border = BorderStroke(
                    1.dp,
                    if (showFavouritesOnly) Color(0xFFE11D48) else SoftBlueBorder
                  )
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (showFavouritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                      contentDescription = null,
                      tint = if (showFavouritesOnly) Color.White else Color(0xFFE11D48),
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${DwelloStrings.savedFavouritesLabel(currentLanguage)} (${favouriteIds.size})",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (showFavouritesOnly) Color.White else DeepNavyText
                      )
                    )
                  }
                }
              }

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

        // 7. Property Cards List / Loading / Error / Empty
        when (propertiesState) {
          is AsyncResourceState.Idle,
          is AsyncResourceState.Loading -> {
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 16.dp)
                  .testTag("find_houses_loading"),
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
                  CircularProgressIndicator(color = SkyBluePrimary)
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = DwelloStrings.loadingPropertiesLabel(currentLanguage),
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
                  .padding(horizontal = 20.dp, vertical = 16.dp)
                  .testTag("find_houses_error_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFECACA))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = propertiesState.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = Color(0xFF991B1B),
                      fontWeight = FontWeight.Medium
                    )
                  )
                  Spacer(modifier = Modifier.height(14.dp))
                  Button(
                    onClick = onRetryLoadProperties,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("find_houses_retry_button")
                  ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DwelloStrings.retryLabel(currentLanguage), fontWeight = FontWeight.Bold)
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
                      text = if (showFavouritesOnly) {
                        if (currentLanguage == AppLanguage.ENGLISH)
                          "No saved properties yet."
                        else
                          "You never save any house yet."
                      } else {
                        if (currentLanguage == AppLanguage.ENGLISH)
                          "No approved properties found matching your search."
                        else
                          "House never dey for this area or filter yet."
                      },
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
              items(filteredProperties, key = { it.id }) { property ->
                DiscoveryPropertyCard(
                  property = property,
                  currentLanguage = currentLanguage,
                  isFavourite = favouriteIds.contains(property.id),
                  isFavouriteLoading = favouriteActionIds.contains(property.id),
                  onToggleFavourite = { onToggleFavourite(property.id) },
                  onViewDetails = {
                    lastRequestedDetailSummary = property
                    onOpenPropertyDetails(property)
                  },
                  onSendEnquiry = {
                    onResetEnquiryState()
                    enquiryProperty = property
                  },
                  modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
              }
            }
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

    // Property Details Full-Screen Overlay (GET /api/properties/{id})
    selectedPropertyDetailState?.let { detailState ->
      PropertyDetailsOverlay(
        detailState = detailState,
        currentLanguage = currentLanguage,
        favouriteIds = favouriteIds,
        favouriteActionIds = favouriteActionIds,
        onToggleFavourite = onToggleFavourite,
        onBack = onClosePropertyDetails,
        onRetry = {
          lastRequestedDetailSummary?.let { onOpenPropertyDetails(it) }
        },
        onSendEnquiry = { prop ->
          onResetEnquiryState()
          enquiryProperty = prop
        }
      )
    }

    // Enquiry Submission Dialog (POST /api/enquiries)
    enquiryProperty?.let { property ->
      EnquiryFormDialog(
        property = property,
        currentLanguage = currentLanguage,
        currentUser = currentUser,
        isSubmitting = enquirySubmitting,
        submitError = enquirySubmitError,
        isSubmitted = enquirySubmittedSuccess,
        onSubmit = { date, message ->
          onSubmitEnquiry(property.id, message, date)
        },
        onDismiss = {
          onResetEnquiryState()
          enquiryProperty = null
        },
        onViewEnquiries = {
          onResetEnquiryState()
          enquiryProperty = null
          onOpenMessageWithLandlord(property)
        }
      )
    }
  }
}

@Composable
fun DiscoveryPropertyCard(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  isFavourite: Boolean,
  isFavouriteLoading: Boolean,
  onToggleFavourite: () -> Unit,
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

        // Verified Listing Pill
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
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = DwelloStrings.verifiedListingLabel(currentLanguage),
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

        // Favourite Button
        Surface(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.BottomEnd),
          color = Color.White.copy(alpha = 0.94f),
          shape = CircleShape,
          shadowElevation = 2.dp
        ) {
          IconButton(
            onClick = onToggleFavourite,
            enabled = !isFavouriteLoading,
            modifier = Modifier
              .size(44.dp)
              .testTag("favourite_toggle_btn_${property.id}")
          ) {
            if (isFavouriteLoading) {
              CircularProgressIndicator(
                color = SkyBluePrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
              )
            } else {
              Icon(
                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavourite) "Remove favourite" else "Add favourite",
                tint = if (isFavourite) Color(0xFFE11D48) else DeepNavyText,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      // 2. Card Content
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
          text = "${property.neighborhoodNote}, Benin City",
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            fontWeight = FontWeight.Medium
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
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

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onViewDetails,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
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

          Button(
            onClick = onSendEnquiry,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
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

@Composable
fun PropertyDetailsOverlay(
  detailState: AsyncResourceState<PropertyItem>,
  currentLanguage: AppLanguage,
  favouriteIds: Set<String>,
  favouriteActionIds: Set<String>,
  onToggleFavourite: (String) -> Unit,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onSendEnquiry: (PropertyItem) -> Unit
) {
  when (detailState) {
    is AsyncResourceState.Idle,
    is AsyncResourceState.Loading -> {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(MainBackgroundWhite)
          .testTag("property_details_loading")
      ) {
        Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("details_back_button")) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepNavyText)
            }
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Property Details" else "House Details",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
            )
          }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = SkyBluePrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Loading property details...", color = NavyTextSecondary)
          }
        }
      }
    }

    is AsyncResourceState.Error -> {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(MainBackgroundWhite)
      ) {
        Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("details_back_button")) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepNavyText)
            }
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Property Details" else "House Details",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
            )
          }
        }
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = detailState.message,
              style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF991B1B), fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onRetry,
              colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
              modifier = Modifier.testTag("property_details_retry_btn")
            ) {
              Text(DwelloStrings.retryLabel(currentLanguage))
            }
          }
        }
      }
    }

    is AsyncResourceState.Success -> {
      val property = detailState.data
      PropertyDetailsView(
        property = property,
        currentLanguage = currentLanguage,
        isFavourite = favouriteIds.contains(property.id),
        isFavouriteLoading = favouriteActionIds.contains(property.id),
        onToggleFavourite = { onToggleFavourite(property.id) },
        onBack = onBack,
        onSendEnquiry = { onSendEnquiry(property) }
      )
    }
  }
}

@Composable
fun PropertyDetailsView(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  isFavourite: Boolean,
  isFavouriteLoading: Boolean,
  onToggleFavourite: () -> Unit,
  onBack: () -> Unit,
  onSendEnquiry: () -> Unit
) {
  var selectedPhotoIndex by remember(property.id) { mutableIntStateOf(0) }
  val remoteUrls = property.photoUris
  val fallbackResList = property.imageResList.ifEmpty { listOf(property.imageResId) }
  val totalPhotos = if (remoteUrls.isNotEmpty()) remoteUrls.size else fallbackResList.size

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
      // 1. Top Bar with Back Button & Favourite Action
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

            IconButton(
              onClick = onToggleFavourite,
              enabled = !isFavouriteLoading,
              modifier = Modifier.testTag("details_favourite_btn_${property.id}")
            ) {
              if (isFavouriteLoading) {
                CircularProgressIndicator(
                  color = SkyBluePrimary,
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(20.dp)
                )
              } else {
                Icon(
                  imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = if (isFavourite) "Remove saved property" else "Save property",
                  tint = if (isFavourite) Color(0xFFE11D48) else DeepNavyText
                )
              }
            }

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
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
          ) {
            if (remoteUrls.isNotEmpty()) {
              AsyncImage(
                model = remoteUrls[selectedPhotoIndex.coerceIn(remoteUrls.indices)],
                contentDescription = property.titleEn,
                placeholder = painterResource(id = property.imageResId),
                error = painterResource(id = property.imageResId),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Image(
                painter = painterResource(id = fallbackResList[selectedPhotoIndex.coerceIn(fallbackResList.indices)]),
                contentDescription = property.titleEn,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }

            Surface(
              modifier = Modifier
                .padding(14.dp)
                .align(Alignment.TopStart),
              color = Color(0xCC263746),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = DwelloStrings.verifiedListingLabel(currentLanguage),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              )
            }

            Surface(
              modifier = Modifier
                .padding(14.dp)
                .align(Alignment.BottomEnd),
              color = Color(0xCC000000),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "${selectedPhotoIndex + 1} / $totalPhotos Photos",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          if (totalPhotos > 1) {
            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .background(VeryLightBlueBackground)
                .padding(vertical = 10.dp, horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items((0 until totalPhotos).toList()) { index ->
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
                  if (remoteUrls.isNotEmpty()) {
                    AsyncImage(
                      model = remoteUrls[index],
                      contentDescription = "Thumbnail $index",
                      placeholder = painterResource(id = property.imageResId),
                      error = painterResource(id = property.imageResId),
                      modifier = Modifier.fillMaxSize(),
                      contentScale = ContentScale.Crop
                    )
                  } else {
                    Image(
                      painter = painterResource(id = fallbackResList[index]),
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
      }

      // 3. Property Information Overview
      item {
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

          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) property.titleEn else property.titlePidgin,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DeepNavyText,
              lineHeight = 28.sp
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

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

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
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
                    "Dwello Homes coordinates inspection between you and the property owner while keeping private phone numbers protected."
                  else
                    "Dwello Homes dey coordinate house inspection with the house owner and protect private phone numbers.",
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

@Composable
fun EnquiryFormDialog(
  property: PropertyItem,
  currentLanguage: AppLanguage,
  currentUser: AuthenticatedUser?,
  isSubmitting: Boolean,
  submitError: String?,
  isSubmitted: Boolean,
  onSubmit: (preferredDate: String, message: String) -> Unit,
  onDismiss: () -> Unit,
  onViewEnquiries: () -> Unit
) {
  var preferredDate by remember { mutableStateOf("This Saturday 11:00 AM") }
  var additionalMessage by remember { mutableStateOf("") }

  val datePresets = listOf(
    "This Saturday 11:00 AM",
    "This Sunday 2:00 PM",
    "Weekday Morning (10:00 AM)",
    "Weekday Evening (4:30 PM)"
  )

  if (!isSubmitted) {
    AlertDialog(
      onDismissRequest = { if (!isSubmitting) onDismiss() },
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
                  text = "${property.area}, Benin City • ${property.priceFormatted}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          if (currentUser != null) {
            item {
              Surface(
                color = SoftBlueAccent,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SoftBlueBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    text = "Submitted under your verified account:",
                    style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                  )
                  Text(
                    text = "${currentUser.fullName} (${currentUser.phone})",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                }
              }
            }
          }

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
                    .clickable(enabled = !isSubmitting) { preferredDate = preset }
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

          item {
            OutlinedTextField(
              value = preferredDate,
              onValueChange = { preferredDate = it },
              label = { Text("Preferred Date / Time") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("enquiry_date_input"),
              singleLine = true,
              enabled = !isSubmitting,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SkyBluePrimary,
                unfocusedBorderColor = SoftBlueBorder
              )
            )
          }

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
              enabled = !isSubmitting,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SkyBluePrimary,
                unfocusedBorderColor = SoftBlueBorder
              )
            )
          }

          if (!submitError.isNullOrBlank()) {
            item {
              Text(
                text = submitError,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.testTag("enquiry_submit_error")
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { onSubmit(preferredDate.trim(), additionalMessage.trim()) },
          enabled = !isSubmitting,
          modifier = Modifier.testTag("submit_enquiry_form_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          shape = RoundedCornerShape(10.dp)
        ) {
          if (isSubmitting) {
            CircularProgressIndicator(
              color = Color.White,
              strokeWidth = 2.dp,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(
            text = "SUBMIT ENQUIRY",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss, enabled = !isSubmitting) {
          Text("Cancel", color = NavyTextSecondary)
        }
      }
    )
  } else {
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
            text = "Enquiry Submitted",
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
            text = "Thank you! Your enquiry has been sent to Dwello Homes.",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Dwello Homes has recorded your inspection request for ${property.titleEn} in ${property.area}. You can track status updates in the Enquiries tab."
            else
              "Dwello Homes don save your inspection request for ${property.titleEn} for ${property.area}. You fit track update inside Enquiries tab.",
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
                text = "Preferred Inspection: $preferredDate",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
              Text(
                text = "Status updates appear in your Enquiries tab.",
                style = MaterialTheme.typography.labelSmall.copy(color = SkyBlueDark)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = onViewEnquiries,
          colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("enquiry_done_view_tab_btn")
        ) {
          Text("View Enquiries", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Done", color = NavyTextSecondary, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
