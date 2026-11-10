# Test plan

## Scope

`test/junit/StudentTests.java` preserves the independent D1 checks and adds deterministic coverage for the D2 acceptance cases. `test/junit/SuppliedChecksTest.java` continues to run the supplied D1 regressions. `test/plain/InfrastructureChecks.java`, called by the JUnit wrapper, verifies the JSON codec, replay provider, and Ollama adapter against a localhost fake HTTP endpoint; it does not contact a real model.

## D1 regression coverage

| Behavior | Test coverage | State/result assertions |
|---|---|---|
| Duration and identifier rules | `ValidHours_1_24`, `InvalidHours_0_25`, `Invalid_Identifiers`; supplied `PublicChecks` | Valid endpoints accepted; invalid values throw `IllegalArgumentException`. |
| Pending B12 proposal | `B1Proposed_B12_Policy1`; supplied `D1-C` | B1/B12, versions 0/1, PENDING; booking and spaces unchanged; no commit or notification. |
| Occupied and unknown targets | `B1Proposed_D09`, `UnkownTarget`; supplied `D1-D`, `D1-E` | Each request is rejected and booking remains unchanged. |
| Protected snapshots | `ExternalObtainedSnapShot`; supplied `D1-F` | Caller cannot mutate the returned booking map; underlying state remains unchanged. |

## D2 acceptance cases

| Case | Test and stimulus | Explicit expected assertions |
|---|---|---|
| D2-A | `B1Proposed_B12_Policy1`: replay READ_BOOKING, LIST_ALTERNATIVES, PROPOSE B12 | Three model calls; pending proposal only; no booking, space, commit, or notification change; later histories contain earlier tool observations. |
| D2-B | `D2B_ExecutePendingProposalIsNotApprovedAndDoesNotMutate` | `NOT_APPROVED`; proposal remains PENDING; booking and spaces unchanged; zero commits/notifications. |
| D2-C | `D2C_ApproveThenExecuteCommitsOnce` | Approval alone changes no domain data; execution returns COMMITTED; booking moves to B12/version 1; old space is released, target occupied, one commit and one notification. |
| D2-D | `D2D_RepeatedExecutionIsDuplicateWithoutSecondCommitOrNotification` | Second execution returns DUPLICATE; proposal remains EXECUTED; booking, occupancy, commit count, and notification count remain at their post-commit values. |
| D2-E | `D2E_RejectThenExecuteDoesNotCommit` | Proposal becomes REJECTED; later execution returns NOT_APPROVED; booking and spaces unchanged; zero commits/notifications. |
| D2-F | `D2F_OldBookingVersionBecomesStaleAfterAnotherCommit` | A second proposal captured at version 0 becomes STALE after another proposal commits version 1; it causes no further booking or occupancy change. |
| D2-G | `D2G_TargetBecomingUnavailablePreventsExecution` | Occupy B12 after proposal/approval; execution returns UNAVAILABLE_OR_INELIGIBLE; proposal becomes STALE; booking unchanged and no commit/notification. |
| D2-H | `D2H_MalformedAndApprovalFieldRequestsAreCountedAndHaveNoAuthority` | Malformed JSON and an extra `approved` property are rejected and counted; later FINISH can proceed; no proposal or domain mutation is created. |
| D2-I | `D2I_UnknownToolAndWrongGoalAreRejected` | Unknown tool and wrong booking produce controlled observations, increment invalid count, and dispatch no operation; state unchanged. |
| D2-J | `D2J_SixInvalidResponsesReturnStepLimitAfterSixCalls`; `D2_CallLimitMustStayWithinOneAndSix` | Exactly six invalid responses yield STEP_LIMIT after six calls; limits 0 and 7 are rejected before provider invocation. |
| D2-K | `D2K_ProviderExceptionAndTimeoutReturnControlledProviderError` | Provider exception and timeout return PROVIDER_ERROR; one call counted; booking and commit/notification state unchanged. |
| D2-L | `D2L_TicksAtZeroFiftyNineAndSixtyEmitAtMostOneWorkItem` | Tick 0 scans/emits one item, 59 does not scan, 60 scans but emits no duplicate; booking/version and commit/notification state unchanged. |
| D2-M | `D2M_BackwardsTickIsRejectedWithoutChangingBooking` | After 0 then 60, tick 59 throws `IllegalArgumentException` with `clock moved backwards`; booking/version and counters unchanged. |
| D2-N | `results/d2-model-status.txt` and `InfrastructureChecks` | No `OLLAMA_CHAT_URL` or `OLLAMA_MODEL` is configured in this workspace. The required access-contingency status is recorded; the localhost fake endpoint tests the adapter contract, not a real model. |

## Recorded run results

Actual command output is retained in `results/` with date, command, and environment:

- `results/d2-checks.txt` — `.\run.ps1 check`; 14 supplied checks, 0 failures.
- `results/d2-junit.txt` — `.\run.ps1 test`; actual Jupiter and adapter-check output.
- `results/d2-periodic.txt` — `.\run.ps1 run tick-demo`; explicit ticks 0, 59, and 60 plus backwards-clock rejection.
- `results/d2-model-status.txt` — `.\run.ps1 run live`; `LIVE_RUN_PENDING_COURSE_ACCESS` because no course endpoint/model variables are configured. No paid access was used and no live transcript was fabricated.

- `results/d2-replay.txt` — `.\run.ps1 run agent-scripted`; deterministic replay transcript showing three tool calls and a pending proposal. This is a test-double demonstration, not a real model transcript.

The live-model transcript remains pending the course-supported endpoint/session. Replay is labelled as a test double and is not presented as a real model run.
