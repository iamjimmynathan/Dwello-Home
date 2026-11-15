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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthenticatedUser
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.PropertyItem
import com.example.data.calculateLandlordStats
import com.example.ui.AsyncResourceState
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun LandlordHomeScreen(
  currentLanguage: AppLanguage,
  currentUser: AuthenticatedUser?,
  propertiesState: AsyncResourceState<List<PropertyItem>>,
  enquiriesState: AsyncResourceState<List<EnquiryItem>>,
  onLanguageToggle: () -> Unit,
  onRefreshData: () -> Unit,
  onNavigateToPostHouse: () -> Unit,
  onNavigateToMyProperties: () -> Unit,
  onNavigateToEnquiries: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val landlordProperties = when (propertiesState) {
    is AsyncResourceState.Success -> propertiesState.data
    else -> emptyList()
  }
  val landlordEnquiries = when (enquiriesState) {
    is AsyncResourceState.Success -> enquiriesState.data
    else -> emptyList()
  }
  val stats = remember(landlordProperties, landlordEnquiries) {
    calculateLandlordStats(landlordProperties, landlordEnquiries)
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
        .testTag("landlord_home_scroll"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
      // Welcome header + Landlord role badge
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = SoftBlueAccent,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SoftBlueBorder)
              ) {
                Text(
                  text = "LANDLORD PORTAL",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary,
                    fontSize = 11.sp
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = DwelloStrings.landlordHomeWelcome(
                currentLanguage,
                currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "Landlord"
              ),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText
              ),
              modifier = Modifier.testTag("landlord_home_welcome_text")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = DwelloStrings.landlordHomeSubtitle(currentLanguage),
              style = MaterialTheme.typography.bodyMedium.copy(
                color = NavyTextSecondary,
                lineHeight = 20.sp
              )
            )
          }

          IconButton(
            onClick = onRefreshData,
            modifier = Modifier
              .size(48.dp)
              .testTag("landlord_home_refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh landlord summary",
              tint = SkyBluePrimary
            )
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
        Spacer(modifier = Modifier.height(18.dp))
      }

      // Live Summary Stats
      item {
        Text(
          text = if (currentLanguage == AppLanguage.ENGLISH) "Portfolio Summary" else "House Summary",
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
            count = stats.totalProperties.toString(),
            colorBg = SoftBlueAccent,
            colorContent = SkyBluePrimary,
            isClickable = true,
            onClick = onNavigateToMyProperties,
            modifier = Modifier.weight(1f),
            testTag = "landlord_home_stat_total"
          )
          DashboardStatCard(
            title = DwelloStrings.statApprovedProperties(currentLanguage),
            count = stats.approvedCount.toString(),
            colorBg = Color(0xFFE8F5E9),
            colorContent = Color(0xFF2E7D32),
            isClickable = true,
            onClick = onNavigateToMyProperties,
            modifier = Modifier.weight(1f),
            testTag = "landlord_home_stat_approved"
          )
          DashboardStatCard(
            title = DwelloStrings.statPendingApproval(currentLanguage),
            count = stats.pendingCount.toString(),
            colorBg = Color(0xFFFEF3C7),
            colorContent = Color(0xFFB45309),
            isClickable = true,
            onClick = onNavigateToMyProperties,
            modifier = Modifier.weight(1f),
            testTag = "landlord_home_stat_pending"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          DashboardStatCard(
            title = DwelloStrings.statRejectedProperties(currentLanguage),
            count = stats.rejectedCount.toString(),
            colorBg = Color(0xFFFEE2E2),
            colorContent = Color(0xFFDC2626),
            isClickable = true,
            onClick = onNavigateToMyProperties,
            modifier = Modifier.weight(1f),
            testTag = "landlord_home_stat_rejected"
          )
          DashboardStatCard(
            title = DwelloStrings.statNewEnquiries(currentLanguage),
            count = stats.newEnquiriesCount.toString(),
            colorBg = SoftBlueAccent,
            colorContent = SkyBlueDark,
            isClickable = true,
            onClick = onNavigateToEnquiries,
            modifier = Modifier.weight(1f),
            testTag = "landlord_home_stat_enquiries"
          )
        }
        Spacer(modifier = Modifier.height(22.dp))
      }

      // Quick Actions
      item {
        Text(
          text = DwelloStrings.landlordQuickActionsHeading(currentLanguage),
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onNavigateToPostHouse,
          modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .testTag("landlord_home_post_house_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SkyBluePrimary,
            contentColor = Color.White
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AddHome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = DwelloStrings.landlordHomePostTitle(currentLanguage),
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              )
              Text(
                text = DwelloStrings.landlordHomePostDesc(currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White.copy(alpha = 0.9f)
                ),
                maxLines = 1
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LandlordActionCard(
          icon = Icons.Default.Apartment,
          title = DwelloStrings.landlordHomePropertiesTitle(currentLanguage),
          subtitle = DwelloStrings.landlordHomePropertiesDesc(currentLanguage),
          onClick = onNavigateToMyProperties,
          testTag = "landlord_home_my_properties_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LandlordActionCard(
          icon = Icons.Default.MailOutline,
          title = DwelloStrings.landlordHomeEnquiriesTitle(currentLanguage),
          subtitle = DwelloStrings.landlordHomeEnquiriesDesc(currentLanguage),
          onClick = onNavigateToEnquiries,
          testTag = "landlord_home_enquiries_button"
        )

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Dwello Homes Support & Approval Rules
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = DwelloStrings.needHelpTitle(currentLanguage),
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = DwelloStrings.needHelpSubtext(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(
                color = NavyTextSecondary,
                lineHeight = 18.sp
              )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                  context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
              ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(DwelloStrings.callDirect(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
              OutlinedButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/234800393556"))
                  context.startActivity(intent)
                },
                border = BorderStroke(1.5.dp, SkyBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
              ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(DwelloStrings.chatWhatsapp(currentLanguage), color = SkyBluePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorderSubtle)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = SkyBluePrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = DwelloStrings.noSelfApprovalNotice(currentLanguage),
              style = MaterialTheme.typography.bodySmall.copy(
                color = NavyTextSecondary,
                lineHeight = 18.sp
              )
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun LandlordActionCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.5.dp, SoftBlueBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(SoftBlueAccent),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = SkyBluePrimary,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = DeepNavyText
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = NavyTextSecondary,
            lineHeight = 17.sp
          )
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = SkyBluePrimary,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
