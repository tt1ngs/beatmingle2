package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTopAppBar(
            title = stringResource(R.string.library),
            actions = {
                IconButton(
                    onClick = {
                        /*TODO*/
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        if (user != null) {
                            navController.navigate("ProfileScreen/${user.value!!.userId}")
                        } else {
                            navController.navigate("LoginScreen")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        TextButton(
            onClick = {
                navController.navigate("LikedScreen")
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.liked_tracks),
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Liked"
                )
            }
        }
        TextButton(
            onClick = {
                navController.navigate("PlaylistsScreen")
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.playlist),
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Playlists"
                )
            }
        }
        TextButton(
            onClick = { /*TODO*/ }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.album),
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Albums"
                )
            }
        }
        TextButton(
            onClick = {
                navController.navigate("LibUploadScreen")
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.uploads),
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Uploads"
                )
            }
        }
    }
}