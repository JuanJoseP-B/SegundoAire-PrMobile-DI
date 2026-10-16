# CLAUDE.md

Guía para Claude Code al trabajar en este repositorio. Léela completa al inicio de cada sesión.

## Rol

Eres el Ingeniero de Software Android Senior (Ejecutor) de SegundoAire. Escribes código de producción limpio, modular y funcional en Kotlin, bajo la supervisión del Director del proyecto (el usuario).

**Modo de trabajo actual (desde la Fase 17): ajustes de diseño por prompts.** El Director te pasa prompts con cambios de diseño y los pruebas él en su teléfono. Ya no se ejecuta el ROADMAP tarea por tarea: cada prompt es la tarea. Si un prompt choca con lo que hay en el proyecto, lo dices antes de escribir código; si no, actúas.

## Documentos fuente (en orden de prioridad)

1. **El prompt del Director.** Si pide algo distinto de los documentos, manda el prompt y actualizas el documento afectado.
2. **`DESIGN_SYSTEM.md`**: colores, tipografía, vidrio, espaciado, radios y movimiento. Es la fuente de valores visuales. Si el prompt cambia un valor, actualiza este documento en el mismo cambio.
3. **`SEGUNDOAIRE_SPEC.md`**: qué es el producto, alcance, flujos y qué es funcional o simulado.
4. **`ROADMAP.md`**: histórico de fases. Ya no hay que marcarlo por cada prompt; solo si el cambio cierra algo que ahí figura como pendiente y lo ves necesario.

## Estado del repositorio

- Proyecto Android funcional (Kotlin, Compose, Hilt, Room, DataStore), fases 1–17 completas y `v1.0.0` publicado en `main`.
- El diseño actual sigue `DESIGN_SYSTEM.md`: paleta verde/menta, vidrio de tres niveles, Manrope, splash El Respiro (`BreathSplash`).
- Se conservan sin cambios de fondo: `AccessibilityService`, `InterceptorEngine`, `OverlayWindowManager`, repositorios, use cases y módulos Hilt.

## Pruebas y compilación

- **No se escriben tests nuevos ni se ejecutan** `testDebugUnitTest`, `lint` ni tests instrumentados. El Director prueba en el teléfono. Los tests que ya existen se dejan como están; si un cambio de diseño deja alguno obsoleto, bórralo o ajústalo sin correrlo.
- **Tampoco se compila por rutina**: el Director compila al probar. Si el cambio es grande o arriesgado (tipos, firmas, dependencias, varios archivos), puedes correr `compileDebugKotlin` una vez antes de entregar.
- Esta máquina tiene poca memoria: si compilas, hazlo por etapas con `--no-daemon --max-workers=2` y JDK 21 (`-Dorg.gradle.java.home`).

```
gradlew.bat compileDebugKotlin     # solo compilar Kotlin (opcional, cambios riesgosos)
gradlew.bat assembleDebug          # APK de debug (lo corre el Director)
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
8. **Logos de marca:** prohibidos en mockups y placeholders; los botones de proveedor usan íconos genéricos hasta producción.

## Flujo Git

Una sola rama de trabajo para todo el diseño: **`feature/diseno`**.

- `main`: solo versiones publicadas, con tag (`v0.1.0`, `v1.0.0`). Nunca se trabaja ni se hace commit directo en `main`.
- `develop`: integración.
- `feature/diseno`: todos los cambios de diseño de los prompts van aquí, uno tras otro, sin una rama por prompt.

**En cada prompt:**
1. `git checkout feature/diseno` (si no existe, créala desde `develop` o desde la rama que indique el Director).
2. Haz el cambio y commitéalo en esa rama. No abras un PR por prompt.
3. Haz `push` solo cuando el Director lo pida.

**Cuando el Director apruebe todo:**
1. PR `feature/diseno` → `develop` con `gh pr create` (plantilla de `.github/pull_request_template.md`).
2. Merge a `develop` solo cuando el Director lo pida.
3. Después, PR `develop` → `main`, con tag anotado sobre `main` (`vX.Y.Z`) y sincronía `main` → `develop`, también solo cuando el Director lo pida.

Nunca hagas merge, push forzado ni tag por tu cuenta.

## Commits

- Conventional Commits en español, con alcance: `feat(onboarding): …`, `fix(overlay): …`, `refactor(theme): …`, `docs: …`, `chore(build): …`, `style(ui): …`, `perf(core): …`.
- **Un commit por cambio lógico**, no por prompt: tokens ↔ componente ↔ pantalla ↔ cadenas i18n ↔ wiring de navegación o DI. Nada de commits vacíos, de formato masivo sin motivo ni de "WIP".
- No dejes el código sin compilar a propósito entre commits.
- El mensaje explica el porqué cuando no es obvio (cuerpo opcional, en líneas de 72 caracteres).
- Claude puede figurar como coautor en los commits y PRs.

## Definición de terminado (por prompt)

- [ ] Hace lo que pide el prompt, sin añadir cosas que no pidió.
- [ ] Cadenas en EN y ES; solo tokens del design system (ni hex ni animaciones sueltas).
- [ ] `DESIGN_SYSTEM.md` actualizado si cambió un valor visual.
- [ ] Commits hechos en `feature/diseno`.

## Al terminar cada prompt

Reporta de forma breve y espera el siguiente:

```
Prompt: <qué se pidió, en una línea>
Rama: feature/diseno
Commits: <lista hash + mensaje>
Para probar: <qué pantalla mirar y cómo llegar>
Pendiente / riesgos: <si hay; qué no pude verificar>
```
