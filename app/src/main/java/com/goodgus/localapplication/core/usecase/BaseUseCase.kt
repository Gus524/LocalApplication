package com.goodgus.localapplication.core.usecase

/**
 * Caso de uso base que implementa el patrón Template Method / Decorator.
 * Encapsula la ejecución segura en un bloque Result<Resultado>.
 */
abstract class BaseUseCase<in Parametros, Resultado> {
    suspend operator fun invoke(params: Parametros): Result<Resultado> {
        return try {
            ejecutar(params)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    /**
     * Lógica de orquestación implementada por el caso de uso concreto mediante retornos explícitos Result.
     */
    protected abstract suspend fun ejecutar(params: Parametros): Result<Resultado>
}

