# Hiring challenge submission checklist

Use this list before selecting **“I am done. Submit this for evaluation.”** on the challenge portal.

## Required by the challenge brief

- [ ] Complete the candidate profile and attach a resume link with the requested sharing permissions.
- [ ] Read the full problem statement and all six submission questions.
- [ ] Create a public GitHub repository only when the challenge deadline/instructions permit it. The email instructions say not to make it public before the deadline.
- [ ] Commit real work incrementally. Never backdate commits or fabricate history.
- [ ] Keep `PROMPTS.md` current with at least five real dated entries, including authorization/access-control/audit decisions, and replace commit placeholders with real commit URLs.
- [ ] Keep `THREATS.md` specific to the implementation and include test evidence. Do not report planned tests as passed tests.
- [ ] Run the backend tests and frontend production build locally; fix failures before claiming they pass.
- [ ] Create a live deployment URL from an account you control. Test it in an incognito/private window and on another device/network.
- [ ] Ensure the deployed database is not publicly exposed and all credentials are stored as deployment secrets/environment variables.
- [ ] Enter the project title, one-line description, GitHub URL, live URL, and all six written answers in the challenge form.
- [ ] Review the final form and only then tick the final submission checkbox.

## Honest status of this supplied code bundle

- Code and documentation changes are included in this bundle.
- Backend tests have **not** been run in this execution environment because Maven is unavailable.
- Frontend production build has **not** been run because npm package installation could not reach the package registry.
- Docker/PostgreSQL integration and concurrency behavior have **not** been run here.
- No public deployment URL has been created or verified.
- Deposit/withdrawal postings are not yet a fully balanced double-entry journal; this limitation is documented in `README.md` and `THREATS.md`.

Do not tick the final submission checkbox or claim the items above are complete until you have performed and verified them. If time runs out, disclose unfinished items accurately rather than inventing results.
