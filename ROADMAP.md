# Roadmap: Dutch Essay Helper

> A local, production-minded LLM writing feedback service for Dutch B2 essay practice.
>
> Built with Java/Spring Boot, Ollama, PostgreSQL, pgvector, Prometheus/Grafana, and k6.
>
> The goal is not just to call a model. The goal is to build an evolvable LLM-backed service that can store feedback history, evaluate quality, reduce hallucination, retrieve useful writing knowledge, and be explained clearly in a system design interview.

---

## 1. Product Goal

Dutch Essay Helper helps a user submit a Dutch B2 writing exercise and receive structured feedback, including:

- Estimated writing level
- Top writing issues
- Sentence-level corrections
- B2-level rewrite
- Reusable sentence patterns
- Targeted follow-up exercises
- Similar previous mistakes, later phase
- Relevant writing notes or rubric snippets, later phase

The system should be:

- Runnable locally
- Observable through metrics and logs
- Testable with unit, integration, and quality regression tests
- Failure-aware through timeouts, fallbacks, runbooks, and incident drills
- Evolvable from a simple synchronous LLM call into a retrieval-augmented writing feedback system

---

## 2. Why This Project Exists

This repository is a learning and portfolio project for system design and end-to-end service ownership.

The main learning goals are:

- Build a real backend service, not only an LLM script
- Understand LLM application architecture
- Practice Spring Boot service design
- Learn how to persist LLM inputs, outputs, and metadata
- Learn observability: logs, metrics, dashboards, health checks
- Practice reliability engineering: timeout, retry, fallback, incident response
- Understand prompt versioning and LLM quality regression
- Understand when relational storage is enough and when vector retrieval becomes useful
- Learn how to explain architecture decisions clearly

The project deliberately avoids cloud deployment in the first stage. The system should be fully runnable on a local machine.

---

## 3. Architecture Evolution Strategy

This project should evolve in layers.

The key rule:

```text
Start with PostgreSQL.
Add pgvector only when retrieval is needed.
Do not introduce a standalone vector database until there is a real scale or operational reason.
```

Why:

- Essays, feedback, request logs, prompt versions, and evaluation results are relational data.
- PostgreSQL is enough for the first production-minded version.
- Retrieval becomes useful only after the system has reusable writing notes, correction examples, rubric chunks, or a personal mistake history.
- pgvector is the natural first step because it adds vector search to PostgreSQL without adding a new database service.
- Qdrant, Milvus, Weaviate, or Chroma can be considered later only if pgvector becomes insufficient.

---

## 4. Target Architecture

### Phase 0-5 Architecture

```text
User / curl / Browser
        |
        v
Spring Boot API Service
        |
        |-- validates essay request
        |-- builds prompt
        |-- calls local Ollama model
        |-- parses structured feedback
        |-- validates grounded quotes
        |-- persists essay, feedback, prompt version, and LLM call log
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

### Phase 6+ Retrieval-Augmented Architecture

```text
User / curl / Browser
        |
        v
Spring Boot API Service
        |
        |-- validates essay request
        |-- extracts candidate issues
        |-- creates embedding for essay or problematic sentences
        |-- retrieves similar mistakes / writing notes / rubric chunks
        |-- builds grounded prompt with retrieved context
        |-- calls local Ollama generation model
        |-- validates structured feedback
        |-- persists feedback and retrieval metadata
        |
        +-------------> Ollama generation model
        |
        +-------------> Ollama embedding model
        |
        +-------------> PostgreSQL + pgvector
```

---

## 5. Recommended Tech Stack

| Area | Technology | Purpose |
|---|---|---|
| Language | Java 21 | Main backend language |
| Framework | Spring Boot 3 | API, configuration, dependency injection |
| LLM integration | Custom Ollama client first, Spring AI optional later | Keep Phase 0-2 explicit and understandable |
| Local model runtime | Ollama | Local model serving |
| Generation model | qwen3:4b or qwen3:4b-instruct | Local writing feedback |
| Embedding model, later | bge-m3 or nomic-embed-text | Semantic retrieval |
| Database | PostgreSQL | Essays, feedback, request logs, prompt versions, eval results |
| Vector extension, later | pgvector | Similarity search inside PostgreSQL |
| Migration | Flyway | Repeatable schema evolution |
| Observability | Spring Boot Actuator + Micrometer | Health and metrics |
| Metrics backend | Prometheus | Metric collection |
| Dashboard | Grafana | Local service dashboard |
| Load testing | k6 | Smoke, load, stress, and spike tests |
| Testing | JUnit 5 + Testcontainers | Unit and integration tests |
| Container orchestration | Docker Compose | Local multi-service setup |
| Documentation | Markdown ADRs, runbooks, postmortems | Engineering communication |

---

## 6. Roadmap Overview

| Phase | Duration | Main Outcome |
|---|---:|---|
| Phase 0 | Day 1 | Local vertical slice: submit essay, call Ollama, return feedback |
| Phase 1 | 1 week | Clean Spring Boot service with validation, config, error handling, and persistence |
| Phase 2 | 1 week | Structured feedback model, prompt templates, hallucination controls, and tests |
| Phase 3 | 1 week | Observability: metrics, logs, dashboards, health checks |
| Phase 4 | 1 week | Reliability: timeout, retry, fallback, incident drill, runbook |
| Phase 5 | 1 week | Load testing, quality evaluation, prompt regression loop |
| Phase 6 | 1 week | Retrieval-ready persistence and personal writing knowledge base |
| Phase 7 | 1 week | pgvector-based retrieval-augmented feedback |
| Phase 8 | 1 week | Portfolio polish: docs, ADRs, final demo script, architecture explanation |

Suggested target:

```text
Core system: 6 weeks, around 80-100 focused hours.
Retrieval extension: +2 weeks, around 25-35 focused hours.
```

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
- [ ] Simple feedback response
- [ ] One sample essay under `examples/`
- [ ] One sample curl command or `run.sh`
- [ ] README explains how to run the demo

## Minimal API

```http
POST /api/essays/correct
Content-Type: application/json

{
  "taskType": "formal_letter",
  "prompt": "A passenger writes to the manager of a bus company and gives feedback on a published bus improvement plan.",
  "essay": "Geachte heer/mevrouw, ..."
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
- How request JSON becomes a Java object
- How a backend service calls a local LLM
- What an LLM prompt actually contains
- Why local model output can be unstable
- Why a working vertical slice beats over-designed architecture

## Definition of Done

- [ ] `curl` or `run.sh` can submit an essay
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
- [ ] LLM request logs persisted

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
    PromptVersion.java
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
      PromptVersionRepository.java
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
- feedback_json
- prompt_version
- model
- created_at

llm_request_logs
- id
- essay_id
- model
- prompt_version
- prompt_tokens_estimate
- completion_tokens_estimate
- latency_ms
- status
- error_message
- raw_response
- created_at

prompt_versions
- id
- name
- version
- template
- description
- active
- created_at
```

## Concepts to Understand

- Why controller/service/client separation matters
- Transaction boundaries
- Data integrity and auditability
- Why LLM calls should be hidden behind an interface
- Why configuration should not be hardcoded
- Why prompt versioning belongs in an LLM-backed system

## Definition of Done

- [ ] App starts with `docker compose up`
- [ ] PostgreSQL schema is created automatically
- [ ] Essay and feedback are saved
- [ ] LLM call logs are saved
- [ ] Prompt version is recorded
- [ ] Invalid requests return clear 4xx errors
- [ ] Ollama failures return clear 5xx errors

---

# Phase 2: Structured Feedback and Hallucination Control

## Goal

Move from free-form LLM output to stable, parseable, and partially verifiable feedback.

## Deliverables

- [ ] Prompt templates in `src/main/resources/prompts/`
- [ ] Structured feedback JSON model
- [ ] Markdown renderer from structured feedback
- [ ] Error taxonomy
- [ ] Grounded quote requirement
- [ ] Validation that each quoted issue exists in the original essay
- [ ] Unit tests for prompt rendering
- [ ] Integration test for correction workflow
- [ ] Bad model output handling

## Feedback Model

```json
{
  "estimatedLevel": "B1+",
  "confidence": "medium",
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
      "originalQuote": "Omdat de bus vaak te laat komt, ik vind het plan belangrijk.",
      "corrected": "Omdat de bus vaak te laat komt, vind ik het plan belangrijk.",
      "explanation": "When a subordinate clause comes first, the main clause requires inversion, so the verb should come before the subject.",
      "grounded": true
    }
  ],
  "b2Rewrite": "...",
  "exercises": [
    {
      "type": "rewrite",
      "instruction": "Rewrite the sentence below with correct inversion.",
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
conciseness
```

## Hallucination Controls

The model should be instructed to follow these rules:

```text
1. Correct only the essay provided.
2. Do not invent content that is not in the essay.
3. Every issue must include an exact original quote.
4. If an issue cannot quote the original essay, do not include it.
5. If uncertain, say uncertain instead of guessing.
6. Keep rewritten text at B2 level.
7. Do not invent official exam rules.
```

The application should validate:

```text
essay.contains(originalQuote)
```

If the quote is missing, the issue should be marked as ungrounded or removed.

## Concepts to Understand

- Prompt engineering as software design
- Why output schema matters
- Why stable interfaces matter for downstream processing
- Why LLM output needs validation
- Why product-specific taxonomy improves learning value
- Difference between model correctness and system-level correctness

## Definition of Done

- [ ] Feedback is returned in a stable JSON-compatible structure
- [ ] Feedback can be rendered as Markdown
- [ ] Top issues are categorized with fixed error types
- [ ] Each sentence-level issue contains an original quote
- [ ] Ungrounded issues are detected
- [ ] Bad model output is handled gracefully
- [ ] Tests cover prompt generation, parsing, and quote validation

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
- [ ] Metrics for LLM latency, request count, error count, correction status, and malformed output
- [ ] Basic service-level objectives documented

## Required Metrics

```text
http_server_requests_seconds
essay_correction_requests_total
essay_correction_success_total
essay_correction_failure_total
llm_request_latency_seconds
llm_request_failures_total
llm_malformed_output_total
llm_ungrounded_issue_total
essay_feedback_persist_latency_seconds
```

## Example Dashboard Panels

- API request rate
- API p95 latency
- LLM p95 latency
- Error rate
- Malformed output count
- Ungrounded issue count
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
- 95% of sentence-level corrections must quote text from the original essay
```

## Concepts to Understand

- Difference between logs, metrics, and traces
- Why p95 latency matters more than average latency
- How to debug a slow LLM-backed API
- How to design metrics from business workflow
- Why observability is part of service ownership
- Why LLM quality failures should be measured, not only HTTP failures

## Definition of Done

- [ ] `GET /actuator/health` works
- [ ] `GET /actuator/prometheus` works
- [ ] Prometheus scrapes the app
- [ ] Grafana shows useful dashboard panels
- [ ] Logs include request ID, essay ID, model, prompt version, latency, and status
- [ ] Malformed output and ungrounded issues are visible in metrics

---

# Phase 4: Reliability and Incident Readiness

## Goal

Make failure modes explicit and practice incident response.

## Deliverables

- [ ] LLM timeout configuration
- [ ] Retry policy for transient LLM failures
- [ ] Clear fallback response for unavailable model
- [ ] Circuit breaker optional
- [ ] Controlled handling for malformed LLM output
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
| Model returns ungrounded issues | Remove or mark ungrounded issues, increment metric |
| Request too large | Reject with 400 and clear validation message |
| Prompt version missing | Fail fast during startup or return controlled internal error |

## Runbook Topics

Create files under `docs/runbooks/`:

```text
docs/runbooks/ollama-unavailable.md
docs/runbooks/high-latency.md
docs/runbooks/database-unavailable.md
docs/runbooks/malformed-llm-output.md
docs/runbooks/ungrounded-feedback.md
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
- Why model quality incidents are still service incidents

## Definition of Done

- [ ] Stopping Ollama produces a controlled failure
- [ ] Slow LLM response triggers timeout
- [ ] Malformed model output is handled predictably
- [ ] Failure metrics show up in Grafana
- [ ] At least one incident drill is documented
- [ ] At least one postmortem is written

---

# Phase 5: Load Testing and Quality Evaluation Loop

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
- [ ] Quality report for hallucination and groundedness

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
    essay_004.txt
    essay_005.txt
  expected/
    essay_001_expected_issues.json
  outputs/
    prompt_v001/
    prompt_v002/
```

Track whether feedback contains:

- Estimated level
- Top 3 issues
- Sentence-level corrections
- B2 rewrite
- At least 3 useful exercises
- No obvious hallucinated exam rule
- No invented essay content
- Every issue has an original quote
- Original quote exists in the essay
- Rewrite does not exceed B2 too aggressively

## Concepts to Understand

- Throughput vs latency
- Bottleneck analysis
- Capacity planning for local services
- LLM quality regression
- Prompt versioning as application versioning
- How to measure subjective feedback quality pragmatically
- Why evals matter before retrieval is added

## Definition of Done

- [ ] k6 tests can run locally
- [ ] Load test report is committed under `docs/performance/`
- [ ] At least 5 essays exist in eval dataset
- [ ] Prompt changes can be compared against previous output
- [ ] Groundedness is measured
- [ ] One performance improvement is documented

---

# Phase 6: Retrieval-Ready Persistence and Writing Knowledge Base

## Goal

Prepare the system for retrieval without introducing vector search too early.

This phase still uses normal PostgreSQL tables. The purpose is to collect useful knowledge and structure it properly before embedding it.

## Deliverables

- [ ] Personal writing notes table
- [ ] Correction examples table
- [ ] Rubric chunks table
- [ ] Source type and provenance fields
- [ ] Admin or seed script for adding notes
- [ ] Basic keyword search
- [ ] Documentation explaining what should and should not be retrieved

## Database Tables

```text
writing_notes
- id
- title
- content
- language
- source_type
- tags
- created_at
- updated_at

correction_examples
- id
- original_sentence
- corrected_sentence
- error_type
- explanation
- level
- source
- created_at

rubric_chunks
- id
- source
- section
- content
- language
- created_at
```

## Example Writing Notes

```text
Title: Dutch inversion after fronted adverbial
Content: If a sentence starts with an adverbial phrase, the finite verb usually comes before the subject.
Tags: word_order, inversion, B1, B2
```

## Concepts to Understand

- Difference between storing knowledge and retrieving knowledge
- Why provenance matters
- Why retrieval quality depends on good chunks
- Why not every document belongs in RAG
- Why keyword search is a useful baseline

## Definition of Done

- [ ] Writing notes can be stored
- [ ] Correction examples can be stored
- [ ] Rubric chunks can be stored
- [ ] Data has source/provenance metadata
- [ ] Basic keyword search works
- [ ] The app still works without vector search

---

# Phase 7: pgvector-Based Retrieval-Augmented Feedback

## Goal

Add semantic retrieval so the system can use previous mistakes, personal writing notes, and rubric chunks to improve feedback.

This phase introduces vector search, but keeps PostgreSQL as the database by using pgvector.

## Decision

Use:

```text
PostgreSQL + pgvector
```

Do not introduce a standalone vector database yet.

Reason:

- The project already uses PostgreSQL.
- Data volume is small.
- Operational simplicity matters.
- pgvector is enough for local semantic retrieval.
- A dedicated vector database can be evaluated later if scale, latency, or advanced retrieval features require it.

## Deliverables

- [ ] pgvector enabled in Docker Compose
- [ ] Flyway migration for vector extension
- [ ] Embedding model configured through Ollama
- [ ] Embedding client implemented
- [ ] Embeddings generated for writing notes
- [ ] Embeddings generated for correction examples
- [ ] Similar mistake retrieval
- [ ] Retrieved context injected into prompt
- [ ] Retrieval metadata logged
- [ ] Evaluation comparing with and without retrieval

## Suggested Retrieval Flow

```text
1. User submits essay.
2. App extracts candidate problematic sentences or uses the full essay.
3. App creates embedding using local embedding model.
4. App retrieves similar correction examples and writing notes from pgvector.
5. App builds prompt with:
   - original essay
   - task prompt
   - top retrieved examples
   - top retrieved writing notes
6. App calls generation model.
7. App validates grounded feedback.
8. App persists retrieval metadata and final feedback.
```

## Suggested Tables

```text
writing_note_embeddings
- id
- writing_note_id
- embedding
- model
- created_at

correction_example_embeddings
- id
- correction_example_id
- embedding
- model
- created_at

retrieval_logs
- id
- essay_id
- query_text
- embedding_model
- retrieved_item_type
- retrieved_item_id
- similarity_score
- created_at
```

## Example SQL

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE correction_example_embeddings (
    id BIGSERIAL PRIMARY KEY,
    correction_example_id BIGINT NOT NULL,
    embedding vector(1024) NOT NULL,
    model TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
```

The embedding dimension must match the chosen embedding model.

## Retrieval Quality Checks

Track:

- Did retrieval improve the usefulness of feedback?
- Did retrieval reduce repeated mistakes?
- Did retrieval introduce irrelevant context?
- Did retrieval increase hallucination?
- Did latency remain acceptable?
- Did the model overfit to retrieved examples?

## Concepts to Understand

- Embeddings
- Cosine similarity
- Vector indexes
- RAG vs fine-tuning
- Retrieval quality vs generation quality
- Why RAG can both reduce and introduce hallucination
- Why retrieval needs evaluation

## Definition of Done

- [ ] pgvector runs locally
- [ ] Embeddings are stored for notes and examples
- [ ] Similar examples can be retrieved
- [ ] Feedback prompt includes retrieved context
- [ ] Retrieval logs are persisted
- [ ] A quality comparison report exists
- [ ] ADR explains why pgvector was chosen before a dedicated vector database

---

# Phase 8: Portfolio and Interview Readiness

## Goal

Make the project easy to understand, demo, and explain.

## Deliverables

- [ ] Updated README
- [ ] Architecture diagram
- [ ] ADRs for key decisions
- [ ] Demo script
- [ ] Final dashboard screenshot optional
- [ ] Final load test report
- [ ] Final quality evaluation report
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
0007-use-structured-output-and-grounded-quotes.md
0008-use-pgvector-before-dedicated-vector-database.md
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
3. View structured feedback result
4. Show grounded quote validation
5. Check saved essay in DB
6. Check LLM request logs
7. Open Grafana dashboard
8. Run k6 smoke test
9. Stop Ollama and show controlled failure
10. Use runbook to explain mitigation
11. Optional: show retrieval-augmented feedback using pgvector
12. Show postmortem or incident drill note

## Interview Talking Points

You should be able to explain:

- Why this is a local LLM service instead of a cloud API service
- Why the first version is synchronous
- What the main bottleneck is
- How you monitor LLM latency and failures
- How you handle malformed model output
- How you reduce hallucination with grounded quotes
- Why PostgreSQL is enough at first
- Why pgvector is a better next step than a standalone vector database
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
- Retrieval as a product capability, not a buzzword

## Definition of Done

- [ ] A new engineer can run the project from README alone
- [ ] The architecture can be explained in 10 minutes
- [ ] The project has at least 5 meaningful ADRs
- [ ] The project has at least 3 runbooks
- [ ] The project has at least 1 postmortem or incident drill report
- [ ] The final demo proves more than “the model can answer”
- [ ] The final story explains how the system can evolve

---

## 7. Suggested Weekly Plan

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
- Grounded quote validation
- Domain model
- Error taxonomy
- Unit and integration tests

Outcome:

```text
A maintainable correction workflow with stable feedback format and basic hallucination control.
```

## Week 3: Observability

Focus:

- Actuator
- Micrometer
- Prometheus
- Grafana
- Structured logs
- LLM quality metrics

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
- Groundedness measurement

Outcome:

```text
A service with measurable performance and feedback quality.
```

## Week 6: Retrieval-Ready Knowledge Base

Focus:

- Writing notes
- Correction examples
- Rubric chunks
- Provenance metadata
- Keyword search baseline

Outcome:

```text
A system that stores reusable writing knowledge before adding vector search.
```

## Week 7: pgvector Retrieval

Focus:

- pgvector
- Local embedding model
- Similar mistake retrieval
- Retrieved context in prompt
- Retrieval evaluation

Outcome:

```text
A local retrieval-augmented writing feedback system.
```

## Week 8: Documentation and Interview Story

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

## 8. Backlog

## Must Have

- [ ] Essay correction endpoint
- [ ] Ollama integration
- [ ] PostgreSQL persistence
- [ ] Docker Compose local setup
- [ ] Structured feedback JSON
- [ ] Grounded quote validation
- [ ] Health check
- [ ] Prometheus metrics
- [ ] Grafana dashboard
- [ ] k6 smoke test
- [ ] Runbook for Ollama failure
- [ ] README with run instructions

## Should Have

- [ ] Error taxonomy
- [ ] Prompt versioning
- [ ] Request ID logging
- [ ] Load test report
- [ ] Quality evaluation dataset
- [ ] Integration tests with Testcontainers
- [ ] ADRs
- [ ] Incident drill report
- [ ] Writing notes table
- [ ] Correction examples table

## Could Have

- [ ] pgvector semantic retrieval
- [ ] RAG over personal writing notes
- [ ] Similar mistake retrieval
- [ ] Rubric chunk retrieval
- [ ] Redis-based rate limiting
- [ ] Async correction job queue
- [ ] Web UI
- [ ] Feedback history dashboard
- [ ] Multi-model provider abstraction
- [ ] OpenTelemetry tracing
- [ ] Dedicated vector database evaluation

## Won't Have, For Now

- Cloud deployment
- User authentication
- Multi-tenant support
- Kubernetes
- Fine-tuning
- Custom model training
- Production-grade security hardening
- Dedicated vector database in the first retrieval version

---

## 9. Vector Database Decision Guide

Use this guide when deciding whether to move beyond PostgreSQL.

## Use Normal PostgreSQL When

- You are storing essays, feedback, logs, prompt versions, and eval results
- You search by ID, date, status, model, prompt version, or error type
- You need auditability and simple reporting
- You are still improving prompt and output structure

## Use PostgreSQL + pgvector When

- You want to retrieve semantically similar mistakes
- You want to retrieve relevant writing notes
- You want to retrieve rubric chunks
- Your data volume is still small to medium
- You want to avoid operating another database

## Consider a Dedicated Vector Database When

- pgvector latency becomes unacceptable
- Retrieval data becomes large enough to justify separate scaling
- You need advanced vector search features that pgvector cannot provide
- You need independent scaling of vector search and relational storage
- You can explain the operational cost clearly

Candidate open-source vector databases:

```text
Qdrant
Milvus
Weaviate
Chroma
```

Default decision for this project:

```text
Start with PostgreSQL.
Add pgvector when retrieval becomes useful.
Do not add a dedicated vector database until there is evidence.
```

---

## 10. Learning Checklist

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
- [ ] How to reduce hallucination with grounded validation
- [ ] How to write a useful runbook
- [ ] How to write a simple postmortem
- [ ] How to explain architecture trade-offs
- [ ] How to evolve a synchronous system toward async processing
- [ ] How to decide between relational storage, pgvector, and a dedicated vector database
- [ ] How retrieval-augmented generation improves and complicates an LLM system

---

## 11. Final Success Criteria

This project is successful when it can prove all of the following:

```text
1. The system runs locally with Docker Compose.
2. A user can submit a Dutch essay and receive useful B2 feedback.
3. The essay, feedback, and LLM call metadata are persisted.
4. Feedback is structured and partially validated against the original essay.
5. The service exposes health checks and metrics.
6. Grafana shows latency, request rate, error rate, and LLM quality signals.
7. k6 can generate load and reveal bottlenecks.
8. Common failures are handled predictably.
9. Runbooks and ADRs explain how to operate and evolve the system.
10. Optional advanced demo: pgvector retrieves similar mistakes or writing notes.
11. The architecture can be explained clearly in an interview.
```

The final message of this project should not be:

```text
I built an AI writing tool.
```

It should be:

```text
I built and operated a local LLM-backed writing feedback service with clear service boundaries, persistence, observability, reliability handling, quality evaluation, retrieval-ready architecture, and documented trade-off decisions.
```

That is the difference between a toy demo and a system design demo.
