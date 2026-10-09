---scala
(id = "the-oracle", seconds = 120, layout = "standard")
---

## The oracle

```scala
trait Oracle:
  def searchIngredients(query: String): Future[List[Ingredient]]
  def inventory(): Future[List[Stock]]
  def save(stock: Stock): Future[Stock]
  def delete(id: StockId): Future[Unit]
```

- Runs in your browser: **random latency** (100–1500 ms)
- **10 % of calls fail** with a rude message. On purpose.
- Same interface over `FetchStream` against a real server: see appendix

## Speaker notes

The fake backend means no server setup and no conference Wi-Fi dependency.
The latency is what makes ordering bugs visible.
