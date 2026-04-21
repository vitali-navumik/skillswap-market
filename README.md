# SkillSwap Market

`SkillSwap Market` is a demo product plus automation playground built to support a strong API/UI test framework.

Spring Boot + React demo product with a custom Java API test framework built on `RestAssured`, `JUnit 5`, and `Allure`.

The project includes:
- a backend with real business rules around roles, offers, slots, bookings, wallet credits, reviews, and admin actions
- a frontend for realistic UI flows
- deterministic demo baseline data for stable local and nightly-reset usage
- reusable prompt artifacts for planning test strategy and framework architecture

The main focus of the repository is not only the demo app itself, but also the future automation stack around it:
- API automation
- UI automation
- framework design
- realistic business validations
- reproducible test data and reset flow

## Technical Focus

- `Spring Boot` backend with business-oriented domain logic
- `React` frontend for realistic product flows
- custom Java API automation framework with actor-style actions, assertions, resolvers, and Allure-friendly reporting
- current API stack: `RestAssured` + `JUnit 5` + `Allure`
- planned UI automation direction: `Playwright + Java`

## What The Product Simulates

The app is a compact marketplace for time-based mentoring sessions.

Users have exactly one role in the MVP:
- `STUDENT`
- `MENTOR`
- `ADMIN`

Core product areas:
- authentication and profile
- catalog and offer management
- slot management
- booking lifecycle
- wallet credits and transactions
- reviews
- admin tools

## Repository Layout

- [backend](/C:/Users/Vitali/IdeaProjects/skillswap-market/backend) - Spring Boot backend
- [frontend](/C:/Users/Vitali/IdeaProjects/skillswap-market/frontend) - frontend application
- [infra](/C:/Users/Vitali/IdeaProjects/skillswap-market/infra) - local infrastructure configuration
- [scripts](/C:/Users/Vitali/IdeaProjects/skillswap-market/scripts) - operational scripts such as reset
- [docs](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs) - product and operations documentation
- [prompts](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts) - prompt artifacts used to align AI-assisted work

## Key Documents

- [English spec](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/specs/skillswap_market_spec_for_codex_English.md)
- [Russian spec](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/specs/skillswap_market_spec_for_codex_Ru.md)
- [MVP implementation freeze](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/freeze/mvp_implementation_freeze.md)
- [Runbook](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/operations/RUNBOOK.md)
- [Baseline demo data](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/operations/BASELINE_DEMO_DATA.md)

## Prompt Artifacts

- [Prompt index](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/README.md)
- [Product Context Prompt](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/product-context-prompt.md)
- [Test Data Strategy Prompt](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/test-data-strategy-prompt.md)
- [Test Framework Architecture Prompt](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/test-framework-architecture-prompt.md)

## Local Start

### Infrastructure

Start Postgres:

```powershell
docker compose -f .\infra\docker-compose.yml up -d
```

### Backend

From [backend](/C:/Users/Vitali/IdeaProjects/skillswap-market/backend):

```powershell
java -jar .\target\skillswap-market-backend-0.0.1-SNAPSHOT.jar
```

Backend health:
- [http://127.0.0.1:8080/api/health](http://127.0.0.1:8080/api/health)

### Frontend

From [frontend](/C:/Users/Vitali/IdeaProjects/skillswap-market/frontend):

```powershell
npm run dev -- --host 127.0.0.1
```

Frontend preview:
- [http://127.0.0.1:5173](http://127.0.0.1:5173)

## Reset Model

The environment uses deterministic seeded baseline data.

The supported cleanup model is a full reset, not targeted cleanup endpoints.

Use:

```powershell
.\scripts\reset-demo-environment.ps1
```

See details in the [Runbook](/C:/Users/Vitali/IdeaProjects/skillswap-market/docs/operations/RUNBOOK.md).

## API Test Reporting

From [tests-framework](/C:/Users/Vitali/IdeaProjects/skillswap-market/tests-framework):

```powershell
.\gradlew.bat test
.\gradlew.bat allureReport
```

Report output:
- raw results: [build/allure-results](/C:/Users/Vitali/IdeaProjects/skillswap-market/tests-framework/build/allure-results)
- generated HTML report: [build/reports/allure-report](/C:/Users/Vitali/IdeaProjects/skillswap-market/tests-framework/build/reports/allure-report)

## CI Test Reporting

GitHub Actions workflow:
- [.github/workflows/tests-allure.yml](/C:/Users/Vitali/IdeaProjects/skillswap-market/.github/workflows/tests-allure.yml)

What happens on each push to `main`:
- PostgreSQL starts in GitHub Actions
- backend is built and started
- all tests from [tests-framework](/C:/Users/Vitali/IdeaProjects/skillswap-market/tests-framework) run
- Allure report is generated
- raw results, HTML report, and backend logs are uploaded as workflow artifacts
- the HTML Allure report is published to GitHub Pages

Where to look:
- `Actions` tab for execution logs and artifacts
- `Pages` deployment link for the published Allure HTML report

Required GitHub configuration:
- `Settings -> Pages -> Source: GitHub Actions`
- repository secret `ADMIN_PASSWORD` for the seeded demo admin used by the test framework

## Current Direction

The repository is being shaped around three parallel goals:
- keep the demo app realistic enough to produce valuable API and UI scenarios
- keep the domain logic deterministic enough for stable automation
- design a reusable test framework that supports resolvers, actor containers, assertions, tags, page objects, Allure reporting, and future API/UI suites

## Maintenance Rule

This `README.md` should stay current as the project evolves.

When the repository structure, startup flow, docs layout, prompt set, or major testing direction changes, update this file together with the related project changes.
