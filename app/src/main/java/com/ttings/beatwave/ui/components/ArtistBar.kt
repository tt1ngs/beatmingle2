package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun ArtistBar(
    user: User,
    onAuthorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(100.dp, 130.dp)
            .clip(shape = RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable {
                onAuthorClick()
            }
    ) {
        Image(
            painter = rememberImagePainter(
                data = user.avatar,
                builder = {
                    crossfade(true)
                    fallback(R.drawable.logo)
                }
            ),
            contentDescription = "Profile Image",
            modifier = Modifier
                .padding(top = 8.dp)
                .size(82.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(CenterHorizontally),
            contentScale = ContentScale.Crop)
        Text(
            text = user.username ?: "",
            style = Typography.bodyMedium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(CenterHorizontally)
        )
    }
}