package com.goodgus.localapplication.inventario.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoParams
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioUiState(
    val productos: List<Producto> = emptyList(),
    val busqueda: String = "",
    val idProductoEliminar: Int? = null,
    val mostrarDialogoEliminar: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface InventarioAction {
    data class OnBuscar(val query: String) : InventarioAction
    data class OnSolicitarEliminar(val idProducto: Int) : InventarioAction
    data object OnConfirmarEliminar : InventarioAction
    data object OnCancelarEliminar : InventarioAction
    data object OnDismissAlerta : InventarioAction
    data object OnCargarProductos : InventarioAction
    data class OnEditarProducto(val idProducto: Int) : InventarioAction
    data object OnCrearProducto : InventarioAction
}

sealed interface InventarioEffect {
    data class NavegarAEditar(val idProducto: Int) : InventarioEffect
    data object NavegarACrear : InventarioEffect
}

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val cambiarEstadoProductoUseCase: CambiarEstadoProductoUseCase,
    private val productoRepository: IProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private val _effect = Channel<InventarioEffect>(Channel.BUFFERED)
    val effect: Flow<InventarioEffect> = _effect.receiveAsFlow()

    init {
        cargarProductos()
    }

    fun onAction(action: InventarioAction) {
        when (action) {
            is InventarioAction.OnBuscar -> buscar(action.query)
            is InventarioAction.OnSolicitarEliminar -> _uiState.update {
                it.copy(mostrarDialogoEliminar = true, idProductoEliminar = action.idProducto)
            }
            is InventarioAction.OnConfirmarEliminar -> confirmarEliminar()
            is InventarioAction.OnCancelarEliminar -> _uiState.update {
                it.copy(mostrarDialogoEliminar = false, idProductoEliminar = null)
            }
            is InventarioAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
            is InventarioAction.OnCargarProductos -> cargarProductos()
            is InventarioAction.OnEditarProducto -> _effect.trySend(InventarioEffect.NavegarAEditar(action.idProducto))
            is InventarioAction.OnCrearProducto -> _effect.trySend(InventarioEffect.NavegarACrear)
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            val lista = productoRepository.obtenerTodos()
            _uiState.update { it.copy(productos = lista) }
        }
    }

    private fun buscar(query: String) {
        _uiState.update { it.copy(busqueda = query) }
        viewModelScope.launch {
            val lista = if (query.isBlank()) {
                productoRepository.obtenerTodos()
            } else {
                productoRepository.buscarPorCriterio(query)
            }
            _uiState.update { it.copy(productos = lista) }
        }
    }

    private fun confirmarEliminar() {
        val id = _uiState.value.idProductoEliminar ?: return
        viewModelScope.launch {
            val resultado = cambiarEstadoProductoUseCase(
                CambiarEstadoProductoParams(id = id, activar = false)
            )
            resultado.fold(
                onSuccess = {
                    cargarProductos()
                    _uiState.update {
                        it.copy(
                            mostrarDialogoEliminar = false,
                            idProductoEliminar = null,
                            mensajeAlerta = "Producto dado de baja correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            mostrarDialogoEliminar = false,
                            idProductoEliminar = null,
                            mensajeAlerta = "Error: ${error.message ?: "No se pudo cambiar el estado"}"
                        )
                    }
                }
            )
        }
    }
}