/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.jagoan.keyboard.titan2.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debouncer utility to reduce frequent updates and save battery.
 * 
 * When an action is triggered multiple times in quick succession, only the last
 * invocation will be executed after the specified delay has elapsed.
 * 
 * This is particularly useful for:
 * - Notification updates (avoid spamming notification manager)
 * - UI updates that trigger frequently
 * - Network requests that should wait for user to finish typing
 * 
 * Usage:
 *   val debouncer = Debouncer(scope, delayMs = 100)
 *   debouncer.debounce {
 *     updateNotification()  // This will only run after 100ms of no new calls
 *   }
 */
class Debouncer(
    private val scope: CoroutineScope,
    private val delayMs: Long = 100L
) {
    private var debounceJob: Job? = null
    
    /**
     * Execute the given action after the debounce delay.
     * If called again before the delay expires, the previous action is cancelled
     * and the timer is reset.
     */
    fun debounce(action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            action()
        }
    }
    
    /**
     * Cancel any pending debounced action.
     */
    fun cancel() {
        debounceJob?.cancel()
        debounceJob = null
    }
    
    /**
     * Check if there's a pending action.
     */
    fun isPending(): Boolean = debounceJob?.isActive == true
}

/**
 * Throttler utility to limit how often an action can be executed.
 * 
 * Unlike debouncing, throttling ensures the action runs at regular intervals
 * even if triggered more frequently. The first call executes immediately,
 * subsequent calls within the throttle window are ignored.
 * 
 * Useful for:
 * - Rate-limiting API calls
 * - Scroll event handlers
 * - Frequent sensor updates
 * 
 * Usage:
 *   val throttler = Throttler(scope, intervalMs = 500)
 *   throttler.throttle {
 *     performExpensiveOperation()  // Runs at most once per 500ms
 *   }
 */
class Throttler(
    private val scope: CoroutineScope,
    private val intervalMs: Long = 500L
) {
    private var lastExecutionTime = 0L
    private var throttleJob: Job? = null
    
    /**
     * Execute the given action, but not more frequently than the specified interval.
     */
    fun throttle(action: suspend () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastExecution = currentTime - lastExecutionTime
        
        if (timeSinceLastExecution >= intervalMs) {
            lastExecutionTime = currentTime
            throttleJob?.cancel()
            throttleJob = scope.launch {
                action()
            }
        }
    }
    
    /**
     * Reset the throttle timer, allowing immediate execution on next call.
     */
    fun reset() {
        lastExecutionTime = 0L
        throttleJob?.cancel()
        throttleJob = null
    }
    
    /**
     * Check if an action is currently executing.
     */
    fun isExecuting(): Boolean = throttleJob?.isActive == true
}

/**
 * Combined debounce + throttle utility.
 * Ensures actions run at most once per throttle interval, with debouncing
 * to wait for a pause in rapid-fire calls.
 * 
 * Usage:
 *   val limiter = RateLimiter(scope, debounceMs = 100, throttleMs = 1000)
 *   limiter.limit {
 *     updateUI()  // Debounced for 100ms, but runs at least once per 1000ms
 *   }
 */
class RateLimiter(
    private val scope: CoroutineScope,
    private val debounceMs: Long = 100L,
    private val throttleMs: Long = 1000L
) {
    private val debouncer = Debouncer(scope, debounceMs)
    private var lastExecutionTime = 0L
    private var hasPendingAction = false
    private var pendingAction: (suspend () -> Unit)? = null
    
    /**
     * Execute the action with both debouncing and throttling.
     */
    fun limit(action: suspend () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastExecution = currentTime - lastExecutionTime
        
        // If throttle interval has passed, execute immediately
        if (timeSinceLastExecution >= throttleMs) {
            lastExecutionTime = currentTime
            debouncer.cancel()
            scope.launch { action() }
        } else {
            // Otherwise, debounce the action
            pendingAction = action
            hasPendingAction = true
            debouncer.debounce {
                if (hasPendingAction) {
                    lastExecutionTime = System.currentTimeMillis()
                    hasPendingAction = false
                    pendingAction?.invoke()
                    pendingAction = null
                }
            }
        }
    }
    
    /**
     * Cancel any pending action.
     */
    fun cancel() {
        debouncer.cancel()
        hasPendingAction = false
        pendingAction = null
    }
}