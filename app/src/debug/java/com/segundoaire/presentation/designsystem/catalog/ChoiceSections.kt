package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.ChoiceChipGroup
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private const val SampleGoalLimit = 2

@Composable
internal fun ChoiceChipSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_choice_chip)) {
        val options = stringArrayResource(R.array.catalog_sample_goals).toList()
        var single by remember { mutableStateOf(setOf(0)) }
        var multiple by remember { mutableStateOf(setOf(1)) }

        CatalogCaption(stringResource(R.string.catalog_choice_single))
        ChoiceChipGroup(options = options, selected = single, onSelectionChange = { single = it }, maxSelected = 1)
        CatalogCaption(stringResource(R.string.catalog_choice_limited, SampleGoalLimit))
        ChoiceChipGroup(
            options = options,
            selected = multiple,
            onSelectionChange = { multiple = it },
            maxSelected = SampleGoalLimit
        )
    }
}

/** Nota corta que explica una variante dentro de una sección. */
@Composable
internal fun CatalogCaption(text: String) {
    Text(
        text = text,
        style = SegundoAireTheme.typography.bodyMd,
        color = SegundoAireTheme.colors.onBackground
    )
}
