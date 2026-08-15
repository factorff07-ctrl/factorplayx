package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.BookmarkEntity
import com.example.model.MediaMetadataDetails
import com.example.model.TrackInfo
import com.example.player.AudioEffectsState
import com.example.player.EqualizerBand
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersiveLightPurple
import com.example.ui.theme.ImmersiveMediumPurple
import com.example.ui.theme.ImmersiveSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEqualizerSheet(
    effectsState: AudioEffectsState,
    onVolumeBoostChanged: (Int) -> Unit,
    onEqualizerToggle: (Boolean) -> Unit,
    onPresetSelected: (String) -> Unit,
    onBandLevelChanged: (Short, Short) -> Unit,
    onBassBoostChanged: (Boolean, Int) -> Unit,
    onVirtualizerChanged: (Boolean, Int) -> Unit,
    onBalanceChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ImmersiveSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = ImmersiveLavender,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Audio & Equalizer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 200% Volume Boost Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = if (effectsState.volumeBoostPercent > 100) ImmersiveLavender.copy(alpha = 0.6f) else ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (effectsState.volumeBoostPercent > 100) ImmersiveLavender else ImmersiveLightPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Software Volume Boost",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${effectsState.volumeBoostPercent}%",
                            fontWeight = FontWeight.Bold,
                            color = if (effectsState.volumeBoostPercent > 100) ImmersiveLavender else ImmersiveLightPurple,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = effectsState.volumeBoostPercent.toFloat(),
                        onValueChange = { onVolumeBoostChanged(it.toInt()) },
                        valueRange = 100f..200f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLavender,
                            activeTrackColor = ImmersiveLavender,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("volume_boost_slider")
                    )

                    if (effectsState.volumeBoostPercent > 100) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gain above 100% uses software amplification. Limiter enabled.",
                                fontSize = 11.sp,
                                color = AmberGold
                            )
                        }
                    }

                    if (effectsState.volumeBoostPercent != 100) {
                        TextButton(
                            onClick = { onVolumeBoostChanged(100) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset to 100%", fontSize = 12.sp, color = ImmersiveLavender)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equalizer Toggle & Presets
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multi-Band Equalizer",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Switch(
                            checked = effectsState.isEqualizerEnabled,
                            onCheckedChange = onEqualizerToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ImmersiveLavender,
                                checkedTrackColor = ImmersiveMediumPurple.copy(alpha = 0.6f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    if (effectsState.isEqualizerEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(effectsState.presets) { preset ->
                                GlassChip(
                                    text = preset,
                                    isSelected = effectsState.currentPreset.equals(preset, ignoreCase = true),
                                    onClick = { onPresetSelected(preset) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Equalizer Bands Sliders
                        if (effectsState.bands.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                effectsState.bands.forEach { band ->
                                    BandControl(
                                        band = band,
                                        onLevelChanged = { lvl ->
                                            onBandLevelChanged(band.index, lvl)
                                        }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Hardware equalizer not available on this stream/device.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost & Virtualizer & Balance
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ImmersiveSurface.copy(alpha = 0.8f),
                borderColor = ImmersiveGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Acoustic Effects & Balance",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bass Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Bass Boost", fontSize = 14.sp, color = Color.White)
                        Text(
                            text = "${(effectsState.bassBoostStrength / 10)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveLavender
                        )
                    }
                    Slider(
                        value = effectsState.bassBoostStrength.toFloat(),
                        onValueChange = { onBassBoostChanged(it > 0, it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLavender,
                            activeTrackColor = ImmersiveLavender,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Virtualizer / 3D Surround
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Virtualizer / 3D Audio", fontSize = 14.sp, color = Color.White)
                        Text(
                            text = "${(effectsState.virtualizerStrength / 10)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveLightPurple
                        )
                    }
                    Slider(
                        value = effectsState.virtualizerStrength.toFloat(),
                        onValueChange = { onVirtualizerChanged(it > 0, it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLightPurple,
                            activeTrackColor = ImmersiveLightPurple,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Audio Balance (L / R)", fontSize = 14.sp, color = Color.White)
                        val balanceLabel = when {
                            effectsState.balance < -0.1f -> "L ${(-effectsState.balance * 100).toInt()}%"
                            effectsState.balance > 0.1f -> "R ${(effectsState.balance * 100).toInt()}%"
                            else -> "Center"
                        }
                        Text(text = balanceLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ImmersiveLavender)
                    }
                    Slider(
                        value = effectsState.balance,
                        onValueChange = onBalanceChanged,
                        valueRange = -1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersiveLavender,
                            activeTrackColor = ImmersiveLavender,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BandControl(
    band: EqualizerBand,
    onLevelChanged: (Short) -> Unit
) {
    val range = (band.maxLevel - band.minLevel).toFloat()
    val normValue = if (range > 0) ((band.currentLevel - band.minLevel) / range).coerceIn(0f, 1f) else 0.5f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(54.dp)
    ) {
        val freqLabel = if (band.centerFreqHz >= 1000) {
            "${band.centerFreqHz / 1000}k"
        } else {
            "${band.centerFreqHz}"
        }
        val db = band.currentLevel / 100

        Text(
            text = if (db > 0) "+$db dB" else "$db dB",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (db != 0) ImmersiveLavender else Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = band.currentLevel.toFloat(),
            onValueChange = { onLevelChanged(it.toInt().toShort()) },
            valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = ImmersiveLavender,
                activeTrackColor = ImmersiveLavender,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.height(120.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleAudioSheet(
    subtitleTracks: List<TrackInfo>,
    selectedSubtitleTrackId: String?,
    onSubtitleSelect: (TrackInfo?) -> Unit,
    audioTracks: List<TrackInfo>,
    selectedAudioTrackId: String?,
    onAudioSelect: (TrackInfo) -> Unit,
    onLoadExternalSubtitle: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = ImmersiveSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracks & Subtitles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Subtitles, contentDescription = null, tint = ImmersiveLavender)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Subtitles", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Button(
                    onClick = onLoadExternalSubtitle,
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ImmersiveLavender, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load SRT/VTT", fontSize = 12.sp, color = ImmersiveLavender, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option: Subtitles Off
            TrackOptionRow(
                title = "Subtitles Off",
                subtitle = "Disable subtitles",
                isSelected = selectedSubtitleTrackId == null,
                onClick = { onSubtitleSelect(null) }
            )

            subtitleTracks.forEach { track ->
                TrackOptionRow(
                    title = track.title,
                    subtitle = track.language ?: "Language undetermined",
                    isSelected = track.id == selectedSubtitleTrackId,
                    onClick = { onSubtitleSelect(track) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio Tracks Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = ImmersiveLightPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Audio Stream / Language", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (audioTracks.isEmpty()) {
                Text(
                    text = "Default Audio Stream (Single track)",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            } else {
                audioTracks.forEach { track ->
                    val extraInfo = buildString {
                        if (track.language != null) append("${track.language} • ")
                        if (track.channels > 0) append("${track.channels}ch • ")
                        if (track.sampleRate > 0) append("${track.sampleRate / 1000}kHz")
                    }
                    TrackOptionRow(
                        title = track.title,
                        subtitle = extraInfo.ifEmpty { "Default Stream" },
                        isSelected = track.id == selectedAudioTrackId,
                        onClick = { onAudioSelect(track) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TrackOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) ImmersiveMediumPurple.copy(alpha = 0.35f) else ImmersiveSurface.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ImmersiveLavender.copy(alpha = 0.5f) else ImmersiveGlassBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ImmersiveLavender else Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = ImmersiveLavender,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInfoBottomSheet(
    details: MediaMetadataDetails,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = ImmersiveSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ImmersiveLavender)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Media Technical Specs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoItemRow("File Name", details.fileName)
            InfoItemRow("Folder Path", details.path)
            InfoItemRow("Resolution", details.resolution)
            if (details.frameRate > 0) {
                InfoItemRow("Frame Rate", String.format("%.2f FPS", details.frameRate))
            }
            InfoItemRow("Duration", details.durationFormatted)
            InfoItemRow("File Size", details.fileSizeFormatted)
            InfoItemRow("Video Codec", details.videoCodec)
            InfoItemRow("Audio Codec", details.audioCodec)
            InfoItemRow("Audio Channels", details.audioChannels)
            InfoItemRow("Audio Sample Rate", details.sampleRate)
            InfoItemRow("Bitrate", details.bitrateFormatted)
            InfoItemRow("MIME Type", details.mimeType)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoItemRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun SleepTimerDialog(
    currentMinutesRemaining: Int,
    isActive: Boolean,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(10, 20, 30, 45, 60, 90)

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ImmersiveSurface,
            borderColor = ImmersiveGlassBorder
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = ImmersiveLavender)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sleep Timer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isActive) {
                    val mins = currentMinutesRemaining / 60
                    val secs = currentMinutesRemaining % 60
                    Text(
                        text = String.format("Playback will pause in %02d:%02d", mins, secs),
                        color = ImmersiveLavender,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Turn Off Sleep Timer")
                    }
                } else {
                    Text(
                        text = "Choose duration to stop playback:",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    presets.chunked(3).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { mins ->
                                Button(
                                    onClick = {
                                        onSetTimer(mins)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ImmersiveMediumPurple.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("$mins min", fontSize = 12.sp, color = ImmersiveLavender, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = ImmersiveLavender)
                }
            }
        }
    }
}

@Composable
fun BookmarksDialog(
    bookmarks: List<BookmarkEntity>,
    currentPosMs: Long,
    onJumpTo: (Long) -> Unit,
    onAddBookmark: (title: String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf("") }

    val formattedCurrent = run {
        val totalSec = currentPosMs / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        String.format("%02d:%02d", m, s)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ImmersiveSurface,
            borderColor = ImmersiveGlassBorder
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = ImmersiveLavender)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Scene Bookmarks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add at current position
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("Add marker at $formattedCurrent", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImmersiveLavender,
                            unfocusedBorderColor = ImmersiveGlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val title = newTitle.ifBlank { "Scene @ $formattedCurrent" }
                            onAddBookmark(title)
                            newTitle = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveMediumPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add", fontSize = 12.sp, color = ImmersiveLightPurple, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (bookmarks.isEmpty()) {
                    Text(
                        text = "No saved bookmarks for this media.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(bookmarks) { bm ->
                            val totalSec = bm.timestampMs / 1000
                            val m = totalSec / 60
                            val s = totalSec % 60
                            val ts = String.format("%02d:%02d", m, s)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ImmersiveSurface.copy(alpha = 0.6f))
                                    .clickable {
                                        onJumpTo(bm.timestampMs)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = bm.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                                    Text(text = ts, fontSize = 12.sp, color = ImmersiveLavender, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { onDeleteBookmark(bm.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
