package com.example.urbango.components

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore

object PreferencesKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
}

// Create DataStore instance in Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
