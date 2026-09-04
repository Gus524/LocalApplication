package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.DetalleVenta
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.model.Venta
import com.goodgus.localapplication.ventas.domain.model.VentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.ui.viewModels.DetalleCuentaAction
import com.goodgus.localapplication.ventas.ui.viewModels.DetalleCuentaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DetalleCuentaViewModelTest {

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
        override suspend fun obtenerCuentaActiva(): Cuenta? = cuentas.values.find { it.estaAbierta }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakeCuentaRepository
    private lateinit var viewModel: DetalleCuentaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCuentaRepository()

        fakeRepository.cuentas[5] = Cuenta(
            id = CuentaId(5),
            informacion = InformacionCuenta("2026-08-30", EstadoCuenta.CERRADA),
            ventas = listOf(
                Venta(
                    id = VentaId(1),
                    productoId = ProductoId(10),
                    nombreProducto = "Pastel Moka",
                    detalle = DetalleVenta(Cantidad(1), "12:30", Dinero(180.0)),
                    estado = EstadoVenta.ACTIVA
                )
            )
        )

        viewModel = DetalleCuentaViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnCargarDetalle carga cuenta y desglose de ventas con exito`() = testScope.runTest {
        viewModel.onAction(DetalleCuentaAction.OnCargarDetalle(5))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.cuenta)
        assertEquals(5, state.cuenta?.id?.valor)
        assertEquals(1, state.cuenta?.ventas?.size)
        assertEquals("Pastel Moka", state.cuenta?.ventas?.first()?.nombreProducto)
        assertEquals(180.0, state.cuenta?.totalCalculado?.monto ?: 0.0, 0.01)
    }

    @Test
    fun `OnCargarDetalle muestra mensaje si la cuenta no existe`() = testScope.runTest {
        viewModel.onAction(DetalleCuentaAction.OnCargarDetalle(999))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Cuenta no encontrada", state.mensajeAlerta)
    }
}
