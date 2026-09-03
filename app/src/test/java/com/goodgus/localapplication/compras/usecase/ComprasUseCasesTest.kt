package com.goodgus.localapplication.compras.usecase

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.ProductoInactivoException
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ComprasUseCasesTest {

    private class FakeCompraRepository : ICompraRepository {
        val compras = mutableMapOf<CompraId, Compra>()
        private var idCounter = 1

        override suspend fun siguienteId(): CompraId = CompraId(idCounter++)

        override suspend fun obtenerPorId(id: CompraId): Compra? = compras[id]

        override suspend fun guardar(agregado: Compra): Result<Unit> {
            compras[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun actualizar(agregado: Compra): Result<Unit> {
            compras[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun eliminar(id: CompraId): Result<Unit> {
            compras.remove(id)
            return Result.success(Unit)
        }

        override suspend fun obtenerTodos(): List<Compra> = compras.values.toList()
    }

    private class FakeProductoRepository : IProductoRepository {
        val productos = mutableMapOf<ProductoId, Producto>()
        private var idCounter = 1

        override suspend fun siguienteId(): ProductoId = ProductoId(idCounter++)

        override suspend fun obtenerPorId(id: ProductoId): Producto? = productos[id]

        override suspend fun obtenerTodos(): List<Producto> = productos.values.toList()

        override suspend fun buscarPorCriterio(criterio: String): List<Producto> = productos.values.toList()

        override suspend fun guardar(agregado: Producto): Result<Unit> {
            productos[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun actualizar(agregado: Producto): Result<Unit> {
            productos[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun eliminar(id: ProductoId): Result<Unit> {
            productos.remove(id)
            return Result.success(Unit)
        }
    }

    private lateinit var compraRepository: FakeCompraRepository
    private lateinit var productoRepository: FakeProductoRepository
    private val testDispatcher = Dispatchers.Unconfined

    private lateinit var registrarCompraUseCase: RegistrarCompraUseCase
    private lateinit var cancelarCompraUseCase: CancelarCompraUseCase
    private lateinit var consultarCompraUseCase: ConsultarCompraUseCase

    @Before
    fun setup() {
        compraRepository = FakeCompraRepository()
        productoRepository = FakeProductoRepository()

        registrarCompraUseCase = RegistrarCompraUseCase(compraRepository, productoRepository)
        cancelarCompraUseCase = CancelarCompraUseCase(compraRepository)
        consultarCompraUseCase = ConsultarCompraUseCase(compraRepository)
    }

    @Test
    fun `RegistrarCompraUseCase registra compra y reabastece stock de productos`() = runBlocking {
        val p1 = Producto(
            id = ProductoId(1),
            informacion = InformacionProducto("Harina", "Tres Estrellas", "Insumos"),
            precioVenta = Dinero(30.0),
            inventario = Inventario(5)
        )
        val p2 = Producto(
            id = ProductoId(2),
            informacion = InformacionProducto("Azúcar", "Zulka", "Insumos"),
            precioVenta = Dinero(28.0),
            inventario = Inventario(10)
        )
        productoRepository.guardar(p1)
        productoRepository.guardar(p2)

        val params = RegistrarCompraParams(
            fecha = "2026-09-02",
            listaCompra = listOf(
                ProductoCompraParams(productoId = 1, cantidad = 10, costoUnitario = 20.0),
                ProductoCompraParams(productoId = 2, cantidad = 5, costoUnitario = 18.0)
            )
        )

        val resultado = registrarCompraUseCase(params)

        assertTrue(resultado.isSuccess)
        val compra = resultado.getOrThrow()
        assertEquals(1, compra.id.valor)
        assertEquals(2, compra.productos.size)
        // Total: (10 * 20.0) + (5 * 18.0) = 200 + 90 = 290.0
        assertEquals(290.0, compra.informacion.total.monto, 0.001)

        // Verificar stock reabastecido
        assertEquals(15, productoRepository.obtenerPorId(ProductoId(1))!!.inventario.disponibles)
        assertEquals(15, productoRepository.obtenerPorId(ProductoId(2))!!.inventario.disponibles)
    }

    @Test
    fun `RegistrarCompraUseCase rechaza lista de compra vacia`() = runBlocking {
        val params = RegistrarCompraParams(
            fecha = "2026-09-02",
            listaCompra = emptyList()
        )

        val resultado = registrarCompraUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `RegistrarCompraUseCase falla si producto no existe`() = runBlocking {
        val params = RegistrarCompraParams(
            fecha = "2026-09-02",
            listaCompra = listOf(
                ProductoCompraParams(productoId = 999, cantidad = 5, costoUnitario = 10.0)
            )
        )

        val resultado = registrarCompraUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `RegistrarCompraUseCase falla si producto esta inactivo`() = runBlocking {
        val pInactivo = Producto(
            id = ProductoId(3),
            informacion = InformacionProducto("Levadura", "Tradi-pan", "Insumos"),
            precioVenta = Dinero(15.0),
            inventario = Inventario(2),
            estado = EstadoProducto.INACTIVO
        )
        productoRepository.guardar(pInactivo)

        val params = RegistrarCompraParams(
            fecha = "2026-09-02",
            listaCompra = listOf(
                ProductoCompraParams(productoId = 3, cantidad = 5, costoUnitario = 8.0)
            )
        )

        val resultado = registrarCompraUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is ProductoInactivoException)
    }

    @Test
    fun `CancelarCompraUseCase transiciona estado a CANCELADA`() = runBlocking {
        val p = Producto(
            id = ProductoId(4),
            informacion = InformacionProducto("Mantequilla", "Gloria", "Insumos"),
            precioVenta = Dinero(40.0),
            inventario = Inventario(5)
        )
        productoRepository.guardar(p)

        val compra = registrarCompraUseCase(
            RegistrarCompraParams(
                fecha = "2026-09-02",
                listaCompra = listOf(ProductoCompraParams(productoId = 4, cantidad = 2, costoUnitario = 25.0))
            )
        ).getOrThrow()

        val resultadoCancelacion = cancelarCompraUseCase(CancelarCompraParams(compraId = compra.id.valor))

        assertTrue(resultadoCancelacion.isSuccess)
        val compraCancelada = resultadoCancelacion.getOrThrow()
        assertEquals(EstadoCompra.CANCELADA, compraCancelada.informacion.estado)
        assertFalse(compraCancelada.estaRegistrada)
    }

    @Test
    fun `CancelarCompraUseCase falla si compra no existe o ya esta cancelada`() = runBlocking {
        val cancelacionInexistente = cancelarCompraUseCase(CancelarCompraParams(compraId = 999))
        assertTrue(cancelacionInexistente.isFailure)
        assertTrue(cancelacionInexistente.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `ConsultarCompraUseCase retorna compra existente`() = runBlocking {
        val p = Producto(
            id = ProductoId(5),
            informacion = InformacionProducto("Sal", "La Fina", "Insumos"),
            precioVenta = Dinero(12.0),
            inventario = Inventario(20)
        )
        productoRepository.guardar(p)

        val compra = registrarCompraUseCase(
            RegistrarCompraParams(
                fecha = "2026-09-02",
                listaCompra = listOf(ProductoCompraParams(productoId = 5, cantidad = 3, costoUnitario = 8.0))
            )
        ).getOrThrow()

        val resultado = consultarCompraUseCase(ConsultarCompraParams(id = compra.id.valor))

        assertTrue(resultado.isSuccess)
        assertEquals(compra.id.valor, resultado.getOrThrow().id.valor)
    }
}
