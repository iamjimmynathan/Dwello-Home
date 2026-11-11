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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.ForestAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightSagePill
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.SoftCreamBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmGoldAccent

@Composable
fun ProfileScreen(
  currentLanguage: AppLanguage,
  onLanguageSelected: (AppLanguage) -> Unit,
  onLanguageToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

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
        .testTag("profile_scroll_view"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
      // User / App Profile Overview
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, SoftCreamBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ForestGreenPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "DH",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "DWELLO HOMES",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ForestGreenDark
                )
              )
              Text(
                text = "Benin City • Edo State",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
              )
              Text(
                text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant & Landlord Platform" else "Tenants and Landlords Hub",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = ForestGreenPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Language Switcher Section
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Choose Language" else "Select Language",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, SoftCreamBorder)
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            // English Option
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLanguageSelected(AppLanguage.ENGLISH) }
                .background(if (currentLanguage == AppLanguage.ENGLISH) LightSagePill else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "🇳🇬 English",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (currentLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimaryDark
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "(Standard English)",
                  style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
              }
              if (currentLanguage == AppLanguage.ENGLISH) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = ForestGreenPrimary
                )
              }
            }

            HorizontalDivider(color = SoftCreamBorder.copy(alpha = 0.5f))

            // Pidgin Option
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLanguageSelected(AppLanguage.PIDGIN) }
                .background(if (currentLanguage == AppLanguage.PIDGIN) LightSagePill else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "🇳🇬 Nigerian Pidgin",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (currentLanguage == AppLanguage.PIDGIN) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimaryDark
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "(Wetin everyone understand)",
                  style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
              }
              if (currentLanguage == AppLanguage.PIDGIN) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = ForestGreenPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Benin City Rental Safety Tips
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Safety & Rental Tips in Benin City" else "Safety Tips for House Renting",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, SoftCreamBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            TipRow(
              number = "1",
              title = if (currentLanguage == AppLanguage.ENGLISH) "Always Inspect Physically" else "Go check the house with your eyes",
              desc = if (currentLanguage == AppLanguage.ENGLISH)
                "Never send rent money until you visit the compound, check running water and meet the caretaker."
              else
                "No pay money transfer until you see the house, test the tap water and meet the caretaker face-to-face."
            )
            Spacer(modifier = Modifier.height(12.dp))
            TipRow(
              number = "2",
              title = if (currentLanguage == AppLanguage.ENGLISH) "Confirm Electricity Meter" else "Check the Nepa meter",
              desc = if (currentLanguage == AppLanguage.ENGLISH)
                "Verify whether the house uses a personal prepaid card meter or shared bills with other tenants."
              else
                "Ask if na personal prepaid card meter or shared bill to avoid fight over light bill later."
            )
            Spacer(modifier = Modifier.height(12.dp))
            TipRow(
              number = "3",
              title = if (currentLanguage == AppLanguage.ENGLISH) "Demand Written Tenancy Agreement" else "Collect paper agreement and receipt",
              desc = if (currentLanguage == AppLanguage.ENGLISH)
                "Ensure you receive an official rent receipt and signed tenancy document from the property owner."
              else
                "Make sure landlord or lawyer give you signed receipt and agreement paper."
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Helpline Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = ForestGreenDark)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Benin City Community Help Line" else "Benin City Customer Support",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "For inquiries, reporting issues, or listing multiple properties."
              else
                "Call us if you need help or want to list multiple houses."
              ,
              style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                context.startActivity(intent)
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = ForestGreenDark
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = ForestGreenDark)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "0800-393-556 (Toll-Free Benin)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Info
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "DWELLO HOMES • Version 1.0",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = TextSecondaryDark
            )
          )
          Text(
            text = "Designed for Benin City, Edo State, Nigeria",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark.copy(alpha = 0.7f))
          )
        }
      }
    }
  }
}

@Composable
fun TipRow(number: String, title: String, desc: String) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(ForestGreenPrimary),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimaryDark
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = TextSecondaryDark,
          lineHeight = 18.sp
        )
      )
    }
  }
}
