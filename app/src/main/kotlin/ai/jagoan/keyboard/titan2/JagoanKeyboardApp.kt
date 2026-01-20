/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard to ai.jagoan.keyboard.titan2
 * - Renamed class from Titan2KeyboardApp to JagoanKeyboardApp
 * - Added LazyLog and PerformanceMonitor utilities integration
 * - Added performance monitoring initialization in debug builds
 * - Added debug logging for performance monitoring
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
 *
 */

package ai.jagoan.keyboard.titan2

import android.app.Application
import ai.jagoan.keyboard.titan2.domain.repository.ShortcutRepository
import ai.jagoan.keyboard.titan2.util.LazyLog
import ai.jagoan.keyboard.titan2.util.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Jagoan Keyboard for Titan 2
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class JagoanKeyboardApp : Application() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize performance monitoring in debug builds
        if (BuildConfig.DEBUG) {
            PerformanceMonitor.enable()
            LazyLog.i("JagoanKeyboardApp") { "Performance monitoring enabled for debug build" }
        }

        // Initialize default shortcuts if none exist
        applicationScope.launch {
            shortcutRepository.initializeDefaults()
        }
    }
}
