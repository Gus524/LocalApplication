package com.goodgus.localapplication.pedidos.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.pedidos.usecase.CancelarPedidoParams
import com.goodgus.localapplication.pedidos.usecase.CancelarPedidoUseCase
import com.goodgus.localapplication.pedidos.usecase.EntregarPedidoParams
import com.goodgus.localapplication.pedidos.usecase.EntregarPedidoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PedidosUiState(
    val pedidos: List<Pedido> = emptyList(),
    val filtroEstado: EstadoPedido? = null,
    val idPedidoCancelar: Int? = null,
    val mostrarDialogoCancelar: Boolean = false,
    val mensajeAlerta: String? = null
) {
    val pedidosFiltrados: List<Pedido>
        get() = if (filtroEstado == null) pedidos else pedidos.filter { it.estado == filtroEstado }

    fun contarPorEstado(estado: EstadoPedido?): Int =
        if (estado == null) pedidos.size else pedidos.count { it.estado == estado }
}

sealed interface PedidosAction {
    data object OnCargarPedidos : PedidosAction
    data class OnFiltrarEstado(val estado: EstadoPedido?) : PedidosAction
    data class OnEntregarPedido(val idPedido: Int) : PedidosAction
    data class OnSolicitarCancelarPedido(val idPedido: Int) : PedidosAction
    data object OnConfirmarCancelarPedido : PedidosAction
    data object OnDescartarCancelarPedido : PedidosAction
    data object OnDismissAlerta : PedidosAction
    data object OnNuevoPedido : PedidosAction
    data class OnEditarPedido(val idPedido: Int) : PedidosAction
}

sealed interface PedidosEffect {
    data object NavegarANuevoPedido : PedidosEffect
    data class NavegarAEditarPedido(val idPedido: Int) : PedidosEffect
}

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val pedidoRepository: IPedidoRepository,
    private val entregarPedidoUseCase: EntregarPedidoUseCase,
    private val cancelarPedidoUseCase: CancelarPedidoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PedidosEffect>(Channel.BUFFERED)
    val effect: Flow<PedidosEffect> = _effect.receiveAsFlow()

    private var pedidosJob: Job? = null

    init {
        observarPedidos()
    }

    fun onAction(action: PedidosAction) {
        when (action) {
            is PedidosAction.OnCargarPedidos -> observarPedidos()
            is PedidosAction.OnFiltrarEstado -> _uiState.update { it.copy(filtroEstado = action.estado) }
            is PedidosAction.OnEntregarPedido -> entregarPedido(action.idPedido)
            is PedidosAction.OnSolicitarCancelarPedido -> _uiState.update {
                it.copy(mostrarDialogoCancelar = true, idPedidoCancelar = action.idPedido)
            }
            is PedidosAction.OnConfirmarCancelarPedido -> confirmarCancelarPedido()
            is PedidosAction.OnDescartarCancelarPedido -> _uiState.update {
                it.copy(mostrarDialogoCancelar = false, idPedidoCancelar = null)
            }
            is PedidosAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
            is PedidosAction.OnNuevoPedido -> _effect.trySend(PedidosEffect.NavegarANuevoPedido)
            is PedidosAction.OnEditarPedido -> _effect.trySend(PedidosEffect.NavegarAEditarPedido(action.idPedido))
        }
    }

    private fun observarPedidos() {
        pedidosJob?.cancel()
        pedidosJob = viewModelScope.launch {
            pedidoRepository.observarTodos().collect { lista ->
                _uiState.update { it.copy(pedidos = lista) }
            }
        }
    }

    private fun entregarPedido(id: Int) {
        viewModelScope.launch {
            val resultado = entregarPedidoUseCase(EntregarPedidoParams(pedidoId = id))
            resultado.fold(
                onSuccess = {
                    com.goodgus.localapplication.core.ui.components.feedback.SnackbarManager.mostrarMensaje("Pedido marcado como entregado")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensajeAlerta = "Error: ${error.message ?: "No se pudo entregar el pedido"}") }
                }
            )
        }
    }

    private fun confirmarCancelarPedido() {
        val id = _uiState.value.idPedidoCancelar ?: return
        viewModelScope.launch {
            val resultado = cancelarPedidoUseCase(CancelarPedidoParams(pedidoId = id))
            resultado.fold(
                onSuccess = {
                    com.goodgus.localapplication.core.ui.components.feedback.SnackbarManager.mostrarMensaje("Pedido cancelado correctamente")
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCancelar = false,
                            idPedidoCancelar = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCancelar = false,
                            idPedidoCancelar = null,
                            mensajeAlerta = "Error: ${error.message ?: "No se pudo cancelar el pedido"}"
                        )
                    }
                }
            )
        }
    }
}