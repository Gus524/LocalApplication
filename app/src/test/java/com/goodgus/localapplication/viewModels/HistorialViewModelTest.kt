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
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialAction
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialEffect
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

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
        override fun observarCuentasCerradas(): Flow<List<Cuenta>> =
            flowOf(cuentas.values.filter { !it.estaAbierta }.sortedByDescending { it.id.valor })
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakeCuentaRepository
    private lateinit var viewModel: HistorialViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCuentaRepository()

        fakeRepository.cuentas[1] = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta("2026-09-01", EstadoCuenta.CERRADA),
            ventas = listOf(
                Venta(
                    id = VentaId(1),
                    productoId = ProductoId(1),
                    nombreProducto = "Donas",
                    detalle = DetalleVenta(Cantidad(2), "10:00", Dinero(15.0)),
                    estado = EstadoVenta.ACTIVA
                )
            )
        )

        fakeRepository.cuentas[2] = Cuenta(
            id = CuentaId(2),
            informacion = InformacionCuenta("2026-09-02", EstadoCuenta.ABIERTA)
        )

        viewModel = HistorialViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializacion carga solo cuentas cerradas en el historial`() = testScope.runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cuentas.size)
        assertEquals(1, state.cuentas[0].id.valor)
        assertEquals(30.0, state.cuentas[0].totalCalculado.monto, 0.01)
    }

    @Test
    fun `OnSeleccionarCuenta emite efecto NavegarADetalleCuenta`() = testScope.runTest {
        viewModel.onAction(HistorialAction.OnSeleccionarCuenta(1))
        advanceUntilIdle()

        val effect = viewModel.effect.first()
        assertEquals(HistorialEffect.NavegarADetalleCuenta(1), effect)
    }
}
