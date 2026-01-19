# HTTP API Design Documentation

---

## Overview

This document defines the HTTP API contract for the **NotebookApp** backend.  
It specifies REST-style endpoints, request/response formats, required headers, parameters, and expected HTTP status codes.  
This document focuses on API design only and does not describe implementation details.

---

## Scope

The API supports the following core domains:

- User registration and authentication
- Note management (basic CRUD operations)

---

## General API Assumptions

- REST-style API design
- Stateless HTTP communication
- JSON used for request and response bodies
- UTF-8 encoding
- Base URL: `http://localhost:8080`

---

## User Endpoints

### Register User

**Method:** POST  
**Path:** `/users/register`

**Description:**  
Creates a new user account.

**Required Headers:**
- `Content-Type: application/json`

**Request Body (JSON):**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 201 Created | User successfully created |
| 400 Bad Request | Missing or invalid request fields |
| 409 Conflict | Email already exists |
| 415 Unsupported Media Type | Content-Type is not application/json |
| 500 Internal Server Error | Unexpected server error |

---

### Authenticate User (Login)

**Method:** POST  
**Path:** `/users/login`

**Description:**  
Authenticates an existing user using email and password.

**Required Headers:**
- `Content-Type: application/json`

**Request Body (JSON):**
```json
{
  "email": "string",
  "password": "string"
}
```

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 200 OK | Authentication successful |
| 400 Bad Request | Missing request fields |
| 401 Unauthorized | Invalid credentials |
| 415 Unsupported Media Type | Content-Type is not application/json |
| 500 Internal Server Error | Unexpected server error |

---

## Note Endpoints

### Create Note

**Method:** POST  
**Path:** `/notes`

**Description:**  
Creates a new note for the authenticated user.

**Required Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer <token>`

**Request Body (JSON):**
```json
{
  "title": "string",
  "content": "string"
}
```

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 201 Created | Note successfully created |
| 400 Bad Request | Missing or invalid request fields |
| 401 Unauthorized | Authentication required |
| 415 Unsupported Media Type | Content-Type is not application/json |
| 500 Internal Server Error | Unexpected server error |

---

### Get All Notes

**Method:** GET  
**Path:** `/notes`

**Description:**  
Retrieves all notes belonging to the authenticated user.

**Required Headers:**
- `Authorization: Bearer <token>`

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 200 OK | Notes retrieved successfully |
| 401 Unauthorized | Authentication required |
| 500 Internal Server Error | Unexpected server error |

---

### Get Single Note

**Method:** GET  
**Path:** `/notes/{id}`

**Description:**  
Retrieves a single note by its identifier.

**Path Parameters:**
- `id` — note identifier

**Required Headers:**
- `Authorization: Bearer <token>`

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 200 OK | Note retrieved successfully |
| 404 Not Found | Note does not exist |
| 401 Unauthorized | Authentication required |
| 500 Internal Server Error | Unexpected server error |

---

### Delete Note

**Method:** DELETE  
**Path:** `/notes/{id}`

**Description:**  
Deletes a note by its identifier.

**Path Parameters:**
- `id` — note identifier

**Required Headers:**
- `Authorization: Bearer <token>`

**Responses:**

| Status Code | Meaning |
|------------|--------|
| 204 No Content | Note successfully deleted |
| 404 Not Found | Note does not exist |
| 401 Unauthorized | Authentication required |
| 500 Internal Server Error | Unexpected server error |
