package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val READER_MODE = intPreferencesKey("reader_mode")

    }

    val currentReaderMode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[READER_MODE] ?: 0
    }

    suspend fun saveReaderMode(mode: Int)
    {
        dataStore.edit { preferences ->
            preferences[READER_MODE] = mode
        }
    }
}