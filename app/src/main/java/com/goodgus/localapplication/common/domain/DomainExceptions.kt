package com.goodgus.localapplication.common.domain

import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.VentaId

/**
 * Excepciones de negocio del dominio.
 */
sealed class DomainException(message: String) : RuntimeException(message)

class StockInsuficienteException(
    val productoId: ProductoId,
    val disponibles: Int,
    val requeridos: Int
) : DomainException("Stock insuficiente para el producto $productoId. Disponibles: $disponibles, Requeridos: $requeridos")

class ProductoInactivoException(
    val productoId: ProductoId
) : DomainException("El producto $productoId se encuentra inactivo y no puede ser operado")

class CuentaCerradaException(
    val cuentaId: CuentaId
) : DomainException("La cuenta $cuentaId se encuentra cerrada y no permite modificaciones")

class VentaNoEncontradaException(
    val ventaId: VentaId,
    val cuentaId: CuentaId
) : DomainException("La venta $ventaId no fue encontrada en la cuenta $cuentaId")
