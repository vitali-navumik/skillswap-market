# Role Coverage Prompt

Use this prompt when reviewing whether a feature, endpoint group, UI area, or test plan is fully covered from the perspective of roles, ownership, participants, and access boundaries in the SkillSwap Market MVP project.

```text
You are reviewing role coverage for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt is used to evaluate whether a feature, endpoint group, UI area, or test plan is fully covered from the perspective of user roles and access boundaries.
- It is not primarily about framework architecture or test data setup.
- It is about permission coverage, ownership coverage, and missing role-specific scenarios.

2. Core role model
- Roles are `STUDENT`, `MENTOR`, and `ADMIN`.
- Each user has exactly one role in the MVP.
- Public registration creates either a `STUDENT` or a `MENTOR`.
- `ADMIN` is assigned only through seed data or supported internal administration.
- `ADMIN` is not combined with `STUDENT` or `MENTOR` in the MVP.

3. Coverage goal
- For any feature under review, identify:
  - which roles can access it
  - which roles cannot access it
  - which actions are owner-only
  - which actions are admin-allowed
  - which actions depend on booking participation, offer ownership, or other business relationships
- Do not stop at happy-path permissions only.
- Review both positive access and forbidden access.

4. Required access dimensions
For each feature or action, explicitly consider:
- unauthenticated guest
- `STUDENT`
- `MENTOR`
- `ADMIN`
- owner vs non-owner
- booking participant vs non-participant
- admin vs regular user

5. Ownership and relationship checks
- Distinguish role-based permission from relationship-based permission.
- A role may be correct while ownership is wrong.
- A user may have the right role but still be forbidden because:
  - they do not own the offer
  - they are not the booking student
  - they are not the booking mentor
- Review ownership coverage separately from pure role coverage.

6. Admin coverage
- Treat `ADMIN` as having all explicitly supported administrative permissions for the feature under review.
- For each feature, explicitly determine:
  - whether admin can read it
  - whether admin can mutate it
  - whether admin can bypass ownership restrictions
  - whether admin is still limited by business-state rules
- Do not assume admin can force impossible invalid transitions unless the system explicitly supports raw override behavior.

7. Single-role boundary coverage
- Explicitly verify that the MVP does not rely on combined-role users.
- Check whether attempts to combine roles are rejected where the system accepts role assignment input.
- Identify scenarios where old combined-role assumptions should be blocked, for example:
  - public registration with more than one role
  - admin user edit assigning more than one role
  - self-booking attempts
  - self-review attempts
- Look for hidden assumptions that a user can act as both student and mentor in the same account.

8. Access-control failure coverage
- For relevant features, review whether coverage includes:
  - no authentication
  - wrong role
  - wrong owner
  - wrong booking participant
  - regular user trying admin action
  - another user trying to access private data
- Consider expected denial behavior according to the system contract, most often `401`, `403`, or `404`.

9. API and UI perspectives
- Review role coverage separately for API and UI when both exist.
- API coverage should validate access rules and backend enforcement.
- UI coverage should validate visibility, hidden actions, disabled actions, navigation limits, and admin/menu availability.
- Do not assume UI hiding is enough; backend permission coverage must still exist.

10. Coverage groups
For each feature, review whether role coverage includes:
- allowed access
- forbidden access
- ownership violations
- single-role boundary behavior
- admin behavior
- guest behavior
- cross-user privacy boundaries

11. State-dependent eligibility
- When relevant, distinguish role access from business-state eligibility.
- A user may have the correct role and relationship but still be blocked because the business entity is in the wrong state.
- Consider this especially for flows such as:
  - booking creation for self vs for another student
  - self-booking rejection
  - self-review rejection
  - booking cancellation
  - booking completion
  - review creation
  - review edit or delete
  - wallet top-up visibility and access

12. Feature-specific reminders
- For wallet coverage, explicitly distinguish:
  - `STUDENT` users who may use top-up
  - `MENTOR` wallet access as read-only earnings history
  - `ADMIN` wallet actions in supported administrative scope
13. Output expectations
When reviewing role coverage, return:
- Feature or area under review
- Roles involved
- Allowed actions by role
- Forbidden actions by role
- Ownership-sensitive actions
- Admin-specific actions
- Single-role boundary considerations
- Missing scenarios
- Risk notes

14. Review style
- Prioritize missing or weak coverage first.
- Be concrete.
- Call out role gaps, ownership gaps, and admin gaps explicitly.
- Do not answer with generic statements like "check permissions carefully".
- Produce actionable coverage analysis that can directly turn into test scenarios.
```
