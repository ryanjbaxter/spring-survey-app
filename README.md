# Spring Cloud Poll Demo - OSS Version

Real-time polling application demonstrating Spring Cloud OSS capabilities for the DevNexus 2025 presentation "Spring Cloud Supercharged for Production-Ready Apps".

## Architecture

```
                                    ┌─────────────────────────────────────────────────┐
                                    │                   Gateway (8080)                │
                                    │  ┌─────────────────────────────────────────┐    │
Browser (Chart.js) ─────────────────┼──│  /poll-service/**    → Poll Service     │────┼──→ RabbitMQ
         ↑ polling                  │  │  /results-service/** → Results Service  │    │       ↓
         │                          │  │  /**                  → poll-ui-js       │    │  (poll-events)
         │                          │  └─────────────────────────────────────────┘    │       ↓
         │                          └─────────────────────────────────────────────────┘  Results Service
         │                                                                                     ↓
         └─────────────────────────────────────────────────────────────────────────────  H2 Database

                     Eureka Server  ←  all services register here
                     Config Server  ←  poll-service, results-service pull config
```

All traffic flows through the Gateway on port 8080. The Gateway routes requests based on path:
- `/poll-service/**` → Poll Service (load balanced)
- `/results-service/**` → Results Service
- `/**` (catch-all) → poll-ui-js static content

## Components

| Service | Port | Description |
|---------|------|-------------|
| **Eureka Server** | 8761 | Service discovery registry |
| **Config Server** | 8888 | Centralized configuration management |
| **Gateway** | 8080 | API Gateway with load balancing (Spring Cloud Gateway) |
| **Poll Service** | 8081 (8082) | Handles poll submissions, publishes events to RabbitMQ |
| **Results Service** | 8083 | Consumes events, aggregates results, serves via REST |
| **Poll UI (JS)** | 8091 | Plain HTML/JS + Chart.js frontend (served via Gateway) |
| **Poll UI (Vaadin)** | 8090 | Vaadin-based frontend (legacy, kept for reference) |

## Spring Cloud Features Demonstrated

- **Service Discovery** - Netflix Eureka
- **Centralized Config** - Spring Cloud Config Server
- **Client-Side Load Balancing** - Spring Cloud LoadBalancer
- **API Gateway** - Spring Cloud Gateway
- **Event-Driven Messaging** - Spring Cloud Stream + RabbitMQ
- **Circuit Breaker** - Resilience4j

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

# Terminal 7 - Poll UI (JS)
cd poll-ui-js
mvn spring-boot:run
```

Or use the startup script (macOS/Linux):

```bash
./start-all.sh
```

### 4. Access the Application

- **Survey Page**: http://localhost:8080 (via Gateway)
- **Live Results**: http://localhost:8080/results (via Gateway)
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672

> **Note:** All user-facing traffic goes through the Gateway on port 8080. The poll-ui-js service (port 8091) is accessed indirectly via the Gateway's catch-all route.

## Event Flow

```
User submits poll answer
    ↓
Browser → Gateway (8080) → /poll-service/submit → Poll Service
    ↓
Poll Service writes to DB + publishes PollSubmittedEvent to RabbitMQ
    ↓
Results Service consumes event from RabbitMQ (poll-events topic)
    ↓
Results Service aggregates and saves to DB
    ↓
Browser polls GET /results-service/{questionId} via Gateway every 2 seconds
    ↓
Chart.js updates horizontal bar charts with new data
```

## Demo Script

### 1. Show Service Discovery

1. Open Eureka Dashboard (http://localhost:8761)
2. Point out all registered services
3. Show multiple instances of poll-service

### 2. Submit Polls & Show Live Results

1. Open the Survey page (http://localhost:8080)
2. Submit a poll response
3. Open the Results page (http://localhost:8080/results) in another tab
4. Watch the bar charts update every 2 seconds via polling
5. Submit more responses — charts update automatically

### 3. Show Load Balancing

1. Check console logs for poll-service on ports 8081 and 8082
2. Submit several polls
3. Show requests alternating between instances (round-robin)

### 4. Show Event Streaming

1. Open RabbitMQ Management UI (http://localhost:15672)
2. Show the `poll-events` exchange
3. Submit a poll
4. Watch message counts increment

### 5. Show Circuit Breaker (Optional)

1. Kill the results-service
2. Submit a poll — poll-service circuit breaker opens
3. Restart results-service — circuit breaker closes
4. Events replay from RabbitMQ, charts update

### 6. Highlight Production Gaps

Point out what's missing for a production deployment (addressed in Demo 2).

## Poll Questions (Configurable)

Defined in `config-server/src/main/resources/config/poll-service.properties`:

1. How many of you are running microservices in production?
2. Who is already using Spring Cloud OSS?
3. What's your biggest pain point today?

To add/modify questions, edit the file and restart poll-service (or use `/actuator/refresh` for zero-downtime updates).

## Frontend Architecture (poll-ui-js)

The active frontend is a plain HTML/JS application served behind the Gateway:

- **Survey page** (`/`) — Radio button forms for each poll question
- **Results page** (`/results`) — Live Chart.js horizontal bar charts
- **Direct API calls** — JavaScript calls backend services directly via Gateway routes (`/poll-service/*`, `/results-service/*`)
- **Polling** — Results page polls each question's results every 2 seconds
- **Chart.js 4** — Loaded via CDN, renders horizontal bar charts with Spring green theme

The poll-ui-js service is a simple static content server. All API routing and load balancing is handled by the Gateway, eliminating the need for a backend proxy layer.

```
poll-ui-js/
└── src/main/
    ├── java/.../uijs/
    │   ├── PollUIJSApplication.java  # Spring Boot app with Eureka discovery
    │   └── WebConfig.java            # View controller for /results
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html            # Survey page (/)
            ├── results.html          # Live results page (/results)
            ├── css/style.css         # Spring-green themed styles
            └── js/
                ├── poll.js           # Survey form logic (calls /poll-service/*)
                └── results.js        # Chart.js polling logic (calls /results-service/*)
```

### Gateway Routes

The Gateway routes all traffic with the following priority:

| Route | Path Pattern | Target | Notes |
|-------|--------------|--------|-------|
| poll-service | `/poll-service/**` | lb://poll-service | StripPrefix=1, load balanced |
| results-service | `/results-service/**` | lb://results-service | StripPrefix=1 |
| poll-ui-js | `/**` | lb://poll-ui-js | order=999 (catch-all) |

## Production Gaps Highlighted

During the demo, point out:

- :x: **No Security** — No authentication, anyone can vote multiple times
- :x: **No Rate Limiting** — Could be spammed
- :x: **Manual Setup** — Eureka, Config Server, RabbitMQ all require ops setup
- :x: **No Centralized Observability** — Logs scattered across services
- :x: **DIY Secrets Management** — Config stored in plain files

These gaps are addressed in Demo 2 with VMware Tanzu Platform.

## Troubleshooting

**Service won't start:**
- Check if the port is already in use
- Ensure Eureka and Config Server started first
- Check RabbitMQ is running

**UI doesn't load questions:**
- Check browser console for errors
- Verify poll-service and gateway are running and registered in Eureka
- Check poll-ui-js logs for proxy errors

**Results not updating:**
- Verify results-service is running
- Check RabbitMQ is running and the `poll-events` exchange exists
- Open browser dev tools Network tab — confirm `/results-service/{id}` requests return data

**Load balancing not working:**
- Ensure both poll-service instances registered in Eureka
- Check gateway logs for route resolution

## Technology Stack

- Spring Boot 4.0.2
- Spring Cloud 2025.1.1
- Spring Cloud Stream (RabbitMQ)
- Chart.js 4 (via CDN)
- H2 Database (in-memory)
- Netflix Eureka
- Resilience4j Circuit Breaker

## License

Apache 2.0

---

Built for DevNexus 2025 by Ryan Baxter (@ryanjbaxter) and Chris Sterling (@csterwa)
