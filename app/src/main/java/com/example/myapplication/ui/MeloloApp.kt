package com.example.myapplication.ui

import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.myapplication.data.model.DramaItem
import com.example.myapplication.data.model.FeedMode
import com.example.myapplication.data.model.LibraryMode
import com.example.myapplication.data.model.WatchHistoryItem
import java.util.Date

@Composable
fun MeloloApp(vm: MeloloViewModel = viewModel()) {
    val state = vm.state
    val activeUrl = remember(state.streamOptions, state.selectedQuality) {
        state.streamOptions.firstOrNull { it.label == state.selectedQuality }?.url
            ?: state.streamOptions.firstOrNull()?.url
            ?: ""
    }

    val lastWatchedForSelected = state.selectedDrama?.bookId?.let { state.lastWatchedByBook[it] }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("MELOLO", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Android Native dengan API Melolo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                TabRow(selectedTabIndex = state.libraryMode.ordinal) {
                    LibraryMode.entries.forEach { mode ->
                        Tab(
                            selected = state.libraryMode == mode,
                            onClick = { vm.selectLibraryMode(mode) },
                            text = { Text(mode.label) }
                        )
                    }
                }
            }

            when (state.libraryMode) {
                LibraryMode.EXPLORE -> {
                    item {
                        TabRow(selectedTabIndex = state.feedMode.ordinal) {
                            FeedMode.entries.forEach { mode ->
                                Tab(
                                    selected = state.feedMode == mode,
                                    onClick = { vm.changeFeedMode(mode) },
                                    text = { Text(mode.label) }
                                )
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.searchText,
                                onValueChange = vm::updateSearchText,
                                label = { Text("Cari drama") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = vm::submitSearch, modifier = Modifier.align(Alignment.CenterVertically)) {
                                Text("Cari")
                            }
                        }
                    }

                    if (state.listLoading) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Text(" Memuat daftar drama...")
                            }
                        }
                    }

                    if (state.listError.isNotBlank()) {
                        item { Text(state.listError, color = MaterialTheme.colorScheme.error) }
                    }

                    items(state.dramas, key = { it.bookId }) { drama ->
                        DramaListItem(
                            item = drama,
                            active = state.selectedDrama?.bookId == drama.bookId,
                            lastWatchedEpisode = state.lastWatchedByBook[drama.bookId]?.episodeIndex,
                            onSelect = { vm.selectDrama(drama) }
                        )
                    }

                    if (!state.listLoading && state.hasMore) {
                        item {
                            Button(onClick = vm::loadMore) {
                                Text(if (state.listAppending) "Memuat..." else "Muat Lagi")
                            }
                        }
                    }
                }

                LibraryMode.FAVORITES -> {
                    items(state.favorites, key = { it.bookId }) { drama ->
                        DramaListItem(
                            item = drama,
                            active = state.selectedDrama?.bookId == drama.bookId,
                            lastWatchedEpisode = state.lastWatchedByBook[drama.bookId]?.episodeIndex,
                            onSelect = { vm.selectDrama(drama) }
                        )
                    }
                    if (state.favorites.isEmpty()) {
                        item { Text("Belum ada favorit.") }
                    }
                }

                LibraryMode.HISTORY -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Riwayat Tontonan", style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = vm::clearHistory,
                                enabled = state.history.isNotEmpty()
                            ) {
                                Text("Hapus Riwayat")
                            }
                        }
                    }
                    items(state.history, key = { it.id }) { history ->
                        HistoryListItem(item = history, onSelect = { vm.openHistoryItem(history) })
                    }
                    if (state.history.isEmpty()) {
                        item { Text("Riwayat tontonan masih kosong.") }
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    FilledTonalButton(onClick = vm::toggleFavorite, enabled = state.selectedDrama != null) {
                        val selected = state.selectedDrama
                        val isFav = selected?.bookId?.let { state.favoriteIds.contains(it) } == true
                        Text(if (isFav) "Unfavorite" else "Favorite")
                    }
                }
            }

            if (state.detailLoading) {
                item { Text("Memuat detail...") }
            }

            if (state.detailError.isNotBlank()) {
                item { Text(state.detailError, color = MaterialTheme.colorScheme.error) }
            }

            state.detail?.let { detail ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(detail.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (detail.intro.isNotBlank()) {
                            Text(detail.intro)
                        }
                        Text("Ditonton: ${detail.playCount}")
                        if (lastWatchedForSelected != null) {
                            Text(
                                "Episode terakhir ditonton: ${lastWatchedForSelected.episodeIndex}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (activeUrl.isNotBlank()) {
                    item { NativePlayer(url = activeUrl) }
                }

                if (state.streamLoading) {
                    item { Text("Memuat stream...") }
                }

                if (state.streamError.isNotBlank()) {
                    item { Text(state.streamError, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.streamOptions, key = { it.label }) { quality ->
                            val active = quality.label == state.selectedQuality
                            if (active) {
                                FilledTonalButton(onClick = { vm.selectQuality(quality.label) }) {
                                    Text(quality.label)
                                }
                            } else {
                                Button(onClick = { vm.selectQuality(quality.label) }) {
                                    Text(quality.label)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Episode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                items(detail.episodes.indices.toList(), key = { idx -> detail.episodes[idx].vid }) { index ->
                    val episode = detail.episodes[index]
                    val active = state.selectedEpisodeIndex == index
                    val isLastWatched = lastWatchedForSelected?.episodeIndex == episode.index
                    val label = buildString {
                        append("Episode ${episode.index}")
                        if (isLastWatched) append(" (Terakhir ditonton)")
                    }
                    Text(
                        text = label,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLastWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.selectEpisode(index) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DramaListItem(
    item: DramaItem,
    active: Boolean,
    lastWatchedEpisode: Int?,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
        if (item.episodeText.isNotBlank()) {
            Text("Episode: ${item.episodeText}")
        }
        if (lastWatchedEpisode != null) {
            Text(
                "Terakhir ditonton: Episode $lastWatchedEpisode",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (item.synopsis.isNotBlank()) {
            Text(item.synopsis, maxLines = 2)
        }
    }
}

@Composable
private fun HistoryListItem(item: WatchHistoryItem, onSelect: () -> Unit) {
    val timeLabel = remember(item.watchedAt) {
        DateFormat.format("dd MMM yyyy HH:mm", Date(item.watchedAt)).toString()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        Text("Episode ${item.episodeIndex} | $timeLabel")
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun NativePlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}