package com.goodgus.localapplication.pedidos.ui.views

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
import com.goodgus.localapplication.core.ui.components.inputs.CampoFecha
import com.goodgus.localapplication.core.ui.components.inputs.CampoTexto
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoAction
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoEffect
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoUiState
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoViewModel

@Composable
fun EditPedidoScreen(
    idPedido: String? = null,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPedidoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idPedido) {
        val id = idPedido?.toIntOrNull()
        if (id != null) {
            viewModel.onAction(EditPedidoAction.OnCargarPedido(id))
        } else {
            viewModel.onAction(EditPedidoAction.OnIniciarNuevo)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditPedidoEffect.NavegarAtras -> navigateBack()
            }
        }
    }

    EditPedidoContent(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = navigateBack,
        modifier = modifier
    )
}

@Composable
fun EditPedidoContent(
    state: EditPedidoUiState,
    onAction: (EditPedidoAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (state.esEdicion) "Editar Pedido" else "Nuevo Pedido",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CampoTexto(
            valor = state.descripcion,
            onValorChange = { onAction(EditPedidoAction.OnDescripcionChange(it)) },
            label = "Descripción del Pedido",
            error = if (state.descripcion.isBlank()) "La descripción es obligatoria" else null
        )

        Spacer(Modifier.height(8.dp))

        CampoTexto(
            valor = state.detalles,
            onValorChange = { onAction(EditPedidoAction.OnDetallesChange(it)) },
            label = "Detalles adicionales (opcional)",
            singleLine = false
        )

        Spacer(Modifier.height(8.dp))

        CampoFecha(
            fecha = state.fechaEntrega,
            onFechaChange = { onAction(EditPedidoAction.OnFechaEntregaChange(it)) },
            label = "Fecha de entrega"
        )

        Spacer(modifier = Modifier.height(32.dp))

        BotonesFormulario(
            onGuardar = { onAction(EditPedidoAction.OnGuardar) },
            onCancelar = onNavigateBack,
            textoGuardar = if (state.esEdicion) "Actualizar" else "Registrar Pedido"
        )
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(EditPedidoAction.OnDismissAlerta) }
        )
    }
}

@PreviewLightDark
@Composable
private fun EditPedidoContentPreview() {
    LocalApplicationTheme {
        EditPedidoContent(
            state = EditPedidoUiState(
                descripcion = "Pastel de Chocolate",
                detalles = "Relleno de fresa",
                fechaEntrega = "10-09-2026",
                esEdicion = false
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}