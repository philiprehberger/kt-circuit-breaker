# Changelog

All notable changes to this library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-03-17

### Added
- `CircuitBreaker` with CLOSED, OPEN, and HALF_OPEN states
- `circuitBreaker()` DSL for configuration
- Configurable failure threshold, success threshold, and reset timeout
- Exception filtering with `recordException` and `ignoreException`
- State change callbacks for observability
- Thread-safe implementation with atomic operations
