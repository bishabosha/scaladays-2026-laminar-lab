package apothecary

import com.raquo.laminar.api.L.*

/** Exercise 02 · Register a potion (solution) */
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
        }

    div(
      cls := "card",
      h2("Register a potion"),
      Oracle.grumpy.panel,
      form(
        onSubmit.preventDefault --> (_ => register()),
        field("name", "Name")(
          input(typ := "text", touch("name"), controlled(value <-- recipe.name.view, onInput.mapToValue --> recipe.name.updater))
        ),
        field("doses", "Doses")(
          input(
            typ := "number", minAttr := "1", maxAttr := Validation.maxDoses.toString, touch("doses"),
            controlled(
              value <-- recipe.doses.view.map(_.toString),
              onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> recipe.doses.updater
            )
          )
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
