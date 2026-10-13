package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.GreetingPeriod
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

/** Parte del día en la hora local del dispositivo, según el reloj inyectado. */
class GetGreetingPeriodUseCase @Inject constructor(
    private val clock: Clock
) {
    operator fun invoke(): GreetingPeriod = GreetingPeriod.at(LocalTime.now(clock).hour)
}
