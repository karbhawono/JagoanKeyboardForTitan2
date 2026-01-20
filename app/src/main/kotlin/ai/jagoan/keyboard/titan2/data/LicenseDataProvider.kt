/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * This file provides structured license information for all third-party
 * libraries and frameworks used in Jagoan Keyboard for Titan 2.
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

package ai.jagoan.keyboard.titan2.data

data class LibraryInfo(
    val name: String,
    val version: String? = null,
    val purpose: String,
    val license: String,
    val url: String? = null,
    val groupId: String? = null
)

object LicenseDataProvider {
    
    fun getBuildTools(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Android Gradle Plugin",
            version = "8.3.0",
            purpose = "Android build system",
            license = "Apache License 2.0",
            url = "https://developer.android.com/studio/releases/gradle-plugin"
        ),
        LibraryInfo(
            name = "Kotlin Gradle Plugin",
            version = "1.9.22",
            purpose = "Kotlin language support for Android",
            license = "Apache License 2.0",
            url = "https://kotlinlang.org/"
        ),
        LibraryInfo(
            name = "Kotlin Symbol Processing (KSP)",
            version = "1.9.22-1.0.17",
            purpose = "Annotation processing for Kotlin",
            license = "Apache License 2.0",
            url = "https://github.com/google/ksp"
        )
    )
    
    fun getCoreLibraries(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Kotlin Standard Library",
            groupId = "org.jetbrains.kotlin:kotlin-stdlib",
            version = "1.9.22",
            purpose = "Kotlin standard library",
            license = "Apache License 2.0",
            url = "https://kotlinlang.org/"
        ),
        LibraryInfo(
            name = "Kotlinx Serialization JSON",
            groupId = "org.jetbrains.kotlinx:kotlinx-serialization-json",
            version = "1.6.2",
            purpose = "JSON serialization/deserialization",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.serialization"
        )
    )
    
    fun getAndroidXLibraries(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "AndroidX Core KTX",
            groupId = "androidx.core:core-ktx",
            version = "1.12.0",
            purpose = "Core Android utilities and Kotlin extensions",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/core"
        ),
        LibraryInfo(
            name = "AndroidX Lifecycle Runtime KTX",
            groupId = "androidx.lifecycle:lifecycle-runtime-ktx",
            version = "2.7.0",
            purpose = "Lifecycle-aware components",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
        ),
        LibraryInfo(
            name = "AndroidX Lifecycle ViewModel KTX",
            groupId = "androidx.lifecycle:lifecycle-viewmodel-ktx",
            version = "2.7.0",
            purpose = "ViewModel components with Kotlin extensions",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
        ),
        LibraryInfo(
            name = "AndroidX Lifecycle Runtime Compose",
            groupId = "androidx.lifecycle:lifecycle-runtime-compose",
            version = "2.7.0",
            purpose = "Lifecycle integration with Jetpack Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
        ),
        LibraryInfo(
            name = "AndroidX Activity Compose",
            groupId = "androidx.activity:activity-compose",
            version = "1.8.2",
            purpose = "Activity integration with Jetpack Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/activity"
        ),
        LibraryInfo(
            name = "AndroidX DataStore Preferences",
            groupId = "androidx.datastore:datastore-preferences",
            version = "1.0.0",
            purpose = "Data storage solution using key-value pairs",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/datastore"
        )
    )
    
    fun getJetpackCompose(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Compose BOM (Bill of Materials)",
            groupId = "androidx.compose:compose-bom",
            version = "2024.02.00",
            purpose = "Unified version management for Compose libraries",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose/bom"
        ),
        LibraryInfo(
            name = "Compose UI",
            groupId = "androidx.compose.ui:ui",
            purpose = "Core Compose UI framework",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        ),
        LibraryInfo(
            name = "Compose UI Graphics",
            groupId = "androidx.compose.ui:ui-graphics",
            purpose = "Graphics primitives for Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        ),
        LibraryInfo(
            name = "Compose UI Tooling Preview",
            groupId = "androidx.compose.ui:ui-tooling-preview",
            purpose = "Preview annotations for Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        ),
        LibraryInfo(
            name = "Compose Material3",
            groupId = "androidx.compose.material3:material3",
            purpose = "Material Design 3 components for Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/compose-material3"
        ),
        LibraryInfo(
            name = "Compose UI Tooling (Debug)",
            groupId = "androidx.compose.ui:ui-tooling",
            purpose = "Tools for debugging Compose layouts",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        )
    )
    
    fun getDependencyInjection(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Hilt Android",
            groupId = "com.google.dagger:hilt-android",
            version = "2.50",
            purpose = "Dependency injection framework for Android",
            license = "Apache License 2.0",
            url = "https://dagger.dev/hilt/"
        ),
        LibraryInfo(
            name = "Hilt Compiler",
            groupId = "com.google.dagger:hilt-compiler",
            version = "2.50",
            purpose = "Annotation processor for Hilt",
            license = "Apache License 2.0",
            url = "https://dagger.dev/hilt/"
        ),
        LibraryInfo(
            name = "Hilt Navigation Compose",
            groupId = "androidx.hilt:hilt-navigation-compose",
            version = "1.1.0",
            purpose = "Hilt integration with Compose Navigation",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/hilt"
        )
    )
    
    fun getCoroutines(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Kotlinx Coroutines Core",
            groupId = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
            version = "1.7.3",
            purpose = "Coroutines support for asynchronous programming",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        LibraryInfo(
            name = "Kotlinx Coroutines Android",
            groupId = "org.jetbrains.kotlinx:kotlinx-coroutines-android",
            version = "1.7.3",
            purpose = "Android-specific coroutines support",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        )
    )
    
    fun getTestingLibraries(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "JUnit Jupiter API",
            groupId = "org.junit.jupiter:junit-jupiter-api",
            version = "5.10.1",
            purpose = "JUnit 5 testing framework API",
            license = "Eclipse Public License 2.0",
            url = "https://junit.org/junit5/"
        ),
        LibraryInfo(
            name = "JUnit Jupiter Engine",
            groupId = "org.junit.jupiter:junit-jupiter-engine",
            version = "5.10.1",
            purpose = "JUnit 5 test engine",
            license = "Eclipse Public License 2.0",
            url = "https://junit.org/junit5/"
        ),
        LibraryInfo(
            name = "JUnit Vintage Engine",
            groupId = "org.junit.vintage:junit-vintage-engine",
            version = "5.10.1",
            purpose = "JUnit 4 compatibility with JUnit 5",
            license = "Eclipse Public License 2.0",
            url = "https://junit.org/junit5/"
        ),
        LibraryInfo(
            name = "MockK",
            groupId = "io.mockk:mockk",
            version = "1.13.9",
            purpose = "Mocking library for Kotlin",
            license = "Apache License 2.0",
            url = "https://mockk.io/"
        ),
        LibraryInfo(
            name = "Turbine",
            groupId = "app.cash.turbine:turbine",
            version = "1.0.0",
            purpose = "Testing library for Kotlin Flows",
            license = "Apache License 2.0",
            url = "https://github.com/cashapp/turbine"
        ),
        LibraryInfo(
            name = "Kotlinx Coroutines Test",
            groupId = "org.jetbrains.kotlinx:kotlinx-coroutines-test",
            version = "1.7.3",
            purpose = "Testing utilities for coroutines",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        LibraryInfo(
            name = "AndroidX Test Ext JUnit",
            groupId = "androidx.test.ext:junit",
            version = "1.1.5",
            purpose = "Android testing framework",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/test"
        ),
        LibraryInfo(
            name = "Compose UI Test JUnit4",
            groupId = "androidx.compose.ui:ui-test-junit4",
            purpose = "JUnit4 integration for Compose UI testing",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose/testing"
        ),
        LibraryInfo(
            name = "Compose UI Test Manifest",
            groupId = "androidx.compose.ui:ui-test-manifest",
            purpose = "Test manifest for Compose UI testing",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose/testing"
        ),
        LibraryInfo(
            name = "Google Truth",
            groupId = "com.google.truth:truth",
            version = "1.4.0",
            purpose = "Fluent assertion library for tests",
            license = "Apache License 2.0",
            url = "https://truth.dev/"
        )
    )
    
    fun getOthers(): List<LibraryInfo> = listOf(
        LibraryInfo(
            name = "Titan2Keyboard",
            version = "0.2.0",
            purpose = "Base Project Skeleton",
            license = "Apache License 2.0",
            url = "https://github.com/Divefire/titan2keyboard"
        )
    )
    
    fun getAllCategories(): Map<String, List<LibraryInfo>> {
        return mapOf(
            "Build Tools & Plugins" to getBuildTools(),
            "Core Libraries" to getCoreLibraries(),
            "AndroidX Libraries" to getAndroidXLibraries(),
            "Jetpack Compose" to getJetpackCompose(),
            "Dependency Injection" to getDependencyInjection(),
            "Coroutines" to getCoroutines(),
            "Testing Libraries" to getTestingLibraries(),
            "Others" to getOthers()
        )
    }
}
