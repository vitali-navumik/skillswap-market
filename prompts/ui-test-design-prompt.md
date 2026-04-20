# UI Test Design Prompt

Use this prompt when designing UI test scenarios, UI coverage structure, and user-visible automation scenarios for the SkillSwap Market MVP project.

```text
You are designing UI test scenarios for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt is used to design UI test coverage for pages, user flows, and role-dependent interface behavior.
- It is about scenario design for UI automation, not about test framework implementation details.
- It should turn product rules and user-facing behavior into practical UI scenarios that can later become automated tests.

2. Scope of UI test design
- Design scenarios for:
  - page-level behavior
  - user journeys
  - UI visibility rules
  - UI actions and state changes
  - validation behavior
  - role-based visibility and access
  - ownership-sensitive UI behavior
  - navigation and menu availability
  - UI error and empty states
- Cover both business success paths and meaningful failure or restriction cases.
- Focus on what the user can see, do, and verify through the interface.

3. Relationship to other prompts
- Use the shared domain truth from the Product Context Prompt.
- Follow fresh-data-first and baseline-read-only rules from the Test Data Strategy Prompt.
- Apply role and ownership completeness from the Role Coverage Prompt.
- Do not redesign the framework here; assume framework architecture is handled separately.
- Do not replace backend access coverage with UI-only assumptions.

4. Core design principle
- Design UI tests around user-observable behavior, not around DOM mechanics.
- A UI scenario should verify a meaningful user-facing outcome.
- Prefer scenarios that reflect realistic user goals, not artificial click sequences.
- Keep the focus on business value, visibility, navigation, and state changes visible through the UI.

5. Baseline and fresh-data usage
- By default, prefer fresh-data-based UI scenarios for logged-in functional flows.
- Use baseline-read-only scenarios only for limited public or demo-style checks.
- If a UI scenario verifies that a newly created object appears in catalog, lists, details, or history, that object should be created by the test itself.
- Do not base important UI verification on mutable pre-seeded entities if the same behavior can be tested on fresh data.

6. Setup strategy for UI scenarios
- UI scenarios may use mixed setup by default.
- Preconditions may be created through API or supported test hooks when this reduces setup cost and does not distort the user-facing purpose of the scenario.
- Do not force long setup through UI if the setup flow itself is not the thing being validated.
- Use UI-based creation only when the UI creation, editing, or submission flow is itself under test.

7. Required UI coverage dimensions
For each feature or area, explicitly consider:
- positive path
- negative path
- validation behavior
- role-based visibility
- ownership-sensitive behavior
- state-dependent UI behavior
- empty states
- permission restrictions
- navigation availability
- UI feedback after actions

8. Role and visibility coverage
- Review what each relevant role can see and do in the UI.
- Explicitly consider:
  - guest
  - `STUDENT`
  - `MENTOR`
  - `ADMIN`
- Check visibility of:
  - menus
  - buttons
  - forms
  - actions
  - admin areas
  - booking and offer controls
- Do not assume backend permission checks are enough; UI visibility and affordance rules also need coverage.

9. Ownership and participant-sensitive UI behavior
- Distinguish UI behavior for:
  - own offer vs another user's offer, including self-booking rejection
  - own booking vs another user's booking
  - booking student vs unrelated student
  - booking mentor vs unrelated mentor
  - admin vs regular user
- Verify not only access denial, but also what the UI actually shows:
  - hidden controls
  - disabled controls
  - unavailable pages
  - missing actions in menus or details pages

10. State-dependent UI behavior
- A user may have the correct role but still see different UI depending on entity state.
- Include state-aware UI coverage especially for:
  - offer status
  - slot availability
  - booking status
  - student self-booking rejection
  - self-review rejection
  - admin booking creation for a selected student where supported
  - student review availability after `COMPLETED`
  - student review edit or delete visibility for own reviews only
  - admin review management visibility in admin UI
  - wallet top-up visibility only for STUDENT
  - MENTOR wallet visibility as a read-only earnings view
  - wallet state and visible balance changes
- When relevant, distinguish:
  - action available
  - action unavailable
  - action hidden
  - action disabled
  - action rejected with visible feedback

11. Validation and feedback
- Include UI validation scenarios where form behavior matters.
- Consider:
  - required fields
  - invalid values
  - boundary inputs
  - inline validation
  - submit blocking
  - error messages
  - post-submit success feedback
- UI tests should verify what the user actually sees, not only that an API call would fail.

12. Navigation and layout behavior
- Include navigation coverage where it matters.
- Consider:
  - access to public pages
  - access to private pages
  - access to admin pages
  - redirects to login
  - redirects after successful actions
  - menu entries visible by role
  - correct route availability by state and role
- UI tests should verify that users can reach the right places and are blocked from the wrong ones.

13. Page-structure convention
- By default, treat list pages as entry points, details pages as read-only views, and create or edit pages as separate flows.
- Prefer scenario design that reflects:
  - list page -> details page
  - details page -> edit page
  - dedicated create page for new entities
- Do not assume free-form inline editing on list pages or details pages unless that behavior is intentionally part of the product.
- Inline actions are acceptable only for bounded command-style operations such as cancel, complete, top-up, delete with confirmation, or explicit status transitions.
- Treat user-facing labels and headings as part of the UI contract. Primary navigation labels, page titles, and primary CTA buttons should use consistent Title Case naming such as `Log In` and `Create Account`, while explanatory body text should remain sentence case.
- Prefer content-sized primary buttons in UI mockups and tests unless the design explicitly calls for a full-width action.

14. Empty states and data presentation
- Include scenarios for empty and partially populated UI states where useful.
- Consider:
  - empty catalog results
  - no bookings yet
  - no offers yet
  - empty wallet history
  - empty admin tables
- Verify that empty states are understandable and do not break the flow.

15. UI outcome assertions
- Each scenario should verify visible user-facing results.
- UI assertions should consider:
  - visible text and labels
  - visible state changes
  - element presence or absence
  - enabled or disabled controls
  - list contents
  - detail page content
  - visible success or error messages
- Keep the main user-visible outcome as the primary assertion.

16. Grouping strategy
- Group UI scenarios by business area rather than only by page name where that is more useful.
- Typical UI design groups may include:
  - auth
  - catalog
  - offer management
  - slot management
  - bookings
  - wallet
  - reviews
  - admin UI
- Within each group, distinguish:
  - page-level UI scenarios
  - flow-level UI scenarios
  - role and visibility scenarios

17. Scenario granularity
- Keep most UI scenarios focused on one primary user goal or one clearly bounded UI risk.
- Do not turn one UI test into a huge end-to-end chain unless the full UI journey is exactly what needs to be validated.
- Prefer shorter, stable, intention-revealing UI scenarios over long brittle ones.
- Use a compact but meaningful scenario set unless wider coverage is explicitly requested.

18. Output structure
- Separate the result into:
  - Page-level UI scenarios
  - Flow-level UI scenarios
  - Role and visibility UI scenarios

19. Output format
For each UI scenario, return:
- Scenario name
- Business area under test
- Test level: UI
- Type: fresh-data-based | baseline-read-only
- Actors / Roles involved
- Goal
- Key user-facing rule under test
- Page or UI area under verification
- Preconditions
- Data created by test
- Baseline data used
- Setup path: API | UI | mixed
- UI steps
- Expected visible result
- Primary assertion
- Supporting assertions
- Visibility or restriction semantics, if relevant
- Stability notes
- Why this scenario matters

20. Review style
- Be concrete and scenario-oriented.
- Prefer coverage that can directly become automated UI tests.
- Prioritize missing, risky, flaky or easy-to-break scenarios first.
- Do not answer with generic advice such as “test the UI thoroughly”.
- Produce a usable UI coverage design, not a vague checklist.
```
