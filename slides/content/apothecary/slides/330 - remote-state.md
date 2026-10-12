---scala
(id = "remote-state", seconds = 150, layout = "standard")
---

## Modelling remote state

```scala
enum Remote[+A]:
  case Loading
  case Ready(value: A)
  case Failed(reason: String)

def consult[A](f: => Future[A]): Signal[Remote[A]] =
  EventStream
    .fromFuture(f.map(Remote.Ready(_)).recover(e => Remote.Failed(e.getMessage)))
    .startWith(Remote.Loading)

child <-- consult(oracle.inventory()).map {
  case Remote.Loading       => p(cls := "loading", "Loading…")
  case Remote.Ready(items)  => ul(items.map(i => li(i.name)))
  case Remote.Failed(why)   => p(cls := "error", why, button("Retry", onClick --> retry))
}
```

## Speaker notes

Errors are values, rendered like anything else. Retry is just another event into the same stream.
