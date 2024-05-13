package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.ttings.beatwave.ui.components.PlaylistPanel
import com.ttings.beatwave.ui.components.SuggestedSection
import com.ttings.beatwave.ui.components.TopAppBar
import com.ttings.beatwave.ui.components.TrackBar
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.SelectedPlaylistViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun SelectedPlaylist(
    navController: NavController,
    playlistId: String,
    playerViewModel: PlayerViewModel,
    viewModel: SelectedPlaylistViewModel = hiltViewModel()
) {

    LaunchedEffect(key1 = true) {
        viewModel.loadPlaylist(playlistId)
        viewModel.checkIfPlaylistLiked(playlistId)
    }

    LaunchedEffect(playlistId) {
        playerViewModel.loadPlaylistTracks(playlistId)
    }

    val user by viewModel.user.observeAsState()
    val playlist by viewModel.playlist.observeAsState()
    val creator by viewModel.creator.observeAsState()

    val tracks by playerViewModel.playlistTracks.collectAsState()

    val isPlaylistLiked by viewModel.isPlaylistLiked.observeAsState()

    var authorNames by remember { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(tracks) {
        try {
            val names = tracks.flatMap { it.artistIds }.associateWith { id ->
                val user = playerViewModel.getAuthorById(id)
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
        TopAppBar(
            title = playlist?.title ?: "",
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
                playlist?.let {
                    PlaylistPanel(
                        playlist = playlist!!,
                        currentUser = user!!,
                        isLiked = isPlaylistLiked?: false,
                        trackCount = it.tracks.size,
                        playlistDuration = "",
                        profileImage = creator?.avatar ?: "",
                        username = creator?.username ?: "Loading...",
                        onLikeClick = { viewModel.likePlaylist(playlistId) },
                        onShuffleClick = { /*TODO*/ },
                        onPlayClick = {
                            playerViewModel.playTrack(tracks[0], tracks)
                        }
                    )
                }
            }

            tracks.let { tracks ->
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
                                playerViewModel.loadUserUploads(user!!.userId, tracks.size + 20)
                            }
                        }
                    }
                }
            }

            item { SuggestedSection(playlistId = playlistId) }

        }
    }
}