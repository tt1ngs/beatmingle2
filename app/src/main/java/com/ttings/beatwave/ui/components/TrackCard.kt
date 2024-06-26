package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun TrackCard(
    track: Track,
    isLiked: Boolean,
    isPlaying: Boolean,
    isFollowed: Boolean,
    onPlayPauseClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onFollowClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    author: User

) {

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 32.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10))) {
            Image(
                painter = rememberImagePainter(data = track.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onLikeClick()
                    }
                ) {
                    Icon(
                        imageVector =
                        if (isLiked) Icons.Rounded.Favorite
                        else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
                IconButton(
                    onClick = {
                        onCommentClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ModeComment,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
                IconButton(
                    onClick = { onAddToPlaylist() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddChart,
                        contentDescription = "Add to playlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            style = Typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row {
                            TextButton(
                                onClick = { onAuthorClick() },
                                content = {
                                    Image(
                                        painter = rememberImagePainter(data = author.avatar),
                                        contentDescription = "Author Image",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = author.username ?: "",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            )
                            FilledTonalButton(
                                onClick = {
                                    onFollowClick()
                                }
                            ) {
                                Text(
                                    text = if (isFollowed) stringResource(R.string.followed) else stringResource(R.string.follow)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.1f))
                    IconButton(onClick = {
                        onPlayPauseClick()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewTrackCard() {
    TrackCard(
        track = Track(
            trackId = "1",
            title = "Разработка",
            image = "https://w7.pngwing.com/pngs/416/531/png-transparent-wrench-bahco-tools.png",
            file = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artistIds = listOf("1"),
        ),
        isLiked = false,
        isPlaying = false,
        isFollowed = false,
        onPlayPauseClick = {},
        onLikeClick = {},
        onCommentClick = {},
        onAuthorClick = {},
        onFollowClick = {},
        onAddToPlaylist = {},
        author = User(
            userId = "1",
            username = "Username",
            avatar = "https://picsum.photos/200/300"
        )
    )
}