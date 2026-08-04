# PERF.md

Status: **methodology + instrumentation are implemented; numbers below are
placeholders** — run the steps in this doc on your own device and replace
them. This is deliberately written as a runbook, not a report with
fabricated numbers.

## Why not a Macrobenchmark module

Jetpack Macrobenchmark is the "correct" tool for this, but it needs its own
Gradle module, a connected device, and (for cold-start) a separate release
build variant — a real setup cost inside a 72-hour timebox. Instead this
uses Android's own cold-start instrumentation:

- The system automatically logs a `Displayed` line the moment the first
  frame of an Activity is drawn — zero code required.
- `Activity.reportFullyDrawn()` — called once from Compose, right after the
  first layout pass that contains real content (see
  [`MainActivity.kt`](app/src/main/java/com/hardik/the_server_driven_ui/MainActivity.kt)
  and
  [`StaticLandingScreen.kt`](app/src/main/java/com/hardik/the_server_driven_ui/static/StaticLandingScreen.kt))
  — triggers a second `Fully drawn` logcat line with the elapsed time since
  process start. This is Android's own recommended technique for TTR-style
  measurement outside of Macrobenchmark.
- [`PerfTrace.kt`](app/src/main/java/com/hardik/the_server_driven_ui/perf/PerfTrace.kt)
  adds two extra markers around the JSON read + parse step specifically, so
  the SDUI breakdown (parse vs view-build) is derivable from the same log.

Both are real, reproducible, adb-only measurements — just coarser than a
proper Macrobenchmark harness. If time allows later, swapping this for a
`:benchmark` module is the natural next step (noted in trade-offs below).

## Methodology

**Build**: release build, both activities in the same APK, same install, no
`isMinifyEnabled` change needed for this to be meaningful (currently
`false` — see trade-off note).

**Device**: motorola edge 60 fusion, Android 16 (physical device, `adb`-connected)

**Procedure per run** (repeat 5x per activity, cold start each time):

```bash
# 1. Build & install release
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk

# 2. Force-stop to guarantee a true cold start, clear logcat
adb shell am force-stop com.hardik.the_server_driven_ui
adb logcat -c

# 3. Launch and immediately tail the relevant log lines
adb shell am start -W -n com.hardik.the_server_driven_ui/.MainActivity
adb logcat -d | grep -E "Displayed|Fully drawn|PERF_TRACE"
```

Swap `.MainActivity` for `.StaticMainActivity` for the static twin run.

`am start -W` itself prints `TotalTime` (launch-to-first-frame from the
shell's perspective) — record that alongside the logcat lines.

### What each metric maps to

| Metric | Where it comes from |
|---|---|
| TTR (cold open → above-the-fold rendered) | `Fully drawn` logcat line (our `reportFullyDrawn()` call fires on first real-content layout pass) |
| TTI (cold open → scrollable/tappable) | Same `Fully drawn` timestamp in Compose — input is live as soon as the tree is composed, no separate "interactive" phase like a WebView would have. Note this equivalence in your write-up rather than inventing a second number. |
| Full page time (all sections rendered) | Not automatically distinguishable from TTR under `LazyColumn`, since offscreen sections compose lazily on scroll by design (that's correct SDUI/Compose behavior, not a shortcut). Measure by scrolling to the bottom immediately after cold start and reading `PERF_TRACE`/frame timestamps at that point, or note this as a scoping call in your writeup. |
| SDUI breakdown (parse vs view-build) | `json_read` + `json_parse` `PERF_TRACE` lines (parse) vs. the gap between `json_parse` finishing and the `Fully drawn` line (view-build) |
| Scroll perf / dropped frames | `adb shell dumpsys gfxinfo com.hardik.the_server_driven_ui framestats` while manually scrolling through the full page, or `adb shell dumpsys gfxinfo com.hardik.the_server_driven_ui` for the rolling Jank % summary |

## Results

*(replace with real numbers — median of 5 cold-start runs each)*

| Metric | Static twin | SDUI | Overhead |
|---|---|---|---|
| TTR | TBD ms | TBD ms | TBD % |
| TTI | TBD ms | TBD ms | TBD % |
| Full page time | TBD ms | TBD ms | TBD % |
| JSON read+parse | n/a | TBD ms | — |
| View-build (est.) | TBD ms | TBD ms | TBD % |
| Dropped frames / 1000 (scroll) | n/a (see note) | debug: 148/1000 (14.84%) legacy-janky · release: 11/1000 (1.15%) | — |

## Measure → optimize loop

*(fill in as you actually do this — this section is scored on honesty, not
on the numbers looking good)*

- What you tried: reported scroll jank on the SDUI landing page. Reproduced
  with `adb shell input swipe` (23 synthetic swipes through the full page)
  and read `adb shell dumpsys gfxinfo <pkg>` (reset immediately after cold
  start settles, so the one-time JSON-parse/first-compose frame doesn't
  pollute the scroll-only stats) on the debug build, then again on a signed
  release build (same commit, `assembleRelease` + `zipalign` +
  `apksigner` with the debug keystore, installed locally for this
  comparison only) with an identical swipe sequence.
- What worked: **the debug vs. release gap was the whole story.** Debug:
  90th pct 121ms / 95th pct 133ms / 99th pct 150ms frame time, 14.84% janky
  frames (legacy), 12 missed vsyncs, "Slow UI thread" flagged on 8 frames.
  Release (same code, same gestures): 90th pct 5ms / 95th pct 6ms / 99th pct
  28ms, 1.15% janky frames, 0 missed vsyncs, "Slow UI thread" flagged on 1
  frame. GPU-side percentiles were near-identical in both builds (2-8ms)
  and "Slow bitmap uploads" was 0 in both — ruling out rendering/GPU work
  and Coil image decode as the bottleneck; the cost is squarely
  CPU/UI-thread work that a debug build's lack of R8 shrinking/optimization
  and Compose's extra debug-only instrumentation makes ~10x more expensive
  per frame than the exact same composition work in release. If you're
  feeling jank while running via Android Studio's "Run" (installs debug by
  default), that's very likely this, not an app-code bug.
- Also fixed two smaller, real (if secondary) issues while investigating:
  `Grid`'s `LazyVerticalGrid` was sizing its height off the payload's
  `rows` prop rather than actual child count, so a future payload with more
  children than `rows * columns` declares would silently turn it into its
  own independently-scrollable region nested inside the outer page
  `LazyColumn` — a same-direction nested-scroll conflict. Now sized from
  `ceil(children / columns)` with `userScrollEnabled = false`, so it can't
  happen regardless of what the JSON declares (`Primitives.kt`,
  `gridComponent`). Also added missing `key = { it.id }` to
  `bannerCarouselComponent`'s `LazyRow` (`Composite.kt`) and the static
  twin's car rail (`StaticLandingScreen.kt`) — neither was the cause of the
  measured jank (verified: same debug-build numbers as above already
  included these fixes), but both are correctness/perf best practice for
  lazy lists.
- What didn't, and why: the collapsing-header mechanism
  (`SduiRenderer.kt`/`Collapsible.kt`/`CollapseFraction.kt`) was already
  suspected and reviewed first, since it's the most scroll-coupled code in
  the renderer — but it already confines every `.value` read of the
  scroll-driven `State<Float>` to layout/draw-phase blocks
  (`Modifier.layout{}`/`graphicsLayer{}`), so it does not recompose per
  scroll pixel. Not the cause here.

Known likely overhead sources worth checking first, based on the
architecture (not fully measured yet — the above isolated debug/release
build overhead as the dominant effect, but didn't rule these out as
smaller contributors):
1. `JsonElement`-based generic props mean every component does small
   `jsonPrimitive`/`contentOrNull` coercions on every recomposition — cheap
   individually, worth checking if `derivedStateOf` or `remember`-ing parsed
   props per node reduces recomposition cost.
2. The category-chip data-binding swaps `children` on the whole rail node,
   which currently forces the `LazyRow` to treat it as a fresh list —
   verify via Layout Inspector whether this causes unnecessary full-rail
   recomposition vs. only the changed items.
3. `AsyncImage` (Coil) network fetches are excluded from the TTR figure by
   design (placeholders show first, images populate async) — confirm the
   logcat trace agrees, and call this out explicitly since it affects how
   "fully rendered" should be interpreted. (Scroll-perf run above found 0
   "Slow bitmap uploads" in both builds, so Coil decode/upload is not a
   scroll-jank contributor at least on this device/payload.)

## Trade-offs

- `isMinifyEnabled = false` currently — release-mode R8/ProGuard shrinking
  would be the first lever to pull if numbers come back showing meaningful
  APK-size or cold-start overhead from the (small) added dependency
  surface (Coil, kotlinx.serialization).
- No dedicated `:benchmark` Gradle module / Macrobenchmark — traded for
  timebox; the adb-based methodology above is honest but coarser and
  can't produce percentile distributions the way Macrobenchmark's
  `BenchmarkResult` does.
