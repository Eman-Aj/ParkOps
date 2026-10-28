# Change log

## Starter 2026-09-15
Supplied infrastructure and unfinished exercise boundary.

## Student changes
Record D1 implementation, D1 feedback fixes, D2 additions and D3 changes here.
I forgot to write within the changelog all the changes I made to this start, as I didn't upload this to a Github repo
Moving forward in D2 and D3 there will be successful and populated changelogs of every new push/commit

## D2.2 — Operator approval and guarded execution (2026-10-28)
- Added proposal lifecycle states for pending, rejected, stale, and committed proposals.
- Added a separate terminal y/n operator decision after the model creates a pending proposal.
- Approval now rechecks proposal identity, booking version, policy version, and target eligibility before committing the reassignment.
- Rejection leaves the booking unchanged. Repeated approval of a committed proposal returns `DUPLICATE` without another notification record.
- Added terminal output for the final proposal state, booking snapshot, and notification record count.

## D2.3 — Periodic task detection (2026-10-28)
- Added a controllable `tick(seconds)` detector with an immediate first scan and 60-second scan boundaries.
- Detects bookings assigned to closed spaces, emits at most one work item per booking/policy version, and suppresses duplicate work while a proposal is active or after a failed attempt.
- Added an explicit operator retry hook for the current booking state and rejection of backwards clock values.
- Added `tick-demo` to demonstrate ticks at 0, 59, and 60 seconds plus backwards-clock rejection without sleeping.
