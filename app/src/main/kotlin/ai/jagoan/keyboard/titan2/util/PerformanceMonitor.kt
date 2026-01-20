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

import android.os.Debug
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance monitoring utility for establishing baselines and measuring improvements.
 * 
 * Features:
 * - Measure operation latency
 * - Track memory allocations
 * - Monitor GC frequency
 * - Calculate percentiles (p50, p95, p99)
 * - Export metrics for analysis
 * 
 * Usage:
 *   // Measure operation latency
 *   PerformanceMonitor.measure("key_event") {
 *     handleKeyEvent()
 *   }
 *   
 *   // Check memory before/after
 *   val snapshot = PerformanceMonitor.takeMemorySnapshot("operation_name")
 *   performOperation()
 *   PerformanceMonitor.recordMemoryDelta(snapshot)
 *   
 *   // Get statistics
 *   val stats = PerformanceMonitor.getStats("key_event")
 */
object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    
    // Enable/disable monitoring at runtime
    @Volatile
    var enabled = false
    
    // Store latency measurements (operation name -> list of durations in ms)
    private val latencyData = ConcurrentHashMap<String, MutableList<Long>>()
    
    // Store memory snapshots
    private val memoryData = ConcurrentHashMap<String, MutableList<MemoryDelta>>()
    
    // Store GC counts
    private val gcCountStart = AtomicLong(0)
    private var lastGcCount = 0L
    
    /**
     * Memory snapshot for delta calculation
     */
    data class MemorySnapshot(
        val operationName: String,
        val timestamp: Long,
        val nativeHeapSize: Long,
        val nativeHeapAllocated: Long,
        val nativeHeapFree: Long
    )
    
    /**
     * Memory delta between snapshots
     */
    data class MemoryDelta(
        val operationName: String,
        val allocatedBytes: Long,
        val durationMs: Long
    )
    
    /**
     * Performance statistics for an operation
     */
    data class PerformanceStats(
        val operationName: String,
        val sampleCount: Int,
        val minMs: Long,
        val maxMs: Long,
        val avgMs: Double,
        val p50Ms: Long,
        val p95Ms: Long,
        val p99Ms: Long,
        val totalMemoryAllocatedBytes: Long,
        val avgMemoryPerCallBytes: Long
    )
    
    /**
     * Initialize performance monitoring
     */
    fun init() {
        gcCountStart.set(Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull() ?: 0)
        lastGcCount = gcCountStart.get()
        enabled = true
        LazyLog.i(TAG) { "PerformanceMonitor initialized" }
    }
    
    /**
     * Measure the execution time of a block of code
     */
    inline fun <T> measure(operationName: String, block: () -> T): T {
        if (!enabled) return block()
        
        val startTime = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            val endTime = SystemClock.elapsedRealtimeNanos()
            val durationMs = (endTime - startTime) / 1_000_000 // Convert to ms
            recordLatency(operationName, durationMs)
        }
    }
    
    /**
     * Take a memory snapshot before an operation
     */
    fun takeMemorySnapshot(operationName: String): MemorySnapshot {
        return MemorySnapshot(
            operationName = operationName,
            timestamp = SystemClock.elapsedRealtime(),
            nativeHeapSize = Debug.getNativeHeapSize(),
            nativeHeapAllocated = Debug.getNativeHeapAllocatedSize(),
            nativeHeapFree = Debug.getNativeHeapFreeSize()
        )
    }
    
    /**
     * Record memory delta since snapshot
     */
    fun recordMemoryDelta(snapshot: MemorySnapshot) {
        if (!enabled) return
        
        val currentAllocated = Debug.getNativeHeapAllocatedSize()
        val delta = currentAllocated - snapshot.nativeHeapAllocated
        val duration = SystemClock.elapsedRealtime() - snapshot.timestamp
        
        val memoryDelta = MemoryDelta(
            operationName = snapshot.operationName,
            allocatedBytes = delta,
            durationMs = duration
        )
        
        memoryData.getOrPut(snapshot.operationName) { mutableListOf() }.add(memoryDelta)
    }
    
    /**
     * Record a latency measurement
     */
    @PublishedApi
    internal fun recordLatency(operationName: String, durationMs: Long) {
        latencyData.getOrPut(operationName) { mutableListOf() }.add(durationMs)
    }
    
    /**
     * Check if GC occurred since last check
     */
    fun checkGcOccurred(): Boolean {
        if (!enabled) return false
        
        val currentGcCount = Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull() ?: 0
        val gcOccurred = currentGcCount > lastGcCount
        lastGcCount = currentGcCount
        return gcOccurred
    }
    
    /**
     * Get performance statistics for an operation
     */
    fun getStats(operationName: String): PerformanceStats? {
        val latencies = latencyData[operationName] ?: return null
        if (latencies.isEmpty()) return null
        
        val sorted = latencies.sorted()
        val count = sorted.size
        
        val min = sorted.first()
        val max = sorted.last()
        val avg = sorted.average()
        
        val p50 = sorted[count * 50 / 100]
        val p95 = sorted[count * 95 / 100]
        val p99 = sorted[count * 99 / 100]
        
        val memoryDeltas = memoryData[operationName] ?: emptyList()
        val totalMemory = memoryDeltas.sumOf { it.allocatedBytes }
        val avgMemory = if (memoryDeltas.isNotEmpty()) totalMemory / memoryDeltas.size else 0L
        
        return PerformanceStats(
            operationName = operationName,
            sampleCount = count,
            minMs = min,
            maxMs = max,
            avgMs = avg,
            p50Ms = p50,
            p95Ms = p95,
            p99Ms = p99,
            totalMemoryAllocatedBytes = totalMemory,
            avgMemoryPerCallBytes = avgMemory
        )
    }
    
    /**
     * Get all tracked operation names
     */
    fun getAllOperationNames(): Set<String> {
        return latencyData.keys.toSet() + memoryData.keys.toSet()
    }
    
    /**
     * Print statistics to log
     */
    fun printStats() {
        if (!enabled) {
            Log.i(TAG, "Performance monitoring is disabled")
            return
        }
        
        Log.i(TAG, "=== Performance Statistics ===")
        
        getAllOperationNames().sorted().forEach { operationName ->
            val stats = getStats(operationName)
            if (stats != null) {
                Log.i(TAG, """
                    |Operation: ${stats.operationName}
                    |  Samples: ${stats.sampleCount}
                    |  Min: ${stats.minMs}ms
                    |  Avg: ${"%.2f".format(stats.avgMs)}ms
                    |  P50: ${stats.p50Ms}ms
                    |  P95: ${stats.p95Ms}ms
                    |  P99: ${stats.p99Ms}ms
                    |  Max: ${stats.maxMs}ms
                    |  Memory: ${formatBytes(stats.totalMemoryAllocatedBytes)} total, ${formatBytes(stats.avgMemoryPerCallBytes)} avg
                """.trimMargin())
            }
        }
        
        val totalGcs = Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull() ?: 0
        val gcsSinceStart = totalGcs - gcCountStart.get()
        Log.i(TAG, "GC count since monitoring started: $gcsSinceStart")
        Log.i(TAG, "===============================")
    }
    
    /**
     * Export statistics as CSV string
     */
    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("operation,samples,min_ms,avg_ms,p50_ms,p95_ms,p99_ms,max_ms,total_memory_bytes,avg_memory_bytes")
        
        getAllOperationNames().sorted().forEach { operationName ->
            val stats = getStats(operationName) ?: return@forEach
            sb.appendLine(
                "${stats.operationName}," +
                "${stats.sampleCount}," +
                "${stats.minMs}," +
                "${"%.2f".format(stats.avgMs)}," +
                "${stats.p50Ms}," +
                "${stats.p95Ms}," +
                "${stats.p99Ms}," +
                "${stats.maxMs}," +
                "${stats.totalMemoryAllocatedBytes}," +
                "${stats.avgMemoryPerCallBytes}"
            )
        }
        
        return sb.toString()
    }
    
    /**
     * Clear all collected data
     */
    fun clear() {
        latencyData.clear()
        memoryData.clear()
        gcCountStart.set(Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull() ?: 0)
        lastGcCount = gcCountStart.get()
        LazyLog.i(TAG) { "Performance data cleared" }
    }
    
    /**
     * Reset and restart monitoring
     */
    fun reset() {
        clear()
        init()
    }
    
    /**
     * Format bytes in human-readable format
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${"%.2f".format(bytes / 1024.0)}KB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0))}MB"
        }
    }
    
    /**
     * Enable performance monitoring
     */
    fun enable() {
        if (!enabled) {
            init()
        }
    }
    
    /**
     * Disable performance monitoring
     */
    fun disable() {
        enabled = false
        LazyLog.i(TAG) { "PerformanceMonitor disabled" }
    }
}

/**
 * Extension function for easy performance measurement
 */
inline fun <T> measurePerformance(operationName: String, block: () -> T): T {
    return PerformanceMonitor.measure(operationName, block)
}