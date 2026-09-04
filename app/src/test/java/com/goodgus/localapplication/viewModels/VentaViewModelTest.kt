package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.ui.viewModels.VentaFormAction
import com.goodgus.localapplication.ventas.ui.viewModels.VentaFormEffect
import com.goodgus.localapplication.ventas.ui.viewModels.VentaViewModel
import com.goodgus.localapplication.ventas.usecase.RegistrarVentaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VentaViewModelTest {

    private class FakeProductoRepository : IProductoRepository {
        val productos = mutableMapOf<Int, Producto>()
        override suspend fun siguienteId(): ProductoId = ProductoId(productos.size + 1)
        override suspend fun obtenerPorId(id: ProductoId): Producto? = productos[id.valor]
        override suspend fun guardar(agregado: Producto): Result<Unit> {
            productos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun actualizar(agregado: Producto): Result<Unit> {
            productos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun eliminar(id: ProductoId): Result<Unit> {
            productos.remove(id.valor)
            return Result.success(Unit)
        }
        override suspend fun obtenerTodos(): List<Producto> = productos.values.toList()
        override suspend fun buscarPorCriterio(criterio: String): List<Producto> =
            productos.values.filter { it.nombre.contains(criterio, ignoreCase = true) }
    }

    private class FakeCuentaRepository : ICuentaRepository {
        val cuentas = mutableMapOf<Int, Cuenta>()
        override suspend fun siguienteId(): CuentaId = CuentaId(cuentas.size + 1)
        override suspend fun obtenerPorId(id: CuentaId): Cuenta? = cuentas[id.valor]
        override suspend fun guardar(agregado: Cuenta): Result<Unit> {
            cuentas[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun actualizar(agregado: Cuenta): Result<Unit> {
            cuentas[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun eliminar(id: CuentaId): Result<Unit> {
            cuentas.remove(id.valor)
            return Result.success(Unit)
        }
        override suspend fun obtenerTodos(): List<Cuenta> = cuentas.values.toList()
        override suspend fun obtenerCuentaActiva(): Cuenta? =
            cuentas.values.find { it.estaAbierta }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeProductoRepository: FakeProductoRepository
    private lateinit var fakeCuentaRepository: FakeCuentaRepository
    private lateinit var registrarVentaUseCase: RegistrarVentaUseCase
    private lateinit var viewModel: VentaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeProductoRepository = FakeProductoRepository()
        fakeCuentaRepository = FakeCuentaRepository()
        registrarVentaUseCase = RegistrarVentaUseCase(fakeCuentaRepository, fakeProductoRepository)

        fakeProductoRepository.productos[1] = Producto(
            id = ProductoId(1),
            informacion = InformacionProducto("Jugo Naranja", "Jumex", "Bebida"),
            precioVenta = Dinero(12.0),
            inventario = Inventario(10),
            estado = EstadoProducto.ACTIVO
        )

        fakeCuentaRepository.cuentas[1] = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta("2026-09-03", EstadoCuenta.ABIERTA)
        )

        viewModel = VentaViewModel(registrarVentaUseCase, fakeProductoRepository, fakeCuentaRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnBuscarProducto busca en el catalogo a traves de IProductoRepository`() = testScope.runTest {
        viewModel.onAction(VentaFormAction.OnBuscarProducto("Jugo"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.resultadosBusqueda.size)
        assertEquals("Jugo Naranja", state.resultadosBusqueda[0].nombre)
    }

    @Test
    fun `OnGuardarVenta descuenta stock y registra linea en cuenta activa emitiendo efecto`() = testScope.runTest {
        val prod = fakeProductoRepository.productos[1]!!
        viewModel.onAction(VentaFormAction.OnSeleccionarProducto(prod))
        viewModel.onAction(VentaFormAction.OnCantidadChange(2))

        viewModel.onAction(VentaFormAction.OnGuardarVenta)
        advanceUntilIdle()

        val cuenta = fakeCuentaRepository.cuentas[1]
        assertNotNull(cuenta)
        assertEquals(1, cuenta?.ventas?.size)
        assertEquals(24.0, cuenta?.totalCalculado?.monto ?: 0.0, 0.01)

        val producto = fakeProductoRepository.productos[1]
        assertEquals(8, producto?.inventario?.disponibles)
        val effectReceived = viewModel.effect.first()
        assertEquals(VentaFormEffect.NavegarAtras, effectReceived)
    }
}
