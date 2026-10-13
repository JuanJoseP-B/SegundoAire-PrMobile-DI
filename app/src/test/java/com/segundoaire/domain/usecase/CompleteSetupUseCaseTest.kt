package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.testing.FakeBlockedAppRepository
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class CompleteSetupUseCaseTest {

    private val blockedApps = FakeBlockedAppRepository()
    private val profile = FakeUserProfileRepository()
    private val useCase = CompleteSetupUseCase(blockedApps, profile)

    private val videos = InstalledApp("com.example.videos", "Videos")
    private val social = InstalledApp("com.example.social", "Social")

    @Test
    fun `guarda la seleccion como apps bloqueadas con el cooldown por defecto`() = runTest {
        useCase(listOf(videos, social))

        assertEquals(
            listOf(
                BlockedApp("com.example.videos", "Videos"),
                BlockedApp("com.example.social", "Social")
            ),
            blockedApps.current
        )
    }

    @Test
    fun `marca el setup como completo`() = runTest {
        useCase(listOf(videos))

        assertTrue(profile.saved?.setupCompleted == true)
    }

    @Test
    fun `reemplaza la lista anterior incluidas las apps de ejemplo`() = runTest {
        blockedApps.upsert(BlockedApp("com.instagram.android", "Instagram"))

        useCase(listOf(videos))

        assertEquals(listOf("com.example.videos"), blockedApps.current.map { it.packageName })
    }

    @Test
    fun `una app que ya estaba conserva su cooldown y vuelve a quedar bloqueada`() = runTest {
        blockedApps.upsert(BlockedApp(videos.packageName, "Viejo nombre", cooldownMinutes = 15, isBlocked = false))

        useCase(listOf(videos))

        assertEquals(
            listOf(BlockedApp(videos.packageName, "Videos", cooldownMinutes = 15, isBlocked = true)),
            blockedApps.current
        )
    }

    @Test
    fun `ignora paquetes repetidos`() = runTest {
        useCase(listOf(videos, videos.copy(appName = "Videos 2")))

        assertEquals(1, blockedApps.current.size)
    }

    @Test
    fun `si Room falla no marca el setup como completo`() = runTest {
        blockedApps.failNextReplace = true

        try {
            useCase(listOf(videos))
            fail("Debía propagar el error de guardado")
        } catch (_: IOException) {
            // esperado
        }

        assertFalse(profile.saved?.setupCompleted == true)
        assertEquals(0, profile.saveCount)
    }
}
