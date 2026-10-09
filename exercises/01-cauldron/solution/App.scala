package apothecary

import com.raquo.laminar.api.L.*

/** Exercise 01 · The cauldron (solution)
 *
 *  1. Three sliders: moonwater, nightshade, fire salt (0–100 each). One is done for you.
 *  2. The cauldron's colour mixes from all three: see `Mix.colour` in apothecary/Mix.scala.
 *  3. Show "It's going to blow!" when fire salt is above 80.
 *  4. ★ A "Stir" button that adds 5 of everything (but never more than 100).
 */
object App:
  def view(): HtmlElement =
    val moonwater = Var(40)
    val nightshade = Var(20)
    val fireSalt = Var(10)

    val colour: Signal[String] =
      moonwater.signal.combineWith(nightshade.signal, fireSalt.signal).map(Mix.colour)

    def stir(amount: Var[Int]): Unit = amount.update(a => (a + 5).min(100))

    div(
      cls := "card",
      h2("The cauldron"),
      slider("Moonwater", moonwater),
      slider("Nightshade", nightshade),
      slider("Fire salt", fireSalt),
      div(
        cls := "row",
        div(cls := "cauldron", backgroundColor <-- colour),
        child.maybe <-- fireSalt.signal.map(f =>
          Option.when(f > 80)(span(cls := "cauldron-warning", "It's going to blow! 💥"))),
      ),
      button("Stir 🥄", onClick --> (_ => List(moonwater, nightshade, fireSalt).foreach(stir)))
    )

  /** A labelled 0–100 slider bound to `amount`. */
  def slider(label: String, amount: Var[Int]): HtmlElement =
    div(
      cls := "slider-row",
      span(label),
      input(
        typ := "range", minAttr := "0", maxAttr := "100",
        controlled(
          value <-- amount.signal.map(_.toString),
          onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> amount
        )
      ),
      span(child.text <-- amount.signal)
    )
