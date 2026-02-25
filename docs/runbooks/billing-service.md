# Billing Service Runbook

## Service Information
- **Service**: billing-service
- **Port**: 8083
- **Database**: billingdb (PostgreSQL)
- **API Base**: `/api/v1/categories`, `/api/v1/hours`, `/api/v1/billing/summary`
- **Health**: `/actuator/health`
- **Events**: Publishes to `billing-events` topic via Dapr

## Health Check
```bash
curl -s http://billing-service:8083/actuator/health | jq .
```

## Common Issues

### 1. 24-Hour Cap Exceeded
**Symptoms**: Hour creation returns validation error.
**Cause**: User has >= 24 hours logged for the work date.
**Resolution**: Expected behaviour. Check existing hours for user+date.

### 2. Billing Summary Slow
**Symptoms**: `/api/v1/billing/summary` p95 > 2s.
**Cause**: Large date range or many billable hours.
**Resolution**: Check query plans, add indexes on (customer_id, work_date).

### 3. Event Publishing Failures
**Symptoms**: Reporting service read model not updating.
**Cause**: Dapr sidecar not running or topic misconfigured.
**Resolution**: Check Dapr sidecar logs, verify pub/sub component config.

## Database Queries
```sql
-- Count billable hours
SELECT count(*) FROM billable_hours;

-- Daily hours by user
SELECT user_id, work_date, SUM(hours) as total
FROM billable_hours
GROUP BY user_id, work_date
ORDER BY work_date DESC;
```
