# API Contract: Billing & Time Tracking Service

**Version**: v1 | **Base Path**: `/api/v1`

## Billing Categories

### List Categories
- **GET** `/api/v1/categories?page=0&size=20&sort=name,asc`
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "string",
        "hourlyRate": 150.00,
        "createdAt": "2026-02-23T10:00:00Z",
        "updatedAt": "2026-02-23T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  },
  "errors": null
}
```

### Create Category
- **POST** `/api/v1/categories`
- **Request Body**:
```json
{
  "name": "string (1-100 chars, unique)",
  "hourlyRate": 150.00
}
```
- **Response 201**: Created category in `data` field.
- **Response 400/422**: Validation errors — rate must be > 0.

### Update Category
- **PUT** `/api/v1/categories/{id}`
- **Response 200**: Updated category.

### Delete Category
- **DELETE** `/api/v1/categories/{id}`
- **Response 204**: No content.
- **Response 409**: Cannot delete — category has associated billable hours.

## Billable Hours

### List Hours
- **GET** `/api/v1/hours?page=0&size=20&sort=workDate,desc&userId={uuid}&customerId={uuid}&categoryId={uuid}&fromDate=2026-01-01&toDate=2026-02-23`
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "customerId": "uuid",
        "categoryId": "uuid",
        "hours": 8.0,
        "workDate": "2026-02-23",
        "createdAt": "2026-02-23T10:00:00Z",
        "updatedAt": "2026-02-23T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 500,
    "totalPages": 25
  },
  "errors": null
}
```

### Create Billable Hour
- **POST** `/api/v1/hours`
- **Request Body**:
```json
{
  "userId": "uuid",
  "customerId": "uuid",
  "categoryId": "uuid",
  "hours": 8.0,
  "workDate": "2026-02-23"
}
```
- **Response 201**: Created entry in `data` field.
- **Response 400/422**: Validation — hours must be > 0 and <= 24, workDate must not be future, total hours for user+date must not exceed 24.

### Update Billable Hour
- **PUT** `/api/v1/hours/{id}`
- **Response 200**: Updated entry.

### Delete Billable Hour
- **DELETE** `/api/v1/hours/{id}`
- **Response 204**: No content.

## Billing Summary
- **GET** `/api/v1/billing/summary?customerId={uuid}&fromDate=2026-01-01&toDate=2026-02-23`
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "customerId": "uuid",
    "customerName": "string",
    "fromDate": "2026-01-01",
    "toDate": "2026-02-23",
    "categories": [
      {
        "categoryId": "uuid",
        "categoryName": "string",
        "hourlyRate": 150.00,
        "totalHours": 40.0,
        "totalAmount": 6000.00
      }
    ],
    "grandTotalHours": 80.0,
    "grandTotalAmount": 12000.00
  },
  "errors": null
}
```

## Events Published (via Dapr pub/sub)

| Event | Topic | Payload |
|-------|-------|---------|
| `hour.created` | `billing-events` | `{ id, userId, customerId, categoryId, hours, workDate }` |
| `hour.updated` | `billing-events` | `{ id, userId, customerId, categoryId, hours, workDate }` |
| `hour.deleted` | `billing-events` | `{ id }` |

## Authentication
- All endpoints require Azure Entra ID bearer token.
- RBAC: `billing.read` scope for GET, `billing.write` scope for POST/PUT/DELETE.
