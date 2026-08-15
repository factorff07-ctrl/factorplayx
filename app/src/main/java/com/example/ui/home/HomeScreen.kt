package com.example.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MediaRepository
import com.example.model.FolderItem
import com.example.model.MediaItemData
import com.example.model.SmartCategory
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassIconButton
import com.example.ui.components.MediaThumbnail
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveEmerald
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderSubtle
import com.example.ui.theme.ImmersiveHeroGradient
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersivePlayGradient
import com.example.ui.theme.ImmersiveSurface

@Composable
fun HomeScreen(
    repository: MediaRepository,
    onPlayMedia: (MediaItemData, resume: Boolean) -> Unit,
    onOpenCategory: (SmartCategory) -> Unit,
    onOpenFolder: (String) -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigatePlaylists: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val allMedia by repository.enrichedMediaList.collectAsState()
    val continueWatching by repository.continueWatchingItems.collectAsState()
    val favorites by repository.favoriteItems.collectAsState()
    val folders by repository.rawFolderList.collectAsState()
    val isScanning by repository.isScanning.collectAsState()
    val scanProgress by repository.scanProgress.collectAsState()

    val totalVideos = allMedia.count { it.isVideo }
    val totalAudio = allMedia.count { !it.isVideo }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // TOP APP BAR / BRAND HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AETHER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ImmersiveLavender,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = " PLAYER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = "$totalVideos Videos • $totalAudio Audio Tracks",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Rescan Library",
                        onClick = { repository.refreshMedia() },
                        testTag = "home_rescan_button"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onNavigateSettings,
                        testTag = "home_settings_button"
                    )
                }
            }
        }

        // SCANNING BANNER
        if (isScanning) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    backgroundColor = ImmersiveDeepPurple.copy(alpha = 0.4f),
                    borderColor = ImmersiveLavender.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ImmersiveLavender,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Scanning Media Library...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Found ${scanProgress.first} media files",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // CONTINUE WATCHING CAROUSEL
        if (continueWatching.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONTINUE WATCHING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = "${continueWatching.size} items",
                            fontSize = 12.sp,
                            color = ImmersiveLavender
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(continueWatching) { item ->
                            ContinueWatchingCard(
                                item = item,
                                onClick = { onPlayMedia(item, true) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // SMART CATEGORIES HORIZONTAL CHIPS
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK CATEGORIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SmartCategory.entries) { cat ->
                        val icon = when (cat) {
                            SmartCategory.ALL_VIDEOS -> Icons.Default.Videocam
                            SmartCategory.MOVIES -> Icons.Default.Movie
                            SmartCategory.SHORT_VIDEOS -> Icons.Default.PlayArrow
                            SmartCategory.LARGE_FILES -> Icons.Default.Folder
                            SmartCategory.UNWATCHED -> Icons.Default.Bookmark
                            SmartCategory.PARTIALLY_WATCHED -> Icons.Default.History
                            SmartCategory.ALL_AUDIO -> Icons.Default.Audiotrack
                        }
                        GlassChip(
                            text = cat.title,
                            isSelected = false,
                            icon = icon,
                            onClick = { onOpenCategory(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // QUICK FOLDERS ROW
        if (folders.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MEDIA FOLDERS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        TextButton(onClick = onNavigateLibrary) {
                            Text("View All", fontSize = 13.sp, color = ImmersiveLavender)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(folders.take(6)) { folder ->
                            FolderMiniCard(
                                folder = folder,
                                onClick = { onOpenFolder(folder.path) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // RECENTLY ADDED MEDIA
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT DISCOVERIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    TextButton(onClick = onNavigateLibrary) {
                        Text("Browse Library", fontSize = 13.sp, color = ImmersiveLavender)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val recentItems = allMedia.take(6)
                if (recentItems.isEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        backgroundColor = ImmersiveSurface.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No media files found",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Add video or audio files to device storage to get started",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentItems.forEach { item ->
                            MediaRowCard(
                                item = item,
                                onClick = { onPlayMedia(item, false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: MediaItemData,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = ImmersiveSurface.copy(alpha = 0.85f),
        borderColor = ImmersiveGlassBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                MediaThumbnail(
                    uriString = item.uriString,
                    isVideo = item.isVideo,
                    modifier = Modifier.fillMaxSize()
                )

                // Play Button overlay with Immersive Play Gradient
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(ImmersivePlayGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Remaining time badge
                GlassBadge(
                    text = item.remainingDurationFormatted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            // Progress bar in glowing lavender
            LinearProgressIndicator(
                progress = { item.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = ImmersiveLavender,
                trackColor = Color(0x22FFFFFF)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${(item.progressFraction * 100).toInt()}% watched",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun FolderMiniCard(
    folder: FolderItem,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
        borderColor = ImmersiveGlassBorder
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ImmersiveMediumPurple.copy(alpha = 0.4f),
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = ImmersiveLavender,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = folder.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${folder.mediaCount} items",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MediaRowCard(
    item: MediaItemData,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = ImmersiveSurface.copy(alpha = 0.75f),
        borderColor = ImmersiveGlassBorderSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 54.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                MediaThumbnail(
                    uriString = item.uriString,
                    isVideo = item.isVideo,
                    modifier = Modifier.fillMaxSize()
                )
                GlassBadge(
                    text = item.formattedDuration,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassBadge(
                        text = item.resolutionLabel,
                        backgroundColor = ImmersiveMediumPurple.copy(alpha = 0.4f),
                        textColor = ImmersiveLavender
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.formattedSize} • ${item.folderName}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = ImmersiveLavender,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
