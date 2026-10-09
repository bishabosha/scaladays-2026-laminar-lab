package apothecary

import scala.util.{Failure, Success}

import com.raquo.laminar.api.L.*

/** Exercise 03 · The cellar
 *
 *  This version works, badly. Turn the oracle's "slowest" slider up and type "newt", then
 *  quickly "nettle": which answer wins?
 *
 *  1. Search: wait for a pause in typing (`debounce`), and only show the answer to the
 *     latest question (`flatMapSwitch` instead of `flatMapMerge`).
 *  2. Show loading / failed / ready for the search, with an "Ask again" button on failure.
 *  3. Shelves: render with `split(_.id)` so each row is created once (watch the
 *     focus and the row's identity in the browser's element inspector).
 *     Add a delete button: `oracle.delete(id)`.
 *  4. Optimistic updates: change the quantity at once, put it back if the oracle refuses
 *     (turn "refuses" up to test it). Show the refusal in `notice`.
 *  5. ★ Dim just the row being saved: track the ids in `saving`, use `cls("saving") <-- …`.
 */
object App:
  val oracle: Oracle = Oracle.grumpy

  def view(): HtmlElement =
    div(
      cls := "card",
      h2("The cellar"),
      Oracle.grumpy.panel,
      Cellar().view
    )

/** The cellar's state lives here, so search results can add to the shelves. */
class Cellar:
  import App.oracle

  val shelves: Var[List[Stock]] = Var(Nil)
  val loaded: Var[Remote[Unit]] = Var(Remote.Loading)
  /** Shelves with a change the oracle hasn't confirmed yet (for TODO 5). */
  val saving: Var[Set[StockId]] = Var(Set.empty)
  /** The oracle's latest refusal, for the notice at the top. */
  val notice: Var[Option[String]] = Var(None)

  def load(): Unit =
    loaded.set(Remote.Loading)
    oracle.inventory().onComplete {
      case Success(stock) => shelves.set(stock); loaded.set(Remote.Ready(()))
      case Failure(e)     => loaded.set(Remote.Failed(e.getMessage))
    }

  private def replace(stock: Stock): Unit =
    shelves.update(_.map(s => if s.id == stock.id then stock else s))

  /** Pessimistic: nothing changes until the oracle answers. TODO 4: make it optimistic. */
  def adjust(id: StockId, delta: Int): Unit =
    shelves.now().find(_.id == id).foreach { before =>
      oracle.save(before.copy(quantity = before.quantity + delta)).onComplete {
        case Success(saved) => replace(saved)
        case Failure(e)     => notice.set(Some(e.getMessage))
      }
    }

  /** A new shelf needs its id from the oracle, so this one waits for the answer. */
  def stockUp(ingredient: Ingredient): Unit =
    shelves.now().find(_.ingredient.id == ingredient.id) match
      case Some(shelf) => adjust(shelf.id, 1)
      case None =>
        oracle.save(Stock(StockId("new"), ingredient, 1)).onComplete {
          case Success(created) => shelves.update(_ :+ created)
          case Failure(e)       => notice.set(Some(e.getMessage))
        }

  def view: HtmlElement =
    div(
      onMountCallback(_ => load()),
      child.maybe <-- notice.signal.map(_.map(why =>
        p(cls := "rude", why, " ", button(cls := "quiet", "OK", onClick.mapTo(None) --> notice)))),
      div(cls := "row", search),
      h2("Shelves"),
      child <-- loaded.signal.map {
        case Remote.Loading      => p(cls := "thinking", "The oracle is counting jars…")
        case Remote.Failed(why)  => p(cls := "rude", why, " ", button("Ask again", onClick --> (_ => load())))
        case Remote.Ready(_)     =>
          // TODO 3: every change re-creates every row. Use `split(_.id)`.
          ul(cls := "shelves", children <-- shelves.signal.map(_.map(shelf)))
      }
    )

  def shelf(stock: Stock): HtmlElement =
    li(
      cls := "shelf",
      span(stock.ingredient.name),
      button(cls := "quiet", "−", disabled := stock.quantity <= 0, onClick --> (_ => adjust(stock.id, -1))),
      span(cls := "quantity", stock.quantity.toString),
      button(cls := "quiet", "+", onClick --> (_ => adjust(stock.id, 1)))
      // TODO 3: a delete button
    )

  def search: HtmlElement =
    val query = Var("")
    // TODO 1: every keystroke asks the oracle, and the slowest answer wins.
    val answers: Signal[List[Ingredient]] =
      query.signal.changes
        .flatMapMerge(q => EventStream.fromFuture(oracle.searchIngredients(q)))
        .startWith(Nil)

    div(
      cls := "field",
      input(typ := "search", placeholder := "Search the catalogue: newt, nettle…",
        controlled(value <-- query, onInput.mapToValue --> query)),
      // TODO 2: loading and failure (see `Remote.consult` in shared/Remote.scala)
      ul(cls := "results", children <-- answers.map(_.map { ingredient =>
        li(
          button(cls := "quiet", "+ shelf", onClick --> (_ => stockUp(ingredient))),
          b(ingredient.name),
          span(cls := s"rarity ${ingredient.rarity}", ingredient.rarity.toString),
          span(cls := "note", ingredient.note)
        )
      }))
    )
