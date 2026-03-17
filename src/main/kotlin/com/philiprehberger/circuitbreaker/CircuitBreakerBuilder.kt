package com.philiprehberger.circuitbreaker

/**
 * Creates a [CircuitBreaker] using a builder DSL.
 *
 * ```kotlin
 * val breaker = circuitBreaker("my-service") {
 *     failureThreshold = 5
 *     successThreshold = 2
 *     resetTimeout = 30.seconds
 *     onStateChange { from, to -> println("$from -> $to") }
 * }
 * ```
 *
 * @param name A descriptive name for the circuit breaker.
 * @param block Configuration block for the circuit breaker.
 * @return A configured [CircuitBreaker] instance.
 */
fun circuitBreaker(name: String, block: CircuitBreakerConfig.Builder.() -> Unit): CircuitBreaker {
    val config = CircuitBreakerConfig.Builder().apply(block).build()
    return CircuitBreaker(name, config)
}
