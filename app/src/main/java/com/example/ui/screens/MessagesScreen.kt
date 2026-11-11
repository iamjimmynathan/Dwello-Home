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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.ui.components.DwelloHeader
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightSagePill
import com.example.ui.theme.SoftCreamBackground
import com.example.ui.theme.SoftCreamBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarmGoldAccent

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
      "Good day! Is the self-contained apartment at Uselu still vacant?",
      "Yes, it is very much available. You can come for physical inspection anytime today.",
      "Is the running water constant and light reliable in that compound?",
      "Yes! We have steady borehole water and personal prepaid meter. No NEPA arguments."
    )
  }

  val mockChats = listOf(
    ChatItem(
      id = "chat_1",
      senderName = "Mr. Osasere",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Landlord (Uselu)" else "Landlord (Uselu)",
      propertyTitle = "Self-Con at Uselu / Ugbowo",
      lastMessage = "Yes, it is very much available. You can come for inspection...",
      time = "10:30 AM",
      phone = "08031234567",
      unread = true
    ),
    ChatItem(
      id = "chat_2",
      senderName = "Chief Ighodaro",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Caretaker (GRA)" else "Caretaker (GRA)",
      propertyTitle = "2-Bedroom Flat in GRA",
      lastMessage = "Inspection is available on Saturday by 11 AM.",
      time = "Yesterday",
      phone = "08029876543",
      unread = false
    ),
    ChatItem(
      id = "chat_3",
      senderName = "DWELLO Help Desk",
      role = if (currentLanguage == AppLanguage.ENGLISH) "Benin Support" else "Benin Support",
      propertyTitle = "House Search Assistance",
      lastMessage = "Welcome to DWELLO HOMES! Need help finding houses in Benin City?",
      time = "2 days ago",
      phone = "0800393556",
      unread = false
    )
  )

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
            text = if (currentLanguage == AppLanguage.ENGLISH) "Landlord & Agent Inquiries" else "Messages with Landlord & Agents",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = ForestGreenDark
            )
          )
          Text(
            text = if (currentLanguage == AppLanguage.ENGLISH)
              "Chat directly about inspection times and house availability"
            else
              "Chat direct to agree time for inspection and house availability",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
          )
          Spacer(modifier = Modifier.height(14.dp))
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
            border = BorderStroke(1.dp, SoftCreamBorder),
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
                  .background(ForestGreenPrimary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = chat.senderName.take(1),
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
                      color = TextPrimaryDark
                    )
                  )
                  Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                  )
                }
                Text(
                  text = chat.propertyTitle,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = ForestGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = chat.lastMessage,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark
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
                  color = ForestGreenPrimary
                )
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = chat.senderName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                )
                Text(
                  text = chat.role,
                  style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
              }
            }

            IconButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${chat.phone}"))
                context.startActivity(intent)
              }
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = ForestGreenPrimary)
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
              color = LightSagePill,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Text(
                text = "Inquiry for: ${chat.propertyTitle}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ForestGreenDark
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
                color = if (isFromMe) ForestGreenPrimary else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = if (isFromMe) null else BorderStroke(1.dp, SoftCreamBorder),
                modifier = Modifier.widthIn(max = 280.dp)
              ) {
                Text(
                  text = msg,
                  modifier = Modifier.padding(12.dp),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isFromMe) Color.White else TextPrimaryDark
                  )
                )
              }
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
              placeholder = { Text(if (currentLanguage == AppLanguage.ENGLISH) "Type message..." else "Type message...") },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
              singleLine = true,
              shape = RoundedCornerShape(20.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestGreenPrimary,
                unfocusedBorderColor = SoftCreamBorder
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
                .background(ForestGreenPrimary)
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
