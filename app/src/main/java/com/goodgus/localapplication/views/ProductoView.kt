package com.goodgus.localapplication.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.BotonesAcciones
import com.goodgus.localapplication.components.CampoCantidad
import com.goodgus.localapplication.components.CampoPrecio
import com.goodgus.localapplication.components.CampoText
import com.goodgus.localapplication.viewModels.ProductoViewModel

@Composable
fun ProductoScreen(
    idProducto: String? = null,
    navigateBack: () -> Unit,
    viewModel: ProductoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idProducto) {
        viewModel.cargarProducto(idProducto)
    }

    ProductoForm(
        viewModel = viewModel,
        uiState = uiState,
        navigateBack = navigateBack
    )

}

@Composable
fun ProductoForm(
    viewModel: ProductoViewModel,
    uiState: ProductoViewModel.ProductoUIState,
    navigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (uiState.isUpdate) "Actualizar producto" else "Nuevo producto",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        CampoText(
            text = uiState.name,
            onTextChange = { viewModel.updateName(it) },
            label = "Nombre"
        )

        Spacer(Modifier.height(8.dp))

        CampoText(
            text = uiState.marca,
            onTextChange = { viewModel.updateMarca(it) },
            label = "Marca"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CampoPrecio(
            precio = uiState.precio,
            onPrecioChange = { viewModel.updatePrecio(it) },
            label = "Precio de venta"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CampoCantidad(
            cantidad = uiState.cantidad,
            onCantidadChange = { viewModel.updateCantidad(it) }
        )

        Spacer(Modifier.height(8.dp))

        CampoText(
            text = uiState.tipo,
            onTextChange = { viewModel.updateTipo(it) },
            label = "Tipo"
        )

        Spacer(modifier = Modifier.height(32.dp))

        BotonesAcciones(
            onGuardar = {
                viewModel.guardarProducto()
                navigateBack()
            },
            onCancelar = {
                navigateBack()
            }
        )
    }
}