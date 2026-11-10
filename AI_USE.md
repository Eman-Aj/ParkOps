# Student AI-use disclosure


| 2026-09-23 to 2026-09-28 | OpenAI Codex (GPT-6) | Understand and implement the D1 booking/proposal path; discuss UML relationships and sequence diagrams; explain how to run and record the scripted checks. | `src/Booking.java`, `src/ParkingSpace.java`, `src/Proposal.java`, `src/ProposalService.java`, `src/DomainRules.java`, `src/StudentApplication.java`, `AI_USE.md` | I used Codex to explain the D1 objective, identifier regular expressions in `DomainRules`, and distinctions such as `StudentApplication` versus an external actor. 


| 2026-10-1 | OpenAI Codex (GPT-6) | Correctly formatting TEST_PLAN | `TEST_PLAN.md`| Used codex to format my TestPlan, I worked better by just brute forcing the test before writing a plan, so I asked it to format the tests that I've already created


| 2026-10-10 | OpenAI Codex (GPT-6) | Cleanly formatted ReadMe | `README.md`| Used codex to fix my old README.md file so there is a proper flow and clear objective as to what my application should do.

| 2026-10-28 | OpenAI Codex (GPT-6) | Implement the D2 four-tool model loop, operator approval and guarded execution, and controllable periodic detection; explain the acceptance requirements and run compilation/replay demonstrations. | `src/StudentApplication.java`, `src/Main.java`, `src/Booking.java`, `src/Proposal.java`, `src/ProposalService.java`, `src/PeriodicTaskDetector.java`, `src/PeriodicTickResult.java`, `src/PeriodicWorkItem.java`, `README.md`, `CHANGELOG.md`, `AI_USE.md` | I reviewed and adjusted the generated implementation in this working session. Compilation succeeded; replay rejection/approval and periodic tick demonstrations were run. No live model was used. |

| 2026-11-08 | OpenAI Codex (GPT-6) | Draft D2.4 diagrams and concise design-choice explanations based on the current Java implementation; create visual PNGs and editable SVGs. | `design.md`, `diagrams/d2-sequence.svg`, `diagrams/d2-sequence.png`, `diagrams/d2-proposal-state.svg`, `diagrams/d2-proposal-state.png`, `CHANGELOG.md`, `AI_USE.md` | I reviewed the diagrams against the current source, rendered the SVGs to PNG, and visually inspected both images. |

| 2026-11-10 | OpenAI Codex (GPT-6) | Add D2-A–M deterministic acceptance tests, preserve D1 regressions, correct approve/execute separation and guarded space occupancy changes, refresh test documentation, and record model-access contingency. | `src/ProposalService.java`, `src/StudentApplication.java`, `src/ParkingSpace.java`, `test/junit/StudentTests.java`, `TEST_PLAN.md`, `README.md`, `CHANGELOG.md`, `results/d2-*.txt`, `design.md`, `diagrams/`, `AI_USE.md` | I reviewed generated changes and ran compile, plain checks, the JUnit suite including localhost fake-HTTP adapter checks, periodic demo, and live-mode access check. No real model or paid service was used. |


The supplied starter was my starting point into this ParkOps application, I do not claim any of the intitial domain rules and student application frameworks were of my design only. I only further implemented and specialized it to the task and my level of understanding.

I remain responsible for reviewing, understanding, and explaining all submitted code. The assistance entries above describe the portions where AI contributed.

