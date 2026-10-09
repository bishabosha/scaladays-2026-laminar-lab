(
  name = "apothecary",
  cells = Vector(
    (
      id = "c1",
      kind = (
        Text = null
      ),
      source = "# The Apothecary\nThe capstone notebook. Each section uses a Laminar widget from the lab's `widgets/` directory (built with `./sake widget <name>`); its value flows into the cells below it.\n\n## Mixer · `potions/mixer` and `potions/swatch`",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c2",
      kind = (
        Code = null
      ),
      source = "import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec\nimport notebook.kernel.{Display, Widgets}\n\ncase class Rgb(r: Int, g: Int, b: Int) derives ConfiguredJsonValueCodec\ncase class Swatch(name: String, r: Int, g: Int, b: Int) derives ConfiguredJsonValueCodec\n\n// a plugin widget: downstream cells see `val mix: Rgb`\nval mixH = Widgets.custom[Rgb](\"mix\", \"potions/mixer\", \"{}\", Rgb(180, 40, 120))",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c2--mix",
      kind = (
        Code = null
      ),
      source = "val mix: Cell_c2.Rgb = notebook.kernel.Widgets.fromJson[Cell_c2.Rgb](\"{\\\"r\\\":180,\\\"g\\\":40,\\\"b\\\":120}\")",
      managedBy = "c2",
      widget = (
        name = "mix",
        spec = (
          Custom = (
            kind = "potions/mixer",
            config = "{}",
            tpe = "Cell_c2.Rgb",
            default = "{\"r\":180,\"g\":40,\"b\":120}"
          )
        )
      ),
      registeredUnder = "555e7cc7cbb9c026"
    ),
    (
      id = "c4",
      kind = (
        Code = null
      ),
      source = "val brightness = (mix.r + mix.g + mix.b) / 3\nval name = if brightness > 160 then \"Elixir of Daylight\" else if mix.r > mix.b then \"Dragon's Blush\" else \"Draught of Midnight\"\n// a plugin display; the fallback (shown without the plugin) is the Swatch record\nDisplay.extensionOf(\"potions/swatch\", Swatch(name, mix.r, mix.g, mix.b))",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c5",
      kind = (
        Code = null
      ),
      source = "// raw JSON payload form, with the default fallback (the JSON text)\nDisplay.extension(\"potions/swatch\", \"\"\"{\"r\":40,\"g\":160,\"b\":90,\"name\":\"Tincture of Moss\"}\"\"\")",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c6",
      kind = (
        Text = null
      ),
      source = "## Recipe · `recipe/form`\nA validated form; its value is a `Potion`. Fill it in and press *Send to the notebook*.",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c7",
      kind = (
        Code = null
      ),
      source = "case class Potion(name: String, doses: Int, ingredients: List[String],\n    drinkable: Boolean, explosive: Boolean, licence: String) derives ConfiguredJsonValueCodec\n\n// downstream cells see `val recipe: Potion`\nval recipeH = Widgets.custom[Potion](\"recipe\", \"recipe/form\", \"{}\",\n  Potion(\"\", 1, Nil, drinkable = true, explosive = false, licence = \"\"))",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c7--recipe",
      kind = (
        Code = null
      ),
      source = "val recipe: Cell_c7.Potion = notebook.kernel.Widgets.fromJson[Cell_c7.Potion](\"{\\\"name\\\":\\\"\\\",\\\"doses\\\":1,\\\"ingredients\\\":[],\\\"drinkable\\\":true,\\\"explosive\\\":false,\\\"licence\\\":\\\"\\\"}\")",
      managedBy = "c7",
      widget = (
        name = "recipe",
        spec = (
          Custom = (
            kind = "recipe/form",
            config = "{}",
            tpe = "Cell_c7.Potion",
            default = "{\"name\":\"\",\"doses\":1,\"drinkable\":true,\"explosive\":false,\"licence\":\"\"}"
          )
        )
      ),
      registeredUnder = "bbe21e50785b046d"
    ),
    (
      id = "c8",
      kind = (
        Code = null
      ),
      source = "if recipe.name.isEmpty then \"Send a recipe from the form above\"\nelse s\"${recipe.name}: ${recipe.doses} doses of ${recipe.ingredients.mkString(\", \")}\"",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c9",
      kind = (
        Text = null
      ),
      source = "## Oracle · `oracle/picker`\nSearch the (grumpy) oracle and pick ingredients; the value is their names.",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c10",
      kind = (
        Code = null
      ),
      source = "import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec\nimport com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker\n\ngiven JsonValueCodec[List[String]] = JsonCodecMaker.make\n\n// downstream cells see `val picked: List[String]`\nval pickedH = Widgets.custom[List[String]](\"picked\", \"oracle/picker\", \"{}\", Nil)",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c10--picked",
      kind = (
        Code = null
      ),
      source = "val picked: scala.collection.immutable.List[scala.Predef.String] = notebook.kernel.Widgets.fromJson[scala.collection.immutable.List[scala.Predef.String]](\"[]\")",
      managedBy = "c10",
      widget = (
        name = "picked",
        spec = (
          Custom = (
            kind = "oracle/picker",
            config = "{}",
            tpe = "scala.collection.immutable.List[scala.Predef.String]",
            default = "[]"
          )
        )
      ),
      registeredUnder = "901fb8f90202a48d"
    ),
    (
      id = "c11",
      kind = (
        Code = null
      ),
      source = "if picked.isEmpty then \"Pick some ingredients above\"\nelse s\"${picked.size} ingredients: ${picked.mkString(\", \")}\"",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c12",
      kind = (
        Text = null
      ),
      source = "## Chart · `chart/brew`\nA plugin *display*: it plots the value every time the cell re-runs. Displays appear in the inspector: click the cell's `(result)` chip (or run it), then move the slider.",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c13",
      kind = (
        Code = null
      ),
      source = "val heatH = Widgets.slider(\"heat\", 0, 100, 40)",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c13--heat",
      kind = (
        Code = null
      ),
      source = "val heat: Int = 40",
      managedBy = "c13",
      widget = (
        name = "heat",
        spec = (
          IntSlider = (
            min = 0,
            max = 100,
            step = 1,
            default = 40
          )
        )
      ),
      registeredUnder = "7abc84b93b87aed0"
    ),
    (
      id = "c14",
      kind = (
        Code = null
      ),
      source = "Display.extension(\"chart/brew\", s\"\"\"{\"label\": \"heat\", \"value\": $heat}\"\"\")",
      managedBy = null,
      widget = null,
      registeredUnder = null
    )
  ),
  edges = Vector(
    (
      from = "c10--picked",
      to = "c10"
    ),
    (
      from = "c11",
      to = "c10--picked"
    ),
    (
      from = "c13--heat",
      to = "c13"
    ),
    (
      from = "c14",
      to = "c13--heat"
    ),
    (
      from = "c2--mix",
      to = "c2"
    ),
    (
      from = "c4",
      to = "c2"
    ),
    (
      from = "c4",
      to = "c2--mix"
    ),
    (
      from = "c7--recipe",
      to = "c7"
    ),
    (
      from = "c8",
      to = "c7"
    ),
    (
      from = "c8",
      to = "c7--recipe"
    )
  ),
  layouts = (
    notebook = Vector(
      "c1",
      "c2",
      "c2--mix",
      "c4",
      "c5",
      "c6",
      "c7",
      "c7--recipe",
      "c8",
      "c9",
      "c10",
      "c10--picked",
      "c11",
      "c12",
      "c13",
      "c13--heat",
      "c14"
    ),
    graph = Vector(
      (
        id = "c1",
        x = 40.0,
        y = 40.0
      ),
      (
        id = "c2",
        x = 100.0,
        y = 120.0
      ),
      (
        id = "c2--mix",
        x = 160.0,
        y = 200.0
      ),
      (
        id = "c4",
        x = 220.0,
        y = 280.0
      ),
      (
        id = "c5",
        x = 280.0,
        y = 360.0
      ),
      (
        id = "c6",
        x = 340.0,
        y = 440.0
      ),
      (
        id = "c7",
        x = 400.0,
        y = 520.0
      ),
      (
        id = "c8",
        x = 460.0,
        y = 600.0
      ),
      (
        id = "c9",
        x = 520.0,
        y = 680.0
      ),
      (
        id = "c10",
        x = 580.0,
        y = 760.0
      ),
      (
        id = "c11",
        x = 640.0,
        y = 840.0
      ),
      (
        id = "c12",
        x = 700.0,
        y = 920.0
      ),
      (
        id = "c13",
        x = 760.0,
        y = 1000.0
      ),
      (
        id = "c14",
        x = 820.0,
        y = 1080.0
      ),
      (
        id = "c13--heat",
        x = 880.0,
        y = 1160.0
      ),
      (
        id = "c7--recipe",
        x = 940.0,
        y = 1240.0
      ),
      (
        id = "c10--picked",
        x = 1000.0,
        y = 1320.0
      )
    )
  )
)