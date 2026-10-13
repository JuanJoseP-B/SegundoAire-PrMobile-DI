package com.segundoaire.presentation.setup

import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.testing.FakeAccessibilityStatusRepository
import com.segundoaire.testing.FakeOverlayPermissionRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionsViewModelTest {

    private val accessibility = FakeAccessibilityStatusRepository()
    private val overlay = FakeOverlayPermissionRepository()

    private fun viewModel() = PermissionsViewModel(
        CheckAccessibilityEnabledUseCase(accessibility),
        CheckOverlayPermissionUseCase(overlay)
    )

    @Test
    fun `sin permisos no se puede continuar`() {
        val state = viewModel().uiState.value

        assertFalse(state.isAccessibilityGranted)
        assertFalse(state.isOverlayGranted)
        assertFalse(state.canContinue)
    }

    @Test
    fun `con un solo permiso tampoco se puede continuar`() {
        accessibility.enabled = true
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canContinue)

        accessibility.enabled = false
        overlay.granted = true
        viewModel.refresh()

        assertTrue(viewModel.uiState.value.isOverlayGranted)
        assertFalse(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `refresh lee los permisos concedidos en Ajustes`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canContinue)

        accessibility.enabled = true
        overlay.granted = true
        viewModel.refresh()

        assertTrue(viewModel.uiState.value.isAccessibilityGranted)
        assertTrue(viewModel.uiState.value.isOverlayGranted)
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `refresh refleja un permiso revocado`() {
        accessibility.enabled = true
        overlay.granted = true
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.canContinue)

        overlay.granted = false
        viewModel.refresh()

        assertFalse(viewModel.uiState.value.isOverlayGranted)
        assertFalse(viewModel.uiState.value.canContinue)
    }
}
