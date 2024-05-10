package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.components.TrackCard
import com.ttings.beatwave.viewmodels.FeedViewModel
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(pageCount = { tracks.itemCount })
    val isPlaying by viewModel.isPlaying.collectAsState()

    if (tracks.itemCount > 0) {
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

                val isFollowing by produceState(initialValue = false) {
                    try {
                        value = viewModel.isFollowing(track.artistIds.first())
                    } catch (e: Exception) {
                        Timber.tag("TrackCard").e(e, "Error checking if following")
                        value = false
                    }
                }

                val isLiked by produceState(initialValue = false) {
                    try {
                        value = viewModel.isLiked(track.trackId)
                    } catch (e: Exception) {
                        Timber.tag("TrackCard").e(e, "Error checking if liked")
                        value = false
                    }
                }

                if (tracks.itemCount > 0) {
                    VerticalPager(state = pagerState, onPageChange = { page ->
                        if (isPlaying) {
                            viewModel.stopPlayback()
                        }
                    }) { page ->
                        TrackCard(
                            track = track,
                            isLiked = isLiked,
                            isPlaying = isPlaying,
                            isFollowed = isFollowing,
                            onPlayPauseClick = { viewModel.togglePlayPause(track) },
                            onLikeClick = {
                                if (isLiked) {
                                    viewModel.deleteTrackFromLibrary(track.trackId)
                                } else {
                                    viewModel.addTrackToLibrary(track.trackId)
                                }
                            },
                            onCommentClick = { /*TODO: onCommentClick FeedScreen*/ },
                            onAuthorClick = {
                                if (author != null) {
                                    val userId = author?.userId
                                    navController.navigate("ProfileScreen/${userId}")
                                } else {
                                    Timber.d("Author == null")
                                }
                            },
                            onFollowClick = {
                                if (isFollowing) {
                                    viewModel.unfollowUser(track.artistIds.first())
                                } else {
                                    viewModel.followUser(track.artistIds.first())
                                }
                            },
                            onAddToPlaylist = { /*TODO*/ },
                            author = author ?: User("")
                        )
                    }
                } else {
                    Text(text = "No tracks found")
                }


            }
        }
    } else {
        Text(text = "No tracks found")
    }
}