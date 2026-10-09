# Widgets

Every directory here is one widget bundle: a Laminar plugin you can show in the
slides **and** use in a notebook.sc notebook.

```sh
./sake new-widget cauldron        # copies _template/ to cauldron/
./sake widget cauldron            # builds cauldron/
./sake widget cauldron WATCH=1    # ...and rebuilds on every save
./sake widgets                    # builds every widget
```

On Windows, run `sake` instead of `./sake`. Run `./sake` on its own to list every command.

Write your code in `widgets/<NAME>/`. `project.scala` holds the shared scala-cli
settings (Scala.js, Laminar, the notebook plugin API), so you don't need your own.

The build output goes to `slides/public/assets/widgets/<NAME>/main.js`. Both the
deck and the notebook read from that folder:

| Where | How to include it |
| --- | --- |
| a slide | `{{demo cauldron/mixer}}`, optionally seeded: `{{demo cauldron/mixer r=120 g=60}}` |
| a notebook cell | `Widgets.custom[Rgb]("mix", "cauldron/mixer", "{}", Rgb(1, 2, 3))` |
| a notebook display | `Display.extensionOf("cauldron/swatch", value)` |

A kind is `<directory>/<name>`. The directory selects the bundle, and your
`view` receives `<name>` as `ctx.name`, so one bundle can serve several widgets.

## Values are case classes

Only JSON crosses between your widget and the notebook. Describe it with a case class on
each side, with the same field names:

```scala
// in the widget (upickle)
case class Rgb(r: Int, g: Int, b: Int) derives Json.ReadWriter
val widget = ctx.widgetOpt.get.as[Rgb](Rgb(0, 0, 0))   // widget.value, widget.set(rgb), widget.signal

// in the notebook (jsoniter)
case class Rgb(r: Int, g: Int, b: Int) derives ConfiguredJsonValueCodec
val mixH = Widgets.custom[Rgb]("mix", "mixer/rgb", "{}", Rgb(180, 40, 120))
```

`Json` and `.as` come from `apothecary/Json.scala`, along with the shop's domain, the grumpy
oracle and `Shop(...)` (the shop's styles). Every widget can use them: `import apothecary.*`.

## Capstone starters

| Directory | Kind | Value | Answer |
| --- | --- | --- | --- |
| `recipe/` | `recipe/form` | a `Potion` | `./sake widget-solution recipe` |
| `oracle/` | `oracle/picker` | a `List[String]` | `./sake widget-solution oracle` |
| `chart/` | `chart/brew` | a display, no value | `./sake widget-solution chart` |
| your own | `./sake new-widget mixer` | e.g. an `Rgb` | `potions/` |

The lab notebook (`notebooks/apothecary.notebook.sc`, run with `./sake notebook`) has a
section wired up for each. The answers live in `_solutions/`.

After rebuilding, the deck reloads by itself (`./sake slides`). In the notebook, refresh the
browser. Plugin *displays* (like the chart) appear in the notebook's inspector: click the
cell's `(result)` chip.
