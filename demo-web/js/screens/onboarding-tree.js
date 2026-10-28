/*
 * The onboarding world: sky, hill, falling leaves and the tree that grows with every
 * answer. Pure view: update(state) restyles existing nodes, nothing is rebuilt.
 */
(function (SA) {
  'use strict';

  var D = SA.onbData;
  var T = D.tree;
  var W = 390;
  var SKY_FOR_MOMENT = ['morning', 'afternoon', 'night', 'late'];
  var NAME_LEAF_MAX = 10;

  function v(name) { return 'var(' + name + ')'; }

  /** One CSS transition entry; delayMs follows the reduced-motion scale. */
  function tr(prop, dur, ease, delayMs) {
    return prop + ' ' + v(dur) + ' ' + v(ease) + ' ' +
      (delayMs ? 'calc(' + delayMs + 'ms * var(--motion-scale))' : '0ms');
  }

  function html() {
    var i;
    var s = '';

    s += '<div class="ob-sun ob-sun--1" aria-hidden="true"></div><div class="ob-sun ob-sun--2" aria-hidden="true"></div>';
    SKY_FOR_MOMENT.forEach(function (n) { s += '<div class="ob-sky ob-sky--' + n + '" data-el="sky" aria-hidden="true"></div>'; });

    s += '<div class="ob-fireflies" data-el="fireflies" aria-hidden="true">';
    [[60, 560, 0], [140, 640, 1], [250, 590, 2], [320, 680, 0.5], [200, 720, 1.5], [30, 700, 2.5]].forEach(function (f) {
      s += '<i class="ob-ff" style="left:' + f[0] + 'px;top:' + f[1] + 'px;animation-delay:' + f[2] + 's"></i>';
    });
    s += '</div>';

    s += '<div class="ob-hill" data-el="hill" aria-hidden="true">' +
      '<svg width="590" height="1000" viewBox="-100 0 590 1000">' +
      '<path class="ob-hill-fill" d="M-100 28 L-10 26 C70 2 140 18 195 14 C255 10 320 -6 400 20 L490 22 L490 1000 L-100 1000 Z"></path>' +
      '<path class="ob-hill-edge" d="M-100 28 L-10 26 C70 2 140 18 195 14 C255 10 320 -6 400 20 L490 22"></path>' +
      '</svg></div>';

    s += '<div class="ob-tree" data-el="tree" aria-hidden="true"><svg width="390" height="280" viewBox="0 0 390 280">' +
      '<g transform="translate(195 255) scale(0.6) translate(-195 -515)">';
    T.roots.forEach(function (r) {
      s += '<path class="ob-root" data-el="root" d="' + r[0] + '" stroke-width="' + r[1] + '" pathLength="1"></path>';
    });
    s += '<g class="ob-ripples" data-el="ripples">';
    [0, 1.3, 2.6].forEach(function (d) {
      s += '<ellipse class="ob-ripple" cx="195" cy="514" rx="40" ry="8" style="animation-delay:' + d + 's"></ellipse>';
    });
    s += '</g><ellipse class="ob-seed" data-el="seed" cx="195" cy="510" rx="11" ry="7"></ellipse>';
    s += '<g class="ob-sway">';
    T.segs.forEach(function (g) {
      s += '<path class="ob-seg" data-el="seg" d="' + g[0] + '" stroke-width="' + g[1] + '" pathLength="1"></path>';
    });
    T.blobs.forEach(function (b, k) {
      s += '<circle class="ob-blob ob-blob--' + (k % 3) + '" data-el="blob" cx="' + b[0] + '" cy="' + b[1] + '" r="' + b[2] + '"></circle>';
    });
    T.leaves.forEach(function (l) {
      s += '<g transform="translate(' + l[0] + ' ' + l[1] + ') rotate(' + l[2] + ') scale(' + l[4] + ')">' +
        '<path class="ob-leaf" data-el="leaf" d="M0 0 C6 -8 18 -8 22 0 C18 8 6 8 0 0Z"></path></g>';
    });
    for (i = 0; i < NAME_LEAF_MAX; i++) {
      var ang = -150 + i * 22;
      var rad = 58 + (i % 2) * 10;
      var x = 195 + Math.cos((ang * Math.PI) / 180) * rad;
      var y = 200 + Math.sin((ang * Math.PI) / 180) * rad * 0.8;
      s += '<g transform="translate(' + x.toFixed(1) + ' ' + y.toFixed(1) + ') rotate(' + ang + ')">' +
        '<path class="ob-name-leaf" data-el="nameLeaf" d="M0 0 C5 -6 13 -6 17 0 C13 6 5 6 0 0Z"></path></g>';
    }
    T.flowers.forEach(function (f) {
      s += '<g transform="translate(' + f[0] + ' ' + f[1] + ')"><g class="ob-flower" data-el="flower">' +
        '<circle class="ob-petal" cx="0" cy="-8" r="7"></circle><circle class="ob-petal" cx="7.6" cy="-2.5" r="7"></circle>' +
        '<circle class="ob-petal" cx="4.7" cy="6.5" r="7"></circle><circle class="ob-petal" cx="-4.7" cy="6.5" r="7"></circle>' +
        '<circle class="ob-petal" cx="-7.6" cy="-2.5" r="7"></circle><circle class="ob-pistil" cx="0" cy="0" r="4.5"></circle></g></g>';
    });
    s += '</g></g></svg></div>';

    s += '<div class="ob-fall" aria-hidden="true">';
    T.fall.forEach(function (f) {
      s += '<div class="ob-fall-item" data-el="fall" style="left:' + f[0] + 'px;top:' + f[1] + 'px;animation-delay:' + f[2] +
        'ms;animation-duration:' + f[3] + 'ms"><svg width="18" height="18" viewBox="0 0 24 24"><path d="' + T.LEAF_SHAPE + '"></path></svg></div>';
    });
    s += '</div>';
    return s;
  }

  function create(frame) {
    var holder = document.createElement('div');
    holder.className = 'ob-world';
    holder.innerHTML = html();
    frame.insertBefore(holder, frame.firstChild);

    var q = {};
    holder.querySelectorAll('[data-el]').forEach(function (el) {
      (q[el.dataset.el] = q[el.dataset.el] || []).push(el);
    });
    var lastGust = 0;
    var lastPetals = false;

    /** Roots are driven by the hold button, so they can be redrawn on their own every frame. */
    function roots(st) {
      var step = st.step;
      var rootP = step === 10 ? st.hold : (step > 10 ? 1 : 0);
      q.root.forEach(function (el, i) {
        var p = SA.ease.clamp(rootP * 1.25 - i * 0.06, 0, 1);
        el.style.strokeDashoffset = (1 - p).toFixed(3);
        el.style.opacity = step >= 10 && step <= 11 ? 0.9 : 0;
        el.style.transition = tr('opacity', '--motion-fade-duration', '--motion-ease', 0);
      });
    }

    function update(st) {
      var step = st.step;
      var isEnd = step >= 12;
      var scr = step < 3 ? 0 : step - 2;
      var inB = step < 3;
      var stage = Math.min(step, 4);
      var showSky = step === 6 && st.moment >= 0;
      var wilt = step === 5 ? D.usage.wilt[st.use] : 0;
      var nameLen = (st.name || '').length;
      var counts = {};
      var leafCounts = {};

      // ---- branches ----
      q.seg.forEach(function (el, i) {
        var g = T.segs[i];
        var on = stage >= g[2];
        el.style.strokeDashoffset = on ? 0 : 1;
        el.style.transition = tr('stroke-dashoffset', on ? '--motion-grow-duration' : '--motion-grow-exit-duration', '--motion-grow-easing', on ? g[3] : 0);
      });

      roots(st);

      // ---- foliage ----
      q.blob.forEach(function (el, i) {
        var b = T.blobs[i];
        var on = stage >= b[3];
        var k = counts[b[3]] = (counts[b[3]] || 0) + 1;
        var wilted = ((i * 7) % 19) < wilt;
        el.style.fill = wilted ? v('--gold') : '';
        el.style.fillOpacity = wilted ? 0.45 : '';
        el.style.transform = 'scale(' + (on ? (wilted ? 0.88 : 1) : 0) + ')';
        el.style.transition = tr('fill', '--motion-fade-wilt-duration', '--motion-ease', 0) + ', ' +
          tr('fill-opacity', '--motion-fade-wilt-duration', '--motion-ease', 0) + ', ' +
          tr('transform', on ? '--motion-bloom-duration' : '--motion-bloom-exit-duration', on ? '--motion-bloom-easing' : '--motion-ease-in', on && step <= 4 ? 550 + k * 90 : 0);
      });

      q.leaf.forEach(function (el, i) {
        var l = T.leaves[i];
        var on = stage >= l[3];
        var k = leafCounts[l[3]] = (leafCounts[l[3]] || 0) + 1;
        el.style.transform = 'scale(' + (on ? 1 : 0) + ')';
        el.style.transition = tr('transform', on ? '--motion-bloom-leaf-duration' : '--motion-bloom-exit-duration', on ? '--motion-bloom-easing' : '--motion-ease-in', on && step <= 4 ? 700 + k * 90 : 0);
      });

      q.nameLeaf.forEach(function (el, i) {
        var on = step >= 3 && !isEnd && i < nameLen;
        el.style.transform = 'scale(' + (on ? 1.2 : 0) + ')';
        el.style.transition = tr('transform', on ? '--motion-bloom-name-duration' : '--motion-bloom-exit-duration', on ? '--motion-bloom-easing' : '--motion-ease-in', 0);
      });

      q.flower.forEach(function (el, i) {
        var on = (step >= 4 && st.picks.indexOf(i) >= 0) || (st.committed && step >= 10);
        el.style.transform = 'scale(' + (on ? 1.3 : 0) + ') rotate(' + (on ? 0 : -90) + 'deg)';
        el.style.transition = tr('transform', on ? '--motion-bloom-flower-duration' : '--motion-bloom-exit-duration', on ? '--motion-bloom-easing' : '--motion-ease-in', on ? i * 90 : 0);
      });

      q.seed[0].style.transform = 'scale(' + (step === 0 ? 1 : 0.6) + ')';
      q.seed[0].style.transition = tr('transform', '--motion-bloom-seed-duration', '--motion-bloom-easing', 0);
      q.ripples[0].style.opacity = step === 0 ? 1 : 0;
      q.ripples[0].style.transition = tr('opacity', '--motion-fade-med-duration', '--motion-ease', 0);

      // ---- tree and hill placement per step ----
      var tY = 0;
      var tS = 1;
      var tO = 0.32;
      if (inB) { tY = D.GROUND_B - 819; tS = 1.55; tO = 0.75; }
      if (step === 7) tO = 0.12;
      if (step === 8) tO = 0.1;
      if (step === 9) tO = 0.2;
      if (step === 10) { tY = -210; tS = 1.15; tO = 0.5; }
      if (step === 11) tO = 0.25;
      if (isEnd) tO = 0;

      var tree = q.tree[0];
      tree.style.transform = 'translateX(' + (-Math.min(scr, 1) * W * 0.1) + 'px) translateY(' + tY + 'px) scale(' + tS + ')';
      tree.style.opacity = tO;
      tree.style.transition = tr('transform', '--motion-settle-duration', '--motion-settle-easing', 0) + ', ' + tr('opacity', '--motion-fade-med-duration', '--motion-ease', 0);

      var hillY = inB ? D.GROUND_B - 14 : (step === 10 ? 819 + tY - 14 : 805);
      var hill = q.hill[0];
      hill.style.transform = 'translateY(' + hillY + 'px)';
      hill.style.opacity = isEnd ? 0 : 1;
      hill.style.transition = tr('transform', '--motion-settle-duration', '--motion-settle-easing', 0) + ', ' + tr('opacity', '--motion-fade-med-duration', '--motion-ease', 0);

      // ---- light from the chosen moment of day ----
      q.sky.forEach(function (el, i) { el.style.opacity = showSky && st.moment === i ? 1 : 0; });
      q.fireflies[0].style.opacity = showSky && (st.moment === 2 || st.moment === 3) ? 1 : 0;

      // ---- falling leaves / petals ----
      if (st.gust !== lastGust || st.petals !== lastPetals) {
        q.fall.forEach(function (el, i) {
          var path = el.querySelector('path');
          var petal = st.petals;
          path.setAttribute('d', petal ? T.PETAL_SHAPE : T.LEAF_SHAPE);
          path.setAttribute('class', petal ? (i % 2 === 0 ? 'ob-fall-gold' : 'ob-fall-mint') : 'ob-fall-leaf');
          if (st.gust !== lastGust) el.className = 'ob-fall-item ' + (st.gust === 0 ? '' : (st.gust % 2 ? 'ob-fall-a' : 'ob-fall-b'));
        });
        lastGust = st.gust;
        lastPetals = st.petals;
      }
    }

    return { update: update, roots: roots };
  }

  SA.onbTree = { create: create };
})(window.SA = window.SA || {});
