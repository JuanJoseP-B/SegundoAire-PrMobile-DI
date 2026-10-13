package com.segundoaire.data.apps

import com.segundoaire.domain.model.InstalledApp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppsRepositoryImplTest {

    private class FakeLauncherAppSource(
        override val ownPackageName: String = "com.segundoaire",
        var entries: List<LauncherEntry> = emptyList()
    ) : LauncherAppSource {
        var queryCount = 0

        override suspend fun queryLauncherEntries(): List<LauncherEntry> {
            queryCount++
            return entries
        }
    }

    private val source = FakeLauncherAppSource()
    private val repository = InstalledAppsRepositoryImpl(source)

    @Test
    fun `sin apps con lanzador devuelve una lista vacia`() = runTest {
        assertTrue(repository.getLaunchableApps().isEmpty())
    }

    @Test
    fun `excluye la propia app`() = runTest {
        source.entries = listOf(
            LauncherEntry("com.segundoaire", "SegundoAire"),
            LauncherEntry("com.example.news", "Noticias")
        )

        val apps = repository.getLaunchableApps()

        assertEquals(listOf("com.example.news"), apps.map { it.packageName })
    }

    @Test
    fun `una app con varias actividades de lanzador aparece una vez`() = runTest {
        source.entries = listOf(
            LauncherEntry("com.example.mail", "Correo"),
            LauncherEntry("com.example.mail", "Correo (cuenta de trabajo)")
        )

        val apps = repository.getLaunchableApps()

        assertEquals(listOf(InstalledApp("com.example.mail", "Correo")), apps)
    }

    @Test
    fun `ordena por nombre sin distinguir mayusculas`() = runTest {
        source.entries = listOf(
            LauncherEntry("com.example.zeta", "zeta"),
            LauncherEntry("com.example.beta", "Beta"),
            LauncherEntry("com.example.alfa", "alfa")
        )

        val names = repository.getLaunchableApps().map { it.appName }

        assertEquals(listOf("alfa", "Beta", "zeta"), names)
    }

    @Test
    fun `sin etiqueta legible usa el paquete como nombre`() = runTest {
        source.entries = listOf(LauncherEntry("com.example.unnamed", "   "))

        val apps = repository.getLaunchableApps()

        assertEquals("com.example.unnamed", apps.single().appName)
    }

    @Test
    fun `recorta los espacios de la etiqueta`() = runTest {
        source.entries = listOf(LauncherEntry("com.example.pad", "  Relojes  "))

        assertEquals("Relojes", repository.getLaunchableApps().single().appName)
    }
}
