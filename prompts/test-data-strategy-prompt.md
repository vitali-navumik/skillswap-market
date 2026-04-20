# Test Data Strategy Prompt

Use this prompt when designing test scenarios and test data usage for the SkillSwap Market MVP project.

```text
You are designing test scenarios and test data usage strategy for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt is intended for scenarios that validate business logic through API and/or UI.
- Its scope includes:
  - business-oriented API scenarios
  - UI scenarios
  - permission / ownership / access-control checks
  - token-based checks
  - end-to-end user flows within the project’s API/UI scope
  - contract/schema checks as an additional API validation layer when they strengthen confidence in the contract and do not replace business assertions
- By default, this prompt should not drift into infrastructure-only, low-level technical, or purely internal checks unless explicitly requested.

2. Core principle
- For any scenario that validates business logic, prefer fresh data created specifically by that test.
- Do not base important verification on pre-existing baseline data if the scenario can be validated on freshly created entities.

3. Fresh data rules
- Each test should create only the minimum set of data required for its scenario, without excessive setup and without hidden dependencies on other tests.
- When a test validates business logic, it should, where possible, verify behavior on entities created specifically by that test.
- Use unique values for newly created entities, such as email, title, display name, and other identifiers, so the test can reliably locate its own data.
- If an entity is created by the test, later steps and assertions should use explicit identifiers returned by the API or UI flow whenever possible, instead of indirect lookup.

4. Baseline data boundaries
- Baseline data is allowed only for read-only or demo-style scenarios where the test does not validate complex business logic and does not depend on mutable entity state.
- Baseline data is primarily acceptable for:
  - public pages without login
  - basic availability of login / register / catalog
  - lightweight smoke login with a demo account
  - reference/static data
  - very light smoke/demo UI checks without validating mutable business data
- Reference/static data may be read from baseline, but it must not be used as the basis for mutable business verification.
- Even for logged-in UI tests, prefer fresh data created within the test by default.
- If a UI scenario verifies that a new object appears in catalog, lists, details, or history, that object should be created by the test itself.

5. Environment model
- The test environment may become dirty during the day as new entities are created.
- Individual cleanup endpoints are not part of the supported model.
- Full environment reset or nightly rebuild restores the deterministic baseline state.
- Tests must remain valid even when unrelated extra data exists in the system.

6. Setup for UI tests
- For UI tests, use a mixed setup approach by default: create preconditions through API or supported test hooks, and validate through UI the exact user flow that is the subject of the test.
- Do not build long setup chains through UI unless the UI flow of creating that data is itself what must be validated.
- Create data through UI only when the creation, editing, or submission flow through UI is itself the business value under test.

7. Setup cost and scenario boundaries
- Prefer fresh data, but control setup cost.
- If a scenario requires a long prerequisite chain, create only the minimal subset of entities needed for the behavior under test.
- Do not combine multiple independent business checks into one scenario just to save setup cost.
- Each test should validate one primary business capability or one clearly bounded risk.
- Keep long end-to-end chains only when the full chain itself is the subject of verification.

8. Related data inside one test
- A single test may create multiple related fresh entities if the scenario requires it.
- Shared helpers, creators, factories, and resolvers are allowed for building such setup.
- Reuse the setup mechanism, not live mutable business state created by another independent test.

9. Scenario robustness
- Do not assert global record counts unless the test owns the full dataset.
- Do not rely on list ordering unless sorting is part of the behavior under test.
- Do not depend on state created by another independent test unless that dependency is explicitly defined.
- Scenarios should be safe for parallel execution and should not conflict with each other through shared data.
- Assertions should target concrete entities created by the test, using id, title, email, owner, status, and other explicit attributes.

10. Access, token, and ownership checks
- Access-control scenarios are part of the default functional scope.
- For relevant scenarios, explicitly consider:
  - missing token
  - invalid token
  - expired token
  - another user’s token
  - wrong role
  - ownership violation
- For such tests, use freshly created users and protected entities so that access boundaries are validated on fully test-controlled data.
- For access/ownership scenarios, consider the expected denial behavior according to the system contract; this will most often be 401, 403, or 404.
- Consider 402 only where that status code is explicitly part of the system contract.

11. Coverage groups
- Do not limit coverage to happy path only.
- For each operation or feature, explicitly evaluate which coverage groups are relevant:
  - positive cases
  - negative cases
  - validation cases
  - boundary cases
  - permission / access / ownership cases
  - conflict / concurrency cases, but only where real races or conflicting actions are possible
  - idempotency cases, where the behavior implies it
- Do not mechanically force every group into every scenario, but do not generate happy-path-only coverage.

12. Validation rules
- Confirm the main expected business result through supported external system interfaces: public API and/or UI.
- Do not make internal technical checks the main source of truth when the same result can be confirmed through external system behavior.
- Use contract/schema checks only where they genuinely support a stable API contract and do not duplicate business validation.
- Use additional technical verification only where it materially increases confidence without making the scenario brittle.

13. Assertion rules
- For each scenario, explicitly separate the primary business assertion from supporting assertions.
- The primary assertion must validate the main business outcome of the scenario.
- Supporting assertions may strengthen confidence, but must not blur the scenario focus.
- Do not add noisy or incidental checks that do not belong to the main goal of the scenario.

14. Scenario classification
- Explicitly classify each scenario as one of:
  - fresh-data-based
  - baseline-read-only
- Prefer fresh-data-based scenarios by default.

15. Output format
For each scenario, return the result in the following format:
- Scenario name
- Test level: API | UI
- Business area under test
- Type: fresh-data-based | baseline-read-only
- Actors / Roles involved
- Goal
- Preconditions
- Data created by test
- Baseline data used
- Steps
- Expected result
- Primary assertion
- Supporting assertions
- Stability notes
- Why this setup is minimal

16. Scope and depth rule
- If scope, depth, or scenario type is not explicitly specified, propose a compact but meaningful set of scenarios.
- Do not expand coverage into a full regression pack unless explicitly requested.
- Clearly distinguish:
  - high-level test plan
  - detailed test cases
- Do not mix API and UI validation inside the same scenario without a clear reason.

When designing tests or a test plan:
- explicitly state what data is created by the test
- explicitly state whether baseline data is used, and only if it is truly necessary
- keep setup and expected results transparent
- design scenarios for repeatability, isolation, robustness, and low maintenance cost
```
