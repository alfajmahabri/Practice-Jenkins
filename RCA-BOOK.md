# Jenkins RCA Book

This is the working root-cause-analysis guide for the Practice-Jenkins
training repository. Use it to investigate failures from evidence instead of
re-running jobs blindly.

## Investigation discipline

1. Identify the first failed stage. Later stages may be skipped or misleading.
2. Capture the exact error, test name, file, and line number.
3. Reproduce the smallest failing command locally.
4. Classify the failure as code, test, build configuration, agent environment,
   infrastructure, or deployment target.
5. Check the recent diff and the generated files before changing anything.
6. Make the smallest fix that addresses the root cause.
7. Run the focused check first, then the full lifecycle.
8. Commit the fix with a message that explains the cause, not only the symptom.

Useful commands:

```sh
mvn test
mvn verify
java -version
mvn -version
git status --short
git diff HEAD~1 --
find target -maxdepth 2 -type f -print
```

## Quick classification table

| Symptom | First place to check | Likely category |
| --- | --- | --- |
| A JUnit assertion fails | Test report, test name, production method | Application logic or contract |
| `release version 17 not supported` | `java -version`, Jenkins tool config | Agent environment |
| Checkstyle violation | Reported file and line, `config/checkstyle.xml` | Static analysis |
| Coverage below minimum | JaCoCo report and untested branches | Test coverage gate |
| `Unable to access jarfile` | Package output and deploy path | Pipeline artifact reference |
| `cp: ... No such file or directory` | Source artifact and destination directory | Deployment filesystem |
| `curl` URL or connection error | Variable value, endpoint, service, port | Deployment configuration or target |

## RCA records

### Test failure: shipping boundary

**Symptom:** `PricingServiceTest` fails around the free-shipping amount.

**Check:** Compare `FREE_SHIPPING_LIMIT` in `PricingService` with the test
input and expected result. Verify whether the business rule intentionally
changed.

**Resolution:** If the new limit is intentional, update the boundary test to a
value that is definitely below or above the new limit. If it was accidental,
restore the original limit. Do not change an assertion merely to make CI green.

### Test failure: order equality

**Symptom:** `OrderTest.comparesOnIdOnly` reports that two orders with the same
id are not equal.

**Check:** Inspect both `equals()` and `hashCode()` in `Order`. The test defines
order identity by `id`, even when SKU and price differ.

**Resolution:** Compare by `id` in both methods. Equal objects must have equal
hash codes; changing only one method creates collection bugs.

### Validation failure: Checkstyle

**Symptom:** Validation reports a line longer than the configured limit or
another style violation.

**Check:** Open the exact file and line named by Checkstyle, then inspect the
rule in `config/checkstyle.xml`.

**Resolution:** Make the smallest formatting or naming correction and rerun
`mvn checkstyle:check`. Do not disable the rule to hide the failure.

### Coverage failure: JaCoCo gate

**Symptom:** `mvn verify` reports that the covered line ratio is below
`jacoco.line.coverage`.

**Check:** Open the JaCoCo report and identify new branches or methods without
tests. Confirm whether the threshold was changed recently.

**Resolution:** Add behavior-focused tests for happy paths, boundaries, and
failure/rollback paths. Lowering the threshold should require a deliberate
quality decision, not be the default fix.

### Test compilation failure: return type mismatch

**Symptom:** Main compilation passes, but test compilation fails because a
caller expects a different return type.

**Check:** Compare the changed method signature with every caller. Remember
that `mvn compile` does not compile test sources.

**Resolution:** Restore the public contract or update all callers as part of an
intentional API change. Consider compiling test sources in the Compile stage
with `mvn compile test-compile`.

### Agent failure: wrong JDK

**Symptom:** Maven reports `release version 17 not supported` or `invalid target
release: 17`.

**Check:** Compare `java -version` and `mvn -version` with
`maven.compiler.release` in `pom.xml` and the JDK configured in Jenkins.

**Resolution:** Configure the Jenkins agent to use a compatible JDK, then
verify the version in the job log. Do not change the project target just to
match an incorrectly configured agent.

### Deploy failure: wrong artifact name

**Symptom:** Java reports `Unable to access jarfile`.

**Check:** List `target/*.jar` after the Package stage and compare the real name
with the path in `Jenkinsfile`. Maven uses the POM's `finalName`.

**Resolution:** Make the deploy command reference the artifact actually
produced. For production pipelines, prefer a shared artifact variable rather
than duplicating the filename in multiple stages.

### Deploy failure: missing destination directory

**Symptom:** `cp` reports `No such file or directory` for the destination.

**Check:** Verify the source JAR exists, then verify the destination directory
exists on the agent or target host. These are separate checks.

**Resolution:** Provision the directory as infrastructure, or have a controlled
deployment script create it. Check permissions and mounts as well; blindly
adding `mkdir` can hide a wrong host or mount configuration.

### Upload failure: missing or unreachable endpoint

**Symptom:** `curl` reports a malformed URL, connection failure, timeout, or
HTTP error.

**Check:** Confirm the environment variable exists, contains a full URL with a
scheme, and points to a real service. Verify DNS/IP, port, firewall, route, and
service health. Never put credentials in the URL or commit them to Git.

**Resolution:** Configure the endpoint through Jenkins credentials or managed
job configuration, validate connectivity from the same agent, and retry only
after the target service is confirmed available.

### Application workflow failure: incorrect status or revenue metric

**Symptom:** `OrderWorkflowServiceTest` reports an unexpected accepted/rejected
count or revenue total.

**Check:** Inspect the `ProcessedOrder` status, the requested status argument,
and whether rejected orders are included in revenue calculations.

**Resolution:** `countByStatus()` must compare each result with the requested
`OrderStatus`. `acceptedRevenue()` must include accepted results only. Keep
these rules covered with batches containing both accepted and rejected orders.

## Recommended Jenkins response

When a job fails:

1. Preserve the failing build number and console log.
2. Use the first failed stage as the starting point.
3. Check test reports and archived artifacts before rerunning.
4. Reproduce locally with the same Java and Maven versions where possible.
5. Separate application failures from agent and target-environment failures.
6. Record the symptom, root cause, fix, and prevention in a commit or ticket.
7. Rerun the smallest relevant check, then validate the entire pipeline.
8. Confirm that deployment was not attempted after a failed quality gate.

## Prevention checklist

- Keep Java/Maven versions explicit and verify them in Jenkins logs.
- Compile test sources early when API changes are possible.
- Publish test and coverage reports on every build.
- Derive artifact names from Maven configuration rather than hardcoding them.
- Provision deployment directories and services outside the application build.
- Store endpoints and secrets in Jenkins-managed configuration.
- Test both successful and rejected workflow outcomes.
- Keep each intentional training failure in its own descriptive commit.
