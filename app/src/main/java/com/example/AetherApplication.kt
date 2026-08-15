package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.MediaRepository
import com.example.player.MediaPlaybackEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AetherApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val mediaRepository by lazy { MediaRepository(this, database, applicationScope) }
    val playbackEngine by lazy {
        MediaPlaybackEngine(this, applicationScope).apply {
            onPositionSaveRequested = { uri, pos, dur, speed, completed ->
                applicationScope.launch {
                    mediaRepository.savePlaybackPosition(
                        uriString = uri,
                        title = uiState.value.currentMedia?.title ?: "",
                        isVideo = uiState.value.currentMedia?.isVideo ?: true,
                        positionMs = pos,
                        durationMs = dur,
                        playbackSpeed = speed,
                        completed = completed
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
