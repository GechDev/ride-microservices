# Manual Testing Guide: Premium Trip Service

Follow these steps to verify the modern Trip Service features. All requests should be sent through the **API Gateway** (Port `8080`).

## 0. Authentication
Before testing, you need a JWT token from Keycloak.
- **Grant Type:** `password`
- **Client ID:** `ride-hailing-client`
- **Username:** `testuser`
- **Password:** `password`
- **URL:** `http://localhost:7080/realms/ride-hailing-realm/protocol/openid-connect/token`

---

## 1. Get Authentication Token
Run this command in your terminal to get a fresh JWT token from Keycloak:

```bash
curl -X POST "http://localhost:7080/realms/ride-hailing-realm/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password" \
     -d "client_id=ride-hailing-client" \
     -d "username=testuser" \
     -d "password=password" \
     -d "scope=openid"
```
**Copy the `access_token` from the response.**

---

## 2. Create a Premium Trip
Replace `YOUR_TOKEN` with the token you just copied:

```bash
curl -X POST "http://localhost:8083/api/v1/trips" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{
       "userId": 1,
       "pickupLocation": "123 Modern Ave, Tech City",
       "dropoffLocation": "456 Amazing Blvd, Innovation Suburb"
     }'
```

**What to look for:**
- Status should be `REQUESTED`.
- `fare` should be `15.50` USD.
- `distanceKm` and `estimatedDurationMin` should be populated.

---

## 2. Verify Trip Status
**GET** `http://localhost:8080/api/v1/trips/{id}`

**Response Check:**
- Ensure the state matches the Enum `REQUESTED`.
- `updatedAt` should show the latest audit timestamp.

---

## 3. Cancel the Trip (User Scenario)
**POST** `http://localhost:8080/api/v1/trips/{id}/cancel`

**Headers:** No body required.

**Expected Result:**
- Status changes to `CANCELED`.
- `canceledBy` field becomes `USER`.

---

## 4. Complete a Trip (Driver Scenario)
> [!NOTE]
> Usually, a trip must be `ASSIGNED` before it can be completed. For testing purposes, you can check the `COMPLETED` logic.

**POST** `http://localhost:8080/api/v1/trips/{id}/complete`

**Expected Error (if not assigned):**
- You should see a clean error from our `GlobalExceptionHandler`:
```json
{
  "success": false,
  "message": "Only assigned or in-progress trips can be completed.",
  "data": null
}
```

---

## 5. View History (Admin/Audit)
**GET** `http://localhost:8080/api/v1/trips`

**Response:**
- A list of all trips with their full metadata and historical statuses.
