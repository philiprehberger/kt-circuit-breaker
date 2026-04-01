package com.philiprehberger.circuitbreaker

/**
 * A snapshot of the circuit breaker metrics.
 *
 * @property state The current circuit state.
 * @property totalFailures The number of recorded failures since last state reset.
 * @property totalSuccesses The number of recorded successes since last state reset.
 * @property consecutiveFailures The current consecutive failure count.
 */
public data class CircuitMetrics(
    public val state: CircuitState,
    public val totalFailures: Int,
    public val totalSuccesses: Int,
    public val consecutiveFailures: Int,
)
