import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** The payload's schema: `{"label": "heat", "value": 42}` (label is optional). */
case class Reading(label: String = "value", value: Double) derives Json.ReadWriter

/** Capstone · chart (solution): a display that plots a cell's value as it changes.
 *
 *  Notebook cell (below a `heat` slider):
 *    Display.extension("chart/brew", s"""{"label": "heat", "value": $heat}""")
 *  Every time the cell re-runs, the notebook calls `update` with the new payload.
 */
object Main extends LaminarPlugin:
  def view(payload: Signal[String], ctx: PluginContext): HtmlElement =
    ctx.name match
      case "brew" => Shop(BrewChart(payload))
      case other  => div(s"chart: unknown display '$other'")

object BrewChart:
  val width = 360
  val height = 120
  val keep = 60

  /** SVG polyline points for `values`, scaled to fill the chart. */
  def points(values: Vector[Double]): String =
    if values.isEmpty then ""
    else
      val (low, high) = (values.min, values.max)
      val span = (high - low).max(1e-9)
      val step = if values.size > 1 then width.toDouble / (values.size - 1) else 0.0
      values.zipWithIndex
        .map((v, i) => f"${i * step}%.1f,${height - 6 - (v - low) / span * (height - 12)}%.1f")
        .mkString(" ")

  def apply(payload: Signal[String]): HtmlElement =
    val history = Var(Vector.empty[Double])
    val readings: Signal[Option[Reading]] = payload.map(p => scala.util.Try(Json.read[Reading](p)).toOption)
    val label = readings.map(_.fold("value")(_.label))

    div(
      cls := "card",
      readings --> { r => r.foreach(reading => history.update(h => (h :+ reading.value).takeRight(keep))) },
      h2(child.text <-- label, " over time"),
      svg.svg(
        svg.width := width.toString, svg.height := height.toString,
        svg.viewBox := s"0 0 $width $height",
        svg.rect(svg.width := "100%", svg.height := "100%", svg.fill := "#fbf8f2"),
        svg.polyline(
          svg.points <-- history.signal.map(points),
          svg.fill := "none", svg.stroke := "#6b3fa0", svg.strokeWidth := "3", svg.strokeLineJoin := "round"
        )
      ),
      p(cls := "note", child.text <-- history.signal.map(h =>
        h.lastOption.fold("waiting for the first value…")(last =>
          f"now $last%.1f · lowest ${h.min}%.1f · highest ${h.max}%.1f · ${h.size} readings")))
    )
