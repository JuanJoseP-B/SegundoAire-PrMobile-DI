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
| `background` | `0xFFEAF4EC` | `0xFF0D1F23` | Fondo de pantalla |
| `onBackground` | `0xFF051F20` | `0xFFB0B4B8` | Texto principal (también sobre vidrio) |
| `onBackgroundMuted` | `0xFF3D5A50` | `0xFF8C9297` | Texto secundario |
| `surfaceGlass` | `0xFFF4FAF6` | `0xFF132E35` | Base sólida del vidrio (respaldo sin blur) |
| `glass1` | `0x61FFFFFF` | `0x73132E35` | Vidrio sutil (38 % / 45 %) |
| `glass2` | `0x94FFFFFF` | `0xA6132E35` | Vidrio estándar (58 % / 65 %) |
| `glass3` | `0xBDFFFFFF` | `0xD1132E35` | Vidrio intenso (74 % / 82 %) |
| `mint` | `0xFF8EB69B` | `0xFF86E0CF` | Centro del blob del splash |
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
| `gold` | `0xFFC9B26B` | `0xFFB09B5F` | Onboarding del árbol: follaje marchito, centro de las flores, pétalos |
| `blossom` | `0xFFDAF1DE` | `0xFFD0E8D6` | Onboarding del árbol: pétalos de las flores, semilla del telón |
| `firefly` | `0xFFE6F5B0` | `0xFFE6F5B0` | Onboarding del árbol: luciérnagas de la noche y la madrugada |
| `skyMorning` | `0x73FFC496` | `0x73FFC496` | Luz del suelo al elegir "Mañana" (45 %) |
| `skyAfternoon` | `0x73FFF6BE` | `0x73FFF6BE` | Luz del suelo al elegir "Tarde" (45 %) |
| `skyNight` | `0xBF0E1640` | `0xBF050A24` | Luz del suelo al elegir "Noche": azul nocturno (75 %), sube hasta el 70 % del alto |
| `skyLate` | `0x9E0D1F23` | `0x9E040C0E` | Luz del suelo al elegir "Madrugada" (62 %) |

### 3.2 Reglas de uso

- `primary` como **texto** solo sobre `background`. Sobre vidrio, usar `onBackground` (o `primary` a 24 sp o más, o en íconos).
- En claro, `onBackgroundMuted` sobre `surfaceGlass` sólido cumple (7.15:1) con los valores actuales; el 2.7:1 era del `surfaceGlass` anterior (`#8EB69B`).
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

**Verificación de la Fase 16.5:** los valores de modo claro de la sección 3.1 (background, onBackgroundMuted, surfaceGlass, glass1–3 y mint) se verificaron con `ColorContrastTest` y **ninguno necesitó ajuste**. Los vidrios se miden compuestos sobre `background`.

| Modo | Par | Contraste |
|---|---|---|
| Claro | `onBackground` / `background` | 15.25 |
| Claro | `onBackgroundMuted` / `background` | 6.72 |
| Claro | `primary` / `background` | 7.77 |
| Claro | `onPrimary` / `primary` | 7.34 |
| Claro | `outline` / `background` (borde, mín. 3) | 4.38 |
| Claro | `onBackground` / `onBackgroundMuted` sobre `surfaceGlass` | 16.23 / 7.15 |
| Claro | `onBackground` / `onBackgroundMuted` sobre `glass1` · `glass2` · `glass3` | 15.96 · 16.34 · 16.65 / 7.03 · 7.20 · 7.34 |
| Oscuro | `onBackgroundMuted` sobre `glass1` · `glass2` · `glass3` (el más bajo) | 5.02 · 4.84 · 4.70 |

### 3.4 Tokens de la app principal (Fase 17)

Salen del objeto `T` de `docs/design/app-dashboard/app-dashboard-referencia.html`. Los tokens vigentes (`background`, `glass*`, `onBackgroundMuted`…) no cambian: la referencia usa `#DAF1DE` de fondo y otros vidrios, pero la app conserva los de §3.1. Los colores con opacidad ya la llevan en el alfa.

| Token Kotlin | Claro | Oscuro | Uso |
|---|---|---|---|
| `hill` | `0x808EB69B` | `0xF2132E35` | Colina del héroe del Panel |
| `trunk` | `0xFF235347` | `0xFF5E7A84` | Tronco y ramas; anillos sin meta; tallos de la semana |
| `leaf` | `0xFF2E7D63` | `0xFF86E0CF` | Hojas nuevas del árbol; anillos de días con meta |
| `petal` | `0xFFF4FBF5` | `0xFFDCE3E0` | Pétalos de las flores (el centro es `gold`) |
| `wood` | `0x598EB69B` | `0xE6132E35` | Corte del tronco en Progreso |
| `aiCard` | `0xEBF0FAF2` | `0xFF11282D` | Relleno de la tarjeta del coach |
| `tree1` · `tree2` · `tree3` | `0x80235347` · `0x8C2E7D63` · `0xD98EB69B` | `0xE62D4A53` · `0x993E8C7A` · `0x5986E0CF` | Follaje, alternado por círculo (`i % 3`) |
| `heroSkyMorning` | `0x66FFC896` | `0x1FFFC896` | Luz radial del héroe, 6:00–10:59 |
| `heroSkyDay` | `0x73FFF6BE` | `0x14FFF6BE` | 11:00–16:59 |
| `heroSkyEvening` | `0x6BF4A06E` | `0x24F4A06E` | 17:00–19:59 |
| `heroSkyNight` | `0x380D1F23` | `0x000D1F23` | 20:00–5:59 (con luciérnagas) |
| `wallScrim` | `0xC7DAF1DE` | `0xB8040C0E` | Velo del GlassWall a pantalla completa |
| `wallOn` · `wallMuted` | `0xFF051F20` · `0xFF2C5649` | `0xFFDCE3E0` · `0xFF8C9297` | Texto del GlassWall |
| `wallTrunk` · `wallLeaf` | `0xFF235347` · `0xFF2E7D63` | `0xFF5E7A84` · `0xFF86E0CF` | Tronco, hoja y tallo de "Continuar" |
| `wallBtn` · `wallBtnOn` | `0xFF235347` · `0xFFDAF1DE` | `0xFF708792` · `0xFF0D1F23` | "Volver a mi bloque" y chip de intención elegido |
| `wallChip` · `wallChipEdge` | `0x14051F20` · `0xB3FFFFFF` | `0x24B0B4B8` · `0x2EB0B4B8` | Chips de intención y chip del candado |
| `wallWinter` | `0x999AA8A2` | `0x993A4A50` | Copa gris (invierno) en Strict |

**Modo enfoque (`FocusColors`)**: siempre oscuro, igual en los dos temas. Fondo `0xFF0B2B26` (el `secondaryDeep` claro), texto `0xFFDAF1DE` (al 70 % el secundario y al 85 % la frase), pista del anillo `0x1FDAF1DE`, progreso `0xFF708792`, punta `0xFF86E0CF`, botón `0xFF2D4A53` y la silueta del árbol en `0xFF8EB69B` / `0xFF2E7D63` al 22 %.

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

### 4.1 Serif del onboarding: Fraunces

Los títulos del onboarding "Lo que crece cuando pausas" van en **Fraunces** (Google Fonts, licencia OFL): `res/font/fraunces_variable.ttf` y `fraunces_italic_variable.ttf`, con el eje óptico `opsz` fijo en 96 y `wght` por peso (`FrauncesFontFamily`). El resto de la app sigue en Manrope.

| Estilo | Tamaño / interlineado | Peso | Tracking | Uso |
|---|---|---|---|---|
| `serifTitle` | 34 / 40 sp | 500 | -0.015 em | Título de cada paso del onboarding |
| `serifEmphasis` | (hereda) | 400 cursiva | — | Palabra clave del título, en `primary` (`*palabra*` en las cadenas) |
| `serif(size, lineHeight, …)` | a medida | 400 o 500 | a medida | Cifras grandes (56 / 60 y 52 / 56), frases en cursiva (22, 26 y 20), wordmark (30), cierre (40 / 46) |

En las cadenas la palabra clave se marca con asteriscos: `Respira *antes* de abrir`. Mindful y Strict no se traducen.

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


### 8.1 Movimiento del onboarding del árbol

Tokens añadidos para "Lo que crece cuando pausas". Con reducir movimiento todos los finitos pasan a `tween(150)` y los infinitos no se arrancan (el árbol aparece en su etapa, sin balanceo, ondas, luces ni hojas que caen; los contadores muestran el valor final).

| Token | Valores | Uso |
|---|---|---|
| `Motion.grow` | `tween(900, CubicBezier(.45, 0, .2, 1))`, retraso por rama; sale en 350 ms; los anillos de la revelación usan 1800 ms | Ramas, raíces, anillos y enredadera que se dibujan |
| `Motion.bloom` | `tween(d, CubicBezier(.34, 1.56, .64, 1))` con `d` = 760 follaje, 640 hojas, 520 hojas del nombre, 700 flores, 600 semilla, 520 hoja-pulgar y opciones, 420 "pop"; al salir, `ease-in` en 280 / 260 / 220 / 280 ms | Brotar con rebote suave |
| `Motion.sway` | `tween(7000, ease-in-out)`, ida y vuelta, ±0.8° sobre (195, 515) | Balanceo continuo del árbol |
| `Motion.drift(d)` | `tween(d, ease-in-out)`, ida y vuelta: 22 s y 28 s luces de fondo, 4 s luciérnagas | Vaivén lento decorativo |
| `Motion.ripple` | `tween(4000, CubicBezier(.2, .6, .3, 1))`, desfase de 1.3 s entre tres ondas | Ondas del suelo en la semilla |
| `Motion.gust` | tiempo lineal 0→1 en 2.4–3.2 s; cada tramo usa `CubicBezier(.3, .1, .4, 1)` (`GustEasing`) | Hojas y pétalos que caen en cada cambio de paso |
| `Motion.curtain` | `tween(900, CubicBezier(.7, 0, .2, 1))` | Telón del login y del cierre |
| `Motion.settle` | `tween(1000, CubicBezier(.3, 1.06, .5, 1))` | Árbol y colina cambian de lugar por paso |
| `Motion.layer` / `layerFade` | 620 ms `CubicBezier(.22, 1, .36, 1)` el desplazamiento; 380 ms `ease-out` el fundido (la capa que entra espera 180 ms) | Cambio de paso: texto a media velocidad y contenido a velocidad completa |
| `Motion.rise` | `tween(d, CubicBezier(.22, 1, .36, 1))`, 520–600 ms | Filas del plan, textos tardíos de la revelación |
| `Motion.fade(d)` | `tween(d, CubicBezier(.25, .1, .25, 1))` | Opacidad y color (cielo, puntos, saludo, marchitado) |
| `Motion.count` | `tween(d, CubicBezier(.33, 1, .68, 1))`: contador 1800 ms, "buena noticia" 1200 ms | Contadores de la revelación |
| `Motion.shake` | lineal 380 ms; `shakeOffset` interpola ±6 / 5 / 3 / 2 dp con `ShakeEasing` | Tercera meta no permitida |
| `Motion.breathHalf` | `tween(4000, ease-in-out)` por mitad: 3 ciclos de 8 s, escala 0.62 ↔ 1 | Respiración de la copa (`Motion.breath` sigue siendo el bucle de 4 + 4 s) |
| `Motion.dot` / `spin` | 400 ms `CubicBezier(.34, 1.3, .64, 1)` / 900 ms lineal infinito | Puntos de la bienvenida / semilla del botón ocupado |

### 8.2 Movimiento de la app principal

Con reducir movimiento los finitos pasan a `tween(150)` y los bucles no se arrancan (el árbol queda quieto, el coach muestra sus puntos sin latir y el texto completo).

| Token | Valores | Uso |
|---|---|---|
| `Motion.navIndicator` | `tween(520, CubicBezier(.3, 1.2, .5, 1))` | Indicador de 64 × 32 dp de la barra inferior |
| `Motion.page` / `pageFade` | 520 ms `CubicBezier(.22, 1, .36, 1)` el desplazamiento de 24 dp; 320 ms "ease" el fundido | Cambio de pestaña |
| `Motion.think` | 2400 ms lineal, infinito | Brillo cónico de `glow` que gira en el borde del coach |
| `Motion.drift(d)` | ya existente: 500 ms puntos del coach (desfase 150 ms), 700 ms skeleton, 4000 ms semilla del coach y punta del anillo | Bucles de espera y respiración |
| `Motion.reveal` | ya existente: 18 ms por carácter | Texto del coach (Fraunces 20 / 28) |
| `Motion.bloom(700)` | ya existente | Hoja nueva que brota en el Panel |
| `Motion.rise(600)` + `fade(500)` | ya existentes | Entrada del modo enfoque (sube 40 dp) |
| Toast | 2,6 s: entra en 312 ms (`rise`), sale en 520 ms (`fade`) | Avisos breves del Panel |

El coach "piensa" 1,4 s (`Motion.CoachThinkingMillis`) antes de revelar el texto; las acciones entran con `fade(400)` y 6 dp de subida al terminar el revelado.

---

## 9. Splash El Respiro

Pantalla de carga de 16 s que se reproduce una vez por arranque (`BreathSplash`). Una forma orgánica aparece, respira, hace una bocanada profunda y se expande hasta cubrir la pantalla; entonces se revela el nombre de la app y todo se funde hacia la pantalla de abajo. Se dibuja como overlay sobre el grafo de navegación, que ya está compuesto. Un toque salta directo a la fase 6.

### Coreografía (16 s)

| Fase | Tiempo | Qué pasa |
|---|---|---|
| 1 · Presencia | 0 – 1.2 s | La forma (unos 150 dp) pasa de escala 0.55 y alfa 0 a 1 y 1, con ease-out suave. |
| 2 · Respiro normal | 1.2 – 5.2 s | Escala 1 → 1.14 → 1; 2 s inhalar y 2 s exhalar, `FastOutSlowIn`. |
| 3 · Bocanada profunda | 5.2 – 10 s | Escala 1 → 1.6 en 4 s con `CubicBezier(0.45, 0, 0.35, 1)` y se sostiene 0.8 s. |
| 4 · Liberación | 10 – 11.6 s | La forma no se contrae: se expande con `CubicBezier(0.55, 0, 0.2, 1)` hasta cubrir la pantalla (escala = diagonal / radio × 1.15). Al final entra un relleno sólido de `primary`. |
| 5 · Revelación | 11.6 – 15 s | "SegundoAire" (32 sp, peso 500, -0.01 em) con fundido y subida de 8 dp, y debajo el eyebrow (11 sp, peso 500, +0.08 em, 85 %), ambos en `onPrimary`. Se sostiene 2.5 s. |
| 6 · Ingreso | 15 – 16 s | El overlay se funde 0.8 s con ease-out; las tarjetas de la bienvenida suben 24 dp y aparecen escalonadas, 120 ms entre una y otra. |

### La forma

- Dos capas translúcidas superpuestas: `primary` al 55 % y `glow` al 45 %, más un halo radial de `glow` al 22 %.
- Cada capa es un `Path` cerrado de 8 puntos con curvas cúbicas (Catmull-Rom). El radio de cada punto oscila con senos de fases distintas, ±5.5 %.
- Periodos de 7 s (capa de `primary`) y 5.5 s (capa de `glow`); esta última rota entre 40° y 70°.
- Mezcla: Multiply en claro y Screen en oscuro, porque multiplicar sobre un fondo casi negro haría desaparecer las capas.
- Colores solo por token: `background`, `primary`, `glow`, `onPrimary`.

### Tiempos y curvas: `BreathSplashSpec`

Todas las duraciones, curvas y medidas viven en el objeto `BreathSplashSpec` (`presentation/splash`). Es la **única excepción** a "todo movimiento sale de `Motion`": las curvas `CubicBezier` son propias de esta pantalla, y los `tween` no aparecen en ningún otro archivo del splash. Cambiar un tiempo es cambiar una constante de ese objeto; `BreathSplashSpecTest` comprueba que las fases suman 16 s.

La fase del borde de cada capa es un `tween` lineal infinito. Con frecuencias enteras de seno, gira una vuelta completa sin salto al reiniciar.

### Reducir movimiento (1.5 s)

Con `ANIMATOR_DURATION_SCALE = 0` no hay movimiento: la forma aparece quieta con un fundido de 150 ms, hay una pausa de 450 ms, el color `primary` cubre y el título entra (150 ms, sin subida), una pausa de 600 ms y el overlay se funde en 150 ms.

### Accesibilidad

- El contenedor tiene `contentDescription` "SegundoAire cargando".
- Un toque salta a la fase 6; el overlay se lleva los toques mientras dura.
- Si la actividad se recrea con el splash a medias (rotación), se da por terminado en vez de reiniciarlo.

---

## 10. Iconografía

- Trazo 1.75 sobre rejilla de 24 dp, extremos y uniones redondeados: `Icons.Rounded.*` / Material Symbols Rounded (peso 400, grado 0). Importar solo los vectores usados, no `material-icons-extended` completo.
- `play` y `stop` son los únicos íconos rellenos.
- Íconos de app de ejemplo: formas geométricas sobre `app1…5`. Nunca logos reales.
- Botones de proveedor: íconos genéricos (globo, dispositivo, sobre) hasta producción, donde se usan los recursos oficiales según las guías de cada marca. Excepción: en el login del onboarding (§13) el de Google lleva la "G" de cuatro colores (`GoogleLogoColors`, iguales en claro y oscuro) y el de Apple la manzana en `onBackground`; el del correo conserva el sobre.

## 11. Accesibilidad

- Foco: 2 dp de `focusRing` con 3 dp de separación (3:1 o más en todas las superficies).
- Controles de 48 dp o más. Toggle con `Role.Switch`; segmentado con `selectableGroup()` + `Role.RadioButton`.
- GlassWall: diálogo modal; la cuenta de 10 s de Mindful se anuncia en el botón.
- Texto de IA: `contentDescription` con el texto completo; el revelado es solo visual.

## 12. Componentes de referencia

Inventario a implementar en la Fase 10 (detalle visual en el design system): PrimaryButton (morph Iniciar ↔ Terminar), SecondaryButton, TextButton, ProviderButton, GlassCard (3 niveles), AppToggleRow, SegmentedSelector, RigorSelector, TimerRing, GlassTextField, ChoiceChip, RangeSelector, AnimatedCounter, BreathingCircle, HoldToCommitButton, RevealText, PermissionBanner, EmptyState, Skeleton, BottomNavBar (4 tabs), QuestionScaffold, GlassWall (Mindful / Strict).

## 13. Onboarding "Lo que crece cuando pausas"

Un solo `OnboardingFlow` de 14 pasos sobre un marco de diseño de 390 × 844 dp (la pantalla cambia la densidad del marco para ajustar la columna a la zona segura —barras del sistema y muesca—; el fondo llega a los cuatro bordes). Un árbol dibujado en un solo `Canvas`, con las coordenadas del SVG del prototipo, crece con cada respuesta: la semilla, el brote y la copa en la bienvenida; una hoja por letra del nombre (máx. 10); una flor por meta (máx. 2); el follaje que se dora según las horas de uso; las raíces con el compromiso. El paso vive en el ViewModel y el botón atrás del sistema retrocede un paso.

| # | Paso | Árbol / escena |
|---|---|---|
| 0–2 | Bienvenida (semilla, brote, copa) | Escala 1.55, opacidad 0.75, suelo en y = 470 |
| 3 | Nombre | Una hoja por letra |
| 4 | Objetivo (máx. 2) | Una flor por meta; la tercera sacude la opción |
| 5 | Uso diario | Selector en rama con hoja-pulgar; marchitado 0 / 2 / 5 / 8 / 12 |
| 6 | Momento | Luz del suelo; luciérnagas en noche y madrugada |
| 7 | Revelación | Anillos, contador 1,8 s, fórmula 1,9 s, buena noticia 2,6 s (`h × 365 × 50 ÷ 8.760`) |
| 8 | Respiración | La copa de 0.62 a 1 en 8 s, tres ciclos; "Saltar" → "Continuar" a los 24 s |
| 9 | Plan | Vidrio con cuatro filas cada 90 ms |
| 10 | Compromiso | Mantener 1,5 s; al soltar se vacía en 300 ms; florece todo y avanza a los 1,6 s |
| 11 | Permisos | Accesibilidad y mostrar sobre otras apps (requeridos) y notificaciones (opcional) |
| 12 | Login | Telón `secondaryDeep` que baja en 900 ms y se retira a 270 dp; botón → círculo de 52 dp |
| 13 | Cierre | El telón cubre todo: "Todo listo, {nombre}." |

La barra de progreso es una enredadera que se dibuja de (paso − 2) / 9 en los pasos 3–11.

### Sonido y háptica

`SoundManager` sintetiza los sonidos (`SoundSynth`, sin archivos de audio) y los reproduce como sonificación: respeta el volumen y el modo silencio del teléfono. Interruptor de sonido arriba a la derecha, encendido por defecto. Volumen bajo (`MasterGain`).

| Evento | Sonido | Háptica |
|---|---|---|
| Avanzar de paso | Viento (pasa-banda 400 → 900 Hz, 1,2 s) y dos notas (Do5, Sol5) | `ContextClick` |
| Elegir meta (florece) | Arpegio Mi5, Sol5, Si5 | `Confirm` |
| Quitar meta, elegir horas, marcas del 33 % y 66 % | **Gota** (620 → 470 Hz en 320 ms y armónico 1240 → 940 Hz) | `TextHandleMove` |
| Elegir momento, activar permiso | Pop (740 → 1180 Hz, 140 ms) | `ContextClick` / `Confirm` |
| Completar el compromiso y cerrar el login | Acorde abierto (Sol4, Si4, Re5, Sol5) | `LongPress` y patrón 30 / 40 / 60 ms |
| Tercera meta no permitida | Tono grave (220 → 180 Hz) | `Reject` |
| Escribir el nombre | **Sin sonido** | `TextHandleMove` por letra |

El "tick" agudo del prototipo (1300 Hz, 50 ms) no existe en la app: la gota lo reemplaza en todos los lugares.

### Notas de diseño

- Fondo: el prototipo usa `0xFFDAF1DE`; la app usa `background` (`0xFFEAF4EC`), el fondo vigente tras la Fase 16.5.
- El marco ocupa toda la ventana, también bajo las barras del sistema: el cielo, la colina y el telón se dibujan a pantalla completa (sin franjas a los lados) y solo el contenido con coordenadas del prototipo va en la columna de 390 × 844 dp, centrada en la zona segura. Los íconos de la barra de estado pasan a claros mientras el telón la cubre. El lienzo del árbol lleva márgenes arriba y abajo: las raíces bajan del área y la capa semitransparente las recortaba.
- Cada arranque reproduce primero el splash BreathSplash (§9) sobre el flujo; un toque lo salta, sin texto.
- Los permisos requeridos bloquean "Continuar". Mientras estén pendientes el Panel mostraría su banner; hoy el paso no ofrece "Hacerlo después".
- Correspondencia con el perfil: "Pasar menos tiempo en redes" → `Goal.FREE_TIME`; Mañana → `WAKING_UP`, Tarde → `STUDY_OR_WORK`, Noche → `BEFORE_SLEEP`, Madrugada → `BORED`.
