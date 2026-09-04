package com.goodgus.localapplication.pedidos.ui.views

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonEditar
import com.goodgus.localapplication.core.ui.components.buttons.BotonEliminar
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoConfirmacion
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import com.goodgus.localapplication.pedidos.ui.viewModels.PedidosAction
import com.goodgus.localapplication.pedidos.ui.viewModels.PedidosEffect
import com.goodgus.localapplication.pedidos.ui.viewModels.PedidosUiState
import com.goodgus.localapplication.pedidos.ui.viewModels.PedidosViewModel

@Composable
fun PedidoScreen(
    navigateToPedido: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PedidosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PedidosEffect.NavegarANuevoPedido -> navigateToPedido("")
                is PedidosEffect.NavegarAEditarPedido -> navigateToPedido(effect.idPedido.toString())
            }
        }
    }

    PedidosContent(
        state = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun PedidosContent(
    state: PedidosUiState,
    onAction: (PedidosAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.pedidos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay pedidos pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { onAction(PedidosAction.OnNuevoPedido) }) {
                    Text("Agregar Pedido")
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
                items(
                    items = state.pedidos,
                    key = { it.id.valor }
                ) { pedido ->
                    PedidoItemCard(
                        pedido = pedido,
                        onEntregar = { onAction(PedidosAction.OnEntregarPedido(pedido.id.valor)) },
                        onEditar = { onAction(PedidosAction.OnEditarPedido(pedido.id.valor)) },
                        onCancelar = { onAction(PedidosAction.OnSolicitarCancelarPedido(pedido.id.valor)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
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
                    onClick = { onAction(PedidosAction.OnNuevoPedido) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo pedido")
                }
            }
        }
    }

    if (state.mostrarDialogoCancelar) {
        DialogoConfirmacion(
            titulo = "Cancelar Pedido",
            mensaje = "¿Seguro que deseas cancelar este pedido?",
            onConfirmar = { onAction(PedidosAction.OnConfirmarCancelarPedido) },
            onDescartar = { onAction(PedidosAction.OnDescartarCancelarPedido) },
            textoConfirmar = "Cancelar Pedido",
            esDestructivo = true
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(PedidosAction.OnDismissAlerta) }
        )
    }
}

@Composable
fun PedidoItemCard(
    pedido: Pedido,
    onEntregar: () -> Unit,
    onEditar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pedido.informacion.descripcion,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                pedido.informacion.detalles?.let { det ->
                    if (det.isNotBlank()) {
                        Text(
                            text = det,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Text(
                    text = "Pedido: ${pedido.plazo.fechaPedido}" + (pedido.plazo.fechaEntrega?.let { " | Entrega: $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Estado: ${pedido.estado.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (pedido.estado) {
                        EstadoPedido.PENDIENTE -> MaterialTheme.colorScheme.primary
                        EstadoPedido.ENTREGADO -> MaterialTheme.colorScheme.tertiary
                        EstadoPedido.CANCELADO -> MaterialTheme.colorScheme.error
                    }
                )
            }
            if (pedido.estaPendiente) {
                Row {
                    IconButton(
                        onClick = onEntregar,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Entregar pedido")
                    }
                    BotonEditar(onClick = onEditar)
                    BotonEliminar(onClick = onCancelar, contentDescription = "Cancelar pedido")
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PedidosContentPreview() {
    LocalApplicationTheme {
        PedidosContent(
            state = PedidosUiState(
                pedidos = listOf(
                    Pedido(
                        id = PedidoId(1),
                        informacion = InformacionPedido(
                            descripcion = "Pastel 3 Leches",
                            detalles = "Para el sábado con mensaje 'Felicidades Juan'"
                        ),
                        plazo = PlazoEntrega(
                            fechaPedido = "2026-09-03",
                            fechaEntrega = "2026-09-06"
                        ),
                        estado = EstadoPedido.PENDIENTE
                    )
                )
            ),
            onAction = {}
        )
    }
}