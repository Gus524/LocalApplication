package com.goodgus.localapplication.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.models.data.Compra
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.repository.CompraRepository
import com.goodgus.localapplication.repository.ProductoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompraViewModel(
    app: Application,
    private val compraRepository: CompraRepository
): AndroidViewModel(app) {
    data class CompraUIState(
        val idCompra: Int = 0,
        val compras: List<Compra> = emptyList(),
        val isBusy: Boolean = false
    )

    private val _uiState = MutableStateFlow(CompraUIState())
    val uiState: StateFlow<CompraUIState> = _uiState.asStateFlow()

    init {
        loadCompras()
    }

    private fun loadCompras() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch(Dispatchers.IO){
            val compras = compraRepository.getCompras()
            _uiState.update { it.copy(
                compras = compras,
                isBusy = false
            ) }
        }
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val compraDAO = app.database.compraDAO()
                    val compraProductoDAO = app.database.compraProductoDAO()
                    val compraRepository = CompraRepository(compraDAO, compraProductoDAO)

                    return CompraViewModel(app, compraRepository) as T
                }
            }
        }
    }
}