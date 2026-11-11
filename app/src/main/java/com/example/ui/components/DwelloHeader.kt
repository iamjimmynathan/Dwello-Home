package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.DwelloStrings
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.WarmGoldAccent

@Composable
fun DwelloHeader(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  selectedArea: String? = null,
  onLocationClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = ForestGreenPrimary,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Simple House Logo + App Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("app_brand_logo_row")
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White)
              .border(1.5.dp, WarmGoldAccent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_dwello_logo),
              contentDescription = "DWELLO HOMES Logo",
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "DWELLO HOMES",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = Color.White
              )
            )
            Text(
              text = "Benin City Property Rental",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.82f)
              )
            )
          }
        }

        // Language Switch: English / Pidgin Toggle
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onLanguageToggle)
            .testTag("language_toggle_button"),
          color = ForestGreenDark,
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Translate,
              contentDescription = "Change Language",
              tint = WarmGoldAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Pidgin" else "English",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }
      }

      // Location bar: Benin City, Edo State
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .clickable(enabled = onLocationClick != null) { onLocationClick?.invoke() }
          .testTag("location_indicator_bar"),
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = WarmGoldAccent,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = selectedArea?.let { "$it • Benin City, Edo State" }
              ?: DwelloStrings.initialLocation(currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    }
  }
}
