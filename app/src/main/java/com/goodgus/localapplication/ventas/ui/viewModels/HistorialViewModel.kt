package com.goodgus.localapplication.ventas.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistorialUiState(
    val cuentas: List<Cuenta> = emptyList(),
    val cargando: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface HistorialAction {
    data object OnCargarHistorial : HistorialAction
    data class OnSeleccionarCuenta(val idCuenta: Int) : HistorialAction
    data object OnDismissAlerta : HistorialAction
}

sealed interface HistorialEffect {
    data class NavegarADetalleCuenta(val idCuenta: Int) : HistorialEffect
}

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(cargando = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HistorialEffect>(Channel.BUFFERED)
    val effect: Flow<HistorialEffect> = _effect.receiveAsFlow()

    private var historialJob: Job? = null

    init {
        observarHistorial()
    }

    fun onAction(action: HistorialAction) {
        when (action) {
            is HistorialAction.OnCargarHistorial -> observarHistorial()
            is HistorialAction.OnSeleccionarCuenta -> _effect.trySend(HistorialEffect.NavegarADetalleCuenta(action.idCuenta))
            is HistorialAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
        }
    }

    private fun observarHistorial() {
        historialJob?.cancel()
        historialJob = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            cuentaRepository.observarCuentasCerradas().collect { lista ->
                _uiState.update { it.copy(cuentas = lista, cargando = false) }
            }
        }
    }
}
