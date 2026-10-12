---scala
(id = "var-signal-stream", seconds = 180, layout = "standard")
---

## `Var`, `Signal`, `EventStream`

{{columns top}}

{{stack}}

```scala
val heat: Var[Int] = Var(40)

val colour: Signal[String] =
  heat.signal.map(h =>
    if h > 80 then "crimson" else "violet")

val bubbles: EventStream[Unit] =
  EventStream.periodic(500).mapToUnit
```

{{end-stack}}

{{stack}}

- **`Var`**: mutable state, `set` / `update`
- **`Signal`**: always has a *current* value
- **`EventStream`**: things that *happen*
- `map`, `combineWith`, `changes`, `startWith`

{{end-stack}}

{{end-columns}}

## Speaker notes

Signal = value over time, EventStream = discrete events. Signals dedupe with ==.
