package com.goodgus.localapplication.core.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class TipoEstadoSemantico {
    EXITO,
    ADVERTENCIA,
    ERROR,
    NEUTRO
}

/**
 * Chip indicador de estado con colores e iconos semánticos.
 */
@Composable
fun BadgeEstado(
    texto: String,
    tipo: TipoEstadoSemantico,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val (fondo, contenido, iconoPorDefecto) = when (tipo) {
        TipoEstadoSemantico.EXITO -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        TipoEstadoSemantico.ADVERTENCIA -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.HourglassTop
        )
        TipoEstadoSemantico.ERROR -> Triple(
            Color(0xFFFDE8E8),
            Color(0xFF991B1B),
            Icons.Default.Cancel
        )
        TipoEstadoSemantico.NEUTRO -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }

    val iconoFinal = icono ?: iconoPorDefecto

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconoFinal,
            contentDescription = null,
            tint = contenido,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            color = contenido,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
