# TrackWise reliability contract (v2)

Service: GET /v1/track, shipment tracking lookup.

## SLA (the customer promise)
99.5% monthly availability. A request fails if it gets a 5xx, times out,
or cannot connect. 4xx (client mistakes) do not count.
Excluded: approved tests, other paths, announced maintenance up to 4h/month.

## SLO (our stricter internal target)
Availability: 99.9% over a rolling 30 days.
Latency: 95% of requests under 300 ms over a rolling 30 days.

## How we measure
Availability: a blackbox probe from infra01 requests /v1/track with a
known-valid ID every 5s. Good = HTTP 2xx within 3s. Anything else is bad.
Latency: the app's request histogram (needs real traffic).
App-side error rate, traffic and `up` are diagnostics, not the SLI.

## Error budget
0.1% of 30 days = 43.2 minutes of failed probes.
NORMAL >50% | CAUTION 25-50% | FREEZE 0-25% | EXHAUSTED 0%

## Lab limits
- Planned lab shutdown is not counted (start/stop order below).
- Restarts and experiments DO spend budget; that is the point.
- The live window is shortened so burn is visible; 30-day behaviour is
  checked with promtool on synthetic data.

## Current Status

Phase 1 complete: contract defined.
