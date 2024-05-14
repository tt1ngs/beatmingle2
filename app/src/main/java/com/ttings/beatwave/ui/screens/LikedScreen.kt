package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.SearchField
import com.ttings.beatwave.ui.components.TopAppBar
import com.ttings.beatwave.ui.components.TrackBar
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.LikedViewModel
import com.ttings.beatwave.viewmodels.PlayerViewModel
import timber.log.Timber

@Composable
fun LikedScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: LikedViewModel
) {

    val likedTracks by viewModel.likedTracks.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    LaunchedEffect(user) {
        user?.userId?.let {
            if (likedTracks.isEmpty()) {
                viewModel.loadLikedTracks(it)
            }
        }
    }

    var authorNames by remember { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(likedTracks) {
        val names = likedTracks.flatMap { it.artistIds }.associateWith { id ->
            val user = viewModel.getAuthorById(id)
            if (user != null && user.username!!.isNotBlank()) {
                user.username
            } else {
                "Unknown Author"
            }
        }
        authorNames = names
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = stringResource(R.string.liked_tracks),
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
        if (likedTracks.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_liked_tracks),
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 230.dp)

            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(likedTracks.size) { index ->
                    val track = likedTracks[index]
                    val authorName = authorNames[track.artistIds[0]] ?: ""
                    TrackBar(
                        authorName = authorName,
                        duration = viewModel.secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = {
                            try {
                                playerViewModel.playTrack(track, likedTracks.subList(index + 1, likedTracks.size))

                            } catch (e: Exception) {
                                Timber.tag("LibUploadScreen").e(e)
                            }
                        },
                        onMoreClick = { /*TODO*/ }
                    )
                    if (index >= likedTracks.size - 1) {
                        LaunchedEffect(likedTracks.size) {
                            viewModel.loadLikedTracks(user!!.userId, likedTracks.size + 20)
                        }
                    }
                }
            }
        }
    }
}