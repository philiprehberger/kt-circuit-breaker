# circuit-breaker

[![Tests](https://github.com/philiprehberger/kt-circuit-breaker/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-circuit-breaker/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/circuit-breaker.svg)](https://central.sonatype.com/artifact/com.philiprehberger/circuit-breaker)
[![Last updated](https://img.shields.io/github/last-commit/philiprehberger/kt-circuit-breaker)](https://github.com/philiprehberger/kt-circuit-breaker/commits/main)

Coroutine-aware circuit breaker for resilient Kotlin services.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.philiprehberger:circuit-breaker:0.1.6")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>circuit-breaker</artifactId>
    <version>0.1.6</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.circuitbreaker.*
import kotlin.time.Duration.Companion.seconds

val breaker = circuitBreaker("my-service") {
    failureThreshold = 5
    successThreshold = 2
    resetTimeout = 30.seconds
    onStateChange { from, to -> println("Circuit: $from -> $to") }
}

val result = breaker.execute {
    callExternalService()
}
```

### State Machine

```
CLOSED ──(failures >= threshold)──> OPEN
  ^                                    │
  │                          (timeout elapsed)
  │                                    v
  └──(successes >= threshold)── HALF_OPEN
                                       │
                              (failure) │
                                       v
                                     OPEN
```

### Exception Filtering

```kotlin
val breaker = circuitBreaker("api") {
    failureThreshold = 3
    recordException<IOException>()      // Only count these as failures
    ignoreException<ValidationException>() // Never count these
}
```

## API

| Class / Function | Description |
|------------------|-------------|
| `circuitBreaker()` | DSL builder for creating circuit breaker instances |
| `CircuitBreaker` | Main class with `execute()` and `state` property |
| `CircuitState` | Enum: `CLOSED`, `OPEN`, `HALF_OPEN` |
| `CircuitBreakerConfig` | Configuration data class with thresholds and timeout |
| `CircuitOpenException` | Thrown when the circuit is open |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## Support

If you find this project useful:

⭐ [Star the repo](https://github.com/philiprehberger/kt-circuit-breaker)

🐛 [Report issues](https://github.com/philiprehberger/kt-circuit-breaker/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

💡 [Suggest features](https://github.com/philiprehberger/kt-circuit-breaker/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

❤️ [Sponsor development](https://github.com/sponsors/philiprehberger)

🌐 [All Open Source Projects](https://philiprehberger.com/open-source-packages)

💻 [GitHub Profile](https://github.com/philiprehberger)

🔗 [LinkedIn Profile](https://www.linkedin.com/in/philiprehberger)

## License

[MIT](LICENSE)
