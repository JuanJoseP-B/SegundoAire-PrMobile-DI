package com.segundoaire.domain.model

/** Lo que el GlassWall deja hacer, según el bloque que esté en curso al interceptar. */
sealed interface GlassWallState {

    /** Sin bloque activo: "Volver" o "Continuar". */
    data object NoBlock : GlassWallState

    /** Bloque Mindful: "Continuar" se habilita tras [MINDFUL_WAIT_SECONDS] con el overlay a la vista. */
    data object Mindful : GlassWallState

    /** Bloque Strict: no hay "Continuar" hasta [blockEndsAt] (reloj de pared, ms desde epoch). */
    data class Strict(val blockEndsAt: Long) : GlassWallState

    companion object {
        const val MINDFUL_WAIT_SECONDS = 10

        /** Un bloque vencido que nadie retiró cuenta como que no hay bloque. */
        fun of(block: ActiveBlock?, now: Long): GlassWallState = when {
            block == null || !block.isActiveAt(now) -> NoBlock
            block.rigor == RigorLevel.MINDFUL -> Mindful
            else -> Strict(block.endsAt)
        }
    }
}
