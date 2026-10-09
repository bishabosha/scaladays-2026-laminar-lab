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

After rebuilding, the deck reloads by itself (`sake slides`). In the notebook
(`sake notebook`), refresh the browser.

`potions/` is a complete example: a colour mixer widget and a swatch display.
