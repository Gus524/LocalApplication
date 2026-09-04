package com.goodgus.localapplication.inventario.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonEditar
import com.goodgus.localapplication.core.ui.components.buttons.BotonEliminar
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoConfirmacion
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioAction
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioEffect
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioUiState
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioViewModel

@Composable
fun InventarioScreen(
    navigateToProducto: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InventarioEffect.NavegarAEditar -> navigateToProducto(effect.idProducto.toString())
                is InventarioEffect.NavegarACrear -> navigateToProducto("")
            }
        }
    }

    InventarioContent(
        state = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun InventarioContent(
    state: InventarioUiState,
    onAction: (InventarioAction) -> Unit,
    modifier: Modifier = Modifier
) {
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
                Surface(tonalElevation = 2.dp) {
                    OutlinedTextField(
                        value = state.busqueda,
                        onValueChange = { onAction(InventarioAction.OnBuscar(it)) },
                        label = { Text("Buscar producto") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        singleLine = true
                    )
                }
            }
            items(
                items = state.productos,
                key = { it.id.valor }
            ) { producto ->
                ProductoCard(
                    producto = producto,
                    onEdit = { onAction(InventarioAction.OnEditarProducto(producto.id.valor)) },
                    onDelete = { onAction(InventarioAction.OnSolicitarEliminar(producto.id.valor)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isFabVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            FloatingActionButton(
                onClick = { onAction(InventarioAction.OnCrearProducto) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar producto")
            }
        }
    }

    if (state.mostrarDialogoEliminar) {
        DialogoConfirmacion(
            titulo = "Dar de baja producto",
            mensaje = "¿Seguro que deseas desactivar este producto?",
            onConfirmar = { onAction(InventarioAction.OnConfirmarEliminar) },
            onDescartar = { onAction(InventarioAction.OnCancelarEliminar) },
            textoConfirmar = "Dar de baja",
            esDestructivo = true
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(InventarioAction.OnDismissAlerta) }
        )
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
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
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Precio: $${producto.precioVenta.monto} | Stock: ${producto.inventario.disponibles}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Marca: ${producto.marca.ifBlank { "N/A" }} | Tipo: ${producto.tipo.ifBlank { "General" }}",
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
private fun InventarioContentPreview() {
    LocalApplicationTheme {
        InventarioContent(
            state = InventarioUiState(
                productos = listOf(
                    Producto(
                        id = ProductoId(1),
                        informacion = InformacionProducto(
                            nombre = "Refresco Cola 600ml",
                            marca = "Marca X",
                            tipo = "Bebidas"
                        ),
                        precioVenta = Dinero(18.0),
                        inventario = Inventario(disponibles = 24)
                    )
                )
            ),
            onAction = {}
        )
    }
}
