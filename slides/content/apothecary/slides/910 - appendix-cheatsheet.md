---scala
(id = "appendix-cheatsheet", seconds = 0, layout = "appendix")
---

## Appendix · Cheat sheet

| I want to… | Laminar |
| --- | --- |
| show a value | `text <-- signal` |
| show a maybe-element | `child.maybe <-- signal` |
| show a list, keyed | `children <-- signal.split(_.id)(…)` |
| react to an event | `onClick --> observer` |
| read state in a handler | `onClick(_.sample(signal)) --> …` |
| run on mount / unmount | `onMountUnmountCallback(…)` |
| drop stale async results | `flatMapSwitch` |

## Speaker notes
