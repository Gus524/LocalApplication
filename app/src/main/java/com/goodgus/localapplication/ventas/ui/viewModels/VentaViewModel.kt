package com.goodgus.localapplication.ventas.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.usecase.RegistrarVentaParams
import com.goodgus.localapplication.ventas.usecase.RegistrarVentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class VentaViewModel @Inject constructor(
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val productoRepository: IProductoRepository,
    private val cuentaRepository: ICuentaRepository
) : ViewModel() {
    data class VentaUIState(
        val productoSeleccionado: Producto? = null,
        val cantidad: Int = 1,
        val resultado: List<Producto> = emptyList(),
        val mensaje: String? = null,
        val isBusy: Boolean = false,
        val isUpdate: Boolean = false,
        val idVenta: Int = 0,
        val idCuenta: Int = 0,
        val lazyVisible: Boolean = false,
        val search: String = "",
        val parcialVenta: Double = 0.0
    )

    private val _uiState = MutableStateFlow(VentaUIState())
    val uiState: StateFlow<VentaUIState> = _uiState.asStateFlow()

    fun searchProduct() {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val query = _uiState.value.search
            val productos = if (query.isBlank()) {
                productoRepository.obtenerTodos()
            } else {
                productoRepository.buscarPorCriterio(query)
            }
            _uiState.update {
                it.copy(
                    resultado = productos.map { p -> toUiEntity(p) },
                    isBusy = false,
                    lazyVisible = true
                )
            }
        }
    }

    fun selectProduct(producto: Producto) {
        _uiState.update {
            it.copy(
                productoSeleccionado = producto,
                lazyVisible = false,
                search = producto.nombre
            )
        }
        updateParcial()
    }

    fun updateCantidad(cantidad: Int) {
        _uiState.update { it.copy(cantidad = cantidad) }
        updateParcial()
    }

    fun updateSearch(search: String) {
        _uiState.update { it.copy(search = search) }
    }

    private fun updateParcial() {
        _uiState.update {
            val prod = it.productoSeleccionado
            val parcial = if (prod != null) it.cantidad * prod.precioVenta else 0.0
            it.copy(parcialVenta = parcial)
        }
    }

    fun guardarVenta() {
        val estado = _uiState.value
        val producto = estado.productoSeleccionado
        val cantidad = estado.cantidad

        if (producto == null || cantidad > producto.disponibles) {
            _uiState.update { it.copy(mensaje = "Error en los datos o existencias insuficientes") }
            return
        }

        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            if (cuentaActiva == null) {
                _uiState.update { it.copy(isBusy = false, mensaje = "No hay ninguna cuenta abierta actualmente") }
                return@launch
            }

            val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val resultado = registrarVentaUseCase(
                RegistrarVentaParams(
                    cuentaId = cuentaActiva.id.valor,
                    productoId = producto.idProducto,
                    cantidad = cantidad,
                    hora = horaActual,
                    precioPersonalizado = producto.precioVenta
                )
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = "Venta registrada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            mensaje = "Error al registrar venta: ${error.message ?: "Operación fallida"}"
                        )
                    }
                }
            )
        }
    }

    fun cargarVenta(idVenta: String?) {
        if (idVenta.isNullOrBlank()) return

        val idVentaInt = idVenta.toIntOrNull() ?: return
        _uiState.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
            val ventaDominio = cuentaActiva?.ventas?.find { it.id.valor == idVentaInt }

            if (ventaDominio != null) {
                val productoDominio = productoRepository.obtenerPorId(ventaDominio.productoId)
                if (productoDominio != null) {
                    val productoUi = toUiEntity(productoDominio)
                    _uiState.update {
                        it.copy(
                            productoSeleccionado = productoUi,
                            idVenta = ventaDominio.id.valor,
                            idCuenta = cuentaActiva.id.valor,
                            cantidad = ventaDominio.detalle.cantidad.valor,
                            parcialVenta = ventaDominio.subtotal.monto,
                            isUpdate = true,
                            isBusy = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isBusy = false, mensaje = "Producto de la venta no encontrado") }
                }
            } else {
                _uiState.update { it.copy(isBusy = false, mensaje = "Venta no encontrada") }
            }
        }
    }

    private fun toUiEntity(domain: com.goodgus.localapplication.inventario.domain.model.Producto): Producto {
        return Producto(
            idProducto = domain.id.valor,
            nombre = domain.nombre,
            marca = domain.marca,
            precioVenta = domain.precioVenta.monto,
            disponibles = domain.inventario.disponibles,
            tipo = domain.tipo,
            estado = if (domain.estaActivo) 1 else 2
        )
    }
}