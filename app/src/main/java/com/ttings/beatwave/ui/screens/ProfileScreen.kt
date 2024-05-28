package com.ttings.beatwave.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.ui.components.UserContent
import com.ttings.beatwave.ui.components.UserPanel
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = userId) {
        viewModel.loadUser(userId)
    }

    var isEditProfileVisible by remember { mutableStateOf(false) }
    val profileScope = rememberCoroutineScope()
    val editProfileState = rememberModalBottomSheetState()

    var profileImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var backgroundImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

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
                    onFollowingClick = { /* TODO: add Follows Screen */ },
                    onFollowerClick = { /* TODO: add Followers Screen */ },
                    onEditClick = { /* TODO */ },
                    onPlayClick = { /* TODO: add Play User Tracks */ }
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

    if (isEditProfileVisible) {
        ModalBottomSheet(
            onDismissRequest = { isEditProfileVisible = false },
            sheetState = editProfileState
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        profileScope.launch {
                            editProfileState.hide()
                        }.invokeOnCompletion {
                            if (!editProfileState.isVisible) {
                                isEditProfileVisible = false
                            }
                        }
                    }
                    ) {
                        Text(text = "Назад")
                    }
                    Text(text = "Настройка профиля")
                    TextButton(onClick = {
                        viewModel.viewModelScope.launch {
                            profileImageUri?.let { viewModel.uploadImage(it) }
                            backgroundImageUri?.let { viewModel.uploadBackground(it) }
                            viewModel.updateUserName(user?.username ?: "")
                        }


                        profileScope.launch {
                            editProfileState.hide()
                        }.invokeOnCompletion {
                            if (!editProfileState.isVisible) {
                                isEditProfileVisible = false
                            }
                        }
                        // TODO save changes
                    }
                    ) {
                        Text(text = "Сохранить")
                    }
                }

                Text(text = "Изображение профиля")
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)
                        .align(Alignment.CenterHorizontally)

                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = profileImageUri,
                            builder = {
                                crossfade(true)
                                fallback(defaultImage)
                            }
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = {
                                pickProfileImageLauncher.launch("image/*")
                            }),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.padding(16.dp))
                CustomText(text = "Задний фон профиля")
                Box(modifier = Modifier
                    .height(84.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .border(2.dp, Color.LightGray)

                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = backgroundImageUri,
                            builder = {
                                crossfade(true)
                                fallback(defaultImage)
                            }
                        ),
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .height(84.dp)
                            .fillMaxWidth()
                            .clickable(onClick = {
                                pickBackgroundImageLauncher.launch("image/*")
                            }),
                        contentScale = ContentScale.FillWidth
                    )
                }

                Spacer(modifier = Modifier.padding(16.dp))
                CustomText(text = "Имя пользователя")
                CustomTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    topPadding = 0
                )



                // Add more content here
            }
        }
    }

}