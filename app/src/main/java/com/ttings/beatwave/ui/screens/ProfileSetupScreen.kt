package com.ttings.beatwave.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.AuthButton
import com.ttings.beatwave.ui.components.DataField
import com.ttings.beatwave.ui.components.ErrorBox
import com.ttings.beatwave.ui.theme.DarkGray
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.ProfileSetupViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(
    navController: NavController,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val usernameError = stringResource(R.string.enter_username)
    val imageError = stringResource(R.string.select_image)

    var username by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.profile_creation),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )


        Box(
            modifier = Modifier
                .padding(top = 50.dp)
                .size(300.dp)
                .background(color = DarkGray, shape = CircleShape)
                .clickable {
                    // Запуск выбора изображения из галереи
                    launcher.launch("image/*")
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
                        .clip(RoundedCornerShape(50)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val image: Painter = painterResource(id = R.drawable.beatminglelogo)
                Image(
                    painter = image,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(270.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.profile_image),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            textAlign = TextAlign.Center,
            style = Typography.titleMedium
        )
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            DataField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.username),
                topPadding = 50,
                bottomPadding = 240
            )
            AuthButton(
                onClick = {
                    when {
                        username.isEmpty() -> {
                            errorMessage = usernameError
                            showError = true
                        }
                        imageUri == null -> {
                            errorMessage = imageError
                            showError = true
                        }
                        else -> {
                            // Сохранение данных
                            viewModel.viewModelScope.launch {
                                imageUri?.let { viewModel.uploadImage(it) }
                                viewModel.updateUserName(username)
                            }
                            navController.navigate("HomeScreen")
                        }
                    }
                },
                text = stringResource(R.string.save),
                bottomPadding = 40
            )
            if (showError) {
                ErrorBox(
                    errorMessage = errorMessage,
                    onDismiss = {
                        showError = false
                        navController.popBackStack()
                    },
                    onConfirmation = {
                        showError = false
                    }
                )
            }
        }
    }
}