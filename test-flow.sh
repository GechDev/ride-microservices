#!/bin/bash

GATEWAY_URL="http://localhost:8080/api/v1"

echo "🧪 Starting Step-by-Step Flow Test..."
echo "------------------------------------"

# 0. Helper: Fetch Access Token from Keycloak
get_token() {
  local USERNAME=$1
  local PASSWORD=$2
  curl -s -X POST "http://localhost:7080/realms/ride-hailing-realm/protocol/openid-connect/token" \
    -d "client_id=ride-hailing-gateway" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD" \
    -d "grant_type=password" | jq -r '.access_token'
}

echo "Step 0: Authenticating as 'testuser'..."
TOKEN=$(get_token "testuser" "password")
if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Failed to get token! Ensure Keycloak is running and realm is imported."
  exit 1
fi
echo "✅ Token acquired."
echo ""

# 1. Create a User (Registration - Public)
echo "Step 1: Creating a User (Registration)..."
USER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/users" \
  -H "Content-Type: application/json" \
  -d '{"name": "Secure User", "email": "secure@test.com"}')
echo "Response: $USER_RESPONSE"
echo ""

# 2. Register a Driver (Registration - Public)
echo "Step 2: Registering an AVAILABLE Driver..."
DRIVER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/drivers" \
  -H "Content-Type: application/json" \
  -d '{"name": "Secure Driver", "licensePlate": "SEC-007", "status": "AVAILABLE"}')
echo "Response: $DRIVER_RESPONSE"
echo ""

# 3. Request a Trip (SECURE)
echo "Step 3: Requesting a Trip (Authenticated)..."
TRIP_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/trips" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "pickupLocation": "Point A", "dropoffLocation": "Point B"}')
echo "Response: $TRIP_RESPONSE"
echo ""


# 4. Update Location
echo "Step 4: Updating Driver Location (Redis GEO)..."
LOCATION_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/locations" \
  -H "Content-Type: application/json" \
  -d '{"driverId": 4, "latitude": 37.7749, "longitude": -122.4194}')
echo "Response: $LOCATION_RESPONSE"
echo ""

# 5. Query Nearby
echo "Step 5: Querying Nearby Drivers..."
NEARBY_RESPONSE=$(curl -s -X GET "$GATEWAY_URL/locations/nearby?lat=37.7749&lon=-122.4194&radius=10")
echo "Response: $NEARBY_RESPONSE"
echo ""


echo "------------------------------------"
echo "✅ Test Flow Complete! Check if 'driverId' is assigned in the Trip response."
