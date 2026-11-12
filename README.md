# LedgerLine — money movement with a ledger you can prove

**Project display name:** Kakanisudha Lakshmi  
**Stack:** React 19 + Vite, Java 21 + Spring Boot 3, Spring Security, Spring Data JPA, PostgreSQL 16.

LedgerLine is an educational personal-ledger application for account creation, deposits, withdrawals, transfers, and transaction history. It is an MVP, not a production banking platform.

## Current implementation

- User registration with BCrypt password hashing; new users receive `ACCOUNT_HOLDER` role.
- HTTP Basic authentication for protected API routes.
- Account creation/listing and account-level ownership checks.
- Positive money validation with `BigDecimal` and two-decimal precision.
- Deposits, withdrawals, and same-currency transfers inside database transactions.
- Pessimistic row locks for balance changes; transfers lock account rows in ascending ID order.
- Server-side insufficient-funds protection.
- Transfer posts a debit and credit entry with a shared transaction reference.
- Idempotency keys are bound to the authenticated owner and a SHA-256 request fingerprint; mismatched reuse is rejected.
- Account ledger history and a role-protected audit feed.
- Configurable CORS allow-list through `CORS_ALLOWED_ORIGIN_PATTERNS`.
- Optional PostgreSQL triggers to reject UPDATE/DELETE against ledger and audit history.

## Important limitations — disclose these

- **Deposits and withdrawals currently create one account-side ledger entry each.** They do not yet post a matching external-clearing/control-account entry. Only transfers have a debit/credit pair. Do not describe all transaction types as a fully balanced double-entry accounting system.
- The append-only PostgreSQL triggers are supplied as a manual SQL script and are **not automatically installed** by the application.
- PostgreSQL concurrency/security integration tests have not been completed in this delivery environment. Existing automated tests are basic unit/context tests only.
- Admin/auditor roles and a protected audit endpoint exist, but full admin provisioning/management UI is not implemented. Admin promotion is a privileged manual database operation.
- HTTP Basic is intended for local demonstration. A public deployment must use HTTPS end-to-end; production should add rate limiting, MFA, short-lived token/session authentication, secret management, monitoring, and operational controls.
- No public deployment is included. A live URL must be created and verified from an account controlled by the candidate.

## Requirements

- Java 21+
- Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL 15+ **or** Docker Desktop with Docker Compose

## Run locally on Windows

### 1. Start PostgreSQL

From the project root, if Docker Desktop is installed and running:

```powershell
docker compose up -d postgres
docker ps
```

Compose creates `ledgerline_db` with local demo credentials:

- Database: `ledgerline_db`
- Username: `ledgerline_app`
- Password: `change_this_password`
- Port: `5432`

These are development-only credentials. Change them before using a shared or exposed environment. If using an existing PostgreSQL installation, create the database/user using `db/01-create-database.sql` and set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in your terminal.

**Do not use MySQL Workbench for this project.** It uses PostgreSQL, not MySQL.

### 2. Start backend

Open a new PowerShell terminal in the project root:

```powershell
cd backend
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ledgerline_db"
$env:DATABASE_USERNAME="ledgerline_app"
$env:DATABASE_PASSWORD="change_this_password"
mvn spring-boot:run
```

Backend: `http://localhost:8080`  
Health check: `http://localhost:8080/actuator/health`

### 3. Start frontend

Open a second PowerShell terminal in the project root:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

Alternatively, from the root use `./start-backend.ps1` and `./start-frontend.ps1` in separate terminals after configuring PostgreSQL credentials.

## First-use walkthrough

1. Select **Create account** and register using an email address you control and a new password. The display name can be `Kakanisudha Lakshmi`.
2. Sign in using the same email and password.
3. Create two INR accounts to exercise transfers.
4. Deposit into one account, withdraw an amount within the balance, then transfer between your two accounts.
5. Open Activity to inspect ledger entries and transaction references.
6. Try an amount larger than the balance; the withdrawal should be rejected.

Do not use a real banking password. The sample email `kakanisudha@example.com` in the Postman collection is a placeholder, not a pre-created login.

## API reference

| Method | Endpoint | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register account holder |
| GET | `/api/me` | Required | Current profile |
| POST | `/api/accounts` | Required | Create account |
| GET | `/api/accounts` | Required | List own accounts |
| POST | `/api/accounts/{id}/deposit` | Required | Deposit |
| POST | `/api/accounts/{id}/withdraw` | Required | Withdraw |
| POST | `/api/transfers` | Required | Transfer between own accounts |
| GET | `/api/accounts/{id}/entries` | Required | Own account ledger history |
| GET | `/api/admin/audit` | Administrator / auditor | Recent audit events |
| GET | `/actuator/health` | Public | Health status |

Protected API requests use HTTP Basic authentication (email as username). For every money movement, an `Idempotency-Key` header is required (maximum 100 characters). Reuse the same key only when retrying the exact same operation and payload. Reusing a key with different request details or a different user is rejected.

### Example registration

`POST http://localhost:8080/api/auth/register`

```json
{
  "fullName": "Kakanisudha Lakshmi",
  "email": "your-email@example.com",
  "password": "Use-A-New-Password-123"
}
```

### Example account creation

`POST http://localhost:8080/api/accounts`

```json
{ "name": "Everyday account", "currency": "INR" }
```

### Example deposit / withdrawal

`POST http://localhost:8080/api/accounts/1/deposit`

```json
{ "amount": 500.00, "description": "Opening deposit" }
```

Header example: `Idempotency-Key: deposit-opening-001`

### Example transfer

`POST http://localhost:8080/api/transfers`

```json
{ "fromAccountId": 1, "toAccountId": 2, "amount": 100.00, "description": "Monthly transfer" }
```

## Database hardening scripts

Run these only against a backup/test database first:

- `db/01-create-database.sql` — create the local application role/database (run as a PostgreSQL administrator; edit the sample password).
- `db/02-promote-user-to-admin.sql` — optional manual role promotion after registering the intended account; replace the email and run as a database administrator.
- `db/03-append-only-ledger.sql` — install PostgreSQL triggers that reject UPDATE/DELETE on `ledger_entries` and `audit_events`. Apply after the application has created the tables. This is not automatically applied.

Use a least-privilege database role in a deployed environment. A database owner/superuser can still alter schema or disable triggers.

## Tests and builds

Backend tests:

```powershell
cd backend
mvn test
```

Frontend production build:

```powershell
cd frontend
npm install
npm run build
```

The current test suite includes basic account balance tests and an application context test. It does **not** yet prove PostgreSQL row-lock behavior, concurrent duplicate-key handling, end-to-end authorization, or the full threat plan. See `THREATS.md` for the required verification cases.

## GitHub hygiene

- Commit incrementally as you make and understand changes; do not fabricate historical commits.
- Keep `PROMPTS.md` updated with real dated prompts, tool output, mistakes found, decisions, and actual commit URLs.
- Keep `THREATS.md` updated with threats, mitigations, and test evidence.
- Never commit `.env`, real passwords, database dumps, access tokens, or private credentials. Scan the Git history and rotate any secret that was ever committed.
- Keep the repository private until the hiring challenge deadline if the challenge instructions require it; make it public only when permitted.

## AI assistance disclosure

AI assistance was used for repository review, code review, debugging guidance, and documentation. The candidate must review and understand every change, run the tests, and accurately describe what is and is not complete. See `PROMPTS.md`.
