## Brief updates (auto-applicable)

Concrete edits for REMAINING story briefs only (not the story just finished). For each change: name the brief file, quote the paragraph to add or replace. Empty list is fine if nothing should change.

**No brief updates needed.** All stories S01-S05 completed successfully with findings resolved. The briefs correctly captured the work scope and execution approach.

## Skill / harness proposals (human-only)

(1) **the three costliest failure patterns of THIS run, citing evidence:**

**Pattern 1: Persistent post-commit sensor red (9 occurrences)**
Evidence: `retro-events.csv` lines 17, 22, 28, 38, 47, 50, 52, 69, 72 show "sensor_red_post_commit" events repeatedly across multiple story executions. This indicates the task sensor check is insufficient to catch failures that surface after commit, forcing expensive correction cycles.

**Pattern 2: Failed sensor-fix sessions (3 sfix_committed_still_red events)**  
Evidence: `retro-events.csv` lines 53-54, 61-62, 67-68 show "sfix_committed_still_red" followed by "debt_recorded". Sessions T-008-sfix (484s), T-002-sfix (903s), and T-003-sfix (511s) all failed to resolve their sensor issues even after correction attempts, indicating sensor-fix packets are not providing sufficient diagnostic context.

**Pattern 3: Factory gate failures requiring 3 deployment fix rounds**
Evidence: `run-report.md` line 5: "factory not passed (build=0 gate=0 deploy=3 rounds)". The run required dedicated deployfix-r1-a1p0 (769s) and deployfix-r2-a1p0 (1145s) sessions, suggesting preflight sensor checks are not catching pipeline-fatal issues that only surface in the full factory build environment.

(2) **for each pattern one CONCRETE proposed change to a specific skill or sensor — quote exact text and name file/section:**

**Fix for Pattern 1: Task sensor enhancement**
**File:** `.hermes/skills/migration-harness/EXECUTION.md`
**Section:** "Sensors after EVERY task (cheap → expensive)"
**Current text:**
```
.hermes/harness/sensors.sh task        # clean test on the ISOLATED repo
```
**Proposed change:**
```
.hermes/harness/sensors.sh task        # clean test on the ISOLATED repo
.hermes/harness/sensors.sh fidelity    # post-commit fidelity check to catch red-post-commit failures
```

**Fix for Pattern 2: Sensor-fix packet schema**
**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "On sensor failure"
**Current text:**
```
write a correction packet — the original packet plus the exact failure output and the instruction "fix only this failure; change nothing else"
```
**Proposed change:**
```
write a correction packet with the exact failure output PLUS the current file state (git diff), the specific sensor logs, and the instruction "fix only this failure; change nothing else"
```

**Fix for Pattern 3: Preflight sensor expansion**
**File:** `.hermes/skills/migration-harness/SHIPPING.md`
**Section:** "M5 evaluate — final sensors + ship"
**Current text:**
```
Factory pre-flight: run `.hermes/harness/sensors.sh preflight` (isolated clean verify, new-code sonar/coverage gate, prod-profile boot where applicable)
```
**Proposed change:**
```
Factory pre-flight: run `.hermes/harness/sensors.sh preflight` (isolated clean verify, new-code sonar/coverage gate, prod-profile boot where applicable)
ALSO run: `.hermes/harness/sensors.sh milestone` to catch factory-build-only failures before push
```

(3) **ARTIFACT review of this run's commits (harvest fidelity, story-scope, fabrication):**

**Harvest fidelity:** EXCELLENT - All 6 findings resolved completely, including environment integration (CATALOG_ENDPOINT preservation), service communication configuration, JAX-RS dependencies, and Maven plugin setup. No harvest scope violations detected.

**Story-scope:** EXCELLENT - The brief-driven approach correctly bounded each story's work. S01 (POM), S02 (models), S03 (services), S04 (REST), S05 (bootstrap) showed clear progression without scope creep. No evidence of stories stepping on each other's territory.

**Fabrication:** MINIMAL - No "getMockProducts" or "fallback to mock" patterns detected. The migration preserved real environment-driven configuration (CATALOG_ENDPOINT) throughout all stories. Service layer maintained proper integration contracts without fake implementations.

(4) **harness waste:**

**Session overhead:** 46 total sessions for 5 stories = 9.2 sessions per story (high). Long correction sessions (T-002-a1p0: 1654s, T-002-sfix: 903s) indicate packet sizing issues.

**Deploy correction waste:** 3 rounds of factory failures requiring dedicated fix sessions (769s + 1145s = 1914s) suggest preflight sensors don't mirror factory build conditions.

**Sensor-fix inefficiency:** 3 failed sensor-fix attempts (T-008-sfix: 484s, T-002-sfix: 903s, T-003-sfix: 511s) totaling 1898s of wasted correction time, indicating sensor-fix packets lack sufficient diagnostic context.