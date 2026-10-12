---scala
(id = "appendix-real-server", seconds = 0, layout = "appendix")
---

## Appendix · An HTTP backend

```scala
class HttpOracle(base: String) extends Oracle:
  def searchIngredients(query: String) =
    dom.fetch(s"$base/ingredients?q=${js.URIUtils.encodeURIComponent(query)}").toFuture
      .flatMap(_.text().toFuture)
      .map(read[List[Ingredient]](_))
```

The exercises depend only on `Oracle`, so this replaces the simulated backend without changes to the views

## Speaker notes
