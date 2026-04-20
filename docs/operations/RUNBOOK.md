# SkillSwap Market Runbook

## Purpose

This project uses a deterministic demo baseline.

During the day the environment may become dirty with new users, offers, bookings, reviews, and wallet transactions.

The supported recovery model is a full demo reset:

1. recreate the Postgres database
2. rerun Flyway migrations
3. reseed the same baseline demo data

That model matches a nightly rebuild flow.

## Local URLs

- Frontend preview: `http://127.0.0.1:5173`
- Backend health: `http://127.0.0.1:8080/api/health`

## Demo Accounts

- `admin@test.com / StrongPass1`
- `mentor1@test.com / StrongPass1`
- `mentor2@test.com / StrongPass1`
- `mentor3@test.com / StrongPass1`
- `student1@test.com / StrongPass1`
- `student2@test.com / StrongPass1`

## Standard Start

### 1. Start Postgres

```powershell
docker compose -f .\infra\docker-compose.yml up -d
```

### 2. Start backend

Run the packaged jar from the `backend` directory:

```powershell
java -jar .\target\skillswap-market-backend-0.0.1-SNAPSHOT.jar
```

At startup the backend will:

- apply Flyway migrations
- seed demo users
- seed baseline offers, slots, bookings, and reviews

### 3. Start frontend

From the `frontend` directory:

```powershell
npm run dev -- --host 127.0.0.1
```

## Full Demo Reset

Use this when the environment is dirty and you want to return to the official baseline state.

```powershell
.\scripts\reset-demo-environment.ps1
```

What it does:

1. stops the local backend jar if it is running
2. destroys the Postgres container volume
3. recreates Postgres
4. waits for Postgres health
5. restarts the backend jar
6. waits for backend health

The backend startup then recreates the schema and reseeds the baseline data.

## Reset Without Backend Restart

If you only want to reset the database and restart the backend yourself:

```powershell
.\scripts\reset-demo-environment.ps1 -SkipBackendRestart
```

After that, restart the backend manually so Flyway and demo seeds run again.

## What Reset Removes

Reset removes everything created after the baseline state, including:

- newly registered users
- newly created offers and slots
- test bookings
- wallet changes
- new reviews

## What Reset Restores

Reset restores the same deterministic seeded state described in [BASELINE_DEMO_DATA.md](C:/Users/Vitali/IdeaProjects/skillswap-market/docs/operations/BASELINE_DEMO_DATA.md).

## Recommended Testing Model

- use the app freely during manual or automated testing
- allow data to accumulate during the day
- rely on full reset or nightly rebuild to restore the clean state

Cleanup endpoints are not part of the current MVP approach.
