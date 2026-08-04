# SDUI Schema Design

This document is the source of truth for the JSON contract between server and
client. Everything else (renderer, registry, docs) follows from this.

## Design goals, in priority order

1. **A component type is a contract, not a layout.** `CarouselRail`,
   `Grid`, `Row`, `Column` don't know what's inside them — they lay out
   opaque child nodes. This is what keeps coverage % high on a screen we
   didn't build for: 90% of a new screen is a new *arrangement* of existing
   primitives, not a new primitive.
2. **Props are an open map, never a fixed data class per component at the
   wire level.** The client never fails to *parse* a payload — only to
   *render* a type it doesn't have a composable for. Unknown keys inside a
   known component's props are ignored, not rejected.
3. **State and styling are data, not code.** A chip selection changing a
   rail's content is a JSON-declared binding, not a `when` branch hardcoded
   for "category chips."
4. **Forward-compatible by construction.** Old clients skip node types and
   prop keys they don't understand and keep rendering the rest of the page.

## Top-level envelope

```json
{
  "schemaVersion": 1,
  "minClientVersion": 1,
  "page": {
    "id": "home_landing",
    "title": "Cars24 Home",
    "sections": [ /* SduiNode[] */ ]
  }
}
```

- `schemaVersion` — increments on any breaking change to the envelope shape
  itself (rare). Client checks this first; a major mismatch it can't parse
  at all triggers a full-page "please update the app" fallback screen
  instead of a partial render.
- `minClientVersion` — the lowest client build that can render this payload
  *reasonably*. Client compares against its own build number and, if
  behind, can still attempt a best-effort render (older clients naturally
  skip node types they don't have registered) or show an update banner.
  This is a page-level hint; per-node overrides exist for finer control
  (see Versioning below).

## SduiNode — the one recursive unit

Every visible thing on the page, from the whole page down to a single
`Text`, is the same shape:

```json
{
  "id": "chip_row_category",
  "type": "ChipRow",
  "minClientVersion": 1,
  "style": {
    "padding": [16, 16, 16, 8],
    "background": "#FFFFFF"
  },
  "props": {
    "items": [
      { "id": "suv", "label": "SUV", "value": "suv" },
      { "id": "sedan", "label": "Sedan", "value": "sedan" }
    ],
    "defaultSelected": "suv"
  },
  "children": [ /* SduiNode[], only for generic containers */ ],
  "actions": {
    "onSelect": {
      "type": "updateState",
      "target": "selectedCategory",
      "payloadFromEvent": "value"
    }
  }
}
```

Fields:

| Field | Required | Meaning |
|---|---|---|
| `id` | yes | Stable identity — used as state-binding key, Compose `key()`, and in the fallback/error log. |
| `type` | yes | Registry lookup key. Unknown → `UnknownComponent` fallback, page keeps rendering. |
| `minClientVersion` | no | Per-node override of the page-level hint. Lets a server ship a *new* node type inside an otherwise-old page without bumping the whole page's requirement — old clients skip just that node. |
| `style` | no | Shared, component-agnostic visual props (padding/margin/background/corner radius/elevation/alignment). Every component reads from the same `SduiStyle` model so styling is never reimplemented per component. |
| `props` | no | Component-specific data. Kept as a generic key→`JsonElement` map at the parsing layer; each component's composable pulls out and coerces the keys it understands and ignores the rest. |
| `children` | no | Only meaningful for generic layout containers (`Row`, `Column`, `Grid`, `CarouselRail`) — an ordered list of child `SduiNode`. |
| `actions` | no | Map of interaction-name → `SduiAction` (see below). Which interaction names a component fires (`onClick`, `onSelect`, `onLongPress`...) is up to that component. |

## Component registry (categories)

| Category | Types | Notes |
|---|---|---|
| Layout primitives | `Row`, `Column`, `Grid`, `CarouselRail` | Generic containers, take `children`. `Grid` takes `columns` in props. `CarouselRail` takes `itemWidth`/`peekNext`. |
| Leaf | `Text`, `Image`, `Spacer`, `Divider`, `Badge` | Single-purpose, no children. |
| Interactive leaf | `Button`, `ChipRow`, `Tag` | Fire `actions`. |
| Composite/domain | `CarCard`, `BannerCarousel`, `SearchHeader`, `ValuePropStrip`, `FooterCta` | Pre-arranged combinations of primitives, exposed as one type because they recur often enough on Cars24-style pages to be worth naming — but internally they're just composed from the same primitives, so a *new* composite the client doesn't have yet degrades to `UnknownComponent`, not a crash. |

Full registry table with prop schemas lives in `COVERAGE.md`.

## Actions

```json
{
  "type": "updateState" | "navigate" | "openSheet" | "openUrl" | "apiCall" | "none",
  "target": "selectedCategory",
  "payload": { "route": "car_detail", "carId": "{{item.id}}" },
  "payloadFromEvent": "value"
}
```

- `updateState` — writes into the shared client-side state store at key
  `target`. `payloadFromEvent` says "take this field off whatever the
  triggering component emitted" (e.g. the tapped chip's `value`) rather than
  a hardcoded literal — this is what makes one `ChipRow` type reusable for
  *any* filter, not just category.
- `navigate` — an intent the host app resolves (route name + params). The
  SDUI layer never knows what a route does; it just emits the intent.
  Deep-link-shaped strings keep this platform-neutral.
- `openSheet` — target names a *node id elsewhere in the page* (or an
  inline node in `payload`) to render inside a bottom sheet. This is how a
  CTA "opens the sheet" per the brief without a new component type.
- `openUrl` / `apiCall` — escape hatches for webviews and future live data;
  unimplemented in v1 but reserved so the enum doesn't need a breaking
  change later.
- `none` — explicit no-op, useful for a chip that's visually selectable but
  intentionally inert in a given payload.

## State binding — how a chip selection changes a rail's content

Rather than a general expression/templating language (rejected — see
`AI_WORKFLOW.md` for why; it blew the timebox for marginal benefit), content
that varies by state uses an explicit **variant map** on the *consuming*
node:

```json
{
  "id": "rail_popular_cars",
  "type": "CarouselRail",
  "props": { "title": "Popular near you" },
  "dataBinding": {
    "stateKey": "selectedCategory",
    "variants": {
      "suv": [ /* CarCard nodes */ ],
      "sedan": [ /* CarCard nodes */ ],
      "default": [ /* CarCard nodes, used if stateKey has no match */ ]
    }
  }
}
```

At render time, the renderer resolves `children` as
`dataBinding.variants[currentState[stateKey]] ?: variants["default"]`. This
covers "tab/chip selection changes content" without inventing a query
language — the server precomputes every variant, which also means no
runtime filtering logic lives on the client at all.

## Unknown-component fallback

Renderer dispatch is a single lookup: `registry[node.type] ?: unknownRenderer`.
`UnknownComponent`:
- Renders nothing visible in release (zero layout impact on the rest of the
  page) or a subtle debug-only placeholder chip showing `node.type` and
  `node.id` in debug builds, gated by a build flag.
- Logs the unknown type once (structured log, not a crash report).
- Never throws — prop coercion inside every real component is defensive
  (`get(...) as? String ?: default`), so a malformed-but-known node also
  degrades to partial rendering instead of taking down the page.

## Versioning story

Two independent axes, both handled without a runtime negotiation protocol:

1. **New node type on old client** → `registry` miss → `UnknownComponent`.
   The page around it still renders. No client change needed for the
   *rest* of the page to keep working when the server adds a component
   type.
2. **New prop on a known node type** → generic `props` map means the parse
   step never fails; the old component composable simply never reads the
   new key. When that client eventually updates, the same payload
   "lights up" the new behavior with no server change.
3. **`minClientVersion`** (page- and node-level) lets the server pre-empt
   sending something it knows will be a no-op fallback for a cohort — it's
   an optimization/analytics signal, not a hard gate, since the fallback
   path is already safe by construction.

Implementation of an actual client-version handshake (client sends its
build number, server tailors the payload) is out of scope for this
timebox — the doc above is the honest story; the fallback + open-props
mechanics that make it safe *are* implemented.
