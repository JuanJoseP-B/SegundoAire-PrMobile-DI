/* Easing helpers: cubic-bezier solver matching CSS timing functions. */
(function (SA) {
  'use strict';

  var KEYWORDS = {
    linear: [0, 0, 1, 1],
    ease: [0.25, 0.1, 0.25, 1],
    'ease-in': [0.42, 0, 1, 1],
    'ease-out': [0, 0, 0.58, 1],
    'ease-in-out': [0.42, 0, 0.58, 1]
  };

  function clamp(v, min, max) { return Math.min(max, Math.max(min, v)); }
  function lerp(a, b, t) { return a + (b - a) * t; }

  function bezier(x1, y1, x2, y2) {
    if (x1 === y1 && x2 === y2) return function (t) { return t; };

    function coord(a, b, t) {
      var u = 1 - t;
      return 3 * u * u * t * a + 3 * u * t * t * b + t * t * t;
    }
    function slope(a, b, t) {
      var u = 1 - t;
      return 3 * u * u * a + 6 * u * t * (b - a) + 3 * t * t * (1 - b);
    }
    function solveT(x) {
      var t = x, i, err, d, lo = 0, hi = 1;
      for (i = 0; i < 8; i++) {
        err = coord(x1, x2, t) - x;
        if (Math.abs(err) < 1e-5) return t;
        d = slope(x1, x2, t);
        if (Math.abs(d) < 1e-6) break;
        t -= err / d;
      }
      t = x;
      for (i = 0; i < 24; i++) {
        err = coord(x1, x2, t) - x;
        if (Math.abs(err) < 1e-5) break;
        if (err > 0) hi = t; else lo = t;
        t = (lo + hi) / 2;
      }
      return t;
    }
    return function (x) {
      if (x <= 0) return 0;
      if (x >= 1) return 1;
      return coord(y1, y2, solveT(x));
    };
  }

  /** Parses a resolved CSS timing function ("cubic-bezier(...)" or a keyword). */
  function fromCss(text) {
    var s = String(text || '').trim().toLowerCase();
    var m = s.match(/cubic-bezier\(([^)]+)\)/);
    var p = m ? m[1].split(',').map(parseFloat) : KEYWORDS[s];
    return p && p.length === 4 ? bezier(p[0], p[1], p[2], p[3]) : function (t) { return t; };
  }

  SA.ease = { bezier: bezier, fromCss: fromCss, clamp: clamp, lerp: lerp };
})(window.SA = window.SA || {});
