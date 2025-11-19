package com.dthurman.moviesaver.core.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.rememberNavController
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_movies.presentation.detail.DetailScreen
import com.dthurman.moviesaver.ui.components.SettingsModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    currentUser: User?,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    startDestination: Destination = Destination.SEEN,
    modifier: Modifier = Modifier
) {
    val appNavController = rememberNavController()
    var selectedIndex by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var selectedMovie by rememberSaveable { mutableStateOf<Movie?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            appNavController.navigate(route = destination.route)
                            selectedIndex = index
                        },
                        icon = {
                            when {
                                destination.icon != null -> {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.contentDescriptionRes)
                                    )
                                }
                                destination.iconRes != null -> {
                                    Icon(
                                        painter = painterResource(destination.iconRes),
                                        contentDescription = stringResource(destination.contentDescriptionRes)
                                    )
                                }
                            }
                        },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            AppNavHost(
                navController = appNavController,
                startDestination = startDestination.route,
                onMovieClick = { movie ->
                    selectedMovie = movie
                    showBottomSheet = true
                },
                onSettingsClick = {
                    showSettingsDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showBottomSheet && selectedMovie != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedMovie = null
            },
            sheetState = sheetState
        ) {
            DetailScreen(movie = selectedMovie!!)
        }
    }

    if (showSettingsDialog) {
        Dialog(
            onDismissRequest = {
                showSettingsDialog = false
            }
        ) {
            SettingsModal(
                onDismiss = {
                    showSettingsDialog = false
                },
                currentUser = currentUser,
                onSignOut = {
                    onSignOut()
                    showSettingsDialog = false
                },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }
    }
}

