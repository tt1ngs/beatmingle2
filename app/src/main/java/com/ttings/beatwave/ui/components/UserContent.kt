package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.PlayerViewModel
import com.ttings.beatwave.viewmodels.ProfileViewModel
import timber.log.Timber

@Composable
fun UserContent(
    user: User,
    userUploadsList: List<Track>,
    userPlaylistsList: List<Playlist>,
    userAlbumsList: List<Playlist>,
    userLikesList: List<Track>,
    onMoreTracksClick: () -> Unit = {}, // TODO
    onMoreAlbumsClick: () -> Unit = {}, // TODO
    onMorePlaylistsClick: () -> Unit = {}, // TODO
    onMoreLikesClick: () -> Unit = {}, // TODO
    profileViewModel: ProfileViewModel = hiltViewModel(),
    viewModel: PlayerViewModel,
    navController: NavController
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
                    val author by produceState<User?>(initialValue = null) {
                        try {
                            value = viewModel.getAuthorById(track.artistIds.first())
                        } catch (e: Exception) {
                            Timber.tag("TrackCard").e(e, "Error getting author")
                            value = null
                        }
                    }
                    TrackBar(
                        authorName = author?.username ?: "",
                        duration = secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = { viewModel.playTrack(track, userUploadsList) },
                        onMoreClick = { onMoreTracksClick() }
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
                    val author by produceState<User?>(initialValue = null) {
                        try {
                            value = viewModel.getAuthorById(playlist.userId)
                        } catch (e: Exception) {
                            Timber.tag("TrackCard").e(e, "Error getting author")
                            value = null
                        }
                    }
                    PlaylistBar(
                        playlistName = playlist.title,
                        authorName = author?.username ?: "",
                        playlistImage = playlist.image,
                        onPlaylistClick = { navController.navigate("SelectedAlbum/${playlist.playlistId}") }
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
                    val author by produceState<User?>(initialValue = null) {
                        try {
                            value = viewModel.getAuthorById(playlist.userId)
                        } catch (e: Exception) {
                            Timber.tag("TrackCard").e(e, "Error getting author")
                            value = null
                        }
                    }
                    PlaylistBar(
                        playlistName = playlist.title,
                        authorName = author?.username ?: "",
                        playlistImage = playlist.image,
                        onPlaylistClick = { navController.navigate("SelectedPlaylist/${playlist.playlistId}") }
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
                    val author by produceState<User?>(initialValue = null) {
                        try {
                            value = viewModel.getAuthorById(track.artistIds.first())
                        } catch (e: Exception) {
                            Timber.tag("TrackCard").e(e, "Error getting author")
                            value = null
                        }
                    }
                    TrackBar(
                        authorName = author?.username ?: "",
                        duration = secondsToMinutesSeconds(track.duration),
                        track = track,
                        onTrackClick = { viewModel.playTrack(track, userLikesList) },
                        onMoreClick = { onMoreTracksClick() }
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