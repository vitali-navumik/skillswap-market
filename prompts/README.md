# Prompt Index

This directory contains reusable prompts used to guide analysis, test design, framework design, and review work for the SkillSwap Market MVP project.

The prompts are intentionally kept separate so they can be:
- reused independently
- combined by task
- updated without affecting unrelated guidance

## Core Prompt Map

- [product-context-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/product-context-prompt.md)
  Shared product and domain truth for the project.
  Use when the task needs correct understanding of roles, booking lifecycle, wallet behavior, reviews, admin scope, and MVP boundaries.

- [test-data-strategy-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/test-data-strategy-prompt.md)
  Test data and scenario setup policy.
  Use when the task involves fresh-data-first test design, baseline usage limits, setup scope, or test isolation.

- [test-framework-architecture-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/test-framework-architecture-prompt.md)
  Test framework structure and implementation style.
  Use when the task involves resolvers, actors, actions, assertions, page objects, Allure/reporting style, or overall framework layout.

- [role-coverage-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/role-coverage-prompt.md)
  Role, ownership, and access-boundary review.
  Use when checking whether a feature or scenario pack covers guest, student, mentor, admin, single-role boundaries, owner, and participant cases.

- [rest-test-design-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/rest-test-design-prompt.md)
  REST API scenario design.
  Use when designing API coverage, endpoint-level cases, flow-level API cases, error semantics, ownership checks, and state-dependent REST behavior.

- [ui-test-design-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/ui-test-design-prompt.md)
  UI scenario design.
  Use when designing page-level UI scenarios, flow-level UI scenarios, role visibility checks, navigation cases, empty states, and user-visible outcomes.

- [assertion-style-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/assertion-style-prompt.md)
  Assertion quality and verification depth.
  Use when defining how strong assertions should be, how to separate primary and supporting assertions, and how to avoid weak or noisy checks.

- [flaky-failure-analysis-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/flaky-failure-analysis-prompt.md)
  Failure and flakiness diagnosis.
  Use when analyzing unstable tests, intermittent failures, setup mismatches, synchronization issues, weak assertions, or missing diagnostic artifacts.

- [test-case-review-prompt.md](/C:/Users/Vitali/IdeaProjects/skillswap-market/prompts/test-case-review-prompt.md)
  Review of an existing scenario pack.
  Use when evaluating whether an already prepared set of test cases is complete, non-duplicative, risk-balanced, and practical to automate.

## Typical Prompt Combinations

- Product understanding:
  - `Product Context Prompt`

- Designing REST scenarios:
  - `Product Context Prompt`
  - `Test Data Strategy Prompt`
  - `Role Coverage Prompt`
  - `REST Test Design Prompt`

- Designing UI scenarios:
  - `Product Context Prompt`
  - `Test Data Strategy Prompt`
  - `Role Coverage Prompt`
  - `UI Test Design Prompt`

- Designing framework structure:
  - `Product Context Prompt`
  - `Test Framework Architecture Prompt`

- Reviewing an existing test pack:
  - `Product Context Prompt`
  - `Test Data Strategy Prompt`
  - `Role Coverage Prompt`
  - `Assertion Style Prompt`
  - `Test Case Review Prompt`

- Investigating failures or flaky tests:
  - `Product Context Prompt`
  - `Test Data Strategy Prompt`
  - `Assertion Style Prompt`
  - `Flaky / Failure Analysis Prompt`

## Maintenance Rule

When a prompt is added, renamed, removed, or significantly repurposed, update this index so it remains a reliable entry point into the prompt set.
