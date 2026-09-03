package com.goodgus.localapplication.pedidos.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.models.data.Pedidos
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val pedidoRepository: IPedidoRepository,
) : ViewModel() {
    data class PedidoUIState(
        val pedidoSeleccionado: Pedidos? = null,
        val pedidos: List<Pedidos> = emptyList(),
        val isBusy: Boolean = false
    )

    private val _uiState = MutableStateFlow(PedidoUIState())
    val uiState: StateFlow<PedidoUIState> = _uiState.asStateFlow()

    private val _pedidos = MutableStateFlow<List<Pedidos>>(emptyList())
    val pedidos: StateFlow<List<Pedidos>> = _pedidos.asStateFlow()

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val lista = pedidoRepository.obtenerTodos()
            val entities = lista.map { toUiEntity(it) }
            _pedidos.value = entities
            _uiState.update { it.copy(pedidos = entities, isBusy = false) }
        }
    }

    private fun toUiEntity(domain: Pedido): Pedidos {
        return Pedidos(
            idPedido = domain.id.valor,
            fechaPedido = domain.plazo.fechaPedido,
            fechaEntrega = domain.plazo.fechaEntrega,
            descripcion = domain.informacion.descripcion,
            detalles = domain.informacion.detalles
        )
    }
}