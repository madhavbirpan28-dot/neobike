package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeoViewModel
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    viewModel: NeoViewModel,
    selectedConversationId: String?,
    onOpenConversation: (String) -> Unit,
    onCloseConversation: () -> Unit,
    onOpenRideDetail: (String) -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val allRides by viewModel.rides.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val blockedUsers by viewModel.blockedUserIds.collectAsState()

    // If an active conversation is open
    if (selectedConversationId != null) {
        val conv = conversations.find { it.id == selectedConversationId }
        if (conv != null) {
            val convMessages = allMessages.filter { it.conversationId == conv.id }
            val pinnedRide = if (conv.pinnedRideId != null) allRides.find { it.id == conv.pinnedRideId } else null

            ActiveChatView(
                conversation = conv,
                messages = convMessages,
                pinnedRide = pinnedRide,
                currentUser = currentUser,
                blockedUsers = blockedUsers,
                onBack = onCloseConversation,
                onSendMessage = { text ->
                    viewModel.sendMessage(conv.id, text)
                },
                onOpenRideDetail = onOpenRideDetail,
                onBlockUser = { userId -> viewModel.blockUser(userId) },
                onReportMessage = { msgId, text ->
                    viewModel.openReportDialog(ReportTargetType.MESSAGE, msgId, "Message: $text")
                }
            )
            return
        }
    }

    // Conversations List
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inbox_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Touring Chats & Inbox",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Direct communications with accepted co-riders, group tours, and clubs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (conversations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active conversations yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(conversations) { conv ->
            val otherParticipantId = conv.participantIds.find { it != currentUser.id }
            val isBlocked = otherParticipantId != null && blockedUsers.contains(otherParticipantId)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeoDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenConversation(conv.id) }
                    .testTag("conversation_item_${conv.id}")
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
                            .background(
                                when (conv.type) {
                                    ConversationType.RIDE_GROUP -> NeoTealDark
                                    ConversationType.COMMUNITY -> NeoAmberDark
                                    ConversationType.DIRECT -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (conv.type) {
                                ConversationType.RIDE_GROUP -> "🏍️"
                                ConversationType.COMMUNITY -> "👥"
                                ConversationType.DIRECT -> "💬"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = conv.lastMessageTime,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isBlocked) "[User Blocked]" else conv.lastMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBlocked) NeoError else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (conv.pinnedRideId != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📌 Pinned Ride Details",
                                fontSize = 10.sp,
                                color = NeoTealLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChatView(
    conversation: Conversation,
    messages: List<Message>,
    pinnedRide: Ride?,
    currentUser: User,
    blockedUsers: Set<String>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Boolean,
    onOpenRideDetail: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onReportMessage: (String, String) -> Unit
) {
    BackHandler { onBack() }

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val otherParticipantId = conversation.participantIds.find { it != currentUser.id }
    val isBlocked = otherParticipantId != null && blockedUsers.contains(otherParticipantId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_chat_view")
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${conversation.participantIds.size} Participants",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (otherParticipantId != null) {
                    TextButton(onClick = { onBlockUser(otherParticipantId) }) {
                        Text(if (isBlocked) "Blocked" else "Block", color = NeoError, fontSize = 11.sp)
                    }
                }
            }
        }

        // Pinned Ride Banner
        if (pinnedRide != null) {
            Surface(
                color = NeoTealDark.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeoTealPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenRideDetail(pinnedRide.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📌 Pinned Ride: ${pinnedRide.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeoTealLight)
                        Text("${pinnedRide.origin} ➔ ${pinnedRide.destination} • ${pinnedRide.departureDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("View ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeoAmberAccent)
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        color = if (isMe) NeoTealDark else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clickable {
                                if (!isMe) onReportMessage(msg.id, msg.text)
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!isMe) {
                                Text(
                                    text = msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = NeoAmberAccent
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.timestamp,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Input Field
        if (isBlocked) {
            Surface(
                color = NeoError.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    "You cannot send messages to this conversation because this contact is blocked.",
                    color = NeoError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type message...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput.trim())
                                textInput = ""
                                scope.launch {
                                    listState.animateScrollToItem((messages.size).coerceAtLeast(0))
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = NeoTealPrimary),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
