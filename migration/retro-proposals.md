# Migration Run Retro Proposals

## Brief updates (auto-applicable)

No brief updates required. The remaining story briefs (S02-S05) are correctly scoped and contain sufficient detail for their respective modernization tasks.

## Skill / harness proposals (human-only)

### (1) The three costliest failure patterns of THIS run, citing evidence

**Pattern 1: Retry overhead from "no_commit" and "quota" events**
Evidence: 
- `retro-events.csv`: T-005 (2 retries: no_commit → quota → success), T-007 (1 retry: quota)
- `retro-metrics.csv`: T-005-a2p0 (377s), T-005-a2p1 (581s) - extended sessions due to retries
- `run-report.md`: 3 no_commit events, 2 quota events out of 25 sessions = 20% of sessions required retries
- Cost: ~958 seconds of wasted worker time (T-005 alone) + supervisor orchestration overhead

**Pattern 2: Sensor red post-commit forcing verification cycles**
Evidence:
- `retro-events.csv`: T-003, T-005 both had sensor_red_post_commit followed by style_autofix
- `run-report.md`: 2 sensor_red_post_commit events
- Cost: Each red sensor triggers a full correction session, adding milestone boundaries mid-task

**Pattern 3: Orphan worker / session instability**
Evidence:
- `retro-events.csv`: T-001 orphan_worker event requiring retry
- `run-report.md`: 1 orphan_worker event
- Cost: Complete session failure + retry cycle for T-001

### (2) For each pattern one CONCRETE proposed change to a specific skill or sensor — quote exact text and name file/section

**Pattern 1 Fix - PLANNING.md §5.3 "Packet content" enhancement:**
```
PROPOSED ADDITION: "Packet size — one concern, bounded scope"
"Characterization-test packets (S01 retro: all four escalations were
this task class) additionally carry: (1) the specific legacy test cases
to port WITH their exact expected assertion values quoted; (2) the
instruction that expectations are the contract — never adjusted to
match code; (3) scope bounded to one class; (4) when the exercised
logic is out of story scope, pin values via a TEST-LOCAL expectation
helper — never invent src/main classes.

PREVENTIVE MEASURE: Add budget exhaustion detection. Before finalizing
packets, estimate token requirements based on file count and complexity.
If estimated tokens > 8000 for worker coding, split into sequential
packets. T-005's 581-second session indicates packet over-size."
File: `.hermes/skills/migration-harness/PLANNING.md`
Section: "Packet content — the design is decided before dispatch"
```

**Pattern 2 Fix - EXECUTION.md §7 "Sensors" pre-commit validation:**
```
PROPOSED CHANGE: "Run the task sensor EXACTLY ONCE, immediately before the commit — not after every edit"
"CHANGE TO: Run the task sensor BEFORE dispatch to catch pre-commit issues. 

SENSOR PRE-FLIGHT: Before dispatching any infer/rewrite packet, run 
`.hermes/harness/sensors.sh task` on the current state. If red, fix 
before dispatching worker. This prevents the pattern where worker 
completes, commits, then supervisor marks red post-commit.

VERIFICATION: Change sensor timing from post-commit to pre-dispatch 
for all coding tasks."
File: `.hermes/skills/migration-harness/EXECUTION.md` 
Section: "Run the task sensor EXACTLY ONCE, immediately before the commit"
```

**Pattern 3 Fix - EXECUTION.md §3 "Dispatch rules" worker stability:**
```
PROPOSED ADDITION: "Worker dispatch is synchronous — never background it"
"STABILITY CHECK: Before dispatching opencode run, verify no residual 
worker processes exist. Add orphan detection:

```bash
# Check for residual workers before dispatch
if pgrep -f "opencode run" > /dev/null; then
  echo "Residual worker detected, waiting for completion..."
  sleep 30
  # Escalate if still running after 30s grace period
fi
```

HEALTH MONITOR: Track worker session completion rates. If orphan_worker 
rate > 5%, investigate platform stability before continuing runs."
File: `.hermes/skills/migration-harness/EXECUTION.md`
Section: "Worker dispatch is synchronous — never background it"
```

### (3) ARTIFACT review of this run's commits (harvest fidelity, story-scope, fabrication)

**Artifact Quality Issues Identified:**

**a) Empty/ceremonial commits:** 6 commits were pure status verification with no code changes:
- `T-002`: "Already satisfied (Quarkus Maven plugin configured correctly, no Spring Boot plugin references)" - pom.xml verified correct
- `T-005`: "Already satisfied (SmallRye Health dependency present, no Spring Boot Actuator)" - pom.xml verified correct  
- `T-007`: "ALREADY COMPLETE (quarkus-rest-jackson present, no Jersey dependencies)" - pom.xml verified correct
- `T-008`: "ALREADY COMPLETE (Quarkus test dependencies present, no Spring Boot test dependencies)" - pom.xml verified correct
- `T-011`: "COMPLETED (already satisfied)" - pom.xml: verified clean of Spring Boot artifacts
- `T-012`: "Already satisfied (worker verified clean tree; O-ESCW)"

**b) Minimal artifact commits:** Several commits changed only 1-2 lines:
- `98ec219 T-006`: 1 line change in pom.xml (2 symbols)
- `adae400 T-005`: 1 line addition to migration/run-log.md
- `403fcbd T-007`: 1 line addition to migration/run-log.md
- `b7bc3ca T-008`: 1 line addition to migration/run-log.md

**c) Over-engineered acceptance commits:** 
- `af62849 T-009`: Created ceremonial AcceptanceEndpoint + tests (49 lines) then `b2e97df` removed them in next commit as "HOLD S01" - wasteful back-and-forth

**d) Good artifact commits:**
- `bb4e90c T-001`: Real 2-line pom.xml change (Spring Boot parent → Quarkus BOM)
- `c08c56d T-003`: 10-line pom.xml Maven plugin updates
- `1e7c6a0 T-004`: 14-line pom.xml native profile addition

**Fidelity Assessment:** HIGH for S01 platform scope - all commits stayed within POM modernization boundary. However, efficiency was low due to ceremonial commits.

### (4) Harness waste

**Quantified waste:**
- **~1,200 seconds** wasted on retry cycles (T-005's 958s + T-007's 235s retry)
- **6 ceremonial commits** that added no functionality (20% of 30 commits)
- **49 lines created then deleted** in acceptance endpoint back-and-forth (T-009 → T-010 → removed)
- **2 sensor cycles** that could have been avoided with pre-dispatch validation
- **1 orphan worker** that required complete retry

**Total estimated session waste:** ~25% of the 25 sessions involved some form of inefficiency

**Root cause:** The S01 story was overly broad, mixing genuine modernization tasks (T-001, T-003, T-004, T-006) with verification tasks that should have been auto-completed. Platform conversion stories should pre-filter for "already satisfied" items before dispatching to workers.