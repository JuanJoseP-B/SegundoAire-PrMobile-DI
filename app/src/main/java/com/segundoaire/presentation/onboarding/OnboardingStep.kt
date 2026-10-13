package com.segundoaire.presentation.onboarding

/** Pasos del onboarding, en el orden de SEGUNDOAIRE_SPEC.md §4.1. Cada uno es un destino del grafo. */
enum class OnboardingStep(val route: String) {
    WELCOME("onboarding/welcome"),
    NAME("onboarding/name"),
    GOALS("onboarding/goals"),
    USAGE("onboarding/usage"),
    MOMENTS("onboarding/moments"),
    REVEAL("onboarding/reveal"),
    BREATHING("onboarding/breathing"),
    PLAN("onboarding/plan"),
    COMMITMENT("onboarding/commitment");

    /** Posición en el progreso segmentado. La bienvenida es 0: va antes de las preguntas. */
    val progress: Int get() = ordinal

    val next: OnboardingStep? get() = entries.getOrNull(ordinal + 1)

    companion object {
        /** Segmentos del progreso: todos los pasos menos la bienvenida. */
        val PROGRESS_STEPS: Int = entries.size - 1
    }
}
