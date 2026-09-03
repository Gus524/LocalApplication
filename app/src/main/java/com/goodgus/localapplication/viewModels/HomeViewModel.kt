package com.goodgus.localapplication.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.DAO.CuentaDAO
import com.goodgus.localapplication.models.data.Cuenta as CuentaEntity
import com.goodgus.localapplication.models.dataView.GetCuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaParams
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaUseCase
import com.goodgus.localapplication.ventas.usecase.CancelarVentaParams
import com.goodgus.localapplication.ventas.usecase.CancelarVentaUseCase
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaParams
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel(
    app: Application,
    private val abrirCuentaUseCase: AbrirCuentaUseCase,
    private val cerrarCuentaUseCase: CerrarCuentaUseCase,
    private val cancelarVentaUseCase: CancelarVentaUseCase,
    private val cuentaRepository: ICuentaRepository,
    private val cuentaDAO: CuentaDAO
): AndroidViewModel(app) {
    data class CuentaUIState(
        val cuenta: Flow<Any> = emptyFlow(),
        val showDelete: Boolean = false,
        val idVenta: Int = 0,
        val showClose: Boolean = false,
        val cuentaSinVentas: List<CuentaEntity> = emptyList()
    )

    private val _uiState = MutableStateFlow(CuentaUIState())
    val uiState: StateFlow<CuentaUIState> = _uiState.asStateFlow()

    val cuenta: StateFlow<List<GetCuenta>> = cuentaDAO.getCuentaActiva()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun closeCuenta() {
        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            if (cuentaActiva != null) {
                cerrarCuentaUseCase(CerrarCuentaParams(cuentaId = cuentaActiva.id.valor))
            }
            _uiState.update { it.copy(showClose = false) }
        }
    }

    fun openCuenta() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            abrirCuentaUseCase(AbrirCuentaParams(fecha = today))
            tryCuenta()
        }
    }

    fun tryCuenta() {
        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            val listaSinVentas = if (cuentaActiva != null && cuentaActiva.ventas.isEmpty()) {
                listOf(
                    CuentaEntity(
                        idCuenta = cuentaActiva.id.valor,
                        fechaCuenta = cuentaActiva.informacion.fecha,
                        estadoCuenta = 1
                    )
                )
            } else {
                emptyList()
            }
            _uiState.update { it.copy(cuentaSinVentas = listaSinVentas) }
        }
    }

    fun showClose() {
        _uiState.update { it.copy(showClose = true) }
    }

    fun showAlert(idVenta: Int) {
        _uiState.update { it.copy(showDelete = true, idVenta = idVenta) }
    }

    fun closeAlert() {
        _uiState.update { it.copy(showDelete = false, showClose = false) }
    }

    fun deleteVenta() {
        val estado = _uiState.value
        val idVenta = estado.idVenta

        viewModelScope.launch {
            if (idVenta != 0) {
                val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
                if (cuentaActiva != null) {
                    cancelarVentaUseCase(
                        CancelarVentaParams(
                            cuentaId = cuentaActiva.id.valor,
                            ventaId = idVenta
                        )
                    )
                }
            }
            _uiState.update { it.copy(showDelete = false, idVenta = 0) }
        }
    }

    companion object {
        fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val app = application as com.goodgus.localapplication.application.LocalApplication
                    val productoDAO = app.database.productoDao()
                    val cuentaDAO = app.database.cuentaDao()
                    val cuentaRepository = com.goodgus.localapplication.ventas.data.repository.CuentaRepository(cuentaDAO, productoDAO)
                    val abrirCuentaUseCase = AbrirCuentaUseCase(cuentaRepository)
                    val cerrarCuentaUseCase = CerrarCuentaUseCase(cuentaRepository)
                    val cancelarVentaUseCase = CancelarVentaUseCase(cuentaRepository)

                    return HomeViewModel(app, abrirCuentaUseCase, cerrarCuentaUseCase, cancelarVentaUseCase, cuentaRepository, cuentaDAO) as T
                }
            }
        }
    }
}