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
5. ★ A problem summary

Done when `./sake test 02-potion-form` passes

{{end-stack}}

{{demo 02-potion-form/app zoom=0.6}}

{{end-columns}}

## Speaker notes

15 minutes. Rule 4 makes a field appear conditionally: `child.maybe <--`.
Tests for `validate` are provided: `./sake test 02-potion-form`. Run with `./sake ex 02-potion-form`.
The ★ also disables the button while the oracle is thinking (disabling while invalid would hide submit errors).
