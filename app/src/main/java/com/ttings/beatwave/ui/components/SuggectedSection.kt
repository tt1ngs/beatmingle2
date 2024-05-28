package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.SuggestionViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedSection(
    playlistId: String,
    onlyUserTracks: Boolean = false,
    viewModel: SuggestionViewModel = hiltViewModel()
) {
    val tracks by if (onlyUserTracks) {
        viewModel.suggestedTracks.observeAsState(initial = emptyList())
    } else {
        viewModel.suggestedTracks.observeAsState(initial = emptyList())
    }

    if (tracks.isEmpty()) {
        Text("Loading...")
    } else {
        Text(
            text = stringResource(R.string.suggested_tracks),
            style = Typography.bodyMedium,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Timber.tag("SuggestedSection").d("Before logging tracks")
            Timber.tag("SuggestedSection").d("Tracks: $tracks")
            Timber.tag("SuggestedSection").d("After logging tracks | Size: ${tracks.size}")

            tracks.forEach { track ->
                Timber.tag("SuggestedSection").d("Track: $track")
                val dismissState = rememberDismissState()

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.StartToEnd),
                    background = {},
                    dismissContent = {
                        TrackBar(
                            authorName = "",
                            duration = viewModel.secondsToMinutesSeconds(track.duration),
                            track = track,
                            onTrackClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.addToPlaylist(track.trackId, playlistId)
                                    viewModel.replaceTrack(track.trackId)
                                }
                            },
                            onMoreClick = { }
                        )
                    }
                )

                if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
                    viewModel.addToPlaylist(track.trackId, playlistId, onlyUserTracks)
                    viewModel.viewModelScope.launch {
                        viewModel.replaceTrack(track.trackId) // Replace the added track
                    }
                }
            }
        }
    }
}