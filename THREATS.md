# LedgerLine threat model

## Scope and assets

LedgerLine is an educational personal-ledger MVP. Protected assets are user credentials, account ownership, account balances, transaction history, idempotency records, and audit events. The trust boundary is the HTTP API between an untrusted browser/client and the Spring Boot service, plus the service-to-PostgreSQL connection.

## Security assumptions and current controls

- Passwords are stored using BCrypt; plaintext passwords are not returned by API DTOs.
- Protected API routes require authentication. Registration and the health endpoint are public.
- Account listing and account operations are scoped to the authenticated user's email/owner ID.
- Transfers require ownership of both accounts, matching currencies, and distinct source/destination accounts.
- Money uses `BigDecimal`; positive amounts with at most two decimal places are required.
- Withdrawals reject insufficient funds. Balance-changing operations use database transactions and pessimistic account-row locks.
- Transfers lock both rows in ascending ID order to reduce deadlock risk.
- Idempotency records now bind a key to the authenticated owner and a SHA-256 fingerprint of the operation and request details.
- The audit endpoint is restricted to administrator/auditor roles.
- Optional PostgreSQL triggers are provided to reject UPDATE/DELETE on ledger and audit history after an operator applies the SQL script.

## Threats, mitigations, and verification still required

| Threat | Impact | Existing mitigation | Required verification / remaining gap |
|---|---|---|---|
| Broken object-level authorization (change account ID) | Read or move another user's money | Owner checks in account and ledger services | Add integration tests for every endpoint using two users; run them against PostgreSQL |
| Privilege escalation / mass assignment | Register as administrator | Registration constructs `ACCOUNT_HOLDER`; role is not accepted in registration DTO | Test that extra `role` JSON fields cannot grant privileges; verify admin provisioning is out-of-band |
| Credential theft / interception | Account takeover | BCrypt password hashing | HTTP Basic sends reusable credentials on every request; expose only behind HTTPS, add rate limiting/MFA and preferably token/session auth before production |
| Duplicate request / retry | Double debit or credit | Owner- and request-bound idempotency record | Add concurrent duplicate-key integration tests; database unique-key race should be verified under PostgreSQL |
| Reuse of key for different payload | Incorrect replay | Request fingerprint comparison | Test changed amount, operation, account, description, and different owner; legacy records without fingerprints intentionally fail closed |
| Concurrent withdrawals | Negative balance / lost update | Pessimistic row lock and transactional debit | Run simultaneous withdrawal tests against PostgreSQL; H2 tests are not a substitute for PostgreSQL locking behavior |
| Transfer deadlock | Failed or delayed requests | Locks acquired in ascending account ID order | Stress test opposite-direction transfers and inspect transaction retries/timeouts |
| SQL injection | Data disclosure or mutation | Spring Data repository methods and parameterized ORM queries | Review future native SQL; run dependency/security scanning |
| Ledger or audit tampering | Loss of transaction evidence | Application has no update/delete endpoint; optional database triggers supplied | Apply `db/03-append-only-ledger.sql`; use a least-privilege database role that cannot disable triggers or alter schema |
| CORS misconfiguration | Untrusted site can call API from browser | Configurable origin patterns, localhost default | Set exact production frontend origin; avoid broad wildcard patterns in production |
| Denial of service / brute force | Service unavailable or account takeover | No complete mitigation currently | Add request limits, login throttling, monitoring, and managed infrastructure protections |
| Secrets committed to Git | Database compromise | `.gitignore` excludes `.env`; `.env.example` contains placeholders | Scan Git history and rotate any credential ever committed; never upload real `.env` |
| Unbalanced accounting / reconciliation | Incorrect books | Transfers create matching debit/credit entries | Deposits and withdrawals currently record only the user-account leg; implement and test external clearing/control-account postings before describing the entire system as double-entry balanced |

## Security test plan

1. Register users A and B. Confirm A cannot read B's account, request B's ledger, deposit to B, withdraw from B, or transfer from/to B.
2. Attempt to include `role: ADMINISTRATOR` in registration JSON; confirm the new user remains `ACCOUNT_HOLDER`.
3. Submit a deposit with an idempotency key twice and confirm the balance changes once and the same transaction reference is returned.
4. Reuse that key with a different amount, account, operation, description, or user; confirm the API rejects it without changing balances.
5. Submit simultaneous withdrawals whose combined amount exceeds the balance; confirm no negative balance and no lost update.
6. Submit transfers concurrently in opposite directions; confirm balances remain non-negative and each transfer has one debit and one credit with the same reference.
7. Apply `db/03-append-only-ledger.sql`; attempt UPDATE and DELETE against both protected tables using the application database role and confirm PostgreSQL rejects them.
8. Verify invalid amounts (zero, negative, >2 decimal places), self-transfers, currency mismatches, and malformed requests are rejected.

## Known limitations — do not claim as complete

- Deposits and withdrawals currently post a single account-side entry, not a balanced external-clearing pair. Transfers have a debit and credit pair.
- The append-only SQL triggers are optional and are not automatically installed by Hibernate.
- Full admin provisioning and management UI, rate limiting, MFA, token-based auth, production monitoring, and reconciliation are not implemented.
- Automated PostgreSQL concurrency/security integration tests and a public deployment must be run and verified before submission.
