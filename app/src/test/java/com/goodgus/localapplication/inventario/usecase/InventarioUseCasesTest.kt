package com.goodgus.localapplication.inventario.usecase

import com.goodgus.localapplication.common.domain.Dinero
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

class InventarioUseCasesTest {

    private class FakeProductoRepository : IProductoRepository {
        val productos = mutableMapOf<ProductoId, Producto>()
        private var idCounter = 1

        override suspend fun siguienteId(): ProductoId = ProductoId(idCounter++)

        override suspend fun obtenerPorId(id: ProductoId): Producto? = productos[id]

        override suspend fun obtenerTodos(): List<Producto> = productos.values.toList()

        override suspend fun buscarPorCriterio(criterio: String): List<Producto> {
            return productos.values.filter {
                it.nombre.contains(criterio, ignoreCase = true) ||
                it.marca.contains(criterio, ignoreCase = true)
            }
        }

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

    private lateinit var repository: FakeProductoRepository
    private val testDispatcher = Dispatchers.Unconfined

    private lateinit var registrarProductoUseCase: RegistrarProductoUseCase
    private lateinit var actualizarPrecioUseCase: ActualizarPrecioProductoUseCase
    private lateinit var cambiarEstadoUseCase: CambiarEstadoProductoUseCase
    private lateinit var consultarInventarioUseCase: ConsultarInventarioUseCase

    @Before
    fun setup() {
        repository = FakeProductoRepository()
        registrarProductoUseCase = RegistrarProductoUseCase(repository)
        actualizarPrecioUseCase = ActualizarPrecioProductoUseCase(repository)
        cambiarEstadoUseCase = CambiarEstadoProductoUseCase(repository)
        consultarInventarioUseCase = ConsultarInventarioUseCase(repository)
    }

    @Test
    fun `RegistrarProductoUseCase registra producto exitosamente con id autogenerado`() = runBlocking {
        val params = RegistrarProductoParams(
            nombre = "Café soluble",
            marca = "Nescafé",
            tipo = "Bebidas",
            precioVenta = 45.50,
            stockInicial = 20
        )

        val resultado = registrarProductoUseCase(params)

        assertTrue(resultado.isSuccess)
        val producto = resultado.getOrThrow()
        assertEquals(1, producto.id.valor)
        assertEquals("Café soluble", producto.nombre)
        assertEquals(45.50, producto.precioVenta.monto, 0.001)
        assertEquals(20, producto.inventario.disponibles)
        assertEquals(EstadoProducto.ACTIVO, producto.estado)
    }

    @Test
    fun `RegistrarProductoUseCase falla con datos invalidos como precio negativo`() = runBlocking {
        val params = RegistrarProductoParams(
            nombre = "Galletas",
            marca = "Gamesa",
            tipo = "Snacks",
            precioVenta = -15.0,
            stockInicial = 5
        )

        val resultado = registrarProductoUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `ActualizarPrecioProductoUseCase actualiza precio de producto existente`() = runBlocking {
        val productoInicial = Producto(
            id = ProductoId(10),
            informacion = InformacionProducto("Refresco", "Coca-Cola", "Bebidas"),
            precioVenta = Dinero(20.0),
            inventario = Inventario(15)
        )
        repository.guardar(productoInicial)

        val params = ActualizarPrecioParams(id = 10, nuevoPrecio = 22.50)
        val resultado = actualizarPrecioUseCase(params)

        assertTrue(resultado.isSuccess)
        val productoActualizado = resultado.getOrThrow()
        assertEquals(22.50, productoActualizado.precioVenta.monto, 0.001)
        assertEquals(22.50, repository.productos[ProductoId(10)]?.precioVenta?.monto ?: 0.0, 0.001)
    }

    @Test
    fun `ActualizarPrecioProductoUseCase falla limpiamente cuando producto no existe`() = runBlocking {
        val params = ActualizarPrecioParams(id = 999, nuevoPrecio = 25.0)
        val resultado = actualizarPrecioUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `CambiarEstadoProductoUseCase desactiva y activa producto`() = runBlocking {
        val productoInicial = Producto(
            id = ProductoId(20),
            informacion = InformacionProducto("Jugo", "Jumex", "Bebidas"),
            precioVenta = Dinero(18.0),
            inventario = Inventario(10),
            estado = EstadoProducto.ACTIVO
        )
        repository.guardar(productoInicial)

        // Desactivar
        val resultadoDesactivar = cambiarEstadoUseCase(CambiarEstadoProductoParams(id = 20, activar = false))
        assertTrue(resultadoDesactivar.isSuccess)
        assertFalse(resultadoDesactivar.getOrThrow().estaActivo)

        // Reactivar
        val resultadoActivar = cambiarEstadoUseCase(CambiarEstadoProductoParams(id = 20, activar = true))
        assertTrue(resultadoActivar.isSuccess)
        assertTrue(resultadoActivar.getOrThrow().estaActivo)
    }

    @Test
    fun `ConsultarInventarioUseCase retorna producto existente`() = runBlocking {
        val producto = Producto(
            id = ProductoId(30),
            informacion = InformacionProducto("Pan", "Bimbo", "Panadería"),
            precioVenta = Dinero(35.0),
            inventario = Inventario(8)
        )
        repository.guardar(producto)

        val resultado = consultarInventarioUseCase(ConsultarInventarioParams(id = 30))

        assertTrue(resultado.isSuccess)
        assertEquals("Pan", resultado.getOrThrow().nombre)
    }

    @Test
    fun `ConsultarInventarioUseCase falla limpiamente si producto no existe`() = runBlocking {
        val resultado = consultarInventarioUseCase(ConsultarInventarioParams(id = 999))

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NoSuchElementException)
    }
}
