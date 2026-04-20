# MVP Implementation Freeze

This document captures the final implementation rules that should be treated as fixed for MVP development.

## 1. Roles

- User roles are stored as a set: `STUDENT`, `MENTOR`, `ADMIN`
- Each user has exactly one role in the MVP
- Public registration may assign only `STUDENT` or `MENTOR`
- `ADMIN` is assigned only through seed data or supported internal administration

## 2. Offer and Slot Model

- `SkillOffer` may be created by a user with `MENTOR`
- `ADMIN` may also create or manage offers and slots where the system explicitly supports administrative actions
- `AvailabilitySlot` is responsible only for time availability
- Slot statuses are limited to `OPEN` and `BOOKED`
- Slot deletion is a hard delete allowed only for a future `OPEN` slot with no booking

## 3. Booking Lifecycle

- Booking statuses are `RESERVED`, `COMPLETED`, `CANCELLED`
- Base lifecycle is `RESERVED -> COMPLETED`
- Booking is the single source of truth for deal state
- When booking is created, slot changes from `OPEN` to `BOOKED`
- If booking is cancelled before slot start, slot returns to `OPEN`
- If booking reached `COMPLETED`, slot is not reopened by later cancellation

## 4. Cancellation Policy

- All dates and comparisons use UTC
- MVP does not use a time-based cancellation penalty
- `cancellationPolicyHours` remains stored in the model but is not used in MVP calculations
- Booking creation charges the student immediately
- Cancelling a `RESERVED` booking refunds the student
- Cancelling a `COMPLETED` booking refunds the student and deducts the same amount from the mentor

## 5. Offer Status Transitions

- Owner may switch `DRAFT <-> ACTIVE`
- Owner may move `ACTIVE -> ARCHIVED`
- Admin may perform any offer status transition that is explicitly supported by the system
- Blocking and unblocking flows must remain explicit and auditable

## 6. Booking Access Rules

- `GET /api/bookings/{id}` is available only to:
- the booking student
- the booking mentor
- admin

- `POST /api/bookings/{id}/cancel` is available only to:
- the booking student
- the booking mentor
- admin

- `POST /api/bookings/{id}/complete` is available to:
- the booking mentor where the action is valid
- admin where the system supports the action in administrative scope

## 7. Admin Scope

- Admin endpoints must exist for users, offers, bookings, wallets, and reviews
- Admin is not limited to read-only access in those areas
- Admin may update user, offer, booking, wallet, and review data or statuses where the system explicitly supports the action
- Admin may access wallet operations and other supported cross-entity administrative tools without ownership restrictions

## 8. Data Integrity

- One slot may have at most one active booking
- Double booking must be prevented at both service and database level
- Booking creation must be transactional

## 9. Immediate Development Order

1. Backend skeleton, PostgreSQL, Flyway, security, JWT
2. Auth, user profile, roles, seed data
3. Offers, slots, catalog
4. Wallet, booking flow, charge/refund/payout
5. Reviews and admin API
6. Frontend core screens
7. API and UI automation suites
