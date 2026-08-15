package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playback_states")
data class PlaybackStateEntity(
    @PrimaryKey val mediaUriString: String,
    val title: String,
    val isVideo: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"]), Index(value = ["playlistId", "mediaUriString"], unique = true)]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val mediaUriString: String,
    val title: String,
    val durationMs: Long = 0L,
    val isVideo: Boolean = true,
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaUriString: String,
    val timestampMs: Long,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaUriString: String,
    val title: String,
    val isVideo: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folder_configs")
data class FolderConfigEntity(
    @PrimaryKey val folderPath: String,
    val isExcluded: Boolean = false,
    val customName: String? = null
)
