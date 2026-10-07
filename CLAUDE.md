# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Estado del repositorio

Actualmente el repositorio solo contiene documentación (`README.md` y `SEGUNDOAIRE_SPEC.md`); todavía no existe código fuente, proyecto Gradle, ni configuración de build/lint/tests. Cuando se cree el proyecto Android, agregar aquí los comandos de build, lint y ejecución de un único test.

`SEGUNDOAIRE_SPEC.md` es la fuente de verdad del diseño y debe consultarse antes de implementar.

## Producto

SegundoAire: app nativa Android 10+ de bienestar digital. Intercepta (en <50 ms) la apertura de apps adictivas mediante un `AccessibilityService` y muestra un bloqueo visual superpuesto (`SYSTEM_ALERT_WINDOW`). Funciona 100% en local, sin backend.

## Arquitectura (Clean Architecture + MVVM)

- **Presentación:** Jetpack Compose, Single-Activity, flujo unidireccional (UDF) con `StateFlow`.
- **Dominio:** casos de uso puros en Kotlin e interfaces de repositorio (sin dependencias de Android).
- **Datos:** Room como SSOT + DataStore Preferences.
- **DI:** Dagger Hilt.
- **Motor de fondo:** `AccessibilityService` (evitar asignaciones/GC excesivo en el camino crítico de <50 ms), `WorkManager` para sincronización diferida y `AlarmManager` para sortear Doze Mode.

Estructura base bajo `app/src/main/java/com/segundoaire/`:
- `di/` — módulos Hilt
- `core/` — servicio de accesibilidad, Doze/batería, workers
- `data/` — Room, DataStore, repositorios, mappers
- `domain/` — modelos, casos de uso, interfaces
- `presentation/` — Compose, ViewModels, navegación, theme, onboarding

## Directrices de UI/UX

- Estética minimalista con Glassmorphism (superficies translúcidas, desenfoque de fondo).
- Dark Mode y Light Mode dinámicos; light mode es el predeterminado, con especial cuidado en contrastes del modo oscuro.
- Tipografía sans-serif geométrica con jerarquía clara de pesos para datos de impacto.
- i18n obligatorio desde el inicio: cadenas en inglés (`en`) y español (`es`); no hardcodear textos en la UI.

## Git commit conventions
- Never add `Co-Authored-By: Claude` or any AI assistant trailers to commit messages.
- Commits must only show the user as the sole author.

## <system_role>
Eres el Ingeniero de Software Android Senior (Ejecutor). Tu función es escribir código de producción limpio, modular y funcional en Kotlin. Estás bajo la supervisión de un Director de Operaciones.
</system_role>

<project_rules>
1. Antes de iniciar cualquier tarea, debes leer y asimilar el archivo `SEGUNDOAIRE_SPEC.md`.
2. Utiliza siempre Jetpack Compose para la UI y Dagger Hilt para inyección de dependencias.
3. Todo el código de UI debe prever soporte para Modo Oscuro/Claro y extraer las cadenas a `strings.xml`.
4. No asumas ni ejecutes fases futuras; limítate estrictamente a la fase solicitada por el usuario.
5. Tras completar una fase lógica, sugiere un commit descriptivo y prepárate para la siguiente instrucción.
</project_rules>

# WORKFLOW GIT: Toda fase nueva debe desarrollarse en una rama separada (ej. `feature/fase-7-temporizador`). Nunca trabajes directamente en `main`.

# COMMITS GRANULARES: Realiza aproximadamente 5 commits lógicos a lo largo de cada fase, dividiendo el trabajo por capas (ej. Dominio, Datos, UI, Integración). Usa Convencional Commits (feat:, fix:, refactor:, chore:).