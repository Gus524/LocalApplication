package com.goodgus.localapplication.core.ui.components.feedback

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Gestor global de Snackbars para coordinar mensajes que deben persistir a través de
 * transiciones de navegación y cambios de pantalla.
 */
object SnackbarManager {
    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

    fun mostrarMensaje(mensaje: String) {
        _mensajes.trySend(mensaje)
    }
}
