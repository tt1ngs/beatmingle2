package com.ttings.beatwave.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun CreatePlaylist(
    infoText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(332.dp)
            .height(75.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(vertical = 5.dp)
            .clickable { onClick() }

    ) {
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.CenterVertically)
                .background(MaterialTheme.colorScheme.onSecondary)
        ) {
            Text(
                modifier = Modifier.fillMaxSize().height(64.dp),
                text = "+",
                style = Typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Column(
            modifier = Modifier
                .width(240.dp)
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)
        ) {
            Text(
                text = infoText,
                style = Typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

        }
    }
}