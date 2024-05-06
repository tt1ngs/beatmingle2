package com.ttings.beatwave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.ui.theme.Typography
import timber.log.Timber
import kotlin.math.abs

@Composable
fun MiniPlayer(
    title: String,
    author: String,
    isPlaying: Boolean,
    isLiked: Boolean,
    currentProgress: Float,
    onPlayPauseClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit
) {

    val swipeThreshold = 350f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                var totalDragAmount = 0f
                detectHorizontalDragGestures { change, dragAmount ->
                    totalDragAmount += dragAmount
                    Timber.d("Drag amount: $dragAmount, Total drag: $totalDragAmount")
                    if (abs(totalDragAmount) > swipeThreshold) {
                        if (totalDragAmount > 0) {
                            Timber.d("Triggering previous track.")
                            onPreviousTrack()
                        } else {
                            Timber.d("Triggering next track.")
                            onNextTrack()
                        }
                        totalDragAmount = 0f // Сброс после обработки свайпа
                    }
                    change.consumeAllChanges() // Указываем, что изменение "потреблено" и не должно обрабатываться дальше
                }
        }
    ) {
        Row {
            Column {
                IconButton(
                    onClick = {
                        onPlayPauseClicked()
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                    )
                }
            }
            Column(
                modifier = Modifier
                    .height(48.dp)
                    .width(IntrinsicSize.Max)
                    .padding(start = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    Text(
                        text = title,
                        style = Typography.bodyMedium,
                        maxLines = 1
                    )
                }
                Row {
                    Text(
                        text = author,
                        style = Typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            Column(
                modifier = Modifier.width(IntrinsicSize.Min).weight(0.18f),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = {
                        onFavoriteClicked()
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Play/Pause",
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = currentProgress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}