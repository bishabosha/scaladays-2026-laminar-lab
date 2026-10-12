---scala
(id = "errors", seconds = 90, layout = "standard")
---

## Error handling

{{stack}}

- Model failures as **values** (`Remote.Failed`, `Problem`) and render them
- One `EventBus[Notice]` per page for notifications
- An exception thrown in an observer is logged and otherwise ignored; register a handler

```scala
AirstreamError.registerUnhandledErrorCallback(e => notices.emit(Notice.Bug(e)))
```

{{end-stack}}

## Speaker notes

The unhandled error callback is the safety net, not the plan.
