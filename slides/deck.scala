//> using scala "3.8.3"
//> using options -experimental -preview
//> using repository ivy2local
//> using dep "io.github.bishabosha::tiger-site-gen-reveal:0.1.0-SNAPSHOT"

/* The Apothecary: deck host for the ScalaDays 2026 Laminar lab.
 *
 *   scala-cli run . -- dev              # http://127.0.0.1:8123/apothecary/
 *   scala-cli run . -- build --display  # static build in dist-display/
 */

import scala.language.experimental.modularity
import model.SiteMapSchema.auto.autoDerived
import live.LiveSite
import model.{SiteRoot, TemplateFunction, TemplateFunctions}
import revealTheme.{DeckPage, RevealTheme, SlideDeck, SlideLayout}
import scalatags.Text.all.*

/** `{{demo potions/mixer r=120 g=60 b=190}}` mounts kind `mixer` of the widget built from
 *  `widgets/potions/` (`sake widget potions`): the same bundle and contract as a notebook.sc
 *  plugin. Optional `key=value` pairs seed the widget's JSON value (numbers stay numbers).
 */
val demo =
  def jsonValue(raw: String) = raw.toDoubleOption.fold(ujson.Str(raw))(_ => ujson.Num(raw.toDouble))
  val render = (args: String) =>
    val kind :: pairs = args.trim.split("\\s+").toList: @unchecked
    require(kind.matches("[a-z0-9-]+/[a-z0-9-]+"), s"{{demo}} expects a <plugin>/<name> kind, got: '$args'")
    val seed = pairs.map { pair =>
      val (key, value) = pair.span(_ != '=')
      require(key.nonEmpty && value.length > 1, s"{{demo}} expects key=value pairs, got: '$pair'")
      key -> jsonValue(value.drop(1))
    }
    // One wrapper element, so the value line never becomes an extra grid column.
    div(cls := "laminar-demo-frame")(
      div(cls := "laminar-demo", data("kind") := kind,
        Option.when(seed.nonEmpty)(data("value") := ujson.write(ujson.Obj.from(seed))))(
        p(cls := "demo-pending", s"demo: $kind")
      ),
      tag("output")(cls := "demo-value")
    ).render
  TemplateFunction(render, render)

/** `{{exercise 02-potion-form}}` labels a hands-on step with its directory. */
val exercise =
  val render = (args: String) =>
    span(cls := "exercise-tag", "exercises/", args.trim).render
  TemplateFunction(render, render)

val labTheme = RevealTheme.withTemplates(
  RevealTheme.defaultTemplates ++ TemplateFunctions((demo = demo, exercise = exercise)),
  slideLayouts = Map(
    "title" -> SlideLayout("title-slide", Some("#2b1d3a")),
    "chapter" -> SlideLayout("dark-slide chapter-slide", Some("#2b1d3a")),
    "exercise" -> SlideLayout("exercise-slide")
  ),
  page = DeckPage(
    stylesheets = Seq("assets/apothecary.css"),
    moduleScripts = Seq("assets/demos.mjs")
  )
)

object LabDeck extends SlideDeck["apothecary"](labTheme)

val lab = LiveSite(LabDeck, RevealTheme.liveSettings(LabDeck.collection))(using SiteRoot.here)

/** `dev` (default), `build [--display]`, `watch`, `serve [--display] [--port N]`. */
@main def deck(args: String*): Unit = lab.main(args)
