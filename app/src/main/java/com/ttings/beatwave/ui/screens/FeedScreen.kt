package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.components.TrackCard
import com.ttings.beatwave.viewmodels.FeedViewModel
import timber.log.Timber
import androidx.compose.runtime.getValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(pageCount = { tracks.itemCount })

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

            val isFollowed by viewModel.isUserFollowed.collectAsState()
            val isLiked by viewModel.isTrackLiked.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()

            // Остановка проигрывания трека при смене карточки
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
                    if (isFollowed) {
                        viewModel.unfollowUser(track.artistIds.first())
                    } else {
                        viewModel.followUser(track.artistIds.first())
                    }
                },
                onAddToPlaylist = { /*TODO*/ },
                author = author ?: User("")
            )
        }
    }
}