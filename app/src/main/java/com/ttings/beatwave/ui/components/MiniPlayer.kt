package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    currentUser: User,
    track: Track,
    playlists: List<Playlist>,
    author: String,
    isPlaying: Boolean,
    isLiked: Boolean,
    currentProgress: Float,
    onSelectedPlaylist: (Playlist) -> Unit,
    onPlayPauseClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit
) {

    val swipeThreshold = 350f

    var isPlayerSheetVisible by remember { mutableStateOf(false) }
    val playerScope = rememberCoroutineScope()
    val playerSheetState = rememberModalBottomSheetState()

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select a playlist") },
            text = {
                LazyColumn {
                    items(playlists.size) { playlist ->
                        val playlist = playlists[playlist]
                        TextButton(
                            onClick = {
                                onSelectedPlaylist(playlist)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                isPlayerSheetVisible = true
                playerScope.launch {
                    playerSheetState.show()
                }
            }
            .pointerInput(Unit) {
                var totalDragAmount = 0f
                detectHorizontalDragGestures { change, dragAmount ->
                    totalDragAmount += dragAmount
                    Timber.d("Drag amount: $dragAmount, Total drag: $totalDragAmount")
                    if (abs(totalDragAmount) > swipeThreshold) {
                        if (totalDragAmount > 0) {
                            Timber.d("Triggering previous track.")
                            onPreviousTrack()
                        } else {
                            Timber.d("Triggering next track.")
                            onNextTrack()
                        }
                        totalDragAmount = 0f // Сброс после обработки свайпа
                    }
                    change.consumeAllChanges() // Указываем, что изменение "потреблено" и не должно обрабатываться дальше
                }
            }
    ) {
        Row {
            Column {
                IconButton(
                    onClick = {
                        onPlayPauseClicked()
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                    )
                }
            }
            Column(
                modifier = Modifier
                    .height(48.dp)
                    .width(IntrinsicSize.Max)
                    .padding(start = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    Text(
                        text = track.title,
                        style = Typography.bodyMedium,
                        maxLines = 1
                    )
                }
                Row {
                    Text(
                        text = author,
                        style = Typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .weight(0.18f),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = {
                        onFavoriteClicked()
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Play/Pause",
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = currentProgress,
            modifier = Modifier.fillMaxWidth()
        )

        if (isPlayerSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isPlayerSheetVisible = false },
                sheetState = playerSheetState,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    /*Image(
                        painter = rememberImagePainter(data = track.image),
                        contentDescription = "Track cover",
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp, 20.dp),
                        contentScale = ContentScale.Crop,
                    )*/
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CustomTopAppBar(
                            title = "", //TODO: playlist name
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        playerScope.launch { playerSheetState.hide() }.invokeOnCompletion {
                                            if (!playerSheetState.isVisible) {
                                                isPlayerSheetVisible = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )

                        Image(
                            painter = rememberImagePainter(data = track.image),
                            contentDescription = "Track cover",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(300.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(10)),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier
                                .padding(top = 20.dp, bottom = 30.dp)
                                .width(300.dp)
                        ) {
                            Text(
                                text = track.title,
                                style = Typography.bodyMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Text(
                                text = author,
                                style = Typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        LinearProgressIndicator(
                            progress = currentProgress,
                            modifier = Modifier
                                .width(300.dp)
                                .padding(bottom = 30.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
//                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(10))
                                .clip(RoundedCornerShape(10)),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    showDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add to playlist",
                                    Modifier.scale(1.5f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onPreviousTrack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous track",
                                    Modifier.scale(2f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onPlayPauseClicked()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    Modifier.scale(2f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onNextTrack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "Next track",
                                    Modifier.scale(2f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    onFavoriteClicked()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Play/Pause",
                                    Modifier.scale(1.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}