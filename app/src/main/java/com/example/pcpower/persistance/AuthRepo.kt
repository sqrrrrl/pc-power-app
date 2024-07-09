package com.example.pcpower.persistance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pcpower.model.Token
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepo(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("token")
    private val TOKEN_EXPIRATION_KEY = longPreferencesKey("expiration")

    suspend fun saveToken(token: Token){
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token.token
            preferences[TOKEN_EXPIRATION_KEY] = ZonedDateTime.parse(token.expire)
                .withZoneSameInstant(ZoneId.systemDefault()).toEpochSecond()
        }
    }

    suspend fun getToken(): String{
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY] ?: ""
    }
}