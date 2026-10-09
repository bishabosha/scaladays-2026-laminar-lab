package apothecary

/* From ScalaDays 2025 (bishabosha/scaladays-2025, laminar-form/lenses.scala).
 * `VarLenses(Potion.empty).name` is a `(updater: Observer[String], view: Signal[String])`. */

import VarLenses.WrappedProduct
import com.raquo.airstream.core.Observer
import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var

import scala.compiletime.ops.int.S
import scala.compiletime.constValue
import scala.collection.mutable
import scala.deriving.Mirror

object VarLenses:
  def apply[C](form: C)(using m: Mirror.ProductOf[C]): VarLenses[C] = new VarLenses(Var(form))
  private class WrappedProduct[C](
    private var base0: Product & C | Null,
    private var idx0: Int,
    private var value0: Any
  )
  extends Product:
    def as(base: C, idx: Int, value: Any): this.type = {
      base0 = base.asInstanceOf[Product & C]
      idx0 = idx
      value0 = value
      this
    }
    private def base1: C & Product = base0.asInstanceOf[C & Product]
    def canEqual(that: Any): Boolean = false
    def productElement(idx: Int): Any =
      if idx == idx0 then value0 else base1.productElement(idx)
    def productArity: Int = base1.productArity

class VarLenses[C](val baseVar: Var[C])(using m: Mirror.ProductOf[C]) extends Selectable:
  private val wrapper = WrappedProduct[C](null, -1, null)
  private val register = mutable.Map.empty[Int, Lens[Any]]
  private def copy(old: C, subIdx: Int, subValue: Any): C =
    m.fromProduct(wrapper.as(old, subIdx, subValue))
  def compute(idx: Int): Lens[Any] =
    register.getOrElseUpdate(idx,
      (
        updater = baseVar.updater((old, nu) => copy(old, idx, nu)),
        view = baseVar.signal.map(_.asInstanceOf[Product].productElement(idx))
      )
    )

  type FieldNames = NamedTuple.Names[NamedTuple.From[C]]
  type IndexOf[A, T <: Tuple, Acc <: Int] <: Int = T match
    case A *: _ => Acc
    case _ *: tail => IndexOf[A, tail, S[Acc]]
  type NameIndex[S <: String] = IndexOf[S, FieldNames, 0]
  type Lens[X] = (updater: Observer[X], view: Signal[X])
  type Fields = NamedTuple.Map[NamedTuple.From[C], Lens]
  inline def selectDynamic(name: String): Lens[Any] =
    compute(constValue[NameIndex[name.type]])
