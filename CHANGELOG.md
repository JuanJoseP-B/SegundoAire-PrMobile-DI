# Changelog

Todos los cambios relevantes de SegundoAire. El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado, [SemVer](https://semver.org/lang/es/).

## [1.0.0] - 2026-10-15

Primera versión publicada. Resume las fases 1 a 17 del [ROADMAP](ROADMAP.md): del motor de intercepción original a un producto con design system propio, onboarding personalizado, progreso y ajustes.

### Añadido

- **Motor de intercepción (fases 1–6).** `AccessibilityService` y `InterceptorEngine` con decisión en memoria (O(1), sin E/S), y GlassWall sobre otras apps mediante `OverlayWindowManager`. Room y DataStore como fuentes de datos, Hilt para la inyección y cadenas en inglés y español.
- **Temporizador de bloques (fase 7).** Anillo de progreso, duración y rigor.
- **Documentación y Gitflow (fase 8).** `CLAUDE.md`, `SEGUNDOAIRE_SPEC.md`, `DESIGN_SYSTEM.md`, `ROADMAP.md`, plantillas de PR e issue y ramas `main` / `develop`.
- **Design system (fase 9).** Tokens de color claro y oscuro con contraste WCAG AA verificado por tests, escala tipográfica con Manrope, vidrio en tres niveles (Haze en Android 12+, respaldo en 10–11), espaciado, radios y movimiento con soporte de *reducir movimiento*.
- **Componentes base (fase 10).** Botones, tarjetas de vidrio, toggles, selector segmentado, selector de rigor, `TimerRing`, campos de texto, chips, contadores animados, círculo de respiración, botón de mantener presionado y estados vacíos, cada uno con previews en claro y oscuro. Catálogo de componentes solo para debug.
- **Onboarding personalizado (fase 11).** Bienvenida, nombre, objetivos, uso diario, momentos de riesgo, revelación con la fórmula visible, demo de respiración, plan personalizado y compromiso con vibración háptica.
- **Cuenta simulada, permisos y apps (fase 12).** Cuenta, login, registro y recuperación de contraseña con validación real y sin backend; permisos guiados con refresco al volver de Ajustes; selección de apps con búsqueda y sugeridas.
- **Shell y Panel (fase 13).** Barra inferior de vidrio con cuatro secciones, transición entre pestañas, saludo personalizado, apps vigiladas, mensaje motivacional simulado y avisos de permisos.
- **Bloques y GlassWall (fase 14).** Temporizador basado en reloj de pared que sobrevive a rotaciones y al fondo, bloque conectado al motor, entrada calmada del GlassWall (blur o scrim) y rigor **Mindful** (continuar tras 10 s) y **Strict** (sin continuar durante el bloque).
- **Progreso (fase 15).** Registro de intercepciones en Room (esquema v2, migración 1→2 con test instrumentado), minutos recuperados, aperturas evitadas, racha, gráfico semanal y modo demo con datos de ejemplo.
- **Perfil y ajustes (fase 16).** Edición de nombre y objetivos, idioma ES/EN dentro de la app con soporte para los ajustes de idioma por app de Android 13+, tema sistema/claro/oscuro, tutorial de cinco tarjetas, interruptor de modo demo y reinicio de la app con confirmación.
- **Pulido y release (fase 17).**
  - Ícono adaptativo con la forma de respiración y capa monocromática para íconos temáticos.
  - Splash con la SplashScreen API, sobre `background` y sin texto, que respeta el tema del sistema.
  - Firma de release leída de `keystore.properties`.
  - `README.md` bilingüe con sección de capturas.

### Cambiado

- Revisión visual de todas las pantallas en claro y oscuro: las etiquetas de los botones bajan de línea con fuente al 200 %, el selector segmentado llega a 48 dp de área táctil, y se dejaron de recortar el saludo del Panel, las filas toggle, el chip demo y los contadores animados. El número del timer y las etiquetas de la barra inferior limitan su escala para no salirse de su contenedor.
- `MainActivity` pasa a `AppCompatActivity` y los temas a AppCompat, requisito del idioma en caliente.
- El build de release activa R8 y la reducción de recursos.

### Corregido

- Cierre de la app al abrir Bloques, Perfil o Permisos por un `ClassCastException` en `PrimaryButton`: la especificación de transición del color debía anotarse como `@Composable`.
- Un bloque Strict en curso sobrevivía al reinicio de la app al vivir en memoria; ahora se olvida junto con los datos.

### Seguridad y privacidad

- Ningún dato sale del dispositivo: la app no declara el permiso de Internet.
- El build de release nunca se firma con la clave de debug; sin `keystore.properties` el build se detiene con un mensaje claro. `keystore.properties` y los keystores están en `.gitignore`.

## [0.1.0] - 2026-10-06

Línea base de las fases 1 a 6: modelos de dominio, repositorios, Room, DataStore, servicio de accesibilidad, motor de intercepción, overlay, onboarding inicial y tema glassmorphism, con cadenas en inglés y español.

[1.0.0]: https://github.com/JuanJoseP-B/SegundoAire-PrMobile-DI/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/JuanJoseP-B/SegundoAire-PrMobile-DI/releases/tag/v0.1.0
