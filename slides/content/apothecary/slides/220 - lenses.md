---scala
(id = "lenses", seconds = 150, layout = "standard")
---

## Less `copy`, more potion

From last year's ScalaDays: a `Selectable` lens over a case-class `Var`

```scala
val form = VarLenses(Potion("", 1, drinkable = true, explosive = false))

input(controlled(
  value <-- form.name.view,
  onInput.mapToValue --> form.name.updater
))
```

- `form.name` is typed `(updater: Observer[String], view: Signal[String])`
- Named tuples + `Mirror.ProductOf`, ~50 lines: `lenses.scala` in the exercise

## Speaker notes

Don't go deep into the macro-free implementation; it's in the repo for the curious.
