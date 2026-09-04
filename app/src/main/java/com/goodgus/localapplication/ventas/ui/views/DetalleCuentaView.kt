package com.goodgus.localapplication.ventas.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.chips.BadgeEstado
import com.goodgus.localapplication.core.ui.components.chips.TipoEstadoSemantico
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.feedback.EstadoVacio
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.shared.utilidades.FechaUtils
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.DetalleVenta
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.model.Venta
import com.goodgus.localapplication.ventas.domain.model.VentaId
import com.goodgus.localapplication.ventas.ui.viewModels.DetalleCuentaAction
import com.goodgus.localapplication.ventas.ui.viewModels.DetalleCuentaUiState
import com.goodgus.localapplication.ventas.ui.viewModels.DetalleCuentaViewModel
import java.util.Locale

@Composable
fun DetalleCuentaScreen(
    idCuenta: Int,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleCuentaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idCuenta) {
        viewModel.onAction(DetalleCuentaAction.OnCargarDetalle(idCuenta))
    }

    DetalleCuentaContent(
        state = uiState,
        onDismissAlerta = { viewModel.onAction(DetalleCuentaAction.OnDismissAlerta) },
        modifier = modifier
    )
}

@Composable
fun DetalleCuentaContent(
    state: DetalleCuentaUiState,
    onDismissAlerta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cuenta = state.cuenta

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cuenta != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumen de Caja #${cuenta.id.valor}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        BadgeEstado(
                            texto = "Cerrada",
                            tipo = TipoEstadoSemantico.NEUTRO
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fecha: ${FechaUtils.formatearAUi(cuenta.informacion.fecha)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total de Ventas:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$ ${String.format(Locale.US, "%.2f", cuenta.totalCalculado.monto)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Líneas de Venta (${cuenta.ventas.count { it.estaActiva }} activas)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (cuenta.ventas.isEmpty()) {
                EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.ReceiptLong,
                    titulo = "Sin ventas",
                    mensaje = "No se registraron ventas en esta cuenta."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cuenta.ventas, key = { it.id.valor }) { venta ->
                        VentaReadonlyItemCard(venta = venta)
                    }
                }
            }
        } else if (!state.cargando) {
            EstadoVacio(
                icono = Icons.AutoMirrored.Filled.ReceiptLong,
                titulo = "Cuenta no encontrada",
                mensaje = "No se pudo cargar la información de la cuenta seleccionada."
            )
        }
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = onDismissAlerta
        )
    }
}

@Composable
fun VentaReadonlyItemCard(
    venta: Venta,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venta.nombreProducto,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hora: ${venta.detalle.hora}  |  Cant: ${venta.detalle.cantidad.valor} x $${String.format(Locale.US, "%.2f", venta.detalle.precioUnitario.monto)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$ ${String.format(Locale.US, "%.2f", venta.subtotal.monto)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (venta.estaActiva) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                if (!venta.estaActiva) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BadgeEstado(texto = "Cancelada", tipo = TipoEstadoSemantico.ERROR)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DetalleCuentaContentPreview() {
    LocalApplicationTheme {
        DetalleCuentaContent(
            state = DetalleCuentaUiState(
                cuenta = Cuenta(
                    id = CuentaId(1),
                    informacion = InformacionCuenta("2026-09-02", EstadoCuenta.CERRADA),
                    ventas = listOf(
                        Venta(
                            id = VentaId(1),
                            productoId = ProductoId(10),
                            nombreProducto = "Coca Cola 600ml",
                            detalle = DetalleVenta(
                                cantidad = Cantidad(2),
                                hora = "14:30",
                                precioUnitario = Dinero(18.5)
                            ),
                            estado = EstadoVenta.ACTIVA
                        )
                    )
                )
            ),
            onDismissAlerta = {}
        )
    }
}
