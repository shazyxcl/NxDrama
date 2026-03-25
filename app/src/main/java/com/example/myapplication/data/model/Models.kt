package com.example.myapplication.data.model

enum class FeedMode(val path: String, val label: String) {
    FOR_YOU("foryou", "For You"),
    TRENDING("trending", "Trending"),
    LATEST("latest", "Terbaru")
}

enum class LibraryMode(val label: String) {
    EXPLORE("Eksplorasi"),
    FAVORITES("Favorit"),
    HISTORY("Riwayat")
}

data class DramaItem(
    val bookId: String,
    val title: String,
    val synopsis: String,
    val episodeText: String,
    val thumbnail: String = "" // Tambahkan thumbnail
)

data class EpisodeItem(
    val vid: String,
    val index: Int
)

data class DramaDetail(
    val title: String,
    val intro: String,
    val playCount: Long,
    val episodes: List<EpisodeItem>,
    val thumbnail: String = "" // Tambahkan thumbnail
)

data class StreamOption(
    val label: String,
    val url: String,
    val rank: Int
)

data class WatchHistoryItem(
    val id: Long,
    val bookId: String,
    val title: String,
    val episodeIndex: Int,
    val videoId: String,
    val watchedAt: Long,
    val thumbnail: String = "" // Tambahkan thumbnail
)