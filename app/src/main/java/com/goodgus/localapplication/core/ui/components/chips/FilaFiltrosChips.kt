package com.goodgus.localapplication.core.ui.components.chips

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fila deslizable reutilizable de FilterChips para cualquier tipo genérico de opción o estado.
 *
 * @param T Tipo de dato de las opciones
 * @param opciones Lista de opciones disponibles (soporta null como opción comodín 'Todos')
 * @param seleccionado Opción actualmente seleccionada
 * @param onSeleccionar Callback al seleccionar una opción
 * @param etiqueta Función para obtener el texto de cada opción
 * @param contador Función opcional para mostrar un badge numérico por opción
 */
@Composable
fun <T> FilaFiltrosChips(
    opciones: List<T?>,
    seleccionado: T?,
    onSeleccionar: (T?) -> Unit,
    etiqueta: (T?) -> String,
    modifier: Modifier = Modifier,
    contador: ((T?) -> Int)? = null
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        opciones.forEach { opcion ->
            val estaSeleccionado = opcion == seleccionado
            val texto = etiqueta(opcion)
            val conteo = contador?.invoke(opcion)
            val textoCompleto = if (conteo != null) "$texto ($conteo)" else texto

            FilterChip(
                selected = estaSeleccionado,
                onClick = { onSeleccionar(opcion) },
                label = { Text(textoCompleto) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
