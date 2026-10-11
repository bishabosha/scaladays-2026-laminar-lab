import scala.scalajs.js

import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.{Json, as}

/** The mixer's value: what the notebook's `case class Rgb(r: Int, g: Int, b: Int)` reads. */
case class Rgb(r: Int, g: Int, b: Int) derives Json.ReadWriter:
  def css = s"rgb($r, $g, $b)"

/** The swatch display's payload. */
case class Swatch(r: Int = 0, g: Int = 0, b: Int = 0, name: String = "") derives Json.ReadWriter:
  def rgb = Rgb(r, g, b)

/** The Apothecary's potion plugin: one bundle, two kinds.
  *  - `potions/mixer`  (widget)  three sliders → a colour, emitted as {"r","g","b"}
  *  - `potions/swatch` (display) a labelled colour swatch for {"r","g","b","name"?}
  */
object Main extends LaminarPlugin:

  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.name match
      case "mixer"  => mixer(payload, ctx)
      case "swatch" => swatch(payload.map(Json.readOr(_, Swatch())))
      case other    => div(s"potions: unknown renderer '$other'")

  private def swatchBox(rgb: Signal[Rgb]) =
    div(
      styleAttr <-- rgb.map(c =>
        s"width:3.5em;height:3.5em;border-radius:50%;border:2px solid #8884;background:${c.css};" +
          "box-shadow:inset -6px -8px 12px #0004"
      )
    )

  def swatch(swatch: Signal[Swatch]): HtmlElement =
    div(
      styleAttr := "display:flex;gap:.8em;align-items:center",
      swatchBox(swatch.map(_.rgb)),
      div(
        b(child.text <-- swatch.map(s => if s.name.nonEmpty then s.name else "unnamed potion")),
        div(styleAttr := "opacity:.7;font-family:monospace", child.text <-- swatch.map(_.rgb.css))
      )
    )

  def mixer(config: Signal[String], ctx: PluginContext): HtmlElement =
    val widget = ctx.widgetOpt.getOrElse(throw js.JavaScriptException(js.Error("mixer is a widget")))
      .as[Rgb](Rgb(0, 0, 0))
    // local state follows external changes; the plugin's own writes go out
    // on every drag tick (the notebook coalesces them latest-wins)
    val colour = Var(widget.value)
    def slider(label: String, get: Rgb => Int, set: (Rgb, Int) => Rgb) =
      label_(
        styleAttr := "display:flex;gap:.5em;align-items:center",
        span(styleAttr := "width:1em", label),
        input(
          typ := "range", minAttr := "0", maxAttr := "255",
          controlled(
            value <-- colour.signal.map(get(_).toString),
            onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> { v =>
              colour.update(set(_, v))
              widget.set(colour.now())
            }
          )
        ),
        code(child.text <-- colour.signal.map(get(_).toString))
      )
    div(
      styleAttr := "display:flex;gap:1em;align-items:center",
      widget.signal --> colour.writer,
      div(
        slider("R", _.r, (c, v) => c.copy(r = v)),
        slider("G", _.g, (c, v) => c.copy(g = v)),
        slider("B", _.b, (c, v) => c.copy(b = v))
      ),
      swatchBox(colour.signal)
    )

  private def label_ = com.raquo.laminar.api.L.label
