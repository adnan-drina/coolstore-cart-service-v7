# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-service-v7:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 25 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: 704c249c; run base: 3729183
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

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     16 success
      4 story_gate_pass
      4 pipeline_succeeded
      3 no_commit
      2 style_autofix
      2 sensor_red_post_commit
      2 quota
      1 preflight_red
      1 orphan_worker
      1 already_complete
```
