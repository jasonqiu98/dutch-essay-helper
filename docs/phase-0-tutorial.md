# Phase 0 Tutorial: Build the First End-to-End Slice

This guide is for building **Phase 0 yourself**.

The goal is not to produce perfect architecture yet. The goal is to get one thin, working request flow:

```text
client -> Spring Boot endpoint -> Ollama -> response
```

You should finish today with a demo that accepts a Dutch essay and returns feedback.

This tutorial is intentionally incomplete in a few places. That is on purpose. You will learn more by filling in some gaps yourself.

---

## What You Are Building Today

By the end of Phase 0, you want this to work:

```http
POST /api/essays/correct
Content-Type: application/json

{
  "taskType": "formal_email",
  "prompt": "Write an email to the management about workspace satisfaction.",
  "essay": "Beste directie, ..."
}
```

And the service should return something like:

```json
{
  "feedback": "..."
}
```

That is enough for today.

Do not add persistence, metrics, retries, dashboards, or a polished domain model yet.

---

## What You Should Learn From This Phase

Focus on these ideas while building:

- How Spring Boot exposes an HTTP endpoint
- How request JSON becomes a Java object
- How your service code calls another HTTP service
- What prompt text you actually send to the model
- What breaks when model output is inconsistent
- Why a small working slice is more valuable than over-planning

When you get stuck, try to understand **why the piece exists**, not just how to make the error disappear.

---

## Suggested Timebox

Use this as a rough plan for one focused session:

- 30-45 min: scaffold Spring Boot app
- 30-45 min: create the endpoint and request/response classes
- 45-60 min: connect to Ollama
- 20-30 min: test with `curl`
- 20-30 min: write short run instructions in README

If one step takes longer, that is fine. Keep the scope small.

---

## Before You Start

You should install or prepare:

- Java 21
- Gradle or Maven
- Docker Desktop or Docker Engine
- Ollama
- A model you can call locally through Ollama

You can decide whether to:

1. Run Spring Boot locally and Ollama locally
2. Run Spring Boot in Docker later, but keep today focused on local development

Recommendation: start with **both running locally** if you want the fastest feedback loop.

---

## Deliverables for Today

You are done when all of these are true:

- A Spring Boot app starts successfully
- `POST /api/essays/correct` exists
- The endpoint accepts the sample JSON
- The app calls Ollama
- The app returns feedback text
- You can test it with one `curl` command
- The README explains how to run the demo

Anything beyond that is a bonus.

---

## Step 1: Scaffold the Project

Create a minimal Spring Boot application.

Include only what you need for today:

- Web
- Validation if you want it now
- Actuator is optional for today

Questions to explore yourself:

- Do you want to use Gradle or Maven?
- What package name will you use?
- Do you want to generate the project from Spring Initializr or create it manually?

Recommended output structure for today:

```text
src/main/java/...
src/main/resources/
```

Your first checkpoint:

- The app starts
- You can open the default local port in a browser or hit it with `curl`

If this takes too long, stop optimizing and just get a generated starter app running.

---

## Step 2: Create the API Contract

Add one controller with one endpoint:

```text
POST /api/essays/correct
```

Create:

- a request DTO
- a response DTO
- a controller

Your request DTO should contain:

- `taskType`
- `prompt`
- `essay`

Your response DTO only needs:

- `feedback`

Do not over-design this part. Keep it plain.

Questions to explore:

- Should the controller return the DTO directly or wrap it in `ResponseEntity`?
- Do you want validation annotations now or later?
- What should happen if `essay` is blank?

Checkpoint:

- You can send a hardcoded response without calling Ollama yet

Example temporary behavior:

```json
{
  "feedback": "stub response"
}
```

This is an important step. Prove the HTTP layer works before adding model calls.

---

## Step 3: Add a Service Layer

Create a service class that the controller calls.

The controller should be thin:

- accept request
- pass it to a service
- return response

The service should own:

- prompt building
- Ollama call
- response extraction

Do not worry yet about perfect package structure. A simple controller + service split is enough.

Ask yourself:

- Why is it useful to keep HTTP code separate from LLM call logic?
- What code would become messy if everything lived in the controller?

Checkpoint:

- The controller delegates to a service
- The service still returns a stub response

---

## Step 4: Call Ollama

Now connect the service to your local Ollama instance.

You need to figure out:

- the Ollama base URL
- the endpoint Ollama exposes for generation or chat
- the request shape it expects
- the response shape it returns

Do not hide this discovery step from yourself. Read the Ollama API docs and inspect a real response.

Recommended approach:

1. Test Ollama outside your app first
2. Confirm the model is available
3. Send one manual request
4. Only then implement the Java client code

Useful things to learn here:

- What is the exact JSON sent to Ollama?
- Do you want one big prompt string or a more structured chat-style input?
- Which field in the response contains the actual generated text?

Implementation hint:

Use whichever Spring HTTP client feels simplest to you for Phase 0. Pick one and move on.

Good enough for today:

- one client class or one service method that posts JSON to Ollama
- extract the generated text
- return it as `feedback`

Do not build an abstraction layer yet unless it helps you think clearly.

---

## Step 5: Write a Basic Prompt

Your prompt does not need to be smart yet. It just needs to reliably ask for useful feedback.

A simple prompt should include:

- the user's task type
- the original writing prompt
- the essay text
- instructions for the kind of feedback you want

For example, ask for:

- estimated level
- top mistakes
- corrected examples
- a better B2 rewrite

Do not spend too long polishing prompt quality today.

What to explore:

- Does the model respond better to bullet-point instructions or numbered instructions?
- Does it behave better if you explicitly ask for Markdown?
- What happens if the essay is short or low quality?

Checkpoint:

- the app sends a real prompt to Ollama
- Ollama returns model output

---

## Step 6: Return the Simplest Useful Response

For Phase 0, do not parse structured JSON from the model yet.

Just return:

```json
{
  "feedback": "raw or lightly cleaned model output"
}
```

This keeps your first version robust enough to demo and small enough to refactor later.

You may want to do a tiny bit of cleanup:

- trim whitespace
- handle empty responses
- return a fallback message if the model output is missing

But keep it light.

---

## Step 7: Test the Vertical Slice

Create one sample essay file for yourself, for example:

```text
examples/essay_001.txt
```

Then test the endpoint with `curl`.

Your test checklist:

- Spring Boot is running
- Ollama is running
- the target model is available
- the request reaches your endpoint
- the app calls Ollama successfully
- the response comes back to the client

When the flow fails, identify where:

- request never reaches controller
- DTO binding fails
- service throws error
- Ollama connection fails
- response parsing fails

That debugging path is part of the learning.

---

## Step 8: Document Only the Minimum

Update the README with:

- prerequisites
- how to start Ollama
- how to run the app
- one sample `curl` request

Do not write the final polished README yet. Just make the project runnable by future-you.

Ask yourself:

- If I come back in 10 days, can I run this in 5 minutes?

If the answer is no, add one more line of documentation.

---

## What To Skip Today

These are important later, but they are not Phase 0 work:

- PostgreSQL
- Flyway
- Docker Compose for every dependency
- Prometheus
- Grafana
- Testcontainers
- retries
- circuit breakers
- structured feedback schema
- runbooks

Protect your learning focus by saying no to these for now.

---

## Suggested File Set

You do not need exactly these names, but something close is enough:

```text
src/main/java/.../DutchEssayHelperApplication.java
src/main/java/.../api/EssayCorrectionController.java
src/main/java/.../api/dto/CorrectionRequest.java
src/main/java/.../api/dto/CorrectionResponse.java
src/main/java/.../application/EssayCorrectionService.java
src/main/resources/application.properties
examples/essay_001.txt
```

If your structure ends up slightly different, that is fine.

---

## When To Stop

Stop Phase 0 as soon as you have:

- one endpoint
- one working Ollama call
- one sample essay
- one reproducible demo command

That stopping discipline matters. It keeps the project moving in layers instead of turning Day 1 into a messy Week 1.

---

## Reflection Questions

After you finish, write short answers to these:

1. What part of the request flow felt most natural?
2. What part felt confusing?
3. What did Ollama return that surprised you?
4. What would be painful to maintain if this code grew 5x?
5. What do you now understand better about backend + LLM integration?

These notes will help you design Phase 1 more thoughtfully.

---

## If You Finish Early

Only pick one:

1. Add basic validation for blank inputs
2. Move Ollama base URL and model name into configuration
3. Add a very small error response for failed Ollama calls

Do not start three new improvements at once.

---

## Final Advice

Your target for today is not "build the system."

Your target is:

```text
prove the core path works, and understand each hop in the flow
```

That is the right level of ambition for Phase 0.
