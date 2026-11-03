# TrackWise SLO Lab

Simple two-host reliability lab for a fictional shipment-tracking API.

## Architecture

- **control01**: TrackWise Spring Boot app (`/v1/track`) on port 8080
- **infra01**: Prometheus, Alertmanager, Blackbox, Grafana + this repository

## Reliability Contract

**Customer journey**  
`GET /v1/track` — returns status of a demonstration shipment.

**Customer SLA**  
99.5% successful eligible requests per calendar month.  
Success = not HTTP 5xx.  
Excluded: 4xx, approved tests, non-`/v1/track` traffic, scheduled maintenance (max 4 h/month with notice).

**Internal SLO**  
99.9% successful eligible requests over a rolling 30-day window.  
Secondary goal: p95 latency ≤ 300 ms.

**Error budget**  
0.1% (the room under 99.9%).

**Budget policy**
- \> 50% left → NORMAL
- 25–50% left → CAUTION
- 0–25% left → FREEZE
- 0% left → EXHAUSTED

**Lab limitation**  
Single-instance service. Restarts, patches and experiments spend real budget. The lab makes that cost visible.

## Current Status

Phase 1 complete: contract defined.
