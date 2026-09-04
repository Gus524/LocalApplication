package com.goodgus.localapplication.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goodgus.localapplication.core.data.dao.AppDataBase
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.data.repository.ProductoEntity as ProductoEntity
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.data.repository.CuentaRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.DetalleVenta
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.model.Venta
import com.goodgus.localapplication.ventas.domain.model.VentaId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class CuentaRepositoryIntegrationTest {

    private lateinit var db: AppDataBase
    private lateinit var repository: CuentaRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()

        db.productoDao().insert(
            ProductoEntity(
                idProducto = 1,
                nombre = "Galletas Marías",
                marca = "Gamesa",
                precioVenta = 16.0,
                disponibles = 20,
                tipo = "Galletas"
            )
        )
        repository = CuentaRepository(db.cuentaDao(), db.productoDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun guardar_y_obtenerCuentaActiva_con_Room_real_persiste_e_hidrata_Agregado_Cuenta_y_lineas_de_Venta() = runTest {
        val cuenta = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta(
                fecha = "2026-09-03",
                estado = EstadoCuenta.ABIERTA,
                total = Dinero(32.0)
            ),
            ventas = listOf(
                Venta(
                    id = VentaId(1),
                    productoId = ProductoId(1),
                    nombreProducto = "Galletas Marías",
                    detalle = DetalleVenta(
                        cantidad = Cantidad(2),
                        hora = "10:15",
                        precioUnitario = Dinero(16.0)
                    ),
                    estado = EstadoVenta.ACTIVA
                )
            )
        )

        val saveResult = repository.guardar(cuenta)
        assertTrue(saveResult.isSuccess)

        val retrieved = repository.obtenerCuentaActiva()
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.id?.valor)
        assertEquals(1, retrieved?.ventas?.size)
        assertEquals("Galletas Marías", retrieved?.ventas?.first()?.nombreProducto)
        assertEquals(32.0, retrieved?.informacion?.total?.monto ?: 0.0, 0.01)
    }

    @Test
    fun observarCuentaActiva_emite_actualizacion_cuando_se_agregan_nuevas_ventas() = runTest {
        val cuentaInicial = Cuenta(
            id = CuentaId(1),
            informacion = InformacionCuenta(
                fecha = "2026-09-03",
                estado = EstadoCuenta.ABIERTA,
                total = Dinero(0.0)
            ),
            ventas = emptyList()
        )
        repository.guardar(cuentaInicial)

        val flow = repository.observarCuentaActiva()
        var lastCuenta: Cuenta? = null
        backgroundScope.launch(Dispatchers.Unconfined) {
            flow.collect { lastCuenta = it }
        }

        assertEquals(0, lastCuenta?.ventas?.size ?: 0)

        val cuentaConVenta = cuentaInicial.copy(
            ventas = listOf(
                Venta(
                    id = VentaId(1),
                    productoId = ProductoId(1),
                    nombreProducto = "Galletas Marías",
                    detalle = DetalleVenta(
                        cantidad = Cantidad(1),
                        hora = "11:00",
                        precioUnitario = Dinero(16.0)
                    ),
                    estado = EstadoVenta.ACTIVA
                )
            )
        )
        repository.actualizar(cuentaConVenta)

        assertEquals(1, lastCuenta?.ventas?.size)
        assertEquals("Galletas Marías", lastCuenta?.ventas?.first()?.nombreProducto)
    }
}
