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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.*
import com.ttings.beatwave.ui.theme.DarkGray
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = userId) {
        viewModel.loadUser(userId)
    }

    var isEditProfileVisible by remember { mutableStateOf(false) }
    val editProfileState = rememberModalBottomSheetState()
    val profileScope = rememberCoroutineScope()

    var profileImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var backgroundImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val currentUser = viewModel.currentUser
    val user by viewModel.user.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                profileImageUri = it
            }
        }
    )
    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                backgroundImageUri = it
            }
        }
    )

    if (user != null) {
        LazyColumn {
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
                    onEditClick = {
                        isEditProfileVisible = true
                        profileScope.launch {
                            editProfileState.show()
                        }
                    },
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
                    onMoreTracksClick = { navController.navigate("SelectedUserItems/${userId}/${0}") },
                    onMorePlaylistsClick = { navController.navigate("SelectedUserItems/${userId}/${1}") },
                    onMoreAlbumsClick = { navController.navigate("SelectedUserItems/${userId}/${2}") },
                    onMoreLikesClick = { navController.navigate("SelectedUserItems/${userId}/${3}") },
                    viewModel = playerViewModel,
                    navController = navController
                )
            }
        }

        if (isEditProfileVisible) {
            ModalBottomSheet(
                onDismissRequest = { isEditProfileVisible = false },
                sheetState = editProfileState
            ) {
                Column(
                    modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)
                ) {
                    CustomTopAppBar(
                        title = stringResource(R.string.edit_profile),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    isEditProfileVisible = false
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
                }
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.profile_image),
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
                                    avatarLauncher.launch("image/*")
                                }
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            val painter = rememberImagePainter(
                                data = profileImageUri ?: user?.avatar ?: R.drawable.beatminglelogo,
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
                            text = stringResource(id = R.string.profile_background),
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
                                .width(300.dp)
                                .height(150.dp)
                                .background(color = DarkGray, shape = RoundedCornerShape(16.dp))
                                .clickable {
                                    backgroundLauncher.launch("image/*")
                                }
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            val painter = rememberImagePainter(
                                data = backgroundImageUri ?: user?.background ?: R.drawable.beatminglelogo,
                                builder = {
                                    crossfade(true)
                                    error(R.drawable.beatminglelogo)
                                }
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Background Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(id = R.string.username),
                            style = Typography.bodyMedium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        var username by remember { mutableStateOf(user?.username ?: "") }
                        DataField(
                            value = username,
                            onValueChange = { username = it },
                            label = stringResource(R.string.username),
                            topPadding = 10,
                            bottomPadding = 10
                        )
                        AuthButton(
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    if (profileImageUri != null) {
                                        viewModel.uploadImage(profileImageUri!!)
                                    }
                                    if (backgroundImageUri != null) {
                                        viewModel.uploadBackground(backgroundImageUri!!)
                                    }
                                    if (username != user?.username) {
                                        viewModel.updateUserName(username)
                                    }
                                }
                            },
                            text = stringResource(id = R.string.save),
                        )
                    }
                }
            }
        }
    }
}
