/*
 * Setup scene: app selection ("Elige las apps que quieres vigilar") and the provisional
 * "Continuará" closing curtain. Mock-up only: the apps are examples with generic icons
 * (shapes on the app1..app5 tokens, never brand logos) and the toggles only change their look.
 *
 * Permissions are step 9 of the onboarding (onboarding.js), as in the Android app.
 */
(function (SA) {
  'use strict';

  var api = null;
  var root = null;
  var host = null;
  var q = {};
  var st = null;
  var enterTimer = 0;

  // `suggested` rows go first under "Sugeridas"; the rest are listed alphabetically.
  var APPS = [
    { name: 'Red social', suggested: true },
    { name: 'Videos cortos', suggested: true },
    { name: 'Mensajería', suggested: true },
    { name: 'Juegos', suggested: true },
    { name: 'Compras' }, { name: 'Correo' }, { name: 'Fotos' }, { name: 'Mapas' },
    { name: 'Música' }, { name: 'Navegador' }, { name: 'Notas' }, { name: 'Noticias' }
  ];
  var INITIAL = ['Red social', 'Videos cortos'];

  function fresh() {
    return { query: '', picked: INITIAL.slice(), end: false };
  }

  function esc(t) {
    return String(t).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }

  function norm(t) {
    return t.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '');
  }

  // ---------- templates ----------

  function row(app) {
    var slot = APPS.indexOf(app) % 5;
    var on = st.picked.indexOf(app.name) >= 0;
    return '<button type="button" role="switch" aria-checked="' + on + '" class="su-row" data-app="' + esc(app.name) + '">' +
      '<span class="su-icon" data-slot="' + slot + '" aria-hidden="true"></span>' +
      '<span class="su-name">' + esc(app.name) + '</span>' +
      '<span class="sa-toggle" aria-hidden="true"><span class="sa-toggle-thumb"></span></span></button>';
  }

  function section(title, apps) {
    if (!apps.length) return '';
    return '<h3 class="su-section">' + title + '</h3>' + apps.map(row).join('');
  }

  function build() {
    return '<div class="su sa-surface" data-el="su"><div class="ob-frame">' +
      '<section class="su-apps" data-el="appsView">' +
      '<div class="su-body"><h2 class="sa-title">Elige las apps que quieres vigilar</h2>' +
      '<p class="sa-sub">Tendrás una pausa antes de que se abran. Puedes cambiarlas cuando quieras.</p>' +
      '<div class="sa-field su-search"><label class="ob-label" for="su-q">Buscar apps</label>' +
      '<input id="su-q" class="ob-input" type="text" placeholder="Nombre de la app" autocomplete="off" spellcheck="false" data-el="search"></div>' +
      '<p class="su-count" data-el="count" aria-live="polite"></p>' +
      '<div class="su-list" data-el="list"></div></div>' +
      '<button type="button" class="ob-btn" data-el="confirm">Continuar</button></section></div>' +
      '<section class="su-end" data-el="end" inert>' +
      '<div class="ob-frame su-end-frame">' +
      '<div class="su-end-brand"><div class="ob-wordmark">SegundoAire</div><div class="ob-tagline">Tu árbol ya echó raíces.</div></div>' +
      '<div class="su-end-body">' +
      '<svg class="ob-curtain-tree su-end-tree" aria-hidden="true" width="150" height="150" viewBox="0 0 190 190">' +
      '<path class="ct-trunk" d="M95 190 C93 160 97 130 95 100"></path>' +
      '<circle class="ct-a" cx="95" cy="70" r="44"></circle><circle class="ct-b" cx="58" cy="92" r="32"></circle>' +
      '<circle class="ct-c" cx="132" cy="92" r="32"></circle><circle class="ct-b" cx="76" cy="52" r="28"></circle>' +
      '<circle class="ct-c" cx="118" cy="50" r="28"></circle><circle class="ct-dot" cx="70" cy="66" r="6"></circle><circle class="ct-dot" cx="124" cy="78" r="6"></circle></svg>' +
      '<h2 class="ob-done-title">Continuará</h2>' +
      '<p class="ob-done-body">El recorrido sigue con el panel principal.</p>' +
      '<button type="button" class="ob-ghost" data-el="restart">Reiniciar recorrido</button></div></div></section></div>';
  }

  // ---------- rendering ----------

  function renderList() {
    var key = norm(st.query.trim());
    var matches = APPS.filter(function (a) { return !key || norm(a.name).indexOf(key) >= 0; });
    var html;
    if (!matches.length) {
      html = '<div class="su-empty"><p class="su-empty-title">Sin coincidencias</p>' +
        '<p class="su-empty-msg">Ninguna app coincide con "' + esc(st.query.trim()) + '". Prueba con otro nombre.</p></div>';
    } else if (key) {
      html = section('Resultados', matches);
    } else {
      html = section('Sugeridas', matches.filter(function (a) { return a.suggested; })) +
        section('Todas las apps', matches.filter(function (a) { return !a.suggested; }));
    }
    q.list.innerHTML = html;
  }

  function renderState() {
    var n = st.picked.length;
    q.count.textContent = n + (n === 1 ? ' app elegida' : ' apps elegidas');
    q.confirm.disabled = n === 0;
    q.list.querySelectorAll('.su-row').forEach(function (el) {
      el.setAttribute('aria-checked', st.picked.indexOf(el.dataset.app) >= 0 ? 'true' : 'false');
    });
    q.su.dataset.end = st.end ? 'true' : 'false';
    q.end.inert = !st.end;
    q.appsView.inert = st.end;
    if (host.style.visibility === 'visible') api.setTone(st.end ? 'light' : 'dark');
  }

  // ---------- behavior ----------

  function toggleApp(name) {
    var i = st.picked.indexOf(name);
    if (i >= 0) st.picked.splice(i, 1); else st.picked.push(name);
    renderState();
  }

  function bind() {
    root.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !root.contains(t) || t.disabled) return;
      if (t === q.confirm) { api.next(); return; }
      if (t === q.restart) { api.restart(); return; }
      if (t.dataset.app !== undefined) toggleApp(t.dataset.app);
    });

    q.search.addEventListener('input', function () {
      st.query = q.search.value;
      renderList();
      renderState();
    });
  }

  function restartEnter() {
    var el = q.appsView;
    el.classList.remove('is-entering');
    void el.offsetWidth;
    el.classList.add('is-entering');
    clearTimeout(enterTimer);
    enterTimer = setTimeout(function () { el.classList.remove('is-entering'); }, 1200);
  }

  // ---------- scene ----------

  var scene = {
    layer: 30,

    mount: function (el, shell) {
      api = shell;
      host = el;
      host.innerHTML = build();
      host.style.visibility = 'hidden';
      root = host;
      host.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      st = fresh();
      bind();
      renderList();
      renderState();
    },

    enter: function (ctx) {
      host.style.visibility = 'visible';
      st.end = ctx.id === 'end';
      if (!st.end && ctx.sceneChange) restartEnter();
      renderState();
    },

    leave: function () {
      clearTimeout(enterTimer);
      host.style.visibility = 'hidden';
    },

    reset: function () {
      clearTimeout(enterTimer);
      st = fresh();
      q.search.value = '';
      host.style.visibility = 'hidden';
      renderList();
      renderState();
    }
  };

  SA.registry.defineScene('setup', scene);
  SA.registry.defineScreens('setup', [
    { id: 'apps', label: 'Setup · Apps a vigilar', hint: 'Elige apps de ejemplo con los toggles. La búsqueda filtra la lista.' },
    { id: 'end', label: 'Continuará', hint: 'Fin de esta parte del recorrido. El panel principal llega en la siguiente etapa.' }
  ]);
})(window.SA = window.SA || {});
