# AI assistance and engineering decision log

This log records assistance used while reviewing and improving the LedgerLine submission. Entries are dated when the work was performed; they are not backdated. Replace each `Commit:` line with the real Git commit URL after committing the corresponding change. Do not invent commit hashes or claim an unrun test passed.

## 2026-11-12 — Repository review and scope check
- **Prompt / request:** Review the supplied LedgerLine ZIP against the hiring challenge and identify what is implemented versus unfinished.
- **Tool output:** A repository inventory and review of the Spring Boot services, React UI, PostgreSQL configuration, API controllers, and existing tests.
- **What needed correction:** The initial README overstated the strength of idempotency and described the ledger as more complete than the code demonstrated. The UI and API are an MVP, not a production banking system.
- **My decision/action:** Kept the existing architecture and made the README distinguish implemented behavior from limitations.
- **Commit:** Add the real commit URL after committing this change.

## 2026-11-12 — Idempotency and duplicate-request safety
- **Prompt / request:** Review whether a repeated idempotency key can accidentally replay a different user's or different operation's transaction.
- **Tool output:** The original implementation looked up a key globally and returned its prior transaction without checking the authenticated owner or request contents.
- **What was wrong:** A key could be reused for a different request and receive an unrelated transaction reference. This was not safe request-level idempotency.
- **My decision/action:** Added owner and SHA-256 request fingerprint fields. Replays now return the original result only when owner and canonical request details match; mismatched reuse fails closed. Existing legacy records without these fields also fail closed.
- **Commit:** Add the real commit URL after committing this change.

## 2026-11-12 — Authorization and account ownership review
- **Prompt / request:** Inspect access-control boundaries for account operations and privileged audit access.
- **Tool output:** Account services scope account listing and access to the authenticated user's owner ID; transfer locks accounts in ascending ID order and checks ownership of both accounts. Audit listing has an administrator/auditor role guard.
- **What needed correction:** These controls were not supported by a dedicated authorization integration-test suite. HTTP Basic is suitable only for a local demo or a correctly TLS-terminated deployment, not plain public HTTP.
- **My decision/action:** Preserved owner checks and role guard, documented the test gap and deployment constraint, and made CORS origins configurable through environment settings.
- **Commit:** Add the real commit URL after committing this change.

## 2026-11-12 — Audit trail integrity
- **Prompt / request:** Assess whether application-level append-only comments protect audit and ledger history against accidental SQL updates/deletes.
- **Tool output:** The original code relied on application convention; database-level mutation protection was not installed.
- **What was wrong:** A direct SQL UPDATE or DELETE could still alter history for a database role with those privileges.
- **My decision/action:** Added an optional PostgreSQL trigger script that rejects UPDATE and DELETE on `ledger_entries` and `audit_events`. It must be applied manually after schema creation and is not represented as already active.
- **Commit:** Add the real commit URL after committing this change.

## 2026-11-12 — Build and deployment verification
- **Prompt / request:** Determine which project checks can be executed in the available environment and avoid claiming unverified results.
- **Tool output:** Java 21 and Node.js 22 are available in the review environment; Maven and Docker are not installed.
- **What was wrong:** A full backend integration run against PostgreSQL cannot be claimed from this environment. No deployment URL can be truthfully supplied without deploying to an account controlled by the candidate.
- **My decision/action:** Added repeatable local test/build commands and documented that backend tests and live deployment still require execution in the candidate's environment.
- **Commit:** Add the real commit URL after committing this change.
