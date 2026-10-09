import scala.scalajs.js

import com.raquo.laminar.api.L.*
import notebook.plugin.*

/** A widget bundle. Its kinds are `<this directory>/<name>`:
  *  - in a slide:    {{demo <dir>/counter count=3}}
  *  - in a notebook: case class Brews(count: Int) derives ConfiguredJsonValueCodec
  *                   val brewsH = Widgets.custom[Brews]("brews", "<dir>/counter", "{}", Brews(0))
  */
object Main extends LaminarPlugin:

  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.name match
      case "counter" => counter(ctx)
      case other     => div(s"unknown widget '$other'")

  /** Only JSON crosses the boundary: `{"count": n}` in and out. */
  def parseCount(json: String): Int =
    val count = js.JSON.parse(json).selectDynamic("count")
    if js.typeOf(count) == "number" then count.asInstanceOf[Double].toInt else 0

  def counter(ctx: PluginContext): HtmlElement =
    val bridge = ctx.widgetOpt.getOrElse(throw new IllegalStateException("counter is a widget"))
    val brews = Var(parseCount(bridge.value()))
    div(
      bridge.signal.map(parseCount) --> brews,
      button(
        "Brew 🧪",
        onClick(_.sample(brews.signal).map(_ + 1)) --> { n =>
          brews.set(n)
          bridge.set(s"""{"count":$n}""")
        }
      ),
      span(" brewed ", child.text <-- brews.signal, " times")
    )
