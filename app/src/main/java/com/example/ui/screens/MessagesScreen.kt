package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AuthAccountRole
import com.example.data.DwelloStrings
import com.example.data.EnquiryItem
import com.example.data.EnquiryStatus
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
fun MessagesScreen(
  currentLanguage: AppLanguage,
  userRole: AuthAccountRole,
  enquiriesState: AsyncResourceState<List<EnquiryItem>>,
  isActionLoading: Boolean,
  actionMessage: String?,
  actionError: String?,
  onRefreshEnquiries: () -> Unit,
  onUpdateEnquiryStatus: (enquiryId: String, status: EnquiryStatus) -> Unit,
  onClearFeedback: () -> Unit,
  onLanguageToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

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
        .testTag("messages_list"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = when (userRole) {
                AuthAccountRole.TENANT -> if (currentLanguage == AppLanguage.ENGLISH) {
                  "My Enquiries & Inspections"
                } else {
                  "My House Enquiries"
                }
                AuthAccountRole.LANDLORD -> if (currentLanguage == AppLanguage.ENGLISH) {
                  "Tenant Property Enquiries"
                } else {
                  "Tenant House Enquiries"
                }
              },
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavyText
              )
            )
            Text(
              text = when (userRole) {
                AuthAccountRole.TENANT -> if (currentLanguage == AppLanguage.ENGLISH) {
                  "Track inspection requests sent to Dwello Homes"
                } else {
                  "Track house inspection requests wey you send to Dwello Homes"
                }
                AuthAccountRole.LANDLORD -> if (currentLanguage == AppLanguage.ENGLISH) {
                  "Manage tenant inspection enquiries for your properties"
                } else {
                  "Check and update enquiries wey tenants send for your houses"
                }
              },
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )
          }

          IconButton(
            onClick = onRefreshEnquiries,
            modifier = Modifier.testTag("refresh_enquiries_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh enquiries",
              tint = SkyBluePrimary
            )
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      when (enquiriesState) {
        is AsyncResourceState.Idle,
        is AsyncResourceState.Loading -> {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("enquiries_loading"),
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
                  text = if (currentLanguage == AppLanguage.ENGLISH) {
                    "Loading enquiries..."
                  } else {
                    "Dey load enquiries..."
                  },
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
                .testTag("enquiries_error_card"),
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
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = Color(0xFFDC2626),
                  modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = enquiriesState.message,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Medium
                  )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = onRefreshEnquiries,
                  colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                  modifier = Modifier.testTag("enquiries_retry_button")
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
          val enquiries = enquiriesState.data
          if (enquiries.isEmpty()) {
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp)
                  .testTag("enquiries_empty_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
                border = BorderStroke(1.dp, SoftBlueBorder)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(40.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = if (userRole == AuthAccountRole.TENANT) {
                      if (currentLanguage == AppLanguage.ENGLISH) {
                        "You haven't submitted any property enquiries yet."
                      } else {
                        "You never send any house enquiry yet."
                      }
                    } else {
                      if (currentLanguage == AppLanguage.ENGLISH) {
                        "No tenant enquiries received yet."
                      } else {
                        "No tenant enquiry don enter yet."
                      }
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = if (userRole == AuthAccountRole.TENANT) {
                      "Open any approved listing in Find Houses and tap 'Send Enquiry' to book an inspection."
                    } else {
                      "Approved listings receive tenant inspection enquiries right here."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                  )
                }
              }
            }
          } else {
            items(enquiries, key = { it.id }) { enq ->
              EnquiryCard(
                enquiry = enq,
                userRole = userRole,
                currentLanguage = currentLanguage,
                isActionLoading = isActionLoading,
                onMarkContacted = { onUpdateEnquiryStatus(enq.id, EnquiryStatus.CONTACTED) },
                onCloseEnquiry = { onUpdateEnquiryStatus(enq.id, EnquiryStatus.CLOSED) }
              )
            }
          }
        }
      }

      // Dwello Coordinator Support Card
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
          border = BorderStroke(1.dp, SoftBlueBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) {
                "Dwello Homes Inspection Desk"
              } else {
                "Dwello Homes Inspection Help Desk"
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) {
                "Need immediate help coordinating a viewing in Benin City? Call our toll-free desk."
              } else {
                "If you need urgent help to inspect house for Benin City, call our free line."
              },
              style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("enquiries_call_support_button")
            ) {
              Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Call Dwello Support (0800-393-556)", fontWeight = FontWeight.Bold)
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

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
          text = if (isErr) "Update Failed" else "Enquiry Updated",
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
private fun EnquiryCard(
  enquiry: EnquiryItem,
  userRole: AuthAccountRole,
  currentLanguage: AppLanguage,
  isActionLoading: Boolean,
  onMarkContacted: () -> Unit,
  onCloseEnquiry: () -> Unit
) {
  val (badgeBg, badgeText) = when (enquiry.status) {
    EnquiryStatus.NEW -> SoftBlueAccent to SkyBlueDark
    EnquiryStatus.CONTACTED -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    EnquiryStatus.CLOSED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 12.dp)
      .testTag("enquiry_card_${enquiry.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, CardBorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = badgeBg,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) enquiry.statusEn else enquiry.statusPidgin,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = badgeText,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          )
        }

        if (enquiry.propertyPrice.isNotBlank()) {
          Text(
            text = enquiry.propertyPrice,
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = SkyBluePrimary
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = enquiry.propertyTitle,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = DeepNavyText
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = SkyBluePrimary,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (userRole == AuthAccountRole.LANDLORD) {
            "${enquiry.propertyArea}, Benin City • Tenant: ${enquiry.tenantName}"
          } else {
            "${enquiry.propertyArea}, Benin City"
          },
          style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        color = VeryLightBlueBackground,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SoftBlueBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Preferred Inspection: ${enquiry.preferredInspectionDate}",
            style = MaterialTheme.typography.labelMedium.copy(
              color = DeepNavyText,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
      }

      if (enquiry.message.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = enquiry.message,
          style = MaterialTheme.typography.bodySmall.copy(
            color = DeepNavyText,
            lineHeight = 18.sp
          )
        )
      }

      if (userRole == AuthAccountRole.LANDLORD && enquiry.status != EnquiryStatus.CLOSED) {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = SoftBlueBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (enquiry.status != EnquiryStatus.CONTACTED) {
            OutlinedButton(
              onClick = onMarkContacted,
              enabled = !isActionLoading,
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("enquiry_mark_contacted_${enquiry.id}"),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, SkyBluePrimary)
            ) {
              Text("Mark Contacted", color = SkyBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Button(
            onClick = onCloseEnquiry,
            enabled = !isActionLoading,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("enquiry_close_${enquiry.id}"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
          ) {
            Text("Close Enquiry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
