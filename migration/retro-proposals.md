# Migration Retro Proposals — coolstore-cart-service-v7

## Brief updates (auto-applicable)

**Empty list** — All migration stories completed successfully:
- S01: Platform and BOM conversion ✓
- S02: Domain model harvest ✓  
- S03: Service layer modernization ✓
- S04: REST endpoint modernization ✓
- S05: Bootstrap and configuration cleanup ✓

No remaining briefs require updates.

## Skill / harness proposals (human-only)

### 1. Worker task execution timeout and budget exhaustion

**Evidence:** T-002-a1p0 session ran 1654 seconds (27+ minutes) before rc=0, T-005-sfix session ran 902 seconds (15 minutes) before rc=124, multiple quota retry events throughout run.

**Proposed change:** In `.hermes/skills/migration-harness/EXECUTION.md`, strengthen packet size discipline:

> **Packet size — one concern, bounded scope**
> A worker packet covers ONE concern and at most ~8 files or violation sites. Split anything larger into sequential packets. Large single packets push the worker (and you) into planning generations that outlast client timeouts; small packets complete in minutes and retry cheaply.
> **ADD:** Worker sessions exceeding 600 seconds MUST be split into smaller packets regardless of task classification. The supervisor should automatically interrupt and re-dispatch oversized packets with explicit "SPLIT REQUIRED" annotation.

### 2. Sensor failure cascade causing correction loops

**Evidence:** 10 `sensor_red_post_commit` events requiring verify cycles, 7 `style_autofix` events, 3 `sfix_committed_still_red` events indicating corrections didn't resolve the root cause.

**Proposed change:** In `.hermes/skills/migration-harness/EXECUTION.md`, replace post-commit sensor pattern:

> **Sensors: run the task sensor BEFORE you commit — never commit red**
> `sensors.sh task` green is a precondition of the commit, not a post-hoc check; a green-work-red-commit costs the session plus a correction session.
> **REPLACE with:** Run `.hermes/skills/migration-harness/sensors.sh task` BEFORE the worker commits, not after. If RED, fix in the SAME session before committing. Post-commit sensors create the correction loop evidenced by 10 sensor_red_post_commit → verify → style_autofix cascades.

### 3. Failed worker sessions with orphaned state

**Evidence:** T-001-a1p0 failed with rc=137 (early orphan_worker), T-005-sfix failed with rc=124, T-002-sfix failed with rc=124. Failed sessions left unclear tree state requiring cleanup.

**Proposed change:** In `.hermes/skills/migration-harness/EXECUTION.md`, strengthen failure handling:

> **Worker dispatch is synchronous — never background it**
> Run the `opencode run` command with a terminal timeout of at least 1800 seconds and WAIT for it to exit.
> **ADD:** On ANY non-zero worker return code, immediately run `git status --porcelain` and `.hermes/skills/migration-harness/sensors.sh task` BEFORE dispatching corrections. Failed sessions with rc=124, rc=137 left unclear state requiring supervisor intervention. Log tree state explicitly to prevent orphan confusion.

### 4. Artifact review — harvest fidelity and story scope

**Evidence:** From run-log.md, all 6 findings successfully resolved with 100% resolution rate. Review of commit patterns shows proper story ordering and dependency compliance.

**Assessment:** 
- **Harvest fidelity:** HIGH — All domain models (Product, ShoppingCart, ShoppingCartItem, Promotion) preserved with exact field structures and constructors
- **Story-scope discipline:** HIGH — Clean story boundaries (S01→S02→S03→S04→S05), no scope violations detected
- **Test coverage:** HIGH — Characterizations tests for business rules (25% discount on item 329299, free shipping ≥$75) properly pinned TARGET behavior
- **No fabrication detected:** All migrated classes trace back to legacy sources, no invented stubs

### 5. Harness waste identification

**Evidence:** 47 model sessions for a 5-story migration indicates high overhead:
- 11 retro sessions (unnecessary post-story overhead)
- Multiple re-dispatch patterns (T-005 appeared 4+ times)
- Long worker sessions (1654s, 902s) suggesting inefficient packet sizing
- 10 sensor correction cycles creating redundant verification

**Waste factors:**
- Excessive retro sessions between story gates
- Oversized worker packets causing timeouts
- Post-commit sensor pattern creating correction loops
- No early tree-state validation on failures

**Impact:** The run succeeded but at 9+ sessions per story vs. optimal 3-4, suggesting systematic efficiency improvements needed in packet sizing and sensor orchestration.

---

**Total sessions:** 47 | **Success rate:** 60% (28 success) | **Completion:** 100% findings resolved | **Deploy:** success: shipped, route 200, 4 products