package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.SearchField
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.components.TrackBar
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.LikedViewModel
import com.ttings.beatwave.viewmodels.PlayerViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: LikedViewModel
) {
    var selectedTrackId by remember { mutableStateOf<String?>(null) }

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    val likedTracks by viewModel.likedTracks.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val playlists by playerViewModel.playlists

    LaunchedEffect(user) {
        user?.userId?.let {
            if (likedTracks.isEmpty()) {
                viewModel.loadLikedTracks(it)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_a_playlist),
                    style = Typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn {
                    items(playlists.size) { playlist ->
                        val playlist = playlists[playlist]
                        TextButton(
                            onClick = {
                                playerViewModel.addTrackToPlaylist(
                                    trackId = selectedTrackId!!,
                                    playlistId = playlist.playlistId
                                )
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(playlist.title)
                        }
                    }
                }
            },
            confirmButton = { }
        )
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
        CustomTopAppBar(
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
                        onMoreClick = { offset ->
                            menuOffset = offset
                            selectedTrackId = track.trackId
                            showMenu = true
                        }
                    )
                    if (index >= likedTracks.size - 1) {
                        LaunchedEffect(likedTracks.size) {
                            viewModel.loadLikedTracks(user!!.userId, likedTracks.size + 20)
                        }
                    }
                    val authorId = track.artistIds[0]
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = menuOffset.toDpOffset()
                    ) {
                        if (user?.userId == authorId) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.edit),
                                        style = Typography.bodySmall
                                    )
                                },
                                onClick = { /*TODO: EditModalBottomSheet*/ }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.delete_or_add),
                                    style = Typography.bodySmall
                                )
                            },
                            onClick = { viewModel.toggleTrackInLibrary(track.trackId, user!!) }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.add_to_playlist),
                                    style = Typography.bodySmall
                                )
                            },
                            onClick = {
                                showDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.authors_profile),
                                    style = Typography.bodySmall
                                )
                            },
                            onClick = {
                                try {
                                    navController.navigate("ProfileScreen/${authorId}")
                                } catch (e: Exception) {
                                    Timber.tag("LikedScreen").e(e)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Offset.toDpOffset(): DpOffset {
    return DpOffset(
        x = with(LocalDensity.current) { this@toDpOffset.x.toDp() },
        y = with(LocalDensity.current) { this@toDpOffset.y.toDp() }
    )
}
