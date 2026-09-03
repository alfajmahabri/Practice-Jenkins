# Practice-Jenkins

This is a dummy project created so that I could practice Jenkins CI/CD.

There is no real application here and nothing in this repository is meant to be
shipped. Everything exists only as material to build pipelines against.

## Why this repository exists

While working through this, I have asked an AI model to come up with different
scenarios so that I could understand how Jenkins is used in MNCs with huge
Jenkins pipelines — multi-stage builds, shared libraries, parallel stages,
multibranch setups, approvals, promotions across environments, and the kind of
failures that show up in a large pipeline but never in a small one.

## What I want to get out of it

- Hands-on practice with Jenkins pipelines rather than just reading about them.
- A feel for how pipelines are structured at scale in large organisations.
- Practice on my Root Cause Analysis skills: when a stage fails, work backwards
  from the symptom to the actual cause instead of guessing or re-running the job.

## How I plan to use it

Each scenario gets added here as its own case — the setup, what breaks, and the
RCA I worked out for it. The pipelines are intentionally allowed to fail so
there is something to investigate.

## The dummy application

A small Java 17 / Maven service called `order-service`. It reserves stock for
orders and prices them. There is no database and no network call — it exists
only to give the pipeline something real to compile, test and package.

```
pom.xml                          build definition
config/checkstyle.xml            static analysis rules
src/main/java/com/practice/jenkins/
    App.java                     entry point, runs a scripted scenario
    Calculator.java              arithmetic helpers
    Order.java                   order line item with validation
    InventoryService.java        in-memory stock tracking
    PricingService.java          discount, tax and shipping rules
src/test/java/com/practice/jenkins/
    ...Test.java                 39 JUnit 5 tests
```

### Commands

| Command | What it does |
| --- | --- |
| `mvn clean compile` | compiles, runs Checkstyle in the `validate` phase |
| `mvn test` | runs the unit tests, writes JUnit XML to `target/surefire-reports` |
| `mvn clean verify` | the full build: Checkstyle, tests, JaCoCo report, JAR, coverage gate |
| `java -jar target/order-service-1.0.0.jar` | runs the packaged application |

### What the build produces

- `target/order-service-1.0.0.jar` — runnable JAR
- `target/surefire-reports/*.xml` — test results
- `target/site/jacoco/` — coverage report (HTML and XML)
- `target/checkstyle-result.xml` — static analysis results

### Build gates I can trip on purpose

These are the knobs for creating failures worth doing an RCA on:

- **Checkstyle** fails the build on any violation, at `warning` severity.
- **JaCoCo** fails `verify` below 70% line coverage. Current coverage is
  around 98%, so raising `jacoco.line.coverage` in `pom.xml` is an easy way to
  break the build.
- **Surefire** fails on any failing test. `PricingService` has boundary values
  (bulk discount at 10 units, free shipping at 500) that break in interesting
  ways when nudged.

### No Jenkinsfile

There is deliberately no `Jenkinsfile` in this repository. I am writing that
myself — that is the whole point of the exercise.

---

## Scenarios

### Task 1 — Boundary value bug

**Setup:** The `FREE_SHIPPING_LIMIT` in `PricingService.java` was changed from `500.0` to `499.0`, shifting the free-shipping boundary.

**Symptom:** `PricingServiceTest.shippingIsChargedBelowTheLimit` failed — expected `40.0` but got `0.0` for `shippingFor(499.99)`.

**Root cause:** The test data (`499.99`) was between the old limit (`500.0`) and the new limit (`499.0`), so the code correctly treated it as free shipping. The test wasn't updated to match the new config.

**Fix:** Updated the test input from `499.99` to `498.99` so it falls below the new threshold and shipping is charged as expected.

### Task 2 — Checkstyle violation (line too long)

**Setup:** A new method `addThreeNumbers` was added to `Calculator.java` with a Javadoc line exceeding 120 characters.

**Symptom:** Validation stage failed — `Line is longer than 120 characters (found 202)` at `Calculator.java:14`.

**Root cause:** The Javadoc description had a single line at 202 characters, violating the Checkstyle `LineLength` rule.

**Fix:** Wrapped the Javadoc comment across multiple lines so each is under 120 characters.

### Task 3 — Coverage drop (untested code)

**Setup:** `clear()` and `reserveBulk()` were added to `InventoryService.java` without tests. The JaCoCo threshold was raised from 70% to 95% to expose the gap.

**Symptom:** Coverage stage failed — `lines covered ratio is 0.82, but expected minimum is 0.95`.

**Root cause:** `InventoryService` dropped to 48% coverage because `clear()` and `reserveBulk()` (with branching logic) were entirely untested.

**Fix:** Added 3 tests — `clearRemovesAllStock`, `reserveBulkReservesAllWhenStockIsSufficient`, and `reserveBulkRollsBackOnPartialFailure` — covering both happy path and rollback logic.

### Task 4 — Compilation error (return type mismatch)

**Setup:** `Calculator.add()` was changed from `int` return to `void` — simulating a refactor that broke callers.

**Symptom:** Test stage failed — callers expected `int` but got `void`.

**Root cause:** The Compile stage runs `mvn compile` (main code only) and passed because `add()` itself compiled fine. The mismatch only surfaced during `mvn test` when test code was compiled. The pipeline's Compile stage should compile both main and test sources (`mvn compile test-compile`) to catch this earlier.

**Fix:** Reverted `add()` return type back to `int`.

### Task 5 — JDK mismatch in Jenkins

**Setup:** The project targets Java 17 (`maven.compiler.release` is set to `17` in `pom.xml`), but the Jenkins agent is configured to use JDK 11 or the Java 17 tool is missing.

**Symptom:** The build fails during `Compile` or `Test` with errors like `error: invalid target release: 17` or `release version 17 not supported`.

**Root cause:** The Jenkins job is running Maven on the wrong Java version. The code is valid for Java 17, but the runner does not have the correct JDK installed or configured.

**Fix:** Install and configure JDK 17 on the Jenkins agent, then set the correct `JAVA_HOME` or Jenkins tool configuration so the pipeline runs with Java 17.

This is a fresh scenario and is not a repeat of the earlier business-rule or boundary-value failures. It is a classic DevOps problem: the code is fine, but the execution environment is wrong.

### Task 6 — Wrong deployment artifact path

**Setup:** The `Package` stage produces a runnable JAR, but the `Deploy` stage points to the wrong filename, such as `target/order-service-1.0.0-SNAPSHOT.jar` instead of the actual `target/order-service-1.0.0.jar`.

**Symptom:** The pipeline reaches the deployment stage, then fails with a Java runtime error or `Error: Unable to access jarfile` because the file path is invalid.

**Root cause:** The deploy command contains a typo or stale artifact name. The build itself succeeded, but the deployment step is using a non-existent artifact.

**Fix:** Correct the artifact path to match the actual packaged JAR, then rerun the pipeline. This is a classic pipeline-level issue: the app builds successfully, but deployment references the wrong artifact.

This is a new failure pattern and is distinct from the earlier boundary-value, logic, coverage, checkstyle, and environment issues.

### Task 7 — Missing deployment environment variable

**Setup:** The `Upload` stage tries to send the packaged JAR to a remote endpoint using `VPS_UPLOAD_URL`, but the variable is not defined in the Jenkins job or credentials configuration.

**Symptom:** The pipeline fails in the `Upload` stage with a `curl` error such as `URL using bad/illegal format or missing URL` or a remote connection failure.

**Root cause:** The app builds successfully, but the pipeline depends on a missing environment variable. The issue is configuration and deployment context, not application code.

**Fix:** Define `VPS_UPLOAD_URL` in Jenkins with the correct endpoint, or pass it as a secret/parameter to the job before the upload step runs.

This task is intentionally different from the earlier code and artifact-path failures. It represents a very common real-world DevOps issue: the pipeline is fine, but the deploy environment is not configured.

### Task 8 — Missing deployment directory

**Setup:** The `Deploy` stage tries to copy the packaged JAR to `/opt/deployments/live/order-service.jar`, but that directory does not exist on the Jenkins agent or target host.

**Symptom:** The pipeline fails during deployment with an error such as `cp: cannot create regular file '/opt/deployments/live/order-service.jar': No such file or directory`.

**Root cause:** The application artifact is valid and the pipeline reached the deployment stage, but the destination directory was never created or is not mounted on the host.

**Fix:** Create the destination path before copying the artifact, or update the deployment script to use a valid target directory that exists in the environment.

This is a fresh deployment-stage problem and is not a repeat of the earlier code, test, or artifact-name issues.

## Expanded application architecture

The project now includes a small order-processing workflow around the original
inventory and pricing services:

- `OrderWorkflowService` coordinates reservations, pricing, single-order
  decisions, batch processing, revenue totals, and status counts.
- `ProcessedOrder` records the order, outcome, payable total, and rejection
  reason.
- `OrderStatus` provides explicit accepted and rejected outcomes.
- `OrderMetrics` calculates acceptance rate and average accepted order value.

The new classes have focused tests for accepted orders, rejected orders,
inventory preservation, batch processing, read-only results, and operational
metrics. This gives the repository a more realistic service layer while keeping
the Jenkins pipeline unchanged until the failure is understood.

### Task 9 — Batch status count misclassifies orders

**Setup:** A change is made to the new batch-processing code so
`OrderWorkflowService.countByStatus()` counts accepted results regardless of
the requested status.

**Symptom:** The Test stage fails in `OrderWorkflowServiceTest` because the
rejected-order count is reported as the accepted-order count.

**Root cause:** The status calculation ignores its `status` argument and uses
the accepted predicate unconditionally.

**Fix:** Compare each result's status with the requested `status`, then rerun
the tests and the full Maven verification lifecycle.

This is a new application-layer scenario. It is not a repeat of the earlier
shipping boundary, equality, Checkstyle, coverage, JDK, artifact path, missing
environment, or missing-directory exercises.

### Task 10 — Jenkinsfile syntax validation failure

**Setup:** A closing brace is accidentally removed from the declarative
`Jenkinsfile`.

**Symptom:** Jenkins rejects the pipeline before any stage starts, with a
Groovy parser or pipeline compilation error such as `unexpected EOF` or
`expecting '}'`.

**Root cause:** The pipeline definition is not syntactically complete. This is
different from a failed build stage: Maven, tests, and the application never
run because Jenkins cannot compile the pipeline script.

**Fix:** Compare the block structure of `pipeline`, `stages`, `post`, and each
stage. Restore the missing closing brace, use Jenkins Pipeline Syntax or a
pipeline linter, and rerun the job.

This is a parser-level Jenkins failure. It is not a repeat of an application
logic failure, test assertion, artifact path issue, environment issue, or
deployment-target issue.

### Task 11 — Post-build reports deleted before archiving

**Setup:** `cleanWs()` is moved to the beginning of the `post { always { ... } }`
block, before JUnit results and JaCoCo reports are collected.

**Symptom:** The build may complete its stages successfully, but Jenkins shows
missing test reports or archived coverage artifacts. With no reports left in
the workspace, `junit` may find nothing and `archiveArtifacts` may create an
empty archive.

**Root cause:** Workspace cleanup runs before post-build evidence is published.
The cleanup step removes `target/surefire-reports` and `target/site/jacoco`.

**Fix:** Publish JUnit results and archive coverage artifacts first, then call
`cleanWs()` as the final action in the `always` block. Preserve evidence before
deleting the workspace.

This is a new post-build observability failure, not a test, application,
artifact-name, deployment, environment, or Jenkinsfile-parser failure.

### Task 12 — Parallel stage writes to the same report path

**Setup:** A Jenkins pipeline is updated to run a `unit-tests` stage and a
`coverage-report` stage in parallel. Both stages write to the same output file,
for example `target/reports/summary.txt` or `target/site/coverage/summary.json`.

**Symptom:** The pipeline may fail intermittently with `Permission denied`,
`No such file or directory`, or stale/corrupted report output. One stage may
report success while the other overwrites the same artifact at the same time.

**Root cause:** The pipeline is using a shared workspace and shared artifact path
without isolating output directories. Parallel execution introduces a race
condition where file writes overwrite each other, and later archive or publish
steps consume the wrong data.

**Fix:** Give each parallel stage a unique output directory or a stage-specific
workspace path, such as `target/unit-reports` and `target/coverage-reports`,
and only archive the correct files after the stage completes. This is a
concurrency and filesystem-isolation issue, not a Java logic problem, JDK issue,
Maven configuration problem, or deployment failure.

This is a fresh pipeline-parallelism failure and is distinct from all earlier
boundary-value, checkstyle, coverage, compile, environment, artifact-path,
deployment, directory, post-build, and parser issues.

### Task 13 — Shared mutable state causes order-dependent test failures

**Setup:** A new singleton cache is added to the pricing or order workflow layer
to memoise expensive calculations. It is stored as a static field and never
cleared between runs.

**Symptom:** The build passes when tests are run individually, but fails when the
full suite runs in a different order. A later test can see values produced by an
earlier test, resulting in wrong totals, stale discounts, or inconsistent stock
reservations.

**Root cause:** The program is using shared mutable state across requests and
across test cases. Because the cache is static and not reset, test order leaks
into the application state, which makes the suite flaky and non-deterministic.

**Fix:** Remove the shared static cache or scope it to a request instance,
explicitly clear it before each test or each calculation cycle, and rerun the
full Maven verification. This is a state-management bug, not a boundary-value,
Checkstyle, coverage, JDK, artifact, deployment, directory, parser, or parallel-
stage issue.

This is a new class of failure: flaky state leakage across test execution
order. It is different from all previous problems in this repository and is a
realistic production bug that often shows up only under full-suite or parallel
runs.
