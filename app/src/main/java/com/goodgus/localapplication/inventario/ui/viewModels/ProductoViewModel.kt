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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarPrecioProductoUseCase: ActualizarPrecioProductoUseCase,
    private val productoRepository: IProductoRepository
) : ViewModel() {
    data class ProductoUIState(
        val cantidad: Int = 0,
        val mensaje: String? = null,
        val isBusy: Boolean = false,
        val isUpdate: Boolean = false,
        val idProducto: String = "",
        val lazyVisible: Boolean = false,
        val name: String = "",
        val precio: Double = 0.0,
        val marca: String = "",
        val tipo: String = ""
    )

    private val _uiState = MutableStateFlow(ProductoUIState())
    val uiState: StateFlow<ProductoUIState> = _uiState.asStateFlow()

    fun updateCantidad(cantidad: Int) {
        _uiState.update { it.copy(cantidad = cantidad) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updatePrecio(precio: Double) {
        _uiState.update { it.copy(precio = precio) }
    }

    fun updateMarca(marca: String) {
        _uiState.update { it.copy(marca = marca) }
    }

    fun updateTipo(tipo: String) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    fun guardarProducto() {
        val estado = _uiState.value
        _uiState.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            val resultado = if (estado.isUpdate) {
                actualizarPrecioProductoUseCase(
                    ActualizarPrecioParams(
                        id = estado.idProducto.toIntOrNull() ?: 0,
                        nuevoPrecio = estado.precio
                    )
                )
            } else {
                registrarProductoUseCase(
                    RegistrarProductoParams(
                        nombre = estado.name,
                        marca = estado.marca,
                        tipo = estado.tipo,
                        precioVenta = estado.precio,
                        stockInicial = estado.cantidad
                    )
                )
            }

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = if (estado.isUpdate) "Producto actualizado correctamente" else "Producto registrado correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = "Error: ${error.message ?: "Operación fallida"}"
                        )
                    }
                }
            )
        }
    }

    fun cargarProducto(idProducto: String?) {
        if (idProducto.isNullOrBlank()) return

        val idInt = idProducto.toIntOrNull() ?: return
        _uiState.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            val producto = productoRepository.obtenerPorId(ProductoId(idInt))
            if (producto != null) {
                _uiState.update {
                    it.copy(
                        idProducto = idProducto,
                        name = producto.nombre,
                        marca = producto.marca,
                        precio = producto.precioVenta.monto,
                        cantidad = producto.inventario.disponibles,
                        tipo = producto.tipo,
                        isUpdate = true,
                        isBusy = false
                    )
                }
            } else {
                _uiState.update { it.copy(isBusy = false, mensaje = "Producto no encontrado") }
            }
        }
    }
}