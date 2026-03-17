package com.philiprehberger.circuitbreaker

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CircuitBreakerTest {

    @Test
    fun `starts in closed state`() {
        val cb = circuitBreaker("test") {
            failureThreshold = 3
        }
        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun `opens after failure threshold`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 3
            resetTimeout = 10000.milliseconds
        }
        repeat(3) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }
        assertEquals(CircuitState.OPEN, cb.state)
    }

    @Test
    fun `rejects requests when open`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 1
            resetTimeout = 60000.milliseconds
        }
        runCatching { cb.execute { throw RuntimeException("fail") } }
        assertEquals(CircuitState.OPEN, cb.state)
        assertFailsWith<CircuitOpenException> {
            cb.execute { "should not run" }
        }
    }

    @Test
    fun `transitions to half-open after timeout`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 1
            resetTimeout = 1.milliseconds
        }
        runCatching { cb.execute { throw RuntimeException("fail") } }
        assertEquals(CircuitState.OPEN, cb.state)

        // Wait for reset timeout
        Thread.sleep(5)

        // Next call should transition to half-open and execute
        val result = cb.execute { "recovered" }
        assertEquals("recovered", result)
        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun `closes after success threshold in half-open`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 1
            successThreshold = 2
            resetTimeout = 1.milliseconds
        }
        runCatching { cb.execute { throw RuntimeException("fail") } }
        assertEquals(CircuitState.OPEN, cb.state)

        Thread.sleep(5)

        // First success transitions to half-open, stays half-open
        cb.execute { "ok" }
        assertEquals(CircuitState.HALF_OPEN, cb.state)

        // Second success closes the circuit
        cb.execute { "ok" }
        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun `re-opens on failure in half-open state`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 1
            successThreshold = 2
            resetTimeout = 1.milliseconds
        }
        runCatching { cb.execute { throw RuntimeException("fail") } }
        assertEquals(CircuitState.OPEN, cb.state)

        Thread.sleep(5)

        // Transition to half-open with a failure
        runCatching { cb.execute { throw RuntimeException("fail again") } }
        assertEquals(CircuitState.OPEN, cb.state)
    }

    @Test
    fun `state change callback is invoked`() = runTest {
        val transitions = mutableListOf<Pair<CircuitState, CircuitState>>()
        val cb = circuitBreaker("test") {
            failureThreshold = 1
            resetTimeout = 1.milliseconds
            onStateChange { from, to -> transitions.add(from to to) }
        }
        runCatching { cb.execute { throw RuntimeException("fail") } }
        assertEquals(listOf(CircuitState.CLOSED to CircuitState.OPEN), transitions)

        Thread.sleep(5)
        cb.execute { "ok" }

        assertEquals(3, transitions.size)
        assertEquals(CircuitState.OPEN to CircuitState.HALF_OPEN, transitions[1])
        assertEquals(CircuitState.HALF_OPEN to CircuitState.CLOSED, transitions[2])
    }

    @Test
    fun `successful calls reset failure count`() = runTest {
        val cb = circuitBreaker("test") {
            failureThreshold = 3
            resetTimeout = 60000.milliseconds
        }
        // Two failures
        repeat(2) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }
        assertEquals(CircuitState.CLOSED, cb.state)

        // One success resets counter
        cb.execute { "ok" }

        // Two more failures should not open (count reset)
        repeat(2) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }
        assertEquals(CircuitState.CLOSED, cb.state)
    }
}
