package com.goodgus.localapplication.core.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Diálogo informativo o de aviso que presenta un mensaje con botón único de aceptación.
 */
@Composable
fun DialogoAlerta(
    mensaje: String,
    onAceptar: () -> Unit,
    modifier: Modifier = Modifier,
    titulo: String = "Aviso",
    textoBoton: String = "Aceptar"
) {
    AlertDialog(
        onDismissRequest = onAceptar,
        title = { Text(text = titulo, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = mensaje, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(onClick = onAceptar) {
                Text(textoBoton)
            }
        },
        modifier = modifier
    )
}
