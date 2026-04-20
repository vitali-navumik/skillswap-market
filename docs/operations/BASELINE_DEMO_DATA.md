# Baseline Demo Data

## Baseline Rule

The baseline demo state is the official starting point of the environment after:

1. Postgres is recreated
2. Flyway migrations run
3. demo seeds run

The same baseline should be restored after every full reset or nightly rebuild.

## Seeded Users

### Admin

- `admin@test.com`
  Roles: `ADMIN`

### Mentors

- `mentor1@test.com`
  Roles: `MENTOR`
- `mentor2@test.com`
  Roles: `MENTOR`
- `mentor3@test.com`
  Roles: `MENTOR`

### Students

- `student1@test.com`
  Roles: `STUDENT`
- `student2@test.com`
  Roles: `STUDENT`

All seeded accounts use the same password:

- `StrongPass1`

## Seeded Wallet Baseline

Initial balances are created for demo accounts and then adjusted by scenario seeding where needed.

The practical outcome is:

- student accounts have enough credits for booking scenarios
- mentor accounts can show wallet activity
- admin is not used as a marketplace wallet actor

## Seeded Offers

The baseline includes these official seeded offers:

- `SQL Interview Coaching`
  Mentor: `mentor1@test.com`
  Category: `Databases`
  Status: `ACTIVE`

- `React Pair Debugging`
  Mentor: `mentor3@test.com`
  Category: `Frontend`
  Status: `ACTIVE`

- `Career Strategy Intensive`
  Mentor: `mentor2@test.com`
  Category: `Career`
  Status: `DRAFT`

- `Community Session`
  Mentor: `mentor1@test.com`
  Category: `Operations`
  Status: `BLOCKED`

## Seeded Slot Baseline

The baseline includes future open slots for active offers:

- multiple future open slots for `SQL Interview Coaching`
- multiple future open slots for `React Pair Debugging`

These are used to demonstrate catalog browsing and booking flows.

## Seeded Booking Scenarios

The baseline includes representative marketplace states:

- completed booking for review flow
- reserved future booking

Those scenarios support student, mentor, and admin walkthroughs without additional setup.

## Seeded Reviews

The baseline includes at least one completed booking with an existing student review so the UI can show:

- completed session history
- existing review display
- average rating on offer details

## Important Scope Rule

Anything created after startup is not part of the baseline.

Examples:

- newly registered users
- manually created offers
- extra bookings
- new wallet top-ups
- new reviews

All such data is expected to disappear after a full reset or nightly rebuild.
