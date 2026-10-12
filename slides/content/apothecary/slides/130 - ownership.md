---scala
(id = "ownership", seconds = 120, layout = "standard")
---

## Subscription ownership

{{stack}}

- Every `<--` / `-->` is a **subscription owned by its element**
- Mounted → active · unmounted → *killed*. No leaks, no `dispose()`
- Laziness: an unobserved signal does no work

```scala
div(
  EventStream.periodic(1000) --> (_ => println("tick")), // stops on unmount
  onMountCallback(ctx => println("mounted"))
)
```

{{end-stack}}

## Speaker notes

This is the Laminar superpower vs. manual React-style cleanup. Mention `.now()` should be rare.
