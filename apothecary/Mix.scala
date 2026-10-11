package apothecary

object Mix:
  /** The cauldron's colour. Moonwater is blue, nightshade magenta and fire salt yellow, so
   *  moonwater and fire salt brew green, and nightshade and fire salt crimson. More of
   *  everything glows brighter; nothing at all is murky grey.
   */
  def colour(moonwater: Int, nightshade: Int, fireSalt: Int): String =
    val total = (moonwater + nightshade + fireSalt).toDouble
    if total == 0 then "hsl(60, 8%, 30%)"
    else
      // Average the three colours, then keep only the hue, so that mixes stay bright.
      def channel(blue: Int, magenta: Int, yellow: Int) =
        (moonwater * blue + nightshade * magenta + fireSalt * yellow) / total
      val (r, g, b) = (channel(0, 200, 255), channel(110, 0, 200), channel(255, 255, 0))
      val (max, min) = (r max g max b, r min g min b)
      val chroma = max - min
      val hue =
        if chroma == 0 then 0.0
        else if max == r then 60 * (((g - b) / chroma) % 6)
        else if max == g then 60 * ((b - r) / chroma + 2)
        else 60 * ((r - g) / chroma + 4)
      val saturation = 45 + 45 * chroma / 255
      val lightness = 32 + 28 * total / 300
      f"hsl(${(hue + 360) % 360}%.0f, $saturation%.0f%%, $lightness%.0f%%)"
