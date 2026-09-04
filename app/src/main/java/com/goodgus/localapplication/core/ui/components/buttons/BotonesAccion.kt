package com.goodgus.localapplication.core.ui.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Botón de acción con icono para edición de elementos.
 */
@Composable
fun BotonEditar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Editar",
    tint: Color = MaterialTheme.colorScheme.primary
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint)
    ) {
        Icon(Icons.Filled.Edit, contentDescription = contentDescription)
    }
}

/**
 * Botón de acción con icono para eliminación o cancelación de elementos.
 */
@Composable
fun BotonEliminar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Eliminar",
    tint: Color = MaterialTheme.colorScheme.error
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint)
    ) {
        Icon(Icons.Filled.Delete, contentDescription = contentDescription)
    }
}
