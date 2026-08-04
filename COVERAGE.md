# COVERAGE.md

## Component registry

| Type | Category | Key props | Notes |
|---|---|---|---|
| `Column` | Layout primitive | — (uses `children`) | Generic vertical stack |
| `Row` | Layout primitive | — (uses `children`) | Generic horizontal stack |
| `Grid` | Layout primitive | `columns` (Int), `title` | Fixed-column `LazyVerticalGrid`, nested inside a scrolling `LazyColumn`; height and `userScrollEnabled = false` are derived from the actual child count (not a `rows` prop) so it can never become its own independently-scrollable region |
| `CarouselRail` | Layout primitive | `title` | Horizontally scrolling `LazyRow`; identical mechanism whether children are `CarCard`s or banners |
| `Text` | Leaf | `text`, `size`, `weight` (`bold`/`medium`/normal), `color` (hex) | |
| `Image` | Leaf | `url`, `width`, `height`, `alt` | Missing/failed `url` renders a stable gray placeholder, never a layout hole |
| `Spacer` | Leaf | `height` | |
| `Divider` | Leaf | — | |
| `Button` | Interactive leaf | `label` | Fires `onClick` |
| `ChipRow` | Interactive leaf | `items[]` (`id`,`label`,`value`), `defaultSelected` | Fires `onSelect`; selection writes to state via `payloadFromEvent: "value"` |
| `CarCard` | Composite | `imageUrl`, `title`, `subtitle`, `price`, `badge` | Fires `onClick` |
| `BannerCarousel` | Composite | `items[]` (`imageUrl`\|`background`, `title`, `route`) | |
| `SearchHeader` | Composite | `title`, `placeholder` | Fires `onClick` |
| `ValuePropStrip` | Composite | `items[]` (`icon`, `label`) | Static trust-badge row |
| `FooterCta` | Composite | `title`, `subtitle`, `buttonLabel`, `background` | Fires `onClick` |
| *(unrecognized type)* | Fallback | — | `UnknownComponent` — logs once, renders nothing in release / a debug-only red outline chip, never crashes |

**Cross-cutting mechanisms available to every node**, independent of type:
- `style` (padding/margin/background/cornerRadius) — never reimplemented per component
- `actions` — any node can carry `onClick`/`onSelect`/etc., interpreted generically by `ActionDispatcher`
- `dataBinding` — swaps a node's `children` based on a shared state key. Used for the category-chip → rail filter *and*, completely unmodified, for the tenure-selector → EMI text in the bottom sheet. One mechanism, two unrelated UI patterns — this reuse is the main coverage lever, not the size of the registry.

## What the schema can express today

- Lists and grids (`CarouselRail`, `Grid` — both take arbitrary `children`, including nested containers)
- Conditional/variant content without a general expression language (`dataBinding` variant maps)
- Actions: state writes, navigation intents, opening a bottom sheet sourced from anywhere else in the page tree
- Styling overrides per-node (no per-component style duplication)
- Forward-compatible payloads: unknown `type` → safe fallback; unknown `props` keys on a *known* type are silently ignored, never a parse failure

## What it can't express yet (known gaps, not discovered by accident — see below)

- **Tab bars with distinct visual chrome from chips.** A `ChipRow` driving a `dataBinding` body already covers the *behavior* of tabs (see self-test below) but not underline/pill tab styling — would need a `TabRow` type or a `style.variant` hint on `ChipRow`.
- **Sticky/pinned regions** (a bottom action bar that stays fixed while the rest of the page scrolls). The renderer currently puts the whole page in one `LazyColumn`; a pinned region needs a page-level structural field (e.g. `page.stickyFooter: SduiNode?`) plus a client change to route it into `Scaffold`'s `bottomBar` slot instead of the scroll list.
- **Overlay/stacked layout** (e.g. a wishlist icon floating on top of a carousel image). No generic `Box`-with-overlay-children type is registered — composites that need this today (badges on `CarCard`) have it hardcoded inside that one composite, not as something JSON can compose generically yet.
- **Expand/collapse (accordion) content.** No component holds open/closed UI state tied to a toggle action.

## Honest coverage claim

**Given a new Cars24 screen built from the same visual vocabulary (rails, grids, cards, banners, chip filters, CTA strips), an estimated 65–75% renders with JSON-only changes.** The self-test below is the basis for that number, not a guess.

### Self-test: a screen we didn't build for

To rehearse the first-round "surprise screen" exercise honestly, we sketched
JSON for a **Cars24 car-detail page** (not the landing page this repo's
JSON targets) using only the registry above, without adding any new
component:

| Detail-page section | Renders JSON-only? | How |
|---|---|---|
| Photo carousel | ✅ | `CarouselRail` of `Image` children |
| Key specs (KM / fuel / transmission / owner) | ✅ | `Grid` of `Column(Text, Text)` tiles |
| Price + "Calculate EMI" → tenure sheet | ✅ | Exact same `Button` → `openSheet` → `ChipRow` + `dataBinding` pattern already in `landing_page.json`, just different copy |
| Overview / Features / Inspection tabs | ✅ (behaviorally) | `ChipRow` (`onSelect` → `updateState`) driving a `Column` with `dataBinding` per tab — same mechanism as the category filter. Visually reads as chips, not underlined tabs. |
| Trust badges (assured, warranty) | ✅ | `ValuePropStrip`, unchanged |
| "Similar cars" rail | ✅ | `CarouselRail` of `CarCard`, unchanged |
| Sticky "Book this car" bottom bar | ❌ | No pinned-region concept yet — needs a schema field + a `MainActivity`/`Scaffold` change |
| Expandable inspection-report accordion | ❌ | No expand/collapse component yet — needs a new type + local toggle state |
| Wishlist icon overlaid on the photo carousel | ❌ | No generic overlay/`Box` type registered — would need one |

**5 of 8 sections (excluding the two "same mechanism, different copy" rows
already counted above, which is really 5 fully novel wins + 2 direct reuses
of an existing pattern) render with zero new client code; 3 need a new
component or a schema field.** That lines up with the 65–75% estimate above
— it isn't padded, and it isn't rounded down to look humble either.

For each gap, the fix is additive (a new registry entry or one new schema
field), not a rewrite — which is the property that actually matters for
"how fast you add it" in the live round.
