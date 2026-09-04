package com.goodgus.localapplication.core.ui.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Campo de texto estándar para entradas generales con validación reactiva por inactividad (1.2s) o pérdida de foco.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next
) {
    var haSidoModificado by remember { mutableStateOf(false) }
    var haPerdidoFoco by remember { mutableStateOf(false) }
    var inactividadCumplida by remember { mutableStateOf(false) }

    LaunchedEffect(valor) {
        if (haSidoModificado) {
            inactividadCumplida = false
            delay(1200L)
            inactividadCumplida = true
        }
    }

    val mostrarError = !error.isNullOrBlank() && haSidoModificado && (haPerdidoFoco || inactividadCumplida)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = {
                haSidoModificado = true
                onValorChange(it)
            },
            label = { Text(label) },
            isError = mostrarError,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && haSidoModificado) {
                        haPerdidoFoco = true
                    }
                }
        )
        if (mostrarError) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
