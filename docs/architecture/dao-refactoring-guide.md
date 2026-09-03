# Guía y Hoja de Ruta: Refactorización de DAOs y Persistencia en Room

## 1. Contexto y Objetivos

Con la migración del dominio puro (DDD) y la introducción del patrón **Generic Repository con Template Method** ([`BaseRepository`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/core/data/repository/BaseRepository.kt)), la capa de persistencia local (Room) debe ser homogeneizada para:

1. **Eliminar consultas SQL redundantes**: Reemplazar métodos manuales `@Query("INSERT OR REPLACE...")` y `@Query("UPDATE...")` por las operaciones tipadas de [`BaseDao<T>`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/BaseDao.kt).
2. **Alinear los DAOs con los hooks de `BaseRepository`**: Garantizar que cada DAO provea las consultas de hidratación completa (`onHydrateQuery`) y listado (`onGetAllQuery`).
3. **Garantizar corrutinas y seguridad de tipos**: Homogeneizar retornos nulos seguros (`T?` en consultas por ID) y soporte asíncrono con `suspend` / `Flow<T>`.

---

## 2. Diagnóstico de Deuda Técnica en los DAOs Actuales

| DAO Actual | Problema Detectado | Acción de Refactorización |
| :--- | :--- | :--- |
| [`ProductoDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/ProductoDAO.kt) | Métodos `addProduct` y `updateProducto` definidos con `@Query` crudo pasando parámetros sueltos (`nombre`, `marca`, etc.). `getProductId` retorna no-nulo forzado. | Eliminar `addProduct`/`updateProducto` (usar `insert`/`update` de `BaseDao`). Cambiar `getProductId(id: Int): Producto?`. Mantener solo queries específicos de negocio (`searchProduct`, `downProduct`). |
| [`CuentaDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CuentaDAO.kt) | Consultas con múltiples joins o updates manuales; acoplamiento entre cuenta y ventas. | Unificar la consulta de hidratación de Cuenta activa con sus items de venta asociados. |
| [`VentaDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/VentaDAO.kt) | Inconsistencia entre inserts individuales y en lote. | Heredar de `BaseDao<VentaEntity>`, tipar inserts y proveer query por `id_cuenta`. |
| [`CompraDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CompraDAO.kt) y [`CompraProductoDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CompraProductoDAO.kt) | Consultas separadas sin `@Transaction` explícito para la persistencia del agregado completo (`Compra` + items). | Asegurar método transaccional o coordinación en repositorio para guardar cabecera e items. |
| [`PedidosDAO`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/PedidosDAO.kt) | Queries duplicados para actualizar estado de pedidos. | Limpiar queries obsoletos y dejar solo filtros por estado (`Pendiente`, `Entregado`, `Cancelado`). |

---

## 3. Contrato Base de Persistencia: `BaseDao<T>`

El contrato base [`BaseDao<T>`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/BaseDao.kt) debe proveer operaciones CRUD elementales y soporte para listas:

```kotlin
package com.goodgus.localapplication.DAO

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>

    @Update
    suspend fun update(entity: T): Int

    @Delete
    suspend fun delete(entity: T): Int
}
```

---

## 4. Matriz de Refactorización por Bounded Context

### 4.1 Bounded Context Inventario: `ProductoDAO`
* **Entidad:** `com.goodgus.localapplication.models.data.Producto`
* **Repositorio:** `com.goodgus.localapplication.inventario.data.repository.ProductoRepository`
* **Métodos requeridos:**
  ```kotlin
  @Dao
  interface ProductoDAO : BaseDao<Producto> {
      @Query("SELECT * FROM Producto WHERE id_producto = :idProducto")
      suspend fun getProductoById(idProducto: Int): Producto?

      @Query("SELECT * FROM Producto WHERE estado = 1")
      suspend fun getAllActiveProducts(): List<Producto>

      @Query("SELECT * FROM Producto WHERE estado = 1 AND (nombre LIKE '%' || :query || '%' OR marca LIKE '%' || :query || '%')")
      suspend fun searchProducts(query: String): List<Producto>

      @Query("UPDATE Producto SET estado = 0 WHERE id_producto = :idProducto")
      suspend fun deactivateProduct(idProducto: Int): Int
  }
  ```

### 4.2 Bounded Context Ventas: `CuentaDAO` y `VentaDAO`
* **Entidades:** `Cuenta`, `Venta`
* **Repositorio:** `com.goodgus.localapplication.ventas.data.repository.CuentaRepository`
* **Métodos requeridos:**
  * `CuentaDAO`:
    ```kotlin
    @Query("SELECT * FROM Cuenta WHERE id_cuenta = :idCuenta")
    suspend fun getCuentaById(idCuenta: Int): Cuenta?

    @Query("SELECT * FROM Cuenta WHERE estado = 'Abierta' LIMIT 1")
    suspend fun getCuentaActiva(): Cuenta?
    ```
  * `VentaDAO`:
    ```kotlin
    @Query("SELECT * FROM Venta WHERE id_cuenta = :idCuenta")
    suspend fun getVentasByCuentaId(idCuenta: Int): List<Venta>
    ```

### 4.3 Bounded Context Compras: `CompraDAO` y `CompraProductoDAO`
* **Entidades:** `Compra`, `CompraProducto`
* **Repositorio:** `com.goodgus.localapplication.compras.data.repository.CompraRepository`
* **Métodos requeridos:**
  * `CompraDAO`:
    ```kotlin
    @Query("SELECT * FROM Compra WHERE id_compra = :idCompra")
    suspend fun getCompraById(idCompra: Int): Compra?

    @Query("SELECT * FROM Compra ORDER BY fecha DESC")
    suspend fun getAllCompras(): List<Compra>
    ```
  * `CompraProductoDAO`:
    ```kotlin
    @Query("SELECT * FROM CompraProducto WHERE id_compra = :idCompra")
    suspend fun getItemsByCompraId(idCompra: Int): List<CompraProducto>
    ```

### 4.4 Bounded Context Pedidos: `PedidosDAO`
* **Entidad:** `Pedidos`
* **Repositorio:** `com.goodgus.localapplication.pedidos.data.repository.PedidoRepository`
* **Métodos requeridos:**
  ```kotlin
  @Query("SELECT * FROM Pedidos WHERE id_pedido = :idPedido")
  suspend fun getPedidoById(idPedido: Int): Pedidos?

  @Query("SELECT * FROM Pedidos ORDER BY fecha_entrega ASC")
  suspend fun getAllPedidos(): List<Pedidos>

  @Query("SELECT * FROM Pedidos WHERE estado = :estado")
  suspend fun getPedidosByEstado(estado: String): List<Pedidos>
  ```

---

## 5. Checklist de Ejecución para la Próxima Sesión

1. [ ] Actualizar [`BaseDao.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/BaseDao.kt) con `suspend` y método `insertAll`.
2. [ ] Refactorizar [`ProductoDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/ProductoDAO.kt) eliminando queries redundantes (`addProduct`, `updateProducto`).
3. [ ] Refactorizar [`CuentaDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CuentaDAO.kt) y [`VentaDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/VentaDAO.kt).
4. [ ] Refactorizar [`CompraDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CompraDAO.kt) y [`CompraProductoDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/CompraProductoDAO.kt).
5. [ ] Refactorizar [`PedidosDAO.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/DAO/PedidosDAO.kt).
6. [ ] Conectar los hooks `onHydrateQuery` y `onGetAllQuery` en cada `*Repository` concreto.
7. [ ] Ejecutar la suite completa de pruebas unitarias (`./gradlew testDebugUnitTest`) para verificar cero regresiones.
