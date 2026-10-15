# DESIGN_SYSTEM.md — SegundoAire

Fuente única de valores visuales para el código. Traducido del design system de SegundoAire (Claude Design, versión 1). Si una pantalla necesita un valor que no está aquí, se agrega primero aquí y luego al código.

Ubicación en código: `presentation/designsystem/theme/` → `Color.kt`, `Type.kt`, `Glass.kt`, `Spacing.kt`, `Shape.kt`, `Motion.kt`, `Theme.kt`.

---

## 1. Principios

- **Una idea por pantalla.** El dato importante va en `displayImpact` o `displayTimer`; lo demás en `bodyMd` con `onBackgroundMuted`.
- **Calma antes que urgencia.** `error` solo para errores reales (un correo mal escrito), nunca para "te pasaste de tiempo".
- **El vidrio es la superficie.** Las tarjetas flotan sobre `background` con `glass1/2/3`; no hay tarjetas sólidas ni bordes de color.
- **El movimiento confirma, no decora.** Cada animación responde a un gesto o a un cambio de estado. Solo la respiración del GlassWall y el brillo del timer se repiten solos.

## 2. Voz y contenido

- Tú, frases cortas, verbo primero: "Iniciar bloque", "Elegir apps", "Volver a mi bloque".
- Sin culpa: "Llevas 23 min aquí. ¿Es lo que querías?" y no "Has desperdiciado 23 min".
- Números concretos y amables ("47 min recuperados hoy"), siempre con cifras tabulares.
- Mayúscula solo en la primera palabra. Mayúsculas completas únicamente en eyebrows (`labelSm`, +0.08em).
- Sin emoji. **Mindful** y **Strict** no se traducen.
- ES y EN siempre; el inglés no es literal: "Toma un segundo aire" → "Take a second breath".

---

## 3. Color

Claro es el predeterminado. En pantallas, siempre por token (`SegundoAireTheme.colors.x`), nunca por hex.

### 3.1 Tokens (`Color.kt`)

| Token Kotlin | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `0xFFDAF1DE` | `0xFF0D1F23` | Fondo de pantalla |
| `onBackground` | `0xFF051F20` | `0xFFB0B4B8` | Texto principal (también sobre vidrio) |
| `onBackgroundMuted` | `0xFF2C5649` | `0xFF8C9297` | Texto secundario |
| `surfaceGlass` | `0xFF8EB69B` | `0xFF132E35` | Base sólida del vidrio (respaldo sin blur) |
| `glass1` | `0x478EB69B` | `0x73132E35` | Vidrio sutil (28 % / 45 %) |
| `glass2` | `0x738EB69B` | `0xA6132E35` | Vidrio estándar (45 % / 65 %) |
| `glass3` | `0xA68EB69B` | `0xD1132E35` | Vidrio intenso (65 % / 82 %) |
| `glassEdgeHi` | `0xB3FFFFFF` | `0x47B0B4B8` | Inicio del borde de luz (arriba-izquierda) |
| `glassEdgeLo` | `0x14FFFFFF` | `0x0AB0B4B8` | Final del borde de luz |
| `primary` | `0xFF235347` | `0xFF708792` | Acción principal, estado activo, anillo del timer |
| `primaryHover` | `0xFF2A5E51` | `0xFF7A909B` | Hover (mouse / ChromeOS) |
| `primaryPressed` | `0xFF1B443A` | `0xFF8699A3` | Pressed, junto al squish |
| `onPrimary` | `0xFFDAF1DE` | `0xFF0D1F23` | Texto sobre `primary` (en oscuro es texto oscuro) |
| `primaryAlt` | `0xFF235347` | `0xFF5A636A` | Solo decorativo (2.77:1), nunca texto ni borde |
| `secondary` | `0xFF163832` | `0xFF2D4A53` | "Terminar bloque", chips seleccionados |
| `secondaryDeep` | `0xFF0B2B26` | `0xFF091619` | Barra del bloque activo |
| `onSecondary` | `0xFFDAF1DE` | `0xFFB0B4B8` | Texto sobre `secondary` / `secondaryDeep` |
| `outline` | `0xFF4A7A63` | `0xFF5E7A84` | Bordes funcionales (campos, toggle apagado) |
| `outlineVariant` | `0x1F051F20` | `0x1FB0B4B8` | Divisores decorativos |
| `glow` | `0xFF2E7D63` | `0xFF86E0CF` | Acento: punta del timer, foco, IA, respiración |
| `onGlow` | `0xFFDAF1DE` | `0xFF0D1F23` | Texto sobre relleno `glow` |
| `focusRing` | `0xFF235347` | `0xFF86E0CF` | Anillo de foco 2 px |
| `stateLayer` | `0x14051F20` | `0x1AB0B4B8` | Capa hover/pressed sobre neutros |
| `disabledContent` | `0x61051F20` | `0x61B0B4B8` | Texto/ícono deshabilitado (38 %) |
| `disabledContainer` | `0x1A051F20` | `0x1AB0B4B8` | Relleno deshabilitado (10 %) |
| `error` | `0xFF9B2C22` | `0xFFF2A497` | Error (siempre con ícono + palabra) |
| `errorContainer` | `0xFFF3D6D0` | `0xFF3A1D1A` | Fondo de error |
| `success` | `0xFF1F6B3A` | `0xFFA6D99A` | Éxito (siempre con ícono + palabra) |
| `successContainer` | `0xFFCDEBD3` | `0xFF1C3320` | Fondo de éxito |
| `warning` | `0xFF7A4F00` | `0xFFE6C47E` | Advertencia, permiso pendiente |
| `warningContainer` | `0xFFF2E2BE` | `0xFF33290F` | Fondo de banners de permisos |
| `scrim` | `0x52051F20` | `0x99040C0E` | Velo del GlassWall (bajo el blur) |
| `app1` … `app5` | `0xFFC98B6B` `0xFF6B8FC9` `0xFFB58BC9` `0xFFC9B26B` `0xFF6BC9B5` | `0xFFB07A5F` `0xFF5F7DB0` `0xFF9C7AB0` `0xFFB09B5F` `0xFF5FB09E` | Íconos de app placeholder |

### 3.2 Reglas de uso

- `primary` como **texto** solo sobre `background`. Sobre vidrio, usar `onBackground` (o `primary` a 24 sp o más, o en íconos).
- En claro, nunca `onBackgroundMuted` sobre `surfaceGlass` sólido (2.7:1).
- `error` y `success` tienen luminosidad parecida: siempre acompañados de ícono y palabra.
- Hover/pressed: `stateLayer` sobre superficies neutras; en el botón primario, `primaryHover` / `primaryPressed`.
- Mapear al `ColorScheme` de Material 3 solo para componentes M3 que se usen (primary, onPrimary, secondary, background, surface = `surfaceGlass`, error, outline). El resto vive en `SegundoAireColors`, expuesto con un `CompositionLocal`.

### 3.3 Ajustes de contraste ya aplicados (WCAG AA)

| Modo | Par | Original | Ajustado |
|---|---|---|---|
| Oscuro | `primary` / `background` | #69818D · 4.14 | #708792 · 4.50 |
| Oscuro | texto sobre `primary` | blanco · 4.10 | #0D1F23 · 4.50 |
| Oscuro | `onBackground` / `secondary` | #AFB3B7 · 4.49 | #B0B4B8 · 4.54 |
| Claro | texto primario sobre `surfaceGlass` | #235347 · 3.88 | usar `onBackground` (7.61) |
| Claro | texto secundario sobre `glass3` | — | #2C5649 · 4.68 |
| Claro | borde de control / `background` | #8EB69B · 1.89 | `outline` #4A7A63 · 4.14 |

---

## 4. Tipografía (`Type.kt`)

Fuente: **Manrope** variable en `res/font/manrope_variable.ttf` (Google Fonts), `FontFamily(Font(R.font.manrope_variable, weight = …))` por peso. Las cifras usan `fontFeatureSettings = "tnum"`.

| Token Kotlin | Tamaño / interlineado | Peso | Tracking | Uso |
|---|---|---|---|---|
| `displayTimer` | 72 / 76 sp | 300 | -0.02 em | Solo el número del TimerRing (tnum) |
| `displayImpact` | 44 / 48 sp | 700 | -0.02 em | Un dato de impacto por pantalla (tnum) |
| `headlineLg` | 32 / 40 sp | 600 | -0.01 em | Título del GlassWall |
| `headlineMd` | 26 / 32 sp | 600 | -0.01 em | Título de pantalla |
| `titleLg` | 20 / 28 sp | 600 | 0 | Títulos de tarjeta |
| `titleMd` | 16 / 24 sp | 600 | 0.005 em | Nombre de app, encabezados de sección |
| `bodyLg` | 16 / 24 sp | 400 | 0 | Texto corrido, mensaje IA, GlassWall |
| `bodyMd` | 14 / 20 sp | 400 | 0 | Descripciones y ayudas |
| `labelLg` | 15 / 20 sp | 600 | 0.01 em | Botones |
| `labelMd` | 12 / 16 sp | 600 | 0.02 em | Tabs, chips, segmentado |
| `labelSm` | 11 / 16 sp | 500 | 0.06 em (0.08 em en mayúsculas) | Metadatos y eyebrows |

En Compose, `letterSpacing` en em: `(-0.02).em`. Mapear también al `Typography` de M3 (displayLarge = `displayTimer`, displayMedium = `displayImpact`, etc.) para componentes M3.

---

## 5. Vidrio (`Glass.kt`)

Tres niveles. Todos con borde de 1 dp en gradiente lineal de `glassEdgeHi` (arriba-izquierda) a `glassEdgeLo` (abajo-derecha).

| Nivel | Relleno | Blur | Sombra claro | Sombra oscuro | Uso |
|---|---|---|---|---|---|
| `Glass.Level1` | `glass1` | 12 dp | 0 1 2 · #051F20 6 % | 0 1 2 · negro 20 % | Filas, campos, chips, segmentado |
| `Glass.Level2` | `glass2` | 20 dp | 0 2 6 · 6 % + 0 8 24 · 8 % | 0 2 6 · 22 % + 0 8 24 · 28 % | Tarjetas, banners, tarjeta IA |
| `Glass.Level3` | `glass3` | 32 dp | 0 4 12 · 8 % + 0 16 48 · 14 % | 0 4 12 · 28 % + 0 16 48 · 40 % | Barra inferior, hoja del GlassWall |

(Sombra: offsetY · blur, color y opacidad.)

**Implementación:**
- API: `Modifier.glass(level: GlassLevel, shape: Shape)`.
- Android 12+: blur con **Haze** (`hazeSource` en el fondo, `hazeEffect` en la superficie).
- Android 10–11: sin blur; el relleno sube un 20 % de opacidad (por ejemplo, `glass2` claro de 45 % a 65 %).
- Sombras: `Modifier.dropShadow` (Compose 1.9+) con las dos capas; si no está disponible, `Modifier.shadow` con `ambientColor`/`spotColor` aproximados.
- El texto cumple contraste sobre el relleno solo; nunca dependas del blur.
- **GlassWall (overlay sobre otras apps):** Haze no aplica, porque el fondo es otra ventana. Usar `WindowManager.LayoutParams.FLAG_BLUR_BEHIND` + `blurBehindRadius` (32 dp en px) en Android 12+, escuchando `addCrossWindowBlurEnabledListener`; en 10–11 o con el blur desactivado, solo `scrim`.

---

## 6. Espaciado (`Spacing.kt`) y radios (`Shape.kt`)

| Token | dp | Uso |
|---|---|---|
| `space1` | 4 | Ícono–texto mínimo |
| `space2` | 8 | Gap dentro de un grupo |
| `space3` | 12 | Padding de chips y filas compactas |
| `space4` | 16 | Margen lateral de pantalla, padding de tarjeta |
| `space5` | 20 | Padding de tarjetas grandes |
| `space6` | 24 | Separación entre bloques |
| `space8` | 32 | Separación entre secciones |
| `space10` | 40 | Aire alrededor del TimerRing |
| `space12` | 48 | Margen superior del GlassWall |
| `space16` | 64 | Espacios heroicos (onboarding) |

| Token | dp | Uso |
|---|---|---|
| `radiusXs` | 8 | Badges, skeleton de texto |
| `radiusSm` | 12 | Íconos de app de 48 dp, chips |
| `radiusMd` | 16 | Campos, banners, filas |
| `radiusLg` | 24 | GlassCard y tarjetas |
| `radiusXl` | 32 | GlassWall, hojas inferiores |
| `radiusFull` | `CircleShape` / 50 % | Botones, toggles, segmentado, barra inferior |

Área táctil mínima: 48 dp.

## 7. Opacidades

`opacityHover` 0.08 · `opacityPressed` 0.12 · `opacityDisabled` 0.38.

---

## 8. Movimiento (`Motion.kt`)

Nueve tokens. Ninguna pantalla usa `spring()` o `tween()` directamente.

| Token | Tipo | Valores Compose | Uso |
|---|---|---|---|
| `Motion.press` | spring | `dampingRatio = 0.6f, stiffness = 400f`; escala presionado 0.95 | Botones, tarjetas, chips |
| `Motion.toggle` | spring | `dampingRatio = 0.5f, stiffness = 400f` | Toggle, selección, indicador del segmentado |
| `Motion.nav` | spring | `dampingRatio = 1f, stiffness = 1500f` | Cambio de tab, entradas de pantalla |
| `Motion.calm` | tween | `durationMillis = 500`, `easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)` *; salida `Motion.calmExit`: misma curva, 250 ms; la hoja entra con escala de 0.92 a 1 | Entrada y salida del GlassWall |
| `Motion.timer` | lineal | `tween(durationMillis = totalMs, easing = LinearEasing)` | Progreso del TimerRing (tiempo real) |
| `Motion.reveal` | por carácter | 18 ms entre caracteres; cada uno con fundido + subida de 4 dp | Texto de la IA |
| `Motion.step` | spring + desplazamiento | desplazamiento 24 dp + fundido; `dampingRatio = 1f, stiffness = 400f` * | Cambio entre preguntas del onboarding |
| `Motion.breath` | ciclo | 4000 ms inhalar + 4000 ms exhalar, `FastOutSlowInEasing`, infinito | Círculo de respiración |
| `Motion.hold` | lineal | `tween(1500, easing = LinearEasing)` | Botón hold-to-commit (tiempo real) |

\* Curva y rigidez no especificadas numéricamente en el design system ("ease-out suave", "spring sin rebote"); estos son los valores propuestos. Ajustar comparando con la tarjeta MotionLab.

**Reglas:**
- `timer` y `hold` son los únicos movimientos lineales, porque representan tiempo real.
- **Reducir movimiento:** `rememberReducedMotion()` lee `Settings.Global.ANIMATOR_DURATION_SCALE`. Si es 0, todos los tokens devuelven `tween(150)` de fundido, sin escala ni desplazamiento. El timer sigue avanzando.
- Squish: `interactionSource.collectIsPressedAsState()` + `graphicsLayer { scaleX; scaleY }` animado con `Motion.press`. Nunca `pointerInput` (rompe click y accesibilidad).
- Listas: aparición escalonada solo en la primera carga; reordenamientos con `Modifier.animateItem()`.
- Nada de animación en el camino crítico del `AccessibilityService`: el overlay se muestra primero y anima después.

---

## 9. Iconografía

- Trazo 1.75 sobre rejilla de 24 dp, extremos y uniones redondeados: `Icons.Rounded.*` / Material Symbols Rounded (peso 400, grado 0). Importar solo los vectores usados, no `material-icons-extended` completo.
- `play` y `stop` son los únicos íconos rellenos.
- Íconos de app de ejemplo: formas geométricas sobre `app1…5`. Nunca logos reales.
- Botones de proveedor: íconos genéricos (globo, dispositivo, sobre) hasta producción, donde se usan los recursos oficiales según las guías de cada marca.

## 10. Accesibilidad

- Foco: 2 dp de `focusRing` con 3 dp de separación (3:1 o más en todas las superficies).
- Controles de 48 dp o más. Toggle con `Role.Switch`; segmentado con `selectableGroup()` + `Role.RadioButton`.
- GlassWall: diálogo modal; la cuenta de 10 s de Mindful se anuncia en el botón.
- Texto de IA: `contentDescription` con el texto completo; el revelado es solo visual.

## 11. Componentes de referencia

Inventario a implementar en la Fase 10 (detalle visual en el design system): PrimaryButton (morph Iniciar ↔ Terminar), SecondaryButton, TextButton, ProviderButton, GlassCard (3 niveles), AppToggleRow, SegmentedSelector, RigorSelector, TimerRing, GlassTextField, ChoiceChip, RangeSelector, AnimatedCounter, BreathingCircle, HoldToCommitButton, RevealText, PermissionBanner, EmptyState, Skeleton, BottomNavBar (4 tabs), QuestionScaffold, GlassWall (Mindful / Strict).