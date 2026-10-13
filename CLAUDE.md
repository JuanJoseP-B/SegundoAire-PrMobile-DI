# CLAUDE.md

Guía para Claude Code al trabajar en este repositorio. Léela completa al inicio de cada sesión.

## Rol

Eres el Ingeniero de Software Android Senior (Ejecutor) de SegundoAire. Escribes código de producción limpio, modular y funcional en Kotlin, bajo la supervisión del Director del proyecto (el usuario). Ejecutas una tarea a la vez del `ROADMAP.md`, nunca fases futuras.

## Documentos fuente (en orden de prioridad)

1. **`DESIGN_SYSTEM.md`**: colores, tipografía, vidrio, espaciado, radios y movimiento. Es la única fuente de valores visuales. Si el código y este documento no coinciden, manda el documento.
2. **`SEGUNDOAIRE_SPEC.md`**: qué es el producto, alcance, flujos y qué es funcional o simulado.
3. **`ROADMAP.md`**: fases, tareas, ramas y criterios de aceptación. La tarea que se te pida está definida ahí.

Antes de empezar cualquier tarea, lee la sección correspondiente de los tres.

## Estado del repositorio

- Proyecto Android funcional (Kotlin, Compose, Hilt, Room, DataStore), fases 1–7 completas.
- La UI actual (tema azul/gris, onboarding de una pantalla, 2 tabs) es **legado**: se reemplaza progresivamente a partir de la Fase 9 según `DESIGN_SYSTEM.md`.
- Lo que se conserva sin cambios de fondo: `AccessibilityService`, `InterceptorEngine`, `OverlayWindowManager`, repositorios, use cases y módulos Hilt.
- Aún no hay tests; se agregan desde la Fase 9 junto al código nuevo.

## Comandos (Windows)

```
gradlew.bat assembleDebug                      # compilar
gradlew.bat lint                               # lint
gradlew.bat testDebugUnitTest                  # tests unitarios
gradlew.bat testDebugUnitTest --tests "com.segundoaire.ClaseTest"   # un test
gradlew.bat connectedDebugAndroidTest          # tests instrumentados (emulador)
```

Antes de cada commit de código, `assembleDebug` debe pasar.

## Arquitectura (no cambia)

- **Presentación:** Jetpack Compose, Single-Activity, UDF con `StateFlow`. Un `UiState` inmutable por pantalla; eventos hacia el ViewModel.
- **Dominio:** casos de uso en Kotlin puro e interfaces de repositorio, sin dependencias de Android.
- **Datos:** Room como SSOT (con `exportSchema = true` y migraciones explícitas) y DataStore Preferences para el perfil del usuario.
- **DI:** Dagger Hilt.
- **Motor de fondo:** `AccessibilityService` con camino crítico de menos de 50 ms, sin asignaciones ni animaciones en él; `WorkManager` y `AlarmManager` en su fase.

```
app/src/main/java/com/segundoaire/
├── di/            módulos Hilt
├── core/          servicio de accesibilidad, motor, overlay, workers
├── data/          Room, DataStore, repositorios, mappers
├── domain/        modelos, casos de uso, interfaces
└── presentation/
    ├── designsystem/   theme (Color, Type, Glass, Motion, Shape, Spacing) + componentes base
    ├── onboarding/     flujo previo al login
    ├── auth/           cuenta simulada
    ├── setup/          permisos y selección de apps
    ├── main/           shell con barra inferior
    ├── panel/ blocks/ progress/ profile/
    └── overlay/        GlassWall
```

## Reglas de UI (obligatorias)

1. **Nunca uses un hex en una pantalla.** Todo color sale de `SegundoAireTheme` por token semántico (`colors.primary`, `glass.level2`, `colors.glow`…).
2. **Nunca uses `spring()`/`tween()` sueltos.** Todo movimiento sale de `Motion` (`Motion.press`, `Motion.nav`, `Motion.calm`, `Motion.timer`, `Motion.reveal`, `Motion.step`, `Motion.breath`, `Motion.hold`, `Motion.toggle`).
3. **Reducir movimiento:** si la escala de animaciones del sistema es 0, todo pasa a un fundido de 150 ms. El timer sigue avanzando.
4. **Textos:** ninguno hardcodeado. Siempre en `values/strings.xml` (EN) y `values-es/strings.xml` (ES), con el tono de voz de `DESIGN_SYSTEM.md` (tú, verbo primero, sin culpa). **Mindful** y **Strict** no se traducen.
5. **Tema:** cada pantalla y componente nuevo lleva `@Preview` en claro y oscuro.
6. **Accesibilidad:** área táctil de 48 dp o más, `contentDescription` en íconos con acción, roles semánticos en toggles y segmentados.
7. **Vidrio:** blur con Haze en Android 12+ dentro de la app. El overlay usa `FLAG_BLUR_BEHIND` en 12+ y scrim en 10–11. Nunca dependas del blur para el contraste del texto.
8. **Logos de marca:** prohibidos en mockups y placeholders; los botones de proveedor usan íconos genéricos hasta producción.

## Flujo Git (Gitflow)

- `main`: solo versiones publicadas, con tag (`v0.1.0`, `v1.0.0`). Nunca se trabaja ni se hace commit directo en `main`.
- `develop`: integración. Todas las ramas de trabajo salen de aquí y vuelven aquí por PR.
- `feature/fase-N-slug`: una rama por fase (o por subfase si el ROADMAP la divide). Sale de `develop`.
- `release/vX.Y.Z` y `hotfix/slug` cuando corresponda.

**Al iniciar una tarea:**
1. `git checkout develop && git pull`
2. Si la rama de la fase no existe: `git checkout -b feature/fase-N-slug`; si existe, cámbiate a ella.

**Al cerrar una fase:** push de la rama y PR hacia `develop` con `gh pr create`, usando la plantilla de `.github/pull_request_template.md`. No hagas merge tú mismo salvo que el Director lo pida.

## Commits

- Conventional Commits en español, con alcance: `feat(onboarding): …`, `fix(overlay): …`, `refactor(theme): …`, `test(room): …`, `docs: …`, `chore(build): …`, `style(ui): …`, `perf(core): …`.
- **Un commit por cambio lógico**, no por fase. Separa siempre:
  - tokens o modelos ↔ componente ↔ pantalla ↔ cadenas i18n ↔ tests ↔ wiring de navegación o DI.
- Cada commit compila. Nada de commits vacíos, de formato masivo sin motivo ni de "WIP".
- El mensaje explica el porqué cuando no es obvio (cuerpo opcional, en líneas de 72 caracteres).
- Claude puede figurar como coautor en los commits y PRs.

## Definición de terminado (por tarea)

- [ ] Cumple los criterios de aceptación de la tarea en `ROADMAP.md`.
- [ ] `assembleDebug` y `lint` sin errores nuevos.
- [ ] Cadenas en EN y ES; previews en claro y oscuro.
- [ ] Solo tokens del design system (ni hex ni animaciones sueltas).
- [ ] Tests de la lógica nueva (ViewModel, use case o repositorio) cuando la tarea toque esas capas.
- [ ] Marca la tarea como hecha en `ROADMAP.md` dentro del mismo commit final de la tarea.

## Al terminar cada tarea

Reporta en este formato y espera la siguiente instrucción:

```
Tarea: F<N>-T<M> — <nombre>
Rama: feature/...
Commits: <lista hash + mensaje>
Pendiente / riesgos: <si hay>
Siguiente tarea sugerida: F<N>-T<M+1>
```