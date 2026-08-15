package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.FolderConfigEntity
import com.example.data.local.entity.PlaybackStateEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import com.example.data.scanner.MediaStoreScanner
import com.example.model.FolderItem
import com.example.model.MediaItemData
import com.example.model.SmartCategory
import com.example.model.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val scanner = MediaStoreScanner(context)
    private val playbackDao = database.playbackStateDao()
    private val playlistDao = database.playlistDao()
    private val bookmarkDao = database.bookmarkDao()
    private val favoriteDao = database.favoriteDao()
    private val folderConfigDao = database.folderConfigDao()

    private val _rawMediaList = MutableStateFlow<List<MediaItemData>>(emptyList())
    val rawMediaList: StateFlow<List<MediaItemData>> = _rawMediaList.asStateFlow()

    private val _rawFolderList = MutableStateFlow<List<FolderItem>>(emptyList())
    val rawFolderList: StateFlow<List<FolderItem>> = _rawFolderList.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0 to 0)
    val scanProgress: StateFlow<Pair<Int, Int>> = _scanProgress.asStateFlow()

    val allPlaybackStates: Flow<List<PlaybackStateEntity>> = playbackDao.getAllPlaybackStates()
    val continueWatchingStates: Flow<List<PlaybackStateEntity>> = playbackDao.getContinueWatching()
    val recentlyPlayedStates: Flow<List<PlaybackStateEntity>> = playbackDao.getRecentlyPlayed()
    val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val allFolderConfigs: Flow<List<FolderConfigEntity>> = folderConfigDao.getAllFolderConfigs()

    // Enriched media items with favorite & playback state info
    val enrichedMediaList: StateFlow<List<MediaItemData>> = combine(
        _rawMediaList,
        playbackDao.getAllPlaybackStates(),
        favoriteDao.getAllFavorites()
    ) { mediaList, states, favorites ->
        val stateMap = states.associateBy { it.mediaUriString }
        val favoriteSet = favorites.map { it.mediaUriString }.toSet()

        mediaList.map { item ->
            val state = stateMap[item.uriString]
            val isFav = favoriteSet.contains(item.uriString)
            item.copy(
                lastPositionMs = state?.positionMs ?: 0L,
                completed = state?.completed ?: false,
                isFavorite = isFav
            )
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    val continueWatchingItems: StateFlow<List<MediaItemData>> = combine(
        enrichedMediaList,
        playbackDao.getContinueWatching()
    ) { mediaList, states ->
        val uriMap = mediaList.associateBy { it.uriString }
        states.mapNotNull { state ->
            uriMap[state.mediaUriString]?.copy(
                lastPositionMs = state.positionMs,
                completed = state.completed
            )
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    val favoriteItems: StateFlow<List<MediaItemData>> = combine(
        enrichedMediaList,
        favoriteDao.getAllFavorites()
    ) { mediaList, favorites ->
        val favSet = favorites.map { it.mediaUriString }.toSet()
        mediaList.filter { favSet.contains(it.uriString) }
    }.flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    init {
        refreshMedia()
    }

    fun refreshMedia() {
        scope.launch(Dispatchers.IO) {
            if (_isScanning.value) return@launch
            _isScanning.value = true
            try {
                val excludedConfigs = folderConfigDao.getAllFolderConfigs().first()
                val excludedPaths = excludedConfigs.filter { it.isExcluded }.map { it.folderPath }.toSet()

                val (media, folders) = scanner.scanAllMedia(
                    excludedFolders = excludedPaths,
                    onProgress = { scanned, total ->
                        _scanProgress.value = scanned to total
                    }
                )
                _rawMediaList.value = media
                _rawFolderList.value = folders
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    suspend fun savePlaybackPosition(
        uriString: String,
        title: String,
        isVideo: Boolean,
        positionMs: Long,
        durationMs: Long,
        playbackSpeed: Float = 1.0f,
        completed: Boolean = false
    ) = withContext(Dispatchers.IO) {
        playbackDao.insertOrUpdate(
            PlaybackStateEntity(
                mediaUriString = uriString,
                title = title,
                isVideo = isVideo,
                positionMs = positionMs,
                durationMs = durationMs,
                completed = completed,
                playbackSpeed = playbackSpeed,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getPlaybackPosition(uriString: String): PlaybackStateEntity? = withContext(Dispatchers.IO) {
        playbackDao.getPlaybackState(uriString)
    }

    suspend fun toggleFavorite(item: MediaItemData) = withContext(Dispatchers.IO) {
        if (item.isFavorite) {
            favoriteDao.removeFavorite(item.uriString)
        } else {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    mediaUriString = item.uriString,
                    title = item.title,
                    isVideo = item.isVideo
                )
            )
        }
    }

    fun isItemFavorite(uriString: String): Flow<Boolean> = favoriteDao.isFavorite(uriString)

    // Bookmarks
    fun getBookmarksForMedia(uriString: String): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksForMedia(uriString)

    suspend fun addBookmark(uriString: String, timestampMs: Long, title: String, note: String = "") = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                mediaUriString = uriString,
                timestampMs = timestampMs,
                title = title,
                note = note
            )
        )
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(id)
    }

    // Playlists
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
    }

    suspend fun renamePlaylist(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(id)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(name = newName))
        }
    }

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = playlistDao.getPlaylistItems(playlistId)

    suspend fun addToPlaylist(playlistId: Long, item: MediaItemData) = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                mediaUriString = item.uriString,
                title = item.title,
                durationMs = item.durationMs,
                isVideo = item.isVideo
            )
        )
    }

    suspend fun removeFromPlaylist(itemId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistItem(itemId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        playbackDao.clearHistory()
    }

    suspend fun toggleFolderExclusion(folderPath: String, isExcluded: Boolean) = withContext(Dispatchers.IO) {
        folderConfigDao.insertFolderConfig(
            FolderConfigEntity(
                folderPath = folderPath,
                isExcluded = isExcluded
            )
        )
        refreshMedia()
    }

    // Search and Filtering helper
    fun filterAndSortMedia(
        items: List<MediaItemData>,
        query: String,
        category: SmartCategory,
        sortOption: SortOption,
        isAscending: Boolean,
        folderPathFilter: String? = null
    ): List<MediaItemData> {
        var result = items

        if (folderPathFilter != null) {
            result = result.filter { it.folderPath == folderPathFilter }
        }

        result = when (category) {
            SmartCategory.ALL_VIDEOS -> result.filter { it.isVideo }
            SmartCategory.MOVIES -> result.filter { it.isVideo && it.durationMs > 20 * 60 * 1000 }
            SmartCategory.SHORT_VIDEOS -> result.filter { it.isVideo && it.durationMs in 1..60000 }
            SmartCategory.LARGE_FILES -> result.filter { it.sizeBytes > 500 * 1024 * 1024 }
            SmartCategory.UNWATCHED -> result.filter { it.isVideo && it.lastPositionMs == 0L }
            SmartCategory.PARTIALLY_WATCHED -> result.filter { it.isVideo && it.lastPositionMs > 5000 && !it.completed }
            SmartCategory.ALL_AUDIO -> result.filter { !it.isVideo }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.displayName.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.folderName.lowercase().contains(q)
            }
        }

        result = when (sortOption) {
            SortOption.NAME -> if (isAscending) result.sortedBy { it.title.lowercase() } else result.sortedByDescending { it.title.lowercase() }
            SortOption.DATE_ADDED -> if (isAscending) result.sortedBy { it.dateAdded } else result.sortedByDescending { it.dateAdded }
            SortOption.DATE_MODIFIED -> if (isAscending) result.sortedBy { it.dateModified } else result.sortedByDescending { it.dateModified }
            SortOption.DURATION -> if (isAscending) result.sortedBy { it.durationMs } else result.sortedByDescending { it.durationMs }
            SortOption.SIZE -> if (isAscending) result.sortedBy { it.sizeBytes } else result.sortedByDescending { it.sizeBytes }
            SortOption.RESOLUTION -> if (isAscending) result.sortedBy { it.width * it.height } else result.sortedByDescending { it.width * it.height }
        }

        return result
    }
}
