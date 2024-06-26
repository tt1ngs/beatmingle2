package com.ttings.beatwave.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ttings.beatwave.ui.components.MiniPlayer
import com.ttings.beatwave.ui.screens.*
import com.ttings.beatwave.viewmodels.*
import timber.log.Timber

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val likedViewModel: LikedViewModel = hiltViewModel()
    val libUploadViewModel: LibUploadViewModel = hiltViewModel()
    val selectedPlaylistViewModel: SelectedPlaylistViewModel = hiltViewModel()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentUser by playerViewModel.currentUser.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentProgress by playerViewModel.currentProgress.collectAsState(0f)
    val currentTrack by playerViewModel.currentTrack.collectAsState(initial = null)
    val isLiked by playerViewModel.isCurrentTrackLiked.collectAsState()

    val isLoggedIn by authViewModel.isLoggedIn().collectAsState(false)

    val playlists by playerViewModel.playlists

    LaunchedEffect(currentUser) {
        currentUser?.let {
            playerViewModel.fetchUserPlaylists(it.userId)
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                if (currentDestination?.route !in listOf("SignInScreen", "SignUpScreen", "ProfileSetupScreen")) {
                    if (currentDestination?.route !in listOf("UploadScreen", "FeedScreen")) {
                        currentTrack?.let {
                            MiniPlayer(
                                currentUser = currentUser!!,
                                track = currentTrack!!,
                                playlists = playlists,
                                author = playerViewModel.currentUser.value?.username ?: "Unknown Author",
                                isPlaying = isPlaying,
                                isLiked = isLiked,
                                currentProgress = currentProgress,
                                onSelectedPlaylist = {
                                    playerViewModel.addTrackToPlaylist(
                                        trackId = currentTrack!!.trackId,
                                        playlistId = it.playlistId
                                    )
                                },
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
            startDestination = if (isLoggedIn) "HomeScreen" else "SignInScreen",
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
                HomeScreen(navController, playerViewModel)
            }
            composable("UploadScreen") {
                UploadScreen(navController)
            }
            composable("LibraryScreen") {
                LibraryScreen(navController)
            }
            composable("LikedScreen") {
                LikedScreen(navController, playerViewModel, likedViewModel)
            }
            composable("LibUploadScreen") {
                LibUploadScreen(navController, playerViewModel, libUploadViewModel)
            }
            composable("FeedScreen") {
                FeedScreen(navController)
            }
            composable("ProfileScreen/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    ProfileScreen(userId, navController, playerViewModel)
                } else {
                    navController.popBackStack()
                    Timber.tag("AppNavigation").e("Error getting userId")
                }
            }
            composable("PlaylistsScreen") {
                PlaylistsScreen(navController)
            }
            composable("SelectedPlaylist/{playlistId}") { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")
                if (playlistId != null) {
                    SelectedPlaylist(navController, playlistId, playerViewModel, selectedPlaylistViewModel)
                } else {
                    // Обработка ошибки, если playlistId не предоставлен
                }
            }
            composable("SearchScreen") {
                SearchScreen(navController)
            }
            composable("SettingsScreen") {
                SettingsScreen(navController)
            }
            composable("AlbumsScreen") {
                AlbumsScreen(navController)
            }
            composable("SelectedAlbum/{albumId}") { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")
                if (albumId != null) {
                    SelectedAlbum(navController, albumId, playerViewModel)
                } else {
                    // Обработка ошибки, если albumId не предоставлен
                }
            }
            composable("SelectedUserItems/{userId}/{type}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                val type = backStackEntry.arguments?.getString("type")?.toIntOrNull()
                if (userId != null && type != null) {
                    SelectedUserItems(
                        navController = navController,
                        userId = userId,
                        nameOfItems = type,
                        playerViewModel = playerViewModel
                    )
                } else {
                    // Обработка ошибки, если userId или type не предоставлены
                }
            }
        }
    }
}