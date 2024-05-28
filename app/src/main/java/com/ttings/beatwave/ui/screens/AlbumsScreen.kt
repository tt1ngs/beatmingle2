package com.ttings.beatwave.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.ui.components.*
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.AlbumsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    navController: NavController,
    viewModel: AlbumsViewModel = hiltViewModel()
) {

    val currentUser by viewModel.currentUser.collectAsState()

    val albums by viewModel.albums.observeAsState(setOf<Pair<Playlist, String>>())
    val likedAlbums by viewModel.likedAlbums.observeAsState(setOf<Pair<Playlist, String>>())
    val uploadedAlbums by viewModel.uploadedAlbums.observeAsState(setOf<Pair<Playlist, String>>())

    var showDialog by remember { mutableStateOf(false) }
    var authorStatus by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        authorStatus = viewModel.getAuthorStatus()
    }

    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf(stringResource(id = R.string.all), stringResource(id = R.string.liked_albums), stringResource(
        id = R.string.created))

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var switchState by remember { mutableStateOf(true) }

    val emptyTitle = stringResource(id = R.string.empty_title)
    val emptyImage = stringResource(id = R.string.empty_image)

    val isLoading by viewModel.isLoading.collectAsState()
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var titleValue by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_albums),
                    style = Typography.titleMedium
                ) },
            text = {
                Column {
                    options.forEachIndexed { index, option ->
                        TextButton(
                            onClick = {
                                selectedOption = index
                                showDialog = false
                            }
                        ) {
                            Text(
                                text = option,
                                style = Typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    val currentAlbums = when (selectedOption) {
        0 -> albums
        1 -> likedAlbums
        2 -> uploadedAlbums
        else -> albums
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        CustomTopAppBar(
            title = stringResource(R.string.album),
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
                        showDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Timber.tag("AlbumsScreen").d("[124] authorStatus: $authorStatus")
            if (authorStatus) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    CreatePlaylist(
                        infoText = stringResource(R.string.create_album),
                        onClick = {
                            showBottomSheet = true
                        }
                    )
                }
            }

            val albumsList = currentAlbums.toList()
            items(albumsList.size) { index ->
                val (playlist, authorName) = albumsList[index]
                Timber.tag("AlbumScreen").d("Playlist: ${playlist.isPrivate}")
                PlaylistBarSlim(
                    playlistName = playlist.title,
                    playlistImage = playlist.image,
                    authorName = authorName,
                    isPrivate = playlist.isPrivate,
                    duration = "",
                    onPlaylistClick = {
                        Timber.tag("PlaylistScreen").d("Playlist clicked: $playlist")
                        try{
                            navController.navigate("SelectedAlbum/${playlist.playlistId}")

                        } catch (e: Exception) {
                            Timber.tag("AlbumScreen").e(e)
                        }
                    },
                    onMoreClick = {

                    }
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomTopAppBar(
                    title = stringResource(R.string.create_playlist),
                    actions = {
                        IconButton(
                            onClick = {
                                when {
                                    titleValue.isEmpty() -> {
                                        errorMessage = emptyTitle
                                        showError = true
                                    }
                                    imageUri == null -> {
                                        errorMessage = emptyImage
                                        showError = true
                                    }
                                    else -> {
                                        try {
                                            viewModel.saveAlbum(
                                                title = titleValue,
                                                imageUri = imageUri!!,
                                                userId = currentUser!!.userId,
                                                isPrivate = switchState
                                            )
                                        } catch (e: Exception) {
                                            Timber.tag("PlaylistScreen").e(e)}
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = "Save playlist",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
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
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .size(250.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable {
                            imageLauncher.launch("image/*")
                        }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(300.dp)
                                .clip(RoundedCornerShape(15.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val image: Painter = painterResource(id = R.drawable.beatminglelogo)
                        Image(
                            painter = image,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(220.dp)
                        )
                    }
                }
                TrackDataField(
                    name = stringResource(id = R.string.title),
                    textValue = titleValue,
                    onTextChange = { titleValue = it },
                    exception = stringResource(id = R.string.empty_playlistName),
                )
                Row(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.privacy_settings),
                            style = Typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (switchState)
                                stringResource(id = R.string.privacy_private)
                            else
                                stringResource(id = R.string.privacy_public),
                            style = Typography.bodySmall,
                            modifier = Modifier.padding(end = 7.dp)
                        )
                        Switch(
                            checked = switchState,
                            onCheckedChange = { switchState = it }
                        )
                    }
                }
                if (showError) {
                    ErrorBox(
                        errorMessage = errorMessage,
                        onDismiss = {
                            showError = false
                            showBottomSheet = false
                        },
                        onConfirmation = {
                            showError = false
                        }
                    )
                }
            }
        }

    }

}