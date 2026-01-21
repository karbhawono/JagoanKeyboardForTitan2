/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.di to ai.jagoan.keyboard.titan2.di
 * - Updated all package references in provides methods
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

package ai.jagoan.keyboard.titan2.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ai.jagoan.keyboard.titan2.data.datastore.ShortcutsDataStore
import ai.jagoan.keyboard.titan2.data.repository.DictionaryRepositoryImpl
import ai.jagoan.keyboard.titan2.data.repository.SettingsRepositoryImpl
import ai.jagoan.keyboard.titan2.data.repository.ShortcutRepositoryImpl
import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import ai.jagoan.keyboard.titan2.domain.repository.SettingsRepository
import ai.jagoan.keyboard.titan2.domain.repository.ShortcutRepository
import ai.jagoan.keyboard.titan2.engine.AutocorrectEngine
import ai.jagoan.keyboard.titan2.engine.AutocorrectManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_settings")

/**
 * Hilt module providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideShortcutsDataStore(
        @ApplicationContext context: Context
    ): ShortcutsDataStore {
        return ShortcutsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideShortcutRepository(
        shortcutsDataStore: ShortcutsDataStore
    ): ShortcutRepository {
        return ShortcutRepositoryImpl(shortcutsDataStore)
    }

    @Provides
    @Singleton
    fun provideDictionaryRepository(
        @ApplicationContext context: Context
    ): DictionaryRepository {
        return DictionaryRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAutocorrectEngine(
        dictionaryRepository: DictionaryRepository
    ): AutocorrectEngine {
        return AutocorrectEngine(dictionaryRepository)
    }

    @Provides
    @Singleton
    fun provideAutocorrectManager(
        dictionaryRepository: DictionaryRepository,
        autocorrectEngine: AutocorrectEngine
    ): AutocorrectManager {
        return AutocorrectManager(dictionaryRepository, autocorrectEngine)
    }
}
