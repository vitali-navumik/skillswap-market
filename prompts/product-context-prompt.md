# Product Context Prompt

Use this prompt as the shared product/domain context before asking AI to design tests, review coverage, analyze failures, or propose implementation ideas for the SkillSwap Market MVP.

```text
You are working with the SkillSwap Market MVP project.

SkillSwap Market is a marketplace for time-based mentoring and learning sessions. Users can browse offers, create offers, open time slots, book sessions, manage wallet credits, leave reviews, and use admin tools depending on their roles.

Role model:
- Roles are STUDENT, MENTOR, and ADMIN.
- Each user has exactly one role in the MVP.
- Public registration may create either a STUDENT or a MENTOR account.
- ADMIN is assigned only through seed data or supported internal administration.
- ADMIN is not combined with STUDENT or MENTOR in the MVP.

Role capabilities:
- STUDENT can browse offers, top up wallet with test credits, book slots, cancel own bookings, leave reviews after completed sessions, and edit or delete own reviews.
- MENTOR can create and manage own offers, create and delete future free slots, view bookings related to own offers, view own wallet as a read-only earnings and payout history area, complete sessions, and read reviews, but cannot create, edit, or delete reviews in MVP and cannot manually top up wallet.
- ADMIN can access admin endpoints and admin UI for users, offers, slots, bookings, wallets, and reviews, may perform any administrative actions explicitly supported by the MVP, and may edit or delete any review.

Core entities:
- User
- Wallet
- SkillOffer
- AvailabilitySlot
- Booking
- Review

Domain rules:
- AvailabilitySlot represents time availability only.
- Booking is the source of truth for reservation, payment state, cancellation, and completion state.
- Booking creation always identifies the student explicitly.
- In the normal student flow, a user may create a booking only for their own student identity.
- In supported administrative booking flow, admin may create a booking for a selected user who has the `STUDENT` role.
- Self-booking is not supported in the MVP.
- Slot statuses in MVP are OPEN and BOOKED.
- Booking lifecycle in MVP is RESERVED -> COMPLETED, with CANCELLED as the cancellation state.
- CONFIRMED is not used as a booking status in MVP.
- Public offer reviews are visible to guests and authenticated users.
- Student review creation in the normal user flow requires the student's own `COMPLETED` booking.
- Self-review is not supported in the MVP.
- Students may edit or delete only their own reviews.
- Admin review creation in supported administrative scope does not have to follow the normal completed-booking restriction.
- Admin may edit or delete any review in supported administrative scope.

Cancellation policy:
- Cancelling a `RESERVED` booking refunds the student.
- Cancelling a `COMPLETED` booking refunds the student and deducts the same amount from the mentor.
- If a `RESERVED` booking is cancelled before slot start time, the slot becomes OPEN again.
- Cancelling a `COMPLETED` booking does not reopen the slot.

Wallet model:
- MVP uses internal virtual credits only.
- There is no external payment provider.
- Wallet top-up in the UI is a demo/test helper for adding virtual credits and is available only for STUDENT users.
- Booking creation charges the selected student immediately.
- Cancelling a `RESERVED` booking refunds the student.
- Completing a booking creates mentor payout.
- Cancelling a `COMPLETED` booking refunds the student and creates a reverse mentor adjustment.
- A MENTOR wallet is a read-only earnings and payout history view in the UI.

Admin scope:
- Admin can view users, offers, slots, bookings, wallets, and reviews.
- Admin may update any user, offer, slot, booking, wallet, and review data or statuses that are explicitly supported by the MVP.

Catalog rules:
- Catalog supports keyword search, category single-select filter, and sort.
- Popular searches are search shortcuts that prefill or trigger keyword search; they are not category filters.

UI flow convention:
- By default, list pages are entry points, details pages are read-only, and create or edit flows live on dedicated pages.
- Entity editing should not happen by default on list pages or details pages.
- Inline actions are acceptable only for bounded command-style operations such as cancel, complete, top-up, delete with confirmation, or explicit status transitions.
- For user-facing page titles, button labels, and primary navigation labels, prefer one consistent UI naming style. Use Title Case for UI labels and headings such as `Log In`, `Create Account`, `Edit Profile`, and `Create Offer`. Use sentence case in descriptive body text.
- Keep primary buttons compact and content-sized unless a full-width control is clearly necessary for the layout.

Baseline and reset model:
- The environment uses deterministic seeded baseline data.
- During testing, new data may be created and accumulated.
- Full reset or nightly rebuild recreates the database and restores the same baseline state.
- Cleanup delete endpoints are not part of the MVP testing model.

MVP scope limits:
- Do not invent unsupported features.
- Do not assume cleanup delete endpoints exist.
- Do not assume real payment integration exists.
- Stay within the current MVP role model, wallet model, booking lifecycle, and reset model.

When generating tests, reviews, analysis, or implementation suggestions:
- keep recommendations consistent with the MVP scope
- distinguish permissions by role carefully
- prefer deterministic test design that remains valid within the baseline/reset environment model
- separate business rules from UI convenience behavior
```
