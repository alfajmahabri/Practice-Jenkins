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
