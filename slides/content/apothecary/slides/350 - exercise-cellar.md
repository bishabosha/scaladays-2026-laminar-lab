---scala
(id = "exercise-cellar", seconds = 720, layout = "exercise-demo")
---

## Exercise 3 · The cellar

{{exercise 03-cellar}}

{{stack}}

1. Ingredient search: debounce + `flatMapSwitch`
2. Loading / failed / ready, with **retry**
3. Shelves with `split`, plus delete
4. Optimistic: change now, undo on refusal
5. ★ Dim just the row being saved

{{end-stack}}

{{demo 03-cellar/app zoom=0.8}}

## Speaker notes

12 minutes. `./sake ex 03-cellar`. Tell them to turn the oracle's "slowest" slider up first to see the race.
Step 4 is the hard one: keep the previous shelf to restore on failure.
