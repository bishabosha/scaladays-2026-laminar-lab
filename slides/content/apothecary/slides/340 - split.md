---scala
(id = "split", seconds = 180, layout = "standard")
---

## Touch only what changed

```scala
val stock: Var[List[Stock]] = Var(Nil)

ul(
  children <-- stock.signal.split(_.id) { (id, initial, item: Signal[Stock]) =>
    li(
      child.text <-- item.map(_.name),
      child.text <-- item.map(s => s" × ${s.quantity}"),
      button("−", onClick --> (_ => adjust(id, -1)))
    )
  }
)
```

- One element **per id**, created once, then fed an updated `Signal`
- Focus, scroll and CSS transitions survive updates

## Speaker notes

Contrast with `children <-- stock.signal.map(_.map(render))`: re-creates every row, loses focus.
