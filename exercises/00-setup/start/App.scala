package apothecary

import com.raquo.laminar.api.L.*

/** Exercise 00: if you can see this page and the greeting follows your typing, you're ready. */
object App:
  def view(): HtmlElement =
    val name = Var("")
    div(
      cls := "card",
      h2("Hello, apprentice 🧪"),
      div(
        cls := "row",
        label("Your name: "),
        input(typ := "text", placeholder := "Hermione", controlled(value <-- name, onInput.mapToValue --> name))
      ),
      p(
        child.text <-- name.signal.map(n =>
          if n.isBlank then "The shop awaits." else s"Welcome to Grimsby & Daughters, ${n.trim}!")
      )
    )
