---scala
(id = "exercise-cauldron", seconds = 600, layout = "exercise")
---

## Exercise 1 · The cauldron

{{exercise 01-cauldron}}

{{columns top}}

{{stack}}

1. Three sliders: **moonwater**, **nightshade**, **fire salt**
2. The cauldron's colour mixes from all three
3. Show *"It's going to blow!"* when fire salt > 80
4. ★ A **Stir** button that adds a little of everything

`./sake ex 01-cauldron`

{{end-stack}}

{{demo 01-cauldron/app zoom=0.9}}

{{end-columns}}

## Speaker notes

10 minutes. Hint for 2: `Signal.combine(a, b, c)` or `a.combineWith(b, c)`.
