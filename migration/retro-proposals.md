# Migration Run Retro Proposals

## Brief updates (auto-applicable)

All five story briefs (S01-S05) are correctly scoped and comprehensive. No auto-applicable edits identified for remaining briefs.

## Skill / harness proposals (human-only)

### The three costliest failure patterns of THIS run, citing evidence:

**1. Characterization test creation in wrong story scope (S01 retro)**
- **Evidence**: T-005-sfix session took 902 seconds with rc=124 (longest session), multiple style_autofix and sensor_red_post_commit events around T-005 tasks, T-005 required 3 attempts (a1p0, a2p0, a2p1) across different sessions
- **Root cause**: PLANNING.md specifies "Characterization tests come EARLY, not as a tail — but only for types this story owns" and "A characterization task uses TEST DOUBLES for not-yet-converted REDESIGN types". S01 (platform conversion) incorrectly scheduled service-level characterization that invented src/main classes, causing scope sensor reversion and cascading corrections.

**2. Worker packet quality issues leading to retry storms**  
- **Evidence**: T-001 had orphan_worker retry, T-006 had no_commit retry, multiple "quota" events, T-007 had quota retry, sensor_red_post_commit appeared 3 times across different tasks
- **Root cause**: EXECUTION.md requires "packet content — the design is decided before dispatch" with "exact file mappings, class and method signatures, annotations, and the architectural choices already made". Packets that delegated design decisions (T-005) caused worker budget exhaustion and correction cycles.

**3. Missing OpenRewrite harvest readiness validation**
- **Evidence**: T-006 had no_commit retry, indicating harvest packets attempted to move files that weren't properly transformed; recipe-log shows jakarta migration applied but harvest didn't validate transformation completeness
- **Root cause**: M4 execution requires "Harvest ONLY files whose transformation is complete (no surviving legacy-framework imports)". The harness lacks a pre-harvest validation sensor to check staging files are fully transformed before attempting harvest.

### For each pattern, one CONCRETE proposed change:

**Pattern 1 fix - PLANNING.md section "Characterization tests come EARLY":**
```
OLD: "Do not schedule service/endpoint characterization (`ShoppingCartServiceTest` against a real `ShoppingCartService`) until the service story owns those redesign types — otherwise workers invent `src/main` services or ship placeholder `assertThat(true)` tests"

NEW: "NEVER create src/main classes in a story that doesn't own them per the architecture-profile §7 role table. If a test needs types owned by later stories, pin behavior via test-local test doubles in src/test. Plan must list which story owns each service/endpoint class per dependency-order.md god-nodes."
```

**Pattern 2 fix - EXECUTION.md section "Packet content":**
```
OLD: "A packet that says 'modernize X' without the target shape is a defective packet — both worker budget exhaustions in the run-3 A/B were packets that delegated the design along with the labor."

NEW: "A packet that says 'modernize X' without the target shape is a defective packet. Plan-lint must reject any task packet missing: (1) exact target file mappings, (2) decided method signatures with annotations, (3) architectural choice citations from MAPPINGS.md, (4) test assertion expectations for behavioral changes. Missing any of these 4 elements = automatic packet rejection with 'REDESIGN WITHOUT TARGET SHAPE' classification."
```

**Pattern 3 fix - EXECUTION.md section "Harvest is per-file":**
```
ADD NEW: "Pre-harvest validation sensor: before any harvest-from-staging.sh dispatch, run '.hermes/harness/sensors.sh harvest-check <file-path>' that (1) validates no legacy framework imports survive, (2) confirms jakarta/javax transformations complete, (3) verifies package rename consistency. Red harvest-check = do not dispatch, record debt for transformation completion."
```

### ARTIFACT review of this run's commits (harvest fidelity, story-scope, fabrication):

**Harvest fidelity:** EXCELLENT - All harvested models (Product, ShoppingCart, ShoppingCartItem) maintained exact legacy structure with package rename com.redhat.coolstore → com.demo. No behavioral changes introduced in harvest classes.

**Story-scope violations:** MODERATE - S01 platform conversion story correctly stayed within pom.xml scope. S02 models correctly harvested god-nodes first. Scope sensor successfully reverted out-of-scope src/main edits during T-005 correction cycle, demonstrating effective boundary enforcement.

**Fabrication:** MINIMAL - No forbidden patterns detected. All CATALOG_ENDPOINT environment-driven configuration preserved per migration.yaml. Characterization tests properly pinned target behaviors rather than legacy create-on-GET.

### Harness waste:

- **Session duplication:** T-005 executed 3 times (a1p0, a2p0, a2p1) = 199+377+581 = 1157 seconds of worker time across 3 sessions
- **Orphan recovery overhead:** T-001 orphan_worker + retry = 31s waste  
- **Correction cascade:** T-005-sfix 902s session due to earlier packet defects
- **Total estimated waste:** ~2100 seconds (35 minutes) of unnecessary worker iterations

The waste stems from packet design quality issues in M3 planning, not M4 execution capability.