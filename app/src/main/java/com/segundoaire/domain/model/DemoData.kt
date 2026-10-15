package com.segundoaire.domain.model

/** Historial de ejemplo para el modo demo. Vive solo en memoria: nunca se guarda. */
data class DemoData(
    val sessions: List<BlockSession>,
    val events: List<InterceptionEvent>
)
