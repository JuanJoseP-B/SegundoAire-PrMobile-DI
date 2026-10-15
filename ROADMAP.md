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
| 13 · Shell y Panel | `feature/fase-13-panel` | 8 |
| 14 · Bloques y GlassWall | `feature/fase-14-bloques-glasswall` | 10 |
| 15 · Progreso | `feature/fase-15-progreso` | 6 |
| 16 · Perfil y Ajustes | `feature/fase-16-perfil` | 7 |
| 17 · Pulido y release | `release/v1.0.0` | 5 |
| **Total** | | **~87** (+12 existentes) |

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
- [x] **F10-T10** `ChoiceChip` (única/múltiple, con límite), `RangeSelector` y `AnimatedCounter`.
- [x] **F10-T11** `BreathingCircle` y `HoldToCommitButton`.
- [x] **F10-T12** `RevealText`, `PermissionBanner`, `EmptyState` y `Skeleton`.
- [x] **F10-T13** Catálogo de componentes (solo debug): `ComponentCatalogScreen` con switch claro/oscuro, switch de reducir movimiento y una sección por componente.

Aceptación: cada componente con previews y respetando reducir movimiento.

## Fase 11 · Onboarding personalizado

Objetivo: el flujo previo al login completo y navegable; los datos se guardan en DataStore.

- [x] **F11-T1** Dominio: modelo `UserProfile` (nombre, objetivos, rango de uso, momentos, rigor sugerido) y use cases.
- [x] **F11-T2** Datos: `UserProfileRepository` sobre DataStore + tests.
- [x] **F11-T3** `OnboardingViewModel` con estado por paso y validaciones + tests.
- [x] **F11-T4** `QuestionScaffold` (progreso segmentado, volver, botón fijo) y transición `step`.
- [x] **F11-T5** Pantalla de bienvenida (3 tarjetas).
- [x] **F11-T6** Pantalla de nombre.
- [x] **F11-T7** Pantalla de objetivos (máximo 2).
- [x] **F11-T8** Pantallas de uso diario y momento de mayor uso.
- [x] **F11-T9** Revelación: cálculo honesto con fórmula visible + `AnimatedCounter` + tests del cálculo.
- [x] **F11-T10** Demo de respiración.
- [x] **F11-T11** Plan personalizado.
- [x] **F11-T12** Compromiso con `HoldToCommitButton` (y vibración háptica).
- [x] **F11-T13** Cadenas EN/ES del flujo completo.
- [x] **F11-T14** Grafo de navegación anidado del onboarding; reemplaza el onboarding legado.

## Fase 12 · Cuenta simulada, permisos y apps

- [x] **F12-T1** Pantalla de cuenta: proveedores + "continuar sin cuenta" (simulado, guarda `authMode`).
- [x] **F12-T2** Login y registro con correo (validación de formulario real, sin backend).
- [x] **F12-T3** Recuperar contraseña (simulado, con estado de éxito).
- [x] **F12-T4** Permisos guiados con checklist y refresco al volver de Ajustes.
- [x] **F12-T5** Repositorio de apps instaladas (`PackageManager`, filtrado de apps de sistema).
- [x] **F12-T6** Pantalla de selección de apps con búsqueda y sugeridas.
- [x] **F12-T7** Guardar selección en Room y marcar el setup como completo.
- [x] **F12-T8** Navegación raíz: onboarding → cuenta → permisos → apps → principal.
- [x] **F12-T9** Cadenas EN/ES.
- [x] **F12-T10** Tests de validación de formularios y del ViewModel de setup.

## Fase 13 · Shell y Panel

- [x] **F13-T1** Barra inferior glass de 4 tabs (Panel/Bloques/Progreso/Perfil), íconos Rounded importados
- [x] **F13-T2** Transición entre tabs con AnimatedContent y Motion.nav
- [x] **F13-T3** Panel: saludo con nombre y objetivo + dato de impacto del día
- [x] **F13-T4** Lista de apps vigiladas con stagger solo en la primera carga + animateItem
- [x] **F13-T5** Tarjeta de mensaje IA simulada (RevealText, banco local según objetivo, interfaz MotivationalMessageProvider)
- [x] **F13-T6** Banners de permisos y estado vacío
- [x] **F13-T7** Migrar MainViewModel + tests; eliminar DashboardScreen y la navegación legado
- [x] **F13-T8** Cadenas EN/ES

## Fase 14 · Bloques y GlassWall

- [x] **F14-T1** TimerScreen rediseñado con los componentes nuevos
- [x] **F14-T2** Timer basado en reloj de pared (sobrevive a la rotación y al fondo)
- [x] **F14-T3** Bloque activo conectado al InterceptorEngine
- [x] **F14-T4** Entrada calm del GlassWall: FLAG_BLUR_BEHIND en Android 12+, scrim en 10-11, luego el contenido
- [x] **F14-T5** BreathingCircle y mensaje personalizado con nombre y objetivo
- [x] **F14-T6** Rigor: Mindful habilita "Continuar" tras 10 s visibles; Strict sin "Continuar" durante el bloque
- [x] **F14-T7** Estado de bloque completado
- [x] **F14-T8** Tests del timer y del motor de rigor + cadenas EN/ES

## Fase 15 · Progreso

- [x] **F15-T1** Room v2: entidad InterceptionEvent (paquete, fecha, resultado) + migración 1→2 + exportSchema
- [x] **F15-T2** Registrar cada intercepción desde el overlay
- [x] **F15-T3** Use cases: minutos recuperados, aperturas evitadas, racha
- [ ] **F15-T4** Pantalla Progreso: dato de impacto con AnimatedCounter, gráfico semanal en Canvas, racha
- [ ] **F15-T5** Modo demo: generador de 7 días de datos de ejemplo (activable desde Ajustes)
- [ ] **F15-T6** Tests de los cálculos + cadenas EN/ES

## Fase 16 · Perfil y Ajustes

- [x] **F16-T1** Perfil: nombre, objetivo, modo de cuenta (simulado), editar nombre y objetivos
- [x] **F16-T2** Idioma ES/EN dentro de la app con AppCompatDelegate.setApplicationLocales + locales_config.xml (también aparece en los ajustes de idioma por app de Android 13+)
- [ ] **F16-T3** Tema: sistema / claro / oscuro, guardado en DataStore
- [ ] **F16-T4** Tutorial: carrusel de 5 tarjetas (qué hace la app, el GlassWall, Mindful vs Strict, bloques, progreso), reutilizando componentes
- [ ] **F16-T5** Reiniciar app: diálogo de confirmación, borra DataStore y Room, y navega a la bienvenida limpiando el back stack. Los permisos del sistema se conservan.
- [ ] **F16-T6** Interruptor de modo demo
- [ ] **F16-T7** Tests (reinicio y preferencias) + cadenas EN/ES

## Fase 17 · Pulido y release

- [ ] **F17-T1** Ícono adaptativo y splash (SplashScreen API)
- [ ] **F17-T2** Revisión visual completa en claro y oscuro: espaciados, contraste, textos cortados
- [ ] **F17-T3** R8 y build de release
- [ ] **F17-T4** README con capturas y versionName 1.0.0
- [ ] **F17-T5** Merge a main y develop, tag v1.0.0