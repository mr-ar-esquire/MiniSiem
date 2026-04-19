# mini-siem

Built this to understand how basic SIEM systems actually work instead of just reading theory.

The idea was simple — take logs, detect suspicious activity, and turn that into something useful.

---

## what it does

* accepts logs through APIs (single, batch, or file upload)
* detects:

  * repeated failed logins (brute force)
  * basic SQL injection patterns
  * suspicious endpoints like `/admin`
* generates alerts with severity (LOW / MEDIUM / HIGH)
* enriches logs with IP location (GeoIP)
* stores everything in Elasticsearch for search
* has a simple dashboard to view logs + alerts

---

## stack

* Java + Spring Boot
* H2 database (quick setup)
* Elasticsearch (log indexing + search)
* Docker (for ES)
* MaxMind GeoLite2 (IP → location)

---

## how to run

1. start Elasticsearch (Docker)
2. run the app:

```
.\mvnw.cmd spring-boot:run
```

3. open:

* logs → http://localhost:8080/logs
* alerts → http://localhost:8080/alerts
* dashboard → http://localhost:8080/index.html

---

## example

```
POST /logs
{
  "message": "Failed password for admin",
  "source": "8.8.8.8"
}
```

---

## geoip note

the GeoLite2 database is included for convenience.

i’m aware this isn’t ideal for production (file size, updates, etc.), but it makes the project easier to run locally without extra setup.

---

## why i built this

wanted to understand how systems actually detect attacks instead of just logging data.
this helped me get a better idea of:

* how detection rules work
* how logs can be correlated over time
* how alerts are generated

---

## what i’d improve next

* streaming logs using Kafka
* better detection rules / anomaly detection
* proper UI instead of basic dashboard
* authentication

---

not production-ready, but a solid learning project.
