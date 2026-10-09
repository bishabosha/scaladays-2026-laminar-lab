package apothecary

/** One thing wrong with the form, attached to the field that shows it. */
case class Problem(field: String, message: String)

object Validation:
  val maxDoses = 12

  /** The Guild's rules. A pure function: `sake test 02-potion-form` checks it.
   *
   *  - name: not blank, at most 40 letters             (field "name")
   *  - doses: between 1 and maxDoses                   (field "doses")
   *  - at least one ingredient                         (field "ingredients")
   *  - not both drinkable and explosive                (field "explosive")
   *  - with Dragon's breath, a licence like DB-1234    (field "licence")
   */
  def validate(p: Potion): List[Problem] =
    List(
      Option.when(p.name.isBlank)(Problem("name", "Every potion needs a name"))
      // TODO: the other rules
    ).flatten
