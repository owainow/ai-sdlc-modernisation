# API Contract: Reporting Service (CQRS Read Model)

**Version**: v1 | **Base Path**: `/api/v1/reports`

## Endpoints

### Monthly Report
- **GET** `/api/v1/reports/monthly?year=2026&month=2&customerId={uuid}`
- **Description**: Returns billing totals broken down by customer, category, and user for a given month.
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "year": 2026,
    "month": 2,
    "customers": [
      {
        "customerId": "uuid",
        "customerName": "string",
        "totalHours": 160.0,
        "totalAmount": 24000.00,
        "categories": [
          {
            "categoryName": "string",
            "hourlyRate": 150.00,
            "totalHours": 80.0,
            "totalAmount": 12000.00
          }
        ],
        "users": [
          {
            "userId": "uuid",
            "userName": "string",
            "totalHours": 80.0
          }
        ]
      }
    ],
    "grandTotalHours": 320.0,
    "grandTotalAmount": 48000.00
  },
  "errors": null
}
```

### Date Range Report
- **GET** `/api/v1/reports/range?fromDate=2026-01-01&toDate=2026-02-23&customerId={uuid}`
- **Description**: Returns billing totals for an arbitrary date range. Supports up to 12-month spans (SC-010 requirement: results within 3 seconds).
- **Response 200**: Same structure as Monthly Report but across the date range.
- **Response 400**: Date range exceeds 12 months or fromDate > toDate.

### User Utilisation Report
- **GET** `/api/v1/reports/utilisation?year=2026&month=2&userId={uuid}`
- **Description**: Returns hours logged by a user broken down by customer and category.
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "userId": "uuid",
    "userName": "string",
    "year": 2026,
    "month": 2,
    "totalHours": 160.0,
    "customers": [
      {
        "customerName": "string",
        "totalHours": 80.0,
        "categories": [
          {
            "categoryName": "string",
            "totalHours": 40.0,
            "totalAmount": 6000.00
          }
        ]
      }
    ]
  },
  "errors": null
}
```

## Data Source

The Reporting service maintains a **read-optimised projection** of billing data, updated by subscribing to events from the Billing & Time Tracking service.

### Events Consumed (via Dapr pub/sub)

| Event | Topic | Action |
|-------|-------|--------|
| `hour.created` | `billing-events` | Insert into read model |
| `hour.updated` | `billing-events` | Update read model |
| `hour.deleted` | `billing-events` | Remove from read model |

### Consistency Model

- **Eventually consistent**: Read model is updated asynchronously after billing events.
- **Staleness SLA**: Read model is updated within 5 seconds of the source event.
- **Fallback**: If the read model is unavailable, the service returns HTTP 503 with a `Retry-After` header.

## Authentication
- All endpoints require Azure Entra ID bearer token.
- RBAC: `reports.read` scope for all GET endpoints. No write endpoints — this is a read-only service.
