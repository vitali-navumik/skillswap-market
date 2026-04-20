# Assertion Style Prompt

Use this prompt when defining assertion style, verification quality, and expected rigor of checks for the SkillSwap Market MVP test suite.

```text
You are defining assertion style and verification quality for the SkillSwap Market MVP test suite.

Use the following rules.

1. Prompt purpose
- This prompt defines how tests should assert results.
- It is not about business scope, test data setup, or framework structure.
- It is about the quality, focus, and style of assertions in API and UI tests.

2. Core assertion principle
- Assertions must verify meaningful behavior, not only technical success.
- A passing test should prove that the intended business outcome happened.
- Do not reduce assertions to status-code-only checks or element-exists-only checks when stronger verification is possible.

3. Primary and supporting assertions
- Each scenario should have one clear primary assertion.
- The primary assertion must validate the main expected business outcome of the scenario.
- Supporting assertions may strengthen confidence, but must not blur the scenario focus.
- If a test has many assertions but no clearly dominant one, the scenario is probably too broad.

4. Business outcome over transport noise
- Prefer assertions that prove the business result through supported external behavior.
- For API tests, do not stop at `200 OK`, `201`, or other status checks if the resulting entity state or business effect can also be verified.
- For UI tests, do not stop at “button exists” or “page opened” if the visible user outcome can also be verified.
- Verify what changed, what became visible, what became unavailable, or what business state was reached.

5. API assertion style
- API assertions should usually combine:
  - status code
  - response body checks
  - business state verification through supported API reads
  - side effects where relevant
- For mutation endpoints, verify not only the immediate response, but also the resulting state when practical.
- When relevant, verify:
  - resource status
  - ownership
  - returned ids
  - updated fields
  - wallet balance, reserved balance, and related credit effects
  - list or detail visibility after mutation
  - error response shape and meaning

6. UI assertion style
- UI assertions should focus on user-visible outcomes.
- Prefer assertions about:
  - visible text
  - state changes
  - list contents
  - detail values
  - presence or absence of controls
  - enabled or disabled state
  - success or error feedback
  - navigation or redirect outcome
- Do not over-focus on low-value DOM details when stronger user-facing assertions are available.

7. Error assertions
- Error assertions should verify meaning, not only failure existence.
- For negative scenarios, verify:
  - expected status code or visible error type
  - expected error semantics
  - expected message or error shape where relevant
  - absence of forbidden business side effects
- Distinguish between different failure types such as:
  - authentication failure
  - permission failure
  - not found
  - conflict
  - validation failure
  - business-rule rejection

8. State-aware assertions
- Assertions should respect state-dependent behavior.
- When a scenario depends on entity state, verify that the correct state transition happened or that the correct state restriction was enforced.
- Especially relevant for:
  - offer status changes
  - slot availability changes
  - booking lifecycle transitions
  - student review eligibility after `COMPLETED`
  - review ownership restrictions for edit or delete
  - admin review override behavior where supported
  - wallet balance and reserve effects

9. Ownership and role-sensitive assertions
- When access, ownership, or participant rules matter, assertions must verify the correct access boundary.
- For allowed access, confirm that the correct actor sees or changes the intended resource.
- For forbidden access, confirm that another actor cannot see, mutate, or reach the resource.
- Where relevant, verify both:
  - the denial itself
  - the absence of unintended state changes

10. Common assertions and domain assertions
- Use a two-layer assertion model:
  - `CommonAssertions` for shared checks such as status codes, error responses, common response rules, and generic API expectations
  - domain assertion classes for business-entity verification
- `CommonAssertions` should handle repeated low-level checks consistently.
- Domain assertion classes should verify business entities and business outcomes rather than raw technical fields only.
- Tests should read like scenario plus outcome, while reusable assertion classes carry repeated verification details.
- Follow the current project style used in `UserAssertions` and `AuthAssertions`:
  - reusable entity checks should be moved out of tests into dedicated assertion classes
  - direct assertions in tests should use meaningful `as(...)` descriptions that express the business rule being checked
  - avoid generic descriptions such as `Response error message` when a more specific business-oriented wording is available

11. Soft assertions for multi-field verification
- For multi-field entity verification, prefer soft assertions.
- Soft assertions are especially useful when validating a business entity across several important fields.
- This helps one failing test reveal multiple mismatches instead of stopping at the first broken field.
- Use this approach especially for entities such as offers, bookings, wallets, reviews, and similar multi-field objects.

12. Assertion parameter style
- Prefer reusable assertion helpers that allow the test to specify only the expected fields that matter for the current scenario.
- Use assertion parameter objects or similar patterns where they improve clarity.
- The goal is to avoid long field-by-field assertion blocks in tests while keeping expectations explicit.
- Assertion helpers should stay intention-revealing and business-oriented.

13. Assertions should not be noisy
- Do not add incidental assertions that are not relevant to the test goal.
- Avoid long lists of weak checks that make failures harder to interpret.
- Prefer a smaller number of strong assertions over a large number of shallow checks.
- Each assertion should earn its place in the test.

14. Positive and negative symmetry
- Do not design assertions only for happy path.
- Negative scenarios should have equally strong expected outcomes.
- A negative test should prove:
  - the request or action failed for the correct reason
  - protected state was not changed
  - unrelated state was not accidentally changed

15. Schema and contract assertions
- Schema or contract assertions may be used as a supporting layer, especially for stable APIs.
- They must not replace business assertions.
- Use them to strengthen confidence in response stability, not as the only proof that behavior is correct.

16. Assertion depth by test type
- Keep assertion depth proportional to the scenario.
- Simple smoke checks may use lighter assertions, but they must still prove a meaningful expected result.
- Core business scenarios should use stronger and deeper verification.
- Do not over-engineer tiny smoke tests, but do not under-assert important business flows.

17. Output expectations
When proposing assertion strategy for a test or scenario, return:
- Scenario or feature
- Primary assertion
- Supporting assertions
- Business outcome being proven
- What must not change
- Assertion risks or gaps
- Suggested stronger checks, if current assertions are weak

18. Review style
- Prioritize weak, missing, or misleading assertions first.
- Be concrete.
- Point out where a test only proves transport success instead of business success.
- Point out where a test checks too much irrelevant detail.
- Recommend clearer and stronger assertion design that can be directly implemented.
```
