(
  name = "plugins",
  cells = Vector(
    (
      id = "c1",
      kind = (
        Text = null
      ),
      source = "# The Apothecary — plugins demo\nThe mixer below is a Laminar widget from a separately linked Scala.js plugin (widgets/potions/ in the lab repo, built with `sake widget potions`). Its value is JSON decoded into `Rgb`; the swatch is a plugin *display* of a downstream value.",
      managedBy = null,
      widget = null,
      registeredUnder = null
    ),
    (
      id = "c2",
      kind = (
        Code = null
      ),
      source = "import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec\nimport notebook.kernel.{Display, Widgets}\n\ncase class Rgb(r: Int, g: Int, b: Int) derives ConfiguredJsonValueCodec\ncase class Potion(name: String, r: Int, g: Int, b: Int) derives ConfiguredJsonValueCodec\n\n// a plugin widget: downstream cells see `val mix: Rgb`\nval mixH = Widgets.custom[Rgb](\"mix\", \"potions/mixer\", \"{}\", Rgb(180, 40, 120))",
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
      registeredUnder = "e5bcf5fad9ee64aa"
    ),
    (
      id = "c4",
      kind = (
        Code = null
      ),
      source = "val brightness = (mix.r + mix.g + mix.b) / 3\nval name = if brightness > 160 then \"Elixir of Daylight\" else if mix.r > mix.b then \"Dragon's Blush\" else \"Draught of Midnight\"\n// a plugin display; the fallback (shown without the plugin) is the Potion record\nDisplay.extensionOf(\"potions/swatch\", Potion(name, mix.r, mix.g, mix.b))",
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
    )
  ),
  edges = Vector(
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
    )
  ),
  layouts = (
    notebook = Vector(
      "c1",
      "c2",
      "c2--mix",
      "c4",
      "c5"
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
      )
    )
  )
)