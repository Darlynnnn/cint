# Real-Time Data Anomaly Detector

A small event-driven system that generates a continuous stream of numerical data, publishes it to Kafka, and detects statistically significant anomalies in real time using a rolling Z-score test.

## Architecture

Three components, each its own independent Gradle project and Docker image:

| Component | Role |
|---|---|
| `cint_common` | Shared library — the `MessagePayload` message contract used by both producer and consumer |
| `cint_producer` | Generates Gaussian-distributed sensor data on a schedule, occasionally injects a significant outlier, publishes to Kafka |
| `cint_consumer` | Subscribes to the topic, maintains a rolling window per data point, computes a Z-score, flags and alerts on anomalies |
| Kafka | `apache/kafka:3.8.0`, single-node KRaft mode (no ZooKeeper) |


## Statistical model

Implemented in `cint_consumer`'s `SlidingWindowZValueCalculator`:

- Maintains a rolling window of the `N` most recent data points (default `N=100`, configurable).
- For each new point, computes the population mean (`μ`) and standard deviation (`σ`) of the *existing* window (before inserting the new point) using an incremental (Welford's) algorithm — O(1) per update rather than rescanning the whole window.
- Z-score: `Z = |x_t − μ| / σ`. A point is flagged as an anomaly if `Z` exceeds a configurable threshold (default `3`).
- Guards against division-by-zero (identical values → zero variance) and against evaluating before enough data exists (`< 2` points in the window).

The math is verified in `SlidingWindowZValueCalculatorTest` via a differential test — the incremental algorithm's output is compared against an independently-computed naive recalculation over a long randomized sequence, not just a handful of hand-picked cases.

## Configuration

| Property | Env var | Default | Meaning                                                             |
|---|---|---|---------------------------------------------------------------------|
| `cint.producer.fixed-rate` | `CINT_PRODUCER_FIXED_RATE` | `1000` (ms) | How often the producer emits a data point                           |
| `gauss.mean` | `GAUSS_MEAN` | `100` | Mean of the normal distribution the producer samples from           |
| `gauss.stdDev` | `GAUSS_STD_DEV` | `15` | Standard deviation of the normal distribution                       |
| `gauss.anomalyChance` | `GAUSS_ANOMALY_CHANCE` | `0.05` | Probability any given point is deliberately an outlier  0 - 1 value |
| `gauss.anomalyMean` | `GAUSS_ANOMALY_MEAN` | `200` | Offset added to the mean when generating an intentional outlier     |
| `cint.window.size` | `CINT_WINDOW_SIZE` | `100` | Rolling window size for the Z-score calculation                     |
| `cint.anomaly.threshold` | `CINT_ANOMALY_THRESHOLD` | `3` | Z-score threshold above which a point is flagged as anomaly         |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address                                                |

## Build & run

`cint_common` is consumed by the other two modules as a real Maven dependency (`org.example:cint_common:0.0.1-SNAPSHOT`), resolved from your local Maven repository (`~/.m2`) — standing in for what would be a real artifact registry in production.

```bash
# 1. Publish the shared library locally
cd cint_common && ./gradlew publishToMavenLocal

# 2. Build and start everything — the jars themselves are built
# automatically inside the Docker build, no separate ./gradlew step needed
cd ../local
docker compose up --build
```

Each service's `Dockerfile` is a multi-stage build: the first stage runs `gradle bootJar` inside the container itself (mounting `~/.m2/repository/org/example/cint_common` in via a Docker Compose `additional_contexts`, so it can resolve the dependency published in step 1), the second stage packages the resulting jar into the runtime image. This mirrors how a real CI pipeline would build these images — resolving the shared library from a repository, not from a jar pre-built on a developer's machine.

Whenever `cint_common` changes, step 1 needs to be re-run before rebuilding the other two.

## Testing

Each module (`cint_producer`, `cint_consumer`) has two test source sets:

- **`./gradlew test`** — fast unit tests, no external dependencies.
- **`./gradlew integrationTest`** — slower, requires Docker. Spins up a real Kafka broker via Testcontainers

## Up Next

**What additional tooling would be used for this in production?**
- **CI**: a pipeline 
- **Artifact registry**: cint_common is shared library, in production it would be published to a Maven repository (Artifactory, Nexus, etc.) instead of `mavenLocal()`.
- **Linting/formatting**: Spotless or Checkstyle wired into the Gradle build, enforced in CI.
- **SonarQube** for static analysis and code quality metrics.

**Would this be deployable to Kubernetes?**
Yes, with some additions:
- With external configuration via ConfigMaps
- With external secret configuration via Secrets injected by for example Vault or keepass, instead of hardcoded values in `application.yml`.
- Health and readiness probe
- With metrics and tracing endpoints (Prometheus, OpenTelemetry) for observability

**Most obvious missing technical requirements:**
- **No dead-letter handling.** In production DLQ should be configured for the consumer, and the producer should have a retry policy for transient failures.
- **Scaling limitations** The rolling windows live in singles JVM memory, Running multiple replicas ( or pods ) would require distributed state management of the sliding window and synchronization
- **No observability beyond console logs.** No metrics (Prometheus counters for anomalies detected, processing latency, number of message published, number of failed publishing/consuming of message etc), no tracing. Console logging satisfies this assignment's spec, but a real deployment would want both.
- **Retry topic** no retry topic for kafka's consumer to process later if processing fails

## Known design tradeoffs

- `cint_common`'s local-only publishing step (see *Build & run*) is a deliberate simplification given the take-home time constraint — see "Up Next" for what it would look like for real.
- The rolling-window statistics use `BigDecimal` throughout for precision consistency with the rest of the message pipeline, at some performance cost compared to primitive `double` arithmetic — reasonable at this data rate, would need revisiting under significantly higher throughput.
- Not multithreaded — the producer and conumer are single-threaded, and the consumer's rolling window is not synchronized. This is a deliberate simplification given the take-home time constraint, but would need to be revisited for a production system.
- kafka's concurrency factor is set to 1, so only one consumer instance will be active at a time. This is a deliberate simplification given the take-home time constraint, but would need to be revisited for a production system.
- 
- Assesment only specify OK / ANOMALY as output meaning if less than 2 points in the window, the zscore will be 0 and OK.