package com.segundoaire.testing

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Reloj que el test mueve a una hora concreta (UTC) para probar el saludo. */
class MutableClock(var instant: Instant = Instant.parse("2026-10-13T09:00:00Z")) : Clock() {
    fun setHour(hour: Int) {
        instant = Instant.parse("2026-10-13T%02d:00:00Z".format(hour))
    }

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this

    override fun instant(): Instant = instant
}
