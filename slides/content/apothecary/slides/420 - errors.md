---scala
(id = "errors", seconds = 90, layout = "standard")
---

## Errors you can see

{{stack}}

- Model failures as **values**: `Remote.Failed`, `Problem`; render them
- One `EventBus[Notice]` per page for toasts, fed from anywhere
- Never let an exception escape an observer: the stream logs it and carries on, *silently*

```scala
AirstreamError.registerUnhandledErrorCallback(e => notices.emit(Notice.Bug(e)))
```

{{end-stack}}

## Speaker notes

The unhandled error callback is the safety net, not the plan.
