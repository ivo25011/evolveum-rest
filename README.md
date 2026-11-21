# Users and Policies REST API

A small REST application for managing **users** and **policies**.
A policy is a set of conditions. When a user matches all conditions of a policy,
the ID of the policy is stored in the user's `policy` list. The application keeps
this list up to date automatically.

## Contents

1. [Running the application](#1-running-the-application)
2. [Users](#2-users)
3. [Policies](#3-policies)
4. [Tutorial](#4-tutorial)
5. [How the system behaves](#5-how-the-system-behaves)
6. [API reference](#6-api-reference)
7. [Errors](#7-errors)
8. [Configuration](#8-configuration)
9. [Adding a new condition type](#9-adding-a-new-condition-type)
10. [Known limitations](#10-known-limitations)

---

## 1. Running the application

**Requirements:** Java 21.

1. Open the project in your IDE, for example IntelliJ IDEA.
2. Run the class `EvolveumRestApplication`.

The application starts on **http://localhost:8080**.

It uses an **in-memory H2 database**, so all data is lost when the application stops.

---

## 2. Users

A user looks like this:

```json
{
  "name": "jdoe",
  "firstName": "John",
  "lastName": "Doe",
  "emailAddress": "jdoe@evolveum.com",
  "organizationUnit": ["Software Development", "Support"],
  "birthDate": "2010-03-15",
  "registeredOn": "2024-05-07",
  "policy": ["underaged", "internal-user", "developer-full-access"]
}
```

| Field | Description |
|---|---|
| `name` | Unique identifier of the user. **Required.** It cannot be changed later. |
| `firstName`, `lastName` | First and last name. |
| `emailAddress` | E-mail address. Used by the `emailDomainIs` condition. |
| `organizationUnit` | List of organization units. Used by the `isMemberOf` condition. |
| `birthDate` | Date of birth in the format `YYYY-MM-DD`. Used by the `youngerThan` condition. |
| `registeredOn` | Registration date. If you do not send it when creating a user, today's date is used. It does not change when the user is updated. |
| `policy` | IDs of the policies that apply to the user. **Calculated by the system.** If you send this field, it is ignored. |

---

## 3. Policies

A policy has an `id`, a `name` and **one or more conditions**:

```json
{
  "id": "underaged",
  "name": "Underaged User",
  "youngerThan": { "value": 18 }
}
```

Every property other than `id` and `name` is a condition. The format of a condition is always
`"conditionName": { "value": ... }`.

### Available conditions

| Condition | Value | The user matches when... | Example |
|---|---|---|---|
| `youngerThan` | whole number, 0 or more | the user's age in whole years is lower than the value | `"youngerThan": { "value": 18 }` |
| `emailDomainIs` | text | the e-mail address ends with `@` + the value (upper and lower case do not matter) | `"emailDomainIs": { "value": "evolveum.com" }` |
| `isMemberOf` | text | the list `organizationUnit` contains exactly this value | `"isMemberOf": { "value": "Software Development" }` |

A user without a birth date never matches `youngerThan`, and a user without an e-mail address
never matches `emailDomainIs`.

### A policy with more conditions

A policy applies to a user only when **all** of its conditions match:

```json
{
  "id": "young-developer",
  "name": "Young Developer",
  "youngerThan": { "value": 25 },
  "isMemberOf": { "value": "Software Development" }
}
```

This policy applies only to users who are younger than 25 **and** are members of Software Development.

---

## 4. Tutorial

The examples use the IntelliJ HTTP Client format. You can send the same requests with
Postman, curl or any other HTTP client. Always use the header `Content-Type: application/json`
for requests with a body.

### Step 1: Create policies

```http
POST http://localhost:8080/policy
Content-Type: application/json

{ "id": "underaged", "name": "Underaged User", "youngerThan": { "value": 18 } }
```

```http
POST http://localhost:8080/policy
Content-Type: application/json

{ "id": "internal-user", "name": "Internal User", "emailDomainIs": { "value": "evolveum.com" } }
```

```http
POST http://localhost:8080/policy
Content-Type: application/json

{ "id": "developer-full-access", "name": "Developer (Full Access)", "isMemberOf": { "value": "Software Development" } }
```

Each request returns `201 Created` and the created policy.

### Step 2: Create a user

```http
POST http://localhost:8080/users
Content-Type: application/json

{
  "name": "jdoe",
  "firstName": "John",
  "lastName": "Doe",
  "emailAddress": "jdoe@evolveum.com",
  "organizationUnit": ["Software Development", "Support"],
  "birthDate": "2010-03-15",
  "registeredOn": "2024-05-07"
}
```

The response is `201 Created`. The system has already calculated the matching policies,
so the response contains the `policy` field. John is younger than 18, has an `evolveum.com`
e-mail address and works in Software Development, so all three policies apply:

```json
"policy": ["underaged", "internal-user", "developer-full-access"]
```

### Step 3: Update the user

The user leaves Software Development:

```http
PUT http://localhost:8080/users/jdoe
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "emailAddress": "jdoe@evolveum.com",
  "organizationUnit": ["Support"],
  "birthDate": "2010-03-15"
}
```

The policies are calculated again, so `developer-full-access` is removed from the user.

**Note:** `PUT` replaces the user's data. Send all fields you want to keep, because fields
that are missing in the request are cleared. The exceptions are `name` (taken from the URL)
and `registeredOn` (never changes).

### Step 4: Change a policy

From now on, only the domain `example.com` counts as internal:

```http
PUT http://localhost:8080/policy/internal-user
Content-Type: application/json

{ "name": "Internal User", "emailDomainIs": { "value": "example.com" } }
```

All users are calculated again. `jdoe` no longer has `internal-user`.

`PUT` replaces the whole policy: its name and all its conditions. The `id` is taken from the URL.

### Step 5: Delete a policy

```http
DELETE http://localhost:8080/policy/underaged
```

The response is `204 No Content`. The policy is removed from all users.

### Step 6: Read the data

```http
GET http://localhost:8080/users
GET http://localhost:8080/users/jdoe
GET http://localhost:8080/policy
GET http://localhost:8080/policy/internal-user
```

---

## 5. How the system behaves

### When are the policies of users calculated?

| Event | What happens |
|---|---|
| A user is created | Policies are calculated for this user. |
| A user is updated | Policies are calculated for this user again. |
| A policy is created, updated or deleted | Policies are calculated again for **all** users. |
| Every night at 01:00 | Policies are calculated again for all users. |

The nightly job is needed because some conditions depend on time. For example, a user turns 18
without any change of their data, so the `underaged` policy must be removed from them.

### Rules

- A policy applies when **all** of its conditions match.
- A policy must have **at least one condition**.
- Every condition must have a **valid value**, for example `youngerThan` must be a whole number.
- The value must have the correct JSON type: a number for `youngerThan`, text for the other conditions.
  For example, `"emailDomainIs": { "value": 123 }` is rejected.
- An unknown condition name, for example `olderThan`, is rejected.
- A policy can contain each condition type only once.
- The `policy` field of a user cannot be set by the client. It is always calculated.

---

## 6. API reference

### Users

| Method | URL | Description | Success |
|---|---|---|---|
| `GET` | `/users` | List all users | `200 OK` |
| `GET` | `/users/{name}` | Get one user | `200 OK` |
| `POST` | `/users` | Create a user | `201 Created` |
| `PUT` | `/users/{name}` | Update a user | `200 OK` |
| `DELETE` | `/users/{name}` | Delete a user | `204 No Content` |

### Policies

| Method | URL | Description | Success |
|---|---|---|---|
| `GET` | `/policy` | List all policies | `200 OK` |
| `GET` | `/policy/{id}` | Get one policy | `200 OK` |
| `POST` | `/policy` | Create a policy | `201 Created` |
| `PUT` | `/policy/{id}` | Replace a policy | `200 OK` |
| `DELETE` | `/policy/{id}` | Delete a policy | `204 No Content` |

---

## 7. Errors

Errors are returned in the standard *Problem Details* format (RFC 9457):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "User with name 'jdoe' not found.",
  "instance": "/users/jdoe"
}
```

| Status | When |
|---|---|
| `400 Bad Request` | Missing `name` of a user or `id` of a policy, unknown condition, invalid condition value, policy without conditions, invalid JSON |
| `404 Not Found` | The user or policy does not exist |
| `409 Conflict` | A user with the same `name` or a policy with the same `id` already exists, or another request changed the same data at the same time (send the request again) |

---

## 8. Configuration

Settings are in `src/main/resources/application.properties`.

| Property | Value | Description |
|---|---|---|
| `policy.recalculation.cron` | `0 0 1 * * *` | When the nightly recalculation runs (Spring cron format: second, minute, hour, day, month, weekday). Required: the application does not start without it. |

---

## 9. Adding a new condition type

The conditions are implemented with the **strategy pattern**. To add a new condition,
for example `olderThan`:

1. Add a new constant to the enum `ConditionType`, with its JSON name and value type:
   ```java
   OLDER_THAN("olderThan", ValueType.INTEGER)
   ```
2. Create a new class that implements `ConditionEvaluator` and is a Spring bean (`@Component`).
   Return the new type from `type()`, write the matching logic in `matches()` and, if needed,
   extra checks of the value in `validate()`.

No other code needs to change. `PolicyEvaluator` finds the new class automatically,
because it gets all implementations of `ConditionEvaluator` from Spring.

---

## 10. Known limitations

These are conscious decisions to keep the solution simple. They could be improved in a real system:

- When a policy changes, all users are calculated again during the same request.
  With a very large number of users, this would be slow. It could run in the background
  or only for the affected users.
- Two requests that change the same user or policy at the same time are detected with
  optimistic locking (`@Version`) and the second one gets `409 Conflict`. However, the version
  is not part of the API. If a client reads a user and saves it a minute later, the server cannot
  know that the client's data is old. This could be solved with `ETag` and `If-Match` headers.
- A user created exactly while a policy change is being recalculated may get policies based on
  the old version of the policy. The nightly recalculation fixes it. A database lock could
  prevent it completely.
- The lists `organizationUnit` and `policy` are stored as array columns. With a separate table
  (`@ElementCollection`), it would be easier to search users by organization unit or by policy.
- The H2 database is in memory only. A real deployment would use a persistent database.
