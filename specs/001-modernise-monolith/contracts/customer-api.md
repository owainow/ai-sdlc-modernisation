# API Contract: Customer Management Service

**Version**: v1 | **Base Path**: `/api/v1/customers`

## Endpoints

### List Customers
- **GET** `/api/v1/customers?page=0&size=20&sort=name,asc`
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "string",
        "createdAt": "2026-02-23T10:00:00Z",
        "updatedAt": "2026-02-23T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  },
  "errors": null
}
```

### Get Customer by ID
- **GET** `/api/v1/customers/{id}`
- **Response 200**: Single customer object in `data` field.
- **Response 404**: RFC 7807 Problem Details.

### Create Customer
- **POST** `/api/v1/customers`
- **Request Body**:
```json
{
  "name": "string (1-200 chars, unique)"
}
```
- **Response 201**: Created customer in `data` field.
- **Response 400/422**: Validation errors in RFC 7807 format.
- **Response 409**: Customer name already exists.

### Update Customer
- **PUT** `/api/v1/customers/{id}`
- **Request Body**: Same as Create.
- **Response 200**: Updated customer in `data` field.
- **Response 404**: Customer not found.

### Delete Customer
- **DELETE** `/api/v1/customers/{id}`
- **Response 204**: No content.
- **Response 404**: Customer not found.
- **Response 409**: Cannot delete — customer has associated billable hours.

## Authentication
- All endpoints require Azure Entra ID bearer token.
- RBAC: `customer.read` scope for GET, `customer.write` scope for POST/PUT/DELETE.
