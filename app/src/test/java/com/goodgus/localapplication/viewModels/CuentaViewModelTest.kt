package com.goodgus.localapplication.viewModels

import android.app.Application
import com.goodgus.localapplication.ventas.data.repository.CuentaDAO
import com.goodgus.localapplication.ventas.data.repository.Cuenta as CuentaEntity
import com.goodgus.localapplication.ventas.data.repository.Venta
import com.goodgus.localapplication.ventas.data.repository.GetCuenta
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import com.goodgus.localapplication.ventas.ui.viewModels.CuentaViewModel
import com.goodgus.localapplication.ventas.usecase.AbrirCuentaUseCase
import com.goodgus.localapplication.ventas.usecase.CancelarVentaUseCase
import com.goodgus.localapplication.ventas.usecase.CerrarCuentaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    private class TestApp : Application()

    private class FakeCuentaDAO : CuentaDAO {
        override fun insert(entity: CuentaEntity): Long = 1L
        override fun update(entity: CuentaEntity): Int = 1
        override fun delete(entity: CuentaEntity): Int = 1
        override fun getById(id: Int): CuentaEntity? = null
        override fun getAll(): List<CuentaEntity> = emptyList()
        override fun getMaxId(): Int? = null

        override fun getCuenta(): Flow<List<CuentaEntity>> = flowOf(emptyList())
        override fun getCuentaActiva(): Flow<List<GetCuenta>> = flowOf(emptyList())
        override fun getCuentaActivaNoVentas(): List<CuentaEntity> = emptyList()
        override fun getVentaId(idVenta: Int): GetCuenta? = null
        override fun closeAccount(): Int = 1
        override fun getAccountId(): Int = 1
        override fun getVentasByCuentaId(idCuenta: Int): List<Venta> = emptyList()
        override fun insertVentas(ventas: List<Venta>): List<Long> = emptyList()
        override fun deleteVentasByCuentaId(idCuenta: Int): Int = 1
    }

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
    private lateinit var cuentaDAO: CuentaDAO
    private lateinit var abrirCuentaUseCase: AbrirCuentaUseCase
    private lateinit var cerrarCuentaUseCase: CerrarCuentaUseCase
    private lateinit var cancelarVentaUseCase: CancelarVentaUseCase
    private lateinit var viewModel: CuentaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        cuentaDAO = FakeCuentaDAO()

        fakeCuentaRepository = FakeCuentaRepository()
        abrirCuentaUseCase = AbrirCuentaUseCase(fakeCuentaRepository)
        cerrarCuentaUseCase = CerrarCuentaUseCase(fakeCuentaRepository)
        cancelarVentaUseCase = CancelarVentaUseCase(fakeCuentaRepository)

        viewModel = CuentaViewModel(
            abrirCuentaUseCase,
            cerrarCuentaUseCase,
            cancelarVentaUseCase,
            fakeCuentaRepository,
            cuentaDAO
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openCuenta abre una nueva cuenta usando AbrirCuentaUseCase`() = testScope.runTest {
        viewModel.openCuenta()
        advanceUntilIdle()

        val cuenta = fakeCuentaRepository.obtenerCuentaActiva()
        assertNotNull(cuenta)
        assertEquals(EstadoCuenta.ABIERTA, cuenta?.informacion?.estado)
    }

    @Test
    fun `closeCuenta cierra la cuenta activa usando CerrarCuentaUseCase`() = testScope.runTest {
        fakeCuentaRepository.cuentas[1] = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta("2026-09-03", EstadoCuenta.ABIERTA)
        )

        viewModel.closeCuenta()
        advanceUntilIdle()

        val cuentaActiva = fakeCuentaRepository.obtenerCuentaActiva()
        assertNull(cuentaActiva)
        assertEquals(EstadoCuenta.CERRADA, fakeCuentaRepository.cuentas[1]?.informacion?.estado)
    }
}
