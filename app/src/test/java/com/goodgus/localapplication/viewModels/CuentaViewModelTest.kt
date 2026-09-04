package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaAction
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaViewModel
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaUseCase
import com.goodgus.localapplication.ventas.usecase.CancelarVentaUseCase
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaUseCase
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CuentaViewModelTest {

    private class FakeCuentaRepository : ICuentaRepository {
        val cuentas = mutableMapOf<Int, Cuenta>()
        private var idCounter = 1

        override suspend fun siguienteId(): CuentaId = CuentaId(idCounter++)
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

    private lateinit var fakeCuentaRepository: FakeCuentaRepository
    private lateinit var abrirCuentaUseCase: AbrirCuentaUseCase
    private lateinit var cerrarCuentaUseCase: CerrarCuentaUseCase
    private lateinit var cancelarVentaUseCase: CancelarVentaUseCase
    private lateinit var viewModel: CuentaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCuentaRepository = FakeCuentaRepository()
        abrirCuentaUseCase = AbrirCuentaUseCase(fakeCuentaRepository)
        cerrarCuentaUseCase = CerrarCuentaUseCase(fakeCuentaRepository)
        cancelarVentaUseCase = CancelarVentaUseCase(fakeCuentaRepository)

        viewModel = CuentaViewModel(
            abrirCuentaUseCase,
            cerrarCuentaUseCase,
            cancelarVentaUseCase,
            fakeCuentaRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnAbrirCuenta abre una nueva cuenta usando AbrirCuentaUseCase`() = testScope.runTest {
        viewModel.onAction(CuentaAction.OnAbrirCuenta)
        advanceUntilIdle()

        val cuenta = fakeCuentaRepository.obtenerCuentaActiva()
        assertNotNull(cuenta)
        assertEquals(EstadoCuenta.ABIERTA, cuenta?.informacion?.estado)
        assertEquals(cuenta, viewModel.uiState.value.cuentaActiva)
    }

    @Test
    fun `OnConfirmarCerrarCuenta cierra la cuenta activa`() = testScope.runTest {
        fakeCuentaRepository.cuentas[1] = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta("2026-09-03", EstadoCuenta.ABIERTA)
        )
        viewModel.onAction(CuentaAction.OnCargarCuenta)
        advanceUntilIdle()

        viewModel.onAction(CuentaAction.OnConfirmarCerrarCuenta)
        advanceUntilIdle()

        val cuentaActiva = fakeCuentaRepository.obtenerCuentaActiva()
        assertNull(cuentaActiva)
        assertNull(viewModel.uiState.value.cuentaActiva)
        assertEquals(EstadoCuenta.CERRADA, fakeCuentaRepository.cuentas[1]?.informacion?.estado)
    }
}
