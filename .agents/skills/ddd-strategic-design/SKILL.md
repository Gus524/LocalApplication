---
name: ddd-strategic-design
description: Workflow for strategic Domain-Driven Design (DDD), discovery of Bounded Contexts, ubiquitous language definitions, and architectural blueprinting before code implementation.
---
# DDD Strategic Design & Ubiquitous Language Workflow

## Overview
This skill guides the strategic discovery and modeling phase of DDD before writing implementation code. It ensures bounded contexts, ubiquitous language, and domain taxonomy (Aggregate Roots, Entities, Value Objects) are clearly defined, validated, and aligned with stakeholders.

## Execution Steps

1. **Strategic Subdomain Classification:**
   - Classify system areas into:
     - **Core Domain:** The unique business value and transaction center.
     - **Supporting Subdomain:** Complements the core domain.
     - **Generic Subdomain:** Standard operations (logging, basic formatting).
   - Evaluate trade-offs with the user before committing to a classification.

2. **Bounded Context (BC) Boundary Mapping:**
   - Identify distinct boundaries and contexts (e.g., `Inventario`, `Ventas`, `Compras`, `Pedidos`).
   - Define package structure rooted at the feature/BC level (e.g., `com.package.<bc>.domain`).

3. **Ubiquitous Language & Nomenclature Alignment:**
   - Establish terminology strictly in the business domain language (e.g., Spanish for Spanish-speaking businesses).
   - Document domain terms and definitions in plain text (avoid premature code blocks) to agree on naming.

4. **Taxonomy & Cohesive Modeling:**
   - **Aggregate Roots (AR):** Implement `AggregateRoot<TId>` as transactional boundaries.
   - **Entities:** Implement `Entity<TId>` for objects with identity inside the aggregate.
   - **Composite Value Objects (VO):** Group related attributes that naturally travel together without identity (e.g., `InformacionProducto`, `InformacionCuenta`, `DetalleVenta`).
   - **Repository Contracts:** Constrain `IRepository<TAggregate : AggregateRoot<TId>, TId>` to ensure repositories exist only for Aggregate Roots.
