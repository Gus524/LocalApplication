package com.goodgus.localapplication.pedidos.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.pedidos.usecase.CrearPedidoParams
import com.goodgus.localapplication.pedidos.usecase.CrearPedidoUseCase
import com.goodgus.localapplication.pedidos.usecase.EditarPedidoParams
import com.goodgus.localapplication.pedidos.usecase.EditarPedidoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class EditPedidoUiState(
    val id: Int? = null,
    val descripcion: String = "",
    val detalles: String = "",
    val fechaEntrega: String = "",
    val esEdicion: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface EditPedidoAction {
    data class OnDescripcionChange(val valor: String) : EditPedidoAction
    data class OnDetallesChange(val valor: String) : EditPedidoAction
    data class OnFechaEntregaChange(val valor: String) : EditPedidoAction
    data class OnCargarPedido(val id: Int) : EditPedidoAction
    data object OnGuardar : EditPedidoAction
    data object OnDismissAlerta : EditPedidoAction
}

sealed interface EditPedidoEffect {
    data object NavegarAtras : EditPedidoEffect
}

@HiltViewModel
class EditPedidoViewModel @Inject constructor(
    private val crearPedidoUseCase: CrearPedidoUseCase,
    private val editarPedidoUseCase: EditarPedidoUseCase,
    private val pedidoRepository: IPedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPedidoUiState())
    val uiState: StateFlow<EditPedidoUiState> = _uiState.asStateFlow()

    private val _effect = Channel<EditPedidoEffect>(Channel.BUFFERED)
    val effect: Flow<EditPedidoEffect> = _effect.receiveAsFlow()

    fun onAction(action: EditPedidoAction) {
        when (action) {
            is EditPedidoAction.OnDescripcionChange -> _uiState.update { it.copy(descripcion = action.valor) }
            is EditPedidoAction.OnDetallesChange -> _uiState.update { it.copy(detalles = action.valor) }
            is EditPedidoAction.OnFechaEntregaChange -> _uiState.update { it.copy(fechaEntrega = action.valor) }
            is EditPedidoAction.OnCargarPedido -> cargarPedido(action.id)
            is EditPedidoAction.OnGuardar -> guardar()
            is EditPedidoAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
        }
    }

    private fun cargarPedido(id: Int) {
        viewModelScope.launch {
            val pedido = pedidoRepository.obtenerPorId(PedidoId(id))
            if (pedido != null) {
                _uiState.update {
                    it.copy(
                        id = pedido.id.valor,
                        descripcion = pedido.informacion.descripcion,
                        detalles = pedido.informacion.detalles ?: "",
                        fechaEntrega = pedido.plazo.fechaEntrega ?: "",
                        esEdicion = true
                    )
                }
            } else {
                _uiState.update { it.copy(mensajeAlerta = "Pedido no encontrado") }
            }
        }
    }

    private fun guardar() {
        val estado = _uiState.value
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now().format(formato)

        viewModelScope.launch {
            val resultado = if (estado.esEdicion && estado.id != null) {
                editarPedidoUseCase(
                    EditarPedidoParams(
                        pedidoId = estado.id,
                        nuevaDescripcion = estado.descripcion,
                        nuevosDetalles = estado.detalles.ifBlank { null },
                        nuevaFechaEntrega = estado.fechaEntrega.ifBlank { null }
                    )
                )
            } else {
                crearPedidoUseCase(
                    CrearPedidoParams(
                        descripcion = estado.descripcion,
                        detalles = estado.detalles.ifBlank { null },
                        fechaPedido = today,
                        fechaEntrega = estado.fechaEntrega.ifBlank { null }
                    )
                )
            }

            resultado.fold(
                onSuccess = {
                    _effect.trySend(EditPedidoEffect.NavegarAtras)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(mensajeAlerta = "Error: ${error.message ?: "No se pudo guardar el pedido"}")
                    }
                }
            )
        }
    }
}