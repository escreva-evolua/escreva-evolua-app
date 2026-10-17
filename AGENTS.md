# Repository Guidelines

## Project Structure & Module Organization

This repository is currently in Product Discovery; it contains specifications, not production code. `docs/` holds numbered product, domain, architecture, and delivery documents. `adrs/` contains numbered Architecture Decision Records (`ADR-NNN-title.md`). `specs/` is reserved for implementation specs; see `specs/README.md` for the proposed numbered feature layout. Keep related material in these directories and update the relevant README or index when adding a new document.

## Build, Test, and Development Commands

There is no application build, local runtime, or automated test command at this stage. For documentation changes, review the rendered Markdown and inspect the patch with `git diff --check` (whitespace errors) and `git diff` (content review). Do not add application tooling until an approved spec calls for it.

## Writing Style & Naming Conventions

Write concise, direct Markdown with descriptive headings and actionable requirements. Preserve the existing Portuguese product language and terminology unless a document is explicitly intended for another audience. Keep numbered documents zero-padded, for example `docs/07-mvp.md`; use `ADR-NNN-kebab-case.md` for decisions. Record durable architectural choices as ADRs. Keep domain and use-case rules independent of external vendors and infrastructure, following the architecture principle in `README.md`.

## Documentation and Spec Review

When changing a requirement, check related user journeys, domain/architecture documents, API contracts, and backlog entries for consistency. Specs should include objectives, scope, requirements, acceptance criteria, invariants, dependencies, error cases, and expected tests, as described in `specs/README.md`. Mark unresolved decisions clearly rather than presenting assumptions as approved policy. No test framework or coverage threshold exists yet; include test expectations in specs and define executable checks when implementation begins.

## Commit & Pull Request Guidelines

The available Git history has one commit, `chore: initial project structure`, which uses a Conventional Commit style. Follow that pattern with a short type and imperative summary, such as `docs: clarify challenge evaluation rules`. Pull requests should explain the product or architecture rationale, list affected documents, link any related issue/spec, and call out decisions or follow-up work. Include screenshots only when a change affects a rendered visual artifact.

## Security & Configuration

Do not commit credentials, personal data, or real user-submitted writing. Keep configuration examples synthetic. For privacy, consent, and retention guidance, align changes with `docs/15-security-lgpd.md` and flag legal or policy questions as open decisions for review.
