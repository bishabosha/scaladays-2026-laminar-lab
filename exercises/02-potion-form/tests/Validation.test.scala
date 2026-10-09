package apothecary

class ValidationTest extends munit.FunSuite:
  val good = Potion("Draught of Daylight", doses = 3, ingredients = List("Moonwater"),
    drinkable = true, explosive = false, licence = "")

  def fields(p: Potion) = Validation.validate(p).map(_.field).toSet

  test("a sensible potion passes") {
    assertEquals(Validation.validate(good), Nil)
  }
  test("a name is required") {
    assertEquals(fields(good.copy(name = "   ")), Set("name"))
  }
  test("the ledger fits 40 letters") {
    assertEquals(fields(good.copy(name = "x" * 41)), Set("name"))
  }
  test("doses are between 1 and 12") {
    assertEquals(fields(good.copy(doses = 0)), Set("doses"))
    assertEquals(fields(good.copy(doses = 13)), Set("doses"))
    assertEquals(fields(good.copy(doses = 12)), Set.empty[String])
  }
  test("at least one ingredient") {
    assertEquals(fields(good.copy(ingredients = Nil)), Set("ingredients"))
  }
  test("not both drinkable and explosive") {
    assertEquals(fields(good.copy(explosive = true)), Set("explosive"))
    assertEquals(fields(good.copy(drinkable = false, explosive = true)), Set.empty[String])
  }
  test("dragon's breath needs a licence") {
    val fiery = good.copy(ingredients = List("Dragon's breath"), drinkable = false)
    assertEquals(fields(fiery), Set("licence"))
    assertEquals(fields(fiery.copy(licence = "DB-12")), Set("licence"))
    assertEquals(fields(fiery.copy(licence = "DB-1234")), Set.empty[String])
  }
  test("problems pile up") {
    assertEquals(fields(Potion.empty.copy(explosive = true)), Set("name", "ingredients", "explosive"))
  }
