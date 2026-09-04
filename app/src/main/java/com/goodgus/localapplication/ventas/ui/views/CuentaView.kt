package com.goodgus.localapplication.ventas.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonEditar
import com.goodgus.localapplication.core.ui.components.buttons.BotonEliminar
import com.goodgus.localapplication.core.ui.components.chips.BadgeEstado
import com.goodgus.localapplication.core.ui.components.chips.TipoEstadoSemantico
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoConfirmacion
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
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaAction
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaEffect
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaUiState
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    navigateToVenta: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CuentaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CuentaEffect.NavegarANuevaVenta -> navigateToVenta("")
                is CuentaEffect.NavegarAEditarVenta -> navigateToVenta(effect.idVenta.toString())
            }
        }
    }

    CuentaContent(
        state = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun CuentaContent(
    state: CuentaUiState,
    onAction: (CuentaAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val cuenta = state.cuentaActiva

    if (cuenta == null) {
        EstadoVacio(
            icono = Icons.Default.Storefront,
            titulo = "No hay cuenta abierta",
            mensaje = "Abre una nueva cuenta de caja para comenzar a registrar las ventas del día.",
            textoBoton = "Abrir Nueva Cuenta",
            onBotonClick = { onAction(CuentaAction.OnAbrirCuenta) },
            modifier = modifier.fillMaxSize()
        )
    } else {
        val listState = rememberLazyListState()
        val isFabVisible by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 || !listState.isScrollInProgress
            }
        }
        val ventasActivas = remember(cuenta.ventas) {
            cuenta.ventas.filter { it.estado == EstadoVenta.ACTIVA }
        }

        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ResumenCuentaHeroCard(
                        cuenta = cuenta,
                        totalVentasActivas = ventasActivas.size,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (ventasActivas.isEmpty()) {
                    item {
                        EstadoVacio(
                            icono = Icons.AutoMirrored.Filled.ReceiptLong,
                            titulo = "Sin ventas registradas",
                            mensaje = "Esta cuenta está lista. Presiona el botón '+' para agregar el primer artículo vendido.",
                            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                } else {
                    items(
                        items = ventasActivas,
                        key = { it.id.valor }
                    ) { venta ->
                        VentaItemCard(
                            venta = venta,
                            onEdit = { onAction(CuentaAction.OnEditarVenta(venta.id.valor)) },
                            onDelete = { onAction(CuentaAction.OnSolicitarEliminarVenta(venta.id.valor)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = isFabVisible,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                FloatingActionButton(
                    onClick = { onAction(CuentaAction.OnSolicitarCerrarCuenta) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Cerrar cuenta")
                }
            }

            AnimatedVisibility(
                visible = isFabVisible,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                FloatingActionButton(
                    onClick = { onAction(CuentaAction.OnNuevaVenta) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva venta")
                }
            }
        }
    }

    if (state.mostrarDialogoCerrarCuenta) {
        DialogoConfirmacion(
            titulo = "Cerrar Cuenta",
            mensaje = "¿Deseas realizar el corte y cerrar la cuenta actual? No se podrán registrar más ventas.",
            onConfirmar = { onAction(CuentaAction.OnConfirmarCerrarCuenta) },
            onDescartar = { onAction(CuentaAction.OnCancelarCerrarCuenta) },
            textoConfirmar = "Cerrar Cuenta",
            esDestructivo = true
        )
    }

    if (state.mostrarDialogoEliminarVenta) {
        DialogoConfirmacion(
            titulo = "Anular Venta",
            mensaje = "¿Seguro que deseas anular esta venta de la cuenta activa?",
            onConfirmar = { onAction(CuentaAction.OnConfirmarEliminarVenta) },
            onDescartar = { onAction(CuentaAction.OnCancelarEliminarVenta) },
            textoConfirmar = "Anular",
            esDestructivo = true
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(CuentaAction.OnDismissAlerta) }
        )
    }
}

@Composable
fun ResumenCuentaHeroCard(
    cuenta: Cuenta,
    totalVentasActivas: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Acumulado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%.2f", cuenta.informacion.total.monto)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FechaUtils.formatearAUi(cuenta.informacion.fecha),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (totalVentasActivas == 1) "1 artículo" else "$totalVentasActivas artículos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun VentaItemCard(
    venta: Venta,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = venta.nombreProducto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%.2f", venta.subtotal.monto)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${venta.detalle.cantidad.valor} pzas  ×  $${String.format(Locale.US, "%.2f", venta.detalle.precioUnitario.monto)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.height(14.dp).width(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = venta.detalle.hora,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonEditar(onClick = onEdit)
                    BotonEliminar(onClick = onDelete)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CuentaContentPreview() {
    LocalApplicationTheme {
        CuentaContent(
            state = CuentaUiState(
                cuentaActiva = Cuenta(
                    id = CuentaId(1),
                    informacion = InformacionCuenta(
                        fecha = "2026-09-03",
                        estado = EstadoCuenta.ABIERTA,
                        total = Dinero(75.0)
                    ),
                    ventas = listOf(
                        Venta(
                            id = VentaId(1),
                            productoId = ProductoId(1),
                            nombreProducto = "Sabritas Original 45g",
                            detalle = DetalleVenta(
                                cantidad = Cantidad(3),
                                hora = "14:30",
                                precioUnitario = Dinero(25.0)
                            ),
                            estado = EstadoVenta.ACTIVA
                        )
                    )
                )
            ),
            onAction = {}
        )
    }
}
