package com.goodgus.localapplication.ventas.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaParams
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaUseCase
import com.goodgus.localapplication.ventas.usecase.CancelarVentaParams
import com.goodgus.localapplication.ventas.usecase.CancelarVentaUseCase
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaParams
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CuentaUiState(
    val cuentaActiva: Cuenta? = null,
    val idVentaEliminar: Int? = null,
    val mostrarDialogoEliminarVenta: Boolean = false,
    val mostrarDialogoCerrarCuenta: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface CuentaAction {
    data object OnCargarCuenta : CuentaAction
    data object OnAbrirCuenta : CuentaAction
    data object OnSolicitarCerrarCuenta : CuentaAction
    data object OnConfirmarCerrarCuenta : CuentaAction
    data object OnCancelarCerrarCuenta : CuentaAction
    data class OnSolicitarEliminarVenta(val idVenta: Int) : CuentaAction
    data object OnConfirmarEliminarVenta : CuentaAction
    data object OnCancelarEliminarVenta : CuentaAction
    data object OnDismissAlerta : CuentaAction
    data object OnNuevaVenta : CuentaAction
    data class OnEditarVenta(val idVenta: Int) : CuentaAction
}

sealed interface CuentaEffect {
    data object NavegarANuevaVenta : CuentaEffect
    data class NavegarAEditarVenta(val idVenta: Int) : CuentaEffect
}

@HiltViewModel
class CuentaViewModel @Inject constructor(
    private val abrirCuentaUseCase: AbrirCuentaUseCase,
    private val cerrarCuentaUseCase: CerrarCuentaUseCase,
    private val cancelarVentaUseCase: CancelarVentaUseCase,
    private val cuentaRepository: ICuentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CuentaEffect>(Channel.BUFFERED)
    val effect: Flow<CuentaEffect> = _effect.receiveAsFlow()

    init {
        cargarCuenta()
    }

    fun onAction(action: CuentaAction) {
        when (action) {
            is CuentaAction.OnCargarCuenta -> cargarCuenta()
            is CuentaAction.OnAbrirCuenta -> abrirCuenta()
            is CuentaAction.OnSolicitarCerrarCuenta -> _uiState.update { it.copy(mostrarDialogoCerrarCuenta = true) }
            is CuentaAction.OnConfirmarCerrarCuenta -> confirmarCerrarCuenta()
            is CuentaAction.OnCancelarCerrarCuenta -> _uiState.update { it.copy(mostrarDialogoCerrarCuenta = false) }
            is CuentaAction.OnSolicitarEliminarVenta -> _uiState.update {
                it.copy(mostrarDialogoEliminarVenta = true, idVentaEliminar = action.idVenta)
            }
            is CuentaAction.OnConfirmarEliminarVenta -> confirmarEliminarVenta()
            is CuentaAction.OnCancelarEliminarVenta -> _uiState.update {
                it.copy(mostrarDialogoEliminarVenta = false, idVentaEliminar = null)
            }
            is CuentaAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
            is CuentaAction.OnNuevaVenta -> _effect.trySend(CuentaEffect.NavegarANuevaVenta)
            is CuentaAction.OnEditarVenta -> _effect.trySend(CuentaEffect.NavegarAEditarVenta(action.idVenta))
        }
    }

    private fun cargarCuenta() {
        viewModelScope.launch {
            val cuenta = cuentaRepository.obtenerCuentaActiva()
            _uiState.update { it.copy(cuentaActiva = cuenta) }
        }
    }

    private fun abrirCuenta() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            val resultado = abrirCuentaUseCase(AbrirCuentaParams(fecha = today))
            resultado.fold(
                onSuccess = {
                    cargarCuenta()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(mensajeAlerta = "Error: ${error.message ?: "No se pudo abrir la cuenta"}")
                    }
                }
            )
        }
    }

    private fun confirmarCerrarCuenta() {
        val cuenta = _uiState.value.cuentaActiva ?: return
        viewModelScope.launch {
            val resultado = cerrarCuentaUseCase(CerrarCuentaParams(cuentaId = cuenta.id.valor))
            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            cuentaActiva = null,
                            mostrarDialogoCerrarCuenta = false,
                            mensajeAlerta = "Cuenta cerrada exitosamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCerrarCuenta = false,
                            mensajeAlerta = "Error: ${error.message ?: "No se pudo cerrar la cuenta"}"
                        )
                    }
                }
            )
        }
    }

    private fun confirmarEliminarVenta() {
        val cuenta = _uiState.value.cuentaActiva ?: return
        val idVenta = _uiState.value.idVentaEliminar ?: return
        viewModelScope.launch {
            val resultado = cancelarVentaUseCase(
                CancelarVentaParams(
                    cuentaId = cuenta.id.valor,
                    ventaId = idVenta
                )
            )
            resultado.fold(
                onSuccess = {
                    cargarCuenta()
                    _uiState.update {
                        it.copy(
                            mostrarDialogoEliminarVenta = false,
                            idVentaEliminar = null,
                            mensajeAlerta = "Venta anulada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            mostrarDialogoEliminarVenta = false,
                            idVentaEliminar = null,
                            mensajeAlerta = "Error: ${error.message ?: "No se pudo anular la venta"}"
                        )
                    }
                }
            )
        }
    }
}