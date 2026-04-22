# MiniSiem

A backend-focused SIEM (Security Information and Event Management) system built with Spring Boot.

I built this to understand how real detection systems work under the hood — not just read about them.
Most SIEM content online explains what they do, not how to actually build one. So I built a small one.

---

## What it does

MiniSiem accepts logs over HTTP, runs them through a detection engine, and generates alerts when
something looks suspicious. It also tags each log with geographic data based on the source IP.

Detection rules currently cover:

- Brute force: multiple failed login attempts from the same IP within a time window
- SQL injection: pattern matching against common payloads (' OR 1=1, UNION SELECT, etc.)
- Suspicious endpoints: requests to paths like /admin, /etc/passwd, /.env

Alerts are categorized as LOW, MEDIUM, or HIGH severity depending on the rule triggered.

Logs are stored in H2 for basic persistence and also indexed in Elasticsearch, which makes it
possible to query and filter them more flexibly.

There's a basic HTML dashboard at /index.html that shows recent logs and active alerts.

---

## Tech stack

- Java 17 + Spring Boot 3
- H2 (in-memory database, mostly for dev convenience)
- Elasticsearch 8 (log indexing and search)
- MaxMind GeoLite2 (IP geolocation)
- Docker (to run Elasticsearch locally)

---

## Project structure
src/
main/
java/com/minisiem/
controller/     -- REST endpoints (LogController, AlertController)
service/        -- detection logic, GeoIP enrichment
repository/     -- H2 + Elasticsearch repositories
model/          -- Log, Alert entities
resources/
static/         -- dashboard HTML
application.properties
---

## How to run

**Prerequisites:**
- Java 17+
- Docker (for Elasticsearch)
- Maven (or use the included wrapper)

**Steps:**

1. Start Elasticsearch:

```bash
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

2. Run the app:

```bash
./mvnw spring-boot:run
# or on Windows:
.\mvnw.cmd spring-boot:run
```

3. Access:

| Endpoint | URL |
|---|---|
| Dashboard | http://localhost:8080/index.html |
| Logs | http://localhost:8080/logs |
| Alerts | http://localhost:8080/alerts |

---

## API examples

**Single log:**
```bash
curl -X POST http://localhost:8080/logs \
  -H "Content-Type: application/json" \
  -d '{"message": "Failed password for root", "source": "192.168.1.10"}'
```

**Batch logs:**
```bash
curl -X POST http://localhost:8080/logs/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"message": "Failed password for root", "source": "192.168.1.10"},
    {"message": "Failed password for admin", "source": "192.168.1.10"}
  ]'
```

**File upload:**
```bash
curl -X POST http://localhost:8080/logs/upload \
  -F "file=@sample-logs.txt"
```

---

## GeoIP note

The GeoLite2 database file is included directly in the repo. I know that's not ideal (it's large
and goes stale), but it makes local setup simpler without needing a MaxMind account. If you want
to keep it updated, you can replace the .mmdb file from MaxMind's download page.

---

## Why I built this

I was studying for a security course and kept running into high-level SIEM diagrams without any
detail on how detection actually works in code. This project helped me understand:

- How logs get ingested and normalized
- How time-windowed detection works (e.g. counting failed logins over 5 minutes)
- How pattern matching fits into a rule-based detection engine
- How alert severity gets determined
- How Elasticsearch fits into a log storage + search pipeline

It's not production-ready. There's no auth, the detection rules are basic, and the UI is minimal.
But it does work, and building it taught me more than any article I read on the topic.

---

## What I'd add next

- Kafka for streaming log ingestion instead of REST-only
- More detection rules (port scanning, privilege escalation patterns)
- Rule configuration through a file or UI, rather than hardcoded
- Authentication layer
- Better frontend (the current dashboard is purely functional)
- Unit tests for the detection logic

---

## Screenshots

See `/screenshots` for the dashboard, alert view, and sample API responses.
