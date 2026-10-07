package com.segundoaire.domain.repository

interface AccessibilityStatusRepository {
    fun isServiceEnabled(): Boolean
}
