package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.ui.theme.LightGray
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun TrackBar(
    authorName: String,
    duration: String,
    track: Track,
    onTrackClick: (Track) -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .width(332.dp)
            .height(75.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = { onTrackClick(track) })
    ) {
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(64.dp)
                .align(Alignment.CenterVertically)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = track.image,
                    builder = {
                        crossfade(true)
                        fallback(R.drawable.logo)
                    }
                ),
                contentDescription = "Track Image",
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
                text = track.title,
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
                Icon(imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp))

                Text(
                    text = "0   $duration", //TODO
                    style = Typography.bodySmall,
                    modifier = Modifier.padding(start = 17.dp, top = 2.dp)
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
                    .clickable(onClick = onMoreClick)
            )
        }
    }
}