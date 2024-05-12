package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun PlaylistBarSlim(
    playlistName: String,
    authorName: String,
    playlistImage: String,
    isPrivate: Boolean,
    duration: String,
    onPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .width(332.dp)
            .height(75.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = { onPlaylistClick() })
    ) {
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(64.dp)
                .align(Alignment.CenterVertically)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = playlistImage,
                    builder = {
                        crossfade(true)
                        fallback(R.drawable.logo)
                    }
                ),
                contentDescription = "Playlist Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop)

        }
        Column(
            modifier = Modifier
                .width(240.dp)
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)
        ) {
            Text(
                text = playlistName,
                style = Typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(start = 5.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = authorName,
                style = Typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.padding(start = 5.dp)
            )
            Box {
                Text(
                    text = "${if (isPrivate) stringResource(id = R.string.privacy_private) else stringResource(id = R.string.privacy_public)}    $duration",
                    style = Typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 11.dp)

        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "More",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { onMoreClick() })
            )
        }
    }
}