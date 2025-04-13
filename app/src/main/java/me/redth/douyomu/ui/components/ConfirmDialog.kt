package me.redth.douyomu.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import me.redth.douyomu.R


@Composable
fun ConfirmDialog(
    title: String,
    description: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
fun confirmDialogOpener(text: String, description: String, action: () -> Unit): () -> Unit {
    var openDialog by remember { mutableStateOf(false) }
    if (openDialog) {
        ConfirmDialog(
            title = text,
            description = description,
            onConfirm = {
                action()
                openDialog = false
            },
            onDismiss = { openDialog = false },
        )
    }
    return { openDialog = true }
}