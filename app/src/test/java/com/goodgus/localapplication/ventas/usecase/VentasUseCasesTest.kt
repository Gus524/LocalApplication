package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.core.domain.CuentaCerradaException
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.StockInsuficienteException
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VentasUseCasesTest {

    private class FakeCuentaRepository : ICuentaRepository {
        val cuentas = mutableMapOf<CuentaId, Cuenta>()
        private var idCounter = 1

        override suspend fun siguienteId(): CuentaId = CuentaId(idCounter++)

        override suspend fun obtenerPorId(id: CuentaId): Cuenta? = cuentas[id]

        override suspend fun obtenerCuentaActiva(): Cuenta? = cuentas.values.firstOrNull { it.estaAbierta }

        override suspend fun guardar(agregado: Cuenta): Result<Unit> {
            cuentas[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun actualizar(agregado: Cuenta): Result<Unit> {
            cuentas[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun eliminar(id: CuentaId): Result<Unit> {
            cuentas.remove(id)
            return Result.success(Unit)
        }

        override suspend fun obtenerTodos(): List<Cuenta> = cuentas.values.toList()
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

    private lateinit var cuentaRepository: FakeCuentaRepository
    private lateinit var productoRepository: FakeProductoRepository
    private val testDispatcher = Dispatchers.Unconfined

    private lateinit var abrirCuentaUseCase: AbrirCuentaUseCase
    private lateinit var registrarVentaUseCase: RegistrarVentaUseCase
    private lateinit var cancelarVentaUseCase: CancelarVentaUseCase
    private lateinit var cerrarCuentaUseCase: CerrarCuentaUseCase
    private lateinit var consultarCuentaUseCase: ConsultarCuentaUseCase

    @Before
    fun setup() {
        cuentaRepository = FakeCuentaRepository()
        productoRepository = FakeProductoRepository()

        abrirCuentaUseCase = AbrirCuentaUseCase(cuentaRepository)
        registrarVentaUseCase = RegistrarVentaUseCase(cuentaRepository, productoRepository)
        cancelarVentaUseCase = CancelarVentaUseCase(cuentaRepository)
        cerrarCuentaUseCase = CerrarCuentaUseCase(cuentaRepository)
        consultarCuentaUseCase = ConsultarCuentaUseCase(cuentaRepository)
    }

    @Test
    fun `AbrirCuentaUseCase abre nueva cuenta exitosamente`() = runBlocking {
        val params = AbrirCuentaParams(fecha = "2026-09-02")
        val resultado = abrirCuentaUseCase(params)

        assertTrue(resultado.isSuccess)
        val cuenta = resultado.getOrThrow()
        assertEquals(1, cuenta.id.valor)
        assertTrue(cuenta.estaAbierta)
        assertEquals("2026-09-02", cuenta.informacion.fecha)
    }

    @Test
    fun `AbrirCuentaUseCase falla cuando ya existe una cuenta abierta`() = runBlocking {
        abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02"))

        val resultadoDuplicado = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02"))

        assertTrue(resultadoDuplicado.isFailure)
        assertTrue(resultadoDuplicado.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `RegistrarVentaUseCase registra venta y descuenta stock atomico`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val producto = Producto(
            id = ProductoId(10),
            informacion = InformacionProducto("Refresco", "Coca-Cola", "Bebidas"),
            precioVenta = Dinero(20.0),
            inventario = Inventario(10),
            estado = EstadoProducto.ACTIVO
        )
        productoRepository.guardar(producto)

        val params = RegistrarVentaParams(
            cuentaId = cuenta.id.valor,
            productoId = 10,
            cantidad = 3,
            hora = "14:30"
        )

        val resultado = registrarVentaUseCase(params)

        assertTrue(resultado.isSuccess)
        val cuentaActualizada = resultado.getOrThrow()
        assertEquals(1, cuentaActualizada.ventas.size)
        assertEquals(60.0, cuentaActualizada.informacion.total.monto, 0.001)

        // Verificar persistencia del stock actualizado
        val productoEnRepo = productoRepository.obtenerPorId(ProductoId(10))!!
        assertEquals(7, productoEnRepo.inventario.disponibles)
    }

    @Test
    fun `RegistrarVentaUseCase con precio personalizado aplica descuento`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val producto = Producto(
            id = ProductoId(11),
            informacion = InformacionProducto("Agua", "Ciel", "Bebidas"),
            precioVenta = Dinero(15.0),
            inventario = Inventario(10),
            estado = EstadoProducto.ACTIVO
        )
        productoRepository.guardar(producto)

        val params = RegistrarVentaParams(
            cuentaId = cuenta.id.valor,
            productoId = 11,
            cantidad = 2,
            hora = "15:00",
            precioPersonalizado = 12.0
        )

        val resultado = registrarVentaUseCase(params)

        assertTrue(resultado.isSuccess)
        assertEquals(24.0, resultado.getOrThrow().informacion.total.monto, 0.001)
    }

    @Test
    fun `RegistrarVentaUseCase falla limpiamente si producto o cuenta no existen`() = runBlocking {
        val resultadoSinCuenta = registrarVentaUseCase(
            RegistrarVentaParams(cuentaId = 999, productoId = 1, cantidad = 1, hora = "10:00")
        )
        assertTrue(resultadoSinCuenta.isFailure)
        assertTrue(resultadoSinCuenta.exceptionOrNull() is NoSuchElementException)

        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val resultadoSinProducto = registrarVentaUseCase(
            RegistrarVentaParams(cuentaId = cuenta.id.valor, productoId = 999, cantidad = 1, hora = "10:00")
        )
        assertTrue(resultadoSinProducto.isFailure)
        assertTrue(resultadoSinProducto.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `RegistrarVentaUseCase falla si stock es insuficiente`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val producto = Producto(
            id = ProductoId(12),
            informacion = InformacionProducto("Sabritas", "Lays", "Snacks"),
            precioVenta = Dinero(18.0),
            inventario = Inventario(1)
        )
        productoRepository.guardar(producto)

        val params = RegistrarVentaParams(
            cuentaId = cuenta.id.valor,
            productoId = 12,
            cantidad = 5,
            hora = "16:00"
        )

        val resultado = registrarVentaUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is StockInsuficienteException)
    }

    @Test
    fun `CancelarVentaUseCase anula venta y recalcula total`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val producto = Producto(
            id = ProductoId(13),
            informacion = InformacionProducto("Galletas", "Gamesa", "Snacks"),
            precioVenta = Dinero(10.0),
            inventario = Inventario(10)
        )
        productoRepository.guardar(producto)

        registrarVentaUseCase(
            RegistrarVentaParams(cuentaId = cuenta.id.valor, productoId = 13, cantidad = 2, hora = "17:00")
        )

        val resultadoCancelacion = cancelarVentaUseCase(
            CancelarVentaParams(cuentaId = cuenta.id.valor, ventaId = 1)
        )

        assertTrue(resultadoCancelacion.isSuccess)
        val cuentaCancelada = resultadoCancelacion.getOrThrow()
        assertEquals(0.0, cuentaCancelada.informacion.total.monto, 0.001)
        assertEquals(EstadoVenta.CANCELADA, cuentaCancelada.ventas.first().estado)
    }

    @Test
    fun `CerrarCuentaUseCase cierra la cuenta y bloquea nuevas ventas`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()
        val producto = Producto(
            id = ProductoId(14),
            informacion = InformacionProducto("Jugo", "Jumex", "Bebidas"),
            precioVenta = Dinero(15.0),
            inventario = Inventario(10)
        )
        productoRepository.guardar(producto)

        val resultadoCierre = cerrarCuentaUseCase(CerrarCuentaParams(cuentaId = cuenta.id.valor))
        assertTrue(resultadoCierre.isSuccess)
        val cuentaCerrada = resultadoCierre.getOrThrow()
        assertEquals(EstadoCuenta.CERRADA, cuentaCerrada.informacion.estado)
        assertFalse(cuentaCerrada.estaAbierta)

        // Intentar registrar venta en cuenta cerrada
        val resultadoVentaFalla = registrarVentaUseCase(
            RegistrarVentaParams(cuentaId = cuenta.id.valor, productoId = 14, cantidad = 1, hora = "18:00")
        )
        assertTrue(resultadoVentaFalla.isFailure)
        assertTrue(resultadoVentaFalla.exceptionOrNull() is CuentaCerradaException)
    }

    @Test
    fun `ConsultarCuentaUseCase retorna cuenta activa o por ID`() = runBlocking {
        val cuenta = abrirCuentaUseCase(AbrirCuentaParams(fecha = "2026-09-02")).getOrThrow()

        // Consulta activa por defecto
        val consultaActiva = consultarCuentaUseCase(ConsultarCuentaParams())
        assertTrue(consultaActiva.isSuccess)
        assertEquals(cuenta.id.valor, consultaActiva.getOrThrow().id.valor)

        // Consulta por ID
        val consultaPorId = consultarCuentaUseCase(ConsultarCuentaParams(id = cuenta.id.valor))
        assertTrue(consultaPorId.isSuccess)
        assertEquals(cuenta.id.valor, consultaPorId.getOrThrow().id.valor)
    }
}
