# Capturas

Los 12 archivos `.png` de esta carpeta son **marcadores de posición** lisos, del color `background` del tema (claro `#DAF1DE`, oscuro `#0D1F23`). Reemplázalos por capturas reales **con el mismo nombre**; el README de la raíz ya los referencia.

| Archivo | Pantalla | Tema |
|---|---|---|
| `welcome-light.png` / `welcome-dark.png` | Bienvenida del onboarding | Claro / Oscuro |
| `panel-light.png` / `panel-dark.png` | Panel | Claro / Oscuro |
| `blocks-light.png` / `blocks-dark.png` | Bloques (timer) | Claro / Oscuro |
| `glasswall-light.png` / `glasswall-dark.png` | GlassWall sobre otra app | Claro / Oscuro |
| `progress-light.png` / `progress-dark.png` | Progreso | Claro / Oscuro |
| `profile-light.png` / `profile-dark.png` | Perfil y ajustes | Claro / Oscuro |

## Cómo capturar

1. Cambia el tema en **Perfil → Tema** (Claro u Oscuro).
2. Con el teléfono conectado por USB y depuración activa:

   ```bash
   adb exec-out screencap -p > docs/screenshots/panel-light.png
   ```

3. Para Progreso, activa antes el **modo demo** en Perfil, así el gráfico semanal tiene datos.
4. Para el GlassWall, abre una app vigilada con los permisos concedidos.

Pautas: mismo dispositivo y orientación vertical en todas, sin datos personales visibles y PNG de 1080 px de ancho o menos.
