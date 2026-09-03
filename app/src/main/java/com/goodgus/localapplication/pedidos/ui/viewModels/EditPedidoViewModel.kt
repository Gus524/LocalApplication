package com.goodgus.localapplication.pedidos.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.pedidos.usecase.CrearPedidoParams
import com.goodgus.localapplication.pedidos.usecase.CrearPedidoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditPedidoViewModel @Inject constructor(
    private val crearPedidoUseCase: CrearPedidoUseCase,
    private val pedidoRepository: IPedidoRepository
) : ViewModel() {
    data class PedidoUIState(
        val cantidad: Int = 0,
        val descripcion: String = "",
        val detalles: String = "",
        val mensaje: String = "",
        val isBusy: Boolean = false
    )

    private val _uiState = MutableStateFlow(PedidoUIState())
    val uiState: StateFlow<PedidoUIState> = _uiState.asStateFlow()

    fun updateDescripcion(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    fun updateDetalles(detalles: String) {
        _uiState.update { it.copy(detalles = detalles) }
    }

    fun guardarPedido() {
        val estado = _uiState.value
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val resultado = crearPedidoUseCase(
                CrearPedidoParams(
                    descripcion = estado.descripcion,
                    detalles = estado.detalles.ifBlank { null },
                    fechaPedido = today.format(formato)
                )
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = "Pedido registrado correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = "Error: ${error.message ?: "No se pudo registrar el pedido"}"
                        )
                    }
                }
            )
        }
    }

    fun cargarPedido(idPedido: String?) {
        if (idPedido.isNullOrBlank()) return

        val idInt = idPedido.toIntOrNull() ?: return
        _uiState.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            val pedido = pedidoRepository.obtenerPorId(PedidoId(idInt))
            if (pedido != null) {
                _uiState.update {
                    it.copy(
                        descripcion = pedido.informacion.descripcion,
                        detalles = pedido.informacion.detalles ?: "",
                        isBusy = false
                    )
                }
            } else {
                _uiState.update { it.copy(isBusy = false, mensaje = "Pedido no encontrado") }
            }
        }
    }
}