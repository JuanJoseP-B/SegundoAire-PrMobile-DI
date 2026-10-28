/*
 * Blocks tab: "Iniciar bloque" card (duration, mode, hold-to-start button) and the watched apps.
 * Mock-up only: starting a block opens the focus mode with a decorative, accelerated timer.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var page = null;
  var q = {};
  var st = null;
  var raf = 0;
  var lastT = 0;

  var DURATIONS = [5, 10, 15, 30];
  var MODES = [
    { id: 'mindful', label: 'Mindful', desc: 'Pausa de 10 s y decides.' },
    { id: 'strict', label: 'Strict', desc: 'Sin salida hasta que termine.' }
  ];
  // Example watched apps: generic icons on the app1..app4 tokens (never brand logos)
  var APPS = [
    { name: 'Videos cortos', min: 48 },
    { name: 'Red social', min: 31 },
    { name: 'Mensajería', min: 22 },
    { name: 'Juegos', min: 9 }
  ];

  function fresh() { return { hold: 0, holding: false, apps: [true, true, true, false] }; }

  function esc(t) { return main.esc(t); }

  // ---------- templates ----------

  function html() {
    var durs = DURATIONS.map(function (d, i) {
      return '<button type="button" class="mn-seg-btn" role="radio" aria-checked="false" data-dur="' + i + '">' + d + ' min</button>';
    }).join('');
    var modes = MODES.map(function (m) {
      return '<button type="button" class="mn-mode" role="radio" aria-checked="false" data-mode="' + m.id + '">' +
        '<span class="mn-mode-name">' + m.label + '</span><span class="mn-mode-desc">' + m.desc + '</span></button>';
    }).join('');
    var apps = APPS.map(function (a, i) {
      return '<div class="mn-glass mn-app mn-rise" style="--i:' + (5 + i) + '">' +
        '<span class="su-icon" data-slot="' + i + '" aria-hidden="true"></span>' +
        '<span class="mn-app-text"><span class="mn-app-name">' + esc(a.name) + '</span>' +
        '<span class="mn-app-min"><strong>' + a.min + ' min</strong> hoy</span></span>' +
        '<button type="button" class="mn-switch" role="switch" aria-checked="false" data-app="' + i + '" aria-label="Vigilar ' + esc(a.name) + '">' +
        '<span class="sa-toggle" aria-hidden="true"><span class="sa-toggle-thumb"></span></span></button></div>';
    }).join('');

    return '<header class="mn-head mn-rise" style="--i:0"><div class="mn-eyebrow">Bloques</div>' +
      '<h1 class="mn-serif mn-title">Protege tu <em>atención</em></h1></header>' +
      '<section class="mn-glass mn-start mn-rise" style="--i:1" aria-label="Iniciar bloque">' +
      '<h2 class="mn-card-title">Iniciar bloque</h2><p class="mn-card-sub">Las apps vigiladas quedarán en pausa.</p>' +
      '<div class="mn-segmented" role="radiogroup" aria-label="Duración" data-el="durs"><i class="mn-seg-ind" data-el="durInd" aria-hidden="true"></i>' + durs + '</div>' +
      '<div class="mn-modes" role="radiogroup" aria-label="Modo" data-el="modes">' + modes + '</div>' +
      '<button type="button" class="mn-hold" data-el="hold" aria-label="Mantén presionado para empezar el bloque">' +
      '<span class="mn-hold-fill" data-el="holdFill" aria-hidden="true"></span><span class="mn-hold-label" data-el="holdLabel"></span></button>' +
      '<p class="mn-hold-hint">Mantén presionado 1,5 s</p></section>' +
      '<div class="mn-section mn-rise" style="--i:4"><h2 class="mn-card-title">Apps vigiladas</h2><span class="mn-section-note">Hoy</span></div>' +
      '<div class="mn-apps">' + apps + '</div>';
  }

  // ---------- rendering ----------

  function renderChoices() {
    var s = main.session;
    q.durs.querySelectorAll('.mn-seg-btn').forEach(function (b, i) {
      b.setAttribute('aria-checked', i === s.dur ? 'true' : 'false');
    });
    q.durs.style.setProperty('--dur', String(s.dur));
    q.modes.querySelectorAll('.mn-mode').forEach(function (b) {
      b.setAttribute('aria-checked', b.dataset.mode === s.mode ? 'true' : 'false');
    });
    page.querySelectorAll('.mn-switch').forEach(function (b) {
      b.setAttribute('aria-checked', st.apps[Number(b.dataset.app)] ? 'true' : 'false');
    });
  }

  function renderHold() {
    var bs = main.block.state();
    var running = !!bs && !bs.finished;
    q.holdFill.style.width = (st.hold * 100).toFixed(1) + '%';
    q.holdLabel.textContent = running ? 'Bloque en curso' : 'Mantén para empezar';
  }

  // ---------- hold button (HoldFillButton: 1.5 s fill, 300 ms release) ----------

  function frame(now) {
    var dt = lastT ? Math.min(now - lastT, 50) : 0;
    lastT = now;
    if (st.holding) {
      st.hold = Math.min(1, st.hold + dt / SA.motion.ms('--motion-hold-duration'));
      if (st.hold >= 1) {
        st.holding = false;
        st.hold = 0;
        renderHold();
        lastT = 0;
        main.block.start(DURATIONS[main.session.dur]);
        main.go('focus');
        return;
      }
    } else if (st.hold > 0) {
      st.hold = Math.max(0, st.hold - dt / SA.motion.ms('--motion-hold-release-duration'));
    }
    renderHold();
    if (st.holding || st.hold > 0) raf = requestAnimationFrame(frame);
    else { raf = 0; lastT = 0; }
  }

  function holdStart() {
    var bs = main.block.state();
    if (bs && !bs.finished) { main.go('focus'); return; }
    st.holding = true;
    if (!raf) raf = requestAnimationFrame(frame);
  }

  function holdEnd() { st.holding = false; }

  function stopHold() {
    cancelAnimationFrame(raf);
    raf = 0;
    lastT = 0;
    st.holding = false;
    st.hold = 0;
  }

  // ---------- behavior ----------

  function bind() {
    page.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !page.contains(t) || t === q.hold) return;
      if (t.dataset.dur !== undefined) { main.session.dur = Number(t.dataset.dur); renderChoices(); }
      else if (t.dataset.mode) { main.session.mode = t.dataset.mode; renderChoices(); }
      else if (t.dataset.app !== undefined) {
        var i = Number(t.dataset.app);
        st.apps[i] = !st.apps[i];
        renderChoices();
      }
    });

    q.hold.addEventListener('pointerdown', function (e) {
      if (e.button) return;
      e.preventDefault();
      holdStart();
    });
    ['pointerup', 'pointerleave', 'pointercancel'].forEach(function (n) { q.hold.addEventListener(n, holdEnd); });
    q.hold.addEventListener('keydown', function (e) {
      if ((e.key === ' ' || e.key === 'Enter') && !e.repeat) { e.preventDefault(); holdStart(); }
    });
    q.hold.addEventListener('keyup', function (e) {
      if (e.key === ' ' || e.key === 'Enter') holdEnd();
    });
    q.hold.addEventListener('blur', holdEnd);
  }

  var tab = {
    html: html,

    mount: function (el, shell) {
      main = shell;
      page = el;
      st = fresh();
      page.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      bind();
      renderChoices();
      renderHold();
      main.onBlock(renderHold);
    },

    enter: function () {
      renderChoices();
      renderHold();
    },

    exit: function () { stopHold(); renderHold(); },

    stop: function () { stopHold(); },

    reset: function () {
      stopHold();
      st = fresh();
      renderChoices();
      renderHold();
    }
  };

  SA.main.registerTab('blocks', tab);
})(window.SA = window.SA || {});
