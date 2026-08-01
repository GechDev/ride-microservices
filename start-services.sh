#!/bin/bash
set -euo pipefail

# Configuration
SERVICES=("discovery-server" "api-gateway" "user-service" "driver-service" "trip-service" "location-service" "matching-service" "notification-service")
LOG_DIR="logs"
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create the logs directory once
mkdir -p "$LOG_DIR"

echo "🚀 Starting Ride-Hailing Microservices..."

port_of() {
    case "$1" in
        discovery-server)   echo 8761 ;;
        api-gateway)        echo 8080 ;;
        user-service)       echo 8081 ;;
        driver-service)     echo 8082 ;;
        trip-service)       echo 8083 ;;
        location-service)   echo 8084 ;;
        matching-service)   echo 8085 ;;
        notification-service) echo 8086 ;;
        *)                  echo "" ;;
    esac
}

wait_for_port() {
    local port=$1
    local name=$2
    echo "Waiting for $name (port $port) to be ready..."
    local attempts=0
    while ! nc -z localhost "$port" 2>/dev/null; do
        attempts=$((attempts + 1))
        if [ "$attempts" -ge 60 ]; then
            echo "❌ $name did not start within 120 seconds. Check $LOG_DIR/$name.log"
            exit 1
        fi
        sleep 2
    done
    echo "✅ $name is UP on port $port."
}

for service in "${SERVICES[@]}"; do
    port=$(port_of "$service")
    echo "Starting $service..."
    (cd "$BASE_DIR/$service" && nohup mvn spring-boot:run > "$BASE_DIR/$LOG_DIR/$service.log" 2>&1 &)
    wait_for_port "$port" "$service"
done

echo "✅ All services are UP and running."
echo "📜 Logs are written to the '$LOG_DIR/' directory."
echo "💡 Use './stop-services.sh' to stop all services."
