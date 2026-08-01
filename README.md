# Ride-Hailing System

## 1. Executive Summary
This project implements a scalable, distributed ride-hailing platform built on a modernized microservices architecture. Designed to evolve from a minimal functional foundation into a fully event-driven, real-time tracking system, the platform facilitates core ride-sharing capabilities: user ride requests, intelligent driver matching, live geospatial tracking, and secure access management. The project is structured to demonstrate architect-level practices including clear system boundaries, reproducibility, and robust documentation.

## 2. Architecture Overview
The system follows an evolutionary architecture broken down into strategic capability phases:
- **Phase 1:** Core RESTful Foundation (API Gateway, User, Driver, and Trip Services).
- **Phase 2:** Event-Driven Architecture utilizing Apache Kafka to decouple service communication.
- **Phase 3:** High-performance real-time location tracking using Redis GEO.
- **Phase 4:** Core intelligence Matching Engine based on distance, rating, and availability.
- **Phase 5:** Real-time WebSockets for live driver-to-rider communication.
- **Phase 6:** Centralized Identity and Access Management (IAM) through Keycloak.

## 3. Microservices Breakdown
The backend consists of the following isolated services:
- **API Gateway:** Central entry point, handling routing, rate limiting, and JWT validation.
- **User Service:** Manages rider profiles, payment methods, and user history.
- **Driver Service:** Manages driver profiles, vehicle data, and availability status.
- **Trip Service:** The orchestrator for the ride lifecycle (requested, accepted, ongoing, completed).
- **Matching Service:** Consumes ride requests and driver locations to intelligently pair drivers and riders.
- **Location Service:** Consumes live GPS pings from drivers and updates Redis.
- **Notification Service:** Pushes real-time WebSocket updates to the client applications.

## 4. Event-Driven Communication (Kafka)
To ensure loose coupling and high availability, synchronous inter-service calls are replaced with asynchronous events orchestrated by **Apache Kafka**:
- `trip.requested`: Published by Trip Service. Consumed by Matching Service to begin driver search.
- `driver.assigned`: Published by Matching Service. Consumed by Trip Service and Notification Service.
- `location.updated`: High-throughput stream used for analytics and tracking.

## 5. Real-Time Location Tracking (Redis GEO)
Relational databases cannot handle the write-heavy load of high-frequency GPS pinging. 
- **Data Structure:** We use Redis GEO sets (`GEOADD`, `GEOSEARCH`) to store live coordinates.
- **Flow:** Drivers send their location every 3-5 seconds. The Location Service writes this directly to Redis. The Matching Service queries Redis for drivers within a specific radius of the rider's pickup location.

## 6. Authentication (Keycloak)
Security is centralized using **Keycloak**.
- **Roles:** Strict separation between `USER` and `DRIVER` roles.
- **Flow:** Clients authenticate against Keycloak via OAuth2/OpenID Connect. Keycloak issues a JWT.
- **Validation:** The API Gateway validates the JWT signature before routing the request to downstream microservices, ensuring secure perimeter defense.

## 7. API Gateway Design
Built with **Spring Cloud Gateway**, this service provides:
- **Routing Rules:** Path-based routing (e.g., `/api/v1/users/**` -> User Service).
- **Security:** Token Relay and centralized JWT validation.
- **Cross-Cutting Concerns:** CORS configuration, request logging, and global rate limiting (to prevent DDoS/abuse).

## 8. Detailed Workflows
### Ride Request & Driver Matching
1. **User requests a ride:** Client -> API Gateway -> Trip Service (Sync).
2. **Trip initialized:** Trip Service saves `status=PENDING` in DB and publishes to Kafka topic `trip.requested`.
3. **Matching:** Matching Service consumes the event -> Queries Redis GEO for nearby drivers -> Runs matching algorithm (distance/rating).
4. **Assignment:** Matching Service publishes `driver.assigned` to Kafka.
5. **Update & Notify:** Trip Service updates the DB. Notification Service pushes the assignment via WebSocket to the User.

### Tracking Workflow
1. Driver app transmits GPS coordinates every 3-5 seconds.
2. API Gateway routes ping to Location Service.
3. Location Service updates Redis via `GEOADD`.
4. Driver's new location is transmitted to the waiting/riding User via WebSockets.

## 9. Scalability Strategies
- **Stateless Services:** All Spring Boot services are stateless, scaling horizontally via Docker/Kubernetes.
- **Read/Write Segregation:** High-frequency location writes go to Redis, avoiding PostgreSQL locks.
- **Partitions:** Kafka topics (e.g., location updates) are highly partitioned to allow concurrent consumer processing.

## 10. Fault Tolerance Mechanisms
- **Circuit Breakers (Resilience4j):** Inter-service synchronous calls (if any) are protected with fallback methods.
- **Event Sourcing:** Kafka provides replayability. If the Matching service crashes, it resumes reading from its last offset upon restart.
- **Database Connection Pooling:** HikariCP used to aggressively manage DB connections.

## 11. Observability
- **Logging:** Structured JSON logging (Logback).
- **Distributed Tracing:** Micrometer Tracing (with Zipkin or Jaeger) tracks requests as they pass from the Gateway through Kafka and downstream services.
- **Metrics:** Spring Boot Actuator exposes health and metrics, scraped by Prometheus and visualized in Grafana.

## 12. Trade-offs and Design Decisions
- **Eventual Consistency vs. Strong Consistency:** Opted for eventual consistency for non-critical reads (like searching drivers) to ensure high availability and low latency.
- **Redis vs Postgres for Locations:** Chosen Redis over PostGIS for real-time tracking due to the sheer volume of writes (hundreds per second), despite Redis geographic data being ephemeral.
- **API Gateway Pattern:** Increases our network hop count by +1, but centralizes security and rate limiting, drastically simplifying our downstream services.
