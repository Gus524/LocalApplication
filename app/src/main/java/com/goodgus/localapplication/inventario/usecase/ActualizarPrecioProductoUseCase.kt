package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class ActualizarPrecioParams(
    val id: Int,
    val nuevoPrecio: Double
)

class ActualizarPrecioProductoUseCase(
    private val productoRepository: IProductoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<ActualizarPrecioParams, Producto>(dispatcher) {

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
