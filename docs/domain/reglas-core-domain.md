# Reglas de Negocio y Especificación del Core Domain

Este documento formaliza las reglas de negocio, invariantes y diseño estratégico del Core Domain para **Inventario** y **Ventas**. Sirve como fuente de verdad para la validación de calidad y cobertura de pruebas.

---

## 1. Lenguaje Ubicuo y Nomenclatura

* **Idioma oficial:** Español.
* **Paquetes raíz por Bounded Context:**
  * `com.goodgus.localapplication.common.domain`
  * `com.goodgus.localapplication.inventario.domain`
  * `com.goodgus.localapplication.ventas.domain`
* **Abstracciones Base:**
  * `Entity<TId>`: Entidades con identidad explícita.
  * `AggregateRoot<TId>`: Raíces de Agregado que marcan fronteras transaccionales.
  * `IRepository<TAggregate : AggregateRoot<TId>, TId>`: Repositorios restringidos exclusivamente a raíces de agregado.

---

## 2. Bounded Context: Inventario

### Modelos y Value Objects
* **`ProductoId`:** Identificador fuertemente tipado (`valor >= 0`).
* **`InformacionProducto` (VO compuesto):**
  * `nombre: String`: No puede estar vacío ni en blanco.
  * `marca: String`: Marca del producto.
  * `tipo: String`: Categoría o tipo de producto.
* **`Inventario` (VO compuesto/atómico):**
  * `disponibles: Int`: No puede ser negativo (`>= 0`).
  * Invariantes: `descontar` (exige cantidad > 0 y existencia suficiente), `reabastecer` (exige cantidad > 0), `tieneExistenciasPara`.
* **`EstadoProducto`:** Enum `ACTIVO`, `INACTIVO`.
* **`Producto` (Aggregate Root):**
  * Gobierna el stock, precio de venta y estado de activación.

### Reglas e Invariantes
* **RN-INV-01 (No negatividad monetaria):** `Dinero` no permite montos menores a 0.0.
* **RN-INV-02 (Existencias físicas):** `Inventario` no permite cantidades negativas.
* **RN-INV-03 (Descuento atómico):** Solo descuenta si el producto está en estado `ACTIVO` y hay existencias suficientes. De lo contrario lanza `ProductoInactivoException` o `StockInsuficienteException`.
* **RN-INV-04 (Reabastecimiento):** `reabastecer` solo acepta cantidades mayores a 0.
* **RN-INV-05 (Actualización de precio):** Requiere un `Dinero` válido.
* **RN-INV-06 (Ciclo de vida):** Métodos explícitos `activar()` y `desactivar()`.

---

## 3. Bounded Context: Ventas

### Modelos y Value Objects
* **`CuentaId`**, **`VentaId`:** Identificadores fuertemente tipados (`valor >= 0`).
* **`CantidadVenta`:** Unidades vendidas (`valor >= 1`).
* **`DetalleVenta` (VO compuesto):**
  * `cantidad: CantidadVenta`, `hora: String`, `precioUnitario: Dinero`.
  * `subtotal: Dinero` derivado exactamente como `precioUnitario * cantidad.valor`.
* **`InformacionCuenta` (VO compuesto / snapshot):**
  * `fecha: String` (no vacía), `estado: EstadoCuenta` (`ABIERTA`, `CERRADA`), `total: Dinero`.
* **`Venta` (Entidad Interna):**
  * `id: VentaId`, `productoId: ProductoId`, `nombreProducto: String`, `detalle: DetalleVenta`, `estado: EstadoVenta` (`ACTIVA`, `CANCELADA`).
  * Si está `CANCELADA`, su subtotal computable es `Dinero.CERO`.
* **`Cuenta` (Aggregate Root):**
  * Gobierna la caja/ticket, líneas de venta y el cálculo del total acumulado.

### Reglas e Invariantes
* **RN-VEN-01 (Mínimo de venta):** No permite ventas con cantidad menor a 1.
* **RN-VEN-02 (Cálculo de subtotal):** `DetalleVenta.subtotal` se calcula siempre como `precioUnitario * cantidad`.
* **RN-VEN-03 (Precio personalizado / acordado):** El precio de venta lo recibe el método del agregado como `Dinero` (permitiendo descuentos especiales sin acoplar la regla al catálogo).
* **RN-VEN-04 (Cuenta abierta obligatoria):** Toda operación de inserción, modificación o cancelación exige que la cuenta esté `ABIERTA`. De lo contrario lanza `CuentaCerradaException`.
* **RN-VEN-05 (Registro atómico de venta):** `agregarVenta` descuenta existencias del `Producto`, agrega la `Venta` y actualiza inmediatamente el `total` en `InformacionCuenta`. Retorna `Pair<Cuenta, Producto>`.
* **RN-VEN-06 (Cancelación de venta):** `cancelarVenta` busca la venta, la marca `CANCELADA` y descuenta su importe del total de la cuenta. Lanza `VentaNoEncontradaException` si no existe.
* **RN-VEN-07 (Cierre de cuenta):** `cerrar()` cambia el estado a `CERRADA` con el total final consolidado. Falla si ya estaba cerrada.
* **RN-VEN-08 (Consistencia de total):** El total de la cuenta siempre coincide con la sumatoria de las ventas activas.
