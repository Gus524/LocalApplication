package com.goodgus.localapplication.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.repository.PedidoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditPedidoViewModel(
    app: Application,
    private val pedidoRepository: PedidoRepository
): AndroidViewModel(app) {
    data class PedidoUIState(
        val cantidad: Int = 0,
        val descripcion: String = "",
        val detalles: String = "",
        val mensaje: String = ""
    )

    private val _uiState = MutableStateFlow(PedidoUIState())
    val uiState: StateFlow<PedidoUIState> = _uiState.asStateFlow()


    fun updateDescripcion(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    fun updateDetalles(detalles: String) {
        _uiState.update { it.copy(detalles = detalles) }
    }


    fun guardarPedido() {
        val estado = _uiState.value
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        viewModelScope.launch(Dispatchers.IO) {
            pedidoRepository.addPedido(
                fechaPedido = today.format(formato),
                descripcion = estado.descripcion,
                detalles = estado.detalles
            )
            _uiState.update { it.copy(mensaje = "Producto registrado correctamente") }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as LocalApplication
                    val pedidosDAO = app.database.pedidosDAO()
                    val pedidoRepository = PedidoRepository(pedidosDAO)

                    return EditPedidoViewModel(app, pedidoRepository) as T
                }
            }
        }
    }
}