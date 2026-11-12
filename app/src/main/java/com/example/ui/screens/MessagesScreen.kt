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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.EnquiryRepository
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepNavyText
import com.example.ui.theme.MainBackgroundWhite
import com.example.ui.theme.NavyTextSecondary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftBlueBorder
import com.example.ui.theme.VeryLightBlueBackground

data class ChatItem(
  val id: String,
  val senderName: String,
  val role: String,
  val propertyTitle: String,
  val lastMessage: String,
  val time: String,
  val phone: String,
  val unread: Boolean = false
)

@Composable
fun MessagesScreen(
  currentLanguage: AppLanguage,
  onLanguageToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeChat by remember { mutableStateOf<ChatItem?>(null) }
  var messageInput by remember { mutableStateOf("") }

  val chatMessages = remember {
    mutableStateListOf(
      "Hello! I submitted an enquiry for the Self-Con apartment at Uselu / Ugbowo.",
      "Hello! Dwello Homes has notified the landlord, Mr. Osasere. The house is available.",
      "When can we schedule the physical inspection?",
      "Inspection is confirmed for this Saturday by 11:00 AM. A Dwello coordinator will accompany you."
    )
  }

  val quickQuestions = listOf(
    if (currentLanguage == AppLanguage.ENGLISH) "When is the inspection slot?" else "When na the inspection time?",
    if (currentLanguage == AppLanguage.ENGLISH) "Has landlord confirmed?" else "Landlord don agree?",
    if (currentLanguage == AppLanguage.ENGLISH) "Can I pay rent direct to landlord?" else "I fit pay rent direct to landlord?"
  )

  val mockChats = listOf(
    ChatItem(
      id = "chat_1",
      senderName = "Dwello Inspection Desk",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Intermediary Service" else "Dwello Intermediary",
      propertyTitle = "Self-Con at Uselu / Ugbowo",
      lastMessage = "Inspection is confirmed for this Saturday by 11:00 AM...",
      time = "10:30 AM",
      phone = "0800393556",
      unread = true
    ),
    ChatItem(
      id = "chat_2",
      senderName = "Dwello Landlord Liaison",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Chief Ighodaro (Landlord)" else "Chief Ighodaro (House Owner)",
      propertyTitle = "2-Bedroom Flat in GRA",
      lastMessage = "Landlord has accepted your inspection request for next Tuesday.",
      time = "Yesterday",
      phone = "0800393556",
      unread = false
    ),
    ChatItem(
      id = "chat_3",
      senderName = "Dwello Support Benin",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Tenant Support Desk" else "Customer Support Desk",
      propertyTitle = "General Rental Enquiries",
      lastMessage = "Welcome to Dwello Homes! We help you connect with verified landlords.",
      time = "2 days ago",
      phone = "0800393556",
      unread = false
    )
  )

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

    if (activeChat == null) {
      // List of chats
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("messages_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
      ) {
        item {
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH) "Dwello Enquiries & Inspections" else "Enquiries & House Inspections",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = DeepNavyText
            )
          )
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Dwello Homes connects you with landlords and facilitates physical property inspections"
            else
              "Dwello Homes dey connect you with house owners and arrange inspection",
            style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
          )
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Real-time Submitted Property Inspection Enquiries
        if (EnquiryRepository.submittedEnquiries.isNotEmpty()) {
          item {
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Your Submitted Enquiries" else "Your House Enquiries",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(EnquiryRepository.submittedEnquiries) { enq ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = VeryLightBlueBackground),
              border = BorderStroke(1.dp, SoftBlueBorder)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
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
                      text = "Inspection Request",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                  }
                  Text(
                    text = enq.propertyPrice,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = enq.propertyTitle,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Text(
                  text = "📍 ${enq.propertyArea}, Benin City • For: ${enq.tenantName}",
                  style = MaterialTheme.typography.bodySmall.copy(color = NavyTextSecondary)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  color = Color.White,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, SoftBlueBorder)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Slot: ${enq.preferredInspectionDate} • Dwello Homes Coordinating",
                      style = MaterialTheme.typography.labelSmall.copy(color = DeepNavyText, fontWeight = FontWeight.SemiBold)
                    )
                  }
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (currentLanguage == AppLanguage.ENGLISH) "Inspection Channels & Support" else "Chat Channels & Support",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DeepNavyText
              )
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
        }

        items(mockChats) { chat ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { activeChat = chat }
              .testTag("chat_item_${chat.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorderSubtle),
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
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(SkyBluePrimary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "D",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = chat.senderName,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = DeepNavyText
                    )
                  )
                  Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                  )
                }
                Text(
                  text = chat.propertyTitle,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = chat.lastMessage,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = NavyTextSecondary
                  ),
                  maxLines = 1
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    } else {
      // Active Conversation View
      val chat = activeChat!!
      Column(modifier = Modifier.fillMaxSize()) {
        // Conversation Top Bar
        Surface(
          color = Color.White,
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { activeChat = null }
            ) {
              Text(
                text = "← Back",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = SkyBluePrimary
                )
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = chat.senderName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepNavyText)
                )
                Text(
                  text = chat.role,
                  style = MaterialTheme.typography.labelSmall.copy(color = NavyTextSecondary)
                )
              }
            }

            IconButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800393556"))
                context.startActivity(intent)
              }
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = "Call Dwello Support", tint = SkyBluePrimary)
            }
          }
        }

        // Messages list
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            Surface(
              color = SoftBlueAccent,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Text(
                text = "Enquiry: ${chat.propertyTitle} • Coordinated via Dwello Homes",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyText
                )
              )
            }
          }

          items(chatMessages) { msg ->
            val isFromMe = chatMessages.indexOf(msg) % 2 == 0
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
            ) {
              Surface(
                color = if (isFromMe) SkyBluePrimary else VeryLightBlueBackground,
                shape = RoundedCornerShape(12.dp),
                border = if (isFromMe) null else BorderStroke(1.dp, SoftBlueBorder),
                modifier = Modifier.widthIn(max = 280.dp)
              ) {
                Text(
                  text = msg,
                  modifier = Modifier.padding(12.dp),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isFromMe) Color.White else DeepNavyText
                  )
                )
              }
            }
          }
        }

        // Quick suggested enquiry pills
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .background(VeryLightBlueBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(quickQuestions) { q ->
            Surface(
              color = Color.White,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, SoftBlueBorder),
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { chatMessages.add(q) }
            ) {
              Text(
                text = q,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SkyBluePrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
        }

        // Message Input
        Surface(
          color = Color.White,
          shadowElevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = messageInput,
              onValueChange = { messageInput = it },
              placeholder = { Text("Send message to Dwello Homes...") },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
              singleLine = true,
              shape = RoundedCornerShape(20.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VeryLightBlueBackground,
                unfocusedContainerColor = VeryLightBlueBackground,
                focusedBorderColor = SkyBluePrimary,
                unfocusedBorderColor = SoftBlueBorder,
                focusedTextColor = DeepNavyText,
                unfocusedTextColor = DeepNavyText
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = {
                if (messageInput.isNotBlank()) {
                  chatMessages.add(messageInput)
                  messageInput = ""
                }
              },
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SkyBluePrimary)
                .testTag("send_chat_message_btn")
            ) {
              Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
          }
        }
      }
    }
  }
}
