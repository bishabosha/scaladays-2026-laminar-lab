//> using scala 3.9.0
//> using jvm 17
//> using dep io.github.bishabosha::flagged::1.0.0

/* sake: a small, portable command runner (in the spirit of `just`) for Sakefiles.
 *
 * Runs on any platform with scala-cli: no shell is involved. Commands start directly
 * (on Windows, `./mill` finds `mill.bat` and `npm` finds `npm.cmd` via PATHEXT), and
 * file operations are built in. There is no file tracking: a recipe always runs.
 *
 * Sakefile syntax:
 *
 *   # A comment. A comment directly above a recipe is its description in `sake --list`.
 *   out := slides/public/assets/widgets       variable, used as {{out}}
 *
 *   [dir: modules/notebook.sc]                 run the next recipe's commands in this directory
 *   recipe NAME WATCH="": dep1 dep2            parameters (with optional defaults), then dependencies
 *       command arg "quoted arg" {{NAME}}      body lines are indented; each line is one command
 *       @command                               `@` hides the echoed command line
 *       -command                               `-` carries on if the command fails
 *       {{WATCH ? --watch}}                    expands to `--watch` only when WATCH is non-empty
 *
 * Built-in variables: {{root}} (the Sakefile's directory), {{os}} (windows, mac or linux).
 * Built-in commands: echo, mkdir, copy, remove, fail, fail-if-exists, fail-if-missing, sake (run a recipe),
 *   `for X in dirs <path> [except a b]: <command using {{X}}>`, and
 *   `remove` also takes a pattern in the last part of a path (`static/reveal_*`), and
 *   `serve <dir> <port> [page]`: a static file server that runs alongside the following
 *   commands; once they finish, sake keeps serving until Ctrl-C.
 *
 * Usage: sake [--list] [--dry-run] [recipe [args...]]...   args are positional or NAME=value
 *   (`sake --help` for details; command-line parsing is flagged's)
 *
 * The launchers (`sake`, `sake.bat`) compile this file with Scala Native when clang is available,
 * and otherwise run it on the JVM, so it sticks to APIs both platforms provide (no java.time, no
 * com.sun.net.httpserver, no System.console, no regex lookaround).
 */

import java.io.{BufferedReader, InputStreamReader}
import java.net.{InetAddress, ServerSocket, Socket, URLDecoder}
import java.nio.charset.StandardCharsets.ISO_8859_1
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util.concurrent.CountDownLatch
import scala.jdk.CollectionConverters.*
import scala.util.control.NoStackTrace

import flagged.*

case class SakeError(message: String, exitCode: Int = 1) extends Exception(message), NoStackTrace

case class Param(name: String, default: Option[String])
case class Line(text: String, number: Int)
case class Recipe(name: String, doc: Option[String], dir: Option[String], params: List[Param],
    deps: List[String], body: List[Line], line: Int):
  def usage = (name :: params.map(p => p.default.fold(p.name)(d => s"""${p.name}="$d""""))).mkString(" ")

case class Sakefile(path: Path, vars: Map[String, String], recipes: List[Recipe]):
  def root: Path = path.getParent
  def recipe(name: String): Recipe =
    recipes.find(_.name == name).getOrElse(throw SakeError(s"unknown recipe '$name'${Suggest.hint(name, recipes.map(_.name))}"))

/** Close names for a mistyped or partly typed recipe: those it starts, then those (or one of
 *  their words) within a few edits.
 */
object Suggest:
  def hint(name: String, names: List[String]): String =
    matches(name, names) match
      case Nil => " (try `sake --list`)"
      case List(one) => s" (did you mean '$one'?)"
      case many => s" (did you mean one of: ${many.mkString(", ")}?)"

  def matches(name: String, names: List[String]): List[String] =
    val typed = name.toLowerCase
    val starts = names.filter(_.toLowerCase.startsWith(typed))
    val limit = (typed.length / 3).max(1)
    // A whole name, or one of its words: `bloggg` finds blog-build, `demos` finds ex-demos.
    // Ties go to the closer whole name: `widgt` lists widget before reset-widget.
    def score(n: String) =
      val whole = distance(typed, n.toLowerCase)
      ((whole :: n.toLowerCase.split('-').toList.filter(_.length >= 3).map(distance(typed, _))).min, whole)
    val close = names
      .map(n => n -> score(n))
      .filter((n, d) => d._1 <= limit && !starts.contains(n))
      .sortBy(_._2).map(_._1)
    (starts ++ close).take(6)

  /** Edits (insert, delete, substitute or swap neighbours) to turn `a` into `b`. */
  def distance(a: String, b: String): Int =
    val d = Array.tabulate(a.length + 1, b.length + 1)((i, j) => if i == 0 then j else if j == 0 then i else 0)
    for i <- 1 to a.length; j <- 1 to b.length do
      val cost = if a(i - 1) == b(j - 1) then 0 else 1
      d(i)(j) = (d(i - 1)(j) + 1).min(d(i)(j - 1) + 1).min(d(i - 1)(j - 1) + cost)
      if i > 1 && j > 1 && a(i - 1) == b(j - 2) && a(i - 2) == b(j - 1) then d(i)(j) = d(i)(j).min(d(i - 2)(j - 2) + 1)
    d(a.length)(b.length)

object Parse:
  private val Variable = """([A-Za-z_][\w-]*)\s*:=\s*(.*)""".r
  private val Attribute = """\[dir:\s*(.+?)\s*\]""".r
  private val Header = """([A-Za-z_][\w-]*)((?:\s+[A-Za-z_]\w*(?:="[^"]*")?)*)\s*:(.*)""".r
  private val ParamPattern = """([A-Za-z_]\w*)(?:="([^"]*)")?""".r

  def apply(path: Path): Sakefile =
    val lines = Files.readAllLines(path).asScala.toList.map(_.stripSuffix("\r")).zipWithIndex
    def fail(n: Int, msg: String) = throw SakeError(s"${path.getFileName}:${n + 1}: $msg")
    var vars = Map.empty[String, String]
    val recipes = List.newBuilder[Recipe]
    var doc: Option[String] = None
    var dir: Option[String] = None
    var rest = lines
    while rest.nonEmpty do
      val (raw, n) = rest.head
      rest = rest.tail
      raw.trim match
        case "" => doc = None
        case c if c.startsWith("#") => doc = Some(c.stripPrefix("#").trim)
        case Attribute(d) => dir = Some(d)
        case _ if raw.head.isWhitespace => fail(n, "indented line outside a recipe")
        case Variable(name, value) => vars += name -> unquote(value.trim)
        case Header(name, params, deps) =>
          val (body, after) = rest.span((l, _) => l.isEmpty || l.head.isWhitespace)
          rest = after
          val ps = ParamPattern.findAllMatchIn(params).map(m => Param(m.group(1), Option(m.group(2)))).toList
          recipes += Recipe(name, doc, dir, ps, deps.trim.split("\\s+").filter(_.nonEmpty).toList,
            body.collect { case (l, i) if l.trim.nonEmpty && !l.trim.startsWith("#") => Line(l.trim, i + 1) }, n + 1)
          doc = None
          dir = None
        case other => fail(n, s"cannot parse: $other")
    val all = recipes.result()
    all.groupBy(_.name).collectFirst { case (name, rs) if rs.size > 1 => throw SakeError(s"recipe '$name' is defined twice") }
    for r <- all; d <- r.deps if !all.exists(_.name == d) do
      throw SakeError(s"${path.getFileName}:${r.line}: recipe '${r.name}' depends on unknown recipe '$d'${Suggest.hint(d, all.map(_.name))}")
    Sakefile(path, vars, all)

  def unquote(s: String) =
    if s.length >= 2 && (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")) then s.slice(1, s.length - 1)
    else s

  /** Split a command line into words, honouring quotes; `{{ ... }}` never splits. */
  def words(line: String): List[String] =
    val out = List.newBuilder[String]
    val cur = StringBuilder()
    var started = false
    var quote: Char = 0
    var i = 0
    while i < line.length do
      val c = line(i)
      if quote != 0 then
        if c == quote then quote = 0 else cur += c
      else if line.startsWith("{{", i) then
        val end = line.indexOf("}}", i)
        if end < 0 then throw SakeError(s"unclosed {{ in: $line")
        cur ++= line.substring(i, end + 2); started = true; i = end + 1
      else if c == '"' || c == '\'' then { quote = c; started = true }
      else if c.isWhitespace then
        if started then { out += cur.result(); cur.clear(); started = false }
      else { cur += c; started = true }
      i += 1
    if quote != 0 then throw SakeError(s"unclosed quote in: $line")
    if started then out += cur.result()
    out.result()

object Platform:
  val name: String = sys.env.get("SAKE_OS").getOrElse {
    val os = System.getProperty("os.name").toLowerCase
    if os.startsWith("windows") then "windows" else if os.contains("mac") then "mac" else "linux"
  }
  val windows = name == "windows"
  /** Colour the echoed commands when the launcher found a terminal (`SAKE_COLOR=1`, set by
   *  `./sake` for an interactive stderr); `NO_COLOR` turns it off.
   */
  val colour: Boolean = sys.env.get("SAKE_COLOR").contains("1") && !sys.env.contains("NO_COLOR")
  private val exts =
    if windows then sys.env.getOrElse("PATHEXT", ".COM;.EXE;.BAT;.CMD").toLowerCase.split(';').toList.filter(_.nonEmpty)
    else Nil

  private def candidates(base: Path): List[Path] =
    exts.map(e => base.resolveSibling(s"${base.getFileName}$e")) :+ base

  private def runnable(p: Path) = Files.isRegularFile(p) && (windows || Files.isExecutable(p))

  /** The argv that starts `command`: local paths resolve against `cwd`, bare names on PATH. */
  def argv(command: String, args: List[String], cwd: Path): List[String] =
    val found =
      if command.contains('/') || command.contains('\\') then candidates(cwd.resolve(command).normalize).find(runnable)
      else
        val path = sys.env.getOrElse("PATH", sys.env.getOrElse("Path", "")).split(java.io.File.pathSeparator).toList
        path.filter(_.nonEmpty).iterator.flatMap(d => candidates(Paths.get(d).resolve(command))).find(runnable)
    found match
      case Some(p) if windows && Seq(".bat", ".cmd").exists(p.toString.toLowerCase.endsWith) =>
        "cmd.exe" :: "/c" :: p.toString :: args
      case Some(p) => p.toString :: args
      case None => throw SakeError(s"command not found: $command")

class Runner(file: Sakefile, dryRun: Boolean):
  private var done = Set.empty[String]

  private def note(text: String): Unit =
    System.err.println(if Platform.colour then s"\u001b[1;35m$text\u001b[0m" else text)

  /** Bind `args` (positional, or NAME=value) to the recipe's parameters. */
  def bind(recipe: Recipe, args: List[String]): Map[String, String] =
    val (named, positional) = args.partition(a => recipe.params.exists(p => a.startsWith(s"${p.name}=")))
    if positional.size > recipe.params.size then throw SakeError(s"too many arguments; usage: sake ${recipe.usage}")
    val supplied = recipe.params.zip(positional).map((p, v) => p.name -> v).toMap ++
      named.map(a => a.span(_ != '=')).map((k, v) => k -> v.drop(1))
    recipe.params.map { p =>
      p.name -> supplied.get(p.name).orElse(p.default).getOrElse(throw SakeError(s"missing ${p.name}; usage: sake ${recipe.usage}"))
    }.toMap

  /** Run a recipe from the command line (or `sake` in a body): dependencies once, then the body. */
  def run(name: String, args: List[String]): Unit =
    val recipe = file.recipe(name)
    val base = Map("root" -> file.root.toString, "os" -> Platform.name) ++ file.vars
    // Defaults may refer to variables: `FROM="{{solutions}}"`.
    val values = bind(recipe, args).map((k, v) => k -> interpolate(v, base))
    for d <- recipe.deps if !done(d) do run(d, Nil)
    done += name
    val scope = base ++ values
    val cwd = recipe.dir.fold(file.root)(d => file.root.resolve(interpolate(d, scope)).normalize)
    for line <- recipe.body do
      try execute(line.text, scope, cwd)
      catch case e: SakeError if !e.message.startsWith("recipe ") =>
        throw SakeError(s"recipe '$name' failed at ${file.path.getFileName}:${line.number}: ${e.message}", e.exitCode)

  private val Expr = """\{\{\s*([A-Za-z_][\w-]*)\s*(?:\?\s*(.*?))?\s*\}\}""".r

  def interpolate(word: String, scope: Map[String, String]): String =
    Expr.replaceAllIn(word, m =>
      val value = scope.getOrElse(m.group(1), throw SakeError(s"unknown variable {{${m.group(1)}}}"))
      java.util.regex.Matcher.quoteReplacement(Option(m.group(2)).fold(value)(t => if value.nonEmpty then t else "")))

  private val ForDirs = """for\s+([A-Za-z_]\w*)\s+in\s+dirs\s+(\S+)(?:\s+except\s+([^:]+))?\s*:\s*(.+)""".r

  private def execute(raw: String, scope: Map[String, String], cwd: Path): Unit =
    val prefix = raw.takeWhile(c => c == '@' || c == '-')
    val quiet = prefix.contains('@')
    val text = raw.drop(prefix.length).trim
    if prefix.contains('-') then
      try execute(text, scope, cwd, quiet)
      catch case e: SakeError => note(s"  (ignored: ${e.message})")
    else execute(text, scope, cwd, quiet)

  private def execute(text: String, scope: Map[String, String], cwd: Path, quiet: Boolean): Unit =
    text match
      case ForDirs(v, dir, except, command) =>
        val base = cwd.resolve(interpolate(dir, scope))
        val skip = Option(except).toList.flatMap(_.trim.split("\\s+"))
        val dirs = Files.list(base).iterator.asScala.filter(Files.isDirectory(_)).map(_.getFileName.toString)
          .filterNot(d => d.startsWith(".") || skip.contains(d)).toList.sorted
        for d <- dirs do execute(command, scope + (v -> d), cwd, quiet)
      case _ =>
        // A word that is a lone {{X ? text}} disappears when X is empty.
        val words = Parse.words(text).flatMap { w =>
          val v = interpolate(w, scope)
          if v.isEmpty && Expr.matches(w) then Nil else List(v)
        }
        if words.nonEmpty then
          if !quiet && words.head != "echo" && words.head != "sake" then note(s"$$ ${words.map(w => if w.isEmpty || w.exists(_.isWhitespace) then s"\"$w\"" else w).mkString(" ")}")
          builtin(words.head, words.tail, cwd)

  private def builtin(command: String, args: List[String], cwd: Path): Unit =
    def path(p: String) = cwd.resolve(p).normalize
    def arity(n: Int) = if args.size != n then throw SakeError(s"$command expects $n argument(s), got: ${args.mkString(" ")}")
    command match
      case "echo" => println(args.mkString(" "))
      case "fail" => throw SakeError(args.mkString(" "))
      case "fail-if-exists" =>
        if args.isEmpty then arity(1)
        if Files.exists(path(args.head)) then throw SakeError(if args.size > 1 then args.tail.mkString(" ") else s"${args.head} already exists")
      case "fail-if-missing" =>
        if args.isEmpty then arity(1)
        if !Files.exists(path(args.head)) then throw SakeError(if args.size > 1 then args.tail.mkString(" ") else s"${args.head} does not exist")
      case "sake" =>
        if args.isEmpty then throw SakeError("sake expects a recipe name")
        run(args.head, args.tail)
      case "mkdir" | "remove" | "copy" | "serve" if dryRun => ()
      case "serve" =>
        if args.size < 2 || args.size > 3 then throw SakeError("serve expects <dir> <port> [page]")
        val port = args(1).toIntOption.getOrElse(throw SakeError(s"serve: not a port: ${args(1)}"))
        Serve.start(path(args(0)), port, args.lift(2).getOrElse("/"))
      case "mkdir" => args.foreach(a => Files.createDirectories(path(a)))
      case "remove" => args.flatMap(matching(cwd, _)).foreach(deleteRecursively)
      case "copy" =>
        arity(2)
        copyRecursively(path(args(0)), path(args(1)))
      case _ =>
        val argv = Platform.argv(command, args, cwd)
        if dryRun then { note(s"  runs ${argv.mkString(" ")}  (in $cwd)"); return }
        val code = ProcessBuilder(argv.asJava).directory(cwd.toFile).inheritIO().start().waitFor()
        if code != 0 then throw SakeError(s"$command exited with code $code", code)

  /** `a/b/name_*`: what the pattern in the last part of a path names (`*` any run, `?` one
   *  character). Split before making a Path, since Windows paths cannot contain `*`.
   */
  private def matching(cwd: Path, arg: String): List[Path] =
    val cut = arg.lastIndexWhere(c => c == '/' || c == '\\')
    val (dir, name) = (if cut < 0 then "." else arg.take(cut).ifEmpty("/"), arg.drop(cut + 1))
    if !name.exists(c => c == '*' || c == '?') then List(cwd.resolve(arg).normalize)
    else
      val pattern = name.map {
        case '*' => ".*"
        case '?' => "."
        case c => java.util.regex.Pattern.quote(c.toString)
      }.mkString.r
      val parent = cwd.resolve(dir).normalize
      if !Files.isDirectory(parent) then Nil
      else Files.list(parent).iterator.asScala.toList.filter(f => pattern.matches(f.getFileName.toString)).sorted

  extension (s: String) private def ifEmpty(other: String) = if s.isEmpty then other else s

  private def deleteRecursively(p: Path): Unit =
    if Files.isDirectory(p) && !Files.isSymbolicLink(p) then Files.list(p).iterator.asScala.toList.foreach(deleteRecursively)
    Files.deleteIfExists(p)

  private def copyRecursively(from: Path, to: Path): Unit =
    if !Files.exists(from) then throw SakeError(s"copy: $from does not exist")
    if Files.isDirectory(from) then
      Files.walk(from).iterator.asScala.toList.foreach { src =>
        val dst = to.resolve(from.relativize(src).toString)
        if Files.isDirectory(src) then Files.createDirectories(dst)
        else Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
      }
    else
      Files.createDirectories(to.toAbsolutePath.getParent)
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)

/** A static file server for local development: no caching, Last-Modified for reload polling. */
/** A static file server for local development: no caching, Last-Modified for reload polling.
 *  Plain sockets, so it works the same on the JVM and with Scala Native.
 */
object Serve:
  @volatile private var running = 0
  private val types = Map(
    "html" -> "text/html; charset=utf-8", "js" -> "text/javascript; charset=utf-8",
    "mjs" -> "text/javascript; charset=utf-8", "css" -> "text/css; charset=utf-8",
    "json" -> "application/json", "map" -> "application/json", "svg" -> "image/svg+xml",
    "png" -> "image/png", "jpg" -> "image/jpeg", "woff2" -> "font/woff2", "txt" -> "text/plain; charset=utf-8")

  def start(root: Path, port: Int, page: String): Unit =
    val base = root.toAbsolutePath.normalize
    if !Files.isDirectory(base) then throw SakeError(s"serve: not a directory: $root")
    val server =
      try ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
      catch case _: java.net.BindException => throw SakeError(s"serve: port $port is in use (is another `sake` still running?)")
    daemon {
      while true do
        val socket = server.accept()
        daemon(respond(base, socket))
    }
    running += 1
    System.err.println(s"serving $root at http://127.0.0.1:$port${if page.startsWith("/") then page else "/" + page}")

  private def daemon(body: => Unit): Unit =
    val thread = Thread(() => body)
    thread.setDaemon(true)
    thread.start()

  /** One request per connection: `GET` or `HEAD` of a file under `base`. */
  private def respond(base: Path, socket: Socket): Unit =
    try
      val in = BufferedReader(InputStreamReader(socket.getInputStream, ISO_8859_1))
      val requestLine = Option(in.readLine()).getOrElse("")
      while Option(in.readLine()).exists(_.nonEmpty) do () // skip the request headers
      val out = socket.getOutputStream
      def send(status: String, headers: Seq[(String, String)], body: Array[Byte]): Unit =
        val head = (s"HTTP/1.1 $status" +: (headers :+ ("Connection" -> "close")).map((k, v) => s"$k: $v"))
          .mkString("", "\r\n", "\r\n\r\n")
        out.write(head.getBytes(ISO_8859_1))
        if body.nonEmpty then out.write(body) // Scala Native's sockets reject empty writes
        out.flush()
      requestLine.split(' ') match
        case Array(method @ ("GET" | "HEAD"), target, _*) =>
          val rawPath = target.takeWhile(c => c != '?' && c != '#')
          val rel = URLDecoder.decode(rawPath, "UTF-8").stripPrefix("/")
          val resolved = base.resolve(rel).normalize
          val file = if Files.isDirectory(resolved) then resolved.resolve("index.html") else resolved
          if !file.startsWith(base) || !Files.isRegularFile(file) || file.getFileName.toString.startsWith(".") then
            send("404 Not Found", Seq("Content-Length" -> "0"), Array.emptyByteArray)
          else
            val ext = file.getFileName.toString.split('.').last.toLowerCase
            val bytes = Files.readAllBytes(file)
            send("200 OK", Seq(
              "Content-Type" -> types.getOrElse(ext, "application/octet-stream"),
              "Content-Length" -> bytes.length.toString,
              "Cache-Control" -> "no-store",
              "Last-Modified" -> httpDate(Files.getLastModifiedTime(file).toMillis)
            ), if method == "HEAD" then Array.emptyByteArray else bytes)
        case _ => send("405 Method Not Allowed", Seq("Content-Length" -> "0"), Array.emptyByteArray)
    catch case _: java.io.IOException => () // the browser went away
    finally socket.close()

  /** RFC 1123 date, e.g. `Fri, 09 Oct 2026 10:32:59 GMT` (no java.time on Scala Native). */
  def httpDate(millis: Long): String =
    val days = Math.floorDiv(millis, 86400000L)
    val secs = Math.floorMod(millis, 86400000L) / 1000
    // Civil date from days since 1970-01-01 (Howard Hinnant's algorithm).
    val z = days + 719468
    val era = Math.floorDiv(z, 146097)
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = doy - (153 * mp + 2) / 5 + 1
    val month = if mp < 10 then mp + 3 else mp - 9
    val year = yoe + era * 400 + (if month <= 2 then 1 else 0)
    val weekday = Seq("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed")(Math.floorMod(days, 7L).toInt)
    val monthName = Seq("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")(month.toInt - 1)
    f"$weekday, $day%02d $monthName $year ${secs / 3600}%02d:${secs / 60 % 60}%02d:${secs % 60}%02d GMT"

  /** Keep serving after the recipe's commands finish, until the process is interrupted. */
  def awaitIfRunning(): Unit =
    if running > 0 then
      System.err.println("still serving; press Ctrl-C to stop")
      CountDownLatch(1).await()

def find(start: Path): Path =
  Iterator.iterate(start.toAbsolutePath.normalize)(_.getParent).takeWhile(_ != null)
    .map(_.resolve("Sakefile")).find(Files.isRegularFile(_))
    .getOrElse(throw SakeError(s"no Sakefile in $start or its parents"))

def list(file: Sakefile): Unit =
  println("Recipes:")
  val width = file.recipes.map(_.usage.length).maxOption.getOrElse(0)
  for r <- file.recipes do println(s"  ${r.usage.padTo(width, ' ')}${r.doc.fold("")(d => s"  # $d")}")

/** Split `a x y b z` into recipe invocations: each recipe takes up to as many arguments as it
 *  has parameters, but stops early at another recipe's name once its remaining parameters
 *  all have defaults.
 */
def invocations(file: Sakefile, args: List[String]): List[(String, List[String])] =
  args match
    case Nil => Nil
    case name :: rest =>
      val params = file.recipe(name).params
      def optionalFrom(i: Int) = params.drop(i).forall(_.default.isDefined)
      val taken = rest.zipWithIndex
        .takeWhile((a, i) => i < params.size && !(file.recipes.exists(_.name == a) && optionalFrom(i)))
        .map(_._1)
      (name, taken) :: invocations(file, rest.drop(taken.size))

@cmd(name = "sake", help = """Run recipes from the nearest Sakefile (in this directory or a parent).
With no recipe, lists them. Each recipe takes its arguments positionally or as NAME=value,
e.g. `sake widget potions WATCH=1`; recipe arguments that start with '-' go after `--`.""")
case class SakeArgs(
    @opt(short = 'l', help = "List the Sakefile's recipes") list: Boolean = false,
    @opt(short = 'n', help = "Print each command, and the program it would run, without running it")
    dryRun: Boolean = false,
    @opt(positional = true, help = "Recipes to run, each followed by its arguments")
    recipes: List[String] = Nil
) derives Parser.Command

@main def sake(args: String*): Unit =
  val options = Flagged.parseOrExit[SakeArgs](args)
  try
    val file = Parse(find(Paths.get("")))
    if options.recipes.isEmpty || options.list then list(file)
    else
      val runner = Runner(file, dryRun = options.dryRun)
      for (name, recipeArgs) <- invocations(file, options.recipes) do runner.run(name, recipeArgs)
      Serve.awaitIfRunning()
  catch
    case e: SakeError =>
      System.err.println(s"sake: ${e.message}")
      sys.exit(e.exitCode)
