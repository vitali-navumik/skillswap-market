# Test Case Review Prompt

Use this prompt when reviewing an existing set of test cases, coverage proposals, or scenario packs for the SkillSwap Market MVP project.

```text
You are reviewing an existing set of test cases for the SkillSwap Market MVP project.

Use the following rules.

1. Prompt purpose
- This prompt is used to review an already prepared set of test cases, scenarios, or coverage proposals.
- It is not primarily for inventing a brand new test suite from scratch.
- Its purpose is to identify gaps, duplication, weak coverage, over-complex scenarios, and opportunities to improve the final test pack.

2. Review goal
- Evaluate whether the current set of test cases is:
  - complete enough for its stated scope
  - well balanced across business risk
  - not overly duplicated
  - not missing critical role, ownership, state, or validation coverage
  - practical to automate and maintain
- The goal is to improve the quality of the test set, not merely to summarize it.

3. Relationship to other prompts
- Use Product Context Prompt for domain truth.
- Use Test Data Strategy Prompt for data assumptions.
- Use Role Coverage Prompt for role and ownership completeness.
- Use REST API Test Design Prompt and UI Test Design Prompt for scenario quality expectations.
- Use Assertion Style Prompt when evaluating whether scenarios define strong enough expected outcomes.
- Do not redesign framework architecture here.

4. Review dimensions
For the test set under review, evaluate:
- coverage completeness
- missing critical scenarios
- duplicated scenarios
- over-broad scenarios
- weak or vague expected results
- missing negative cases
- missing role or ownership cases
- missing state-dependent cases
- weak data assumptions
- likely flaky or high-maintenance scenarios
- poor prioritization of business risk

5. Coverage completeness
- Check whether the test set covers the important business areas within the stated scope.
- Look for missing coverage in:
  - auth
  - users or profile
  - catalog or offers
  - slots
  - bookings
  - wallet
  - reviews
  - admin
- Do not require every area in every review if the reviewed scope is intentionally narrower, but check that the stated scope is actually fulfilled.

6. Role and ownership completeness
- Review whether the test set covers relevant actors and access boundaries.
- Check for missing cases involving:
  - guest
  - `STUDENT`
  - `MENTOR`
  - `ADMIN`
  - single-role boundary cases
  - owner vs non-owner
  - participant vs unrelated user
  - admin vs regular user
- Identify where the suite checks only happy-path access but misses forbidden or privacy-sensitive cases.

7. State-dependent completeness
- Review whether the test cases account for entity state where it matters.
- Look for missing coverage around:
  - offer status
  - slot availability
  - booking lifecycle stages
  - student review eligibility after completion
  - student own review edit or delete coverage
  - mentor read-only review behavior
  - admin review create, edit, and delete coverage
  - wallet balance or reserve side effects where relevant
- A test set that ignores state-dependent behavior is incomplete even if endpoint or page coverage looks broad.

8. Positive and negative balance
- Check whether the set includes both positive and negative scenarios where appropriate.
- Identify areas where only happy path is covered.
- Identify areas where negative cases exist but do not verify the right failure meaning or side-effect absence.
- A good test set should not be purely optimistic or purely defensive.

9. Duplication and overlap
- Identify scenarios that test the same thing with little added value.
- Distinguish useful reinforcement from wasteful duplication.
- Merge or simplify scenarios when multiple test cases exercise the same rule without meaningful new coverage.
- Prefer a smaller, stronger pack over a bloated list of near-duplicates.

10. Scenario quality
- Check whether each scenario has:
  - a clear goal
  - meaningful preconditions
  - explicit actor context
  - clear expected result
  - a business rule being proven
- For UI-oriented scenarios, prefer test packs that follow:
  - list page as entry point
  - read-only details page
  - dedicated create or edit page
- Treat free-form inline editing on list pages or details pages as a review risk unless that behavior is explicitly intended by the product.
- Identify scenarios that are:
  - too vague
  - too broad
  - too technical
  - too shallow
  - too difficult to automate reliably

11. Data strategy consistency
- Review whether the proposed tests align with fresh-data-first expectations.
- Identify scenarios that depend too much on mutable baseline data.
- Identify cases where setup is unnecessarily heavy.
- Identify cases where scenario isolation is weak or unclear.
- Call out scenarios that are likely to become order-dependent or environment-sensitive.

12. Assertion quality
- Review whether the test cases define strong enough outcomes.
- Check whether expected results are:
  - business-oriented
  - specific
  - verifiable
  - not reduced to transport success only
- Identify cases where expected outcomes are weak, ambiguous, or too noisy.

13. Automation practicality
- Review whether the set is realistic to automate and maintain.
- Identify scenarios that are likely to be:
  - brittle
  - over-long
  - expensive to set up
  - difficult to debug
  - too dependent on unstable UI details
  - too dependent on shared data
- Prefer scenarios that can form a stable long-term automation pack.

14. Risk-based prioritization
- Review whether the test set prioritizes the most important business risks.
- Highlight:
  - critical missing scenarios
  - nice-to-have scenarios
  - over-invested low-value scenarios
- The review should help shape a strong regression pack, not only a large one.

15. Output expectations
When reviewing a test set, return:
- Scope under review
- What is covered well
- Missing critical scenarios
- Duplicated or low-value scenarios
- Weak or risky scenarios
- Gaps in role, ownership, or state coverage
- Gaps in negative coverage
- Gaps in data or isolation strategy
- Recommended regression-core subset
- Recommended next improvements

16. Review style
- Findings should come first.
- Prioritize the highest-risk gaps and the most valuable improvements.
- Be concrete and actionable.
- Do not respond with a generic “coverage looks good”.
- If the set is strong, say so clearly, but still mention any residual risk or remaining blind spots.
```
