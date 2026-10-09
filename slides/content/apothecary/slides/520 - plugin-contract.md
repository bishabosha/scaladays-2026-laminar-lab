---scala
(id = "plugin-contract", seconds = 180, layout = "standard")
---

## The plugin contract

```scala
object Main extends LaminarPlugin:                  // notebook-plugin-api
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    val bridge = ctx.widgetOpt.get                  // value(), set(json), signal
    val colour = Var(Rgb.parse(bridge.value()))
    div(bridge.signal.map(Rgb.parse) --> colour, sliders(colour, c => bridge.set(c.json)))
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

- Separately linked bundles: **only JSON crosses**
- Unknown plugin? The notebook shows a fallback
- The same bundle drives this deck's demos

{{end-stack}}

{{end-columns}}

## Speaker notes

Kinds are `<pluginId>/<name>`: one bundle at `/plugins/<pluginId>/main.js` serves several names via `ctx.name`.
Full example: `modules/notebook.sc/demo/plugins-src/potions/Main.scala`.
