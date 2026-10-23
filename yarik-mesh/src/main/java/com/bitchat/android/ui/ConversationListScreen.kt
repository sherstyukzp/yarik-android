package com.bitchat.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.geohash.ChannelID
import com.bitchat.android.geohash.GeohashBookmarksStore
import com.bitchat.android.geohash.GeohashChannel
import com.bitchat.android.geohash.GeohashChannelLevel
import com.bitchat.android.geohash.LocationChannelManager
import com.bitchat.android.model.BitchatMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class PublicChatPreview(
    val text: String,
    val timestamp: Long?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: ChatViewModel,
    onOpenPublicChat: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val channelMessages by viewModel.channelMessages.collectAsStateWithLifecycle()
    val joinedChannels by viewModel.joinedChannels.collectAsStateWithLifecycle()
    val unreadChannelMessages by viewModel.unreadChannelMessages.collectAsStateWithLifecycle()
    val connectedPeers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val selectedLocationChannel by viewModel.selectedLocationChannel.collectAsStateWithLifecycle()
    val geohashParticipantCounts by viewModel.geohashParticipantCounts.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    val locationManager = remember { LocationChannelManager.getInstance(context) }
    val bookmarksStore = remember { GeohashBookmarksStore.getInstance(context) }
    val availableChannels by locationManager.availableChannels.collectAsStateWithLifecycle()
    val locationNames by locationManager.locationNames.collectAsStateWithLifecycle()
    val bookmarks by bookmarksStore.bookmarks.collectAsStateWithLifecycle()
    val bookmarkNames by bookmarksStore.bookmarkNames.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var showLocationChannels by remember { mutableStateOf(false) }

    val meshPreview = remember(messages) { messages.toPreview() }
    val locationItems = remember(
        selectedLocationChannel,
        availableChannels,
        bookmarks,
        bookmarkNames,
        locationNames,
        channelMessages,
        unreadChannelMessages,
        geohashParticipantCounts,
    ) {
        buildLocationRows(
            selectedLocationChannel = selectedLocationChannel,
            availableChannels = availableChannels,
            bookmarks = bookmarks,
            bookmarkNames = bookmarkNames,
            locationNames = locationNames,
            channelMessages = channelMessages,
            unreadChannelMessages = unreadChannelMessages,
            participantCounts = geohashParticipantCounts,
        )
    }
    val channelItems = remember(joinedChannels, channelMessages, unreadChannelMessages) {
        joinedChannels.sorted().map { channel ->
            val preview = channelMessages[channel].toPreview()
            Triple(channel, preview, unreadChannelMessages[channel] ?: 0)
        }
    }

    val normalizedQuery = query.trim()
    val visibleConversations = conversations.filter { summary ->
        matchesQuery(normalizedQuery, summary.displayName, summary.latestMessagePreview)
    }
    val visibleLocations = locationItems.filter { row ->
        matchesQuery(normalizedQuery, row.title, row.preview.text)
    }
    val visibleChannels = channelItems.filter { (channel, preview, _) ->
        matchesQuery(normalizedQuery, channel, preview.text)
    }
    val showNearby = matchesQuery(
        normalizedQuery,
        context.getString(R.string.nearby_chat_title),
        meshPreview.text,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showLocationChannels = true }) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = stringResource(R.string.cd_open_location_channels),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showMeshPeerList() }) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = stringResource(R.string.new_chat),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_chats)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
            )

            val isEmpty = !showNearby &&
                visibleLocations.isEmpty() &&
                visibleChannels.isEmpty() &&
                visibleConversations.isEmpty()

            if (isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_chats_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    if (showNearby) {
                        item(key = "nearby") {
                            ConversationRow(
                                title = stringResource(R.string.nearby_chat_title),
                                preview = meshPreview.text.ifBlank {
                                    stringResource(R.string.nearby_chat_subtitle)
                                },
                                timestamp = meshPreview.timestamp,
                                unreadCount = 0,
                                connected = isConnected,
                                leading = {
                                    ConversationAvatar(
                                        icon = Icons.Outlined.Bluetooth,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                },
                                supporting = stringResource(
                                    R.string.people_nearby_count,
                                    connectedPeers.size,
                                ),
                                onClick = {
                                    viewModel.endPrivateChat()
                                    viewModel.switchToChannel(null)
                                    viewModel.selectLocationChannel(ChannelID.Mesh)
                                    onOpenPublicChat()
                                },
                            )
                        }
                    }

                    items(visibleLocations, key = { "geo:${it.geohash}" }) { row ->
                        ConversationRow(
                            title = row.title,
                            preview = row.preview.text.ifBlank {
                                stringResource(R.string.location_chat_subtitle)
                            },
                            timestamp = row.preview.timestamp,
                            unreadCount = row.unread,
                            connected = row.people > 0,
                            leading = {
                                ConversationAvatar(
                                    icon = Icons.Filled.LocationOn,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            },
                            supporting = stringResource(R.string.people_nearby_count, row.people),
                            onClick = {
                                viewModel.endPrivateChat()
                                viewModel.switchToChannel(null)
                                viewModel.selectLocationChannel(
                                    ChannelID.Location(
                                        GeohashChannel(
                                            level = row.level,
                                            geohash = row.geohash,
                                        )
                                    )
                                )
                                onOpenPublicChat()
                            },
                        )
                    }

                    items(visibleChannels, key = { (channel, _, _) -> "channel:$channel" }) { (channel, preview, unread) ->
                        ConversationRow(
                            title = channel,
                            preview = preview.text.ifBlank {
                                stringResource(R.string.channel_chat_subtitle)
                            },
                            timestamp = preview.timestamp,
                            unreadCount = unread,
                            connected = false,
                            leading = {
                                ConversationAvatar(
                                    icon = Icons.Filled.Tag,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            },
                            onClick = {
                                viewModel.endPrivateChat()
                                viewModel.switchToChannel(channel)
                                onOpenPublicChat()
                            },
                        )
                    }

                    items(visibleConversations, key = { it.conversationID }) { summary ->
                        ConversationRow(
                            title = summary.displayName,
                            preview = summary.draft?.takeIf { it.isNotBlank() }
                                ?: summary.latestMessagePreview,
                            timestamp = summary.latestMessageAt,
                            unreadCount = summary.unreadCount,
                            connected = summary.isConnected,
                            leading = {
                                PeerAvatar(
                                    name = summary.displayName,
                                    color = MaterialTheme.colorScheme.primary,
                                    isFavorite = summary.isPinned,
                                )
                            },
                            supporting = if (summary.isConnected) {
                                stringResource(R.string.status_online)
                            } else {
                                stringResource(R.string.status_offline)
                            },
                            onClick = { viewModel.showPrivateChatSheet(summary.conversationID) },
                        )
                    }
                }
            }
        }
    }

    if (showLocationChannels) {
        LocationChannelsSheet(
            isPresented = true,
            onDismiss = { showLocationChannels = false },
            onLocationNotesClick = { showLocationChannels = false },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun ConversationRow(
    title: String,
    preview: String,
    timestamp: Long?,
    unreadCount: Int,
    connected: Boolean,
    leading: @Composable () -> Unit,
    supporting: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = preview.ifBlank { supporting.orEmpty() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (unreadCount > 0) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
        leadingContent = {
            BadgedBox(
                badge = {
                    if (connected) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary)
                    }
                }
            ) {
                leading()
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                timestamp?.let {
                    Text(
                        text = formatConversationTime(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unreadCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (unreadCount > 0) {
                    Badge(modifier = Modifier.padding(top = 4.dp)) {
                        Text(unreadCount.coerceAtMost(99).toString())
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ConversationAvatar(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private data class LocationRow(
    val geohash: String,
    val level: GeohashChannelLevel,
    val title: String,
    val preview: PublicChatPreview,
    val unread: Int,
    val people: Int,
)

private fun buildLocationRows(
    selectedLocationChannel: ChannelID?,
    availableChannels: List<GeohashChannel>,
    bookmarks: List<String>,
    bookmarkNames: Map<String, String>,
    locationNames: Map<GeohashChannelLevel, String>,
    channelMessages: Map<String, List<BitchatMessage>>,
    unreadChannelMessages: Map<String, Int>,
    participantCounts: Map<String, Int>,
): List<LocationRow> {
    val selected = (selectedLocationChannel as? ChannelID.Location)?.channel
    val rows = linkedMapOf<String, LocationRow>()

    fun add(channel: GeohashChannel, titleOverride: String? = null) {
        val key = channel.geohash
        if (rows.containsKey(key)) return
        val geoKey = "geo:$key"
        rows[key] = LocationRow(
            geohash = key,
            level = channel.level,
            title = titleOverride
                ?: bookmarkNames[key]
                ?: locationNames[channel.level]
                ?: channel.displayName,
            preview = channelMessages[geoKey].toPreview(),
            unread = unreadChannelMessages[geoKey] ?: 0,
            people = participantCounts[key] ?: 0,
        )
    }

    selected?.let { add(it) }
    availableChannels.forEach { channel ->
        if (channel.geohash in bookmarks || channel.level == GeohashChannelLevel.NEIGHBORHOOD) {
            add(channel, locationNames[channel.level])
        }
    }
    bookmarks.forEach { geohash ->
        val level = geohashLevelFor(geohash)
        add(GeohashChannel(level, geohash), bookmarkNames[geohash])
    }
    return rows.values.toList()
}

private fun geohashLevelFor(geohash: String): GeohashChannelLevel {
    return GeohashChannelLevel.allCases().firstOrNull { it.precision == geohash.length }
        ?: GeohashChannelLevel.NEIGHBORHOOD
}

private fun List<BitchatMessage>?.toPreview(): PublicChatPreview {
    val latest = this
        ?.asReversed()
        ?.firstOrNull { it.sender != "system" && it.content.isNotBlank() }
    return PublicChatPreview(
        text = latest?.content.orEmpty(),
        timestamp = latest?.timestamp?.time,
    )
}

private fun matchesQuery(query: String, vararg fields: String): Boolean {
    if (query.isBlank()) return true
    return fields.any { it.contains(query, ignoreCase = true) }
}

private fun formatConversationTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    return if (
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    ) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
    }
}
