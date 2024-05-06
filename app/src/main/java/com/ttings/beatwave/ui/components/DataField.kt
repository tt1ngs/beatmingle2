package com.ttings.beatwave.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun DataField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    topPadding: Int,
    imeAction: ImeAction = ImeAction.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onImeAction: () -> Unit = {},
    bottomPadding: Int = 0
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        textStyle = Typography.bodyMedium,
        modifier = Modifier
            .width(300.dp)
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(10.dp)),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }),
        maxLines = 1
    )
}

@Preview
@Composable
fun DataFieldPreview() {
    DataField(
        value = "Text",
        onValueChange = {},
        label = "Label",
        topPadding = 0
    )
}