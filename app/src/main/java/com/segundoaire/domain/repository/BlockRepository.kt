package com.segundoaire.domain.repository

import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import kotlinx.coroutines.flow.StateFlow

interface BlockRepository {
    /**
     * Bloque en curso o `null`. Es un [StateFlow] para que el motor de
     * intercepción lea `value` desde memoria, sin E/S ni corrutinas.
     */
    fun observeActiveBlock(): StateFlow<ActiveBlock?>

    /** Inicia un bloque desde ahora y lo persiste; reemplaza al que hubiera. */
    suspend fun start(durationMinutes: Int, rigor: RigorLevel)

    /** El usuario termina el bloque antes de tiempo. */
    suspend fun stop()

    /** El bloque llegó a su fin: deja de estar activo y queda como completado por ver. */
    suspend fun complete()

    /**
     * Minutos del último bloque que llegó a su fin y el usuario todavía no ha visto,
     * o `null`. Sobrevive al cierre de la app: un bloque que vence con la app cerrada
     * aparece aquí al volver a abrirla.
     */
    fun observeCompletedMinutes(): StateFlow<Int?>

    /** El usuario ya vio el bloque completado. */
    suspend fun acknowledgeCompleted()
}
