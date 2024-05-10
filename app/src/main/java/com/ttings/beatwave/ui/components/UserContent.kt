package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun UserContent(
    user: User,
    userUploadsList: List<Track>,
    userPlaylistsList: List<Playlist>,
    userAlbumsList: List<Playlist>,
    userLikesList: List<Track>,
    onMoreTracksClick: () -> Unit = {},
    onMoreAlbumsClick: () -> Unit = {},
    onMorePlaylistsClick: () -> Unit = {},
    onMoreLikesClick: () -> Unit = {}
) {
    Column {

        if (userUploadsList.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(36.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.tracks),
                    style = Typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp)
                )
                TextButton(
                    onClick = { onMoreTracksClick() }
                ) {
                    Text(
                        text = stringResource(id = R.string.see_all),
                        style = Typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 16.dp)

                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Три последних загруженных трека
                userUploadsList.takeLast(3).forEach { track ->
                    TrackBar(
                        authorName = user.username!!,
                        duration = secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = { /* TODO */ },
                        onMoreClick = { /* TODO */ }
                    )
                }
            }
        }
        if (userAlbumsList.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(36.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.album),
                    style = Typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp)
                )
                TextButton(
                    onClick = { onMoreAlbumsClick() }
                ) {
                    Text(
                        text = stringResource(id = R.string.see_all),
                        style = Typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 16.dp)

                    )
                }
            }
            // Три последних созданных альбома
            LazyRow {
                // Три последних загруженных трека
                items(userAlbumsList.takeLast(3).size) { index ->
                    val playlist = userAlbumsList.takeLast(3)[index]
                    PlaylistBar(
                        playlistName = playlist.title,
                        authorName = user.username!!,
                        playlistImage = playlist.image
                    )
                }
            }
        }
        if (userPlaylistsList.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(36.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.playlist),
                    style = Typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp)
                )
                TextButton(
                    onClick = { onMorePlaylistsClick() }
                ) {
                    Text(
                        text = stringResource(id = R.string.see_all),
                        style = Typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 16.dp)

                    )
                }
            }
            // Три последних созданных плейлиста
            LazyRow() {
                items(userPlaylistsList.takeLast(3).size) { index ->
                    val playlist = userPlaylistsList.takeLast(3)[index]
                    PlaylistBar(
                        playlistName = playlist.title,
                        authorName = user.username!!,
                        playlistImage = playlist.image
                    )
                }
            }
        }
        if (userLikesList.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(36.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.liked_tracks),
                    style = Typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp)
                )
                TextButton(
                    onClick = { onMoreLikesClick() }
                ) {
                    Text(
                        text = stringResource(id = R.string.see_all),
                        style = Typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 16.dp)

                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Три последних лайкнутых трека
                userLikesList.takeLast(3).forEach { track ->
                    TrackBar(
                        authorName = user.username!!,
                        duration = secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = { /* TODO */ },
                        onMoreClick = { /* TODO */ }
                    )
                }
            }
        }
    }

}

fun secondsToMinutesSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Preview
@Composable
fun UserContentPreview() {
    UserContent(
        user = User(
            userId = "1",
            username = "User",
            avatar = "https://www.example.com/image.jpg"
        ),
        userUploadsList = listOf(
            Track(
                trackId = "1",
                title = "Track",
                artistIds = listOf("1"),
                duration = 180,
                image = "https://www.example.com/image.jpg"
            ),
            Track(
                trackId = "1",
                title = "Track",
                artistIds = listOf("1"),
                duration = 180,
                image = "https://www.example.com/image.jpg"
            ),
            Track(
                trackId = "1",
                title = "Track",
                artistIds = listOf("1"),
                duration = 180,
                image = "https://www.example.com/image.jpg"
            ),
            Track(
                trackId = "1",
                title = "Track",
                artistIds = listOf("1"),
                duration = 180,
                image = "https://www.example.com/image.jpg"
            ),
        ),
        userPlaylistsList = listOf(
            Playlist(
                playlistId = "1",
                title = "Playlist",
                userId = "1",
                image = "https://www.example.com/image.jpg"
            ),
            Playlist(
                playlistId = "1",
                title = "Playlist",
                userId = "1",
                image = "https://www.example.com/image.jpg"
            ),
            Playlist(
                playlistId = "1",
                title = "Playlist",
                userId = "1",
                image = "https://www.example.com/image.jpg"
            ),
            Playlist(
                playlistId = "1",
                title = "Playlist",
                userId = "1",
                image = "https://www.example.com/image.jpg"
            ),
        ),
        userAlbumsList = listOf(
            Playlist(
                playlistId = "1",
                title = "Album",
                userId = "1",
                image = "https://www.example.com/image.jpg"
            )
        ),
        userLikesList = listOf(
            Track(
                trackId = "1",
                title = "Track",
                artistIds = listOf("1"),
                duration = 180,
                image = "https://www.example.com/image.jpg"
            )
        )
    )
}