/*
 * GlassWall overlay: the wall that covers an intercepted app (GlassWallChoreography).
 * Timeline: 0 ms scrim + blur, 320 ms content in, a leaf falls (1.6 s), 1.7 s breathing (8 s),
 * 9.7 s the context block rises. Mindful: intent chips, "Volver" and "Continuar en 10...".
 * Strict: grey canopy, lock chip and no way to continue. Mock-up only: nothing is recorded.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var el = null;
  var q = {};
  var st = null;
  var timers = [];
  var countTimer = 0;

  // Timeline in ms (GlassWallChoreography): when each phase starts
  var BREATH_AT = 1700;
  var ASK_AT = 9700;
  var COUNT_FROM = 10;       // "Continuar en 10..." counts down one second at a time
  var COUNT_STEP_MS = 1000;
  var WEEK_LEAVES = 12;      // leaves of the week before the ones of this session

  var BLOBS = [[195, 150, 70], [140, 170, 50], [250, 170, 50], [165, 115, 45], [225, 112, 45], [195, 195, 55], [195, 95, 40]];
  var INTENTS = ['Responder un mensaje', 'Buscar algo', 'Solo aburrimiento'];

  function fresh() { return { phase: '', count: COUNT_FROM, intent: -1, variant: 'mindful' }; }

  function clearTimers() {
    timers.forEach(clearTimeout);
    timers = [];
    clearTimeout(countTimer);
  }

  function later(fn, ms) { timers.push(setTimeout(fn, ms)); }

  // ---------- templates ----------

  function html() {
    var blobs = BLOBS.map(function (b, i) {
      return '<circle class="mn-wall-blob mn-wall-blob--' + (i % 3) + '" cx="' + b[0] + '" cy="' + b[1] + '" r="' + b[2] + '"></circle>';
    }).join('');
    var intents = INTENTS.map(function (t, i) {
      return '<button type="button" class="mn-wall-intent" aria-pressed="false" data-intent="' + i + '">' + t + '</button>';
    }).join('');
    var lock = main.icon('M7 11h10a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2zM8 11V8a4 4 0 0 1 8 0v3', 14);

    return '<div class="mn-wall-app" aria-hidden="true"><div class="mn-wall-app-bar"></div>' +
      '<div class="mn-wall-app-row"><i></i><i></i><i></i><i></i></div><div class="mn-wall-app-hero"></div><div class="mn-wall-app-card"></div></div>' +
      '<div class="mn-wall-scrim" aria-hidden="true"></div>' +
      '<div class="mn-wall-content">' +
      '<div class="mn-wall-top"><div class="mn-wall-eyebrow">Videos cortos · <span data-el="modeName">Mindful</span></div>' +
      '<h2 class="mn-serif mn-wall-title">Toma un <em>segundo aire</em></h2></div>' +
      '<div class="mn-wall-breath">' +
      '<svg aria-hidden="true" viewBox="0 0 390 300"><g class="mn-wall-canopy">' + blobs + '</g>' +
      '<path class="mn-wall-trunk" d="M195 300 C192 270 198 250 195 225" stroke-width="7" stroke-linecap="round" fill="none"></path></svg>' +
      '<div class="mn-wall-leaf" aria-hidden="true"><svg width="34" height="34" viewBox="0 0 24 24"><path class="mn-wall-leaf-body" d="M2 12 C6 4 18 4 22 12 C18 20 6 20 2 12Z"></path>' +
      '<path class="mn-wall-leaf-vein" d="M2 12 H20" stroke-width="1"></path></svg></div>' +
      '<div class="mn-wall-words"><div class="mn-wall-in mn-serif">Inhala</div><div class="mn-wall-out mn-serif">Exhala</div></div></div>' +
      '<div class="mn-wall-ask" data-el="ask">' +
      '<p class="mn-wall-q">Llevas 23 min en esta app hoy.<br>¿Es lo que querías?</p>' +
      '<p class="mn-wall-leaves">Tu árbol lleva <span data-el="leaves">12</span> hojas esta semana.</p>' +
      '<div class="mn-wall-mindful mn-wall-intents"><div class="mn-wall-intent-q">¿Para qué entras?</div><div class="mn-wall-intent-row">' + intents + '</div></div>' +
      '<div class="mn-wall-strict"><span class="mn-wall-lock">' + lock + 'Se abre a las 18:30</span></div>' +
      '<button type="button" class="mn-btn mn-wall-back" data-el="back">Volver a mi bloque</button>' +
      '<button type="button" class="mn-wall-cont mn-wall-mindful" data-el="cont" disabled>' +
      '<svg width="28" height="16" viewBox="0 0 28 16" aria-hidden="true"><path class="mn-wall-stem" data-el="stem" d="M2 14 C8 14 12 10 14 6" fill="none" stroke-width="2" stroke-linecap="round" pathLength="1" stroke-dasharray="1" stroke-dashoffset="1"></path>' +
      '<path class="mn-wall-stemleaf" d="M14 6 C16 2 22 1 26 3 C24 8 18 9 14 6Z"></path></svg>' +
      '<span data-el="contLabel">Continuar en 10…</span></button></div></div>';
  }

  // ---------- rendering ----------

  function setPhase(p) {
    st.phase = p;
    el.dataset.phase = p;
    el.dataset.on = p && p !== 'leave' ? 'true' : 'false';
    render();
  }

  function render() {
    var strict = st.variant === 'strict';
    el.dataset.variant = st.variant;
    q.modeName.textContent = strict ? 'Strict' : 'Mindful';
    q.leaves.textContent = String(WEEK_LEAVES + main.session.leaves - 4);
    var ready = st.count <= 0;
    q.cont.disabled = !ready || strict;
    q.cont.dataset.ready = ready ? 'true' : 'false';
    q.contLabel.textContent = ready ? 'Continuar 5 min' : 'Continuar en ' + st.count + '…';
    q.stem.setAttribute('stroke-dashoffset', (1 - (COUNT_FROM - st.count) / COUNT_FROM).toFixed(2));
    el.querySelectorAll('.mn-wall-intent').forEach(function (b, i) {
      b.setAttribute('aria-pressed', i === st.intent ? 'true' : 'false');
    });
  }

  // ---------- behavior ----------

  function countdown() {
    clearTimeout(countTimer);
    countTimer = setTimeout(function step() {
      if (st.phase !== 'ask' || st.count <= 0) return;
      st.count -= 1;
      render();
      if (st.count > 0) countTimer = setTimeout(step, COUNT_STEP_MS);
    }, COUNT_STEP_MS);
  }

  function hide() {
    el.dataset.open = 'false';
    el.inert = true;
    st.phase = '';
    el.dataset.phase = '';
    el.dataset.on = 'false';
  }

  function back() {
    if (st.phase !== 'ask') return;
    clearTimers();
    setPhase('rise');
    // The leaf rises and leaves; then the Panel blooms a new leaf for the respected pause
    later(function () {
      main.session.leaves += 1;
      main.session.pauses += 1;
      main.session.saved += 3;
      main.go('panel');
    }, SA.motion.ms('--motion-wall-rise-duration'));
  }

  function proceed() {
    if (st.phase !== 'ask' || st.variant === 'strict' || st.count > 0) return;
    clearTimers();
    setPhase('leave');
    later(function () {
      main.go('panel');
      main.toast('Disfruta tus 5 min');
    }, SA.motion.ms('--motion-wall-leave-duration'));
  }

  var overlay = {
    html: html,

    mount: function (node) {
      el = node;
      st = fresh();
      el.setAttribute('role', 'dialog');
      el.setAttribute('aria-modal', 'true');
      el.setAttribute('aria-label', 'Toma un segundo aire');
      el.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      hide();
      render();
      q.back.addEventListener('click', back);
      q.cont.addEventListener('click', proceed);
      el.addEventListener('click', function (e) {
        var b = e.target.closest('.mn-wall-intent');
        if (b && el.contains(b)) { st.intent = Number(b.dataset.intent); render(); }
      });
    },

    open: function (cfg) {
      clearTimers();
      st = fresh();
      st.variant = cfg.variant || 'mindful';
      el.dataset.open = 'true';
      el.inert = false;
      setPhase('');
      void el.offsetWidth;
      setPhase('enter');
      later(function () { setPhase('breath'); }, BREATH_AT);
      later(function () {
        setPhase('ask');
        countdown();
        q.back.focus({ preventScroll: true });
      }, ASK_AT);
    },

    close: function (immediate) {
      clearTimers();
      if (!st || el.dataset.open !== 'true') return;
      if (immediate) { hide(); return; }
      // After "Volver" the leaf already rose: only fade the wall, so the leaf does not replay
      if (st.phase === 'rise') el.dataset.on = 'false';
      else if (st.phase !== 'leave') setPhase('leave');
      later(hide, SA.motion.ms('--motion-wall-leave-duration'));
    },

    stop: function () { clearTimers(); },

    reset: function () {
      clearTimers();
      st = fresh();
      hide();
      render();
    }
  };

  SA.main.registerOverlay('wall', overlay);
})(window.SA = window.SA || {});
