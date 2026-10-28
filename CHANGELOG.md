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
