# Spring Cloud Poll Demo - OSS Version

Real-time polling application demonstrating Spring Cloud OSS capabilities for the DevNexus 2025 presentation "Spring Cloud Supercharged for Production-Ready Apps".

## Architecture

```
Vaadin UI → Gateway → Poll Service → RabbitMQ → Results Service
                ↓                        ↓              ↓
            Eureka Server            (poll-events)  (results-events)
                ↓                                       ↓
            Config Server                          SSE to UI
```

## Components

- **Eureka Server** (8761) - Service discovery registry
- **Config Server** (8888) - Centralized configuration
- **Gateway** (8080) - API Gateway with load balancing
- **Poll Service** (8081, 8082) - Handles poll submissions, publishes events
- **Results Service** (8083) - Aggregates results, streams updates via SSE
- **Poll UI** (8090) - Vaadin-based web interface

## Prerequisites

- Java 21+
- Maven 3.8+
- RabbitMQ (Docker recommended)

## Quick Start

### 1. Start RabbitMQ

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

RabbitMQ Management UI: http://localhost:15672 (guest/guest)

### 2. Build All Services

```bash
mvn clean install
```

### 3. Start Services (in this order)

```bash
# Terminal 1 - Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2 - Config Server
cd config-server
mvn spring-boot:run

# Terminal 3 - Gateway
cd gateway
mvn spring-boot:run

# Terminal 4 - Poll Service Instance 1
cd poll-service
mvn spring-boot:run

# Terminal 5 - Poll Service Instance 2 (for load balancing demo)
cd poll-service
SERVER_PORT=8082 mvn spring-boot:run

# Terminal 6 - Results Service
cd results-service
mvn spring-boot:run

# Terminal 7 - Poll UI
cd poll-ui
mvn spring-boot:run
```

### 4. Access the Application

- **Poll UI**: http://localhost:8090
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672

## Demo Script

### Show Service Discovery

1. Open Eureka Dashboard (http://localhost:8761)
2. Point out all registered services
3. Show multiple instances of poll-service

### Submit Polls & Show Real-Time Updates

1. Open Poll UI (http://localhost:8090)
2. Submit a poll response
3. Watch the results chart update in real-time (< 1 second)
4. Open a second browser tab - both update simultaneously

### Show Load Balancing

1. Check console logs for poll-service on ports 8081 and 8082
2. Submit several polls
3. Show requests alternating between instances (round-robin)

### Show Circuit Breaker (Optional)

1. Kill the results-service
2. Submit a poll - poll-service circuit breaker opens
3. Restart results-service - circuit breaker closes
4. Events replay from RabbitMQ, charts update

### Show Event Streaming

1. Open RabbitMQ Management UI (http://localhost:15672)
2. Show the `poll-events` and `results-events` exchanges
3. Submit a poll
4. Watch message counts increment

## Configuration

Poll questions are configured in `config-server/src/main/resources/config/poll-service.properties`.

To add/modify questions, edit the file and restart poll-service (or use `/actuator/refresh` for zero-downtime updates).

## Production Gaps Highlighted

During the demo, point out:

- ❌ **No Security** - No authentication, anyone can vote multiple times
- ❌ **No Rate Limiting** - Could be spammed
- ❌ **Manual Setup** - Eureka, Config Server, RabbitMQ all require ops setup
- ❌ **No Centralized Observability** - Logs scattered across services
- ❌ **DIY Secrets Management** - Config stored in plain files

These gaps are addressed in Demo 2 with VMware Tanzu Platform.

## Troubleshooting

**Service won't start:**
- Check if the port is already in use
- Ensure Eureka and Config Server started first
- Check RabbitMQ is running

**UI doesn't update:**
- Check browser console for errors
- Verify results-service is running
- Check SSE connection in browser Network tab

**Load balancing not working:**
- Ensure both poll-service instances registered in Eureka
- Check gateway logs for route resolution

## Technology Stack

- Spring Boot 4.0.2
- Spring Cloud 2025.1.1
- Spring Cloud Stream (RabbitMQ)
- Vaadin 24.5.4
- H2 Database (in-memory)
- Netflix Eureka
- Resilience4j Circuit Breaker

## License

Apache 2.0
