# Customer Service Runbook

## Service Information
- **Service**: customer-service
- **Port**: 8082
- **Database**: customerdb (PostgreSQL)
- **API Base**: `/api/v1/customers`
- **Health**: `/actuator/health`

## Health Check
```bash
curl -s http://customer-service:8082/actuator/health | jq .
```

## Common Issues

### 1. Delete Rejection (409)
**Symptoms**: Customer deletion returns 409.
**Cause**: Customer has associated billable hours in billing-service.
**Resolution**: This is expected. Delete billable hours first, then delete customer.

### 2. Inter-Service Communication Failure
**Symptoms**: Delete guard check fails, customer deletion always succeeds.
**Cause**: billing-service `/api/v1/hours/exists` endpoint unreachable.
**Resolution**: Verify billing-service is healthy, check network policies.

## Database Queries
```sql
-- Count customers
SELECT count(*) FROM customers;

-- Find customer by name
SELECT id, name, created_at FROM customers WHERE name ILIKE '%<search>%';
```
