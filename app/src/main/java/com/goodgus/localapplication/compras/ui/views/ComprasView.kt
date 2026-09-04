package com.goodgus.localapplication.compras.ui.views

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.ui.viewModels.CompraAction
import com.goodgus.localapplication.compras.ui.viewModels.CompraEffect
import com.goodgus.localapplication.compras.ui.viewModels.CompraUiState
import com.goodgus.localapplication.compras.ui.viewModels.CompraViewModel
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonEliminar
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoConfirmacion

@Composable
fun CompraScreen(
    navigateToDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CompraEffect.NavegarANuevaCompra -> navigateToDetalle("")
            }
        }
    }

    CompraContent(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateToDetalle = navigateToDetalle,
        modifier = modifier
    )
}

@Composable
fun CompraContent(
    state: CompraUiState,
    onAction: (CompraAction) -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.compras.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay compras registradas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { onAction(CompraAction.OnNuevaCompra) }) {
                    Text("Registrar Compra")
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
                    items = state.compras,
                    key = { it.id.valor }
                ) { compra ->
                    CompraItemCard(
                        compra = compra,
                        onCancelar = { onAction(CompraAction.OnSolicitarCancelarCompra(compra.id.valor)) },
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
                    onClick = { onAction(CompraAction.OnNuevaCompra) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva compra")
                }
            }
        }
    }

    if (state.mostrarDialogoCancelar) {
        DialogoConfirmacion(
            titulo = "Cancelar Compra",
            mensaje = "¿Seguro que deseas cancelar esta compra? Se revertirá el stock de los productos.",
            onConfirmar = { onAction(CompraAction.OnConfirmarCancelarCompra) },
            onDescartar = { onAction(CompraAction.OnDescartarCancelarCompra) },
            textoConfirmar = "Cancelar Compra",
            esDestructivo = true
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(CompraAction.OnDismissAlerta) }
        )
    }
}

@Composable
fun CompraItemCard(
    compra: Compra,
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
                    text = "Compra #${compra.id.valor}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fecha: ${compra.informacion.fechaCompra}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: $${compra.informacion.total.monto}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Estado: ${if (compra.estaRegistrada) "Completada" else "Cancelada"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (compra.estaRegistrada) {
                BotonEliminar(
                    onClick = onCancelar,
                    contentDescription = "Cancelar compra"
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CompraContentPreview() {
    LocalApplicationTheme {
        CompraContent(
            state = CompraUiState(
                compras = listOf(
                    Compra(
                        id = CompraId(1),
                        informacion = InformacionCompra(
                            fechaCompra = "2026-09-03",
                            total = Dinero(350.0),
                            estado = EstadoCompra.REGISTRADA
                        )
                    )
                )
            ),
            onAction = {},
            onNavigateToDetalle = {}
        )
    }
}