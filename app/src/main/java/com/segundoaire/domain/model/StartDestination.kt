package com.segundoaire.domain.model

/**
 * Por dónde entra la app al abrirse, según qué tan avanzada esté la primera
 * ejecución. Cada valor implica que los anteriores ya se cumplieron.
 */
enum class StartDestination {
    /** Aún no se completó el onboarding. */
    ONBOARDING,

    /** Onboarding listo, pero falta elegir cómo continuar (cuenta simulada). */
    ACCOUNT,

    /** Cuenta lista, pero el setup (permisos y apps) no se confirmó. */
    SETUP,

    /** Todo configurado: va directo al Panel. */
    MAIN
}
