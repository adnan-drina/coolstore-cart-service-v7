# Migration Retro Proposals

## Brief updates (auto-applicable)
Concrete edits for REMAINING story briefs only (not the story just finished). For each change: name the brief file, quote the paragraph to add or replace. Empty list is fine if nothing should change.

**No brief updates needed for remaining S04/S05 stories based on this run's evidence.**

## Skill / harness proposals (human-only)

### Three costliest failure patterns from this run:

**1. Already Complete Task Spam (HIGHEST COST)**
Evidence: 4 "already_complete" events (T-001, T-003, T-005, T-006, T-007, T-008) consuming 377+ seconds of session time for work that was already satisfied. Example: T-005-a2p0 ran 377 seconds only to find dependencies already satisfied.
Root cause: Task pre-flight verification missing before dispatch.

**2. Sensor Red Post Commit (HIGH COST)**  
Evidence: 6 "sensor_red_post_commit" events requiring correction sessions. T-008 required 484-second correction session after initial work passed sensors but failed post-commit verification.
Root cause: Milestone sensors not running frequently enough to catch cumulative violations.

**3. Quota Exhaustion and Worker Kill (MEDIUM COST)**
Evidence: T-005-sfix consumed 902 seconds then killed (rc=124), representing lost work requiring restart. T-005 had "quota" events in retry loop.
Root cause: No early termination on quota approaching; workers run until hard kill.

### Concrete proposed skill/sensor changes:

**Pattern 1 Fix: PLANNING.md Pre-flight Enhancement**
**File:** `.hermes/skills/migration-harness/PLANNING.md`
**Section:** "M3 — plan (spec handoff)" 
**Current text (line 95-102):**
```
Every task changes code or tests. No ceremonial tasks ("final commit", "run validation", "prepare for gate") — commits happen per task and the gate runs in the factory; a task whose only product is a commit message or a report executes as an empty commit and wastes a session.
```
**Proposed addition:**
```
Pre-flight validation: Before creating any task, verify the destination state doesn't already satisfy the task goal. Check for: (a) dependency presence via `mvn dependency:tree` verification, (b) existing class files in target paths, (c) configuration keys already present. Tasks that would make no changes are marked "ALREADY COMPLETE" without dispatch.
```

**Pattern 2 Fix: EXECUTION.md Milestone Cadence**
**File:** `.hermes/skills/migration-harness/EXECUTION.md`
**Section:** "Verify-and-commit (orphan / retry) — no automatic second worker"
**Current text (line 262-266):**
```
Run the task sensor EXACTLY ONCE, immediately before the commit — not after every edit (each run is a full Maven cycle; sessions were measured spending 2–4 of them). Edit until you believe the work is done, run the sensor once, fix only what it reports, commit.
```
**Proposed enhancement:**
```
Milestone cadence: Run `.hermes/harness/sensors.sh milestone` every 3-4 tasks OR when any task shows "sensor_red_post_commit" pattern. Cumulative violations must be caught before commit, not corrected after. Post-commit sensor failures trigger immediate milestone sensor run to prevent cascade.
```

**Pattern 3 Fix: EXECUTION.md Quota Management**
**File:** `.hermes/skills/migration-harness/EXECUTION.md`
**Section:** "Escalation valve (budget exhausted)"
**Current text (line 333-345):**
```
Escalation valve (budget exhausted): before recording debt, you MAY implement the task directly with your own file tools — division of labor is the default, not an invariant.
```
**Proposed enhancement:**
```
Worker quota monitoring: Track worker runtime per task packet. Terminate workers approaching quota limits (e.g., >80% of timeout) and record partial completion as debt. Never allow workers to run to hard kill (rc=124) — early termination preserves session state for restart.
```

### Artifact review of this run's commits:

**Harveast Fidelity: GOOD**
- Domain models (S02) harvested cleanly with package rename com.redhat.coolstore → com.demo
- No evidence of fabricated classes or incorrect transformations
- All model files maintain legacy business logic preservation

**Story Scope: MIXED**  
- Most commits stayed within story boundaries
- T-008 sensor fix issues suggest scope creep in characterization tests
- S03 service layer commits appropriately modernized to @ApplicationScoped + constructor injection

**Fabrication: LOW RISK**
- No evidence of fabricated platform stubs or mock services
- CATALOG_ENDPOINT preservation correctly maintained across stories
- REST client conversion properly used @RegisterRestClient patterns

### Harness waste identification:

**Session Time Waste:**
- T-005-a2p0: 377 seconds on "already complete" verification
- T-005-sfix: 902 seconds before quota kill (rc=124)
- Total waste from already_complete + quota events: ~1,279 seconds (21+ minutes)

**Sensor Redundancy:**
- Post-commit sensors ran 6 times when milestone sensors could have caught issues earlier
- Each post-commit correction required full session restart

**Proposed waste reduction:**
1. Add pre-flight verification to eliminate already_complete dispatch
2. Increase milestone sensor frequency to catch issues before commit
3. Implement quota monitoring for early worker termination