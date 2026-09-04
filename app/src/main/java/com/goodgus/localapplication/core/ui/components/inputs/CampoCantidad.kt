package com.goodgus.localapplication.core.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Campo numérico para cantidades enteras.
 */
@Composable
fun CampoCantidad(
    cantidad: Int,
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Cantidad",
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = if (cantidad == 0) "" else cantidad.toString(),
        onValueChange = { input ->
            val soloDigitos = input.filter { it.isDigit() }
            onCantidadChange(soloDigitos.toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
    )
}
