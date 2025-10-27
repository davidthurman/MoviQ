package com.dthurman.moviesaver.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.features.feature_detail.DetailScreen
import com.dthurman.moviesaver.ui.nav.AppNavHost
import com.dthurman.moviesaver.ui.nav.Destination
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            val startDestination = Destination.SEEN
            var selectedIndex by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
            
            // Bottom sheet state
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var selectedMovie by rememberSaveable { mutableStateOf<Movie?>(null) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                            Destination.entries.forEachIndexed { index, destination ->
                                NavigationBarItem(
                                    selected = selectedIndex == index,
                                    onClick = {
                                        navController.navigate(route = destination.route)
                                        selectedIndex = index
                                    },
                                    icon = {
                                        Icon(
                                            destination.icon,
                                            contentDescription = destination.contentDescription
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                ) {  innerPadding ->
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
                            navController = navController,
                            startDestination = startDestination.route,
                            onMovieClick = { movie ->
                                selectedMovie = movie
                                showBottomSheet = true
                            },
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Modal Bottom Sheet for movie details
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
            }
        }
    }
}
