package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.BeatwaveTheme

@Composable
fun CommentBar(
    user: User,
    comment: String,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .width(332.dp)
            .height(intrinsicSize = IntrinsicSize.Min)
            .clip(shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = { /*TODO*/ })
    ) {
        Image(
            painter = rememberImagePainter(
                data = user.avatar,
                builder = {
                    crossfade(true)
                    fallback(R.drawable.logo)
                }
            ),
            contentDescription = "Track Image",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(50))
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .width(240.dp)
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = user.username ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = comment,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun CommentBarPreview() {
    BeatwaveTheme {
        CommentBar(
            user = User("username"),
            comment = "CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentComment"
        )

    }
}