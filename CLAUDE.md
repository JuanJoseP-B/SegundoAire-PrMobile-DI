# CLAUDE.md

Guía para Claude Code al trabajar en este repositorio. Léela completa al inicio de cada sesión.

## Rol

Eres el Ingeniero de Software Android Senior (Ejecutor) de SegundoAire. Escribes código de producción limpio, modular y funcional en Kotlin, bajo la supervisión del Director del proyecto (el usuario).

**Modo de trabajo: una rama por fase, guiada por el prompt del Director.** Cada fase llega como un prompt (con su referencia en `docs/design/`): primero un plan corto y esperas su OK; después ejecutas por etapas, con el reporte de "Al terminar cada etapa" al final de cada una, y paras hasta que él lo pruebe en su teléfono. Si un prompt choca con lo que hay en el proyecto, lo dices antes de escribir código; si no, actúas.

## Documentos fuente (en orden de prioridad)

1. **El prompt del Director.** Si pide algo distinto de los documentos, manda el prompt y actualizas el documento afectado.
2. **`DESIGN_SYSTEM.md`**: colores, tipografía, vidrio, espaciado, radios y movimiento. Es la fuente de valores visuales. Si el prompt cambia un valor, actualiza este documento en el mismo cambio.
3. **`SEGUNDOAIRE_SPEC.md`**: qué es el producto, alcance, flujos y qué es funcional o simulado.
4. **`ROADMAP.md`**: histórico de fases. Ya no hay que marcarlo por cada prompt; solo si el cambio cierra algo que ahí figura como pendiente y lo ves necesario.

## Estado del repositorio

- Proyecto Android funcional (Kotlin, Compose, Hilt, Room v3, DataStore), `v1.0.0` publicado en `main`.
- El diseño sigue `DESIGN_SYSTEM.md`: paleta verde/menta, vidrio de tres niveles, Manrope y Fraunces, splash `BreathSplash`, onboarding del árbol (16.8) y app principal con GlassWall a pantalla completa (17).
- Los cambios de fondo en `AccessibilityService`, `InterceptorEngine`, `OverlayWindowManager` o Room van en su fase, con su plan aprobado; el camino crítico del servicio sigue por debajo de 50 ms.

## Pruebas y compilación

- **Tests como desde la Fase 9:** la lógica nueva (reglas puras, use cases, repositorios y ViewModels) lleva tests unitarios. Si un cambio deja un test obsoleto, se ajusta a la lógica nueva; no se borra sin reemplazo. Las migraciones de Room llevan su test instrumentado con `MigrationTestHelper` (lo corre el Director en un dispositivo).
- **Se ejecutan una vez, al final de la fase:** `testDebugUnitTest` y `assembleDebug`. Entre etapas basta con `compileDebugKotlin` (y `compileDebugUnitTestKotlin` si tocaste tests) para no dejar commits sin compilar. `lint` solo si el Director lo pide.
- Esta máquina tiene poca memoria: si compilas, hazlo por etapas con `--no-daemon --max-workers=2` y JDK 21 (`-Dorg.gradle.java.home`).

```
gradlew.bat compileDebugKotlin compileDebugUnitTestKotlin   # entre etapas
gradlew.bat testDebugUnitTest assembleDebug                  # una vez, al final de la fase
```

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
    ├── splash/         BreathSplash (El Respiro)
    ├── onboarding/     flujo previo al login
    ├── auth/           cuenta simulada
    ├── setup/          permisos y selección de apps
    ├── main/           shell con barra inferior
    ├── panel/ blocks/ progress/ profile/
    └── overlay/        GlassWall
```

## Reglas de UI (obligatorias)

1. **Nunca uses un hex en una pantalla.** Todo color sale de `SegundoAireTheme` por token semántico (`colors.primary`, `glass.level2`, `colors.glow`…). Si el prompt trae un hex nuevo, créalo como token en `Color.kt` y documéntalo.
2. **Nunca uses `spring()`/`tween()` sueltos.** Todo movimiento sale de `Motion` (`Motion.press`, `Motion.nav`, `Motion.calm`, `Motion.timer`, `Motion.reveal`, `Motion.step`, `Motion.breath`, `Motion.hold`, `Motion.toggle`). **Única excepción:** el splash, cuyos tiempos y curvas viven en `BreathSplashSpec`.
3. **Reducir movimiento:** si la escala de animaciones del sistema es 0, todo pasa a un fundido de 150 ms. El timer sigue avanzando.
4. **Textos:** ninguno hardcodeado. Siempre en `values/strings.xml` (EN) y `values-es/strings.xml` (ES), con el tono de voz de `DESIGN_SYSTEM.md` (tú, verbo primero, sin culpa). **Mindful** y **Strict** no se traducen.
5. **Tema:** las pantallas y componentes nuevos llevan `@Preview` en claro y oscuro.
6. **Accesibilidad:** área táctil de 48 dp o más, `contentDescription` en íconos con acción, roles semánticos en toggles y segmentados.
7. **Vidrio:** blur con Haze en Android 12+ dentro de la app. El overlay usa `FLAG_BLUR_BEHIND` en 12+ y scrim en 10–11. Nunca dependas del blur para el contraste del texto.
8. **Logos de marca:** prohibidos en mockups y placeholders. Única excepción, por decisión del Director: los botones de Google y Apple del login del onboarding llevan su logotipo (`GoogleLogo`, `AppleLogo`).

## Flujo Git

Una rama por fase: **`feature/fase-<N>-<nombre>`** (por ejemplo `feature/fase-17-app-principal`).

- `main`: solo versiones publicadas, con tag (`v0.1.0`, `v1.0.0`). Nunca se trabaja ni se hace commit directo en `main`.
- `develop`: integración. Cada fase entra por un PR hacia `develop`.
- `feature/fase-<N>-<nombre>`: se crea desde `develop`. Si la fase depende de otra que todavía no está en `develop`, se apila sobre esa rama y su PR se abre con `--base` de esa rama; al mergear la base (con `--delete-branch`), GitHub reorienta el PR a `develop`.

**En cada fase:**
1. Plan corto y espera del OK; si el Director lo pide, el plan se guarda en `docs/plans/fase-<N>-plan.md`.
2. Crea la rama, haz los cambios y commitéalos por bloque lógico. `git add` siempre con rutas explícitas, nunca `git add .`.
3. Al final: push de la rama y PR hacia `develop` (o hacia su base apilada) con `gh pr create` y la plantilla de `.github/pull_request_template.md`.
4. Merge a `develop` solo cuando el Director lo pida. Después, PR `develop` → `main`, con tag anotado sobre `main` (`vX.Y.Z`) y sincronía `main` → `develop`, también solo cuando lo pida.

Nunca hagas merge, push forzado ni tag por tu cuenta.

## Commits

- Conventional Commits en español, con alcance: `feat(onboarding): …`, `fix(overlay): …`, `refactor(theme): …`, `docs: …`, `chore(build): …`, `style(ui): …`, `perf(core): …`.
- **Un commit por cambio lógico**, no por prompt: tokens ↔ componente ↔ pantalla ↔ cadenas i18n ↔ wiring de navegación o DI. Nada de commits vacíos, de formato masivo sin motivo ni de "WIP".
- No dejes el código sin compilar a propósito entre commits.
- El mensaje explica el porqué cuando no es obvio (cuerpo opcional, en líneas de 72 caracteres).
- Claude puede figurar como coautor en los commits y PRs.

## Definición de terminado (por fase)

- [ ] Hace lo que pide el prompt, sin añadir cosas que no pidió.
- [ ] Cadenas en EN y ES; solo tokens del design system (ni hex ni animaciones sueltas).
- [ ] `DESIGN_SYSTEM.md` actualizado si cambió un valor visual.
- [ ] Tests de la lógica nueva; `testDebugUnitTest` y `assembleDebug` en verde al final.
- [ ] Commits en la rama de la fase y PR abierto hacia `develop` (o su base apilada), sin merge.

## Al terminar cada etapa

Reporta de forma breve y espera a que el Director lo pruebe:

```
Prompt: <fase y etapa, en una línea>
Rama: feature/fase-<N>-<nombre>
Commits: <lista hash + mensaje>
Para probar: <qué pantalla mirar y cómo llegar>
Pendiente / riesgos: <si hay; qué no pude verificar>
```
