package com.goodgus.localapplication.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.models.data.Cuenta
import com.goodgus.localapplication.models.dataView.GetCuenta
import com.goodgus.localapplication.repository.CuentaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    app: Application,
    private val cuentaRepository: CuentaRepository
): AndroidViewModel(app) {
    data class CuentaUIState(
        val cuenta: Flow<Any> = emptyFlow(),
        val showDelete: Boolean = false,
        val idVenta: Int = 0,
        val showClose: Boolean = false,
        val cuentaSinVentas: List<Cuenta> = emptyList()
    )

    private val _uiState = MutableStateFlow(CuentaUIState())
    val uiState: StateFlow<CuentaUIState> = _uiState.asStateFlow()

    val cuenta : StateFlow<List<GetCuenta>> = cuentaRepository.getCuentaActiva()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun closeCuenta() {
        viewModelScope.launch(Dispatchers.IO) {
            cuentaRepository.closeAccount()
        }
        _uiState.update { it.copy(showClose = false) }
    }

    fun openCuenta() {
        viewModelScope.launch(Dispatchers.IO) {
            cuentaRepository.addAccount()
            _uiState.update { it.copy(cuentaSinVentas = cuentaRepository.getCuentaNoVentas()) }
        }
    }

    fun tryCuenta() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(cuentaSinVentas = cuentaRepository.getCuentaNoVentas()) }
        }
    }
    fun showClose(){
        _uiState.update { it.copy(showClose = true) }
    }

    fun showAlert(idVenta: Int) {
        _uiState.update { it.copy(showDelete = true, idVenta = idVenta) }
    }

    fun closeAlert(){
        _uiState.update { it.copy(showDelete = false, showClose = false) }
    }

    fun deleteVenta() {
        val estado = _uiState.value
        val idVenta = estado.idVenta

        viewModelScope.launch (Dispatchers.IO){
            if(idVenta != 0) {
                cuentaRepository.removeVenta(idVenta)
            }
            _uiState.update { it.copy(showDelete = false, idVenta = 0) }
        }
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val ventaDAO = app.database.ventaDao()
                    val cuentaDAO = app.database.cuentaDao()
                    val cuentaRepository = CuentaRepository(cuentaDAO, ventaDAO)

                    return HomeViewModel(app, cuentaRepository) as T
                }
            }
        }
    }
}