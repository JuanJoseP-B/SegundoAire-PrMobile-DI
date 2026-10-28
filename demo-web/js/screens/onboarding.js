/*
 * Onboarding "Lo que crece cuando pausas": welcome (3 cards) + 9 numbered steps, the last one
 * being the permissions. One scene, one tree: each screen id maps to a step number and the
 * scene re-renders from a single state object (UDF, like the app). The account curtain and
 * everything after it live in login.js and setup.js.
 */
(function (SA) {
  'use strict';

  var D = SA.onbData;
  var STEPS = ['welcome-1', 'welcome-2', 'welcome-3', 'name', 'goals', 'usage', 'moment', 'reveal', 'breath', 'plan', 'commit', 'perms'];
  var LAST_NUMBERED = 11;
  var QUESTION_COUNT = 9;
  var BREATH_SKIP_MS = 24000;
  var HOLD_RELEASE_MS = 300;

  var api = null;
  var root = null;
  var tree = null;
  var q = {};
  var layers = [];
  var st = null;
  var timers = { shown: 0, breath: 0, commit: 0, enter: 0 };
  var cancelCount = [];
  var holding = false;
  var holdRaf = 0;

  function fresh() {
    return {
      step: 0, name: '', picks: [], use: D.usage.initial, moment: -1,
      hold: 0, committed: false, gust: 0, petals: false,
      perms: D.perms.map(function () { return false; })
    };
  }

  function esc(t) {
    return String(t).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }

  function icon(path, size, cls) {
    return '<svg class="' + (cls || '') + '" width="' + size + '" height="' + size + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
      'stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="' + path + '"></path></svg>';
  }

  function chip(text, withLeaf) {
    return '<span class="ob-chip">' + (withLeaf ? icon(D.ICON.leaf, 14, 'ob-chip-icon') : '') + '<span>' + esc(text) + '</span></span>';
  }

  function stepChip(n, extra) {
    return chip('Paso ' + n + ' de ' + QUESTION_COUNT + (extra || ''), false);
  }

  function title(inner, cls) { return '<h2 class="ob-title ' + (cls || '') + '">' + inner + '</h2>'; }
  function button(label, attrs) { return '<button type="button" class="ob-btn" ' + (attrs || '') + '>' + label + '</button>'; }

  // ---------- layer templates ----------

  function welcomeText() {
    return D.welcome.map(function (t, i) {
      return '<div class="ob-b-text" data-b="' + i + '">' + chip(t.eyebrow, true) +
        title(esc(t.a) + '<em>' + esc(t.em) + '</em>' + esc(t.b), 'ob-title--b') +
        '<p class="ob-sub ob-sub--b">' + esc(t.body) + '</p></div>';
    }).join('');
  }

  function goalsContent() {
    return '<div class="ob-opts" data-el="goalOpts">' + D.goals.map(function (g, i) {
      return '<button type="button" class="ob-opt" data-goal="' + i + '" aria-pressed="false" style="--i:' + i + '">' +
        '<span class="ob-opt-icon' + (g.filled ? ' is-filled' : '') + '">' + icon(g.icon, 24) + '</span><span>' + esc(g.label) + '</span></button>';
    }).join('') + '<p class="ob-hint" data-el="goalHint">Ya elegiste 2. Quita una para cambiar.</p></div>' +
      button('Continuar', 'data-el="goalsNext" data-next');
  }

  function usageContent() {
    var u = D.usage;
    var nodes = u.caps.map(function (c, i) {
      return '<button type="button" role="radio" aria-checked="false" aria-label="' + esc(u.labels[i]) + '" class="ob-node" data-use="' + i + '" style="left:' + (i * u.step) + 'px"><span class="ob-node-dot"></span></button>';
    }).join('');
    var caps = u.caps.map(function (c, i) {
      return '<span class="ob-cap" data-cap="' + i + '" aria-hidden="true" style="left:' + (i * u.step) + 'px">' + esc(c) + '</span>';
    }).join('');
    return '<div class="ob-usage-value"><div class="ob-usage-number" data-el="useLabel"></div><p class="ob-sub ob-sub--center">al día</p></div>' +
      '<div class="ob-slider" role="radiogroup" aria-label="Horas al día">' +
      '<svg class="ob-slider-line" aria-hidden="true" width="310" height="20" viewBox="0 0 310 20"><path d="M-8 12 C60 4 120 16 160 10 S260 4 318 10"></path></svg>' +
      '<div class="ob-thumb" data-el="thumb" aria-hidden="true"><svg width="36" height="36" viewBox="0 0 24 24"><path class="ob-thumb-leaf" d="M3 12 C6 4 18 4 21 12 C18 20 6 20 3 12Z"></path><path class="ob-thumb-vein" d="M3 12 H19"></path></svg></div>' +
      nodes + caps + '</div>' + button('Continuar', 'data-next');
  }

  function momentContent() {
    return '<div class="ob-opts ob-opts--moments" role="radiogroup" aria-label="Momento del día" data-el="momentOpts">' + D.moments.map(function (m, i) {
      return '<button type="button" role="radio" aria-checked="false" class="ob-opt" data-moment="' + i + '" style="--i:' + i + '">' +
        icon(m.icon, 24, 'ob-opt-glyph') + '<span class="ob-opt-label">' + esc(m.label) + '</span><span class="ob-opt-hours">' + esc(m.hours) + '</span></button>';
    }).join('') + '</div>' + button('Continuar', 'data-el="momentNext" data-next');
  }

  function revealContent() {
    var rings = '';
    for (var r = 0; r < 7; r++) {
      rings += '<ellipse class="rv-ring" cx="110" cy="110" rx="' + (34 + r * 12) + '" ry="' + (30 + r * 11.4).toFixed(1) +
        '" pathLength="1" style="--i:' + r + ';--op:' + (0.25 + r * 0.1).toFixed(2) + '"></ellipse>';
    }
    return '<div class="ob-reveal" data-el="reveal">' +
      '<div class="rv-orb"><svg aria-hidden="true" width="220" height="220" viewBox="0 0 220 220">' + rings + '</svg>' +
      '<div class="rv-center"><div class="rv-years" data-el="years">0,0</div><div class="rv-unit">años</div></div></div>' +
      '<p class="ob-sub rv-late rv-caption">frente a una pantalla en los próximos 50 años, a tu ritmo actual.</p>' +
      '<p class="rv-formula rv-late" data-el="formula"></p>' +
      '<div class="glass glass-2 rv-late rv-good-card"><div class="ob-kicker">La buena noticia</div>' +
      '<p class="rv-good-text">Si recuperas 1 h al día, ganas <strong data-el="goodYears">0,0 años</strong> para ti.</p></div></div>' +
      button('Continuar', 'data-next');
  }

  function breathContent() {
    return '<div class="ob-breath" data-el="breath">' +
      '<svg aria-hidden="true" width="260" height="260" viewBox="0 0 260 260"><g class="br-canopy">' +
      '<circle class="br-c br-c--a" cx="130" cy="120" r="70"></circle><circle class="br-c br-c--b" cx="80" cy="140" r="50"></circle>' +
      '<circle class="br-c br-c--c" cx="180" cy="140" r="50"></circle><circle class="br-c br-c--d" cx="105" cy="90" r="45"></circle>' +
      '<circle class="br-c br-c--e" cx="160" cy="88" r="45"></circle><circle class="br-c br-c--f" cx="130" cy="160" r="55"></circle></g></svg>' +
      '<div class="br-words" aria-live="off"><div class="br-word br-in">Inhala</div><div class="br-word br-out">Exhala</div></div></div>' +
      button('Saltar', 'data-el="breathNext" data-next');
  }

  function planContent() {
    return '<div class="ob-plan" data-el="plan"><div class="glass glass-2 ob-plan-card" data-el="planRows"></div></div>' +
      button('Me gusta este plan', 'data-next');
  }

  function commitContent() {
    return '<div class="glass glass-2 ob-quote">' +
      '<p class="ob-quote-text">“Yo, <span data-bind="name"></span>, me comprometo a darme un segundo aire antes de abrir lo que me distrae.”</p>' +
      '<p class="ob-quote-sign">— <span data-bind="name"></span> · <span data-el="today"></span></p></div>' +
      '<button type="button" class="ob-btn ob-hold" data-el="hold" aria-label="Mantén presionado para comprometerte">' +
      '<span class="ob-hold-fill" data-el="holdFill" aria-hidden="true"></span><span class="ob-hold-label" data-el="holdLabel">Mantén para comprometerte</span></button>';
  }

  function permsContent() {
    return '<div class="ob-perms" data-el="perms">' + D.perms.map(function (p, i) {
      return '<div class="glass glass-2 ob-perm" role="group" aria-label="' + esc(p.title) + '" data-perm-row="' + i + '" data-on="false">' +
        '<span class="ob-perm-tile"><span class="ob-perm-ico ob-perm-ico--todo">' + icon(p.icon, 20) + '</span>' +
        '<span class="ob-perm-ico ob-perm-ico--ok">' + icon(D.ICON.check, 20) + '</span></span>' +
        '<span class="ob-perm-text"><span class="ob-perm-title">' + esc(p.title) + '</span><span class="ob-perm-desc">' + esc(p.desc) + '</span></span>' +
        '<button type="button" class="ob-perm-act" data-perm="' + i + '" aria-label="Activar ' + esc(p.title) + '"><span>Activar</span></button>' +
        '<span class="ob-perm-ok">Listo</span></div>';
    }).join('') + '</div>' + button('Continuar', 'data-el="permsNext" data-next disabled');
  }

  var LAYER_DEFS = [
    { i: 0, text: welcomeText(), content: button('Siguiente', 'data-el="welcomeNext" data-next') },
    {
      i: 1,
      text: stepChip(1) + title('¿Cómo te <em>llamamos</em>?') + '<p class="ob-sub">Cada letra le da una hoja a tu árbol.</p>',
      content: '<div class="ob-field"><label for="ob-name" class="ob-label">Tu nombre</label>' +
        '<input id="ob-name" class="ob-input" type="text" maxlength="' + D.MAX_NAME + '" placeholder="Escribe tu nombre" autocomplete="off" autocapitalize="words" spellcheck="false" data-el="nameInput">' +
        '<p class="ob-greet" data-el="greet" aria-live="polite">Encantados, <span data-bind="name"></span>.</p></div>' +
        button('Continuar', 'data-el="nameNext" data-next disabled')
    },
    {
      i: 2,
      text: stepChip(2, ' · Elige hasta 2') + title('¿Qué quieres <em>lograr</em><span data-el="goalsTail">?</span>'),
      content: goalsContent()
    },
    {
      i: 3,
      text: stepChip(3) + title('¿Cuánto tiempo pasas en el <em>celular</em>?') + '<p class="ob-sub">Un cálculo aproximado basta.</p>',
      content: usageContent()
    },
    {
      i: 4,
      text: stepChip(4) + title('¿Cuándo te <em>atrapa</em> más?') + '<p class="ob-sub">Ahí pondremos tu primer bloque.</p>',
      content: momentContent()
    },
    {
      i: 5,
      text: stepChip(5) + title('No es para asustarte. Es para que lo <em>veas</em>.', 'ob-title--sm'),
      content: revealContent()
    },
    {
      i: 6,
      text: stepChip(6) + title('Respira <em>con</em> tu árbol') + '<p class="ob-sub">Tres respiraciones. Así se sentirá cada pausa.</p>',
      content: breathContent()
    },
    {
      i: 7,
      text: stepChip(7) + title('El plan de <em data-bind="name"></em>') + '<p class="ob-sub">Hecho con lo que nos contaste. Lo puedes cambiar cuando quieras.</p>',
      content: planContent()
    },
    {
      i: 8,
      text: stepChip(8) + title('Echa <em>raíces</em>'),
      content: commitContent()
    },
    {
      i: 9,
      text: stepChip(9) + title('Un último <em>paso</em>') + '<p class="ob-sub">SegundoAire necesita esto para poder darte la pausa.</p>',
      content: permsContent()
    }
  ];

  function build() {
    var layerHtml = LAYER_DEFS.map(function (d) {
      return '<section class="ob-layer ob-layer--text" data-i="' + d.i + '" data-kind="text">' + d.text + '</section>' +
        '<section class="ob-layer ob-layer--content" data-i="' + d.i + '" data-kind="content">' + d.content + '</section>';
    }).join('');

    return '<div class="ob" data-el="ob"><div class="ob-frame" data-el="frame">' +
      '<div class="ob-dots" data-el="dots" aria-hidden="true"><span class="ob-dot"></span><span class="ob-dot"></span><span class="ob-dot"></span></div>' +
      '<svg class="ob-vine" aria-hidden="true" width="390" height="40" viewBox="0 0 390 40">' +
      '<path class="ob-vine-track" d="M64 20 C100 12 130 28 160 20 S220 12 250 20 S310 28 326 20"></path>' +
      '<path class="ob-vine-fill" data-el="vine" d="M64 20 C100 12 130 28 160 20 S220 12 250 20 S310 28 326 20" pathLength="1"></path></svg>' +
      '<button type="button" class="ob-back" data-el="back" aria-label="Atrás">' + icon('M15 18l-6-6 6-6', 24) + '</button>' +
      layerHtml + '</div></div>';
  }

  // ---------- rendering ----------

  function nameShown() { return (st.name || '').trim() || D.FALLBACK_NAME; }

  function renderLayers(scr) {
    layers.forEach(function (l) {
      var rel = l.i - scr;
      var state = rel === 0 ? 'active' : (Math.abs(rel) > 1 ? 'far' : 'near');
      l.el.style.setProperty('--rel', rel);
      l.el.dataset.state = state;
      l.el.inert = state !== 'active';
    });
  }

  function render() {
    var step = st.step;
    var scr = step < 3 ? 0 : step - 2;
    var inB = step < 3;
    var nm = (st.name || '').trim();

    renderLayers(scr);
    tree.update(st);

    // Welcome cards, progress dots
    var bi = Math.min(step, 2);
    root.querySelectorAll('.ob-b-text').forEach(function (el, i) {
      el.dataset.pos = i === bi ? '0' : (i < bi ? '-1' : '1');
    });
    q.dots.style.opacity = inB ? 1 : 0;
    q.dots.querySelectorAll('.ob-dot').forEach(function (el, i) { el.classList.toggle('is-on', i === bi); });
    q.welcomeNext.textContent = step >= 2 ? 'Empezar' : 'Siguiente';

    // Vine progress and back button
    var vp = step >= 3 && step <= LAST_NUMBERED ? (step - 2) / QUESTION_COUNT : (step > LAST_NUMBERED ? 1 : 0);
    q.vine.style.strokeDashoffset = (1 - vp).toFixed(3);
    q.vine.parentNode.style.opacity = step >= 3 && step <= LAST_NUMBERED ? 1 : 0;
    var backOn = step > 0 && step <= LAST_NUMBERED && !(step === 10 && st.committed);
    q.back.style.opacity = backOn ? 1 : 0;
    q.back.style.pointerEvents = backOn ? 'auto' : 'none';
    q.back.inert = !backOn;

    // Name-bound copy
    root.querySelectorAll('[data-bind="name"]').forEach(function (el) { el.textContent = nameShown(); });
    q.greet.style.opacity = nm ? 1 : 0;
    q.nameNext.disabled = !nm;
    q.goalsTail.textContent = nm ? ', ' + nm + '?' : '?';

    // Goals
    var full = st.picks.length >= D.MAX_GOALS;
    q.goalOpts.querySelectorAll('.ob-opt').forEach(function (el, i) {
      var on = st.picks.indexOf(i) >= 0;
      el.classList.toggle('is-on', on);
      el.classList.toggle('is-dim', full && !on);
      el.setAttribute('aria-pressed', on ? 'true' : 'false');
    });
    q.goalHint.style.opacity = full && step === 4 ? 1 : 0;
    q.goalsNext.disabled = !st.picks.length;

    // Usage
    q.useLabel.textContent = D.usage.labels[st.use];
    q.thumb.style.left = (st.use * D.usage.step - 18) + 'px';
    root.querySelectorAll('.ob-node').forEach(function (el, i) {
      el.setAttribute('aria-checked', i === st.use ? 'true' : 'false');
      el.classList.toggle('is-filled', i <= st.use);
    });
    root.querySelectorAll('.ob-cap').forEach(function (el, i) { el.classList.toggle('is-on', i === st.use); });

    // Moment
    q.momentOpts.querySelectorAll('.ob-opt').forEach(function (el, i) {
      var on = st.moment === i;
      el.classList.toggle('is-on', on);
      el.setAttribute('aria-checked', on ? 'true' : 'false');
    });
    q.momentNext.disabled = st.moment < 0;

    // Commit button
    q.holdFill.style.width = (st.hold * 100).toFixed(1) + '%';
    q.holdLabel.textContent = st.committed ? 'Comprometido' : 'Mantén para comprometerte';

    // Permissions (visual only): the two required ones unlock "Continuar"
    root.querySelectorAll('.ob-perm').forEach(function (el, i) {
      el.dataset.on = st.perms[i] ? 'true' : 'false';
    });
    q.permsNext.disabled = !D.perms.every(function (p, i) { return !p.required || st.perms[i]; });

    // Shared with the account scenes ("Todo listo, {name}.")
    SA.session = SA.session || {};
    SA.session.name = nameShown();

    api.setTone('dark');
  }

  // ---------- step behaviors ----------

  function clearTimers() {
    clearTimeout(timers.shown);
    clearTimeout(timers.breath);
    clearTimeout(timers.commit);
    clearTimeout(timers.enter);
    cancelCount.forEach(function (c) { c(); });
    cancelCount = [];
    cancelAnimationFrame(holdRaf);
    holding = false;
  }

  function restartAnim(el, cls) {
    el.classList.remove(cls);
    void el.offsetWidth;
    el.classList.add(cls);
  }

  function fmt(n) { return n.toFixed(1).replace('.', ','); }

  function startReveal() {
    var hours = D.usage.hours[st.use];
    var years = (hours * 365 * 50) / 8760;
    var good = (365 * 50) / 8760;
    var m = SA.motion;
    q.reveal.classList.remove('rv-good');
    q.formula.textContent = String(hours).replace('.', ',') + ' h × 365 × 50 ÷ 8.760';
    q.years.textContent = fmt(0);
    q.goodYears.textContent = fmt(0) + ' años';
    restartAnim(q.reveal, 'rv-on');

    cancelCount.push(m.tween({
      from: 0, to: years, duration: m.ms('--motion-count-duration'), easing: m.easing('--motion-count-easing'),
      onUpdate: function (v) { q.years.textContent = fmt(v); }
    }));
    cancelCount.push(m.tween({
      from: 0, to: good, duration: m.ms('--motion-count-good-duration'), delay: m.ms('--motion-count-good-delay'), easing: m.easing('--motion-count-easing'),
      onUpdate: function (v, k) {
        q.goodYears.textContent = fmt(v) + ' años';
        if (k > 0) q.reveal.classList.add('rv-good');
      }
    }));
  }

  function startBreath() {
    q.breathNext.textContent = 'Saltar';
    restartAnim(q.breath, 'br-on');
    timers.breath = setTimeout(function () { q.breathNext.textContent = 'Continuar'; }, BREATH_SKIP_MS);
  }

  function renderPlan() {
    q.planRows.innerHTML = D.planRows(st).map(function (r, i) {
      return '<div class="pl-row" style="--i:' + i + '"><span class="pl-tile">' + icon(r.icon, 22) + '</span>' +
        '<span class="pl-text"><span class="pl-key">' + esc(r.k) + '</span><span class="pl-value">' + esc(r.v) + '</span></span></div>';
    }).join('');
    restartAnim(q.plan, 'pl-on');
  }

  function reveal(container) {
    clearTimeout(timers.shown);
    container.classList.remove('is-shown');
    timers.shown = setTimeout(function () { container.classList.add('is-shown'); }, 420);
  }

  function commit() {
    holding = false;
    st.hold = 1;
    st.committed = true;
    st.petals = true;
    st.gust += 1;
    render();
    timers.commit = setTimeout(function () { api.go('perms'); }, SA.motion.ms('--motion-commit-duration'));
  }

  function holdLoop() {
    var last = 0;
    var total = SA.motion.ms('--motion-hold-duration');
    function frame(now) {
      if (st.step !== 10 || st.committed) return;
      var dt = last ? Math.min(now - last, 50) : 0;
      last = now;
      var h = st.hold;
      if (holding) h = Math.min(1, h + dt / total);
      else if (h > 0) h = Math.max(0, h - dt / HOLD_RELEASE_MS);
      if (h !== st.hold) {
        st.hold = h;
        q.holdFill.style.width = (h * 100).toFixed(1) + '%';
        tree.roots(st);
      }
      if (h >= 1) { commit(); return; }
      holdRaf = requestAnimationFrame(frame);
    }
    holdRaf = requestAnimationFrame(frame);
  }

  function setHolding(on) {
    if (st.committed || st.step !== 10) return;
    holding = on;
  }

  // ---------- interactions ----------

  function toggleGoal(i, btn) {
    var k = st.picks.indexOf(i);
    var icn = btn.querySelector('.ob-opt-icon');
    if (k >= 0) {
      st.picks.splice(k, 1);
    } else if (st.picks.length >= D.MAX_GOALS) {
      restartAnim(btn, 'is-shake');
      setTimeout(function () { btn.classList.remove('is-shake'); }, SA.motion.ms('--motion-shake-duration'));
      return;
    } else {
      st.picks.push(i);
      restartAnim(icn, 'is-pop');
    }
    render();
  }

  function grantPerm(i) {
    if (st.perms[i]) return;
    st.perms[i] = true;
    render();
    restartAnim(root.querySelectorAll('.ob-perm-tile')[i], 'is-pop');
  }

  function bind() {
    root.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !root.contains(t)) return;
      if (t.hasAttribute('data-next')) { api.next(); return; }
      if (t === q.back) { api.prev(); return; }
      if (t.dataset.goal !== undefined) { toggleGoal(+t.dataset.goal, t); return; }
      if (t.dataset.use !== undefined) { st.use = +t.dataset.use; render(); return; }
      if (t.dataset.moment !== undefined) { st.moment = +t.dataset.moment; render(); return; }
      if (t.dataset.perm !== undefined) { grantPerm(+t.dataset.perm); }
    });

    q.nameInput.addEventListener('input', function () {
      st.name = q.nameInput.value;
      render();
    });
    q.nameInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && st.name.trim()) api.next();
    });

    // Hold-to-commit: pointer and keyboard
    q.hold.addEventListener('pointerdown', function () { setHolding(true); });
    ['pointerup', 'pointerleave', 'pointercancel'].forEach(function (n) {
      q.hold.addEventListener(n, function () { setHolding(false); });
    });
    q.hold.addEventListener('keydown', function (e) {
      if ((e.key === ' ' || e.key === 'Enter') && !e.repeat) { e.preventDefault(); setHolding(true); }
    });
    q.hold.addEventListener('keyup', function (e) {
      if (e.key === ' ' || e.key === 'Enter') setHolding(false);
    });
    q.hold.addEventListener('contextmenu', function (e) { e.preventDefault(); });
  }

  // ---------- scene ----------

  var scene = {
    layer: 10,

    mount: function (host, shell) {
      api = shell;
      host.innerHTML = build();
      root = host;
      host.querySelectorAll('[data-el]').forEach(function (el) { q[el.dataset.el] = el; });
      tree = SA.onbTree.create(q.frame);
      layers = Array.prototype.map.call(host.querySelectorAll('.ob-layer'), function (el) {
        return { el: el, i: +el.dataset.i };
      });
      q.today.textContent = new Date().toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
      st = fresh();
      bind();
      render();
    },

    enter: function (ctx) {
      var step = STEPS.indexOf(ctx.id);
      var fromSplash = ctx.prevId === 'splash';
      clearTimers();
      st.step = step;
      st.petals = false;
      if (!fromSplash) st.gust += 1;
      if (step === 10) { st.hold = 0; st.committed = false; }
      if (step === 4) reveal(q.goalOpts);
      if (step === 6) reveal(q.momentOpts);
      if (step === 8) startBreath();
      if (step === 9) renderPlan();
      render();
      if (step === 7) startReveal();
      if (step === 10) holdLoop();
      if (fromSplash) {
        restartAnim(q.ob, 'is-entering');
        timers.enter = setTimeout(function () { q.ob.classList.remove('is-entering'); }, 1200);
      }
    },

    reset: function () {
      clearTimers();
      st = fresh();
      q.nameInput.value = '';
      q.ob.classList.remove('is-entering');
      render();
    }
  };

  SA.registry.defineScene('onboarding', scene);
})(window.SA = window.SA || {});
