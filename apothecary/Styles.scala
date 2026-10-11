package apothecary

import com.raquo.laminar.api.L.*

/** The shop's look, as an element: `Shop(h2("…"), …)` wherever Laminar renders. */
object Shop:
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "apothecary-app", styleTag(Styles.css), content)

/** The shop's look, injected by `Main` so an exercise looks the same on its page, in a slide
 *  and in a notebook. Everything is scoped to `.apothecary-app` and sized in em, from
 *  `--apothecary-font-size` (16px unless a host such as the slides sets it). You can use these
 *  classes freely.
 */
object Styles:
  val css: String = """
.apothecary-app {
  --ink:#2b1d3a; --muted:#6c6278; --potion:#6b3fa0; --elixir:#2f8f6f; --brass:#c08a2e; --rude:#b3261e;
  --paper:#fbf8f2; --rule:#ddd3c4;
  font:var(--apothecary-font-size, 16px)/1.45 system-ui, -apple-system, "Segoe UI", sans-serif; color:var(--ink); text-align:left;
  display:flex; flex-direction:column; gap:1em;
}
.apothecary-app * { box-sizing:border-box; }
.apothecary-app h2 { margin:0; font-size:1.3em; }
.apothecary-app button {
  font:inherit; padding:.35em .8em; border-radius:.4em; border:1px solid var(--potion);
  background:var(--potion); color:#fff; cursor:pointer;
}
.apothecary-app button.quiet { background:transparent; color:var(--potion); }
.apothecary-app button:disabled { opacity:.45; cursor:not-allowed; }
.apothecary-app input[type=text], .apothecary-app input[type=number], .apothecary-app input[type=search] {
  font:inherit; padding:.35em .5em; border:1px solid var(--rule); border-radius:.35em; background:#fff; min-width:0;
}
.apothecary-app .card { background:var(--paper); border:1px solid var(--rule); border-radius:.6em; padding:1em; }
.apothecary-app .row { display:flex; gap:.75em; align-items:center; flex-wrap:wrap; }

/* 01 · cauldron */
.apothecary-app .slider-row { display:grid; grid-template-columns:8em 1fr 3em; gap:.5em; align-items:center; }
.apothecary-app .cauldron {
  width:9em; height:6em; border-radius:0 0 4.5em 4.5em; border:6px solid #2b2b2b; border-top-width:10px;
  transition:background-color .3s; box-shadow:inset 0 -12px 18px #0004;
}
.apothecary-app .cauldron-warning { color:var(--rude); font-weight:600; }

/* 02 · forms */
.apothecary-app form { display:flex; flex-direction:column; gap:.8em; }
.apothecary-app .field { display:flex; flex-direction:column; gap:.25em; }
.apothecary-app .field > label { font-weight:600; }
.apothecary-app .field.invalid input { border-color:var(--rude); }
.apothecary-app .field-error { color:var(--rude); font-size:.9em; }
.apothecary-app .checks { display:flex; flex-wrap:wrap; gap:.3em 1em; }
.apothecary-app .problems { color:var(--rude); margin:0; padding-left:1.2em; }
.apothecary-app .success { color:var(--elixir); font-weight:600; }

/* 03 · async */
.apothecary-app .thinking { color:var(--muted); font-style:italic; }
.apothecary-app .rude { color:var(--rude); }
.apothecary-app ul.results, .apothecary-app ul.shelves { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:.3em; }
.apothecary-app .results li { display:flex; gap:.5em; align-items:baseline; }
.apothecary-app .rarity { font-size:.75em; padding:0 .4em; border-radius:.3em; background:var(--rule); }
.apothecary-app .rarity.Legendary { background:var(--brass); color:#fff; }
.apothecary-app .note { color:var(--muted); font-size:.9em; }
.apothecary-app .shelf { display:grid; grid-template-columns:1fr auto auto auto auto; gap:.5em; align-items:center; }
.apothecary-app .shelf.saving { opacity:.55; }
.apothecary-app .quantity { min-width:2.5em; text-align:center; font-variant-numeric:tabular-nums; }

/* 04 · brew */
.apothecary-app .shake { animation:apothecary-shake .5s; }
@keyframes apothecary-shake {
  0%, 100% { transform:none; } 20% { transform:translate(-8px, 2px); } 40% { transform:translate(8px, -2px); }
  60% { transform:translate(-6px, 1px); } 80% { transform:translate(6px, -1px); }
}

/* the oracle's control panel */
.apothecary-app .oracle-panel {
  display:flex; flex-wrap:wrap; gap:.4em 1.2em; align-items:center; font-size:.85em; color:var(--muted);
  border:1px dashed var(--rule); border-radius:.5em; padding:.4em .7em;
}
.apothecary-app .oracle-panel label { display:flex; gap:.4em; align-items:center; }
.apothecary-app .oracle-title { font-weight:600; color:var(--ink); }
.apothecary-app .oracle-busy.active { color:var(--potion); }
"""
