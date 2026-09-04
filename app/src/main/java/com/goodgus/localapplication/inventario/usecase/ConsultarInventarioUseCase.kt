package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.core.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository

import javax.inject.Inject

data class ConsultarInventarioParams(
    val id: Int
)

class ConsultarInventarioUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : BaseUseCase<ConsultarInventarioParams, Producto>() {

    override suspend fun ejecutar(params: ConsultarInventarioParams): Result<Producto> {
        val productoId = ProductoId(params.id)
        val producto = productoRepository.obtenerPorId(productoId)
            ?: return Result.failure(NoSuchElementException("Producto con ID ${params.id} no encontrado"))
        return Result.success(producto)
    }
}
