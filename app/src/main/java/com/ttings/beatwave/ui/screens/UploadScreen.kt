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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.ErrorBox
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.components.TrackDataField
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.UploadViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user = viewModel.currentUser
    val isLoading by viewModel.isLoading.collectAsState()


    val emptyFile = stringResource(id = R.string.empty_file)
    val emptyTitle = stringResource(id = R.string.empty_title)
    val emptyGenre = stringResource(id = R.string.empty_genre)
    val emptyImage = stringResource(id = R.string.empty_image)
    val successMessage = stringResource(id = R.string.success_message)

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var trackUri by remember { mutableStateOf<Uri?>(null) }

    var textValue by remember { mutableStateOf("") }
    var titleValue by remember { mutableStateOf("") }
    var genreValue by remember { mutableStateOf("") }
    var switchState by remember { mutableStateOf(true) }

    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
            }
        }
    )
    val trackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                trackUri = it
                val fileName = uri.lastPathSegment
                textValue = fileName.toString()
                Timber.tag("UploadScreen").d("File name: $fileName \n Track URI: $trackUri")
            }
        }
    )

    var isGenreSheetVisible by remember { mutableStateOf(false) }
    val genreScope = rememberCoroutineScope()
    val genreSheetState = rememberModalBottomSheetState()
    val genres by viewModel.genres.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTopAppBar(
            title = stringResource(id = R.string.upload),
            actions = {
                IconButton(
                    onClick = {
                        when {
                            titleValue.isEmpty() -> {
                                errorMessage = emptyTitle
                                showError = true
                            }
                            genreValue.isEmpty() -> {
                                errorMessage = emptyGenre
                                showError = true
                            }
                            imageUri == null -> {
                                errorMessage = emptyImage
                                showError = true
                            }
                            trackUri == null -> {
                                errorMessage = emptyFile
                                showError = true
                            }
                            else -> {
                                try {
                                    viewModel.uploadTrack(
                                        trackId = UUID.randomUUID().toString(),
                                        title = titleValue,
                                        genre = genreValue,
                                        artistIds = listOf(user.value!!.userId),
                                        albumId = null,
                                        duration = viewModel.getAudioFileDuration(context = context, audioUri = trackUri!!).toInt(),
                                        imageUri = imageUri.toString(),
                                        trackUri = trackUri.toString(),
                                        isPrivate = switchState
                                    )
                                    Timber.tag("UploadScreen").d("${viewModel.getAudioFileDuration(context = context, audioUri = trackUri!!).toInt()}")
                                } catch (e: Exception) {
                                    Timber.tag("UploadScreen").e(e, "Error uploading track")
                                }
                                errorMessage = successMessage
                                showError = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Save track",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
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

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackDataField(
                name = stringResource(id = R.string.filename),
                textValue = textValue,
                onTextChange = { textValue = it },
                onClick = {
                    trackLauncher.launch("audio/*")
                },
                exception = stringResource(id = R.string.empty_file),
                enabled = false,
            )
            TrackDataField(
                name = stringResource(id = R.string.title),
                textValue = titleValue,
                onTextChange = { titleValue = it },
                exception = stringResource(id = R.string.empty_field),
            )
            TrackDataField(
                name = stringResource(id = R.string.genre),
                textValue = genreValue,
                onTextChange = { genreValue = it },
                onClick = {
                    isGenreSheetVisible = true
                    genreScope.launch {
                        genreSheetState.show()
                    }
                },
                exception = stringResource(id = R.string.empty_genre),
                enabled = false,
            )
            Row(
                modifier = Modifier
                    .width(400.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
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
                        navController.popBackStack()
                    },
                ) {
                    showError = false
                }
            }
        }


        if (isGenreSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isGenreSheetVisible = false },
                sheetState = genreSheetState
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = {
                                genreScope.launch {
                                    genreSheetState.hide()
                                }.invokeOnCompletion {
                                    if (!genreSheetState.isVisible) {
                                        isGenreSheetVisible = false
                                    }
                                }
                            }) {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    style = Typography.bodyMedium,
                                )
                            }
                            Text(
                                text = stringResource(R.string.select_genre),
                                style = Typography.bodyMedium,
                                modifier = Modifier
                                    .padding(start = 63.dp)
                            )
                        }
                        genres.forEach { genre ->
                            TextButton(
                                onClick = {
                                    genreValue = genre
                                    genreScope.launch {
                                        genreSheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!genreSheetState.isVisible) {
                                            isGenreSheetVisible = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = genre,
                                    style = Typography.bodyMedium,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}