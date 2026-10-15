package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.DemoData
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.InterceptionResult.CONTINUO
import com.segundoaire.domain.model.InterceptionResult.VOLVIO
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RigorLevel.MINDFUL
import com.segundoaire.domain.model.RigorLevel.STRICT
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Siete días de ejemplo que terminan hoy, en la zona del reloj: bloques (alguno
 * detenido antes de tiempo) e intercepciones con su resultado. Es determinista,
 * así la demo se ve igual cada vez, y nada queda en el futuro: de hoy solo aparece
 * lo que ya pasó a esta hora.
 */
class GenerarDatosDemoUseCase @Inject constructor(
    private val clock: Clock
) {
    operator fun invoke(): DemoData {
        val zone = clock.zone
        val now = clock.millis()
        val today = LocalDate.now(clock)
        val sessions = mutableListOf<BlockSession>()
        val events = mutableListOf<InterceptionEvent>()

        WEEK.forEachIndexed { index, plan ->
            val day = today.minusDays((WEEK.size - 1 - index).toLong())
            fun millisAt(time: LocalTime) = day.atTime(time).atZone(zone).toInstant().toEpochMilli()

            plan.blocks.forEach { block ->
                val end = millisAt(block.endsAt)
                if (end <= now) {
                    sessions += BlockSession(
                        startedAt = end - block.minutes * MILLIS_PER_MINUTE,
                        endedAt = end,
                        durationMinutes = block.minutes,
                        rigor = block.rigor,
                        completed = block.completed
                    )
                }
            }
            plan.interceptions.forEach { (time, result) ->
                val at = millisAt(time)
                if (at <= now) {
                    events += InterceptionEvent(events.size + 1L, DEMO_PACKAGE, at, result)
                }
            }
        }
        return DemoData(sessions, events)
    }

    private data class DemoBlock(
        val endsAt: LocalTime,
        val minutes: Int,
        val rigor: RigorLevel,
        val completed: Boolean = true
    )

    private data class DemoDay(
        val blocks: List<DemoBlock>,
        val interceptions: List<Pair<LocalTime, InterceptionResult>>
    )

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L

        // Paquete ficticio: no corresponde a ninguna app real
        const val DEMO_PACKAGE = "com.segundoaire.demo"

        fun at(hour: Int, minute: Int): LocalTime = LocalTime.of(hour, minute)

        // Del día más antiguo a hoy. Todos los días tienen al menos un bloque completado
        val WEEK = listOf(
            DemoDay(
                blocks = listOf(DemoBlock(at(9, 30), 25, MINDFUL), DemoBlock(at(18, 0), 15, STRICT)),
                interceptions = listOf(at(10, 15) to VOLVIO, at(13, 40) to CONTINUO, at(16, 20) to VOLVIO, at(21, 5) to VOLVIO)
            ),
            DemoDay(
                blocks = listOf(DemoBlock(at(8, 45), 30, STRICT), DemoBlock(at(20, 30), 10, MINDFUL, completed = false)),
                interceptions = listOf(at(11, 0) to VOLVIO, at(15, 30) to VOLVIO, at(22, 10) to CONTINUO)
            ),
            DemoDay(
                blocks = listOf(
                    DemoBlock(at(10, 0), 15, MINDFUL),
                    DemoBlock(at(14, 30), 30, MINDFUL),
                    DemoBlock(at(19, 0), 15, STRICT)
                ),
                interceptions = listOf(
                    at(9, 20) to VOLVIO,
                    at(12, 45) to VOLVIO,
                    at(17, 10) to VOLVIO,
                    at(21, 40) to CONTINUO,
                    at(23, 0) to VOLVIO
                )
            ),
            DemoDay(
                blocks = listOf(DemoBlock(at(7, 30), 10, MINDFUL)),
                interceptions = listOf(at(13, 15) to CONTINUO, at(18, 50) to VOLVIO)
            ),
            DemoDay(
                blocks = listOf(DemoBlock(at(9, 0), 30, STRICT), DemoBlock(at(16, 0), 25, MINDFUL)),
                interceptions = listOf(at(10, 30) to VOLVIO, at(14, 5) to VOLVIO, at(20, 20) to VOLVIO, at(22, 45) to VOLVIO)
            ),
            DemoDay(
                blocks = listOf(DemoBlock(at(11, 30), 15, MINDFUL), DemoBlock(at(21, 0), 30, STRICT)),
                interceptions = listOf(at(12, 10) to CONTINUO, at(15, 55) to VOLVIO, at(19, 35) to VOLVIO)
            ),
            DemoDay(
                blocks = listOf(
                    DemoBlock(at(8, 15), 15, MINDFUL),
                    DemoBlock(at(12, 30), 25, STRICT),
                    DemoBlock(at(17, 45), 15, MINDFUL)
                ),
                interceptions = listOf(at(9, 5) to VOLVIO, at(11, 40) to VOLVIO, at(16, 10) to CONTINUO, at(19, 25) to VOLVIO)
            )
        )
    }
}
