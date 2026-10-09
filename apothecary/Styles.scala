package apothecary

import com.raquo.laminar.api.L.*

/** The shop's look, as an element: `Shop(h2("…"), …)` wherever Laminar renders. */
object Shop:
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(cls := "apothecary-app", styleTag(Styles.css), content)

/** The shop's look, injected by `Main` so an exercise looks the same on its page, in a slide
 *  and in a notebook. Everything is scoped to `.apothecary-app`. You can use these classes freely.
 */
object Styles:
  val css: String = """
.apothecary-app {
  --ink:#2b1d3a; --muted:#6c6278; --potion:#6b3fa0; --elixir:#2f8f6f; --brass:#c08a2e; --rude:#b3261e;
  --paper:#fbf8f2; --rule:#ddd3c4;
  font:16px/1.45 system-ui, -apple-system, "Segoe UI", sans-serif; color:var(--ink); text-align:left;
  display:flex; flex-direction:column; gap:1rem;
}
.apothecary-app * { box-sizing:border-box; }
.apothecary-app h2 { margin:0; font-size:1.3rem; }
.apothecary-app button {
  font:inherit; padding:.35rem .8rem; border-radius:.4rem; border:1px solid var(--potion);
  background:var(--potion); color:#fff; cursor:pointer;
}
.apothecary-app button.quiet { background:transparent; color:var(--potion); }
.apothecary-app button:disabled { opacity:.45; cursor:not-allowed; }
.apothecary-app input[type=text], .apothecary-app input[type=number], .apothecary-app input[type=search] {
  font:inherit; padding:.35rem .5rem; border:1px solid var(--rule); border-radius:.35rem; background:#fff; min-width:0;
}
.apothecary-app .card { background:var(--paper); border:1px solid var(--rule); border-radius:.6rem; padding:1rem; }
.apothecary-app .row { display:flex; gap:.75rem; align-items:center; flex-wrap:wrap; }

/* 01 · cauldron */
.apothecary-app .slider-row { display:grid; grid-template-columns:8rem 1fr 3rem; gap:.5rem; align-items:center; }
.apothecary-app .cauldron {
  width:9rem; height:6rem; border-radius:0 0 4.5rem 4.5rem; border:6px solid #2b2b2b; border-top-width:10px;
  transition:background-color .3s; box-shadow:inset 0 -12px 18px #0004;
}
.apothecary-app .cauldron-warning { color:var(--rude); font-weight:600; }

/* 02 · forms */
.apothecary-app form { display:flex; flex-direction:column; gap:.8rem; }
.apothecary-app .field { display:flex; flex-direction:column; gap:.25rem; }
.apothecary-app .field > label { font-weight:600; }
.apothecary-app .field.invalid input { border-color:var(--rude); }
.apothecary-app .field-error { color:var(--rude); font-size:.9em; }
.apothecary-app .checks { display:flex; flex-wrap:wrap; gap:.3rem 1rem; }
.apothecary-app .problems { color:var(--rude); margin:0; padding-left:1.2rem; }
.apothecary-app .success { color:var(--elixir); font-weight:600; }

/* 03 · async */
.apothecary-app .thinking { color:var(--muted); font-style:italic; }
.apothecary-app .rude { color:var(--rude); }
.apothecary-app ul.results, .apothecary-app ul.shelves { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:.3rem; }
.apothecary-app .results li { display:flex; gap:.5rem; align-items:baseline; }
.apothecary-app .rarity { font-size:.75em; padding:0 .4em; border-radius:.3em; background:var(--rule); }
.apothecary-app .rarity.Legendary { background:var(--brass); color:#fff; }
.apothecary-app .note { color:var(--muted); font-size:.9em; }
.apothecary-app .shelf { display:grid; grid-template-columns:1fr auto auto auto auto; gap:.5rem; align-items:center; }
.apothecary-app .shelf.saving { opacity:.55; }
.apothecary-app .quantity { min-width:2.5rem; text-align:center; font-variant-numeric:tabular-nums; }

/* 04 · brew */
.apothecary-app .shake { animation:apothecary-shake .5s; }
@keyframes apothecary-shake {
  0%, 100% { transform:none; } 20% { transform:translate(-8px, 2px); } 40% { transform:translate(8px, -2px); }
  60% { transform:translate(-6px, 1px); } 80% { transform:translate(6px, -1px); }
}

/* the oracle's control panel */
.apothecary-app .oracle-panel {
  display:flex; flex-wrap:wrap; gap:.4rem 1.2rem; align-items:center; font-size:.85em; color:var(--muted);
  border:1px dashed var(--rule); border-radius:.5rem; padding:.4rem .7rem;
}
.apothecary-app .oracle-panel label { display:flex; gap:.4rem; align-items:center; }
.apothecary-app .oracle-title { font-weight:600; color:var(--ink); }
.apothecary-app .oracle-busy.active { color:var(--potion); }
"""
