# Prompt para Claude Code: App principal (4 paneles) + GlassWall

<rol>
Eres un ingeniero Android senior experto en Jetpack Compose, Canvas, animación con springs, overlays de sistema y audio/háptica.
</rol>

<contexto>
SegundoAire es una app Android nativa (Kotlin, Compose, Material 3), con Clean + MVVM + UDF y StateFlow, en ES/EN. El onboarding con el árbol ya está implementado o en curso (rama `feature/fase-16-8-onboarding-arbol`). Reutiliza su `TreeState`, el árbol en Canvas, los tokens de Motion, el `SoundManager` y la tipografía Fraunces.

Antes de nada, lee CLAUDE.md, DESIGN_SYSTEM.md, la navegación actual (BottomNav, AppNavHost), las pantallas existentes de Panel, Bloques, Progreso y Perfil, y el GlassWall actual (servicio de overlay y AccessibilityService o UsageStats).

FUENTE DE VERDAD (en `docs/design/app-dashboard/`):
- `app-dashboard-referencia.html`: prototipo aprobado. Toda la lógica está en el `<script>` final:
  - `renderVals()` contiene los tokens de color claro/oscuro (objeto `T`), la geometría, los textos y los tiempos.
  - El CSS de `<helmet>` contiene las keyframes (respiración, hojas, brillo de la IA, etc.).
  - El prop `dark` alterna el tema.
  Léelo completo y replícalo 1:1. Si algo no se puede traducir, avísame antes de improvisar.
- `onboarding-arbol-referencia.html`: geometría del árbol (`segDefs`, `blobDefs`), compartida con el Panel.
</contexto>

<temas>
Implementa modo claro y oscuro con los valores del objeto `T` de la referencia, mapeados a tokens del tema (no hex en pantallas). Los tokens nuevos son:
- `hill`, `trunk`, `leaf`, `petal` y `wood`.
- `aiCard`.
- Los tonos del árbol (`tree` = 3 colores con su opacidad).
- Los del overlay: `wallScrim`, `wallOn`, `wallMuted`, `wallBtn`, `wallChip`.

Documéntalos en DESIGN_SYSTEM.md. El modo enfoque siempre es oscuro (#0B2B26 = secondary-deep), en los dos temas.
</temas>

<pantallas>
**Navegación:** BottomNav flotante de vidrio intenso, con un indicador de 64×32 que se desliza con spring (cubic-bezier(.3, 1.2, .5, 1), 520 ms). La pantalla entrante se desplaza 24dp en la dirección del tab y hace un fundido.

**1 · Panel**
- Saludo según la hora ("Buenos días / Buenas tardes / Buenas noches") y el nombre en Fraunces.
- Héroe de 300dp:
  - El árbol completo de `TreeState`, a escala 0.5, con un balanceo de ±0,8° en 7 s.
  - Colina y luz radial según la hora real. Luciérnagas entre las 20:00 y las 6:00.
  - Las flores salen de las metas del onboarding.
- Dato "X min recuperados hoy" (Fraunces 44) más pausas respetadas y aperturas evitadas.
- **Hoja nueva en vivo:** cada pausa respetada en el GlassWall agrega una hoja al árbol (posiciones en `leafSlots`), que brota con spring al volver al Panel.
- Barra de bloque activo (si lo hay): vidrio, anillo de 36dp y "Quedan m:ss". Al tocarla, abre el modo enfoque.
- **Coach IA (protagonista):**
  - Mientras piensa, un borde con brillo cónico de `glow` gira (2,4 s), 3 puntos laten y hay líneas skeleton. Dura unos 1,4 s.
  - Revelado del texto: Fraunces 20/28 que aparece por caracteres.
  - Las acciones (una sólida y una de vidrio) entran solo cuando termina el revelado.
  - Botón para pedir otra sugerencia, más los chips de "Pregúntale".
  - Las acciones navegan o actúan de verdad: crear bloque → Bloques, iniciar 15 min → bloque, ver progreso → Progreso, recordatorio → toast.
  - Con TalkBack, el texto completo se anuncia de una vez.
  - Conecta el coach a un `CoachRepository` con una interfaz; por ahora, mensajes locales generados a partir de las estadísticas reales (los de la referencia son ejemplos).

**2 · Bloques**
- Tarjeta "Iniciar bloque":
  - Segmentado de duración (5/10/15/30) con un indicador que se desliza con spring.
  - Modo Mindful o Strict (borde primary de 2px cuando está activo).
  - HoldButton de 1,5 s con relleno; al soltar, se vacía en 300 ms.
- Al completar → **modo enfoque** a pantalla completa:
  - TimerRing de 240dp con una punta `glow` que respira.
  - Tiempo en display-timer, 72sp peso 300 y tabular.
  - Frase "Tu árbol crece mientras no miras." y el árbol en silueta respirando abajo.
  - Botones "Terminar bloque" y minimizar.
  - El tiempo es real (×1; la referencia usa ×30 solo para la demo).
- Apps vigiladas: AppRow con un ícono genérico de color (app-1…4, sin logos), los minutos de hoy y un toggle con spring (el pulgar crece de 16 a 24dp).
- NO hay bloques programados.

**3 · Progreso**
- Corte de tronco: un anillo por día de historial. El grosor depende de los minutos; los días con meta van en `leaf` y el resto en `trunk`.
- Semana en 7 tallos con altura animada (spring) y una flor si se cumplieron 30 min o más. Los días futuros van con borde punteado.
- Tocar un día ilumina su anillo en `glow` y actualiza el título ("Jue 15 · 52 min").
- 4 tarjetas de datos: recuperados en la semana, aperturas evitadas, mejor racha y hora más difícil.
- Sin logros ni extras.

**4 · Perfil**
- Avatar con un árbol pequeño, nombre en Fraunces y "Plantado el {fecha del onboarding}".
- Metas como chips, con un botón para editarlas.
- Ajustes en vidrio:
  - Plan y modo.
  - Sonidos y vibración (switch real, conectado al SoundManager).
  - Recordatorios.
  - Permisos (con su estado).
  - Cuenta.
- Botón "Compartir mi árbol": genera una imagen del árbol y los anillos y la comparte con un Intent; sin datos sensibles.
</pantallas>

<glasswall>
Es el overlay que aparece sobre la app interceptada (`TYPE_APPLICATION_OVERLAY` o la Activity actual del GlassWall). Coreografía exacta de la referencia (`openWall`, `closeWall`):

1. **0 ms:** la app de fondo se desenfoca (RenderEffect 14 en Android 12+; en versiones anteriores, solo scrim) y sube el scrim. A los 320 ms entra el contenido con fundido y escala de 0.92 a 1. Una hoja cae al centro (1,6 s).
2. **1,7 s:** respiración de 8 s. La copa escala de 0.62 a 1 y vuelve a 0.62, con "Inhala / Exhala" y el sonido `calm`. **Sin botones** durante este tiempo.
3. **9,7 s:** sube el bloque de contexto:
   - "Llevas {min} min en esta app hoy. ¿Es lo que querías?" y "Tu árbol lleva {n} hojas esta semana".
   - **Mindful:** chips "¿Para qué entras?" (Responder un mensaje · Buscar algo · Solo aburrimiento), guardados para el coach. Luego "Volver a mi bloque" (principal) y "Continuar en 10…", con un tallo que se dibuja durante 10 s y una hoja que brota al habilitarse ("Continuar 5 min").
   - **Strict:** copa gris (invierno), chip con candado "Se abre a las {hora}" y sin opción de continuar.
4. **"Volver a mi bloque":** la hoja sube y se va (1 s) con sonido de gota. Se cierra la app interceptada y se vuelve al Panel, donde brota la hoja nueva con el arpegio `bloom` y la háptica. Suma a las pausas respetadas.
5. **"Continuar":** el overlay se desvanece y aparece el toast "Disfruta tus 5 min". Sin regaños.

Es un diálogo modal: el foco empieza en "Volver a mi bloque". Con Reducir movimiento, la respiración pasa a un cambio de opacidad, sin hoja que cae ni sube.
</glasswall>

<sonido_y_haptica>
Usa el mismo SoundManager del onboarding (sin sonido de teclado y sin "tick"):

| Evento | Sonido | Háptica |
|---|---|---|
| Seleccionar duración, modo, día o toggle; acciones de la IA | `drop` (gota) | `ContextClick` |
| Iniciar bloque | `commit` (acorde) | Patrón 20/30/40 |
| Entrar a la respiración del GlassWall | `calm` (dos notas largas) | — |
| Volver a mi bloque | `drop` y luego `bloom` al brotar la hoja | `Confirm` |
| Terminar un bloque completo | `bloom` | `Confirm` |

Todo se desactiva desde Perfil → Sonidos y vibración, y se respeta el modo silencio.
</sonido_y_haptica>

<reglas>
- Nada de hex en pantallas; nada de `spring()` ni `tween()` sueltos (todo sale de Motion.kt).
- Los valores animados se leen en la fase de dibujo.
- Textos en strings.xml (ES/EN). Números tabulares y decimales con coma en ES.
- La lógica pura va en funciones testeables:
  - progreso y tiempo restante del bloque,
  - posición de la punta del anillo,
  - grosor del anillo según los minutos,
  - flor por día,
  - cuenta regresiva de Mindful,
  - selección de saludo y luz por hora.
- Arquitectura: un ViewModel por pantalla, con el estado de tab en el NavHost, y `TreeState` y estadísticas compartidos desde el repositorio.
</reglas>

<entrega>
1. Primero, un plan corto: archivos, tokens nuevos, cambios en el overlay y riesgos. Espera mi OK.
2. Haz commits por bloque: temas y tokens → BottomNav → Panel (árbol y coach IA) → Bloques y modo enfoque → Progreso → Perfil → GlassWall → sonido y háptica.
3. Agrega previews claro/oscuro de cada pantalla, y entradas "App principal" y "GlassWall" en el catálogo con un botón "Repetir".
4. Tests unitarios de la lógica pura.
5. Crea la rama `feature/fase-17-app-principal` desde develop según CLAUDE.md. Abre un PR hacia develop y NO hagas merge: lo reviso en el teléfono.
</entrega>
