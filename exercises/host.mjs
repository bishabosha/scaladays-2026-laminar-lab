// The exercise page: mounts out/<ex>/main.js (from `sake ex <ex>`) and reloads after each rebuild.
// Every exercise is a notebook.sc plugin bundle, mounted with the same contract the slides use:
//   mount(container, payload, ctx) -> { update(payload), unmount() }

const ex = new URLSearchParams(location.search).get('ex');
const status = document.getElementById('build-status');
const app = document.getElementById('app');
const bundle = ex && /^[a-z0-9-]+$/.test(ex) ? `out/${ex}/main.js` : null;

function show(text, kind) {
  status.textContent = text;
  status.dataset.kind = kind;
}

async function lastModified() {
  try {
    const response = await fetch(bundle, { method: 'HEAD', cache: 'no-store' });
    return response.ok ? response.headers.get('Last-Modified') : null;
  } catch {
    return null; // the server has stopped
  }
}

async function mount() {
  const module = await import(`./${bundle}?v=${Date.now()}`);
  const ctx = {
    apiVersion: 1, kind: `${ex}/app`, pluginId: ex, name: 'app', mode: 'widget', theme: { dark: false },
    widgets: { name: 'app', value: () => '{}', set: json => console.log('widget value', json), onChange: () => () => {} }
  };
  module.mount(app, '{}', ctx);
}

async function start() {
  if (!bundle) {
    show('no exercise selected: run `sake ex 01-cauldron`', 'error');
    return;
  }
  document.getElementById('exercise-name').textContent = ex;
  document.title = `${ex} · The Apothecary`;
  let seen = await lastModified();
  while (!seen) {
    await new Promise(r => setTimeout(r, 1000));
    seen = await lastModified();
  }
  try {
    await mount();
    show(`built ${new Date(seen).toLocaleTimeString()}`, 'ok');
  } catch (error) {
    show(`failed to start: ${error}`, 'error');
    console.error(error);
  }
  // scala-cli --watch rewrites main.js after every successful compile: reload when it changes.
  for (;;) {
    await new Promise(r => setTimeout(r, 1000));
    const now = await lastModified();
    if (now && now !== seen) location.reload();
  }
}

start();
