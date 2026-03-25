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
fun HomeScreen(
    vm: MeloloViewModel,
    onMenu: () -> Unit,
    onOpenDetail: (DramaItem) -> Unit
) {
    val state = vm.state
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore Drama") },
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, null)
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

            if (showSearch) {
                item {
                    OutlinedTextField(
                        value = state.searchText,
                        onValueChange = vm::updateSearchText,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Cari drama...") }
                    )
                }
            }

            items(state.dramas) { drama ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onOpenDetail(drama) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(drama.title)
                        Text("Total: ${drama.episodeText}")
                    }
                }
            }
        }
    }
}