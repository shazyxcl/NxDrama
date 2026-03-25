package id.sansekai.melolo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteDramaEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MeloloDatabase : RoomDatabase() {
    abstract fun dao(): MeloloDao
}
