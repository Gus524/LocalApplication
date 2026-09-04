package com.goodgus.localapplication.compras.usecase

import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.usecase.BaseUseCase
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.domain.model.ProductoCompradoId
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository

import javax.inject.Inject

data class ProductoCompraParams(
    val productoId: Int,
    val cantidad: Int,
    val costoUnitario: Double
)

data class RegistrarCompraParams(
    val fecha: String,
    val listaCompra: List<ProductoCompraParams>
)

class RegistrarCompraUseCase @Inject constructor(
    private val compraRepository: ICompraRepository,
    private val productoRepository: IProductoRepository
) : BaseUseCase<RegistrarCompraParams, Compra>() {

    override suspend fun ejecutar(params: RegistrarCompraParams): Result<Compra> {
        if (params.listaCompra.isEmpty()) {
            return Result.failure(IllegalArgumentException("La compra debe incluir al menos un producto en la lista"))
        }

        val nuevoCompraId = compraRepository.siguienteId()
        var compraActual = Compra(
            id = nuevoCompraId,
            informacion = InformacionCompra(
                fechaCompra = params.fecha,
                estado = EstadoCompra.REGISTRADA
            )
        )

        val productosModificados = mutableListOf<Producto>()

        for ((index, item) in params.listaCompra.withIndex()) {
            val productoId = ProductoId(item.productoId)
            val producto = productoRepository.obtenerPorId(productoId)
                ?: return Result.failure(NoSuchElementException("Producto con ID ${item.productoId} no encontrado"))

            val cantidadVo = Cantidad(item.cantidad)
            val costoUnitarioVo = Dinero(item.costoUnitario)
            val lineaId = ProductoCompradoId(index + 1)

            val resultadoAgregar = compraActual.agregarProducto(
                lineaId = lineaId,
                producto = producto,
                cantidad = cantidadVo,
                costoUnitario = costoUnitarioVo
            )

            if (resultadoAgregar.isFailure) {
                return Result.failure(resultadoAgregar.exceptionOrNull()!!)
            }

            val (compraActualizada, productoReabastecido) = resultadoAgregar.getOrThrow()
            compraActual = compraActualizada
            productosModificados.add(productoReabastecido)
        }

        for (producto in productosModificados) {
            val actualizarResult = productoRepository.actualizar(producto)
            if (actualizarResult.isFailure) {
                return Result.failure(actualizarResult.exceptionOrNull()!!)
            }
        }

        val guardarCompraResult = compraRepository.guardar(compraActual)
        return guardarCompraResult.map { compraActual }
    }
}
