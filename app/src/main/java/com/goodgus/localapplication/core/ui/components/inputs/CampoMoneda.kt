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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Campo especializado para valores monetarios con entrada estilo Nu (desplazamiento automático de decimales).
 * Siempre muestra 2 decimales y gestiona la entrada de dígitos de derecha a izquierda.
 */
@Composable
fun CampoMoneda(
    precio: Double,
    onPrecioChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Precio",
    readOnly: Boolean = false,
    error: String? = null
) {
    var rawDigits by remember {
        val initialCents = (precio * 100).roundToLong()
        mutableStateOf(if (initialCents > 0) initialCents.toString() else "")
    }

    var haSidoModificado by remember { mutableStateOf(false) }
    var haPerdidoFoco by remember { mutableStateOf(false) }
    var inactividadCumplida by remember { mutableStateOf(false) }

    LaunchedEffect(precio) {
        val currentCents = rawDigits.toLongOrNull() ?: 0L
        val expectedCents = (precio * 100).roundToLong()
        if (currentCents != expectedCents) {
            rawDigits = if (expectedCents > 0) expectedCents.toString() else ""
        }
    }

    LaunchedEffect(rawDigits) {
        if (haSidoModificado) {
            inactividadCumplida = false
            delay(1200L)
            inactividadCumplida = true
        }
    }

    val cents = rawDigits.toLongOrNull() ?: 0L
    val displayText = String.format(Locale.US, "%.2f", cents / 100.0)
    val textFieldValue = TextFieldValue(
        text = displayText,
        selection = TextRange(displayText.length)
    )

    val mostrarError = !error.isNullOrBlank() && haSidoModificado && (haPerdidoFoco || inactividadCumplida)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (readOnly) return@OutlinedTextField
                haSidoModificado = true

                if (newValue.text.length < displayText.length) {
                    // Backspace presionado: elimina el último dígito
                    rawDigits = if (rawDigits.isNotEmpty()) rawDigits.dropLast(1) else ""
                } else {
                    // Extrae el carácter ingresado (dígito)
                    val newChars = newValue.text.filter { it.isDigit() }
                    val currentDigitsInDisplay = displayText.filter { it.isDigit() }
                    if (newChars.length > currentDigitsInDisplay.length && rawDigits.length < 9) {
                        val addedDigit = newChars.last()
                        rawDigits = (if (rawDigits == "0") "" else rawDigits) + addedDigit
                    }
                }

                val newCents = rawDigits.toLongOrNull() ?: 0L
                onPrecioChange(newCents / 100.0)
            },
            label = { Text(label) },
            prefix = { Text("$ ") },
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
