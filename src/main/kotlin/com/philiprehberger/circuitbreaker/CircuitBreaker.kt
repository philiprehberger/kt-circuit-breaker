package com.philiprehberger.circuitbreaker

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * A coroutine-aware circuit breaker that protects downstream services from cascading failures.
 *
 * State transitions:
 * - **CLOSED -> OPEN**: After [CircuitBreakerConfig.failureThreshold] consecutive failures.
 * - **OPEN -> HALF_OPEN**: After [CircuitBreakerConfig.resetTimeout] has elapsed.
 * - **HALF_OPEN -> CLOSED**: After [CircuitBreakerConfig.successThreshold] consecutive successes.
 * - **HALF_OPEN -> OPEN**: On any failure in half-open state.
 *
 * @property name A descriptive name for this circuit breaker (used in exception messages).
 * @property config The configuration for this circuit breaker.
 */
public class CircuitBreaker(
    public val name: String,
    private val config: CircuitBreakerConfig,
) {
    private val stateRef = AtomicReference(CircuitState.CLOSED)
    private val failureCount = AtomicInteger(0)
    private val successCount = AtomicInteger(0)
    private val openedAt = AtomicLong(0L)

    /** The current state of the circuit breaker. */
    public val state: CircuitState get() = stateRef.get()

    /**
     * Executes [block] if the circuit allows it.
     *
     * @param T The return type of the block.
     * @param block The suspending block to execute.
     * @return The result of [block].
     * @throws CircuitOpenException If the circuit is open and the reset timeout has not elapsed.
     * @throws Throwable Any exception thrown by [block] (after recording the failure).
     */
    public suspend fun <T> execute(block: suspend () -> T): T {
        val currentState = checkState()
        if (currentState == CircuitState.OPEN) {
            throw CircuitOpenException("Circuit breaker '$name' is OPEN")
        }

        return try {
            val result = block()
            onSuccess()
            result
        } catch (e: Throwable) {
            if (shouldRecord(e)) {
                onFailure()
            }
            throw e
        }
    }

    private fun checkState(): CircuitState {
        val current = stateRef.get()
        if (current == CircuitState.OPEN) {
            val elapsed = System.currentTimeMillis() - openedAt.get()
            if (elapsed >= config.resetTimeout.inWholeMilliseconds) {
                transitionTo(CircuitState.HALF_OPEN)
                return CircuitState.HALF_OPEN
            }
        }
        return current
    }

    private fun onSuccess() {
        when (stateRef.get()) {
            CircuitState.HALF_OPEN -> {
                val count = successCount.incrementAndGet()
                if (count >= config.successThreshold) {
                    transitionTo(CircuitState.CLOSED)
                }
            }
            CircuitState.CLOSED -> {
                failureCount.set(0)
            }
            else -> {}
        }
    }

    private fun onFailure() {
        when (stateRef.get()) {
            CircuitState.HALF_OPEN -> {
                transitionTo(CircuitState.OPEN)
            }
            CircuitState.CLOSED -> {
                val count = failureCount.incrementAndGet()
                if (count >= config.failureThreshold) {
                    transitionTo(CircuitState.OPEN)
                }
            }
            else -> {}
        }
    }

    private fun transitionTo(newState: CircuitState) {
        val oldState = stateRef.getAndSet(newState)
        if (oldState != newState) {
            when (newState) {
                CircuitState.OPEN -> {
                    openedAt.set(System.currentTimeMillis())
                    failureCount.set(0)
                    successCount.set(0)
                }
                CircuitState.CLOSED -> {
                    failureCount.set(0)
                    successCount.set(0)
                }
                CircuitState.HALF_OPEN -> {
                    successCount.set(0)
                }
            }
            config.onStateChange(oldState, newState)
        }
    }

    private fun shouldRecord(e: Throwable): Boolean {
        if (config.ignoreExceptions.any { it.isInstance(e) }) return false
        if (config.recordExceptions.isEmpty()) return true
        return config.recordExceptions.any { it.isInstance(e) }
    }
}
