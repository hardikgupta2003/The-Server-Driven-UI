# The Server Driven UI — Cars24 SDUI Assignment

An Android/Kotlin/Jetpack Compose Server-Driven UI system: the server sends
JSON, the client renders the page. Built for the Cars24 Mobile Engineering
SDUI assignment.

## Screen chosen, and why

**The Cars24 home/landing page.** The brief explicitly flags it as clearing
the complexity bar, and it's the screen most likely to be re-used as the
basis for comparison against whatever "surprise screen" shows up in the
first round — building deep on the landing page's vocabulary (rails, grids,
cards, chip filters, CTA banners) is what makes the coverage self-test in
[`COVERAGE.md`](COVERAGE.md) meaningful rather than a shallow one-page demo
wearing a JSON costume.

It clears all four required bars:
- 5+ distinct section types: `SearchHeader`, `BannerCarousel`, `ChipRow`,
  `CarouselRail`, `Grid`, `ValuePropStrip`, `FooterCta` (7, not just 5)
- A horizontal rail (`CarouselRail`/`BannerCarousel`) **and** a vertical
  grid (`Grid` of `CarCard`s)
- An SDUI-driven interactive element: the category `ChipRow` swaps the car
  rail's contents via `dataBinding` — not a hardcoded `when` branch — and a
  second, independent interaction (the EMI tenure selector inside a bottom
  sheet) reuses the exact same mechanism for something unrelated
- Real-feeling hardcoded data: real Indian car models/prices/mileage,
  hardcoded in [`landing_page.json`](app/src/main/assets/sdui/landing_page.json)

## Setup

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Two launchable activities:
- `MainActivity` — the SDUI-rendered page (loads
  `assets/sdui/landing_page.json` at runtime)
- `StaticMainActivity` — the hand-written hardcoded twin of the same page,
  for the Part 2 perf comparison (see [`PERF.md`](PERF.md))

To see a JSON-only change take effect: edit
`app/src/main/assets/sdui/landing_page.json`, rebuild, reinstall. No Kotlin
file needs to change for a layout/content/copy change — that's the point.
(Assets are packaged at build time, so "change JSON → see it on next
launch" here means "edit the file, rebuild" rather than a true hot-reload
over the network; swapping the mock loader for a real HTTP call is a
one-function change in
[`JsonPageLoader.kt`](app/src/main/java/com/hardik/the_server_driven_ui/sdui/loader/JsonPageLoader.kt)
and the live-edit story becomes a real server-push story with zero
renderer changes.)

## Architecture overview

```
JSON payload (assets/sdui/landing_page.json)
        │
        ▼
JsonPageLoader  ──────────────►  PageSchema (kotlinx.serialization)
        │
        ▼
SduiPage (LazyColumn over top-level sections)
        │
        ▼
RenderNode  ── recursive dispatch point for every node, top-level and nested
        │
        ├─► resolveDataBinding(node, currentState)   — variant-map swap
        │
        ├─► ComponentRegistry.resolve(node.type)
        │        │
        │        ├─ found  → that component's @Composable
        │        └─ miss   → UnknownComponent (logs, never crashes)
        │
        └─► RenderContext { currentState, dispatch, renderChild }
                 used by interactive components to fire SduiActions
                 and by containers to recurse into children
```

Full schema rationale (why props are a generic map, why `dataBinding` is a
variant map instead of an expression language, why styling is centralized)
lives in [`SCHEMA.md`](SCHEMA.md) — that document is the actual design
artifact this system is built from, not written after the fact.

Key packages under `app/src/main/java/.../sdui/`:

| Package | Responsibility |
|---|---|
| `model/` | `SduiNode`, `SduiAction`, `SduiStyle`, `PageSchema` — the wire format |
| `renderer/` | `ComponentRegistry`, the recursive `RenderNode`/`SduiPage` dispatcher, `UnknownComponent`, style-to-`Modifier` mapping |
| `components/` | Every registered component's actual `@Composable` implementation |
| `state/` | `SduiStateStore` — shared state for data-binding + the open bottom sheet |
| `action/` | `ActionDispatcher` — the single place `updateState`/`navigate`/`openSheet` are interpreted |
| `loader/` | Reads + parses the mock-server JSON payload |

## Versioning story

Two independent, already-implemented mechanisms make old clients safe
against new server payloads without a negotiation protocol:

1. **Unknown `type` → `UnknownComponent`.** A server-added component type
   an old client doesn't recognize is skipped; the rest of the page still
   renders. Demonstrated live in `landing_page.json` via the
   `LiveAuctionWidget` node (see it fall back cleanly in the demo
   recording).
2. **Unknown prop keys on a *known* type are silently ignored** — `props`
   parses as a generic map, so a new key a component doesn't read yet
   never fails parsing; when the client updates, the same payload "lights
   up" the new behavior with no server change.

`schemaVersion` (envelope-level) and `minClientVersion` (page- and
node-level) exist in the model as page-level compatibility hints for a
server to make informed decisions about what to send a given client
cohort — full doc in `SCHEMA.md`. Implementing an actual client-version
handshake is out of scope for this timebox (the brief calls this
acceptable: "a section in your README is enough; implementation is
bonus") — the fallback + open-props mechanics that make an unversioned
payload *safe* are implemented and demoed; the negotiation layer on top of
them is not.

## Trade-offs

- **No expression/templating language** in the schema (`{{if x == y}}`
  style conditionals). Chose a precomputed variant-map (`dataBinding`)
  instead — less powerful in the abstract, but sufficient for every
  required interaction pattern and a much smaller surface to get right in
  the timebox. Detailed in `AI_WORKFLOW.md`'s second story.
- **No dedicated overlay/`Box` component or pinned/sticky-region concept**
  yet — both are real gaps surfaced by the `COVERAGE.md` self-test against
  an unplanned screen, not discovered by the grader first.
- **Images via Coil + network URLs** (Lorem Picsum) rather than bundled
  drawables — more "real-feeling" for the demo, but means TTR numbers in
  `PERF.md` need to explicitly account for image loads being async and
  off the critical path, not silently baked into the headline number.
- **No R8/ProGuard shrinking enabled** (`isMinifyEnabled = false`) — first
  lever to pull if `PERF.md` numbers show release-build overhead worth
  chasing.

## Docs map

- [`SCHEMA.md`](SCHEMA.md) — the JSON contract and why it's shaped this way
- [`PERF.md`](PERF.md) — perf methodology, instrumentation, and results
- [`COVERAGE.md`](COVERAGE.md) — registry table + honest coverage self-test
- [`AI_WORKFLOW.md`](AI_WORKFLOW.md) — tool stack, real prompt/outcome
  stories, the one AI failure, verification strategy
