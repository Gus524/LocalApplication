package com.goodgus.localapplication.compras.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.compras.usecase.CancelarCompraParams
import com.goodgus.localapplication.compras.usecase.CancelarCompraUseCase
import com.goodgus.localapplication.compras.usecase.RegistrarCompraParams
import com.goodgus.localapplication.compras.usecase.RegistrarCompraUseCase
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

data class CompraUiState(
    val compras: List<Compra> = emptyList(),
    val idCompraCancelar: Int? = null,
    val mostrarDialogoCancelar: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface CompraAction {
    data object OnCargarCompras : CompraAction
    data class OnRegistrarCompra(val params: RegistrarCompraParams) : CompraAction
    data class OnSolicitarCancelarCompra(val idCompra: Int) : CompraAction
    data object OnConfirmarCancelarCompra : CompraAction
    data object OnDescartarCancelarCompra : CompraAction
    data object OnDismissAlerta : CompraAction
    data object OnNuevaCompra : CompraAction
}

sealed interface CompraEffect {
    data object NavegarANuevaCompra : CompraEffect
}

@HiltViewModel
class CompraViewModel @Inject constructor(
    private val registrarCompraUseCase: RegistrarCompraUseCase,
    private val cancelarCompraUseCase: CancelarCompraUseCase,
    private val compraRepository: ICompraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompraUiState())
    val uiState: StateFlow<CompraUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CompraEffect>(Channel.BUFFERED)
    val effect: Flow<CompraEffect> = _effect.receiveAsFlow()

    private var comprasJob: Job? = null

    init {
        observarCompras()
    }

    fun onAction(action: CompraAction) {
        when (action) {
            is CompraAction.OnCargarCompras -> observarCompras()
            is CompraAction.OnRegistrarCompra -> registrarCompra(action.params)
            is CompraAction.OnSolicitarCancelarCompra -> _uiState.update {
                it.copy(mostrarDialogoCancelar = true, idCompraCancelar = action.idCompra)
            }
            is CompraAction.OnConfirmarCancelarCompra -> confirmarCancelarCompra()
            is CompraAction.OnDescartarCancelarCompra -> _uiState.update {
                it.copy(mostrarDialogoCancelar = false, idCompraCancelar = null)
            }
            is CompraAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
            is CompraAction.OnNuevaCompra -> _effect.trySend(CompraEffect.NavegarANuevaCompra)
        }
    }

    private fun observarCompras() {
        comprasJob?.cancel()
        comprasJob = viewModelScope.launch {
            compraRepository.observarTodos().collect { lista ->
                _uiState.update { it.copy(compras = lista) }
            }
        }
    }

    private fun registrarCompra(params: RegistrarCompraParams) {
        viewModelScope.launch {
            val resultado = registrarCompraUseCase(params)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(mensajeAlerta = "Compra registrada correctamente") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensajeAlerta = "Error: ${error.message ?: "No se pudo registrar la compra"}") }
                }
            )
        }
    }

    private fun confirmarCancelarCompra() {
        val id = _uiState.value.idCompraCancelar ?: return
        viewModelScope.launch {
            val resultado = cancelarCompraUseCase(CancelarCompraParams(compraId = id))
            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCancelar = false,
                            idCompraCancelar = null,
                            mensajeAlerta = "Compra cancelada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCancelar = false,
                            idCompraCancelar = null,
                            mensajeAlerta = "Error: ${error.message ?: "No se pudo cancelar la compra"}"
                        )
                    }
                }
            )
        }
    }
}