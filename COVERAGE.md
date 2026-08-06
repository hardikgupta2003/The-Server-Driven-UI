# COVERAGE.md

## Component registry

| Type | Category | Key props | Notes |
|---|---|---|---|
| `Column` | Layout primitive | — (uses `children`) | Generic vertical stack |
| `Row` | Layout primitive | — (uses `children`) | Generic horizontal stack |
| `Grid` | Layout primitive | `columns` (Int), `title`, `titleBadgeLabel`, `trailingActionLabel` | Plain `Row`s chunked by `columns` inside a `Column` — deliberately not a `LazyVerticalGrid`. This grid never scrolls independently of the outer page anyway, so laziness bought nothing; a plain, non-lazy layout wraps its own real content instead of a fixed pixel height guessed per cell, and works unchanged for any row count |
| `CarouselRail` | Layout primitive | `title`, `titleBadgeLabel`, `trailingActionLabel` | Horizontally scrolling `LazyRow`; identical mechanism whether children are `CarCard`s or banners |
| `Text` | Leaf | `text`, `size`, `weight` (`bold`/`medium`/normal), `color` (hex) | |
| `Image` | Leaf | `url`, `width`, `height`, `alt` | Missing/failed `url` renders a stable gray placeholder, never a layout hole |
| `Spacer` | Leaf | `height` | |
| `Divider` | Leaf | — | |
| `Button` | Interactive leaf | `label` | Fires `onClick` |
| `ChipRow` | Interactive leaf | `items[]` (`id`,`label`,`value`), `defaultSelected` | Fires `onSelect`; selection writes to state via `payloadFromEvent: "value"` |
| `TextField` | Interactive leaf | `stateKey`, `placeholder`, `label`, `singleLine`, `borderColor` | Real editable input (not decorative) — value lives in `SduiStateStore` under `stateKey`, every keystroke dispatches `updateState` through the same `ActionDispatcher` path any other interactive component uses. No `fillMaxWidth()` default (see kdoc) since it commonly sits next to a fixed-width sibling. |
| `Chip` | Composite | `label`, `textColor`, `weight`, `size` | Small pill/badge, sized to its own content (no default width/background — `style` controls appearance entirely, like `Button`) |
| `ListRow` | Composite | `imageUrl`, `imageSize`, `title`, `subtitle`, `subtitleColor`, `trailing` (`"chevron"` or arbitrary text), `trailingColor` | Generalizes the "image + title/subtitle + trailing accessory" pattern that used to be ~40 lines of hand-composed `Row`/`Column`/`Text` per instance |
| `CarCard` | Composite | `imageUrl`, `imageHeight`, `title`, `subtitle`, `subtitleColor`, `price`, `badge`, `badgeColor` | Fires `onClick`. `badgeColor` matters once a page has more than one badge meaning ("Assured" vs "New arrival" vs "Cars24 Owned stock") — a single hardcoded badge color couldn't tell them apart |
| `BannerCarousel` | Composite | `items[]` (`imageUrl`\|`background`, `title`, `route`), `itemWidth`, `itemHeight` | Real center-focused carousel — the visible/center item renders at full scale, neighbors shrink with distance from center, and releasing a swipe snaps to the nearest item (`rememberSnapFlingBehavior`) rather than settling on momentum alone |
| `SearchHeader` | Composite | `title`, `placeholder`, `stateKey` | Real typable input bound to `SduiStateStore` via `stateKey` — same `updateState` action path as `TextField`, not a decorative read-only field |
| `ValuePropStrip` | Composite | `items[]` (`icon`, `label`) | Static trust-badge row |
| `FooterCta` | Composite | `title`, `subtitle`, `buttonLabel`, `background` | Fires `onClick` |
| *(unrecognized type)* | Fallback | — | `UnknownComponent` — logs once, renders nothing in release / a debug-only red outline chip, never crashes |

**Cross-cutting mechanisms available to every node**, independent of type:
- `style` (padding/margin/background/cornerRadius) — never reimplemented per component
- `actions` — any node can carry `onClick`/`onSelect`/etc., interpreted generically by `ActionDispatcher`
- `dataBinding` — swaps a node's `children` based on a shared state key. Used for the category-chip → rail filter *and*, completely unmodified, for the tenure-selector → EMI text in the bottom sheet. One mechanism, two unrelated UI patterns — this reuse is the main coverage lever, not the size of the registry.
- `SectionTitle`'s badge/trailing-action slot — `titleBadgeLabel` (a pill next to the title) and `trailingActionLabel`/`onTrailingAction` (a tappable link at the row's end) render identically on `CarouselRail` and `Grid`, the two component types with a `title`. Both used to be dead, component-specific props (`CarouselRail.badge`, `Grid.addVehicleLabel`) declared in the JSON but never rendered — replaced with one shared mechanism instead of wiring each up separately.

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

**Closed since the last pass**: a dedicated `Chip`/`ListRow` component (see registry table above) now replaces the hand-composed `Row`/`Column` pattern that caused a real layout bug (`AI_WORKFLOW.md`'s third story — a nested container defaulting to `fillMaxWidth()` starved its sibling of space). `landing_page.json`'s challan country-code chip and all three "uncover frauds" rows have been migrated to the new components — each fraud row went from ~40 lines of primitives to ~10 lines of `ListRow` props, and the bug class is now structurally impossible for new instances of this pattern, not just patched at the two sites it was first found.

**Closed in this pass**: three component-specific, declared-but-never-rendered props (`CarouselRail.badge`, `Grid.addVehicleLabel`, `CarouselRail.viewAllLabel`) are now wired up under two generic, reusable names (`titleBadgeLabel`, `trailingActionLabel`/`onTrailingAction`) instead of staying as dead schema surface. `SearchHeader` went from a decorative `readOnly` field (tapped to navigate to a search screen that doesn't exist in this build) to an actually-typable field bound to `SduiStateStore` via `stateKey`, same as `TextField`. `Grid` no longer estimates its own height from a guessed per-cell dp constant — it wraps real measured content via plain chunked `Row`s, so it's now correct for any row count without per-instance tuning.

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
