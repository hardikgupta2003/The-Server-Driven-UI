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

## Three prompt → outcome stories

### 1. "write json schema design and setting up the module structure for me"

**Outcome**: `SCHEMA.md` plus the full `sdui/` package (models, registry,
renderer, state, actions, components) in one pass, verified against a real
`./gradlew :app:compileDebugKotlin` run rather than shipped on inspection
alone.

**What got rejected/rewritten**: the first draft of the unknown-component
debug placeholder (`UnknownComponent.kt`) invented its own `Int.dp()`
helper with a broken operator-overload chain instead of just using
Compose's existing `Int.dp` extension property — a case of the AI adding
incidental complexity for something the platform already provides for
free. Caught immediately because it wouldn't have compiled anyway; rewrote
to plain `8.dp`, `12.dp` literals. Same pass, a `SectionTitle` composable
came out with a nonsensical chained-`Modifier.let{}` sequence that did
nothing — rewritten to a plain `Modifier.padding(...)` call. Neither was
subtle; both are the kind of "generated something that looks like code but
doesn't reduce to anything sensible" output that's worth scanning for
specifically, not just trusting because it type-checked eventually.

### 2. Designing how a chip selection changes a car rail's content

**Outcome**: the `dataBinding` variant-map mechanism in `SCHEMA.md` — the
server precomputes every variant (`suv`/`sedan`/`hatchback`/`muv`), the
client just picks one based on state.

**What got rejected**: the more "obviously general" first instinct for this
— and the direction several real SDUI frameworks actually take — is a
small expression/templating language (`{{if category == 'suv'}}...`) so
the client can filter/compute against arbitrary conditions. That was
deliberately **not** built. Reasoning: it's a much bigger surface to get
right (parsing, evaluation, error cases) for marginal benefit inside an
8–10 hour budget, and the variant-map approach still satisfies the brief's
actual requirement ("a tab/chip selection that changes content") without
needing a runtime evaluator at all. This is the one design rejection in
the project that wasn't a bug catch — it was scoping a fancier-sounding
option down on purpose, which is exactly the kind of judgment call worth
being explicit about rather than quietly defaulting to whichever version
sounds more impressive.

### 3. "you do it i want to get code in my working directory"

**Outcome**: the SDUI branch committed and fast-forward merged into the
user's actual project folder (`main`), moving work out of an isolated git
worktree the user hadn't asked for and couldn't see in Android Studio.

**What went wrong and got caught**: the first commit attempt used a
heredoc commit message containing `->` arrow characters; the shell
misparsed it and `git commit` silently printed its own usage/help text
instead of committing — no commit was created. This wasn't caught by
assuming success; it was caught by treating "the output doesn't look like
a normal commit confirmation" as a signal to immediately re-run `git log`
and `git status` and confirm nothing had landed, then re-committing with a
plainer message. A second, unrelated surprise in the same stretch: something
outside this session's control (most likely Android Studio's VCS sync)
checked the user's main folder out from `main` to `development` right
after the merge — caught the same way, via `git reflog`, not assumed away.

## One AI failure

**Where**: Compose's `LazyRow`/`LazyColumn`/`LazyVerticalGrid` list-based
`items(...)` DSL is implemented as an **extension function** that must be
explicitly imported (`androidx.compose.foundation.lazy.items`, and
separately `androidx.compose.foundation.lazy.grid.items` for grids). Code
was generated calling `items(node.children, key = { it.id }) { ... }`
inside several component files without that import present.

**Why it went wrong silently at first**: without the import, Kotlin didn't
raise "unresolved reference" — it resolved `items` to a *different*,
also-valid overload (`LazyListScope.items(count: Int, ...)`), then reported
the failure several layers downstream as `Argument type mismatch: actual
type is 'List<SduiNode>', but 'Int' was expected` in four unrelated files
at once. That's a platform-API failure mode specifically because the
overload exists and is plausible, not a straightforward typo — a purely
visual code review of any single file would not obviously flag it.

**How it was caught**: running `./gradlew :app:compileDebugKotlin` after
finishing the module and reading the actual compiler output, rather than
assuming a set of individually-plausible-looking Compose files would link.
The fix was mechanical once diagnosed (add the missing imports in
`SduiRenderer.kt`, `Primitives.kt`, `Interactive.kt`, `Composite.kt`), but
the failure mode itself is exactly why "the AI wrote that part" isn't
sufficient verification — the compiler is the actual check, and it was
run after every non-trivial file addition in this project, not just once
at the end.

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
  behavior — which is what caught both the misparsed commit message and
  the external branch-switch described above.
- **Read the unknown-component and action-dispatch paths against the
  brief's actual requirement** ("must never crash") rather than trusting
  they work because the happy path compiles — traced the registry-miss
  branch and the malformed-action branch explicitly rather than assuming
  defensive-looking code (`as? String ?: default`) is automatically
  correct everywhere it's used.
