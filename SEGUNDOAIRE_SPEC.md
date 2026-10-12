# SEGUNDOAIRE — Especificación de producto v2

> v2 (12 oct 2026): incorpora el design system, el onboarding personalizado, la cuenta simulada y la navegación de 4 secciones. Los valores visuales viven en `DESIGN_SYSTEM.md`; el plan de trabajo en `ROADMAP.md`.

## 1. Visión

SegundoAire es una app nativa para Android 10+ de bienestar digital. Cuando el usuario abre una app que eligió vigilar, la intercepta en menos de 50 ms con un `AccessibilityService` y muestra una pausa, el **GlassWall** (`SYSTEM_ALERT_WINDOW`), antes de dejarla abrir. La idea no es prohibir, sino devolver un segundo de conciencia: "Toma un segundo aire".

Funciona 100 % en local. Es un proyecto de la materia Diseño de Interfaces, por lo que se diseña y construye como un producto real; algunas partes son simuladas (ver §5).

## 2. Principios de experiencia

- **Calma antes que urgencia.** Nada regaña al usuario; el lenguaje es cálido y sin culpa.
- **Una idea por pantalla**, con mucho aire y un solo dato de impacto.
- **Personalización visible:** el nombre y el objetivo del usuario reaparecen en el Panel, el GlassWall y los mensajes.
- **Movimiento con propósito:** cada animación responde a un gesto o a un cambio de estado.
- **Ética de diseño:** cifras honestas y con su fórmula visible; registro opcional; sin patrones oscuros.

## 3. Usuario objetivo

Personas de 16 a 35 años que sienten que el celular les quita tiempo de estudio, sueño o presencia, y que quieren reducirlo sin un bloqueo total.

## 4. Flujos

### 4.1 Primera ejecución
1. **Bienvenida** (3 tarjetas): el problema, cómo funciona la pausa y la promesa.
2. **Nombre.**
3. **Objetivos** (máximo 2): concentrarme, dormir mejor, estar presente con los míos, cuidar mi salud mental, recuperar tiempo para lo que me gusta.
4. **Uso diario estimado:** menos de 2 h · 2–4 h · 4–6 h · 6–8 h · más de 8 h.
5. **Momento de mayor uso:** al despertar · antes de dormir · estudiando o trabajando · cuando me aburro.
6. **Revelación:** años de pantalla proyectados (con la fórmula visible) y tiempo recuperable.
7. **Demo de respiración** (3 ciclos).
8. **Plan personalizado:** objetivo, rigor sugerido y horarios de riesgo.
9. **Compromiso** con botón de mantener presionado.
10. **Cuenta:** Google / Apple / correo / continuar sin cuenta.
11. **Permisos guiados:** accesibilidad y mostrar sobre otras apps.
12. **Elegir apps a vigilar** → Panel.

### 4.2 Uso diario
- **Panel:** saludo, dato del día, apps vigiladas con su toggle y avisos de permisos.
- **Bloques:** duración (5/10/15/30 min), rigor (Mindful/Strict), TimerRing e iniciar/terminar.
- **Progreso:** minutos recuperados, aperturas evitadas, racha y gráfico semanal.
- **Perfil:** datos del usuario, cuentas conectadas y ajustes.

### 4.3 Intercepción (GlassWall)
- Entrada calmada (desenfoque o scrim y luego el contenido), círculo de respiración y mensaje personalizado.
- **Sin bloque activo:** "Volver" o "Continuar".
- **Bloque activo Mindful:** "Continuar" se habilita tras 10 s.
- **Bloque activo Strict:** solo "Volver" hasta que termine el bloque.

## 5. Alcance: funcional vs. simulado

| Área | Estado |
|---|---|
| Intercepción, GlassWall, bloques, rigor | Funcional |
| Onboarding y perfil (DataStore) | Funcional |
| Sesiones y estadísticas (Room) | Funcional |
| Login, registro, recuperar contraseña, proveedores | Simulado: UI y validación completas, sin backend |
| Cuentas conectadas | Simulado |
| Mensajes de IA | Simulado: banco local de mensajes detrás de una interfaz reemplazable |
| Sincronización en la nube, notificaciones push | Fuera de alcance |

## 6. Requisitos no funcionales

- **Rendimiento:** decisión de intercepción en menos de 50 ms, sin asignaciones ni animaciones en ese camino.
- **Plataforma:** minSdk 29, target/compile 35. El blur de fondo requiere Android 12+, con respaldo de scrim y opacidad.
- **Accesibilidad:** WCAG AA en contraste, áreas táctiles de 48 dp o más, soporte de TalkBack y de reducir movimiento.
- **i18n:** inglés y español desde el primer día; ningún texto hardcodeado.
- **Privacidad:** ningún dato sale del dispositivo.
- **Batería:** sin trabajo continuo fuera del servicio de accesibilidad; tareas diferidas con WorkManager y AlarmManager.

## 7. Arquitectura

Clean Architecture + MVVM + UDF.

- **Presentación:** Jetpack Compose (Single-Activity), `StateFlow`, design system en `presentation/designsystem`.
- **Dominio:** casos de uso en Kotlin puro e interfaces de repositorio.
- **Datos:** Room (SSOT, con esquema exportado y migraciones) y DataStore Preferences.
- **DI:** Dagger Hilt.
- **Motor de fondo:** `AccessibilityService`, `InterceptorEngine`, `OverlayWindowManager`, `WorkManager` y `AlarmManager`.

## 8. Modelo de datos (resumen)

- `BlockedApp` (Room): paquete, nombre, minutos de pausa, vigilada sí/no.
- `UserProfile` (DataStore): nombre, objetivos, rango de uso, momentos de riesgo, rigor sugerido, modo de cuenta, onboarding y setup completos.
- `BlockSession` (Room, Fase 16): inicio, fin, duración, rigor, completada sí/no.
- `InterceptionEvent` (Room, Fase 16): paquete, fecha, resultado (volvió / continuó).