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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Campo numérico para cantidades enteras con validación reactiva por inactividad o pérdida de foco.
 */
@Composable
fun CampoCantidad(
    cantidad: Int,
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Cantidad",
    readOnly: Boolean = false,
    error: String? = null
) {
    var haSidoModificado by remember { mutableStateOf(false) }
    var haPerdidoFoco by remember { mutableStateOf(false) }
    var inactividadCumplida by remember { mutableStateOf(false) }

    val rawValue = if (cantidad == 0 && !haSidoModificado) "" else if (cantidad == 0) "" else cantidad.toString()

    LaunchedEffect(cantidad) {
        if (haSidoModificado) {
            inactividadCumplida = false
            delay(1200L)
            inactividadCumplida = true
        }
    }

    val mostrarError = !error.isNullOrBlank() && haSidoModificado && (haPerdidoFoco || inactividadCumplida)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = rawValue,
            onValueChange = { input ->
                if (readOnly) return@OutlinedTextField
                haSidoModificado = true
                val soloDigitos = input.filter { it.isDigit() }
                onCantidadChange(soloDigitos.toIntOrNull() ?: 0)
            },
            label = { Text(label) },
            readOnly = readOnly,
            singleLine = true,
            isError = mostrarError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
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
