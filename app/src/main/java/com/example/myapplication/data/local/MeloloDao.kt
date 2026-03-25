package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeloloDao {
    @Query("SELECT * FROM favorite_dramas ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteDramaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(item: FavoriteDramaEntity)

    @Query("DELETE FROM favorite_dramas WHERE bookId = :bookId")
    suspend fun removeFavorite(bookId: String)

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 80")
    fun observeHistory(): Flow<List<WatchHistoryEntity>>

    @Insert
    suspend fun insertHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id NOT IN (SELECT id FROM watch_history ORDER BY watchedAt DESC LIMIT :limit)")
    suspend fun pruneHistory(limit: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}
