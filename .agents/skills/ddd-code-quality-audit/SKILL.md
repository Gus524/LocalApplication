---
name: ddd-code-quality-audit
description: Workflow for evaluating and auditing pure Kotlin domain code quality, test suite coverage, edge cases, ubiquitous language fidelity, and DDD invariant compliance without modifying files.
---
# DDD Code Quality & Invariant Audit Workflow

## Overview
This skill guides a non-modifying auditor to critically evaluate domain code quality, invariant enforcement, test coverage, and boundary isolation against defined domain specifications.

## Audit Checklist & Dimensions

1. **Framework & Layer Isolation:**
   - Verify zero imports from Android framework (`android.*`, `androidx.room.*`, `androidx.compose.*`, etc.) in all `domain` packages.
   - Verify all models are pure Kotlin data classes, value classes, or sealed types.

2. **DDD Structural Conformance:**
   - Verify every Aggregate Root implements `AggregateRoot<TId>`.
   - Verify inner entities implement `Entity<TId>` and DO NOT implement `AggregateRoot<TId>`.
   - Verify repositories implement `IRepository<TAggregate : AggregateRoot<TId>, TId>`.
   - Verify Value Objects are immutable with encapsulated validation in `init` blocks.

3. **Ubiquitous Language & Nomenclature:**
   - Compare terms in code against the specification in `docs/domain/reglas-core-domain.md`.
   - Verify consistent naming in Spanish across classes, methods, and parameters.

4. **Business Rule Enforcement & Edge Cases:**
   - Audit each rule in `docs/domain/reglas-core-domain.md` (e.g., negative money, zero quantity, inactive product, closed account, non-existent sale cancellation).
   - Check if unit tests in `app/src/test/java/` cover both normal scenarios (happy paths) and edge cases (boundary numbers, negative numbers, blank strings, invalid state transitions).

5. **Scoring & Report Generation:**
   - Grade the implementation across:
     - Domain Purity & Architecture (0 - 100)
     - Ubiquitous Language Fidelity (0 - 100)
     - Invariant & Business Rule Enforcement (0 - 100)
     - Unit Test Completeness & Edge Cases (0 - 100)
   - Emit a structured markdown report with specific findings, identified gaps (if any), and recommendations.
