package com.goodgus.localapplication.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goodgus.localapplication.core.data.dao.AppDataBase
import com.goodgus.localapplication.pedidos.data.repository.PedidoRepository
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
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
class PedidoRepositoryIntegrationTest {

    private lateinit var db: AppDataBase
    private lateinit var repository: PedidoRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PedidoRepository(db.pedidosDAO(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun guardar_y_obtenerPorId_con_Room_real_mapea_bidireccionalmente_Agregado_Pedido_y_Entity_SQLite() = runTest {
        val pedido = Pedido(
            id = PedidoId(1),
            informacion = InformacionPedido(
                descripcion = "Pastel Moka",
                detalles = "Con decorado de café"
            ),
            plazo = PlazoEntrega(
                fechaPedido = "2026-09-03",
                fechaEntrega = null
            ),
            estado = EstadoPedido.PENDIENTE
        )

        val saveResult = repository.guardar(pedido)
        assertTrue(saveResult.isSuccess)

        val retrieved = repository.obtenerPorId(PedidoId(1))
        assertNotNull(retrieved)
        assertEquals("Pastel Moka", retrieved?.informacion?.descripcion)
        assertEquals("Con decorado de café", retrieved?.informacion?.detalles)
        assertEquals(null, retrieved?.plazo?.fechaEntrega)
        assertEquals(EstadoPedido.PENDIENTE, retrieved?.estado)
    }
}
