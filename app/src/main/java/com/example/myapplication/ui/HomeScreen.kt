package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.myapplication.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MeloloViewModel,
    onMenu: () -> Unit,
    onOpenDetail: (DramaItem) -> Unit
) {
    val state = vm.state
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.libraryMode) {
                            LibraryMode.EXPLORE -> "Explore Drama"
                            LibraryMode.FAVORITES -> "Favorit"
                            LibraryMode.HISTORY -> "Riwayat"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },

        // 🔥 Bottom Navigation
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.libraryMode == LibraryMode.EXPLORE,
                    onClick = { vm.selectLibraryMode(LibraryMode.EXPLORE) },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Eksplorasi") }
                )
                NavigationBarItem(
                    selected = state.libraryMode == LibraryMode.FAVORITES,
                    onClick = { vm.selectLibraryMode(LibraryMode.FAVORITES) },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Favorit") }
                )
                NavigationBarItem(
                    selected = state.libraryMode == LibraryMode.HISTORY,
                    onClick = { vm.selectLibraryMode(LibraryMode.HISTORY) },
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Riwayat") }
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // ================= EXPLORE =================
            if (state.libraryMode == LibraryMode.EXPLORE) {

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
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan judul drama") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                }

                item {
                    Button(
                        onClick = {
                            vm.updateSearchText(searchText)
                            vm.submitSearch() // 🔥 FIX SEARCH
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cari Drama")
                    }
                }

                items(state.dramas) { drama ->
                    DramaItemView(drama) {
                        onOpenDetail(drama)
                    }
                }
            }

            // ================= FAVORITES =================
            if (state.libraryMode == LibraryMode.FAVORITES) {

                if (state.favorites.isEmpty()) {
                    item {
                        Text("Belum ada favorit")
                    }
                } else {
                    items(state.favorites) { drama ->
                        DramaItemView(drama) {
                            onOpenDetail(drama)
                        }
                    }
                }
            }

            // ================= HISTORY =================
            if (state.libraryMode == LibraryMode.HISTORY) {

                item {
                    TextButton(onClick = { vm.clearHistory() }) {
                        Text("Hapus Semua Riwayat")
                    }
                }

                if (state.history.isEmpty()) {
                    item {
                        Text("Riwayat kosong")
                    }
                } else {
                    items(state.history) { history ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    vm.openHistoryItem(history)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(history.title)
                                Text("Episode ${history.episodeIndex}")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= ITEM VIEW =================

@Composable
fun DramaItemView(
    drama: DramaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(drama.title)
            Text("Total: ${drama.episodeText}")
            Text(drama.synopsis, maxLines = 2)
        }
    }
}