package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingIcons
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader
import com.segundoaire.presentation.onboarding.SvgIcon
import kotlinx.coroutines.launch

/** Una fila del plan: lo que se dice (k), su valor (v) y su ícono. */
internal class PlanRow(val key: String, val value: String, val icon: String)

@Composable
internal fun PlanText(step: Int, total: Int, name: String) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_plan_title, name),
        subtitle = stringResource(R.string.onb_plan_subtitle)
    )
}

/** Tarjeta de vidrio con cuatro filas que entran cada 90 ms al llegar al paso ([active]). */
@Composable
internal fun PlanContent(active: Boolean, rows: List<PlanRow>, onContinue: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val entrance = remember { List(rows.size) { Animatable(0f) } }

    LaunchedEffect(active) {
        if (!active) {
            entrance.forEach { it.snapTo(0f) }
            return@LaunchedEffect
        }
        entrance.forEachIndexed { i, row ->
            launch { row.animateTo(1f, motion.rise(520, OnboardingRules.planRowDelayMillis(i))) }
        }
    }

    GlassCard(
        modifier = Modifier
            .offset(x = StepContentSide, y = 264.dp)
            .width(358.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        rows.forEachIndexed { index, row ->
            val divider = colors.outlineVariant
            val last = index == rows.lastIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val p = entrance[index].value.coerceIn(0f, 1f)
                        alpha = p
                        translationY = (1f - p) * 6.dp.toPx()
                    }
                    .drawBehind {
                        if (!last) {
                            drawLine(divider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                        }
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(colors.primary.copy(alpha = 0.12f), SegundoAireTheme.shapes.radiusSm),
                    contentAlignment = Alignment.Center
                ) {
                    SvgIcon(row.icon, colors.primary, size = 22.dp)
                }
                Column(Modifier.weight(1f)) {
                    Text(text = row.key, style = typography.labelMd, color = colors.onBackgroundMuted)
                    Text(text = row.value, style = typography.titleMd.copy(lineHeight = 22.sp), color = colors.onBackground)
                }
            }
        }
    }
    StepButton(text = stringResource(R.string.onb_plan_accept), onClick = onContinue)
}

/** Las filas del plan según lo que se respondió; sin metas, "Vivir con más calma". */
@Composable
internal fun planRows(goals: List<String>, momentBlock: String, strict: Boolean): List<PlanRow> = listOf(
    PlanRow(
        key = stringResource(R.string.onb_plan_goals),
        value = if (goals.isEmpty()) stringResource(R.string.onb_plan_goals_default) else goals.joinToString(" · "),
        icon = OnboardingIcons.Leaf
    ),
    PlanRow(stringResource(R.string.onb_plan_block), momentBlock, OnboardingIcons.Sun),
    PlanRow(
        // Mindful y Strict no se traducen
        key = stringResource(R.string.onb_plan_mode, if (strict) "Strict" else "Mindful"),
        value = stringResource(if (strict) R.string.onb_plan_strict_value else R.string.onb_plan_mindful_value),
        icon = OnboardingIcons.Flame
    ),
    PlanRow(
        stringResource(R.string.onb_plan_daily),
        stringResource(R.string.onb_plan_daily_value),
        OnboardingIcons.Drop
    )
)
