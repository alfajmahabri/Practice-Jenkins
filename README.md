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
