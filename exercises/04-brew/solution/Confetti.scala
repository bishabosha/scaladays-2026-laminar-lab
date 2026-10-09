package apothecary

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** A facade for canvas-confetti, imported straight from a CDN. */
object Confetti:
  @js.native
  @JSImport("https://esm.sh/canvas-confetti@1.9.3", JSImport.Default)
  private def confetti(options: js.Object): js.Any = js.native

  def fire(colours: List[String] = Nil): Unit =
    val options = js.Dynamic.literal(particleCount = 150, spread = 70, origin = js.Dynamic.literal(y = 0.7))
    if colours.nonEmpty then options.updateDynamic("colors")(js.Array(colours*))
    confetti(options)
