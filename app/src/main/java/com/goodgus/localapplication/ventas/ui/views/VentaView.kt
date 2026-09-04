package com.goodgus.localapplication.ventas.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonesFormulario
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.inputs.CampoCantidad
import com.goodgus.localapplication.core.ui.components.inputs.CampoLectura
import com.goodgus.localapplication.core.ui.components.inputs.CampoMoneda
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.ui.viewModels.VentaFormAction
import com.goodgus.localapplication.ventas.ui.viewModels.VentaFormEffect
import com.goodgus.localapplication.ventas.ui.viewModels.VentaFormUiState
import com.goodgus.localapplication.ventas.ui.viewModels.VentaViewModel

@Composable
fun VentaScreen(
    idVenta: String? = null,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VentaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idVenta) {
        idVenta?.toIntOrNull()?.let { id ->
            viewModel.onAction(VentaFormAction.OnCargarVenta(id))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VentaFormEffect.NavegarAtras -> navigateBack()
            }
        }
    }

    VentaFormContent(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = navigateBack,
        modifier = modifier
    )
}

@Composable
fun VentaFormContent(
    state: VentaFormUiState,
    onAction: (VentaFormAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (state.esEdicion) "Detalle de Venta" else "Nueva Venta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!state.esEdicion) {
            OutlinedTextField(
                value = state.busqueda,
                onValueChange = { onAction(VentaFormAction.OnBuscarProducto(it)) },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )

            if (state.mostrarResultados && state.resultadosBusqueda.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp)) {
                    items(state.resultadosBusqueda, key = { it.id.valor }) { prod ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAction(VentaFormAction.OnSeleccionarProducto(prod)) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = prod.nombre, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Precio: $${prod.precioVenta.monto} | Disp: ${prod.inventario.disponibles}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        state.productoSeleccionado?.let { prod ->
            Spacer(Modifier.height(8.dp))

            CampoLectura(
                valor = prod.nombre,
                label = "Producto Seleccionado"
            )

            Spacer(Modifier.height(8.dp))

            CampoMoneda(
                precio = prod.precioVenta.monto,
                onPrecioChange = {},
                readOnly = true,
                label = "Precio Unitario"
            )

            Spacer(Modifier.height(8.dp))

            CampoCantidad(
                cantidad = state.cantidad,
                onCantidadChange = { onAction(VentaFormAction.OnCantidadChange(it)) },
                label = "Cantidad a Vender",
                readOnly = state.esEdicion
            )

            Spacer(Modifier.height(8.dp))

            CampoLectura(
                valor = "$${state.subtotal}",
                label = "Subtotal de la Venta"
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!state.esEdicion) {
                BotonesFormulario(
                    onGuardar = { onAction(VentaFormAction.OnGuardarVenta) },
                    onCancelar = onNavigateBack,
                    textoGuardar = "Registrar Venta"
                )
            }
        }
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(VentaFormAction.OnDismissAlerta) }
        )
    }
}

@PreviewLightDark
@Composable
private fun VentaFormContentPreview() {
    LocalApplicationTheme {
        VentaFormContent(
            state = VentaFormUiState(
                productoSeleccionado = Producto(
                    id = ProductoId(1),
                    informacion = InformacionProducto(
                        nombre = "Jugo Naranja 500ml",
                        marca = "Jumex",
                        tipo = "Bebidas"
                    ),
                    precioVenta = Dinero(15.0),
                    inventario = Inventario(disponibles = 8)
                ),
                cantidad = 2,
                subtotal = 30.0,
                esEdicion = false
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
