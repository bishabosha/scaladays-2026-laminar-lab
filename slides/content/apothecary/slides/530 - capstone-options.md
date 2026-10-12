---scala
(id = "capstone-options", seconds = 120, layout = "standard")
---

## Capstone options

| Widget | Start in | Module |
| --- | --- | --- |
| **Mixer**: sliders → an `Rgb` | `new-widget` | 1 |
| **Recipe**: a form → a `Potion` | `widgets/recipe` | 2 |
| **Oracle**: search → `List[String]` | `widgets/oracle` | 3 |
| **Chart**: plots a cell over time | `widgets/chart` | 4 |

`./sake widget recipe WATCH=1` · `./sake notebook`

## Speaker notes

Everything lives in widgets/<name>/; see widgets/README.md. The same build shows up in the deck with the demo template.
The lab notebook (notebooks/apothecary.notebook.sc) already has a section wired up for each option.
Mixer: `./sake new-widget mixer`, the answer is widgets/potions. Others: `./sake widget-solution <name>` builds the answer.
Remind them: `./sake notebook` rebuilds every widget first; refresh the browser after a rebuild.
