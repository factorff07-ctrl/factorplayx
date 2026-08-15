package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.FolderConfigDao
import com.example.data.local.dao.PlaybackStateDao
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.FolderConfigEntity
import com.example.data.local.entity.PlaybackStateEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlaybackStateEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        BookmarkEntity::class,
        FavoriteEntity::class,
        FolderConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackStateDao(): PlaybackStateDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun folderConfigDao(): FolderConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aether_player_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialPlaylists(database.playlistDao())
                    }
                }
            }

            suspend fun populateInitialPlaylists(playlistDao: PlaylistDao) {
                playlistDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Watch Later",
                        isSystem = true
                    )
                )
                playlistDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Favorite Clips",
                        isSystem = true
                    )
                )
                playlistDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Study & Focus",
                        isSystem = true
                    )
                )
            }
        }
    }
}
