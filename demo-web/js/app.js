/* Shell: boots the scenes, fits the phone to the viewport and wires the demo controls. */
(function (SA) {
  'use strict';

  var R = SA.registry;
  var root = document.documentElement;
  var seen = {};
  var els = {};

  function byId(id) { return document.getElementById(id); }

  // ---------- phone scaling ----------

  function fit() {
    var phone = els.phone;
    var w = phone.offsetWidth;
    var h = phone.offsetHeight;
    var gap = 8;
    var scale = Math.min(1, (els.stage.clientWidth - gap) / w, (els.stage.clientHeight - gap) / h);
    root.style.setProperty('--phone-scale', Math.max(0.3, scale).toFixed(4));
  }

  // ---------- theme ----------

  var dark = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

  function isDark() {
    return root.dataset.theme ? root.dataset.theme === 'dark' : !!(dark && dark.matches);
  }

  function syncThemeButton() {
    els.theme.setAttribute('aria-label', isDark() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
  }

  function toggleTheme() {
    root.dataset.theme = isDark() ? 'light' : 'dark';
    syncThemeButton();
  }

  // ---------- demo navigation ----------

  function buildDots() {
    els.dots.innerHTML = '';
    R.screens().forEach(function (s) {
      var li = document.createElement('li');
      var b = document.createElement('button');
      b.type = 'button';
      b.className = 'demo-dot';
      b.dataset.id = s.id;
      b.setAttribute('aria-label', s.label);
      b.title = s.label;
      b.addEventListener('click', function () { R.go(s.id); });
      li.appendChild(b);
      els.dots.appendChild(li);
    });
  }

  function onChange(screen, index) {
    var all = R.screens();
    seen[screen.id] = true;
    els.dots.querySelectorAll('.demo-dot').forEach(function (b) {
      var active = b.dataset.id === screen.id;
      if (active) b.setAttribute('aria-current', 'step'); else b.removeAttribute('aria-current');
      b.classList.toggle('is-seen', !!seen[b.dataset.id]);
    });
    els.count.textContent = (index + 1) + ' / ' + all.length;
    els.prev.disabled = index === 0;
    els.next.disabled = index === all.length - 1;
    els.caption.innerHTML = '<strong></strong><span></span>';
    els.caption.firstChild.textContent = screen.label;
    els.caption.lastChild.textContent = screen.hint ? ' · ' + screen.hint : '';
  }

  function onKey(e) {
    if (e.defaultPrevented || e.altKey || e.ctrlKey || e.metaKey) return;
    var t = e.target;
    if (t && t.closest && t.closest('input, textarea, select, [role="radiogroup"]')) return;
    if (e.key === 'ArrowRight') { R.next(); e.preventDefault(); }
    if (e.key === 'ArrowLeft') { R.prev(); e.preventDefault(); }
  }

  function restart() {
    seen = {};
    R.restart();
  }

  function boot() {
    ['stage', 'phone', 'screen', 'scenes', 'dots', 'count', 'caption', 'prevBtn', 'nextBtn', 'restartBtn', 'themeToggle']
      .forEach(function (id) { els[id.replace('Btn', '').replace('Toggle', '')] = byId(id); });

    R.mountAll(els.screen, els.scenes);
    buildDots();
    R.onChange(onChange);

    els.prev.addEventListener('click', R.prev);
    els.next.addEventListener('click', R.next);
    els.restart.addEventListener('click', restart);
    els.theme.addEventListener('click', toggleTheme);
    document.addEventListener('keydown', onKey);
    if (dark && dark.addEventListener) dark.addEventListener('change', syncThemeButton);

    syncThemeButton();
    fit();
    window.addEventListener('resize', fit);
    if (window.ResizeObserver) new ResizeObserver(fit).observe(els.stage);

    R.go(R.screens()[0].id);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();
})(window.SA = window.SA || {});
