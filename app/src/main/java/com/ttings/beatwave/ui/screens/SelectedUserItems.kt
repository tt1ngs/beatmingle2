package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.components.*
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.ProfileViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedUserItems(
    navController: NavController,
    userId: String,
    nameOfItems: Int,
    playerViewModel: PlayerViewModel,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    LaunchedEffect(key1 = userId) {
        viewModel.loadUser(userId)
    }

    val userUploadsList = viewModel.userUploads.collectAsState().value
    val userPlaylistsList = viewModel.userPlaylists.collectAsState().value
    val userAlbumsList = viewModel.userAlbums.collectAsState().value
    val userLikesList = viewModel.userLikes.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomTopAppBar(
            title =
                when (nameOfItems) {
                    0 -> {
                        stringResource(id = R.string.tracks)
                    }
                    1 -> {
                        stringResource(id = R.string.playlists)
                    }
                    2 -> {
                        stringResource(id = R.string.album)
                    }
                    else -> {
                        stringResource(id = R.string.liked_tracks)
                    }
                },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Library",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
        when (nameOfItems) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(userUploadsList.size) { index ->
                        val track = userUploadsList[index]
                        val author by produceState<User?>(initialValue = null) {
                            try {
                                value = viewModel.getUserById(track.artistIds.first())
                            } catch (e: Exception) {
                                Timber.tag("TrackCard").e(e, "Error getting author")
                                value = null
                            }
                        }
                        TrackBar(
                            authorName = author?.username ?: "",
                            duration = secondsToMinutesSeconds(track.duration),
                            track = track,
                            onTrackClick = { playerViewModel.playTrack(track, userUploadsList) },
                            onMoreClick = {  }
                        )
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(userPlaylistsList.size) { index ->
                        val playlist = userPlaylistsList[index]
                        val author by produceState<User?>(initialValue = null) {
                            try {
                                value = viewModel.getUserById(playlist.userId)
                            } catch (e: Exception) {
                                Timber.tag("TrackCard").e(e, "Error getting author")
                                value = null
                            }
                        }
                        PlaylistBarSlim(
                            playlistName = playlist.title,
                            authorName = author?.username ?: "",
                            playlistImage = playlist.image,
                            duration = "",
                            isPrivate = playlist.isPrivate,
                            onMoreClick = {},
                            onPlaylistClick = { navController.navigate("SelectedPlaylist/${playlist.playlistId}") }
                        )
                    }
                }
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(userAlbumsList.size) { index ->
                        val album = userAlbumsList[index]
                        val author by produceState<User?>(initialValue = null) {
                            try {
                                value = viewModel.getUserById(album.userId)
                            } catch (e: Exception) {
                                Timber.tag("TrackCard").e(e, "Error getting author")
                                value = null
                            }
                        }
                        PlaylistBarSlim(
                            playlistName = album.title,
                            authorName = author?.username ?: "",
                            playlistImage = album.image,
                            duration = "",
                            isPrivate = album.isPrivate,
                            onMoreClick = {},
                            onPlaylistClick = { navController.navigate("SelectedAlbum/${album.playlistId}") }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(userLikesList.size) { index ->
                        val track = userLikesList[index]
                        val author by produceState<User?>(initialValue = null) {
                            try {
                                value = viewModel.getUserById(track.artistIds.first())
                            } catch (e: Exception) {
                                Timber.tag("TrackCard").e(e, "Error getting author")
                                value = null
                            }
                        }
                        TrackBar(
                            authorName = author?.username ?: "",
                            duration = secondsToMinutesSeconds(track.duration),
                            track = track,
                            onTrackClick = { playerViewModel.playTrack(track, userLikesList) },
                            onMoreClick = {  }
                        )
                    }
                }
            }
        }
    }
}