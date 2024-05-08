package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.SearchField
import com.ttings.beatwave.ui.components.TopAppBar
import com.ttings.beatwave.ui.components.TrackBar
import com.ttings.beatwave.viewmodels.PlayerViewModel
import timber.log.Timber

@Composable
fun LibUploadScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel
) {

    val tracks by playerViewModel.uploadedTracks.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()
    val user by playerViewModel.currentUser.collectAsState()

    LaunchedEffect(user) {
        user?.userId?.let {
            if (tracks.isEmpty()) { // Только инициализируем загрузку, если треки еще не загружены
                playerViewModel.loadUserUploads(it)
            }
        }
    }

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
        TopAppBar(
            title = stringResource(R.string.uploads),
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
            },
            actions = {
                IconButton(
                    onClick = {
                        /* TODO: shuffle */
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
        SearchField()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            try {
                items(tracks.size) { index ->
                    val track = tracks[index]
                    val authorName = authorNames[track.artistIds[0]] ?: "Unknown Author"
                    TrackBar(
                        authorName = authorName,
                        duration = playerViewModel.secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = {
                            try {
                                playerViewModel.playTrack(track, tracks.subList(index + 1, tracks.size))

                            } catch (e: Exception) {
                                Timber.tag("LibUploadScreen").e(e)
                            }
                        },
                        onMoreClick = { /*TODO*/ }
                    )
                    if (index >= tracks.size - 1) {
                        LaunchedEffect(tracks.size) {
                            playerViewModel.loadUserUploads(user!!.userId, tracks.size + 20)
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.tag("LibUploadScreen").e(e)
            }
        }
    }

}