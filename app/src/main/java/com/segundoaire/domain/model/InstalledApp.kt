package com.segundoaire.domain.model

/** Una app instalada con ícono de lanzador, candidata a vigilarse. */
data class InstalledApp(
    val packageName: String,
    val appName: String
)
