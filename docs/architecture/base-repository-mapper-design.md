# Arquitectura de Persistencia: BaseRepository e IMapper

Este documento especifica el diseño de la capa de datos para la implementación de repositorios y mapeadores, eliminando código repetitivo (*boilerplate*) y garantizando el desacoplamiento total entre las entidades de base de datos (Room) y los modelos de Dominio Puro.

---

## 1. Contrato `IMapper<TDomain, TPersistence>`

Ubicado en la capa de datos (`com.goodgus.localapplication.common.data.mapper`), este contrato garantiza la transformación bidireccional entre la entidad de persistencia (Room) y el modelo de dominio puro:

```kotlin
package com.goodgus.localapplication.common.data.mapper

/**
 * Contrato genérico para transformaciones bidireccionales entre el Dominio y la Persistencia.
 */
interface IMapper<TDomain, TPersistence> {
    fun toDomain(entity: TPersistence): TDomain
    fun toPersistence(domain: TDomain): TPersistence

    fun toDomainList(entities: List<TPersistence>): List<TDomain> = entities.map { toDomain(it) }
    fun toPersistenceList(domains: List<TDomain>): List<TPersistence> = domains.map { toPersistence(it) }
}
```

---

## 2. Abstracción `BaseRepository`

Ubicada en la capa de datos (`com.goodgus.localapplication.common.data.repository`), esta clase abstracta implementa el contrato `IRepository<TAggregate, TId>` y centraliza el manejo de corrutinas (`Dispatchers.IO`), excepciones (`runCatching`) y delegación de mapeo:

```kotlin
package com.goodgus.localapplication.common.data.repository

import com.goodgus.localapplication.common.data.mapper.IMapper
import com.goodgus.localapplication.common.domain.AggregateRoot
import com.goodgus.localapplication.common.domain.IRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clase base abstracta para repositorios concretos.
 * Centraliza la ejecución en hilos de I/O y la transformación automática de modelos vía IMapper.
 */
abstract class BaseRepository<TAggregate : AggregateRoot<TId>, TId, TPersistence>(
    protected val mapper: IMapper<TAggregate, TPersistence>,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IRepository<TAggregate, TId> {

    protected suspend fun <R> executeIo(block: suspend () -> R): Result<R> =
        withContext(ioDispatcher) {
            runCatching { block() }
        }
}
```

---

## 3. Implementaciones Concretas por Bounded Context

Cada Bounded Context implementará su repositorio extendiendo de `BaseRepository` e inyectando su `IMapper`:

1. **`ProductoRepositoryImpl`:**
   * Implementa `IProductoRepository`.
   * Usa `ProductoMapper : IMapper<Producto, models.data.Producto>` y `ProductoDAO`.
2. **`CuentaRepositoryImpl`:**
   * Implementa `ICuentaRepository`.
   * Usa `CuentaMapper : IMapper<Cuenta, models.data.Cuenta>` junto con `CuentaDAO`, `VentaDAO` y la vista `GetCuenta`.
3. **`CompraRepositoryImpl`:**
   * Implementa `ICompraRepository`.
   * Usa `CompraMapper : IMapper<Compra, models.data.Compra>` junto con `CompraDAO` y `CompraProductoDAO`.
4. **`PedidoRepositoryImpl`:**
   * Implementa `IPedidoRepository`.
   * Usa `PedidoMapper : IMapper<Pedido, models.data.Pedidos>` y `PedidosDAO`.

---

## 4. Beneficios Arquitectónicos
* **Pureza del Dominio:** Los modelos de Room (`models.data.*`) no escapan a la capa de dominio ni a la capa de presentación.
* **Cero Boilerplate:** No se duplica lógica de `withContext(Dispatchers.IO)` ni manejo repetitivo de listas en cada repositorio.
* **Testabilidad:** Permite testear los mappers de forma aislada y mockear fácilmente los repositorios en pruebas de casos de uso y ViewModels.
