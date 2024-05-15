package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTopAppBar(
            title = stringResource(id = R.string.home),
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate("UploadScreen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Upload,
                        contentDescription = "Upload track",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("NotifyScreen")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        Column {
            Text(
                text = stringResource(id = R.string.home_rec),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }
        Column {
            Text(
                text = stringResource(id = R.string.home_mixes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }
        Column {
            Text(
                text = stringResource(id = R.string.home_attention),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = Typography.titleMedium
            )
        }
    }
}