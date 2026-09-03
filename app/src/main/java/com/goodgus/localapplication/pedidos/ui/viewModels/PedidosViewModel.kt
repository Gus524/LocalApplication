package com.goodgus.localapplication.pedidos.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.models.data.Pedidos
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PedidosViewModel(
    app: Application,
    private val pedidoRepository: IPedidoRepository,
) : AndroidViewModel(app) {
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

    companion object {
        fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val app = application as com.goodgus.localapplication.application.LocalApplication
                    val pedidosDAO = app.database.pedidosDAO()
                    val pedidoRepository = com.goodgus.localapplication.pedidos.data.repository.PedidoRepository(pedidosDAO)

                    return PedidosViewModel(app, pedidoRepository) as T
                }
            }
        }
    }
}