package com.goodgus.localapplication.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.InfoCard
import com.goodgus.localapplication.models.data.Pedidos
import com.goodgus.localapplication.viewModels.PedidosViewModel

@Composable
fun PedidoScreen(
    navigateToPedido: (String) -> Unit,
    viewModel: PedidosViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedidosState = viewModel.pedidos.collectAsState()
    val pedidos = pedidosState.value

    if(pedidos.isNotEmpty()) {
        ShowPedidos(
            pedidos = pedidos,
            navigateToPedido = navigateToPedido,
            uiState = uiState,
            viewModel = viewModel
        )
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No hay pedidos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
            )
            Button(onClick = { navigateToPedido("") }) {
                Text("Agregar pedido")
            }
        }
    }
}

@Composable
fun ShowPedidos(
    pedidos: List<Pedidos>,
    navigateToPedido: (String) -> Unit,
    uiState: PedidosViewModel.PedidoUIState,
    viewModel: PedidosViewModel
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(pedidos) { pedido ->
                PedidoCard(
                    pedido = pedido,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }
        FloatingActionButton(
            onClick = { navigateToPedido("") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)

        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Agregar Pedido"
            )
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedidos,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val pedidoData = listOf(
        "Fecha" to pedido.fechaPedido,
        "Detalles" to pedido.detalles.toString()
    )

    InfoCard(
        title = pedido.descripcion,
        dataList = pedidoData,
        onEditClick = onEdit,
        onDeleteClick = onDelete,
        modifier = Modifier.padding(4.dp)
    )
}