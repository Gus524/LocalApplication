package com.goodgus.localapplication.inventario.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.buttons.BotonesFormulario
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.inputs.CampoCantidad
import com.goodgus.localapplication.core.ui.components.inputs.CampoMoneda
import com.goodgus.localapplication.core.ui.components.inputs.CampoTexto
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoFormAction
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoFormEffect
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoFormUiState
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoViewModel

@Composable
fun ProductoScreen(
    idProducto: String? = null,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idProducto) {
        val id = idProducto?.toIntOrNull()
        if (id != null) {
            viewModel.onAction(ProductoFormAction.OnCargarProducto(id))
        } else {
            viewModel.onAction(ProductoFormAction.OnIniciarNuevo)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProductoFormEffect.NavegarAtras -> navigateBack()
            }
        }
    }

    ProductoFormContent(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = navigateBack,
        modifier = modifier
    )
}

@Composable
fun ProductoFormContent(
    state: ProductoFormUiState,
    onAction: (ProductoFormAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (state.esEdicion) "Actualizar Producto" else "Nuevo Producto",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CampoTexto(
            valor = state.nombre,
            onValorChange = { onAction(ProductoFormAction.OnNombreChange(it)) },
            label = "Nombre",
            error = if (state.nombre.isBlank()) "El nombre es obligatorio" else null
        )

        Spacer(Modifier.height(8.dp))

        CampoTexto(
            valor = state.marca,
            onValorChange = { onAction(ProductoFormAction.OnMarcaChange(it)) },
            label = "Marca"
        )

        Spacer(Modifier.height(8.dp))

        CampoMoneda(
            precio = state.precioVenta,
            onPrecioChange = { onAction(ProductoFormAction.OnPrecioChange(it)) },
            label = "Precio de venta",
            error = if (state.precioVenta <= 0.0) "El precio debe ser mayor a 0" else null
        )

        Spacer(Modifier.height(8.dp))

        CampoCantidad(
            cantidad = state.stock,
            onCantidadChange = { onAction(ProductoFormAction.OnStockChange(it)) },
            label = "Stock disponible",
            readOnly = state.esEdicion
        )

        Spacer(Modifier.height(8.dp))

        CampoTexto(
            valor = state.tipo,
            onValorChange = { onAction(ProductoFormAction.OnTipoChange(it)) },
            label = "Tipo"
        )

        Spacer(Modifier.height(32.dp))

        BotonesFormulario(
            onGuardar = { onAction(ProductoFormAction.OnGuardar) },
            onCancelar = onNavigateBack,
            textoGuardar = if (state.esEdicion) "Actualizar" else "Guardar"
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(ProductoFormAction.OnDismissAlerta) }
        )
    }
}

@PreviewLightDark
@Composable
private fun ProductoFormContentPreview() {
    LocalApplicationTheme {
        ProductoFormContent(
            state = ProductoFormUiState(
                nombre = "Agua 1L",
                marca = "Epura",
                tipo = "Bebida",
                precioVenta = 12.50,
                stock = 10,
                esEdicion = false
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}