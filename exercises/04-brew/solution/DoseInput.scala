package apothecary

import com.raquo.laminar.api.L.*

/** A component: signals in, observers out, an element back. */
object DoseInput:
  def apply(value: Signal[Int], onChange: Observer[Int], max: Int, modifiers: Modifier[HtmlElement]*): HtmlElement =
    div(
      cls := "row",
      button(typ := "button", cls := "quiet", "−", onClick(_.sample(value).map(v => (v - 1).max(1))) --> onChange),
      span(cls := "quantity", child.text <-- value),
      button(typ := "button", cls := "quiet", "+", onClick(_.sample(value).map(v => (v + 1).min(max))) --> onChange),
      modifiers
    )
