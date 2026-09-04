package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository

import javax.inject.Inject

data class ActualizarPrecioParams(
    val id: Int,
    val nuevoPrecio: Double
)

class ActualizarPrecioProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : BaseUseCase<ActualizarPrecioParams, Producto>() {

    override suspend fun ejecutar(params: ActualizarPrecioParams): Result<Producto> {
        val productoId = ProductoId(params.id)
        val nuevoPrecio = Dinero(params.nuevoPrecio)

        val producto = productoRepository.obtenerPorId(productoId)
            ?: return Result.failure(NoSuchElementException("Producto con ID ${params.id} no encontrado"))

        val productoActualizado = producto.actualizarPrecio(nuevoPrecio)
        val actualizarResult = productoRepository.actualizar(productoActualizado)
        return actualizarResult.map { productoActualizado }
    }
}
