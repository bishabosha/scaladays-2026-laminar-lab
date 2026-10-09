---scala
(id = "exercise-cellar", seconds = 720, layout = "exercise")
---

## Exercise 3 · The cellar

{{exercise 03-cellar}}

{{columns top}}

{{stack}}

1. Ingredient search: debounce + `flatMapSwitch`
2. Loading / failed / ready, with **retry**
3. Shelves with `split`, plus delete
4. Optimistic: change now, undo on refusal
5. ★ Dim just the row being saved

{{end-stack}}

{{demo 03-cellar/app}}

{{end-columns}}

## Speaker notes

12 minutes. `./sake ex 03-cellar`. Tell them to turn the oracle's "slowest" slider up first to see the race.
Step 4 is the hard one: keep the previous shelf to restore on failure.
