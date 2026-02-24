# User Service Runbook

## Service Information
- **Service**: user-service
- **Port**: 8081
- **Database**: userdb (PostgreSQL)
- **API Base**: `/api/v1/users`
- **Health**: `/actuator/health`

## Health Check
```bash
curl -s http://user-service:8081/actuator/health | jq .
```

## Common Issues

### 1. Authentication Failures
**Symptoms**: 401 responses on all endpoints.
**Cause**: Azure Entra ID token validation failing.
**Resolution**: Verify AZURE_AD_TENANT_ID and AZURE_AD_CLIENT_ID environment variables.

### 2. Duplicate Username (409)
**Symptoms**: User creation fails with 409 Conflict.
**Cause**: Username already exists in database.
**Resolution**: This is expected behaviour. Client should use a different username.

### 3. BCrypt Performance
**Symptoms**: Slow user creation response times.
**Cause**: BCrypt hashing is CPU-intensive by design.
**Resolution**: Monitor CPU usage. Scale replicas if p95 > 200ms.

## Database Queries
```sql
-- Count users
SELECT count(*) FROM users;

-- Find user by username
SELECT id, username, created_at FROM users WHERE username = '<username>';
```
