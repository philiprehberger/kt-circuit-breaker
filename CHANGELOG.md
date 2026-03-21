# Changelog

## 0.1.6 (2026-03-20)

- Fix README: remove Groovy section, update badge label to "Tests"
- Fix CHANGELOG formatting: split malformed entry, remove preamble

## 0.1.5 (2026-03-20)

- Standardize README: fix title, badges, version sync, remove Requirements section

## 0.1.4 (2026-03-20)

- Add issueManagement to POM metadata

## 0.1.2 (2026-03-18)

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## 0.1.1 (2026-03-18)

- Fix CI badge and gradlew permissions

## 0.1.0 (2026-03-17)

### Added
- `CircuitBreaker` with CLOSED, OPEN, and HALF_OPEN states
- `circuitBreaker()` DSL for configuration
- Configurable failure threshold, success threshold, and reset timeout
- Exception filtering with `recordException` and `ignoreException`
- State change callbacks for observability
- Thread-safe implementation with atomic operations
