#!/bin/bash

# Configuration
BASE_URL="http://localhost:8081/api/v1/users"

echo "1. Registering a Driver..."
curl -s -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Super Driver",
       "email": "driver@amazing.com",
       "password": "password123",
       "phoneNumber": "+123456789",
       "role": "DRIVER"
     }' | jq .

echo -e "\n2. Registering a User..."
curl -s -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Happy Rider",
       "email": "rider@amazing.com",
       "password": "password123",
       "phoneNumber": "+987654321",
       "role": "USER"
     }' | jq .

echo -e "\n3. Testing Duplicate Registration (Expect 409 Conflict)..."
curl -s -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Duplicate User",
       "email": "rider@amazing.com",
       "password": "password123",
       "phoneNumber": "+000000000",
       "role": "USER"
     }' | jq .

echo -e "\n4. Logging in..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "email": "rider@amazing.com",
       "password": "password123"
     }')

echo $LOGIN_RES | jq .

TOKEN=$(echo $LOGIN_RES | jq -r '.data.access_token')

echo -e "\n5. Getting Profile..."
curl -s -X GET "$BASE_URL/me" \
     -H "Authorization: Bearer $TOKEN" | jq .
