---scala
(id = "setup", seconds = 300, layout = "exercise")
---

## Setup check

{{exercise 00-setup}}

```sh
git clone --recurse-submodules git@github.com:bishabosha/scaladays-2026-laminar-lab.git
cd scaladays-2026-laminar-lab
./sake check          # Windows: sake check
./sake ex 00-setup    # serves http://127.0.0.1:8000: say hello
```

No SSH key? `git clone https://github.com/bishabosha/scaladays-2026-laminar-lab.git`, then `./sake fetch-https`

Peek at an answer: `./sake ex-solution 01-cauldron` (from the `solutions` branch)

Start over: `./sake reset-ex 01-cauldron`. Stuck? Raise your hand.

## Speaker notes

Five minutes. Everyone must see the hello page before we move on.
Resets: `reset-ex <ex>`, `reset-widget <name>`; `reset-all` (rehearsal clean-up) discards everything.
The HTTPS route: clone without --recurse-submodules, then `./sake fetch-https` (Windows: `sake fetch-https`).
Dependencies should already be cached if they followed the pre-lab email.
