package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun AuthButton(
    onClick: () -> Unit,
    text: String,
    topPadding: Int = 0,
    bottomPadding: Int = 0
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(223.dp)
            .padding(top = topPadding.dp, bottom = bottomPadding.dp),
        shape = MaterialTheme.shapes.large,
        content = {
            Text(
                text = text,
                style = Typography.bodyMedium
            )
        }
    )
}

@Preview
@Composable
fun AuthButtonPreview() {
    AuthButton(
        onClick = {},
        text = "Button"
    )
}