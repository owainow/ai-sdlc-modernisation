# API Contract: User Management Service

**Version**: v1 | **Base Path**: `/api/v1/users`

## Endpoints

### List Users
- **GET** `/api/v1/users?page=0&size=20&sort=username,asc`
- **Response 200**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "username": "string",
        "firstName": "string",
        "lastName": "string",
        "createdAt": "2026-02-23T10:00:00Z",
        "updatedAt": "2026-02-23T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "errors": null
}
```

### Get User by ID
- **GET** `/api/v1/users/{id}`
- **Response 200**: Single user object in `data` field.
- **Response 404**: RFC 7807 Problem Details.

### Create User
- **POST** `/api/v1/users`
- **Request Body**:
```json
{
  "username": "string (3-50 chars, alphanumeric)",
  "firstName": "string (1-100 chars)",
  "lastName": "string (1-100 chars)",
  "password": "string (8+ chars, complexity requirements)"
}
```
- **Response 201**: Created user (without password) in `data` field.
- **Response 400/422**: Validation errors in RFC 7807 format.
- **Response 409**: Username already exists.

### Update User
- **PUT** `/api/v1/users/{id}`
- **Request Body**: Same as Create (excluding password).
- **Response 200**: Updated user in `data` field.
- **Response 404**: User not found.

### Delete User
- **DELETE** `/api/v1/users/{id}`
- **Response 204**: No content.
- **Response 404**: User not found.

## Error Response Format (RFC 7807)

```json
{
  "type": "https://api.example.com/problems/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' was not found.",
  "instance": "/api/v1/users/123e4567-e89b-12d3-a456-426614174000"
}
```

## Authentication
- All endpoints require Azure Entra ID bearer token.
- RBAC: `user.read` scope for GET, `user.write` scope for POST/PUT/DELETE.
