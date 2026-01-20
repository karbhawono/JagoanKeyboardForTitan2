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

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Generic object pool for reusing objects to reduce allocations in hot paths.
 * Thread-safe implementation using ConcurrentLinkedQueue.
 * 
 * Usage:
 *   val pool = ObjectPool({ StringBuilder(64) }, maxSize = 10)
 *   val sb = pool.acquire()
 *   try {
 *     sb.append("text")
 *     // use StringBuilder
 *   } finally {
 *     pool.release(sb)
 *   }
 */
class ObjectPool<T : Any>(
    private val factory: () -> T,
    private val maxSize: Int = 10,
    private val reset: (T) -> Unit = {}
) {
    private val pool = ConcurrentLinkedQueue<T>()
    
    @Volatile
    private var createdCount = 0
    
    /**
     * Acquire an object from the pool, or create a new one if pool is empty.
     */
    fun acquire(): T {
        return pool.poll() ?: run {
            createdCount++
            factory()
        }
    }
    
    /**
     * Return an object to the pool for reuse.
     * The object will be reset before being added back to the pool.
     */
    fun release(obj: T) {
        if (pool.size < maxSize) {
            reset(obj)
            pool.offer(obj)
        }
    }
    
    /**
     * Execute a block with a pooled object, automatically releasing it afterwards.
     */
    inline fun <R> use(block: (T) -> R): R {
        val obj = acquire()
        try {
            return block(obj)
        } finally {
            release(obj)
        }
    }
    
    /**
     * Get current pool size (available objects).
     */
    fun size(): Int = pool.size
    
    /**
     * Get total number of objects created.
     */
    fun totalCreated(): Int = createdCount
    
    /**
     * Clear the pool, removing all cached objects.
     */
    fun clear() {
        pool.clear()
    }
}

/**
 * Pre-configured StringBuilder pool for text manipulation.
 * Automatically clears StringBuilder before returning to pool.
 */
object StringBuilderPool {
    @PublishedApi
    internal val pool = ObjectPool(
        factory = { StringBuilder(128) },
        maxSize = 20,
        reset = { it.clear() }
    )
    
    fun acquire(): StringBuilder = pool.acquire()
    
    fun release(sb: StringBuilder) = pool.release(sb)
    
    inline fun <R> use(block: (StringBuilder) -> R): R = pool.use(block)
}

/**
 * Extension function to clear StringBuilder (for compatibility).
 */
fun StringBuilder.clear(): StringBuilder {
    setLength(0)
    return this
}