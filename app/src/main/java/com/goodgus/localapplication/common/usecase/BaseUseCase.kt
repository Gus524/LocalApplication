package com.goodgus.localapplication.common.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso base que implementa el patrón Template Method / Decorator.
 * Centraliza la ejecución segura en un hilo de cálculo/background y encapsula el resultado en Result<Resultado>.
 */
abstract class BaseUseCase<in Parametros, Resultado>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(params: Parametros): Result<Resultado> {
        return withContext(dispatcher) {
            try {
                ejecutar(params)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }

    /**
     * Lógica de orquestación implementada por el caso de uso concreto mediante retornos explícitos Result.
     */
    protected abstract suspend fun ejecutar(params: Parametros): Result<Resultado>
}
