package com.example.ui.playlists

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import com.example.data.repository.MediaRepository
import com.example.model.MediaItemData
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.MediaThumbnail
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersiveSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    repository: MediaRepository,
    onPlayMedia: (MediaItemData, resume: Boolean) -> Unit
) {
    val playlists by repository.allPlaylists.collectAsState(initial = emptyList())
    val favorites by repository.favoriteItems.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToRename by remember { mutableStateOf<PlaylistEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .testTag("playlists_screen")
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Playlists",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${playlists.size + 1} lists available",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.testTag("create_playlist_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ImmersiveLightPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Playlist", color = ImmersiveLightPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FAVORITES SYSTEM PLAYLIST
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (favorites.isNotEmpty()) {
                                onPlayMedia(favorites.first(), false)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = ImmersiveSurface.copy(alpha = 0.85f),
                    borderColor = ImmersiveLavender.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ImmersiveMediumPurple.copy(alpha = 0.4f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = ImmersiveLavender,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorites",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${favorites.size} favorited tracks and videos",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        if (favorites.isNotEmpty()) {
                            IconButton(onClick = { onPlayMedia(favorites.first(), false) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play All",
                                    tint = ImmersiveLavender
                                )
                            }
                        }
                    }
                }
            }

            // USER AND CUSTOM PLAYLISTS
            items(playlists) { pl ->
                val itemsFlow = remember(pl.id) { repository.getPlaylistItems(pl.id) }
                val itemsList by itemsFlow.collectAsState(initial = emptyList())

                PlaylistItemRow(
                    playlist = pl,
                    itemCount = itemsList.size,
                    onClick = { selectedPlaylistForDetail = pl },
                    onPlayAll = {
                        if (itemsList.isNotEmpty()) {
                            val mediaMap = repository.enrichedMediaList.value.associateBy { it.uriString }
                            val firstMedia = mediaMap[itemsList.first().mediaUriString]
                            if (firstMedia != null) {
                                onPlayMedia(firstMedia, false)
                            }
                        }
                    },
                    onRename = { playlistToRename = pl },
                    onDelete = {
                        scope.launch { repository.deletePlaylist(pl.id) }
                    }
                )
            }
        }

        // CREATE PLAYLIST DIALOG
        if (showCreateDialog) {
            var plName by remember { mutableStateOf("") }
            Dialog(onDismissRequest = { showCreateDialog = false }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ImmersiveSurface,
                    borderColor = ImmersiveGlassBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Create New Playlist",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = plName,
                            onValueChange = { plName = it },
                            placeholder = { Text("Playlist name...", color = Color.White.copy(alpha = 0.4f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ImmersiveLavender,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCreateDialog = false }) {
                                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (plName.isNotBlank()) {
                                        scope.launch {
                                            repository.createPlaylist(plName.trim())
                                            showCreateDialog = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text("Create", color = ImmersiveLightPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // RENAME PLAYLIST DIALOG
        if (playlistToRename != null) {
            val pl = playlistToRename!!
            var renameText by remember { mutableStateOf(pl.name) }
            Dialog(onDismissRequest = { playlistToRename = null }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ImmersiveSurface,
                    borderColor = ImmersiveGlassBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Rename Playlist",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ImmersiveLavender,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { playlistToRename = null }) {
                                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (renameText.isNotBlank()) {
                                        scope.launch {
                                            repository.renamePlaylist(pl.id, renameText.trim())
                                            playlistToRename = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text("Save", color = ImmersiveLightPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // PLAYLIST DETAIL BOTTOM SHEET
        if (selectedPlaylistForDetail != null) {
            val pl = selectedPlaylistForDetail!!
            val itemsFlow = remember(pl.id) { repository.getPlaylistItems(pl.id) }
            val itemsList by itemsFlow.collectAsState(initial = emptyList())

            ModalBottomSheet(
                onDismissRequest = { selectedPlaylistForDetail = null },
                sheetState = rememberModalBottomSheetState(),
                containerColor = ImmersiveSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = pl.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "${itemsList.size} media items", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }

                        if (itemsList.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val mediaMap = repository.enrichedMediaList.value.associateBy { it.uriString }
                                    val firstMedia = mediaMap[itemsList.first().mediaUriString]
                                    if (firstMedia != null) {
                                        selectedPlaylistForDetail = null
                                        onPlayMedia(firstMedia, false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ImmersiveLightPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", color = ImmersiveLightPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (itemsList.isEmpty()) {
                        Text(
                            text = "This playlist is empty. Add media from the Library tab.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            items(itemsList) { item ->
                                val mediaMap = repository.enrichedMediaList.value.associateBy { it.uriString }
                                val resolved = mediaMap[item.mediaUriString]

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.07f))
                                        .clickable {
                                            if (resolved != null) {
                                                selectedPlaylistForDetail = null
                                                onPlayMedia(resolved, false)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = if (item.isVideo) "Video" else "Audio", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                    }
                                    IconButton(
                                        onClick = { scope.launch { repository.removeFromPlaylist(item.id) } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItemRow(
    playlist: PlaylistEntity,
    itemCount: Int,
    onClick: () -> Unit,
    onPlayAll: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
        borderColor = ImmersiveGlassBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ImmersiveMediumPurple.copy(alpha = 0.35f),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = ImmersiveLavender,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "$itemCount media items",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (itemCount > 0) {
                IconButton(onClick = onPlayAll) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = ImmersiveLavender)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White.copy(alpha = 0.6f))
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Playlist") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
