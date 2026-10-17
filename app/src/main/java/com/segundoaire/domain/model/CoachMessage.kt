package com.segundoaire.domain.model

/** Lo que el usuario le pregunta al coach con los chips de "Pregúntale". */
enum class CoachTopic { WEEK, IDEA, HARDEST_TIME }

/** Lo que hace cada botón del coach. Todas actúan de verdad; ninguna promete algo que la app no hace. */
enum class CoachAction {
    /** Lleva a Bloques para crear uno; no programa ningún horario. */
    CREATE_BLOCK,

    /** Inicia un bloque de 15 min. */
    START_15,

    /** Lleva a Progreso. */
    VIEW_PROGRESS,

    /** Confirma la idea con un aviso breve; no agenda notificaciones. */
    REMIND,

    /** Pide otro mensaje. */
    NEXT
}

/**
 * Un mensaje del coach. Lleva los datos, no el texto: la presentación lo arma en el
 * idioma del usuario. [primary] es la acción sólida y [secondary] la de vidrio.
 */
sealed interface CoachMessage {
    val primary: CoachAction
    val secondary: CoachAction

    /** La hora de la semana con más aperturas interrumpidas. */
    data class HardestHour(val hour: Int, val openings: Int) : CoachMessage {
        override val primary = CoachAction.CREATE_BLOCK
        override val secondary = CoachAction.NEXT
    }

    /** Pausas respetadas hoy, comparadas con ayer. */
    data class PausesToday(val today: Int, val yesterday: Int) : CoachMessage {
        override val primary = CoachAction.START_15
        override val secondary = CoachAction.VIEW_PROGRESS
    }

    /** Resumen de los últimos 7 días. */
    data class WeekSummary(val minutes: Int, val pauses: Int) : CoachMessage {
        override val primary = CoachAction.VIEW_PROGRESS
        override val secondary = CoachAction.START_15
    }

    /** Para qué entra más seguido, según los chips del GlassWall de la semana. */
    data class IntentPattern(val intent: InterceptionIntent, val times: Int) : CoachMessage {
        override val primary = CoachAction.REMIND
        override val secondary = CoachAction.NEXT
    }

    /** Una idea para hoy según el objetivo principal. */
    data class GoalIdea(val goal: Goal?) : CoachMessage {
        override val primary = CoachAction.REMIND
        override val secondary = CoachAction.NEXT
    }

    /** Todavía no hay datos: invita al primer bloque. */
    data object FirstSteps : CoachMessage {
        override val primary = CoachAction.START_15
        override val secondary = CoachAction.CREATE_BLOCK
    }
}
