package com.segundoaire.core.service

import android.util.Log
import com.segundoaire.di.ApplicationScope
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/**
 * Lleva al historial cada aparición del GlassWall y cómo se cerró. Se usa desde el
 * hilo principal y con la ventana ya agregada: aquí solo se toma la hora y se lanza
 * la escritura en [scope], sin esperar nunca a Room.
 *
 * La intercepción se guarda al mostrarse, sin resultado, y el resultado llega al
 * cerrarse. Sin `@Singleton`: vive lo que el servicio que lo recibe, así una
 * intercepción que quedó abierta al irse el servicio no se cierra con la siguiente.
 */
class InterceptionRecorder @Inject constructor(
    private val repository: InterceptionEventRepository,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope
) {
    // Id (por llegar) de la intercepción a la vista que aún no tiene resultado
    private var pending: Deferred<Long?>? = null

    /**
     * El GlassWall quedó a la vista sobre [packageName]. Si había otra intercepción
     * abierta, el usuario la dejó sin pulsar "Continuar": cuenta como que volvió.
     */
    fun onShown(packageName: String) {
        onClosed(InterceptionResult.VOLVIO)
        val timestampMillis = clock.millis()
        pending = scope.async { write { repository.record(packageName, timestampMillis) } }
    }

    /** El GlassWall se cerró con [result]. Sin una intercepción abierta no hace nada. */
    fun onClosed(result: InterceptionResult) {
        val id = pending ?: return
        pending = null
        scope.launch {
            val eventId = id.await() ?: return@launch
            write { repository.setResult(eventId, result) }
        }
    }

    /** El usuario dijo para qué entraba (chips de Mindful): queda en la intercepción abierta. */
    fun onIntent(intent: InterceptionIntent) {
        val id = pending ?: return
        scope.launch {
            val eventId = id.await() ?: return@launch
            write { repository.setIntent(eventId, intent) }
        }
    }

    // Una escritura fallida pierde un dato de Progreso, nunca tumba el servicio
    private suspend fun <T> write(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo registrar la intercepción", e)
        null
    }

    private companion object {
        const val TAG = "InterceptionRecorder"
    }
}
