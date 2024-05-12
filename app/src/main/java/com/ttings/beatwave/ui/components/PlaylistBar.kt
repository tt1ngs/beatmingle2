package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun PlaylistBar(
    playlistName: String,
    authorName: String,
    playlistImage: String
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(118.dp, 160.dp)
            .clip(shape = RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Image(
            painter = rememberImagePainter(
                data = playlistImage,
                builder = {
                    crossfade(true)
                    fallback(R.drawable.logo)
                }
            ),
            contentDescription = "Track Image",
            modifier = Modifier
                .padding(top = 8.dp)
                .size(102.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(CenterHorizontally),
            contentScale = ContentScale.Crop)
        Text(
            text = playlistName,
            style = Typography.bodyMedium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(CenterHorizontally)
        )
        Text(
            text = authorName,
            style = Typography.titleSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(CenterHorizontally)

        )
    }
}

@Preview
@Composable
fun PlaylistBarPreview() {
    PlaylistBar(
        playlistName = "Playlist",
        authorName = "Author",
        playlistImage = "https://www.example.com/image.jpg"
    )
}