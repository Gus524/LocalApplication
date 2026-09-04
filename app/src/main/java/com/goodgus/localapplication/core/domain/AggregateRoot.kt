package com.goodgus.localapplication.core.domain

/**
 * Contrato marcador para Raíces de Agregado (Aggregate Root).
 * Define la frontera transaccional para un conjunto coherente de entidades y Value Objects.
 */
interface AggregateRoot<TId> : Entity<TId>
