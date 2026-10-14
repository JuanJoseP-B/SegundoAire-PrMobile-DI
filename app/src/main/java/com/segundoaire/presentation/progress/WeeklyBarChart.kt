package com.segundoaire.presentation.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import java.time.LocalDate
import java.time.format.TextStyle as DayNameStyle

private val ChartHeight = 128.dp
private val BarMaxWidth = 28.dp

// Parte de cada columna que ocupa su barra; el resto es aire entre barras
private const val BarSlotFraction = 0.55f

/**
 * Gráfico de barras de la semana: una barra por día de [days], del más antiguo
 * a hoy. Las barras tienen `radiusSm` solo arriba; [today] va en `glow` y el resto
 * en `primary`. La altura entra con `Motion.calm` y, con reducir movimiento, se
 * dibuja directamente. TalkBack lee el día y los minutos de cada barra.
 */
@Composable
fun WeeklyBarChart(
    days: List<DayMinutes>,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    val barShape = SegundoAireTheme.shapes.radiusSm.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize
    )
    val maxMinutes = (days.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)

    // Las barras crecen desde cero la primera vez que el gráfico se compone
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val fractions = days.map { day ->
        val fraction = day.minutes / maxMinutes.toFloat()
        if (motion.reduced) {
            fraction
        } else {
            animateFloatAsState(
                targetValue = if (entered) fraction else 0f,
                animationSpec = motion.calm(),
                label = "weekBar"
            ).value
        }
    }

    val description = days.map { day ->
        pluralStringResource(
            R.plurals.progress_week_day,
            day.minutes,
            day.date.dayOfWeek.getDisplayName(DayNameStyle.FULL, locale),
            day.minutes
        )
    }.joinToString()

    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight)
        ) {
            if (days.isEmpty()) return@Canvas
            val slot = size.width / days.size
            val barWidth = minOf(slot * BarSlotFraction, BarMaxWidth.toPx())
            days.forEachIndexed { index, day ->
                val barHeight = size.height * fractions[index].coerceIn(0f, 1f)
                if (barHeight <= 0f) return@forEachIndexed
                // En RTL hoy queda a la izquierda, igual que su etiqueta
                val column = if (layoutDirection == LayoutDirection.Rtl) days.lastIndex - index else index
                val left = slot * column + (slot - barWidth) / 2
                translate(left = left, top = size.height - barHeight) {
                    drawOutline(
                        outline = barShape.createOutline(Size(barWidth, barHeight), layoutDirection, this),
                        color = if (day.date == today) colors.glow else colors.primary
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { day ->
                val isToday = day.date == today
                Text(
                    text = day.date.dayOfWeek.getDisplayName(DayNameStyle.NARROW, locale),
                    style = if (isToday) typography.labelMd else typography.labelSm,
                    color = if (isToday) colors.onBackground else colors.onBackgroundMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private val PreviewToday: LocalDate = LocalDate.of(2026, 10, 14)

private val PreviewWeek = listOf(40, 30, 60, 0, 55, 45, 25).mapIndexed { index, minutes ->
    DayMinutes(PreviewToday.minusDays(6L - index), minutes)
}

@Composable
private fun WeeklyBarChartSample() {
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space6)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            WeeklyBarChart(days = PreviewWeek, today = PreviewToday, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(name = "WeeklyBarChart · claro")
@Composable
private fun WeeklyBarChartLightPreview() {
    SegundoAireTheme(darkTheme = false) { WeeklyBarChartSample() }
}

@Preview(name = "WeeklyBarChart · oscuro")
@Composable
private fun WeeklyBarChartDarkPreview() {
    SegundoAireTheme(darkTheme = true) { WeeklyBarChartSample() }
}
