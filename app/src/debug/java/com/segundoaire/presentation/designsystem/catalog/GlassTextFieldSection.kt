package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassTextField

@Composable
internal fun GlassTextFieldSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_glass_text_field)) {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        // El error aparece al escribir algo que no parece un correo
        GlassTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.auth_email_label),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.auth_email_placeholder),
            errorMessage = if (email.isNotEmpty() && '@' !in email) {
                stringResource(R.string.auth_email_error)
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        GlassTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.auth_password_label),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(R.string.catalog_password_hint),
            isPassword = true
        )
        GlassTextField(
            value = stringResource(R.string.catalog_state_disabled),
            onValueChange = {},
            label = stringResource(R.string.catalog_state_disabled),
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}
