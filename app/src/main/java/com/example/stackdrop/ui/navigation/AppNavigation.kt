package com.example.stackdrop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stackdrop.ui.screens.AboutScreen
import com.example.stackdrop.ui.screens.GameScreen
import com.example.stackdrop.ui.screens.HomeScreen
import com.example.stackdrop.ui.screens.ResultScreen
import com.example.stackdrop.ui.screens.SettingsScreen
import com.example.stackdrop.ui.screens.SplashScreen
import com.example.stackdrop.viewmodel.StackDropViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: StackDropViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val statistics by viewModel.statistics.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                statistics = statistics,
                selectedDifficulty = uiState.difficulty,
                onDifficultySelected = { diff ->
                    viewModel.setDifficulty(diff)
                },
                onPlayClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClicked = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                uiState = uiState,
                userSettings = userSettings,
                onTapDrop = { viewModel.tapToDrop() },
                onUpdateGame = { dt -> viewModel.updateGame(dt) },
                onSetDimensions = { w, h -> viewModel.setBoardDimensions(w, h) },
                onPause = { viewModel.pauseGame() },
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartGame() },
                onExit = {
                    viewModel.exitGame()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                result = uiState.lastResult,
                onPlayAgain = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    viewModel.exitGame()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                userSettings = userSettings,
                onSettingsChanged = { updated ->
                    viewModel.updateSettings(updated)
                },
                onResetStatistics = {
                    viewModel.resetStatistics()
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}
