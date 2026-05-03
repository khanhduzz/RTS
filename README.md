# Real-Time Trading System (RTS)

**Technical Proposal & Full System Blueprint**

**Project Focus**: Production-grade simulation/sandbox platform for HOSE, HNX, UPCoM using **Java 21 + Spring Boot**, latest technologies, free tiers only. Designed as a major portfolio project for job applications at big companies (FPT, VNG, banks, fintech).

**Build Timeline**: 2–4 months (modular phases).  
**Status**: Enhanced with Vietnam-specific realism, stronger AI, better free-stack.

---

## 1. System Overview

This is a **cloud-native, event-driven trading simulation ecosystem** for the Vietnam stock market featuring:

- Real-time / near-real-time market data processing
- High-fidelity order matching engine (sandbox)
- AI-assisted decision making & signals
- Portfolio & risk management
- Backtesting & historical replay
- Full observability and resilience

**Key Architecture Patterns**:

- Event-driven + CQRS (partial Event Sourcing)
- Reactive programming (WebFlux + Project Reactor)
- Domain-Driven Design (DDD)
- Cloud-native & Kubernetes-ready

**Vietnam Market Specifics**:

- Exchanges: HOSE, HNX, UPCoM
- Trading hours: Mon–Fri 09:00–11:30 & 13:00–15:00 (ATO/ATC sessions)
- Order types: LO, MP, ATO, ATC, etc.
- T+2 settlement simulation
- VND currency & local symbols

---

## 2. Non-Functional Requirements

### Performance

- Order latency: **< 100ms (p95)**
- Market data latency: **< 100ms**
- Throughput: **10k–50k events/sec** (Kafka)

### Availability & Resilience

- 99.9% uptime (simulation mode)
- Circuit breakers, retries, dead-letter queues
- Graceful degradation

### Scalability

- Horizontal scaling (stateless services)
- Kafka partition scaling
- In-memory order book optimized

### Cost

- **Near-zero** using free tiers and local dev

---

## 3. High-Level Architecture

### Core Services (Java/Spring Boot 3.x)

- **API Gateway** – Spring Cloud Gateway
- **Identity Service** – Keycloak or Spring Security + JWT
- **Market Data Service** – Ingestion & normalization
- **Trading Engine** – Order book & matching
- **Portfolio Service** – Event-sourced
- **Risk Engine** – VaR, exposure limits
- **AI Service** – Signals & recommendations
- **Notification & Analytics Service**
- **Backtesting Service**

### Infrastructure (Free-Friendly)

- **Event Backbone**: Kafka (Redpanda recommended for local)
- **Task Queue**: RabbitMQ (optional)
- **Cache & Rate Limit**: Redis
- **OLTP**: PostgreSQL (Supabase free / TimescaleDB)
- **Read Models / Documents**: MongoDB Atlas (free tier)
- **Object Storage**: MinIO
- **Monitoring**: Prometheus + Grafana + OpenTelemetry + Jaeger

**Deployment**: Docker Compose → Kubernetes (Minikube / free cloud trials)

---

## 4. Data Sources (Vietnam-Focused)

- **Real-time**: SSI FastConnect, TCBS, iTick API (WebSocket/REST), public endpoints
- **Historical**: vnstock logic / CafeF / Vietstock → import to DB
- **News/Sentiment**: RSS + free LLM (Ollama / Gemini free tier)
- **Fallback**: CSV replay simulator with realistic timing

**Implementation**: Adapter pattern for easy source swapping.

---

## 5. Event Design

**Principles**: Immutable, versioned, idempotent.

**Example Event** (Price Tick):

```json
{
  "eventId": "uuid",
  "type": "PriceTick",
  "version": 1,
  "timestamp": "2026-05-02T15:30:00Z",
  "payload": {
    "symbol": "VNM",
    "price": 70000.0,
    "volume": 100,
    "exchange": "HOSE"
  }
}
```

**Kafka Topics**:

- `market.price.tick.{symbol}` (partition by symbol)
- `order.placed, order.matched`, trade.executed
- `portfolio.updated`, `ai.signal`

**Schema**: Avro + Schema Registry (local)

## 6. Trading Engine Design (CV Highlight)

**Order Book Structure:**

- `ConcurrentSkipListMap<Double, Deque<Order>>` (price → orders)
- Buy side (descending), Sell side (ascending)

**Matching Algorithm** (Pseudo-code):

```java
while (!buyOrders.isEmpty() && !sellOrders.isEmpty() && bestBuyPrice >= bestSellPrice) {
    MatchResult result = match(bestBuy, bestSell);
    publish(TradeExecutedEvent.of(result));
    updateRemainingQuantities();
}
```

**Features:**

- Partial fills
- FIFO within price level
- Support for ATO/ATC logic
- In-memory + event persistence

## 7. AI Integration

- **Framework:** Spring AI
- **Models:**
  - Local: Ollama (Llama 3 / Mistral)
  - Cloud free: Google Gemini Flash
- **Capabilities:**
  - Technical indicators (TA4J library)
  - News sentiment analysis
  - Basic price forecasting (time-series)
  - Recommendation engine

## 8. Consistency Model

Trading Engine

- Strong (in-memory)

Portfolio

- Eventual (CQRS)

Analytics

- Eventual

AI Signals

- Best-effort

## 9. Redis Design

- `market:{symbol}` – Latest quote (TTL 5–60s)
- `rate:{userId}` – Rate limiting
- `session:{token}` – Sessions
- `order:lock:{orderId}` – Concurrency control

## 10. Security Architecture

- OAuth2 / JWT + RBAC
- API Gateway filters
- Input validation & sanitization
- Rate limiting (Redis)
- CORS, HTTPS, secret management (Docker Secrets / Kubernetes Secrets)

## 11. Observability

- **Metrics**: Micrometer + Prometheus (`order_latency`, `kafka_lag`, `match_rate`)
- **Logs:** Structured JSON + Correlation ID
- **Tracing:** OpenTelemetry + Jaeger
- **Dashboards:** Grafana
- **Alerts:** Basic rules

## 12. Development Roadmap (Recommended Phases)

**Phase 1 (Weeks 1-3):** Modulith + Domain Model + PostgreSQL + Basic Matching

**Phase 2 (Weeks 4-6):** Kafka integration + Market Data Simulator

**Phase 3 (Weeks 7-8):** Reactive WebSocket UI + Real Data Ingestion

**Phase 4 (Weeks 9-10):** Portfolio, Risk, Backtesting

**Phase 5 (Weeks 11-12):** AI Integration + Observability

**Phase 6:** Microservices extraction + Kubernetes + Load/Chaos Testing

**Tools:**

- Build: Maven
- Testing: JUnit 5 + Testcontainers + Gatling/k6
- CI/CD: GitHub Actions

## 13. Docker Compose (Simplified)

```YAML
services:
    kafka:
        image: redpanda/redpanda
    redis:
        image: redis:alpine
    postgres:
        image: postgres:16
    mongo:
        image: mongo
    trading-engine:
        build: ./trading-engine
    # ... other services
```

## 14. Kubernetes Considerations

- 2–3 replicas per service
- ClusterIP + Ingress
- ConfigMap + Secrets
- Horizontal Pod Autoscaler (HPA)
- Chaos Mesh (optional for resilience testing)

## 15. Failure Handling

- Kafka down → Exponential backoff + DLQ
- Duplicate events → Idempotency keys
- Service crash → Kubernetes restart + event replay
- Data source failure → Fallback to simulator

## 16. Load Testing & Chaos

- Tools: k6, Gatling
- Scenarios: Market open spikes, high-frequency orders
- Chaos: Kill brokers, inject latency

## 17. Cost Optimization (Free Tier Focus)

- Redpanda instead of full Kafka
- Supabase + MongoDB Atlas free tiers
- Local Ollama for AI
- MinIO + GitHub Actions
- Limited retention policies

## 18. Frontend (Recommended)

- React / Next.js + TypeScript
- WebSocket (STOMP or reactive)
- Charts: TradingView Lightweight or Recharts
- Live order book, portfolio dashboard, AI insights panel

## 19. Conclusion & Value

This system demonstrates **advanced backend engineering** skills:

- Distributed event-driven systems
- Real-time processing with Kafka + Reactive Java
- Domain complexity (trading engine)
- AI/ML integration
- Cloud-native & observable architecture
- Vietnam market domain knowledge

Recommended Portfolio Assets:

- Clean GitHub repo with diagrams (C4 model)
- Architecture Decision Records (ADR)
- Live demo (if possible)
- Blog posts / README walkthrough

Next Steps:

1. Domain modeling (Order, Portfolio aggregates)
2. Implement core Trading Engine
3. Set up Kafka + events

End of V4 Blueprint

Version: Enhanced May 2026
