/*
 * Main app scene (MainShell): the four tabs (Panel, Bloques, Progreso, Perfil) behind the floating
 * glass bottom bar, plus the full-screen overlays (focus mode, GlassWall, edit sheet).
 * Mock-up only: figures are fixed examples and nothing is saved.
 *
 * This file is the shell. Each tab / overlay lives in its own script and registers itself:
 *   SA.main.registerTab('panel', { html(), mount(page, main), enter(info), exit(), tick(), stop(), reset() })
 *   SA.main.registerOverlay('wall', { html(), mount(el, main), open(cfg), close(immediate), stop(), reset() })
 * The tour screens map to a tab plus an optional overlay (see SCREENS).
 */
(function (SA) {
  'use strict';

  var SPEED = 30;      // the demo block runs 30x faster than real time (as in the reference prototype)
  var TOAST_MS = 2600; // matches --motion-toast-duration
  var TICK_MS = 100;
  var ENTER_MS = 1400; // how long a page keeps its "entering" class (stagger + rise)

  // Tour screen -> tab index and optional overlay
  var SCREENS = {
    panel: { tab: 0 },
    blocks: { tab: 1 },
    focus: { tab: 1, overlay: 'focus' },
    progress: { tab: 2 },
    profile: { tab: 3 },
    wall: { tab: 0, overlay: 'wall', variant: 'mindful' },
    'wall-strict': { tab: 0, overlay: 'wall', variant: 'strict' }
  };

  // Tab icons (strokes of the reference prototype): leaf, clock, rings, person
  var TABS = [
    { id: 'panel', label: 'Panel', icon: 'M5 19c0-8 6-14 14-14 0 8-6 14-14 14zM5 19l7-7' },
    { id: 'blocks', label: 'Bloques', icon: 'M12 7v5l3 2M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z' },
    { id: 'progress', label: 'Progreso', icon: 'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM12 11a1 1 0 1 0 0 2 1 1 0 0 0 0-2z' },
    { id: 'profile', label: 'Perfil', icon: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 20c1.5-4 4.5-6 8-6s6.5 2 8 6' }
  ];

  var FALLBACK_NAME = 'Ana';

  var api = null;
  var host = null;
  var root = null;
  var q = {};
  var st = null;
  var tabMods = {};
  var ovMods = {};
  var tickers = [];
  var blockListeners = [];
  var tickTimer = 0;
  var toastTimer = 0;
  var enterTimer = 0;
  var freshTimer = 0;

  // ---------- shared session (fixed example data) ----------

  var session = {};

  function freshSession() {
    return {
      saved: 47,          // minutes recovered today
      pauses: 3,          // respected pauses today
      leaves: 4,          // leaves of the tree (one per respected pause of the week)
      mode: 'mindful',
      dur: 2,             // index in 5 / 10 / 15 / 30 min
      block: null,        // { total, start } while a block runs
      profileName: '',
      goals: [0, 1]
    };
  }

  function resetSession() {
    var f = freshSession();
    Object.keys(session).forEach(function (k) { delete session[k]; });
    Object.keys(f).forEach(function (k) { session[k] = f[k]; });
  }
  resetSession();

  function fresh() { return { tab: -1, overlay: null }; }

  // ---------- helpers ----------

  function esc(t) {
    return String(t).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }

  function icon(path, size, cls) {
    return '<svg' + (cls ? ' class="' + cls + '"' : '') + ' width="' + size + '" height="' + size + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
      'stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="' + path + '"></path></svg>';
  }

  function userName() {
    return session.profileName || (SA.session && SA.session.name) || FALLBACK_NAME;
  }

  // ---------- demo block (decorative timer, 30x) ----------

  function blockState() {
    var b = session.block;
    if (!b) return null;
    var elapsed = (performance.now() - b.start) * SPEED;
    var prog = Math.min(1, elapsed / b.total);
    return { prog: prog, remain: Math.max(0, b.total - elapsed), total: b.total, finished: prog >= 1 };
  }

  function fmtClock(ms) {
    var s = Math.ceil(ms / 1000);
    return Math.floor(s / 60) + ':' + ('0' + (s % 60)).slice(-2);
  }

  function emitBlock() { blockListeners.forEach(function (fn) { fn(); }); }

  function ensureTick() {
    if (tickTimer || !session.block) return;
    tickTimer = setInterval(function () {
      if (!session.block) { clearInterval(tickTimer); tickTimer = 0; return; }
      tickers.forEach(function (fn) { fn(); });
    }, TICK_MS);
  }

  function stopTick() {
    clearInterval(tickTimer);
    tickTimer = 0;
  }

  var block = {
    start: function (min) {
      session.block = { total: min * 60000, start: performance.now() };
      ensureTick();
      emitBlock();
    },
    end: function () {
      session.block = null;
      stopTick();
      emitBlock();
    },
    state: blockState,
    clock: fmtClock
  };

  // ---------- templates ----------

  function build() {
    var pages = TABS.map(function (t, i) {
      return '<section class="mn-page" data-tab="' + i + '" data-pos="1" aria-label="' + t.label + '" inert>' +
        '<div class="mn-scroll"><div class="mn-content" data-mod="' + t.id + '">' + tabMods[t.id].html() + '</div></div></section>';
    }).join('');
    var nav = TABS.map(function (t, i) {
      return '<button type="button" class="mn-navbtn" data-nav="' + i + '" aria-current="false">' +
        '<span class="mn-navicon">' + icon(t.icon, 22) + '</span><span class="mn-navlbl">' + t.label + '</span></button>';
    }).join('');
    var overlays = Object.keys(ovMods).map(function (id) {
      return '<div class="mn-ov mn-ov--' + id + '" data-ov="' + id + '">' + ovMods[id].html() + '</div>';
    }).join('');
    return '<div class="mn sa-surface" data-el="mn">' + pages +
      '<nav class="mn-nav glass-3" aria-label="Navegación principal"><i class="mn-nav-ind" aria-hidden="true"></i>' + nav + '</nav>' +
      '<div class="mn-toast glass-2" role="status" data-el="toast"></div>' + overlays + '</div>';
  }

  // ---------- behavior ----------

  function toast(text) {
    q.toast.textContent = text;
    q.toast.classList.remove('is-on');
    void q.toast.offsetWidth;
    q.toast.classList.add('is-on');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { q.toast.classList.remove('is-on'); }, TOAST_MS);
  }

  function restartEntering(page) {
    page.classList.remove('is-entering');
    void page.offsetWidth;
    page.classList.add('is-entering');
    clearTimeout(enterTimer);
    enterTimer = setTimeout(function () { page.classList.remove('is-entering'); }, ENTER_MS);
  }

  function showTab(i, first) {
    var prev = st.tab;
    if (prev === i) return;
    if (first) q.mn.classList.add('is-instant');
    q.pages.forEach(function (p, k) {
      p.dataset.pos = k === i ? '0' : (k < i ? '-1' : '1');
      p.inert = k !== i;
    });
    q.mn.style.setProperty('--tab', String(i));
    q.navs.forEach(function (b, k) { b.setAttribute('aria-current', k === i ? 'page' : 'false'); });
    if (first) { void q.mn.offsetWidth; q.mn.classList.remove('is-instant'); }

    if (prev >= 0 && tabMods[TABS[prev].id].exit) tabMods[TABS[prev].id].exit();
    st.tab = i;
    q.pages[i].querySelector('.mn-scroll').scrollTop = 0;
    restartEntering(q.pages[i]);
    tabMods[TABS[i].id].enter({ first: !!first || prev < 0 });
  }

  function setActive(on) {
    host.style.visibility = on ? 'visible' : 'hidden';
    host.inert = !on;
    if (on) host.removeAttribute('data-off'); else host.setAttribute('data-off', '');
  }

  function closeOverlays(immediate) {
    Object.keys(ovMods).forEach(function (id) { ovMods[id].close(immediate); });
    st.overlay = null;
  }

  function stopAll() {
    stopTick();
    clearTimeout(toastTimer);
    clearTimeout(enterTimer);
    clearTimeout(freshTimer);
    Object.keys(tabMods).forEach(function (id) { if (tabMods[id].stop) tabMods[id].stop(); });
    Object.keys(ovMods).forEach(function (id) { if (ovMods[id].stop) ovMods[id].stop(); });
  }

  function bind() {
    root.addEventListener('click', function (e) {
      var nav = e.target.closest('.mn-navbtn');
      if (nav && root.contains(nav)) api.go(TABS[Number(nav.dataset.nav)].id);
    });
  }

  // ---------- scene ----------

  var scene = {
    layer: 40,

    mount: function (el, shell) {
      api = shell;
      host = el;
      root = host;
      st = fresh();
      host.innerHTML = build();
      setActive(false);
      host.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      q.pages = Array.prototype.slice.call(host.querySelectorAll('.mn-page'));
      q.navs = Array.prototype.slice.call(host.querySelectorAll('.mn-navbtn'));
      q.pages.forEach(function (p, i) { tabMods[TABS[i].id].mount(p, SA.main); });
      Object.keys(ovMods).forEach(function (id) { ovMods[id].mount(host.querySelector('[data-ov="' + id + '"]'), SA.main); });
      bind();
    },

    enter: function (ctx) {
      var cfg = SCREENS[ctx.id];
      var first = ctx.sceneChange || st.tab < 0;
      setActive(true);
      if (first) {
        q.mn.classList.remove('is-fresh');
        void q.mn.offsetWidth;
        q.mn.classList.add('is-fresh');
        clearTimeout(freshTimer);
        freshTimer = setTimeout(function () { q.mn.classList.remove('is-fresh'); }, ENTER_MS);
      }
      var hadOverlay = st.overlay;
      var prevTab = st.tab;
      if (st.overlay && st.overlay !== cfg.overlay) {
        ovMods[st.overlay].close(false);
        st.overlay = null;
      }
      showTab(cfg.tab, first);
      // Same tab after an overlay closed (wall -> panel, focus -> blocks): let the tab refresh what changed
      if (hadOverlay && !cfg.overlay && prevTab === cfg.tab) tabMods[TABS[cfg.tab].id].enter({ first: false, revisit: true });
      if (cfg.overlay) {
        st.overlay = cfg.overlay;
        ovMods[cfg.overlay].open(cfg);
      }
      ensureTick();
      api.setTone(cfg.overlay === 'focus' ? 'focus' : 'dark');
    },

    leave: function () {
      stopAll();
      closeOverlays(true);
      // Coming back re-enters as a fresh visit (instant placement, cards rise again)
      if (st.tab >= 0 && tabMods[TABS[st.tab].id].exit) tabMods[TABS[st.tab].id].exit();
      st.tab = -1;
      setActive(false);
    },

    reset: function () {
      stopAll();
      resetSession();
      closeOverlays(true);
      Object.keys(tabMods).forEach(function (id) { if (tabMods[id].reset) tabMods[id].reset(); });
      Object.keys(ovMods).forEach(function (id) { if (ovMods[id].reset) ovMods[id].reset(); });
      if (q.toast) q.toast.classList.remove('is-on');
      st = fresh();
      if (q.pages) {
        q.pages.forEach(function (p) { p.dataset.pos = '1'; p.inert = true; });
        q.navs.forEach(function (b) { b.setAttribute('aria-current', 'false'); });
      }
      setActive(false);
    }
  };

  SA.main = {
    session: session,
    block: block,
    esc: esc,
    icon: icon,
    name: userName,
    go: function (id) { api.go(id); },
    toast: toast,
    onTick: function (fn) { tickers.push(fn); },
    onBlock: function (fn) { blockListeners.push(fn); },
    registerTab: function (id, mod) { tabMods[id] = mod; },
    registerOverlay: function (id, mod) { ovMods[id] = mod; }
  };

  SA.registry.defineScene('main', scene);
  SA.registry.defineScreens('main', [
    { id: 'panel', label: 'App · Panel', hint: 'Tu árbol, los minutos recuperados y el coach. Pregúntale algo o abre la app simulada.' },
    { id: 'blocks', label: 'App · Bloques', hint: 'Elige duración y modo. Mantén el botón 1,5 s para empezar un bloque.' },
    { id: 'focus', label: 'App · Modo enfoque', hint: 'El temporizador corre acelerado, solo de forma decorativa. Terminar o minimizar vuelve a Bloques.' },
    { id: 'progress', label: 'App · Progreso', hint: 'Toca un día de la semana para iluminar su anillo en el corte del tronco.' },
    { id: 'profile', label: 'App · Perfil', hint: 'Tu árbol, tus metas y los ajustes. "Editar" abre la hoja de perfil.' },
    { id: 'wall', label: 'GlassWall · Mindful', hint: 'El muro sobre una app vigilada: respira 8 s, luego decides. También sale desde "Simular abrir app" en el Panel.' },
    { id: 'wall-strict', label: 'GlassWall · Strict', hint: 'Copa gris, candado y sin opción de continuar.' }
  ]);
})(window.SA = window.SA || {});
