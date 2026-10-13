package com.segundoaire.presentation.designsystem.catalog

import androidx.navigation.NavGraphBuilder

/** `true` solo en builds debug: el catálogo no existe en release. */
const val ComponentCatalogAvailable = false

const val ComponentCatalogRoute = "debug/component-catalog"

@Suppress("UnusedReceiverParameter")
fun NavGraphBuilder.componentCatalog() = Unit
