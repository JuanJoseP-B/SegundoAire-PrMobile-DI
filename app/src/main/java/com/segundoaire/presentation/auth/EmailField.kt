package com.segundoaire.presentation.auth

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassTextField

/** Campo de correo de los formularios de cuenta: teclado de correo, sin autocorrección. */
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    onBlur: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    GlassTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.auth_email_label),
        modifier = modifier.onBlur(onBlur),
        placeholder = stringResource(R.string.auth_email_placeholder),
        errorMessage = if (showError) stringResource(R.string.auth_email_error) else null,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions
    )
}

/** Llama a [onBlur] cuando el campo pierde el foco después de haberlo tenido. */
fun Modifier.onBlur(onBlur: () -> Unit): Modifier = composed {
    val currentOnBlur by rememberUpdatedState(onBlur)
    var hadFocus by remember { mutableStateOf(false) }
    onFocusChanged { focus ->
        if (hadFocus && !focus.hasFocus) currentOnBlur()
        hadFocus = focus.hasFocus
    }
}
