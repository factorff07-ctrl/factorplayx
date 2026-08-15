package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.MediaRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderSubtle
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersiveSurface
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    repository: MediaRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var resumePlayback by remember { mutableStateOf(true) }
    var autoPlayNext by remember { mutableStateOf(true) }
    var gestureBrightness by remember { mutableStateOf(true) }
    var gestureVolume by remember { mutableStateOf(true) }
    var gestureSeek by remember { mutableStateOf(true) }
    var defaultSeekSeconds by remember { mutableIntStateOf(10) }
    var subtitleSizeSp by remember { mutableFloatStateOf(16f) }

    val openSocialLink: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        item {
            Column {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Customize playback, audio amplification and visual theme",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // OWNER & DEVELOPER SECTION (COMPACT & MINIMAL)
        item {
            SettingsCategoryHeader(title = "Owner & Developer", icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_developer_card"),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.85f),
                borderColor = ImmersiveGlassBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Photo
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.5.dp, ImmersiveLavender.copy(alpha = 0.8f)), CircleShape)
                            .background(ImmersiveDeepPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.dev_profile,
                            contentDescription = "Vikash - Owner & Developer profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Profile Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Vikash",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Owner & Developer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmersiveLavender
                        )
                    }

                    // Social Links Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instagram
                        IconButton(
                            onClick = { openSocialLink("https://www.instagram.com/py.vikash") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("social_instagram"),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = ImmersiveMediumPurple.copy(alpha = 0.25f)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_social_instagram),
                                contentDescription = "Instagram profile of Vikash",
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // GitHub
                        IconButton(
                            onClick = { openSocialLink("https://github.com/vjjgkoihai-a11y") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("social_github"),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = ImmersiveMediumPurple.copy(alpha = 0.25f)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_social_github),
                                contentDescription = "GitHub profile of Vikash",
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // X (Twitter)
                        IconButton(
                            onClick = { openSocialLink("https://x.com/pyvikash") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("social_x"),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = ImmersiveMediumPurple.copy(alpha = 0.25f)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_social_x),
                                contentDescription = "X profile of Vikash",
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // APPEARANCE & THEME SECTION
        item {
            SettingsCategoryHeader(title = "Appearance & Visuals", icon = Icons.Default.Palette)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Display Theme Mode",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Dark", "AMOLED", "Light", "System").forEach { theme ->
                            GlassChip(
                                text = theme,
                                isSelected = currentTheme == theme,
                                onClick = { onThemeChanged(theme) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AMOLED mode uses pure pitch-black surfaces for enhanced battery savings on OLED screens.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // PLAYBACK SETTINGS
        item {
            SettingsCategoryHeader(title = "Playback & Controls", icon = Icons.Default.PlayCircleOutline)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Smart Resume Playback",
                        subtitle = "Remember playback position and offer 1-tap resume",
                        checked = resumePlayback,
                        onCheckedChange = { resumePlayback = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = "Auto-Play Next in Playlist",
                        subtitle = "Automatically queue next video upon finish",
                        checked = autoPlayNext,
                        onCheckedChange = { autoPlayNext = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Double Tap Seek Interval",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { sec ->
                            GlassChip(
                                text = "${sec}s",
                                isSelected = defaultSeekSeconds == sec,
                                onClick = { defaultSeekSeconds = sec }
                            )
                        }
                    }
                }
            }
        }

        // GESTURE CONTROLS
        item {
            SettingsCategoryHeader(title = "Gestures & HUD", icon = Icons.Default.Speed)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Left Swipe Brightness Control",
                        subtitle = "Vertical swipe on left edge modifies screen brightness",
                        checked = gestureBrightness,
                        onCheckedChange = { gestureBrightness = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsSwitchRow(
                        title = "Right Swipe Volume & 200% Boost",
                        subtitle = "Vertical swipe on right edge controls sound up to 200%",
                        checked = gestureVolume,
                        onCheckedChange = { gestureVolume = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsSwitchRow(
                        title = "Horizontal Seek Swipe",
                        subtitle = "Swipe horizontally to preview precise timestamp seek",
                        checked = gestureSeek,
                        onCheckedChange = { gestureSeek = it }
                    )
                }
            }
        }

        // AUDIO AMPLIFICATION & EFFECT SPECS
        item {
            SettingsCategoryHeader(title = "Audio Engine Specs", icon = Icons.Default.VolumeUp)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveLavender.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "200% Software Loudness Booster",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ImmersiveLavender
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Provides up to +20.0 dB software gain using Android LoudnessEnhancer with dynamic range compression to protect speakers from clipping distortion.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supported Codecs: MP3, AAC, FLAC, WAV, OGG, Opus, M4A, ALAC.",
                        fontSize = 12.sp,
                        color = ImmersiveLightPurple
                    )
                }
            }
        }

        // PRIVACY & STORAGE MAINTENANCE
        item {
            SettingsCategoryHeader(title = "Storage & Privacy", icon = Icons.Default.Security)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            repository.refreshMedia()
                            Toast.makeText(context, "Rescanning storage...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ImmersiveLavender)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Media Library Rescan", color = ImmersiveLavender, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                repository.clearHistory()
                                Toast.makeText(context, "Playback history cleared", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CoralRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Playback History", color = CoralRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ABOUT APP & CODEC COMPLIANCE
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.6f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FactorPlayX Pro v1.0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ImmersiveLavender
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AndroidX Media3 ExoPlayer Engine • Local-First Architecture • Hardware Acceleration",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ImmersiveLavender,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ImmersiveLavender,
                checkedTrackColor = ImmersiveMediumPurple.copy(alpha = 0.6f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}
