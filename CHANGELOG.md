# Change log

## Starter 2026-09-15
Supplied infrastructure and unfinished exercise boundary.

## Student changes
Record D1 implementation, D1 feedback fixes, D2 additions and D3 changes here.
I forgot to write within the changelog all the changes I made to this start, as I didn't upload this to a Github repo
Moving forward in D2 and D3 there will be successful and populated changelogs of every new push/commit

## D2.2 — Operator approval and guarded execution (2026-10-28)
- Added proposal lifecycle states for pending, approved, rejected, stale, and executed proposals.
- Added a separate terminal y/n operator decision after the model creates a pending proposal.
- Execution rechecks proposal identity, booking version, policy version, and target eligibility before committing the reassignment.
- Rejection leaves the booking unchanged. Repeated execution of an executed proposal returns `DUPLICATE` without another notification record.
- Added terminal output for the final proposal state, booking snapshot, and notification record count.

## D2.3 — Periodic task detection (2026-10-28)
- Added a controllable `tick(seconds)` detector with an immediate first scan and 60-second scan boundaries.
- Detects bookings assigned to closed spaces, emits at most one work item per booking/policy version, and suppresses duplicate work while a proposal is active or after a failed attempt.
- Added an explicit operator retry hook for the current booking state and rejection of backwards clock values.
- Added `tick-demo` to demonstrate ticks at 0, 59, and 60 seconds plus backwards-clock rejection without sleeping.

## D2.4 — Workflow and proposal diagrams (2026-11-08)
- Added model/tool, operator decision, guarded execution, duplicate handling, and periodic detection sequence diagrams to `design.md`.
- Added the proposal lifecycle state diagram and brief explanations of two design choices with alternatives.
- Exported visual PNG diagrams with editable SVG sources under `diagrams/` and embedded them in `design.md`.

## D2.5 — Acceptance tests and model-access record (2026-11-10)
- Split proposal approval/rejection from execution; execution of pending or rejected proposals returns `NOT_APPROVED`.
- Recheck current booking/policy versions, proposal identity, and target eligibility during execution; release old-space occupancy and occupy the target on commit.
- Keep commit records separate from notification records and return `DUPLICATE` without a second update.
- Added JUnit coverage for D2-A through D2-M while preserving D1 regression tests; recorded the D2-N pending course-access status because no supported endpoint/model is configured.
- Recorded actual check, JUnit, replay, periodic trace, and live-access status output under `results/`.
