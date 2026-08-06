# PERF.md

Status: **all numbers below (cold-start and scroll-jank) are now measured
on the same physical device** — a Motorola Edge 60 Fusion, Android 16 —
so there is no emulator caveat on the reported overhead %. Two earlier
emulator runs (Pixel 10 Pro AVD) are kept further down for the historical
record, since the second of those runs is what caught a real static-twin
equivalence bug worth documenting, but neither is used for the final
overhead claim anymore.

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
- [`PerfTrace.kt`](sdui/src/main/java/com/hardik/the_server_driven_ui/sdui/perf/PerfTrace.kt)
  adds two extra markers around the JSON read + parse step specifically, so
  the SDUI breakdown (parse vs view-build) is derivable from the same log.

Both are real, reproducible, adb-only measurements — just coarser than a
proper Macrobenchmark harness. If time allows later, swapping this for a
`:benchmark` module is the natural next step (noted in trade-offs below).

## Methodology

**Build**: release build, both activities in the same APK, same install, no
`isMinifyEnabled` change needed for this to be meaningful (currently
`false` — see trade-off note). `assembleRelease` is unsigned by default
(no `signingConfig` on the `release` block); for local measurement the
APK was `zipalign`'d and signed with the debug keystore, same as the
scroll-jank comparison below.

**Device (all numbers reported below)**: Motorola Edge 60 Fusion, Android
16, physical device, `adb`-connected wirelessly over Wi-Fi. Both the
cold-start (TTR/TTI/breakdown) and scroll-jank measurements now come from
this same physical device — no emulator numbers are used in the final
overhead claim.

**Superseded emulator runs**: two earlier passes were run on a Pixel 10
Pro AVD (emulator) before the physical device was used for cold-start
measurement — kept below under "Results" for the historical record,
since the second one is what caught a real static-twin equivalence bug
(see that section). Emulator timing has known distortions relative to
physical hardware (no thermal throttling, host-CPU contention, virtualized
graphics), which is part of why this was upgraded to physical-device
measurement rather than left as the final answer.

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

**The authoritative numbers are the physical-device run at the bottom of
this section** ("Physical device run — Motorola Edge 60 Fusion"). The two
emulator runs above it are kept for the historical record — the first is
superseded outright (unfair comparison, see below), the second is a real
measurement but on an emulator, upgraded to physical hardware rather than
reported as final.

**Superseded run below (kept for the historical record, not used for the
overhead claim)**: the first pass at this benchmark predates a rewrite of
`StaticLandingScreen.kt`. That static twin turned out to be a stale,
visually-unrelated mock (different layout, no header, no collapsing
behavior, ~8 sections of invented content) rather than an actual
hardcoded copy of `landing_page.json` — caught when actually comparing
screenshots of the two side by side. It's since been rewritten to mirror
the JSON's real header (same collapsing-scroll mechanism), all 7 nav
tabs, same colors/copy/images. The numbers immediately below reflect
*that* old, non-equivalent static build and should be disregarded for
the overhead claim — re-measured against the corrected twin further
down.

<details>
<summary>Original (superseded) run</summary>

Median of 5 cold-start runs each, release build, Pixel 10 Pro emulator
(`am start -W` + `Displayed`/`Fully drawn` logcat lines). Per-run raw data:

| Run | Static `Fully drawn` (ms) | SDUI `Fully drawn` (ms) | SDUI json_read (ms) | SDUI json_parse (ms) |
|---|---|---|---|---|
| 1 | 406 | 653 | 2.09 | 23.24 |
| 2 | 418 | 613 | 1.33 | 20.61 |
| 3 | 402 | 598 | 1.77 | 20.58 |
| 4 | 422 | 583 | 1.46 | 21.43 |
| 5 | 438 | 583 | 1.35 | 22.29 |
| **Median** | **418** | **598** | **1.46** | **21.43** |

TTR overhead computed as +43.1% (418ms static vs. 598ms SDUI) — but
against a static twin that rendered roughly half as much content as the
SDUI page, so this number understated what a fair comparison should
show and is not used below.

</details>

### Re-run against the corrected static twin (emulator — superseded by the physical-device run below)

Same methodology, same device (Pixel 10 Pro emulator, `emulator-5554`),
same session, 5 cold-start runs each, immediately back-to-back with the
original run above so environmental conditions (emulator host load,
thermal state) are as comparable as this setup allows:

| Run | Static `Fully drawn` (ms) | SDUI `Fully drawn` (ms) | SDUI json_read (ms) | SDUI json_parse (ms) |
|---|---|---|---|---|
| 1 | 953 | 1202 | 26.56 | 69.77 |
| 2 | 907 | 1228 | 27.15 | 75.90 |
| 3 | 956 | 1220 | 26.52 | 68.46 |
| 4 | 980 | 1239 | 29.30 | 77.15 |
| 5 | 1030 | 1235 | 26.82 | 75.56 |
| **Median** | **956** | **1228** | **26.82** | **75.56** |

View-build (`json_parse` finish → `Fully drawn`), per-run: 534, 541, 540,
547, 550 ms → median **541 ms**. Process init (process start → `json_read`
start): 598, 611, 611, 614, 609 ms → median **611 ms** — present in both
variants, not SDUI-specific, included in TTR since that's what a cold
open actually feels like.

| Metric | Static twin | SDUI | Overhead |
|---|---|---|---|
| TTR (`Fully drawn`) | 956 ms | 1228 ms | **+28.5%** |
| TTI | 956 ms (same instant) | 1228 ms (same instant) | +28.5% |
| Full page time | not distinguishable from TTR under `LazyColumn` (see methodology note) | same | — |
| JSON read+parse | n/a | ~102.4 ms (median read+parse combined) | — |
| View-build (est.) | ~956 ms total (no JSON step to subtract) | ~541 ms | — |
| Dropped frames / 1000 (scroll, physical device) | n/a (see note) | debug: 148/1000 (14.84%) legacy-janky · release: 11/1000 (1.15%) | — |

**Two honest observations, not smoothed over:**

1. **Absolute times roughly doubled vs. the original run** (SDUI 598ms →
   1228ms, static 418ms → 956ms), for both variants proportionally. That
   points at something environmental — emulator host contention or a
   cold Coil/image-decode cache after reinstall — rather than a code
   regression, since both variants moved together. This is exactly the
   kind of number that would need re-verifying on physical hardware
   before being cited as a real latency claim (see the emulator caveat
   under Methodology).
2. **The overhead percentage went *down* (43.1% → 28.5%) even though the
   static twin now renders roughly 2x more content** (17 sections vs.
   ~8, plus 6 additional full tab bodies it didn't have before). That's
   counterintuitive enough to flag rather than quietly accept: it means
   the fixed per-launch costs (process init, JSON read+parse, first
   composition pass) are a larger share of the *old* static twin's much
   smaller total, so the relative gap looked bigger when the comparison
   itself was unfair. With a properly-equivalent static twin, SDUI's
   real per-node dispatch tax is still visible (~102ms JSON read+parse +
   a view-build phase that, unlike the previous run, is now *faster*
   than static's own view-build — 541ms vs. static's ~956ms total —
   which is itself suspicious and likely means the two builds' Coil
   image-loading/caching state differed at measurement time rather than
   SDUI's renderer being genuinely faster than hand-written Compose).
   Bottom line: the JSON parse cost (~75ms) is real and attributable to
   SDUI; the rest of the gap is noisy enough on this emulator that it
   should be re-measured on physical hardware, several more times, and
   ideally with image loading excluded/mocked before treating the
   view-build comparison as conclusive.

### Physical device run — Motorola Edge 60 Fusion, Android 16 (authoritative)

Same methodology and APK, same corrected static twin, 5 cold-start runs
each, connected over `adb` via Wi-Fi:

| Run | Static `Fully drawn` (ms) | SDUI `Fully drawn` (ms) | SDUI json_read (ms) | SDUI json_parse (ms) |
|---|---|---|---|---|
| 1 | 870 | 1247 | 3.01 | 36.80 |
| 2 | 883 | 1173 | 2.71 | 35.75 |
| 3 | 875 | 1269 | 2.72 | 36.64 |
| 4 | 871 | 1243 | 2.75 | 35.47 |
| 5 | 880 | 1224 | 2.65 | 36.59 |
| **Median** | **875** | **1243** | **2.72** | **36.59** |

View-build (`json_parse` finish → `Fully drawn`), per-run: 685, 650, 711,
710, 688 ms → median **688 ms**. Process init (process start →
`json_read` start), per-run: 525, 487, 521, 497, 500 ms → median
**500 ms** — present in both variants, included in TTR since that's what
a cold open actually feels like.

| Metric | Static twin | SDUI | Overhead |
|---|---|---|---|
| TTR (`Fully drawn`) | 875 ms | 1243 ms | **+42.1%** |
| TTI | 875 ms (same instant) | 1243 ms (same instant) | +42.1% |
| Full page time | not distinguishable from TTR under `LazyColumn` (see methodology note) | same | — |
| JSON read+parse | n/a | ~39.2 ms (median read+parse combined) | — |
| View-build (est.) | ~875 ms total (no JSON step to subtract) | ~688 ms | — |
| Dropped frames / 1000 (scroll) | n/a (see note) | debug: 0/1530 (0.00%) legacy-janky · release: 6/1516 (0.40%) — re-verified after the `@Immutable`/keys/collapse-fraction fixes, see "Measure → optimize loop" | — |

**This is the number to cite: SDUI cold-start overhead is +42.1% on real
hardware**, no emulator caveat attached. Reading it honestly:

- **JSON read+parse (~39ms) is the smallest, cleanest attributable SDUI
  cost** — real, small, and exactly what you'd expect from parsing a
  ~250KB payload once per cold start.
- **The bulk of the gap (~1243 − 875 − 39 ≈ 329ms) sits in view-build** —
  the generic component-registry dispatch + recomposition path costing
  more per node than static's purpose-built Composables reading fields
  directly. That's the real, structural SDUI tax this architecture pays,
  not a measurement artifact — unlike the emulator run above, where
  SDUI's view-build looked *faster* than static's (a sign of session
  noise), on physical hardware SDUI's view-build (688ms) is slower than
  static's total time (875ms) once you account for static having no
  separate parse phase to subtract — consistent with the architecture,
  not contradicting it.
- **The physical run's absolute numbers are close to the second emulator
  run's** (1243ms vs. 1228ms TTR for SDUI; 875ms vs. 956ms for static),
  which is reassuring: it suggests the earlier emulator numbers weren't
  wildly unrepresentative, just carrying an avoidable caveat. The percent
  overhead moved from 28.5% (emulator) to 42.1% (physical) mostly because
  static's physical-device time (875ms) came in lower relative to SDUI's
  (1243ms) than on the emulator — a reminder that overhead *percentage*
  is more sensitive to session noise than the absolute ms gap is, and why
  this doc reports both rather than just the headline percentage.

## Measure → optimize loop

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

### Re-verification on physical hardware, after the `@Immutable`/keys/collapse-fraction fixes

The debug/release comparison above was measured *before* the later commit
that annotated every SDUI model `@Immutable`, moved the collapse fraction
to a stable `State<Float>` read only in layout/draw phase, and added the
remaining missing list keys (see `AI_WORKFLOW.md`'s "One AI failure").
Re-ran the identical 23-swipe procedure on the Motorola Edge 60 Fusion
against the current build, both debug and release:

| Build | Total frames | Janky (legacy) | 50th/90th/95th/99th pct |
|---|---|---|---|
| Debug | 1530 | 0 (0.00%) | 6/7/7/7 ms |
| Release | 1516 | 6 (0.40%) | 5/6/6/15 ms |

**The debug-vs-release gap has effectively disappeared.** Both builds are
now smooth — debug is no longer ~10x more expensive per frame than
release, because the fixes reduced *recomposition volume* at the source
(stable inputs the Compose compiler can actually skip, no per-pixel
recompose from the collapse fraction, correct list diffing), rather than
relying on R8/release-mode stripping to paper over expensive-but-avoidable
recomposition. This is a stronger result than the original finding: the
original jank wasn't only a debug-build artifact, it was a real
architectural issue that debug build's lack of optimization happened to
expose first and release build's R8 happened to mostly hide.

One measurement quirk worth flagging rather than hiding: `dumpsys gfxinfo`
reported **"Number High input latency"** at nearly the total frame count
in both runs (1529/1530 debug, 3030/1516\* release — \*this counter isn't
reset by `reset` the same way frame stats are, so it's cumulative across
the session, not just this run). This metric tracks input-event-to-frame
latency, and `adb shell input swipe`-injected touch events are not real
touchscreen samples — they don't carry the same timing metadata a
physical finger does, so this counter is not a meaningful jank signal for
synthetic-input testing specifically. The frame-time percentiles and
janky-frame counts (which don't depend on input timestamps) are the
numbers actually being reported above.

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

### A methodology gap found after the fact, not re-measured yet

While auditing the codebase for consistency (unrelated to a specific perf
complaint), `StaticLandingScreen.kt`'s `reportFullyDrawn()`/`PerfTrace.mark`
call was found attached to the *inner* body `LazyColumn`'s own modifier,
while `MainActivity.kt`'s equivalent call is attached to the `modifier`
passed into `SduiPage(...)` — which, because `landing_page.json` defines a
`header`, `SduiPage` applies to the *outer* `Column` wrapping header + body,
not the inner list (see `SduiRenderer.kt`'s `SduiPage` branching). That means
the two variants' `Fully drawn` timestamps were not firing at strictly the
same structural point: the SDUI version's marker included the header's
first layout pass, the static version's did not. Fixed by moving the static
marker to the outer `Column`, matching the SDUI path exactly.

**This was not re-measured before writing this note.** The physical-device
numbers above (TTR 875ms static / 1243ms SDUI, +42.1% overhead) were
captured *before* this fix, so they carry a small, currently-unquantified
asymmetry — the static number likely undercounts its own header's
first-frame cost slightly relative to the SDUI number. Given the header is
a small, fast-to-compose region (a location row + search bar + nav chips,
no network images), this is expected to be a minor correction, not one that
flips the conclusion — but "expected to be minor" is a claim, not a
measurement, and should be re-run before citing these numbers as final.

## Trade-offs

- `isMinifyEnabled = false` currently — release-mode R8/ProGuard shrinking
  would be the first lever to pull if numbers come back showing meaningful
  APK-size or cold-start overhead from the (small) added dependency
  surface (Coil, kotlinx.serialization).
- No dedicated `:benchmark` Gradle module / Macrobenchmark — traded for
  timebox; the adb-based methodology above is honest but coarser and
  can't produce percentile distributions the way Macrobenchmark's
  `BenchmarkResult` does.
