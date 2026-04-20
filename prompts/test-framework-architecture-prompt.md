# Test Framework Architecture Prompt

Use this prompt when designing the test framework architecture, test code conventions, API/UI framework layers, and reporting style for the SkillSwap Market MVP project.

```text
You are designing the test framework architecture and implementation style for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt defines how the test framework and test code should be structured.
- It applies to:
  - API test framework architecture
  - UI test framework architecture
  - test setup and fixtures
  - resolvers and annotations
  - actor containers and action classes
  - assertions
  - invocation/template-based scenario design
  - reporting and naming conventions
- This prompt is about framework structure and implementation style, not about product business rules or test data policy.

2. General framework principles
- Build the framework by business domain and by responsibility.
- Keep tests scenario-focused and move technical complexity into reusable framework primitives.
- Prefer explicit, typed abstractions over ad hoc helpers and low-level setup code.
- The test body should read like a scenario, not like transport-layer plumbing.
- Keep API and UI layers stylistically aligned while allowing each layer to use structures appropriate to its responsibilities.

3. Domain-first API framework layout
- Organize the API framework by business domain.
- Within each domain, prefer a consistent structure such as:
  - actions
  - requests
  - responses
  - assertions
  - invocations
  - helpers
  - providers
- Cross-cutting layers should be kept separately, for example:
  - config
  - connectors
  - resolvers
  - tags
  - common assertions
  - invocation infrastructure
  - generators and shared helpers
- Do not group test models into a generic DTO bucket if they belong naturally to a specific domain.

4. Actor model and action containers
- Use an actor-oriented approach for API tests.
- Tests should work through actor containers rather than manual token handling or raw HTTP calls.
- Prefer an `ActionsContainer`-style object that bundles:
  - user context
  - authenticated state
  - access to domain action classes
- The actor container should let the test operate at business-action level instead of transport level.

5. Centralized actor assembly
- Build actors centrally through an `ActionsFactory`-style assembly layer.
- Use factory and helper classes to construct actors consistently.
- Actor creation should not be duplicated across tests.
- Keep shared actor assembly logic in one place so resolver behavior and actor capabilities stay predictable.

6. Resolvers and annotation-driven setup
- Prefer declarative test setup through annotations and parameter resolvers.
- If a test needs users, roles, or prepared actors, they should be injected as typed test parameters where possible.
- Annotation values should express business intent, for example the required role or preset.
- Resolvers should create fresh users, assign roles, prepare authenticated actor containers, and return ready-to-use test objects.
- If a test needs multiple participants, each participant should appear explicitly in the method signature as a separate parameter.

7. Helpers, creators, and setup support
- Use helpers, creators, and setup utilities for complex arrangements.
- These helpers should reduce boilerplate and keep test methods short.
- Reuse setup mechanisms, not live mutable business state.
- Complex setup should be encapsulated in dedicated creators instead of expanded inline in the test body.
- Creator classes should make scenario intent obvious.

8. Requests, builders, and test models
- Keep request models concise and test-friendly.
- For Java request and test models, Lombok `@Data` and `@Builder` are preferred where they improve readability and reduce boilerplate.
- Use `@Builder.Default` for sensible defaults that reduce noise in tests.
- Request builders should let a test override only the fields that matter for the current scenario.
- Use inheritance and interfaces only where they simplify contracts or remove meaningful duplication.
- Prefer `MapStruct` for reusable request/response mapping when models share a stable structure.
- Keep mapper interfaces close to the relevant business domain and use builders with `toBuilder()` when tests need to clone an existing object and override a small subset of fields.

9. Generated data
- Use a shared `FakerGenerator` or equivalent generator for fresh test data.
- Do not spread ad hoc random generation through tests.
- Generated data should be readable, reusable, and unique enough for isolation.
- Generators should support typical entities such as users, offers, titles, comments, and other business-relevant values.

10. Connectors and transport abstraction
- Tests must not perform raw HTTP calls directly.
- Use a connector/sender abstraction between tests and the HTTP client.
- Common request configuration, authorization setup, base URLs, and response handling should be centralized.
- Domain action classes should operate through connector abstractions rather than making low-level calls from test methods.

11. Domain assertions and common assertions
- Use a two-layer assertion model:
  - `CommonAssertions` for shared checks such as status codes, error responses, and generic API expectations
  - domain assertion classes for business-entity validation
- Domain assertion classes should validate business objects, not just isolated fields.
- Prefer assertion parameter objects such as `AssertionParams` so tests declare only relevant expectations.
- Use soft assertions for multi-field validation where that improves diagnostics.
- Keep assertion details out of test methods whenever reusable assertion classes can express them more clearly.
- Follow the current project style used in `UserAssertions` and `AuthAssertions`:
  - reusable entity checks live in dedicated assertion classes
  - assertion parameter objects declare only relevant expected fields
  - assertion descriptions should be meaningful and business-readable, for example `... is correct`
  - tests should prefer assertion classes over long inline assertion blocks

12. Invocation and template-based scenarios
- Use invocation/template-based design for matrix-like or repetitive scenarios.
- Invocation classes should help express scenario variants without duplicating test bodies.
- This approach is especially suitable for:
  - role/access matrices
  - validation matrices
  - ownership checks
  - state transition variants
  - cancellation variants
- Invocation infrastructure should be reusable and shared, not reimplemented per test class.

13. Tags and execution slices
- Use tags to group tests by domain, scope, and purpose.
- Tags should support practical execution slices such as:
  - smoke
  - auth
  - catalog
  - offer
  - slot
  - booking
  - wallet
  - review
  - admin
  - api
  - ui
- Tags should help with selective execution and reporting, not duplicate class names.

14. UI framework entry point
- Use a shared `Application` object as the main entry point for UI tests.
- Prefer an `ApplicationResolver`-style mechanism to inject `Application` into tests.
- Tests should obtain pages through `app.getPage(...)`, not by manually instantiating page objects.
- The UI framework should manage browser, page, and shared UI context centrally.

15. Page Object Model structure
- Use Page Object Model for UI tests.
- Base page behavior should live in `BasePage`.
- Separate responsibilities clearly:
  - `Page` for actions and navigation
  - `Elements` for locators
  - `Assertions` for UI checks
- Keep locators out of test methods.
- Shared page functionality should live in reusable base classes or components where it truly reduces duplication.

16. Fluent UI scenario style
- UI tests should read as a fluent scenario.
- Prefer chainable page/component methods in the style of:
  - page open
  - component access
  - action execution
  - assertions access
- The goal is a readable flow such as:
  - get page
  - open page
  - interact with component
  - switch to assertions
  - verify result
- Actions and assertions must stay clearly separated even in a fluent chain.

17. Selectors and UI stability
- Prefer stable `data-testid` selectors for UI automation.
- Avoid brittle selector strategies when stable product-owned selectors can be used.
- UI assertions should live in assertion classes, not inline next to locators in tests.
- Do not rely on hardcoded sleeps or arbitrary timing delays when Playwright built-in waiters or condition-based waiting can be used instead.
- Prefer Playwright-native synchronization based on page, locator, navigation, and assertion state rather than manual timing guesses.

18. Reporting and step annotations
- Design the framework for strong `Allure Report` readability.
- Use `@Step` annotations for meaningful actions, setup helpers, and assertions where this improves report clarity.
- Step descriptions should explain what is happening in clear business-oriented language.
- Include key runtime values in step descriptions when they improve diagnostics.
- Reports should show an understandable scenario flow, not technical noise.
- In the current project style:
  - keep `@Step` on action-layer methods and reusable assertion methods when they form a meaningful reporting block
  - do not duplicate `@Step` on wrappers that only delegate to another assertion method already creating a clear reporting step
  - in `CommonAssertions`, keep explicit `step(\"...\", () -> ...)` blocks instead of mixing that style with `@Step` on the same methods

19. Display names and naming conventions
- Use `@DisplayName` as the primary human-readable test description layer.
- If external test case ids are used, keep them in `@DisplayName` or another dedicated reporting annotation used consistently across the project.
- Method names should stay shorter and technical, expressing the verified behavior in code-friendly form.
- Test class names should reflect the domain or feature under test.
- Naming should make both code navigation and Allure output easy to understand.

20. Configuration and environment
- Use a centralized configuration layer for URLs, credentials, environment flags, and runtime settings.
- Do not hardcode environment-specific values in tests.
- Environment switching should happen through config, not through editing test code.

21. Database access policy
- Database access may be used as a supporting tool.
- It must not become the default source of truth for business verification when the same result can be checked through API or UI.
- DB usage is acceptable for:
  - targeted setup support
  - rare technical validation
  - side-effect verification where external behavior is not sufficient
- Prefer supported external behavior over internal inspection.

22. Framework output expectations
When proposing framework architecture, class layout, or implementation conventions:
- propose reusable framework primitives instead of one-off helpers
- keep API and UI layers separate but stylistically aligned
- prefer resolvers, actor containers, action classes, assertion classes, and page objects
- optimize for readable tests and readable Allure reports
- minimize boilerplate in test methods
- keep the framework extensible for future API and UI suites
```
