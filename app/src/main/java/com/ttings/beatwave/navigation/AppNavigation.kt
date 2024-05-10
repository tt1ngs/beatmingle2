package com.ttings.beatwave.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ttings.beatwave.ui.components.MiniPlayer
import com.ttings.beatwave.ui.screens.*
import com.ttings.beatwave.viewmodels.PlayerViewModel
import timber.log.Timber

@Composable
fun AppNavigation() {
    val playerViewModel: PlayerViewModel = hiltViewModel()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentProgress by playerViewModel.currentProgress.collectAsState(0f)
    val currentTrack by playerViewModel.currentTrack.collectAsState(initial = null)
    val isLiked by playerViewModel.isCurrentTrackLiked.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                if (currentDestination?.route !in listOf("SignInScreen", "SignUpScreen", "ProfileSetupScreen")) {
                    if (currentDestination?.route !in listOf("UploadScreen")) {
//                        Timber.tag("AppNavigation").d("$currentTrack")
                        currentTrack?.let {
                            MiniPlayer(
                                title = currentTrack!!.title,
                                author = playerViewModel.currentUser.value?.username ?: "Unknown Author",
                                isPlaying = isPlaying,
                                isLiked = isLiked,
                                currentProgress = currentProgress,
                                onPlayPauseClicked = {
                                    playerViewModel.togglePlayPause()
                                },
                                onFavoriteClicked = {
                                    playerViewModel.toggleFavorite()
                                },
                                onNextTrack = {
                                    playerViewModel.nextTrack()
                                },
                                onPreviousTrack = {
                                    playerViewModel.previousTrack()
                                }
                            )
                        }
                    }
                    NavigationBar {
                        listOfNavItems.forEach { navItem ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true,
                                onClick = {
                                    navController.navigate(navItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = navItem.icon,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(text = navItem.title)
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "SignInScreen",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("SignInScreen") {
                SignInScreen(navController)
            }
            composable("SignUpScreen") {
                SignUpScreen(navController)
            }
            composable("ProfileSetupScreen") {
                ProfileSetupScreen(navController)
            }
            composable("HomeScreen") {
                HomeScreen(navController)
            }
            composable("UploadScreen") {
                UploadScreen(navController)
            }
            composable("LibraryScreen") {
                LibraryScreen(navController)
            }
            composable("LikedScreen") {
                LikedScreen(navController, playerViewModel)
            }
            composable("LibUploadScreen") {
                LibUploadScreen(navController, playerViewModel)
            }
            composable("FeedScreen") {
                FeedScreen(navController)
            }
            composable("ProfileScreen/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    ProfileScreen(userId, navController)
                } else {
                    navController.popBackStack()
                    Timber.tag("AppNavigation").e("Error getting userId")
                }
            }
        }
    }
}