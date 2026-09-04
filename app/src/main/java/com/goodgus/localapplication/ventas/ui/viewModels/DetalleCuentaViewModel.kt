package com.goodgus.localapplication.ventas.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleCuentaUiState(
    val cuenta: Cuenta? = null,
    val cargando: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface DetalleCuentaAction {
    data class OnCargarDetalle(val idCuenta: Int) : DetalleCuentaAction
    data object OnDismissAlerta : DetalleCuentaAction
}

@HiltViewModel
class DetalleCuentaViewModel @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleCuentaUiState())
    val uiState: StateFlow<DetalleCuentaUiState> = _uiState.asStateFlow()

    fun onAction(action: DetalleCuentaAction) {
        when (action) {
            is DetalleCuentaAction.OnCargarDetalle -> cargarDetalle(action.idCuenta)
            is DetalleCuentaAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
        }
    }

    private fun cargarDetalle(idCuenta: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val cuenta = cuentaRepository.obtenerPorId(CuentaId(idCuenta))
            if (cuenta != null) {
                _uiState.update { it.copy(cuenta = cuenta, cargando = false) }
            } else {
                _uiState.update { it.copy(cargando = false, mensajeAlerta = "Cuenta no encontrada") }
            }
        }
    }
}
