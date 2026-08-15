package com.example.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.AetherApplication
import com.example.model.MediaItemData
import com.example.model.SmartCategory
import com.example.ui.components.GlassCard
import com.example.ui.components.MiniPlayer
import com.example.ui.folders.FolderBrowserScreen
import com.example.ui.home.HomeScreen
import com.example.ui.library.VideoLibraryScreen
import com.example.ui.player.PlayerScreen
import com.example.ui.playlists.PlaylistsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.NeonCyan

enum class NavTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    LIBRARY("Library", Icons.Filled.Videocam, Icons.Outlined.Videocam),
    FOLDERS("Folders", Icons.Filled.Folder, Icons.Outlined.Folder),
    PLAYLISTS("Playlists", Icons.Filled.PlaylistPlay, Icons.Outlined.PlaylistPlay),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppNavigation(
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    onRequestPip: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as AetherApplication
    val repository = app.mediaRepository
    val engine = app.playbackEngine

    val playerState by engine.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }

    var selectedCategoryForLibrary by remember { mutableStateOf(SmartCategory.ALL_VIDEOS) }
    var selectedFolderForLibrary by remember { mutableStateOf<String?>(null) }

    // Permission Checking
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        hasStoragePermission = granted
        if (granted) {
            repository.refreshMedia()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasStoragePermission) {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isFullPlayerOpen || playerState.pipModeActive) {
            Scaffold(
                bottomBar = {
                    Column {
                        // Mini Player
                        AnimatedVisibility(
                            visible = playerState.currentMedia != null && !isFullPlayerOpen,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            MiniPlayer(
                                playerState = playerState,
                                onPlayPause = { engine.togglePlayPause() },
                                onExpandPlayer = { isFullPlayerOpen = true },
                                onClose = { engine.pause() }
                            )
                        }

                        // Glass Bottom Navigation Bar
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .testTag("bottom_nav_bar")
                            ) {
                                NavTab.entries.forEach { tab ->
                                    val isSelected = selectedTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectedTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                tint = if (isSelected) com.example.ui.theme.ImmersiveLavender else Color.White.copy(alpha = 0.45f)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) com.example.ui.theme.ImmersiveLavender else Color.White.copy(alpha = 0.45f)
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = com.example.ui.theme.ImmersiveMediumPurple,
                                            selectedIconColor = com.example.ui.theme.ImmersiveLavender,
                                            unselectedIconColor = Color.White.copy(alpha = 0.45f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!hasStoragePermission) {
                        PermissionRequiredCard(
                            onRequestPermission = {
                                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
                                } else {
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                permissionLauncher.launch(permissionsToRequest)
                            }
                        )
                    } else {
                        when (selectedTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    repository = repository,
                                    onPlayMedia = { media, resume ->
                                        val startPos = if (resume) media.lastPositionMs else 0L
                                        engine.playMedia(media, startPos)
                                        isFullPlayerOpen = true
                                    },
                                    onOpenCategory = { cat ->
                                        selectedCategoryForLibrary = cat
                                        selectedFolderForLibrary = null
                                        selectedTab = NavTab.LIBRARY
                                    },
                                    onOpenFolder = { path ->
                                        selectedFolderForLibrary = path
                                        selectedCategoryForLibrary = SmartCategory.ALL_VIDEOS
                                        selectedTab = NavTab.LIBRARY
                                    },
                                    onNavigateLibrary = {
                                        selectedCategoryForLibrary = SmartCategory.ALL_VIDEOS
                                        selectedFolderForLibrary = null
                                        selectedTab = NavTab.LIBRARY
                                    },
                                    onNavigatePlaylists = { selectedTab = NavTab.PLAYLISTS },
                                    onNavigateSettings = { selectedTab = NavTab.SETTINGS }
                                )
                            }
                            NavTab.LIBRARY -> {
                                VideoLibraryScreen(
                                    repository = repository,
                                    initialCategory = selectedCategoryForLibrary,
                                    initialFolderPath = selectedFolderForLibrary,
                                    onPlayMedia = { media, resume ->
                                        val startPos = if (resume) media.lastPositionMs else 0L
                                        engine.playMedia(media, startPos)
                                        isFullPlayerOpen = true
                                    }
                                )
                            }
                            NavTab.FOLDERS -> {
                                FolderBrowserScreen(
                                    repository = repository,
                                    onOpenFolder = { path ->
                                        selectedFolderForLibrary = path
                                        selectedCategoryForLibrary = SmartCategory.ALL_VIDEOS
                                        selectedTab = NavTab.LIBRARY
                                    }
                                )
                            }
                            NavTab.PLAYLISTS -> {
                                PlaylistsScreen(
                                    repository = repository,
                                    onPlayMedia = { media, resume ->
                                        val startPos = if (resume) media.lastPositionMs else 0L
                                        engine.playMedia(media, startPos)
                                        isFullPlayerOpen = true
                                    }
                                )
                            }
                            NavTab.SETTINGS -> {
                                SettingsScreen(
                                    currentTheme = currentTheme,
                                    onThemeChanged = onThemeChanged,
                                    repository = repository
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Player Overlay
        AnimatedVisibility(
            visible = isFullPlayerOpen && playerState.currentMedia != null && !playerState.pipModeActive,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerScreen(
                engine = engine,
                onNavigateBack = { isFullPlayerOpen = false },
                onRequestPip = onRequestPip
            )
        }
    }
}

@Composable
fun PermissionRequiredCard(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = NeonCyan.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Storage Access Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FactorPlayX is local-first. We need permission to discover video and audio files stored on your device.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveLavender),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("grant_permission_button")
                ) {
                    Text("Grant Media Permission", color = Color(0xFF140D26), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
