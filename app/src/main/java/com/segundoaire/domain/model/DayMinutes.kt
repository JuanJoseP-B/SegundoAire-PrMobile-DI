package com.segundoaire.domain.model

import java.time.LocalDate

/** Minutos recuperados (de bloques completados) en un día de calendario. */
data class DayMinutes(val date: LocalDate, val minutes: Int)
