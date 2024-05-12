package com.ttings.beatwave.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.Typography

@Composable
fun ErrorBox(
    errorMessage: String,
    bottomPadding: Int = 0,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit
) {

    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.TwoTone.Error,
                contentDescription = "Error icon",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.error),
                style = Typography.bodyMedium
            )
        },
        text = {
            Text(
                text = errorMessage,
                style = Typography.bodyMedium
            )
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(
                    text = stringResource(id = R.string.confirm),
                    style = Typography.bodyMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = stringResource(id = R.string.dismiss),
                    style = Typography.bodyMedium
                )
            }
        },

    )
}