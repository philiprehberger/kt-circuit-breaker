package com.philiprehberger.circuitbreaker

/**
 * Represents the possible states of a circuit breaker.
 */
public enum class CircuitState {
    /** Circuit is closed; requests flow through normally. */
    CLOSED,

    /** Circuit is open; requests are immediately rejected. */
    OPEN,

    /** Circuit is half-open; a limited number of requests are allowed through to test recovery. */
    HALF_OPEN,
}
