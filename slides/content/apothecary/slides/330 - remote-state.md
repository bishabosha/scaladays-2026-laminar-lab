---scala
(id = "remote-state", seconds = 150, layout = "standard")
---

## Loading, ready, or rude

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
  case Remote.Loading       => p(cls := "bubbling", "The oracle is thinking…")
  case Remote.Ready(items)  => ul(items.map(i => li(i.name)))
  case Remote.Failed(why)   => p(cls := "rude", why, button("Ask again", onClick --> retry))
}
```

## Speaker notes

Errors are values, rendered like anything else. Retry is just another event into the same stream.
