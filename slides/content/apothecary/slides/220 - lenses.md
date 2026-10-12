---scala
(id = "lenses", seconds = 150, layout = "standard")
---

## Lenses over a case-class `Var`

Simplified updaters for large form-like data (product types).

```scala
val form = VarLenses(Potion("", 1, drinkable = true, explosive = false))

input(controlled(
  value <-- form.name.view,
  onInput.mapToValue --> form.name.updater
))
```

- `form.name` is a named tuple with two fields: `updater: Observer[String]` and `view: Signal[String]`.
- Compatible with any case class that supports `scala.deriving.Mirror`.

## Speaker notes

Don't go deep into the macro-free implementation; it's in the repo for the curious.
