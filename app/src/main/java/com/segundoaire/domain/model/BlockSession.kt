package com.segundoaire.domain.model

/**
 * Un bloque de enfoque que ya terminó. [completed] es `true` si llegó a su fin y
 * `false` si el usuario lo detuvo antes. Los instantes son ms desde epoch.
 */
data class BlockSession(
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val rigor: RigorLevel,
    val completed: Boolean
)
