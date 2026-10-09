---scala
(id = "validation", seconds = 180, layout = "standard")
---

## Validation is just a `Signal`

```scala
case class Problem(field: String, message: String)

def validate(p: Potion): List[Problem] =
  List(
    Option.when(p.name.isBlank)(Problem("name", "Every potion needs a name")),
    Option.when(p.doses > 12)(Problem("doses", "At most 12 doses per batch")),
    Option.when(p.drinkable && p.explosive)(
      Problem("explosive", "A potion may not be both drinkable and explosive"))
  ).flatten

val problems: Signal[List[Problem]] = potion.signal.map(validate)
```

A pure function: **test it without a browser**

## Speaker notes

Key message: keep validation out of the view. Cross-field rules are trivial this way.
