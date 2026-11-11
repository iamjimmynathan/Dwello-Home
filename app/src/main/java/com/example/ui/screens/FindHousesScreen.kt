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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightSagePill
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.SoftCreamBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmGoldAccent

@Composable
fun FindHousesScreen(
  currentLanguage: AppLanguage,
  initialSearchQuery: String?,
  onLanguageToggle: () -> Unit,
  onOpenMessageWithLandlord: (PropertyItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery ?: "") }
  var selectedCategory by remember { mutableStateOf("All") }
  var selectedPropertyForDetails by remember { mutableStateOf<PropertyItem?>(null) }

  val categories = listOf("All", "Self-Con", "2-Bedroom Flat", "3-Bedroom Flat", "Single Room", "Commercial Shop")

  val filteredProperties = SamplePropertyData.sampleProperties.filter { item ->
    val matchesSearch = searchQuery.isBlank() ||
      item.area.contains(searchQuery, ignoreCase = true) ||
      item.titleEn.contains(searchQuery, ignoreCase = true) ||
      item.neighborhoodNote.contains(searchQuery, ignoreCase = true)

    val matchesCategory = selectedCategory == "All" || item.categoryEn == selectedCategory

    matchesSearch && matchesCategory
  }

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
        .testTag("find_houses_list"),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      // Screen Title & Search bar
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Find Houses in Benin City" else "Find Houses for Benin City",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = ForestGreenDark
            )
          )
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Direct connection to Benin landlords and verified caretakers"
            else
              "Direct contact with Benin landlords and trusted caretakers",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("find_screen_search_input"),
            placeholder = {
              Text(
                text = DwelloStrings.searchPlaceholder(currentLanguage),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark.copy(alpha = 0.7f))
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ForestGreenPrimary
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondaryDark)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = ForestGreenPrimary,
              unfocusedBorderColor = SoftCreamBorder,
              focusedTextColor = TextPrimaryDark,
              unfocusedTextColor = TextPrimaryDark
            )
          )
        }
      }

      // Categories filter horizontal bar
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(categories) { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable { selectedCategory = cat }
                .testTag("category_filter_$cat"),
              color = if (isSelected) ForestGreenPrimary else Color.White,
              shape = RoundedCornerShape(18.dp),
              border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else SoftCreamBorder)
            ) {
              Text(
                text = cat,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else TextPrimaryDark
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Count of houses
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${filteredProperties.size} ${if (currentLanguage == AppLanguage.ENGLISH) "houses found" else "houses dey"}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = ForestGreenDark
            )
          )
          Text(
            text = "Edo State • Nigeria",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
          )
        }
      }

      // Properties List
      if (filteredProperties.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH)
                  "No houses found matching this area or type."
                else
                  "House never dey for this area or type yet.",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = TextSecondaryDark,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }
        }
      } else {
        items(filteredProperties) { property ->
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
      }
    }
  }

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
