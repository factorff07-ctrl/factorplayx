package com.example.player

import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.AspectRatioMode
import com.example.model.MediaItemData
import com.example.model.MediaMetadataDetails
import com.example.model.TrackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class ABRepeatState(
    val isEnabled: Boolean = false,
    val pointAMs: Long? = null,
    val pointBMs: Long? = null
)

data class PlayerUiState(
    val currentMedia: MediaItemData? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isLocked: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val subtitleTracks: List<TrackInfo> = emptyList(),
    val audioTracks: List<TrackInfo> = emptyList(),
    val selectedSubtitleTrackId: String? = null,
    val selectedAudioTrackId: String? = null,
    val abRepeat: ABRepeatState = ABRepeatState(),
    val sleepTimerSecondsRemaining: Int = 0,
    val isSleepTimerActive: Boolean = false,
    val metadataDetails: MediaMetadataDetails? = null,
    val errorMessage: String? = null,
    val pipModeActive: Boolean = false
)

@OptIn(UnstableApi::class)
class MediaPlaybackEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    val audioEffectsManager: AudioEffectsManager = AudioEffectsManager()
) {
    private val TAG = "MediaPlaybackEngine"

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(10000)
        .setSeekForwardIncrementMs(10000)
        .build()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var positionTrackerJob: Job? = null
    private var sleepTimer: CountDownTimer? = null
    var onPositionSaveRequested: ((uriString: String, pos: Long, dur: Long, speed: Float, completed: Boolean) -> Unit)? = null

    init {
        setupPlayerListeners()
        startPositionTracker()
    }

    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _uiState.value = _uiState.value.copy(isBuffering = true, isEnded = false)
                    }
                    Player.STATE_READY -> {
                        val dur = exoPlayer.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(
                            isBuffering = false,
                            isEnded = false,
                            durationMs = dur
                        )
                        audioEffectsManager.attachSession(exoPlayer.audioSessionId)
                        extractMetadata()
                    }
                    Player.STATE_ENDED -> {
                        _uiState.value = _uiState.value.copy(isBuffering = false, isEnded = true)
                        saveCurrentState(completed = true)
                    }
                    Player.STATE_IDLE -> {
                        _uiState.value = _uiState.value.copy(isBuffering = false)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val reason = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "Hardware/Software Decoder Initialization Failed"
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> "Codec Unsupported or File Stream Corrupted"
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Media File Not Found or Access Revoked"
                    PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "Storage Permission Required to Play This File"
                    else -> error.localizedMessage ?: "Unknown Playback Error (${error.errorCodeName})"
                }
                Log.e(TAG, "ExoPlayer error: $reason", error)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unable to play file: $reason",
                    isBuffering = false
                )
            }

            override fun onTracksChanged(tracks: Tracks) {
                parseTracks(tracks)
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _uiState.value = _uiState.value.copy(playbackSpeed = playbackParameters.speed)
            }
        })
    }

    private fun startPositionTracker() {
        positionTrackerJob?.cancel()
        positionTrackerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    val buf = exoPlayer.bufferedPosition.coerceAtLeast(0L)

                    // Check A-B Repeat Loop
                    val ab = _uiState.value.abRepeat
                    if (ab.isEnabled && ab.pointBMs != null && pos >= ab.pointBMs) {
                        val loopStart = ab.pointAMs ?: 0L
                        exoPlayer.seekTo(loopStart)
                    }

                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = buf
                    )

                    // Auto periodic save
                    if (pos > 0 && dur > 0 && pos % 5000 < 500) {
                        saveCurrentState(completed = false)
                    }
                }
                delay(300)
            }
        }
    }

    fun playMedia(media: MediaItemData, startPositionMs: Long = 0L, initialSpeed: Float = 1.0f) {
        _uiState.value = _uiState.value.copy(
            currentMedia = media,
            errorMessage = null,
            isEnded = false,
            playbackSpeed = initialSpeed,
            abRepeat = ABRepeatState()
        )

        val mediaItem = MediaItem.Builder()
            .setUri(media.uri)
            .setMediaId(media.uriString)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(media.title)
                    .setArtist(media.artist)
                    .setAlbumTitle(media.album)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playbackParameters = PlaybackParameters(initialSpeed, 1.0f)
        exoPlayer.prepare()

        if (startPositionMs > 0L) {
            exoPlayer.seekTo(startPositionMs)
        }

        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            saveCurrentState(completed = false)
        } else {
            if (_uiState.value.isEnded) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
    }

    fun play() {
        if (_uiState.value.isEnded) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
        saveCurrentState(completed = false)
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
        _uiState.value = _uiState.value.copy(currentPositionMs = target)
    }

    fun seekBy(deltaSeconds: Int) {
        val current = exoPlayer.currentPosition
        val target = (current + deltaSeconds * 1000L).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 4.0f)
        exoPlayer.playbackParameters = PlaybackParameters(clamped, 1.0f)
        _uiState.value = _uiState.value.copy(playbackSpeed = clamped)
    }

    fun setAspectRatioMode(mode: AspectRatioMode) {
        _uiState.value = _uiState.value.copy(aspectRatioMode = mode)
    }

    fun toggleLock() {
        _uiState.value = _uiState.value.copy(isLocked = !_uiState.value.isLocked)
    }

    fun setLock(locked: Boolean) {
        _uiState.value = _uiState.value.copy(isLocked = locked)
    }

    fun setPipMode(active: Boolean) {
        _uiState.value = _uiState.value.copy(pipModeActive = active)
    }

    // A-B Repeat Controls
    fun setPointA() {
        val current = exoPlayer.currentPosition
        val currentAB = _uiState.value.abRepeat
        _uiState.value = _uiState.value.copy(
            abRepeat = currentAB.copy(pointAMs = current, isEnabled = currentAB.pointBMs != null && currentAB.pointBMs > current)
        )
    }

    fun setPointB() {
        val current = exoPlayer.currentPosition
        val currentAB = _uiState.value.abRepeat
        val pointA = currentAB.pointAMs ?: 0L
        if (current > pointA) {
            _uiState.value = _uiState.value.copy(
                abRepeat = currentAB.copy(pointBMs = current, isEnabled = true)
            )
        }
    }

    fun clearABRepeat() {
        _uiState.value = _uiState.value.copy(abRepeat = ABRepeatState())
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        val totalMs = minutes * 60 * 1000L
        _uiState.value = _uiState.value.copy(
            isSleepTimerActive = true,
            sleepTimerSecondsRemaining = minutes * 60
        )

        sleepTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    sleepTimerSecondsRemaining = (millisUntilFinished / 1000).toInt()
                )
            }

            override fun onFinish() {
                _uiState.value = _uiState.value.copy(
                    isSleepTimerActive = false,
                    sleepTimerSecondsRemaining = 0
                )
                pause()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
        _uiState.value = _uiState.value.copy(
            isSleepTimerActive = false,
            sleepTimerSecondsRemaining = 0
        )
    }

    // Subtitle & Audio Track Switching
    private fun parseTracks(tracks: Tracks) {
        val subTracks = mutableListOf<TrackInfo>()
        val audTracks = mutableListOf<TrackInfo>()

        var selectedSubId: String? = null
        var selectedAudId: String? = null

        for (group in tracks.groups) {
            val trackType = group.type
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val isSelected = group.isTrackSelected(i)
                val trackId = "${group.mediaTrackGroup.id}_$i"

                if (trackType == C.TRACK_TYPE_TEXT) {
                    val lang = format.language ?: "Undetermined"
                    val label = format.label ?: "Subtitle #$i ($lang)"
                    subTracks.add(
                        TrackInfo(
                            id = trackId,
                            index = i,
                            title = label,
                            language = lang,
                            mimeType = format.sampleMimeType,
                            isSelected = isSelected
                        )
                    )
                    if (isSelected) selectedSubId = trackId
                } else if (trackType == C.TRACK_TYPE_AUDIO) {
                    val lang = format.language ?: "Default Audio"
                    val label = format.label ?: "Audio Track #$i ($lang)"
                    audTracks.add(
                        TrackInfo(
                            id = trackId,
                            index = i,
                            title = label,
                            language = lang,
                            mimeType = format.sampleMimeType,
                            isSelected = isSelected,
                            channels = format.channelCount,
                            sampleRate = format.sampleRate,
                            bitrate = format.bitrate
                        )
                    )
                    if (isSelected) selectedAudId = trackId
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            subtitleTracks = subTracks,
            audioTracks = audTracks,
            selectedSubtitleTrackId = selectedSubId,
            selectedAudioTrackId = selectedAudId
        )
    }

    fun selectSubtitleTrack(trackInfo: TrackInfo?) {
        val trackParameters = exoPlayer.trackSelectionParameters.buildUpon()
        if (trackInfo == null) {
            // Disable subtitles
            trackParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
        } else {
            trackParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            val currentTracks = exoPlayer.currentTracks
            for (group in currentTracks.groups) {
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        val id = "${group.mediaTrackGroup.id}_$i"
                        if (id == trackInfo.id) {
                            trackParameters.setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                            )
                        }
                    }
                }
            }
        }
        exoPlayer.trackSelectionParameters = trackParameters.build()
        _uiState.value = _uiState.value.copy(selectedSubtitleTrackId = trackInfo?.id)
    }

    fun selectAudioTrack(trackInfo: TrackInfo) {
        val trackParameters = exoPlayer.trackSelectionParameters.buildUpon()
        val currentTracks = exoPlayer.currentTracks
        for (group in currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val id = "${group.mediaTrackGroup.id}_$i"
                    if (id == trackInfo.id) {
                        trackParameters.setOverrideForType(
                            TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                        )
                    }
                }
            }
        }
        exoPlayer.trackSelectionParameters = trackParameters.build()
        _uiState.value = _uiState.value.copy(selectedAudioTrackId = trackInfo.id)
    }

    fun addExternalSubtitle(uri: Uri, mimeType: String = MimeTypes.APPLICATION_SUBRIP, label: String = "External Subtitle") {
        val currentItem = exoPlayer.currentMediaItem ?: return
        val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mimeType)
            .setLanguage("en")
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .setLabel(label)
            .build()

        val updatedItem = currentItem.buildUpon()
            .setSubtitleConfigurations(listOf(subtitleConfig))
            .build()

        val currentPos = exoPlayer.currentPosition
        exoPlayer.setMediaItem(updatedItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(currentPos)
        exoPlayer.play()
    }

    private fun extractMetadata() {
        val media = _uiState.value.currentMedia ?: return
        val videoFormat = exoPlayer.videoFormat
        val audioFormat = exoPlayer.audioFormat

        val resolution = if (videoFormat != null && videoFormat.width > 0 && videoFormat.height > 0) {
            "${videoFormat.width} x ${videoFormat.height}"
        } else if (media.width > 0 && media.height > 0) {
            "${media.width} x ${media.height}"
        } else "N/A"

        val frameRate = videoFormat?.frameRate ?: 0f
        val vCodec = videoFormat?.sampleMimeType?.substringAfter("/")?.uppercase() ?: media.mimeType.substringAfter("/")
        val aCodec = audioFormat?.sampleMimeType?.substringAfter("/")?.uppercase() ?: "AAC / Auto"
        val channels = if (audioFormat != null && audioFormat.channelCount > 0) {
            if (audioFormat.channelCount == 1) "Mono (1.0)" else if (audioFormat.channelCount == 2) "Stereo (2.0)" else "${audioFormat.channelCount} Channels (Surround)"
        } else "Stereo (2.0)"
        val sampleRate = if (audioFormat != null && audioFormat.sampleRate > 0) "${audioFormat.sampleRate / 1000.0} kHz" else "44.1 kHz"
        val bitrate = if (videoFormat != null && videoFormat.bitrate > 0) "${videoFormat.bitrate / 1000} kbps" else "Variable"

        val details = MediaMetadataDetails(
            fileName = media.displayName,
            path = media.folderPath,
            fileSizeFormatted = media.formattedSize,
            durationFormatted = media.formattedDuration,
            resolution = resolution,
            frameRate = frameRate,
            videoCodec = vCodec,
            audioCodec = aCodec,
            audioChannels = channels,
            sampleRate = sampleRate,
            bitrateFormatted = bitrate,
            mimeType = media.mimeType
        )

        _uiState.value = _uiState.value.copy(metadataDetails = details)
    }

    fun saveCurrentState(completed: Boolean = false) {
        val media = _uiState.value.currentMedia ?: return
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val dur = exoPlayer.duration.coerceAtLeast(0L)
        val speed = exoPlayer.playbackParameters.speed
        onPositionSaveRequested?.invoke(media.uriString, pos, dur, speed, completed)
    }

    fun release() {
        saveCurrentState(completed = false)
        cancelSleepTimer()
        positionTrackerJob?.cancel()
        audioEffectsManager.releaseEffects()
        exoPlayer.release()
    }
}
