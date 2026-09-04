# Subdominios de Soporte y Genéricos - Especificación del Sistema

Este documento resume los subdominios secundarios identificados en el análisis del código base existente (`app/src/main/java/com/goodgus/localapplication`), sus componentes actuales en Room/UI y su propuesta de modelado DDD para futuras fases.

---

## 1. Bounded Context: Compras y Abastecimiento (Supporting Subdomain)

### Estado Actual en la Aplicación
* **Entidades Room actuales:**
  * `models.data.Compra` (`id_compra`, `total_compra`, `fecha_compra`)
  * `models.data.CompraProducto` (`id_compra_producto`, `parcial_compra`, `cantidad_producto`, `id_compra`, `id_producto`)
* **DAOs y Repositorios actuales:**
  * `DAO.CompraDAO`, `DAO.CompraProductoDAO`
  * `repository.CompraRepository`
* **UI / ViewModels actuales:**
  * `viewModels.CompraViewModel`
  * `views.ComprasView`, `views.CompraView`

### Propuesta DDD para Futura Migración
* **Aggregate Root:** `Compra` (`AggregateRoot<CompraId>`)
  * Encapsula la transacción de abastecimiento con proveedores.
* **Entidad Interna:** `ItemCompra` (`Entity<CompraProductoId>`)
  * Representa cada artículo adquirido en el lote de compra.
* **Value Objects:**
  * `CompraId`, `CompraProductoId`
  * `FechaCompra`
  * `DetalleItemCompra` (`cantidad: Int`, `costoUnitario: Dinero`, `parcial: Dinero`)
* **Invariantes y Reglas Principales:**
  * Cantidad comprada debe ser estrictamente mayor a cero (`> 0`).
  * Cálculo exacto del costo total de compra a partir de los parciales de los ítems.
  * Orquestación de reabastecimiento: la confirmación de una compra incrementa automáticamente el `Inventario` del `Producto` correspondiente (`producto.reabastecer(cantidad)`).

---

## 2. Bounded Context: Pedidos y Encargos (Supporting Subdomain)

### Estado Actual en la Aplicación
* **Entidad Room actual:**
  * `models.data.Pedidos` (`id_pedido`, `fecha_pedido`, `fecha_entrega`, `descripcion`, `detalles`)
* **DAOs y Repositorios actuales:**
  * `DAO.PedidosDAO`
  * `repository.PedidoRepository`
* **UI / ViewModels actuales:**
  * `viewModels.PedidosViewModel`, `viewModels.EditPedidoViewModel`
  * `views.PedidosView`, `views.EditPedidoView`

### Propuesta DDD para Futura Migración
* **Aggregate Root:** `Pedido` (`AggregateRoot<PedidoId>`)
  * Representa un encargo o apartado especial solicitado por un cliente.
* **Value Objects:**
  * `PedidoId`
  * `InformacionPedido` (`descripcion: String`, `detalles: String?`)
  * `PlazoEntrega` (`fechaPedido: String`, `fechaEntrega: String?`)
  * `EstadoPedido` (Enum: `PENDIENTE`, `ENTREGADO`, `CANCELADO`)
* **Invariantes y Reglas Principales:**
  * `descripcion` no puede estar vacía.
  * `fechaEntrega` (si existe) debe ser igual o posterior a la `fechaPedido`.
  * Transición explícita de estados (`entregar()`, `cancelar()`).

---

## 3. Subdominios Genéricos y Componentes Transversales

* **Persistencia Local (Data Layer):**
  * `DAO.AppDataBase`: Base de datos SQLite Room con precarga desde asset `assets/database/db_local.db`.
  * Mappers pendientes: transformación bidireccional entre Entities Room (`models.data.*`) y los modelos de Dominio Puro.
* **Navegación y UI (Presentation Layer):**
  * `core.navigation.NavigationWrapper`, `core.navigation.Screens`, `core.navigation.NavigationBar`.
  * Componentes reutilizables: `components.AppScaffold`, `components.ButtonBar`, `components.TopBar`, `components.InfoCard`.
  * Próxima modernización: migración de ViewModels a MVI / UDF (`StateFlow<UiState>`, `UiEvent`) consumiendo Casos de Uso del Dominio.
