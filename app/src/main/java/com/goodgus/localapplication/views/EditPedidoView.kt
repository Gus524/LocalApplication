package com.goodgus.localapplication.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.BotonesAcciones
import com.goodgus.localapplication.components.CampoText
import com.goodgus.localapplication.viewModels.EditPedidoViewModel

@Composable
fun EditPedidoScreen(
    idPedido: String? = null,
    navigateBack: () -> Unit,
    viewModel: EditPedidoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PedidoForm(
        viewModel = viewModel,
        uiState = uiState,
        navigateBack = navigateBack
    )
}

@Composable
fun PedidoForm(
    viewModel: EditPedidoViewModel,
    uiState: EditPedidoViewModel.PedidoUIState,
    navigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Agregar nuevo pedido",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        CampoText(
            text = uiState.descripcion,
            onTextChange = { viewModel.updateDescripcion(it) },
            label = "Descripcion"
        )

        Spacer(Modifier.height(8.dp))

        CampoText(
            text = uiState.detalles,
            onTextChange = { viewModel.updateDetalles(it) },
            label = "Detalles"
        )

        Spacer(modifier = Modifier.height(32.dp))

        BotonesAcciones(
            onGuardar = {
                viewModel.guardarPedido()
                navigateBack()
            },
            onCancelar = {
                navigateBack()
            }
        )
    }
}