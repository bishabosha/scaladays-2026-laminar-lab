package apothecary

import scala.util.Try

import com.raquo.laminar.api.L.*
import notebook.plugin.*

/** Only JSON crosses between a widget and the notebook. A schema is a case class deriving
 *  `Json.ReadWriter`; its field names are the JSON keys, which matches what the notebook's
 *  jsoniter codecs (`derives ConfiguredJsonValueCodec`) read and write.
 *
 *  Unlike `upickle.default`, this writes fields that equal their default value, so the
 *  notebook never sees a missing field.
 */
object Json extends upickle.AttributeTagged:
  override def serializeDefaults: Boolean = true

  /** `json` as an `A`, or `fallback` when it doesn't fit (e.g. a fresh widget's "{}"). */
  def readOr[A: Reader](json: String, fallback: => A): A =
    Try(read[A](json)).getOrElse(fallback)

/** A notebook widget's value as an `A` instead of a JSON string. */
class Widget[A: Json.ReadWriter](bridge: WidgetBridge, fallback: => A):
  def value: A = Json.readOr(bridge.value(), fallback)
  def set(a: A): Unit = bridge.set(Json.write(a))
  /** The value, following changes made elsewhere (another tab, a kernel-side `handle.set`). */
  def signal: Signal[A] = bridge.signal.map(Json.readOr(_, fallback))
  /** Send every value it observes to the notebook. */
  def writer: Observer[A] = Observer(set)

extension (bridge: WidgetBridge)
  def as[A: Json.ReadWriter](fallback: => A): Widget[A] = Widget(bridge, fallback)
