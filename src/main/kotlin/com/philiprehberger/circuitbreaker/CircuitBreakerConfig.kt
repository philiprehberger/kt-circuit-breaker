package com.philiprehberger.circuitbreaker

import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for a [CircuitBreaker].
 *
 * @property failureThreshold Number of consecutive failures before the circuit opens.
 * @property successThreshold Number of consecutive successes in half-open state to close the circuit.
 * @property resetTimeout Duration to wait in the open state before transitioning to half-open.
 * @property recordExceptions Exception types that count as failures. Empty means all exceptions are recorded.
 * @property ignoreExceptions Exception types that are never counted as failures.
 * @property onStateChange Callback invoked when the circuit state changes.
 */
data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,
    val successThreshold: Int = 1,
    val resetTimeout: Duration = 30.seconds,
    val recordExceptions: Set<KClass<out Throwable>> = emptySet(),
    val ignoreExceptions: Set<KClass<out Throwable>> = emptySet(),
    val onStateChange: (CircuitState, CircuitState) -> Unit = { _, _ -> },
) {

    /**
     * Builder for constructing a [CircuitBreakerConfig] via DSL.
     */
    class Builder {
        /** Number of consecutive failures before the circuit opens. */
        var failureThreshold: Int = 5

        /** Number of consecutive successes in half-open state to close the circuit. */
        var successThreshold: Int = 1

        /** Duration to wait in the open state before transitioning to half-open. */
        var resetTimeout: Duration = 30.seconds

        @PublishedApi internal val recordExceptions = mutableSetOf<KClass<out Throwable>>()
        @PublishedApi internal val ignoreExceptions = mutableSetOf<KClass<out Throwable>>()
        private var onStateChange: (CircuitState, CircuitState) -> Unit = { _, _ -> }

        /**
         * Registers an exception type to be counted as a failure.
         * If no types are registered, all exceptions count as failures.
         */
        inline fun <reified T : Throwable> recordException() {
            recordExceptions.add(T::class)
        }

        /**
         * Registers an exception type to be ignored (never counted as a failure).
         */
        inline fun <reified T : Throwable> ignoreException() {
            ignoreExceptions.add(T::class)
        }

        /**
         * Sets a callback invoked when the circuit state changes.
         *
         * @param callback Receives the old state and the new state.
         */
        fun onStateChange(callback: (from: CircuitState, to: CircuitState) -> Unit) {
            this.onStateChange = callback
        }

        internal fun build(): CircuitBreakerConfig = CircuitBreakerConfig(
            failureThreshold = failureThreshold,
            successThreshold = successThreshold,
            resetTimeout = resetTimeout,
            recordExceptions = recordExceptions.toSet(),
            ignoreExceptions = ignoreExceptions.toSet(),
            onStateChange = onStateChange,
        )
    }
}
