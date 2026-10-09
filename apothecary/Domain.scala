package apothecary

/** Grimsby & Daughters' catalogue: what the oracle knows about. */

enum Rarity:
  case Common, Uncommon, Rare, Legendary

case class Ingredient(id: String, name: String, rarity: Rarity, note: String)

case class StockId(value: String)

/** A shelf in the cellar: how much of one ingredient the shop holds. */
case class Stock(id: StockId, ingredient: Ingredient, quantity: Int)

/** A recipe, as registered with the Guild of Apothecaries. */
case class Potion(
    name: String,
    doses: Int,
    ingredients: List[String],
    drinkable: Boolean,
    explosive: Boolean,
    licence: String
) derives Json.ReadWriter

object Potion:
  val empty = Potion(name = "", doses = 1, ingredients = Nil, drinkable = true, explosive = false, licence = "")

object Ingredients:
  private def i(name: String, rarity: Rarity, note: String) =
    Ingredient(name.toLowerCase.replaceAll("[^a-z]+", "-").stripSuffix("-"), name, rarity, note)

  import Rarity.*

  val all: List[Ingredient] = List(
    i("Moonwater", Common, "Collected at midnight. Tastes of nothing, loudly."),
    i("Nightshade", Uncommon, "Deadly. Use sparingly, or for enemies."),
    i("Fire salt", Uncommon, "Keep away from beards."),
    i("Dragon's breath", Legendary, "Requires a Guild licence. Do not inhale."),
    i("Eye of newt", Common, "The newt is fine, it has a spare."),
    i("Stinging nettle", Common, "Fresh from the shop's back garden."),
    i("Newt tail", Common, "Grows back. Ask the newt."),
    i("Mandrake root", Rare, "Screams when picked. Wear earmuffs."),
    i("Phoenix feather", Legendary, "Self-replacing, eventually."),
    i("Troll sweat", Uncommon, "Fresh troll sweat. Do not ask how."),
    i("Unicorn hair", Rare, "Shed naturally, never plucked."),
    i("Basilisk scale", Rare, "Do not make eye contact with the jar."),
    i("Bat wing", Common, "Ethically sourced from retired bats."),
    i("Toadstool", Common, "The red ones. Probably."),
    i("Wolfsbane", Uncommon, "Popular around the full moon."),
    i("Pixie dust", Uncommon, "Causes mild levitation and giggling."),
    i("Kraken ink", Rare, "Stains everything, including memories."),
    i("Griffin talon", Rare, "Half eagle, half lion, all paperwork."),
    i("Mermaid scale", Rare, "Smells faintly of the sea and of disappointment."),
    i("Goblin earwax", Common, "Cheap for a reason."),
    i("Bottled starlight", Legendary, "Only open at night."),
    i("Thunder egg", Uncommon, "Rumbles before a storm."),
    i("Frost lichen", Common, "Keeps the cellar cool."),
    i("Ghost pepper", Uncommon, "Spicy, and slightly haunted."),
    i("Witch hazel", Common, "Good for bruises and curses alike."),
    i("Sloth sigh", Rare, "Takes a while to bottle."),
    i("Yeti dandruff", Uncommon, "Like snow, but itchier."),
    i("Owl's whisper", Rare, "Mostly says 'who'."),
    i("Spider silk", Common, "Spun by the cellar's resident, Gerald."),
    i("Ember moss", Uncommon, "Warm to the touch, warmer to the tongue."),
    i("Bog myrtle", Common, "Keeps midges away and visitors too."),
    i("Salamander tail", Uncommon, "Fireproof, mostly."),
    i("Hag's thumbnail", Rare, "Donated. Allegedly."),
    i("Mermaid tears", Legendary, "Very hard to obtain. Mermaids rarely cry."),
    i("Quicksilver", Uncommon, "Runs away if you look at it."),
    i("Mugwort", Common, "For dreams, and for saying 'mugwort'."),
    i("Raven feather", Common, "The ravens want it back."),
    i("Silver dew", Uncommon, "Gathered from enchanted lawns."),
    i("Wyvern scale", Rare, "Like dragon, but cheaper and grumpier."),
    i("Nettle wine", Common, "For the apothecary, not the potion.")
  )

  def byName(name: String): Option[Ingredient] = all.find(_.name == name)

  val dragonsBreath: Ingredient = byName("Dragon's breath").get
