package com.segundoaire.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.navigation.flowGraph
import com.segundoaire.presentation.navigation.navigateFrom
import com.segundoaire.presentation.navigation.popFrom

const val AuthGraphRoute = "auth"

private const val AccountRoute = "auth/account"
private const val EmailRoute = "auth/email"
private const val ForgotPasswordRoute = "auth/forgot-password"

/**
 * Grafo anidado de la cuenta simulada: cuenta → correo → recuperar contraseña,
 * con `Motion.step` entre ellos y fundido (`Motion.nav`) al entrar o salir.
 * Cada destino tiene su propio
 * ViewModel. [onCompleted] se llama una vez que el modo de acceso quedó guardado.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    motion: SegundoAireMotion,
    stepOffsetPx: Int,
    forward: Int,
    onCompleted: () -> Unit
) {
    flowGraph(
        route = AuthGraphRoute,
        startDestination = AccountRoute,
        motion = motion,
        stepOffsetPx = stepOffsetPx,
        forward = forward
    ) {
        composable(AccountRoute) {
            val viewModel: AccountViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CompletionEffect(state.isCompleted, onCompleted)
            AccountScreen(
                state = state,
                onAuthModeSelected = { mode ->
                    if (mode == AuthMode.EMAIL) {
                        if (!state.isBusy) navController.navigateFrom(AccountRoute, EmailRoute)
                    } else {
                        viewModel.onAuthModeSelected(mode)
                    }
                }
            )
        }
        composable(EmailRoute) {
            val viewModel: EmailAuthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CompletionEffect(state.isCompleted, onCompleted)
            EmailAuthScreen(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmationChange = viewModel::onConfirmationChange,
                onFieldBlur = viewModel::onFieldBlur,
                onToggleMode = viewModel::onToggleMode,
                onForgotPassword = { navController.navigateFrom(EmailRoute, ForgotPasswordRoute) },
                onSubmit = viewModel::onSubmit,
                onBack = { navController.popFrom(EmailRoute) }
            )
        }
        composable(ForgotPasswordRoute) {
            val viewModel: ForgotPasswordViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ForgotPasswordScreen(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onEmailBlur = viewModel::onEmailBlur,
                onSubmit = viewModel::onSubmit,
                onBack = { navController.popFrom(ForgotPasswordRoute) }
            )
        }
    }
}

@Composable
private fun CompletionEffect(isCompleted: Boolean, onCompleted: () -> Unit) {
    val currentOnCompleted by rememberUpdatedState(onCompleted)
    LaunchedEffect(isCompleted) {
        if (isCompleted) currentOnCompleted()
    }
}
