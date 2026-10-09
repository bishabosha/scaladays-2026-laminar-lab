import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** Capstone · recipe (solution): a validated form whose value is a `Potion`.
 *
 *  Notebook cell:
 *    case class Potion(name: String, doses: Int, ingredients: List[String],
 *      drinkable: Boolean, explosive: Boolean, licence: String) derives ConfiguredJsonValueCodec
 *    val recipeH = Widgets.custom[Potion]("recipe", "recipe/form", "{}",
 *      Potion("", 1, Nil, true, false, ""))
 */
object Main extends LaminarPlugin:
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.widgetOpt match
      case Some(bridge) if ctx.name == "form" => Shop(RecipeForm(bridge))
      case _ => div(s"recipe: '${ctx.name}' is not a widget here")

object RecipeForm:
  val shortlist = List("Moonwater", "Nightshade", "Fire salt", "Eye of newt", "Mandrake root", "Pixie dust")

  def rules(p: Potion): List[String] =
    List(
      Option.when(p.name.isBlank)("Every potion needs a name"),
      Option.when(p.doses < 1 || p.doses > 12)("Brew between 1 and 12 doses"),
      Option.when(p.ingredients.isEmpty)("A potion needs at least one ingredient"),
      Option.when(p.drinkable && p.explosive)("Not both drinkable and explosive")
    ).flatten

  def apply(bridge: WidgetBridge): HtmlElement =
    val widget = bridge.as[Potion](Potion.empty)
    val draft = VarLenses(widget.value)
    val submitted = Var(false)
    val sent = Var(Option.empty[Potion])
    val problems = draft.baseVar.signal.map(rules)

    def toggle(name: String)(p: Potion) =
      p.copy(ingredients = if p.ingredients.contains(name) then p.ingredients.filterNot(_ == name) else p.ingredients :+ name)

    def send(): Unit =
      submitted.set(true)
      val potion = draft.baseVar.now()
      if rules(potion).isEmpty then
        widget.set(potion)
        sent.set(Some(potion))

    div(
      cls := "card",
      // Someone else (another tab, a kernel-side `recipeH.set`) changed the value.
      widget.signal --> draft.baseVar,
      h2("A new recipe"),
      form(
        onSubmit.preventDefault --> (_ => send()),
        div(cls := "field", label("Name"),
          input(typ := "text", controlled(value <-- draft.name.view, onInput.mapToValue --> draft.name.updater))),
        div(cls := "field", label("Doses"),
          input(typ := "number", minAttr := "1", maxAttr := "12", controlled(
            value <-- draft.doses.view.map(_.toString),
            onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> draft.doses.updater))),
        div(cls := "field", label("Ingredients"),
          div(cls := "checks", shortlist.map(name =>
            label(input(typ := "checkbox",
              checked <-- draft.ingredients.view.map(_.contains(name)),
              onChange --> (_ => draft.baseVar.update(toggle(name)))), " ", name)))),
        div(cls := "checks",
          label(input(typ := "checkbox",
            controlled(checked <-- draft.drinkable.view, onClick.mapToChecked --> draft.drinkable.updater)), " drinkable"),
          label(input(typ := "checkbox",
            controlled(checked <-- draft.explosive.view, onClick.mapToChecked --> draft.explosive.updater)), " explosive")),
        child.maybe <-- problems.combineWith(submitted.signal).map((ps, s) =>
          Option.when(s && ps.nonEmpty)(ul(cls := "problems", ps.map(li(_))))),
        div(cls := "row",
          button(typ := "submit", "Send to the notebook"),
          child.maybe <-- sent.signal.combineWith(draft.baseVar.signal).map((s, now) =>
            Option.when(s.contains(now))(span(cls := "success", "sent ✓"))))
      )
    )
