import com.raquo.laminar.api.L.*
import notebook.plugin.*
import apothecary.*

/** The payload's schema: `{"label": "heat", "value": 42}` (label is optional). */
case class Reading(label: String = "value", value: Double) derives Json.ReadWriter

/** Capstone · chart: a display that plots a cell's value as it changes.
 *
 *  Done for you: collecting the history and drawing it.
 *  1. `points`: turn the history into SVG coordinates (x left to right, high values at the top).
 *  2. ★ Mark the latest reading with a circle, and label the lowest and highest.
 *  3. ★ Several series: payloads with different labels get their own colour.
 *  Build: ./sake widget chart WATCH=1 · answer: ./sake widget-solution chart
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
    // TODO 1: "x1,y1 x2,y2 …" for each value, scaled so the lowest is near the bottom
    //         (y close to `height`) and the highest near the top (y close to 0).
    values.zipWithIndex.map((v, i) => s"${i * 6},${height / 2}").mkString(" ")

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
