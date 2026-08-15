package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.FolderConfigEntity
import com.example.data.local.entity.PlaybackStateEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackStateDao {
    @Query("SELECT * FROM playback_states ORDER BY lastPlayedTimestamp DESC")
    fun getAllPlaybackStates(): Flow<List<PlaybackStateEntity>>

    @Query("SELECT * FROM playback_states WHERE completed = 0 AND positionMs > 5000 AND (durationMs - positionMs) > 10000 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getContinueWatching(): Flow<List<PlaybackStateEntity>>

    @Query("SELECT * FROM playback_states ORDER BY lastPlayedTimestamp DESC LIMIT 30")
    fun getRecentlyPlayed(): Flow<List<PlaybackStateEntity>>

    @Query("SELECT * FROM playback_states WHERE mediaUriString = :uriString LIMIT 1")
    suspend fun getPlaybackState(uriString: String): PlaybackStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: PlaybackStateEntity)

    @Query("DELETE FROM playback_states WHERE mediaUriString = :uriString")
    suspend fun deleteState(uriString: String)

    @Query("DELETE FROM playback_states")
    suspend fun clearHistory()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long

    @Query("DELETE FROM playlist_items WHERE id = :itemId")
    suspend fun deletePlaylistItem(itemId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaUriString = :uriString")
    suspend fun deletePlaylistItemByUri(playlistId: Long, uriString: String)

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun getPlaylistItemCount(playlistId: Long): Flow<Int>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE mediaUriString = :mediaUriString ORDER BY timestampMs ASC")
    fun getBookmarksForMedia(mediaUriString: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("UPDATE bookmarks SET title = :newTitle, note = :newNote WHERE id = :id")
    suspend fun updateBookmark(id: Long, newTitle: String, newNote: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaUriString = :uriString)")
    fun isFavorite(uriString: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaUriString = :uriString")
    suspend fun removeFavorite(uriString: String)
}

@Dao
interface FolderConfigDao {
    @Query("SELECT * FROM folder_configs")
    fun getAllFolderConfigs(): Flow<List<FolderConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderConfig(config: FolderConfigEntity)

    @Query("UPDATE folder_configs SET isExcluded = :isExcluded WHERE folderPath = :folderPath")
    suspend fun setFolderExcluded(folderPath: String, isExcluded: Boolean)

    @Query("DELETE FROM folder_configs WHERE folderPath = :folderPath")
    suspend fun deleteFolderConfig(folderPath: String)
}
