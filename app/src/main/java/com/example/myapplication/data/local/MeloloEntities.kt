package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_dramas")
data class FavoriteDramaEntity(
    @PrimaryKey val bookId: String,
    val title: String,
    val synopsis: String,
    val episodeText: String,
    val updatedAt: Long
)

@Entity(
    tableName = "watch_history",
    indices = [Index("bookId"), Index("watchedAt")]
)
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val title: String,
    val episodeIndex: Int,
    val videoId: String,
    val watchedAt: Long
)
