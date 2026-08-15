package com.example.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.repository.MediaRepository
import com.example.model.MediaItemData
import com.example.model.SmartCategory
import com.example.model.SortOption
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.MediaThumbnail
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderSubtle
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersiveSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLibraryScreen(
    repository: MediaRepository,
    onPlayMedia: (MediaItemData, resume: Boolean) -> Unit,
    initialCategory: SmartCategory = SmartCategory.ALL_VIDEOS,
    initialFolderPath: String? = null
) {
    val allMedia by repository.enrichedMediaList.collectAsState()
    val isScanning by repository.isScanning.collectAsState()
    val playlists by repository.allPlaylists.collectAsState(initial = emptyList())

    var isGridView by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_MODIFIED) }
    var isAscending by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    var selectedItemForPlaylist by remember { mutableStateOf<MediaItemData?>(null) }
    val scope = rememberCoroutineScope()

    val filteredMedia = remember(allMedia, searchQuery, selectedCategory, selectedSortOption, isAscending, initialFolderPath) {
        repository.filterAndSortMedia(
            items = allMedia,
            query = searchQuery,
            category = selectedCategory,
            sortOption = selectedSortOption,
            isAscending = isAscending,
            folderPathFilter = initialFolderPath
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .testTag("video_library_screen")
    ) {
        // TOP APP BAR & SEARCH
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialFolderPath != null) "Folder Videos" else "Media Library",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Grid / List View Toggle
                    GlassIconButton(
                        icon = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        onClick = { isGridView = !isGridView },
                        testTag = "toggle_view_mode"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort & Filter
                    GlassIconButton(
                        icon = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        onClick = { showSortSheet = true },
                        testTag = "sort_options_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search ${filteredMedia.size} items..."
            )
        }

        // CATEGORY CHIPS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SmartCategory.entries) { cat ->
                GlassChip(
                    text = cat.title,
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // MEDIA COUNT & CURRENT SORT SUMMARY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredMedia.size} files found",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isAscending = !isAscending }
            ) {
                Text(
                    text = "${selectedSortOption.displayName} • ${if (isAscending) "Asc" else "Desc"}",
                    fontSize = 12.sp,
                    color = ImmersiveLavender,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = ImmersiveLavender,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // MEDIA CONTENT GRID / LIST
        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No media matching '$searchQuery'" else "No media files available in this category",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia, key = { it.uriString }) { item ->
                    VideoGridCard(
                        item = item,
                        onClick = { onPlayMedia(item, item.lastPositionMs > 5000) },
                        onToggleFavorite = {
                            scope.launch { repository.toggleFavorite(item) }
                        },
                        onAddToPlaylist = { selectedItemForPlaylist = item }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia, key = { it.uriString }) { item ->
                    VideoListCard(
                        item = item,
                        onClick = { onPlayMedia(item, item.lastPositionMs > 5000) },
                        onToggleFavorite = {
                            scope.launch { repository.toggleFavorite(item) }
                        },
                        onAddToPlaylist = { selectedItemForPlaylist = item }
                    )
                }
            }
        }

        // SORT OPTIONS BOTTOM SHEET
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = ImmersiveSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Sort Media By",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SortOption.entries.forEach { option ->
                        val isSelected = selectedSortOption == option
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedSortOption = option
                                    showSortSheet = false
                                },
                            color = if (isSelected) ImmersiveMediumPurple.copy(alpha = 0.5f) else Color.Transparent
                        ) {
                            Text(
                                text = option.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ImmersiveLavender else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ADD TO PLAYLIST DROPDOWN SHEET
        if (selectedItemForPlaylist != null) {
            val item = selectedItemForPlaylist!!
            ModalBottomSheet(
                onDismissRequest = { selectedItemForPlaylist = null },
                sheetState = rememberModalBottomSheetState(),
                containerColor = ImmersiveSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add to Playlist",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (playlists.isEmpty()) {
                        Text(
                            text = "No user playlists found. Create one in the Playlists tab.",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    } else {
                        playlists.forEach { pl ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            repository.addToPlaylist(pl.id, item)
                                            selectedItemForPlaylist = null
                                        }
                                    },
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = pl.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun VideoGridCard(
    item: MediaItemData,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
        borderColor = if (item.isFavorite) ImmersiveLavender.copy(alpha = 0.5f) else ImmersiveGlassBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                MediaThumbnail(
                    uriString = item.uriString,
                    isVideo = item.isVideo,
                    modifier = Modifier.fillMaxSize()
                )

                // Top right favorite badge
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) ImmersiveLavender else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Resolution Badge
                GlassBadge(
                    text = item.resolutionLabel,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                )

                // Duration Badge
                GlassBadge(
                    text = item.formattedDuration,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }

            // Progress indicator if partially watched
            if (item.lastPositionMs > 0) {
                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = ImmersiveLavender,
                    trackColor = Color(0x22FFFFFF)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.formattedSize} • ${item.folderName}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VideoListCard(
    item: MediaItemData,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
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
                    .size(width = 90.dp, height = 60.dp)
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
                        .padding(3.dp)
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
                Spacer(modifier = Modifier.height(3.dp))
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
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) ImmersiveLavender else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onAddToPlaylist) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Add to playlist",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
