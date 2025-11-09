package com.dthurman.moviesaver.core.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.feature_auth.presentation.LoginScreen
import com.dthurman.moviesaver.feature_auth.presentation.LoginViewModel
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    
    private val sharedPreferences by lazy {
        getSharedPreferences("app_preferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentUser by loginViewModel.currentUser.collectAsStateWithLifecycle()
            var isDarkMode by rememberSaveable { 
                mutableStateOf(sharedPreferences.getBoolean("is_dark_mode", true))
            }

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
                    AppScaffold(
                        currentUser = currentUser,
                        isDarkMode = isDarkMode,
                        onThemeToggle = { newValue ->
                            isDarkMode = newValue
                            sharedPreferences.edit { putBoolean("is_dark_mode", newValue) }
                        },
                        onSignOut = { loginViewModel.signOut() }
                    )
                }
            }
        }
    }
}
