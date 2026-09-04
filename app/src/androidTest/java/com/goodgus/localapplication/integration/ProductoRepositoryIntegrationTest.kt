package com.goodgus.localapplication.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goodgus.localapplication.core.data.dao.AppDataBase
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.data.repository.ProductoRepository
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
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
class ProductoRepositoryIntegrationTest {

    private lateinit var db: AppDataBase
    private lateinit var repository: ProductoRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProductoRepository(db.productoDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun guardar_y_obtenerPorId_con_Room_real_mapea_bidireccionalmente_entre_Dominio_y_SQLite() = runTest {
        val domain = Producto(
            id = ProductoId(1),
            informacion = InformacionProducto("Manzana Roja", "Frutas S.A.", "Fruta"),
            precioVenta = Dinero(25.0),
            inventario = Inventario(50),
            estado = EstadoProducto.ACTIVO
        )

        val result = repository.guardar(domain)
        assertTrue(result.isSuccess)

        val retrieved = repository.obtenerPorId(ProductoId(1))
        assertNotNull(retrieved)
        assertEquals("Manzana Roja", retrieved?.nombre)
        assertEquals("Frutas S.A.", retrieved?.marca)
        assertEquals(25.0, retrieved?.precioVenta?.monto ?: 0.0, 0.01)
        assertEquals(50, retrieved?.inventario?.disponibles)
        assertEquals(EstadoProducto.ACTIVO, retrieved?.estado)
    }

    @Test
    fun buscarPorCriterio_con_Room_real_filtra_por_nombre_marca_o_tipo() = runTest {
        repository.guardar(
            Producto(
                id = ProductoId(1),
                informacion = InformacionProducto("Yogurt Fresa", "Danone", "Lácteos"),
                precioVenta = Dinero(14.0),
                inventario = Inventario(10)
            )
        )
        repository.guardar(
            Producto(
                id = ProductoId(2),
                informacion = InformacionProducto("Yogurt Natural", "Danone", "Lácteos"),
                precioVenta = Dinero(13.0),
                inventario = Inventario(12)
            )
        )

        val results = repository.buscarPorCriterio("Fresa")
        assertEquals(1, results.size)
        assertEquals("Yogurt Fresa", results[0].nombre)
    }

    @Test
    fun observarTodos_emite_automaticamente_nueva_lista_cuando_se_inserta_un_producto() = runTest {
        val initialList = repository.observarTodos()
        var lastEmitted: List<Producto>? = null
        backgroundScope.launch(Dispatchers.Unconfined) {
            initialList.collect { lastEmitted = it }
        }

        assertEquals(0, lastEmitted?.size ?: 0)

        repository.guardar(
            Producto(
                id = ProductoId(1),
                informacion = InformacionProducto("Arroz 1kg", "SOS", "Granos"),
                precioVenta = Dinero(35.0),
                inventario = Inventario(20)
            )
        )

        assertEquals(1, lastEmitted?.size)
        assertEquals("Arroz 1kg", lastEmitted?.first()?.nombre)
    }
}
