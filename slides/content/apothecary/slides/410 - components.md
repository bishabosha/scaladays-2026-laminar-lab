---scala
(id = "components", seconds = 150, layout = "standard")
---

## A component is a function

```scala
object DoseInput:
  def apply(value: Signal[Int], onChange: Observer[Int], max: Int = 12): HtmlElement =
    div(
      cls := "dose-input",
      button("−", onClick(_.sample(value).map(v => (v - 1) max 1)) --> onChange),
      child.text <-- value,
      button("+", onClick(_.sample(value).map(v => (v + 1) min max)) --> onChange)
    )
```

- **In:** `Signal`s. **Out:** `Observer`s. **Return:** an element
- State lives with whoever *needs* it, usually the page, not the widget
- Needs to expose more? Return a small class: `.element` plus extra signals

## Speaker notes

`onClick(_.sample(value))`: the event-processor syntax, no `.now()` needed.
Business apps: one object per page, components below it, no global state.
