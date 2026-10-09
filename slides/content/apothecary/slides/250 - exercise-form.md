---scala
(id = "exercise-form", seconds = 900, layout = "exercise")
---

## Exercise 2 · Register a potion

{{exercise 02-potion-form}}

{{columns top}}

{{stack}}

1. Name, doses, ingredients, two checkboxes
2. Errors appear after **blur** or **submit**
3. Not both *drinkable* and *explosive*
4. **Dragon's breath** needs a licence no.
5. ★ Disable submit while invalid

{{end-stack}}

{{demo apothecary/potion-form}}

{{end-columns}}

## Speaker notes

15 minutes. Rule 4 makes a field appear conditionally: `child.maybe <--`.
Tests for `validate` are provided; run `sake test-02`.
