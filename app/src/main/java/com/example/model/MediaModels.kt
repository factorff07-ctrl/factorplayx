package com.example.model

import android.net.Uri

data class MediaItemData(
    val id: Long,
    val uriString: String,
    val title: String,
    val displayName: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "",
    val isVideo: Boolean = true,
    val folderName: String = "",
    val folderPath: String = "",
    val lastPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val bookmarkCount: Int = 0,
    val completed: Boolean = false
) {
    val uri: Uri get() = Uri.parse(uriString)

    val resolutionLabel: String
        get() = when {
            width >= 3840 || height >= 2160 -> "4K UHD"
            width >= 2560 || height >= 1440 -> "2K QHD"
            width >= 1920 || height >= 1080 -> "1080p FHD"
            width >= 1280 || height >= 720 -> "720p HD"
            width > 0 && height > 0 -> "${width}x${height}"
            else -> if (isVideo) "Video" else "Audio"
        }

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val progressFraction: Float
        get() = if (durationMs > 0) (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingDurationFormatted: String
        get() {
            val remainingMs = (durationMs - lastPositionMs).coerceAtLeast(0L)
            val totalSeconds = remainingMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d left", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d left", minutes, seconds)
            }
        }
}

data class FolderItem(
    val path: String,
    val name: String,
    val mediaCount: Int,
    val videoCount: Int,
    val audioCount: Int,
    val representativeUri: String? = null,
    val totalSizeBytes: Long = 0L,
    val isExcluded: Boolean = false
) {
    val formattedTotalSize: String
        get() {
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return if (gb >= 1.0) String.format("%.2f GB", gb) else String.format("%.1f MB", mb)
        }
}

data class TrackInfo(
    val id: String,
    val index: Int,
    val title: String,
    val language: String?,
    val mimeType: String?,
    val isSelected: Boolean = false,
    val channels: Int = 0,
    val sampleRate: Int = 0,
    val bitrate: Int = 0
)

data class MediaMetadataDetails(
    val fileName: String,
    val path: String,
    val fileSizeFormatted: String,
    val durationFormatted: String,
    val resolution: String,
    val frameRate: Float = 0f,
    val videoCodec: String = "Auto Detect",
    val audioCodec: String = "Auto Detect",
    val audioChannels: String = "Stereo (2.0)",
    val sampleRate: String = "44.1 kHz",
    val bitrateFormatted: String = "Unknown",
    val mimeType: String = "",
    val dateModifiedFormatted: String = ""
)

enum class AspectRatioMode(val displayName: String) {
    FIT("Fit Screen"),
    FILL("Fill / Crop"),
    ZOOM("Zoom 100%"),
    STRETCH("Stretch"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3")
}

enum class SortOption(val displayName: String) {
    NAME("Name"),
    DATE_ADDED("Date Added"),
    DATE_MODIFIED("Date Modified"),
    DURATION("Duration"),
    SIZE("File Size"),
    RESOLUTION("Resolution")
}

enum class SmartCategory(val title: String, val iconName: String) {
    ALL_VIDEOS("All Videos", "VideoLibrary"),
    MOVIES("Movies & Long (>20m)", "Movie"),
    SHORT_VIDEOS("Short Videos (<1m)", "Bolt"),
    LARGE_FILES("Large Files (>500M)", "SdCard"),
    UNWATCHED("Unwatched", "VisibilityOff"),
    PARTIALLY_WATCHED("Continue Watching", "History"),
    ALL_AUDIO("Audio & Music", "Audiotrack")
}
