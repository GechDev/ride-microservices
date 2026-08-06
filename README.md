# Ride-Hailing System

A scalable, distributed ride-hailing platform built with Spring Boot microservices. The platform covers the full ride-sharing lifecycle: ride requests, intelligent driver matching, real-time geospatial tracking, event-driven coordination via Apache Kafka, and centralized identity and access management via Keycloak.

## 1. Architecture Overview

The system follows an evolutionary architecture split into capability phases:

- **Phase 1:** Core RESTful Foundation (API Gateway, User, Driver, and Trip Services).
- **Phase 2:** Event-Driven Architecture using Apache Kafka to decouple service communication.
- **Phase 3:** High-performance real-time location tracking using Redis GEO.
- **Phase 4:** Core intelligence Matching Engine based on distance, rating, and availability.
- **Phase 5:** Real-time WebSockets for live driver-to-rider communication.
- **Phase 6:** Centralized Identity and Access Management (IAM) through Keycloak.

### Services Overview

| Service               | Port | Responsibility                                                       |
|-----------------------|------|----------------------------------------------------------------------|
| discovery-server      | 8761 | Service registry (Eureka) for dynamic routing                        |
| api-gateway           | 8080 | Entry point: routing, JWT validation, rate limiting, logging         |
| user-service          | 8081 | Rider profiles, registration, login (Keycloak), admin endpoints      |
| driver-service        | 8082 | Driver profiles, vehicle data, availability status                   |
| trip-service          | 8083 | Ride lifecycle orchestrator (requested → assigned → completed)       |
| location-service      | 8084 | Live GPS pings stored in Redis GEO, nearby driver queries            |
| matching-service      | 8085 | Consumes ride requests and pairs drivers with riders                 |
| notification-service  | 8086 | Pushes real-time WebSocket updates to clients                        |

## 2. Event-Driven Communication (Kafka)

Synchronous inter-service calls are replaced with asynchronous events orchestrated by Apache Kafka:

- `trip.requested` — Published by Trip Service. Consumed by Matching Service to begin driver search.
- `driver.assigned` — Published by Matching Service. Consumed by Trip Service and Notification Service.
- `trip.canceled` — Published by Trip Service to release drivers on cancellation.
- `location.updated` — High-throughput stream used for analytics and tracking.

## 3. Real-Time Location Tracking (Redis GEO)

- **Data Structure:** Redis GEO sets (`GEOADD`, `GEOSEARCH`) store live driver coordinates.
- **Flow:** Drivers send their location every 3-5 seconds. The Location Service writes directly to Redis. The Matching Service queries Redis for drivers within a radius of the rider's pickup location.

## 4. Authentication (Keycloak)

- **Roles:** Strict separation between `USER` and `DRIVER` roles.
- **Flow:** Clients authenticate against Keycloak via OAuth2/OpenID Connect. Keycloak issues a JWT.
- **Validation:** The API Gateway validates the JWT signature before routing requests downstream, providing centralized perimeter defense.

## 5. Getting Started

### Prerequisites

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL (5433), Redis (6380), Kafka/Zookeeper (9092), Keycloak (7080), and Jaeger (16686).

### 2. Start Services

Start the discovery server first, then the remaining services:

```bash
./start-services.sh
```

or run each service individually with `mvn spring-boot:run` from its directory.

### 3. Stop Services

```bash
./stop-services.sh
```

### 4. Test the End-to-End Flow

```bash
./test-flow.sh
```

The script authenticates against Keycloak, registers a user and driver, requests a trip, and verifies nearby-driver matching.

## 6. Project Structure

```
ride-microservices/
├── api-gateway/           # Spring Cloud Gateway (routing, security, filters)
├── discovery-server/      # Eureka service registry
├── user-service/          # User & auth domain
├── driver-service/        # Driver & vehicle domain
├── trip-service/          # Trip lifecycle orchestrator
├── location-service/      # Redis GEO location tracking
├── matching-service/      # Driver-rider matching engine
├── notification-service/  # WebSocket notifications
├── docker-compose.yml     # Local infrastructure
└── realm-import.json      # Keycloak realm definition
```

## 7. Observability

- **Logging:** Structured logging via Logback.
- **Distributed Tracing:** Micrometer Tracing with OTLP export to Jaeger (`http://localhost:16686`).
- **Metrics:** Spring Boot Actuator exposes health and metrics for Prometheus/Grafana scraping.

## 8. Detailed Workflows

### Ride Request & Driver Matching

1. **User requests a ride:** Client → API Gateway → Trip Service.
2. **Trip initialized:** Trip Service saves `status=PENDING` and publishes `trip.requested` to Kafka.
3. **Matching:** Matching Service consumes the event → queries Redis GEO for nearby drivers → runs the matching algorithm (distance/rating).
4. **Assignment:** Matching Service publishes `driver.assigned` to Kafka.
5. **Update & Notify:** Trip Service updates the DB. Notification Service pushes the assignment via WebSocket.

### Tracking Workflow

1. Driver app transmits GPS coordinates every 3-5 seconds.
2. API Gateway routes the ping to Location Service.
3. Location Service updates Redis via `GEOADD`.
4. The driver's new location is relayed to the waiting/riding user via WebSockets.

## 9. Trade-offs and Design Decisions

- **Eventual vs. Strong Consistency:** Eventual consistency for non-critical reads (e.g., searching drivers) to ensure high availability and low latency.
- **Redis vs. Postgres for Locations:** Redis chosen over PostGIS for real-time tracking due to write volume, despite geographic data being ephemeral.
- **API Gateway Pattern:** Adds one network hop but centralizes security and rate limiting, simplifying downstream services.

## 10. Deployment & Environment Configuration

### Environment Variables

Configuration is externalized so each service can be tuned per environment without code changes:

| Variable | Default | Scope |
|----------|---------|-------|
| `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | local PostgreSQL | user, driver, trip services |
| `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` | `127.0.0.1:6380` | location-service |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `127.0.0.1:9092` | all event-driven services |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | local Keycloak realm | resource servers |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://localhost:8761/eureka/` | all registered services |
| `RIDE_FARE_*` | see `trip-service` config | fare engine tuning |
| `RIDE_LOCATION_GEO_KEY` / `RIDE_LOCATION_DEFAULT_SEARCH_RADIUS_KM` | `driver_locations` / `5.0` | location queries |

### Startup Order

Infrastructure containers report readiness via Docker Compose health checks (`pg_isready`, `redis-cli ping`, Kafka topic listing), and dependent services only start once dependencies are healthy. Application services should be started in order:

1. `discovery-server`
2. business services (`user`, `driver`, `trip`, `location`, `matching`, `notification`)
3. `api-gateway`

### Graceful Shutdown

Every service enables graceful shutdown (`server.shutdown: graceful`) with a 20s lifecycle timeout so in-flight requests drain and Kafka consumer offsets commit cleanly before the process exits.

### Production Considerations

- Build reproducible artifacts with `mvn clean package` and deploy as versioned containers.
- Replace the embedded H2/default datasource with managed PostgreSQL and run Flyway-style schema migrations instead of `ddl-auto: update`.
- Terminate TLS at the API Gateway and restrict downstream traffic to the internal network.
- Scale stateless services horizontally behind the gateway; the discovery server and Redis GEO act as the coordination layer.
