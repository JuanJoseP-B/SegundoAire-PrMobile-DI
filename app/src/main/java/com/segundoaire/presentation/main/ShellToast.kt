package com.segundoaire.presentation.main

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import kotlinx.coroutines.delay

private val ToastRise = 12.dp

/**
 * Aviso breve sobre la barra inferior: vidrio, entra subiendo 12 dp, se queda y se va,
 * 2,6 s en total. TalkBack lo lee como estado. Al terminar avisa con [onDone].
 */
@Composable
fun ShellToast(
    message: ShellToastMessage?,
    onDone: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = SegundoAireTheme.motion
    // En la preview no corren las animaciones: el aviso se ve ya entrado
    val inspection = LocalInspectionMode.current
    val progress = remember { Animatable(if (inspection) 1f else 0f) }
    val done by rememberUpdatedState(onDone)
    val risePx = with(LocalDensity.current) { ToastRise.toPx() }

    LaunchedEffect(message?.id) {
        val current = message ?: return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, motion.rise(Motion.ToastInMillis))
        delay(Motion.ToastMillis - Motion.ToastInMillis - Motion.ToastOutMillis)
        progress.animateTo(0f, motion.fade(Motion.ToastOutMillis))
        done(current.id)
    }

    if (message == null) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SegundoAireTheme.spacing.space6)
            .graphicsLayer {
                alpha = progress.value
                translationY = if (motion.reduced) 0f else risePx * (1f - progress.value)
            }
            .glass(GlassLevel.Level2, SegundoAireTheme.shapes.radiusMd)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = SegundoAireTheme.spacing.space4, vertical = SegundoAireTheme.spacing.space3)
    ) {
        Text(
            text = message.text,
            style = SegundoAireTheme.typography.labelLg.copy(fontSize = 14.sp),
            color = SegundoAireTheme.colors.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ShellToastSample() {
    Box(Modifier.background(SegundoAireTheme.colors.background).padding(vertical = SegundoAireTheme.spacing.space6)) {
        ShellToast(message = ShellToastMessage(stringResource(R.string.coach_toast_remind), 1), onDone = {})
    }
}

@Preview(name = "ShellToast · claro")
@Composable
private fun ShellToastLightPreview() {
    SegundoAireTheme(darkTheme = false) { ShellToastSample() }
}

@Preview(name = "ShellToast · oscuro")
@Composable
private fun ShellToastDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ShellToastSample() }
}
