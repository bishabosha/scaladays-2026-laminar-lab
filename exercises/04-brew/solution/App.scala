package apothecary

import scala.scalajs.js

import com.raquo.laminar.api.L.*

/** Exercise 04 · Celebrate a brew (solution) */
object App:
  /** The ingredients a recipe can choose from on this recipe. */
  val shortlist: List[String] = List(
    "Moonwater", "Nightshade", "Fire salt", "Dragon's breath", "Eye of newt", "Mandrake root", "Pixie dust")

  def view(): HtmlElement =
    val recipe = VarLenses(Potion.empty)
    val touched = Var(Set.empty[String])
    val submitted = Var(false)
    val registering = Var(false)
    val outcome = Var(Option.empty[Either[String, String]])
    val shaking = Var(false)

    /** Explosive potions get red confetti and a shaking shop. */
    def celebrate(potion: Potion): Unit =
      if potion.explosive then
        Confetti.fire(colours = List("#b3261e", "#ff7a1a", "#2b2b2b"))
        shaking.set(true)
        js.timers.setTimeout(600)(shaking.set(false))
      else Confetti.fire()

    val problems: Signal[List[Problem]] = recipe.baseVar.signal.map(Validation.validate)

    /** A field's problems, but only once it was touched or the form was submitted. */
    def errorsFor(field: String): Signal[List[Problem]] =
      problems
        .combineWith(touched.signal, submitted.signal)
        .map((ps, t, s) => if s || t(field) then ps.filter(_.field == field) else Nil)

    def touch(field: String) = onBlur --> (_ => touched.update(_ + field))

    def field(name: String, title: String)(controls: Modifier[HtmlElement]*): HtmlElement =
      div(
        cls := "field",
        cls("invalid") <-- errorsFor(name).map(_.nonEmpty),
        label(title),
        controls,
        children <-- errorsFor(name).map(_.map(p => span(cls := "field-error", p.message)))
      )

    def toggle(ingredient: String)(p: Potion): Potion =
      p.copy(ingredients =
        if p.ingredients.contains(ingredient) then p.ingredients.filterNot(_ == ingredient)
        else p.ingredients :+ ingredient)

    def register(): Unit =
      submitted.set(true)
      val potion = recipe.baseVar.now()
      if Validation.validate(potion).isEmpty then
        registering.set(true)
        outcome.set(None)
        Oracle.grumpy.register(potion).onComplete { result =>
          registering.set(false)
          outcome.set(Some(result.toEither.left.map(_.getMessage)))
          if result.isSuccess then celebrate(potion)
        }

    div(
      cls := "card",
      cls("shake") <-- shaking,
      h2("Brew and register"),
      Oracle.grumpy.panel,
      form(
        onSubmit.preventDefault --> (_ => register()),
        field("name", "Name")(
          input(typ := "text", touch("name"), controlled(value <-- recipe.name.view, onInput.mapToValue --> recipe.name.updater))
        ),
        field("doses", "Doses")(
          DoseInput(recipe.doses.view, recipe.doses.updater, max = Validation.maxDoses, touch("doses"))
        ),
        field("ingredients", "Ingredients")(
          div(
            cls := "checks",
            shortlist.map { name =>
              label(
                input(
                  typ := "checkbox", touch("ingredients"),
                  checked <-- recipe.ingredients.view.map(_.contains(name)),
                  onChange --> (_ => recipe.baseVar.update(toggle(name)))
                ),
                " ", name
              )
            }
          )
        ),
        field("explosive", "Properties")(
          div(
            cls := "checks",
            label(input(typ := "checkbox", touch("explosive"),
              controlled(checked <-- recipe.drinkable.view, onClick.mapToChecked --> recipe.drinkable.updater)), " drinkable"),
            label(input(typ := "checkbox", touch("explosive"),
              controlled(checked <-- recipe.explosive.view, onClick.mapToChecked --> recipe.explosive.updater)), " explosive")
          )
        ),
        // Only recipes with dragon's breath need a licence.
        child.maybe <-- recipe.ingredients.view.map(_.contains(Ingredients.dragonsBreath.name)).distinct.map(needsLicence =>
          Option.when(needsLicence)(
            field("licence", "Guild licence")(
              input(typ := "text", placeholder := "DB-1234", touch("licence"),
                controlled(value <-- recipe.licence.view, onInput.mapToValue --> recipe.licence.updater))
            )
          )
        ),
        div(
          cls := "row",
          button(typ := "submit", disabled <-- registering.signal, child.text <-- registering.signal.map(
            if _ then "Consulting the Guild…" else "Register")),
          child.maybe <-- outcome.signal.map(_.map {
            case Right(number) => span(cls := "success", s"Registered as $number ✨")
            case Left(why)     => span(cls := "rude", why)
          })
        ),
        // ★ A summary of every problem, after the first submit.
        child.maybe <-- problems.combineWith(submitted.signal).map((ps, s) =>
          Option.when(s && ps.nonEmpty)(ul(cls := "problems", ps.map(p => li(p.message))))
        )
      )
    )
