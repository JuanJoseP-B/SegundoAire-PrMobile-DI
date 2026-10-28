/*
 * Screen registry and navigator.
 *
 * To add screens later (login, permissions, dashboard...):
 *   1. SA.registry.defineScene('name', { layer, mount(host, api), enter(ctx), leave(ctx), reset() })
 *   2. SA.registry.defineScreens('name', [{ id, label, hint }, ...])
 *   3. Load the script in index.html before app.js. The nav controls update themselves.
 *
 * Screens keep the order in which they were defined; go(id) moves between them.
 */
(function (SA) {
  'use strict';

  var scenes = {};
  var order = [];
  var screens = [];
  var current = null;
  var listeners = [];
  var screenEl = null;

  function defineScene(name, def) {
    scenes[name] = def;
    order.push(name);
  }

  function defineScreens(scene, list) {
    list.forEach(function (s) {
      screens.push({ id: s.id, label: s.label, hint: s.hint || '', scene: scene });
    });
  }

  function indexOf(id) {
    for (var i = 0; i < screens.length; i++) if (screens[i].id === id) return i;
    return -1;
  }

  /** What a scene may ask of the shell. */
  var api = {
    go: function (id) { go(id); },
    next: function () { go(relative(1)); },
    prev: function () { go(relative(-1)); },
    restart: function () { restart(); },
    setTone: function (tone) { if (screenEl) screenEl.dataset.tone = tone; }
  };

  function relative(delta) {
    var i = current ? indexOf(current.id) + delta : 0;
    return screens[Math.min(screens.length - 1, Math.max(0, i))].id;
  }

  function mountAll(screen, container) {
    screenEl = screen;
    order
      .slice()
      .sort(function (a, b) { return (scenes[a].layer || 0) - (scenes[b].layer || 0); })
      .forEach(function (name) {
        var el = document.createElement('div');
        el.className = 'scene scene--' + name;
        el.dataset.scene = name;
        el.style.zIndex = String(scenes[name].layer || 0);
        container.appendChild(el);
        scenes[name].host = el;
        scenes[name].mount(el, api);
      });
  }

  function go(id) {
    var i = indexOf(id);
    if (i < 0 || (current && current.id === id)) return;

    var prev = current;
    var target = screens[i];
    var direction = prev && indexOf(prev.id) > i ? -1 : 1;
    var sceneChange = !prev || prev.scene !== target.scene;
    current = target;

    if (prev && sceneChange && scenes[prev.scene].leave) {
      scenes[prev.scene].leave({ nextId: id, direction: direction });
    }
    scenes[target.scene].enter({
      id: id,
      prevId: prev ? prev.id : null,
      direction: direction,
      sceneChange: sceneChange
    });
    listeners.forEach(function (cb) { cb(target, i); });
  }

  function restart() {
    current = null;
    order.forEach(function (name) { if (scenes[name].reset) scenes[name].reset(); });
    go(screens[0].id);
  }

  SA.registry = {
    defineScene: defineScene,
    defineScreens: defineScreens,
    mountAll: mountAll,
    screens: function () { return screens.slice(); },
    current: function () { return current; },
    indexOf: indexOf,
    go: go,
    next: api.next,
    prev: api.prev,
    restart: restart,
    onChange: function (cb) { listeners.push(cb); }
  };
})(window.SA = window.SA || {});
