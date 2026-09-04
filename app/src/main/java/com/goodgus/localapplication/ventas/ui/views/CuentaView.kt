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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonEditar
import com.goodgus.localapplication.core.ui.components.buttons.BotonEliminar
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoConfirmacion
import com.goodgus.localapplication.inventario.domain.model.ProductoId
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
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay cuenta abierta",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { onAction(CuentaAction.OnAbrirCuenta) }) {
                    Text("Abrir Cuenta")
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        val isFabVisible by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 || !listState.isScrollInProgress
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Surface(tonalElevation = 4.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total de la cuenta:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$${cuenta.informacion.total.monto}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                items(
                    items = cuenta.ventas.filter { it.estado == EstadoVenta.ACTIVA },
                    key = { it.id.valor }
                ) { venta ->
                    VentaItemCard(
                        venta = venta,
                        onEdit = { onAction(CuentaAction.OnEditarVenta(venta.id.valor)) },
                        onDelete = { onAction(CuentaAction.OnSolicitarEliminarVenta(venta.id.valor)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
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
                    containerColor = MaterialTheme.colorScheme.errorContainer
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
            mensaje = "¿Deseas cerrar la cuenta actual? No se podrán registrar más ventas.",
            onConfirmar = { onAction(CuentaAction.OnConfirmarCerrarCuenta) },
            onDescartar = { onAction(CuentaAction.OnCancelarCerrarCuenta) },
            textoConfirmar = "Cerrar Cuenta",
            esDestructivo = true
        )
    }

    if (state.mostrarDialogoEliminarVenta) {
        DialogoConfirmacion(
            titulo = "Anular Venta",
            mensaje = "¿Seguro que deseas anular esta venta?",
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
fun VentaItemCard(
    venta: Venta,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venta.nombreProducto,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cant: ${venta.detalle.cantidad.valor} x $${venta.detalle.precioUnitario.monto} = $${venta.subtotal.monto}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Hora: ${venta.detalle.hora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                BotonEditar(onClick = onEdit)
                BotonEliminar(onClick = onDelete)
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
