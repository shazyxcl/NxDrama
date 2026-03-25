package com.example.myapplication.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeloloApp(vm: MeloloViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var showPlayer by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Explore") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("home")
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        showSettings = true
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {

        NavHost(navController, startDestination = "home") {

            composable("home") {
                HomeScreen(
                    vm = vm,
                    onMenu = { scope.launch { drawerState.open() } },
                    onOpenDetail = {
                        vm.selectDrama(it)
                        navController.navigate("detail")
                    }
                )
            }

            composable("detail") {
                DetailScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onPlay = { showPlayer = true }
                )
            }
        }

        if (showPlayer) {
            PlayerScreen(
                url = vm.state.streamOptions.firstOrNull()?.url ?: "",
                title = vm.state.detail?.title ?: "",
                onBack = { showPlayer = false },
                onToggleFullscreen = { vm.toggleFullscreen() },
                isFullscreen = vm.state.isFullscreen
            )
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text("Tutup")
                    }
                },
                title = { Text("Settings") },
                text = { Text("Melolo v1.0") }
            )
        }
    }
}