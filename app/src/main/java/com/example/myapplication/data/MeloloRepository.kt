package id.sansekai.melolo.data

import android.content.Context
import android.util.Base64
import androidx.room.Room
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.sansekai.melolo.data.local.FavoriteDramaEntity
import id.sansekai.melolo.data.local.MeloloDatabase
import id.sansekai.melolo.data.local.WatchHistoryEntity
import id.sansekai.melolo.data.model.DramaDetail
import id.sansekai.melolo.data.model.DramaItem
import id.sansekai.melolo.data.model.EpisodeItem
import id.sansekai.melolo.data.model.FeedMode
import id.sansekai.melolo.data.model.StreamOption
import id.sansekai.melolo.data.model.WatchHistoryItem
import id.sansekai.melolo.data.network.MeloloApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MeloloRepository private constructor(
    private val api: MeloloApi,
    private val db: MeloloDatabase
) {
    suspend fun loadFeed(mode: FeedMode, query: String, page: Int): List<DramaItem> {
        val payload = if (query.isNotBlank()) {
            api.search(query = query, page = page)
        } else {
            api.feed(feed = mode.path, page = page)
        }
        return extractBooks(payload)
    }

    suspend fun loadDetail(bookId: String): DramaDetail? {
        val root = api.detail(bookId)
        val info = root.getAsJsonObject("data")?.getAsJsonObject("video_data") ?: return null
        val title = info.getString("series_title") ?: return null
        val intro = info.getString("series_intro").orEmpty()
        val playCount = info.getLong("series_play_cnt") ?: 0L
        val episodes = info.getAsJsonArray("video_list")?.mapNotNull { item ->
            val obj = item.asJsonObject
            val vid = obj.getString("vid") ?: return@mapNotNull null
            EpisodeItem(vid = vid, index = obj.getInt("vid_index") ?: 0)
        }.orEmpty()
        return DramaDetail(title = title, intro = intro, playCount = playCount, episodes = episodes)
    }

    suspend fun loadStream(videoId: String): List<StreamOption> {
        val root = api.stream(videoId)
        val data = root.getAsJsonObject("data") ?: JsonObject()
        val options = mutableListOf<StreamOption>()
        val seen = mutableSetOf<String>()

        data.getString("main_url")?.let { direct ->
            val normalized = normalizeUrl(direct)
            options += StreamOption("Default", normalized, 0)
            seen += normalized
        }

        data.getString("video_model")?.let { rawModel ->
            runCatching {
                val model = JsonParser.parseString(rawModel).asJsonObject
                val list = model.getAsJsonObject("video_list") ?: JsonObject()
                list.entrySet().forEach { (_, entry) ->
                    val item = entry.asJsonObject
                    val definition = item.getString("definition") ?: "Auto"
                    val rawUrl = item.getString("main_url") ?: return@forEach
                    val decoded = decodeBase64(rawUrl)
                    if (!decoded.startsWith("http")) return@forEach
                    val normalized = normalizeUrl(decoded)
                    if (seen.contains(normalized)) return@forEach

                    val rank = definition.filter { it.isDigit() }.toIntOrNull() ?: 0
                    options += StreamOption(definition, normalized, rank)
                    seen += normalized
                }
            }
        }

        return options.sortedByDescending { it.rank }
    }

    fun observeFavoriteIds(): Flow<Set<String>> {
        return db.dao().observeFavorites().map { list -> list.map { it.bookId }.toSet() }
    }

    fun observeFavorites(): Flow<List<DramaItem>> {
        return db.dao().observeFavorites().map { list ->
            list.map { entity ->
                DramaItem(
                    bookId = entity.bookId,
                    title = entity.title,
                    synopsis = entity.synopsis,
                    episodeText = entity.episodeText
                )
            }
        }
    }

    suspend fun toggleFavorite(item: DramaItem, isFavorite: Boolean) {
        if (isFavorite) {
            db.dao().removeFavorite(item.bookId)
            return
        }

        db.dao().upsertFavorite(
            FavoriteDramaEntity(
                bookId = item.bookId,
                title = item.title,
                synopsis = item.synopsis,
                episodeText = item.episodeText,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveHistory(item: DramaItem, episode: EpisodeItem) {
        db.dao().insertHistory(
            WatchHistoryEntity(
                bookId = item.bookId,
                title = item.title,
                episodeIndex = episode.index,
                videoId = episode.vid,
                watchedAt = System.currentTimeMillis()
            )
        )
        db.dao().pruneHistory(limit = 200)
    }

    suspend fun clearHistory() {
        db.dao().clearHistory()
    }

    fun observeHistory(): Flow<List<WatchHistoryItem>> {
        return db.dao().observeHistory().map { list ->
            list.map { entity ->
                WatchHistoryItem(
                    id = entity.id,
                    bookId = entity.bookId,
                    title = entity.title,
                    episodeIndex = entity.episodeIndex,
                    videoId = entity.videoId,
                    watchedAt = entity.watchedAt
                )
            }
        }
    }

    private fun extractBooks(payload: JsonElement): List<DramaItem> {
        val items = mutableListOf<DramaItem>()
        val seen = mutableSetOf<String>()

        fun walk(node: JsonElement?) {
            if (node == null || node.isJsonNull) return
            if (node.isJsonArray) {
                node.asJsonArray.forEach { walk(it) }
                return
            }
            if (!node.isJsonObject) return

            val obj = node.asJsonObject
            obj.getAsJsonArray("books")?.forEach { candidate ->
                if (!candidate.isJsonObject) return@forEach
                val book = candidate.asJsonObject
                val id = book.getString("book_id") ?: return@forEach
                if (seen.contains(id)) return@forEach
                val title = book.getString("book_name") ?: return@forEach
                items += DramaItem(
                    bookId = id,
                    title = title,
                    synopsis = book.getString("abstract").orEmpty(),
                    episodeText = book.getString("serial_count").orEmpty()
                )
                seen += id
            }

            obj.entrySet().forEach { (_, value) -> walk(value) }
        }

        walk(payload)
        return items
    }

    private fun normalizeUrl(raw: String): String {
        return if (raw.startsWith("http://")) {
            "https://${raw.removePrefix("http://")}"
        } else {
            raw
        }
    }

    private fun decodeBase64(value: String): String {
        val candidates = listOf(Base64.DEFAULT, Base64.URL_SAFE)
        for (flag in candidates) {
            val decoded = runCatching { String(Base64.decode(value, flag)) }.getOrNull()
            if (!decoded.isNullOrBlank()) return decoded
        }
        return ""
    }

    companion object {
        @Volatile
        private var instance: MeloloRepository? = null

        fun getInstance(context: Context): MeloloRepository {
            return instance ?: synchronized(this) {
                instance ?: create(context).also { instance = it }
            }
        }

        private fun create(context: Context): MeloloRepository {
            val api = Retrofit.Builder()
                .baseUrl("https://api.sansekai.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MeloloApi::class.java)

            val db = Room.databaseBuilder(
                context.applicationContext,
                MeloloDatabase::class.java,
                "melolo_native.db"
            ).build()

            return MeloloRepository(api = api, db = db)
        }
    }
}