package com.example.myapplication.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MeloloRepository
import com.example.myapplication.ui.data.model.DramaDetail
import id.sansekai.melolo.data.model.DramaItem
import id.sansekai.melolo.data.model.FeedMode
import id.sansekai.melolo.data.model.LibraryMode
import id.sansekai.melolo.data.model.StreamOption
import id.sansekai.melolo.data.model.WatchHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val libraryMode: LibraryMode = LibraryMode.EXPLORE,
    val feedMode: FeedMode = FeedMode.FOR_YOU,
    val searchText: String = "",
    val searchQuery: String = "",
    val page: Int = 0,
    val hasMore: Boolean = true,
    val listLoading: Boolean = true,
    val listAppending: Boolean = false,
    val listError: String = "",
    val dramas: List<DramaItem> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val favorites: List<DramaItem> = emptyList(),
    val history: List<WatchHistoryItem> = emptyList(),
    val lastWatchedByBook: Map<String, WatchHistoryItem> = emptyMap(),
    val selectedDrama: DramaItem? = null,
    val detailLoading: Boolean = false,
    val detailError: String = "",
    val detail: DramaDetail? = null,
    val selectedEpisodeIndex: Int = 0,
    val streamLoading: Boolean = false,
    val streamError: String = "",
    val streamOptions: List<StreamOption> = emptyList(),
    val selectedQuality: String = ""
)

class MeloloViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MeloloRepository.getInstance(application)

    var state by mutableStateOf(UiState())
        private set

    init {
        observeLocalLibrary()
        refreshFeed()
    }

    fun selectLibraryMode(mode: LibraryMode) {
        state = state.copy(libraryMode = mode)
    }

    fun changeFeedMode(mode: FeedMode) {
        state = state.copy(feedMode = mode, searchQuery = "")
        refreshFeed()
    }

    fun updateSearchText(text: String) {
        state = state.copy(searchText = text)
    }

    fun submitSearch() {
        state = state.copy(searchQuery = state.searchText.trim())
        refreshFeed()
    }

    fun refreshFeed() {
        loadFeed(reset = true)
    }

    fun loadMore() {
        if (state.listLoading || state.listAppending || !state.hasMore) return
        loadFeed(reset = false)
    }

    fun selectDrama(item: DramaItem) {
        state = state.copy(selectedDrama = item)
        val preferredEpisode = state.lastWatchedByBook[item.bookId]?.episodeIndex
        loadDetail(item.bookId, preferredEpisode)
    }

    fun openHistoryItem(item: WatchHistoryItem) {
        val drama = state.favorites.firstOrNull { it.bookId == item.bookId }
            ?: state.dramas.firstOrNull { it.bookId == item.bookId }
            ?: DramaItem(
                bookId = item.bookId,
                title = item.title,
                synopsis = "",
                episodeText = ""
            )
        state = state.copy(selectedDrama = drama)
        loadDetail(drama.bookId, preferredEpisodeIndex = item.episodeIndex)
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.clearHistory()
        }
    }

    fun toggleFavorite() {
        val selected = state.selectedDrama ?: return
        val isFavorite = state.favoriteIds.contains(selected.bookId)
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleFavorite(selected, isFavorite)
        }
    }

    fun selectEpisode(index: Int) {
        state = state.copy(selectedEpisodeIndex = index)
        val episode = state.detail?.episodes?.getOrNull(index) ?: return
        loadStream(episode.vid, trackHistory = true)
    }

    fun selectQuality(label: String) {
        state = state.copy(selectedQuality = label)
    }

    private fun observeLocalLibrary() {
        viewModelScope.launch {
            repo.observeFavoriteIds().collectLatest { ids ->
                state = state.copy(favoriteIds = ids)
            }
        }
        viewModelScope.launch {
            repo.observeFavorites().collectLatest { list ->
                state = state.copy(favorites = list)
            }
        }
        viewModelScope.launch {
            repo.observeHistory().collectLatest { list ->
                state = state.copy(
                    history = list,
                    lastWatchedByBook = list.groupBy { it.bookId }
                        .mapValues { (_, items) -> items.maxBy { it.watchedAt } }
                )
            }
        }
    }

    private fun loadFeed(reset: Boolean) {
        val targetPage = if (reset) 1 else state.page + 1
        viewModelScope.launch {
            state = if (reset) {
                state.copy(listLoading = true, listError = "", dramas = emptyList(), page = 0, hasMore = true)
            } else {
                state.copy(listAppending = true, listError = "")
            }

            val current = state
            val result = withContext(Dispatchers.IO) {
                runCatching { repo.loadFeed(current.feedMode, current.searchQuery, targetPage) }
            }

            result.onSuccess { incoming ->
                val merged = if (reset) incoming else mergeUnique(state.dramas, incoming)
                val hasMoreData = incoming.isNotEmpty()
                val selected = state.selectedDrama
                    ?.takeIf { current -> merged.any { it.bookId == current.bookId } }
                    ?: merged.firstOrNull()
                state = state.copy(
                    listLoading = false,
                    listAppending = false,
                    dramas = merged,
                    page = if (hasMoreData) targetPage else state.page,
                    hasMore = hasMoreData,
                    selectedDrama = selected
                )
                if (selected != null && state.detail == null) {
                    loadDetail(selected.bookId)
                }
            }.onFailure { error ->
                state = state.copy(
                    listLoading = false,
                    listAppending = false,
                    listError = error.message ?: "Gagal memuat daftar drama"
                )
            }
        }
    }

    private fun loadDetail(bookId: String, preferredEpisodeIndex: Int? = null) {
        viewModelScope.launch {
            state = state.copy(
                detailLoading = true,
                detailError = "",
                detail = null,
                selectedEpisodeIndex = 0,
                streamOptions = emptyList(),
                selectedQuality = ""
            )
            val result = withContext(Dispatchers.IO) {
                runCatching { repo.loadDetail(bookId) }
            }
            result.onSuccess { detail ->
                state = state.copy(detailLoading = false, detail = detail)
                val episodes = detail?.episodes.orEmpty()
                val targetIndex = episodes.indexOfFirst { it.index == preferredEpisodeIndex }
                    .takeIf { it >= 0 }
                    ?: 0
                val targetEpisode = episodes.getOrNull(targetIndex)
                if (targetEpisode != null) {
                    state = state.copy(selectedEpisodeIndex = targetIndex)
                    loadStream(targetEpisode.vid, trackHistory = false)
                }
            }.onFailure { error ->
                state = state.copy(
                    detailLoading = false,
                    detailError = error.message ?: "Gagal memuat detail drama"
                )
            }
        }
    }

    private fun loadStream(videoId: String, trackHistory: Boolean) {
        viewModelScope.launch {
            state = state.copy(streamLoading = true, streamError = "", streamOptions = emptyList())
            val result = withContext(Dispatchers.IO) {
                runCatching { repo.loadStream(videoId) }
            }
            result.onSuccess { options ->
                state = state.copy(
                    streamLoading = false,
                    streamOptions = options,
                    selectedQuality = options.firstOrNull()?.label.orEmpty()
                )
                if (trackHistory) {
                    val drama = state.selectedDrama
                    val episode = state.detail?.episodes?.getOrNull(state.selectedEpisodeIndex)
                    if (drama != null && episode != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            repo.saveHistory(drama, episode)
                        }
                    }
                }
            }.onFailure { error ->
                state = state.copy(
                    streamLoading = false,
                    streamError = error.message ?: "Gagal memuat stream"
                )
            }
        }
    }

    private fun mergeUnique(base: List<DramaItem>, incoming: List<DramaItem>): List<DramaItem> {
        if (incoming.isEmpty()) return base
        val seen = base.map { it.bookId }.toMutableSet()
        val merged = base.toMutableList()
        incoming.forEach { item ->
            if (seen.add(item.bookId)) merged += item
        }
        return merged
    }
}
