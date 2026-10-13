package com.segundoaire.presentation.designsystem.catalog

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** `true` solo en builds debug: el catálogo no existe en release. */
const val ComponentCatalogAvailable = true

const val ComponentCatalogRoute = "debug/component-catalog"

fun NavGraphBuilder.componentCatalog() {
    composable(ComponentCatalogRoute) { ComponentCatalogScreen() }
}
