---scala
(id = "arrows", seconds = 150, layout = "standard")
---

## Pointing the arrows

```scala
def cauldron(heat: Var[Int]): HtmlElement =
  div(
    cls := "cauldron",
    backgroundColor <-- heat.signal.map(colourFor), // data → DOM
    input(
      typ := "range", minAttr := "0", maxAttr := "100",
      controlled(
        value <-- heat.signal.map(_.toString),
        onInput.mapToValue.map(_.toInt) --> heat     // DOM → data
      )
    ),
    child.text <-- heat.signal.map(h => s"$h°"),
    button("Stir", onClick --> (_ => heat.update(_ + 5)))
  )
```

`<--` reads *into* the element · `-->` writes *out* of it

## Speaker notes

Most Laminar code is these two arrows. Note `--> heat`: a Var is an Observer.
