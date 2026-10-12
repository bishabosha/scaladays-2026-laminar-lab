// Mounts live Laminar demos into slides.
//
// `{{demo plugin/name}}` renders <div class="laminar-demo" data-kind="plugin/name">. Each
// plugin is a widget bundle built from the repo's widgets/<plugin>/ into widgets/<plugin>/main.js
// next to this file, mounted with the same v1 contract the notebook uses:
//
//   mount(container, payload, ctx) -> { update(payload), unmount() }
//
// The deck acts as the notebook: `ctx.widgets.set(json)` shows the value in the demo's
// <output class="demo-value"> instead of sending it to a kernel. An optional data-value attribute seeds the widget.

const mounted = new Map(); // container -> handle

function deckContext(container, pluginId, name) {
  let current = container.dataset.value ?? '{}';
  const echo = container.parentElement.querySelector(':scope > .demo-value');
  return {
    apiVersion: 1,
    kind: `${pluginId}/${name}`,
    pluginId,
    name,
    mode: 'widget',
    theme: { dark: false },
    widgets: {
      name,
      value: () => current,
      set(json) {
        current = json;
        if (echo) echo.textContent = `→ ${json}`;
      },
      // Nothing else writes the value in a deck; the plugin's own writes are not echoed.
      onChange: () => () => {}
    }
  };
}

async function mountDemo(container) {
  const [pluginId, name] = container.dataset.kind.split('/');
  container.dataset.state = 'loading';
  try {
    const module = await import(new URL(`widgets/${pluginId}/main.js`, import.meta.url));
    container.replaceChildren();
    const ctx = deckContext(container, pluginId, name);
    mounted.set(container, module.mount(container, container.dataset.payload ?? '{}', ctx));
    container.dataset.state = 'mounted';
  } catch (error) {
    container.dataset.state = 'missing';
    container.title = String(error);
    console.warn(`demo "${container.dataset.kind}" not available; build it with \`sake widget ${pluginId}\``, error);
  }
}

// A demo loads the first time its slide is on screen (Reveal hides the others), so opening the
// deck loads no bundle until a slide needs one. Once mounted, a demo stays mounted.
const onScreen = new IntersectionObserver(entries => {
  for (const entry of entries) {
    if (!entry.isIntersecting) continue;
    onScreen.unobserve(entry.target);
    if (!entry.target.dataset.state) mountDemo(entry.target);
  }
});

function scan() {
  for (const [container, handle] of mounted) {
    if (!container.isConnected) {
      mounted.delete(container);
      try { handle?.unmount?.(); } catch (error) { console.warn(error); }
    }
  }
  // Thumbnail copies in the authoring sidebar run no scripts; only mount real slides.
  for (const container of document.querySelectorAll('.reveal .slides .laminar-demo:not([data-state])')) {
    onScreen.observe(container);
  }
}

scan();
document.addEventListener('tiger-live:updated', scan);
