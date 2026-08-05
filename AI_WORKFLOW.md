# AI_WORKFLOW.md

## Tool stack

- **Claude Code** (Anthropic's agentic CLI) as the sole AI pair-programmer
  for this project — driving file edits, Gradle builds, JSON authoring,
  and git operations directly, not just suggesting snippets to paste.
- No pre-existing `CLAUDE.md`/rules file — this was a fresh repository. In
  place of a rules file written *before* coding started, **`SCHEMA.md` was
  written first and treated as the binding spec for every file generated
  after it.** Every component, prop name, and action type traces back to a
  decision made in that doc, not to something invented ad hoc while
  writing a component file. That ordering (spec → scaffolding → content) is
  the actual "context file" discipline this project used, just authored as
  step one of the session instead of handed in beforehand.
- A running task list (created via the CLI's task tool) served as the
  execution brief across the session — each unit of work (schema, models,
  registry, components, JSON content, static twin, perf hooks) was tracked
  explicitly rather than done in one undifferentiated pass, which is also
  why the git history for this repo is incremental rather than one giant
  commit.

## Prompt → outcome stories

### 1. "the schema doesn't have enough styling/attributes — add real support for borders, opacity, rotation, and flex-style alignment"

**Outcome**: `SduiStyle` grew from its original 8 fields
(`padding`/`margin`/`background`/`cornerRadius`/`elevation`/`alignment`/
`width`/`height` — see `dac056e`, the very first schema commit) to its
current 13: adding `justifyContent`/`alignItems` (flexbox-style main-/
cross-axis control shared by `Row` and `Column`), `opacity`, `borderWidth`/
`borderColor`, and `rotation`. This is the difference between the schema
being able to express "a box with a color" and being able to express an
actual bordered card, a rotated badge, or a properly-centered chip row —
real patterns the Cars24 landing page needed once its content stopped
being placeholder text.

**What got rejected/rewritten**: `elevation` — one of the *original* 8
fields — was never actually wired up. It's still declared on `SduiStyle`
today but `toModifier()` never reads it; every card in the schema gets its
elevation from Material3's default `Card` styling instead. Caught by
tracing every `SduiStyle` field to its consumer in `StyleModifier.kt`
while writing this doc, not by the AI flagging it unprompted — a small,
honest example of a field that sounded reasonable at schema-design time
but never got a real caller, which is exactly the kind of thing "explains
every line in debrief" is supposed to surface.

### 2. "add a collapsing header and make the nav tabs swap the whole page body, like the real Cars24 app"

**Outcome**: a page-level `header` slot (separate from `page.sections`,
see `PageContent`), a `NestedScrollConnection`-driven collapse fraction
(`CollapseFraction.kt`/`Collapsible.kt`) that lets the header eat scroll
pixels before the body list scrolls, `ColorBinding` as a new schema
concept (a node's background can now depend on app state, not just its own
static style), and the `tab_body` node's `dataBinding` scoped to the
*entire* page body — so selecting "Buy used car" swaps all 17 sections at
once, not just one rail.

**A gap flagged in the JSON that turned out not to be one**: `nav_tab_chips`
carries a `_note` worrying that the search bar + tab row aren't truly
"pinned" the way the real Cars24 app does it, since the schema has no
explicit sticky/pinned-region concept. Re-verified this directly on
physical hardware while writing this doc (scrolled deep past "Manage your
vehicle" into "7 showrooms in your city") — search+tabs stay fixed at the
top the entire time. They're pinned for free, as a structural side effect
of the header rendering in its own `Column` *above* the body `LazyColumn*`
rather than inside it: once the location row's height collapses to 0, the
header `Column`'s remaining height is just search+tabs, permanently, and
the `LazyColumn` beneath it scrolls independently. The `_note`'s caution
was reasonable to write at the time but turned out to be overly
pessimistic — worth catching before it became a false "known gap" in
`COVERAGE.md`. (The one real, still-open pinned-region gap is a *bottom*
sticky action bar, tracked separately in `COVERAGE.md`.)

### 3. Debugging why the challan tab looked broken on-device — a schema-level layout bug, not a data bug

**Outcome**: caught by actually looking at screenshots on the Pixel 10 Pro
emulator, not by re-reading the JSON. The "IND" country-code chip inside
the challan form had exploded into a ~900px-tall box with its sibling
placeholder text invisible; separately, three "why choose us" columns and
a 3-stat trust row were each collapsing down to just their first item.
Root cause: `Column`/`Row` default to `Modifier.fillMaxWidth()`
unconditionally in `Primitives.kt`, and `style.width == "wrap"` was
supposed to opt out of that but was implemented as a literal no-op
(`spec == "wrap" -> modifier`) — since it composed *after* the
already-applied `fillMaxWidth()` base, it had nothing left to undo.

**What got rejected and rewritten**: the first fix only special-cased
`"wrap"` (skip the `fillMaxWidth()` default when width is exactly
`"wrap"`). It looked right and fixed the IND chip — but the challan
"why" columns, given an explicit fixed width (`"100"`) instead of
`"wrap"`, *still* collapsed to one visible item after that fix, because
`Modifier.fillMaxWidth().width(100.dp)` doesn't shrink to 100dp — the
first modifier already fixed min=max=parent-width, and Compose's
constraint resolution coerces the later, smaller request back up to
satisfy it. That regression was only caught by re-screenshotting after
the "fix" and noticing the exact same symptom, not by assuming the
narrower patch was sufficient. The real fix generalizes the condition to
*any* explicit `style.width` (not just `"wrap"`) suppressing the default —
then a full-file grep for the two arrangement patterns most likely to hit
this (`spaceBetween`, `spaceEvenly`) found and fixed all seven affected
nodes across two tabs, instead of patching only the one the bug report
pointed at.

### 4. "check on your own — all grid items are cropped from the bottom"

**Outcome**: `Grid`'s `LazyVerticalGrid` needs a fixed pixel height up
front (it can't measure "wrap content" without knowing an item's size
before scrolling to it). The first fix guessed a `cellHeight` constant
(100dp) tuned against the icon-tile grids, multiplied by row count. Told
to check every tab, not just the one reported: three more grids (90dp
category-tile images, not 48-56dp icons) were *also* clipped, because
the same guessed constant didn't fit them either — bumped to 180dp.

**What got rejected**: *"increasing the cellHeightDp is your solution?
if i increase the no. of rows to 4-5 then what would you suggest?"* — a
correct call-out that a bigger guess is still a guess, not a fix; it
would need a new number for every future row count and every new cell
shape. Since this `Grid` already sets `userScrollEnabled` behavior that
disables independent scrolling, laziness was buying nothing — rewrote it
as plain `Row`s chunked by `columns` inside a `Column`, which Compose
measures and wraps to real content automatically. No constant, no
per-instance tuning, correct for any row/column count going forward.
The lesson: "it renders now" isn't the bar — "why does this number
exist, and does it survive the next screen" is, and an AI-guessed
constant that merely stops the immediate complaint should be treated as
a placeholder to interrogate, not a fix to ship.

## One AI failure

**Where**: the whole SDUI landing page's scroll — every rail, every
recomposition, every collapsing-header frame — was janky and dropping
frames after an earlier round of AI-generated feature work (the
collapsing header + dynamic color binding + multi-tab body swap). The
animations visibly stuttered under real scrolling, not just in a
synthetic benchmark.

**Why it went wrong**: three separate root causes stacked on top of each
other, all introduced the same way — code that *looked* reasonable and
compiled cleanly, but wasn't actually stable/cheap under Compose's
recomposition model:
1. `SduiNode`/`SduiStyle`/`ColorBinding`/etc. all hold `Map`/`List`
   properties, which the Compose compiler treats as **unstable by
   default** — it can't prove immutability from the interface alone, so
   every component function taking an `SduiNode` parameter was skipped
   from Compose's "skip recomposition if inputs are unchanged"
   optimization, even though these nodes are parsed once from JSON and
   never mutated in place.
2. The collapse fraction was first passed around as a raw `Float`, so
   *reading* it anywhere in a composable body — not just in a layout/draw
   phase block — triggered a full recomposition on every scroll-pixel
   update, dozens of times a second.
3. Several `LazyRow`/`LazyColumn`/`LazyVerticalGrid` lists were missing
   explicit `key = { it.id }` on their `items(...)` calls, so Compose
   couldn't diff list identity across recompositions and re-created/
   re-measured rows it didn't need to.

**How it was caught and fixed**: not by reading the code and guessing —
by treating the visible jank as the actual bug report and instrumenting
against it (`adb shell dumpsys gfxinfo` frame-time percentiles, described
in `PERF.md`'s measure→optimize loop), then fixing each cause at its
source rather than papering over the symptom: annotated every SDUI model
data class `@Immutable` (`SduiModels.kt`) so Compose can trust them as
stable inputs; changed `LocalSduiCollapseFraction` to carry a stable
`State<Float>` object instead of a raw `Float`, with every real per-pixel
read confined to `Modifier.layout{}`/`graphicsLayer{}` blocks in
`CollapsibleOnHide` (draw/layout phase, not composition — see that file's
kdoc); and added explicit item keys to the lists that were missing them.
One gap survived even that pass: `ChipRow`'s two `itemsIndexed(...)` calls
in `Interactive.kt` were still missing keys after the rest of the app was
fixed — found and closed out later, while double-checking this exact
story for accuracy before writing it down, which is its own small lesson
in why "we already fixed the perf issue" needs to be re-verified rather
than taken as a closed book.

## Verification strategy

- **Compile after every meaningful unit of change**, not just once at the
  end — `./gradlew :app:compileDebugKotlin` (fast) during iteration,
  `:app:assembleDebug` before treating a chunk of work as done, to also
  catch manifest/resource-linking issues the Kotlin compiler alone won't.
- **Validate hand-authored JSON independently of the app** —
  `python -c "import json; json.load(...)"` on `landing_page.json` before
  wiring it into `MainActivity`, so a malformed payload is caught as a
  JSON problem, not misdiagnosed as a Kotlin/serialization problem.
- **Re-check git state after every git operation** — `git status`/`git
  log`/`git reflog`, not just trusting the previous command's exit
  behavior. Caught a real incident earlier in the project: a heredoc
  commit message containing `->` arrow characters got misparsed by the
  shell, so `git commit` silently printed its own usage/help text instead
  of committing — no commit was created. Treating "the output doesn't
  look like a normal commit confirmation" as a signal to immediately
  re-run `git log`/`git status` (rather than assuming success) is what
  caught it, before it could look like lost work later.
- **Read the unknown-component and action-dispatch paths against the
  brief's actual requirement** ("must never crash") rather than trusting
  they work because the happy path compiles — traced the registry-miss
  branch and the malformed-action branch explicitly rather than assuming
  defensive-looking code (`as? String ?: default`) is automatically
  correct everywhere it's used.
- **Screenshot the actual running app on-device, not just the JSON/code**
  — the challan-tab layout bug (story 3 above) was invisible from reading
  `landing_page.json` or `Primitives.kt` in isolation; both looked
  reasonable on their own. It only surfaced by `adb exec-out screencap`
  against the Pixel 10 Pro emulator and comparing what actually rendered
  against the static twin. The same discipline caught the first fix
  attempt's regression (see story 3) — re-screenshotting after a "fix"
  rather than assuming it worked because the reasoning sounded right.
- **Real unit tests for the pure logic, not just manual on-device
  checks.** `SduiValidator.validate`, `resolveDataBinding`/
  `resolveColorBinding`, and `ActionDispatcher` are covered by 28 JUnit
  tests in `sdui/src/test` (duplicate-id detection, dangling `openSheet`
  targets, variant fallback-to-default, state-write plumbing). The two
  renderer functions were `private`; made `internal` specifically so
  they're reachable from a plain JVM test without pulling in Compose test
  infrastructure — a deliberate, narrow visibility change, not a
  loosening of the module's public API. `validateAndLog` and two
  `ActionDispatcher` branches call `android.util.Log` directly and were
  left untested rather than pulled in Robolectric to mock it — a scoping
  call worth being explicit about rather than silently skipping.
- **Check every `dataBinding` variant a fix could plausibly touch, not
  just the tab the bug was reported on.** Story 4's grid-cropping fix was
  first verified only against the tab where it was noticed; asked to
  check the others, three more tabs turned out to have the exact same
  bug from the exact same guessed constant. A fix that only "renders
  now" for one variant of a shared component hasn't actually been
  verified against the component — it's been verified against one
  caller of it.
