# ROADMAP — SegundoAire

Plan de trabajo por fases y tareas. Cada fase es una rama `feature/fase-N-slug` que sale de `develop` y vuelve por PR. Cada tarea produce uno o más commits lógicos. Marca `[x]` al terminar.

**Estado inicial (12 oct 2026):** fases 1–7 completas, 12 commits en el historial. Meta: 100 commits o más al llegar a `v1.0.0`.

| Fase | Rama | Commits est. |
|---|---|---|
| 8 · Reencauce y documentación | `feature/fase-8-docs-gitflow` | 6 |
| 9 · Fundamentos del design system | `feature/fase-9-design-tokens` | 9 |
| 10 · Componentes base | `feature/fase-10-componentes` | 12 |
| 11 · Onboarding personalizado | `feature/fase-11-onboarding` | 14 |
| 12 · Cuenta simulada, permisos y apps | `feature/fase-12-setup` | 10 |
| 13 · Shell principal y Panel | `feature/fase-13-panel` | 8 |
| 14 · Bloques (timer v2) | `feature/fase-14-bloques` | 8 |
| 15 · GlassWall v2 y rigor | `feature/fase-15-glasswall` | 10 |
| 16 · Sesiones y Progreso | `feature/fase-16-progreso` | 10 |
| 17 · Perfil, ajustes e IA simulada | `feature/fase-17-perfil` | 8 |
| 18 · Trabajo en segundo plano | `feature/fase-18-background` | 5 |
| 19 · Calidad: tests, accesibilidad y rendimiento | `feature/fase-19-calidad` | 8 |
| 20 · Release 1.0 | `release/v1.0.0` | 4 |
| **Total** | | **~112** (+12 existentes) |

---

## Fase 8 · Reencauce y documentación

Objetivo: dejar el repositorio listo para Gitflow y con documentación alineada al design system.

- [x] **F8-T1** Crear `develop` desde `main`; PR de `feature/fase-7-temporizador` → `develop`; tag `v0.1.0` sobre `main` tras el merge de la línea base. *(acción manual o guiada; sin commits nuevos)* (tag v0.1.0 = fases 1-6; la Fase 7 sale en la primera release)
- [x] **F8-T2** `docs: reemplazar CLAUDE.md con reglas de Gitflow y design system`
- [x] **F8-T3** `docs: reescribir SEGUNDOAIRE_SPEC.md como especificación de producto v2`
- [x] **F8-T4** `docs: agregar DESIGN_SYSTEM.md con tokens traducidos a Compose`
- [x] **F8-T5** `docs: agregar ROADMAP.md`
- [x] **F8-T6** `chore(github): agregar plantilla de PR y de issue`
- [x] **F8-T7** `chore: actualizar .gitignore`

Aceptación: `develop` existe en el remoto; los cinco documentos están en el repo; PR abierto hacia `develop`.

## Fase 9 · Fundamentos del design system

Objetivo: el tema nuevo existe en código y la app actual ya lo usa (sin rediseñar pantallas todavía).

- [ ] **F9-T1** Agregar la fuente Manrope (variable) en `res/font`.
- [ ] **F9-T2** `Color.kt`: tokens semánticos claro/oscuro, con los ajustes de contraste y `glow`.
- [ ] **F9-T3** `Type.kt`: escala tipográfica completa (`displayTimer`, `displayImpact`… `labelSm`) con `tabular-nums` donde aplique.
- [ ] **F9-T4** `Glass.kt`: niveles 1/2/3 (relleno, blur, sombra, borde de gradiente) + respaldo para Android 11 o anterior.
- [ ] **F9-T5** `Spacing.kt` y `Shape.kt`: rejilla de 4/8 dp y radios.
- [ ] **F9-T6** `Motion.kt`: los nueve tokens + `rememberReducedMotion()`.
- [ ] **F9-T7** `SegundoAireTheme` con `CompositionLocal`s para glass, motion y spacing; eliminar la paleta legado.
- [ ] **F9-T8** Agregar dependencia Haze y `Modifier.glass(level)`.
- [ ] **F9-T9** Tests unitarios de contraste de los pares de color críticos.

Aceptación: la app compila y se ve con la paleta nueva; cero hex fuera de `Color.kt`.

## Fase 10 · Componentes base

Objetivo: biblioteca de componentes en `presentation/designsystem/components`, cada uno con previews en claro y oscuro.

- [x] **F10-T1** `Modifier.pressScale()` (token `press`, con `interactionSource`).
- [x] **F10-T2** `PrimaryButton` con morph de estado (Iniciar ↔ Terminar) y `SecondaryButton` / `TextButton`.
- [x] **F10-T3** `ProviderButton` (Google / Apple / correo) con íconos genéricos.
- [x] **F10-T4** `GlassCard` v2 en 3 niveles, presionable.
- [x] **F10-T5** `AppToggleRow` con toggle animado (`toggle`).
- [x] **F10-T6** `SegmentedSelector` con indicador deslizante.
- [x] **F10-T7** `RigorSelector` (Mindful/Strict) con descripción animada.
- [x] **F10-T8** `TimerRing` v2: progreso lineal continuo, glow en la punta y `displayTimer`.
- [x] **F10-T9** `GlassTextField` (foco, error, mostrar/ocultar contraseña).
- [ ] **F10-T10** `ChoiceChip` (única/múltiple, con límite), `RangeSelector` y `AnimatedCounter`.
- [ ] **F10-T11** `BreathingCircle` y `HoldToCommitButton`.
- [ ] **F10-T12** `RevealText`, `PermissionBanner`, `EmptyState` y `Skeleton`.
- [x] **F10-T13** Catálogo de componentes (solo debug): `ComponentCatalogScreen` con switch claro/oscuro, switch de reducir movimiento y una sección por componente.

Aceptación: cada componente con previews y respetando reducir movimiento.

## Fase 11 · Onboarding personalizado

Objetivo: el flujo previo al login completo y navegable; los datos se guardan en DataStore.

- [ ] **F11-T1** Dominio: modelo `UserProfile` (nombre, objetivos, rango de uso, momentos, rigor sugerido) y use cases.
- [ ] **F11-T2** Datos: `UserProfileRepository` sobre DataStore + tests.
- [ ] **F11-T3** `OnboardingViewModel` con estado por paso y validaciones + tests.
- [ ] **F11-T4** `QuestionScaffold` (progreso segmentado, volver, botón fijo) y transición `step`.
- [ ] **F11-T5** Pantalla de bienvenida (3 tarjetas).
- [ ] **F11-T6** Pantalla de nombre.
- [ ] **F11-T7** Pantalla de objetivos (máximo 2).
- [ ] **F11-T8** Pantallas de uso diario y momento de mayor uso.
- [ ] **F11-T9** Revelación: cálculo honesto con fórmula visible + `AnimatedCounter` + tests del cálculo.
- [ ] **F11-T10** Demo de respiración.
- [ ] **F11-T11** Plan personalizado.
- [ ] **F11-T12** Compromiso con `HoldToCommitButton` (y vibración háptica).
- [ ] **F11-T13** Cadenas EN/ES del flujo completo.
- [ ] **F11-T14** Grafo de navegación anidado del onboarding; reemplaza el onboarding legado.

## Fase 12 · Cuenta simulada, permisos y apps

- [ ] **F12-T1** Pantalla de cuenta: proveedores + "continuar sin cuenta" (simulado, guarda `authMode`).
- [ ] **F12-T2** Login y registro con correo (validación de formulario real, sin backend).
- [ ] **F12-T3** Recuperar contraseña (simulado, con estado de éxito).
- [ ] **F12-T4** Permisos guiados con checklist y refresco al volver de Ajustes.
- [ ] **F12-T5** Repositorio de apps instaladas (`PackageManager`, filtrado de apps de sistema).
- [ ] **F12-T6** Pantalla de selección de apps con búsqueda y sugeridas.
- [ ] **F12-T7** Guardar selección en Room y marcar el setup como completo.
- [ ] **F12-T8** Navegación raíz: onboarding → cuenta → permisos → apps → principal.
- [ ] **F12-T9** Cadenas EN/ES.
- [ ] **F12-T10** Tests de validación de formularios y del ViewModel de setup.

## Fase 13 · Shell principal y Panel

- [ ] **F13-T1** Barra inferior glass de 4 tabs (Panel / Bloques / Progreso / Perfil) con íconos Rounded (vectores importados).
- [ ] **F13-T2** Transición entre tabs con `AnimatedContent` y token `nav`.
- [ ] **F13-T3** Panel: saludo con nombre y objetivo, y dato de impacto del día.
- [ ] **F13-T4** Panel: lista de apps vigiladas con aparición escalonada (solo la primera vez) y `animateItem`.
- [ ] **F13-T5** Banners de permisos y estado vacío.
- [ ] **F13-T6** Migrar `MainViewModel` al estado nuevo + tests.
- [ ] **F13-T7** Eliminar `DashboardScreen` y la navegación legado.
- [ ] **F13-T8** Cadenas EN/ES.

## Fase 14 · Bloques (timer v2)

- [ ] **F14-T1** Rediseñar `TimerScreen` con los componentes nuevos.
- [ ] **F14-T2** Timer basado en reloj de pared (sobrevive a la rotación y al fondo), no en ticks acumulados.
- [ ] **F14-T3** Conectar el bloque activo al `InterceptorEngine` (las apps se interceptan durante el bloque).
- [ ] **F14-T4** Notificación persistente del bloque activo (servicio en primer plano).
- [ ] **F14-T5** Estado de bloque completado con celebración sobria.
- [ ] **F14-T6** Tests del `TimerViewModel` y del motor.
- [ ] **F14-T7** Cadenas EN/ES.

## Fase 15 · GlassWall v2 y rigor

- [ ] **F15-T1** Entrada `calm` del overlay: blur detrás (`FLAG_BLUR_BEHIND`, Android 12+) o scrim (10–11), luego el contenido.
- [ ] **F15-T2** `BreathingCircle` en el overlay.
- [ ] **F15-T3** Mensaje personalizado ("Ana, ¿esto te acerca a dormir mejor?").
- [ ] **F15-T4** Variante Mindful: "Continuar" se habilita tras 10 s, con la cuenta visible.
- [ ] **F15-T5** Variante Strict: sin "Continuar" durante un bloque activo.
- [ ] **F15-T6** Registrar cada intercepción (abierta o evitada) para Progreso.
- [ ] **F15-T7** Medir que el camino crítico se mantiene por debajo de 50 ms (log en debug).
- [ ] **F15-T8** Cadenas EN/ES y tests del motor de rigor.

## Fase 16 · Sesiones y Progreso

- [ ] **F16-T1** Room v2: entidades `BlockSession` e `InterceptionEvent` + migración 1→2 + test de migración.
- [ ] **F16-T2** DAOs y repositorios.
- [ ] **F16-T3** Use cases de estadísticas (minutos recuperados, aperturas evitadas, racha).
- [ ] **F16-T4** Pantalla Progreso: dato de impacto, gráfico semanal (Canvas) y racha.
- [ ] **F16-T5** Detalle por app.
- [ ] **F16-T6** Tests de los cálculos de estadísticas.
- [ ] **F16-T7** Cadenas EN/ES.

## Fase 17 · Perfil, ajustes e IA simulada

- [ ] **F17-T1** Perfil: nombre, objetivo y cuentas conectadas (simulado).
- [ ] **F17-T2** Ajustes: tema (sistema/claro/oscuro), idioma, rigor por defecto, reducir movimiento.
- [ ] **F17-T3** Editar apps vigiladas y objetivos.
- [ ] **F17-T4** Tarjeta de mensaje IA con `RevealText`, alimentada por un banco local de mensajes según el objetivo.
- [ ] **F17-T5** Interfaz `MotivationalMessageProvider` (implementación local; lista para cambiar por IA real).
- [ ] **F17-T6** Cadenas EN/ES y tests.

## Fase 18 · Trabajo en segundo plano

- [ ] **F18-T1** `WorkManager`: resumen diario y limpieza de eventos antiguos.
- [ ] **F18-T2** `AlarmManager` exacto para el fin de bloque en Doze.
- [ ] **F18-T3** Recuperación del estado tras reinicio del dispositivo (`BOOT_COMPLETED`).
- [ ] **F18-T4** Tests con `WorkManagerTestInitHelper`.

## Fase 19 · Calidad

- [ ] **F19-T1** Tests de UI de Compose para el onboarding y Bloques.
- [ ] **F19-T2** Auditoría de accesibilidad (TalkBack, tamaño de fuente 200 %, contraste).
- [ ] **F19-T3** Profiling del servicio de accesibilidad (asignaciones en el camino crítico).
- [ ] **F19-T4** Baseline Profile para el arranque.
- [ ] **F19-T5** Corrección de hallazgos (commits `fix:` según aparezcan).

## Fase 20 · Release 1.0

- [ ] **F20-T1** Ícono adaptativo y splash con la marca.
- [ ] **F20-T2** Reglas de R8/ProGuard y build de release firmado.
- [ ] **F20-T3** `versionName 1.0.0`, CHANGELOG y README con capturas.
- [ ] **F20-T4** Merge `release/v1.0.0` → `main` y → `develop`; tag `v1.0.0`.