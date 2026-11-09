# TrackWise SLO Lab

A small reliability project. TrackWise is a fictional shipment-tracking API with a written reliability contract, and this repo holds the monitoring that measures whether the contract is being kept.

I built it to learn how SLIs, SLOs, error budgets and SLAs fit together on a real service, using Linux, systemd, Prometheus and Grafana. It runs on two RHEL VMs:

- **control01** runs the TrackWise app (Spring Boot, port 8080)
- **infra01** runs Prometheus, the blackbox exporter and Grafana

[add: where the app code lives]

## The contract

Service: `GET /v1/track`, the shipment lookup.

**SLA (the customer promise).** 99.5% monthly availability. A request fails if it gets a 5xx, times out, or cannot connect. 4xx responses are client mistakes and do not count. Excluded: approved tests, other paths, and announced maintenance up to 4 hours a month.

**SLO (my stricter internal target).**
- Availability: 99.9% over a rolling 30 days.
- Latency: 95% of requests under 300 ms over a rolling 30 days. (Planned, not measured yet.)

The SLO is tighter than the SLA on purpose. The gap between 0.1% and 0.5% of 30 days (about 173 minutes) is the warning room before the promise is broken.

## How availability is measured

A blackbox probe on infra01 requests `/v1/track` every 5 seconds. Good means an HTTP 2xx within 3 seconds. Anything else is bad. This is slightly stricter than the SLA, because the probe sends a valid request and a valid request should always succeed.

The probe measures from outside the service. The app's own metrics (request rate, 5xx rate, `up`) are diagnostics for finding out why something is wrong, not the SLI. See "What I found" for why.

## Error budget

0.1% of 30 days is 43.2 minutes of failed probes.

    error ratio      = 1 - availability
    budget remaining = 1 - (error ratio / 0.001)
    burn rate        = error ratio / 0.001

A burn rate of 1 spends the budget exactly over the window. A value of 10 spends it ten times too fast.

| Remaining | State | What it means |
|---|---|---|
| over 50% | NORMAL | continue normal work and experiments |
| 25-50% | CAUTION | be careful with risky changes |
| 0-25% | FREEZE | stop feature work, focus on reliability |
| 0% or below | EXHAUSTED | security fixes and recovery only |

A negative value means the budget is overspent, and the dashboard shows it that way instead of clamping to zero.

## Lab limits

- The VMs are shut down between sessions. Planned shutdowns are not counted. Start control01 first, then infra01. Stop infra01 first, then control01, so the probe never runs against an app that is down on purpose.
- The live error budget uses a 6-hour window, so I can watch it move in one session. The contract stays at 30 days.
- Restarts and experiments spend real budget. This is a single-instance service, and that is the point.

## What's here

- `monitoring/prometheus.yml`: scrape jobs for the app, the probe and node exporters
- `monitoring/blackbox.yml`: the `http_trackwise` probe module
- `monitoring/rules.yml`: recording rules for availability (5m and 6h) and error budget remaining
- `monitoring/dashboard_trackwise.json`: the Grafana dashboard export

## What I found

**My first availability measure had a blind spot.** I originally measured availability from the app's own request counters. When I stopped the service, that panel kept looking healthy for about 7 minutes in Prometheus (8-9 in Grafana), and then the line stopped. It never dropped to zero, because a dead app produces no failed requests to count. The `up` metric went to 0 immediately, because Prometheus creates it itself on every scrape. A blackbox probe does the same: it makes the request and records the failure. After I moved the SLI to the probe, `probe_success` dropped to 0 within seconds and recovered as soon as I restarted the service.

## Still to do

- Burn-rate alert
- Latency SLI from a 300 ms histogram bucket
- Rebuild the dashboard around the contract
- Test the 30-day logic with promtool on synthetic data
- One scored experiment with a prediction written first
- A postmortem
