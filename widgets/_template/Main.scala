import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** The widget's value. Only JSON crosses to the notebook: this is `{"count": n}`. */
case class Brews(count: Int) derives Json.ReadWriter

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

  def counter(ctx: PluginContext): HtmlElement =
    val widget = ctx.widgetOpt.getOrElse(throw new IllegalStateException("counter is a widget"))
      .as[Brews](Brews(0))
    val brews = Var(widget.value.count)
    div(
      widget.signal.map(_.count) --> brews,
      button(
        "Brew 🧪",
        onClick(_.sample(brews.signal).map(_ + 1)) --> { n =>
          brews.set(n)
          widget.set(Brews(n))
        }
      ),
      span(" brewed ", child.text <-- brews.signal, " times")
    )
