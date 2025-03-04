package com.goodgus.localapplication.viewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventarioViewModel(
    app: Application,
    private val productoRepository: ProductoRepository
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

    private fun loadProducts() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch(Dispatchers.IO){
            val productos = productoRepository.getStorage()
            Log.d("ViewModel", "Productos cargados: ${productos.size}")
            _uiState.update { it.copy(
                productos = productos,
                isBusy = false
            ) }
        }
    }

    fun updateSearch(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun searchProducts() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val searchText = _uiState.value.searchText
            val productos = if(searchText.isBlank()) {
                productoRepository.getStorage()
            } else {
                productoRepository.searchProduct(searchText)
            }
            _uiState.update { it.copy(
                productos = productos,
                isBusy = false
            ) }
        }
    }

    fun showAlert(idProducto: Int) {
        _uiState.update { it.copy(showDelete = true, idProducto = idProducto) }
    }

    fun closeAlert(){
        _uiState.update { it.copy(showDelete = false) }
    }

    fun downProduct() {
        val estado = _uiState.value
        val idProducto = estado.idProducto

        viewModelScope.launch (Dispatchers.IO){
            if(idProducto != 0) {
                productoRepository.downProduct(idProducto)

                loadProducts()
            }
            _uiState.update { it.copy(showDelete = false, idProducto = 0) }
        }
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val productoDAO = app.database.productoDao()
                    val productoRepository = ProductoRepository(productoDAO)

                    return InventarioViewModel(app, productoRepository) as T
                }
            }
        }
    }
}