#!/bin/bash
set -euo pipefail

echo "🛑 Stopping Ride-Hailing Microservices..."

# Prefer jps (part of the JDK) to find the forked service JVMs, fall back to pgrep.
if command -v jps >/dev/null 2>&1; then
    pids=$(jps -l 2>/dev/null | grep -E "com\.ride\." | awk '{print $1}' || true)
elif command -v pgrep >/dev/null 2>&1; then
    pids=$(pgrep -f "spring-boot:run" || true)
else
    pids=""
fi

if [ -z "$pids" ]; then
    echo "No running Ride-Hailing services found."
    exit 0
fi

for pid in $pids; do
    echo "Stopping process $pid"
    kill "$pid" 2>/dev/null || true
done

echo "✅ All services stopped."
