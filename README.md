# ParkOps

ParkOps is a Java 21 course project. The D1 example uses synthetic data for booking B1 and demonstrates checking a requested parking space and creating a pending reassignment proposal. It does not approve or execute that proposal.

## Requirements

- Java Development Kit (JDK) 21
- Windows PowerShell for the commands below

Open this folder in VS Code, then choose **Terminal → New Terminal**. Run the commands from the project folder. The scripts also accept `COURSE_JAVA_HOME` or `JAVA_HOME` if Java 21 is not on `PATH`.

## Run the D1 example

```powershell
.\run.ps1 run scripted
```

The scripted input requests B12 for booking B1. The program should display a `PENDING` proposal with booking version 0 and policy version 1, then show that the booking remains at A17, version 0. This command uses a scripted fixture; it is not a live AI run and does not prompt for an arbitrary target.

To save the output in `results/d1.txt` while running the example:

```powershell
.\run.ps1 run scripted > .\results\d1.txt 2>&1
```

Open `results/d1.txt` in VS Code to view the recorded output. The `>` writes terminal output to the file, and `2>&1` includes error output.

## Run checks

Run the supplied D1 checks:

```powershell
.\run.ps1 check
```

This includes checks for valid durations and identifiers, a pending B12 proposal, rejection of occupied D09 and unknown targets, and a protected booking snapshot. A successful run ends with `FAILED 0` and exits with code 0.

To save those results:

```powershell
.\run.ps1 check > .\results\d1-checks.txt 2>&1
```

The JUnit suite includes the supplied checks and tests in `test/junit/StudentTests.java`. Set up the pinned JUnit dependency once, then run the suite:

```powershell
.\setup-junit.ps1
.\run.ps1 test
```

Compilation alone can be checked with:

```powershell
.\run.ps1 compile
```

## Current scope

The application initializes the synthetic booking and spaces in `StudentApplication`; it currently does not load `data/scenario.json`. The D1 scripted command requests B12. `D09` and unknown-target rejection are covered by the supplied `check` cases.

## Periodic detection demonstration

Run `.\run.ps1 run tick-demo` to advance the detector through explicit times 0, 59 and 60 seconds and then demonstrate rejection of a backwards clock. There is no background scheduler or real-time wait. The first tick scans immediately; later ticks scan at the next 60-second boundary. A booking/policy version pair produces at most one automatic work item and an active proposal suppresses another. After a failed attempt, call `retryPeriodic("B1")` explicitly to allow one new work item for the current state; a changed booking or policy version is a distinct state and can be detected independently.

All scenario data is synthetic. Follow the matching course brief for milestone requirements, diagrams, evidence, and submission instructions. Record actual test output in `results/`; do not report a check as passing unless it was run.
