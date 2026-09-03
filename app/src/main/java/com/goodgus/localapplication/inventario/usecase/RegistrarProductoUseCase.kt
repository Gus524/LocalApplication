package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class RegistrarProductoParams(
    val nombre: String,
    val marca: String,
    val tipo: String,
    val precioVenta: Double,
    val stockInicial: Int
)

class RegistrarProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : BaseUseCase<RegistrarProductoParams, Producto>() {

    override suspend fun ejecutar(params: RegistrarProductoParams): Result<Producto> {
        val nuevoId = productoRepository.siguienteId()

        val producto = Producto(
            id = nuevoId,
            informacion = InformacionProducto(
                nombre = params.nombre,
                marca = params.marca,
                tipo = params.tipo
            ),
            precioVenta = Dinero(params.precioVenta),
            inventario = Inventario(params.stockInicial),
            estado = EstadoProducto.ACTIVO
        )

        val guardarResult = productoRepository.guardar(producto)
        return guardarResult.map { producto }
    }
}
