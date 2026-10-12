---scala
(id = "notebook-tour", seconds = 180, layout = "standard")
---

## notebook.sc

{{columns top}}

{{stack}}

- Scala cells form a **dependency graph**: run one, its dependents re-run
- The UI is written in Laminar
- Built-in widgets: `slider`, `select`, `text`, `form`
- A widget's value becomes a cell: `val heat: Int = 42`

{{end-stack}}

{{stack}}

```scala
// cell: a slider widget
val heatH =
  Widgets.slider("heat", 0, 100, 40)

// cell: re-runs when heat changes
if heat > 80 then "high" else "normal"
```

{{end-stack}}

{{end-columns}}

## Speaker notes

Live demo in the notebook: move the slider, the downstream cell updates.
The slider's value lives in a managed cell `val heat: Int = 40`; the handle (`heatH`) is for writes.
