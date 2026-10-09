package apothecary

import scala.concurrent.Future

import com.raquo.laminar.api.L.*

/** An answer from the oracle that may not have arrived yet, or may never. */
enum Remote[+A]:
  case Loading
  case Ready(value: A)
  case Failed(reason: String)

object Remote:
  /** Ask the oracle: emits `Loading` at once, then `Ready` or `Failed`. */
  def consult[A](question: => Future[A]): EventStream[Remote[A]] =
    EventStream.merge(
      EventStream.fromValue(Remote.Loading),
      EventStream.fromFuture(question.map(Remote.Ready(_)).recover { case e => Remote.Failed(e.getMessage) })
    )
