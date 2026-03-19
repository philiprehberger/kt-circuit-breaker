package com.philiprehberger.circuitbreaker

/**
 * Thrown when a request is rejected because the circuit breaker is in the [CircuitState.OPEN] state.
 *
 * @param message A description of why the circuit is open.
 */
public class CircuitOpenException(message: String) : RuntimeException(message)
