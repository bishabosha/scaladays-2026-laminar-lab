package apothecary

import com.raquo.laminar.api.L.*
import notebook.plugin.*

/** The entry point of every exercise. You never edit this: write `App.view()` instead.
 *
 *  Each exercise links as a notebook.sc plugin bundle, so the same build runs on the exercise
 *  page (`sake ex …`), in a slide (`{{demo 01-cauldron/app}}`) and in a notebook.
 */
object Main extends LaminarPlugin:
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    Shop(App.view())
