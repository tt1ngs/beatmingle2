package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.ArtistBar
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.components.PlaylistBar
import com.ttings.beatwave.ui.components.TrackBar
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.HomeViewModel
import com.ttings.beatwave.viewmodels.PlayerViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val tracks by viewModel.tracks.observeAsState(initial = emptyList())
    val playlists by viewModel.playlists.observeAsState(initial = emptyList())
    val authors by viewModel.authors.observeAsState(initial = emptyList())
    val chunkedTracks = tracks.shuffled().chunked(3)

    LaunchedEffect(Unit) {
        viewModel.loadPlaylistsAndAuthors()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTopAppBar(
            title = stringResource(id = R.string.home),
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate("UploadScreen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Upload,
                        contentDescription = "Upload track",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("NotifyScreen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        Column {
            Text(
                text = stringResource(id = R.string.home_rec),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }

        LazyRow {
            items(chunkedTracks.take(3)) { trackChunk ->
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    items(trackChunk) { track ->
                        TrackBar(
                            track = track,
                            authorName = "",
                            duration = viewModel.secondsToMinutesSeconds(track.duration),
                            onMoreClick = {},
                            onTrackClick = { playerViewModel.playTrack(track, trackChunk) }
                        )
                    }
                }
            }
        }

        Column {
            Text(
                text = stringResource(id = R.string.playlists),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(playlists.shuffled().take(3)) { playlist ->
                PlaylistBar(
                    playlistName = playlist.title,
                    authorName = "",
                    playlistImage = playlist.image,
                    onPlaylistClick = {
                        try {
                            navController.navigate("SelectedPlaylist/${playlist.playlistId}")
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                )
            }
        }

        Column {
            Text(
                text = stringResource(id = R.string.home_attention),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(authors.shuffled().take(5)) { author ->
                ArtistBar(
                    user = author,
                    onAuthorClick = {
                        if (author != null) {
                            val userId = author?.userId
                            navController.navigate("ProfileScreen/${userId}")
                        } else {
                            Timber.d("Author == null")
                        }
                    }
                )
            }
        }

    }
}