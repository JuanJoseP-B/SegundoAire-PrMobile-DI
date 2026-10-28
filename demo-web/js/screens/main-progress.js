/*
 * Progress tab: trunk cross-section (one ring per day), the week as seven stems and four stat cards.
 * Mock-up only: all figures are fixed examples.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var page = null;
  var q = {};
  var st = null;

  var GOAL = 30;      // minutes a day that make a flower
  var TODAY = 4;      // index of "Hoy" in the week
  // Daily history, oldest first; the last five entries are the current Monday..Friday
  var HISTORY = [22, 35, 12, 40, 28, 15, 38, 32, 18, 45, 52, 47];
  var DAYS = [
    { d: 'L', name: 'Lun 12', min: 32 }, { d: 'M', name: 'Mar 13', min: 18 }, { d: 'X', name: 'Mié 14', min: 45 },
    { d: 'J', name: 'Jue 15', min: 52 }, { d: 'V', name: 'Hoy', min: 47 }, { d: 'S', name: 'Sáb 17', min: -1 }, { d: 'D', name: 'Dom 18', min: -1 }
  ];
  var STATS = [
    { v: '3 h 14 min', k: 'recuperados esta semana' }, { v: '41', k: 'aperturas evitadas' },
    { v: '5 días', k: 'mejor racha' }, { v: '21:00', k: 'tu hora más difícil' }
  ];

  function fresh() { return { day: TODAY }; }

  /** Today follows the minutes the Panel shows; the other days are fixed. */
  function dayMin(i) { return i === TODAY ? main.session.saved : DAYS[i].min; }

  function stemHeight(min) { return min < 0 ? 6 : Math.round(Math.min(min, 60) / 60 * 96) + 6; }

  // ---------- templates ----------

  function rings() {
    return HISTORY.map(function (v, i) {
      var rx = 14 + (i + 1) * 9;
      var wob = (i % 3) - 1;
      return '<ellipse class="mn-ring' + (v >= GOAL ? ' is-goal' : '') + '" data-ring="' + i + '" style="--k:' + i + ';stroke-width:' + Math.max(1.5, v / 12).toFixed(2) + '" ' +
        'cx="' + (130 + wob) + '" cy="' + (115 - wob) + '" rx="' + rx + '" ry="' + (rx * 0.87).toFixed(1) + '" fill="none" pathLength="1"></ellipse>';
    }).join('');
  }

  function flower() {
    return '<svg width="16" height="16" viewBox="-10 -10 20 20" aria-hidden="true"><circle class="mn-petal" cx="0" cy="-5" r="4"></circle>' +
      '<circle class="mn-petal" cx="4.8" cy="-1.5" r="4"></circle><circle class="mn-petal" cx="3" cy="4" r="4"></circle>' +
      '<circle class="mn-petal" cx="-3" cy="4" r="4"></circle><circle class="mn-petal" cx="-4.8" cy="-1.5" r="4"></circle><circle class="mn-gold" r="2.5"></circle></svg>';
  }

  function html() {
    var week = DAYS.map(function (d, i) {
      var future = d.min < 0;
      return '<button type="button" class="mn-day' + (future ? ' is-future' : '') + '" data-day="' + i + '" aria-pressed="false" ' +
        (future ? 'aria-disabled="true" ' : '') + 'aria-label="' + d.name + (future ? ', sin datos' : ', ' + d.min + ' min recuperados') + '">' +
        '<span class="mn-day-flower" data-flower="' + (d.min >= GOAL ? 'on' : 'off') + '">' + flower() + '</span>' +
        '<span class="mn-stem" data-el="stem' + i + '"></span><span class="mn-day-lbl">' + d.d + '</span></button>';
    }).join('');
    var stats = STATS.map(function (s, i) {
      return '<div class="mn-glass mn-stat mn-rise" style="--i:' + (5 + i) + '"><div class="mn-serif mn-stat-v">' + s.v + '</div><div class="mn-stat-k">' + s.k + '</div></div>';
    }).join('');
    return '<header class="mn-head mn-rise" style="--i:0"><div class="mn-eyebrow">Progreso</div>' +
      '<h1 class="mn-serif mn-title">Tus anillos de <em>crecimiento</em></h1></header>' +
      '<div class="mn-trunk mn-rise" style="--i:1"><svg class="mn-rings" data-el="rings" aria-hidden="true" width="260" height="230" viewBox="0 0 260 230">' +
      '<ellipse class="mn-wood" cx="130" cy="115" rx="124" ry="108"></ellipse>' + rings() + '<circle class="mn-pith" cx="130" cy="115" r="5"></circle></svg></div>' +
      '<div class="mn-daytitle mn-rise" style="--i:2" aria-live="polite"><div class="mn-serif mn-daytitle-t" data-el="dayTitle"></div><div class="mn-daytitle-s" data-el="daySub"></div></div>' +
      '<section class="mn-glass mn-week mn-rise" style="--i:3" aria-label="Esta semana"><div class="mn-week-head"><div class="mn-card-title">Esta semana</div>' +
      '<div class="mn-week-goal">Meta: ' + GOAL + ' min/día</div></div><div class="mn-week-grid" data-el="week">' + week + '</div></section>' +
      '<div class="mn-stats-head mn-rise" style="--i:4"><span class="mn-note">Datos de ejemplo</span></div>' +
      '<div class="mn-stats">' + stats + '</div>';
  }

  // ---------- rendering ----------

  function renderDay() {
    var d = DAYS[st.day];
    var min = Math.max(0, dayMin(st.day));
    q.dayTitle.textContent = d.name + ' · ' + min + ' min';
    q.daySub.textContent = min >= GOAL ? 'Día de flor: cumpliste tu meta' : 'Un anillo fino también es crecimiento';
    var selRing = HISTORY.length - 5 + st.day;
    q.rings.querySelectorAll('.mn-ring').forEach(function (r) {
      r.classList.toggle('is-sel', Number(r.dataset.ring) === selRing);
    });
    page.querySelectorAll('.mn-day').forEach(function (b) {
      var i = Number(b.dataset.day);
      b.setAttribute('aria-pressed', i === st.day ? 'true' : 'false');
      var stem = q['stem' + i];
      stem.classList.toggle('is-sel', i === st.day);
      stem.style.height = stemHeight(dayMin(i)) + 'px';
    });
  }

  /** Stems grow from the ground and the rings draw themselves, every time the tab is shown. */
  function replay() {
    q.rings.classList.remove('is-drawing');
    page.querySelectorAll('.mn-stem').forEach(function (s) { s.classList.add('no-tr'); s.style.height = '6px'; });
    void q.rings.getBoundingClientRect();
    q.rings.classList.add('is-drawing');
    page.querySelectorAll('.mn-stem').forEach(function (s) { s.classList.remove('no-tr'); });
    renderDay();
  }

  // ---------- behavior ----------

  function bind() {
    page.addEventListener('click', function (e) {
      var b = e.target.closest('.mn-day');
      if (!b || !page.contains(b) || DAYS[Number(b.dataset.day)].min < 0) return;
      st.day = Number(b.dataset.day);
      renderDay();
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
      renderDay();
    },

    enter: function () { replay(); },

    reset: function () {
      st = fresh();
      renderDay();
    }
  };

  SA.main.registerTab('progress', tab);
})(window.SA = window.SA || {});
