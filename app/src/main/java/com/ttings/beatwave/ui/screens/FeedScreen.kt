package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.components.TrackCard
import com.ttings.beatwave.viewmodels.FeedViewModel
import timber.log.Timber
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.CommentBar
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.components.DataField
import com.ttings.beatwave.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel()
) {

    var selectedTrackId by remember { mutableStateOf<String?>(null) }
    val currentUser by viewModel.currentUser.collectAsState()

    val commentString = stringResource(id = R.string.ur_comment)

    var currentTrack by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }

    var isCommentSheetVisible by remember { mutableStateOf(false) }
    val commentScope = rememberCoroutineScope()
    val commentSheetState = rememberModalBottomSheetState()

    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(pageCount = { tracks.itemCount })

    var showDialog by remember { mutableStateOf(false) }
    val playlists by viewModel.playlists

    LaunchedEffect(currentUser) {
        currentUser?.let {
            viewModel.fetchUserPlaylists(it.userId)
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
                                viewModel.addTrackToPlaylist(
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

    VerticalPager(state = pagerState) { page ->
        tracks[page]?.let { track ->
            val author by produceState<User?>(initialValue = null) {
                try {
                    value = viewModel.getAuthorById(track.artistIds.first())
                } catch (e: Exception) {
                    Timber.tag("TrackCard").e(e, "Error getting author")
                    value = null
                }
            }

            LaunchedEffect(track.trackId) {
                viewModel.updateLikeState(track.trackId)
                viewModel.updateFollowState(track.artistIds.first())
            }

            val isLiked by viewModel.isTrackLiked.collectAsState()
            val isFollowed by viewModel.isUserFollowed.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()

            currentTrack = track.trackId

            DisposableEffect(track) {
                onDispose {
                    if (viewModel.currentPlayingTrack == track) {
                        viewModel.stopPlayback()
                    }
                }
            }

            TrackCard(
                track = track,
                isLiked = isLiked,
                isPlaying = isPlaying,
                isFollowed = isFollowed,
                onPlayPauseClick = {
                    if (isPlaying) {
                        viewModel.stopPlayback()
                    } else {
                        viewModel.startPlayback(track)
                    }
                },
                onLikeClick = {
                    if (isLiked) {
                        viewModel.deleteTrackFromLibrary(track.trackId)
                    } else {
                        viewModel.addTrackToLibrary(track.trackId)
                    }
                },
                onCommentClick = {
                    isCommentSheetVisible = true
                    commentScope.launch {
                        viewModel.loadComments(track.trackId)
                        commentSheetState.show()
                    }
                },
                onAuthorClick = {
                    if (author != null) {
                        val userId = author?.userId
                        navController.navigate("ProfileScreen/${userId}")
                    } else {
                        Timber.d("Author == null")
                    }
                },
                onFollowClick = {
                    if (isFollowed) {
                        viewModel.unfollowUser(track.artistIds.first())
                    } else {
                        viewModel.followUser(track.artistIds.first())
                    }
                },
                onAddToPlaylist = {
                    showDialog = true
                    selectedTrackId = track.trackId
                },
                author = author ?: User("")
            )

            if (isCommentSheetVisible) {
                val comments by viewModel.comments.collectAsState()
                Timber.tag("Comments").d("Comments: $comments")
                ModalBottomSheet(
                    onDismissRequest = {
                        isCommentSheetVisible = false
                        commentScope.launch {
                            commentSheetState.hide()
                        }
                    },
                    sheetState = commentSheetState,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        CustomTopAppBar(
                            title = stringResource(R.string.comments),
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        isCommentSheetVisible = false
                                        commentScope.launch {
                                            commentSheetState.hide()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(600.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            try {
                                items(comments.size) { index ->
                                    val comment = comments[index]
                                    val user by produceState<User?>(initialValue = null) {
                                        value = viewModel.getUserById(comment.userId)
                                    }
                                    if (user != null) {
                                        CommentBar(
                                            user = user!!,
                                            comment = comment.comment,
                                            onUserClick = {
                                                navController.navigate("ProfileScreen/${user?.userId}")
                                            }
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.tag("FeedScreen").e(e, "Error getting comments")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 10.dp, vertical = 25.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            DataField(
                                value = comment,
                                onValueChange = { comment = it },
                                label = commentString,
                                topPadding = 0,
                                width = 280
                            )
                            IconButton(
                                onClick = {
                                    try {
                                        currentTrack.let { track ->
                                            viewModel.addComment(track, comment)
                                        }
                                    } catch (e: Exception) {
                                        Timber.tag("FeedScreen").e(e, "Error adding comment")
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(50)
                                    )

                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
