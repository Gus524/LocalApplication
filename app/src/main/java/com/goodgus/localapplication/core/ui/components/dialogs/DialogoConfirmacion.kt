package com.goodgus.localapplication.core.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Diálogo estándar de confirmación para acciones del usuario (guardar, descartar, eliminar).
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onDescartar: () -> Unit,
    modifier: Modifier = Modifier,
    textoConfirmar: String = "Aceptar",
    textoCancelar: String = "Cancelar",
    esDestructivo: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text(text = titulo, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = mensaje, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = if (esDestructivo) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDescartar) {
                Text(textoCancelar)
            }
        },
        modifier = modifier
    )
}
