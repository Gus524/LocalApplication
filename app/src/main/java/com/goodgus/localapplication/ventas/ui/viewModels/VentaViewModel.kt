package com.goodgus.localapplication.ventas.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.usecase.RegistrarVentaParams
import com.goodgus.localapplication.ventas.usecase.RegistrarVentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class VentaFormUiState(
    val busqueda: String = "",
    val resultadosBusqueda: List<Producto> = emptyList(),
    val productoSeleccionado: Producto? = null,
    val precioUnitario: Double = 0.0,
    val cantidad: Int = 1,
    val subtotal: Double = 0.0,
    val esEdicion: Boolean = false,
    val idVenta: Int? = null,
    val mostrarResultados: Boolean = false,
    val mensajeAlerta: String? = null
)

sealed interface VentaFormAction {
    data object OnIniciarNuevo : VentaFormAction
    data class OnBuscarProducto(val query: String) : VentaFormAction
    data class OnSeleccionarProducto(val producto: Producto) : VentaFormAction
    data class OnPrecioChange(val precio: Double) : VentaFormAction
    data class OnCantidadChange(val cantidad: Int) : VentaFormAction
    data class OnCargarVenta(val idVenta: Int) : VentaFormAction
    data object OnGuardarVenta : VentaFormAction
    data object OnDismissAlerta : VentaFormAction
}

sealed interface VentaFormEffect {
    data object NavegarAtras : VentaFormEffect
}

@HiltViewModel
class VentaViewModel @Inject constructor(
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val productoRepository: IProductoRepository,
    private val cuentaRepository: ICuentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaFormUiState())
    val uiState: StateFlow<VentaFormUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VentaFormEffect>(Channel.BUFFERED)
    val effect: Flow<VentaFormEffect> = _effect.receiveAsFlow()

    fun onAction(action: VentaFormAction) {
        when (action) {
            is VentaFormAction.OnIniciarNuevo -> _uiState.value = VentaFormUiState()
            is VentaFormAction.OnBuscarProducto -> buscarProductos(action.query)
            is VentaFormAction.OnSeleccionarProducto -> seleccionarProducto(action.producto)
            is VentaFormAction.OnPrecioChange -> actualizarPrecio(action.precio)
            is VentaFormAction.OnCantidadChange -> actualizarCantidad(action.cantidad)
            is VentaFormAction.OnCargarVenta -> cargarVenta(action.idVenta)
            is VentaFormAction.OnGuardarVenta -> guardarVenta()
            is VentaFormAction.OnDismissAlerta -> _uiState.update { it.copy(mensajeAlerta = null) }
        }
    }

    private fun buscarProductos(query: String) {
        _uiState.update { it.copy(busqueda = query) }
        viewModelScope.launch {
            val lista = if (query.isBlank()) {
                productoRepository.obtenerTodos()
            } else {
                productoRepository.buscarPorCriterio(query)
            }
            _uiState.update {
                it.copy(resultadosBusqueda = lista, mostrarResultados = true)
            }
        }
    }

    private fun seleccionarProducto(producto: Producto) {
        _uiState.update {
            val precio = producto.precioVenta.monto
            val subtotal = it.cantidad * precio
            it.copy(
                productoSeleccionado = producto,
                precioUnitario = precio,
                mostrarResultados = false,
                busqueda = producto.nombre,
                subtotal = subtotal
            )
        }
    }

    private fun actualizarPrecio(precio: Double) {
        _uiState.update {
            val subtotal = it.cantidad * precio
            it.copy(precioUnitario = precio, subtotal = subtotal)
        }
    }

    private fun actualizarCantidad(cantidad: Int) {
        _uiState.update {
            val subtotal = cantidad * it.precioUnitario
            it.copy(cantidad = cantidad, subtotal = subtotal)
        }
    }

    private fun guardarVenta() {
        val estado = _uiState.value
        val producto = estado.productoSeleccionado

        if (producto == null) {
            _uiState.update { it.copy(mensajeAlerta = "Debe seleccionar un producto") }
            return
        }

        if (estado.cantidad <= 0 || estado.cantidad > producto.inventario.disponibles) {
            _uiState.update { it.copy(mensajeAlerta = "Existencias insuficientes o cantidad inválida") }
            return
        }

        if (estado.precioUnitario < 0.0) {
            _uiState.update { it.copy(mensajeAlerta = "El precio no puede ser negativo") }
            return
        }

        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            if (cuentaActiva == null) {
                _uiState.update { it.copy(mensajeAlerta = "No hay ninguna cuenta abierta actualmente") }
                return@launch
            }

            val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val resultado = registrarVentaUseCase(
                RegistrarVentaParams(
                    cuentaId = cuentaActiva.id.valor,
                    productoId = producto.id.valor,
                    cantidad = estado.cantidad,
                    hora = horaActual,
                    precioPersonalizado = estado.precioUnitario
                )
            )

            resultado.fold(
                onSuccess = {
                    _effect.trySend(VentaFormEffect.NavegarAtras)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(mensajeAlerta = "Error: ${error.message ?: "No se pudo registrar la venta"}")
                    }
                }
            )
        }
    }

    private fun cargarVenta(idVenta: Int) {
        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            val venta = cuentaActiva?.ventas?.find { it.id.valor == idVenta }

            if (venta != null) {
                val producto = productoRepository.obtenerPorId(venta.productoId)
                if (producto != null) {
                    _uiState.update {
                        it.copy(
                            productoSeleccionado = producto,
                            idVenta = venta.id.valor,
                            precioUnitario = venta.detalle.precioUnitario.monto,
                            cantidad = venta.detalle.cantidad.valor,
                            subtotal = venta.subtotal.monto,
                            busqueda = producto.nombre,
                            esEdicion = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(mensajeAlerta = "Producto no encontrado") }
                }
            } else {
                _uiState.update { it.copy(mensajeAlerta = "Venta no encontrada") }
            }
        }
    }
}