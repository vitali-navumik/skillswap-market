# REST Test Design Prompt

Use this prompt when designing REST API test scenarios, coverage structure, and business-oriented API verification for the SkillSwap Market MVP project.

```text
You are designing REST API test scenarios for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt is used to design REST API coverage for features, endpoint groups, and business flows.
- It is not about test framework implementation details.
- It is about scenario design, API behavior coverage, and turning product rules into testable REST cases.

2. Scope of REST test design
- Design scenarios for:
  - endpoint behavior
  - business rules
  - validation
  - permissions
  - ownership
  - status transitions
  - API error handling
  - privacy and cross-user access boundaries
- Include both success paths and failure paths.
- Treat schema/contract checks as an additional verification layer where they add value, but do not let them replace business assertions.

3. Relationship to other prompts
- Use the shared product/domain truth from the Product Context Prompt.
- Follow fresh-data-first and baseline-read-only rules from the Test Data Strategy Prompt.
- Apply role and ownership completeness from the Role Coverage Prompt.
- Do not redesign the test framework here; assume framework architecture is handled separately.

4. Core design principle
- Design REST tests around business behavior, not only around endpoints in isolation.
- If several endpoints form a meaningful business flow, include both:
  - focused endpoint-level scenarios
  - flow-level API scenarios across multiple REST endpoints where useful
- Prefer scenarios that verify observable business outcomes, not only transport success.

5. Required coverage dimensions
For each endpoint or feature, explicitly consider:
- positive path
- negative path
- validation behavior
- access and permission rules
- ownership and relationship rules
- state-dependent behavior
- conflict scenarios
- idempotency, where relevant
- response shape, where useful
- error response semantics

6. Authentication and authorization
- Review authentication coverage explicitly.
- For relevant endpoints, consider:
  - no token
  - invalid token
  - expired token
  - wrong user token
  - wrong role
  - ownership violation
  - cross-user privacy access
- Consider denial responses according to the system contract, most often `401`, `403`, or `404`.
- Do not assume UI restrictions are sufficient; REST coverage must validate backend enforcement.

7. Ownership and relationship coverage
- Distinguish role-based access from relationship-based access.
- A user may have the correct role but still be forbidden because the resource belongs to another actor.
- Explicitly consider:
  - own vs another user's offer, including self-booking rejection
  - own vs another user's booking
  - booking student vs another student
  - booking mentor vs another mentor
  - admin vs regular user
- Review the combination of role, ownership, and relationship rules, not each factor in isolation only.

8. State-dependent behavior
- For operations that depend on entity state, explicitly include state-aware coverage.
- A request may have the correct actor and valid payload but still fail because the entity is in the wrong state.
- Consider state-dependent rules especially for:
  - offer status transitions
  - slot availability
  - booking creation, including explicit student identity and admin-created booking for a selected student where supported
  - self-booking rejection
  - booking cancel and complete
  - wallet top-up access for STUDENT only
  - MENTOR rejection for wallet top-up
  - student review creation after `COMPLETED`
  - self-review rejection
  - student review edit or delete ownership rules
  - admin review creation outside the normal completed-booking flow, where supported
  - wallet-related side effects

9. Validation design
- Include request validation coverage where it matters.
- Validate:
  - required fields
  - invalid field values
  - boundary values
  - malformed requests
  - business validation failures
- Distinguish transport-level invalid input from business-rule rejection.

10. Conflict and concurrency behavior
- Include conflict cases where real contention or state collision is possible.
- Especially consider:
  - double booking
  - repeated mutation of the same resource
  - stale-state actions
  - repeated cancel or complete requests
- Include concurrency-related scenarios only where they are meaningfully supported by the domain.

11. Error semantics
- Include explicit error coverage where relevant.
- Consider expected semantics for:
  - `400 Bad Request`
  - `401 Unauthorized`
  - `403 Forbidden`
  - `404 Not Found`
  - `409 Conflict`
  - `422 Unprocessable Entity`
- Do not treat all failures as interchangeable. The expected failure meaning matters.

12. Business outcome assertions
- Each scenario should validate the business outcome, not only the status code.
- REST assertions should consider:
  - response status
  - response body
  - business state visible through supported API endpoints
  - wallet balance or credit effects where relevant
  - slot, booking, or offer state after mutation
- Keep the primary business assertion clear and focused.

13. Schema and contract checks
- Schema/contract validation may be included where it strengthens confidence in API stability.
- Use schema checks as a supporting layer, not as a substitute for business verification.
- Prefer schema checks especially for:
  - stable response contracts
  - admin list and detail responses
  - error response formats
  - core business entities returned by public APIs

14. Endpoint grouping strategy
- Group scenarios by business area rather than by raw CRUD shape when that produces more useful coverage.
- Typical REST design groups may include:
  - auth
  - users or profile
  - catalog or offers
  - slots
  - bookings
  - wallet
  - reviews
  - admin
- Within each group, distinguish:
  - endpoint-level scenarios
  - flow-level API scenarios
  - role and access scenarios

15. Scenario granularity
- Keep most scenarios focused on one primary business goal.
- Do not merge multiple unrelated checks into one API scenario just because they touch the same endpoint.
- Use longer API flows only when the full flow itself is the thing being validated.
- Prefer a compact but meaningful scenario set unless broader regression coverage is explicitly requested.

16. Output structure
- Separate the result into:
  - Endpoint-level scenarios
  - Flow-level scenarios

17. Output format
For each REST scenario, return:
- Scenario name
- Business area under test
- Endpoint(s)
- Test level: API
- Type: fresh-data-based | baseline-read-only
- Actors / Roles involved
- Goal
- Key business rule under test
- Entity or resource under verification
- Preconditions
- Data created by test
- Baseline data used
- Request steps
- Expected status code
- Expected response
- Expected business result
- Primary assertion
- Supporting assertions
- Error semantics, if relevant
- Stability notes
- Why this scenario matters

18. Review style
- Be concrete and scenario-oriented.
- Prefer coverage that can directly become automated REST tests.
- Prioritize missing, risky, or easy-to-break scenarios first.
- Do not answer with generic advice such as “test positive and negative cases”.
- Produce a usable REST coverage design, not a vague checklist.
```
