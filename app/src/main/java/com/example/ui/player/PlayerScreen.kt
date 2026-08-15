package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioMode
import com.example.player.MediaPlaybackEngine
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveHeroGradient
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.PlayerScrimGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    engine: MediaPlaybackEngine,
    onNavigateBack: () -> Unit,
    onRequestPip: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by engine.uiState.collectAsState()
    val audioEffectsState by engine.audioEffectsManager.effectsState.collectAsState()
    val bookmarks by engine.uiState.value.currentMedia?.let {
        remember(it.uriString) {
            // Observe bookmarks for current media
            com.example.AetherApplication::class.java.cast(context.applicationContext)
                ?.mediaRepository?.getBookmarksForMedia(it.uriString)
        }
    }?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    var controlsVisible by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showTrackSheet by remember { mutableStateOf(false) }
    var showVideoInfo by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }

    // Gesture HUD States
    var gestureHudText by remember { mutableStateOf<String?>(null) }
    var gestureHudIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var gestureHudProgress by remember { mutableFloatStateOf(0f) }
    var isTurboSpeedActive by remember { mutableStateOf(false) }

    // Drag Seek State
    var isDraggingSeek by remember { mutableStateOf(false) }
    var draggedSeekPositionMs by remember { mutableLongStateOf(0L) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val coroutineScope = rememberCoroutineScope()

    // Subtitle File Picker Launcher
    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            engine.addExternalSubtitle(uri)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(controlsVisible, uiState.isPlaying) {
        if (controlsVisible && uiState.isPlaying && !uiState.isLocked) {
            delay(4000)
            controlsVisible = false
        }
    }

    // Keep screen on during playback
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = engine.exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.RATIO_16_9, AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            },
            update = { playerView ->
                playerView.player = engine.exoPlayer
                playerView.resizeMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.RATIO_16_9, AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Detection Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isLocked) {
                    if (uiState.isLocked) {
                        detectTapGestures(
                            onTap = { controlsVisible = !controlsVisible }
                        )
                        return@pointerInput
                    }

                    detectTapGestures(
                        onTap = {
                            controlsVisible = !controlsVisible
                        },
                        onDoubleTap = { offset ->
                            val xPos = offset.x
                            val widthPx = size.width
                            if (xPos < widthPx * 0.35f) {
                                engine.seekBy(-10)
                                gestureHudIcon = Icons.Default.FastRewind
                                gestureHudText = "-10 sec"
                                coroutineScope.launch {
                                    delay(800)
                                    gestureHudText = null
                                }
                            } else if (xPos > widthPx * 0.65f) {
                                engine.seekBy(10)
                                gestureHudIcon = Icons.Default.FastForward
                                gestureHudText = "+10 sec"
                                coroutineScope.launch {
                                    delay(800)
                                    gestureHudText = null
                                }
                            } else {
                                engine.togglePlayPause()
                            }
                        },
                        onLongPress = {
                            // 4× Turbo Speed on Long Press!
                            isTurboSpeedActive = true
                            engine.setPlaybackSpeed(4.0f)
                            gestureHudIcon = Icons.Default.Speed
                            gestureHudText = "4.0× TURBO SPEED"
                        }
                    )
                }
                .pointerInput(uiState.isLocked) {
                    if (uiState.isLocked) return@pointerInput

                    detectVerticalDragGestures(
                        onDragStart = {},
                        onDragEnd = {
                            gestureHudText = null
                        },
                        onDragCancel = {
                            gestureHudText = null
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val isLeft = change.position.x < size.width / 2
                            val delta = -dragAmount / size.height

                            if (isLeft) {
                                // Brightness Gesture
                                val window = activity?.window
                                val params = window?.attributes
                                val currentBrightness = if (params != null && params.screenBrightness >= 0) params.screenBrightness else 0.5f
                                val newBrightness = (currentBrightness + delta).coerceIn(0.05f, 1.0f)
                                if (params != null) {
                                    params.screenBrightness = newBrightness
                                    window.attributes = params
                                }
                                gestureHudIcon = Icons.Default.BrightnessMedium
                                gestureHudText = "Brightness ${(newBrightness * 100).toInt()}%"
                                gestureHudProgress = newBrightness
                            } else {
                                // Volume & Boost Gesture (Right side)
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val currentBoost = audioEffectsState.volumeBoostPercent

                                if (delta > 0) {
                                    if (currentVol < maxVol) {
                                        val newVol = (currentVol + (delta * maxVol).toInt().coerceAtLeast(1)).coerceAtMost(maxVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        val percent = (newVol.toFloat() / maxVol.toFloat() * 100).toInt()
                                        gestureHudIcon = Icons.Default.VolumeUp
                                        gestureHudText = "Volume $percent%"
                                        gestureHudProgress = newVol.toFloat() / maxVol.toFloat()
                                    } else {
                                        // Increase volume boost 100% -> 200%
                                        val newBoost = (currentBoost + (delta * 100).toInt().coerceAtLeast(2)).coerceIn(100, 200)
                                        engine.audioEffectsManager.setVolumeBoostPercent(newBoost)
                                        gestureHudIcon = Icons.Default.VolumeUp
                                        gestureHudText = "Volume Boost $newBoost%"
                                        gestureHudProgress = (newBoost - 100) / 100f
                                    }
                                } else {
                                    if (currentBoost > 100) {
                                        val newBoost = (currentBoost + (delta * 100).toInt()).coerceIn(100, 200)
                                        engine.audioEffectsManager.setVolumeBoostPercent(newBoost)
                                        gestureHudIcon = Icons.Default.VolumeUp
                                        gestureHudText = "Volume Boost $newBoost%"
                                        gestureHudProgress = (newBoost - 100) / 100f
                                    } else {
                                        val newVol = (currentVol + (delta * maxVol).toInt()).coerceIn(0, maxVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        val percent = (newVol.toFloat() / maxVol.toFloat() * 100).toInt()
                                        gestureHudIcon = Icons.Default.VolumeUp
                                        gestureHudText = "Volume $percent%"
                                        gestureHudProgress = newVol.toFloat() / maxVol.toFloat()
                                    }
                                }
                            }
                        }
                    )
                }
        )

        // Reset Turbo Speed when touch ends
        LaunchedEffect(isTurboSpeedActive) {
            if (isTurboSpeedActive) {
                // Return to 1.0x or normal speed after 2.5s or tap
                delay(2500)
                isTurboSpeedActive = false
                engine.setPlaybackSpeed(1.0f)
                gestureHudText = null
            }
        }

        // Gesture HUD Overlay
        AnimatedVisibility(
            visible = gestureHudText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ImmersiveSurface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    gestureHudIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ImmersiveLavender,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = gestureHudText ?: "",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (gestureHudIcon == Icons.Default.BrightnessMedium || gestureHudIcon == Icons.Default.VolumeUp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { gestureHudProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(120.dp)
                                .height(4.dp),
                            color = ImmersiveLavender,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }

        // Buffering Indicator
        if (uiState.isBuffering) {
            CircularProgressIndicator(
                color = ImmersiveLavender,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )
        }

        // Lock Screen Floating Button
        if (uiState.isLocked) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                GlassIconButton(
                    icon = Icons.Default.Lock,
                    contentDescription = "Unlock Screen",
                    onClick = { engine.toggleLock() },
                    tint = CoralRed,
                    testTag = "unlock_screen_button"
                )
            }
        }

        // Full Controls Overlay (Top bar & Bottom bar)
        AnimatedVisibility(
            visible = controlsVisible && !uiState.isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PlayerScrimGradient)
            ) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            onClick = onNavigateBack,
                            testTag = "player_back_button"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.currentMedia?.title ?: "Playing Media",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GlassBadge(
                                    text = uiState.currentMedia?.resolutionLabel ?: "Video",
                                    backgroundColor = ImmersiveMediumPurple.copy(alpha = 0.4f),
                                    textColor = ImmersiveLavender
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (uiState.playbackSpeed != 1.0f) {
                                    GlassBadge(
                                        text = "${uiState.playbackSpeed}×",
                                        backgroundColor = ImmersiveDeepPurple.copy(alpha = 0.6f),
                                        textColor = ImmersiveLightPurple
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rotation toggle
                        GlassIconButton(
                            icon = Icons.Default.ScreenRotation,
                            contentDescription = "Rotate",
                            onClick = {
                                val current = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                activity?.requestedOrientation = if (current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Picture in Picture
                        GlassIconButton(
                            icon = Icons.Default.PictureInPicture,
                            contentDescription = "PiP",
                            onClick = onRequestPip,
                            testTag = "pip_button"
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Lock Button
                        GlassIconButton(
                            icon = Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            onClick = { engine.toggleLock() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // More Menu
                        Box {
                            GlassIconButton(
                                icon = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                onClick = { showMoreMenu = true },
                                testTag = "player_more_options"
                            )
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Media Technical Specs") },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showVideoInfo = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sleep Timer") },
                                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showSleepTimerDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bookmarks") },
                                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showBookmarksDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (uiState.abRepeat.isEnabled) "Clear A-B Repeat" else "Set A-B Repeat Loop") },
                                    leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        if (uiState.abRepeat.isEnabled) {
                                            engine.clearABRepeat()
                                        } else {
                                            engine.setPointA()
                                            gestureHudText = "Point A Set @ ${uiState.currentMedia?.formattedDuration}"
                                            coroutineScope.launch {
                                                delay(1000)
                                                gestureHudText = null
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // CENTER PLAY/PAUSE BIG CONTROLS
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // -10s Rewind
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = ImmersiveSurface.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder)
                    ) {
                        IconButton(onClick = { engine.seekBy(-10) }) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Main Play / Pause
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = ImmersiveMediumPurple,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ImmersiveLavender.copy(alpha = 0.5f)),
                        shadowElevation = 12.dp
                    ) {
                        IconButton(
                            onClick = { engine.togglePlayPause() },
                            modifier = Modifier.testTag("player_play_pause_center")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Play/Pause" else "Play",
                                tint = ImmersiveLightPurple,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    // +10s Fast Forward
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = ImmersiveSurface.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder)
                    ) {
                        IconButton(onClick = { engine.seekBy(10) }) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                // BOTTOM CONTROLS BAR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Seek Bar with Timestamps
                    val effectivePosition = if (isDraggingSeek) draggedSeekPositionMs else uiState.currentPositionMs
                    val posText = run {
                        val s = effectivePosition / 1000
                        val m = s / 60
                        val sec = s % 60
                        String.format("%02d:%02d", m, sec)
                    }
                    val durText = run {
                        val s = uiState.durationMs / 1000
                        val m = s / 60
                        val sec = s % 60
                        String.format("%02d:%02d", m, sec)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = posText,
                            color = ImmersiveLavender,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = durText,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }

                    Slider(
                        value = effectivePosition.toFloat(),
                        onValueChange = {
                            isDraggingSeek = true
                            draggedSeekPositionMs = it.toLong()
                        },
                        onValueChangeFinished = {
                            isDraggingSeek = false
                            engine.seekTo(draggedSeekPositionMs)
                        },
                        valueRange = 0f..uiState.durationMs.coerceAtLeast(1L).toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLavender,
                            activeTrackColor = ImmersiveLavender,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seekbar")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Secondary Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speed selector button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ImmersiveSurface.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder),
                                modifier = Modifier.clickable { showSpeedMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = ImmersiveLavender,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${uiState.playbackSpeed}×",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false }
                            ) {
                                val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f)
                                speeds.forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${speed}× Speed",
                                                fontWeight = if (uiState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.playbackSpeed == speed) ImmersiveLavender else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            engine.setPlaybackSpeed(speed)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Aspect Ratio Mode Cycle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ImmersiveSurface.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder),
                            modifier = Modifier.clickable {
                                val modes = AspectRatioMode.entries
                                val nextIndex = (uiState.aspectRatioMode.ordinal + 1) % modes.size
                                engine.setAspectRatioMode(modes[nextIndex])
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = null,
                                    tint = ImmersiveLightPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.aspectRatioMode.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Subtitle & Tracks sheet
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ImmersiveSurface.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder),
                            modifier = Modifier.clickable { showTrackSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = ImmersiveLavender,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tracks",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Equalizer & Audio Boost sheet
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (audioEffectsState.volumeBoostPercent > 100) ImmersiveMediumPurple.copy(alpha = 0.5f) else ImmersiveSurface.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (audioEffectsState.volumeBoostPercent > 100) ImmersiveLavender.copy(alpha = 0.6f) else ImmersiveGlassBorder),
                            modifier = Modifier.clickable { showAudioSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (audioEffectsState.volumeBoostPercent > 100) ImmersiveLavender else ImmersiveLightPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (audioEffectsState.volumeBoostPercent > 100) "${audioEffectsState.volumeBoostPercent}% Boost" else "Equalizer",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs & Bottom Sheets
        if (showAudioSheet) {
            AudioEqualizerSheet(
                effectsState = audioEffectsState,
                onVolumeBoostChanged = { engine.audioEffectsManager.setVolumeBoostPercent(it) },
                onEqualizerToggle = { engine.audioEffectsManager.setEqualizerEnabled(it) },
                onPresetSelected = { engine.audioEffectsManager.usePreset(it) },
                onBandLevelChanged = { band, lvl -> engine.audioEffectsManager.setBandLevel(band, lvl) },
                onBassBoostChanged = { en, str -> engine.audioEffectsManager.setBassBoost(en, str) },
                onVirtualizerChanged = { en, str -> engine.audioEffectsManager.setVirtualizer(en, str) },
                onBalanceChanged = { engine.audioEffectsManager.setBalance(it) },
                onDismiss = { showAudioSheet = false }
            )
        }

        if (showTrackSheet) {
            SubtitleAudioSheet(
                subtitleTracks = uiState.subtitleTracks,
                selectedSubtitleTrackId = uiState.selectedSubtitleTrackId,
                onSubtitleSelect = { engine.selectSubtitleTrack(it) },
                audioTracks = uiState.audioTracks,
                selectedAudioTrackId = uiState.selectedAudioTrackId,
                onAudioSelect = { engine.selectAudioTrack(it) },
                onLoadExternalSubtitle = {
                    subtitlePickerLauncher.launch(arrayOf("*/*"))
                },
                onDismiss = { showTrackSheet = false }
            )
        }

        if (showVideoInfo && uiState.metadataDetails != null) {
            VideoInfoBottomSheet(
                details = uiState.metadataDetails!!,
                onDismiss = { showVideoInfo = false }
            )
        }

        if (showSleepTimerDialog) {
            SleepTimerDialog(
                currentMinutesRemaining = uiState.sleepTimerSecondsRemaining,
                isActive = uiState.isSleepTimerActive,
                onSetTimer = { engine.startSleepTimer(it) },
                onCancelTimer = { engine.cancelSleepTimer() },
                onDismiss = { showSleepTimerDialog = false }
            )
        }

        if (showBookmarksDialog && uiState.currentMedia != null) {
            BookmarksDialog(
                bookmarks = bookmarks,
                currentPosMs = uiState.currentPositionMs,
                onJumpTo = { engine.seekTo(it) },
                onAddBookmark = { title ->
                    val app = context.applicationContext as? com.example.AetherApplication
                    app?.applicationScope?.launch {
                        app.mediaRepository.addBookmark(
                            uriString = uiState.currentMedia!!.uriString,
                            timestampMs = uiState.currentPositionMs,
                            title = title
                        )
                    }
                },
                onDeleteBookmark = { id ->
                    val app = context.applicationContext as? com.example.AetherApplication
                    app?.applicationScope?.launch {
                        app.mediaRepository.deleteBookmark(id)
                    }
                },
                onDismiss = { showBookmarksDialog = false }
            )
        }
    }
}
