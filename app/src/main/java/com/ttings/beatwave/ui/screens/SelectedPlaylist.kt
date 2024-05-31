package com.ttings.beatwave.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.*
import com.ttings.beatwave.ui.theme.DarkGray
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.SelectedPlaylistViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedPlaylist(
    navController: NavController,
    playlistId: String,
    playerViewModel: PlayerViewModel,
    viewModel: SelectedPlaylistViewModel
) {

    var isEditPlaylistVisible by remember { mutableStateOf(false) }
    val editState = rememberModalBottomSheetState()
    val editScope = rememberCoroutineScope()

    val user by viewModel.currentUser.collectAsState()
    val playlistTracks by viewModel.playlistTracks.collectAsState()
    val playlist by viewModel.playlist.observeAsState()
    val creator by viewModel.creator.observeAsState()
    val isPlaylistLiked by viewModel.isPlaylistLiked.observeAsState()

    var switchState by remember { mutableStateOf(playlist?.isPrivate ?: false) }

    var showAlert by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                coverUri = it
            }
        }
    )

    LaunchedEffect(key1 = true) {
        viewModel.loadPlaylist(playlistId)
        viewModel.checkIfPlaylistLiked(playlistId)
    }

    LaunchedEffect(user) {
        user?.userId?.let {
            if (playlistTracks.isEmpty()) {
                viewModel.loadPlaylistTracks(playlistId)
            }
        }
    }

    var authorNames by remember { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(playlistTracks) {
        try {
            val names = playlistTracks.flatMap { it.artistIds }.associateWith { id ->
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
                            playerViewModel.playTrack(playlistTracks[0], playlistTracks)
                        },
                        onMoreClick = {
                            isEditPlaylistVisible = true
                        },
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

            playlistTracks.let { tracks ->
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
                            onMoreClick = { offset ->
                                menuOffset = offset
                                showMenu = true
                            }
                        )
                        if (index >= tracks.size - 1) {
                            LaunchedEffect(tracks.size) {
                                // TODO: rework queue
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
                                            text = stringResource(id = R.string.delete_from_playlist),
                                            style = Typography.bodySmall
                                        )
                                    },
                                    onClick = {
                                        viewModel.deleteTrack(
                                            trackId = track.trackId,
                                            playlistId = playlistId
                                        )
                                        Timber.tag("SelectedPlaylist").d("Delete track ${track.trackId} from playlist $playlistId")
                                    }
                                )
                            }
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

            if (user?.userId == creator?.userId) {
                item { SuggestedSection(playlistId = playlistId) }
            }
        }

        if (showAlert) {
            ErrorBox(
                errorMessage = stringResource(id = R.string.delete_warning),
                onDismiss = { showAlert = false },
                onConfirmation = {
                    viewModel.deletePlaylist(playlistId)
                    isEditPlaylistVisible = false
                    navController.popBackStack()
                }
            )
        }

        if (isEditPlaylistVisible) {
            ModalBottomSheet(
                onDismissRequest = { isEditPlaylistVisible = false },
                sheetState = editState
            ) {
                CustomTopAppBar(
                    title = stringResource(id = R.string.edit_playlist),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                isEditPlaylistVisible = false
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.cover),
                            style = Typography.bodyMedium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .size(300.dp)
                                .background(color = DarkGray, shape = CircleShape)
                                .clickable {
                                    coverLauncher.launch("image/*")
                                }
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            val painter = rememberImagePainter(
                                data = coverUri ?: playlist?.image ?: R.drawable.beatminglelogo,
                                builder = {
                                    crossfade(true)
                                    error(R.drawable.beatminglelogo)
                                }
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(270.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(id = R.string.playlist_name),
                            style = Typography.bodyMedium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        var name by remember { mutableStateOf(playlist?.title ?: "") }
                        DataField(
                            value = name,
                            onValueChange = { name = it },
                            label = stringResource(R.string.title),
                            topPadding = 0,
                            bottomPadding = 10
                        )
                        Switch(
                            checked = switchState,
                            onCheckedChange = { switchState = it }
                        )
                        AuthButton(
                            onClick = {
                                if (coverUri != null && coverUri.toString() != playlist?.image) {
                                    viewModel.updatePlaylistCover(playlistId, coverUri!!)
                                }
                                if (playlist?.title != name) {
                                    viewModel.updatePlaylistName(playlistId, name)
                                }
                                if (playlist?.isPrivate != switchState) {
                                    viewModel.updatePlaylistPrivacy(playlistId, switchState)
                                }
                                isEditPlaylistVisible = false
                            },
                            text = stringResource(id = R.string.save),
                            bottomPadding = 15
                        )
                        AuthButton(
                            onClick = {
                                showAlert = true
                            },
                            text = stringResource(id = R.string.delete),
                            bottomPadding = 15
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