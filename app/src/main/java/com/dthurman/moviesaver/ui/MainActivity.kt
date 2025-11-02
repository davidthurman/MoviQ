package com.dthurman.moviesaver.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.SideEffect
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
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.dthurman.moviesaver.ui.reusable_components.dialogs.SettingsModal
import com.dthurman.moviesaver.ui.features.feature_detail.DetailScreen
import com.dthurman.moviesaver.ui.features.feature_login.LoginScreen
import com.dthurman.moviesaver.ui.features.feature_login.LoginViewModel
import com.dthurman.moviesaver.ui.nav.AppNavHost
import com.dthurman.moviesaver.ui.nav.Destination
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var movieRepository: MovieRepository
    
    private val sharedPreferences by lazy {
        getSharedPreferences("app_preferences", MODE_PRIVATE)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentUser by loginViewModel.currentUser.collectAsStateWithLifecycle()
            var isDarkMode by rememberSaveable { 
                mutableStateOf(sharedPreferences.getBoolean("is_dark_mode", true))
            }
            
            val appNavController = rememberNavController()
            val startDestination = Destination.SEEN
            var selectedIndex by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var selectedMovie by rememberSaveable { mutableStateOf<Movie?>(null) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

            AppTheme(darkTheme = isDarkMode, dynamicColor = false) {
                val view = window.decorView
                SideEffect {
                    val windowInsetsController = WindowCompat.getInsetsController(window, view)
                    windowInsetsController.isAppearanceLightStatusBars = !isDarkMode
                    windowInsetsController.isAppearanceLightNavigationBars = !isDarkMode
                }
                
                if (currentUser == null) {
                    LoginScreen()
                } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                                    loginViewModel.signOut()
                                    showSettingsDialog = false
                                },
                                isDarkMode = isDarkMode,
                                onThemeToggle = { newValue ->
                                    isDarkMode = newValue
                                    sharedPreferences.edit { putBoolean("is_dark_mode", newValue) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
