package com.goodgus.localapplication.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.models.data.Venta
import com.goodgus.localapplication.repository.CuentaRepository
import com.goodgus.localapplication.repository.ProductoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VentaViewModel(
    app: Application,
    private val ventaRepository: CuentaRepository,
    private val productoRepository: ProductoRepository
): AndroidViewModel(app) {
    data class VentaUIState(
        val productoSeleccionado: Producto? = null,
        val cantidad: Int = 1,
        val resultado: List<Producto> = emptyList(),
        val mensaje: String? = null,
        val isBusy: Boolean = false,
        val isUpdate: Boolean = false,
        val idVenta: Int = 0,
        val idCuenta: Int = 0,
        val lazyVisible: Boolean = false,
        val search: String = "",
        val parcialVenta: Double = 0.0
    )

    private val _uiState = MutableStateFlow(VentaUIState())
    val uiState: StateFlow<VentaUIState> = _uiState.asStateFlow()

    fun searchProduct() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch (Dispatchers.IO){
            val productos = productoRepository.searchProduct(uiState.value.search)
            _uiState.update {
                it.copy(
                    resultado = productos,
                    isBusy = false,
                    lazyVisible = true
                )
            }
        }
    }

    fun selectProduct(producto: Producto) {
        _uiState.update { it.copy(productoSeleccionado = producto, lazyVisible = false, search = producto.nombre) }
        updateParcial()
    }

    fun updateCantidad(cantidad: Int) {
        _uiState.update { it.copy(cantidad = cantidad) }
        updateParcial()
    }

    fun updateSearch(search: String) {
        _uiState.update { it.copy(search = search) }
    }

    private fun updateParcial() {
        _uiState.update { it.copy(parcialVenta = it.cantidad * it.productoSeleccionado!!.precioVenta ) }
    }

    fun guardarVenta() {
        val estado = _uiState.value
        val producto = estado.productoSeleccionado
        val cantidad = estado.cantidad

        if(producto == null || cantidad > producto.disponibles) {
            _uiState.update { it.copy(mensaje = "Error en los datos") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val idCuenta = ventaRepository.getAccountId()
            println(idCuenta)
            if(estado.isUpdate) {
                ventaRepository.updateVenta(
                    idVenta = estado.idVenta,
                    cantidad = cantidad,
                    idProducto = producto.idProducto,
                    parcialVenta = estado.parcialVenta
                )
            } else {
                   ventaRepository.addVenta(
                       cantidad = cantidad,
                       idCuenta = idCuenta,
                       idProducto = producto.idProducto,
                       parcialVenta = estado.parcialVenta
                   )
                }
            _uiState.update { it.copy(mensaje = "Venta regstrada correctamente") }
            }
        }

    fun cargarVenta(idVenta: String?) {
        if(idVenta == "") return

        viewModelScope.launch(Dispatchers.IO) {
            val venta = ventaRepository.getCuentaId(idVenta = idVenta?.toInt() ?: 0)
            if(venta != null) {
                val producto = productoRepository.getProductId(venta.idProducto)
                _uiState.update {
                    it.copy(
                        productoSeleccionado = producto,
                        idVenta = venta.idVenta,
                        idCuenta = venta.idCuenta,
                        cantidad = venta.cantidadProducto,
                        isUpdate = true
                    )
                }
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val ventaDAO = app.database.ventaDao()
                    val cuentaDAO = app.database.cuentaDao()
                    val productoDAO = app.database.productoDao()
                    val productoRepository = ProductoRepository(productoDAO)
                    val cuentaRepository = CuentaRepository(cuentaDAO, ventaDAO)

                    return VentaViewModel(app, cuentaRepository, productoRepository) as T
                }
            }
        }
    }
}