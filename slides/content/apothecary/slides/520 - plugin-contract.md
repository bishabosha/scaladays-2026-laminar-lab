---scala
(id = "plugin-contract", seconds = 180, layout = "standard")
---

## The plugin contract

```scala
case class Rgb(r: Int, g: Int, b: Int) derives Json.ReadWriter   // upickle

object Main extends LaminarPlugin:                                // notebook-plugin-api
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    val widget = ctx.widgetOpt.get.as[Rgb](Rgb(0, 0, 0))          // value, set, signal
    val colour = Var(widget.value)
    div(widget.signal --> colour, sliders(colour, onChange = widget.writer))
```

{{columns top}}

```scala
// a notebook cell
case class Rgb(r: Int, g: Int, b: Int)
  derives ConfiguredJsonValueCodec
val mixH = Widgets.custom[Rgb](
  "mix", "potions/mixer",
  config = "{}", Rgb(180, 40, 120))
```

{{stack}}

- Separate bundles: **only JSON crosses**, a case class each side
- An unknown plugin renders as a fallback
- The same bundle drives this deck's demos

{{end-stack}}

{{end-columns}}

## Speaker notes

Kinds are `<pluginId>/<name>`: one bundle at `/plugins/<pluginId>/main.js` serves several names via `ctx.name`.
Full example: `modules/notebook.sc/demo/plugins-src/potions/Main.scala`.
