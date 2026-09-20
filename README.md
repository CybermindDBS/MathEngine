# MathEngine

An arbitrary-precision expression evaluator, built as a set of reactive Spring services.

The engine parses and evaluates expressions from scratch — no scripting library underneath — and
carries every value as an arbitrary-precision decimal, so `9000!` returns the full digit sequence
rather than `Infinity`. Expressions can declare variables, and can define their own functions in
Java, which are compiled and loaded at runtime.

## Input format

The first line is the expression. Lines after it declare variables, imports, and functions:

```
9! * (calc(10) / 2) + pi + var

var = 10 / 5 * 2

public int calc(int a) {
    logger.log("variable: " + a);
    return a * 1000;
}
```

Returns `1814400007.141592653589793238462643383279502884197...` plus whatever the function logged.

User-defined functions are compiled with Janino into an in-memory class, invoked reflectively, and
given a `logger` parameter that the engine injects into the signature — so a function can report its
own intermediate values back in the response.

Built in: `sqrt`, `cubrt`, `nthroot`, `abs`, `recip`, `logn`, `exp`, `pct`, `trunc`, `fib`, `pi`,
factorial, exponentiation, and `sum(...)` for summations over a range with a bound variable.

## Services

| Service | Role |
| --- | --- |
| `mathengine-core` | The parser, evaluator, and `BigNumber` arithmetic. Exposes `POST /api/evaluate`. |
| `mathengine-client` | Web front end. Accounts, saved calculations, saved function libraries. |
| `mathengine-assist` | Result cache over Redis. |
| `mathengine-gateway` | Spring Cloud Gateway. Routing and per-IP rate limiting. |
| `mathengine-service-registry` | Eureka. |
| `mathengine-admin` | Spring Boot Admin. |

### Caching only what is worth caching

Most expressions evaluate faster than a cache round-trip, so caching everything would make the
common case slower. Instead `mathengine-core` wraps evaluation in an aspect that gives the
calculation a head start — 250 ms by default, via `mathengineassist.config.min_wait_to_save` — and
only consults the cache if that deadline passes. Fast expressions never touch Redis.

Cache keys are canonical, not literal. Whitespace is stripped and variables, imports and function
bodies are sorted before hashing with SHA-256, so the same computation written two ways hits the
same entry.

The aspect also polls `mathengine-assist` for health every ten seconds and evaluates directly when
the cache is unreachable, so losing Redis costs speed rather than availability.

### Rate limiting

The gateway resolves a key per client IP and applies a Redis token bucket — 1 request/second
replenish, burst of 5 — to `/api/**`.

## Running it

Images are published to GHCR, so the compose file needs no local build:

```
docker compose up
```

The client is on `:8080` through the gateway. Eureka is on `:8761`.

To run a single service from source, each module has its own Maven wrapper:

```
cd mathengine-core && ./mvnw spring-boot:run
```

### Observability

`docker-compose.override.yml` is picked up automatically and adds the monitoring stack:

| | |
| --- | --- |
| Zipkin | `:9411` — distributed traces, Micrometer sampling at 100% |
| Kibana | `:5601` — log search, fed by Filebeat into Elasticsearch |
| Spring Boot Admin | `:8083` — health and metrics across all services |

Services log as structured JSON via `logstash-logback-encoder`, so traces and logs correlate by span.
Drop the override file to run without any of it.

## Notes

- Anonymous users get a UUID cookie, so calculations persist without an account. Signing in
  (form login or Google) attaches them to a user.
- The client stores accounts and calculation history in MySQL over R2DBC, reactive end to end.
- `Jenkinsfile` builds and pushes all six images. The stages shell out via `bat`, so it expects a
  Windows agent.
- Integration tests cover the API surface, including a 30-second budget for `9000!`.
