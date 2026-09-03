package com.goodgus.localapplication.compras.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.compras.usecase.CancelarCompraParams
import com.goodgus.localapplication.compras.usecase.CancelarCompraUseCase
import com.goodgus.localapplication.compras.usecase.RegistrarCompraParams
import com.goodgus.localapplication.compras.usecase.RegistrarCompraUseCase
import com.goodgus.localapplication.models.data.Compra
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompraViewModel(
    app: Application,
    private val registrarCompraUseCase: RegistrarCompraUseCase,
    private val cancelarCompraUseCase: CancelarCompraUseCase,
    private val compraRepository: ICompraRepository
): AndroidViewModel(app) {
    data class CompraUIState(
        val idCompra: Int = 0,
        val compras: List<Compra> = emptyList(),
        val isBusy: Boolean = false,
        val mensaje: String? = null
    )

    private val _uiState = MutableStateFlow(CompraUIState())
    val uiState: StateFlow<CompraUIState> = _uiState.asStateFlow()

    init {
        loadCompras()
    }

    fun loadCompras() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val compras = compraRepository.obtenerTodos()
            _uiState.update {
                it.copy(
                    compras = compras.map { c -> toUiEntity(c) },
                    isBusy = false
                )
            }
        }
    }

    fun registrarCompra(params: RegistrarCompraParams) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val resultado = registrarCompraUseCase(params)
            resultado.fold(
                onSuccess = {
                    loadCompras()
                    _uiState.update { it.copy(mensaje = "Compra registrada correctamente", isBusy = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensaje = "Error: ${error.message}", isBusy = false) }
                }
            )
        }
    }

    fun cancelarCompra(idCompra: Int) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val resultado = cancelarCompraUseCase(CancelarCompraParams(compraId = idCompra))
            resultado.fold(
                onSuccess = {
                    loadCompras()
                    _uiState.update { it.copy(mensaje = "Compra cancelada correctamente", isBusy = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensaje = "Error: ${error.message}", isBusy = false) }
                }
            )
        }
    }

    private fun toUiEntity(domain: com.goodgus.localapplication.compras.domain.model.Compra): Compra {
        return Compra(
            idCompra = domain.id.valor,
            totalCompra = domain.informacion.total.monto,
            fechaCompra = domain.informacion.fechaCompra
        )
    }

    companion object {
        fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val app = application as com.goodgus.localapplication.application.LocalApplication
                    val productoDAO = app.database.productoDao()
                    val compraDAO = app.database.compraDAO()
                    val productoRepository = com.goodgus.localapplication.inventario.data.repository.ProductoRepository(productoDAO)
                    val compraRepository = com.goodgus.localapplication.compras.data.repository.CompraRepository(compraDAO, productoDAO)
                    val registrarCompraUseCase = RegistrarCompraUseCase(compraRepository, productoRepository)
                    val cancelarCompraUseCase = CancelarCompraUseCase(compraRepository)

                    return CompraViewModel(app, registrarCompraUseCase, cancelarCompraUseCase, compraRepository) as T
                }
            }
        }
    }
}