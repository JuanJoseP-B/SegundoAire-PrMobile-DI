/*
 * Profile tab (avatar tree, goals, settings, share button) and its edit sheet ("Editar").
 * Mock-up only: nothing is saved anywhere; the name and goals only change what the demo shows.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var page = null;
  var q = {};
  var st = null;

  var sheetEl = null;
  var sq = {};
  var draft = null;

  var PLANTED = '16 oct 2026';
  var CHEVRON = 'M9 6l6 6-6 6';
  var MODE_NAME = { mindful: 'Mindful', strict: 'Strict' };
  var GOALS = [
    { label: 'Menos tiempo en redes', icon: 'M5 19c0-8 6-14 14-14 0 8-6 14-14 14zM5 19l7-7' },
    { label: 'Dormir mejor', icon: 'M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z' },
    { label: 'Concentrarme al estudiar', icon: 'M4 5.5A1.5 1.5 0 0 1 5.5 4H20v14H5.5A1.5 1.5 0 0 0 4 19.5zM4 19.5A1.5 1.5 0 0 0 5.5 21H20' },
    { label: 'Despertar con calma', icon: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M2 12h2M20 12h2' },
    { label: 'Menos aburrimiento', icon: 'M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z' }
  ];

  function fresh() { return { sound: true }; }

  function esc(t) { return main.esc(t); }

  // ---------- templates ----------

  function avatar() {
    return '<svg class="mn-avatar" aria-hidden="true" width="72" height="72" viewBox="0 0 72 72"><circle class="mn-avatar-bg" cx="36" cy="36" r="36"></circle>' +
      '<g class="mn-avatar-tree"><path class="mn-avatar-trunk" d="M36 64 C35 54 37 46 36 38" stroke-width="3" stroke-linecap="round" fill="none"></path>' +
      '<circle class="mn-avatar-a" cx="36" cy="28" r="15"></circle><circle class="mn-avatar-b" cx="25" cy="36" r="11"></circle><circle class="mn-avatar-c" cx="47" cy="36" r="11"></circle></g></svg>';
  }

  function row(label, right, extra) {
    return '<button type="button" class="mn-row" ' + (extra || '') + '><span class="mn-row-label">' + label + '</span>' + right + main.icon(CHEVRON, 18, 'mn-row-chev') + '</button>';
  }

  function html() {
    return '<header class="mn-profile-head mn-rise" style="--i:0">' + avatar() +
      '<div><h1 class="mn-serif mn-name" data-el="name"></h1><div class="mn-planted">Plantado el ' + PLANTED + '</div></div></header>' +
      '<div class="mn-eyebrow mn-eyebrow--pad mn-rise" style="--i:1">Tus metas</div>' +
      '<div class="mn-chips mn-chips--pad mn-rise" style="--i:1" data-el="goals"></div>' +
      '<div class="mn-eyebrow mn-eyebrow--pad mn-rise" style="--i:2">Ajustes</div>' +
      '<div class="mn-glass mn-settings mn-rise" style="--i:2">' +
      row('Plan y modo', '<span class="mn-row-value" data-el="mode"></span>', 'data-act="mode"') +
      '<div class="mn-divider"></div>' +
      '<div class="mn-row mn-row--static"><span class="mn-row-label">Sonidos y vibración</span>' +
      '<button type="button" class="mn-switch" role="switch" aria-checked="true" aria-label="Sonidos y vibración" data-el="sound">' +
      '<span class="sa-toggle" aria-hidden="true"><span class="sa-toggle-thumb"></span></span></button></div>' +
      '<div class="mn-divider"></div>' +
      row('Permisos', '<span class="mn-row-value mn-row-value--ok">Activos</span>') +
      '<div class="mn-divider"></div>' +
      row('Cuenta', '') + '</div>' +
      '<button type="button" class="mn-btn mn-share mn-rise" style="--i:3" data-act="share">' +
      main.icon('M12 15V3M7 8l5-5 5 5M5 14v5a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-5', 18) + 'Compartir mi árbol</button>';
  }

  // ---------- rendering ----------

  function render() {
    var s = main.session;
    q.name.textContent = main.name();
    q.mode.textContent = MODE_NAME[s.mode];
    q.sound.setAttribute('aria-checked', st.sound ? 'true' : 'false');
    q.goals.innerHTML = s.goals.map(function (g) {
      return '<span class="mn-chip mn-chip--static">' + main.icon(GOALS[g].icon, 14) + esc(GOALS[g].label) + '</span>';
    }).join('') + '<button type="button" class="mn-chip mn-chip--dashed" data-act="edit">Editar</button>';
  }

  // ---------- behavior ----------

  function bind() {
    page.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !page.contains(t)) return;
      if (t === q.sound) { st.sound = !st.sound; render(); return; }
      var act = t.dataset.act;
      if (act === 'edit') sheet.open();
      else if (act === 'mode') {
        main.session.mode = main.session.mode === 'mindful' ? 'strict' : 'mindful';
        render();
        main.toast('Rigor de los bloques: ' + MODE_NAME[main.session.mode]);
      } else if (act === 'share') main.toast('Tu árbol está listo para compartir');
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
      render();
    },

    enter: function () { render(); },

    exit: function () { sheet.close(false); },

    stop: function () {},

    reset: function () {
      st = fresh();
      render();
    }
  };

  // ---------- edit sheet (ProfileEditSheet) ----------

  function renderSheet() {
    sq.goals.innerHTML = GOALS.map(function (g, i) {
      var on = draft.goals.indexOf(i) >= 0;
      return '<button type="button" class="mn-chip' + (on ? ' mn-chip--solid' : '') + '" data-goal="' + i + '" aria-pressed="' + on + '">' +
        main.icon(g.icon, 14) + esc(g.label) + '</button>';
    }).join('');
  }

  function setSheet(on) {
    sheetEl.dataset.open = on ? 'true' : 'false';
    sheetEl.inert = !on;
  }

  var sheet = {
    html: function () {
      return '<div class="mn-sheet-scrim" data-el="scrim"></div>' +
        '<div class="mn-sheet glass-3" role="dialog" aria-modal="true" aria-label="Editar perfil">' +
        '<i class="mn-sheet-grab" aria-hidden="true"></i><h2 class="mn-sheet-title">Editar perfil</h2>' +
        '<div class="sa-field"><label class="ob-label" for="mn-name">Nombre</label>' +
        '<input id="mn-name" class="ob-input" type="text" autocomplete="off" spellcheck="false" data-el="input"></div>' +
        '<div class="mn-eyebrow mn-sheet-eyebrow">Tus metas</div><div class="mn-chips" data-el="goals"></div>' +
        '<button type="button" class="mn-btn mn-sheet-save" data-el="save">Guardar</button></div>';
    },

    mount: function (node, shell) {
      main = main || shell;
      sheetEl = node;
      sheetEl.querySelectorAll('[data-el]').forEach(function (n) { sq[n.dataset.el] = n; });
      setSheet(false);
      sq.scrim.addEventListener('click', function () { sheet.close(false); });
      sq.goals.addEventListener('click', function (e) {
        var b = e.target.closest('button');
        if (!b) return;
        var i = Number(b.dataset.goal);
        var at = draft.goals.indexOf(i);
        if (at >= 0) draft.goals.splice(at, 1); else draft.goals.push(i);
        renderSheet();
      });
      sq.save.addEventListener('click', function () {
        var name = sq.input.value.trim();
        main.session.profileName = name;
        main.session.goals = draft.goals.slice().sort();
        sheet.close(false);
        render();
      });
      sheetEl.addEventListener('keydown', function (e) { if (e.key === 'Escape') sheet.close(false); });
    },

    open: function () {
      draft = { goals: main.session.goals.slice() };
      sq.input.value = main.name();
      renderSheet();
      setSheet(true);
      sq.input.focus({ preventScroll: true });
    },

    close: function () { if (sheetEl) setSheet(false); },

    reset: function () { if (sheetEl) { setSheet(false); sq.input.value = ''; } }
  };

  SA.main.registerTab('profile', tab);
  SA.main.registerOverlay('sheet', sheet);
})(window.SA = window.SA || {});
