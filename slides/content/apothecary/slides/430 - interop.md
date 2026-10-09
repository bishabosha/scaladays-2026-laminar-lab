---scala
(id = "interop", seconds = 120, layout = "standard")
---

## Borrowing from JavaScript

```scala
object Confetti:
  @js.native @JSImport("https://esm.sh/canvas-confetti@1.9.3", JSImport.Default)
  def fire(options: js.Object): js.Promise[Unit] = js.native

button(
  "Brew!",
  onClick --> (_ => Confetti.fire(js.Dynamic.literal(particleCount = 150, spread = 70)))
)
```

Widgets that own DOM: hand them a node on mount, clean up on unmount

```scala
div(onMountUnmountCallback(
  mount = ctx => chart = Chart(ctx.thisNode.ref, config),
  unmount = _ => chart.destroy()
))
```

## Speaker notes

Same pattern applies to jQuery widgets: `$(ctx.thisNode.ref).datepicker()` in mount, `destroy` in unmount.
