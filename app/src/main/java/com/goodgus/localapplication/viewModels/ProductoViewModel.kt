package com.goodgus.localapplication.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.repository.ProductoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductoViewModel(
    app: Application,
    private val productoRepository: ProductoRepository
): AndroidViewModel(app) {
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

        viewModelScope.launch(Dispatchers.IO) {
            if(estado.isUpdate) {
                productoRepository.editProduct(
                    idProducto = estado.idProducto.toInt(),
                    nombre = estado.name,
                    marca = estado.marca,
                    precioVenta = estado.precio,
                    disponibles = estado.cantidad,
                    tipo = estado.tipo
                )
            } else {
                productoRepository.addProduct(
                    nombre = estado.name,
                    marca = estado.marca,
                    precioVenta = estado.precio,
                    disponibles = estado.cantidad,
                    tipo = estado.tipo
                )
            }
            _uiState.update { it.copy(mensaje = "Producto registrado correctamente") }
        }
    }

    fun cargarProducto(idProducto: String?) {
        if(idProducto == "") return

        viewModelScope.launch(Dispatchers.IO) {
            val producto = productoRepository.getProductId(idProducto?.toIntOrNull() ?: 0)
            _uiState.update {
                it.copy(
                    idProducto = idProducto.toString(),
                    name = producto.nombre,
                    marca = producto.marca,
                    precio = producto.precioVenta,
                    cantidad = producto.disponibles,
                    tipo = producto.tipo,
                    isUpdate = true
                )
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val productoDAO = app.database.productoDao()
                    val productoRepository = ProductoRepository(productoDAO)

                    return ProductoViewModel(app, productoRepository) as T
                }
            }
        }
    }
}