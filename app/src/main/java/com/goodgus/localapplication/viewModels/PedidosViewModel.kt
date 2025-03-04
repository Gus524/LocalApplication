package com.goodgus.localapplication.viewModels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.models.data.Pedidos
import com.goodgus.localapplication.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class PedidosViewModel(
    app: Application,
    private val pedidoRepository: PedidoRepository,
) : AndroidViewModel(app) {
    data class PedidoUIState(
        val pedidoSeleccionado: Pedidos? = null
    )

    private val _uiState = MutableStateFlow(PedidoUIState())
    val uiState: StateFlow<PedidoUIState> = _uiState.asStateFlow()

    val pedidos : StateFlow<List<Pedidos>> = pedidoRepository.getPedidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @Suppress("UNCHECKED_CAST")
    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val pedidosDAO = app.database.pedidosDAO()
                    val pedidoRepository = PedidoRepository(pedidosDAO)

                    return PedidosViewModel(app, pedidoRepository) as T
                }
            }
        }
    }
}