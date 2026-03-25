package com.example.myapplication.ui

import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.foundation.background
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.DramaItem
import com.example.myapplication.data.model.FeedMode
import com.example.myapplication.data.model.LibraryMode
import com.example.myapplication.data.model.WatchHistoryItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeloloApp(vm: MeloloViewModel = viewModel()) {
    val state = vm.state
    var showFullscreenPlayer by remember { mutableStateOf(false) }
    var showDrawer by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val activeUrl = remember(state.streamOptions, state.selectedQuality) {
        state.streamOptions.firstOrNull { it.label == state.selectedQuality }?.url
            ?: state.streamOptions.firstOrNull()?.url
            ?: ""
    }

    if (showFullscreenPlayer && activeUrl.isNotBlank()) {
        PlayerScreen(
            url = activeUrl,
            title = state.detail?.title ?: "Player",
            onBack = { showFullscreenPlayer = false },
            onToggleFullscreen = { vm.toggleFullscreen() },
            isFullscreen = state.isFullscreen
        )
    } else {
        ModalNavigationDrawer(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            gesturesEnabled = showDrawer,
            drawerContent = {
                ModalDrawerSheet {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                "MELOLO",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Streaming Drama",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                        label = { Text("Explore") },
                        selected = state.libraryMode == LibraryMode.EXPLORE,
                        onClick = {
                            vm.selectLibraryMode(LibraryMode.EXPLORE)
                            showDrawer = false
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = state.libraryMode == LibraryMode.FAVORITES,
                        onClick = {
                            vm.selectLibraryMode(LibraryMode.FAVORITES)
                            showDrawer = false
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = state.libraryMode == LibraryMode.HISTORY,
                        onClick = {
                            vm.selectLibraryMode(LibraryMode.HISTORY)
                            showDrawer = false
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            showSettingsDialog = true
                            showDrawer = false
                        }
                    )
                }
            }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                when (state.libraryMode) {
                                    LibraryMode.EXPLORE -> "Explore Drama"
                                    LibraryMode.FAVORITES -> "Favorite Drama"
                                    LibraryMode.HISTORY -> "Watch History"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { showDrawer = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (state.libraryMode == LibraryMode.EXPLORE) {
                                IconButton(onClick = { /* Search functionality */ }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                            IconButton(onClick = { /* Settings */ }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ) {
                        LibraryMode.entries.forEach { mode ->
                            NavigationBarItem(
                                selected = state.libraryMode == mode,
                                onClick = { vm.selectLibraryMode(mode) },
                                label = { Text(mode.label) },
                                icon = {
                                    Icon(
                                        when (mode) {
                                            LibraryMode.EXPLORE -> Icons.Default.Search
                                            LibraryMode.FAVORITES -> Icons.Default.Favorite
                                            LibraryMode.HISTORY -> Icons.Default.History
                                        },
                                        contentDescription = mode.label
                                    )
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Welcome Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Selamat Datang!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Nikmati streaming drama favoritmu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    when (state.libraryMode) {
                        LibraryMode.EXPLORE -> {
                            item {
                                TabRow(
                                    selectedTabIndex = state.feedMode.ordinal,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    FeedMode.entries.forEach { mode ->
                                        Tab(
                                            selected = state.feedMode == mode,
                                            onClick = { vm.changeFeedMode(mode) },
                                            text = { Text(mode.label, fontWeight = FontWeight.Medium) }
                                        )
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = state.searchText,
                                    onValueChange = vm::updateSearchText,
                                    label = { Text("Cari drama...") },
                                    placeholder = { Text("Masukkan judul drama") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            
                            item {
                                Button(
                                    onClick = vm::submitSearch,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cari Drama")
                                }
                            }

                            if (state.listLoading && state.dramas.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Memuat daftar drama...")
                                        }
                                    }
                                }
                            }

                            if (state.listError.isNotBlank()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            state.listError,
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            items(state.dramas, key = { it.bookId }) { drama ->
                                DramaListItem(
                                    item = drama,
                                    active = state.selectedDrama?.bookId == drama.bookId,
                                    lastWatchedEpisode = state.lastWatchedByBook[drama.bookId]?.episodeIndex,
                                    onSelect = { vm.selectDrama(drama) }
                                )
                            }

                            if (!state.listLoading && state.hasMore && state.dramas.isNotEmpty()) {
                                item {
                                    Button(
                                        onClick = vm::loadMore,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !state.listAppending,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (state.listAppending) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(if (state.listAppending) "Memuat..." else "Muat Lagi")
                                    }
                                }
                            }
                        }

                        LibraryMode.FAVORITES -> {
                            if (state.favorites.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "Belum ada favorit",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                "Tambahkan drama favoritmu dari menu Explore",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(state.favorites, key = { it.bookId }) { drama ->
                                    DramaListItem(
                                        item = drama,
                                        active = state.selectedDrama?.bookId == drama.bookId,
                                        lastWatchedEpisode = state.lastWatchedByBook[drama.bookId]?.episodeIndex,
                                        onSelect = { vm.selectDrama(drama) }
                                    )
                                }
                            }
                        }

                        LibraryMode.HISTORY -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Riwayat Tontonan",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = vm::clearHistory,
                                        enabled = state.history.isNotEmpty()
                                    ) {
                                        Text("Hapus Semua")
                                    }
                                }
                            }
                            
                            if (state.history.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "Riwayat kosong",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                "Tonton drama untuk melihat riwayat",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(state.history, key = { it.id }) { history ->
                                    HistoryListItem(
                                        item = history,
                                        onSelect = { vm.openHistoryItem(history) }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Detail Section
                    if (state.selectedDrama != null) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Detail Drama",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                FilledTonalButton(
                                    onClick = vm::toggleFavorite,
                                    enabled = state.selectedDrama != null,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    val selected = state.selectedDrama
                                    val isFav = selected?.bookId?.let { state.favoriteIds.contains(it) } == true
                                    Icon(
                                        if (isFav) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                                        contentDescription = if (isFav) "Remove from favorites" else "Add to favorites"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isFav) "Unfavorite" else "Favorite")
                                }
                            }
                        }

                        if (state.detailLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (state.detailError.isNotBlank()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        state.detailError,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        state.detail?.let { detail ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (detail.thumbnail.isNotBlank()) {
                                            AsyncImage(
                                                model = detail.thumbnail,
                                                contentDescription = detail.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        
                                        Text(
                                            detail.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        if (detail.intro.isNotBlank()) {
                                            Text(
                                                detail.intro,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.History,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Ditonton: ${detail.playCount} kali",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        
                                        state.lastWatchedByBook[state.selectedDrama?.bookId]?.let { lastWatched ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    "Episode terakhir ditonton: ${lastWatched.episodeIndex}",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (activeUrl.isNotBlank()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showFullscreenPlayer = true }
                                            .height(220.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            NativePlayer(url = activeUrl)
                                            Icon(
                                                Icons.Default.Fullscreen,
                                                contentDescription = "Fullscreen",
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }

                            if (state.streamLoading) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            if (state.streamError.isNotBlank()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            state.streamError,
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            if (state.streamOptions.isNotEmpty()) {
                                item {
                                    Text(
                                        "Kualitas Video",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.streamOptions, key = { it.label }) { quality ->
                                            val active = quality.label == state.selectedQuality
                                            if (active) {
                                                FilledTonalButton(
                                                    onClick = { vm.selectQuality(quality.label) },
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(quality.label)
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { vm.selectQuality(quality.label) },
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(quality.label)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    "Daftar Episode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            items(detail.episodes.indices.toList(), key = { idx -> detail.episodes[idx].vid }) { index ->
                                val episode = detail.episodes[index]
                                val active = state.selectedEpisodeIndex == index
                                val isLastWatched = state.lastWatchedByBook[state.selectedDrama?.bookId]?.episodeIndex == episode.index
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.selectEpisode(index) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Episode ${episode.index}",
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                                color = if (active) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isLastWatched && !active) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        "Terakhir",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                        if (active) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Playing",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Settings Dialog
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Pengaturan") },
                text = {
                    Column {
                        Text("Versi Aplikasi: 1.0.0")
                        Text("Developer: Melolo Team")
                        Text("API: Melolo API v1")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (item.thumbnail.isNotBlank()) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2
                )
                if (item.episodeText.isNotBlank()) {
                    Text(
                        "Total: ${item.episodeText} episode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (lastWatchedEpisode != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Episode $lastWatchedEpisode",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                if (item.synopsis.isNotBlank()) {
                    Text(
                        item.synopsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryListItem(item: WatchHistoryItem, onSelect: () -> Unit) {
    val timeLabel = remember(item.watchedAt) {
        DateFormat.format("dd MMM yyyy HH:mm", Date(item.watchedAt)).toString()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (item.thumbnail.isNotBlank()) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Text(
                    "Episode ${item.episodeIndex}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}