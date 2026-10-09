import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** Capstone · oracle: pick ingredients with an async search; the value is their names.
 *
 *  Done for you: the picked chips and sending them to the notebook.
 *  1. Search the oracle as the user types: debounce, `flatMapSwitch`, `Remote.consult`.
 *  2. Show loading / refused (with "Ask again") / no match / results with a "+" to pick.
 *  3. ★ Disable "+" for ingredients already picked.
 *  Build: ./sake widget oracle WATCH=1 · answer: ./sake widget-solution oracle
 *
 *  Notebook cell:
 *    import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
 *    import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
 *    given JsonValueCodec[List[String]] = JsonCodecMaker.make
 *    val pickedH = Widgets.custom[List[String]]("picked", "oracle/picker", "{}", Nil)
 */
object Main extends LaminarPlugin:
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.widgetOpt match
      case Some(bridge) if ctx.name == "picker" => Shop(Picker(bridge))
      case _ => div(s"oracle: '${ctx.name}' is not a widget here")

object Picker:
  def apply(bridge: WidgetBridge): HtmlElement =
    val widget = bridge.as[List[String]](Nil)
    val picked = Var(widget.value)
    val query = Var("")
    val retry = EventBus[Unit]()

    def pick(names: List[String]): Unit =
      picked.set(names)
      widget.set(names)

    // TODO 1: answers to the latest question (see exercise 03, and `Remote.consult`)
    val answers: Signal[Option[Remote[List[Ingredient]]]] = Val(None)

    div(
      cls := "card",
      widget.signal --> picked,
      h2("Ask the oracle"),
      Oracle.grumpy.panel,
      div(
        cls := "row",
        children <-- picked.signal.split(identity)((name, _, _) =>
          button(cls := "quiet", s"$name ✕", onClick(_.sample(picked.signal)) --> (now => pick(now.filterNot(_ == name)))))
      ),
      input(typ := "search", placeholder := "newt, nettle, dragon…",
        controlled(value <-- query, onInput.mapToValue --> query)),
      // TODO 2: render `answers`, with a "+" button per ingredient: pick(now :+ ingredient.name)
      child <-- answers.map(_ => emptyNode)
    )
