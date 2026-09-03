package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.common.domain.Cantidad
import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.VentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class RegistrarVentaParams(
    val cuentaId: Int,
    val productoId: Int,
    val cantidad: Int,
    val hora: String,
    val precioPersonalizado: Double? = null
)

class RegistrarVentaUseCase @Inject constructor(
    private val cuentaRepository: ICuentaRepository,
    private val productoRepository: IProductoRepository
) : BaseUseCase<RegistrarVentaParams, Cuenta>() {

    override suspend fun ejecutar(params: RegistrarVentaParams): Result<Cuenta> {
        val cuentaId = CuentaId(params.cuentaId)
        val cuenta = cuentaRepository.obtenerPorId(cuentaId)
            ?: return Result.failure(NoSuchElementException("Cuenta con ID ${params.cuentaId} no encontrada"))

        val productoId = ProductoId(params.productoId)
        val producto = productoRepository.obtenerPorId(productoId)
            ?: return Result.failure(NoSuchElementException("Producto con ID ${params.productoId} no encontrado"))

        val cantidadVo = Cantidad(params.cantidad)
        val precio = if (params.precioPersonalizado != null) {
            Dinero(params.precioPersonalizado)
        } else {
            producto.precioVenta
        }

        val nuevoVentaId = VentaId((cuenta.ventas.maxOfOrNull { it.id.valor } ?: 0) + 1)

        val resultadoAgregado = cuenta.agregarVenta(
            ventaId = nuevoVentaId,
            producto = producto,
            cantidad = cantidadVo,
            hora = params.hora,
            precioUnitario = precio
        )

        if (resultadoAgregado.isFailure) {
            return Result.failure(resultadoAgregado.exceptionOrNull()!!)
        }

        val (cuentaActualizada, productoActualizado) = resultadoAgregado.getOrThrow()

        val actualizarProductoResult = productoRepository.actualizar(productoActualizado)
        if (actualizarProductoResult.isFailure) {
            return Result.failure(actualizarProductoResult.exceptionOrNull()!!)
        }

        val actualizarCuentaResult = cuentaRepository.actualizar(cuentaActualizada)
        return actualizarCuentaResult.map { cuentaActualizada }
    }
}
