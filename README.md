# Dutch Essay Helper

A small Spring Boot application that uses a local Ollama model to give feedback on Dutch writing.

The current goal is simple: build one working end-to-end flow.

```text
client -> Spring Boot endpoint -> Ollama -> feedback response
```

This project is currently in **Phase 0**. The focus is not perfect architecture yet. The focus is proving that a Dutch essay can be submitted to a backend endpoint, sent to a local language model, and returned as useful writing feedback.

---

## What This App Does

The app exposes one API endpoint:

```http
POST /api/essays/correct
```

It accepts:

- the task type
- the original writing prompt
- the student's Dutch essay

It returns:

- model-generated feedback
- grammar comments
- improvement suggestions
- B2-level rewrites

This is intended for practicing Dutch NT2 writing, especially B1-B2 style tasks such as formal emails, opinion letters, and short argumentative texts.

---

## Example Writing Task

The repository currently includes one example essay:

```text
examples/aantrekkelijker-busvervoer.txt
```

This example is a letter from a passenger to a bus company manager. The passenger responds to a publicly announced plan for improving bus services and gives their own opinion and suggestions.

You can use this file as the first realistic test case for the correction endpoint.

---

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Ollama
- Qwen3 4B or another local Ollama model

Recommended local model for an 8GB-memory machine:

```bash
ollama pull qwen3:4b
```

For this project, a small local model is enough for the first version. The goal is to test the application flow before optimizing model quality.

---

## Start Ollama

If Ollama is installed as a system service, start or restart it:

```bash
sudo systemctl restart ollama
```

Check whether the Ollama API is available:

```bash
curl http://localhost:11434/api/tags
```

If you do not use the system service, run Ollama manually:

```bash
ollama serve
```

Keep that terminal open.

You do **not** need to run `ollama run qwen3:4b` before starting the Spring Boot app. `ollama run` is mainly for manual chat testing. The backend API request will trigger Ollama to load the configured model.

---

## Run the Spring Boot App

From the project root:

```bash
mvn spring-boot:run
```

The app should start on:

```text
http://localhost:8080
```

---

## Test the Endpoint

The easiest way is to use the provided script:

```bash
chmod +x run.sh
./run.sh
```

The script reads:

```text
examples/aantrekkelijker-busvervoer.txt
```

and sends it to:

```text
http://localhost:8080/api/essays/correct
```

---

## Manual Curl Example

You can also call the endpoint manually:

```bash
curl -X POST http://localhost:8080/api/essays/correct \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "formal_letter",
    "prompt": "A passenger writes to the manager of a bus company and gives feedback on a published bus improvement plan.",
    "essay": "Beste heer/mevrouw, ..."
  }'
```

---

## Troubleshooting

### Ollama is not reachable

If you see a connection error to `localhost:11434`, check:

```bash
curl http://localhost:11434/api/tags
```

If this fails, restart Ollama:

```bash
sudo systemctl restart ollama
```

### Spring Boot port is already in use

If port `8080` is occupied, change the port in:

```text
src/main/resources/application.yaml
```

Example:

```yaml
server:
  port: 8081
```

Then update `run.sh` or run it with:

```bash
API_URL=http://localhost:8081/api/essays/correct ./run.sh
```

### The model is slow

Local models can be slow, especially on CPU. For Phase 0, correctness of the request flow matters more than speed.

### The model output is inconsistent

For Phase 0, the app returns raw or lightly cleaned model output. A structured feedback schema can be added later.

---

## Phase 0 Stopping Point

Stop Phase 0 when these are true:

- The Spring Boot app starts
- `POST /api/essays/correct` works
- The app can read an essay request
- The app calls Ollama
- The app returns feedback
- `run.sh` can reproduce the demo

Do not add PostgreSQL, Docker Compose, metrics, retries, dashboards, or complex schemas yet.

Small vertical slice first. Fancy architecture later.