package com.goodgus.localapplication.inventario.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioParams
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioProductoUseCase
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoParams
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoUseCase
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

data class ProductoFormUiState(
    val id: Int? = null,
    val nombre: String = "",
    val marca: String = "",
    val tipo: String = "",
    val precioVenta: Double = 0.0,
    val stock: Int = 0,
    val esEdicion: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface ProductoFormAction {
    data object OnIniciarNuevo : ProductoFormAction
    data class OnNombreChange(val valor: String) : ProductoFormAction
    data class OnMarcaChange(val valor: String) : ProductoFormAction
    data class OnTipoChange(val valor: String) : ProductoFormAction
    data class OnPrecioChange(val valor: Double) : ProductoFormAction
    data class OnStockChange(val valor: Int) : ProductoFormAction
    data class OnCargarProducto(val id: Int) : ProductoFormAction
    data object OnGuardar : ProductoFormAction
    data object OnDismissAlerta : ProductoFormAction
}

sealed interface ProductoFormEffect {
    data object NavegarAtras : ProductoFormEffect
}

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarPrecioProductoUseCase: ActualizarPrecioProductoUseCase,
    private val productoRepository: IProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoFormUiState())
    val uiState: StateFlow<ProductoFormUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProductoFormEffect>(Channel.BUFFERED)
    val effect: Flow<ProductoFormEffect> = _effect.receiveAsFlow()

    fun onAction(action: ProductoFormAction) {
        when (action) {
            is ProductoFormAction.OnIniciarNuevo -> _uiState.value = ProductoFormUiState()
            is ProductoFormAction.OnNombreChange -> _uiState.update { it.copy(nombre = action.valor) }
            is ProductoFormAction.OnMarcaChange -> _uiState.update { it.copy(marca = action.valor) }
            is ProductoFormAction.OnTipoChange -> _uiState.update { it.copy(tipo = action.valor) }
            is ProductoFormAction.OnPrecioChange -> _uiState.update { it.copy(precioVenta = action.valor) }
            is ProductoFormAction.OnStockChange -> _uiState.update { it.copy(stock = action.valor) }
            is ProductoFormAction.OnCargarProducto -> cargarProducto(action.id)
            is ProductoFormAction.OnGuardar -> guardar()
            is ProductoFormAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
        }
    }

    private fun cargarProducto(id: Int) {
        viewModelScope.launch {
            val producto = productoRepository.obtenerPorId(ProductoId(id))
            if (producto != null) {
                _uiState.update {
                    it.copy(
                        id = producto.id.valor,
                        nombre = producto.nombre,
                        marca = producto.marca,
                        tipo = producto.tipo,
                        precioVenta = producto.precioVenta.monto,
                        stock = producto.inventario.disponibles,
                        esEdicion = true
                    )
                }
            } else {
                _uiState.update { it.copy(mensajeAlerta = "Producto no encontrado") }
            }
        }
    }

    private fun guardar() {
        val estado = _uiState.value
        viewModelScope.launch {
            val resultado = if (estado.esEdicion && estado.id != null) {
                actualizarPrecioProductoUseCase(
                    ActualizarPrecioParams(id = estado.id, nuevoPrecio = estado.precioVenta)
                )
            } else {
                registrarProductoUseCase(
                    RegistrarProductoParams(
                        nombre = estado.nombre,
                        marca = estado.marca,
                        tipo = estado.tipo,
                        precioVenta = estado.precioVenta,
                        stockInicial = estado.stock
                    )
                )
            }

            resultado.fold(
                onSuccess = {
                    _effect.trySend(ProductoFormEffect.NavegarAtras)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(mensajeAlerta = "Error: ${error.message ?: "Operación fallida"}")
                    }
                }
            )
        }
    }
}