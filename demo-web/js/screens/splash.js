/*
 * Splash "El Respiro" (BreathSplash): an organic form appears, breathes, takes one deep
 * breath and expands until it covers the screen; then the name is revealed and the
 * overlay fades out. Timings and curves come from the --motion-splash-* variables.
 * A tap (or "Siguiente") skips straight to the exit.
 */
(function (SA) {
  'use strict';

  var W = 393;
  var H = 852;
  var CX = W / 2;
  var CY = H / 2;
  var RADIUS = 75;
  var HALO_FACTOR = 1.9;
  var POINTS = 8;
  var AMPLITUDE = 0.055;
  var TWO_PI = Math.PI * 2;
  var LAYER_A_ALPHA = 0.55;
  var LAYER_B_ALPHA = 0.45;
  var REVEAL_LAG = 0.3;
  var EYEBROW_ALPHA = 0.85;
  var TITLE_RISE = 8;
  var START_SCALE = 0.55;
  var BREATH_SCALE = 1.14;
  var DEEP_SCALE = 1.6;
  var RELEASE_MARGIN = 1.15;

  var FREQ = [[1, 2, 3, 2, 1, 3, 2, 3], [2, 3, 1, 3, 2, 1, 3, 1]];
  var OFFS = [
    [0, 1.3, 2.6, 3.9, 5.2, 0.7, 2.0, 4.4],
    [2.2, 0.4, 3.3, 5.5, 1.1, 4.6, 0.9, 3.0]
  ];

  var els = {};
  var api = null;
  var raf = 0;
  var startedAt = 0;
  var plan = null;
  var leaving = false;
  var hideTimer = 0;
  var fosi = SA.ease.bezier(0.4, 0, 0.2, 1);

  /** Blob outline as a closed Catmull-Rom path around (0, 0). */
  function blobPath(phase, layer) {
    var xs = [];
    var ys = [];
    var i;
    for (i = 0; i < POINTS; i++) {
      var angle = -Math.PI / 2 + (TWO_PI * i) / POINTS;
      var r = RADIUS + RADIUS * AMPLITUDE * Math.sin(phase * FREQ[layer][i] + OFFS[layer][i]);
      xs.push(r * Math.cos(angle));
      ys.push(r * Math.sin(angle));
    }
    var d = 'M' + xs[0].toFixed(2) + ' ' + ys[0].toFixed(2);
    for (i = 0; i < POINTS; i++) {
      var p0 = (i + POINTS - 1) % POINTS;
      var p2 = (i + 1) % POINTS;
      var p3 = (i + 2) % POINTS;
      d += 'C' + (xs[i] + (xs[p2] - xs[p0]) / 6).toFixed(2) + ' ' + (ys[i] + (ys[p2] - ys[p0]) / 6).toFixed(2) +
        ' ' + (xs[p2] - (xs[p3] - xs[i]) / 6).toFixed(2) + ' ' + (ys[p2] - (ys[p3] - ys[i]) / 6).toFixed(2) +
        ' ' + xs[p2].toFixed(2) + ' ' + ys[p2].toFixed(2);
    }
    return d + 'Z';
  }

  function releaseTarget() {
    return (Math.hypot(W, H) / RADIUS) * RELEASE_MARGIN;
  }

  /** Reads the timeline once per run so a theme or motion change applies on restart. */
  function buildPlan() {
    var m = SA.motion;
    var reduced = m.reduced();
    var p = { reduced: reduced };
    if (reduced) {
      p.fade = m.ms('--motion-grow-duration');
      p.shapeHold = 450;
      p.titleHold = 600;
      p.total = p.fade + p.shapeHold + p.fade + p.titleHold;
      return p;
    }
    p.presence = m.ms('--motion-splash-presence-duration');
    p.breath = m.ms('--motion-splash-breath-duration');
    p.deep = m.ms('--motion-splash-deep-duration');
    p.deepHold = m.ms('--motion-splash-deep-hold');
    p.release = m.ms('--motion-splash-release-duration');
    p.solidDelay = m.ms('--motion-splash-solid-delay');
    p.solid = m.ms('--motion-splash-solid-duration');
    p.reveal = m.ms('--motion-splash-reveal-duration');
    p.revealHold = m.ms('--motion-splash-reveal-hold');
    p.exit = m.ms('--motion-splash-exit-duration');
    p.exitTail = m.ms('--motion-splash-exit-tail');
    p.ePresence = m.easing('--motion-splash-presence-easing');
    p.eDeep = m.easing('--motion-splash-deep-easing');
    p.eRelease = m.easing('--motion-splash-release-easing');
    p.eOut = m.easing('--motion-splash-out-easing');
    p.target = releaseTarget();
    p.total = p.presence + 2 * p.breath + p.deep + p.deepHold + p.release + p.reveal + p.revealHold;
    return p;
  }

  /** Form scale, form alpha, solid alpha and title progress at time t (ms). */
  function sample(t) {
    var p = plan;
    var s = { scale: 1, alpha: 1, solid: 0, title: 0 };
    var u;

    if (p.reduced) {
      s.alpha = Math.min(1, t / p.fade);
      u = t - p.fade - p.shapeHold;
      s.solid = s.title = SA.ease.clamp(u / p.fade, 0, 1);
      return s;
    }

    var t1 = p.presence;
    var t2 = t1 + p.breath;
    var t3 = t2 + p.breath;
    var t4 = t3 + p.deep;
    var t5 = t4 + p.deepHold;
    var t6 = t5 + p.release;

    if (t < t1) {
      u = p.ePresence(t / t1);
      s.scale = SA.ease.lerp(START_SCALE, 1, u);
      s.alpha = u;
    } else if (t < t2) {
      s.scale = SA.ease.lerp(1, BREATH_SCALE, fosi((t - t1) / p.breath));
    } else if (t < t3) {
      s.scale = SA.ease.lerp(BREATH_SCALE, 1, fosi((t - t2) / p.breath));
    } else if (t < t4) {
      s.scale = SA.ease.lerp(1, DEEP_SCALE, p.eDeep((t - t3) / p.deep));
    } else if (t < t5) {
      s.scale = DEEP_SCALE;
    } else {
      s.scale = SA.ease.lerp(DEEP_SCALE, p.target, p.eRelease(SA.ease.clamp((t - t5) / p.release, 0, 1)));
      s.solid = p.eOut(SA.ease.clamp((t - t5 - p.solidDelay) / p.solid, 0, 1));
      s.title = p.eOut(SA.ease.clamp((t - t6) / p.reveal, 0, 1));
    }
    return s;
  }

  function draw(now) {
    var t = now - startedAt;
    var s = sample(t);
    var phaseA = ((t % 7000) / 7000) * TWO_PI;
    var phaseB = ((t % 5500) / 5500) * TWO_PI;
    var span = (t / 12000) % 2;
    var rotation = 40 + 30 * fosi(span <= 1 ? span : 2 - span);

    if (plan.reduced) {
      phaseA = 1.1;
      phaseB = 3.4;
      rotation = 55;
    }

    els.form.setAttribute('transform', 'translate(' + CX + ' ' + CY + ') scale(' + s.scale.toFixed(4) + ')');
    els.rot.setAttribute('transform', 'rotate(' + rotation.toFixed(2) + ')');
    els.a.setAttribute('d', blobPath(phaseA, 0));
    els.b.setAttribute('d', blobPath(phaseB, 1));
    els.a.style.fillOpacity = (LAYER_A_ALPHA * s.alpha).toFixed(3);
    els.b.style.fillOpacity = (LAYER_B_ALPHA * s.alpha).toFixed(3);
    els.halo.style.opacity = s.alpha.toFixed(3);
    els.solid.style.opacity = s.solid.toFixed(3);

    var lag = plan.reduced ? 0 : REVEAL_LAG;
    var eyebrow = SA.ease.clamp((s.title - lag) / (1 - lag), 0, 1);
    var rise = plan.reduced ? 0 : TITLE_RISE;
    els.name.style.opacity = s.title.toFixed(3);
    els.name.style.transform = 'translateY(' + ((1 - s.title) * rise).toFixed(2) + 'px)';
    els.eyebrow.style.opacity = (eyebrow * EYEBROW_ALPHA).toFixed(3);
    els.eyebrow.style.transform = 'translateY(' + ((1 - eyebrow) * rise).toFixed(2) + 'px)';

    if (!leaving) api.setTone(s.solid > 0.5 ? 'light' : 'dark');

    if (!leaving && t >= plan.total) {
      finish();
      return;
    }
    raf = requestAnimationFrame(draw);
  }

  function finish() {
    if (leaving) return;
    api.go('welcome-1');
  }

  function stop() {
    cancelAnimationFrame(raf);
    clearTimeout(hideTimer);
  }

  var scene = {
    layer: 100,

    mount: function (host, shell) {
      api = shell;
      host.className += ' splash';
      host.innerHTML =
        '<svg class="splash-art" width="' + W + '" height="' + H + '" viewBox="0 0 ' + W + ' ' + H + '" aria-hidden="true">' +
        '<defs><radialGradient id="splashHalo">' +
        '<stop offset="0" class="splash-halo-in"></stop><stop offset="1" class="splash-halo-out"></stop>' +
        '</radialGradient></defs>' +
        '<g data-el="form">' +
        '<circle data-el="halo" r="' + (RADIUS * HALO_FACTOR) + '" fill="url(#splashHalo)"></circle>' +
        '<path data-el="a" class="splash-layer splash-layer--a"></path>' +
        '<g data-el="rot"><path data-el="b" class="splash-layer splash-layer--b"></path></g>' +
        '</g></svg>' +
        '<div class="splash-solid" data-el="solid"></div>' +
        '<div class="splash-title">' +
        '<h1 class="splash-name" data-el="name">SegundoAire</h1>' +
        '<p class="splash-eyebrow" data-el="eyebrow">Toma un segundo aire</p>' +
        '</div>' +
        '<button type="button" class="splash-skip" aria-label="SegundoAire cargando. Toca para omitir la intro"></button>';

      host.querySelectorAll('[data-el]').forEach(function (el) { els[el.dataset.el] = el; });
      host.querySelector('.splash-skip').addEventListener('click', finish);
      this.host = host;
    },

    enter: function () {
      var host = this.host;
      stop();
      leaving = false;
      host.style.transition = 'none';
      host.style.opacity = '1';
      host.style.visibility = 'visible';
      host.style.pointerEvents = 'auto';
      plan = buildPlan();
      startedAt = performance.now();
      api.setTone('dark');
      raf = requestAnimationFrame(draw);
    },

    leave: function () {
      var host = this.host;
      var exit = plan.reduced ? plan.fade : plan.exit;
      var tail = plan.reduced ? 0 : plan.exitTail;
      leaving = true;
      api.setTone('dark');
      host.style.pointerEvents = 'none';
      host.style.transition = 'opacity ' + exit + 'ms ' + (plan.reduced ? 'ease-out' : 'cubic-bezier(0, 0, 0.2, 1)');
      host.style.opacity = '0';
      clearTimeout(hideTimer);
      hideTimer = setTimeout(function () {
        stop();
        host.style.visibility = 'hidden';
      }, exit + tail);
    },

    reset: function () {
      stop();
      leaving = false;
    }
  };

  SA.registry.defineScene('splash', scene);
  SA.registry.defineScreens('splash', [{
    id: 'splash',
    label: 'Splash · El Respiro',
    hint: 'Una forma respira y se expande. Toca el teléfono para saltar.'
  }]);
})(window.SA = window.SA || {});
