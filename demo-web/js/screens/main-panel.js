/*
 * Panel tab: greeting, hero (hill, tree, light by hour, fireflies at night), recovered minutes,
 * active block bar and the AI coach. Mock-up only: the coach messages are fixed examples.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var page = null;
  var q = {};
  var st = null;
  var thinkTimer = 0;
  var revealTimer = 0;
  var stopCount = null;
  var leafTimer = 0;

  var SVG_NS = 'http://www.w3.org/2000/svg';
  var THINK_MS = 1400;  // the coach "thinks" for about 1.4 s
  var REVEAL_MS = 36;   // one step of the character reveal
  var REVEAL_STEP = 2;  // characters per step
  var LEAF_DELAY_MS = 520; // a new leaf blooms after the page has slid in

  // Tree geometry shared with the onboarding (segDefs / blobDefs of the reference)
  var LIMBS = [
    ['M195 515 C190 450 202 380 195 300', 10], ['M196 400 C215 385 235 380 255 362', 4.5], ['M194 370 C175 355 158 345 140 328', 4.5],
    ['M195 300 C190 250 200 210 195 165', 8], ['M195 250 C220 235 245 228 278 210', 4.5], ['M195 232 C170 218 150 208 115 196', 4.5],
    ['M195 190 C212 160 232 140 262 126', 3.5], ['M195 185 C178 155 158 140 128 124', 3.5], ['M195 170 L195 110', 3.5]
  ];
  var BLOBS = [
    [196, 300, 40], [255, 352, 32], [140, 318, 32], [228, 318, 28], [165, 300, 28], [195, 170, 52], [278, 205, 40], [115, 192, 40],
    [245, 240, 34], [148, 232, 34], [205, 228, 36], [195, 100, 56], [262, 118, 46], [128, 116, 46], [305, 160, 38], [85, 155, 38],
    [235, 150, 44], [155, 148, 44], [195, 140, 48]
  ];
  var FLOWERS = [[262, 112], [128, 110]];
  var LEAF_SLOTS = [[300, 210, -20], [92, 200, 200], [320, 150, -10], [70, 140, 190], [250, 80, -50], [140, 78, 230], [330, 260, 10], [60, 255, 170]];

  var MESSAGES = [
    { a: ['Crear bloque 21:00', 'Ahora no'], act: ['blocks', 'next'],
      text: function () { return 'Ayer la noche fue tu momento más difícil: 48 min en videos cortos después de las 21:00. ¿Te armo un bloque suave a esa hora?'; } },
    { a: ['Iniciar 15 min', 'Ver progreso'], act: ['start15', 'progress'],
      text: function () { return 'Hoy llevas ' + main.session.pauses + ' pausas respetadas, más que el martes. Una más antes de las 18:00 y tu árbol florece.'; } },
    { a: ['Recuérdamelo', 'Otra idea'], act: ['remind', 'next'],
      text: function () { return 'Abres la red social sobre todo al despertar. ¿Probamos dejar el celular lejos de la cama esta noche?'; } }
  ];
  var ASKS = [['¿Cómo voy esta semana?', 1], ['Dame una idea para hoy', 2], ['¿Cuándo me cuesta más?', 0]];

  function fresh() { return { idx: 0, n: 0, thinking: true, started: false, shown: 0, leaves: 0 }; }

  function esc(t) { return main.esc(t); }

  // ---------- templates ----------

  function treeSvg() {
    var limbs = LIMBS.map(function (l) {
      return '<path class="mn-limb" d="' + l[0] + '" fill="none" stroke-width="' + l[1] + '" stroke-linecap="round"></path>';
    }).join('');
    var blobs = BLOBS.map(function (b, i) {
      return '<circle class="mn-blob mn-blob--' + (i % 3) + '" cx="' + b[0] + '" cy="' + b[1] + '" r="' + b[2] + '"></circle>';
    }).join('');
    var flowers = FLOWERS.map(function (f) {
      return '<g transform="translate(' + f[0] + ' ' + f[1] + ') scale(1.3)">' +
        '<circle class="mn-petal" cx="0" cy="-8" r="7"></circle><circle class="mn-petal" cx="7.6" cy="-2.5" r="7"></circle>' +
        '<circle class="mn-petal" cx="4.7" cy="6.5" r="7"></circle><circle class="mn-petal" cx="-4.7" cy="6.5" r="7"></circle>' +
        '<circle class="mn-petal" cx="-7.6" cy="-2.5" r="7"></circle><circle class="mn-gold" cx="0" cy="0" r="4.5"></circle></g>';
    }).join('');
    return '<path class="mn-hill" d="M-10 262 C70 240 140 256 195 252 C255 248 320 232 400 256 L400 300 L-10 300 Z"></path>' +
      '<g transform="translate(195 256) scale(0.5) translate(-195 -515)"><g class="mn-sway">' + limbs + blobs + flowers +
      '<g data-el="leaves"></g></g></g>';
  }

  function html() {
    var ffs = [[60, 160, 0], [300, 120, 1], [250, 220, 2], [110, 90, 1.5]].map(function (f) {
      return '<i class="mn-ff" style="left:' + f[0] + 'px;top:' + f[1] + 'px;animation-delay:' + f[2] + 's"></i>';
    }).join('');
    var asks = ASKS.map(function (a) {
      return '<button type="button" class="mn-chip" data-ask="' + a[1] + '">' + esc(a[0]) + '</button>';
    }).join('');
    return '<header class="mn-top mn-rise" style="--i:0"><div><div class="mn-greet" data-el="greet"></div>' +
      '<div class="mn-serif mn-name" data-el="name"></div></div>' +
      '<button type="button" class="mn-chip mn-chip--icon" data-act="wall">' +
      main.icon('M9 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM11 18h2', 16) + 'Simular abrir app</button></header>' +

      '<div class="mn-hero mn-rise" style="--i:1">' +
      '<div class="mn-sky" data-el="sky" aria-hidden="true"></div>' +
      '<div class="mn-fireflies" data-el="ff" aria-hidden="true">' + ffs + '</div>' +
      '<svg class="mn-hero-art" aria-hidden="true" viewBox="0 0 390 300">' + treeSvg() + '</svg>' +
      '<div class="mn-hero-saved"><div class="mn-serif mn-saved"><span data-el="saved">0</span> <span class="mn-saved-unit">min</span></div>' +
      '<div class="mn-hero-cap">recuperados hoy</div></div>' +
      '<div class="mn-hero-stats"><div><strong data-el="pauses">3</strong> pausas respetadas</div><div><strong>12</strong> aperturas evitadas</div></div></div>' +

      '<button type="button" class="mn-glass mn-blockbar" data-el="blockBar" hidden>' +
      '<svg width="36" height="36" viewBox="0 0 36 36" aria-hidden="true"><circle class="mn-ring-track" cx="18" cy="18" r="14" fill="none" stroke-width="4"></circle>' +
      '<circle class="mn-ring-fill" data-el="miniRing" cx="18" cy="18" r="14" fill="none" stroke-width="4" stroke-linecap="round" pathLength="1" stroke-dasharray="1" transform="rotate(-90 18 18)"></circle></svg>' +
      '<span class="mn-blockbar-text"><span class="mn-blockbar-title">Bloque de enfoque</span><span class="mn-blockbar-sub" data-el="remain"></span></span>' +
      main.icon('M9 6l6 6-6 6', 20) + '</button>' +

      '<div class="mn-ai-wrap mn-rise" style="--i:2"><div class="mn-ai" data-el="ai" data-thinking="true" data-done="false">' +
      '<div class="mn-think" aria-hidden="true"></div>' +
      '<div class="mn-ai-card"><div class="mn-ai-head">' +
      '<svg class="mn-seed" width="36" height="36" viewBox="0 0 36 36" aria-hidden="true"><circle class="mn-seed-halo" cx="18" cy="18" r="17"></circle>' +
      '<path class="mn-seed-leaf" d="M18 9 C24 14 24 22 18 28 C12 22 12 14 18 9Z"></path><path class="mn-seed-vein" d="M18 14v10" stroke-width="1.5" stroke-linecap="round"></path></svg>' +
      '<div class="mn-ai-who"><div class="mn-ai-title">Tu coach</div><div class="mn-ai-status" data-el="aiStatus"></div></div>' +
      '<button type="button" class="mn-chip mn-chip--sm" data-act="ai-next" aria-label="Otra sugerencia">' +
      main.icon('M20 12a8 8 0 1 1-2.3-5.7M20 4v5h-5', 16) + '</button></div>' +
      '<div class="mn-ai-body" aria-live="polite" data-el="aiLive">' +
      '<div class="mn-ai-wait" data-el="aiWait"><div class="mn-dots"><i></i><i></i><i></i></div>' +
      '<div class="mn-skel" style="width:100%"></div><div class="mn-skel" style="width:86%"></div><div class="mn-skel" style="width:60%"></div></div>' +
      '<div class="mn-serif mn-ai-text" data-el="aiText" aria-hidden="true"></div></div>' +
      '<div class="mn-ai-actions" data-el="aiActions"><button type="button" class="mn-chip mn-chip--solid" data-act="ai-a" data-el="aiA"></button>' +
      '<button type="button" class="mn-chip" data-act="ai-b" data-el="aiB"></button></div></div></div></div>' +

      '<div class="mn-ask mn-rise" style="--i:3"><div class="mn-eyebrow">Pregúntale</div><div class="mn-chips">' + asks + '</div></div>';
  }

  // ---------- rendering ----------

  function greeting() {
    var h = new Date().getHours();
    return h >= 5 && h < 12 ? 'Buenos días' : (h >= 12 && h < 19 ? 'Buenas tardes' : 'Buenas noches');
  }

  function renderHero() {
    var h = new Date().getHours();
    q.greet.textContent = greeting() + ',';
    q.name.textContent = main.name();
    q.sky.dataset.light = h >= 6 && h < 11 ? 'morning' : (h >= 11 && h < 17 ? 'day' : (h >= 17 && h < 20 ? 'evening' : 'night'));
    q.ff.dataset.on = h >= 20 || h < 6 ? 'true' : 'false';
    q.pauses.textContent = String(main.session.pauses);
  }

  function countTo(value) {
    if (stopCount) stopCount();
    var from = st.shown;
    if (SA.motion.reduced() || from === value) {
      st.shown = value;
      q.saved.textContent = String(value);
      return;
    }
    stopCount = SA.motion.tween({
      from: from,
      to: value,
      duration: SA.motion.ms('--motion-count-good-duration'),
      easing: SA.motion.easing('--motion-count-easing'),
      onUpdate: function (v) { st.shown = Math.round(v); q.saved.textContent = String(st.shown); },
      onDone: function () { st.shown = value; q.saved.textContent = String(value); }
    });
  }

  function syncLeaves(animate) {
    var target = Math.min(main.session.leaves, LEAF_SLOTS.length);
    var group = q.leaves;
    var delay = 0;
    while (st.leaves < target) {
      var s = LEAF_SLOTS[st.leaves];
      var g = document.createElementNS(SVG_NS, 'g');
      g.setAttribute('transform', 'translate(' + s[0] + ' ' + s[1] + ') rotate(' + s[2] + ') scale(1.6)');
      var p = document.createElementNS(SVG_NS, 'path');
      p.setAttribute('class', animate ? 'mn-leaf mn-newleaf' : 'mn-leaf');
      p.setAttribute('d', 'M0 0 C6 -8 18 -8 22 0 C18 8 6 8 0 0Z');
      if (animate) p.style.animationDelay = (LEAF_DELAY_MS + delay) + 'ms';
      g.appendChild(p);
      group.appendChild(g);
      st.leaves += 1;
      delay += 160;
    }
  }

  function renderBlock() {
    var s = main.block.state();
    var on = !!s && !s.finished;
    q.blockBar.hidden = !on;
    if (!on) return;
    q.remain.textContent = 'Quedan ' + main.block.clock(s.remain);
    q.miniRing.style.strokeDashoffset = (1 - s.prog).toFixed(3);
  }

  function renderAi() {
    var m = MESSAGES[st.idx];
    var full = m.text();
    var ready = !st.thinking;
    var done = ready && st.n >= full.length;
    q.ai.dataset.thinking = st.thinking ? 'true' : 'false';
    q.ai.dataset.done = done ? 'true' : 'false';
    q.aiStatus.textContent = st.thinking ? 'Mirando tu día…' : 'Hace un momento';
    q.aiText.textContent = ready ? full.slice(0, st.n) : '';
    q.aiLive.setAttribute('aria-label', ready ? full : 'Tu coach está pensando');
    q.aiA.textContent = m.a[0];
    q.aiB.textContent = m.a[1];
    q.aiA.tabIndex = done ? 0 : -1;
    q.aiB.tabIndex = done ? 0 : -1;
  }

  function clearAi() {
    clearTimeout(thinkTimer);
    clearInterval(revealTimer);
  }

  function think(i) {
    clearAi();
    st.idx = i;
    st.n = 0;
    st.thinking = true;
    renderAi();
    thinkTimer = setTimeout(function () {
      var full = MESSAGES[st.idx].text();
      st.thinking = false;
      if (SA.motion.reduced()) { st.n = full.length; renderAi(); return; }
      renderAi();
      revealTimer = setInterval(function () {
        st.n = Math.min(full.length, st.n + REVEAL_STEP);
        renderAi();
        if (st.n >= full.length) clearInterval(revealTimer);
      }, REVEAL_MS);
    }, THINK_MS);
  }

  function aiDo(act) {
    if (act === 'next') { think((st.idx + 1) % MESSAGES.length); return; }
    if (act === 'blocks') { main.go('blocks'); main.toast('Bloque de 21:00 listo para hoy'); return; }
    if (act === 'progress') { main.go('progress'); return; }
    if (act === 'start15') { main.session.dur = 2; main.block.start(15); main.go('focus'); return; }
    if (act === 'remind') main.toast('Te lo recordaré a las 22:00');
  }

  // ---------- behavior ----------

  function bind() {
    page.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !page.contains(t) || t.disabled) return;
      if (t === q.blockBar) { main.go('focus'); return; }
      if (t.dataset.ask !== undefined) { think(Number(t.dataset.ask)); return; }
      var act = t.dataset.act;
      if (act === 'wall') main.go('wall');
      else if (act === 'ai-next') think((st.idx + 1) % MESSAGES.length);
      else if (act === 'ai-a') aiDo(MESSAGES[st.idx].act[0]);
      else if (act === 'ai-b') aiDo(MESSAGES[st.idx].act[1]);
    });
  }

  var tab = {
    html: html,

    mount: function (el, shell) {
      main = shell;
      page = el;
      st = fresh();
      page.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      bind();
      renderHero();
      syncLeaves(false);
      renderBlock();
      renderAi();
      main.onTick(renderBlock);
      main.onBlock(renderBlock);
    },

    enter: function (info) {
      renderHero();
      renderBlock();
      countTo(main.session.saved);
      clearTimeout(leafTimer);
      syncLeaves(true);
      if (!st.started) { st.started = true; think(0); }
      if (info.revisit) renderAi();
    },

    stop: function () {
      clearAi();
      clearTimeout(leafTimer);
      if (stopCount) { stopCount(); stopCount = null; }
    },

    reset: function () {
      tab.stop();
      st = fresh();
      while (q.leaves.firstChild) q.leaves.removeChild(q.leaves.firstChild);
      q.saved.textContent = '0';
      syncLeaves(false);
      renderHero();
      renderBlock();
      renderAi();
    }
  };

  SA.main.registerTab('panel', tab);
})(window.SA = window.SA || {});
