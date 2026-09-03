package com.goodgus.localapplication.inventario.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoParams
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoUseCase
import com.goodgus.localapplication.models.data.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventarioViewModel(
    app: Application,
    private val cambiarEstadoProductoUseCase: CambiarEstadoProductoUseCase,
    private val productoRepository: IProductoRepository
): AndroidViewModel(app) {
    data class InventarioUIState(
        val showDelete: Boolean = false,
        val idProducto: Int = 0,
        val productos: List<Producto> = emptyList(),
        val isBusy: Boolean = false,
        val searchText: String = ""
    )

    private val _uiState = MutableStateFlow(InventarioUIState())
    val uiState: StateFlow<InventarioUIState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val domainProducts = productoRepository.obtenerTodos()
            _uiState.update {
                it.copy(
                    productos = domainProducts.map { p -> toUiEntity(p) },
                    isBusy = false
                )
            }
        }
    }

    fun updateSearch(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun searchProducts() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val searchText = _uiState.value.searchText
            val domainProducts = if (searchText.isBlank()) {
                productoRepository.obtenerTodos()
            } else {
                productoRepository.buscarPorCriterio(searchText)
            }
            _uiState.update {
                it.copy(
                    productos = domainProducts.map { p -> toUiEntity(p) },
                    isBusy = false
                )
            }
        }
    }

    fun showAlert(idProducto: Int) {
        _uiState.update { it.copy(showDelete = true, idProducto = idProducto) }
    }

    fun closeAlert() {
        _uiState.update { it.copy(showDelete = false) }
    }

    fun downProduct() {
        val estado = _uiState.value
        val idProducto = estado.idProducto

        viewModelScope.launch {
            if (idProducto != 0) {
                cambiarEstadoProductoUseCase(
                    CambiarEstadoProductoParams(id = idProducto, activar = false)
                )
                loadProducts()
            }
            _uiState.update { it.copy(showDelete = false, idProducto = 0) }
        }
    }

    private fun toUiEntity(domain: com.goodgus.localapplication.inventario.domain.model.Producto): Producto {
        return Producto(
            idProducto = domain.id.valor,
            nombre = domain.nombre,
            marca = domain.marca,
            precioVenta = domain.precioVenta.monto,
            disponibles = domain.inventario.disponibles,
            tipo = domain.tipo,
            estado = if (domain.estaActivo) 1 else 2
        )
    }

    companion object {
        fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val app = application as com.goodgus.localapplication.application.LocalApplication
                    val productoDAO = app.database.productoDao()
                    val productoRepository = com.goodgus.localapplication.inventario.data.repository.ProductoRepository(productoDAO)
                    val cambiarEstadoUseCase = CambiarEstadoProductoUseCase(productoRepository)

                    return InventarioViewModel(app, cambiarEstadoUseCase, productoRepository) as T
                }
            }
        }
    }
}