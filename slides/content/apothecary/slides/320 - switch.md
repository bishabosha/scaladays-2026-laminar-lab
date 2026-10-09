---scala
(id = "switch", seconds = 180, layout = "standard")
---

## Typing faster than the oracle

```scala
val query = Var("")

val results: EventStream[List[Ingredient]] =
  query.signal.changes
    .debounce(300)                                       // wait for a pause
    .flatMapSwitch(q =>                                  // drop stale answers
      EventStream.fromFuture(oracle.searchIngredients(q)))
```

- `flatMapMerge` would show *whichever answer arrives last* 😱
- `flatMapSwitch` only listens to the **latest** request

## Speaker notes

Demo the bug with flatMapMerge first: type "newt", then "nettle"; the newt answer arrives late.
