package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.ui.components.*
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.SelectedAlbumViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedAlbum(
    navController: NavController,
    playlistId: String,
    playerViewModel: PlayerViewModel,
    viewModel: SelectedAlbumViewModel = hiltViewModel()
) {

    val user by viewModel.currentUser.collectAsState()
    val albumTracks by viewModel.albumTracks.collectAsState()
    val album by viewModel.album.observeAsState()
    val creator by viewModel.creator.observeAsState()
    val isAlbumLiked by viewModel.isAlbumLiked.observeAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadAlbum(playlistId)
        viewModel.checkIfAlbumLiked(playlistId)
    }

    LaunchedEffect(user) {
        user?.userId?.let {
            if (albumTracks.isEmpty()) {
                viewModel.loadAlbumTracks(playlistId)
            }
        }
    }

    var authorNames by remember { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(albumTracks) {
        try {
            val names = albumTracks.flatMap { it.artistIds }.associateWith { id ->
                val user = viewModel.getAuthorById(id)
                if (user != null && user.username!!.isNotBlank()) {
                    user.username
                } else {
                    "Unknown Author"
                }
            }
            authorNames = names
        } catch (e: Exception) {
            Timber.tag("LibUploadScreen").e(e)
        }

    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Timber.tag("SelectedPlaylist").d(creator.toString())
        CustomTopAppBar(
            title = album?.title ?: "",
            navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                album?.let {
                    PlaylistPanel(
                        playlist = album!!,
                        currentUser = user!!,
                        isLiked = isAlbumLiked?: false,
                        trackCount = it.tracks.size,
                        playlistDuration = "",
                        profileImage = creator?.avatar ?: "",
                        username = creator?.username ?: "Loading...",
                        onLikeClick = { viewModel.likeAlbum(playlistId) },
                        onShuffleClick = { /*TODO*/ },
                        onPlayClick = {
                            playerViewModel.playTrack(albumTracks[0], albumTracks)
                        },
                        onMoreClick = {},
                        onUserClick = {
                            if (user != null) {
                                val userId = user!!.userId
                                navController.navigate("ProfileScreen/${userId}")
                            } else {
                                Timber.d("Author == null")
                            }
                        }
                    )
                }
            }

            albumTracks.let { tracks ->
                if (tracks.isEmpty()) {
                    item { Text("No tracks available") }
                } else {
                    items(tracks.size) { index ->
                        val track = tracks[index]
                        val authorName = authorNames[track.artistIds[0]] ?: ""
                        TrackBar(
                            authorName = authorName,
                            duration = viewModel.secondsToMinutesSeconds(track.duration),
                            track = track,
                            onTrackClick = {
                                playerViewModel.playTrack(track, tracks.subList(index + 1, tracks.size))
                            },
                            onMoreClick = { /* TODO: On moreClick */ }
                        )
                        if (index >= tracks.size - 1) {
                            LaunchedEffect(tracks.size) {
                                // TODO: rework queue
                            }
                        }
                    }
                }
            }

            if (user?.userId == creator?.userId) {
                item { SuggestedSection(playlistId = playlistId, onlyUserTracks = true) }
            }

        }
    }
}