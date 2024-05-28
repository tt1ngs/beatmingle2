package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Vibe
import com.ttings.beatwave.ui.theme.BeatwaveTheme
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun VibesScreen(
    navController: NavController,
    vibe: Vibe,
) {

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier.height(156.dp)
        ) {

            Image(
                painter = painterResource(id = vibe.location),
                contentDescription = "Vibe image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart)
                    .background(color = MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back button"
                )
            }

            Text(
                text = vibe.name,
                style = Typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            )

        }

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.height(30.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.all),
                    style = Typography.bodyMedium
                )
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.tracks),
                    style = Typography.bodyMedium
                )
            }
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.playlist),
                    style = Typography.bodyMedium
                )
            }
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.album),
                    style = Typography.bodyMedium
                )
            }
        }

        // Отображение контента в зависимости от выбранной вкладки
        when (selectedTab) {
            0 -> {

            }
            1 -> { /* Код для отображения плейлистов */ }
            2 -> { /* Код для отображения альбомов */ }
            3 -> { /* Код для отображения альбомов */ }
        }
        
    }

}

@Preview
@Composable
fun PreviewVibesScreen() {
    BeatwaveTheme {
        Surface {
            VibesScreen(
                navController = NavController(context = LocalContext.current),
                vibe = Vibe(
                    name = "Pop",
                    location = R.drawable.vibes_pop,
                ),
            )
        }
    }
}