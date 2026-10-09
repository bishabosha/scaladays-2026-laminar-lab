package apothecary

import scala.util.{Failure, Success}

import com.raquo.laminar.api.L.*

/** Exercise 03 · The cellar (solution) */
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
  /** Shelves with a change the oracle hasn't confirmed yet. */
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

  /** Optimistic: change the shelf now, put it back if the oracle refuses. */
  def adjust(id: StockId, delta: Int): Unit =
    shelves.now().find(_.id == id).foreach { before =>
      val after = before.copy(quantity = before.quantity + delta)
      replace(after)
      saving.update(_ + id)
      oracle.save(after).onComplete { result =>
        saving.update(_ - id)
        result.failed.foreach { e =>
          replace(before)
          notice.set(Some(e.getMessage))
        }
      }
    }

  /** Optimistic too: the shelf vanishes now and comes back (in place) on refusal. */
  def remove(id: StockId): Unit =
    val before = shelves.now()
    before.find(_.id == id).foreach { stock =>
      shelves.update(_.filterNot(_.id == id))
      oracle.delete(id).failed.foreach { e =>
        val index = before.indexWhere(_.id == id)
        shelves.update(now => if now.exists(_.id == id) then now else now.patch(index, List(stock), 0))
        notice.set(Some(e.getMessage))
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
          ul(
            cls := "shelves",
            children <-- shelves.signal.split(_.id)((id, _, stock) => shelf(id, stock))
          )
      }
    )

  /** One row per shelf, created once and then fed updates. */
  def shelf(id: StockId, stock: Signal[Stock]): HtmlElement =
    li(
      cls := "shelf",
      cls("saving") <-- saving.signal.map(_.contains(id)),
      span(child.text <-- stock.map(_.ingredient.name)),
      button(cls := "quiet", "−", disabled <-- stock.map(_.quantity <= 0), onClick --> (_ => adjust(id, -1))),
      span(cls := "quantity", child.text <-- stock.map(_.quantity)),
      button(cls := "quiet", "+", onClick --> (_ => adjust(id, 1))),
      button(cls := "quiet", "🗑", title := "Clear the shelf", onClick --> (_ => remove(id)))
    )

  def search: HtmlElement =
    val query = Var("")
    val retry = EventBus[Unit]()
    val questions: EventStream[String] =
      EventStream.merge(query.signal.changes.debounce(300), retry.events.sample(query.signal))
    val answers: Signal[Option[Remote[List[Ingredient]]]] =
      questions
        .flatMapSwitch(q =>
          if q.isBlank then EventStream.fromValue(None)
          else Remote.consult(oracle.searchIngredients(q)).map(Some(_)))
        .startWith(None)

    div(
      cls := "field",
      input(typ := "search", placeholder := "Search the catalogue: newt, nettle…",
        controlled(value <-- query, onInput.mapToValue --> query)),
      child <-- answers.map {
        case None                         => emptyNode
        case Some(Remote.Loading)         => p(cls := "thinking", "The oracle is thinking…")
        case Some(Remote.Failed(why))     => p(cls := "rude", why, " ", button("Ask again", onClick.mapToUnit --> retry))
        case Some(Remote.Ready(Nil))      => p(cls := "note", "Nothing by that name in the catalogue.")
        case Some(Remote.Ready(found))    =>
          ul(cls := "results", found.map { ingredient =>
            li(
              button(cls := "quiet", "+ shelf", onClick --> (_ => stockUp(ingredient))),
              b(ingredient.name),
              span(cls := s"rarity ${ingredient.rarity}", ingredient.rarity.toString),
              span(cls := "note", ingredient.note)
            )
          })
      }
    )
