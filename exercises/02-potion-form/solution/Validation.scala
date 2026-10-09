package apothecary

/** One thing wrong with the form, attached to the field that shows it. */
case class Problem(field: String, message: String)

object Validation:
  val maxDoses = 12
  private val Licence = """DB-\d{4}""".r

  /** The Guild's rules. A pure function: see tests/Validation.test.scala. */
  def validate(p: Potion): List[Problem] =
    List(
      Option.when(p.name.isBlank)(Problem("name", "Every potion needs a name")),
      Option.when(p.name.trim.length > 40)(Problem("name", "The Guild's ledger only fits 40 letters")),
      Option.when(p.doses < 1 || p.doses > maxDoses)(Problem("doses", s"Brew between 1 and $maxDoses doses")),
      Option.when(p.ingredients.isEmpty)(Problem("ingredients", "A potion needs at least one ingredient")),
      Option.when(p.drinkable && p.explosive)(
        Problem("explosive", "A potion may not be both drinkable and explosive")),
      Option.when(p.ingredients.contains(Ingredients.dragonsBreath.name) && !Licence.matches(p.licence.trim))(
        Problem("licence", "Dragon's breath needs a Guild licence number, like DB-1234"))
    ).flatten
