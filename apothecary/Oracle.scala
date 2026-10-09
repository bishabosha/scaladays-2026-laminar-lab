package apothecary

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.util.{Random, Try}

import com.raquo.laminar.api.L.*

/** For `Future.map` and friends in the exercises. */
given ExecutionContext = JSExecutionContext.queue

/** The oracle refused to answer. The message is suitable for showing to customers, sort of. */
class OracleRefused(message: String) extends Exception(message)

/** The shop's backend. Every answer takes a while, and some never come. */
trait Oracle:
  /** Ingredients whose name contains `query` (case-insensitive), best matches first, at most 8. */
  def searchIngredients(query: String): Future[List[Ingredient]]
  def inventory(): Future[List[Stock]]
  /** Create (unknown id) or replace a shelf. Fails for a negative quantity. */
  def save(stock: Stock): Future[Stock]
  def delete(id: StockId): Future[Unit]
  /** Register a recipe with the Guild; answers its registration number. */
  def register(potion: Potion): Future[String]

object Oracle:
  /** The in-browser oracle: random latency, sometimes refuses. One per page is plenty. */
  lazy val grumpy: GrumpyOracle = GrumpyOracle()

/** An oracle living in the browser. Its mood is adjustable while the page runs: see `panel`. */
class GrumpyOracle(random: Random = Random()) extends Oracle:
  /** Each answer takes between `minLatency` and `maxLatency` milliseconds. */
  val minLatency: Var[Int] = Var(100)
  val maxLatency: Var[Int] = Var(1500)
  /** The chance, between 0 and 1, that a consultation is refused. */
  val failureRate: Var[Double] = Var(0.1)
  /** Consultations not answered yet. */
  val inFlight: Var[Int] = Var(0)

  private var shelves: Vector[Stock] =
    Vector("Moonwater" -> 12, "Eye of newt" -> 30, "Fire salt" -> 4, "Mandrake root" -> 2, "Pixie dust" -> 7)
      .zipWithIndex
      .map { case ((name, quantity), n) => Stock(StockId(s"shelf-${n + 1}"), Ingredients.byName(name).get, quantity) }
  private var nextShelf = shelves.size + 1
  private var registered = Map.empty[String, String]

  private val rudeness = Vector(
    "The oracle is napping. Try again, quietly.",
    "The oracle says: ask me later, mortal.",
    "Your question bores the oracle.",
    "The crystal ball is foggy today.",
    "A raven ate your request.",
    "The oracle is on a tea break.",
    "The oracle foresaw this question and declines it."
  )

  /** Answer after a random delay, or refuse. `answer` runs when the oracle answers. */
  private def consult[A](answer: => A): Future[A] =
    val promise = Promise[A]()
    val (low, high) = (minLatency.now(), maxLatency.now())
    val delay = low + random.nextInt((high - low).max(0) + 1)
    inFlight.update(_ + 1)
    js.timers.setTimeout(delay.toDouble) {
      inFlight.update(_ - 1)
      if random.nextDouble() < failureRate.now() then
        promise.failure(OracleRefused(rudeness(random.nextInt(rudeness.size))))
      else promise.complete(Try(answer))
    }
    promise.future

  def searchIngredients(query: String): Future[List[Ingredient]] = consult {
    val q = query.trim.toLowerCase
    if q.isEmpty then Nil
    else
      Ingredients.all
        .filter(_.name.toLowerCase.contains(q))
        .sortBy(i => (!i.name.toLowerCase.startsWith(q), i.name))
        .take(8)
  }

  def inventory(): Future[List[Stock]] = consult(shelves.toList)

  def save(stock: Stock): Future[Stock] = consult {
    if stock.quantity < 0 then throw OracleRefused(s"Negative ${stock.ingredient.name}? Not in this universe.")
    val saved =
      if shelves.exists(_.id == stock.id) then stock
      else
        val created = stock.copy(id = StockId(s"shelf-$nextShelf"))
        nextShelf += 1
        created
    shelves = shelves.filterNot(_.id == saved.id) :+ saved
    saved
  }

  def delete(id: StockId): Future[Unit] = consult {
    shelves = shelves.filterNot(_.id == id)
  }

  def register(potion: Potion): Future[String] = consult {
    val key = potion.name.trim.toLowerCase
    if registered.contains(key) then throw OracleRefused(s"The Guild already knows a potion called ${potion.name.trim}.")
    val number = f"GUILD-${1000 + random.nextInt(9000)}%04d"
    registered += key -> number
    number
  }

  /** A small control panel: change the oracle's mood while your page runs. */
  def panel: HtmlElement =
    def percent(d: Double) = s"${math.round(d * 100)}%"
    div(
      cls := "oracle-panel",
      span(cls := "oracle-title", "🔮 Oracle"),
      label(
        "refuses ",
        input(
          typ := "range", minAttr := "0", maxAttr := "100",
          controlled(
            value <-- failureRate.signal.map(r => math.round(r * 100).toString),
            onInput.mapToValue.map(_.toIntOption.getOrElse(0) / 100.0) --> failureRate
          )
        ),
        child.text <-- failureRate.signal.map(percent)
      ),
      label(
        "slowest ",
        input(
          typ := "range", minAttr := "100", maxAttr := "5000", stepAttr := "100",
          controlled(
            value <-- maxLatency.signal.map(_.toString),
            onInput.mapToValue.map(_.toIntOption.getOrElse(1500)) --> maxLatency
          )
        ),
        child.text <-- maxLatency.signal.map(ms => s"${ms} ms")
      ),
      span(
        cls := "oracle-busy",
        cls("active") <-- inFlight.signal.map(_ > 0),
        child.text <-- inFlight.signal.map(n => if n == 0 then "idle" else s"thinking about $n…")
      )
    )
