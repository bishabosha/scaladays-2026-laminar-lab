---scala
(id = "touched", seconds = 150, layout = "standard")
---

## Don't shout before they've typed

```scala
val touched = Var(Set.empty[String])
val submitted = Var(false)

def errorsFor(field: String): Signal[List[Problem]] =
  problems
    .combineWith(touched.signal, submitted.signal)
    .map((ps, t, s) => if s || t(field) then ps.filter(_.field == field) else Nil)

input(onBlur --> (_ => touched.update(_ + "name")), /* ... */)

form(onSubmit.preventDefault --> { _ =>
  submitted.set(true)
  if validate(potion.now()).isEmpty then save(potion.now())
})
```

## Speaker notes

`.now()` in an event handler is fine; avoid it in rendering. Mention `disabled <-- problems.map(_.nonEmpty)`.
