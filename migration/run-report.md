# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-service-v7:
success: shipped, route 200, 4 products. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 48 model sessions.

- Outcome: success: shipped, route 200, 4 products
- Supervisor version: 033f3f6f; run base: 85ef4059e7f4b6567c7121ffb56a523d8b8be83e
- Orchestrator: custom:maas-m2/minimax-m2; worker: qwen27b/qwen3-6-27b

## Sessions

| session | seconds | rc |
|---|---|---|
| T-001-a1p0 | 31 | rc=137 |
| T-005-a1p0 | 46 | rc=0 |
| T-006-a1p0 | 131 | rc=0 |
| T-006-a2p0 | 32 | rc=0 |
| T-009-a1p0 | 163 | rc=0 |
| T-011-a1p0 | 149 | rc=0 |
| T-014-a1p0 | 133 | rc=0 |
| T-015-a1p0 | 77 | rc=0 |
| T-017-a1p0 | 343 | rc=0 |
| m5-evaluate-a1p0 | 354 | rc=0 |
| retro | 104 | rc=0 |
| T-003-sfix | 330 | rc=0 |
| T-005-a1p0 | 199 | rc=0 |
| T-005-a2p0 | 377 | rc=0 |
| T-005-a2p1 | 581 | rc=0 |
| m5-evaluate-a1p0 | 38 | rc=0 |
| retro | 37 | rc=0 |
| T-002-a1p0 | 136 | rc=0 |
| T-002-a2p0 | 30 | rc=0 |
| T-005-a1p0 | 46 | rc=0 |
| T-007-a1p0 | 235 | rc=0 |
| T-007-a1p1 | 93 | rc=0 |
| T-008-a1p0 | 69 | rc=0 |
| T-011-a1p0 | 95 | rc=0 |
| m5-evaluate-a1p0 | 65 | rc=0 |
| retro | 60 | rc=0 |
| T-005-sfix | 902 | rc=124 |
| m5-evaluate-a1p0 | 61 | rc=0 |
| retro | 36 | rc=0 |
| T-001-a1p0 | 71 | rc=0 |
| T-002-a1p0 | 53 | rc=0 |
| T-005-sfix | 323 | rc=0 |
| T-007-a1p0 | 468 | rc=0 |
| T-008-sfix | 484 | rc=0 |
| m5-evaluate-a1p0 | 105 | rc=0 |
| retro | 59 | rc=0 |
| T-001-a1p0 | 602 | rc=0 |
| T-002-a1p0 | 1654 | rc=0 |
| T-002-sfix | 903 | rc=124 |
| T-003-a1p0 | 584 | rc=0 |
| T-003-sfix | 511 | rc=0 |
| m5-evaluate-a1p0 | 713 | rc=0 |
| deployfix-r1-a1p0 | 769 | rc=0 |
| deployfix-r2-a1p0 | 1145 | rc=0 |
| retro | 110 | rc=0 |
| T-006-sfix | 396 | rc=0 |
| m5-evaluate-a1p0 | 119 | rc=0 |
| retro | 62 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     28 success
     11 pipeline_succeeded
     10 sensor_red_post_commit
      8 already_complete
      7 style_autofix
      6 story_gate_pass
      3 sfix_committed_still_red
      3 no_commit
      3 debt_recorded
      2 quota
      2 acceptance_pass
      1 scope_violation
      1 preflight_red
      1 pipeline_
      1 orphan_worker
```
