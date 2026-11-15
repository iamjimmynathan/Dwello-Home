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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.AuthenticatedUser
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.calculateLandlordStats
import com.example.ui.AsyncResourceState
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

@Composable
fun ProfileScreen(
  currentLanguage: AppLanguage,
  currentUser: AuthenticatedUser,
  landlordPropertiesState: AsyncResourceState<List<PropertyItem>>,
  landlordEnquiriesState: AsyncResourceState<List<EnquiryItem>>,
  isLoggingOut: Boolean,
  isActionLoading: Boolean,
  onLanguageSelected: (AppLanguage) -> Unit,
  onLanguageToggle: () -> Unit,
  onLogoutClick: () -> Unit,
  onNavigateToLandlordDashboard: () -> Unit,
  onToggleAvailability: (PropertyItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val landlordProperties = when (landlordPropertiesState) {
    is AsyncResourceState.Success -> landlordPropertiesState.data
    else -> emptyList()
  }
  val landlordEnquiries = when (landlordEnquiriesState) {
    is AsyncResourceState.Success -> landlordEnquiriesState.data
    else -> emptyList()
  }
  val landlordStats = remember(landlordProperties, landlordEnquiries) {
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
        .testTag("profile_scroll_view"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
      // 1. Authenticated User & Account Information Overview (No client-side role switcher)
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
              val avatarLetter = currentUser.fullName.trim().firstOrNull()?.uppercaseChar()?.toString()
                ?: if (currentUser.role == AuthAccountRole.TENANT) "T" else "L"

              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(SkyBluePrimary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = avatarLetter,
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentUser.fullName,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavyText
                  ),
                  modifier = Modifier.testTag("profile_user_name")
                )
                Text(
                  text = currentUser.phone,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = NavyTextSecondary,
                    fontWeight = FontWeight.Medium
                  ),
                  modifier = Modifier.testTag("profile_user_phone")
                )
                if (!currentUser.email.isNullOrBlank()) {
                  Text(
                    text = currentUser.email,
                    style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                  color = SoftBlueAccent,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = when (currentUser.role) {
                      AuthAccountRole.TENANT -> "TENANT ACCOUNT"
                      AuthAccountRole.LANDLORD -> "LANDLORD ACCOUNT"
                    },
                    modifier = Modifier
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                      .testTag("profile_user_role_badge"),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = SkyBluePrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
              onClick = onLogoutClick,
              enabled = !isLoggingOut,
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("profile_open_auth_button"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.5.dp, SkyBluePrimary)
            ) {
              if (isLoggingOut) {
                CircularProgressIndicator(
                  color = SkyBluePrimary,
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
              } else {
                Icon(
                  imageVector = Icons.Default.Logout,
                  contentDescription = null,
                  tint = SkyBluePrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
              }
              Text(
                text = DwelloStrings.logoutToWelcomeBtn(currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(
                  color = SkyBluePrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Role-specific tools & guidelines card (Tenant vs Landlord)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            when (currentUser.role) {
              AuthAccountRole.TENANT -> {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Tenant Inspection & Enquiry Guide" else "Tenant Inspection Guide",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "• Dwello Homes coordinates physical inspections before you pay any rent.\n• Submit enquiries directly through the app.\n• Verify borehole running water and personal prepaid meter.\n• Save favourite listings across sessions using your account."
                  else
                    "• Dwello Homes go help you arrange inspection before you pay any rent.\n• Send your enquiry direct for the app.\n• Make sure say borehole water and personal meter dey.\n• Save houses wey you like inside your account.",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, lineHeight = 20.sp)
                )
              }
              AuthAccountRole.LANDLORD -> {
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH) "Landlord Property Management" else "House Owner Information",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (currentLanguage == AppLanguage.ENGLISH)
                    "• List your houses in Benin City on Dwello Homes.\n• All listings undergo Dwello Homes verification.\n• Newly posted properties start as 'Pending Approval'.\n• Only 'Approved' properties appear in tenant searches.\n• Your personal phone number is never exposed to tenants."
                  else
                    "• Put your house for Benin City for Dwello Homes.\n• Dwello Homes must check and verify your house first.\n• New house go start as 'Pending Approval'.\n• Na only 'Approved' house go show for tenant search.\n• Tenants no go see your personal phone number.",
                  style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, lineHeight = 20.sp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Landlord's Properties & Dashboard Section (Landlord role only)
      if (currentUser.role == AuthAccountRole.LANDLORD) {
        item {
          Button(
            onClick = onNavigateToLandlordDashboard,
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("open_landlord_dashboard_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SkyBluePrimary,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "OPEN FULL LANDLORD DASHBOARD"
              else
                "OPEN LANDLORD OFFICE",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
            border = BorderStroke(1.dp, SoftBlueBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Portfolio Summary",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${landlordStats.totalProperties}", fontWeight = FontWeight.Bold, color = DeepNavyText, fontSize = 18.sp)
                  Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${landlordStats.approvedCount}", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 18.sp)
                  Text(text = "Approved", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${landlordStats.pendingCount}", fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 18.sp)
                  Text(text = "Pending", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${landlordStats.rejectedCount}", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 18.sp)
                  Text(text = "Rejected", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "${landlordStats.newEnquiriesCount}", fontWeight = FontWeight.Bold, color = SkyBluePrimary, fontSize = 18.sp)
                  Text(text = "Enquiries", style = MaterialTheme.typography.labelSmall, color = NavyTextSecondary)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = DwelloStrings.myPropertiesHeading(currentLanguage),
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = DeepNavyText
            )
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        items(landlordProperties, key = { it.id }) { prop ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
              .testTag("profile_landlord_prop_${prop.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorderSubtle)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = prop.titleEn,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText),
                  modifier = Modifier.weight(1f)
                )

                val (badgeColor, textColor, statusText) = when (prop.approvalStatus) {
                  PropertyApprovalStatus.PENDING_APPROVAL -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "Pending Approval")
                  PropertyApprovalStatus.APPROVED -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), "Approved (Live)")
                  PropertyApprovalStatus.REJECTED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "Rejected")
                  PropertyApprovalStatus.UNAVAILABLE -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "Unavailable")
                }

                Surface(
                  color = badgeColor,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = textColor)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${prop.area}, Benin City • ${prop.priceFormatted}",
                style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
              )

              if (prop.approvalStatus == PropertyApprovalStatus.REJECTED && !prop.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  color = Color(0xFFFEF2F2),
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, Color(0xFFFECACA)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                      text = "Dwello Moderation Note:",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                    )
                    Text(
                      text = prop.rejectionReason,
                      style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D), fontSize = 11.sp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = onNavigateToLandlordDashboard,
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, SkyBluePrimary),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = SkyBluePrimary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Manage Listing", fontSize = 12.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                }

                if (prop.approvalStatus == PropertyApprovalStatus.APPROVED) {
                  Button(
                    onClick = { onToggleAvailability(prop) },
                    enabled = !isActionLoading,
                    modifier = Modifier
                      .weight(1.2f)
                      .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = Color(0xFFF1F5F9),
                      contentColor = DeepNavyText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.VisibilityOff,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp),
                      tint = DeepNavyText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Mark Unavailable",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = VeryLightBlueBackground,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Landlords cannot approve their own listings. All new or edited properties are independently verified by Dwello Homes moderation before appearing live to tenants in Benin City.",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.bodySmall.copy(color = DeepNavyText, fontSize = 11.sp, lineHeight = 16.sp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
        }
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
                  text = "English",
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
                  text = "Nigerian Pidgin",
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
              text = if (currentLanguage == AppLanguage.ENGLISH) "Contact Dwello Homes Support" else "Contact Dwello Homes Help Desk",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH)
                "Direct line for assistance with house inspections, landlord connections, and tenancy questions in Benin City."
              else
                "Call us if you need help with house inspection or landlord connection for Benin City.",
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                context.startActivity(intent)
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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
            text = "Property-Rental Intermediary Platform",
            style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
          )
          Text(
            text = "Connecting Tenants & Landlords in Benin City, Edo State",
            style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary.copy(alpha = 0.7f), fontSize = 10.sp)
          )
        }
      }
    }
  }
}
