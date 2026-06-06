# Roadmap: Dutch Essay Helper

> A local, production-minded LLM writing feedback service for Dutch B2 essay practice.  
> Built with Java/Spring Boot, Ollama, PostgreSQL, Prometheus/Grafana, and k6.  
> The goal is not only to build a working demo, but to understand how to own, observe, test, and improve an LLM-backed service end to end.

---

## 1. Product Goal

Dutch Essay Helper helps a user submit a Dutch B2 writing exercise and receive structured feedback, including:

- Estimated writing level
- Top writing issues
- Sentence-level correction
- B2-level rewrite
- Reusable sentence patterns
- Targeted follow-up exercises

The system runs locally via Docker Compose and is designed to be:

- Runnable locally
- Observable via metrics and logs
- Testable with unit/integration tests
- Load-testable with k6
- Failure-aware through timeouts, fallbacks, runbooks, and incident drills

---

## 2. Why This Project Exists

This repository is a learning and portfolio project for system design and end-to-end service ownership.

The main learning goals are:

- Build a real backend service, not only an LLM script
- Understand LLM application architecture
- Practice Spring Boot service design
- Learn observability: logs, metrics, dashboards, health checks
- Practice reliability engineering: timeout, retry, fallback, incident response
- Understand trade-offs around synchronous vs asynchronous workflows
- Learn how to explain architecture decisions clearly

This project deliberately avoids cloud deployment in the first stage. The system should be fully runnable on a local machine.

---

## 3. Target Architecture

```text
User / curl / Browser
        |
        v
Spring Boot API Service
        |
        |-- validates essay request
        |-- builds prompt
        |-- calls local Ollama model
        |-- parses feedback
        |-- persists essay, feedback, and LLM call log
        |-- emits metrics and structured logs
        |
        +-------------> Ollama local LLM
        |
        +-------------> PostgreSQL
        |
        +-------------> Redis optional, later phases

Observability:

Spring Boot Actuator + Micrometer
        |
        v
Prometheus
        |
        v
Grafana

Load testing:

k6 -> Spring Boot API
```

---

## 4. Recommended Tech Stack

| Area | Technology | Purpose |
|---|---|---|
| Language | Java 21 | Main backend language |
| Framework | Spring Boot 3 | API, configuration, dependency injection |
| LLM integration | Spring AI / custom Ollama client | Local LLM call abstraction |
| Local model runtime | Ollama | Free local model serving |
| Database | PostgreSQL | Essays, feedback, request logs |
| Migration | Flyway | Repeatable schema evolution |
| Observability | Spring Boot Actuator + Micrometer | Health and metrics |
| Metrics backend | Prometheus | Metric collection |
| Dashboard | Grafana | Local service dashboard |
| Load testing | k6 | Smoke, load, stress, spike tests |
| Testing | JUnit 5 + Testcontainers | Unit and integration tests |
| Container orchestration | Docker Compose | Local multi-service setup |
| Documentation | Markdown ADRs, runbooks, postmortems | Engineering communication |

---

## 5. Roadmap Overview

| Phase | Duration | Main Outcome |
|---|---:|---|
| Phase 0 | Day 1 | A local demo that sends an essay to Ollama and returns feedback |
| Phase 1 | 1 week | Clean Spring Boot service with basic API, persistence, and Docker Compose |
| Phase 2 | 1 week | Structured feedback model, prompt templates, DB schema, and tests |
| Phase 3 | 1 week | Observability: metrics, logs, dashboards, health checks |
| Phase 4 | 1 week | Reliability: timeout, retry, fallback, incident drill, runbook |
| Phase 5 | 1 week | Load testing, performance analysis, quality/eval loop |
| Phase 6 | 1 week | Portfolio polish: docs, ADRs, final demo script, architecture explanation |

Suggested target: **6 weeks, around 80–100 focused hours**.

---

# Phase 0: Day-One Local Demo

## Goal

Run the smallest possible version of the system locally.

The demo should prove:

```text
POST essay text -> call Ollama -> return Dutch writing feedback
```

## Deliverables

- [ ] Basic Spring Boot app
- [ ] `POST /api/essays/correct`
- [ ] Local Ollama call
- [ ] Simple Markdown feedback response
- [ ] Basic `docker-compose.yml` with app + Ollama, or app local + Ollama local
- [ ] One sample essay in `examples/essay_001.txt`
- [ ] One sample curl command in README

## Minimal API

```http
POST /api/essays/correct
Content-Type: application/json

{
  "taskType": "formal_email",
  "prompt": "Write an email to the management about workspace satisfaction.",
  "essay": "Beste directie, ..."
}
```

## Minimal Response

```json
{
  "feedback": "## Estimated Level\nB1+\n\n## Top Issues\n..."
}
```

## Concepts to Understand

- How Spring Boot exposes an HTTP endpoint
- How a backend service calls a local LLM
- What an LLM prompt actually contains
- Why local model output can be unstable
- Why a working vertical slice beats over-designed architecture

## Definition of Done

- [ ] `curl` can submit an essay
- [ ] The API returns meaningful feedback
- [ ] The README explains how to run the demo
- [ ] The code is small enough to refactor later

---

# Phase 1: Clean Service Foundation

## Goal

Turn the demo into a maintainable backend service.

## Deliverables

- [ ] Standard package structure
- [ ] Controller / service / client separation
- [ ] DTO validation
- [ ] Centralized error handling
- [ ] Configurable model name and Ollama base URL
- [ ] PostgreSQL added to Docker Compose
- [ ] Flyway migration added
- [ ] Essay and feedback persisted

## Suggested Package Structure

```text
src/main/java/com/jasonqiu/dutchessayhelper/
  DutchEssayHelperApplication.java

  api/
    EssayCorrectionController.java
    dto/
      CorrectionRequest.java
      CorrectionResponse.java
      ErrorResponse.java

  application/
    EssayCorrectionService.java
    PromptTemplateService.java
    FeedbackPersistenceService.java

  domain/
    Essay.java
    Feedback.java
    LlmRequestLog.java
    CorrectionStatus.java
    ErrorType.java

  infrastructure/
    llm/
      LlmClient.java
      OllamaLlmClient.java
    persistence/
      EssayRepository.java
      FeedbackRepository.java
      LlmRequestLogRepository.java
    config/
      LlmProperties.java
```

## Database Tables

```text
essays
- id
- task_type
- prompt
- original_text
- created_at

feedbacks
- id
- essay_id
- estimated_level
- feedback_markdown
- created_at

llm_request_logs
- id
- essay_id
- model
- prompt_tokens_estimate
- completion_tokens_estimate
- latency_ms
- status
- error_message
- created_at
```

## Concepts to Understand

- Why controller/service/client separation matters
- Transaction boundaries
- Data integrity and auditability
- Why LLM calls should be hidden behind an interface
- Why configuration should not be hardcoded

## Definition of Done

- [ ] App starts with `docker compose up`
- [ ] PostgreSQL schema is created automatically
- [ ] Essay and feedback are saved
- [ ] LLM call logs are saved
- [ ] Invalid requests return clear 4xx errors
- [ ] Ollama failures return clear 5xx errors

---

# Phase 2: Structured Feedback and Prompt Design

## Goal

Move from free-form LLM output to stable, parseable feedback.

## Deliverables

- [ ] Prompt templates in `src/main/resources/prompts/`
- [ ] Structured feedback JSON model
- [ ] Markdown renderer from structured feedback
- [ ] Error taxonomy
- [ ] Unit tests for prompt rendering
- [ ] Integration test for correction workflow

## Feedback Model

```json
{
  "estimatedLevel": "B1+",
  "examRisk": "medium",
  "summary": "The meaning is clear, but the sentences are somewhat wordy, and verb position and preposition usage are inconsistent.",
  "scores": {
    "taskCompletion": 3,
    "coherence": 3,
    "vocabulary": 3,
    "grammar": 2,
    "register": 3,
    "conciseness": 2
  },
  "topIssues": [
    {
      "type": "verb_position",
      "severity": "high",
      "explanation": "Verb placement is inconsistent in subordinate clauses and inverted sentence structures."
    }
  ],
  "sentenceCorrections": [
    {
      "original": "In de laatste tijd hebben we een onderzoek uitgevoerd.",
      "corrected": "Onlangs hebben we een onderzoek uitgevoerd.",
      "explanation": "'In de laatste tijd' sounds unnatural here; 'onlangs' is more suitable for formal writing.",
      "errorType": "collocation"
    }
  ],
  "b2Rewrite": "...",
  "exercises": [
    {
      "type": "rewrite",
      "instruction": "Rewrite the sentence below to make it more concise.",
      "question": "..."
    }
  ]
}
```

## Error Taxonomy

```text
verb_position
word_order
preposition
article
noun_gender
collocation
register
sentence_too_long
coherence
task_completion
vocabulary_choice
spelling
punctuation
```

## Concepts to Understand

- Prompt engineering as software design
- Why output schema matters
- Why stable interfaces matter for downstream processing
- Why LLM output needs validation
- Why product-specific taxonomy improves learning value

## Definition of Done

- [ ] Feedback is returned in a stable JSON-compatible structure
- [ ] Feedback can be rendered as Markdown
- [ ] Top issues are categorized with fixed error types
- [ ] Bad model output is handled gracefully
- [ ] Tests cover prompt generation and parsing behavior

---

# Phase 3: Observability

## Goal

Make the service observable like a real owned service.

## Deliverables

- [ ] Spring Boot Actuator enabled
- [ ] Prometheus endpoint exposed
- [ ] Grafana dashboard added
- [ ] Structured JSON logs
- [ ] Request ID / correlation ID
- [ ] Metrics for LLM latency, request count, error count, and correction status
- [ ] Basic service-level objectives documented

## Required Metrics

```text
http_server_requests_seconds
essay_correction_requests_total
essay_correction_success_total
essay_correction_failure_total
llm_request_latency_seconds
llm_request_failures_total
essay_feedback_persist_latency_seconds
```

## Example Dashboard Panels

- API request rate
- API p95 latency
- LLM p95 latency
- Error rate
- Correction success/failure count
- JVM memory
- Database connection pool usage

## Example SLOs

```text
Availability target:
- 99% successful correction requests during local demo tests

Latency target:
- p95 API latency under 30 seconds for single-user local mode

Correctness target:
- 90% of responses must include estimated level, top issues, and B2 rewrite
```

## Concepts to Understand

- Difference between logs, metrics, and traces
- Why p95 latency matters more than average latency
- How to debug a slow LLM-backed API
- How to design metrics from business workflow
- Why observability is part of service ownership

## Definition of Done

- [ ] `GET /actuator/health` works
- [ ] `GET /actuator/prometheus` works
- [ ] Prometheus scrapes the app
- [ ] Grafana shows useful dashboard panels
- [ ] Logs include request ID, essay ID, model, latency, and status

---

# Phase 4: Reliability and Incident Readiness

## Goal

Make failure modes explicit and practice incident response.

## Deliverables

- [ ] LLM timeout configuration
- [ ] Retry policy for transient LLM failures
- [ ] Clear fallback response for unavailable model
- [ ] Circuit breaker optional
- [ ] Runbook for common failures
- [ ] Incident drill scripts
- [ ] Postmortem template

## Failure Modes to Handle

| Failure | Expected Behavior |
|---|---|
| Ollama unavailable | Return controlled error, log failure, increment metric |
| Ollama slow | Timeout, return retryable error message |
| PostgreSQL unavailable | Fail fast with clear error and health check degraded |
| Model returns malformed output | Store raw output, return partial feedback or controlled failure |
| Request too large | Reject with 400 and clear validation message |

## Runbook Topics

Create files under `docs/runbooks/`:

```text
docs/runbooks/ollama-unavailable.md
docs/runbooks/high-latency.md
docs/runbooks/database-unavailable.md
docs/runbooks/malformed-llm-output.md
```

Each runbook should include:

- Symptoms
- Dashboard signals
- Log patterns
- Immediate mitigation
- Long-term fix
- Owner notes

## Concepts to Understand

- Timeout vs retry vs fallback
- Why retry can make overload worse
- Difference between mitigation and root cause fix
- How to reduce customer impact within SLA
- How to write useful runbooks and postmortems

## Definition of Done

- [ ] Stopping Ollama produces a controlled failure
- [ ] Slow LLM response triggers timeout
- [ ] Failure metrics show up in Grafana
- [ ] At least one incident drill is documented
- [ ] At least one postmortem is written

---

# Phase 5: Load Testing and Quality Loop

## Goal

Understand system behavior under pressure and improve feedback quality.

## Deliverables

- [ ] k6 smoke test
- [ ] k6 load test
- [ ] k6 stress test
- [ ] Load test result summary
- [ ] Small evaluation dataset
- [ ] Prompt versioning
- [ ] Regression comparison between prompt versions

## k6 Test Types

```text
smoke.js
- 1 virtual user
- validates basic endpoint works

load.js
- stable moderate traffic
- checks p95 latency and error rate

stress.js
- gradually increases traffic
- finds breaking point

spike.js
- sudden traffic jump
- observes recovery behavior
```

## Quality Evaluation Dataset

Create:

```text
eval/
  essays/
    essay_001.txt
    essay_002.txt
    essay_003.txt
  expected/
    essay_001_expected_issues.json
```

Track whether feedback contains:

- Estimated level
- Top 3 issues
- Sentence-level corrections
- B2 rewrite
- At least 3 useful exercises
- No obvious hallucinated exam rule

## Concepts to Understand

- Throughput vs latency
- Bottleneck analysis
- Capacity planning for local services
- LLM quality regression
- Prompt versioning as application versioning
- How to measure subjective feedback quality pragmatically

## Definition of Done

- [ ] k6 tests can run locally
- [ ] Load test report is committed under `docs/performance/`
- [ ] At least 5 essays exist in eval dataset
- [ ] Prompt changes can be compared against previous output
- [ ] One performance improvement is documented

---

# Phase 6: Portfolio and Interview Readiness

## Goal

Make the project easy to understand, demo, and explain.

## Deliverables

- [ ] Updated README
- [ ] Architecture diagram
- [ ] ADRs for key decisions
- [ ] Demo script
- [ ] Final dashboard screenshot optional
- [ ] Final load test report
- [ ] Final incident drill report
- [ ] Clear explanation of trade-offs

## ADRs to Write

Create under `docs/adr/`:

```text
0001-use-java-spring-boot.md
0002-use-ollama-for-local-llm.md
0003-use-postgresql-for-persistence.md
0004-start-with-synchronous-correction.md
0005-add-observability-before-advanced-features.md
0006-use-k6-for-local-load-testing.md
```

Each ADR should include:

- Context
- Decision
- Alternatives considered
- Consequences

## Demo Script

The final demo should show:

1. Start system with Docker Compose
2. Submit sample essay
3. View feedback result
4. Check saved essay in DB
5. Open Grafana dashboard
6. Run k6 smoke test
7. Stop Ollama and show controlled failure
8. Use runbook to explain mitigation
9. Show postmortem or incident drill note

## Interview Talking Points

You should be able to explain:

- Why this is a local LLM service instead of a cloud API service
- Why the first version is synchronous
- What the main bottleneck is
- How you monitor LLM latency and failures
- How you handle malformed model output
- How you would scale this if multiple users used it
- How you would add async processing later
- How you would evaluate feedback quality
- How you would reduce business continuity risk

## Concepts to Understand

- Architecture communication
- Stakeholder-friendly explanation
- Trade-off framing
- Evolutionary architecture
- Production ownership mindset

## Definition of Done

- [ ] A new engineer can run the project from README alone
- [ ] The architecture can be explained in 10 minutes
- [ ] The project has at least 3 meaningful ADRs
- [ ] The project has at least 2 runbooks
- [ ] The project has at least 1 postmortem or incident drill report
- [ ] The final demo proves more than “the model can answer”

---

## 6. Suggested Weekly Plan

## Week 1: Runnable Service

Focus:

- Spring Boot basics
- Ollama integration
- Basic API
- Docker Compose
- PostgreSQL persistence

Outcome:

```text
A local API that can correct an essay and save the result.
```

## Week 2: Clean Domain and Feedback Model

Focus:

- Prompt templates
- Structured output
- Domain model
- Error taxonomy
- Unit and integration tests

Outcome:

```text
A maintainable correction workflow with stable feedback format.
```

## Week 3: Observability

Focus:

- Actuator
- Micrometer
- Prometheus
- Grafana
- Structured logs

Outcome:

```text
A service that can be monitored and debugged.
```

## Week 4: Reliability

Focus:

- Timeout
- Retry
- Fallback
- Health states
- Runbooks
- Incident drills

Outcome:

```text
A service that fails predictably instead of mysteriously.
```

## Week 5: Load and Quality Evaluation

Focus:

- k6 smoke/load/stress tests
- Performance report
- Eval dataset
- Prompt comparison

Outcome:

```text
A service with measurable performance and feedback quality.
```

## Week 6: Documentation and Interview Story

Focus:

- README polish
- ADRs
- Architecture diagram
- Final demo script
- Postmortem
- Trade-off explanation

Outcome:

```text
A portfolio-ready system design demo.
```

---

## 7. Backlog

## Must Have

- [ ] Essay correction endpoint
- [ ] Ollama integration
- [ ] PostgreSQL persistence
- [ ] Docker Compose local setup
- [ ] Health check
- [ ] Prometheus metrics
- [ ] Grafana dashboard
- [ ] k6 smoke test
- [ ] Runbook for Ollama failure
- [ ] README with run instructions

## Should Have

- [ ] Structured feedback JSON
- [ ] Error taxonomy
- [ ] Prompt versioning
- [ ] Request ID logging
- [ ] Load test report
- [ ] Integration tests with Testcontainers
- [ ] ADRs
- [ ] Incident drill report

## Could Have

- [ ] Redis-based rate limiting
- [ ] Async correction job queue
- [ ] Web UI
- [ ] Feedback history dashboard
- [ ] Multi-model provider abstraction
- [ ] RAG over personal writing notes
- [ ] OpenTelemetry tracing

## Won't Have, For Now

- Cloud deployment
- User authentication
- Multi-tenant support
- Kubernetes
- Fine-tuning
- Custom model training
- Production-grade security hardening

---

## 8. Learning Checklist

By the end of this roadmap, I should understand:

- [ ] How a Spring Boot backend is structured
- [ ] How to integrate a local LLM service
- [ ] How to design a stable LLM workflow
- [ ] How to persist LLM inputs, outputs, and metadata
- [ ] How to expose health and metrics
- [ ] How to build a Grafana dashboard
- [ ] How to run local load tests
- [ ] How to reason about p95 latency and error rate
- [ ] How to handle LLM timeouts and malformed output
- [ ] How to write a useful runbook
- [ ] How to write a simple postmortem
- [ ] How to explain architecture trade-offs
- [ ] How to evolve a synchronous system toward async processing

---

## 9. Final Success Criteria

This project is successful when it can prove all of the following:

```text
1. The system runs locally with Docker Compose.
2. A user can submit a Dutch essay and receive useful B2 feedback.
3. The essay, feedback, and LLM call metadata are persisted.
4. The service exposes health checks and metrics.
5. Grafana shows latency, request rate, and error rate.
6. k6 can generate load and reveal bottlenecks.
7. Common failures are handled predictably.
8. Runbooks and ADRs explain how to operate and evolve the system.
9. The architecture can be explained clearly in an interview.
```

The final message of this project should not be:

```text
I built an AI writing tool.
```

It should be:

```text
I built and operated a local LLM-backed writing feedback service with clear service boundaries, persistence, observability, reliability handling, load testing, and documented architecture decisions.
```

That is the difference between a toy demo and a system design demo.
