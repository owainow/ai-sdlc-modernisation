# Runbook Template

## Service Information
- **Service**: [Service Name]
- **Owner Team**: Platform Engineering
- **Escalation**: On-call engineer → Team Lead → Engineering Manager

## Health Check
```bash
curl -s http://<service-url>/actuator/health | jq .
```

## Common Failure Modes

### 1. Service Unavailable (503)
**Symptoms**: Health endpoint returns 503, requests failing.
**Diagnosis**:
```bash
# Check ACA revision status
az containerapp revision list -n <app-name> -g <rg> -o table

# Check logs
az containerapp logs show -n <app-name> -g <rg> --tail 100
```
**Resolution**: Check resource limits, database connectivity, restart revision.

### 2. Database Connection Issues
**Symptoms**: Connection timeouts, pool exhaustion.
**Diagnosis**:
```bash
# Check PostgreSQL status
az postgres flexible-server show -n <server-name> -g <rg> -o table

# Check active connections (via psql)
SELECT count(*) FROM pg_stat_activity WHERE datname = '<dbname>';
```
**Resolution**: Check connection pool settings, verify firewall rules, restart if needed.

### 3. High Latency (p95 > 500ms)
**Symptoms**: Slow API responses, alerts triggered.
**Diagnosis**:
```bash
# Check Application Insights
az monitor app-insights query --app <ai-name> -g <rg> \
  --analytics-query "requests | where duration > 500 | summarize count() by name"
```
**Resolution**: Check N+1 queries, Redis cache hit rate, CPU/memory usage.

## Metrics to Monitor
| Metric | Warning | Critical |
|--------|---------|----------|
| CPU Usage | >70% | >85% |
| Memory Usage | >75% | >90% |
| p95 Latency | >300ms | >500ms |
| 5xx Rate | >0.5% | >1% |
| DB Connection Pool | >80% used | >95% used |
