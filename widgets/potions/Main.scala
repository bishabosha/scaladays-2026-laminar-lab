import scala.scalajs.js

import com.raquo.laminar.api.L.*
import notebook.plugin.*

/** The Apothecary's potion plugin: one bundle, two kinds.
  *  - `potions/mixer`  (widget)  three sliders → a colour, emitted as {"r","g","b"}
  *  - `potions/swatch` (display) a labelled colour swatch for {"r","g","b","name"?}
  */
object Main extends LaminarPlugin:

  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.name match
      case "mixer"  => mixer(payload, ctx)
      case "swatch" => swatch(payload.map(parseRgb))
      case other    => div(s"potions: unknown renderer '$other'")

  final case class Rgb(r: Int, g: Int, b: Int, name: String = ""):
    def css = s"rgb($r, $g, $b)"
    def json = s"""{"r":$r,"g":$g,"b":$b}"""

  private def parseRgb(json: String): Rgb =
    val d = js.JSON.parse(json)
    def channel(k: String) =
      val v = d.selectDynamic(k)
      if js.typeOf(v) == "number" then v.asInstanceOf[Double].toInt.max(0).min(255) else 0
    val name = d.selectDynamic("name")
    Rgb(channel("r"), channel("g"), channel("b"), if js.typeOf(name) == "string" then name.toString else "")

  private def swatchBox(rgb: Signal[Rgb]) =
    div(
      styleAttr <-- rgb.map(c =>
        s"width:3.5rem;height:3.5rem;border-radius:50%;border:2px solid #8884;background:${c.css};" +
          "box-shadow:inset -6px -8px 12px #0004"
      )
    )

  def swatch(rgb: Signal[Rgb]): HtmlElement =
    div(
      styleAttr := "display:flex;gap:.8rem;align-items:center",
      swatchBox(rgb),
      div(
        b(child.text <-- rgb.map(c => if c.name.nonEmpty then c.name else "unnamed potion")),
        div(styleAttr := "opacity:.7;font-family:monospace", child.text <-- rgb.map(_.css))
      )
    )

  def mixer(config: Signal[String], ctx: PluginContext): HtmlElement =
    val bridge = ctx.widgetOpt.getOrElse(throw js.JavaScriptException(js.Error("mixer is a widget")))
    // local state follows external changes; the plugin's own writes go out
    // on every drag tick (the notebook coalesces them latest-wins)
    val colour = Var(parseRgb(bridge.value()))
    def slider(label: String, get: Rgb => Int, set: (Rgb, Int) => Rgb) =
      label_(
        styleAttr := "display:flex;gap:.5rem;align-items:center",
        span(styleAttr := "width:1rem", label),
        input(
          typ := "range", minAttr := "0", maxAttr := "255",
          controlled(
            value <-- colour.signal.map(get(_).toString),
            onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> { v =>
              colour.update(set(_, v))
              bridge.set(colour.now().json)
            }
          )
        ),
        code(child.text <-- colour.signal.map(get(_).toString))
      )
    div(
      styleAttr := "display:flex;gap:1rem;align-items:center",
      bridge.signal.map(parseRgb) --> colour.writer,
      div(
        slider("R", _.r, (c, v) => c.copy(r = v)),
        slider("G", _.g, (c, v) => c.copy(g = v)),
        slider("B", _.b, (c, v) => c.copy(b = v))
      ),
      swatchBox(colour.signal)
    )

  private def label_ = com.raquo.laminar.api.L.label
