---scala
(id = "appendix-real-server", seconds = 0, layout = "appendix")
---

## Appendix · A real oracle

```scala
class HttpOracle(base: String) extends Oracle:
  def searchIngredients(query: String) =
    dom.fetch(s"$base/ingredients?q=${js.URIUtils.encodeURIComponent(query)}").toFuture
      .flatMap(_.text().toFuture)
      .map(read[List[Ingredient]](_))
```

The exercises only depend on `Oracle`, so this slots in without touching the views

## Speaker notes
