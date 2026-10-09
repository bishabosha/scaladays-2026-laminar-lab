---scala
(id = "controlled", seconds = 150, layout = "standard")
---

## Controlled inputs

```scala
case class Potion(name: String, doses: Int, drinkable: Boolean, explosive: Boolean)

val potion = Var(Potion("", 1, drinkable = true, explosive = false))

input(
  controlled(
    value <-- potion.signal.map(_.name),
    onInput.mapToValue --> potion.updater[String]((p, n) => p.copy(name = n))
  )
)
```

- `controlled` makes the `Var` the **single source of truth**
- Filter input *before* it lands: `onInput.mapToValue.filter(_.forall(_.isDigit))`

## Speaker notes

Without `controlled`, typing and the signal can fight. Show filter dropping letters live.
