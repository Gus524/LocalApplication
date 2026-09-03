# Lessons Learned & Architecture Memory

## 1. 4-Step DDD Engineering Lifecycle
For any new domain module, feature, or phase:
1. **Strategic Discovery & Blueprinting:** Identify Bounded Contexts, define Ubiquitous Language (Spanish business domain), propose AR, Entity, and Composite VO taxonomy in plain text before touching code (`.agents/skills/ddd-strategic-design/SKILL.md`).
2. **Business Invariant Gate:** Present structured business rules catalog (VO, Entity, AR, Exceptions) for explicit user confirmation (`.agents/skills/ddd-business-invariants/SKILL.md`).
3. **Pure Kotlin Implementation (Strict TDD):** Implement pure domain models without Android/Room framework dependencies. Base contracts: `Entity<TId>`, `AggregateRoot<TId>`, `IRepository<TAggregate, TId>`. Ensure green unit tests with edge cases.
4. **Read-Only QA Audit:** Execute `domain_quality_auditor` subagent to grade purity, language fidelity, and test edge case coverage (`.agents/skills/ddd-code-quality-audit/SKILL.md`).

## 2. Core Architectural Invariants
* **Package by Bounded Context at Root Level:**
  * `com.goodgus.localapplication.common.domain`
  * `com.goodgus.localapplication.<bc>.domain.model`
  * `com.goodgus.localapplication.<bc>.domain.repository`
* **Pure Domain vs Use Case Separation:**
  * **Aggregate Root Invariants:** Only receive validated VOs and enforce atomic state/balance transitions in pure immutable Kotlin.
  * **Use Cases:** Orchestrate data origin (e.g. default catalog price vs custom discounted price input).
* **Existing Storage & Persistence:**
  * Existing Room models (`models/data/*`) remain as internal persistence data layer, decoupled from Domain via Mappers.

## 3. Environment & Execution Runtime
* Bazzite OS with Java 25 (`java-dev` distrobox exported to `~/.local/bin/`).
* `gradle.properties` includes `kotlin.jvm.target.validation.mode=warning` for JDK 25 compatibility.
* Test runner: `./gradlew testDebugUnitTest`.
