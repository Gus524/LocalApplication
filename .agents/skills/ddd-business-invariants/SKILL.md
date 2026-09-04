---
name: ddd-business-invariants
description: Workflow for discovering, formalizing, encapsulating, and verifying pure domain business rules and invariants inside Aggregate Roots and Value Objects in Kotlin.
---
# DDD Business Invariants & Domain Rules Workflow

## Overview
This skill guides the formalization and encapsulation of pure domain business rules. It guarantees that domain models enforce invariants internally, eliminating anemic models and preventing domain logic leakage into ViewModels, DAOs, or UI components.

## Execution Steps

1. **Rule Discovery & Extraction:**
   - Audit existing ViewModels, Repositories, and SQL queries to identify implicit or scattered business logic (e.g., stock reductions, price calculations, state transitions).
   - Extract domain constraints into explicit invariant rules.

2. **Business Rules Proposal & Human-in-the-Loop Validation:**
   - Present a clear, structured catalog of the discovered and proposed business invariants (in plain text, categorized by VO, Entity, and AR).
   - Solicit explicit user feedback and confirmation before writing or modifying any code.

3. **Value Object Invariant Enforcement:**
   - Guard value creation in `init` blocks or factory constructors (`require(...)`).
   - Implement domain-specific operations directly on Value Objects (e.g., `Dinero.plus`, `Inventario.descontar`, `Inventario.reabastecer`).
   - Ensure Value Objects are immutable.

4. **Aggregate Root Transactional Guarding:**
   - Encapsulate aggregate state mutations behind explicit business methods returning `Result<TAggregate>` or updated aggregates.
   - Enforce lifecycle and transactional preconditions (e.g., cannot add sales or mutate a closed account).
   - Compute and derive financial balances internally within the aggregate root to guarantee consistency.

5. **Domain Exception Handling:**
   - Define expressive domain-specific errors (e.g., `StockInsuficienteException`, `CuentaCerradaException`).
   - Keep domain free of Android framework, Room, or external library dependencies.
