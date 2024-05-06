package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorBox(
    errorMessage: String,
    bottomPadding: Int = 0,
    onDismiss: () -> Unit
) {
    Column{
        Snackbar(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = bottomPadding.dp),
            action = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
}