#!/bin/bash

# Configuration
SERVICES=("api-gateway" "user-service" "driver-service" "trip-service")
LOG_DIR="logs"

# Create logs directory if it doesn't exist
mkdir -p $LOG_DIR

# Create logs directory if it doesn't exist
mkdir -p $LOG_DIR

echo "🚀 Starting Ride-Hailing Microservices..."

wait_for_port() {
    local port=$1
    echo "Waiting for port $port to be ready..."
    while ! nc -z localhost $port; do
      sleep 2
    done
    echo "Port $port is UP!"
}

# 0. Discovery Server (8761)
echo "Starting discovery-server..."
cd discovery-server
nohup ./mvnw spring-boot:run > "../$LOG_DIR/discovery-server.log" 2>&1 &
cd ..
wait_for_port 8761

# 1. API Gateway (8080)
echo "Starting api-gateway..."
cd api-gateway
nohup ./mvnw spring-boot:run > "../$LOG_DIR/api-gateway.log" 2>&1 &
cd ..
wait_for_port 8080

# 2. User Service (8081)
echo "Starting user-service..."
cd user-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/user-service.log" 2>&1 &
cd ..
wait_for_port 8081

# 3. Driver Service (8082)
echo "Starting driver-service..."
cd driver-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/driver-service.log" 2>&1 &
cd ..
wait_for_port 8082

# 4. Trip Service (8083)
echo "Starting trip-service..."
cd trip-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/trip-service.log" 2>&1 &
cd ..
wait_for_port 8083

# 5. Location Service (8084)
echo "Starting location-service..."
cd location-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/location-service.log" 2>&1 &
cd ..
wait_for_port 8084

# 6. Matching Service (8085)
echo "Starting matching-service..."
cd matching-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/matching-service.log" 2>&1 &
cd ..
wait_for_port 8085

# 7. Notification Service (8086)
echo "Starting notification-service..."
cd notification-service
nohup ./mvnw spring-boot:run > "../$LOG_DIR/notification-service.log" 2>&1 &
cd ..
wait_for_port 8086

echo "✅ All services are UP and running."
echo "📜 You can follow the logs in the '$LOG_DIR/' directory."
echo "💡 Use 'pkill -f spring-boot' to stop all services later."
