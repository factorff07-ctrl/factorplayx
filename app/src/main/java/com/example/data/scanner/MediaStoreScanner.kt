package com.example.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.FolderItem
import com.example.model.MediaItemData
import com.example.model.SmartCategory
import com.example.model.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun scanAllMedia(
        excludedFolders: Set<String> = emptySet(),
        onProgress: ((scanned: Int, total: Int) -> Unit)? = null
    ): Pair<List<MediaItemData>, List<FolderItem>> = withContext(Dispatchers.IO) {
        val videos = scanVideos(excludedFolders)
        val audios = scanAudio(excludedFolders)
        val allMedia = (videos + audios).sortedByDescending { it.dateModified }
        onProgress?.invoke(allMedia.size, allMedia.size)

        val folders = extractFolders(allMedia, excludedFolders)
        Pair(allMedia, folders)
    }

    private fun scanVideos(excludedFolders: Set<String>): List<MediaItemData> {
        val mediaList = mutableListOf<MediaItemData>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        try {
            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val displayCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Video.Media.ALBUM)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val displayName = if (displayCol != -1) cursor.getString(displayCol) ?: "Unknown" else "Unknown"
                    val title = if (titleCol != -1) cursor.getString(titleCol) ?: displayName else displayName
                    val artist = if (artistCol != -1) cursor.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumCol != -1) cursor.getString(albumCol) ?: "Unknown Album" else "Unknown Album"
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol != -1) cursor.getLong(dateAddedCol) else 0L
                    val dateMod = if (dateModCol != -1) cursor.getLong(dateModCol) else 0L
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 0
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 0
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "video/*" else "video/*"
                    val rawPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""

                    val folderPath = if (rawPath.isNotEmpty()) {
                        File(rawPath).parent ?: "Internal Storage"
                    } else "Internal Storage"
                    val folderName = if (folderPath.isNotEmpty()) {
                        File(folderPath).name.ifEmpty { "Internal Storage" }
                    } else "Internal Storage"

                    if (excludedFolders.contains(folderPath)) {
                        continue
                    }

                    mediaList.add(
                        MediaItemData(
                            id = id,
                            uriString = uri.toString(),
                            title = title.ifEmpty { displayName },
                            displayName = displayName,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            dateModified = dateMod,
                            width = width,
                            height = height,
                            mimeType = mime,
                            isVideo = true,
                            folderName = folderName,
                            folderPath = folderPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mediaList
    }

    private fun scanAudio(excludedFolders: Set<String>): List<MediaItemData> {
        val audioList = mutableListOf<MediaItemData>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA
        )

        try {
            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val displayCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val displayName = if (displayCol != -1) cursor.getString(displayCol) ?: "Unknown" else "Unknown"
                    val title = if (titleCol != -1) cursor.getString(titleCol) ?: displayName else displayName
                    val artist = if (artistCol != -1) cursor.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumCol != -1) cursor.getString(albumCol) ?: "Unknown Album" else "Unknown Album"
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol != -1) cursor.getLong(dateAddedCol) else 0L
                    val dateMod = if (dateModCol != -1) cursor.getLong(dateModCol) else 0L
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "audio/*" else "audio/*"
                    val rawPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""

                    val folderPath = if (rawPath.isNotEmpty()) {
                        File(rawPath).parent ?: "Music"
                    } else "Music"
                    val folderName = if (folderPath.isNotEmpty()) {
                        File(folderPath).name.ifEmpty { "Music" }
                    } else "Music"

                    if (excludedFolders.contains(folderPath)) {
                        continue
                    }

                    audioList.add(
                        MediaItemData(
                            id = id,
                            uriString = uri.toString(),
                            title = title.ifEmpty { displayName },
                            displayName = displayName,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            dateModified = dateMod,
                            width = 0,
                            height = 0,
                            mimeType = mime,
                            isVideo = false,
                            folderName = folderName,
                            folderPath = folderPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return audioList
    }

    private fun extractFolders(mediaList: List<MediaItemData>, excludedFolders: Set<String>): List<FolderItem> {
        val folderMap = mutableMapOf<String, MutableList<MediaItemData>>()

        for (item in mediaList) {
            val list = folderMap.getOrPut(item.folderPath) { mutableListOf() }
            list.add(item)
        }

        return folderMap.map { (path, items) ->
            val videoCount = items.count { it.isVideo }
            val audioCount = items.count { !it.isVideo }
            val totalSize = items.sumOf { it.sizeBytes }
            val representative = items.firstOrNull { it.isVideo }?.uriString ?: items.firstOrNull()?.uriString
            val folderName = items.firstOrNull()?.folderName ?: File(path).name.ifEmpty { "Folder" }

            FolderItem(
                path = path,
                name = folderName,
                mediaCount = items.size,
                videoCount = videoCount,
                audioCount = audioCount,
                representativeUri = representative,
                totalSizeBytes = totalSize,
                isExcluded = excludedFolders.contains(path)
            )
        }.sortedByDescending { it.mediaCount }
    }
}
