# SkillSwap Market — Consolidated Demo Application Specification for Codex

## 1. Project Goal

`SkillSwap Market` is a demo application for practicing:
- API testing with `Java + REST Assured`
- UI/E2E testing with `Java + Playwright`

The application should look like a realistic product while remaining compact enough to be implemented quickly and used as a learning sandbox and portfolio project.

The core idea: users publish skills or mini-services, open time slots, and other users book those slots using internal credits.

This file is the working consolidated version of the requirements. It combines the original specification and the agreed clarifications around the role model, booking lifecycle, cancellation policy, admin scope, and disputes.

---

## 2. Product Concept

The platform allows users to:
- register and sign in
- create a profile
- choose product roles
- publish a skill or service
- define available time slots
- book a slot
- reserve credits in a wallet
- cancel a booking according to refund rules
- complete a session
- leave a review
- open a dispute in case of conflict

Example skills:
- Java interview mock session
- Help with SQL queries
- Conversational English for 30 minutes
- Resume review
- Automation QA interview preparation

---

## 3. Why This Domain Fits Test Automation

This domain naturally contains:
- roles and access restrictions
- non-trivial statuses
- positive and negative scenarios
- race conditions when booking the same slot
- cancellation and refund business rules
- validation checks
- multi-screen UI flows
- administrative scenarios

The project is useful at the same time for:
- API automation
- UI automation
- integration tests
- business logic verification

---

## 4. Role Model and Access

### 4.1 Guest

An unauthenticated user.

Can:
- browse the public catalog
- open offer detail pages
- register
- sign in

Cannot:
- create bookings
- create offers
- manage slots
- leave reviews
- use the wallet
- access private or admin endpoints

### 4.2 Authenticated User Roles

An authenticated user has exactly one role:
- `STUDENT`
- `MENTOR`
- `ADMIN`

Allowed roles:
- `STUDENT`
- `MENTOR`
- `ADMIN`

For MVP:
- during public registration, a user must choose exactly one role: `STUDENT` or `MENTOR`
- `ADMIN` cannot be selected through public registration
- `ADMIN` is assigned only through seed data or an internal administrative mechanism
- `ADMIN` is not combined with `STUDENT` or `MENTOR`

### 4.3 STUDENT Permissions

Can:
- edit their profile
- top up their credit balance
- book other users' slots
- cancel their own bookings
- view their bookings
- participate in student-side session flows if needed in the UI
- leave reviews after `COMPLETED`
- open disputes

Cannot:
- create offers without the `MENTOR` role
- manage slots without the `MENTOR` role
- access admin endpoints without the `ADMIN` role

### 4.4 MENTOR Permissions

Can:
- create and edit their own offers
- create and delete their own future free slots
- view bookings related to their offers
- view their own wallet as a read-only earnings and payout history area
- complete a booking through `complete`
- participate in disputes as the offer owner

Cannot:
- access admin endpoints without the `ADMIN` role
- create bookings without the `STUDENT` role
- manually top up wallet credits without the `STUDENT` role

### 4.5 ADMIN Permissions

Can:
- view and update users
- view and update offers
- create and delete slots in administrative scope, subject to normal entity-state restrictions
- view, create, cancel, and complete bookings where the system supports those actions
- view wallets, top up credits, and inspect wallet transactions in administrative scope
- view and update disputes
- create reviews or disputes in administrative scope where the system explicitly supports those actions

For MVP, a dedicated seeded admin account is recommended. Admin is treated as a superuser role for supported administrative actions and is not limited to read-only moderation.

---

## 5. MVP Scope

### Included in MVP

- registration
- login
- JWT authentication
- user profile
- choosing `STUDENT` and `MENTOR` roles during registration
- catalog browsing
- offer creation and editing
- slot creation and deletion
- slot booking
- credit reservation
- booking cancellation
- session completion
- reviews
- simplified disputes
- basic admin panel

### Not Included in MVP

- real payment integration with external providers
- real-time chat
- video calls
- advanced recommendation system
- multilingual support
- mobile version
- automatic money recalculation through disputes
- full dispute center with file attachments and SLA
- complex mentor moderation workflow

---

## 6. Main User Scenarios

### Scenario 1. User acts as a mentor

1. The user registers with the `MENTOR` role
2. Logs in
3. Creates an offer
4. Adds available slots
5. Sees the offer in the catalog

### Scenario 2. User acts as a student

1. The user logs in with the `STUDENT` role
2. Tops up the credit balance
3. Finds an offer in the catalog
4. Opens the offer details page
5. Selects a free slot
6. Books the slot
7. Sees the booking in their account area

### Scenario 3. Session execution and completion

1. The mentor completes the booking
3. The booking moves to `COMPLETED`
4. The system captures the funds
5. The student leaves a review

### Scenario 4. Cancellation

1. The student opens their booking list
2. Selects a booking
3. Cancels it
4. The system applies the refund rule
5. The booking status and balances are updated
6. If cancellation happens before `startTime`, the slot becomes available again

### Scenario 5. Administrator

1. The admin logs in
2. Opens the lists of users, offers, bookings, and disputes
3. Changes a user status if needed
4. Blocks or archives an offer if needed
5. Moves a dispute to another status if needed

---

## 7. Domain Model

### 7.1 User

Fields:
- `id`
- `email`
- `passwordHash`
- `firstName`
- `lastName`
- `displayName`
- `roles`
- `status`
- `createdAt`
- `updatedAt`

`roles`:
- `STUDENT`
- `MENTOR`
- `ADMIN`

`status`:
- `ACTIVE`
- `INACTIVE`

Rules:
- email must be unique
- a user cannot register without either `STUDENT` or `MENTOR`
- `ADMIN` cannot be assigned through public registration
- an inactive user cannot log in

### 7.2 SkillOffer

Fields:
- `id`
- `mentorId`
- `title`
- `description`
- `category`
- `durationMinutes`
- `priceCredits`
- `cancellationPolicyHours`
- `status`
- `createdAt`
- `updatedAt`

`status`:
- `DRAFT`
- `ACTIVE`
- `ARCHIVED`
- `BLOCKED`

Rules:
- the owner with the `MENTOR` role can edit their own offer
- `ADMIN` may also edit offers where the system explicitly supports administrative offer management
- only an `ACTIVE` offer can be booked
- an archived offer remains in history but is unavailable for new bookings
- `cancellationPolicyHours` remains in the model for future extensibility, but MVP does not apply a time-based cancellation penalty

### 7.3 AvailabilitySlot

Fields:
- `id`
- `offerId`
- `startTime`
- `endTime`
- `status`
- `createdAt`

`status`:
- `OPEN`
- `BOOKED`

Rules:
- a slot belongs to one offer
- a slot cannot overlap with another active slot of the same mentor
- the slot is responsible only for time availability
- the commercial state of the deal is defined by `Booking`
- a slot can be deleted only if it is `OPEN`, in the future, and has no booking

### 7.4 Booking

Fields:
- `id`
- `slotId`
- `offerId`
- `studentId`
- `mentorId`
- `status`
- `priceCredits`
- `reservedAmount`
- `cancelledByUserId`
- `noShowSide`
- `createdAt`
- `updatedAt`

`status`:
- `RESERVED`
- `COMPLETED`
- `CANCELLED`

Rules:
- self-booking is not supported in MVP
- a booking can only be created for a free slot
- booking requires sufficient available balance
- credits are reserved when booking is created
- repeated booking of the same slot must fail
- `Booking` is the single source of truth for the deal state
- when a `Booking` is created in `RESERVED`, the slot becomes `BOOKED`
- if a booking is cancelled before `slot.startTime`, the slot becomes `OPEN` again
- once a booking reaches `COMPLETED`, the slot is not reopened by later cancellation

### 7.5 Wallet

Fields:
- `id`
- `userId`
- `balance`
- `reservedBalance`
- `updatedAt`

Rules:
- `balance` cannot be negative
- `reservedBalance` cannot be negative
- available balance = `balance - reservedBalance`
- wallet top-up with test credits is available only for `STUDENT`
- a `MENTOR` wallet is a read-only earnings and payout history view in the UI
- when booking is created, the amount is moved into reserve
- after `COMPLETED`, the amount is deducted from the student and credited to the mentor
- cancellation releases or reverses funds according to whether the booking is `RESERVED` or `COMPLETED`

### 7.6 Transaction

Fields:
- `id`
- `walletId`
- `bookingId`
- `type`
- `amount`
- `status`
- `createdAt`

`type`:
- `TOP_UP`
- `RESERVE`
- `RELEASE`
- `CAPTURE`
- `REFUND`
- `PAYOUT`
- `ADJUSTMENT`

`status`:
- `CREATED`
- `COMPLETED`
- `FAILED`

### 7.7 Review

Fields:
- `id`
- `bookingId`
- `authorId`
- `targetUserId`
- `rating`
- `comment`
- `createdAt`

Rules:
- guest may only read public reviews through offer pages
- a student may create a review only for their own `COMPLETED` booking in the normal user flow
- a student may edit or delete only their own existing reviews
- a mentor may read reviews but may not create, edit, or delete them in MVP
- one user can leave only one review for a specific booking in the normal student flow
- self-review is not supported in MVP
- admin may create reviews in supported administrative scope, including flows that do not require a `COMPLETED` booking
- admin may edit or delete any review in supported administrative scope

### 7.8 Dispute

For MVP, this is implemented in a simplified form.

Fields:
- `id`
- `bookingId`
- `createdBy`
- `reason`
- `description`
- `status`
- `resolution`
- `createdAt`
- `updatedAt`

`status`:
- `OPEN`
- `UNDER_REVIEW`
- `RESOLVED`
- `REJECTED`

Rules:
- a dispute can only be opened for an existing booking
- a dispute does not trigger automatic money recalculation in MVP
- admin may review and update dispute data or statuses that are explicitly supported by the system
- admin dispute creation, where supported, may be exposed through a dedicated admin UI flow that still opens a dispute for a specific booking

---

## 8. MVP Business Rules

### 8.1 Registration and Login

- email must be unique
- password must satisfy minimal requirements
- during registration, the user must choose at least one role from `STUDENT` and `MENTOR`
- after login, the user receives a JWT access token

### 8.2 Offer Creation

- a user with the `MENTOR` role can create an offer
- admin may also create an offer where the system explicitly supports administrative offer management
- title is required
- `priceCredits` must be greater than 0
- `durationMinutes` must be greater than 0

### 8.3 Slot Creation

- a slot cannot be created in the past
- a slot must not overlap with another active slot of the same mentor
- a slot can only be created for the user's own offer unless admin is performing an administrative action supported by the system
- a slot can only be created for an offer with status `ACTIVE`

### 8.4 Booking

- a user with the `STUDENT` role can create a booking
- admin may also create a booking where the system explicitly supports administrative booking actions
- self-booking is not supported in MVP
- booking requires sufficient available balance
- an inactive offer cannot be booked
- an already occupied slot cannot be booked

### 8.5 Cancellation

All timestamps and comparisons must use UTC.

Rules:
- for MVP, `cancellationPolicyHours` is stored but not used in booking calculations
- cancelling a `RESERVED` booking releases the student's reserved credits
- cancelling a `COMPLETED` booking refunds the student and deducts the same amount from the mentor
- admin may also cancel supported bookings in administrative scope while preserving the system's financial and slot-state rules
- if cancellation happens before `slot.startTime`, the slot becomes `OPEN` again
- if `slot.startTime` has already been reached or passed, the slot is not reopened

### 8.6 Session Completion

- the mentor completes the booking through `complete`
- admin may also complete a supported booking in administrative scope
- after `COMPLETED`, the reserved amount is transferred to the mentor
- after `COMPLETED`, review creation becomes available

### 8.7 Administration

- admin can update user profile data, roles, and statuses through supported admin tools
- admin can update offer content and supported offer statuses
- admin can manage slots in administrative scope, subject to normal slot integrity rules
- admin can view and mutate bookings through supported administrative actions
- admin can inspect wallets, top up credits, and view wallet transactions
- admin can review and update disputes
- admin can perform other supported cross-entity administrative actions without ownership restrictions

---

## 9. Booking States and Transitions

Base lifecycle:

`RESERVED -> COMPLETED`

Terminal alternative branches:
- `RESERVED -> CANCELLED`

Restrictions:
- `COMPLETED` cannot transition back to a previous state
- a review cannot be created before `COMPLETED`
- a `COMPLETED` booking may be cancelled only through supported refund/reversal behavior
- a separate `confirm-completion` endpoint is not required in MVP

---

## 10. REST API — Preliminary Contract

### 10.1 Auth

#### POST `/api/auth/register`

Request:
- `email`
- `password`
- `firstName`
- `lastName`
- `roles`

Restrictions:
- allowed roles for public registration: `STUDENT`, `MENTOR`
- `ADMIN` cannot be passed

Response:
- `userId`
- `email`
- `roles`
- `status`

#### POST `/api/auth/login`

Request:
- `email`
- `password`

Response:
- `accessToken`
- `tokenType`
- `expiresIn`
- `user`

### 10.2 Users

#### GET `/api/users`

List users for admin.

#### GET `/api/users/{id}`

Get a user profile. Regular users may access only their own profile; `ADMIN` may access any user.

#### PATCH `/api/users/{id}`

Update a user profile. Regular users may update only their own profile-safe fields; `ADMIN` may also update email, roles, and status.

### 10.3 Offers

#### GET `/api/offers`

Public offer list.

Support:
- pagination
- sorting
- filtering by category
- filtering by price range
- search by title

#### POST `/api/offers`

Create an offer. Requires the `MENTOR` role or supported admin privileges.

#### GET `/api/offers/{id}`

Get offer details.

#### PATCH `/api/offers/{id}`

Update the user's own offer or an offer in admin scope.

#### PATCH `/api/offers/{id}/status`

Change offer status.

Status transition rules:
- owner may switch `DRAFT <-> ACTIVE`
- owner may move `ACTIVE -> ARCHIVED`
- admin may perform any offer status transition that is explicitly supported by the system
- admin-managed blocking or unblocking behavior must remain explicit and auditable

#### GET `/api/offers?scope=all`

List all offers in management scope. Supported for `ADMIN`.

### 10.4 Slots

#### POST `/api/offers/{offerId}/slots`

Create a slot. Requires the `MENTOR` role or supported admin privileges.

#### GET `/api/offers/{offerId}/slots`

Get offer slots.

#### DELETE `/api/slots/{slotId}`

Delete a slot.

Restrictions:
- slot must be `OPEN`
- slot must be in the future
- slot must not have any booking
- owner or admin may perform the deletion if the system supports the action in that scope

### 10.5 Bookings

#### POST `/api/bookings`

Create a booking.

Request:
- `slotId`
- `studentId`

Response:
- `bookingId`
- `status`
- `reservedAmount`

Requires:
- authenticated caller

Rules:
- `studentId` is always required
- a `STUDENT` may create a booking only for their own `studentId`
- `ADMIN` may create a booking for any selected user who has the `STUDENT` role
- wallet reserve and later settlement apply to the selected student user, not to the admin

#### GET `/api/bookings`

List accessible bookings.

#### GET `/api/bookings/{id}`

Get booking details.

Access rules:
- booking student
- booking mentor
- admin

#### POST `/api/bookings/{id}/cancel`

Cancel a booking.

Access rules:
- booking student may cancel their own booking before `COMPLETED`
- booking mentor may cancel a booking linked to their own offer before `COMPLETED`
- admin may cancel a supported booking in administrative scope

Behavior:
- financial outcome depends on whether the booking is `RESERVED` or `COMPLETED`

#### POST `/api/bookings/{id}/complete`

Move booking to `COMPLETED` and capture funds.

Access rules:
- booking mentor
- admin

- admin

### 10.6 Wallet

#### GET `/api/users/{id}/wallet`

Get wallet data. Regular users may access only their own wallet; `ADMIN` may access any supported wallet.

#### POST `/api/users/{id}/wallet/top-up`

Top up balance with test credits.

Access:
- `STUDENT`
- not available for `MENTOR`

#### GET `/api/users/{id}/wallet/transactions`

Get wallet transaction history. Regular users may access only their own history; `ADMIN` may access any supported wallet history.

### 10.7 Reviews

#### POST `/api/bookings/{id}/reviews`

Create a review.

Access:
- booking student in the normal flow
- admin where the system explicitly supports administrative review creation

#### PATCH `/api/reviews/{id}`

Update an existing review.

Access:
- review author for their own review in the normal student flow
- admin in supported administrative scope

#### DELETE `/api/reviews/{id}`

Delete an existing review.

Access:
- review author for their own review in the normal student flow
- admin in supported administrative scope

#### GET `/api/offers/{offerId}/reviews`

Get reviews for an offer.

#### GET `/api/reviews`

List accessible reviews.

#### GET `/api/reviews/{id}`

Get review details.

#### POST `/api/reviews`

Create a direct review in administrative scope. Supported for `ADMIN`.

### 10.8 Disputes

#### POST `/api/bookings/{id}/disputes`

Open a dispute.

Access:
- case participant in the normal flow
- admin where the system explicitly supports administrative dispute creation

Notes:
- admin UI may expose dispute creation as a dedicated admin page, but the dispute still belongs to a specific booking

#### GET `/api/disputes/{id}`

Get a dispute by id for a case participant or admin.

#### GET `/api/disputes`

List accessible disputes.

#### PATCH `/api/disputes/{id}`

Update dispute status.

---

## 11. HTTP Errors and Validation

Support centralized handling for:
- `400 Bad Request` — invalid data
- `401 Unauthorized` — unauthenticated request
- `403 Forbidden` — insufficient permissions
- `404 Not Found` — entity not found
- `409 Conflict` — state conflict, for example slot already booked
- `422 Unprocessable Entity` — business validation error

Examples:
- repeated booking of an already occupied slot -> `409`
- trying to leave a review before `COMPLETED` -> `422`
- editing someone else's offer -> `403`
- trying to create an offer without `MENTOR` -> `403`
- trying to create a booking without `STUDENT` -> `403`

Unified error format:
- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `validationErrors`

---

## 12. Frontend UI Screens

### Public

- Login page
- Register page
- Catalog page
- Offer details page

### Private

- Dashboard
- My profile
- My wallet / earnings view
- My bookings
- My offers
- Create/Edit offer
- Slot management
- Review submission form
- Dispute submission form

### Admin

- Admin users page
- Admin offers page
- Admin bookings page
- Admin reviews page
- Admin disputes page

The UI must take the user's roles into account and hide unavailable actions.

---

## 13. Mandatory API Tests

### Auth

- successful registration with `STUDENT`
- successful registration with `MENTOR`
- registration with multiple roles is forbidden
- registration without roles is forbidden
- registration with `ADMIN` is forbidden
- registration with existing email
- login with valid credentials
- login with invalid password

### Offers

- offer creation by user with `MENTOR`
- offer creation forbidden for user without `MENTOR`
- editing own offer
- editing someone else's offer is forbidden
- catalog filtering and pagination
- admin can update a foreign offer
- admin can perform supported offer status changes

### Slots

- valid slot creation
- creating a slot in the past is forbidden
- overlapping slots are forbidden
- deleting a future free slot
- deleting a slot with a booking is forbidden
- admin can create a slot in admin scope
- admin can delete an eligible slot in admin scope

### Bookings

- successful booking by user with `STUDENT`
- booking forbidden without `STUDENT`
- self-booking is forbidden
- failure on insufficient balance
- failure on double booking
- cancellation of `RESERVED` releases reserved credits
- cancellation of `COMPLETED` reverses payout and refunds the student
- transition to `COMPLETED`
- admin can cancel a foreign booking
- admin can complete a foreign booking

### Wallet

- balance top-up
- top-up forbidden for `MENTOR`
- correct reserve on booking
- correct capture on completion
- correct refund/reversal on completed-booking cancellation
- admin can view a foreign wallet
- admin can top up a foreign wallet
- admin can view foreign wallet transactions

### Reviews

- successful review creation after `COMPLETED`
- review before `COMPLETED` is forbidden
- duplicate review is forbidden
- student can edit own review
- student cannot edit чужой review
- student can delete own review
- student cannot delete чужой review
- mentor cannot create a review
- mentor cannot edit or delete reviews
- self-review is forbidden
- admin can create a review in administrative scope without the normal completed-booking restriction
- admin can edit any review
- admin can delete any review

### Disputes

- successful dispute creation
- case participant can retrieve dispute
- admin can update dispute status
- admin can create a dispute in administrative scope if supported by the system

### Admin

- admin can block a user
- admin can update user profile data and roles
- regular user cannot access admin endpoints
- admin can view users, offers, bookings, and disputes
- admin can update foreign offers and supported offer statuses
- admin can mutate foreign bookings through supported admin actions
- admin can inspect and top up foreign wallets

---

## 14. Mandatory UI Tests

### Happy path

1. Register as `STUDENT`
2. Log in
3. Top up wallet
4. Search for an offer
5. Book it
6. Verify booking appears in the account

### Mentor flow

1. Register or log in as `MENTOR`
2. Create an offer
3. Add a slot
4. Verify the slot is displayed

### Single-role boundary flow

1. Verify public registration rejects selecting more than one role
2. Verify admin user edit rejects assigning more than one role
3. Verify a user cannot book their own offer
4. Verify a user cannot review their own booking

### Cancel flow

1. Student cancels a booking
2. Refund policy warning is displayed
3. Updated status and balance are visible in the UI

### Review flow

1. After `COMPLETED`, student leaves a review
2. The review appears on the offer page

### Authorization / Role UI

- a user without `MENTOR` does not see create-offer actions
- a user without `STUDENT` does not see booking actions
- a `MENTOR` sees wallet history but does not see wallet top-up controls
- a regular user does not see admin menu
- an admin sees admin menu
- admin can open a dedicated create-dispute flow where the system supports it
- a guest cannot book without logging in

---

## 15. Proposed Technical Stack

### Backend

- Java 17+
- Spring Boot 3
- Spring Web
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- Validation API

### Frontend

- React
- TypeScript
- Vite
- React Router
- React Query
- MUI

### Testing

- JUnit 5
- REST Assured
- Playwright for Java
- Allure Reports

### Infrastructure

- Docker
- Docker Compose

---

## 16. Backend Module Structure

Recommended package structure:

- `auth`
- `user`
- `offer`
- `slot`
- `booking`
- `wallet`
- `review`
- `dispute`
- `admin`
- `common`
- `security`

Inside each module:
- `controller`
- `service`
- `repository`
- `dto`
- `entity`
- `mapper`
- `exception`

---

## 17. Seed Data

Recommended initial data for testing:
- `admin@test.com`
- `mentor1@test.com`
- `mentor2@test.com`
- `mentor3@test.com`
- `student1@test.com`
- `student2@test.com`

Recommended roles:
- `admin@test.com` -> `ADMIN`
- `mentor1@test.com` -> `MENTOR`
- `mentor2@test.com` -> `MENTOR`
- `mentor3@test.com` -> `MENTOR`
- `student1@test.com` -> `STUDENT`
- `student2@test.com` -> `STUDENT`

Also useful to pre-create:
- 5-10 offers
- 10-20 slots
- several `COMPLETED` bookings
- several `ARCHIVED` offers
- several disputes in different statuses

Seed data should be deterministic.

---

## 18. V2 Extensions

After MVP, the following may be added:
- full dispute workflow
- notifications
- waitlist for occupied slots
- promo codes
- trust score
- auto-cancel scheduler
- audit log
- webhooks/mock integrations
- file attachments in disputes
- report abuse
- mentor verification moderation flow

---

## 19. What Matters for Implementation in Codex

Implementation should focus not on the maximum number of features, but on quality and testability.

Key requirements:
- clean and understandable architecture
- predictable statuses
- real business validations
- unified error format
- strong seed data
- UI convenient for E2E
- `data-testid` for key elements
- several strong end-to-end scenarios
- dates stored in UTC
- double-booking protection at database and transaction level

---

## 20. Practical Implementation Plan

### Phase 1

- set up backend skeleton
- configure PostgreSQL and Flyway
- configure security and JWT
- implement auth + users + roles

### Phase 2

- implement offers + slots
- implement catalog
- add ownership checks and role-based access

### Phase 3

- implement wallet + ledger
- implement bookings
- add reserve/cancel/capture/payout logic
- add double-booking protection

### Phase 4

- implement reviews
- implement disputes in MVP scope
- add admin endpoints

### Phase 5

- set up frontend
- build core screens
- integrate with backend
- add role-based UI

### Phase 6

- write API test suite
- write Playwright UI suite
- integrate Allure
- prepare seed data and demo scenarios

---

## 21. Final Recommendation

For a demo project, it is better not to build a full marketplace. A compact but logically rich system is the stronger choice.

`SkillSwap Market` is a good fit because it contains:
- roles
- statuses
- booking lifecycle
- money logic through internal credits
- conflict scenarios
- natural API and UI tests

The MVP can be summarized as:

`SkillSwap Market` is a marketplace of time slots where each user has exactly one role (`STUDENT`, `MENTOR`, or `ADMIN`) in the MVP, the slot is responsible only for time availability, the booking is the single source of truth for the deal state, and dispute in MVP is used for case tracking without automatic financial recalculation.
