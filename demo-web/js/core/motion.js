/*
 * Motion bridge: JS-driven animation reads durations and curves from the --motion-*
 * CSS variables (tokens.css), so the numbers live in one place.
 */
(function (SA) {
  'use strict';

  var probe = null;
  var query = window.matchMedia ? window.matchMedia('(prefers-reduced-motion: reduce)') : null;

  function getProbe() {
    if (!probe) {
      probe = document.createElement('div');
      probe.setAttribute('aria-hidden', 'true');
      probe.style.cssText = 'position:absolute;width:0;height:0;visibility:hidden;pointer-events:none';
      document.documentElement.appendChild(probe);
    }
    return probe;
  }

  /** Resolved value of a duration variable, in milliseconds. */
  function ms(name) {
    var el = getProbe();
    el.style.transitionDuration = 'var(' + name + ')';
    var v = getComputedStyle(el).transitionDuration.split(',')[0].trim();
    var n = parseFloat(v) || 0;
    return /ms$/.test(v) ? n : n * 1000;
  }

  /** Resolved easing function of an easing variable. */
  function easing(name) {
    var el = getProbe();
    el.style.transitionTimingFunction = 'var(' + name + ')';
    return SA.ease.fromCss(getComputedStyle(el).transitionTimingFunction.split(/,(?![^(]*\))/)[0]);
  }

  /** Runs onUpdate(value, progress) on every frame. Returns a cancel function. */
  function tween(o) {
    var start = performance.now() + (o.delay || 0);
    var duration = Math.max(1, o.duration);
    var ease = o.easing || function (t) { return t; };
    var raf = 0;
    var dead = false;

    function frame(now) {
      if (dead) return;
      var k = Math.min(1, Math.max(0, (now - start) / duration));
      o.onUpdate(SA.ease.lerp(o.from, o.to, ease(k)), k);
      if (k < 1) raf = requestAnimationFrame(frame);
      else if (o.onDone) o.onDone();
    }
    raf = requestAnimationFrame(frame);
    return function cancel() { dead = true; cancelAnimationFrame(raf); };
  }

  SA.motion = {
    reduced: function () { return !!(query && query.matches); },
    ms: ms,
    easing: easing,
    tween: tween
  };
})(window.SA = window.SA || {});
