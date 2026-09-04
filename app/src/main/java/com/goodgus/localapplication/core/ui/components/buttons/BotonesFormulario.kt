package com.goodgus.localapplication.core.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fila de botones estándar para formularios con acciones Cancelar y Guardar.
 */
@Composable
fun BotonesFormulario(
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    textoGuardar: String = "Guardar",
    textoCancelar: String = "Cancelar",
    habilitado: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Text(textoCancelar)
        }
        Button(
            onClick = onGuardar,
            enabled = habilitado,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        ) {
            Text(textoGuardar)
        }
    }
}
