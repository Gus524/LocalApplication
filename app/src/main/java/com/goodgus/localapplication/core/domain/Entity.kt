package com.goodgus.localapplication.core.domain

/**
 * Contrato base para cualquier entidad del dominio con identidad única.
 */
interface Entity<TId> {
    val id: TId
}
