# Test plan

## Scope and result-recording status

This plan reflects the test source currently in `test/junit/StudentTests.java` and the supplied cases in `test/plain/PublicChecks.java` (also wrapped by `test/junit/SuppliedChecksTest.java`). The workspace does not contain the assignment PDF briefs, so the D1 letter mapping below follows the behavior named in `PublicChecks`; verify the labels against the brief before submission. The D2-A–N case-by-case wording is not available here, so those letters are marked for mapping when the brief is available rather than guessed.

**Actual results:** No test command was run while preparing this plan. Each result is therefore recorded as **Not run**. Do not treat an existing assertion or an empty test method as a passing result. After running tests, replace these entries with the observed outcomes.

## D1 cases

### D1-A — Duration bounds

- **Preconditions:** None; duration validation is independent of application state.
- **Input:** Durations 1 and 24; invalid durations 0, 25, and -1.
- **Expected:** 1 and 24 are accepted; 0, 25, and -1 throw `IllegalArgumentException`.
- **Actual:** Not run.
- **Source/test method:** `DomainRules.requireDuration`; `PublicChecks` cases `duration endpoints` and `invalid duration ...`; independent Jupiter methods `StudentTests.ValidHours_1_24` and `StudentTests.InvalidHours_0_25` cover 1, 24, 0, and 25, but not -1.

### D1-B — Identifier format

- **Preconditions:** None.
- **Input:** Valid examples `B1`, `A_1-2`, and `ABCDEFGHIJKLMNOP`; invalid examples null, empty, lowercase/incorrectly formed values, and an overlength value.
- **Expected:** Valid identifiers are accepted; invalid identifiers throw `IllegalArgumentException`.
- **Actual:** Not run.
- **Source/test method:** `DomainRules.requireIdentifier`; `PublicChecks` cases `valid identifiers` and `invalid identifier ...`; `StudentTests.Invalid_Identifiers` checks null and empty booking/space IDs, but does not independently test the full format boundaries.

### D1-C — Pending B12 proposal; capture versions; no mutation

- **Preconditions:** Fresh `StudentApplication` using the supplied synthetic scenario; B1 is at A17/version 0; policy version is 1; B12 is available.
- **Input:** `app.propose("B12")`.
- **Expected:** A valid application-generated proposal ID, booking B1, target B12, status `PENDING`, observed booking version 0 and policy version 1. Every booking field remains unchanged, including ID, space ID, duration, version, and accessibility requirement.
- **Actual:** Not run. The independent test `StudentTests.B1Proposed_B12_Policy1` is currently empty and contains no assertions.
- **Source/test method:** `StudentApplication.propose`; `ProposalService.propose`; `PublicChecks` case `D1-C pending B12, no booking change`. The supplied case checks the snapshot before/after and specifically checks A17/version 0.

### D1-D — Occupied D09 rejected without mutation

- **Preconditions:** Fresh application; D09 exists but is occupied.
- **Input:** `app.propose("D09")`.
- **Expected:** `IllegalArgumentException`; every booking field remains unchanged.
- **Actual:** Not run.
- **Source/test method:** `StudentApplication.propose`; `ProposalService.propose`; `PublicChecks` case `D1-D occupied D09 rejected unchanged`. `StudentTests.B1Proposed_D09` is only a setup stub: it does not construct the service, invoke `propose`, assert rejection, or check unchanged booking fields.

### D1-E — Unknown target rejected without mutation

- **Preconditions:** Fresh application with the supplied spaces; target does not exist.
- **Input:** `app.propose("Z99")`; the independent test also tries null, `MadeUpTarget`, and `S11`.
- **Expected:** `IllegalArgumentException`; every booking field remains unchanged after each rejected input.
- **Actual:** Not run.
- **Source/test method:** `StudentApplication.propose`; `ProposalService.propose`; `PublicChecks` case `D1-E unknown target rejected unchanged` checks the full snapshot for Z99. `StudentTests.UnkownTarget` checks exceptions for its inputs but does not verify booking state after rejection.

### D1-F — Protected booking snapshot

- **Preconditions:** A booking or fresh application exists.
- **Input:** Obtain a snapshot and attempt to change its `spaceId` or `id` entry.
- **Expected:** The returned map rejects modification with `UnsupportedOperationException`; the underlying booking remains unchanged.
- **Actual:** Not run.
- **Source/test method:** `Booking.snapshot`; `StudentApplication.bookingSnapshot`; `StudentTests.ExternalObtainedSnapShot` checks that a `Booking` snapshot rejects a put. `PublicChecks` case `D1-F protected snapshot` attempts to change the application snapshot and checks A17 remains, but tolerates the exception rather than explicitly requiring it.

## Running and recording results

Run `./run.ps1 check` for the supplied plain Java cases and `./run.ps1 test` for the JUnit suite. Save the actual output and exit status in the appropriate results file; update the **Actual** entries above from that output. The supplied tests are useful regression checks, but they do not replace the independent Jupiter tests required by the brief.
