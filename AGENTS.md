# Repository Guidelines

## Project Structure & Module Organization

This repository is in Product Discovery and includes an executable backend foundation, but no business features yet. `backend/` contains the Java 25/Quarkus application, Maven Wrapper, and integration tests; `.github/workflows/backend-ci.yml` runs its build and tests. `docs/` holds numbered product, domain, architecture, and delivery documents. `adrs/` contains numbered Architecture Decision Records (`ADR-NNN-title.md`). `specs/` contains approved implementation specs; see `specs/README.md` for the numbered feature layout. Keep related material in these directories and update the relevant README or index when adding a new document.

## Build, Test, and Development Commands

The backend requires JDK 25. From `backend/`, run `./mvnw --batch-mode --no-transfer-progress verify` to reproduce the CI build and tests, or `./mvnw quarkus:dev` to start it locally. The current foundation does not require external services or secrets; see `backend/README.md` for details and health-check URLs. For documentation-only changes, review the rendered Markdown and inspect the patch with `git diff --check` (whitespace errors) and `git diff` (content review).

## Writing Style & Naming Conventions

Write concise, direct Markdown with descriptive headings and actionable requirements. Preserve the existing Portuguese product language and terminology unless a document is explicitly intended for another audience. Keep numbered documents zero-padded, for example `docs/07-mvp.md`; use `ADR-NNN-kebab-case.md` for decisions. Record durable architectural choices as ADRs. Keep domain and use-case rules independent of external vendors and infrastructure, following the architecture principle in `README.md`.

## Documentation and Spec Review

When changing a requirement, check related user journeys, domain/architecture documents, API contracts, and backlog entries for consistency. Specs should include objectives, scope, requirements, acceptance criteria, invariants, dependencies, error cases, and expected tests, as described in `specs/README.md`. Mark unresolved decisions clearly rather than presenting assumptions as approved policy. The backend currently uses Quarkus integration tests; run the documented `verify` command for every backend feature or correction before opening or updating a pull request, and record the applicable CI result before marking its spec complete. Define any additional executable checks in the relevant spec.

## Commit & Pull Request Guidelines

Use Conventional Commit style with a short type and imperative summary, such as `docs: clarify challenge evaluation rules`. Pull requests should explain the product or architecture rationale, list affected documents, link any related issue/spec, and call out decisions or follow-up work. Include screenshots only when a change affects a rendered visual artifact.

## Security & Configuration

Do not commit credentials, personal data, or real user-submitted writing. Keep configuration examples synthetic. For privacy, consent, and retention guidance, align changes with `docs/15-security-lgpd.md` and flag legal or policy questions as open decisions for review.
