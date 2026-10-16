package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.usecase.PasswordError
import com.segundoaire.domain.usecase.ValidatePasswordUseCase
import com.segundoaire.presentation.auth.EmailAuthField
import com.segundoaire.presentation.auth.EmailAuthUiState
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepTitle
import com.segundoaire.presentation.onboarding.StepSubtitle

internal enum class LoginPhase { Idle, Busy }

private val LoginButtonTop = 610.dp
private val BusyButtonSize = 52.dp
private val IdleButtonWidth = 358.dp
private const val SeedLeaf = "M12 3 C15 6 15 9 12 12 C9 9 9 6 12 3Z"
private const val SeedLeafLower = "M12 21 C9 18 9 15 12 12 C15 15 15 18 12 21Z"

/**
 * Entrada a la cuenta: correo y contraseña con "Crear cuenta", tres accesos genéricos y
 * "Continuar sin cuenta". Al pulsar, el botón se encoge a un círculo de 52 dp con una semilla
 * girando ([phase] es `Busy`). Debajo está el telón; esta capa solo pone el contenido.
 */
@Composable
internal fun LoginContent(
    state: EmailAuthUiState,
    phase: LoginPhase,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFieldBlur: (EmailAuthField) -> Unit,
    onCreate: () -> Unit,
    onWeb: () -> Unit,
    onDevice: () -> Unit,
    onMail: () -> Unit,
    onGuest: () -> Unit
) {
    val busy = phase == LoginPhase.Busy

    Column(
        Modifier
            .offset(x = 24.dp, y = 340.dp)
            .width(342.dp)
    ) {
        StepTitle(
            text = stringResource(R.string.onb_login_title),
            style = SegundoAireTheme.typography.serifTitle.copy(fontSize = 30.sp, lineHeight = 36.sp)
        )
        StepSubtitle(stringResource(R.string.onb_login_subtitle), Modifier.offset(y = 6.dp))
    }

    Column(
        Modifier
            .offset(x = StepContentSide, y = 430.dp)
            .width(358.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BlurAware(EmailAuthField.EMAIL, onFieldBlur) { source ->
            GlassTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.auth_email_label),
                placeholder = stringResource(R.string.onb_login_email_placeholder),
                errorMessage = if (state.showEmailError) stringResource(R.string.auth_email_error) else null,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                interactionSource = source
            )
        }
        BlurAware(EmailAuthField.PASSWORD, onFieldBlur) { source ->
            GlassTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.auth_password_label),
                placeholder = stringResource(R.string.onb_login_password_placeholder),
                errorMessage = when (state.passwordError) {
                    PasswordError.TOO_SHORT ->
                        stringResource(R.string.auth_password_error_short, ValidatePasswordUseCase.MIN_LENGTH)
                    PasswordError.MISSING_LETTER -> stringResource(R.string.auth_password_error_letter)
                    PasswordError.MISSING_NUMBER -> stringResource(R.string.auth_password_error_number)
                    null -> null
                },
                isPassword = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCreate() }),
                interactionSource = source
            )
        }
    }

    CreateAccountButton(busy = busy, onClick = onCreate)

    Row(
        Modifier
            .offset(y = 700.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        ProviderIconButton(SegundoAireIcons.Globe, stringResource(R.string.onb_login_web), !busy, onWeb)
        ProviderIconButton(SegundoAireIcons.Device, stringResource(R.string.onb_login_device), !busy, onDevice)
        ProviderIconButton(SegundoAireIcons.Envelope, stringResource(R.string.onb_login_mail), !busy, onMail)
    }

    Box(Modifier.offset(y = 770.dp).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        TextButton(
            text = stringResource(R.string.onb_login_guest),
            onClick = onGuest,
            enabled = !busy,
            modifier = Modifier.width(200.dp).height(48.dp)
        )
    }
}

/** Avisa con [onBlur] cuando el campo pierde el foco, para que muestre su error recién entonces. */
@Composable
private fun BlurAware(
    field: EmailAuthField,
    onBlur: (EmailAuthField) -> Unit,
    content: @Composable (MutableInteractionSource) -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    var wasFocused by remember { mutableStateOf(false) }
    LaunchedEffect(focused) {
        if (wasFocused && !focused) onBlur(field)
        wasFocused = focused
    }
    content(source)
}

@Composable
private fun CreateAccountButton(busy: Boolean, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val source = remember { MutableInteractionSource() }
    val width = animateDpAsState(
        targetValue = if (busy) BusyButtonSize else IdleButtonWidth,
        animationSpec = motion.bloom(true, 500),
        label = "createWidth"
    )
    val label = stringResource(R.string.onb_login_create)
    val connecting = stringResource(R.string.onb_login_connecting)
    val spin: State<Float>? = if (busy && !motion.reduced) {
        rememberInfiniteTransition(label = "spin").animateFloat(0f, 360f, Motion.spin(), label = "seed")
    } else {
        null
    }

    Box(
        Modifier
            .offset(y = LoginButtonTop)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier
                .width(width.value)
                .height(BusyButtonSize)
                .pressScale(source, enabled = !busy)
                .clip(SegundoAireTheme.shapes.radiusFull)
                .drawBehind { drawRect(colors.primary) }
                .clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = !busy,
                    role = Role.Button,
                    onClick = onClick
                )
                .semantics {
                    contentDescription = label
                    if (busy) stateDescription = connecting
                },
            contentAlignment = Alignment.Center
        ) {
            if (busy) {
                Seed(Modifier.graphicsLayer { rotationZ = spin?.value ?: 0f })
            } else {
                androidx.compose.material3.Text(
                    text = label,
                    style = SegundoAireTheme.typography.labelLg,
                    color = colors.onPrimary
                )
            }
        }
    }
}

/** Semilla del loader: dos hojas, la de abajo más tenue. */
@Composable
private fun Seed(modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val upper = remember { PathParser().parsePathString(SeedLeaf).toPath() }
    val lower = remember { PathParser().parsePathString(SeedLeafLower).toPath() }
    androidx.compose.foundation.Canvas(modifier.size(22.dp)) {
        scale(this.size.minDimension / 24f, Offset.Zero) {
            drawPath(upper, colors.onPrimary)
            drawPath(lower, colors.onPrimary.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ProviderIconButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    val shape = SegundoAireTheme.shapes.radiusFull
    Box(
        Modifier
            .size(48.dp)
            .pressScale(source, enabled)
            .glass(GlassLevel.Level2, shape)
            .border(1.dp, colors.outlineVariant, shape)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
    }
}
