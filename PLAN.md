# Zendesk Ticket Analysis Dashboard

## Overview

A dashboard that connects to the Zendesk API, fetches ticket data, and displays analytics about where the team is spending the most time. Built with a **Go backend** and a **simple frontend** (HTML/CSS/JS).

---

## Architecture

```
┌─────────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│   Frontend (SPA)    │──────▶│   Go Backend     │──────▶│  Zendesk API    │
│   HTML/CSS/JS       │◀──────│   REST API       │◀──────│                 │
│   Chart.js          │       │   SQLite cache   │       │                 │
└─────────────────────┘       └──────────────────┘       └─────────────────┘
```

- **Frontend**: Vanilla HTML/JS with Chart.js for graphs. Served by the Go backend as static files.
- **Backend**: Go HTTP server that fetches from Zendesk, caches in SQLite, and exposes REST endpoints.
- **Storage**: SQLite for local caching so we don't hit Zendesk API limits on every page load.

---

## Project Structure

```
zendesk-ticket-analyse/
├── PLAN.md
├── go.mod
├── go.sum
├── main.go                  # Entry point, starts HTTP server
├── .env.example             # Example env vars (Zendesk credentials)
├── internal/
│   ├── config/
│   │   └── config.go        # Load env vars / config
│   ├── zendesk/
│   │   ├── client.go        # Zendesk API client
│   │   └── models.go        # Zendesk data models (Ticket, User, etc.)
│   ├── store/
│   │   ├── sqlite.go        # SQLite setup and migrations
│   │   └── queries.go       # DB queries for analytics
│   ├── analytics/
│   │   └── analytics.go     # Business logic: compute time-spent, breakdowns
│   └── handler/
│       ├── handler.go       # HTTP handlers (API routes)
│       └── routes.go        # Route registration
└── web/
    ├── index.html           # Main dashboard page
    ├── style.css            # Styles
    └── app.js               # Frontend logic, fetch API data, render charts
```

---

## Backend Details

### Tech Stack

| Component   | Choice                                     |
| ----------- | ------------------------------------------ |
| Language    | Go 1.22+                                   |
| Router      | `net/http` (stdlib) with `http.ServeMux`   |
| Database    | SQLite via `modernc.org/sqlite` (pure Go)  |
| Zendesk API | Custom HTTP client using `net/http`        |
| Config      | Environment variables (`.env` file)        |

### Zendesk API Integration

- **Auth**: API token authentication (`{email}/token:{api_token}`)
- **Endpoints used**:
  - `GET /api/v2/tickets.json` — list tickets (with pagination)
  - `GET /api/v2/tickets/{id}/audits.json` — ticket history/events
  - `GET /api/v2/tickets/{id}/metrics.json` — time metrics per ticket

- **Rate limiting**: Respect Zendesk's `Retry-After` header, implement backoff
- **Sync strategy**: Incremental sync using `updated_after` parameter

### REST API Endpoints

```
GET  /api/sync                  # Trigger a sync from Zendesk
GET  /api/summary               # Overall summary stats
GET  /api/tickets/time-spent    # Tickets ranked by time spent

GET  /api/tickets/by-status     # Breakdown by status
GET  /api/tickets/by-priority   # Breakdown by priority
GET  /api/tickets/trends        # Ticket volume over time (daily/weekly)

```

### SQLite Schema

```sql
CREATE TABLE tickets (
    id              INTEGER PRIMARY KEY,
    subject         TEXT,
    description     TEXT,
    status          TEXT,
    priority        TEXT,
    assignee_id     INTEGER,
    created_at      DATETIME,
    updated_at      DATETIME,
    solved_at       DATETIME,
    first_reply_at  DATETIME,
    full_resolution_minutes INTEGER,
    first_reply_minutes     INTEGER,
    agent_wait_minutes      INTEGER,
    requester_wait_minutes  INTEGER,
    tags            TEXT  -- JSON array stored as text
);


CREATE TABLE sync_state (
    id              INTEGER PRIMARY KEY DEFAULT 1,
    last_synced_at  DATETIME
);
```

---

## Frontend Details

### Tech Stack

- **No build step** — plain HTML, CSS, JS
- **Chart.js** (via CDN) for charts and graphs
- **Fetch API** to call backend endpoints

### Dashboard Sections

1. **Summary Cards** (top row)
   - Total tickets
   - Average resolution time
   - Average first reply time
   - Open tickets count

2. **Top Time-Consuming Tickets** (table)
   - Ticket ID, subject, status, priority, total resolution time
   - Sortable columns, clickable links to Zendesk

3. **Tickets by Priority** (doughnut chart)
   - Urgent / High / Normal / Low distribution

4. **Tickets by Status** (doughnut chart)
   - Open / Pending / Solved / Closed

5. **Ticket Volume Trend** (line chart)
   - Daily/weekly ticket creation over time

---

## Implementation Plan

### Phase 1: Project Setup
- [ ] Initialize Go module (`go mod init`)
- [ ] Create project directory structure
- [ ] Set up config loading from `.env`
- [ ] Create `.env.example` with required variables

### Phase 2: Zendesk Client
- [ ] Implement Zendesk API client with token auth
- [ ] Add ticket list fetching with pagination
- [ ] Add ticket metrics fetching

- [ ] Handle rate limiting and errors

### Phase 3: Storage Layer
- [ ] Set up SQLite database with auto-migration
- [ ] Implement ticket upsert (insert or update)
- [ ] Implement sync state tracking
- [ ] Write analytics queries (time-spent ranking, status/priority breakdown, etc.)

### Phase 4: API Handlers
- [ ] Set up HTTP server and router
- [ ] Implement `/api/sync` endpoint
- [ ] Implement all analytics endpoints
- [ ] Serve static files from `web/` directory
- [ ] Add CORS headers if needed

### Phase 5: Frontend Dashboard
- [ ] Build HTML layout with summary cards, tables, and chart containers
- [ ] Style with CSS (clean, minimal design)
- [ ] Fetch data from API and render summary cards
- [ ] Render charts using Chart.js
- [ ] Add ticket table with sorting
- [ ] Add "Sync Now" button

### Phase 6: Polish
- [ ] Add loading states and error handling on frontend
- [ ] Add periodic background sync option
- [ ] Test end-to-end with real Zendesk data

---

## Configuration

Required environment variables (`.env` file):

```env
ZENDESK_SUBDOMAIN=yourcompany        # yourcompany.zendesk.com
ZENDESK_EMAIL=admin@yourcompany.com
ZENDESK_API_TOKEN=your_api_token_here
SERVER_PORT=8080
```

To get a Zendesk API token:
1. Go to Zendesk Admin Center
2. Apps and integrations > Zendesk API
3. Enable Token Access and create a new token

---

## How to Run

```bash
# 1. Clone and enter the project
cd zendesk-ticket-analyse

# 2. Copy env file and fill in credentials
cp .env.example .env
# edit .env with your Zendesk credentials

# 3. Run the server
go run main.go

# 4. Open browser
open http://localhost:8080
```

---

## Key Metrics Tracked

| Metric                    | Source                         | Purpose                          |
| ------------------------- | ------------------------------ | -------------------------------- |
| Full resolution time      | Ticket metrics API             | Which tickets take longest       |
| First reply time          | Ticket metrics API             | Response speed                   |
| Agent wait time           | Ticket metrics API             | Time agents spend working        |
| Requester wait time       | Ticket metrics API             | Customer wait experience         |
| Ticket volume over time   | Ticket `created_at`            | Workload trends                  |
| Priority distribution     | Ticket `priority`              | Urgency breakdown                |
