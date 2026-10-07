# SEGUNDOAIRE - Project Specification

## 1. Visión del Producto
SegundoAire es una aplicación nativa para Android 10+ orientada al bienestar digital. Su función principal es interceptar la apertura de aplicaciones adictivas en menos de 50ms mediante un `AccessibilityService`, desplegando un bloqueo visual superpuesto (`SYSTEM_ALERT_WINDOW`). Opera 100% en local y enfatiza una Experiencia de Usuario (UX) premium y emocional.

## 2. Directrices de UI/UX
- **Estética:** Minimalista, utilizando principios de *Glassmorphism* (superficies translúcidas, desenfoque de fondo).
- **Temas:** Soporte nativo y dinámico para Dark Mode y Light Mode (con un enfoque meticuloso en los contrastes del modo oscuro, light mode predeterminado).
- **Tipografía:** Sans-serif geométrica, limpia, con pesos jerárquicos claros para los datos de impacto.
- **Internacionalización (i18n):** Todo el sistema debe soportar cadenas en Inglés (`en`) y Español (`es`) desde el día cero.

## 3. Arquitectura del Sistema (Clean Architecture + MVVM)
- **Presentación:** Jetpack Compose (Single-Activity), Unidirectional Data Flow (UDF) con `StateFlow`.
- **Dominio:** Casos de uso puros en Kotlin, interfaces de repositorio.
- **Datos:** Room Database (SSOT) y DataStore Preferences.
- **Inyección de Dependencias:** Dagger Hilt.
- **Motor de Fondo:** `AccessibilityService` (optimizado sin recolección de basura excesiva), `WorkManager` (sincronización diferida), y `AlarmManager` (para sortear el Doze Mode).

## 4. Estructura de Directorios (Base)
`/app/src/main/java/com/segundoaire/`
 ├── `di/` (Módulos Hilt)
 ├── `core/` (Servicio Accesibilidad, Doze/Batería, Workers)
 ├── `data/` (Room, DataStore, Repositorios, Mappers)
 ├── `domain/` (Modelos, Casos de Uso, Interfaces)
 └── `presentation/` (Compose, ViewModels, Navegación, Theme, Onboarding)