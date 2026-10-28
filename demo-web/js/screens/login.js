/*
 * Account scene: the onboarding login ("Guarda tu árbol") on the dark curtain, its email
 * branch (sign in / create account / forgot password) and the "Todo listo" closing curtain.
 * Mock-up only: nothing is validated or sent. Buttons just move the tour forward.
 *
 * Linear screens: login -> done. The email branch is reached from the envelope button and
 * lives inside the "login" screen, so the external Next/Previous order is not affected.
 */
(function (SA) {
  'use strict';

  var FALLBACK_NAME = 'Ana';
  var BUSY_MS = 1500;       // OnboardingRules.LoginBusyMillis: the button stays busy at least 1.5 s
  var REST_DELAY_MS = 850;  // LoginCurtainRestMillis: the curtain retracts after covering

  var api = null;
  var root = null;
  var host = null;
  var q = {};
  var st = null;
  var timers = { rest: 0, busy: 0 };

  function fresh() {
    return { screen: 'login', view: 'login', mode: 'login', curtain: 'up', busy: false };
  }

  function esc(t) {
    return String(t).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }

  function icon(path, size) {
    return '<svg width="' + size + '" height="' + size + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
      'stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="' + path + '"></path></svg>';
  }

  // ---------- icons ----------

  var BACK = 'M15 18l-6-6 6-6';
  var EYE = 'M2.5 12s3.5-6.5 9.5-6.5S21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12zM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z';
  var EYE_OFF = 'M3 3l18 18M10.6 5.7A9.6 9.6 0 0 1 12 5.5c6 0 9.5 6.5 9.5 6.5a16 16 0 0 1-3.1 3.8M6.5 7.6A16 16 0 0 0 2.5 12S6 18.5 12 18.5a9.4 9.4 0 0 0 3.4-.6M9.9 9.9a3 3 0 0 0 4.2 4.2';
  var ENVELOPE = 'M5.5 5.5h13A2.5 2.5 0 0 1 21 8v8a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 16V8a2.5 2.5 0 0 1 2.5-2.5zM4 7.5l8 5.5 8-5.5';

  // GoogleLogo (4 colors, 48 x 48) and AppleLogo (single color, 24 x 24), as in ProviderLogos.kt
  var GOOGLE_LOGO = '<svg width="20" height="20" viewBox="0 0 48 48" aria-hidden="true">' +
    '<path class="lg-red" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path>' +
    '<path class="lg-blue" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path>' +
    '<path class="lg-yellow" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path>' +
    '<path class="lg-green" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path></svg>';
  var APPLE_LOGO = '<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">' +
    '<path d="M12.152 6.896c-.948 0-2.415-1.078-3.96-1.04-2.04.027-3.91 1.183-4.961 3.014-2.117 3.675-.546 9.103 1.519 12.09 1.013 1.454 2.208 3.09 3.792 3.039 1.52-.065 2.09-.987 3.935-.987 1.831 0 2.35.987 3.96.948 1.637-.026 2.676-1.48 3.676-2.948 1.156-1.688 1.636-3.325 1.662-3.415-.039-.013-3.182-1.221-3.22-4.857-.026-3.04 2.48-4.494 2.597-4.559-1.429-2.09-3.623-2.324-4.39-2.376-2-.156-3.675 1.09-4.61 1.09z"></path>' +
    '<path d="M15.53 3.83c.843-1.012 1.4-2.427 1.245-3.83-1.207.052-2.662.805-3.532 1.818-.78.896-1.454 2.338-1.273 3.714 1.338.104 2.715-.688 3.559-1.701"></path></svg>';

  // ---------- templates ----------

  function field(id, label, opts) {
    opts = opts || {};
    var eye = opts.password
      ? '<button type="button" class="sa-eye" data-eye="' + id + '" aria-label="Mostrar contraseña" aria-pressed="false">' + icon(EYE, 20) + '</button>'
      : '';
    return '<div class="sa-field"' + (opts.el ? ' data-el="' + opts.el + '"' : '') + '>' +
      '<label class="ob-label" for="' + id + '">' + esc(label) + '</label>' +
      '<div class="sa-input-wrap' + (opts.password ? ' has-eye' : '') + '">' +
      '<input id="' + id + '" class="ob-input" type="' + (opts.password ? 'password' : 'text') + '"' +
      (opts.email ? ' inputmode="email"' : '') +
      ' placeholder="' + esc(opts.placeholder || '') + '" autocomplete="off" autocapitalize="off" spellcheck="false">' + eye + '</div>' +
      (opts.support ? '<p class="sa-support" data-el="' + opts.support + '"></p>' : '') + '</div>';
  }

  function scaffoldHead(titleEl, subEl) {
    return '<h2 class="sa-title" data-el="' + titleEl + '"></h2><p class="sa-sub" data-el="' + subEl + '"></p>';
  }

  function loginView() {
    return '<section class="ac-view ac-view--login" data-v="login">' +
      '<div class="ac-head"><h2 class="ac-title">Guarda <em>tu árbol</em></h2>' +
      '<p class="ob-sub ac-sub">Crea tu cuenta para no perder tu progreso.</p></div>' +
      '<div class="ac-fields">' + field('ac-email', 'Correo', { placeholder: 'tu@correo.com', email: true }) +
      field('ac-pass', 'Contraseña', { placeholder: 'Mínimo 8 caracteres', password: true }) + '</div>' +
      '<div class="ac-create-wrap"><button type="button" class="ac-create" data-el="create" data-go aria-label="Crear cuenta">' +
      '<span class="ac-create-label">Crear cuenta</span>' +
      '<svg class="ac-seed" width="22" height="22" viewBox="0 0 24 24" aria-hidden="true">' +
      '<path d="M12 3 C15 6 15 9 12 12 C9 9 9 6 12 3Z"></path><path class="ac-seed-low" d="M12 21 C9 18 9 15 12 12 C15 15 15 18 12 21Z"></path></svg></button></div>' +
      '<div class="ac-providers">' +
      '<button type="button" class="ac-prov" data-go aria-label="Continuar con Google">' + GOOGLE_LOGO + '</button>' +
      '<button type="button" class="ac-prov" data-go aria-label="Continuar con Apple">' + APPLE_LOGO + '</button>' +
      '<button type="button" class="ac-prov ac-prov--mail" data-open="email" aria-label="Continuar con correo">' + icon(ENVELOPE, 20) + '</button></div>' +
      '<div class="ac-guest"><button type="button" class="sa-text-btn" data-go data-el="guest">Continuar sin cuenta</button></div>' +
      '</section>';
  }

  function emailView() {
    return '<section class="ac-view ac-view--email sa-view" data-v="email">' + scaffoldHead('emTitle', 'emSub') +
      '<div class="sa-form">' +
      field('em-email', 'Correo', { placeholder: 'tu@correo.com', email: true }) +
      field('em-pass', 'Contraseña', { placeholder: '', password: true, support: 'emSupport' }) +
      field('em-conf', 'Confirma tu contraseña', { password: true, el: 'emConf' }) +
      '<div class="sa-links"><button type="button" class="sa-text-btn" data-open="forgot" data-el="emForgot">¿Olvidaste tu contraseña?</button>' +
      '<button type="button" class="sa-text-btn" data-el="emSwitch"></button></div></div>' +
      '<button type="button" class="ob-btn" data-go data-el="emSubmit"></button></section>';
  }

  function forgotView() {
    return '<section class="ac-view ac-view--forgot sa-view" data-v="forgot">' +
      '<h2 class="sa-title">Recupera tu contraseña</h2>' +
      '<p class="sa-sub">Escribe tu correo y te enviamos un enlace para crear una nueva.</p>' +
      '<div class="sa-form">' + field('fg-email', 'Correo', { placeholder: 'tu@correo.com', email: true }) + '</div>' +
      '<button type="button" class="ob-btn" data-open="sent">Enviar enlace</button></section>';
  }

  function sentView() {
    return '<section class="ac-view ac-view--sent sa-view" data-v="sent">' +
      '<h2 class="sa-title">Te enviamos un enlace</h2>' +
      '<p class="sa-sub" data-el="sentBody"></p>' +
      '<button type="button" class="ob-btn" data-open="email">Volver a entrar</button></section>';
  }

  function build() {
    return '<div class="ac sa-surface" data-el="ac">' +
      '<div class="ac-curtain" aria-hidden="true"><div class="ac-curtain-fill"></div>' +
      '<svg class="ac-curve" viewBox="0 0 390 70" preserveAspectRatio="none"><path d="M0 0 H390 V10 C300 80 140 40 0 64 Z"></path></svg></div>' +
      '<div class="ob-frame ac-frame">' +
      '<div class="ac-brand"><div class="ob-wordmark">SegundoAire</div><div class="ob-tagline">Tu árbol ya echó raíces.</div></div>' +
      '<svg class="ob-curtain-tree ac-tree" aria-hidden="true" width="190" height="190" viewBox="0 0 190 190">' +
      '<path class="ct-trunk" d="M95 190 C93 160 97 130 95 100"></path>' +
      '<circle class="ct-a" cx="95" cy="70" r="44"></circle><circle class="ct-b" cx="58" cy="92" r="32"></circle>' +
      '<circle class="ct-c" cx="132" cy="92" r="32"></circle><circle class="ct-b" cx="76" cy="52" r="28"></circle>' +
      '<circle class="ct-c" cx="118" cy="50" r="28"></circle><circle class="ct-dot" cx="70" cy="66" r="6"></circle><circle class="ct-dot" cx="124" cy="78" r="6"></circle></svg>' +
      loginView() + emailView() + forgotView() + sentView() +
      '<section class="ac-done" data-el="done">' +
      '<h2 class="ob-done-title">Todo listo, <em data-el="doneName"></em>.</h2>' +
      '<p class="ob-done-body">Tu árbol te espera en el panel principal.<br>Crece con cada minuto que recuperas.</p>' +
      '<button type="button" class="ob-ghost" data-next>Continuar</button></section>' +
      '<button type="button" class="sa-back" data-el="back" aria-label="Volver">' + icon(BACK, 24) + '</button>' +
      '</div></div>';
  }

  // ---------- rendering ----------

  function renderEmail() {
    var reg = st.mode === 'register';
    q.emTitle.textContent = reg ? 'Crea tu cuenta' : 'Entra con tu correo';
    q.emSub.textContent = reg ? 'Solo necesitas un correo y una contraseña.' : 'Retoma donde lo dejaste.';
    q.emSupport.textContent = reg ? '8 caracteres o más, con una letra y un número.' : '';
    q.emSupport.hidden = !reg;
    q.emConf.hidden = !reg;
    q.emForgot.hidden = reg;
    q.emSwitch.textContent = reg ? 'Ya tengo cuenta' : 'Crear una cuenta';
    q.emSubmit.textContent = reg ? 'Crear cuenta' : 'Entrar';
  }

  function render() {
    var onDone = st.screen === 'done';
    var view = onDone ? 'none' : st.view;
    // The dark curtain frames the login and the closing; the email branch sits on the light surface
    var curtain = onDone ? 'cover' : (st.view === 'login' ? st.curtain : 'up');

    q.ac.dataset.view = view;
    q.ac.dataset.state = curtain;
    q.ac.dataset.busy = st.busy ? 'true' : 'false';
    root.querySelectorAll('.ac-view').forEach(function (el) {
      el.inert = el.dataset.v !== view;
    });
    q.done.inert = !onDone;

    var backOn = !onDone && (st.view === 'email' || st.view === 'forgot');
    q.back.style.opacity = backOn ? 1 : 0;
    q.back.style.pointerEvents = backOn ? 'auto' : 'none';
    q.back.inert = !backOn;

    q.create.disabled = st.busy;
    q.create.setAttribute('aria-busy', st.busy ? 'true' : 'false');
    q.create.setAttribute('aria-label', st.busy ? 'Creando tu cuenta' : 'Crear cuenta');
    root.querySelectorAll('.ac-prov, .sa-text-btn[data-go]').forEach(function (el) { el.disabled = st.busy; });

    q.doneName.textContent = (SA.session && SA.session.name) || FALLBACK_NAME;
    renderEmail();
    if (!host.hasAttribute('data-off') && host.style.visibility === 'visible') api.setTone(curtain === 'up' ? 'dark' : 'light');
  }

  // ---------- behavior ----------

  function clearTimers() {
    clearTimeout(timers.rest);
    clearTimeout(timers.busy);
  }

  /** Drops the curtain over everything and retracts it to the top band, like the app. */
  function dropCurtain() {
    st.curtain = 'up';
    render();
    void q.ac.offsetWidth;
    st.curtain = 'cover';
    render();
    timers.rest = setTimeout(function () { st.curtain = 'rest'; render(); }, REST_DELAY_MS);
  }

  /** Any account button: the button shrinks to a spinning seed, then the tour moves on. */
  function submit() {
    if (st.busy) return;
    st.busy = true;
    render();
    timers.busy = setTimeout(function () {
      st.busy = false;
      api.next();
    }, BUSY_MS);
  }

  function open(view) {
    st.view = view;
    if (view === 'email') st.mode = st.mode || 'login';
    if (view === 'sent') {
      var typed = root.querySelector('#fg-email').value.trim();
      q.sentBody.textContent = 'Revisa ' + (typed || 'tu@correo.com') + ' y sigue el enlace para crear una contraseña nueva.';
    }
    if (view === 'login') st.curtain = 'rest';
    render();
  }

  function back() {
    if (st.view === 'forgot') open('email');
    else if (st.view === 'email') open('login');
  }

  function toggleEye(btn) {
    var input = root.querySelector('#' + btn.dataset.eye);
    var show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    btn.setAttribute('aria-pressed', show ? 'true' : 'false');
    btn.setAttribute('aria-label', show ? 'Ocultar contraseña' : 'Mostrar contraseña');
    btn.innerHTML = icon(show ? EYE_OFF : EYE, 20);
  }

  function bind() {
    root.addEventListener('click', function (e) {
      var t = e.target.closest('button');
      if (!t || !root.contains(t) || t.disabled) return;
      if (t.hasAttribute('data-next')) { api.next(); return; }
      if (t === q.back) { back(); return; }
      if (t.dataset.eye) { toggleEye(t); return; }
      if (t === q.emSwitch) { st.mode = st.mode === 'login' ? 'register' : 'login'; render(); return; }
      if (t.dataset.open) { open(t.dataset.open); return; }
      if (t === q.emSubmit) { api.next(); return; }
      if (t.hasAttribute('data-go')) submit();
    });

    root.addEventListener('keydown', function (e) {
      if (e.key !== 'Enter' || !e.target.matches('input')) return;
      if (e.target.id === 'ac-pass' || e.target.id === 'ac-email') submit();
      else if (e.target.id === 'fg-email') open('sent');
      else api.next();
    });
  }

  // A child with an explicit visibility:visible beats a hidden parent, so the host also carries data-off.
  function setActive(on) {
    host.style.visibility = on ? 'visible' : 'hidden';
    host.inert = !on;
    if (on) host.removeAttribute('data-off'); else host.setAttribute('data-off', '');
  }

  // ---------- scene ----------

  var scene = {
    layer: 20,

    mount: function (el, shell) {
      api = shell;
      host = el;
      host.innerHTML = build();
      setActive(false);
      root = host;
      host.querySelectorAll('[data-el]').forEach(function (n) { q[n.dataset.el] = n; });
      st = fresh();
      bind();
      render();
    },

    enter: function (ctx) {
      clearTimers();
      setActive(true);
      var wasDone = ctx.prevId === 'done';
      st.screen = ctx.id;
      st.busy = false;
      if (ctx.id === 'login') {
        st.view = 'login';
        st.mode = 'login';
        if (wasDone) { st.curtain = 'rest'; render(); } else dropCurtain();
      } else {
        st.curtain = 'cover';
        render();
      }
    },

    leave: function () {
      clearTimers();
      setActive(false);
      st.curtain = 'up';
      st.busy = false;
    },

    reset: function () {
      clearTimers();
      st = fresh();
      root.querySelectorAll('input').forEach(function (i) { i.value = ''; });
      setActive(false);
      render();
    }
  };

  SA.registry.defineScene('account', scene);
  SA.registry.defineScreens('account', [
    { id: 'login', label: 'Cuenta · Guarda tu árbol', hint: 'Correo, tres accesos y continuar sin cuenta. El sobre abre el acceso con correo y su recuperación.' },
    { id: 'done', label: 'Cierre del onboarding · Todo listo', hint: 'El telón vuelve a cubrir la pantalla y saluda por tu nombre.' }
  ]);
})(window.SA = window.SA || {});
