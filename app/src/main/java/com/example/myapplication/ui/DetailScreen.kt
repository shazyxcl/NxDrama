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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    vm: MeloloViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit
) {
    val state = vm.state
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.toggleFavorite() }) {
                        Icon(
                            if (state.favoriteIds.contains(state.selectedDrama?.bookId))
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔥 VIDEO PLAYER PREVIEW (AUTO LOAD)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.height(220.dp)) {

                        if (state.streamOptions.isNotEmpty()) {
                            PlayerScreen(
                                url = state.streamOptions
    .firstOrNull { it.label == state.selectedQuality }
    ?.url ?: ""
                                title = state.detail?.title ?: "",
                                onBack = {},
                                onToggleFullscreen = { vm.toggleFullscreen() },
                                isFullscreen = state.isFullscreen
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Loading video...")
                            }
                        }
                    }
                }
            }

            item {
            
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
            
                    TextField(
                        value = state.selectedQuality,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kualitas") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
            
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.streamOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    vm.selectQuality(option.label)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 🔥 PLAY BUTTON
            item {
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Play Video")
                }
            }

            // 🔥 DESKRIPSI
            item {
                ElevatedCard(
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        state.detail?.intro ?: "",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 🔥 EPISODE LIST
            items(state.detail?.episodes ?: emptyList()) { ep ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            vm.selectEpisode(ep.index - 1)
                            onPlay()
                        }
                ) {
                    Text(
                        "Episode ${ep.index}",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}