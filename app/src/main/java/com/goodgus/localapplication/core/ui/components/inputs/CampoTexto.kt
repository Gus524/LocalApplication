package com.goodgus.localapplication.core.ui.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Campo de texto estándar para entradas generales (nombres, descripciones, clientes).
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            label = { Text(label) },
            isError = error != null,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
