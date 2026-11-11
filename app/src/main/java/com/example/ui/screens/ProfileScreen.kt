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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

enum class UserRole {
  TENANT,
  LANDLORD,
  AGENT
}

@Composable
fun ProfileScreen(
  currentLanguage: AppLanguage,
  onLanguageSelected: (AppLanguage) -> Unit,
  onLanguageToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedRole by remember { mutableStateOf(UserRole.TENANT) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackgroundWhite)
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
      // 1. User & Account Information Overview
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorderSubtle),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(SkyBluePrimary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = when (selectedRole) {
                    UserRole.TENANT -> "T"
                    UserRole.LANDLORD -> "L"
                    UserRole.AGENT -> "A"
                  },
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Text(
                  text = when (selectedRole) {
                    UserRole.TENANT -> if (currentLanguage == AppLanguage.ENGLISH) "Benin Tenant Account" else "Benin Tenant Profile"
                    UserRole.LANDLORD -> if (currentLanguage == AppLanguage.ENGLISH) "Property Owner / Landlord" else "House Owner Account"
                    UserRole.AGENT -> if (currentLanguage == AppLanguage.ENGLISH) "Edo Real Estate Agent" else "Benin Agent Profile"
                  },
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  )
                )
                Text(
                  text = "📍 Benin City, Edo State",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
                Surface(
                  color = SoftBlueAccent,
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.padding(top = 2.dp)
                ) {
                  Text(
                    text = when (selectedRole) {
                      UserRole.TENANT -> "Looking for House"
                      UserRole.LANDLORD -> "Posting Properties"
                      UserRole.AGENT -> "Verified Agent Scout"
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = SkyBluePrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // 2. Role Selector (Tenant, Landlord, Agent)
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Switch User Role" else "Select Your Role",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Tenant Role
          val isTenant = selectedRole == UserRole.TENANT
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedRole = UserRole.TENANT }
              .testTag("role_tenant_btn"),
            color = if (isTenant) SkyBluePrimary else Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isTenant) SkyBluePrimary else SoftBlueBorder)
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isTenant) Color.White else SkyBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tenant",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isTenant) Color.White else DeepNavyText
                )
              )
            }
          }

          // Landlord Role
          val isLandlord = selectedRole == UserRole.LANDLORD
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedRole = UserRole.LANDLORD }
              .testTag("role_landlord_btn"),
            color = if (isLandlord) SkyBluePrimary else Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isLandlord) SkyBluePrimary else SoftBlueBorder)
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = if (isLandlord) Color.White else SkyBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Landlord",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isLandlord) Color.White else DeepNavyText
                )
              )
            }
          }

          // Agent Role
          val isAgent = selectedRole == UserRole.AGENT
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedRole = UserRole.AGENT }
              .testTag("role_agent_btn"),
            color = if (isAgent) SkyBluePrimary else Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isAgent) SkyBluePrimary else SoftBlueBorder)
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = if (isAgent) Color.White else SkyBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Agent",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isAgent) Color.White else DeepNavyText
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Role-specific tools & guidelines card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            when (selectedRole) {
              UserRole.TENANT -> {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant Inspection Advice" else "Tenant Inspection Guide",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "• Never pay before physical inspection in Benin City.\n• Test borehole running water tap.\n• Confirm prepaid meter card.\n• Ask neighbors about street security and flood levels during rain season."
                  else
                    "• No pay money before you see the house with your eye.\n• Test tap water make you sure say borehole dey pump.\n• Confirm prepaid meter card so NEPA fight no go happen.\n• Ask neighbors if street dey flood when rain fall heavy.",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, lineHeight = 20.sp)
                )
              }
              UserRole.LANDLORD -> {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Landlord Notice Board" else "House Owner Information",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "• You can post multiple houses in Benin City for free on DWELLO HOMES.\n• Always issue signed receipts upon receiving rent.\n• Maintain clear compound rules for quiet and peaceful co-living."
                  else
                    "• You fit put as many house as you get for Benin City free of charge.\n• Always give signed paper receipt to tenants.\n• Keep clear compound rules so peace go dey.",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, lineHeight = 20.sp)
                )
              }
              UserRole.AGENT -> {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Agent Ethics & Direct Inspection" else "Agent Rules and Code",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "• Only list houses where you have direct contact with the owner or principal caretaker.\n• No fake inspection charges or phantom listings.\n• Ensure full disclosure of caution fees and agreement terms."
                  else
                    "• Put only houses wey you sabi the landlord or main caretaker well.\n• No take fake inspection fee.\n• Tell tenants the exact agreement and caution fee upfront.",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, lineHeight = 20.sp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // 3. Language Switcher Section
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Choose Language" else "Select Language",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            // English Option
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLanguageSelected(AppLanguage.ENGLISH) }
                .background(if (currentLanguage == AppLanguage.ENGLISH) SoftBlueAccent else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "🇳🇬 English",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (currentLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Medium,
                    color = DeepNavyText
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "(Standard English)",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
              }
              if (currentLanguage == AppLanguage.ENGLISH) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = SkyBluePrimary
                )
              }
            }

            HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))

            // Pidgin Option
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLanguageSelected(AppLanguage.PIDGIN) }
                .background(if (currentLanguage == AppLanguage.PIDGIN) SoftBlueAccent else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "🇳🇬 Nigerian Pidgin",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (currentLanguage == AppLanguage.PIDGIN) FontWeight.Bold else FontWeight.Medium,
                    color = DeepNavyText
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "(Wetin everyone understand)",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )
              }
              if (currentLanguage == AppLanguage.PIDGIN) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = SkyBluePrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // 4. Helpline & Support Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Benin City Community Help Line" else "Benin City Customer Support",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "Direct line for assistance with inspections, complaints, or questions."
              else
                "Call us if you need help or get question about house for Benin.",
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                context.startActivity(intent)
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(
                containerColor = SkyBluePrimary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "0800-393-556 (Toll-Free Benin)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Info & Demonstration Notice
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "DWELLO HOMES • Version 1.0",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = DeepNavyText
            )
          )
          Text(
            text = "Designed for Benin City, Edo State, Nigeria",
            style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
          )
          Text(
            text = "Demonstration Platform • Connecting Tenants & Landlords",
            style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary.copy(alpha = 0.7f), fontSize = 10.sp)
          )
        }
      }
    }
  }
}
