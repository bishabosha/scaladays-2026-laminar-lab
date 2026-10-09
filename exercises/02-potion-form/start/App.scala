package apothecary

import com.raquo.laminar.api.L.*

/** Exercise 02 · Register a potion
 *
 *  The name field is done: it shows its problems once it was touched (blurred) or the form
 *  was submitted. Follow its pattern.
 *
 *  1. Fields for doses (number), ingredients (checkboxes from `shortlist`), drinkable and
 *     explosive (checkboxes). Their problems go under "doses", "ingredients" and "explosive".
 *  2. Fill in the Guild's rules in Validation.scala; `sake test 02-potion-form` checks them.
 *  3. Not both drinkable and explosive (that's a rule in Validation too).
 *  4. A "Guild licence" field that only appears when Dragon's breath is an ingredient
 *     (`child.maybe <--`).
 *  5. ★ After the first submit, list every problem under the button; disable the button
 *     while the oracle is thinking.
 */
object App:
  /** The ingredients a recipe can choose from on this form. */
  val shortlist: List[String] = List(
    "Moonwater", "Nightshade", "Fire salt", "Dragon's breath", "Eye of newt", "Mandrake root", "Pixie dust")

  def view(): HtmlElement =
    val recipe = VarLenses(Potion.empty)
    val touched = Var(Set.empty[String])
    val submitted = Var(false)
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

    def register(): Unit =
      submitted.set(true)
      val potion = recipe.baseVar.now()
      if Validation.validate(potion).isEmpty then
        outcome.set(None)
        Oracle.grumpy.register(potion).onComplete { result =>
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
        // TODO 1: doses, ingredients, drinkable and explosive
        // TODO 4: the licence field, only with Dragon's breath
        div(
          cls := "row",
          button(typ := "submit", "Register"),
          child.maybe <-- outcome.signal.map(_.map {
            case Right(number) => span(cls := "success", s"Registered as $number ✨")
            case Left(why)     => span(cls := "rude", why)
          })
        )
        // TODO 5 ★: the problem summary
      )
    )
