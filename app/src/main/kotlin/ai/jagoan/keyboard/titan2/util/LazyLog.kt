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

import android.util.Log

/**
 * Lazy logging utility to avoid string construction overhead when logging is disabled.
 * 
 * This utility uses lambda functions to defer string construction until it's confirmed
 * that the log level is enabled, preventing unnecessary allocations and string operations
 * in production builds or when log levels are filtered.
 * 
 * Usage:
 *   LazyLog.d(TAG) { "Expensive operation: ${computeExpensiveValue()}" }
 *   LazyLog.v(TAG) { "Debug info: count=$count, state=$state" }
 */
object LazyLog {
    
    /**
     * Minimum log level to output. Set to Log.ASSERT to disable all logging.
     * Can be configured at runtime based on build type or user settings.
     */
    @Volatile
    var minLogLevel: Int = if (android.os.Build.TYPE == "user") {
        Log.INFO  // Production builds: only INFO and above
    } else {
        Log.VERBOSE  // Debug builds: all logs
    }
    
    /**
     * Send a VERBOSE log message.
     * The message will only be constructed if VERBOSE logging is enabled.
     */
    inline fun v(tag: String, messageProvider: () -> String) {
        if (minLogLevel <= Log.VERBOSE && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, messageProvider())
        }
    }
    
    /**
     * Send a VERBOSE log message with throwable.
     */
    inline fun v(tag: String, throwable: Throwable, messageProvider: () -> String) {
        if (minLogLevel <= Log.VERBOSE && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, messageProvider(), throwable)
        }
    }
    
    /**
     * Send a DEBUG log message.
     * The message will only be constructed if DEBUG logging is enabled.
     */
    inline fun d(tag: String, messageProvider: () -> String) {
        if (minLogLevel <= Log.DEBUG && Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, messageProvider())
        }
    }
    
    /**
     * Send a DEBUG log message with throwable.
     */
    inline fun d(tag: String, throwable: Throwable, messageProvider: () -> String) {
        if (minLogLevel <= Log.DEBUG && Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, messageProvider(), throwable)
        }
    }
    
    /**
     * Send an INFO log message.
     * The message will only be constructed if INFO logging is enabled.
     */
    inline fun i(tag: String, messageProvider: () -> String) {
        if (minLogLevel <= Log.INFO && Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, messageProvider())
        }
    }
    
    /**
     * Send an INFO log message with throwable.
     */
    inline fun i(tag: String, throwable: Throwable, messageProvider: () -> String) {
        if (minLogLevel <= Log.INFO && Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, messageProvider(), throwable)
        }
    }
    
    /**
     * Send a WARN log message.
     * The message will only be constructed if WARN logging is enabled.
     */
    inline fun w(tag: String, messageProvider: () -> String) {
        if (minLogLevel <= Log.WARN && Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, messageProvider())
        }
    }
    
    /**
     * Send a WARN log message with throwable.
     */
    inline fun w(tag: String, throwable: Throwable, messageProvider: () -> String) {
        if (minLogLevel <= Log.WARN && Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, messageProvider(), throwable)
        }
    }
    
    /**
     * Send an ERROR log message.
     * The message will only be constructed if ERROR logging is enabled.
     */
    inline fun e(tag: String, messageProvider: () -> String) {
        if (minLogLevel <= Log.ERROR && Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, messageProvider())
        }
    }
    
    /**
     * Send an ERROR log message with throwable.
     */
    inline fun e(tag: String, throwable: Throwable, messageProvider: () -> String) {
        if (minLogLevel <= Log.ERROR && Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, messageProvider(), throwable)
        }
    }
    
    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Always outputs regardless of log level.
     */
    inline fun wtf(tag: String, messageProvider: () -> String) {
        Log.wtf(tag, messageProvider())
    }
    
    /**
     * What a Terrible Failure: Report an exception that should never happen.
     */
    inline fun wtf(tag: String, throwable: Throwable, messageProvider: () -> String) {
        Log.wtf(tag, messageProvider(), throwable)
    }
    
    /**
     * Configure minimum log level at runtime.
     * Useful for enabling verbose logging in debug builds or specific scenarios.
     */
    fun configure(level: Int) {
        minLogLevel = level
    }
    
    /**
     * Disable all logging except WTF.
     */
    fun disableLogging() {
        minLogLevel = Log.ASSERT
    }
    
    /**
     * Enable all logging levels.
     */
    fun enableAllLogging() {
        minLogLevel = Log.VERBOSE
    }
}