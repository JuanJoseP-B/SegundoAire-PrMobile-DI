# Fase 17 · App principal (4 paneles) + GlassWall — plan aprobado

Fuente: `docs/design/app-dashboard/PROMPT.md` y `app-dashboard-referencia.html` (lógica en el `<script>` final: `renderVals()`, `openWall`, `closeWall`). Geometría del árbol: `onboarding-arbol-referencia.html`.

## Git

- Rama `feature/fase-17-app-principal`, apilada sobre `feature/fase-16-8-onboarding-arbol`.
- PR con `--base feature/fase-16-8-onboarding-arbol`. Al mergear la 16.8 a `develop` (con `--delete-branch`) GitHub reorienta el PR a `develop`; avisar si no lo hace. **No mergear nada.**
- `git add` siempre con rutas explícitas y `git commit -- <rutas>`: hay dos PNG borrados en *staged* (`docs/desing/onboarding-arbol/...`) que no se tocan.
- Para esta fase manda el prompt sobre CLAUDE.md: rama nueva, PR y tests unitarios. Al final, proponer (diff, sin commit) el cambio de CLAUDE.md al flujo real: ramas por fase, PR hacia `develop`, tests desde la Fase 9.
- Tests: escribirlos y correr **una vez al final** `testDebugUnitTest` y `assembleDebug` (JDK 21, `--no-daemon --max-workers=2`).

## Ejecución en 3 etapas (reporte del CLAUDE.md y PARAR al final de cada una)

- **Etapa A:** tokens, TreeState, plantedAt, BottomNav, Panel y coach.
- **Etapa B:** Bloques con modo enfoque, Progreso y Perfil.
- **Etapa C:** migración de Room, medición de minutos, GlassWall, sonido y háptica, catálogo, tests y PR.

## Decisiones de producto

- **TreeState** en `domain/model`. La geometría del árbol se mueve a un módulo compartido (`presentation/tree/`, sin duplicar); el onboarding sigue igual.
- **plantedAt** en DataStore al completar el onboarding; usuarios existentes: `PackageInfo.firstInstallTime`.
- **Hojas del árbol:** una por pausa respetada (GlassWall → "Volver") en la semana, máx. 8 (`leafSlots`). DataStore guarda cuántas ya se vieron: al volver al Panel solo brotan las nuevas (bloom + Confirm).
- **Panel:** *aperturas evitadas hoy* = intercepciones del día; *pausas respetadas* = las que terminaron en "Volver"; *min recuperados hoy* = minutos reales de bloques completados (sin sumar nada inventado).
- **Bloque Strict:** terminar antes exige mantener presionado (regla actual).
- **Coach:** `CoachRepository` (dominio) + implementación local con estadísticas reales; mensajes estructurados que la UI mapea a `strings.xml`. "Crear un bloque" navega a Bloques sin prometer horario. "Recuérdamelo" muestra un toast que no promete una notificación real. "Iniciar 15 min" inicia el bloque; "Ver progreso" va a Progreso.
- **Perfil:** avatar, nombre, "Plantado el…", metas en chips + "Editar" (hoja con la lógica actual). Ajustes: Plan y modo, Sonidos y vibración (switch real persistido, controla `SoundManager` y háptica), Permisos (estado), Cuenta → subpantalla con tema, idioma, modo demo, reiniciar y tutorial. **Sin fila "Recordatorios".** "Compartir mi árbol": imagen del árbol y los anillos vía FileProvider + `ACTION_SEND`, sin datos sensibles.
- **Bloques:** "Editar" apps vigiladas en el encabezado de la sección. Íconos genéricos `app1..5` (sin logos reales). Sin bloques programados.
- **"Simular abrir app"** solo en el catálogo.
- **Colores:** se conservan los tokens vigentes (`background`, `glass*`, `onBackgroundMuted`…) y solo se agregan los nuevos.
- **GlassWall sin bloque:** se comporta como Mindful (chips + "Continuar en 10…"), con el botón principal "Volver".

## Tokens nuevos (Color.kt + DESIGN_SYSTEM.md)

- Árbol / tarjetas: `hill`, `trunk`, `leaf`, `petal`, `wood`, `aiCard`, `tree1..3` (+ alfa).
- Overlay: `wallScrim`, `wallOn`, `wallMuted`, `wallBtn`, `wallBtnOn`, `wallChip`, `wallTrunk`, `wallLeaf`, `wallWinter`.
- Luz del héroe por franja y tema: `heroSky*`.
- Modo enfoque (iguales en claro y oscuro, fondo `#0B2B26`): `focus*`.
- Motion: `navIndicator` (520 ms, (.3,1.2,.5,1)), `page` (24 dp, 520 ms (.22,1,.36,1) + fundido 320 ms), `think` (2,4 s), skeleton, puntos, caída/subida de la hoja del muro, toast.

## Minutos por app (Etapa C)

- Con eventos del `AccessibilityService`, solo apps vigiladas. Sin `UsageStatsManager` ni permisos nuevos; la cifra se presenta como estimación.
- Acumula en memoria y vuelca a Room por lotes (cambio de app, pantalla apagada, periódico), siempre fuera del camino crítico (<50 ms).
- `ACTION_SCREEN_OFF` cierra la sesión; se descartan huecos anormales.
- **Una sola migración Room v2→v3**: tabla de uso + columna de intención en `interception_events`. `exportSchema = true` y test con `MigrationTestHelper`.
- El modo demo alimenta las mismas tablas.

## GlassWall (Etapa C)

- Pantalla completa, coreografía de la referencia: 0 ms blur (14 dp, `FLAG_BLUR_BEHIND` 12+; scrim en 10–11) + scrim; 320 ms contenido (fundido, escala 0.92→1); hoja que cae 1,6 s; 1,7 s respiración de 8 s (copa 0.62↔1, Inhala/Exhala, sonido `calm`, sin botones); 9,7 s bloque de contexto (Mindful: chips de intención guardados para el coach, "Volver a mi bloque", "Continuar en 10…" con tallo que se dibuja y hoja que brota; Strict: copa gris, chip "Se abre a las {hora}", sin continuar).
- "Volver a mi bloque": la hoja sube (1 s) con gota, se abre `MainActivity` en el Panel donde brota la hoja (bloom + Confirm). Si `startActivity` falla o es bloqueada: `GLOBAL_ACTION_HOME` y log.
- "Continuar": fundido y toast "Disfruta tus 5 min".
- Diálogo modal, foco inicial en "Volver a mi bloque". Reducir movimiento: respiración por opacidad, sin hoja que cae ni sube.

## Sonido y háptica

| Evento | Sonido | Háptica |
|---|---|---|
| Duración, modo, día, toggle; acciones de la IA | `drop` | `ContextClick` |
| Iniciar bloque | `commit` | patrón 20/30/40 |
| Entrar a la respiración del GlassWall | `calm` | — |
| Volver a mi bloque | `drop` y `bloom` al brotar | `Confirm` |
| Terminar un bloque completo | `bloom` | `Confirm` |

Todo se apaga desde Perfil → Sonidos y vibración, y se respeta el modo silencio.

## Lógica pura con tests

Progreso y tiempo restante del bloque, posición de la punta del anillo, grosor del anillo por minutos, flor por día, cuenta regresiva Mindful, saludo y luz por hora, TreeState. Los tests viejos de ViewModels se ajustan a la nueva lógica (no se borran sin reemplazo).

## Entrega final

Previews claro/oscuro de cada pantalla; entradas "App principal" y "GlassWall" en el catálogo con botón "Repetir"; PR hacia la 16.8.
