package com.example

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.MediaItemData
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var currentThemeMode by mutableStateOf("Dark")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent if opened via file manager or external link
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme(themeMode = currentThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        currentTheme = currentThemeMode,
                        onThemeChanged = { currentThemeMode = it },
                        onRequestPip = { enterPipMode() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val dataUri: Uri? = intent.data

        if (Intent.ACTION_VIEW == action && dataUri != null) {
            val app = applicationContext as? AetherApplication ?: return
            val mimeType = intent.type ?: contentResolver.getType(dataUri) ?: "video/*"
            val isVideo = !mimeType.startsWith("audio/")

            val title = dataUri.lastPathSegment ?: "Media Stream"
            val externalMedia = MediaItemData(
                id = System.currentTimeMillis(),
                uriString = dataUri.toString(),
                title = title,
                displayName = title,
                durationMs = 0L,
                sizeBytes = 0L,
                mimeType = mimeType,
                dateAdded = System.currentTimeMillis() / 1000,
                dateModified = System.currentTimeMillis() / 1000,
                folderPath = "External",
                folderName = "External",
                isVideo = isVideo
            )

            app.playbackEngine.playMedia(externalMedia, 0L)
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val app = applicationContext as? AetherApplication
                val videoSize = app?.playbackEngine?.exoPlayer?.videoSize
                val width = videoSize?.width ?: 16
                val height = videoSize?.height ?: 9
                val aspectRatio = if (width > 0 && height > 0) {
                    val clampedWidth = width.coerceIn(1, 239)
                    val clampedHeight = height.coerceIn(1, 239)
                    Rational(clampedWidth, clampedHeight)
                } else {
                    Rational(16, 9)
                }

                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                // Fallback for PiP if ratio constraints fail
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val app = applicationContext as? AetherApplication
        val isPlaying = app?.playbackEngine?.uiState?.value?.isPlaying == true
        val isVideo = app?.playbackEngine?.uiState?.value?.currentMedia?.isVideo == true
        if (isPlaying && isVideo) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val app = applicationContext as? AetherApplication
        app?.playbackEngine?.setPipMode(isInPictureInPictureMode)
    }
}
