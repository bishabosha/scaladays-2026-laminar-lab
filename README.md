# The Apothecary: a Laminar lab

A two-hour, hands-on lab from ScalaDays 2026. Grimsby & Daughters, purveyors of fine
potions since 1642, are going digital, and you'll build their shop in
[Laminar](https://laminar.dev): a cauldron, a potion registration form with the Guild's
many rules, a cellar backed by a slow and grumpy oracle, and finally widgets for the
[notebook.sc](https://github.com/bishabosha/notebook.sc) notebook.

Follow the slides on your own screen: <https://bishabosha.github.io/apothecary/>

## Getting started

We'll do this together at the start of the lab, on your own laptop. You need a terminal,
a browser, Git and the internet. Everything else is downloaded as you go: Scala CLI and
Mill fetch their own Java, so you don't need to install one.

Every lab command is `./sake <something>`. **On Windows**, type `sake <something>` in
Command Prompt, or `.\sake.bat <something>` in PowerShell, instead.

### Step 1: install Scala CLI and Node.js

- **Scala CLI**: follow <https://scala-cli.virtuslab.org/install>
- **Node.js** (runs exercise 2's tests): follow <https://nodejs.org/en/download>

Skip either if you have it already: `scala-cli version` and `node --version` print a
version. Open a new terminal afterwards, so it finds the new commands.

### Step 2: download the lab

```sh
git clone --recurse-submodules git@github.com:bishabosha/scaladays-2026-laminar-lab.git
cd scaladays-2026-laminar-lab
```

**No SSH key for GitHub?** (The clone says *Permission denied (publickey)*.) Use HTTPS
instead, then fetch the two submodules over HTTPS too:

```sh
git clone https://github.com/bishabosha/scaladays-2026-laminar-lab.git
cd scaladays-2026-laminar-lab
./sake fetch-https
```

### Step 3: check your setup

```sh
./sake check
```

The first run downloads Scala, Scala.js, Laminar, Mill and a Java runtime, so give it a
few minutes. It finishes with **Ready, apprentice**.

### Step 4: start building everything else (and leave it running)

Open a **second terminal**, go to the lab directory again, and run:

```sh
./sake prepare
```

This compiles every exercise, the capstone widgets and the notebook for the capstone.
Leave it running while the lab carries on; it can take ten minutes or more on conference
Wi-Fi. It finishes with **All set, apprentice**.

### Step 5: say hello

Back in the first terminal:

```sh
./sake ex 00-setup
```

Open <http://127.0.0.1:8000/?ex=00-setup> in your browser and type your name: the shop
greets you. You're ready. Press Ctrl-C to stop it, and wait for exercise 1.

Something went wrong? See [Troubleshooting](#troubleshooting), or raise your hand.

## During the lab

The slides drive the lab. Each exercise has a `start/` to edit and an answer you can
peek at.

| Exercise | You build | Laminar |
| --- | --- | --- |
| `00-setup` | a greeting | it works! |
| `01-cauldron` | sliders that mix the cauldron's colour | `Var`, `Signal`, `-->`, `<--` |
| `02-potion-form` | a validated registration form | controlled inputs, validation as a `Signal` |
| `03-cellar` | ingredient search and stock list | `debounce`, `flatMapSwitch`, `split`, optimistic updates |
| `04-brew` | a component, and confetti | components, JS interop |
| capstone | a widget for notebook.sc | Laminar inside another app |

```sh
./sake ex 01-cauldron           # build, serve on http://127.0.0.1:8000, rebuild on save
./sake test 02-potion-form      # exercise 2's validation tests
./sake solution 01-cauldron     # copy the answer into exercises/01-cauldron/solution
./sake ex-solution 01-cauldron  # ...and run it
./sake reset-ex 01-cauldron     # start the exercise over (discards your changes)
```

Edit the files in `exercises/<exercise>/start/`. The page reloads by itself after each
rebuild. Every page has a 🔮 panel to change the oracle's mood: how often it refuses, and
how slow it is.

### Capstone: notebook.sc widgets

Widgets live in `widgets/<name>/`. The same build shows up in the slides and in the
notebook. See [widgets/README.md](widgets/README.md) for the details.

```sh
./sake widget recipe WATCH=1    # build a starter, and rebuild on save
./sake notebook                 # notebook.sc on http://localhost:8080 with every widget
./sake new-widget mixer         # start your own widget from the template
./sake widget-solution recipe   # build the answer instead
./sake reset-widget recipe      # start the widget over
```

Starters: `recipe` (a form, whose value is a `Potion`), `oracle` (an async ingredient
picker) and `chart` (plots a cell's value over time). The notebook,
`notebooks/apothecary.notebook.sc`, has a section wired up for each. After rebuilding a
widget, refresh the notebook in the browser.

### What's where

| Path | What |
| --- | --- |
| `exercises/` | exercises 00 to 04, each with a `start/` |
| `widgets/` | the capstone starters, the `potions` example and `_template` |
| `apothecary/` | shared code: the domain, the oracle, `VarLenses`, `Json`, `Shop` |
| `notebooks/` | the lab's notebook |
| `slides/` | the deck |
| `modules/` | notebook.sc and tiger-site-gen (the deck's site generator), as submodules |
| `Sakefile` | every `./sake` command; `tooling/sake/` is the runner |

### Troubleshooting

- **"Failed to download … notebook-plugin-api"** is harmless: it's then found in your
  local Ivy repository, which `./sake prepare` filled.
- **"port 8000 is in use"**: another `./sake ex` is still running. Stop it with Ctrl-C.
- **A submodule failed to clone**: you probably have no GitHub SSH key. Use
  `./sake fetch-https`.
- **Scala CLI says it's outdated, or munit is outdated**: harmless, ignore it.
- **`./sake: Permission denied`** (macOS/Linux): run `chmod +x sake` once.
- **"sake: compiling natively with Scala Native"**: the first `./sake` compiles itself into a
  fast native binary when `clang` is installed (cached in `.sake/`). Without `clang`, or if
  that fails, it runs on the JVM instead: slower to start, but otherwise the same. To skip
  the native build, set `SAKE_JVM=1`.
- **Exercise 04 shows no confetti**: the confetti library loads from esm.sh, so it needs
  the internet.
- **I broke everything**: `./sake reset-all` puts the exercises, widgets and notebook
  back to how the lab starts. It discards all your work.

## For presenters

### Setting up once

```sh
./sake prepare          # everything attendees have
./sake publish-tiger    # tiger-site-gen (with reveal.js bundled) into your local Ivy repository
./sake ex-demos         # the exercise answers, as live demos in their slides
```

### Running the lab

```sh
./sake slides           # live deck: http://127.0.0.1:8123/apothecary/ (Content studio at /__author/)
./sake slides-display   # or a static build: http://127.0.0.1:8127/apothecary/
./sake notebook         # notebook.sc for the capstone
```

Timings and speaker notes are in each slide's source, in
`slides/content/apothecary/slides/`. The deck adds up to 120 minutes.

To show capstone answers in the notebook, run `./sake widget-solution <name>` while it
runs, then refresh. After a rehearsal, run `./sake reset-all` to get back to the
starting state.

### Publishing the slides

The deck is published with the blog, which tiger-site-gen builds from `blog/_docs`, at
<https://bishabosha.github.io/apothecary/> (linked from the blog's About page).

```sh
./sake publish-deck                          # build the deck into the blog's sources
./sake blog-build                            # build the blog into modules/tiger-site-gen/dist/breeze
./sake publish-blog ../bishabosha.github.io  # copy it into your checkout of the site
```

`publish-deck` builds the deck with every live demo (fetching the answers from the
`solutions` branch). The deck's page becomes the blog page `apothecary/index.md`: the built
HTML after the SON header in `slides/blog-page.son`, rendered as it is by the blog's `raw`
layout. Its assets go in the blog's `static/reveal_<hash>/`, replacing older bundles. Commit
those in tiger-site-gen; then, after `publish-blog`, commit and push the site's checkout.

### Answers and the solutions branch

`main` holds only the starting state. The answers are one commit on top, on the
`solutions` branch, and `./sake solution` and `./sake widget-solution` copy them from
`origin/solutions` on demand.

- **Change the lab:** commit on `main`, then run `./sake push`. It rebases `solutions`
  onto `main` and pushes both branches.
- **Change an answer:** `git switch solutions`, edit, `git commit --amend`,
  `git switch main`, then `./sake push`.

### Before the event

- [ ] Make this repository public.
- [ ] Run through `./sake prepare` and an exercise on Windows.
- [ ] Optionally, publish `notebook-plugin-api` to Maven Central. That removes the
      "Failed to download" noise and the Mill step from `./sake prepare`.
