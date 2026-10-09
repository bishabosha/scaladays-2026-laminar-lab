import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** Capstone · oracle (solution): pick ingredients with an async search; the value is their names.
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

    val answers: Signal[Option[Remote[List[Ingredient]]]] =
      EventStream.merge(query.signal.changes.debounce(300), retry.events.sample(query.signal))
        .flatMapSwitch(q =>
          if q.isBlank then EventStream.fromValue(None)
          else Remote.consult(Oracle.grumpy.searchIngredients(q)).map(Some(_)))
        .startWith(None)

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
      child <-- answers.map {
        case None                     => emptyNode
        case Some(Remote.Loading)     => p(cls := "thinking", "The oracle is thinking…")
        case Some(Remote.Failed(why)) => p(cls := "rude", why, " ", button("Ask again", onClick.mapToUnit --> retry))
        case Some(Remote.Ready(Nil))  => p(cls := "note", "Nothing by that name.")
        case Some(Remote.Ready(found)) =>
          ul(cls := "results", found.map(ingredient =>
            li(
              button(cls := "quiet", "+",
                disabled <-- picked.signal.map(_.contains(ingredient.name)),
                onClick(_.sample(picked.signal)) --> (now => pick(now :+ ingredient.name))),
              b(ingredient.name),
              span(cls := s"rarity ${ingredient.rarity}", ingredient.rarity.toString)
            )))
      }
    )
