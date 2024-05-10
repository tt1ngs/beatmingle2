package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.ui.components.UserContent
import com.ttings.beatwave.ui.components.UserPanel
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = userId) {
        viewModel.loadUser(userId)
    }

    val currentUser = viewModel.currentUser
    val user by viewModel.user.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    if (user != null) {
        LazyColumn() {
            item {
                UserPanel(
                    user = user!!,
                    currenUserId = currentUser.value?.userId ?: "",
                    following = followingCount.toString(),
                    followers = followersCount.toString(),
                    isFollowed = isFollowing ?: false,
                    onFollowClick = { viewModel.onFollowButtonClick(user!!.userId) },
                    onBackClick = { navController.popBackStack() },
                    onFollowingClick = { },
                    onFollowerClick = { },
                    onEditClick = { },
                    onPlayClick = { }
                )
            }
            item {
                UserContent(
                    user = user!!,
                    userUploadsList = viewModel.userUploads.collectAsState().value,
                    userPlaylistsList = viewModel.userPlaylists.collectAsState().value,
                    userAlbumsList = viewModel.userAlbums.collectAsState().value,
                    userLikesList = viewModel.userLikes.collectAsState().value,
                    onMoreTracksClick = { },
                    onMorePlaylistsClick = { },
                    onMoreAlbumsClick = { },
                    onMoreLikesClick = { },
                )
            }
        }
    }
}