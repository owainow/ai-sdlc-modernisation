# Reporting Service Runbook

## Service Information
- **Service**: reporting-service
- **Port**: 8084
- **Database**: reportingdb (PostgreSQL)
- **API Base**: `/api/v1/reports`
- **Health**: `/actuator/health`
- **Events**: Consumes from `billing-events` topic via Dapr

## Health Check
```bash
curl -s http://reporting-service:8084/actuator/health | jq .
```

## Common Issues

### 1. Stale Read Model
**Symptoms**: Reports missing recent billable hours.
**Cause**: Event consumption lag or failed event processing.
**Resolution**: Check Dapr subscription logs. Verify events are being published.

### 2. 503 Service Unavailable
**Symptoms**: All report endpoints return 503.
**Cause**: Read model database unavailable.
**Resolution**: Check PostgreSQL connectivity. Service will return Retry-After header.

### 3. Report Performance (>3s)
**Symptoms**: Range report exceeds 3s SLA.
**Cause**: Large date range, missing indexes.
**Resolution**: Check query plans, verify indexes on (work_date, customer_id).

## Database Queries
```sql
-- Read model record count
SELECT count(*) FROM billing_read_model;

-- Check for stale entries
SELECT MAX(updated_at) FROM billing_read_model;
```
