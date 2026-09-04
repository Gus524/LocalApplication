package com.goodgus.localapplication.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goodgus.localapplication.compras.data.repository.CompraRepository
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.DetalleCompra
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.domain.model.ProductoComprado
import com.goodgus.localapplication.compras.domain.model.ProductoCompradoId
import com.goodgus.localapplication.core.data.dao.AppDataBase
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.data.repository.Producto as ProductoEntity
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CompraRepositoryIntegrationTest {

    private lateinit var db: AppDataBase
    private lateinit var repository: CompraRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()

        db.productoDao().insert(
            ProductoEntity(
                idProducto = 1,
                nombre = "Aceite 1L",
                marca = "Nutrioli",
                precioVenta = 42.0,
                disponibles = 10,
                tipo = "Abarrotes"
            )
        )
        repository = CompraRepository(db.compraDAO(), db.productoDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun guardar_y_obtenerPorId_con_Room_real_persiste_e_hidrata_Agregado_Compra_con_sus_items() = runTest {
        val compra = Compra(
            id = CompraId(1),
            informacion = InformacionCompra(
                fechaCompra = "2026-09-03",
                estado = EstadoCompra.REGISTRADA,
                total = Dinero(300.0)
            ),
            productos = listOf(
                ProductoComprado(
                    id = ProductoCompradoId(1),
                    productoId = ProductoId(1),
                    nombreProducto = "Aceite 1L",
                    detalle = DetalleCompra(
                        cantidad = Cantidad(10),
                        costoUnitario = Dinero(30.0)
                    )
                )
            )
        )

        val saveResult = repository.guardar(compra)
        assertTrue(saveResult.isSuccess)

        val retrieved = repository.obtenerPorId(CompraId(1))
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.id?.valor)
        assertEquals(1, retrieved?.productos?.size)
        assertEquals("Aceite 1L", retrieved?.productos?.first()?.nombreProducto)
        assertEquals(300.0, retrieved?.informacion?.total?.monto ?: 0.0, 0.01)
    }
}
