/*
 * Focus mode overlay (FocusMode): always dark, in both themes. Big ring with a breathing tip,
 * the remaining time and the quote. Mock-up only: the clock runs 30x faster than real time.
 */
(function (SA) {
  'use strict';

  var main = SA.main;
  var el = null;
  var q = {};

  var CX = 196.5;
  var CY = 330;
  var R = 120;
  var DURATIONS = [5, 10, 15, 30];
  var MODE_LABEL = { mindful: 'Mindful', strict: 'Strict' };

  function html() {
    return '<div class="mn-focus-eyebrow">Bloque de enfoque · <span data-el="mode">Mindful</span></div>' +
      '<svg class="mn-focus-art" aria-hidden="true" viewBox="0 0 393 852">' +
      '<g class="mn-focus-tree">' +
      '<path class="mn-focus-trunk" d="M196.5 852 C193.5 798 199.5 768 196.5 728" stroke-width="6" fill="none"></path>' +
      '<circle class="mn-focus-a" cx="196.5" cy="698" r="58"></circle><circle class="mn-focus-b" cx="151.5" cy="723" r="40"></circle>' +
      '<circle class="mn-focus-a" cx="241.5" cy="723" r="40"></circle><circle class="mn-focus-b" cx="171.5" cy="668" r="36"></circle>' +
      '<circle class="mn-focus-a" cx="223.5" cy="664" r="36"></circle></g>' +
      '<circle class="mn-focus-track" cx="' + CX + '" cy="' + CY + '" r="' + R + '" fill="none" stroke-width="10"></circle>' +
      '<circle class="mn-focus-progress" data-el="ring" cx="' + CX + '" cy="' + CY + '" r="' + R + '" fill="none" stroke-width="10" stroke-linecap="round" ' +
      'pathLength="1" stroke-dasharray="1" stroke-dashoffset="1" transform="rotate(-90 ' + CX + ' ' + CY + ')"></circle>' +
      '<circle class="mn-focus-halo" data-el="halo" r="16" cx="' + CX + '" cy="' + (CY - R) + '"></circle>' +
      '<circle class="mn-focus-tip" data-el="tip" r="7" cx="' + CX + '" cy="' + (CY - R) + '"></circle></svg>' +
      '<div class="mn-focus-time"><div class="mn-focus-clock" data-el="clock">15:00</div><div class="mn-focus-sub">restantes</div></div>' +
      '<p class="mn-focus-quote">Tu árbol crece mientras no miras.</p>' +
      '<button type="button" class="mn-focus-end" data-el="end">Terminar bloque</button>' +
      '<button type="button" class="mn-focus-min" data-el="min" aria-label="Minimizar">' + SA.main.icon('M6 9l6 6 6-6', 24) + '</button>';
  }


  function render() {
    var s = main.block.state();
    if (!s) return;
    var ang = -Math.PI / 2 + s.prog * Math.PI * 2;
    var x = (CX + Math.cos(ang) * R).toFixed(1);
    var y = (CY + Math.sin(ang) * R).toFixed(1);
    q.ring.setAttribute('stroke-dashoffset', (1 - s.prog).toFixed(4));
    q.tip.setAttribute('cx', x);
    q.tip.setAttribute('cy', y);
    q.halo.setAttribute('cx', x);
    q.halo.setAttribute('cy', y);
    q.clock.textContent = main.block.clock(s.remain);
    q.end.textContent = s.finished ? 'Volver al panel' : 'Terminar bloque';
    q.mode.textContent = MODE_LABEL[main.session.mode];
  }

  function setOpen(on) {
    el.dataset.open = on ? 'true' : 'false';
    el.inert = !on;
  }

  var overlay = {
    html: html,

    mount: function (node, shell) {
      main = shell;
      el = node;
      el.setAttribute('role', 'dialog');
      el.setAttribute('aria-label', 'Bloque de enfoque');
      el.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      setOpen(false);
      q.end.addEventListener('click', function () {
        var s = main.block.state();
        var finished = !!s && s.finished;
        main.block.end();
        if (finished) {
          main.session.saved += DURATIONS[main.session.dur];
          main.session.leaves += 1;
          main.go('panel');
        } else {
          main.go('blocks');
          main.toast('Bloque terminado. Mañana sigue creciendo.');
        }
      });
      q.min.addEventListener('click', function () { main.go('blocks'); });
      el.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') main.go('blocks');
      });
      main.onTick(function () { if (el.dataset.open === 'true') render(); });
    },

    open: function () {
      if (!main.block.state()) main.block.start(DURATIONS[main.session.dur]);
      render();
      setOpen(true);
    },

    close: function () { setOpen(false); },

    reset: function () { setOpen(false); }
  };

  SA.main.registerOverlay('focus', overlay);
})(window.SA = window.SA || {});
