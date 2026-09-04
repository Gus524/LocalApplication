package com.goodgus.localapplication.core.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.goodgus.localapplication.shared.utilidades.formatPrecio
import com.goodgus.localapplication.shared.utilidades.parsePrecio

/**
 * Campo especializado para valores monetarios con validación de hasta 2 decimales.
 */
@Composable
fun CampoMoneda(
    precio: Double,
    onPrecioChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Precio",
    readOnly: Boolean = false
) {
    var textValue by remember { mutableStateOf(formatPrecio(precio)) }

    LaunchedEffect(precio) {
        val currentParsed = parsePrecio(textValue)
        if (currentParsed != precio) {
            textValue = formatPrecio(precio)
        }
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            if (newValue.matches(Regex("^\\d*([.,]\\d{0,2})?$"))) {
                textValue = newValue
                onPrecioChange(parsePrecio(newValue))
            }
        },
        label = { Text(label) },
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}
