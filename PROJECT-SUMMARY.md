# Spring Cloud Poll Demo - OSS Version

## Project Summary

Complete working Spring Cloud microservices demo application built for the DevNexus 2025 presentation "Spring Cloud Supercharged for Production-Ready Apps".

## What's Included

### ✅ 6 Microservices

1. **eureka-server** - Service discovery registry (Netflix Eureka)
2. **config-server** - Centralized configuration management
3. **gateway** - API Gateway with load balancing (Spring Cloud Gateway)
4. **poll-service** - Handles poll submissions, publishes to RabbitMQ Stream
5. **results-service** - Consumes events, aggregates results, streams via SSE
6. **poll-ui** - Vaadin-based frontend with real-time chart updates

### ✅ Spring Cloud Features Demonstrated

- ✓ Service Discovery (Eureka)
- ✓ Centralized Config (Config Server)
- ✓ Client-Side Load Balancing (Spring Cloud LoadBalancer)
- ✓ API Gateway (Spring Cloud Gateway)
- ✓ Event-Driven Messaging (Spring Cloud Stream + RabbitMQ)
- ✓ Circuit Breaker (Resilience4j)
- ✓ Real-time updates (Server-Sent Events)

### ✅ Technologies Used

- Spring Boot 4.0.2
- Spring Cloud 2025.1.1
- Vaadin 24.5.4
- RabbitMQ (via Spring Cloud Stream)
- H2 Database (in-memory)
- Netflix Eureka
- Resilience4j

## File Structure

```
poll-demo-oss/
├── pom.xml                    # Parent POM
├── README.md                  # Full documentation
├── start-all.sh              # Startup script
├── eureka-server/            # Service discovery
├── config-server/            # Configuration management
│   └── src/main/resources/config/
│       ├── poll-service.properties    # Poll questions config
│       └── results-service.properties # Database config
├── gateway/                  # API Gateway
├── poll-service/            # Poll submission service
│   ├── model/               # PollResponse, PollQuestions
│   ├── repository/          # JPA repository
│   ├── service/             # Business logic
│   ├── controller/          # REST endpoints
│   └── event/               # PollSubmittedEvent
├── results-service/         # Results aggregation service
│   ├── model/               # PollResults
│   ├── repository/          # JPA repository
│   ├── service/             # Stream consumer, SSE publisher
│   ├── controller/          # REST + SSE endpoints
│   └── event/               # ResultsUpdatedEvent
└── poll-ui/                 # Vaadin frontend
    ├── model/               # DTOs
    ├── client/              # WebClient to backend
    └── view/                # Vaadin UI components

```

## Quick Start

```bash
# 1. Start RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Build all services
mvn clean install

# 3. Use the startup script (macOS/Linux with gnome-terminal)
./start-all.sh

# OR start services manually (see README.md for order)
```

## Access Points

- **Poll UI**: http://localhost:8090
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **API Gateway**: http://localhost:8080

## Demo Flow

1. **Show Service Discovery** - Open Eureka dashboard, show all registered services
2. **Submit Polls** - Open UI, submit answers, watch charts update in real-time
3. **Show Load Balancing** - Check logs, see requests distributed across poll-service instances
4. **Show Event Streaming** - Open RabbitMQ management, watch messages flow
5. **Show Circuit Breaker** - Kill results-service, show graceful degradation
6. **Highlight Production Gaps** - No auth, no rate limiting, manual setup required

## Poll Questions (Configurable)

Defined in `config-server/src/main/resources/config/poll-service.properties`:

1. How many of you are running microservices in production?
2. Who is already using Spring Cloud OSS?
3. What's your biggest pain point today?

## Event Flow

```
User submits poll
    ↓
Vaadin UI → Gateway → Poll Service
    ↓
Poll Service writes to DB
    ↓
Poll Service publishes PollSubmittedEvent to RabbitMQ
    ↓
Results Service consumes event
    ↓
Results Service aggregates stats
    ↓
Results Service publishes ResultsUpdatedEvent
    ↓
Vaadin UI receives SSE update → Chart updates in real-time
```

## Production Gaps (To Highlight in Demo)

- ❌ No authentication/authorization
- ❌ No rate limiting
- ❌ Manual infrastructure setup (Eureka, Config Server, RabbitMQ)
- ❌ No centralized secrets management
- ❌ No unified observability dashboard
- ❌ DIY circuit breaker configuration

These gaps are addressed in **Demo 2** with VMware Tanzu Platform.

## Next Steps

For Demo 2, you would:
1. Deploy these same services to Tanzu Platform
2. Enable OAuth2/JWT via policy
3. Add rate limiting via configuration
4. Show Tanzu Observability dashboard
5. Demonstrate managed RabbitMQ and Data Flow visual pipeline

## License

Apache 2.0

---

Built for DevNexus 2025 by Ryan Baxter (@ryanjbaxter) and Chris Sterling (@csterwa)
