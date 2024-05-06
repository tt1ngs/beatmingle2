package com.ttings.beatwave.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.BeatwaveTheme
import com.ttings.beatwave.ui.theme.Typography
import timber.log.Timber

@Composable
fun TrackDataField(
    name: String,
    textValue: String, // Используйте передаваемое значение
    onTextChange: (String) -> Unit,
    onClick: () -> Unit = {},
    exception: String,
    enabled: Boolean = true
) {
    val maxCharCount = 100

    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(300.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                style = Typography.bodySmall,
            )
            Text(
                text = if (textValue.isEmpty()) exception else "${maxCharCount - textValue.length}",
                color = if (textValue.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                style = Typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BasicTextField(
            enabled = enabled,
            value = textValue,
            onValueChange = {
                if (it.length <= maxCharCount) onTextChange(it)
            },
            singleLine = true,
            textStyle = Typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { /* Handle done action */ }),
            decorationBox = { innerTextField ->
                Column {
                    if (textValue.isEmpty()) {
                        Text(stringResource(R.string.required), style = Typography.bodyMedium)
                    } else {
                        innerTextField() // This places the actual BasicTextField
                    }
                }
            }
        )
    }
}
