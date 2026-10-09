---scala
(id = "exercise-cellar", seconds = 720, layout = "exercise")
---

## Exercise 3 · The cellar

{{exercise 03-cellar}}

{{columns top}}

{{stack}}

1. Ingredient search: debounce + `flatMapSwitch`
2. Loading / failed / ready, with **retry**
3. Inventory list with `split`: add, ± quantity, delete
4. Optimistic update: change now, roll back if the oracle refuses
5. ★ Show a spinner on just the row being saved

{{end-stack}}

{{demo apothecary/cellar}}

{{end-columns}}

## Speaker notes

12 minutes. Step 4 is the hard one: keep the previous list to restore on failure.
