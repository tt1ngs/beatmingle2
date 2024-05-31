package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun PlaylistPanel(
    playlist: Playlist,
    currentUser: User,
    isLiked: Boolean,
    trackCount: Int,
    playlistDuration: String,
    profileImage: String,
    username: String,
    onLikeClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onMoreClick: () -> Unit,
    onPlayClick: () -> Unit,
    onUserClick: () -> Unit

) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = playlist.image,
                    builder = {
                        fallback(R.drawable.logo)
                        error(R.drawable.ic_launcher_background)
                    }
                ),
                contentDescription = "Playlist Image",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(100.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(10)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .padding(vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = playlist.title,
                        style = Typography.bodyMedium
                    )
                }
                Row {
                    Text(
                        text = "${if (playlist.isPrivate) stringResource(id = R.string.privacy_private) else stringResource(
                            id = R.string.privacy_public)} • $trackCount ${stringResource(id = R.string.tracks)} • $playlistDuration",
                        style = Typography.bodySmall,
//                        modifier = Modifier.clickable(onClick = { TODO(): --- })
                    )
                }
                Row(
                    modifier = Modifier.clickable { onUserClick() }
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = profileImage,
                            builder = {
                                fallback(R.drawable.logo)
                                error(R.drawable.ic_launcher_background)
                            }
                        ),
                        contentDescription = "",
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(35.dp)
                            .clip(RoundedCornerShape(50))
                            .align(Alignment.CenterVertically),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "By $username",
                        style = Typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector =
                        if (isLiked) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        contentDescription = "Add to favorite")
                }
                if (currentUser.userId == playlist.userId) {
                    IconButton(
                        onClick = { onMoreClick() },
                        modifier = Modifier.padding(start = 45.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "More")
                    }
                }
            }
            Box() {
                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.padding(end = 45.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle")
                }
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.padding(start = 45.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play")
                }
            }
        }
    }
}