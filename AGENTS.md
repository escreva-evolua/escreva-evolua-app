# Repository Guidelines

## Project Structure & Module Organization

This repository contains the public backend foundation, but no business features yet. `backend/` contains the Java 25/Quarkus application, Maven Wrapper, and integration tests; `.github/workflows/backend-ci.yml` runs its build and tests. Product discovery documents, ADRs, implementation specs, and internal AI evaluation materials are maintained separately and are not part of this public repository.

## Build, Test, and Development Commands

The backend requires JDK 25. From `backend/`, run `./mvnw --batch-mode --no-transfer-progress verify` to reproduce the CI build and tests, or `./mvnw quarkus:dev` to start it locally. The current foundation does not require external services or secrets; see `backend/README.md` for details and health-check URLs. For documentation-only changes, review the rendered Markdown and inspect the patch with `git diff --check` (whitespace errors) and `git diff` (content review).

## Writing Style & Naming Conventions

Write concise, direct Markdown with descriptive headings and actionable instructions. Preserve the existing Portuguese language and terminology unless a document is explicitly intended for another audience. Keep public documentation limited to approved product information and instructions needed to build, test, or operate this repository. Do not copy private discovery or specification material into this repository without publication review.

## Documentation and Spec Review

For backend changes, check the relevant code and tests. The backend currently uses Quarkus integration tests; run the documented `verify` command for every backend feature or correction before opening or updating a pull request.

## Commit & Pull Request Guidelines

Use Conventional Commit style with a short type and imperative summary, such as `docs: clarify challenge evaluation rules`. Pull requests should explain the product or architecture rationale, list affected documents, link any related issue/spec, and call out decisions or follow-up work. Include screenshots only when a change affects a rendered visual artifact.

## Security & Configuration

Do not commit credentials, personal data, or real user-submitted writing. Keep configuration examples synthetic. Flag legal, privacy, and policy questions for review; do not present unresolved assumptions as approved policy.
