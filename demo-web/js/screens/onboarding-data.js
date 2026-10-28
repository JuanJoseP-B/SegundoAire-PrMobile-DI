/*
 * Static content for the onboarding "Lo que crece cuando pausas": copy, option lists
 * and the tree geometry (same coordinates as the design prototype, 390 x 844 frame).
 */
(function (SA) {
  'use strict';

  var ICON = {
    leaf: 'M5 19c0-8 6-14 14-14 0 8-6 14-14 14zM5 19l7-7',
    moon: 'M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z',
    flame: 'M12 3c4 4 5 9 0 18-5-9-4-14 0-18zM12 8v9',
    sprout: 'M12 3v8M12 11c-3 2-5 5-6 9M12 11c3 2 5 5 6 9M12 11v9',
    drop: 'M12 3c4 5 6 8 6 11a6 6 0 0 1-12 0c0-3 2-6 6-11z',
    sunrise: 'M3 18h18M6 14a6 6 0 0 1 12 0M12 4v3M4.5 8.5l2 2M19.5 8.5l-2 2',
    sun: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5L19 19M5 19l1.5-1.5M17.5 6.5L19 5',
    stars: 'M12 3l1.5 3.5L17 8l-3.5 1.5L12 13l-1.5-3.5L7 8l3.5-1.5zM18 15l.8 1.7 1.7.8-1.7.8-.8 1.7-.8-1.7-1.7-.8 1.7-.8z',
    sunSmall: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M2 12h2M20 12h2',
    chart: 'M4 19V9M10 19V5M16 19v-7M22 19H2',
    window: 'M4 6h16v12H4zM8 10h8v4H8z',
    bell: 'M6 16V11a6 6 0 0 1 12 0v5l2 2H4zM10 20a2 2 0 0 0 4 0',
    check: 'M5 12l5 5 9-10'
  };

  SA.onbData = {
    ICON: ICON,
    FALLBACK_NAME: 'Ana',
    MAX_NAME: 20,
    MAX_GOALS: 2,
    GROUND_B: 470,

    welcome: [
      { eyebrow: '01 · Semilla', a: 'Respira ', em: 'antes', b: ' de abrir', body: 'Una pausa breve entre tú y las apps que te atrapan.' },
      { eyebrow: '02 · Brote', a: 'Una pausa, ', em: 'no', b: ' un castigo', body: 'Antes de abrir una app que elijas, tendrás un momento para decidir.' },
      { eyebrow: '03 · Copa', a: 'Recupera ', em: 'tu tiempo', b: '', body: 'Cada pausa que eliges hace crecer algo tuyo.' }
    ],

    goals: [
      { label: 'Pasar menos tiempo en redes', icon: ICON.leaf, filled: true },
      { label: 'Dormir mejor', icon: ICON.moon, filled: true },
      { label: 'Concentrarme al estudiar', icon: ICON.flame, filled: true },
      { label: 'Estar más presente', icon: ICON.sprout, filled: false },
      { label: 'Bajar la ansiedad', icon: ICON.drop, filled: true }
    ],

    usage: {
      labels: ['menos de 2 h', '2 – 4 h', '4 – 6 h', '6 – 8 h', 'más de 8 h'],
      caps: ['<2', '2–4', '4–6', '6–8', '8+'],
      hours: [1.5, 3, 5, 7, 9],
      wilt: [0, 2, 5, 8, 12],
      step: 77.5,
      initial: 2
    },

    moments: [
      { label: 'Mañana', hours: '6 – 12 h', icon: ICON.sunrise, plan: 'Mañana · 07:00 – 08:00' },
      { label: 'Tarde', hours: '12 – 18 h', icon: ICON.sun, plan: 'Tarde · 14:00 – 15:00' },
      { label: 'Noche', hours: '18 – 24 h', icon: ICON.moon, plan: 'Noche · 21:00 – 22:00' },
      { label: 'Madrugada', hours: '0 – 6 h', icon: ICON.stars, plan: 'Madrugada · 00:00 – 01:00' }
    ],

    /** Step 9 of 9: the two required permissions and the optional one (visual only). */
    perms: [
      { title: 'Accesibilidad', desc: 'Para saber qué app se abre. Requerido.', icon: ICON.chart, required: true },
      { title: 'Mostrar sobre otras apps', desc: 'Para mostrar la pausa a tiempo. Requerido.', icon: ICON.window, required: true },
      { title: 'Notificaciones', desc: 'Para avisarte del bloque. Opcional.', icon: ICON.bell, required: false }
    ],

    /** Four rows of the plan card, built from the answers so far. */
    planRows: function (st) {
      var goals = st.picks.map(function (i) { return SA.onbData.goals[i].label; });
      var moment = st.moment >= 0 ? st.moment : 2;
      return [
        { k: 'Tus metas', v: goals.length ? goals.join(' · ') : 'Vivir con más calma', icon: ICON.leaf },
        { k: 'Primer bloque de enfoque', v: SA.onbData.moments[moment].plan, icon: ICON.sunSmall },
        { k: 'Modo Mindful', v: 'Una pausa de 10 s antes de abrir', icon: ICON.flame },
        { k: 'Meta diaria', v: 'Recuperar 1 h al día', icon: ICON.drop }
      ];
    },

    tree: {
      // [path, width, stage, delay ms]
      segs: [
        ['M195 515 C195 490 193 470 196 440', 5, 1, 0], ['M195 515 C190 450 202 380 195 300', 10, 2, 0],
        ['M196 400 C215 385 235 380 255 362', 4.5, 2, 400], ['M194 370 C175 355 158 345 140 328', 4.5, 2, 480],
        ['M195 300 C190 250 200 210 195 165', 8, 3, 0], ['M195 250 C220 235 245 228 278 210', 4.5, 3, 350],
        ['M195 232 C170 218 150 208 115 196', 4.5, 3, 420], ['M195 190 C212 160 232 140 262 126', 3.5, 4, 0],
        ['M195 185 C178 155 158 140 128 124', 3.5, 4, 80], ['M195 170 L195 110', 3.5, 4, 120]
      ],
      // [path, width]
      roots: [
        ['M195 515 C190 560 170 600 140 650', 5], ['M195 515 C200 570 230 610 270 660', 5],
        ['M195 515 C196 580 192 640 195 710', 6], ['M182 560 C150 580 120 590 80 600', 3],
        ['M210 572 C250 585 280 592 315 600', 3]
      ],
      // [cx, cy, r, stage]
      blobs: [
        [196, 300, 40, 2], [255, 352, 32, 2], [140, 318, 32, 2], [228, 318, 28, 2], [165, 300, 28, 2],
        [195, 170, 52, 3], [278, 205, 40, 3], [115, 192, 40, 3], [245, 240, 34, 3], [148, 232, 34, 3], [205, 228, 36, 3],
        [195, 100, 56, 4], [262, 118, 46, 4], [128, 116, 46, 4], [305, 160, 38, 4], [85, 155, 38, 4],
        [235, 150, 44, 4], [155, 148, 44, 4], [195, 140, 48, 4]
      ],
      // [x, y, rotation, stage, scale]
      leaves: [
        [196, 442, -150, 1, 1.5], [196, 442, -30, 1, 1.5], [196, 452, -160, 1, 1.1], [196, 452, -20, 1, 1.1],
        [300, 330, -15, 2, 1.2], [92, 300, 195, 2, 1.2], [330, 180, -10, 3, 1.2], [62, 170, 190, 3, 1.2]
      ],
      flowers: [[262, 112], [128, 110], [195, 82], [250, 160], [140, 158]],
      // [left, top, delay ms, duration ms]
      fall: [[40, 120, 0, 2600], [120, 60, 180, 3000], [210, 140, 90, 2400], [300, 80, 260, 2900],
        [80, 220, 340, 2700], [250, 30, 420, 3200], [340, 180, 140, 2500]],
      LEAF_SHAPE: 'M2 12 C6 4 18 4 22 12 C18 20 6 20 2 12Z',
      PETAL_SHAPE: 'M12 3 C17 7 17 15 12 21 C7 15 7 7 12 3Z'
    }
  };

  SA.registry.defineScreens('onboarding', [
    { id: 'welcome-1', label: 'Bienvenida 1 · Semilla', hint: 'La semilla en el suelo, con ondas que se expanden.' },
    { id: 'welcome-2', label: 'Bienvenida 2 · Brote', hint: 'El tronco y las primeras hojas brotan.' },
    { id: 'welcome-3', label: 'Bienvenida 3 · Copa', hint: 'Aparece la copa del árbol.' },
    { id: 'name', label: 'Paso 1 de 9 · Nombre', hint: 'Cada letra del nombre da una hoja al árbol.' },
    { id: 'goals', label: 'Paso 2 de 9 · Metas', hint: 'Elige hasta 2 metas: cada una es una flor. La tercera sacude la opción.' },
    { id: 'usage', label: 'Paso 3 de 9 · Uso diario', hint: 'Más horas de uso, más follaje marchito.' },
    { id: 'moment', label: 'Paso 4 de 9 · Momento', hint: 'El momento elegido cambia la luz del suelo.' },
    { id: 'reveal', label: 'Paso 5 de 9 · Revelación', hint: 'Anillos y un contador: años frente a la pantalla.' },
    { id: 'breath', label: 'Paso 6 de 9 · Respiración', hint: 'Tres respiraciones con la copa del árbol.' },
    { id: 'plan', label: 'Paso 7 de 9 · Plan', hint: 'Un plan armado con las respuestas.' },
    { id: 'commit', label: 'Paso 8 de 9 · Compromiso', hint: 'Mantén presionado el botón: crecen las raíces.' },
    { id: 'perms', label: 'Paso 9 de 9 · Permisos', hint: 'Activar un permiso solo cambia su aspecto: la demo no pide nada de verdad.' }
  ]);
})(window.SA = window.SA || {});
