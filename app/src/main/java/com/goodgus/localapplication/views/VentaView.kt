package com.goodgus.localapplication.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.goodgus.localapplication.components.CampoTitle
import com.goodgus.localapplication.components.ProductButton
import com.goodgus.localapplication.components.SearchProduct
import com.goodgus.localapplication.viewModels.VentaViewModel

@Composable
fun VentaScreen(
    idVenta: String? = null,
    navigateBack: () -> Unit,
    viewModel: VentaViewModel
){
    val uiState by viewModel.uiState.collectAsState()

    // Cargar la ventana si estamos actualizando
    LaunchedEffect(idVenta) {
        viewModel.cargarVenta(idVenta)
    }
    UpdateForm(
        viewModel,
        uiState,
        navigateBack
    )

}

@Composable
fun UpdateForm(
    viewModel: VentaViewModel,
    uiState: VentaViewModel.VentaUIState,
    navigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (uiState.isUpdate) "Actualizar venta" else "Nueva venta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        SearchProduct(
            search = uiState.search,
            onTextChange = {
                viewModel.updateSearch(it)
                viewModel.searchProduct()
            },
            resultados = uiState.resultado,
            isLoading = uiState.isBusy,
            onProductSelect = { viewModel.selectProduct(it) },
            isLazyVisible = uiState.lazyVisible
        )
        if (uiState.productoSeleccionado != null) {
            Spacer(Modifier.height(8.dp))

            CampoTitle(nombre = uiState.productoSeleccionado.nombre)

            Spacer(modifier = Modifier.height(8.dp))

            CampoPrecio(
                precio = uiState.productoSeleccionado.precioVenta,
                onPrecioChange = {},
                readOnly = true,
                label = "Precio de venta"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoCantidad(
                cantidad = uiState.cantidad,
                onCantidadChange = { viewModel.updateCantidad(it) }
            )



            Spacer(Modifier.height(8.dp))

            CampoPrecio(
                precio = String.format("%.2f", uiState.parcialVenta).toDouble(),
                onPrecioChange = {},
                readOnly = true,
                label = "Total de venta"
            )

            Spacer(modifier = Modifier.height(32.dp))

            BotonesAcciones(
                onGuardar = {
                    viewModel.guardarVenta()
                    navigateBack()
                },
                onCancelar = {
                    navigateBack()
                }
            )
        }
    }
}
