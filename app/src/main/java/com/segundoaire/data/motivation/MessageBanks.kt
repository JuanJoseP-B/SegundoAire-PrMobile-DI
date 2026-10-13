package com.segundoaire.data.motivation

import androidx.annotation.ArrayRes
import com.segundoaire.R
import com.segundoaire.domain.model.Goal

// El dominio no conoce los recursos: aquí se asocia cada objetivo a su banco.
// Sin objetivo se usa un banco general.
@ArrayRes
internal fun Goal?.messageBankRes(): Int = when (this) {
    Goal.FOCUS -> R.array.motivation_focus
    Goal.SLEEP -> R.array.motivation_sleep
    Goal.PRESENCE -> R.array.motivation_presence
    Goal.MENTAL_HEALTH -> R.array.motivation_mental_health
    Goal.FREE_TIME -> R.array.motivation_free_time
    null -> R.array.motivation_general
}
