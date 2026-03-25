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

@Composable
fun DetailScreen(
    vm: MeloloViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit
) {
    val state = vm.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
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

            item {
                Button(
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play Video")
                }
            }

            item {
                Text(state.detail?.intro ?: "", modifier = Modifier.padding(8.dp))
            }

            items(state.detail?.episodes ?: emptyList()) { ep ->
                Text(
                    "Episode ${ep.index}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectEpisode(ep.index - 1)
                            onPlay()
                        }
                        .padding(12.dp)
                )
            }
        }
    }
}