---scala
(id = "setup", seconds = 240, layout = "exercise")
---

## Setup, step by step

{{exercise 00-setup}}

1. Install [Scala CLI](https://scala-cli.virtuslab.org/install) and [Node.js](https://nodejs.org/en/download)
2. `git clone --recurse-submodules git@github.com:bishabosha/scaladays-2026-laminar-lab.git` then `cd scaladays-2026-laminar-lab`
3. `./sake check` (must succeed)
4. In a **second terminal**: `./sake prepare`, and leave it running
5. `./sake ex 00-setup`, open <http://127.0.0.1:8000/?ex=00-setup>

Full instructions: **github.com/bishabosha/scaladays-2026-laminar-lab** · Windows: `sake` instead of `./sake`

## Speaker notes

Put this slide up while people arrive. The first `./sake check` downloads Scala, Scala.js, Laminar, Mill and a JVM:
several minutes on conference Wi-Fi. Everyone must see the hello page before exercise 1.
`./sake prepare` builds the remaining exercises, widgets and notebook.sc in the background; it only needs to finish before the capstone.
No SSH key ("Permission denied (publickey)"): clone the https URL without --recurse-submodules, then `./sake fetch-https`.
