package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class CambiarEstadoProductoParams(
    val id: Int,
    val activar: Boolean
)

class CambiarEstadoProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : BaseUseCase<CambiarEstadoProductoParams, Producto>() {

    override suspend fun ejecutar(params: CambiarEstadoProductoParams): Result<Producto> {
        val productoId = ProductoId(params.id)
        val producto = productoRepository.obtenerPorId(productoId)
            ?: return Result.failure(NoSuchElementException("Producto con ID ${params.id} no encontrado"))

        val productoActualizado = if (params.activar) producto.activar() else producto.desactivar()
        val actualizarResult = productoRepository.actualizar(productoActualizado)
        return actualizarResult.map { productoActualizado }
    }
}
